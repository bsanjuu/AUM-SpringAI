# AUM Data Loading Guide

## Overview

This guide explains how to load Auburn University at Montgomery (AUM) data into the knowledge base so the chatbot can provide accurate, university-specific information to students.

## Architecture

The system uses a **RAG (Retrieval-Augmented Generation)** architecture:

1. **Web Scraping** - Extracts content from AUM official websites
2. **Document Chunking** - Splits large documents into optimal chunks for vector indexing
3. **Vector Indexing** - Stores document chunks with embeddings for semantic search
4. **Knowledge Retrieval** - Finds relevant documents for user queries
5. **AI Response** - Generates accurate answers based on retrieved AUM content

## Components

### 1. WebScraperService
- Fetches content from AUM websites
- Extracts clean text from HTML
- Removes navigation, headers, footers
- Categorizes content automatically

**Location**: `Backend/src/main/java/com/bsanju/aum/service/WebScraperService.java`

### 2. DocumentChunkingService
- Splits large documents into optimal chunks (1000-1500 characters)
- Maintains context with overlapping chunks
- Preserves paragraph boundaries

**Location**: `Backend/src/main/java/com/bsanju/aum/service/DocumentChunkingService.java`

### 3. AumDataLoaderService
- Orchestrates scraping, chunking, and indexing
- Handles batch operations
- Provides loading statistics

**Location**: `Backend/src/main/java/com/bsanju/aum/service/AumDataLoaderService.java`

## Pre-configured AUM URLs

The system comes pre-configured with official AUM URLs:

1. **Homepage**: https://www.aum.edu/
2. **Academic Catalogs**: https://www.aum.edu/academics/catalogs/
3. **Digital Archives**: https://digitalarchives.aum.edu/catalogs
4. **Admissions**: https://www.aum.edu/admissions/
5. **Directory**: https://www.aum.edu/directory/

## Loading AUM Data

### Method 1: Using the Admin API

**Endpoint**: `POST /api/admin/load-aum-data`

**Using curl**:
```bash
curl -X POST http://localhost:8080/api/admin/load-aum-data
```

**Response**:
```json
{
  "success": true,
  "message": "AUM data loaded successfully",
  "stats": {
    "urlsRequested": 5,
    "urlsScraped": 5,
    "chunksCreated": 25,
    "documentsIndexed": 25,
    "durationMs": 15000,
    "successRate": "100.0%",
    "indexingRate": "100.0%"
  }
}
```

### Method 2: Loading Custom URLs

**Endpoint**: `POST /api/admin/load-from-urls`

**Request Body**:
```json
[
  "https://www.aum.edu/tuition-fees",
  "https://www.aum.edu/academic-calendar",
  "https://www.aum.edu/student-services"
]
```

**Using curl**:
```bash
curl -X POST http://localhost:8080/api/admin/load-from-urls \
  -H "Content-Type: application/json" \
  -d '["https://www.aum.edu/tuition-fees", "https://www.aum.edu/academic-calendar"]'
```

## Running the Application

### Prerequisites
1. Java 25
2. OpenAI API key (for production) or Ollama (for local development)

### Development Mode (with Ollama)

```bash
cd Backend

# Set profile to dev (uses H2 in-memory database)
export SPRING_PROFILES_ACTIVE=dev

# Run application
./gradlew bootRun
```

### Production Mode (with OpenAI)

```bash
cd Backend

# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export OPENAI_API_KEY=your-api-key-here
export DATABASE_URL=jdbc:postgresql://localhost:5432/aum_faq_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your-password

# Run application
./gradlew bootRun
```

## Loading Data Workflow

### Step 1: Start the Application
```bash
cd Backend
./gradlew bootRun
```

Wait for the message: "Started BackendApplication in X seconds"

### Step 2: Load AUM Data
```bash
curl -X POST http://localhost:8080/api/admin/load-aum-data
```

**Expected Timeline**:
- Scraping 5 URLs: ~10-15 seconds
- Chunking documents: ~1-2 seconds
- Indexing chunks: ~5-10 seconds
- **Total**: ~20-30 seconds

### Step 3: Verify Data Loading

**Check Statistics**:
```bash
curl http://localhost:8080/api/admin/stats
```

