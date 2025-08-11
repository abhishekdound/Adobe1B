package com.adobe.hackathon.model.dto;

public class AdobeAnalysisRequest extends AnalysisRequest {
    private boolean enableRelatedSections = true;
    private boolean generateInsights = false;
    private boolean enablePodcastMode = false;
    private int maxRelatedSections = 3;
    private double similarityThreshold = 0.3;

    public AdobeAnalysisRequest() {
        super();
    }

    public AdobeAnalysisRequest(String persona, String jobToBeDone) {
        super(persona, jobToBeDone);
    }

    // Getters and Setters
    public boolean isEnableRelatedSections() { return enableRelatedSections; }
    public void setEnableRelatedSections(boolean enableRelatedSections) { this.enableRelatedSections = enableRelatedSections; }

    public boolean isGenerateInsights() { return generateInsights; }
    public void setGenerateInsights(boolean generateInsights) { this.generateInsights = generateInsights; }

    public boolean isEnablePodcastMode() { return enablePodcastMode; }
    public void setEnablePodcastMode(boolean enablePodcastMode) { this.enablePodcastMode = enablePodcastMode; }

    public int getMaxRelatedSections() { return maxRelatedSections; }
    public void setMaxRelatedSections(int maxRelatedSections) { this.maxRelatedSections = maxRelatedSections; }

    public double getSimilarityThreshold() { return similarityThreshold; }
    public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
}