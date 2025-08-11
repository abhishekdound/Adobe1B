package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DetailedAnalysisResponse {
    private Metadata metadata;

    @JsonProperty("extracted_sections")
    private List<ExtractedSection> extractedSections;

    @JsonProperty("subsection_analysis")
    private List<SubsectionAnalysis> subsectionAnalysis;

    public static class Metadata {
        @JsonProperty("test_cases_documents")
        private List<String> testCasesDocuments;

        private String persona;

        @JsonProperty("job_to_be_done")
        private String jobToBeDone;

        // Constructors
        public Metadata() {}

        public Metadata(List<String> testCasesDocuments, String persona, String jobToBeDone) {
            this.testCasesDocuments = testCasesDocuments;
            this.persona = persona;
            this.jobToBeDone = jobToBeDone;
        }

        // Getters and Setters
        public List<String> getTestCasesDocuments() { return testCasesDocuments; }
        public void setTestCasesDocuments(List<String> testCasesDocuments) { this.testCasesDocuments = testCasesDocuments; }

        public String getPersona() { return persona; }
        public void setPersona(String persona) { this.persona = persona; }

        public String getJobToBeDone() { return jobToBeDone; }
        public void setJobToBeDone(String jobToBeDone) { this.jobToBeDone = jobToBeDone; }
    }

    // Constructors
    public DetailedAnalysisResponse() {}

    // Getters and Setters
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public List<ExtractedSection> getExtractedSections() { return extractedSections; }
    public void setExtractedSections(List<ExtractedSection> extractedSections) { this.extractedSections = extractedSections; }

    public List<SubsectionAnalysis> getSubsectionAnalysis() { return subsectionAnalysis; }
    public void setSubsectionAnalysis(List<SubsectionAnalysis> subsectionAnalysis) { this.subsectionAnalysis = subsectionAnalysis; }
}