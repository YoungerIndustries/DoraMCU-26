-- V96: Create ai_admin_incidents table for AI admin incident management

CREATE TABLE ai_admin_incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_type VARCHAR(20) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    priority INT NOT NULL DEFAULT 5,
    status VARCHAR(15) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    galaxy_name VARCHAR(100) NOT NULL,
    assigned_ai_admin_id BIGINT,
    investigator_notes VARCHAR(2000),
    resolution_action VARCHAR(2000),
    root_cause VARCHAR(2000),
    related_order_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    escalated_at TIMESTAMP NULL,
    INDEX idx_aai_status (status),
    INDEX idx_aai_severity (severity),
    INDEX idx_aai_galaxy (galaxy_name),
    INDEX idx_aai_admin (assigned_ai_admin_id),
    INDEX idx_aai_created (created_at)
);
