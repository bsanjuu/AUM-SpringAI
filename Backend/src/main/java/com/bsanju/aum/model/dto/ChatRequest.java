package com.bsanju.aum.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for chat messages.
 * Immutable record for processing user queries.
 */
public record ChatRequest(
        @NotBlank(message = "Message cannot be blank")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        String message,

        String sessionId,

        String category,

        String userIp,

        String userAgent
) {
    /**
     * Constructor with defaults for optional fields.
     */
    public ChatRequest {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * Creates a simple chat request with just a message.
     */
    public static ChatRequest of(String message) {
        return new ChatRequest(message, null, null, null, null);
    }

    /**
     * Creates a chat request with message and session ID.
     */
    public static ChatRequest of(String message, String sessionId) {
        return new ChatRequest(message, sessionId, null, null, null);
    }
}
