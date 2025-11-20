package com.bsanju.aum.service;

import com.bsanju.aum.model.dto.ChatRequest;
import com.bsanju.aum.model.dto.ChatResponse;
import com.bsanju.aum.model.dto.MetricsDto;
import com.bsanju.aum.model.entity.QueryMetrics;
import com.bsanju.aum.repository.MetricsRepository;
import com.bsanju.aum.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final MetricsRepository metricsRepository;
    private final FeedbackRepository feedbackRepository;

    public MetricsService(MetricsRepository metricsRepository, FeedbackRepository feedbackRepository) {
        this.metricsRepository = metricsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public void recordQuery(ChatRequest request, ChatResponse response) {
        try {
            QueryMetrics metrics = new QueryMetrics();
            metrics.setSessionId(request.sessionId());
            metrics.setCategory(request.category());
            metrics.setConfidenceScore(response.confidence());
            metrics.setResponseTimeMs(response.responseTimeMs());
            metrics.setNeedsHumanAssistance(response.needsHumanAssistance());
            metrics.setDocumentsRetrieved(0); // Fixed: removed sources() which doesn't exist
            metrics.setQueryLength(request.message().length());
            metrics.setResponseLength(response.response().length());

            metricsRepository.save(metrics);
            logger.debug("Recorded metrics for session: {}", request.sessionId());
        } catch (Exception e) {
            logger.error("Failed to record metrics", e);
        }
    }

    public MetricsDto getDailyMetrics(LocalDate date) {
        LocalDate endDate = date.plusDays(1);
        return buildMetricsDto(date, endDate, "daily");
    }

    public MetricsDto getWeeklyMetrics(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(7);
        return buildMetricsDto(startDate, endDate, "weekly");
    }

    public MetricsDto getMonthlyMetrics(LocalDate startDate) {
        LocalDate endDate = startDate.plusMonths(1);
        return buildMetricsDto(startDate, endDate, "monthly");
    }

    private MetricsDto buildMetricsDto(LocalDate startDate, LocalDate endDate, String periodType) {
        // Query metrics
        List<QueryMetrics> metrics = metricsRepository.findByDateOnlyBetween(startDate, endDate);
        long totalQueries = metrics.size();

        double avgConfidence = metrics.stream()
                .mapToDouble(QueryMetrics::getConfidenceScore)
                .average()
                .orElse(0.0);

        double avgResponseTime = metrics.stream()
                .mapToDouble(QueryMetrics::getResponseTimeMs)
                .average()
                .orElse(0.0);

        double humanAssistanceRate = totalQueries > 0 ?
                metrics.stream().mapToLong(m -> m.isNeedsHumanAssistance() ? 1 : 0).sum() / (double) totalQueries : 0.0;

        // Category distribution
        Map<String, Long> categoryCounts = metrics.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCategory() != null ? m.getCategory() : "UNKNOWN",
                        Collectors.counting()
                ));

        // Hourly distribution
        Map<Integer, Long> hourlyDistribution = metrics.stream()
                .collect(Collectors.groupingBy(
                        QueryMetrics::getHourOfDay,
                        Collectors.counting()
                ));

        // Daily queries
        Map<LocalDate, Long> dailyQueries = metrics.stream()
                .collect(Collectors.groupingBy(
                        QueryMetrics::getDateOnly,
                        Collectors.counting()
                ));

        // Feedback stats
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        long totalFeedback = feedbackRepository.countTotalFeedbackSince(startDateTime);
        long helpfulFeedback = feedbackRepository.countHelpfulFeedbackSince(startDateTime);
        Double avgRating = feedbackRepository.averageRatingSince(startDateTime);

        double helpfulPercentage = totalFeedback > 0 ? helpfulFeedback / (double) totalFeedback * 100 : 0.0;

        MetricsDto.FeedbackStats feedbackStats = new MetricsDto.FeedbackStats(
                totalFeedback,
                helpfulPercentage,
                avgRating != null ? avgRating : 0.0
        );

        MetricsDto.Period period = new MetricsDto.Period(
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                periodType
        );

        return new MetricsDto(
                totalQueries,
                avgConfidence,
                avgResponseTime,
                humanAssistanceRate,
                categoryCounts,
                hourlyDistribution,
                dailyQueries,
                feedbackStats,
                period
        );
    }
}
