-- V86: Add apartment building system for character housing
-- Creates apartment_buildings and apartment_units tables
-- Adds corporation_id to apartment_buildings for ownership tracking
-- Adds apartment_unit_id to earth2026_characters for character-to-apartment linkage

-- 1. Create apartment_buildings table
CREATE TABLE IF NOT EXISTS apartment_buildings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    location_id BIGINT NOT NULL,
    apartment_floors INT NOT NULL DEFAULT 10,
    basement_levels INT NOT NULL DEFAULT 10,
    total_apartments INT NOT NULL DEFAULT 0,
    occupied_apartments INT NOT NULL DEFAULT 0,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    corporation_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

-- 2. Create apartment_units table
CREATE TABLE IF NOT EXISTS apartment_units (
    id BIGINT NOT NULL AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    floor_level INT NOT NULL,
    unit_number INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    size_sqm INT NOT NULL DEFAULT 0,
    bedrooms INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    tenant_id BIGINT,
    monthly_rent DECIMAL(19,2) NOT NULL DEFAULT 0,
    assigned_at DATETIME,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

-- 3. Add corporation_id column to apartment_buildings for ownership tracking
ALTER TABLE apartment_buildings ADD COLUMN IF NOT EXISTS corporation_id BIGINT;

-- 4. Add apartment_unit_id to earth2026_characters for character-to-apartment linkage
ALTER TABLE earth2026_characters ADD COLUMN IF NOT EXISTS apartment_unit_id BIGINT;

-- 5. Create indexes for query performance
CREATE INDEX IF NOT EXISTS idx_ab_location ON apartment_buildings(location_id);
CREATE INDEX IF NOT EXISTS idx_ab_active ON apartment_buildings(active);
CREATE INDEX IF NOT EXISTS idx_ab_corporation ON apartment_buildings(corporation_id);
CREATE INDEX IF NOT EXISTS idx_au_building ON apartment_units(building_id);
CREATE INDEX IF NOT EXISTS idx_au_tenant ON apartment_units(tenant_id);
CREATE INDEX IF NOT EXISTS idx_au_status ON apartment_units(status);
CREATE INDEX IF NOT EXISTS idx_earth2026_char_apartment ON earth2026_characters(apartment_unit_id);
