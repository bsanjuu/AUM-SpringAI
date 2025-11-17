package com.bsanju.aum.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user feedback on AI responses.
 * Allows users to rate and provide comments on responses.
 */
public record FeedbackRequest(
        @NotBlank(message = "Session ID cannot be blank")
        String sessionId,

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment,

        Boolean helpful,

        String feedbackType,

        String userIp
) {
    /**
     * Creates a simple feedback request with session ID and rating.
     */
    public static FeedbackRequest of(String sessionId, Integer rating) {
        return new FeedbackRequest(sessionId, rating, null, null, null, null);
    }

    /**
     * Creates a feedback request with session ID, rating, and comment.
     */
    public static FeedbackRequest of(String sessionId, Integer rating, String comment) {
        return new FeedbackRequest(sessionId, rating, comment, null, null, null);
    }
}
