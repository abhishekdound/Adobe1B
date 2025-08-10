// DocumentAnalysisServiceTest.java
package com.adobe.hackathon.service;

import com.adobe.hackathon.model.dto.AnalysisRequest;
import com.adobe.hackathon.model.entity.AnalysisJob;
import com.adobe.hackathon.repository.AnalysisJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentAnalysisServiceTest {

    @Mock
    private AnalysisJobRepository jobRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PdfAnalysisService pdfAnalysisService;

    @Mock
    private SemanticAnalysisService semanticAnalysisService;

    @InjectMocks
    private DocumentAnalysisService documentAnalysisService;

    private AnalysisRequest testRequest;
    private MultipartFile[] testFiles;

    @BeforeEach
    void setUp() {
        testRequest = new AnalysisRequest("Data Scientist", "Extract ML techniques");
        testFiles = new MultipartFile[]{
                new MockMultipartFile("test.pdf", "test.pdf", "application/pdf", "content".getBytes())
        };
    }

    @Test
    void submitAnalysis_Success() throws Exception {
        // Given
        AnalysisJob savedJob = new AnalysisJob("job-id", "Data Scientist", "Extract ML techniques");
        when(jobRepository.save(any(AnalysisJob.class))).thenReturn(savedJob);
        when(fileStorageService.storeFiles(any(), anyString())).thenReturn("/path/to/files");

        // When
        String result = documentAnalysisService.submitAnalysis(testRequest, testFiles);

        // Then
        assertNotNull(result);
        verify(jobRepository).save(any(AnalysisJob.class));
        verify(fileStorageService).storeFiles(eq(testFiles), anyString());
    }

    @Test
    void getJobStatus_JobExists() {
        // Given
        String jobId = "test-job-id";
        AnalysisJob job = new AnalysisJob(jobId, "Data Scientist", "Extract ML techniques");
        job.setStatus("COMPLETED");
        job.setProgress(1.0);

        when(jobRepository.findByJobId(jobId)).thenReturn(Optional.of(job));

        // When
        var status = documentAnalysisService.getJobStatus(jobId);

        // Then
        assertEquals(jobId, status.getJobId());
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(1.0, status.getProgress());
    }

    @Test
    void getJobStatus_JobNotFound() {
        // Given
        String jobId = "non-existent-job";
        when(jobRepository.findByJobId(jobId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            documentAnalysisService.getJobStatus(jobId);
        });
    }
}