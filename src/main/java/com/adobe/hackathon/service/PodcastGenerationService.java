// PodcastGenerationService.java
package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.AdobeAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.adobe.hackathon.model.dto.JobStatusResponse;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PodcastGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PodcastGenerationService.class);

    @Autowired
    private LLMIntegrationService llmService;

    @Autowired
    private TextToSpeechService ttsService;

    @Autowired
    private AdobeAnalysisService adobeAnalysisService;

    @Value("${app.tts.enabled:false}")
    private boolean ttsEnabled;

    public AdobeAnalysisResponse.PodcastContent generatePodcast(String jobId, int durationSeconds) throws Exception {
        logger.info("Generating podcast for job: {} with duration: {}s", jobId, durationSeconds);

        // Get the analysis data
        JobStatusResponse jobStatus = adobeAnalysisService.getJobStatus(jobId);
        if (!"COMPLETED".equals(jobStatus.getStatus())) {
            throw new RuntimeException("Analysis must be completed before generating podcast");
        }

        AdobeAnalysisResponse.PodcastContent podcast = new AdobeAnalysisResponse.PodcastContent();

        try {
            // Generate podcast script
            String script = generatePodcastScript(jobId, durationSeconds);
            podcast.setTranscript(script);

            // Extract key topics
            List<String> keyTopics = extractKeyTopics(script);
            podcast.setKeyTopics(keyTopics);

            // Set duration
            podcast.setDurationSeconds(durationSeconds);

            // Generate audio if TTS is enabled
            if (ttsEnabled) {
                String audioUrl = ttsService.generateAudio(script, jobId);
                podcast.setAudioUrl(audioUrl);
            } else {
                podcast.setAudioUrl("/api/adobe/podcast/demo-audio/" + jobId);
            }

            logger.info("Successfully generated podcast for job: {}", jobId);

        } catch (Exception e) {
            logger.error("Error generating podcast for job: {}", jobId, e);
            // Return fallback podcast
            podcast = generateFallbackPodcast(jobId, durationSeconds);
        }

        return podcast;
    }

    private String generatePodcastScript(String jobId, int durationSeconds) throws Exception {
        // Get document outline and content
        Map<String, Object> outline = adobeAnalysisService.getDocumentOutline(jobId);

        // Estimate words needed (average speaking rate: 150 words per minute)
        int targetWords = (durationSeconds / 60) * 150;

        String prompt = """
            Create a podcast script based on the following document analysis.
            The script should be engaging, conversational, and approximately %d words.
            
            Structure:
            1. Introduction (10%% of content)
            2. Main content overview (70%% of content)  
            3. Key insights and takeaways (20%% of content)
            
            Make it sound natural and engaging for audio consumption.
            Use transitions between topics and keep the tone professional but accessible.
            
            Document Analysis:
            %s
            
            Podcast Script:
            """.formatted(targetWords, outline.toString());

        try {
            return llmService.generateResponse(prompt);
        } catch (Exception e) {
            logger.warn("LLM call failed for podcast script, using fallback", e);
            return generateFallbackScript(jobId, durationSeconds);
        }
    }

    private List<String> extractKeyTopics(String script) {
        // Simple keyword extraction from script
        Set<String> commonWords = Set.of("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "a", "an", "this", "that", "these", "those", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can");

        return Arrays.stream(script.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 4)
                .filter(word -> !commonWords.contains(word))
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(Map.Entry::getKey)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.toList());
    }

    private String generateFallbackScript(String jobId, int durationSeconds) {
        int minutes = durationSeconds / 60;

        return String.format("""
            Welcome to this document analysis podcast for job %s.
            
            In today's episode, we'll explore the key findings from our comprehensive document analysis. 
            Over the next %d minutes, we'll cover the most important sections, highlight related content, 
            and discuss the insights we've discovered.
            
            Our analysis has identified several interconnected themes and topics that span across 
            the documents. We've found meaningful relationships between different sections that 
            help create a more complete understanding of the content.
            
            The document structure reveals a well-organized approach to presenting information, 
            with clear hierarchies and logical flow between topics. This organization makes it 
            easier for readers to navigate and understand the material.
            
            Key takeaways from this analysis include the importance of structured content, 
            the value of identifying related sections, and the benefits of comprehensive 
            document understanding for improved comprehension.
            
            Thank you for listening to this document analysis overview. The insights generated 
            can help you better navigate and understand the analyzed content.
            """, jobId, minutes);
    }

    private AdobeAnalysisResponse.PodcastContent generateFallbackPodcast(String jobId, int durationSeconds) {
        AdobeAnalysisResponse.PodcastContent podcast = new AdobeAnalysisResponse.PodcastContent();

        podcast.setTranscript(generateFallbackScript(jobId, durationSeconds));
        podcast.setDurationSeconds(durationSeconds);
        podcast.setKeyTopics(Arrays.asList(
                "Document Analysis",
                "Content Structure",
                "Related Sections",
                "Key Insights",
                "Information Navigation"
        ));
        podcast.setAudioUrl("/api/adobe/podcast/demo-audio/" + jobId);

        return podcast;
    }
}