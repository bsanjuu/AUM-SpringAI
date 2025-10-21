// controller/HealthController.java
package com.bsanju.aum.controller;

import com.bsanju.aum.service.KnowledgeRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health Check API", description = "System health check endpoints")
public class HealthController {

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public HealthController(KnowledgeRetrievalService knowledgeRetrievalService) {
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    @GetMapping
    @Operation(summary = "Basic health check")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "University FAQ Backend"
        );
        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed")
    @Operation(summary = "Detailed health check")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        boolean vectorStoreHealthy = knowledgeRetrievalService.isVectorStoreHealthy();

        Map<String, Object> health = Map.of(
                "status", vectorStoreHealthy ? "UP" : "DEGRADED",
                "timestamp", LocalDateTime.now(),
                "service", "University FAQ Backend",
                "components", Map.of(
                        "database", "UP", // We assume DB is up if we can respond
                        "vectorStore", vectorStoreHealthy ? "UP" : "DOWN",
                        "chatService", "UP"
                )
        );

        return ResponseEntity.ok(health);
    }

    @GetMapping("/readiness")
    @Operation(summary = "Readiness probe")
    public ResponseEntity<Map<String, String>> readiness() {
        // Check if all required services are ready
        boolean ready = knowledgeRetrievalService.isVectorStoreHealthy();

        if (ready) {
            return ResponseEntity.ok(Map.of("status", "READY"));
        } else {
            return ResponseEntity.status(503).body(Map.of("status", "NOT_READY"));
        }
    }

    @GetMapping("/liveness")
    @Operation(summary = "Liveness probe")
    public ResponseEntity<Map<String, String>> liveness() {
        // Simple liveness check - if we can respond, we're alive
        return ResponseEntity.ok(Map.of("status", "ALIVE"));
    }
}
