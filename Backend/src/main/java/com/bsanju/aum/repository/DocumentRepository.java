package com.bsanju.aum.repository;

import com.bsanju.aum.model.entity.UniversityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UniversityDocument entity.
 * Provides data access methods for document management and indexing.
 */
@Repository
public interface DocumentRepository extends JpaRepository<UniversityDocument, Long> {

    /**
     * Find all documents by category.
     */
    List<UniversityDocument> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Find all indexed documents.
     */
    List<UniversityDocument> findByIndexedTrueOrderByUpdatedAtDesc();

    /**
     * Find all non-indexed documents.
     */
    List<UniversityDocument> findByIndexedFalseOrderByCreatedAtDesc();

    /**
     * Find document by title.
     */
    Optional<UniversityDocument> findByTitle(String title);

    /**
     * Find documents by title containing (case-insensitive).
     */
    List<UniversityDocument> findByTitleContainingIgnoreCase(String titlePart);

    /**
     * Find document by checksum (for duplicate detection).
     */
    Optional<UniversityDocument> findByChecksum(String checksum);

    /**
     * Find document by vector ID.
     */
    Optional<UniversityDocument> findByVectorId(String vectorId);

    /**
     * Find documents updated after a certain date.
     */
    @Query("SELECT d FROM UniversityDocument d WHERE d.updatedAt > :since ORDER BY d.updatedAt DESC")
    List<UniversityDocument> findUpdatedSince(@Param("since") LocalDateTime since);

    /**
     * Count documents by category.
     */
    @Query("SELECT d.category, COUNT(d) FROM UniversityDocument d GROUP BY d.category")
    List<Object[]> countByCategory();

    /**
     * Count indexed documents.
     */
    Long countByIndexedTrue();

    /**
     * Count non-indexed documents.
     */
    Long countByIndexedFalse();

    /**
     * Find documents by source.
     */
    List<UniversityDocument> findBySourceOrderByCreatedAtDesc(String source);

    /**
     * Search documents by content (full-text search - basic).
     */
    @Query("SELECT d FROM UniversityDocument d WHERE LOWER(d.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UniversityDocument> searchByContent(@Param("searchTerm") String searchTerm);

    /**
     * Find documents that need re-indexing (updated but indexed flag is true).
     */
    @Query("SELECT d FROM UniversityDocument d WHERE d.indexed = true AND d.updatedAt > :since")
    List<UniversityDocument> findDocumentsNeedingReindex(@Param("since") LocalDateTime since);

    /**
     * Get latest document update timestamp.
     */
    @Query("SELECT MAX(d.updatedAt) FROM UniversityDocument d")
    LocalDateTime getLatestUpdateTimestamp();
}
