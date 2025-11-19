package com.bsanju.aum.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for scraping and extracting content from web pages.
 * Uses Jsoup for HTML parsing and content extraction.
 */
@Service
public class WebScraperService {

    private static final Logger logger = LoggerFactory.getLogger(WebScraperService.class);

    private static final int TIMEOUT_MS = 30000; // 30 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; AUM-AI-Bot/1.0)";

    /**
     * Scrape content from a URL and extract clean text.
     *
     * @param url The URL to scrape
     * @return Extracted content
     */
    public ScrapedContent scrapeUrl(String url) {
        logger.info("Scraping URL: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            String title = extractTitle(doc);
            String content = extractContent(doc);
            String category = categorizeContent(url, title);

            logger.info("Successfully scraped: {} (length: {} chars)", title, content.length());

            return new ScrapedContent(url, title, content, category);

        } catch (IOException e) {
            logger.error("Failed to scrape URL: {}", url, e);
            throw new RuntimeException("Failed to scrape URL: " + url, e);
        }
    }

    /**
     * Scrape multiple URLs in sequence.
     *
     * @param urls List of URLs to scrape
     * @return List of scraped content
     */
    public List<ScrapedContent> scrapeUrls(List<String> urls) {
        logger.info("Scraping {} URLs", urls.size());

        List<ScrapedContent> results = new ArrayList<>();
        int success = 0;
        int failed = 0;

        for (String url : urls) {
            try {
                ScrapedContent content = scrapeUrl(url);
                results.add(content);
                success++;

                // Be polite: small delay between requests
                Thread.sleep(1000);

            } catch (Exception e) {
                logger.error("Failed to scrape URL: {}", url, e);
                failed++;
            }
        }

        logger.info("Scraping complete: {} successful, {} failed", success, failed);
        return results;
    }

    /**
     * Extract title from HTML document.
     */
    private String extractTitle(Document doc) {
        // Try multiple title sources
        String title = doc.title();

        if (title == null || title.isBlank()) {
            Element h1 = doc.selectFirst("h1");
            if (h1 != null) {
                title = h1.text();
            }
        }

        if (title == null || title.isBlank()) {
            title = "Untitled Document";
        }

        return cleanText(title);
    }

    /**
     * Extract main content from HTML document.
     * Removes navigation, headers, footers, scripts, and styling.
     */
    private String extractContent(Document doc) {
        // Remove unwanted elements
        doc.select("nav, header, footer, script, style, .navigation, .menu, #menu, #nav").remove();
        doc.select("[role=navigation], [role=banner], [role=contentinfo]").remove();

        // Try to find main content area
        Element mainContent = doc.selectFirst("main, article, .content, #content, .main, #main");

        if (mainContent == null) {
            mainContent = doc.body();
        }

        // Extract text from paragraphs, headings, and lists
        StringBuilder content = new StringBuilder();

        Elements elements = mainContent.select("h1, h2, h3, h4, h5, h6, p, li, td, th");
        for (Element element : elements) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                content.append(text).append("\n");
            }
        }

        String result = content.toString().trim();

        // If extraction failed, fall back to body text
        if (result.isEmpty()) {
            result = mainContent.text();
        }

        return cleanText(result);
    }

    /**
     * Clean and normalize text content.
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replaceAll("\\s+", " ")  // Normalize whitespace
                .replaceAll("[\\r\\n]+", "\n")  // Normalize line breaks
                .trim();
    }

    /**
     * Categorize content based on URL and title.
     */
    private String categorizeContent(String url, String title) {
        String urlLower = url.toLowerCase();
        String titleLower = title.toLowerCase();

        if (urlLower.contains("admission") || titleLower.contains("admission")) {
            return "ADMISSIONS";
        } else if (urlLower.contains("catalog") || titleLower.contains("catalog") ||
                   urlLower.contains("course") || titleLower.contains("course")) {
            return "COURSES";
        } else if (urlLower.contains("tuition") || urlLower.contains("fee") ||
                   titleLower.contains("tuition") || titleLower.contains("fee")) {
            return "TUITION";
        } else if (urlLower.contains("deadline") || titleLower.contains("deadline") ||
                   urlLower.contains("calendar") || titleLower.contains("calendar")) {
            return "DEADLINES";
        } else if (urlLower.contains("policy") || urlLower.contains("policies") ||
                   titleLower.contains("policy") || titleLower.contains("policies")) {
            return "POLICIES";
        } else if (urlLower.contains("directory") || titleLower.contains("directory") ||
                   urlLower.contains("contact") || titleLower.contains("contact")) {
            return "GENERAL";
        } else if (urlLower.contains("academic") || titleLower.contains("academic")) {
            return "COURSES";
        }

        return "GENERAL";
    }

    /**
     * Data class for scraped content.
     */
    public record ScrapedContent(
            String url,
            String title,
            String content,
            String category
    ) {}
}
