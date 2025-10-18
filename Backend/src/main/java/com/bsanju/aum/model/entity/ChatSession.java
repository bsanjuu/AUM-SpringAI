package com.bsanju.aum.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Lob
    @Column(name = "user_message", nullable = false)
    private String userMessage;

    @Lob
    @Column(name = "ai_response", nullable = false)
    private String aiResponse;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "needs_human_assistance")
    private boolean needsHumanAssistance;

    @Column(name = "response_time_ms")
    private long responseTimeMs;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Constructors
    public ChatSession() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatSession(String sessionId, String userMessage, String aiResponse) {
        this();
        this.sessionId = sessionId;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
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

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
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

    public boolean isNeedsHumanAssistance() {
        return needsHumanAssistance;
    }

    public void setNeedsHumanAssistance(boolean needsHumanAssistance) {
        this.needsHumanAssistance = needsHumanAssistance;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                ", needsHumanAssistance=" + needsHumanAssistance +
                ", timestamp=" + timestamp +
                '}';
    }
}