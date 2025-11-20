package com.bsanju.aum.repository;

import com.bsanju.aum.model.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserFeedback entity.
 * Provides data access methods for user feedback management.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<UserFeedback, Long> {

    /**
     * Find feedback by session ID.
     */
    Optional<UserFeedback> findBySessionId(String sessionId);

    /**
     * Find all feedback by session ID (in case multiple feedbacks per session).
     */
    List<UserFeedback> findAllBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find feedback by rating.
     */
    List<UserFeedback> findByRatingOrderByTimestampDesc(Integer rating);

    /**
     * Find negative feedback (rating <= 2).
     */
    @Query("SELECT f FROM UserFeedback f WHERE f.rating <= 2 ORDER BY f.timestamp DESC")
    List<UserFeedback> findNegativeFeedback();

    /**
     * Find positive feedback (rating >= 4).
     */
    @Query("SELECT f FROM UserFeedback f WHERE f.rating >= 4 ORDER BY f.timestamp DESC")
    List<UserFeedback> findPositiveFeedback();

    /**
     * Find helpful feedback.
     */
    List<UserFeedback> findByHelpfulTrueOrderByTimestampDesc();

    /**
     * Find not helpful feedback.
     */
    List<UserFeedback> findByHelpfulFalseOrderByTimestampDesc();

    /**
     * Find unresolved feedback.
     */
    List<UserFeedback> findByResolvedFalseOrderByTimestampDesc();

    /**
     * Find resolved feedback.
     */
    List<UserFeedback> findByResolvedTrueOrderByTimestampDesc();

    /**
     * Find feedback by type.
     */
    List<UserFeedback> findByFeedbackTypeOrderByTimestampDesc(String feedbackType);

    /**
     * Find feedback within time range.
     */
    @Query("SELECT f FROM UserFeedback f WHERE f.timestamp BETWEEN :startDate AND :endDate ORDER BY f.timestamp DESC")
    List<UserFeedback> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count total feedback.
     */
    Long count();

    /**
     * Count feedback since a specific time.
     */
    @Query("SELECT COUNT(f) FROM UserFeedback f WHERE f.timestamp > :since")
    Long countFeedbackSince(@Param("since") LocalDateTime since);

    /**
     * Count total feedback since a specific time (alias for countFeedbackSince).
     */
    @Query("SELECT COUNT(f) FROM UserFeedback f WHERE f.timestamp >= :since")
    Long countTotalFeedbackSince(@Param("since") LocalDateTime since);

    /**
     * Count helpful feedback since a specific time.
     */
    @Query("SELECT COUNT(f) FROM UserFeedback f WHERE f.timestamp >= :since AND f.helpful = true")
    Long countHelpfulFeedbackSince(@Param("since") LocalDateTime since);

    /**
     * Get average rating since a specific time.
     */
    @Query("SELECT AVG(f.rating) FROM UserFeedback f WHERE f.timestamp >= :since AND f.rating IS NOT NULL")
    Double averageRatingSince(@Param("since") LocalDateTime since);

    /**
     * Get average rating.
     */
    @Query("SELECT AVG(f.rating) FROM UserFeedback f WHERE f.rating IS NOT NULL")
    Double getAverageRating();

    /**
     * Count by rating.
     */
    @Query("SELECT f.rating, COUNT(f) FROM UserFeedback f WHERE f.rating IS NOT NULL GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> countByRating();

    /**
     * Count unresolved negative feedback.
     */
    @Query("SELECT COUNT(f) FROM UserFeedback f WHERE f.rating <= 2 AND f.resolved = false")
    Long countUnresolvedNegativeFeedback();

    /**
     * Find feedback with comments (non-null and non-empty).
     */
    @Query("SELECT f FROM UserFeedback f WHERE f.comment IS NOT NULL AND f.comment != '' ORDER BY f.timestamp DESC")
    List<UserFeedback> findFeedbackWithComments();

    /**
     * Find recent unresolved negative feedback.
     */
    @Query("SELECT f FROM UserFeedback f WHERE f.rating <= 2 AND f.resolved = false ORDER BY f.timestamp DESC LIMIT :limit")
    List<UserFeedback> findRecentUnresolvedNegativeFeedback(@Param("limit") int limit);
}
