-- V102: Add specialization column to ai_characters for AI admin role specialization
-- AI admins can specialize in HEALTH, PERFORMANCE, SECURITY, or SYSTEM incident types
-- The dispatch service prefers specialized admins when routing matching incident types

ALTER TABLE ai_characters
    ADD COLUMN specialization VARCHAR(20) NULL;

CREATE INDEX idx_ac_specialization ON ai_characters (specialization);
