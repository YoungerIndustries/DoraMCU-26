-- V36: Add planet game character mapping table
-- Tracks which game characters are assigned to which planets,
-- allowing Earth 2026 to reuse existing AI characters from game data
-- and only create new ones as necessary.

CREATE TABLE IF NOT EXISTS planet_game_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    planet_id BIGINT NOT NULL,
    game_module_name VARCHAR(50) NOT NULL,
    game_character_id VARCHAR(100) NOT NULL,
    character_name VARCHAR(200) NOT NULL,
    character_role VARCHAR(50),
    species_id VARCHAR(50),
    is_player_controlled BOOLEAN NOT NULL DEFAULT FALSE,
    empire_name VARCHAR(100),
    country_or_region VARCHAR(200),
    ai_character_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_planet_game_char_planet (planet_id),
    INDEX idx_planet_game_char_game_id (game_module_name, game_character_id),
    INDEX idx_planet_game_char_ai (ai_character_id),
    UNIQUE KEY uk_planet_game_char (planet_id, game_module_name, game_character_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add game module name column to earth2026_planets for tracking which game created/owns a planet
ALTER TABLE earth2026_planets
    ADD COLUMN IF NOT EXISTS game_module_name VARCHAR(50),
    ADD COLUMN IF NOT EXISTS game_planet_metadata JSON;
