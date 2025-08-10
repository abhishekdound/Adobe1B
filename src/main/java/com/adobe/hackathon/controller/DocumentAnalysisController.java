package com.adobe.hackathon.controller;

import com.adobe.hackathon.model.dto.AnalysisRequest;
import com.adobe.hackathon.model.dto.JobStatusResponse;
import com.adobe.hackathon.service.ApplicationMetrics;
import com.adobe.hackathon.service.DocumentAnalysisService;
import com.adobe.hackathon.util.ValidationUtil;

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
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> results = mapper.readValue(status.getResult(), Map.class);
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
}