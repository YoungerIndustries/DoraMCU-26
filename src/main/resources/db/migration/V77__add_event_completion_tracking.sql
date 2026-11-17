-- V77: Add event completion tracking and AI assistant enhancements

-- Add completed_at column to hq_events for tracking manual event completion
ALTER TABLE hq_events ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
