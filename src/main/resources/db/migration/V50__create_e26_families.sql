-- V50: Create e26_families and e26_family_members tables
-- Replaces comma-separated family strings in E26Npc with proper entity relationships

CREATE TABLE IF NOT EXISTS e26_families (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    home_structure_id BIGINT,
    family_wealth INT DEFAULT 50,
    family_reputation INT DEFAULT 50,
    family_traits TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_e26_family_name ON e26_families(name);
CREATE INDEX idx_e26_family_home ON e26_families(home_structure_id);

CREATE TABLE IF NOT EXISTS e26_family_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    family_id BIGINT NOT NULL,
    character_id BIGINT,
    npc_id BIGINT,
    relationship_type VARCHAR(20) NOT NULL,
    bond_score INT DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (family_id) REFERENCES e26_families(id)
);

CREATE INDEX idx_e26_family_member_family ON e26_family_members(family_id);
CREATE INDEX idx_e26_family_member_character ON e26_family_members(character_id);
CREATE INDEX idx_e26_family_member_npc ON e26_family_members(npc_id);

-- Add family_id column to e26_npcs to link NPCs to families
ALTER TABLE e26_npcs ADD COLUMN IF NOT EXISTS family_id BIGINT;
CREATE INDEX idx_e26_npc_family ON e26_npcs(family_id);
