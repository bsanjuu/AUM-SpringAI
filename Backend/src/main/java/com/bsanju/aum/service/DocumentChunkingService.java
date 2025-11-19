package com.bsanju.aum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for chunking large documents into smaller pieces for vector indexing.
 * Uses intelligent chunking to maintain context and semantic meaning.
 */
@Service
public class DocumentChunkingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentChunkingService.class);

    // Optimal chunk size for embeddings (characters)
    private static final int TARGET_CHUNK_SIZE = 1000;
    private static final int MAX_CHUNK_SIZE = 1500;
    private static final int MIN_CHUNK_SIZE = 200;
    private static final int OVERLAP_SIZE = 200; // Overlap between chunks to maintain context

    /**
     * Split a document into chunks suitable for vector indexing.
     *
     * @param content The full document content
     * @param title The document title (used for context)
     * @return List of document chunks
     */
    public List<DocumentChunk> chunkDocument(String content, String title) {
        logger.debug("Chunking document: {} (length: {} chars)", title, content.length());

        List<DocumentChunk> chunks = new ArrayList<>();

        // If content is small enough, return as single chunk
        if (content.length() <= MAX_CHUNK_SIZE) {
            chunks.add(new DocumentChunk(content, title, 0, 1));
            logger.debug("Document fits in single chunk");
            return chunks;
        }

        // Split by paragraphs first
        String[] paragraphs = content.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;
        int totalChunks = estimateChunkCount(content.length());

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();

            if (paragraph.isEmpty()) {
                continue;
            }

            // If adding this paragraph exceeds max size, save current chunk
            if (currentChunk.length() + paragraph.length() > MAX_CHUNK_SIZE && currentChunk.length() > MIN_CHUNK_SIZE) {
                chunks.add(new DocumentChunk(
                        currentChunk.toString().trim(),
                        title,
                        chunkIndex++,
                        totalChunks
                ));

                // Start new chunk with overlap from previous chunk
                currentChunk = new StringBuilder();
                String overlap = getOverlap(currentChunk.toString());
                if (!overlap.isEmpty()) {
                    currentChunk.append(overlap).append("\n\n");
                }
            }

            currentChunk.append(paragraph).append("\n\n");

            // If current chunk is close to target size, consider splitting
            if (currentChunk.length() >= TARGET_CHUNK_SIZE) {
                chunks.add(new DocumentChunk(
                        currentChunk.toString().trim(),
                        title,
                        chunkIndex++,
                        totalChunks
                ));

                // Start new chunk with overlap
                currentChunk = new StringBuilder();
                String overlap = getOverlap(paragraph);
                if (!overlap.isEmpty()) {
                    currentChunk.append(overlap).append("\n\n");
                }
            }
        }

        // Add remaining content as final chunk
        if (currentChunk.length() > 0) {
            chunks.add(new DocumentChunk(
                    currentChunk.toString().trim(),
                    title,
                    chunkIndex,
                    totalChunks
            ));
        }

        // Update total chunks count
        int actualTotal = chunks.size();
        if (actualTotal != totalChunks) {
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk oldChunk = chunks.get(i);
                chunks.set(i, new DocumentChunk(
                        oldChunk.content(),
                        oldChunk.sourceTitle(),
                        i,
                        actualTotal
                ));
            }
        }

        logger.info("Document chunked into {} pieces", chunks.size());
        return chunks;
    }

    /**
     * Get overlap text from the end of content for context continuity.
     */
    private String getOverlap(String content) {
        if (content.length() <= OVERLAP_SIZE) {
            return content;
        }

        // Try to find a sentence boundary for clean overlap
        String overlap = content.substring(Math.max(0, content.length() - OVERLAP_SIZE));
        int sentenceEnd = overlap.indexOf(". ");

        if (sentenceEnd > 0) {
            return overlap.substring(sentenceEnd + 2);
        }

        return overlap;
    }

    /**
     * Estimate number of chunks needed.
     */
    private int estimateChunkCount(int contentLength) {
        return Math.max(1, (contentLength + TARGET_CHUNK_SIZE - 1) / TARGET_CHUNK_SIZE);
    }

    /**
     * Chunk multiple documents in batch.
     *
     * @param documents List of documents to chunk
     * @return List of all chunks from all documents
     */
    public List<DocumentChunk> chunkDocuments(List<DocumentToChunk> documents) {
        logger.info("Chunking {} documents", documents.size());

        List<DocumentChunk> allChunks = new ArrayList<>();

        for (DocumentToChunk doc : documents) {
            try {
                List<DocumentChunk> chunks = chunkDocument(doc.content(), doc.title());
                allChunks.addAll(chunks);
            } catch (Exception e) {
                logger.error("Failed to chunk document: {}", doc.title(), e);
            }
        }

        logger.info("Created {} total chunks from {} documents", allChunks.size(), documents.size());
        return allChunks;
    }

    /**
     * Input document for chunking.
     */
    public record DocumentToChunk(
            String title,
            String content,
            String category,
            String source
    ) {}

    /**
     * A chunk of a document with metadata.
     */
    public record DocumentChunk(
            String content,
            String sourceTitle,
            int chunkIndex,
            int totalChunks
    ) {
        /**
         * Get the full title for this chunk including chunk number.
         */
        public String getChunkTitle() {
            if (totalChunks == 1) {
                return sourceTitle;
            }
            return sourceTitle + " (Part " + (chunkIndex + 1) + " of " + totalChunks + ")";
        }
    }
}
