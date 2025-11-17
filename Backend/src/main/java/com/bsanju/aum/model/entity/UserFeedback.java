package com.bsanju.aum.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing user feedback on AI responses.
 * Helps improve system accuracy and identify problematic responses.
 */
@Entity
@Table(name = "user_feedback", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_rating", columnList = "rating"),
        @Index(name = "idx_helpful", columnList = "helpful")
})
public class UserFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "helpful")
    private Boolean helpful;

    @Column(name = "feedback_type", length = 50)
    private String feedbackType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "resolved")
    private boolean resolved = false;

    @Column(name = "admin_notes", length = 2000)
    private String adminNotes;

    // Constructors
    public UserFeedback() {
        this.timestamp = LocalDateTime.now();
    }

    public UserFeedback(String sessionId, Integer rating) {
        this();
        this.sessionId = sessionId;
        this.rating = rating;
    }

    public UserFeedback(String sessionId, Integer rating, String comment) {
        this(sessionId, rating);
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getHelpful() {
        return helpful;
    }

    public void setHelpful(Boolean helpful) {
        this.helpful = helpful;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    @Override
    public String toString() {
        return "UserFeedback{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", rating=" + rating +
                ", helpful=" + helpful +
                ", feedbackType='" + feedbackType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
