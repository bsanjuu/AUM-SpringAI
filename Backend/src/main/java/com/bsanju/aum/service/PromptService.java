package com.bsanju.aum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for loading and managing prompt templates.
 * Handles different prompt templates for various categories.
 */
@Service
public class PromptService {

    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);

    private final ResourceLoader resourceLoader;
    private final Map<String, String> promptCache;

    public PromptService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.promptCache = new HashMap<>();
        loadPrompts();
    }

    /**
     * Load all prompt templates into cache.
     */
    private void loadPrompts() {
        logger.info("Loading prompt templates");

        try {
            promptCache.put("SYSTEM", loadPromptTemplate("system-prompt.txt"));
            promptCache.put("TUITION", loadPromptTemplate("tuition-prompt-template.txt"));
            promptCache.put("COURSES", loadPromptTemplate("course-prompt-template.txt"));
            promptCache.put("POLICIES", loadPromptTemplate("policy-prompt-template.txt"));
            promptCache.put("FALLBACK", loadPromptTemplate("fallback-prompt.txt"));

            logger.info("Successfully loaded {} prompt templates", promptCache.size());

        } catch (Exception e) {
            logger.error("Error loading prompt templates", e);
        }
    }

    /**
     * Load a prompt template from the classpath.
     *
     * @param filename The template filename
     * @return The template content
     */
    private String loadPromptTemplate(String filename) throws IOException {
        String location = "classpath:prompts/" + filename;
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            logger.warn("Prompt template not found: {}", filename);
            return getDefaultPrompt();
        }

        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading prompt template: {}", filename, e);
            throw e;
        }
    }

    /**
     * Get prompt template for a specific category.
     *
     * @param category The category (TUITION, COURSES, etc.)
     * @return The prompt template
     */
    public String getPromptForCategory(String category) {
        if (category == null) {
            return getSystemPrompt();
        }

        String prompt = promptCache.get(category.toUpperCase());
        if (prompt == null) {
            logger.debug("No specific prompt for category: {}, using system prompt", category);
            prompt = getSystemPrompt();
        }

        return prompt;
    }

    /**
     * Get the system prompt template.
     *
     * @return The system prompt
     */
    public String getSystemPrompt() {
        return promptCache.getOrDefault("SYSTEM", getDefaultPrompt());
    }

    /**
     * Get the fallback prompt template.
     *
     * @return The fallback prompt
     */
    public String getFallbackPrompt() {
        return promptCache.getOrDefault("FALLBACK", getDefaultPrompt());
    }

    /**
     * Build a complete prompt with context and question.
     *
     * @param template The prompt template
     * @param context The context documents
     * @param question The user question
     * @return The complete prompt
     */
    public String buildPrompt(String template, String context, String question) {
        return template
                .replace("{context}", context)
                .replace("{question}", question);
    }

    /**
     * Get default prompt when templates are not available.
     *
     * @return Default prompt template
     */
    private String getDefaultPrompt() {
        return """
               You are a helpful university FAQ assistant.
               
               Context: {context}
               
               Question: {question}
               
               Please provide an accurate and helpful response based on the context above.
               """;
    }

    /**
     * Reload all prompt templates (for dynamic updates).
     */
    public void reloadPrompts() {
        logger.info("Reloading prompt templates");
        promptCache.clear();
        loadPrompts();
    }

    /**
     * Get all available prompt categories.
     *
     * @return Set of category names
     */
    public java.util.Set<String> getAvailableCategories() {
        return promptCache.keySet();
    }
}
