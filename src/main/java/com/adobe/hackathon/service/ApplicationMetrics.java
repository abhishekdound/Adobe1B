// ApplicationMetrics.java
package com.adobe.hackathon.service;

import com.adobe.hackathon.repository.AnalysisJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationMetrics {

    @Autowired
    private AnalysisJobRepository jobRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Job metrics
        metrics.put("totalJobs", jobRepository.count());
        metrics.put("pendingJobs", jobRepository.countByStatus("PENDING"));
        metrics.put("processingJobs", jobRepository.countByStatus("PROCESSING"));
        metrics.put("completedJobs", jobRepository.countByStatus("COMPLETED"));
        metrics.put("failedJobs", jobRepository.countByStatus("FAILED"));

        // System metrics
        Runtime runtime = Runtime.getRuntime();
        metrics.put("totalMemory", runtime.totalMemory());
        metrics.put("freeMemory", runtime.freeMemory());
        metrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("maxMemory", runtime.maxMemory());

        // Calculate memory usage percentage
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        metrics.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);

        // Performance metrics
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("uptime", getUptime());

        return metrics;
    }

    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> performance = new HashMap<>();

        // Job processing times (would need to be tracked in real implementation)
        performance.put("averageProcessingTime", calculateAverageProcessingTime());
        performance.put("successRate", calculateSuccessRate());
        performance.put("throughput", calculateThroughput());

        return performance;
    }

    private long getUptime() {
        return System.currentTimeMillis() - getApplicationStartTime();
    }

    private long getApplicationStartTime() {
        // This would be set when the application starts
        // For now, return a reasonable default
        return System.currentTimeMillis() - (60000 * 30); // 30 minutes ago
    }

    private double calculateAverageProcessingTime() {
        // Implementation would query completed jobs and calculate average
        // For now, return a placeholder
        return 120.5; // seconds
    }

    private double calculateSuccessRate() {
        long total = jobRepository.count();
        long completed = jobRepository.countByStatus("COMPLETED");

        if (total == 0) return 100.0;

        return (double) completed / total * 100;
    }

    private double calculateThroughput() {
        // Jobs per hour (placeholder implementation)
        long completedJobs = jobRepository.countByStatus("COMPLETED");
        return completedJobs / 24.0; // jobs per hour over last 24 hours
    }
}