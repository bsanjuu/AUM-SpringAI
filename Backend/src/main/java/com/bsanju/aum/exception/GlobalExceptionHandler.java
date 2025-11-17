package com.bsanju.aum.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles exceptions thrown by controllers and services.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors,
                request.getDescription(false)
        );

        logger.warn("Validation error: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle ChatServiceException.
     */
    @ExceptionHandler(ChatServiceException.class)
    public ResponseEntity<Map<String, Object>> handleChatServiceException(
            ChatServiceException ex, WebRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                Map.of(
                        "errorCode", ex.getErrorCode(),
                        "sessionId", ex.getSessionId() != null ? ex.getSessionId() : "unknown"
                ),
                request.getDescription(false)
        );

        logger.error("Chat service error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle DocumentNotFoundException.
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentNotFoundException(
            DocumentNotFoundException ex, WebRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex.getDocumentId() != null ? Map.of("documentId", ex.getDocumentId()) : Map.of(),
                request.getDescription(false)
        );

        logger.warn("Document not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle RateLimitExceededException.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceededException(
            RateLimitExceededException ex, WebRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                Map.of(
                        "retryAfterSeconds", ex.getRetryAfterSeconds(),
                        "userIdentifier", ex.getUserIdentifier()
                ),
                request.getDescription(false)
        );

        logger.warn("Rate limit exceeded: user={}, retryAfter={}s", 
                   ex.getUserIdentifier(), ex.getRetryAfterSeconds());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                Map.of("error", ex.getClass().getSimpleName()),
                request.getDescription(false)
        );

        logger.error("Unexpected error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                Map.of(),
                request.getDescription(false)
        );

        logger.warn("Illegal argument: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Create standardized error response.
     */
    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String message,
            Map<String, ?> details,
            String path) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("path", path);
        
        if (details != null && !details.isEmpty()) {
            response.put("details", details);
        }
        
        return response;
    }
}
