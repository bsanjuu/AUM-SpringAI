package com.bsanju.aum.repository;

import com.bsanju.aum.model.entity.QueryMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for QueryMetrics entity.
 * Provides data access methods for metrics and analytics.
 */
@Repository
public interface MetricsRepository extends JpaRepository<QueryMetrics, Long> {

    /**
     * Find metrics by session ID.
     */
    List<QueryMetrics> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find metrics by category.
     */
    List<QueryMetrics> findByCategoryOrderByTimestampDesc(String category);

    /**
     * Find failed queries.
     */
    List<QueryMetrics> findBySuccessFalseOrderByTimestampDesc();

    /**
     * Find queries needing human assistance.
     */
    List<QueryMetrics> findByNeedsHumanAssistanceTrueOrderByTimestampDesc();

    /**
     * Find metrics within time range.
     */
    @Query("SELECT m FROM QueryMetrics m WHERE m.timestamp BETWEEN :startDate AND :endDate ORDER BY m.timestamp DESC")
    List<QueryMetrics> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count total queries.
     */
    @Query("SELECT COUNT(m) FROM QueryMetrics m")
    Long countTotalQueries();

    /**
     * Count queries since a specific time.
     */
    @Query("SELECT COUNT(m) FROM QueryMetrics m WHERE m.timestamp > :since")
    Long countQueriesSince(@Param("since") LocalDateTime since);

    /**
     * Count queries in last 24 hours.
     */
    @Query("SELECT COUNT(m) FROM QueryMetrics m WHERE m.timestamp > :since")
    Long countQueriesLast24Hours(@Param("since") LocalDateTime since);

    /**
     * Count queries by category.
     */
    @Query("SELECT m.category, COUNT(m) FROM QueryMetrics m GROUP BY m.category")
    List<Object[]> countQueriesByCategory();

    /**
     * Get average confidence.
     */
    @Query("SELECT AVG(m.confidence) FROM QueryMetrics m")
    Double getAverageConfidence();

    /**
     * Get average confidence by category.
     */
    @Query("SELECT m.category, AVG(m.confidence) FROM QueryMetrics m GROUP BY m.category")
    List<Object[]> getAverageConfidenceByCategory();

    /**
     * Get average response time.
     */
    @Query("SELECT AVG(m.responseTimeMs) FROM QueryMetrics m")
    Double getAverageResponseTime();

    /**
     * Get response time distribution.
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN m.responseTimeMs < 500 THEN 'fast' " +
           "  WHEN m.responseTimeMs < 2000 THEN 'medium' " +
           "  WHEN m.responseTimeMs < 5000 THEN 'slow' " +
           "  ELSE 'very_slow' " +
           "END as speed, COUNT(m) " +
           "FROM QueryMetrics m GROUP BY " +
           "CASE " +
           "  WHEN m.responseTimeMs < 500 THEN 'fast' " +
           "  WHEN m.responseTimeMs < 2000 THEN 'medium' " +
           "  WHEN m.responseTimeMs < 5000 THEN 'slow' " +
           "  ELSE 'very_slow' " +
           "END")
    List<Object[]> getResponseTimeDistribution();

    /**
     * Count low confidence queries (confidence < threshold).
     */
    @Query("SELECT COUNT(m) FROM QueryMetrics m WHERE m.confidence < :threshold")
    Long countLowConfidenceQueries(@Param("threshold") double threshold);

    /**
     * Count queries needing human assistance.
     */
    @Query("SELECT COUNT(m) FROM QueryMetrics m WHERE m.needsHumanAssistance = true")
    Long countQueriesNeedingHumanAssistance();

    /**
     * Get model usage statistics.
     */
    @Query("SELECT m.modelUsed, COUNT(m) FROM QueryMetrics m GROUP BY m.modelUsed")
    List<Object[]> getModelUsageStats();

    /**
     * Delete old metrics (data cleanup).
     */
    @Query("DELETE FROM QueryMetrics m WHERE m.timestamp < :cutoffDate")
    void deleteOldMetrics(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find queries with low confidence in a time range.
     */
    @Query("SELECT m FROM QueryMetrics m WHERE m.confidence < :threshold AND m.timestamp BETWEEN :startDate AND :endDate ORDER BY m.confidence ASC")
    List<QueryMetrics> findLowConfidenceQueriesInRange(@Param("threshold") double threshold, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find metrics by date range (using LocalDate).
     */
    @Query("SELECT m FROM QueryMetrics m WHERE CAST(m.timestamp AS LocalDate) >= :startDate AND CAST(m.timestamp AS LocalDate) < :endDate ORDER BY m.timestamp DESC")
    List<QueryMetrics> findByDateOnlyBetween(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
}
