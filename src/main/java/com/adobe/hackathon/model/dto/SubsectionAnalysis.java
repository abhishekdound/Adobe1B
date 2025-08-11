package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubsectionAnalysis {
    private String document;

    @JsonProperty("refined_text")
    private String refinedText;

    @JsonProperty("page_number")
    private int pageNumber;

    // Constructors
    public SubsectionAnalysis() {}

    public SubsectionAnalysis(String document, String refinedText, int pageNumber) {
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
}