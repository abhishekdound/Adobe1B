
package com.adobe.hackathon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitoring service to ensure Adobe Challenge requirements
 * - Base app must run on CPU with ≤ 10 sec response time
 * - Navigation should complete in < 2 seconds
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    
    private final Map<String, Long> operationStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> operationCounts = new ConcurrentHashMap<>();
    
    // Performance thresholds as per Adobe requirements
    private static final long ANALYSIS_THRESHOLD_MS = 10000; // 10 seconds for analysis
    private static final long NAVIGATION_THRESHOLD_MS = 2000; // 2 seconds for navigation
    
    public void startOperation(String operationId, String operationType) {
        operationStartTimes.put(operationId, System.currentTimeMillis());
        operationCounts.merge(operationType, 1, Integer::sum);
        logger.debug("Started operation: {} ({})", operationId, operationType);
    }
    
    public boolean checkPerformanceThreshold(String operationId, String operationType) {
        Long startTime = operationStartTimes.get(operationId);
        if (startTime == null) {
            return false;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        long threshold = getThresholdForOperation(operationType);
        
        if (duration > threshold) {
            logger.warn("Performance threshold exceeded for {}: {}ms > {}ms", 
                operationId, duration, threshold);
            return false;
        }
        
        logger.info("Operation {} completed within threshold: {}ms < {}ms", 
            operationId, duration, threshold);
        return true;
    }
    
    public long endOperation(String operationId, String operationType) {
        Long startTime = operationStartTimes.remove(operationId);
        if (startTime == null) {
            return -1;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        long threshold = getThresholdForOperation(operationType);
        
        if (duration > threshold) {
            logger.error("PERFORMANCE VIOLATION: {} took {}ms (threshold: {}ms)", 
                operationType, duration, threshold);
        } else {
            logger.info("Performance OK: {} completed in {}ms", operationType, duration);
        }
        
        return duration;
    }
    
    private long getThresholdForOperation(String operationType) {
        switch (operationType.toLowerCase()) {
            case "analysis":
            case "pdf_processing":
                return ANALYSIS_THRESHOLD_MS;
            case "navigation":
            case "section_lookup":
            case "related_sections":
                return NAVIGATION_THRESHOLD_MS;
            default:
                return NAVIGATION_THRESHOLD_MS; // Default to stricter requirement
        }
    }
    
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("activeOperations", operationStartTimes.size());
        metrics.put("operationCounts", new ConcurrentHashMap<>(operationCounts));
        metrics.put("thresholds", Map.of(
            "analysis_ms", ANALYSIS_THRESHOLD_MS,
            "navigation_ms", NAVIGATION_THRESHOLD_MS
        ));
        return metrics;
    }
}
