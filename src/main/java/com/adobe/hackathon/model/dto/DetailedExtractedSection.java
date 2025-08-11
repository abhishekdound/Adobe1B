// DetailedExtractedSection.java
package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailedExtractedSection {
    private String document;

    @JsonProperty("section_title")
    private String sectionTitle;

    @JsonProperty("importance_rank")
    private int importanceRank;

    @JsonProperty("relevance_score")
    private Double relevanceScore;

    @JsonProperty("page_number")
    private int pageNumber;

    @JsonProperty("section_type")
    private String sectionType;

    @JsonProperty("word_count")
    private int wordCount;

    @JsonProperty("key_topics")
    private List<String> keyTopics;

    @JsonProperty("student_relevance")
    private String studentRelevance;

    @JsonProperty("group_applicability")
    private String groupApplicability;

    @JsonProperty("extraction_confidence")
    private Double extractionConfidence;

    @JsonProperty("related_sections")
    private List<String> relatedSections;

    // Constructors
    public DetailedExtractedSection() {}

    public DetailedExtractedSection(String document, String sectionTitle, int importanceRank, int pageNumber) {
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

    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getSectionType() { return sectionType; }
    public void setSectionType(String sectionType) { this.sectionType = sectionType; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public List<String> getKeyTopics() { return keyTopics; }
    public void setKeyTopics(List<String> keyTopics) { this.keyTopics = keyTopics; }

    public String getStudentRelevance() { return studentRelevance; }
    public void setStudentRelevance(String studentRelevance) { this.studentRelevance = studentRelevance; }

    public String getGroupApplicability() { return groupApplicability; }
    public void setGroupApplicability(String groupApplicability) { this.groupApplicability = groupApplicability; }

    public Double getExtractionConfidence() { return extractionConfidence; }
    public void setExtractionConfidence(Double extractionConfidence) { this.extractionConfidence = extractionConfidence; }

    public List<String> getRelatedSections() { return relatedSections; }
    public void setRelatedSections(List<String> relatedSections) { this.relatedSections = relatedSections; }
}

