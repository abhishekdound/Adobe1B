package com.adobe.hackathon.model.dto;

import java.time.LocalDateTime;

public class JobStatusResponse {

    private String jobId;
    private String status;
    private Double progress;
    private String persona;
    private String jobToBeDone;
    private String errorMessage;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public JobStatusResponse() {}

    public JobStatusResponse(String jobId, String status, Double progress) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public String getJobToBeDone() { return jobToBeDone; }
    public void setJobToBeDone(String jobToBeDone) { this.jobToBeDone = jobToBeDone; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}