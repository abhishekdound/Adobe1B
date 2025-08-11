package com.adobe.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedDetailedAnalysisResponse {

    private AnalysisSummary analysisSummary;

    private EnhancedMetadata metadata;

    private ExtractionMethodology extractionMethodology;

    private List<DetailedExtractedSection> extractedSections;

    private ContentAnalysis contentAnalysis;

    private List<DetailedSubsectionAnalysis> subsectionAnalysis;

    private Recommendations recommendations;

    private ProcessingStatistics processingStatistics;

    private VersionInfo versionInfo;

    // Default constructor
    public EnhancedDetailedAnalysisResponse() {
        // Initialize version info with default values
        this.versionInfo = new VersionInfo();
        this.versionInfo.setOutputFormatVersion("1.0.0");
        this.versionInfo.setLastUpdated(LocalDateTime.now());
        this.versionInfo.setBackwardCompatibility("v1.0");
        this.versionInfo.setSchemaValidation("valid");
    }

    public static class AnalysisSummary {
        private LocalDateTime executionTimestamp;

        private Long processingDurationMs;

        private Integer totalDocumentsProcessed;

        private Integer totalSectionsExtracted;

        private Long totalCharactersAnalyzed;

        private Double analysisConfidenceScore;

        private String modelVersion;

        // Constructors
        public AnalysisSummary() {
            this.executionTimestamp = LocalDateTime.now();
            this.modelVersion = "document-analyzer-v3.0.0";
        }

        // Getters and Setters
        public LocalDateTime getExecutionTimestamp() { return executionTimestamp; }
        public void setExecutionTimestamp(LocalDateTime executionTimestamp) { this.executionTimestamp = executionTimestamp; }

        public Long getProcessingDurationMs() { return processingDurationMs; }
        public void setProcessingDurationMs(Long processingDurationMs) { this.processingDurationMs = processingDurationMs; }

        public Integer getTotalDocumentsProcessed() { return totalDocumentsProcessed; }
        public void setTotalDocumentsProcessed(Integer totalDocumentsProcessed) { this.totalDocumentsProcessed = totalDocumentsProcessed; }

        public Integer getTotalSectionsExtracted() { return totalSectionsExtracted; }
        public void setTotalSectionsExtracted(Integer totalSectionsExtracted) { this.totalSectionsExtracted = totalSectionsExtracted; }

        public Long getTotalCharactersAnalyzed() { return totalCharactersAnalyzed; }
        public void setTotalCharactersAnalyzed(Long totalCharactersAnalyzed) { this.totalCharactersAnalyzed = totalCharactersAnalyzed; }

        public Double getAnalysisConfidenceScore() { return analysisConfidenceScore; }
        public void setAnalysisConfidenceScore(Double analysisConfidenceScore) { this.analysisConfidenceScore = analysisConfidenceScore; }

        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    }

    public static class EnhancedMetadata {
        private List<DocumentInfo> testCasesDocuments;

        private PersonaInfo persona;

        private JobInfo jobToBeDone;

        public static class DocumentInfo {
            private String filename;
            private Long fileSizeKb;
            private Integer pageCount;
            private String creationDate;
            private Integer sectionsFound;
            private Integer wordCount;
            private String processingNotes;

            // Constructors and getters/setters
            public DocumentInfo() {}

            public String getFilename() { return filename; }
            public void setFilename(String filename) { this.filename = filename; }

            public Long getFileSizeKb() { return fileSizeKb; }
            public void setFileSizeKb(Long fileSizeKb) { this.fileSizeKb = fileSizeKb; }

            public Integer getPageCount() { return pageCount; }
            public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

            public String getCreationDate() { return creationDate; }
            public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

            public Integer getSectionsFound() { return sectionsFound; }
            public void setSectionsFound(Integer sectionsFound) { this.sectionsFound = sectionsFound; }

            public Integer getWordCount() { return wordCount; }
            public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

            public String getProcessingNotes() { return processingNotes; }
            public void setProcessingNotes(String processingNotes) { this.processingNotes = processingNotes; }
        }

        public static class PersonaInfo {
            private String role;
            private String expertiseLevel;
            private List<String> specializations;
            private String targetDemographic;

            // Constructors and getters/setters
            public PersonaInfo() {}

            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }

            public String getExpertiseLevel() { return expertiseLevel; }
            public void setExpertiseLevel(String expertiseLevel) { this.expertiseLevel = expertiseLevel; }

            public List<String> getSpecializations() { return specializations; }
            public void setSpecializations(List<String> specializations) { this.specializations = specializations; }

            public String getTargetDemographic() { return targetDemographic; }
            public void setTargetDemographic(String targetDemographic) { this.targetDemographic = targetDemographic; }
        }

        public static class JobInfo {
            private String primaryTask;
            private Map<String, Object> constraints;
            private List<String> successCriteria;

            // Constructors and getters/setters
            public JobInfo() {}

            public String getPrimaryTask() { return primaryTask; }
            public void setPrimaryTask(String primaryTask) { this.primaryTask = primaryTask; }

            public Map<String, Object> getConstraints() { return constraints; }
            public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }

            public List<String> getSuccessCriteria() { return successCriteria; }
            public void setSuccessCriteria(List<String> successCriteria) { this.successCriteria = successCriteria; }
        }

        // Getters and Setters for EnhancedMetadata
        public List<DocumentInfo> getTestCasesDocuments() { return testCasesDocuments; }
        public void setTestCasesDocuments(List<DocumentInfo> testCasesDocuments) { this.testCasesDocuments = testCasesDocuments; }

        public PersonaInfo getPersona() { return persona; }
        public void setPersona(PersonaInfo persona) { this.persona = persona; }

        public JobInfo getJobToBeDone() { return jobToBeDone; }
        public void setJobToBeDone(JobInfo jobToBeDone) { this.jobToBeDone = jobToBeDone; }
    }

    public static class ExtractionMethodology {
        private String rankingAlgorithm;
        private Map<String, Double> weightingFactors;
        private List<String> filteringCriteria;

        // Constructors and getters/setters
        public ExtractionMethodology() {
            this.rankingAlgorithm = "enhanced_contextual_relevance_score";
        }

        public String getRankingAlgorithm() { return rankingAlgorithm; }
        public void setRankingAlgorithm(String rankingAlgorithm) { this.rankingAlgorithm = rankingAlgorithm; }

        public Map<String, Double> getWeightingFactors() { return weightingFactors; }
        public void setWeightingFactors(Map<String, Double> weightingFactors) { this.weightingFactors = weightingFactors; }

        public List<String> getFilteringCriteria() { return filteringCriteria; }
        public void setFilteringCriteria(List<String> filteringCriteria) { this.filteringCriteria = filteringCriteria; }
    }

    public static class ContentAnalysis {
        private Map<String, ThematicInfo> thematicBreakdown;

        public static class ThematicInfo {
            private Integer sectionsCount;
            private String relevanceToPersona;
            private Integer budgetOptionsIdentified;
            private Integer groupFriendlyOptions;

            // Constructors and getters/setters
            public ThematicInfo() {}

            public Integer getSectionsCount() { return sectionsCount; }
            public void setSectionsCount(Integer sectionsCount) { this.sectionsCount = sectionsCount; }

            public String getRelevanceToPersona() { return relevanceToPersona; }
            public void setRelevanceToPersona(String relevanceToPersona) { this.relevanceToPersona = relevanceToPersona; }

            public Integer getBudgetOptionsIdentified() { return budgetOptionsIdentified; }
            public void setBudgetOptionsIdentified(Integer budgetOptionsIdentified) { this.budgetOptionsIdentified = budgetOptionsIdentified; }

            public Integer getGroupFriendlyOptions() { return groupFriendlyOptions; }
            public void setGroupFriendlyOptions(Integer groupFriendlyOptions) { this.groupFriendlyOptions = groupFriendlyOptions; }
        }

        public Map<String, ThematicInfo> getThematicBreakdown() { return thematicBreakdown; }
        public void setThematicBreakdown(Map<String, ThematicInfo> thematicBreakdown) { this.thematicBreakdown = thematicBreakdown; }
    }

    public static class Recommendations {
        private List<String> highPrioritySections;
        private List<String> contentGapsIdentified;
        private List<String> nextSteps;

        // Constructors and getters/setters
        public Recommendations() {}

        public List<String> getHighPrioritySections() { return highPrioritySections; }
        public void setHighPrioritySections(List<String> highPrioritySections) { this.highPrioritySections = highPrioritySections; }

        public List<String> getContentGapsIdentified() { return contentGapsIdentified; }
        public void setContentGapsIdentified(List<String> contentGapsIdentified) { this.contentGapsIdentified = contentGapsIdentified; }

        public List<String> getNextSteps() { return nextSteps; }
        public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }
    }

    public static class ProcessingStatistics {
        private Integer documentsSuccessfullyProcessed;
        private Integer documentsWithErrors;
        private Integer totalSectionsEvaluated;
        private Integer sectionsMeetingRelevanceThreshold;
        private Double averageRelevanceScore;
        private String processingEfficiency;
        private Long memoryUsageMb;
        private Double cpuTimeSeconds;

        // Constructors and getters/setters
        public ProcessingStatistics() {}

        public Integer getDocumentsSuccessfullyProcessed() { return documentsSuccessfullyProcessed; }
        public void setDocumentsSuccessfullyProcessed(Integer documentsSuccessfullyProcessed) { this.documentsSuccessfullyProcessed = documentsSuccessfullyProcessed; }

        public Integer getDocumentsWithErrors() { return documentsWithErrors; }
        public void setDocumentsWithErrors(Integer documentsWithErrors) { this.documentsWithErrors = documentsWithErrors; }

        public Integer getTotalSectionsEvaluated() { return totalSectionsEvaluated; }
        public void setTotalSectionsEvaluated(Integer totalSectionsEvaluated) { this.totalSectionsEvaluated = totalSectionsEvaluated; }

        public Integer getSectionsMeetingRelevanceThreshold() { return sectionsMeetingRelevanceThreshold; }
        public void setSectionsMeetingRelevanceThreshold(Integer sectionsMeetingRelevanceThreshold) { this.sectionsMeetingRelevanceThreshold = sectionsMeetingRelevanceThreshold; }

        public Double getAverageRelevanceScore() { return averageRelevanceScore; }
        public void setAverageRelevanceScore(Double averageRelevanceScore) { this.averageRelevanceScore = averageRelevanceScore; }

        public String getProcessingEfficiency() { return processingEfficiency; }
        public void setProcessingEfficiency(String processingEfficiency) { this.processingEfficiency = processingEfficiency; }

        public Long getMemoryUsageMb() { return memoryUsageMb; }
        public void setMemoryUsageMb(Long memoryUsageMb) { this.memoryUsageMb = memoryUsageMb; }

        public Double getCpuTimeSeconds() { return cpuTimeSeconds; }
        public void setCpuTimeSeconds(Double cpuTimeSeconds) { this.cpuTimeSeconds = cpuTimeSeconds; }
    }

    public static class VersionInfo {
        private String outputFormatVersion;
        private LocalDateTime lastUpdated;
        private String backwardCompatibility;
        private String schemaValidation;

        // Constructors and getters/setters
        public VersionInfo() {
            this.outputFormatVersion = "4.0.0";
            this.lastUpdated = LocalDateTime.now();
            this.backwardCompatibility = "3.x.x";
            this.schemaValidation = "passed";
        }

        public String getOutputFormatVersion() { return outputFormatVersion; }
        public void setOutputFormatVersion(String outputFormatVersion) { this.outputFormatVersion = outputFormatVersion; }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

        public String getBackwardCompatibility() { return backwardCompatibility; }
        public void setBackwardCompatibility(String backwardCompatibility) { this.backwardCompatibility = backwardCompatibility; }

        public String getSchemaValidation() { return schemaValidation; }
        public void setSchemaValidation(String schemaValidation) { this.schemaValidation = schemaValidation; }
    }

    // Getters and Setters for main class
    public AnalysisSummary getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(AnalysisSummary analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public EnhancedMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(EnhancedMetadata metadata) {
        this.metadata = metadata;
    }

    public ExtractionMethodology getExtractionMethodology() {
        return extractionMethodology;
    }

    public void setExtractionMethodology(ExtractionMethodology extractionMethodology) {
        this.extractionMethodology = extractionMethodology;
    }

    public List<DetailedExtractedSection> getExtractedSections() {
        return extractedSections;
    }

    public void setExtractedSections(List<DetailedExtractedSection> extractedSections) {
        this.extractedSections = extractedSections;
    }

    public ContentAnalysis getContentAnalysis() {
        return contentAnalysis;
    }

    public void setContentAnalysis(ContentAnalysis contentAnalysis) {
        this.contentAnalysis = contentAnalysis;
    }

    public List<DetailedSubsectionAnalysis> getSubsectionAnalysis() {
        return subsectionAnalysis;
    }

    public void setSubsectionAnalysis(List<DetailedSubsectionAnalysis> subsectionAnalysis) {
        this.subsectionAnalysis = subsectionAnalysis;
    }

    public Recommendations getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Recommendations recommendations) {
        this.recommendations = recommendations;
    }

    public ProcessingStatistics getProcessingStatistics() {
        return processingStatistics;
    }

    public void setProcessingStatistics(ProcessingStatistics processingStatistics) {
        this.processingStatistics = processingStatistics;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }
}