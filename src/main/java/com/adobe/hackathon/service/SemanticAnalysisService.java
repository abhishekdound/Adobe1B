package com.adobe.hackathon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SemanticAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticAnalysisService.class);

    // Keywords for different domains
    private static final Map<String, List<String>> DOMAIN_KEYWORDS = Map.of(
            "machine_learning", Arrays.asList("neural network", "deep learning", "algorithm", "model", "training", "classification"),
            "data_science", Arrays.asList("data analysis", "statistics", "visualization", "dataset", "correlation", "regression"),
            "software_engineering", Arrays.asList("architecture", "design pattern", "API", "framework", "deployment", "testing"),
            "business", Arrays.asList("strategy", "market", "customer", "revenue", "profit", "analysis", "growth")
    );

    public Map<String, Object> performSemanticAnalysis(Map<String, Object> pdfAnalysis, String persona, String jobToBeDone) {
        Map<String, Object> semanticResults = new HashMap<>();

        try {
            // Extract text from PDF analysis
            String combinedText = extractCombinedText(pdfAnalysis);

            // Perform analysis based on persona and job
            Map<String, Object> personaAnalysis = analyzeForPersona(combinedText, persona);
            Map<String, Object> jobAnalysis = analyzeForJob(combinedText, jobToBeDone);
            Map<String, Object> keywordsAnalysis = extractKeywords(combinedText);
            Map<String, Object> sentimentAnalysis = analyzeSentiment(combinedText);

            semanticResults.put("personaRelevance", personaAnalysis);
            semanticResults.put("jobRelevance", jobAnalysis);
            semanticResults.put("keywords", keywordsAnalysis);
            semanticResults.put("sentiment", sentimentAnalysis);
            semanticResults.put("summary", generateSummary(combinedText, persona, jobToBeDone));
            semanticResults.put("success", true);

            logger.info("Semantic analysis completed for persona: {} and job: {}", persona, jobToBeDone);

        } catch (Exception e) {
            logger.error("Error in semantic analysis", e);
            semanticResults.put("error", "Failed to perform semantic analysis: " + e.getMessage());
            semanticResults.put("success", false);
        }

        return semanticResults;
    }

    private String extractCombinedText(Map<String, Object> pdfAnalysis) {
        StringBuilder combinedText = new StringBuilder();

        if (pdfAnalysis.containsKey("files")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> files = (List<Map<String, Object>>) pdfAnalysis.get("files");

            for (Map<String, Object> file : files) {
                if (file.containsKey("textPreview")) {
                    combinedText.append(file.get("textPreview").toString()).append(" ");
                }
            }
        }

        return combinedText.toString();
    }

    private Map<String, Object> analyzeForPersona(String text, String persona) {
        Map<String, Object> personaAnalysis = new HashMap<>();
        String lowerText = text.toLowerCase();
        String lowerPersona = persona.toLowerCase();

        // Simple relevance scoring based on persona keywords
        int relevanceScore = 0;

        // Check for persona-specific terms
        if (lowerPersona.contains("data scientist")) {
            relevanceScore = countKeywordMatches(lowerText, DOMAIN_KEYWORDS.get("data_science"));
        } else if (lowerPersona.contains("software engineer")) {
            relevanceScore = countKeywordMatches(lowerText, DOMAIN_KEYWORDS.get("software_engineering"));
        } else if (lowerPersona.contains("business analyst")) {
            relevanceScore = countKeywordMatches(lowerText, DOMAIN_KEYWORDS.get("business"));
        }

        personaAnalysis.put("relevanceScore", relevanceScore);
        personaAnalysis.put("maxScore", 100);
        personaAnalysis.put("percentage", Math.min(100, (relevanceScore * 100) / 10));

        return personaAnalysis;
    }

    private Map<String, Object> analyzeForJob(String text, String jobToBeDone) {
        Map<String, Object> jobAnalysis = new HashMap<>();
        String lowerText = text.toLowerCase();
        String lowerJob = jobToBeDone.toLowerCase();

        // Extract key terms from job description
        String[] jobKeywords = lowerJob.split("\\s+");
        int matches = 0;

        for (String keyword : jobKeywords) {
            if (keyword.length() > 3 && lowerText.contains(keyword)) {
                matches++;
            }
        }

        jobAnalysis.put("keywordMatches", matches);
        jobAnalysis.put("totalKeywords", jobKeywords.length);
        jobAnalysis.put("matchPercentage", jobKeywords.length > 0 ? (matches * 100) / jobKeywords.length : 0);

        return jobAnalysis;
    }

    private Map<String, Object> extractKeywords(String text) {
        Map<String, Object> keywordsAnalysis = new HashMap<>();
        String lowerText = text.toLowerCase();

        // Simple keyword extraction (in production, use NLP libraries)
        Map<String, Integer> keywordCounts = new HashMap<>();

        for (Map.Entry<String, List<String>> domain : DOMAIN_KEYWORDS.entrySet()) {
            for (String keyword : domain.getValue()) {
                int count = countOccurrences(lowerText, keyword);
                if (count > 0) {
                    keywordCounts.put(keyword, count);
                }
            }
        }

        keywordsAnalysis.put("topKeywords", keywordCounts);
        keywordsAnalysis.put("totalUniqueKeywords", keywordCounts.size());

        return keywordsAnalysis;
    }

    private Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> sentimentAnalysis = new HashMap<>();

        // Very basic sentiment analysis (use proper NLP libraries in production)
        String lowerText = text.toLowerCase();

        List<String> positiveWords = Arrays.asList("good", "great", "excellent", "positive", "beneficial", "effective");
        List<String> negativeWords = Arrays.asList("bad", "poor", "negative", "problem", "issue", "failure");

        int positiveCount = countKeywordMatches(lowerText, positiveWords);
        int negativeCount = countKeywordMatches(lowerText, negativeWords);

        String overallSentiment = positiveCount > negativeCount ? "Positive" :
                negativeCount > positiveCount ? "Negative" : "Neutral";

        sentimentAnalysis.put("positive", positiveCount);
        sentimentAnalysis.put("negative", negativeCount);
        sentimentAnalysis.put("overall", overallSentiment);

        return sentimentAnalysis;
    }

    private String generateSummary(String text, String persona, String jobToBeDone) {
        // Generate a basic summary based on analysis
        StringBuilder summary = new StringBuilder();
        summary.append("Analysis Summary for ").append(persona).append(":\n");
        summary.append("Job: ").append(jobToBeDone).append("\n\n");
        summary.append("The documents contain ");
        summary.append(text.length()).append(" characters of text. ");

        // Add more intelligent summarization logic here
        summary.append("Content appears to be relevant to the specified persona and job requirements.");

        return summary.toString();
    }

    private int countKeywordMatches(String text, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            count += countOccurrences(text, keyword);
        }
        return count;
    }

    private int countOccurrences(String text, String keyword) {
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        return (int) pattern.matcher(text).results().count();
    }
}