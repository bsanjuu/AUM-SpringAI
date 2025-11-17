-- V3__Add_feedback_table.sql
-- Adds user_feedback table for collecting user feedback on AI responses

CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    rating INTEGER,
    comment VARCHAR(1000),
    helpful BOOLEAN,
    feedback_type VARCHAR(50),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_ip VARCHAR(45),
    resolved BOOLEAN DEFAULT FALSE,
    admin_notes VARCHAR(2000),
    
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);

-- Indexes for performance
CREATE INDEX idx_feedback_session_id ON user_feedback(session_id);
CREATE INDEX idx_feedback_timestamp ON user_feedback(timestamp);
CREATE INDEX idx_feedback_rating ON user_feedback(rating);
CREATE INDEX idx_feedback_helpful ON user_feedback(helpful);
CREATE INDEX idx_feedback_resolved ON user_feedback(resolved);

-- Comments for documentation
COMMENT ON TABLE user_feedback IS 'Stores user feedback on AI responses for quality improvement';
COMMENT ON COLUMN user_feedback.session_id IS 'References the chat session this feedback is for';
COMMENT ON COLUMN user_feedback.rating IS 'User rating from 1 (poor) to 5 (excellent)';
COMMENT ON COLUMN user_feedback.helpful IS 'Boolean flag indicating if response was helpful';
COMMENT ON COLUMN user_feedback.resolved IS 'Whether admin has addressed this feedback';
