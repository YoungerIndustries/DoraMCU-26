-- V97: Create collaboration tables for AI admin incident collaboration

CREATE TABLE ai_admin_incident_collaborators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    ai_admin_id BIGINT NOT NULL,
    role VARCHAR(15) NOT NULL,
    status VARCHAR(10) NOT NULL,
    notes VARCHAR(2000),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    UNIQUE KEY uk_aaic_incident_admin (incident_id, ai_admin_id),
    INDEX idx_aaic_incident (incident_id),
    INDEX idx_aaic_admin (ai_admin_id),
    INDEX idx_aaic_status (status)
);

CREATE TABLE ai_admin_collaboration_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    ai_admin_id BIGINT NOT NULL,
    note VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aacn_incident (incident_id),
    INDEX idx_aacn_created (created_at)
);
