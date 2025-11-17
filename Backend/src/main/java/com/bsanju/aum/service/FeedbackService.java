package com.bsanju.aum.service;

import com.bsanju.aum.event.FeedbackReceivedEvent;
import com.bsanju.aum.model.dto.FeedbackRequest;
import com.bsanju.aum.model.entity.UserFeedback;
import com.bsanju.aum.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing user feedback on AI responses.
 * Handles feedback collection, analysis, and retrieval.
 */
@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FeedbackService(FeedbackRepository feedbackRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.feedbackRepository = feedbackRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Save user feedback for a chat session.
     *
     * @param request The feedback request
     * @return The saved feedback entity
     */
    @Transactional
    public UserFeedback saveFeedback(FeedbackRequest request) {
        logger.info("Saving feedback for session: {}, rating: {}", 
                    request.sessionId(), request.rating());

        try {
            UserFeedback feedback = new UserFeedback(
                    request.sessionId(),
                    request.rating(),
                    request.comment()
            );

            feedback.setHelpful(request.helpful());
            feedback.setFeedbackType(request.feedbackType());
            feedback.setUserIp(request.userIp());

            feedback = feedbackRepository.save(feedback);

            logger.info("Feedback saved successfully: ID={}", feedback.getId());

            // Publish event for low ratings
            if (request.rating() != null && request.rating() <= 2) {
                eventPublisher.publishEvent(new FeedbackReceivedEvent(this, feedback.getId()));
            }

            return feedback;

        } catch (Exception e) {
            logger.error("Error saving feedback", e);
            throw new RuntimeException("Failed to save feedback", e);
        }
    }

    /**
     * Get feedback for a specific session.
     *
     * @param sessionId The session ID
     * @return Optional containing feedback if found
     */
    public Optional<UserFeedback> getFeedbackBySession(String sessionId) {
        logger.debug("Retrieving feedback for session: {}", sessionId);
        return feedbackRepository.findBySessionId(sessionId);
    }

    /**
     * Get all feedback for a session (in case of multiple feedbacks).
     *
     * @param sessionId The session ID
     * @return List of feedback entries
     */
    public List<UserFeedback> getAllFeedbackBySession(String sessionId) {
        logger.debug("Retrieving all feedback for session: {}", sessionId);
        return feedbackRepository.findAllBySessionIdOrderByTimestampDesc(sessionId);
    }

    /**
     * Get negative feedback (rating <= 2).
     *
     * @return List of negative feedback
     */
    public List<UserFeedback> getNegativeFeedback() {
        logger.debug("Retrieving negative feedback");
        return feedbackRepository.findNegativeFeedback();
    }

    /**
     * Get positive feedback (rating >= 4).
     *
     * @return List of positive feedback
     */
    public List<UserFeedback> getPositiveFeedback() {
        logger.debug("Retrieving positive feedback");
        return feedbackRepository.findPositiveFeedback();
    }

    /**
     * Get unresolved feedback.
     *
     * @return List of unresolved feedback
     */
    public List<UserFeedback> getUnresolvedFeedback() {
        logger.debug("Retrieving unresolved feedback");
        return feedbackRepository.findByResolvedFalseOrderByTimestampDesc();
    }

    /**
     * Get recent unresolved negative feedback.
     *
     * @param limit Maximum number of entries
     * @return List of recent unresolved negative feedback
     */
    public List<UserFeedback> getRecentUnresolvedNegativeFeedback(int limit) {
        logger.debug("Retrieving recent unresolved negative feedback, limit: {}", limit);
        return feedbackRepository.findRecentUnresolvedNegativeFeedback(limit);
    }

    /**
     * Mark feedback as resolved.
     *
     * @param feedbackId The feedback ID
     * @param adminNotes Optional admin notes
     */
    @Transactional
    public void resolveFeedback(Long feedbackId, String adminNotes) {
        logger.info("Resolving feedback: {}", feedbackId);

        try {
            Optional<UserFeedback> feedbackOpt = feedbackRepository.findById(feedbackId);
            if (feedbackOpt.isEmpty()) {
                logger.warn("Feedback not found: {}", feedbackId);
                return;
            }

            UserFeedback feedback = feedbackOpt.get();
            feedback.setResolved(true);
            if (adminNotes != null) {
                feedback.setAdminNotes(adminNotes);
            }

            feedbackRepository.save(feedback);
            logger.info("Feedback resolved: {}", feedbackId);

        } catch (Exception e) {
            logger.error("Error resolving feedback", e);
            throw new RuntimeException("Failed to resolve feedback", e);
        }
    }

    /**
     * Get average rating across all feedback.
     *
     * @return Average rating or 0.0 if no feedback
     */
    public double getAverageRating() {
        try {
            Double avg = feedbackRepository.getAverageRating();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("Error calculating average rating", e);
            return 0.0;
        }
    }

    /**
     * Get feedback statistics.
     *
     * @return Map of feedback statistics
     */
    public java.util.Map<String, Object> getFeedbackStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try {
            stats.put("totalFeedback", feedbackRepository.count());
            stats.put("averageRating", getAverageRating());
            stats.put("unresolvedNegativeFeedback", 
                     feedbackRepository.countUnresolvedNegativeFeedback());

            // Rating distribution
            List<Object[]> ratingDist = feedbackRepository.countByRating();
            java.util.Map<Integer, Long> ratingMap = new java.util.HashMap<>();
            for (Object[] row : ratingDist) {
                ratingMap.put((Integer) row[0], (Long) row[1]);
            }
            stats.put("ratingDistribution", ratingMap);

        } catch (Exception e) {
            logger.error("Error getting feedback stats", e);
        }

        return stats;
    }

    /**
     * Get feedback with comments.
     *
     * @return List of feedback with non-empty comments
     */
    public List<UserFeedback> getFeedbackWithComments() {
        logger.debug("Retrieving feedback with comments");
        return feedbackRepository.findFeedbackWithComments();
    }
}
