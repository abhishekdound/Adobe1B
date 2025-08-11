package com.adobe.hackathon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);

    @Value("${tts.provider:azure}")
    private String ttsProvider;

    @Value("${tts.api.key:#{environment.AZURE_TTS_KEY}}")
    private String apiKey;

    @Value("${tts.endpoint:#{environment.AZURE_TTS_ENDPOINT}}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAudio(String text, String jobId) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("No TTS API key configured, returning demo audio URL");
            return "/api/adobe/podcast/demo-audio/" + jobId;
        }

        switch (ttsProvider.toLowerCase()) {
            case "azure":
                return generateAzureTTS(text, jobId);
            case "google":
                return generateGoogleTTS(text, jobId);
            default:
                logger.warn("Unknown TTS provider: {}, returning demo URL", ttsProvider);
                return "/api/adobe/podcast/demo-audio/" + jobId;
        }
    }

    private String generateAzureTTS(String text, String jobId) throws Exception {
        String url = endpoint + "/cognitiveservices/v1";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/ssml+xml"));
        headers.set("Ocp-Apim-Subscription-Key", apiKey);
        headers.set("X-Microsoft-OutputFormat", "audio-24khz-48kbitrate-mono-mp3");
        headers.set("User-Agent", "Adobe-Hackathon-TTS");

        String ssml = String.format("""
            <speak version='1.0' xml:lang='en-US'>
                <voice xml:lang='en-US' xml:gender='Female' name='en-US-JennyNeural'>
                    %s
                </voice>
            </speak>
            """, text);

        HttpEntity<String> entity = new HttpEntity<>(ssml, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // In a real implementation, you would save the audio file and return its URL
                // For demo purposes, return a placeholder URL
                return "/api/adobe/podcast/audio/" + jobId + ".mp3";
            } else {
                throw new RuntimeException("Azure TTS failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling Azure TTS", e);
            return "/api/adobe/podcast/demo-audio/" + jobId;
        }
    }

    private String generateGoogleTTS(String text, String jobId) throws Exception {
        // Similar implementation for Google TTS
        logger.warn("Google TTS not implemented, returning demo URL");
        return "/api/adobe/podcast/demo-audio/" + jobId;
    }
}