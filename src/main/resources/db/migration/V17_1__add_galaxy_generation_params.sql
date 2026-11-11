-- V17: Add generation parameters to galaxies table
-- Enables galaxy regeneration from original parameters,
-- admin UI showing galaxy configuration, and template-based galaxy creation.

ALTER TABLE galaxies ADD COLUMN seed BIGINT;
ALTER TABLE galaxies ADD COLUMN system_count INT;
ALTER TABLE galaxies ADD COLUMN planets_per_system INT;
ALTER TABLE galaxies ADD COLUMN moon_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN asteroid_belt_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN gas_giant_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN nebula_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN black_hole_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN comet_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN space_station_chance DOUBLE;
ALTER TABLE galaxies ADD COLUMN created_at TIMESTAMP;
ALTER TABLE galaxies ADD COLUMN description TEXT;
ALTER TABLE galaxies ADD COLUMN game_type VARCHAR(50);
ALTER TABLE galaxies ADD COLUMN difficulty_level VARCHAR(20);
ALTER TABLE galaxies ADD COLUMN theme VARCHAR(100);
