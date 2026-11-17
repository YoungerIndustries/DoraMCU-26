-- V71: Add corporate meetings table for the Younger Industries HQ meeting system
-- Allows corporations to request, accept, decline, complete, and cancel business meetings

CREATE TABLE IF NOT EXISTS corporate_meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_corporation_id BIGINT NOT NULL,
    target_corporation_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_time TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    meeting_notes VARCHAR(4000)
);

CREATE INDEX IF NOT EXISTS idx_cm_requester ON corporate_meetings (requester_corporation_id);
CREATE INDEX IF NOT EXISTS idx_cm_target ON corporate_meetings (target_corporation_id);
CREATE INDEX IF NOT EXISTS idx_cm_status ON corporate_meetings (status);
