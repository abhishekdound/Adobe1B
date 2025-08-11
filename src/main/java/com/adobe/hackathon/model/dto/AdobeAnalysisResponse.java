package com.adobe.hackathon.model.dto;

import java.util.List;
import java.util.Map;

public class AdobeAnalysisResponse {
    private String jobId;
    private String status;
    private Map<String, Object> documentAnalysis;
    private List<PDFSectionInfo> highlightedSections;
    private List<RelatedSection> relatedSections;
    private InsightsBulb insights;
    private PodcastContent podcastContent;
    private long processingTimeMs;

    // Nested classes
    public static class InsightsBulb {
        private List<String> keyInsights;
        private List<String> didYouKnowFacts;
        private List<String> contradictions;
        private List<String> connections;

        // Getters and Setters
        public List<String> getKeyInsights() { return keyInsights; }
        public void setKeyInsights(List<String> keyInsights) { this.keyInsights = keyInsights; }

        public List<String> getDidYouKnowFacts() { return didYouKnowFacts; }
        public void setDidYouKnowFacts(List<String> didYouKnowFacts) { this.didYouKnowFacts = didYouKnowFacts; }

        public List<String> getContradictions() { return contradictions; }
        public void setContradictions(List<String> contradictions) { this.contradictions = contradictions; }

        public List<String> getConnections() { return connections; }
        public void setConnections(List<String> connections) { this.connections = connections; }
    }

    public static class PodcastContent {
        private String audioUrl;
        private String transcript;
        private int durationSeconds;
        private List<String> keyTopics;

        // Getters and Setters
        public String getAudioUrl() { return audioUrl; }
        public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }

        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

        public List<String> getKeyTopics() { return keyTopics; }
        public void setKeyTopics(List<String> keyTopics) { this.keyTopics = keyTopics; }
    }

    // Main class Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getDocumentAnalysis() { return documentAnalysis; }
    public void setDocumentAnalysis(Map<String, Object> documentAnalysis) { this.documentAnalysis = documentAnalysis; }

    public List<PDFSectionInfo> getHighlightedSections() { return highlightedSections; }
    public void setHighlightedSections(List<PDFSectionInfo> highlightedSections) { this.highlightedSections = highlightedSections; }

    public List<RelatedSection> getRelatedSections() { return relatedSections; }
    public void setRelatedSections(List<RelatedSection> relatedSections) { this.relatedSections = relatedSections; }

    public InsightsBulb getInsights() { return insights; }
    public void setInsights(InsightsBulb insights) { this.insights = insights; }

    public PodcastContent getPodcastContent() { return podcastContent; }
    public void setPodcastContent(PodcastContent podcastContent) { this.podcastContent = podcastContent; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}