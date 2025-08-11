package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.*;
import com.adobe.hackathon.model.entity.AnalysisJob;
import com.adobe.hackathon.repository.AnalysisJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AdobeAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AdobeAnalysisService.class);

    @Autowired
    private AnalysisJobRepository jobRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EnhancedPdfAnalysisService enhancedPdfService;

    @Autowired
    private ObjectMapper objectMapper;

    // In-memory storage for quick access during demo
    private final Map<String, AdobeAnalysisResponse> analysisResults = new HashMap<>();
    private final Map<String, Map<String, Object>> documentAnalyses = new HashMap<>();

    public String submitAnalysis(AdobeAnalysisRequest request, MultipartFile[] files) throws Exception {
        String jobId = UUID.randomUUID().toString();

        // Create and save analysis job
        AnalysisJob job = new AnalysisJob(jobId, request.getPersona(), request.getJobToBeDone());
        job.setStatus("PENDING");
        job.setProgress(0.0);

        // Store files
        String filePaths = fileStorageService.storeFiles(files, jobId);
        job.setFilePaths(filePaths);

        // Save job to database
        jobRepository.save(job);

        // Start async processing
        processAdobeAnalysisAsync(jobId, request);

        logger.info("Adobe analysis job submitted with ID: {}", jobId);
        return jobId;
    }

    @Async
    public CompletableFuture<Void> processAdobeAnalysisAsync(String jobId, AdobeAnalysisRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            AnalysisJob job = jobRepository.findByJobId(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            // Update status to processing
            job.setStatus("PROCESSING");
            job.setProgress(0.1);
            jobRepository.save(job);

            // Step 1: Enhanced PDF Analysis with Adobe requirements
            logger.info("Starting Adobe PDF analysis for job: {}", jobId);
            Map<String, Object> pdfAnalysis = enhancedPdfService.analyzePdfsForAdobe(job.getFilePaths());
            job.setProgress(0.4);
            jobRepository.save(job);

            // Store for later access
            documentAnalyses.put(jobId, pdfAnalysis);

            // Step 2: Extract sections and identify related sections
            logger.info("Extracting sections and finding relationships for job: {}", jobId);
            List<PDFSectionInfo> highlightedSections = extractHighlightedSections(pdfAnalysis, request);
            List<RelatedSection> relatedSections = findRelatedSections(highlightedSections, request);
            job.setProgress(0.7);
            jobRepository.save(job);

            // Step 3: Create Adobe Analysis Response
            AdobeAnalysisResponse response = new AdobeAnalysisResponse();
            response.setJobId(jobId);
            response.setStatus("COMPLETED");
            response.setDocumentAnalysis(pdfAnalysis);
            response.setHighlightedSections(highlightedSections);
            response.setRelatedSections(relatedSections);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            // Store results
            analysisResults.put(jobId, response);

            // Save final results
            String resultJson = objectMapper.writeValueAsString(response);
            job.setResult(resultJson);
            job.setStatus("COMPLETED");
            job.setProgress(1.0);
            jobRepository.save(job);

            logger.info("Adobe analysis completed for job: {} in {}ms", jobId, response.getProcessingTimeMs());

        } catch (Exception e) {
            logger.error("Error processing Adobe analysis for job: {}", jobId, e);

            // Update job with error status
            jobRepository.findByJobId(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                job.setErrorMessage(e.getMessage());
                jobRepository.save(job);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public JobStatusResponse getJobStatus(String jobId) {
        AnalysisJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        JobStatusResponse response = new JobStatusResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        response.setProgress(job.getProgress());
        response.setPersona(job.getPersona());
        response.setJobToBeDone(job.getJobToBeDone());
        response.setErrorMessage(job.getErrorMessage());
        response.setResult(job.getResult());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());

        return response;
    }

    public List<RelatedSection> getRelatedSections(String jobId, int sectionId) {
        AdobeAnalysisResponse analysis = analysisResults.get(jobId);
        if (analysis == null) {
            throw new RuntimeException("Analysis not found for job: " + jobId);
        }

        return analysis.getRelatedSections().stream()
                .filter(rs -> rs.getSourceSection().getId() == sectionId)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDocumentOutline(String jobId) {
        Map<String, Object> documentAnalysis = documentAnalyses.get(jobId);
        if (documentAnalysis == null) {
            throw new RuntimeException("Document analysis not found for job: " + jobId);
        }

        Map<String, Object> outline = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) documentAnalysis.get("files");

        List<Map<String, Object>> documentOutlines = new ArrayList<>();

        for (Map<String, Object> file : files) {
            Map<String, Object> docOutline = new HashMap<>();
            docOutline.put("filename", file.get("filename"));
            docOutline.put("pageCount", file.get("pageCount"));

            @SuppressWarnings("unchecked")
            List<PDFSectionInfo> sections = (List<PDFSectionInfo>) file.get("sections");

            // Group sections by page for better navigation
            Map<Integer, List<PDFSectionInfo>> sectionsByPage = sections.stream()
                    .collect(Collectors.groupingBy(PDFSectionInfo::getPageNumber));

            List<Map<String, Object>> pageOutlines = new ArrayList<>();
            for (Map.Entry<Integer, List<PDFSectionInfo>> entry : sectionsByPage.entrySet()) {
                Map<String, Object> pageOutline = new HashMap<>();
                pageOutline.put("pageNumber", entry.getKey());
                pageOutline.put("sections", entry.getValue());
                pageOutlines.add(pageOutline);
            }

            docOutline.put("pages", pageOutlines);
            documentOutlines.add(docOutline);
        }

        outline.put("documents", documentOutlines);
        outline.put("totalDocuments", files.size());
        outline.put("totalSections", files.stream()
                .mapToInt(f -> ((List<?>) f.get("sections")).size())
                .sum());

        return outline;
    }

    public List<PDFSectionInfo> searchDocuments(String jobId, String query, int maxResults) {
        Map<String, Object> documentAnalysis = documentAnalyses.get(jobId);
        if (documentAnalysis == null) {
            throw new RuntimeException("Document analysis not found for job: " + jobId);
        }

        List<PDFSectionInfo> allSections = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) documentAnalysis.get("files");

        for (Map<String, Object> file : files) {
            @SuppressWarnings("unchecked")
            List<PDFSectionInfo> sections = (List<PDFSectionInfo>) file.get("sections");
            allSections.addAll(sections);
        }

        String lowerQuery = query.toLowerCase();

        return allSections.stream()
                .filter(section ->
                        section.getTitle().toLowerCase().contains(lowerQuery) ||
                                section.getContentPreview().toLowerCase().contains(lowerQuery) ||
                                section.getKeywords().stream().anyMatch(keyword ->
                                        keyword.toLowerCase().contains(lowerQuery))
                )
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPageContent(String jobId, int pageNumber) {
        Map<String, Object> documentAnalysis = documentAnalyses.get(jobId);
        if (documentAnalysis == null) {
            throw new RuntimeException("Document analysis not found for job: " + jobId);
        }

        Map<String, Object> pageContent = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) documentAnalysis.get("files");

        for (Map<String, Object> file : files) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pages = (List<Map<String, Object>>) file.get("pages");

            if (pageNumber <= pages.size()) {
                Map<String, Object> page = pages.get(pageNumber - 1);
                pageContent.put("filename", file.get("filename"));
                pageContent.put("pageNumber", pageNumber);
                pageContent.put("content", page.get("content"));
                pageContent.put("wordCount", page.get("wordCount"));
                pageContent.put("headings", page.get("headings"));

                // Get sections on this page
                @SuppressWarnings("unchecked")
                List<PDFSectionInfo> sections = (List<PDFSectionInfo>) file.get("sections");
                List<PDFSectionInfo> pageSections = sections.stream()
                        .filter(section -> section.getPageNumber() == pageNumber)
                        .collect(Collectors.toList());

                pageContent.put("sections", pageSections);
                break;
            }
        }

        return pageContent;
    }

    public Map<String, Object> exportAnalysis(String jobId, String format) {
        AdobeAnalysisResponse analysis = analysisResults.get(jobId);
        if (analysis == null) {
            throw new RuntimeException("Analysis not found for job: " + jobId);
        }

        Map<String, Object> exportData = new HashMap<>();
        exportData.put("jobId", jobId);
        exportData.put("exportFormat", format);
        exportData.put("exportTimestamp", System.currentTimeMillis());

        switch (format.toLowerCase()) {
            case "json":
                exportData.put("data", analysis);
                break;
            case "summary":
                exportData.put("data", createSummaryExport(analysis));
                break;
            case "sections":
                exportData.put("data", analysis.getHighlightedSections());
                break;
            case "related":
                exportData.put("data", analysis.getRelatedSections());
                break;
            default:
                throw new RuntimeException("Unsupported export format: " + format);
        }

        return exportData;
    }

    // Private helper methods

    private List<PDFSectionInfo> extractHighlightedSections(Map<String, Object> pdfAnalysis, AdobeAnalysisRequest request) {
        List<PDFSectionInfo> allSections = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) pdfAnalysis.get("files");

        for (Map<String, Object> file : files) {
            @SuppressWarnings("unchecked")
            List<PDFSectionInfo> sections = (List<PDFSectionInfo>) file.get("sections");
            allSections.addAll(sections);
        }

        // Filter and rank sections based on relevance
        return allSections.stream()
                .filter(section -> section.getRelevanceScore() > 0.5) // Only high relevance sections
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(10) // Top 10 most relevant sections
                .collect(Collectors.toList());
    }

    private List<RelatedSection> findRelatedSections(List<PDFSectionInfo> sections, AdobeAnalysisRequest request) {
        List<RelatedSection> relatedSections = new ArrayList<>();

        for (PDFSectionInfo section : sections) {
            List<PDFSectionInfo> related = sections.stream()
                    .filter(candidate -> !candidate.equals(section))
                    .filter(candidate -> calculateSectionSimilarity(section, candidate) > request.getSimilarityThreshold())
                    .sorted((a, b) -> Double.compare(
                            calculateSectionSimilarity(section, b),
                            calculateSectionSimilarity(section, a)
                    ))
                    .limit(request.getMaxRelatedSections())
                    .collect(Collectors.toList());

            if (!related.isEmpty()) {
                RelatedSection relatedSection = new RelatedSection();
                relatedSection.setSourceSection(section);
                relatedSection.setRelatedSections(related);
                relatedSection.setRelationshipType("content_similarity");
                relatedSection.setConfidenceScore(
                        related.stream()
                                .mapToDouble(r -> calculateSectionSimilarity(section, r))
                                .average()
                                .orElse(0.0)
                );
                relatedSection.setExplanation(generateRelationshipExplanation(section, related));

                relatedSections.add(relatedSection);
            }
        }

        return relatedSections;
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

    private String generateRelationshipExplanation(PDFSectionInfo source, List<PDFSectionInfo> related) {
        Set<String> commonKeywords = new HashSet<>(source.getKeywords());
        related.forEach(section -> commonKeywords.retainAll(section.getKeywords()));

        if (!commonKeywords.isEmpty()) {
            return "Related through shared concepts: " + String.join(", ", commonKeywords);
        } else {
            return "Related through content similarity and thematic connections";
        }
    }

    private Map<String, Object> createSummaryExport(AdobeAnalysisResponse analysis) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalSections", analysis.getHighlightedSections().size());
        summary.put("totalRelatedSections", analysis.getRelatedSections().size());
        summary.put("processingTime", analysis.getProcessingTimeMs() + "ms");

        // Average relevance score
        double avgRelevance = analysis.getHighlightedSections().stream()
                .mapToDouble(PDFSectionInfo::getRelevanceScore)
                .average()
                .orElse(0.0);
        summary.put("averageRelevanceScore", avgRelevance);

        // Top keywords
        Map<String, Long> keywordFrequency = analysis.getHighlightedSections().stream()
                .flatMap(section -> section.getKeywords().stream())
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.counting()
                ));

        List<String> topKeywords = keywordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        summary.put("topKeywords", topKeywords);

        return summary;
    }
}