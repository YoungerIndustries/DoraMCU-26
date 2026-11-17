-- V70: Add corporation kiosk system in Younger Industries HQ
-- Creates the base tables for HQ, office units, corporation office state, and kiosk upgrades.
-- Extends office_units and corporation_office_state with kiosk/desk fields.
-- New tables: kiosk_upgrades

-- 1. Create the earth2026 sequence used by YoungerIndustriesHQ
CREATE SEQUENCE IF NOT EXISTS earth2026_sequence START WITH 1 INCREMENT BY 1;

-- 2. Create the younger_industries_hq table (if not exists — Hibernate DDL may have created it)
CREATE TABLE IF NOT EXISTS younger_industries_hq (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location_id BIGINT NOT NULL,
    office_floors INT NOT NULL DEFAULT 3,
    basement_levels INT NOT NULL DEFAULT 3,
    total_offices INT NOT NULL DEFAULT 0,
    occupied_offices INT NOT NULL DEFAULT 0,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_yihq_location ON younger_industries_hq(location_id);
CREATE INDEX IF NOT EXISTS idx_yihq_active ON younger_industries_hq(active);

-- 3. Create the office_units table (if not exists)
CREATE TABLE IF NOT EXISTS office_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hq_id BIGINT NOT NULL,
    floor_level INT NOT NULL,
    unit_number INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    size_sqm INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    owner_id BIGINT,
    is_owned BOOLEAN NOT NULL DEFAULT FALSE,
    monthly_rent DECIMAL(19, 2) NOT NULL DEFAULT 0,
    purchase_price DECIMAL(19, 2) NOT NULL DEFAULT 0,
    assigned_at TIMESTAMP NULL,
    corporation_id BIGINT,
    kiosk_type VARCHAR(50) DEFAULT 'DESK',
    monitor_count INT DEFAULT 1,
    has_holographic_display BOOLEAN DEFAULT FALSE,
    has_secure_terminal BOOLEAN DEFAULT FALSE,
    customization_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ou_hq ON office_units(hq_id);
CREATE INDEX IF NOT EXISTS idx_ou_owner ON office_units(owner_id);
CREATE INDEX IF NOT EXISTS idx_ou_status ON office_units(status);
CREATE INDEX IF NOT EXISTS idx_ou_corp ON office_units(corporation_id);

-- 4. Create the corporation_office_state table (if not exists)
CREATE TABLE IF NOT EXISTS corporation_office_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    office_building_level INT NOT NULL DEFAULT 0,
    last_saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    younger_hq_id BIGINT,
    office_unit_id BIGINT,
    office_floor VARCHAR(20),
    office_unit_number VARCHAR(10),
    kiosk_appearance VARCHAR(100),
    version INT,
    CONSTRAINT uk_cos_corp UNIQUE (corporation_id)
);

CREATE INDEX IF NOT EXISTS idx_cos_corp ON corporation_office_state(corporation_id);

-- 5. Create the kiosk_upgrades table
CREATE TABLE IF NOT EXISTS kiosk_upgrades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    office_unit_id BIGINT NOT NULL,
    upgrade_type VARCHAR(50) NOT NULL,
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ku_corp_unit_type UNIQUE (corporation_id, office_unit_id, upgrade_type)
);

CREATE INDEX IF NOT EXISTS idx_ku_corp ON kiosk_upgrades(corporation_id);
CREATE INDEX IF NOT EXISTS idx_ku_unit ON kiosk_upgrades(office_unit_id);
