-- V1__Initial_schema.sql
-- Initial database schema for AUM-SpringAI University FAQ System
-- Creates the university_documents table for knowledge base management

CREATE TABLE IF NOT EXISTS university_documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50),
    source VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    indexed BOOLEAN NOT NULL DEFAULT FALSE,
    vector_id VARCHAR(100),
    checksum VARCHAR(64),
    
    CONSTRAINT uk_title UNIQUE (title),
    CONSTRAINT uk_vector_id UNIQUE (vector_id),
    CONSTRAINT uk_checksum UNIQUE (checksum)
);

-- Indexes for performance
CREATE INDEX idx_documents_category ON university_documents(category);
CREATE INDEX idx_documents_indexed ON university_documents(indexed);
CREATE INDEX idx_documents_created_at ON university_documents(created_at);
CREATE INDEX idx_documents_updated_at ON university_documents(updated_at);

-- Comments for documentation
COMMENT ON TABLE university_documents IS 'Stores university documents used for RAG-based query responses';
COMMENT ON COLUMN university_documents.title IS 'Document title';
COMMENT ON COLUMN university_documents.content IS 'Full text content of the document';
COMMENT ON COLUMN university_documents.category IS 'Category: TUITION, COURSES, DEADLINES, POLICIES, TECHNICAL, etc.';
COMMENT ON COLUMN university_documents.vector_id IS 'ID in the vector store for retrieval';
COMMENT ON COLUMN university_documents.checksum IS 'SHA-256 checksum for duplicate detection';
