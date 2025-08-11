package com.adobe.hackathon.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_jobs")
public class AnalysisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String jobId;

    @Column(nullable = false)
    private String persona;

    @Column(nullable = false, length = 500)
    private String jobToBeDone;

    @Column(nullable = false)
    private String status = "PENDING";

    private Double progress = 0.0;

    @Column(length = 1000)
    private String filePaths;

    @Column(length = 2000)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String result;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "analysis_type")
    private String analysisType = "standard";

    @Column(name = "sections_extracted")
    private Integer sectionsExtracted;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    // Add getters and setters
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }

    public Integer getSectionsExtracted() { return sectionsExtracted; }
    public void setSectionsExtracted(Integer sectionsExtracted) { this.sectionsExtracted = sectionsExtracted; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    // Constructors
    public AnalysisJob() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public AnalysisJob(String jobId, String persona, String jobToBeDone) {
        this();
        this.jobId = jobId;
        this.persona = persona;
        this.jobToBeDone = jobToBeDone;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public String getJobToBeDone() { return jobToBeDone; }
    public void setJobToBeDone(String jobToBeDone) { this.jobToBeDone = jobToBeDone; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) {
        this.progress = progress;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFilePaths() { return filePaths; }
    public void setFilePaths(String filePaths) { this.filePaths = filePaths; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}