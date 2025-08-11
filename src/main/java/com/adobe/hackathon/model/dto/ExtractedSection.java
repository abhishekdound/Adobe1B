package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractedSection {
    private String document;

    @JsonProperty("section_title")
    private String sectionTitle;

    @JsonProperty("importance_rank")
    private int importanceRank;

    @JsonProperty("page_number")
    private int pageNumber;

    // Constructors
    public ExtractedSection() {}

    public ExtractedSection(String document, String sectionTitle, int importanceRank, int pageNumber) {
        this.document = document;
        this.sectionTitle = sectionTitle;
        this.importanceRank = importanceRank;
        this.pageNumber = pageNumber;
    }

    // Getters and Setters
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    public int getImportanceRank() { return importanceRank; }
    public void setImportanceRank(int importanceRank) { this.importanceRank = importanceRank; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
}