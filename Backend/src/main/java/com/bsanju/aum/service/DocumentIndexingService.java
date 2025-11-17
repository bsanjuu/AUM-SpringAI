package com.bsanju.aum.service;

import com.bsanju.aum.event.DocumentUpdatedEvent;
import com.bsanju.aum.model.dto.DocumentDto;
import com.bsanju.aum.model.entity.UniversityDocument;
import com.bsanju.aum.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for indexing university documents into the vector store.
 * Handles document upload, processing, and vector embedding.
 */
@Service
public class DocumentIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexingService.class);

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DocumentIndexingService(VectorStore vectorStore,
                                   DocumentRepository documentRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Index a single document into the vector store.
     *
     * @param documentDto The document to index
     * @return The saved document entity
     */
    @Transactional
    public UniversityDocument indexDocument(DocumentDto documentDto) {
        logger.info("Indexing document: {}", documentDto.title());

        try {
            // Calculate checksum for duplicate detection
            String checksum = calculateChecksum(documentDto.content());

            // Check for duplicates
            Optional<UniversityDocument> existing = documentRepository.findByChecksum(checksum);
            if (existing.isPresent()) {
                logger.warn("Document with same content already exists: {}", documentDto.title());
                return existing.get();
            }

            // Create and save entity
            UniversityDocument doc = new UniversityDocument(
                    documentDto.title(),
                    documentDto.content(),
                    documentDto.category()
            );
            doc.setSource(documentDto.source());
            doc.setMetadata(documentDto.metadata());
            doc.setChecksum(checksum);

            doc = documentRepository.save(doc);

            // Create vector document
            Document vectorDoc = new Document(
                    doc.getId().toString(),
                    doc.getContent(),
                    Map.of(
                            "title", doc.getTitle(),
                            "category", Optional.ofNullable(doc.getCategory()).orElse("GENERAL"),
                            "source", Optional.ofNullable(doc.getSource()).orElse("unknown"),
                            "documentId", doc.getId().toString()
                    )
            );

            // Add to vector store
            vectorStore.add(List.of(vectorDoc));

            // Update indexed flag
            doc.setVectorId(doc.getId().toString());
            doc.setIndexed(true);
            doc = documentRepository.save(doc);

            logger.info("Successfully indexed document: {} (ID: {})", doc.getTitle(), doc.getId());

            // Publish event
            eventPublisher.publishEvent(new DocumentUpdatedEvent(this, doc.getId()));

            return doc;

        } catch (Exception e) {
            logger.error("Error indexing document: {}", documentDto.title(), e);
            throw new RuntimeException("Failed to index document", e);
        }
    }

    /**
     * Index multiple documents in batch.
     *
     * @param documents List of documents to index
     * @return Number of documents successfully indexed
     */
    @Transactional
    public int indexDocuments(List<DocumentDto> documents) {
        logger.info("Batch indexing {} documents", documents.size());

        int successCount = 0;
        for (DocumentDto doc : documents) {
            try {
                indexDocument(doc);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to index document: {}", doc.title(), e);
            }
        }

        logger.info("Successfully indexed {}/{} documents", successCount, documents.size());
        return successCount;
    }

    /**
     * Re-index all documents (for updates or rebuilding index).
     */
    @Transactional
    public void reindexAllDocuments() {
        logger.info("Starting full reindex of all documents");

        try {
            List<UniversityDocument> allDocs = documentRepository.findAll();
            logger.info("Found {} documents to reindex", allDocs.size());

            int reindexed = 0;
            for (UniversityDocument doc : allDocs) {
                try {
                    // Create vector document
                    Document vectorDoc = new Document(
                            doc.getId().toString(),
                            doc.getContent(),
                            Map.of(
                                    "title", doc.getTitle(),
                                    "category", Optional.ofNullable(doc.getCategory()).orElse("GENERAL"),
                                    "source", Optional.ofNullable(doc.getSource()).orElse("unknown"),
                                    "documentId", doc.getId().toString()
                            )
                    );

                    // Add to vector store
                    vectorStore.add(List.of(vectorDoc));

                    // Update entity
                    doc.setVectorId(doc.getId().toString());
                    doc.setIndexed(true);
                    doc.setUpdatedAt(LocalDateTime.now());
                    documentRepository.save(doc);

                    reindexed++;

                } catch (Exception e) {
                    logger.error("Error reindexing document: {}", doc.getTitle(), e);
                }
            }

            logger.info("Reindexing complete. {}/{} documents reindexed", reindexed, allDocs.size());

        } catch (Exception e) {
            logger.error("Error during full reindex", e);
            throw new RuntimeException("Failed to reindex documents", e);
        }
    }

    /**
     * Delete a document from both database and vector store.
     *
     * @param documentId The ID of the document to delete
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        logger.info("Deleting document with ID: {}", documentId);

        try {
            Optional<UniversityDocument> docOpt = documentRepository.findById(documentId);
            if (docOpt.isEmpty()) {
                logger.warn("Document not found: {}", documentId);
                return;
            }

            UniversityDocument doc = docOpt.get();

            // Remove from vector store if indexed
            if (doc.getVectorId() != null) {
                vectorStore.delete(List.of(doc.getVectorId()));
            }

            // Remove from database
            documentRepository.delete(doc);

            logger.info("Successfully deleted document: {}", documentId);

        } catch (Exception e) {
            logger.error("Error deleting document: {}", documentId, e);
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    /**
     * Get indexing statistics.
     */
    public Map<String, Object> getIndexingStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long total = documentRepository.count();
            long indexed = documentRepository.countByIndexedTrue();
            long notIndexed = documentRepository.countByIndexedFalse();

            stats.put("totalDocuments", total);
            stats.put("indexedDocuments", indexed);
            stats.put("notIndexedDocuments", notIndexed);
            stats.put("indexingRate", total > 0 ? (indexed * 100.0 / total) : 0.0);
            stats.put("lastUpdate", documentRepository.getLatestUpdateTimestamp());

            // Category breakdown
            List<Object[]> categoryStats = documentRepository.countByCategory();
            Map<String, Long> categoryMap = new HashMap<>();
            for (Object[] row : categoryStats) {
                categoryMap.put((String) row[0], (Long) row[1]);
            }
            stats.put("documentsByCategory", categoryMap);

        } catch (Exception e) {
            logger.error("Error getting indexing stats", e);
        }

        return stats;
    }

    /**
     * Calculate SHA-256 checksum for duplicate detection.
     */
    private String calculateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error calculating checksum", e);
            return UUID.randomUUID().toString();
        }
    }
}
