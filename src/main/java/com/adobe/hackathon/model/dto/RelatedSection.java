// RelatedSection.java
package com.adobe.hackathon.model.dto;

import java.util.List;

public class RelatedSection {
    private PDFSectionInfo sourceSection;
    private List<PDFSectionInfo> relatedSections;
    private String relationshipType;
    private double confidenceScore;
    private String explanation;

    // Constructors
    public RelatedSection() {}

    // Getters and Setters
    public PDFSectionInfo getSourceSection() { return sourceSection; }
    public void setSourceSection(PDFSectionInfo sourceSection) { this.sourceSection = sourceSection; }

    public List<PDFSectionInfo> getRelatedSections() { return relatedSections; }
    public void setRelatedSections(List<PDFSectionInfo> relatedSections) { this.relatedSections = relatedSections; }

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}