-- V75: Add shared character travel log table for cross-system travel tracking
-- This table logs when characters travel between game systems via portals

CREATE TABLE IF NOT EXISTS shared_character_travel_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    from_system VARCHAR(20) NOT NULL,
    to_system VARCHAR(20) NOT NULL,
    travel_notes TEXT,
    traveled_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_travel_log_character ON shared_character_travel_log(character_id);
CREATE INDEX IF NOT EXISTS idx_travel_log_from ON shared_character_travel_log(from_system);
CREATE INDEX IF NOT EXISTS idx_travel_log_to ON shared_character_travel_log(to_system);
