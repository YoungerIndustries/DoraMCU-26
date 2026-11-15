-- V57: Create game_lores table for the universal game lore database
-- This table stores metadata for game universes/lores that can be used to create themed galaxies/games.
-- Lore entries can be pre-loaded (popular franchises) or fetched dynamically from the internet.

CREATE TABLE game_lores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Core identity
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    short_description VARCHAR(1000),
    description TEXT,

    -- Classification
    genre VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    technology_level VARCHAR(50),
    tone VARCHAR(50),

    -- Universe details (stored as JSON arrays)
    factions TEXT,
    species TEXT,

    -- Galaxy generation hints
    naming_convention VARCHAR(50),
    galaxy_shape VARCHAR(50),
    suggested_empire_count INT,

    -- Game mode suitability
    recommended_game_modes TEXT,
    suitable_for_rpg BOOLEAN,
    suitable_for_4x BOOLEAN,
    suitable_for_tycoon BOOLEAN,
    suitable_for_tactical BOOLEAN,
    suitable_for_survival BOOLEAN,

    -- Source tracking
    source_url VARCHAR(500),
    internet_sourced BOOLEAN DEFAULT FALSE,

    -- Status
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for fast lookups
CREATE UNIQUE INDEX idx_game_lore_slug ON game_lores (slug);
CREATE INDEX idx_game_lore_genre ON game_lores (genre);
CREATE INDEX idx_game_lore_category ON game_lores (category);
CREATE INDEX idx_game_lore_active ON game_lores (active);
