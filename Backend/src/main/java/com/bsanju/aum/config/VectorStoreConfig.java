package com.bsanju.aum.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);

        // Load existing vector store if it exists
        File vectorStoreFile = new File("vector-store.json");
        if (vectorStoreFile.exists()) {
            try {
                vectorStore.load(new FileSystemResource(vectorStoreFile));
            } catch (Exception e) {
                // Log error but continue with empty vector store
                System.err.println("Failed to load existing vector store: " + e.getMessage());
            }
        }

        return vectorStore;
    }
}