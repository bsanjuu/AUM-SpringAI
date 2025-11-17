package com.bsanju.aum.service;

import com.bsanju.aum.model.entity.UniversityDocument;
import com.bsanju.aum.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving relevant documents from the knowledge base.
 * Uses vector similarity search for RAG-based query responses.
 */
@Service
public class KnowledgeRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    public KnowledgeRetrievalService(VectorStore vectorStore, 
                                    DocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    /**
     * Retrieve relevant documents for a query using vector similarity search.
     *
     * @param query The user query
     * @param topK Number of documents to retrieve
     * @return List of relevant documents
     */
    public List<Document> retrieveRelevantDocuments(String query, int topK) {
        logger.debug("Retrieving relevant documents for query: {}, topK: {}", query, topK);

        try {
            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(topK)
                    .withSimilarityThreshold(0.5);

            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            
            logger.info("Retrieved {} documents for query", documents.size());
            return documents;

        } catch (Exception e) {
            logger.error("Error retrieving documents from vector store", e);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieve documents by category.
     *
     * @param category The category to filter by
     * @param limit Maximum number of documents
     * @return List of documents in the category
     */
    public List<UniversityDocument> retrieveByCategory(String category, int limit) {
        logger.debug("Retrieving documents by category: {}, limit: {}", category, limit);

        try {
            List<UniversityDocument> documents = documentRepository
                    .findByCategoryOrderByCreatedAtDesc(category);

            return documents.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error retrieving documents by category", e);
            return Collections.emptyList();
        }
    }

    /**
     * Search documents by content (fallback when vector search fails).
     *
     * @param searchTerm The term to search for
     * @return List of matching documents
     */
    public List<UniversityDocument> searchByContent(String searchTerm) {
        logger.debug("Searching documents by content: {}", searchTerm);

        try {
            return documentRepository.searchByContent(searchTerm);
        } catch (Exception e) {
            logger.error("Error searching documents by content", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get total indexed document count.
     */
    public long getIndexedDocumentCount() {
        try {
            return documentRepository.countByIndexedTrue();
        } catch (Exception e) {
            logger.error("Error counting indexed documents", e);
            return 0;
        }
    }

    /**
     * Check if knowledge base is ready (has indexed documents).
     */
    public boolean isKnowledgeBaseReady() {
        return getIndexedDocumentCount() > 0;
    }
}
