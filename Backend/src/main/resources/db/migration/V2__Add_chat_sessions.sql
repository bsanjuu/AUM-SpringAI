-- V2__Add_chat_sessions.sql
-- Adds chat_sessions table for storing conversation history

CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    user_message TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    category VARCHAR(50),
    confidence DOUBLE PRECISION DEFAULT 0.0,
    needs_human_assistance BOOLEAN DEFAULT FALSE,
    response_time_ms BIGINT DEFAULT 0,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_ip VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Indexes for performance
CREATE INDEX idx_chat_sessions_session_id ON chat_sessions(session_id);
CREATE INDEX idx_chat_sessions_timestamp ON chat_sessions(timestamp);
CREATE INDEX idx_chat_sessions_category ON chat_sessions(category);
CREATE INDEX idx_chat_sessions_confidence ON chat_sessions(confidence);
CREATE INDEX idx_chat_sessions_human_assistance ON chat_sessions(needs_human_assistance);

-- Comments for documentation
COMMENT ON TABLE chat_sessions IS 'Stores chat conversation history and AI responses';
COMMENT ON COLUMN chat_sessions.session_id IS 'Unique session identifier for grouping related queries';
COMMENT ON COLUMN chat_sessions.confidence IS 'Confidence score of the AI response (0.0 to 1.0)';
COMMENT ON COLUMN chat_sessions.needs_human_assistance IS 'Flag indicating if query needs human intervention';
COMMENT ON COLUMN chat_sessions.response_time_ms IS 'Response time in milliseconds';
