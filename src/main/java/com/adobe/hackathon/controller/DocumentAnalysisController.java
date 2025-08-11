package com.adobe.hackathon.controller;

import com.adobe.hackathon.model.dto.AnalysisRequest;
import com.adobe.hackathon.model.dto.JobStatusResponse;
import com.adobe.hackathon.service.ApplicationMetrics;
import com.adobe.hackathon.service.DocumentAnalysisService;
import com.adobe.hackathon.service.EnhancedDocumentAnalysisService;
import com.adobe.hackathon.util.ValidationUtil;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import com.adobe.hackathon.model.dto.EnhancedDetailedAnalysisResponse;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class DocumentAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalysisController.class);

    @Autowired
    private DocumentAnalysisService analysisService;

    @Autowired
    private ApplicationMetrics applicationMetrics;

    @GetMapping("/results/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobResults(@PathVariable String jobId) {
        try {
            JobStatusResponse status = analysisService.getJobStatus(jobId);

            if (!"COMPLETED".equals(status.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Job not completed yet. Status: " + status.getStatus());
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            if (status.getResult() != null) {
                Map<String, Object> results = objectMapper.readValue(status.getResult(), Map.class);
                response.put("data", results);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAnalysis(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("persona") String persona,
            @RequestParam("jobToBeDone") String jobToBeDone) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate files
            ValidationUtil.ValidationResult fileValidation = ValidationUtil.validateFiles(files);
            if (!fileValidation.isValid()) {
                response.put("success", false);
                response.put("errors", fileValidation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Validate request parameters
            ValidationUtil.ValidationResult requestValidation =
                    ValidationUtil.validateAnalysisRequest(persona, jobToBeDone);
            if (!requestValidation.isValid()) {
                response.put("success", false);
                response.put("errors", requestValidation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Create request object
            AnalysisRequest request = new AnalysisRequest(persona, jobToBeDone);

            // Submit analysis
            String jobId = analysisService.submitAnalysis(request, files);

            response.put("success", true);
            response.put("data", jobId);
            response.put("message", "Analysis job submitted successfully");

            logger.info("Analysis submitted successfully with job ID: {}", jobId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error submitting analysis", e);
            response.put("success", false);
            response.put("error", "Failed to submit analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobStatusResponse status = analysisService.getJobStatus(jobId);

            response.put("success", true);
            response.put("data", status);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error getting job status for: {}", jobId, e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Unexpected error getting job status for: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to get job status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/cancel/{jobId}")
    public ResponseEntity<Map<String, Object>> cancelJob(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            analysisService.cancelJob(jobId);

            response.put("success", true);
            response.put("message", "Job cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error cancelling job: {}", jobId, e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Unexpected error cancelling job: {}", jobId, e);
            response.put("success", false);
            response.put("error", "Failed to cancel job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = applicationMetrics.getSystemMetrics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", metrics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting system metrics", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get system metrics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "Adobe Document Analysis Service");

        return ResponseEntity.ok(response);
    }

    @Autowired
    private EnhancedDocumentAnalysisService enhancedAnalysisService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/submit-enhanced")
    public ResponseEntity<Map<String, Object>> submitEnhancedAnalysis(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("persona") String persona,
            @RequestParam("jobToBeDone") String jobToBeDone) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate files
            ValidationUtil.ValidationResult fileValidation = ValidationUtil.validateFiles(files);
            if (!fileValidation.isValid()) {
                response.put("success", false);
                response.put("errors", fileValidation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Validate request parameters
            ValidationUtil.ValidationResult requestValidation =
                    ValidationUtil.validateAnalysisRequest(persona, jobToBeDone);
            if (!requestValidation.isValid()) {
                response.put("success", false);
                response.put("errors", requestValidation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Create request object
            AnalysisRequest request = new AnalysisRequest(persona, jobToBeDone);

            // Submit enhanced analysis
            String jobId = enhancedAnalysisService.submitAnalysis(request, files);

            response.put("success", true);
            response.put("data", jobId);
            response.put("message", "Enhanced analysis job submitted successfully");
            response.put("analysisType", "enhanced");

            logger.info("Enhanced analysis submitted successfully with job ID: {}", jobId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error submitting enhanced analysis", e);
            response.put("success", false);
            response.put("error", "Failed to submit enhanced analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/results-enhanced/{jobId}")
    public ResponseEntity<Map<String, Object>> getEnhancedJobResults(@PathVariable String jobId) {
        try {
            JobStatusResponse status = enhancedAnalysisService.getJobStatus(jobId);

            if (!"COMPLETED".equals(status.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Job not completed yet. Status: " + status.getStatus());
                response.put("progress", status.getProgress());
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisType", "enhanced");

            if (status.getResult() != null) {
                try {
                    // Parse the result JSON into the enhanced response
                    EnhancedDetailedAnalysisResponse enhancedResponse = objectMapper.readValue(
                        status.getResult(), EnhancedDetailedAnalysisResponse.class);
                    
                    response.put("data", enhancedResponse);
                } catch (Exception parseError) {
                    logger.warn("Could not parse enhanced response, returning raw result: {}", parseError.getMessage());
                    // Fallback to raw result if parsing fails
                    Map<String, Object> results = objectMapper.readValue(status.getResult(), Map.class);
                    response.put("data", results);
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting enhanced job results for jobId: {}", jobId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get enhanced results: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/enhanced-direct/{jobId}")
    public ResponseEntity<Map<String, Object>> getEnhancedResultsDirect(@PathVariable String jobId) {
        try {
            // Get the enhanced response directly from the service
            JobStatusResponse status = enhancedAnalysisService.getJobStatus(jobId);
            
            if (!"COMPLETED".equals(status.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Job not completed yet. Status: " + status.getStatus());
                response.put("progress", status.getProgress());
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisType", "enhanced");
            response.put("jobId", jobId);
            response.put("persona", status.getPersona());
            response.put("jobToBeDone", status.getJobToBeDone());

            if (status.getResult() != null) {
                // The result is now directly the EnhancedDetailedAnalysisResponse
                EnhancedDetailedAnalysisResponse enhancedResponse = objectMapper.readValue(
                    status.getResult(), EnhancedDetailedAnalysisResponse.class);
                
                response.put("data", enhancedResponse);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting enhanced results directly for jobId: {}", jobId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get enhanced results: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/enhanced-status/{jobId}")
    public ResponseEntity<Map<String, Object>> getEnhancedJobStatus(@PathVariable String jobId) {
        try {
            JobStatusResponse status = enhancedAnalysisService.getJobStatus(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisType", "enhanced");
            response.put("jobId", jobId);
            response.put("status", status.getStatus());
            response.put("progress", status.getProgress());
            response.put("persona", status.getPersona());
            response.put("jobToBeDone", status.getJobToBeDone());
            response.put("submittedAt", status.getCreatedAt());
            response.put("completedAt", status.getUpdatedAt());
            
            if (status.getStatus().equals("FAILED")) {
                response.put("error", status.getErrorMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting enhanced job status for jobId: {}", jobId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get enhanced job status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/submit-enhanced-batch")
    public ResponseEntity<Map<String, Object>> submitEnhancedBatchAnalysis(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("personas") String[] personas,
            @RequestParam("jobsToBeDone") String[] jobsToBeDone) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate files
            ValidationUtil.ValidationResult fileValidation = ValidationUtil.validateFiles(files);
            if (!fileValidation.isValid()) {
                response.put("success", false);
                response.put("errors", fileValidation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Validate arrays have same length
            if (personas.length != jobsToBeDone.length) {
                response.put("success", false);
                response.put("error", "Personas and jobsToBeDone arrays must have the same length");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate each persona and job
            for (int i = 0; i < personas.length; i++) {
                ValidationUtil.ValidationResult requestValidation =
                        ValidationUtil.validateAnalysisRequest(personas[i], jobsToBeDone[i]);
                if (!requestValidation.isValid()) {
                    response.put("success", false);
                    response.put("error", "Invalid request at index " + i + ": " + requestValidation.getErrors());
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Submit batch analysis
            String[] jobIds = new String[personas.length];
            for (int i = 0; i < personas.length; i++) {
                AnalysisRequest request = new AnalysisRequest(personas[i], jobsToBeDone[i]);
                jobIds[i] = enhancedAnalysisService.submitAnalysis(request, files);
            }

            response.put("success", true);
            response.put("data", jobIds);
            response.put("message", "Enhanced batch analysis submitted successfully");
            response.put("analysisType", "enhanced-batch");
            response.put("batchSize", personas.length);

            logger.info("Enhanced batch analysis submitted successfully with {} jobs", personas.length);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error submitting enhanced batch analysis", e);
            response.put("success", false);
            response.put("error", "Failed to submit enhanced batch analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/enhanced-batch-status")
    public ResponseEntity<Map<String, Object>> getEnhancedBatchStatus(
            @RequestParam("jobIds") String[] jobIds) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisType", "enhanced-batch");
            
            Map<String, Object> batchStatus = new HashMap<>();
            int completedCount = 0;
            int failedCount = 0;
            int processingCount = 0;
            
            for (String jobId : jobIds) {
                try {
                    JobStatusResponse status = enhancedAnalysisService.getJobStatus(jobId);
                    Map<String, Object> jobInfo = new HashMap<>();
                    jobInfo.put("status", status.getStatus());
                    jobInfo.put("progress", status.getProgress());
                    jobInfo.put("persona", status.getPersona());
                    jobInfo.put("jobToBeDone", status.getJobToBeDone());
                    jobInfo.put("submittedAt", status.getCreatedAt());
                    jobInfo.put("completedAt", status.getUpdatedAt());
                    
                    if (status.getStatus().equals("COMPLETED")) {
                        completedCount++;
                    } else if (status.getStatus().equals("FAILED")) {
                        failedCount++;
                        jobInfo.put("error", status.getErrorMessage());
                    } else {
                        processingCount++;
                    }
                    
                    batchStatus.put(jobId, jobInfo);
                    
                } catch (Exception e) {
                    Map<String, Object> jobInfo = new HashMap<>();
                    jobInfo.put("status", "ERROR");
                    jobInfo.put("error", "Failed to retrieve job status: " + e.getMessage());
                    batchStatus.put(jobId, jobInfo);
                    failedCount++;
                }
            }
            
            response.put("batchStatus", batchStatus);
            response.put("summary", Map.of(
                "total", jobIds.length,
                "completed", completedCount,
                "failed", failedCount,
                "processing", processingCount
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting enhanced batch status", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get enhanced batch status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api-docs")
    public ResponseEntity<Map<String, Object>> getApiDocumentation() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Adobe Challenge 1B API Documentation");
        response.put("version", "1.0.0");
        response.put("baseUrl", "/api/analysis");
        
        Map<String, Object> endpoints = new HashMap<>();
        
        // Standard Analysis Endpoints
        Map<String, Object> standardEndpoints = new HashMap<>();
        standardEndpoints.put("submit", Map.of(
            "method", "POST",
            "path", "/submit",
            "description", "Submit standard document analysis",
            "parameters", Map.of(
                "files", "MultipartFile[] - PDF files to analyze",
                "persona", "String - Target persona",
                "jobToBeDone", "String - Job to be accomplished"
            ),
            "response", "Job ID for tracking"
        ));
        
        standardEndpoints.put("results", Map.of(
            "method", "GET",
            "path", "/results/{jobId}",
            "description", "Get standard analysis results",
            "parameters", Map.of(
                "jobId", "String - Job ID from submit"
            ),
            "response", "Analysis results data"
        ));
        
        standardEndpoints.put("status", Map.of(
            "method", "GET",
            "path", "/status/{jobId}",
            "description", "Get job status",
            "parameters", Map.of(
                "jobId", "String - Job ID from submit"
            ),
            "response", "Job status and progress"
        ));
        
        endpoints.put("standard", standardEndpoints);
        
        // Enhanced Analysis Endpoints
        Map<String, Object> enhancedEndpoints = new HashMap<>();
        enhancedEndpoints.put("submit-enhanced", Map.of(
            "method", "POST",
            "path", "/submit-enhanced",
            "description", "Submit enhanced document analysis with Python-inspired logic",
            "parameters", Map.of(
                "files", "MultipartFile[] - PDF files to analyze",
                "persona", "String - Target persona",
                "jobToBeDone", "String - Job to be accomplished"
            ),
            "response", "Job ID for tracking enhanced analysis"
        ));
        
        enhancedEndpoints.put("results-enhanced", Map.of(
            "method", "GET",
            "path", "/results-enhanced/{jobId}",
            "description", "Get enhanced analysis results",
            "parameters", Map.of(
                "jobId", "String - Job ID from submit-enhanced"
            ),
            "response", "Enhanced analysis results with detailed sections and subsections"
        ));
        
        enhancedEndpoints.put("enhanced-direct", Map.of(
            "method", "GET",
            "path", "/enhanced-direct/{jobId}",
            "description", "Get enhanced results directly (cleaner response format)",
            "parameters", Map.of(
                "jobId", "String - Job ID from submit-enhanced"
            ),
            "response", "Direct EnhancedDetailedAnalysisResponse object"
        ));
        
        enhancedEndpoints.put("enhanced-status", Map.of(
            "method", "GET",
            "path", "/enhanced-status/{jobId}",
            "description", "Get enhanced job status with detailed information",
            "parameters", Map.of(
                "jobId", "String - Job ID from submit-enhanced"
            ),
            "response", "Detailed job status for enhanced analysis"
        ));
        
        enhancedEndpoints.put("submit-enhanced-batch", Map.of(
            "method", "POST",
            "path", "/submit-enhanced-batch",
            "description", "Submit multiple enhanced analyses for different personas/jobs",
            "parameters", Map.of(
                "files", "MultipartFile[] - PDF files to analyze",
                "personas", "String[] - Array of target personas",
                "jobsToBeDone", "String[] - Array of jobs to be accomplished"
            ),
            "response", "Array of job IDs for batch processing"
        ));
        
        enhancedEndpoints.put("enhanced-batch-status", Map.of(
            "method", "GET",
            "path", "/enhanced-batch-status",
            "description", "Get status of multiple enhanced analysis jobs",
            "parameters", Map.of(
                "jobIds", "String[] - Array of job IDs to check"
            ),
            "response", "Batch status summary and individual job details"
        ));
        
        endpoints.put("enhanced", enhancedEndpoints);
        
        // Utility Endpoints
        Map<String, Object> utilityEndpoints = new HashMap<>();
        utilityEndpoints.put("cancel", Map.of(
            "method", "DELETE",
            "path", "/cancel/{jobId}",
            "description", "Cancel a running analysis job",
            "parameters", Map.of(
                "jobId", "String - Job ID to cancel"
            ),
            "response", "Cancellation confirmation"
        ));
        
        utilityEndpoints.put("metrics", Map.of(
            "method", "GET",
            "path", "/metrics",
            "description", "Get system performance metrics",
            "parameters", "None",
            "response", "System metrics and statistics"
        ));
        
        utilityEndpoints.put("health", Map.of(
            "method", "GET",
            "path", "/health",
            "description", "Health check endpoint",
            "parameters", "None",
            "response", "System health status"
        ));
        
        endpoints.put("utility", utilityEndpoints);
        
        response.put("endpoints", endpoints);
        response.put("features", Map.of(
            "standard_analysis", "Basic document analysis with PDF processing",
            "enhanced_analysis", "Advanced analysis with Python-inspired section extraction",
            "batch_processing", "Process multiple analysis requests simultaneously",
            "async_processing", "Non-blocking analysis with job tracking",
            "progress_tracking", "Real-time job progress monitoring",
            "error_handling", "Comprehensive error reporting and validation"
        ));
        
        return ResponseEntity.ok(response);
    }
}