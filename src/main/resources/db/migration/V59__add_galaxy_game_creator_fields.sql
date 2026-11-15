-- V59: Add game creator integration fields to galaxies table
-- Links each galaxy to its source lore and selected game modes

ALTER TABLE galaxies ADD COLUMN lore_slug VARCHAR(100) DEFAULT NULL;
ALTER TABLE galaxies ADD COLUMN game_modes VARCHAR(500) DEFAULT NULL;

-- Index for looking up galaxies by lore
CREATE INDEX idx_galaxies_lore_slug ON galaxies(lore_slug);