**Expected Response**:
```json
{
  "success": true,
  "stats": {
    "totalDocuments": 25,
    "indexedDocuments": 25,
    "notIndexedDocuments": 0,
    "indexingRate": 100.0,
    "documentsByCategory": {
      "ADMISSIONS": 8,
      "COURSES": 10,
      "GENERAL": 7
    }
  }
}
```

### Step 4: Test the Chat

**Example Query**:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What are the admission requirements for AUM?",
    "sessionId": "test-session-123"
  }'
```

**Expected Response**:
```json
{
  "response": "Based on AUM's admission requirements...",
  "sessionId": "test-session-123",
  "category": "ADMISSIONS",
  "confidence": 0.85,
  "needsHumanAssistance": false,
  "timestamp": "2025-11-19T10:30:00"
}
```

## Troubleshooting

### Issue: Web scraping fails

**Error**: "Failed to scrape URL"

**Solutions**:
1. Check internet connectivity
2. Verify URL is accessible
3. Check if website is blocking automated requests
4. Increase timeout in WebScraperService.java

### Issue: No documents indexed

**Error**: "No content was scraped from URLs"

**Solutions**:
1. Check application logs for specific errors
2. Verify URLs are returning HTML content
3. Check if content extraction is working

### Issue: Chat returns generic responses

**Cause**: Knowledge base is empty or not retrieved

**Solutions**:
1. Verify data was loaded: `GET /api/admin/stats`
2. Check vector store file exists: `vector-store.json`
3. Reload data: `POST /api/admin/load-aum-data`

### Issue: Low confidence scores

**Cause**: Retrieved documents don't match query well

**Solutions**:
1. Load more diverse content
2. Adjust similarity threshold in KnowledgeRetrievalService.java
3. Add more specific AUM content for that topic

## Configuration

### Document Chunking
- **Target size**: 1000 characters
- **Max size**: 1500 characters
- **Min size**: 200 characters
- **Overlap**: 200 characters

Adjust in `DocumentChunkingService.java`

### Vector Search
- **Top K**: 5 documents
- **Similarity threshold**: 0.5

Adjust in `KnowledgeRetrievalService.java`

### Categories
- ADMISSIONS
- COURSES
- TUITION
- DEADLINES
- POLICIES
- GENERAL

Add more in `application.yml` → `app.categories`

## Best Practices

1. **Initial Load**: Load comprehensive content from main AUM pages
2. **Regular Updates**: Reload data when AUM website content changes
3. **Category Specific**: Add URLs for specific topics (tuition, calendar, etc.)
4. **Verify Quality**: Test chat responses after loading new data
5. **Monitor Stats**: Regularly check indexing statistics

## API Endpoints Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/load-aum-data` | POST | Load pre-configured AUM URLs |
| `/api/admin/load-from-urls` | POST | Load custom URLs |
| `/api/admin/documents` | POST | Index single document |
| `/api/admin/reindex` | POST | Reindex all documents |
| `/api/admin/stats` | GET | Get indexing statistics |
| `/api/admin/health` | GET | Health check |
| `/api/chat` | POST | Send chat message |
| `/api/chat/history/{sessionId}` | GET | Get chat history |

## Example: Complete Setup

```bash
# 1. Start application
cd Backend
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun

# 2. Wait for startup (in another terminal)
sleep 30

# 3. Load AUM data
curl -X POST http://localhost:8080/api/admin/load-aum-data

# 4. Check stats
curl http://localhost:8080/api/admin/stats

# 5. Test chat
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tell me about AUM",
    "sessionId": "demo-session"
  }'
```

## Data Persistence

### Development Mode (H2)
- Data stored in memory
- Lost on restart
- Reload data after each restart

### Production Mode (PostgreSQL + SimpleVectorStore)
- Database: PostgreSQL (persistent)
- Vector Store: `vector-store.json` file (persistent)
- Data survives restarts

## Next Steps

1. Load initial AUM data
2. Test with various student queries
3. Add more specific AUM pages as needed
4. Monitor and improve responses
5. Set up scheduled reloading for updates

## Support

For issues or questions:
1. Check application logs: `logs/aum-spring-ai.log`
2. Review CLAUDE.md for architecture details
3. Check Spring AI documentation: https://docs.spring.io/spring-ai/

---

**Last Updated**: 2025-11-19
