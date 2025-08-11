package com.adobe.hackathon.model.dto;

import java.util.List;

public class PDFSectionInfo {
    private int id;
    private String title;
    private int pageNumber;
    private int startPosition;
    private double relevanceScore;
    private List<String> keywords;
    private String contentPreview;
    private String sectionType;

    // Constructors
    public PDFSectionInfo() {}

    public PDFSectionInfo(String title, int pageNumber) {
        this.title = title;
        this.pageNumber = pageNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getStartPosition() { return startPosition; }
    public void setStartPosition(int startPosition) { this.startPosition = startPosition; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }

    public String getSectionType() { return sectionType; }
    public void setSectionType(String sectionType) { this.sectionType = sectionType; }
}

