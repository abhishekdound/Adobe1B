package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.PDFSectionInfo;
import com.adobe.hackathon.model.dto.RelatedSection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EnhancedPdfAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPdfAnalysisService.class);

    // Patterns for identifying sections and headings
    private static final Pattern HEADING_PATTERN = Pattern.compile(
            "^\\s*(\\d+\\.?\\s*|[A-Z][a-z]*\\.?\\s*|[IVXLCDM]+\\.?\\s*)?([A-Z][A-Za-z\\s]{2,50})\\s*$",
            Pattern.MULTILINE
    );

    private static final Pattern SUBSECTION_PATTERN = Pattern.compile(
            "^\\s*(\\d+\\.\\d+\\.?\\s*|[a-z]\\)\\s*|\\*\\s*|•\\s*)([A-Za-z][A-Za-z\\s]{3,100})\\s*$",
            Pattern.MULTILINE
    );

    public Map<String, Object> analyzePdfsForAdobe(String filePaths) throws Exception {
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> fileAnalyses = new ArrayList<>();

        String[] paths = filePaths.split(",");

        for (String path : paths) {
            File pdfFile = new File(path.trim());
            if (pdfFile.exists()) {
                Map<String, Object> fileAnalysis = analyzeSinglePdfForAdobe(pdfFile);
                fileAnalyses.add(fileAnalysis);
            }
        }

        analysis.put("files", fileAnalyses);
        analysis.put("totalFiles", fileAnalyses.size());
        analysis.put("analysisTimestamp", System.currentTimeMillis());

        return analysis;
    }

    private Map<String, Object> analyzeSinglePdfForAdobe(File pdfFile) throws IOException {
        Map<String, Object> fileAnalysis = new HashMap<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            // Basic document info
            fileAnalysis.put("filename", pdfFile.getName());
            fileAnalysis.put("pageCount", document.getNumberOfPages());
            fileAnalysis.put("fileSize", pdfFile.length());

            // Extract full text
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullText = textStripper.getText(document);
            fileAnalysis.put("fullText", fullText);
            fileAnalysis.put("wordCount", countWords(fullText));

            // Extract sections with position information
            List<PDFSectionInfo> sections = extractSectionsWithPositions(document, fullText);
            fileAnalysis.put("sections", sections);
            fileAnalysis.put("sectionCount", sections.size());

            // Find related sections (core requirement)
            List<RelatedSection> relatedSections = identifyRelatedSections(sections, fullText);
            fileAnalysis.put("relatedSections", relatedSections);

            // Extract page-by-page content for better navigation
            List<Map<String, Object>> pageContents = extractPageByPageContent(document);
            fileAnalysis.put("pages", pageContents);

            logger.info("Analyzed PDF: {} - {} pages, {} sections, {} related sections",
                    pdfFile.getName(), document.getNumberOfPages(),
                    sections.size(), relatedSections.size());
        }

        return fileAnalysis;
    }

    private List<PDFSectionInfo> extractSectionsWithPositions(PDDocument document, String fullText) {
        List<PDFSectionInfo> sections = new ArrayList<>();

        // Find headings in the text
        Matcher headingMatcher = HEADING_PATTERN.matcher(fullText);
        int sectionId = 1;

        while (headingMatcher.find()) {
            String heading = headingMatcher.group(2).trim();
            int startPos = headingMatcher.start();

            // Estimate page number (rough calculation)
            int estimatedPage = estimatePageNumber(fullText, startPos, document.getNumberOfPages());

            PDFSectionInfo section = new PDFSectionInfo();
            section.setId(sectionId++);
            section.setTitle(heading);
            section.setPageNumber(estimatedPage);
            section.setStartPosition(startPos);
            section.setRelevanceScore(calculateRelevanceScore(heading, fullText));
            section.setKeywords(extractKeywords(heading));

            // Extract content preview (next 200 characters)
            String preview = extractContentPreview(fullText, startPos, 200);
            section.setContentPreview(preview);

            sections.add(section);
        }

        return sections;
    }

    private List<RelatedSection> identifyRelatedSections(List<PDFSectionInfo> sections, String fullText) {
        List<RelatedSection> relatedSections = new ArrayList<>();

        // Use keyword similarity and content analysis to find related sections
        for (int i = 0; i < sections.size(); i++) {
            PDFSectionInfo section = sections.get(i);
            List<PDFSectionInfo> related = new ArrayList<>();

            for (int j = 0; j < sections.size(); j++) {
                if (i != j) {
                    PDFSectionInfo candidate = sections.get(j);
                    double similarity = calculateSectionSimilarity(section, candidate);

                    if (similarity > 0.3) { // Threshold for relatedness
                        related.add(candidate);
                    }
                }
            }

            if (!related.isEmpty()) {
                // Sort by similarity (highest first)
                related.sort((a, b) -> Double.compare(
                        calculateSectionSimilarity(section, b),
                        calculateSectionSimilarity(section, a)
                ));

                // Take top 3 most related sections
                List<PDFSectionInfo> topRelated = related.stream()
                        .limit(3)
                        .collect(Collectors.toList());

                RelatedSection relatedSection = new RelatedSection();
                relatedSection.setSourceSection(section);
                relatedSection.setRelatedSections(topRelated);
                relatedSection.setRelationshipType("content_similarity");
                relatedSection.setConfidenceScore(
                        topRelated.stream()
                                .mapToDouble(r -> calculateSectionSimilarity(section, r))
                                .average()
                                .orElse(0.0)
                );

                relatedSections.add(relatedSection);
            }
        }

        return relatedSections;
    }

    private List<Map<String, Object>> extractPageByPageContent(PDDocument document) throws IOException {
        List<Map<String, Object>> pageContents = new ArrayList<>();
        PDFTextStripper textStripper = new PDFTextStripper();

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            textStripper.setStartPage(i);
            textStripper.setEndPage(i);

            String pageText = textStripper.getText(document);

            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("pageNumber", i);
            pageInfo.put("content", pageText);
            pageInfo.put("wordCount", countWords(pageText));
            pageInfo.put("hasHeadings", containsHeadings(pageText));

            // Extract headings on this page
            List<String> headings = extractHeadingsFromText(pageText);
            pageInfo.put("headings", headings);

            pageContents.add(pageInfo);
        }

        return pageContents;
    }

    private double calculateSectionSimilarity(PDFSectionInfo section1, PDFSectionInfo section2) {
        // Calculate similarity based on keywords and content
        Set<String> keywords1 = new HashSet<>(section1.getKeywords());
        Set<String> keywords2 = new HashSet<>(section2.getKeywords());

        // Jaccard similarity
        Set<String> intersection = new HashSet<>(keywords1);
        intersection.retainAll(keywords2);

        Set<String> union = new HashSet<>(keywords1);
        union.addAll(keywords2);

        if (union.isEmpty()) return 0.0;

        return (double) intersection.size() / union.size();
    }

    private double calculateRelevanceScore(String heading, String fullText) {
        // Simple relevance scoring based on heading characteristics
        double score = 0.0;

        // Length factor (medium length headings are often more relevant)
        int length = heading.length();
        if (length >= 10 && length <= 50) score += 0.3;

        // Contains common important keywords
        String lowerHeading = heading.toLowerCase();
        if (lowerHeading.contains("introduction") || lowerHeading.contains("overview")) score += 0.2;
        if (lowerHeading.contains("conclusion") || lowerHeading.contains("summary")) score += 0.2;
        if (lowerHeading.contains("method") || lowerHeading.contains("approach")) score += 0.15;
        if (lowerHeading.contains("result") || lowerHeading.contains("finding")) score += 0.15;

        return Math.min(1.0, score);
    }

    private List<String> extractKeywords(String text) {
        // Simple keyword extraction
        return Arrays.stream(text.toLowerCase().split("\\s+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !isStopWord(word))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "for", "are", "but", "not",
                "you", "all", "can", "had", "her", "was",
                "one", "our", "out", "day", "get", "has",
                "him", "his", "how", "its", "may", "new",
                "now", "old", "see", "two", "who", "boy",
                "did", "does", "let", "put", "say", "she",
                "too", "use");
        return stopWords.contains(word.toLowerCase());
    }

    private String extractContentPreview(String fullText, int startPos, int maxLength) {
        int endPos = Math.min(startPos + maxLength, fullText.length());
        String preview = fullText.substring(startPos, endPos);

        // Clean up the preview
        preview = preview.replaceAll("\\s+", " ").trim();

        if (endPos < fullText.length()) {
            preview += "...";
        }

        return preview;
    }

    private int estimatePageNumber(String fullText, int position, int totalPages) {
        // Rough estimation based on position in text
        double ratio = (double) position / fullText.length();
        return Math.max(1, (int) Math.ceil(ratio * totalPages));
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    private boolean containsHeadings(String text) {
        Matcher matcher = HEADING_PATTERN.matcher(text);
        return matcher.find();
    }

    private List<String> extractHeadingsFromText(String text) {
        List<String> headings = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(text);

        while (matcher.find()) {
            String heading = matcher.group(2);
            if (heading != null) {
                headings.add(heading.trim());
            }
        }

        return headings;
    }
}