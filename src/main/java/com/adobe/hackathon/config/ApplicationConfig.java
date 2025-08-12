package com.adobe.hackathon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

    private String llmProvider;
    private String googleApplicationCredentials;
    private String geminiModel;
    private String ttsProvider;
    private String azureTtsKey;
    private String azureTtsEndpoint;

    // Getters and Setters
    public String getLlmProvider() { return llmProvider; }
    public void setLlmProvider(String llmProvider) { this.llmProvider = llmProvider; }

    public String getGoogleApplicationCredentials() { return googleApplicationCredentials; }
    public void setGoogleApplicationCredentials(String credentials) { this.googleApplicationCredentials = credentials; }

    public String getGeminiModel() { return geminiModel; }
    public void setGeminiModel(String geminiModel) { this.geminiModel = geminiModel; }

    public String getTtsProvider() { return ttsProvider; }
    public void setTtsProvider(String ttsProvider) { this.ttsProvider = ttsProvider; }

    public String getAzureTtsKey() { return azureTtsKey; }
    public void setAzureTtsKey(String azureTtsKey) { this.azureTtsKey = azureTtsKey; }

    public String getAzureTtsEndpoint() { return azureTtsEndpoint; }
    public void setAzureTtsEndpoint(String azureTtsEndpoint) { this.azureTtsEndpoint = azureTtsEndpoint; }
}
