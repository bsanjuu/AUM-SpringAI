-- V4__Add_metrics_table.sql
-- Adds query_metrics table for analytics and monitoring

CREATE TABLE IF NOT EXISTS query_metrics (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    query_text VARCHAR(2000) NOT NULL,
    category VARCHAR(50),
    confidence DOUBLE PRECISION DEFAULT 0.0,
    response_time_ms BIGINT DEFAULT 0,
    needs_human_assistance BOOLEAN DEFAULT FALSE,
    documents_retrieved INTEGER DEFAULT 0,
    model_used VARCHAR(50),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_ip VARCHAR(45),
    success BOOLEAN DEFAULT TRUE,
    error_message VARCHAR(1000)
);

-- Indexes for performance
CREATE INDEX idx_metrics_session_id ON query_metrics(session_id);
CREATE INDEX idx_metrics_timestamp ON query_metrics(timestamp);
CREATE INDEX idx_metrics_category ON query_metrics(category);
CREATE INDEX idx_metrics_confidence ON query_metrics(confidence);
CREATE INDEX idx_metrics_human_assistance ON query_metrics(needs_human_assistance);
CREATE INDEX idx_metrics_success ON query_metrics(success);
CREATE INDEX idx_metrics_model_used ON query_metrics(model_used);

-- Comments for documentation
COMMENT ON TABLE query_metrics IS 'Stores detailed metrics for analytics and performance monitoring';
COMMENT ON COLUMN query_metrics.query_text IS 'Original user query (truncated to 2000 chars)';
COMMENT ON COLUMN query_metrics.documents_retrieved IS 'Number of documents retrieved from vector store';
COMMENT ON COLUMN query_metrics.model_used IS 'AI model used (e.g., gpt-3.5-turbo, llama2)';
COMMENT ON COLUMN query_metrics.success IS 'Whether the query was processed successfully';
