package com.bsanju.aum.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking query metrics and analytics.
 * Used for monitoring system performance and generating insights.
 */
@Entity
@Table(name = "query_metrics", indexes = {
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_confidence", columnList = "confidence"),
        @Index(name = "idx_human_assistance", columnList = "needs_human_assistance")
})
public class QueryMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "query_text", nullable = false, length = 2000)
    private String queryText;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "response_time_ms")
    private long responseTimeMs;

    @Column(name = "needs_human_assistance")
    private boolean needsHumanAssistance;

    @Column(name = "documents_retrieved")
    private int documentsRetrieved;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "success")
    private boolean success = true;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "query_length")
    private int queryLength;

    @Column(name = "response_length")
    private int responseLength;

    // Constructors
    public QueryMetrics() {
        this.timestamp = LocalDateTime.now();
    }

    public QueryMetrics(String sessionId, String queryText, String category) {
        this();
        this.sessionId = sessionId;
        this.queryText = queryText;
        this.category = category;
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

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isNeedsHumanAssistance() {
        return needsHumanAssistance;
    }

    public void setNeedsHumanAssistance(boolean needsHumanAssistance) {
        this.needsHumanAssistance = needsHumanAssistance;
    }

    public int getDocumentsRetrieved() {
        return documentsRetrieved;
    }

    public void setDocumentsRetrieved(int documentsRetrieved) {
        this.documentsRetrieved = documentsRetrieved;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getQueryLength() {
        return queryLength;
    }

    public void setQueryLength(int queryLength) {
        this.queryLength = queryLength;
    }

    public int getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(int responseLength) {
        this.responseLength = responseLength;
    }

    // Convenience methods for MetricsService
    public double getConfidenceScore() {
        return this.confidence;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidence = confidenceScore;
    }

    public int getHourOfDay() {
        return this.timestamp != null ? this.timestamp.getHour() : 0;
    }

    public java.time.LocalDate getDateOnly() {
        return this.timestamp != null ? this.timestamp.toLocalDate() : null;
    }

    @Override
    public String toString() {
        return "QueryMetrics{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                ", responseTimeMs=" + responseTimeMs +
                ", timestamp=" + timestamp +
                '}';
    }
}
