// DocumentAnalysisControllerTest.java
package com.adobe.hackathon.controller;

import com.adobe.hackathon.service.DocumentAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockitoBean ;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentAnalysisController.class)
class DocumentAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private DocumentAnalysisService analysisService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile testPdfFile;

    @BeforeEach
    void setUp() {
        testPdfFile = new MockMultipartFile(
                "files",
                "test.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );
    }

    @Test
    void submitAnalysis_Success() throws Exception {
        String jobId = "test-job-id";
        when(analysisService.submitAnalysis(any(), any())).thenReturn(jobId);

        mockMvc.perform(multipart("/api/analysis/submit")
                        .file(testPdfFile)
                        .param("persona", "Data Scientist")
                        .param("jobToBeDone", "Extract ML techniques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(jobId));
    }

    @Test
    void submitAnalysis_NoFiles() throws Exception {
        mockMvc.perform(multipart("/api/analysis/submit")
                        .param("persona", "Data Scientist")
                        .param("jobToBeDone", "Extract ML techniques"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJobStatus_Success() throws Exception {
        // Test implementation for job status endpoint
        mockMvc.perform(get("/api/analysis/status/test-job-id"))
                .andExpect(status().isOk());
    }
}