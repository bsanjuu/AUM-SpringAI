// service/MetricsService.java
package com.bsanju.aum.service;

import com.bsanju.aum.model.dto.ChatRequest;
import com.bsanju.aum.model.dto.ChatResponse;
import com.bsanju.aum.model.dto.MetricsDto;
import com.bsanju.aum.model.entity.QueryMetrics;
import com.bsanju.aum.repository.MetricsRepository;
import com.bsanju.aum.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final MetricsRepository metricsRepository;
    private final FeedbackRepository feedbackRepository;

    public MetricsService(MetricsRepository metricsRepository, FeedbackRepository feedbackRepository) {
        this.metricsRepository = metricsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public void recordQuery(ChatRequest request, ChatResponse response) {
        try {
            QueryMetrics metrics = new QueryMetrics();
            metrics.setSessionId(request.sessionId());
            metrics.setCategory(request.category());
            metrics.setConfidenceScore(response.confidence());
            metrics.setResponseTimeMs(response.responseTimeMs());
            metrics.setNeedsHumanAssistance(response.needsHumanAssistance());
            metrics.setDocumentsRetrieved(response.sources().size());
            metrics.setQueryLength(request.message().length());
            metrics.setResponseLength(response.response().length());

            metricsRepository.save(metrics);
            logger.debug("Recorded metrics for session: {}", request.sessionId());
        } catch (Exception e) {
            logger.error("Failed to record metrics", e);
        }
    }

    public MetricsDto getDailyMetrics(LocalDate date) {
        LocalDate endDate = date.plusDays(1);
        return buildMetricsDto(date, endDate, "daily");
    }

    public MetricsDto getWeeklyMetrics(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(7);
        return buildMetricsDto(startDate, endDate, "weekly");
    }

    public MetricsDto getMonthlyMetrics(LocalDate startDate) {
        LocalDate endDate = startDate.plusMonths(1);
        return buildMetricsDto(startDate, endDate, "monthly");
    }

    private MetricsDto buildMetricsDto(LocalDate startDate, LocalDate endDate, String periodType) {
        // Query metrics
        List<QueryMetrics> metrics = metricsRepository.findByDateOnlyBetween(startDate, endDate);
        long totalQueries = metrics.size();

        double avgConfidence = metrics.stream()
                .mapToDouble(QueryMetrics::getConfidenceScore)
                .average()
                .orElse(0.0);

        double avgResponseTime = metrics.stream()
                .mapToDouble(QueryMetrics::getResponseTimeMs)
                .average()
                .orElse(0.0);

        double humanAssistanceRate = totalQueries > 0 ?
                metrics.stream().mapToLong(m -> m.isNeedsHumanAssistance() ? 1 : 0).sum() / (double) totalQueries : 0.0;

        // Category distribution
        Map<String, Long> categoryCounts = metrics.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCategory() != null ? m.getCategory() : "UNKNOWN",
                        Collectors.counting()
                ));

        // Hourly distribution
        Map<Integer, Long> hourlyDistribution = metrics.stream()
                .collect(Collectors.groupingBy(
                        QueryMetrics::getHourOfDay,
                        Collectors.counting()
                ));

        // Daily queries
        Map<LocalDate, Long> dailyQueries = metrics.stream()
                .collect(Collectors.groupingBy(
                        QueryMetrics::getDateOnly,
                        Collectors.counting()
                ));

        // Feedback stats
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        long totalFeedback = feedbackRepository.countTotalFeedbackSince(startDateTime);
        long helpfulFeedback = feedbackRepository.countHelpfulFeedbackSince(startDateTime);
        Double avgRating = feedbackRepository.averageRatingSince(startDateTime);

        double helpfulPercentage = totalFeedback > 0 ? helpfulFeedback / (double) totalFeedback * 100 : 0.0;

        MetricsDto.FeedbackStats feedbackStats = new MetricsDto.FeedbackStats(
                totalFeedback,
                helpfulPercentage,
                avgRating != null ? avgRating : 0.0
        );

        MetricsDto.Period period = new MetricsDto.Period(
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                periodType
        );

        return new MetricsDto(
                totalQueries,
                avgConfidence,
                avgResponseTime,
                humanAssistanceRate,
                categoryCounts,
                hourlyDistribution,
                dailyQueries,
                feedbackStats,
                period
        );
    }
}

// service/FeedbackService.java
package com.bsanju.aum.service;

