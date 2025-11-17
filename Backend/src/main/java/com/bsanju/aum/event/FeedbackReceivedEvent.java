package com.bsanju.aum.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when user feedback is received.
 * Can be used to trigger notifications for low ratings or collect analytics.
 */
public class FeedbackReceivedEvent extends ApplicationEvent {

    private final Long feedbackId;
    private final String sessionId;
    private final Integer rating;
    private final boolean isNegative;

    public FeedbackReceivedEvent(Object source, Long feedbackId) {
        super(source);
        this.feedbackId = feedbackId;
        this.sessionId = null;
        this.rating = null;
        this.isNegative = false;
    }

    public FeedbackReceivedEvent(Object source, 
                                Long feedbackId, 
                                String sessionId, 
                                Integer rating) {
        super(source);
        this.feedbackId = feedbackId;
        this.sessionId = sessionId;
        this.rating = rating;
        this.isNegative = rating != null && rating <= 2;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getRating() {
        return rating;
    }

    public boolean isNegative() {
        return isNegative;
    }

    @Override
    public String toString() {
        return "FeedbackReceivedEvent{" +
                "feedbackId=" + feedbackId +
                ", sessionId='" + sessionId + '\'' +
                ", rating=" + rating +
                ", isNegative=" + isNegative +
                '}';
    }
}
