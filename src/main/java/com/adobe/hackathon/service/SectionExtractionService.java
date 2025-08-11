package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.ExtractedSection;
import com.adobe.hackathon.model.dto.SubsectionAnalysis;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SectionExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(SectionExtractionService.class);

    // Persona-specific keywords for relevance scoring
    private static final Map<String, List<String>> PERSONA_KEYWORDS = Map.of(
            "travel planner", Arrays.asList("travel", "trip", "hotel", "restaurant", "visit", "tour", "destination", "budget", "activity", "attraction"),
            "business analyst", Arrays.asList("business", "strategy", "analysis", "market", "revenue", "cost", "roi", "performance", "metrics"),
            "data scientist", Arrays.asList("data", "analysis", "model", "algorithm", "statistics", "machine learning", "visualization")
    );

    // Job-specific keywords
    private static final Map<String, List<String>> JOB_KEYWORDS = Map.of(
            "college friends", Arrays.asList("student", "budget", "group", "young", "affordable", "activity", "fun", "nightlife", "social"),
            "family trip", Arrays.asList("family", "children", "kid", "safe", "educational", "entertainment"),
            "business trip", Arrays.asList("conference", "meeting", "professional", "networking", "corporate")
    );

    public List<ExtractedSection> extractSectionsFromDocuments(String jobDirectory, String persona, String jobToBeDone) {
        List<ExtractedSection> allSections = new ArrayList<>();

        File pdfsDir = new File(jobDirectory, "PDFs");
        if (!pdfsDir.exists()) return allSections;

        File[] pdfFiles = pdfsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null) return allSections;

        for (File pdfFile : pdfFiles) {
            List<ExtractedSection> fileSections = extractSectionsFromFile(pdfFile, persona, jobToBeDone);
            allSections.addAll(fileSections);
        }

        return rankAndFilterSections(allSections, persona, jobToBeDone);
    }

    public List<SubsectionAnalysis> extractSubsectionAnalysis(String jobDirectory, List<ExtractedSection> topSections) {
        List<SubsectionAnalysis> subsections = new ArrayList<>();

        File pdfsDir = new File(jobDirectory, "PDFs");
        if (!pdfsDir.exists()) return subsections;

        for (ExtractedSection section : topSections) {
            SubsectionAnalysis analysis = extractRefinedText(pdfsDir, section);
            if (analysis != null) {
                subsections.add(analysis);
            }
        }

        return subsections;
    }

    private List<ExtractedSection> extractSectionsFromFile(File pdfFile, String persona, String jobToBeDone) {
        List<ExtractedSection> sections = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);

                List<String> sectionsInPage = extractSectionTitles(pageText);

                for (String sectionTitle : sectionsInPage) {
                    int relevanceScore = calculateRelevanceScore(sectionTitle, pageText, persona, jobToBeDone);

                    sections.add(new ExtractedSection(
                            pdfFile.getName(),
                            sectionTitle,
                            relevanceScore,
                            page
                    ));
                }
            }
        } catch (IOException e) {
            logger.error("Error extracting sections from: {}", pdfFile.getName(), e);
        }

        return sections;
    }

    private List<String> extractSectionTitles(String pageText) {
        List<String> sections = new ArrayList<>();

        // Pattern to match common section headers
        Pattern[] headerPatterns = {
                Pattern.compile("^([A-Z][A-Za-z\\s-]{3,50})\\s*$", Pattern.MULTILINE),
                Pattern.compile("^\\s*([A-Z][A-Za-z\\s-]{3,50}):", Pattern.MULTILINE),
                Pattern.compile("^\\s*•\\s*([A-Z][A-Za-z\\s-]{3,50})\\s*:", Pattern.MULTILINE),
                Pattern.compile("^\\s*\\d+\\.\\s*([A-Z][A-Za-z\\s-]{3,50})\\s*$", Pattern.MULTILINE)
        };

        for (Pattern pattern : headerPatterns) {
            Matcher matcher = pattern.matcher(pageText);
            while (matcher.find()) {
                String title = matcher.group(1).trim();
                if (title.length() > 3 && title.length() < 100) {
                    sections.add(title);
                }
            }
        }

        // Remove duplicates
        return sections.stream().distinct().collect(Collectors.toList());
    }

    private int calculateRelevanceScore(String sectionTitle, String content, String persona, String jobToBeDone) {
        int score = 0;
        String lowerTitle = sectionTitle.toLowerCase();
        String lowerContent = content.toLowerCase();
        String lowerPersona = persona.toLowerCase();
        String lowerJob = jobToBeDone.toLowerCase();

        // Score based on persona relevance
        List<String> personaKeywords = PERSONA_KEYWORDS.getOrDefault(lowerPersona, Arrays.asList());
        for (String keyword : personaKeywords) {
            if (lowerTitle.contains(keyword) || lowerContent.contains(keyword)) {
                score += 10;
            }
        }

        // Score based on job relevance
        for (String jobKeyword : lowerJob.split("\\s+")) {
            if (jobKeyword.length() > 3 && (lowerTitle.contains(jobKeyword) || lowerContent.contains(jobKeyword))) {
                score += 15;
            }
        }

        // Score based on common job types
        for (Map.Entry<String, List<String>> entry : JOB_KEYWORDS.entrySet()) {
            if (lowerJob.contains(entry.getKey())) {
                for (String keyword : entry.getValue()) {
                    if (lowerTitle.contains(keyword) || lowerContent.contains(keyword)) {
                        score += 8;
                    }
                }
            }
        }

        // Bonus for actionable content
        if (lowerTitle.contains("tip") || lowerTitle.contains("guide") ||
                lowerTitle.contains("how to") || lowerTitle.contains("budget") ||
                lowerTitle.contains("recommended") || lowerTitle.contains("must")) {
            score += 20;
        }

        return Math.min(score, 100); // Cap at 100
    }

    private List<ExtractedSection> rankAndFilterSections(List<ExtractedSection> sections, String persona, String jobToBeDone) {
        // Sort by relevance score (descending)
        sections.sort((a, b) -> Integer.compare(b.getImportanceRank(), a.getImportanceRank()));

        // Assign ranking (1-5) and filter top sections
        Map<String, Integer> documentRanking = new HashMap<>();
        List<ExtractedSection> rankedSections = new ArrayList<>();

        for (ExtractedSection section : sections) {
            String doc = section.getDocument();
            int currentRank = documentRanking.getOrDefault(doc, 0) + 1;

            if (currentRank <= 5) { // Top 5 per document
                section.setImportanceRank(currentRank);
                documentRanking.put(doc, currentRank);
                rankedSections.add(section);
            }
        }

        return rankedSections;
    }

    private SubsectionAnalysis extractRefinedText(File pdfsDir, ExtractedSection section) {
        File pdfFile = findPdfFile(pdfsDir, section.getDocument());
        if (pdfFile == null) return null;

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(section.getPageNumber());
            stripper.setEndPage(section.getPageNumber());
            String pageText = stripper.getText(document);

            String refinedText = extractRelevantText(pageText, section.getSectionTitle());

            return new SubsectionAnalysis(
                    section.getDocument(),
                    refinedText,
                    section.getPageNumber()
            );

        } catch (IOException e) {
            logger.error("Error extracting refined text from: {}", pdfFile.getName(), e);
            return null;
        }
    }

    private File findPdfFile(File pdfsDir, String filename) {
        File[] files = pdfsDir.listFiles((dir, name) -> name.endsWith(filename));
        return (files != null && files.length > 0) ? files[0] : null;
    }

    private String extractRelevantText(String pageText, String sectionTitle) {
        String[] lines = pageText.split("\n");
        List<String> relevantLines = new ArrayList<>();
        boolean inSection = false;
        int lineCount = 0;

        for (String line : lines) {
            if (line.trim().contains(sectionTitle)) {
                inSection = true;
                continue;
            }

            if (inSection && lineCount < 3) { // Get first few lines of the section
                if (line.trim().length() > 10) {
                    relevantLines.add(line.trim());
                    lineCount++;
                }
            }
        }

        String result = String.join(" ", relevantLines);
        return result.length() > 200 ? result.substring(0, 200) + "..." : result;
    }
}