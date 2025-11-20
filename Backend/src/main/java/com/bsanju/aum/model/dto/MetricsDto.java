package com.bsanju.aum.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for time-based metrics and analytics (daily, weekly, monthly).
 * Provides insights into system performance and query statistics over a period.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricsDto(
        long totalQueries,
        double avgConfidence,
        double avgResponseTime,
        double humanAssistanceRate,
        Map<String, Long> categoryCounts,
        Map<Integer, Long> hourlyDistribution,
        Map<LocalDate, Long> dailyQueries,
        FeedbackStats feedbackStats,
        Period period
) {
    /**
     * Nested record for feedback statistics.
     */
    public record FeedbackStats(
            long totalFeedback,
            double helpfulPercentage,
            double averageRating
    ) {}

    /**
     * Nested record for time period information.
     */
    public record Period(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String periodType
    ) {}
}
