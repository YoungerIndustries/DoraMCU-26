-- V73: Add NPC system for Younger Industries HQ
-- Creates the hq_npcs table and seeds default NPCs

CREATE TABLE IF NOT EXISTS hq_npcs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hq_id BIGINT NOT NULL,
    npc_type VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    floor_location VARCHAR(10),
    dialogue_json TEXT,
    services_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hnpc_hq ON hq_npcs(hq_id);
CREATE INDEX IF NOT EXISTS idx_hnpc_type ON hq_npcs(npc_type);
CREATE INDEX IF NOT EXISTS idx_hnpc_active ON hq_npcs(is_active);

-- Seed default NPCs for Younger Industries HQ (HQ ID = 1)
INSERT IGNORE INTO hq_npcs (id, hq_id, npc_type, name, title, description, is_active, floor_location) VALUES
(1, 1, 'RECEPTIONIST', 'Sarah Chen', 'Head Receptionist', 'Warm and welcoming, Sarah greets every visitor and knows everything about the HQ.', TRUE, 'Lobby'),
(2, 1, 'SECURITY_GUARD', 'Marcus Johnson', 'Chief Security Officer', 'Ex-military, Marcus keeps the HQ safe and enforces all security protocols.', TRUE, 'Lobby'),
(3, 1, 'JANITOR', 'Hiro Tanaka', 'Maintenance Supervisor', 'Quiet and efficient, Hiro keeps the building spotless and can fix anything.', TRUE, 'Basement'),
(4, 1, 'TECH_SUPPORT', 'Aisha Patel', 'Senior Technician', 'Aisha is the go-to person for any kiosk or technical issues.', TRUE, 'Floor 1'),
(5, 1, 'LEGAL_ADVISOR', 'Robert Williams', 'General Counsel', 'Robert has 20 years of experience in intergalactic corporate law.', TRUE, 'Floor 2'),
(6, 1, 'RECRUITER', 'Elena Rodriguez', 'Talent Acquisition Lead', 'Elena finds the best AI characters and matches them to your needs.', TRUE, 'Floor 3'),
(7, 1, 'TRADER', 'Zara Okafor', 'Resource Broker', 'Zara has connections across all galaxies and always has the best deals.', TRUE, 'Floor 1'),
(8, 1, 'MEDIC', 'Dr. James Kim', 'Chief Medical Officer', 'Dr. Kim provides top-notch healthcare for all HQ residents.', TRUE, 'Floor 2'),
(9, 1, 'BARISTA', 'Luna Martinez', 'Cafe Manager', 'Luna brews the best coffee in the galaxy and knows everyone by name.', TRUE, 'Lobby'),
(10, 1, 'NEWS_ANCHOR', 'David Thompson', 'Galactic News Correspondent', 'David brings the latest news from across all connected galaxies.', TRUE, 'Lobby'),
(11, 1, 'HISTORIAN', 'Prof. Maria Santos', 'Younger Industries Archivist', 'Professor Santos preserves and shares the rich history of Younger Industries.', TRUE, 'Floor 3'),
(12, 1, 'PORTAL_OPERATOR', 'Alex Novak', 'Portal Systems Engineer', 'Alex manages the basement portals and ensures safe travel between galaxies.', TRUE, 'Basement B1');

-- Update sequence for hq_npcs
INSERT IGNORE INTO hibernate_sequence (sequence_name, next_val) VALUES ('hq_npcs_sequence', 100);
