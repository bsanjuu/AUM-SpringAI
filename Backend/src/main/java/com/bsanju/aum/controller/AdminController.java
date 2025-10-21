// controller/AdminController.java
package com.bsanju.aum.controller;

import com.bsanju.aum.model.dto.DocumentDto;
import com.bsanju.aum.model.dto.MetricsDto;
import com.bsanju.aum.model.entity.UniversityDocument;
import com.bsanju.aum.repository.DocumentRepository;
import com.bsanju.aum.service.DocumentIndexingService;
import com.bsanju.aum.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin API", description = "Administrative endpoints for managing the FAQ system")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final DocumentRepository documentRepository;
    private final DocumentIndexingService documentIndexingService;
    private final MetricsService metricsService;

    public AdminController(
            DocumentRepository documentRepository,
            DocumentIndexingService documentIndexingService,
            MetricsService metricsService) {
        this.documentRepository = documentRepository;
        this.documentIndexingService = documentIndexingService;
        this.metricsService = metricsService;
    }

    @GetMapping("/documents")
    @Operation(summary = "Get all documents", description = "Retrieve paginated list of university documents")
    public ResponseEntity<List<DocumentDto>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {

        List<UniversityDocument> documents;

        if (category != null) {
            try {
                UniversityDocument.DocumentCategory cat = UniversityDocument.DocumentCategory.valueOf(category.toUpperCase());
                documents = documentRepository.findByCategoryAndActiveTrue(cat);
            } catch (IllegalArgumentException e) {
                documents = documentRepository.findByActiveTrue();
            }
        } else {
            documents = documentRepository.findByActiveTrue();
        }

        List<DocumentDto> documentDtos = documents.stream()
                .map(DocumentDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDtos);
    }

    @GetMapping("/documents/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> ResponseEntity.ok(DocumentDto.fromEntity(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/documents")
    @Operation(summary = "Create new document")
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody DocumentDto documentDto) {
        UniversityDocument document = documentDto.toEntity();
        document.setId(null); // Ensure it's a new document

        UniversityDocument saved = documentRepository.save(document);
        documentIndexingService.indexDocument(saved);

        logger.info("Created new document: {}", saved.getTitle());
        return ResponseEntity.ok(DocumentDto.fromEntity(saved));
    }

    @PutMapping("/documents/{id}")
    @Operation(summary = "Update existing document")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDto documentDto) {

        return documentRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(documentDto.title());
                    existing.setContent(documentDto.content());
                    existing.setCategory(documentDto.category() != null ?
                            UniversityDocument.DocumentCategory.valueOf(documentDto.category()) : null);
                    existing.setSource(documentDto.source());
                    existing.setVersion(documentDto.version());
                    existing.setTags(documentDto.tags());
                    existing.setActive(documentDto.active());

                    UniversityDocument saved = documentRepository.save(existing);
                    documentIndexingService.indexDocument(saved);

                    logger.info("Updated document: {}", saved.getTitle());
                    return ResponseEntity.ok(DocumentDto.fromEntity(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "Delete document")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(document -> {
                    document.setActive(false); // Soft delete
                    documentRepository.save(document);
                    logger.info("Deleted document: {}", document.getTitle());
                    return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/documents/reindex")
    @Operation(summary = "Reindex all documents")
    public ResponseEntity<Map<String, String>> reindexDocuments() {
        try {
            documentIndexingService.reindexAllDocuments();
            return ResponseEntity.ok(Map.of("message", "Reindexing started successfully"));
        } catch (Exception e) {
            logger.error("Failed to start reindexing", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to start reindexing"));
        }
    }

    @GetMapping("/metrics/daily")
    @Operation(summary = "Get daily metrics")
    public ResponseEntity<MetricsDto> getDailyMetrics(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        MetricsDto metrics = metricsService.getDailyMetrics(targetDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/weekly")
    @Operation(summary = "Get weekly metrics")
    public ResponseEntity<MetricsDto> getWeeklyMetrics(
            @RequestParam(required = false) String startDate) {

        LocalDate targetDate = startDate != null ? LocalDate.parse(startDate) :
                LocalDate.now().minusDays(7);
        MetricsDto metrics = metricsService.getWeeklyMetrics(targetDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/monthly")
    @Operation(summary = "Get monthly metrics")
    public ResponseEntity<MetricsDto> getMonthlyMetrics(
            @RequestParam(required = false) String startDate) {

        LocalDate targetDate = startDate != null ? LocalDate.parse(startDate) :
                LocalDate.now().minusMonths(1);
        MetricsDto metrics = metricsService.getMonthlyMetrics(targetDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        long totalDocuments = documentRepository.countActiveDocuments();

        Map<String, Object> stats = Map.of(
                "totalDocuments", totalDocuments,
                "documentsByCategory", documentRepository.getCategoryDistribution(),
                "systemStatus", "healthy",
                "lastUpdated", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(stats);
    }
}