package com.adobe.hackathon.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalysisRequest {

    @NotBlank(message = "Persona is required")
    @Size(max = 200, message = "Persona must be less than 200 characters")
    private String persona;

    @NotBlank(message = "Job to be done is required")
    @Size(max = 500, message = "Job description must be less than 500 characters")
    private String jobToBeDone;

    // Constructors
    public AnalysisRequest() {}

    public AnalysisRequest(String persona, String jobToBeDone) {
        this.persona = persona;
        this.jobToBeDone = jobToBeDone;
    }

    // Getters and Setters
    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public String getJobToBeDone() { return jobToBeDone; }
    public void setJobToBeDone(String jobToBeDone) { this.jobToBeDone = jobToBeDone; }
}