package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailedSubsectionAnalysis {
    private String document;

    @JsonProperty("refined_text")
    private String refinedText;

    @JsonProperty("page_number")
    private int pageNumber;

    @JsonProperty("analysis_details")
    private Map<String, Object> analysisDetails;

    // Constructors
    public DetailedSubsectionAnalysis() {}

    public DetailedSubsectionAnalysis(String document, String refinedText, int pageNumber) {
        this.document = document;
        this.refinedText = refinedText;
        this.pageNumber = pageNumber;
    }

    // Getters and Setters
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getRefinedText() { return refinedText; }
    public void setRefinedText(String refinedText) { this.refinedText = refinedText; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public Map<String, Object> getAnalysisDetails() { return analysisDetails; }
    public void setAnalysisDetails(Map<String, Object> analysisDetails) { this.analysisDetails = analysisDetails; }
}
