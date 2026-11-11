-- Create player_character_registry table for unified character tracking
CREATE TABLE player_character_registry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    character_type VARCHAR(20) NOT NULL,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(100) NOT NULL,
    character_role VARCHAR(20) NOT NULL,
    galaxy_name VARCHAR(100) NOT NULL,
    is_ai_controlled BOOLEAN NOT NULL DEFAULT FALSE,
    home_location_id VARCHAR(100),
    home_location_type VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pcr_player_character UNIQUE (player_id, character_type, character_id),
    CONSTRAINT fk_pcr_player FOREIGN KEY (player_id) REFERENCES users(id)
);

-- Add indexes for common queries
CREATE INDEX idx_pcr_player ON player_character_registry(player_id);
CREATE INDEX idx_pcr_galaxy ON player_character_registry(galaxy_name);
CREATE INDEX idx_pcr_role ON player_character_registry(character_role);

-- Add galaxy_name and owner_player_id to cl_crew_members for wormhole travel tracking
ALTER TABLE cl_crew_members ADD COLUMN galaxy_name VARCHAR(100);
ALTER TABLE cl_crew_members ADD COLUMN owner_player_id BIGINT;
