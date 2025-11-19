package com.bsanju.aum.service;

import com.bsanju.aum.model.dto.DocumentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for loading Auburn University at Montgomery (AUM) data into the knowledge base.
 * Orchestrates web scraping, document chunking, and indexing.
 */
@Service
public class AumDataLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(AumDataLoaderService.class);

    private final WebScraperService webScraperService;
    private final DocumentChunkingService chunkingService;
    private final DocumentIndexingService indexingService;

    // AUM URLs to scrape
    private static final List<String> AUM_URLS = List.of(
            "https://www.aum.edu/",
            "https://www.aum.edu/academics/catalogs/",
            "https://digitalarchives.aum.edu/catalogs",
            "https://www.aum.edu/admissions/",
            "https://www.aum.edu/directory/"
    );

    public AumDataLoaderService(
            WebScraperService webScraperService,
            DocumentChunkingService chunkingService,
            DocumentIndexingService indexingService) {
        this.webScraperService = webScraperService;
        this.chunkingService = chunkingService;
        this.indexingService = indexingService;
    }

    /**
     * Load AUM data from all configured URLs.
     *
     * @return Statistics about the loading operation
     */
    public LoadingStats loadAumData() {
        logger.info("Starting AUM data loading from {} URLs", AUM_URLS.size());

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Scrape all URLs
            logger.info("Step 1/3: Scraping {} URLs...", AUM_URLS.size());
            List<WebScraperService.ScrapedContent> scrapedContent = webScraperService.scrapeUrls(AUM_URLS);

            if (scrapedContent.isEmpty()) {
                logger.warn("No content was scraped from URLs");
                return new LoadingStats(0, 0, 0, 0, System.currentTimeMillis() - startTime);
            }

            // Step 2: Chunk documents
            logger.info("Step 2/3: Chunking {} documents...", scrapedContent.size());
            List<DocumentChunkingService.DocumentToChunk> docsToChunk = scrapedContent.stream()
                    .map(sc -> new DocumentChunkingService.DocumentToChunk(
                            sc.title(),
                            sc.content(),
                            sc.category(),
                            sc.url()
                    ))
                    .toList();

            List<DocumentChunkingService.DocumentChunk> chunks = chunkingService.chunkDocuments(docsToChunk);

            // Step 3: Index chunks
            logger.info("Step 3/3: Indexing {} chunks...", chunks.size());
            List<DocumentDto> documentsToIndex = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunkingService.DocumentChunk chunk = chunks.get(i);
                WebScraperService.ScrapedContent original = scrapedContent.get(
                        Math.min(i / Math.max(1, chunks.size() / scrapedContent.size()), scrapedContent.size() - 1)
                );

                DocumentDto dto = DocumentDto.builder()
                        .title(chunk.getChunkTitle())
                        .content(chunk.content())
                        .category(original.category())
                        .source(original.url())
                        .metadata(buildMetadata(chunk, original))
                        .build();

                documentsToIndex.add(dto);
            }

            int indexed = indexingService.indexDocuments(documentsToIndex);

            long duration = System.currentTimeMillis() - startTime;

            logger.info("AUM data loading completed: {} URLs, {} chunks, {} indexed in {}ms",
                    scrapedContent.size(), chunks.size(), indexed, duration);

            return new LoadingStats(
                    AUM_URLS.size(),
                    scrapedContent.size(),
                    chunks.size(),
                    indexed,
                    duration
            );

        } catch (Exception e) {
            logger.error("Error loading AUM data", e);
            throw new RuntimeException("Failed to load AUM data", e);
        }
    }

    /**
     * Load data from custom URLs.
     *
     * @param urls List of URLs to scrape and index
     * @return Loading statistics
     */
    public LoadingStats loadFromUrls(List<String> urls) {
        logger.info("Loading data from {} custom URLs", urls.size());

        long startTime = System.currentTimeMillis();

        try {
            // Scrape URLs
            List<WebScraperService.ScrapedContent> scrapedContent = webScraperService.scrapeUrls(urls);

            if (scrapedContent.isEmpty()) {
                return new LoadingStats(urls.size(), 0, 0, 0, System.currentTimeMillis() - startTime);
            }

            // Chunk documents
            List<DocumentChunkingService.DocumentToChunk> docsToChunk = scrapedContent.stream()
                    .map(sc -> new DocumentChunkingService.DocumentToChunk(
                            sc.title(),
                            sc.content(),
                            sc.category(),
                            sc.url()
                    ))
                    .toList();

            List<DocumentChunkingService.DocumentChunk> chunks = chunkingService.chunkDocuments(docsToChunk);

            // Index chunks
            List<DocumentDto> documentsToIndex = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunkingService.DocumentChunk chunk = chunks.get(i);
                WebScraperService.ScrapedContent original = findOriginalContent(chunk, scrapedContent);

                DocumentDto dto = DocumentDto.builder()
                        .title(chunk.getChunkTitle())
                        .content(chunk.content())
                        .category(original.category())
                        .source(original.url())
                        .metadata(buildMetadata(chunk, original))
                        .build();

                documentsToIndex.add(dto);
            }

            int indexed = indexingService.indexDocuments(documentsToIndex);

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Custom URL loading completed: {} URLs, {} chunks, {} indexed in {}ms",
                    scrapedContent.size(), chunks.size(), indexed, duration);

            return new LoadingStats(
                    urls.size(),
                    scrapedContent.size(),
                    chunks.size(),
                    indexed,
                    duration
            );

        } catch (Exception e) {
            logger.error("Error loading from custom URLs", e);
            throw new RuntimeException("Failed to load from custom URLs", e);
        }
    }

    /**
     * Find the original scraped content for a chunk.
     */
    private WebScraperService.ScrapedContent findOriginalContent(
            DocumentChunkingService.DocumentChunk chunk,
            List<WebScraperService.ScrapedContent> scrapedContent) {

        return scrapedContent.stream()
                .filter(sc -> chunk.sourceTitle().startsWith(sc.title()))
                .findFirst()
                .orElse(scrapedContent.get(0));
    }

    /**
     * Build metadata JSON for a chunk.
     */
    private String buildMetadata(
            DocumentChunkingService.DocumentChunk chunk,
            WebScraperService.ScrapedContent original) {

        return String.format(
                "{\"chunkIndex\":%d,\"totalChunks\":%d,\"originalTitle\":\"%s\",\"url\":\"%s\"}",
                chunk.chunkIndex(),
                chunk.totalChunks(),
                original.title().replace("\"", "\\\""),
                original.url()
        );
    }

    /**
     * Statistics about data loading operation.
     */
    public record LoadingStats(
            int urlsRequested,
            int urlsScraped,
            int chunksCreated,
            int documentsIndexed,
            long durationMs
    ) {
        public double getSuccessRate() {
            return urlsRequested > 0 ? (urlsScraped * 100.0 / urlsRequested) : 0.0;
        }

        public double getIndexingRate() {
            return chunksCreated > 0 ? (documentsIndexed * 100.0 / chunksCreated) : 0.0;
        }
    }
}
