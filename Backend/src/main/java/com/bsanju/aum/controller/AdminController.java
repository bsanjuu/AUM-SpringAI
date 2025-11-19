package com.bsanju.aum.controller;

import com.bsanju.aum.model.dto.DocumentDto;
import com.bsanju.aum.service.AumDataLoaderService;
import com.bsanju.aum.service.DocumentIndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin controller for managing documents and loading university data.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final DocumentIndexingService documentIndexingService;
    private final AumDataLoaderService aumDataLoaderService;

    public AdminController(
            DocumentIndexingService documentIndexingService,
            AumDataLoaderService aumDataLoaderService) {
        this.documentIndexingService = documentIndexingService;
        this.aumDataLoaderService = aumDataLoaderService;
    }

    /**
     * Load AUM university data from official websites.
     */
    @PostMapping("/load-aum-data")
    public ResponseEntity<Map<String, Object>> loadAumData() {
        logger.info("Received request to load AUM data");

        try {
            AumDataLoaderService.LoadingStats stats = aumDataLoaderService.loadAumData();

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "AUM data loaded successfully",
                    "stats", Map.of(
                            "urlsRequested", stats.urlsRequested(),
                            "urlsScraped", stats.urlsScraped(),
                            "chunksCreated", stats.chunksCreated(),
                            "documentsIndexed", stats.documentsIndexed(),
                            "durationMs", stats.durationMs(),
                            "successRate", String.format("%.1f%%", stats.getSuccessRate()),
                            "indexingRate", String.format("%.1f%%", stats.getIndexingRate())
                    )
            );

            logger.info("AUM data loaded: {} documents indexed", stats.documentsIndexed());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to load AUM data", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to load AUM data",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Load data from custom URLs.
     */
    @PostMapping("/load-from-urls")
    public ResponseEntity<Map<String, Object>> loadFromUrls(@RequestBody List<String> urls) {
        logger.info("Received request to load data from {} custom URLs", urls.size());

        if (urls == null || urls.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "URL list cannot be empty"
                    ));
        }

        try {
            AumDataLoaderService.LoadingStats stats = aumDataLoaderService.loadFromUrls(urls);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Data loaded successfully from custom URLs",
                    "stats", Map.of(
                            "urlsRequested", stats.urlsRequested(),
                            "urlsScraped", stats.urlsScraped(),
                            "chunksCreated", stats.chunksCreated(),
                            "documentsIndexed", stats.documentsIndexed(),
                            "durationMs", stats.durationMs(),
                            "successRate", String.format("%.1f%%", stats.getSuccessRate()),
                            "indexingRate", String.format("%.1f%%", stats.getIndexingRate())
                    )
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to load data from custom URLs", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to load data from custom URLs",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Index a single document manually.
     */
    @PostMapping("/documents")
    public ResponseEntity<Map<String, Object>> indexDocument(@RequestBody DocumentDto documentDto) {
        logger.info("Received request to index document: {}", documentDto.title());

        try {
            documentIndexingService.indexDocument(documentDto);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Document indexed successfully",
                    "title", documentDto.title()
            ));

        } catch (Exception e) {
            logger.error("Failed to index document", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to index document",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Reindex all existing documents.
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindexAllDocuments() {
        logger.info("Received request to reindex all documents");

        try {
            documentIndexingService.reindexAllDocuments();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reindexing started successfully"
            ));

        } catch (Exception e) {
            logger.error("Failed to start reindexing", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to start reindexing",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Get indexing statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexingStats() {
        logger.debug("Received request for indexing stats");

        try {
            Map<String, Object> stats = documentIndexingService.getIndexingStats();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "stats", stats
            ));

        } catch (Exception e) {
            logger.error("Failed to get indexing stats", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to get indexing stats",
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Health check endpoint for admin API.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "admin-api",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
