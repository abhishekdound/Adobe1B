
package com.adobe.hackathon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.adobe.hackathon.model.dto.PDFSectionInfo;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service to validate Adobe Challenge requirements
 */
@Service
public class AdobeRequirementsValidationService {

    private static final Logger logger = LoggerFactory.getLogger(AdobeRequirementsValidationService.class);
    
    // Adobe Challenge Requirements
    private static final double MIN_ACCURACY_THRESHOLD = 0.8; // >80% accuracy
    private static final int MIN_RELATED_SECTIONS = 3; // At least 3 relevant sections
    private static final int MAX_SNIPPET_SENTENCES = 2; // 1-2 sentences for snippets
    
    /**
     * Validate that highlighted sections meet Adobe requirements
     */
    public Map<String, Object> validateHighlightedSections(List<PDFSectionInfo> sections) {
        Map<String, Object> validation = new HashMap<>();
        
        // Check minimum sections requirement
        boolean hasMinSections = sections.size() >= MIN_RELATED_SECTIONS;
        validation.put("hasMinimumSections", hasMinSections);
        validation.put("sectionCount", sections.size());
        validation.put("minimumRequired", MIN_RELATED_SECTIONS);
        
        // Check accuracy requirement (>80%)
        long highAccuracySections = sections.stream()
            .mapToDouble(PDFSectionInfo::getRelevanceScore)
            .filter(score -> score > MIN_ACCURACY_THRESHOLD)
            .count();
            
        double averageAccuracy = sections.stream()
            .mapToDouble(PDFSectionInfo::getRelevanceScore)
            .average()
            .orElse(0.0);
            
        boolean meetsAccuracyRequirement = averageAccuracy > MIN_ACCURACY_THRESHOLD;
        validation.put("meetsAccuracyRequirement", meetsAccuracyRequirement);
        validation.put("averageAccuracy", averageAccuracy);
        validation.put("highAccuracySections", highAccuracySections);
        validation.put("accuracyThreshold", MIN_ACCURACY_THRESHOLD);
        
        // Overall compliance
        boolean compliant = hasMinSections && meetsAccuracyRequirement;
        validation.put("adobeCompliant", compliant);
        
        if (!compliant) {
            logger.warn("Adobe requirements not met - Sections: {}/{}, Accuracy: {:.2f}%", 
                sections.size(), MIN_RELATED_SECTIONS, averageAccuracy * 100);
        } else {
            logger.info("Adobe requirements satisfied - Sections: {}, Accuracy: {:.2f}%", 
                sections.size(), averageAccuracy * 100);
        }
        
        return validation;
    }
    
    /**
     * Validate snippet length (1-2 sentences requirement)
     */
    public boolean validateSnippetLength(String snippet) {
        if (snippet == null || snippet.trim().isEmpty()) {
            return false;
        }
        
        // Count sentences (rough approximation)
        int sentenceCount = snippet.split("[.!?]+").length;
        return sentenceCount >= 1 && sentenceCount <= MAX_SNIPPET_SENTENCES;
    }
    
    /**
     * Generate Adobe-compliant snippet from content
     */
    public String generateCompliantSnippet(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "No content available for preview.";
        }
        
        String[] sentences = content.split("[.!?]+");
        if (sentences.length == 0) {
            return content.substring(0, Math.min(100, content.length())) + "...";
        }
        
        // Take first 1-2 sentences as per requirement
        StringBuilder snippet = new StringBuilder();
        int sentencesToTake = Math.min(2, sentences.length);
        
        for (int i = 0; i < sentencesToTake; i++) {
            if (i > 0) snippet.append(" ");
            snippet.append(sentences[i].trim());
            if (!sentences[i].trim().endsWith(".") && 
                !sentences[i].trim().endsWith("!") && 
                !sentences[i].trim().endsWith("?")) {
                snippet.append(".");
            }
        }
        
        return snippet.toString();
    }
    
    /**
     * Check if response time meets Adobe requirements
     */
    public Map<String, Object> validateResponseTime(long responseTimeMs, String operationType) {
        Map<String, Object> validation = new HashMap<>();
        
        long threshold = operationType.toLowerCase().contains("navigation") ? 2000L : 10000L;
        boolean meetsRequirement = responseTimeMs <= threshold;
        
        validation.put("responseTimeMs", responseTimeMs);
        validation.put("thresholdMs", threshold);
        validation.put("meetsRequirement", meetsRequirement);
        validation.put("operationType", operationType);
        
        return validation;
    }
}
