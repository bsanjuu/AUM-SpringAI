package com.bsanju.aum.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text processing and manipulation.
 * Provides common text operations used throughout the application.
 */
public class TextUtils {

    /**
     * Clean and normalize text.
     */
    public static String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.trim()
                .replaceAll("\\s+", " ") // Normalize whitespace
                .replaceAll("[\\r\\n]+", "\n"); // Normalize line breaks
    }

    /**
     * Extract email addresses from text.
     */
    public static List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();
        if (text == null) {
            return emails;
        }

        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            emails.add(matcher.group());
        }

        return emails;
    }

    /**
     * Extract phone numbers from text.
     */
    public static List<String> extractPhoneNumbers(String text) {
        List<String> phones = new ArrayList<>();
        if (text == null) {
            return phones;
        }

        Pattern pattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            phones.add(matcher.group());
        }

        return phones;
    }

    /**
     * Extract dates from text (MM/DD/YYYY format).
     */
    public static List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();
        if (text == null) {
            return dates;
        }

        Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            dates.add(matcher.group());
        }

        return dates;
    }

    /**
     * Truncate text to a maximum length with ellipsis.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Capitalize first letter of each word.
     */
    public static String titleCase(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Remove HTML tags from text.
     */
    public static String stripHtml(String text) {
        if (text == null) {
            return null;
        }

        return text.replaceAll("<[^>]*>", "");
    }

    /**
     * Count words in text.
     */
    public static int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    /**
     * Check if text contains any of the keywords (case-insensitive).
     */
    public static boolean containsAnyKeyword(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove special characters, keep only alphanumeric and spaces.
     */
    public static String removeSpecialCharacters(String text) {
        if (text == null) {
            return null;
        }

        return text.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    /**
     * Extract sentences from text.
     */
    public static List<String> extractSentences(String text) {
        List<String> sentences = new ArrayList<>();
        if (text == null) {
            return sentences;
        }

        String[] parts = text.split("[.!?]+");
        for (String sentence : parts) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }

        return sentences;
    }

    /**
     * Check if string is null or blank.
     */
    public static boolean isNullOrBlank(String text) {
        return text == null || text.isBlank();
    }

    /**
     * Generate excerpt from text.
     */
    public static String generateExcerpt(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        // Try to cut at sentence boundary
        String truncated = text.substring(0, maxLength);
        int lastPeriod = truncated.lastIndexOf('.');
        if (lastPeriod > maxLength / 2) {
            return truncated.substring(0, lastPeriod + 1);
        }

        // Cut at word boundary
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > 0) {
            return truncated.substring(0, lastSpace) + "...";
        }

        return truncated + "...";
    }
}