import com.bsanju.aum.model.dto.FeedbackRequest;
import com.bsanju.aum.model.entity.UserFeedback;
import com.bsanju.aum.repository.FeedbackRepository;
import com.bsanju.aum.event.FeedbackReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FeedbackService(FeedbackRepository feedbackRepository, ApplicationEventPublisher eventPublisher) {
        this.feedbackRepository = feedbackRepository;
        this.eventPublisher = eventPublisher;
    }

    public void recordFeedback(FeedbackRequest request) {
        try {
            UserFeedback feedback = new UserFeedback();
            feedback.setSessionId(request.sessionId());
            feedback.setChatSessionId(request.chatSessionId());
            feedback.setHelpful(request.helpful());
            feedback.setRating(request.rating());
            feedback.setComments(request.comments());
            feedback.setCategory(request.category());

            feedbackRepository.save(feedback);

            // Publish event for further processing
            eventPublisher.publishEvent(new FeedbackReceivedEvent(this, feedback));

            logger.info("Recorded feedback: sessionId={}, helpful={}, rating={}",
                    request.sessionId(), request.helpful(), request.rating());

        } catch (Exception e) {
            logger.error("Failed to record feedback", e);
            throw new RuntimeException("Failed to record feedback", e);
        }
    }
}

// service/DocumentIndexingService.java
package com.bsanju.aum.service;

