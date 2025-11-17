package com.bsanju.aum.exception;

/**
 * Custom exception for chat service errors.
 * Thrown when processing chat queries fails.
 */
public class ChatServiceException extends RuntimeException {

    private final String sessionId;
    private final String errorCode;

    public ChatServiceException(String message) {
        super(message);
        this.sessionId = null;
        this.errorCode = "CHAT_ERROR";
    }

    public ChatServiceException(String message, Throwable cause) {
        super(message, cause);
        this.sessionId = null;
        this.errorCode = "CHAT_ERROR";
    }

    public ChatServiceException(String message, String sessionId, String errorCode) {
        super(message);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
    }

    public ChatServiceException(String message, String sessionId, String errorCode, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
