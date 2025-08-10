package com.adobe.hackathon.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(PdfAnalysisService.class);

    public Map<String, Object> analyzePdfs(String jobDirectory) {
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> fileAnalyses = new ArrayList<>();

        try {
            File pdfsDir = new File(jobDirectory, "PDFs");
            if (!pdfsDir.exists() || !pdfsDir.isDirectory()) {
                logger.warn("PDFs directory not found: {}", pdfsDir.getAbsolutePath());
                analysis.put("error", "PDFs directory not found");
                return analysis;
            }

            File[] pdfFiles = pdfsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfFiles == null || pdfFiles.length == 0) {
                logger.warn("No PDF files found in: {}", pdfsDir.getAbsolutePath());
                analysis.put("error", "No PDF files found");
                return analysis;
            }

            for (File pdfFile : pdfFiles) {
                Map<String, Object> fileAnalysis = analyzeSinglePdf(pdfFile);
                fileAnalyses.add(fileAnalysis);
            }

            analysis.put("totalFiles", pdfFiles.length);
            analysis.put("files", fileAnalyses);
            analysis.put("success", true);

        } catch (Exception e) {
            logger.error("Error analyzing PDFs in directory: {}", jobDirectory, e);
            analysis.put("error", "Failed to analyze PDFs: " + e.getMessage());
            analysis.put("success", false);
        }

        return analysis;
    }

    private Map<String, Object> analyzeSinglePdf(File pdfFile) {
        Map<String, Object> fileAnalysis = new HashMap<>();
        fileAnalysis.put("filename", pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            // Extract basic metadata
            fileAnalysis.put("pageCount", document.getNumberOfPages());
            fileAnalysis.put("fileSize", pdfFile.length());

            // Extract text content
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            fileAnalysis.put("textLength", text.length());
            fileAnalysis.put("wordCount", text.split("\\s+").length);

            // Store extracted text (first 1000 characters as preview)
            String preview = text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
            fileAnalysis.put("textPreview", preview);

            // Basic content analysis
            fileAnalysis.put("containsImages", hasImages(document));
            fileAnalysis.put("success", true);

            logger.info("Analyzed PDF: {} ({} pages, {} chars)",
                    pdfFile.getName(), document.getNumberOfPages(), text.length());

        } catch (IOException e) {
            logger.error("Error analyzing PDF: {}", pdfFile.getName(), e);
            fileAnalysis.put("error", "Failed to analyze: " + e.getMessage());
            fileAnalysis.put("success", false);
        }

        return fileAnalysis;
    }

    private boolean hasImages(PDDocument document) {
        // Simplified image detection - in real implementation,
        // you would check for image XObjects in the PDF
        return false; // Placeholder
    }
}