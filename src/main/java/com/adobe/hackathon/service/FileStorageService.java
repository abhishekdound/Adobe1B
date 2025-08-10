package com.adobe.hackathon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.file.storage.upload-dir:./uploads}")
    private String uploadDir;

    public String storeFiles(MultipartFile[] files, String jobId) throws IOException {
        // Create job-specific directory
        String jobDirectory = uploadDir + "/" + jobId;
        Path jobPath = Paths.get(jobDirectory);
        Files.createDirectories(jobPath);

        // Create PDFs subdirectory
        Path pdfsPath = jobPath.resolve("PDFs");
        Files.createDirectories(pdfsPath);

        List<String> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String filename = UUID.randomUUID().toString() + "_" + originalFilename;
                Path filePath = pdfsPath.resolve(filename);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                savedFiles.add(filename);

                logger.info("Stored file: {} as {}", originalFilename, filename);
            }
        }

        logger.info("Stored {} files for job: {}", savedFiles.size(), jobId);
        return jobDirectory;
    }

    public void deleteJobFiles(String jobDirectory) {
        try {
            Path jobPath = Paths.get(jobDirectory);
            if (Files.exists(jobPath)) {
                Files.walk(jobPath)
                        .sorted((path1, path2) -> path2.compareTo(path1)) // Delete files before directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                logger.warn("Failed to delete: {}", path, e);
                            }
                        });
                logger.info("Deleted job directory: {}", jobDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to delete job directory: {}", jobDirectory, e);
        }
    }

    // Additional methods from the provided code...
}