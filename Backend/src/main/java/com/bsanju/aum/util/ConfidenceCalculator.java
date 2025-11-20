package com.bsanju.aum.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for calculating confidence scores for AI responses.
 * Uses various heuristics to determine response quality and reliability.
 */
@Component
public class ConfidenceCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ConfidenceCalculator.class);

    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.50;

    /**
     * Calculate confidence score for a response (instance method).
     *
     * @param response The AI response
     * @param relevantDocs List of relevant documents retrieved
     * @return Confidence score between 0.0 and 1.0
     */
    public double calculate(String response, List<String> relevantDocs) {
        int documentsRetrieved = relevantDocs != null ? relevantDocs.size() : 0;
        // Category is not passed, so we use null (will result in default confidence)
        return calculateConfidence(response, documentsRetrieved, null);
    }

    /**
     * Calculate confidence score for a response.
     *
     * @param response The AI response
     * @param documentsRetrieved Number of documents retrieved
     * @param category The query category
     * @return Confidence score between 0.0 and 1.0
     */
    public static double calculateConfidence(String response,
                                            int documentsRetrieved,
                                            String category) {
        double confidence = 0.0;

        // Base confidence from document retrieval (40% weight)
        confidence += calculateDocumentConfidence(documentsRetrieved) * 0.4;

        // Response quality confidence (40% weight)
        confidence += calculateResponseQualityConfidence(response) * 0.4;

        // Category-specific confidence (20% weight)
        confidence += calculateCategoryConfidence(category, response) * 0.2;

        // Ensure confidence is between 0 and 1
        confidence = Math.max(0.0, Math.min(1.0, confidence));

        logger.debug("Calculated confidence: {} (docs: {}, category: {})", 
                    confidence, documentsRetrieved, category);

        return confidence;
    }

    /**
     * Calculate confidence based on number of documents retrieved.
     */
    private static double calculateDocumentConfidence(int documentsRetrieved) {
        if (documentsRetrieved == 0) {
            return 0.0;
        } else if (documentsRetrieved == 1) {
            return 0.5;
        } else if (documentsRetrieved >= 3) {
            return 1.0;
        } else {
            return 0.75;
        }
    }

    /**
     * Calculate confidence based on response quality.
     */
    private static double calculateResponseQualityConfidence(String response) {
        if (response == null || response.isBlank()) {
            return 0.0;
        }

        double qualityScore = 0.0;

        // Length check (responses should be substantive)
        int length = response.length();
        if (length < 50) {
            qualityScore += 0.3;
        } else if (length < 150) {
            qualityScore += 0.6;
        } else {
            qualityScore += 1.0;
        }

        // Check for uncertainty markers
        String lowerResponse = response.toLowerCase();
        if (containsUncertaintyMarkers(lowerResponse)) {
            qualityScore *= 0.7; // Reduce confidence
        }

        // Check for specific information (numbers, dates, etc.)
        if (containsSpecificInformation(response)) {
            qualityScore *= 1.1; // Boost confidence
        }

        return Math.min(1.0, qualityScore);
    }

    /**
     * Calculate category-specific confidence.
     */
    private static double calculateCategoryConfidence(String category, String response) {
        if (category == null) {
            return 0.5; // Neutral confidence for unknown category
        }

        String lowerResponse = response.toLowerCase();

        return switch (category.toUpperCase()) {
            case "TUITION" -> containsTuitionKeywords(lowerResponse) ? 1.0 : 0.6;
            case "COURSES" -> containsCourseKeywords(lowerResponse) ? 1.0 : 0.6;
            case "DEADLINES" -> containsDateInformation(response) ? 1.0 : 0.5;
            case "POLICIES" -> containsPolicyKeywords(lowerResponse) ? 1.0 : 0.6;
            case "TECHNICAL" -> containsTechnicalKeywords(lowerResponse) ? 1.0 : 0.6;
            default -> 0.7; // Default confidence for other categories
        };
    }

    /**
     * Check if response contains uncertainty markers.
     */
    private static boolean containsUncertaintyMarkers(String response) {
        List<String> uncertaintyWords = List.of(
                "i don't know", "not sure", "might", "maybe", "possibly",
                "i'm not certain", "unclear", "cannot confirm", "unable to",
                "i don't have", "no information"
        );

        return uncertaintyWords.stream().anyMatch(response::contains);
    }

    /**
     * Check if response contains specific information.
     */
    private static boolean containsSpecificInformation(String response) {
        // Check for numbers, dates, amounts, etc.
        return response.matches(".*\\d+.*") || // Contains digits
               response.matches(".*\\$.*") ||  // Contains currency
               response.matches(".*(january|february|march|april|may|june|july|august|september|october|november|december).*");
    }

    /**
     * Check for tuition-related keywords.
     */
    private static boolean containsTuitionKeywords(String response) {
        List<String> keywords = List.of(
                "tuition", "fee", "payment", "cost", "price", "dollar", "$", "semester"
        );
        return keywords.stream().anyMatch(response::contains);
    }

    /**
     * Check for course-related keywords.
     */
    private static boolean containsCourseKeywords(String response) {
        List<String> keywords = List.of(
                "course", "class", "credit", "prerequisite", "registration", 
                "enroll", "schedule", "semester"
        );
        return keywords.stream().anyMatch(response::contains);
    }

    /**
     * Check for date information.
     */
    private static boolean containsDateInformation(String response) {
        return response.matches(".*(\\d{1,2}/\\d{1,2}/\\d{2,4}).*") || // Date format
               response.matches(".*(january|february|march|april|may|june|july|august|september|october|november|december).*"); // Month names
    }

    /**
     * Check for policy-related keywords.
     */
    private static boolean containsPolicyKeywords(String response) {
        List<String> keywords = List.of(
                "policy", "rule", "regulation", "requirement", "must", "should",
                "allowed", "prohibited", "procedure"
        );
        return keywords.stream().anyMatch(response::contains);
    }

    /**
     * Check for technical keywords.
     */
    private static boolean containsTechnicalKeywords(String response) {
        List<String> keywords = List.of(
                "login", "password", "access", "portal", "system", "account",
                "email", "website", "technical", "support"
        );
        return keywords.stream().anyMatch(response::contains);
    }

    /**
     * Determine if response needs human assistance based on confidence.
     */
    public static boolean needsHumanAssistance(double confidence) {
        return confidence < LOW_CONFIDENCE_THRESHOLD;
    }

    /**
     * Determine if response is high confidence.
     */
    public static boolean isHighConfidence(double confidence) {
        return confidence >= HIGH_CONFIDENCE_THRESHOLD;
    }

    /**
     * Get confidence level description.
     */
    public static String getConfidenceLevel(double confidence) {
        if (confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            return "HIGH";
        } else if (confidence >= LOW_CONFIDENCE_THRESHOLD) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
