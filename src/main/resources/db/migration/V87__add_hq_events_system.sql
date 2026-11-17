-- V87: Add HQ events system for Younger Industries HQ
-- Events are time-limited occurrences that affect corporations in the HQ

CREATE TABLE IF NOT EXISTS hq_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hq_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(50),
    participating_corps TEXT,
    rewards_json TEXT,
    triggered_by_corp BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_he_hq ON hq_events(hq_id);
CREATE INDEX IF NOT EXISTS idx_he_type ON hq_events(event_type);
CREATE INDEX IF NOT EXISTS idx_he_status ON hq_events(status);
CREATE INDEX IF NOT EXISTS idx_he_start ON hq_events(start_time);
CREATE INDEX IF NOT EXISTS idx_he_end ON hq_events(end_time);
