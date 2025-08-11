package com.adobe.hackathon.controller;

import com.adobe.hackathon.model.dto.*;
import com.adobe.hackathon.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adobe")
@CrossOrigin(origins = "*")
public class AdobeChallengeController {

    private static final Logger logger = LoggerFactory.getLogger(AdobeChallengeController.class);

    @Autowired
    private AdobeAnalysisService adobeAnalysisService;

    @Autowired
    private InsightsBulbService insightsBulbService;

    @Autowired
    private PodcastGenerationService podcastService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Core endpoint for Adobe Challenge - Upload PDFs and get analysis with related sections
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzePdfs(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "persona", defaultValue = "student") String persona,
            @RequestParam(value = "jobToBeDone", defaultValue = "document analysis") String jobToBeDone,
            @RequestParam(value = "enableInsights", defaultValue = "false") boolean enableInsights,
            @RequestParam(value = "enablePodcast", defaultValue = "false") boolean enablePodcast) {

        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate files
            if (files == null || files.length == 0) {
                response.put("success", false);
                response.put("error", "No files provided");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file types (PDF only)
            for (MultipartFile file : files) {
                if (!file.getContentType().equals("application/pdf")) {
                    response.put("success", false);
                    response.put("error", "Only PDF files are supported");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Create analysis request
            AdobeAnalysisRequest request = new AdobeAnalysisRequest(persona, jobToBeDone);
            request.setGenerateInsights(enableInsights);
            request.setEnablePodcastMode(enablePodcast);

            // Submit analysis
            String jobId = adobeAnalysisService.submitAnalysis(request, files);

            // Wait for completion (synchronous for demo purposes)
            AdobeAnalysisResponse result = waitForAnalysisCompletion(jobId, 30000); // 30 second timeout

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("data", result);
            response.put("processingTimeMs", System.currentTimeMillis() - startTime);

            logger.info("Adobe analysis completed successfully for job: {}", jobId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in Adobe PDF analysis", e);
            response.put("success", false);
            response.put("error", "Analysis failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get related sections for a specific section
     */
    @GetMapping("/related-sections/{jobId}/{sectionId}")
    public ResponseEntity<Map<String, Object>> getRelatedSections(
            @PathVariable String jobId,
            @PathVariable int sectionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<RelatedSection> relatedSections = adobeAnalysisService.getRelatedSections(jobId, sectionId);

            response.put("success", true);
            response.put("sectionId", sectionId);
            response.put("relatedSections", relatedSections);
            response.put("count", relatedSections.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting related sections for job: {} section: {}", jobId, sectionId, e);
            response.put("success", false);
            response.put("error", "Failed to get related sections: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generate insights for analyzed document
     */
    @PostMapping("/insights/{jobId}")
    public ResponseEntity<Map<String, Object>> generateInsights(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            AdobeAnalysisResponse.InsightsBulb insights = insightsBulbService.generateInsights(jobId);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("insights", insights);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating insights for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to generate insights: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generate podcast content for analyzed document
     */
    @PostMapping("/podcast/{jobId}")
    public ResponseEntity<Map<String, Object>> generatePodcast(
            @PathVariable String jobId,
            @RequestParam(value = "duration", defaultValue = "120") int durationSeconds) {

        Map<String, Object> response = new HashMap<>();

        try {
            AdobeAnalysisResponse.PodcastContent podcast = podcastService.generatePodcast(jobId, durationSeconds);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("podcast", podcast);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating podcast for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to generate podcast: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get document outline with navigation structure
     */
    @GetMapping("/outline/{jobId}")
    public ResponseEntity<Map<String, Object>> getDocumentOutline(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> outline = adobeAnalysisService.getDocumentOutline(jobId);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("outline", outline);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting document outline for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to get document outline: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search within analyzed documents
     */
    @GetMapping("/search/{jobId}")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @PathVariable String jobId,
            @RequestParam String query,
            @RequestParam(value = "maxResults", defaultValue = "10") int maxResults) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<PDFSectionInfo> searchResults = adobeAnalysisService.searchDocuments(jobId, query, maxResults);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("query", query);
            response.put("results", searchResults);
            response.put("count", searchResults.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching documents for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to search documents: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get page content with highlighted sections
     */
    @GetMapping("/page/{jobId}/{pageNumber}")
    public ResponseEntity<Map<String, Object>> getPageContent(
            @PathVariable String jobId,
            @PathVariable int pageNumber) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> pageContent = adobeAnalysisService.getPageContent(jobId, pageNumber);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("pageNumber", pageNumber);
            response.put("content", pageContent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting page content for job: {} page: {}", jobId, pageNumber, e);
            response.put("success", false);
            response.put("error", "Failed to get page content: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Export analysis results in various formats
     */
    @GetMapping("/export/{jobId}")
    public ResponseEntity<Map<String, Object>> exportAnalysis(
            @PathVariable String jobId,
            @RequestParam(value = "format", defaultValue = "json") String format) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> exportData = adobeAnalysisService.exportAnalysis(jobId, format);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("format", format);
            response.put("data", exportData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error exporting analysis for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to export analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get analysis status and progress
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getAnalysisStatus(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobStatusResponse status = adobeAnalysisService.getJobStatus(jobId);

            response.put("success", true);
            response.put("jobId", jobId);
            response.put("status", status.getStatus());
            response.put("progress", status.getProgress());
            response.put("createdAt", status.getCreatedAt());
            response.put("updatedAt", status.getUpdatedAt());

            if ("FAILED".equals(status.getStatus())) {
                response.put("error", status.getErrorMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting analysis status for job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to get analysis status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Demo endpoint - Get sample data for frontend development
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> getDemoData() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> demoData = createDemoData();

            response.put("success", true);
            response.put("message", "Demo data for Adobe Challenge frontend development");
            response.put("data", demoData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating demo data", e);
            response.put("success", false);
            response.put("error", "Failed to generate demo data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Helper methods

    private AdobeAnalysisResponse waitForAnalysisCompletion(String jobId, long timeoutMs) throws Exception {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            JobStatusResponse status = adobeAnalysisService.getJobStatus(jobId);

            if ("COMPLETED".equals(status.getStatus())) {
                // Parse the result into AdobeAnalysisResponse
                return objectMapper.readValue(status.getResult(), AdobeAnalysisResponse.class);
            } else if ("FAILED".equals(status.getStatus())) {
                throw new RuntimeException("Analysis failed: " + status.getErrorMessage());
            }

            // Wait 500ms before checking again
            Thread.sleep(500);
        }

        throw new RuntimeException("Analysis timeout - job is still processing");
    }

    private Map<String, Object> createDemoData() {
        Map<String, Object> demo = new HashMap<>();

        // Sample document sections
        List<PDFSectionInfo> sections = Arrays.asList(
                createSampleSection(1, "Introduction to Machine Learning", 1, 0.9),
                createSampleSection(2, "Data Preprocessing Techniques", 3, 0.8),
                createSampleSection(3, "Neural Network Architectures", 5, 0.95),
                createSampleSection(4, "Training and Optimization", 8, 0.85),
                createSampleSection(5, "Model Evaluation Methods", 12, 0.75)
        );

        // Sample related sections
        List<RelatedSection> relatedSections = Arrays.asList(
                createSampleRelatedSection(sections.get(0), Arrays.asList(sections.get(1), sections.get(2))),
                createSampleRelatedSection(sections.get(2), Arrays.asList(sections.get(3), sections.get(4)))
        );

        // Sample insights
        AdobeAnalysisResponse.InsightsBulb insights = new AdobeAnalysisResponse.InsightsBulb();
        insights.setKeyInsights(Arrays.asList(
                "Machine learning models require extensive preprocessing",
                "Neural networks show superior performance on complex datasets",
                "Proper evaluation metrics are crucial for model selection"
        ));
        insights.setDidYouKnowFacts(Arrays.asList(
                "The first neural network was created in 1943",
                "Deep learning requires GPUs for efficient training",
                "Data quality is more important than quantity"
        ));

        demo.put("sections", sections);
        demo.put("relatedSections", relatedSections);
        demo.put("insights", insights);
        demo.put("totalSections", sections.size());
        demo.put("averageRelevanceScore", sections.stream()
                .mapToDouble(PDFSectionInfo::getRelevanceScore)
                .average()
                .orElse(0.0));

        return demo;
    }

    private PDFSectionInfo createSampleSection(int id, String title, int page, double relevance) {
        PDFSectionInfo section = new PDFSectionInfo();
        section.setId(id);
        section.setTitle(title);
        section.setPageNumber(page);
        section.setRelevanceScore(relevance);
        section.setKeywords(Arrays.asList("machine", "learning", "data", "model"));
        section.setContentPreview("This section covers " + title.toLowerCase() + " with detailed examples...");
        section.setSectionType("heading");
        return section;
    }

    private RelatedSection createSampleRelatedSection(PDFSectionInfo source, List<PDFSectionInfo> related) {
        RelatedSection relatedSection = new RelatedSection();
        relatedSection.setSourceSection(source);
        relatedSection.setRelatedSections(related);
        relatedSection.setRelationshipType("content_similarity");
        relatedSection.setConfidenceScore(0.85);
        relatedSection.setExplanation("Related through shared concepts and terminology");
        return relatedSection;
    }
}