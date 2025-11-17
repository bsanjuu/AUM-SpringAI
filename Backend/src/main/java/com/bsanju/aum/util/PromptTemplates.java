package com.bsanju.aum.util;

import java.util.List;

/**
 * Utility class for managing prompt templates and building context strings.
 * Provides methods for constructing prompts for different categories.
 */
public class PromptTemplates {

    /**
     * Build a system prompt with context for RAG.
     *
     * @param category The query category
     * @param context The retrieved document context
     * @param question The user question
     * @return Complete prompt string
     */
    public static String buildPromptWithContext(String category, String context, String question) {
        String template = getTemplateForCategory(category);
        return template
                .replace("{context}", context)
                .replace("{question}", question);
    }

    /**
     * Get the appropriate template for a category.
     */
    private static String getTemplateForCategory(String category) {
        if (category == null) {
            return getDefaultTemplate();
        }

        return switch (category.toUpperCase()) {
            case "TUITION" -> getTuitionTemplate();
            case "COURSES" -> getCourseTemplate();
            case "DEADLINES" -> getDeadlineTemplate();
            case "POLICIES" -> getPolicyTemplate();
            case "TECHNICAL" -> getTechnicalTemplate();
            default -> getDefaultTemplate();
        };
    }

    /**
     * Default system template.
     */
    private static String getDefaultTemplate() {
        return """
               You are a helpful university FAQ assistant. Answer questions accurately based on the provided context.
               
               Context:
               {context}
               
               Question: {question}
               
               Please provide a clear, helpful answer based on the context above.
               """;
    }

    /**
     * Tuition-specific template.
     */
    private static String getTuitionTemplate() {
        return """
               You are assisting with tuition and financial information.
               Provide specific amounts, deadlines, and payment information from the context.
               
               Context:
               {context}
               
               Question: {question}
               
               Answer with specific tuition amounts, fees, and payment deadlines from the context.
               """;
    }

    /**
     * Course-specific template.
     */
    private static String getCourseTemplate() {
        return """
               You are assisting with course registration and information.
               Include prerequisites, schedules, and registration procedures from the context.
               
               Context:
               {context}
               
               Question: {question}
               
               Answer with course details, prerequisites, and registration information from the context.
               """;
    }

    /**
     * Deadline-specific template.
     */
    private static String getDeadlineTemplate() {
        return """
               You are providing information about important dates and deadlines.
               Always mention specific dates and what they apply to.
               
               Context:
               {context}
               
               Question: {question}
               
               Answer with specific dates and deadlines from the context.
               """;
    }

    /**
     * Policy-specific template.
     */
    private static String getPolicyTemplate() {
        return """
               You are explaining university policies and procedures.
               Be clear about requirements and any exceptions.
               
               Context:
               {context}
               
               Question: {question}
               
               Explain the relevant policy clearly based on the context.
               """;
    }

    /**
     * Technical support template.
     */
    private static String getTechnicalTemplate() {
        return """
               You are providing technical support for university systems.
               Provide step-by-step guidance when applicable.
               
               Context:
               {context}
               
               Question: {question}
               
               Provide technical assistance based on the context.
               """;
    }

    /**
     * Build context string from document list.
     *
     * @param documents List of document contents
     * @return Formatted context string
     */
    public static String buildContextFromDocuments(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            return "No relevant documents found.";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            context.append("Document ").append(i + 1).append(":\n");
            context.append(documents.get(i));
            context.append("\n\n");
        }

        return context.toString();
    }

    /**
     * Create fallback prompt when no context is available.
     */
    public static String buildFallbackPrompt(String question) {
        return """
               You are a university FAQ assistant, but you don't have specific information about this question.
               
               Question: %s
               
               Politely explain that you don't have information on this topic and suggest contacting the appropriate university office.
               """.formatted(question);
    }

    /**
     * Detect category from question text.
     */
    public static String detectCategory(String question) {
        String lowerQuestion = question.toLowerCase();

        if (containsAny(lowerQuestion, "tuition", "fee", "payment", "cost", "price")) {
            return "TUITION";
        } else if (containsAny(lowerQuestion, "course", "class", "registration", "enroll")) {
            return "COURSES";
        } else if (containsAny(lowerQuestion, "deadline", "date", "when", "due")) {
            return "DEADLINES";
        } else if (containsAny(lowerQuestion, "policy", "rule", "regulation", "requirement")) {
            return "POLICIES";
        } else if (containsAny(lowerQuestion, "login", "password", "access", "technical", "portal")) {
            return "TECHNICAL";
        } else {
            return "GENERAL";
        }
    }

    /**
     * Helper method to check if string contains any of the keywords.
     */
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Truncate context to fit within token limits.
     */
    public static String truncateContext(String context, int maxLength) {
        if (context == null || context.length() <= maxLength) {
            return context;
        }

        return context.substring(0, maxLength) + "\n...(truncated)";
    }
}
