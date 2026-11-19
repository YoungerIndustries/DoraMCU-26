-- V99: Add AI admin scheduling tables
-- Stores scheduled shifts for AI admins with galaxy assignments and time windows.

CREATE TABLE ai_admin_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_admin_id BIGINT NOT NULL,
    shift_type VARCHAR(15) NOT NULL,
    galaxy_name VARCHAR(100),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    day_of_week VARCHAR(10),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_aas_admin ON ai_admin_schedules(ai_admin_id);
CREATE INDEX idx_aas_galaxy ON ai_admin_schedules(galaxy_name);
CREATE INDEX idx_aas_active ON ai_admin_schedules(is_active);
CREATE INDEX idx_aas_type ON ai_admin_schedules(shift_type);
