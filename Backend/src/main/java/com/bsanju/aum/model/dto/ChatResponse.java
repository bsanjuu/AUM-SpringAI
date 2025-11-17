package com.bsanju.aum.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for chat messages.
 * Contains AI response with metadata including confidence scores.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
        String response,

        String sessionId,

        String category,

        double confidence,

        boolean needsHumanAssistance,

        LocalDateTime timestamp,

        List<String> suggestions,

        Long responseTimeMs,

        String model
) {
    /**
     * Builder for ChatResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String response;
        private String sessionId;
        private String category;
        private double confidence;
        private boolean needsHumanAssistance;
        private LocalDateTime timestamp;
        private List<String> suggestions;
        private Long responseTimeMs;
        private String model;

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder needsHumanAssistance(boolean needsHumanAssistance) {
            this.needsHumanAssistance = needsHumanAssistance;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder suggestions(List<String> suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public ChatResponse build() {
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }
            return new ChatResponse(
                    response,
                    sessionId,
                    category,
                    confidence,
                    needsHumanAssistance,
                    timestamp,
                    suggestions,
                    responseTimeMs,
                    model
            );
        }
    }
}
