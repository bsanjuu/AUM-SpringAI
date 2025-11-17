package com.bsanju.aum.controller;

import com.bsanju.aum.model.dto.ChatRequest;
import com.bsanju.aum.model.dto.ChatResponse;
import com.bsanju.aum.model.dto.FeedbackRequest;
import com.bsanju.aum.model.entity.ChatSession;
import com.bsanju.aum.service.ChatService;
import com.bsanju.aum.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for chat operations.
 * Handles user queries, chat history, and suggestions.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "${app.security.cors.allowed-origins}")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final FeedbackService feedbackService;

    public ChatController(ChatService chatService, FeedbackService feedbackService) {
        this.chatService = chatService;
        this.feedbackService = feedbackService;
    }

    /**
     * Process a chat message.
     * Main endpoint for handling user queries.
     *
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Received chat request: sessionId={}, category={}", 
                    request.sessionId(), request.category());
        
        try {
            // Extract user information
            String userIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Create enriched request
            ChatRequest enrichedRequest = new ChatRequest(
                    request.message(),
                    request.sessionId(),
                    request.category(),
                    userIp,
                    userAgent
            );
            
            // Process query
            ChatResponse response = chatService.processQuery(enrichedRequest);
            
            logger.info("Chat response generated: sessionId={}, confidence={}", 
                        response.sessionId(), response.confidence());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.builder()
                            .response("I apologize, but I encountered an error processing your request. Please try again.")
                            .sessionId(request.sessionId())
                            .confidence(0.0)
                            .needsHumanAssistance(true)
                            .build());
        }
    }

    /**
     * Get chat history for a session.
     * Retrieves conversation history ordered by timestamp.
     *
     * GET /api/chat/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatSession>> getChatHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        
        logger.debug("Fetching chat history: sessionId={}, limit={}", sessionId, limit);
        
        try {
            List<ChatSession> history = chatService.getChatHistory(sessionId, limit);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Error fetching chat history", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Get session summary.
     * Returns metadata about a chat session.
     *
     * GET /api/chat/session/{sessionId}/summary
     */
    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<Map<String, Object>> getSessionSummary(@PathVariable String sessionId) {
        logger.debug("Fetching session summary: sessionId={}", sessionId);
        
        try {
            List<ChatSession> sessions = chatService.getChatHistory(sessionId, 100);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("sessionId", sessionId);
            summary.put("totalMessages", sessions.size());
            summary.put("categories", sessions.stream()
                    .map(ChatSession::getCategory)
                    .distinct()
                    .toList());
            summary.put("averageConfidence", sessions.stream()
                    .mapToDouble(ChatSession::getConfidence)
                    .average()
                    .orElse(0.0));
            summary.put("needsHumanAssistance", sessions.stream()
                    .anyMatch(ChatSession::isNeedsHumanAssistance));
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error fetching session summary", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch session summary"));
        }
    }

    /**
     * Get suggestions for a category.
     * Returns common questions for the specified category.
     *
     * GET /api/chat/suggestions/{category}
     */
    @GetMapping("/suggestions/{category}")
    public ResponseEntity<List<String>> getSuggestions(@PathVariable String category) {
        logger.debug("Fetching suggestions for category: {}", category);
        
        try {
            List<String> suggestions = chatService.getSuggestions(category);
            return ResponseEntity.ok(suggestions);
            
        } catch (Exception e) {
            logger.error("Error fetching suggestions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("How can I register for courses?",
                                  "What are the tuition fees?",
                                  "When is the registration deadline?"));
        }
    }

    /**
     * Get general suggestions.
     * Returns common questions across all categories.
     *
     * GET /api/chat/suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, List<String>>> getAllSuggestions() {
        logger.debug("Fetching all suggestions");
        
        try {
            Map<String, List<String>> allSuggestions = new HashMap<>();
            List<String> categories = List.of("TUITION", "COURSES", "DEADLINES", "POLICIES", "TECHNICAL");
            
            for (String category : categories) {
                allSuggestions.put(category, chatService.getSuggestions(category));
            }
            
            return ResponseEntity.ok(allSuggestions);
            
        } catch (Exception e) {
            logger.error("Error fetching all suggestions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of());
        }
    }

    /**
     * Submit feedback for a chat session.
     * Allows users to rate and provide comments on responses.
     *
     * POST /api/chat/feedback
     */
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Received feedback: sessionId={}, rating={}", 
                    request.sessionId(), request.rating());
        
        try {
            String userIp = getClientIp(httpRequest);
            
            FeedbackRequest enrichedRequest = new FeedbackRequest(
                    request.sessionId(),
                    request.rating(),
                    request.comment(),
                    request.helpful(),
                    request.feedbackType(),
                    userIp
            );
            
            feedbackService.saveFeedback(enrichedRequest);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Thank you for your feedback!"
            ));
            
        } catch (Exception e) {
            logger.error("Error submitting feedback", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to submit feedback"
                    ));
        }
    }

    /**
     * Clear chat session.
     * Utility endpoint to start a new session.
     *
     * POST /api/chat/clear/{sessionId}
     */
    @PostMapping("/clear/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        logger.info("Clearing session: {}", sessionId);
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Session cleared (history retained in database)"
        ));
    }

    /**
     * Health check for chat service.
     *
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "chat"
        ));
    }

    /**
     * Extract client IP address from request.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
