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
        String formatted = fieldName.replace("_", " ").replace("-", " ");

        // Capitalize first letter of each word
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : formatted.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
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
