-- V80: Extend building construction with corporation ownership and production tracking

ALTER TABLE planet_structures ADD COLUMN corporation_id BIGINT;
ALTER TABLE planet_structures ADD COLUMN building_level INT DEFAULT 1;
ALTER TABLE planet_structures ADD COLUMN production_output VARCHAR(500);

CREATE INDEX idx_planet_structures_corporation ON planet_structures(corporation_id);
CREATE INDEX idx_planet_structures_building_level ON planet_structures(building_level);
