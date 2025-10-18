package com.bsanju.aum.service;

import com.bsanju.aum.model.dto.ChatRequest;
import com.bsanju.aum.model.dto.ChatResponse;
import com.bsanju.aum.model.entity.ChatSession;
import com.bsanju.aum.repository.ChatSessionRepository;
import com.bsanju.aum.util.ConfidenceCalculator;
import com.bsanju.aum.util.PromptTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final KnowledgeRetrievalService retrievalService;
    private final PromptTemplates promptTemplates;
    private final ConfidenceCalculator confidenceCalculator;
    private final MetricsService metricsService;
    private final ChatSessionRepository chatSessionRepository;

    public ChatService(
            ChatClient chatClient,
            KnowledgeRetrievalService retrievalService,
            PromptTemplates promptTemplates,
            ConfidenceCalculator confidenceCalculator,
            MetricsService metricsService,
            ChatSessionRepository chatSessionRepository) {
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
        this.promptTemplates = promptTemplates;
        this.confidenceCalculator = confidenceCalculator;
        this.metricsService = metricsService;
        this.chatSessionRepository = chatSessionRepository;
    }

    public ChatResponse processQuery(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Processing query: {}", request.message());

            // Retrieve relevant context
            List<String> relevantDocs = retrievalService
                    .findRelevantDocuments(request.message(), 5);

            logger.debug("Found {} relevant documents", relevantDocs.size());

            // Build prompt with context
            String systemPrompt = promptTemplates.buildSystemPrompt(
                    relevantDocs, request.category()
            );

            // Call LLM
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.message())
                    .call()
                    .content();

            // Calculate confidence and suggestions
            double confidence = confidenceCalculator.calculate(response, relevantDocs);
            boolean needsHuman = confidence < 0.7 ||
                    response.toLowerCase().contains("i don't know") ||
                    response.toLowerCase().contains("contact");

            List<String> suggestions = generateSuggestions(request.category());
            List<String> sources = extractSources(relevantDocs);

            long responseTime = System.currentTimeMillis() - startTime;

            ChatResponse chatResponse = ChatResponse.builder()
                    .response(response)
                    .sessionId(request.sessionId())
                    .sources(sources)
                    .confidence(confidence)
                    .needsHumanAssistance(needsHuman)
                    .category(request.category())
                    .timestamp(LocalDateTime.now())
                    .suggestedQuestions(suggestions)
                    .responseTimeMs(responseTime)
                    .build();

            // Save chat session
            saveChatSession(request, chatResponse);

            // Record metrics
            metricsService.recordQuery(request, chatResponse);

            logger.info("Query processed successfully: confidence={}, responseTime={}ms",
                    confidence, responseTime);

            return chatResponse;

        } catch (Exception e) {
            logger.error("Error processing query", e);

            long responseTime = System.currentTimeMillis() - startTime;

            return ChatResponse.builder()
                    .response("I'm sorry, I'm experiencing technical difficulties. " +
                            "Please try again later or contact the registrar's office.")
                    .sessionId(request.sessionId())
                    .sources(List.of())
                    .confidence(0.0)
                    .needsHumanAssistance(true)
                    .category(request.category())
                    .timestamp(LocalDateTime.now())
                    .suggestedQuestions(getDefaultSuggestions())
                    .responseTimeMs(responseTime)
                    .build();
        }
    }

    @Cacheable(value = "faq-responses", key = "#request.message().toLowerCase()")
    public ChatResponse getCachedResponse(ChatRequest request) {
        logger.debug("Checking cache for: {}", request.message());
        return processQuery(request);
    }

    public List<String> getSuggestions(String category) {
        return generateSuggestions(category);
    }

    public List<ChatResponse> getChatHistory(String sessionId, int limit) {
        List<ChatSession> sessions = chatSessionRepository
                .findBySessionIdOrderByTimestampDesc(sessionId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private List<String> generateSuggestions(String category) {
        Map<String, List<String>> categoryQuestions = Map.of(
                "TUITION", List.of(
                        "What is the tuition per credit hour?",
                        "When are tuition payments due?",
                        "What fees are included besides tuition?",
                        "Are there payment plans available?"
                ),
                "COURSES", List.of(
                        "What courses are offered this semester?",
                        "How do I register for classes?",
                        "What are the prerequisites for this course?",
                        "When does registration open?"
                ),
                "DEADLINES", List.of(
                        "When is the last day to drop a course?",
                        "What is the graduation application deadline?",
                        "When does registration close?",
                        "What are the refund deadlines?"
                ),
                "POLICIES", List.of(
                        "What is the attendance policy?",
                        "How do I appeal a grade?",
                        "What is the academic probation policy?",
                        "Where can I find the student handbook?"
                ),
                "TECHNICAL", List.of(
                        "How do I reset my password?",
                        "How do I access the student portal?",
                        "Where do I find my class schedule?",
                        "How do I contact IT support?"
                )
        );

        return categoryQuestions.getOrDefault(category, getDefaultSuggestions());
    }

    private List<String> getDefaultSuggestions() {
        return List.of(
                "What is the tuition for undergraduate students?",
                "When is the registration deadline?",
                "How do I contact the registrar's office?",
                "Where can I find academic policies?"
        );
    }

    private List<String> extractSources(List<String> relevantDocs) {
        return relevantDocs.stream()
                .map(doc -> doc.length() > 50 ? doc.substring(0, 50) + "..." : doc)
                .limit(3)
                .collect(Collectors.toList());
    }

    private void saveChatSession(ChatRequest request, ChatResponse response) {
        try {
            ChatSession session = new ChatSession();
            session.setSessionId(request.sessionId());
            session.setUserMessage(request.message());
            session.setAiResponse(response.response());
            session.setCategory(request.category());
            session.setConfidence(response.confidence());
            session.setNeedsHumanAssistance(response.needsHumanAssistance());
            session.setTimestamp(LocalDateTime.now());

            chatSessionRepository.save(session);
        } catch (Exception e) {
            logger.error("Failed to save chat session", e);
        }
    }

    private ChatResponse convertToResponse(ChatSession session) {
        return ChatResponse.builder()
                .response(session.getAiResponse())
                .sessionId(session.getSessionId())
                .confidence(session.getConfidence())
                .needsHumanAssistance(session.isNeedsHumanAssistance())
                .category(session.getCategory())
                .timestamp(session.getTimestamp())
                .sources(List.of())
                .suggestedQuestions(List.of())
                .responseTimeMs(0)
                .build();
    }
}