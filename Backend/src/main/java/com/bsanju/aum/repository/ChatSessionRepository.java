package com.bsanju.aum.repository;

import com.bsanju.aum.model.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatSession entity.
 * Provides data access methods for chat session operations.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Find all chat sessions for a given session ID, ordered by timestamp descending.
     */
    List<ChatSession> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find recent chat sessions for a session ID with limit.
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.sessionId = :sessionId ORDER BY cs.timestamp DESC LIMIT :limit")
    List<ChatSession> findRecentBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * Find all sessions that need human assistance.
     */
    List<ChatSession> findByNeedsHumanAssistanceTrue();

    /**
     * Find sessions by category.
     */
    List<ChatSession> findByCategoryOrderByTimestampDesc(String category);

    /**
     * Find sessions with confidence below threshold.
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.confidence < :threshold ORDER BY cs.timestamp DESC")
    List<ChatSession> findLowConfidenceSessions(@Param("threshold") double threshold);

    /**
     * Find sessions within a time range.
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.timestamp BETWEEN :startDate AND :endDate ORDER BY cs.timestamp DESC")
    List<ChatSession> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count sessions in the last N hours.
     */
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.timestamp > :since")
    Long countSessionsSince(@Param("since") LocalDateTime since);

    /**
     * Count sessions by category.
     */
    @Query("SELECT cs.category, COUNT(cs) FROM ChatSession cs GROUP BY cs.category")
    List<Object[]> countByCategory();

    /**
     * Get average confidence score.
     */
    @Query("SELECT AVG(cs.confidence) FROM ChatSession cs")
    Double getAverageConfidence();

    /**
     * Get average confidence by category.
     */
    @Query("SELECT cs.category, AVG(cs.confidence) FROM ChatSession cs GROUP BY cs.category")
    List<Object[]> getAverageConfidenceByCategory();

    /**
     * Get average response time.
     */
    @Query("SELECT AVG(cs.responseTimeMs) FROM ChatSession cs")
    Double getAverageResponseTime();

    /**
     * Delete old sessions (data cleanup).
     */
    @Query("DELETE FROM ChatSession cs WHERE cs.timestamp < :cutoffDate")
    void deleteOldSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
