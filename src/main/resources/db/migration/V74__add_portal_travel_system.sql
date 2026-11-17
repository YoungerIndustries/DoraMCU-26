-- V74: Add portal travel tracking and HqPortal energy/cooldown fields

-- Add energy_cost and cooldown_seconds to hq_portals (if not exists)
ALTER TABLE hq_portals ADD COLUMN IF NOT EXISTS energy_cost BIGINT DEFAULT 100;
ALTER TABLE hq_portals ADD COLUMN IF NOT EXISTS cooldown_seconds INT DEFAULT 300;

-- Create portal_travel table for tracking intergalactic travel from HQ
CREATE TABLE IF NOT EXISTS portal_travel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    portal_id BIGINT NOT NULL,
    corporation_id BIGINT,
    destination_game VARCHAR(50) NOT NULL,
    energy_cost BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    traveled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cooldown_expires_at TIMESTAMP NULL,
    travel_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pt_character (character_id),
    INDEX idx_pt_portal (portal_id),
    INDEX idx_pt_corporation (corporation_id),
    INDEX idx_pt_status (status),
    INDEX idx_pt_traveled_at (traveled_at)
);