import com.bsanju.aum.model.entity.UniversityDocument;
import com.bsanju.aum.repository.DocumentRepository;
import com.bsanju.aum.event.DocumentUpdatedEvent;
import com.bsanju.aum.util.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class DocumentIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexingService.class);

    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ResourceLoader resourceLoader;
    private final DocumentProcessor documentProcessor;

    public DocumentIndexingService(
            DocumentRepository documentRepository,
            ApplicationEventPublisher eventPublisher,
            ResourceLoader resourceLoader,
            DocumentProcessor documentProcessor) {
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
        this.resourceLoader = resourceLoader;
        this.documentProcessor = documentProcessor;
    }

    @PostConstruct
    public void initializeDocuments() {
        logger.info("Initializing university documents...");
        loadInitialDocuments();
    }

    public void loadInitialDocuments() {
        try {
            // Load JSON data files
            loadJsonDocuments();

            // Load Markdown FAQ files
            loadMarkdownFAQs();

            // Load policy documents
            loadPolicyDocuments();

            logger.info("Successfully loaded initial documents");
        } catch (Exception e) {
            logger.error("Failed to load initial documents", e);
        }
    }

    private void loadJsonDocuments() {
        try {
            // Load tuition data
            Resource tuitionResource = resourceLoader.getResource("classpath:data/university-data/tuition-fees.json");
            if (tuitionResource.exists()) {
                String content = documentProcessor.processJsonToText(tuitionResource);
                createDocument("Tuition and Fees Information", content,
                        UniversityDocument.DocumentCategory.TUITION, "tuition-fees.json");
            }

            // Load course catalog
            Resource catalogResource = resourceLoader.getResource("classpath:data/university-data/course-catalog.json");
            if (catalogResource.exists()) {
                String content = documentProcessor.processJsonToText(catalogResource);
                createDocument("Course Catalog", content,
                        UniversityDocument.DocumentCategory.COURSES, "course-catalog.json");
            }

            // Load academic calendar
            Resource calendarResource = resourceLoader.getResource("classpath:data/university-data/academic-calendar.json");
            if (calendarResource.exists()) {
                String content = documentProcessor.processJsonToText(calendarResource);
                createDocument("Academic Calendar", content,
                        UniversityDocument.DocumentCategory.DEADLINES, "academic-calendar.json");
            }

        } catch (Exception e) {
            logger.error("Failed to load JSON documents", e);
        }
    }

    private void loadMarkdownFAQs() {
        try {
            String[] faqFiles = {
                    "tuition-faqs.md", "course-faqs.md", "registration-faqs.md", "general-faqs.md"
            };

            for (String filename : faqFiles) {
                Resource resource = resourceLoader.getResource("classpath:data/university-data/faqs/" + filename);
                if (resource.exists()) {
                    String content = documentProcessor.processMarkdownToText(resource);
                    String title = filename.replace("-", " ").replace(".md", "").toUpperCase();
                    UniversityDocument.DocumentCategory category = determineCategoryFromFilename(filename);
                    createDocument(title, content, category, filename);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load FAQ documents", e);
        }
    }

    private void loadPolicyDocuments() {
        try {
            String[] policyFiles = {
                    "academic-policies.md", "financial-policies.md",
                    "registration-policies.md", "international-student-policies.md"
            };

            for (String filename : policyFiles) {
                Resource resource = resourceLoader.getResource("classpath:data/university-data/policies/" + filename);
                if (resource.exists()) {
                    String content = documentProcessor.processMarkdownToText(resource);
                    String title = filename.replace("-", " ").replace(".md", "").toUpperCase();
                    createDocument(title, content,
                            UniversityDocument.DocumentCategory.POLICIES, filename);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load policy documents", e);
        }
    }

    private UniversityDocument.DocumentCategory determineCategoryFromFilename(String filename) {
        if (filename.contains("tuition")) return UniversityDocument.DocumentCategory.TUITION;
        if (filename.contains("course")) return UniversityDocument.DocumentCategory.COURSES;
        if (filename.contains("registration")) return UniversityDocument.DocumentCategory.DEADLINES;
        return UniversityDocument.DocumentCategory.GENERAL;
    }

    private void createDocument(String title, String content,
                                UniversityDocument.DocumentCategory category, String source) {
        // Check if document already exists
        if (documentRepository.findByTitleAndActiveTrue(title).isPresent()) {
            logger.debug("Document already exists: {}", title);
            return;
        }

        UniversityDocument document = new UniversityDocument();
        document.setTitle(title);
        document.setContent(content);
        document.setCategory(category);
        document.setSource(source);
        document.setVersion("1.0");
        document.setActive(true);

        UniversityDocument saved = documentRepository.save(document);

        // Publish event for indexing in vector store
        eventPublisher.publishEvent(new DocumentUpdatedEvent(this, saved));

        logger.info("Created document: {}", title);
    }

    public void indexDocument(UniversityDocument document) {
        eventPublisher.publishEvent(new DocumentUpdatedEvent(this, document));
    }

    public void reindexAllDocuments() {
        logger.info("Starting reindexing of all documents...");

        List<UniversityDocument> allDocuments = documentRepository.findByActiveTrue();
        for (UniversityDocument doc : allDocuments) {
            eventPublisher.publishEvent(new DocumentUpdatedEvent(this, doc));
        }

        logger.info("Reindexing completed for {} documents", allDocuments.size());
    }
}

// service/PromptService.java
package com.bsanju.aum.service;

import com.bsanju.aum.util.PromptTemplates;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromptService {

    private final ResourceLoader resourceLoader;
    private final PromptTemplates promptTemplates;
    private final Map<String, String> cachedPrompts = new ConcurrentHashMap<>();

    public PromptService(ResourceLoader resourceLoader, PromptTemplates promptTemplates) {
        this.resourceLoader = resourceLoader;
        this.promptTemplates = promptTemplates;
    }

    @PostConstruct
    public void loadPrompts() {
        loadPromptFromFile("system-prompt", "classpath:prompts/system-prompt.txt");
        loadPromptFromFile("tuition-prompt", "classpath:prompts/tuition-prompt-template.txt");
        loadPromptFromFile("course-prompt", "classpath:prompts/course-prompt-template.txt");
        loadPromptFromFile("policy-prompt", "classpath:prompts/policy-prompt-template.txt");
        loadPromptFromFile("fallback-prompt", "classpath:prompts/fallback-prompt.txt");
    }

    private void loadPromptFromFile(String key, String resourcePath) {
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (resource.exists()) {
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                cachedPrompts.put(key, content);
            }
        } catch (IOException e) {
            // Fall back to default prompts from PromptTemplates
            switch (key) {
                case "tuition-prompt" -> cachedPrompts.put(key, promptTemplates.getTuitionPromptTemplate());
                case "course-prompt" -> cachedPrompts.put(key, promptTemplates.getCoursePromptTemplate());
                case "policy-prompt" -> cachedPrompts.put(key, promptTemplates.getPolicyPromptTemplate());
                case "fallback-prompt" -> cachedPrompts.put(key, promptTemplates.getFallbackPrompt());
            }
        }
    }

    public String getPrompt(String key) {
        return cachedPrompts.get(key);
    }

    public String getSystemPrompt() {
        return cachedPrompts.get("system-prompt");
    }

    public String getCategoryPrompt(String category) {
        return switch (category.toUpperCase()) {
            case "TUITION" -> cachedPrompts.get("tuition-prompt");
            case "COURSES" -> cachedPrompts.get("course-prompt");
            case "POLICIES" -> cachedPrompts.get("policy-prompt");
            default -> cachedPrompts.get("system-prompt");
        };
    }
}