-- V100: Add AI admin shift handover table
-- Enables outgoing admins to brief incoming admins on pending work before shift changes

CREATE TABLE ai_admin_shift_handovers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outgoing_admin_id BIGINT NOT NULL,
    incoming_admin_id BIGINT NOT NULL,
    galaxy_name VARCHAR(100) NOT NULL,
    handover_type VARCHAR(10) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    summary TEXT,
    pending_incidents TEXT,
    pending_orders TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL
);

CREATE INDEX idx_aash_outgoing ON ai_admin_shift_handovers(outgoing_admin_id);
CREATE INDEX idx_aash_incoming ON ai_admin_shift_handovers(incoming_admin_id);
CREATE INDEX idx_aash_galaxy ON ai_admin_shift_handovers(galaxy_name);
CREATE INDEX idx_aash_status ON ai_admin_shift_handovers(status);
CREATE INDEX idx_aash_created ON ai_admin_shift_handovers(created_at);
