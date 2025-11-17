package com.bsanju.aum.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for system metrics and analytics.
 * Provides insights into system performance and query statistics.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricsDto(
        Long totalQueries,

        Long queriesLast24Hours,

        Long queriesLastWeek,

        Double averageConfidence,

        Double averageResponseTime,

        Long lowConfidenceQueries,

        Long humanAssistanceRequired,

        Map<String, Long> queriesByCategory,

        Map<String, Double> averageConfidenceByCategory,

        Map<String, Long> responseTimeDistribution,

        Long totalDocuments,

        Long indexedDocuments,

        LocalDateTime lastIndexUpdate,

        Map<String, Object> systemHealth
) {
    /**
     * Builder for MetricsDto.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalQueries;
        private Long queriesLast24Hours;
        private Long queriesLastWeek;
        private Double averageConfidence;
        private Double averageResponseTime;
        private Long lowConfidenceQueries;
        private Long humanAssistanceRequired;
        private Map<String, Long> queriesByCategory;
        private Map<String, Double> averageConfidenceByCategory;
        private Map<String, Long> responseTimeDistribution;
        private Long totalDocuments;
        private Long indexedDocuments;
        private LocalDateTime lastIndexUpdate;
        private Map<String, Object> systemHealth;

        public Builder totalQueries(Long totalQueries) {
            this.totalQueries = totalQueries;
            return this;
        }

        public Builder queriesLast24Hours(Long queriesLast24Hours) {
            this.queriesLast24Hours = queriesLast24Hours;
            return this;
        }

        public Builder queriesLastWeek(Long queriesLastWeek) {
            this.queriesLastWeek = queriesLastWeek;
            return this;
        }

        public Builder averageConfidence(Double averageConfidence) {
            this.averageConfidence = averageConfidence;
            return this;
        }

        public Builder averageResponseTime(Double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder lowConfidenceQueries(Long lowConfidenceQueries) {
            this.lowConfidenceQueries = lowConfidenceQueries;
            return this;
        }

        public Builder humanAssistanceRequired(Long humanAssistanceRequired) {
            this.humanAssistanceRequired = humanAssistanceRequired;
            return this;
        }

        public Builder queriesByCategory(Map<String, Long> queriesByCategory) {
            this.queriesByCategory = queriesByCategory;
            return this;
        }

        public Builder averageConfidenceByCategory(Map<String, Double> averageConfidenceByCategory) {
            this.averageConfidenceByCategory = averageConfidenceByCategory;
            return this;
        }

        public Builder responseTimeDistribution(Map<String, Long> responseTimeDistribution) {
            this.responseTimeDistribution = responseTimeDistribution;
            return this;
        }

        public Builder totalDocuments(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
            return this;
        }

        public Builder indexedDocuments(Long indexedDocuments) {
            this.indexedDocuments = indexedDocuments;
            return this;
        }

        public Builder lastIndexUpdate(LocalDateTime lastIndexUpdate) {
            this.lastIndexUpdate = lastIndexUpdate;
            return this;
        }

        public Builder systemHealth(Map<String, Object> systemHealth) {
            this.systemHealth = systemHealth;
            return this;
        }

        public MetricsDto build() {
            return new MetricsDto(
                    totalQueries,
                    queriesLast24Hours,
                    queriesLastWeek,
                    averageConfidence,
                    averageResponseTime,
                    lowConfidenceQueries,
                    humanAssistanceRequired,
                    queriesByCategory,
                    averageConfidenceByCategory,
                    responseTimeDistribution,
                    totalDocuments,
                    indexedDocuments,
                    lastIndexUpdate,
                    systemHealth
            );
        }
    }
}
