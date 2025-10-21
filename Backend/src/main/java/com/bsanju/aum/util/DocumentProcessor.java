// util/DocumentProcessor.java
package com.bsanju.aum.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Component
public class DocumentProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String processJsonToText(Resource resource) throws IOException {
        String jsonContent = resource.getContentAsString(StandardCharsets.UTF_8);
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        StringBuilder textContent = new StringBuilder();
        processJsonNode(rootNode, textContent, "");

        return textContent.toString();
    }

    private void processJsonNode(JsonNode node, StringBuilder content, String prefix) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = node.get(fieldName);

                if (fieldValue.isValueNode()) {
                    content.append(prefix).append(formatFieldName(fieldName))
                            .append(": ").append(fieldValue.asText()).append("\n");
                } else if (fieldValue.isObject()) {
                    content.append(prefix).append(formatFieldName(fieldName)).append(":\n");
                    processJsonNode(fieldValue, content, prefix + "  ");
                } else if (fieldValue.isArray()) {
                    content.append(prefix).append(formatFieldName(fieldName)).append(":\n");
                    for (JsonNode arrayItem : fieldValue) {
                        if (arrayItem.isValueNode()) {
                            content.append(prefix).append("- ").append(arrayItem.asText()).append("\n");
                        } else {
                            processJsonNode(arrayItem, content, prefix + "  ");
                        }
                    }
                }
            }
        }
    }

    private String formatFieldName(String fieldName) {
        return fieldName.replace("_", " ").replace("-", " ")
                .replaceAll("\\b\\w", m -> m.group().toUpperCase());
    }

    public String processMarkdownToText(Resource resource) throws IOException {
        String markdownContent = resource.getContentAsString(StandardCharsets.UTF_8);

        // Simple markdown to text conversion
        // Remove markdown syntax while preserving content
        return markdownContent
                .replaceAll("#+ ", "") // Remove headers
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1") // Remove bold
                .replaceAll("\\*(.*?)\\*", "$1") // Remove italic
                .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1") // Remove links, keep text
                .replaceAll("```[\\s\\S]*?```", "") // Remove code blocks
                .replaceAll("`(.*?)`", "$1") // Remove inline code
                .replaceAll("^\\s*[-*+]\\s+", "", java.util.regex.Pattern.MULTILINE) // Remove list markers
                .replaceAll("\\n{3,}", "\n\n") // Normalize line breaks
                .trim();
    }

    public String extractKeywords(String content) {
        // Simple keyword extraction
        return content.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public boolean isContentValid(String content) {
        return content != null &&
                !content.trim().isEmpty() &&
                content.trim().length() > 10;
    }
}

// util/TextUtils.java
package com.bsanju.aum.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class TextUtils {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");

    private static final List<String> STOP_WORDS = Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
            "to", "was", "will", "with", "the", "this", "but", "they", "have",
            "had", "what", "said", "each", "which", "she", "do", "how", "their",
            "if", "up", "out", "many", "then", "them", "these", "so", "some"
    );

    public String normalizeText(String text) {
        if (text == null) return "";

        // Normalize unicode characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Remove diacritical marks
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase
        normalized = normalized.toLowerCase();

        // Normalize whitespace
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");

        return normalized.trim();
    }

    public String sanitizeInput(String input) {
        if (input == null) return "";

        // Remove potentially harmful characters but keep basic punctuation
        String sanitized = input.replaceAll("[<>\"';&]", "");

        // Limit length
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }

        return sanitized.trim();
    }

    public List<String> extractKeywords(String text) {
        String normalized = normalizeText(text);

        // Remove punctuation except hyphens and apostrophes
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s'-]", " ");

        // Split into words and filter
        return Arrays.stream(normalized.split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .filter(word -> !word.matches("\\d+")) // Remove pure numbers
                .distinct()
                .limit(20) // Limit to top 20 keywords
                .toList();
    }

    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;

        List<String> keywords1 = extractKeywords(text1);
        List<String> keywords2 = extractKeywords(text2);

        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0;

        long commonKeywords = keywords1.stream()
                .mapToLong(keyword -> keywords2.contains(keyword) ? 1 : 0)
                .sum();

        return (2.0 * commonKeywords) / (keywords1.size() + keywords2.size());
    }

    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        // Try to truncate at word boundary
        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');

        if (lastSpace > maxLength * 0.8) { // If space is reasonably close to end
            return truncated.substring(0, lastSpace) + "...";
        } else {
            return truncated + "...";
        }
    }

    public boolean isProfanity(String text) {
        // Basic profanity filter - in production, use a more comprehensive solution
        List<String> basicProfanity = Arrays.asList(
                "spam", "scam", "hack", "phishing"
        );

        String normalized = normalizeText(text);
        return basicProfanity.stream().anyMatch(normalized::contains);
    }

    public String maskSensitiveInfo(String text) {
        // Mask potential sensitive information
        String masked = text;

        // Mask email addresses
        masked = masked.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "[EMAIL]");

        // Mask phone numbers
        masked = masked.replaceAll("\\b\\d{3}-\\d{3}-\\d{4}\\b", "[PHONE]");
        masked = masked.replaceAll("\\b\\(\\d{3}\\)\\s*\\d{3}-\\d{4}\\b", "[PHONE]");

        // Mask potential SSN
        masked = masked.replaceAll("\\b\\d{3}-\\d{2}-\\d{4}\\b", "[SSN]");

        return masked;
    }
}