// ValidationUtil.java
package com.adobe.hackathon.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final String[] ALLOWED_CONTENT_TYPES = {"application/pdf"};

    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.valid = true;
        }

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }

    public static ValidationResult validateFiles(MultipartFile[] files) {
        ValidationResult result = new ValidationResult();

        if (files == null || files.length == 0) {
            result.addError("No files provided");
            return result;
        }

        if (files.length > 10) {
            result.addError("Maximum 10 files allowed");
        }

        for (MultipartFile file : files) {
            validateSingleFile(file, result);
        }

        return result;
    }

    private static void validateSingleFile(MultipartFile file, ValidationResult result) {
        if (file.isEmpty()) {
            result.addError("Empty file: " + file.getOriginalFilename());
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            result.addError("File too large: " + file.getOriginalFilename() +
                    " (" + (file.getSize() / 1024 / 1024) + "MB > 100MB)");
        }

        String contentType = file.getContentType();
        boolean validType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }

        if (!validType) {
            result.addError("Invalid file type: " + file.getOriginalFilename() +
                    " (must be PDF)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            result.addError("Invalid file extension: " + filename + " (must end with .pdf)");
        }
    }

    public static ValidationResult validateAnalysisRequest(String persona, String jobToBeDone) {
        ValidationResult result = new ValidationResult();

        if (persona == null || persona.trim().isEmpty()) {
            result.addError("Persona is required");
        } else if (persona.length() > 200) {
            result.addError("Persona must be less than 200 characters");
        }

        if (jobToBeDone == null || jobToBeDone.trim().isEmpty()) {
            result.addError("Job to be done is required");
        } else if (jobToBeDone.length() > 500) {
            result.addError("Job description must be less than 500 characters");
        }

        return result;
    }
}