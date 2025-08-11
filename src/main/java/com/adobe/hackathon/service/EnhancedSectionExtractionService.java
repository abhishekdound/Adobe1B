package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.ExtractedSection;
import com.adobe.hackathon.model.dto.SubsectionAnalysis;
import com.adobe.hackathon.model.dto.DetailedExtractedSection;
import com.adobe.hackathon.model.dto.DetailedSubsectionAnalysis;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EnhancedSectionExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedSectionExtractionService.class);

    // Enhanced persona-specific keywords with weights
    private static final Map<String, Map<String, Integer>> WEIGHTED_PERSONA_KEYWORDS = Map.of(
            "travel planner", Map.ofEntries(
                    Map.entry("travel", 15),
                    Map.entry("trip", 12),
                    Map.entry("hotel", 10),
                    Map.entry("restaurant", 10),
                    Map.entry("visit", 8),
                    Map.entry("tour", 8),
                    Map.entry("destination", 12),
                    Map.entry("budget", 15),
                    Map.entry("activity", 10),
                    Map.entry("attraction", 8),
                    Map.entry("booking", 10),
                    Map.entry("accommodation", 12),
                    Map.entry("transportation", 10),
                    Map.entry("itinerary", 15)
            ),
            "business analyst", Map.ofEntries(
                    Map.entry("business", 15),
                    Map.entry("strategy", 12),
                    Map.entry("analysis", 15),
                    Map.entry("market", 10),
                    Map.entry("revenue", 12),
                    Map.entry("cost", 10),
                    Map.entry("roi", 15),
                    Map.entry("performance", 12),
                    Map.entry("metrics", 15),
                    Map.entry("data", 10),
                    Map.entry("insight", 12),
                    Map.entry("forecast", 10),
                    Map.entry("trend", 8)
            )
    );


    // Enhanced job-specific keywords
    private static final Map<String, Map<String, Integer>> WEIGHTED_JOB_KEYWORDS = Map.of(
            "college friends", Map.of(
            ),
            "family trip", Map.of(
                    "family", 20, "children", 15, "kid", 15, "safe", 12, "educational", 10,
                    "entertainment", 12, "playground", 8, "museum", 10
            )
    );

    // Content type classification patterns
    private static final Map<String, Pattern> CONTENT_TYPE_PATTERNS = Map.of(
            "practical_advice", Pattern.compile("(?i)(tip|guide|advice|how to|step|instruction|recommendation)", Pattern.CASE_INSENSITIVE),
            "demographic_specific", Pattern.compile("(?i)(student|family|senior|youth|adult|child)", Pattern.CASE_INSENSITIVE),
            "entertainment", Pattern.compile("(?i)(show|performance|theater|concert|event|festival)", Pattern.CASE_INSENSITIVE),
            "activity", Pattern.compile("(?i)(activity|adventure|experience|tour|excursion|visit)", Pattern.CASE_INSENSITIVE),
            "sightseeing", Pattern.compile("(?i)(attraction|landmark|monument|historic|museum|gallery)", Pattern.CASE_INSENSITIVE),
            "accommodation", Pattern.compile("(?i)(hotel|hostel|accommodation|stay|lodging|resort)", Pattern.CASE_INSENSITIVE),
            "dining", Pattern.compile("(?i)(restaurant|cafe|food|cuisine|dining|meal|eat)", Pattern.CASE_INSENSITIVE)
    );

    public List<DetailedExtractedSection> extractDetailedSectionsFromDocuments(
            String jobDirectory, String persona, String jobToBeDone) {

        long startTime = System.currentTimeMillis();
        List<DetailedExtractedSection> allSections = new ArrayList<>();
        Map<String, Object> processingStats = new HashMap<>();

        File pdfsDir = new File(jobDirectory, "PDFs");
        if (!pdfsDir.exists()) {
            logger.warn("PDFs directory not found: {}", pdfsDir.getPath());
            return allSections;
        }

        File[] pdfFiles = pdfsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null) {
            logger.warn("No PDF files found in directory: {}", pdfsDir.getPath());
            return allSections;
        }

        logger.info("Processing {} PDF files for detailed extraction", pdfFiles.length);
        int totalSections = 0;
        int totalPages = 0;

        for (File pdfFile : pdfFiles) {
            try {
                List<DetailedExtractedSection> fileSections = extractDetailedSectionsFromFile(
                        pdfFile, persona, jobToBeDone);
                allSections.addAll(fileSections);
                totalSections += fileSections.size();

                // Count pages
                try (PDDocument document = PDDocument.load(pdfFile)) {
                    totalPages += document.getNumberOfPages();
                }
            } catch (Exception e) {
                logger.error("Error processing file: {}", pdfFile.getName(), e);
            }
        }

        // Enhanced ranking and filtering
        List<DetailedExtractedSection> rankedSections = rankAndFilterDetailedSections(
                allSections, persona, jobToBeDone);

        long processingTime = System.currentTimeMillis() - startTime;
        logger.info("Detailed extraction completed: {} sections from {} files, {} pages in {}ms",
                rankedSections.size(), pdfFiles.length, totalPages, processingTime);

        return rankedSections;
    }

    private List<DetailedExtractedSection> extractDetailedSectionsFromFile(
            File pdfFile, String persona, String jobToBeDone) throws IOException {

        List<DetailedExtractedSection> sections = new ArrayList<>();
        Map<String, String> pageContents = new HashMap<>();
        Map<String, Integer> wordCounts = new HashMap<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();

            // First pass: extract all page contents
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);
                pageContents.put("page_" + page, pageText);
                wordCounts.put("page_" + page, countWords(pageText));
            }

            // Second pass: analyze sections with context
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                String pageText = pageContents.get("page_" + page);
                List<String> sectionsInPage = extractSectionTitles(pageText);

                for (String sectionTitle : sectionsInPage) {
                    DetailedExtractedSection detailedSection = createDetailedSection(
                            pdfFile, sectionTitle, page, pageText, persona, jobToBeDone,
                            wordCounts.get("page_" + page));

                    sections.add(detailedSection);
                }
            }
        }

        return sections;
    }

    private DetailedExtractedSection createDetailedSection(File pdfFile, String sectionTitle,
                                                           int page, String pageText, String persona, String jobToBeDone, int pageWordCount) {

        // Calculate detailed relevance metrics
        Map<String, Double> relevanceMetrics = calculateDetailedRelevance(
                sectionTitle, pageText, persona, jobToBeDone);

        // Determine content type
        String contentType = determineContentType(sectionTitle, pageText);

        // Extract key topics
        List<String> keyTopics = extractKeyTopics(sectionTitle, pageText);

        // Calculate applicability scores
        String studentRelevance = calculateStudentRelevance(sectionTitle, pageText);
        String groupApplicability = calculateGroupApplicability(sectionTitle, pageText);

        // Extract related sections
        List<String> relatedSections = findRelatedSections(sectionTitle, keyTopics);

        DetailedExtractedSection section = new DetailedExtractedSection();
        section.setDocument(pdfFile.getName());
        section.setSectionTitle(sectionTitle);
        section.setPageNumber(page);
        section.setRelevanceScore(relevanceMetrics.get("totalScore"));
        section.setImportanceRank(0); // Will be set during ranking
        section.setSectionType(contentType);
        section.setWordCount(countWordsInSection(pageText, sectionTitle));
        section.setKeyTopics(keyTopics);
        section.setStudentRelevance(studentRelevance);
        section.setGroupApplicability(groupApplicability);
        section.setExtractionConfidence(relevanceMetrics.get("confidence"));
        section.setRelatedSections(relatedSections);

        return section;
    }

    private Map<String, Double> calculateDetailedRelevance(String sectionTitle, String content,
                                                           String persona, String jobToBeDone) {

        Map<String, Double> metrics = new HashMap<>();
        double totalScore = 0.0;
        double maxPossibleScore = 100.0;

        String lowerTitle = sectionTitle.toLowerCase();
        String lowerContent = content.toLowerCase();
        String lowerPersona = persona.toLowerCase();
        String lowerJob = jobToBeDone.toLowerCase();

        // Persona relevance scoring (35% weight)
        double personaScore = 0.0;
        Map<String, Integer> personaKeywords = WEIGHTED_PERSONA_KEYWORDS.getOrDefault(lowerPersona, new HashMap<>());
        for (Map.Entry<String, Integer> entry : personaKeywords.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();

            if (lowerTitle.contains(keyword)) {
                personaScore += weight * 1.5; // Title matches get higher weight
            }
            if (lowerContent.contains(keyword)) {
                personaScore += weight * 0.8; // Content matches
            }
        }
        personaScore = Math.min(personaScore, 35.0);
        totalScore += personaScore;

        // Job relevance scoring (30% weight)
        double jobScore = 0.0;
        String[] jobWords = lowerJob.split("\\s+");
        for (String jobWord : jobWords) {
            if (jobWord.length() > 3) {
                if (lowerTitle.contains(jobWord)) {
                    jobScore += 5.0;
                }
                if (lowerContent.contains(jobWord)) {
                    jobScore += 3.0;
                }
            }
        }

        // Check specific job type keywords
        for (Map.Entry<String, Map<String, Integer>> jobType : WEIGHTED_JOB_KEYWORDS.entrySet()) {
            if (lowerJob.contains(jobType.getKey())) {
                for (Map.Entry<String, Integer> keyword : jobType.getValue().entrySet()) {
                    if (lowerTitle.contains(keyword.getKey()) || lowerContent.contains(keyword.getKey())) {
                        jobScore += keyword.getValue() * 0.5;
                    }
                }
            }
        }
        jobScore = Math.min(jobScore, 30.0);
        totalScore += jobScore;

        // Content quality scoring (20% weight)
        double qualityScore = 0.0;
        if (lowerTitle.matches(".*\\b(tip|guide|how to|best|top|recommended|must)\\b.*")) {
            qualityScore += 8.0;
        }
        if (lowerContent.length() > 100) {
            qualityScore += 5.0;
        }
        if (countSentences(content) >= 3) {
            qualityScore += 4.0;
        }
        if (containsActionableContent(content)) {
            qualityScore += 3.0;
        }
        qualityScore = Math.min(qualityScore, 20.0);
        totalScore += qualityScore;

        // Uniqueness scoring (15% weight)
        double uniquenessScore = calculateUniquenessScore(sectionTitle, content);
        totalScore += uniquenessScore;

        // Calculate confidence based on multiple factors
        double confidence = calculateConfidence(personaScore, jobScore, qualityScore,
                sectionTitle.length(), content.length());

        metrics.put("totalScore", Math.min(totalScore, maxPossibleScore));
        metrics.put("personaScore", personaScore);
        metrics.put("jobScore", jobScore);
        metrics.put("qualityScore", qualityScore);
        metrics.put("uniquenessScore", uniquenessScore);
        metrics.put("confidence", confidence);

        return metrics;
    }

    private String determineContentType(String title, String content) {
        String combined = (title + " " + content).toLowerCase();

        for (Map.Entry<String, Pattern> entry : CONTENT_TYPE_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(combined).find()) {
                return entry.getKey();
            }
        }

        return "general";
    }

    private List<String> extractKeyTopics(String title, String content) {
        Set<String> topics = new HashSet<>();
        String combined = (title + " " + content).toLowerCase();

        // Extract key phrases using simple n-gram analysis
        String[] words = combined.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            String bigram = words[i] + "_" + words[i + 1];
            if (bigram.matches(".*\\b(travel|hotel|restaurant|budget|student|group|activity)\\b.*")) {
                topics.add(bigram.replace("_", " "));
            }
        }

        // Add single important keywords
        String[] importantKeywords = {"budget", "travel", "student", "group", "hotel", "restaurant",
                "activity", "cultural", "historic", "entertainment", "nightlife"};
        for (String keyword : importantKeywords) {
            if (combined.contains(keyword)) {
                topics.add(keyword);
            }
        }

        return new ArrayList<>(topics).stream().limit(6).collect(Collectors.toList());
    }

    private String calculateStudentRelevance(String title, String content) {
        String combined = (title + " " + content).toLowerCase();
        int relevanceScore = 0;

        String[] studentKeywords = {"student", "budget", "cheap", "affordable", "young",
                "university", "college", "discount", "backpack"};
        for (String keyword : studentKeywords) {
            if (combined.contains(keyword)) {
                relevanceScore++;
            }
        }

        if (relevanceScore >= 4) return "excellent";
        if (relevanceScore >= 2) return "high";
        if (relevanceScore >= 1) return "medium";
        return "low";
    }

    private String calculateGroupApplicability(String title, String content) {
        String combined = (title + " " + content).toLowerCase();
        int groupScore = 0;

        String[] groupKeywords = {"group", "friends", "party", "together", "social",
                "team", "multiple", "shared", "collective"};
        for (String keyword : groupKeywords) {
            if (combined.contains(keyword)) {
                groupScore++;
            }
        }

        if (groupScore >= 3) return "excellent";
        if (groupScore >= 2) return "high";
        if (groupScore >= 1) return "medium";
        return "low";
    }

    private List<String> findRelatedSections(String sectionTitle, List<String> keyTopics) {
        List<String> related = new ArrayList<>();

        // Simple related section suggestions based on content type
        String lower = sectionTitle.toLowerCase();
        if (lower.contains("budget")) {
            related.addAll(Arrays.asList("Cost Planning", "Affordable Options", "Money Saving Tips"));
        }
        if (lower.contains("student")) {
            related.addAll(Arrays.asList("Youth Activities", "Educational Discounts", "Hostel Information"));
        }
        if (lower.contains("restaurant") || lower.contains("food")) {
            related.addAll(Arrays.asList("Local Cuisine", "Dining Experiences", "Food Markets"));
        }

        return related.stream().limit(3).collect(Collectors.toList());
    }

    private List<DetailedExtractedSection> rankAndFilterDetailedSections(
            List<DetailedExtractedSection> sections, String persona, String jobToBeDone) {

        // Sort by relevance score (descending)
        sections.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));

        // Assign ranking and filter top sections per document
        Map<String, Integer> documentRanking = new HashMap<>();
        List<DetailedExtractedSection> rankedSections = new ArrayList<>();

        for (DetailedExtractedSection section : sections) {
            String doc = section.getDocument();
            int currentRank = documentRanking.getOrDefault(doc, 0) + 1;

            if (currentRank <= 5) { // Top 5 per document
                section.setImportanceRank(currentRank);
                documentRanking.put(doc, currentRank);
                rankedSections.add(section);
            }
        }

        logger.info("Ranked {} sections across {} documents", rankedSections.size(), documentRanking.size());
        return rankedSections;
    }

    public List<DetailedSubsectionAnalysis> extractDetailedSubsectionAnalysis(
            String jobDirectory, List<DetailedExtractedSection> topSections) {

        List<DetailedSubsectionAnalysis> subsections = new ArrayList<>();
        File pdfsDir = new File(jobDirectory, "PDFs");

        if (!pdfsDir.exists()) {
            logger.warn("PDFs directory not found for subsection analysis");
            return subsections;
        }

        logger.info("Extracting detailed subsection analysis for {} sections", topSections.size());

        for (DetailedExtractedSection section : topSections) {
            try {
                DetailedSubsectionAnalysis analysis = extractDetailedRefinedText(pdfsDir, section);
                if (analysis != null) {
                    subsections.add(analysis);
                }
            } catch (Exception e) {
                logger.error("Error extracting subsection for section: {}", section.getSectionTitle(), e);
            }
        }

        return subsections;
    }

    private DetailedSubsectionAnalysis extractDetailedRefinedText(
            File pdfsDir, DetailedExtractedSection section) {

        File pdfFile = findPdfFile(pdfsDir, section.getDocument());
        if (pdfFile == null) {
            logger.warn("PDF file not found: {}", section.getDocument());
            return null;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(section.getPageNumber());
            stripper.setEndPage(section.getPageNumber());
            String pageText = stripper.getText(document);

            String refinedText = extractRefinedTextWithContext(pageText, section.getSectionTitle());
            Map<String, Object> analysisDetails = performTextAnalysis(refinedText, section);

            DetailedSubsectionAnalysis analysis = new DetailedSubsectionAnalysis();
            analysis.setDocument(section.getDocument());
            analysis.setRefinedText(refinedText);
            analysis.setPageNumber(section.getPageNumber());
            analysis.setAnalysisDetails(analysisDetails);

            return analysis;

        } catch (IOException e) {
            logger.error("Error extracting refined text from: {}", pdfFile.getName(), e);
            return null;
        }
    }

    private String extractRefinedTextWithContext(String pageText, String sectionTitle) {
        String[] lines = pageText.split("\n");
        List<String> relevantLines = new ArrayList<>();
        boolean inSection = false;
        int lineCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.toLowerCase().contains(sectionTitle.toLowerCase())) {
                inSection = true;
                continue;
            }

            if (inSection && lineCount < 5 && trimmedLine.length() > 15) {
                // Clean and enhance the text
                String cleanedLine = cleanText(trimmedLine);
                if (!cleanedLine.isEmpty()) {
                    relevantLines.add(cleanedLine);
                    lineCount++;
                }
            }

            // Stop if we hit another section header
            if (inSection && lineCount > 0 && isLikelyHeader(trimmedLine)) {
                break;
            }
        }

        String result = String.join(" ", relevantLines);
        return result.length() > 300 ? result.substring(0, 297) + "..." : result;
    }

    private Map<String, Object> performTextAnalysis(String text, DetailedExtractedSection section) {
        Map<String, Object> details = new HashMap<>();

        details.put("characterCount", text.length());
        details.put("sentenceCount", countSentences(text));
        details.put("keyInsights", extractKeyInsights(text));
        details.put("actionableAdvice", containsActionableContent(text));
        details.put("budgetRelevance", calculateBudgetRelevance(text));
        details.put("groupPlanningValue", calculateGroupPlanningValue(text));
        details.put("informationType", determineInformationType(text));
        details.put("confidenceLevel", section.getExtractionConfidence());

        return details;
    }

    private List<String> extractKeyInsights(String text) {
        List<String> insights = new ArrayList<>();
        String lower = text.toLowerCase();

        if (lower.contains("best time") || lower.contains("when to")) {
            insights.add("timing_strategy");
        }
        if (lower.contains("budget") || lower.contains("cheap") || lower.contains("affordable")) {
            insights.add("cost_optimization");
        }
        if (lower.contains("student") || lower.contains("young") || lower.contains("university")) {
            insights.add("demographic_match");
        }
        if (lower.contains("group") || lower.contains("friends") || lower.contains("together")) {
            insights.add("group_dynamics");
        }

        return insights;
    }

    // Helper methods
    private int countWords(String text) {
        return text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }

    private int countWordsInSection(String pageText, String sectionTitle) {
        String sectionText = extractRefinedTextWithContext(pageText, sectionTitle);
        return countWords(sectionText);
    }

    private int countSentences(String text) {
        return text.split("[.!?]+").length;
    }

    private boolean containsActionableContent(String text) {
        String lower = text.toLowerCase();
        String[] actionWords = {"visit", "try", "book", "go to", "check out", "consider", "make sure", "don't miss"};
        return Arrays.stream(actionWords).anyMatch(lower::contains);
    }

    private double calculateUniquenessScore(String title, String content) {
        // Simple uniqueness calculation based on content length and specificity
        double score = 0.0;

        if (title.length() > 20) score += 3.0;
        if (content.contains("€") || content.contains("$") || content.contains("price")) score += 2.0;
        if (content.matches(".*\\b(specific|unique|special|exclusive)\\b.*")) score += 3.0;

        return Math.min(score, 15.0);
    }

    private double calculateConfidence(double personaScore, double jobScore, double qualityScore,
                                       int titleLength, int contentLength) {
        double confidence = 0.0;

        if (personaScore > 20) confidence += 0.3;
        else if (personaScore > 10) confidence += 0.2;
        else confidence += 0.1;

        if (jobScore > 15) confidence += 0.3;
        else if (jobScore > 8) confidence += 0.2;
        else confidence += 0.1;

        if (qualityScore > 10) confidence += 0.2;
        else confidence += 0.1;

        if (titleLength > 10 && contentLength > 50) confidence += 0.2;
        else confidence += 0.1;

        return Math.round(confidence * 100.0) / 100.0;
    }

    private String calculateBudgetRelevance(String text) {
        String lower = text.toLowerCase();
        int budgetScore = 0;

        String[] budgetKeywords = {"budget", "cheap", "affordable", "cost", "price", "free", "discount"};
        for (String keyword : budgetKeywords) {
            if (lower.contains(keyword)) budgetScore++;
        }

        if (budgetScore >= 3) return "high";
        if (budgetScore >= 1) return "medium";
        return "low";
    }

    private String calculateGroupPlanningValue(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("group") || lower.contains("friends") || lower.contains("together")) {
            return "excellent";
        }
        if (lower.contains("social") || lower.contains("shared")) {
            return "high";
        }
        return "medium";
    }

    private String determineInformationType(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("tip") || lower.contains("advice")) return "advice";
        if (lower.contains("student") || lower.contains("young")) return "demographic_specific";
        if (lower.contains("activity") || lower.contains("do")) return "activity";
        return "general";
    }

    private String cleanText(String text) {
        return text.replaceAll("\\s+", " ")
                .replaceAll("^[•\\-\\*]+\\s*", "")
                .trim();
    }

    private boolean isLikelyHeader(String line) {
        return line.length() < 100 &&
                (line.matches("^[A-Z][A-Za-z\\s-]{5,}$") ||
                        line.matches("^\\d+\\.\\s+[A-Z].*"));
    }

    private File findPdfFile(File pdfsDir, String filename) {
        File[] files = pdfsDir.listFiles((dir, name) -> name.contains(filename.replace(".pdf", "")));
        return (files != null && files.length > 0) ? files[0] : null;
    }

    private List<String> extractSectionTitles(String pageText) {
        List<String> sections = new ArrayList<>();

        // Enhanced patterns for section detection
        Pattern[] headerPatterns = {
                Pattern.compile("^([A-Z][A-Za-z\\s-]{3,80})\\s*$", Pattern.MULTILINE),
                Pattern.compile("^\\s*([A-Z][A-Za-z\\s-]{3,80}):", Pattern.MULTILINE),
                Pattern.compile("^\\s*•\\s*([A-Z][A-Za-z\\s-]{3,80})\\s*:", Pattern.MULTILINE),
                Pattern.compile("^\\s*\\d+\\.\\s*([A-Z][A-Za-z\\s-]{3,80})\\s*$", Pattern.MULTILINE),
                Pattern.compile("^\\s*\\*\\s*([A-Z][A-Za-z\\s-]{3,80})\\s*\\*?\\s*$", Pattern.MULTILINE)
        };

        for (Pattern pattern : headerPatterns) {
            Matcher matcher = pattern.matcher(pageText);
            while (matcher.find()) {
                String title = matcher.group(1).trim();
                if (title.length() > 3 && title.length() < 150 && !title.matches(".*\\d{3,}.*")) {
                    sections.add(title);
                }
            }
        }

        return sections.stream().distinct().collect(Collectors.toList());
    }
}