package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.AnalysisRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class EnhancedDocumentAnalysisServiceTest {

    @Autowired
    private EnhancedDocumentAnalysisService enhancedService;

    @Test
    void testEnhancedAnalysis() {
        // Create test files and request
        AnalysisRequest request = new AnalysisRequest("Travel Planner",
                "Plan a trip of 4 days for a group of 10 college friends");

        // Mock files
        MultipartFile[] files = createMockPdfFiles();

        try {
            String jobId = enhancedService.submitAnalysis(request, files);
            assertNotNull(jobId);

            // Wait for processing and check results
            // Add appropriate test logic

        } catch (Exception e) {
            fail("Enhanced analysis should not throw exception: " + e.getMessage());
        }
    }

    private MultipartFile[] createMockPdfFiles() {
        // Implement mock file creation
        return new MultipartFile[0];
    }
}
