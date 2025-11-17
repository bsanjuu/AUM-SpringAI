package com.bsanju.aum.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for university documents.
 * Used for document upload, retrieval, and indexing operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentDto(
        Long id,

        @NotBlank(message = "Title cannot be blank")
        @Size(max = 500, message = "Title cannot exceed 500 characters")
        String title,

        @NotBlank(message = "Content cannot be blank")
        String content,

        String category,

        String source,

        String metadata,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        boolean indexed
) {
    /**
     * Builder for DocumentDto.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a simple document DTO with title and content.
     */
    public static DocumentDto of(String title, String content, String category) {
        return new DocumentDto(
                null,
                title,
                content,
                category,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );
    }

    public static class Builder {
        private Long id;
        private String title;
        private String content;
        private String category;
        private String source;
        private String metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean indexed;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder indexed(boolean indexed) {
            this.indexed = indexed;
            return this;
        }

        public DocumentDto build() {
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            if (updatedAt == null) {
                updatedAt = LocalDateTime.now();
            }
            return new DocumentDto(
                    id,
                    title,
                    content,
                    category,
                    source,
                    metadata,
                    createdAt,
                    updatedAt,
                    indexed
            );
        }
    }
}
