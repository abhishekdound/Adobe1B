package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.AnalysisRequest;
import com.adobe.hackathon.model.dto.JobStatusResponse;
import com.adobe.hackathon.model.entity.AnalysisJob;
import com.adobe.hackathon.repository.AnalysisJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalysisService.class);

    @Autowired
    private AnalysisJobRepository jobRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PdfAnalysisService pdfAnalysisService;

    @Autowired
    private SemanticAnalysisService semanticAnalysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String submitAnalysis(AnalysisRequest request, MultipartFile[] files) throws Exception {
        // Generate unique job ID
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
        processAnalysisAsync(jobId);

        logger.info("Analysis job submitted with ID: {}", jobId);
        return jobId;
    }

    @Async
    public CompletableFuture<Void> processAnalysisAsync(String jobId) {
        try {
            AnalysisJob job = jobRepository.findByJobId(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            // Update status to processing
            job.setStatus("PROCESSING");
            job.setProgress(0.1);
            jobRepository.save(job);

            // Step 1: PDF Analysis
            logger.info("Starting PDF analysis for job: {}", jobId);
            Map<String, Object> pdfAnalysis = pdfAnalysisService.analyzePdfs(job.getFilePaths());
            job.setProgress(0.5);
            jobRepository.save(job);

            // Step 2: Semantic Analysis
            logger.info("Starting semantic analysis for job: {}", jobId);
            Map<String, Object> semanticAnalysis = semanticAnalysisService.performSemanticAnalysis(
                    pdfAnalysis, job.getPersona(), job.getJobToBeDone());
            job.setProgress(0.8);
            jobRepository.save(job);

            // Step 3: Combine results
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("pdfAnalysis", pdfAnalysis);
            finalResult.put("semanticAnalysis", semanticAnalysis);
            finalResult.put("jobId", jobId);
            finalResult.put("persona", job.getPersona());
            finalResult.put("jobToBeDone", job.getJobToBeDone());

            // Save final results
            String resultJson = objectMapper.writeValueAsString(finalResult);
            job.setResult(resultJson);
            job.setStatus("COMPLETED");
            job.setProgress(1.0);
            jobRepository.save(job);

            logger.info("Analysis completed for job: {}", jobId);

        } catch (Exception e) {
            logger.error("Error processing analysis for job: {}", jobId, e);

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

    public void cancelJob(String jobId) {
        AnalysisJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        if ("PENDING".equals(job.getStatus()) || "PROCESSING".equals(job.getStatus())) {
            job.setStatus("CANCELLED");
            jobRepository.save(job);

            // Clean up files
            if (job.getFilePaths() != null) {
                fileStorageService.deleteJobFiles(job.getFilePaths());
            }

            logger.info("Job cancelled: {}", jobId);
        } else {
            throw new RuntimeException("Cannot cancel job in status: " + job.getStatus());
        }
    }
}