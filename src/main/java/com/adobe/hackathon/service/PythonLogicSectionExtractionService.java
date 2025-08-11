package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.ExtractedSection;
import com.adobe.hackathon.model.dto.SubsectionAnalysis;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PythonLogicSectionExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(PythonLogicSectionExtractionService.class);

    private static final int DEFAULT_TOP_N = 5;

    public List<ExtractedSection> extractTopHeadings(String jobDirectory,
                                                     String persona,
                                                     String jobToBeDone,
                                                     int topN) {
        List<ExtractedSection> extractedSections = new ArrayList<>();

        File pdfsDir = new File(jobDirectory, "PDFs");
        if (!pdfsDir.exists() || !pdfsDir.isDirectory()) {
            logger.warn("PDFs directory not found: {}", pdfsDir.getAbsolutePath());
            return extractedSections;
        }

        String queryText = buildQueryText(persona, jobToBeDone);
        List<File> pdfFiles = Optional.ofNullable(pdfsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf")))
                .map(Arrays::asList).orElseGet(ArrayList::new);

        for (File pdfFile : pdfFiles) {
            try {
                List<HeadingCandidate> candidates = extractHeadingsWithPageAndNextLine(pdfFile);
                if (candidates.isEmpty()) {
                    continue;
                }

                Map<String, Double> similarityByHeading = scoreHeadingsByQuery(queryText, candidates);

                List<HeadingCandidate> ranked = candidates.stream()
                        .sorted((a, b) -> Double.compare(
                                similarityByHeading.getOrDefault(b.headingText, 0.0),
                                similarityByHeading.getOrDefault(a.headingText, 0.0)))
                        .limit(Math.max(1, topN))
                        .collect(Collectors.toList());

                int rank = 1;
                for (HeadingCandidate candidate : ranked) {
                    ExtractedSection section = new ExtractedSection();
                    section.setDocument(pdfFile.getName());
                    section.setSectionTitle(candidate.headingText);
                    section.setImportanceRank(rank++);
                    section.setPageNumber(candidate.pageNumber);
                    extractedSections.add(section);
                }
            } catch (Exception e) {
                logger.error("Error processing PDF {}", pdfFile.getName(), e);
            }
        }

        return extractedSections;
    }

    public List<ExtractedSection> extractTopHeadings(String jobDirectory,
                                                     String persona,
                                                     String jobToBeDone) {
        return extractTopHeadings(jobDirectory, persona, jobToBeDone, DEFAULT_TOP_N);
    }

    public List<SubsectionAnalysis> extractSubsectionAnalysis(String jobDirectory,
                                                              List<ExtractedSection> topSections) {
        List<SubsectionAnalysis> analyses = new ArrayList<>();

        File pdfsDir = new File(jobDirectory, "PDFs");
        if (!pdfsDir.exists() || !pdfsDir.isDirectory()) {
            logger.warn("PDFs directory not found for subsection analysis: {}", pdfsDir.getAbsolutePath());
            return analyses;
        }

        Map<String, Map<SectionKey, String>> cache = new HashMap<>();

        for (ExtractedSection section : topSections) {
            try {
                File pdf = findPdfFile(pdfsDir, section.getDocument());
                if (pdf == null) {
                    logger.warn("PDF not found for subsection extraction: {}", section.getDocument());
                    continue;
                }

                Map<SectionKey, String> nextLineByHeading = cache.computeIfAbsent(
                        pdf.getAbsolutePath(), k -> buildNextLineLookup(pdf));

                SectionKey key = new SectionKey(section.getSectionTitle(), section.getPageNumber());
                String refinedText = nextLineByHeading.getOrDefault(key, "");

                SubsectionAnalysis analysis = new SubsectionAnalysis();
                analysis.setDocument(section.getDocument());
                analysis.setRefinedText(refinedText);
                analysis.setPageNumber(section.getPageNumber());
                analyses.add(analysis);
            } catch (Exception e) {
                logger.error("Error extracting subsection for {}", section.getDocument(), e);
            }
        }

        return analyses;
    }

    private String buildQueryText(String persona, String jobToBeDone) {
        String p = persona == null ? "" : persona.trim();
        String j = jobToBeDone == null ? "" : jobToBeDone.trim();
        return "Persona: " + p + ". Job to be done: " + j;
    }

    private List<HeadingCandidate> extractHeadingsWithPageAndNextLine(File pdfFile) throws IOException {
        List<HeadingCandidate> candidates = new ArrayList<>();
        Set<String> dedupe = new HashSet<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);
                List<String> lines = Arrays.stream(pageText.split("\n")).map(String::trim).collect(Collectors.toList());

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (isPotentialHeading(line)) {
                        String key = (line + "|" + page).toLowerCase(Locale.ROOT);
                        if (dedupe.add(key)) {
                            String nextLine = findFirstNonEmptyFollowingLine(lines, i + 1);
                            candidates.add(new HeadingCandidate(line, page, nextLine));
                        }
                    }
                }
            }
        }

        return candidates;
    }

    private Map<String, Double> scoreHeadingsByQuery(String query, List<HeadingCandidate> candidates) {
        Map<String, Integer> queryVector = toTermFrequencyVector(query);
        Map<String, Double> scores = new HashMap<>();

        for (HeadingCandidate candidate : candidates) {
            Map<String, Integer> headingVector = toTermFrequencyVector(candidate.headingText);
            double sim = cosineSimilarity(queryVector, headingVector);
            scores.put(candidate.headingText, sim);
        }

        return scores;
    }

    private Map<String, Integer> toTermFrequencyVector(String text) {
        Map<String, Integer> tf = new HashMap<>();
        if (text == null || text.isBlank()) return tf;
        String[] tokens = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\s]", " ")
                .split("\\s+");
        for (String token : tokens) {
            if (token.length() < 2) continue;
            tf.merge(token, 1, Integer::sum);
        }
        return tf;
    }

    private double cosineSimilarity(Map<String, Integer> a, Map<String, Integer> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> vocab = new HashSet<>();
        vocab.addAll(a.keySet());
        vocab.addAll(b.keySet());

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String term : vocab) {
            int va = a.getOrDefault(term, 0);
            int vb = b.getOrDefault(term, 0);
            dot += (double) va * vb;
            normA += (double) va * va;
            normB += (double) vb * vb;
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private boolean isPotentialHeading(String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        if (trimmed.length() <= 5 || trimmed.length() >= 100) return false;

        if (isAllUppercase(trimmed)) return true;
        if (isTitleCase(trimmed)) return true;
        if (startsWithNumberDot(trimmed)) return true;
        return false;
    }

    private boolean isAllUppercase(String s) {
        boolean hasAlpha = s.chars().anyMatch(Character::isLetter);
        return hasAlpha && s.equals(s.toUpperCase(Locale.ROOT));
    }

    private boolean isTitleCase(String s) {
        String[] parts = s.split("\\s+");
        int titleCased = 0;
        int checked = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!Character.isLetter(part.charAt(0))) continue;
            checked++;
            if (Character.isUpperCase(part.charAt(0))) titleCased++;
        }
        return checked > 0 && titleCased >= Math.max(1, checked - 2);
    }

    private boolean startsWithNumberDot(String s) {
        if (s.isEmpty()) return false;
        if (!Character.isDigit(s.charAt(0))) return false;
        int idx = s.indexOf('.');
        return idx >= 0 && idx <= 2;
    }

    private String findFirstNonEmptyFollowingLine(List<String> lines, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            String l = lines.get(i).trim();
            if (!l.isEmpty()) return l;
        }
        return "";
    }

    private File findPdfFile(File pdfsDir, String filename) {
        File[] files = pdfsDir.listFiles((dir, name) -> name.endsWith(filename));
        if (files == null || files.length == 0) return null;
        return files[0];
    }

    private Map<SectionKey, String> buildNextLineLookup(File pdfFile) {
        Map<SectionKey, String> map = new HashMap<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);
                List<String> lines = Arrays.stream(pageText.split("\n")).map(String::trim).collect(Collectors.toList());
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (isPotentialHeading(line)) {
                        String next = findFirstNonEmptyFollowingLine(lines, i + 1);
                        map.put(new SectionKey(line, page), next);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error building next-line lookup for {}", pdfFile.getName(), e);
        }
        return map;
    }

    private static final class HeadingCandidate {
        private final String headingText;
        private final int pageNumber;
        private final String nextLine;

        private HeadingCandidate(String headingText, int pageNumber, String nextLine) {
            this.headingText = headingText;
            this.pageNumber = pageNumber;
            this.nextLine = nextLine;
        }
    }

    private static final class SectionKey {
        private final String heading;
        private final int page;

        private SectionKey(String heading, int page) {
            this.heading = heading;
            this.page = page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SectionKey that = (SectionKey) o;
            return page == that.page && Objects.equals(heading, that.heading);
        }

        @Override
        public int hashCode() {
            return Objects.hash(heading, page);
        }
    }
}



