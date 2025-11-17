package com.bsanju.aum.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a chat query is completed.
 * Can be used for analytics, logging, or triggering additional processing.
 */
public class ChatCompletionEvent extends ApplicationEvent {

    private final String sessionId;
    private final String category;
    private final double confidence;
    private final boolean needsHumanAssistance;
    private final long responseTimeMs;

    public ChatCompletionEvent(Object source, 
                              String sessionId, 
                              String category, 
                              double confidence,
                              boolean needsHumanAssistance,
                              long responseTimeMs) {
        super(source);
        this.sessionId = sessionId;
        this.category = category;
        this.confidence = confidence;
        this.needsHumanAssistance = needsHumanAssistance;
        this.responseTimeMs = responseTimeMs;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isNeedsHumanAssistance() {
        return needsHumanAssistance;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    @Override
    public String toString() {
        return "ChatCompletionEvent{" +
                "sessionId='" + sessionId + '\'' +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                ", needsHumanAssistance=" + needsHumanAssistance +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}
