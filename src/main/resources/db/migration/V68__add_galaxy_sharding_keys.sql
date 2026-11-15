-- V68: Add galaxy_id sharding keys for multi-process galaxy architecture
-- This migration adds galaxy_id columns to key tables to enable database-level
-- galaxy isolation. The galaxy_id allows queries to be scoped to specific galaxies,
-- reducing cross-galaxy contention and enabling future database sharding.

-- Add galaxy_id to game_sessions table (if not exists)
ALTER TABLE game_sessions ADD COLUMN IF NOT EXISTS galaxy_id BIGINT;
ALTER TABLE game_sessions ADD INDEX IF NOT EXISTS idx_game_sessions_galaxy_id (galaxy_id);

-- Add galaxy_id to persisted_game_session table (if not exists)
ALTER TABLE persisted_game_session ADD COLUMN IF NOT EXISTS galaxy_id BIGINT;
ALTER TABLE persisted_game_session ADD INDEX IF NOT EXISTS idx_persisted_game_session_galaxy_id (galaxy_id);

-- Add galaxy_id to ai_characters table for galaxy-scoped AI queries
-- (ai_characters already has galaxy_id in some schemas, this ensures consistency)
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS galaxy_shard_id BIGINT;
ALTER TABLE ai_characters ADD INDEX IF NOT EXISTS idx_ai_characters_galaxy_shard (galaxy_shard_id);

-- Create galaxy_processor_state table for tracking per-galaxy processing state
CREATE TABLE IF NOT EXISTS galaxy_processor_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_id BIGINT NOT NULL UNIQUE,
    galaxy_name VARCHAR(255) NOT NULL,
    last_turn_processed BIGINT DEFAULT 0,
    last_health_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    health_score INT,
    active_players INT DEFAULT 0,
    ai_agents_count INT DEFAULT 0,
    event_queue_size INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_galaxy_processor_galaxy_id (galaxy_id)
);

-- Create galaxy_event_log table for distributed event history
CREATE TABLE IF NOT EXISTS galaxy_event_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_id BIGINT NOT NULL,
    galaxy_name VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_payload JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_galaxy_event_galaxy_id (galaxy_id),
    INDEX idx_galaxy_event_type (event_type),
    INDEX idx_galaxy_event_created (created_at)
);

-- Add galaxy_id to e26_galaxy table if it doesn't have an explicit shard key
-- (The galaxies table already has id, which serves as the galaxy_id)
-- This comment documents that galaxies.id is the canonical galaxy_id for sharding
