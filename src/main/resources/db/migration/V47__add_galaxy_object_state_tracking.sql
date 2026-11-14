-- V47: Add galaxy object state tracking table
-- This table tracks the lifecycle state of all galaxy objects so that the
-- Galaxy Overlord manages state and only builds objects needed for player
-- interactions. Objects destroyed from conflict are recorded and can be rebuilt.

CREATE TABLE IF NOT EXISTS galaxy_object_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_name VARCHAR(255) NOT NULL,
    object_type VARCHAR(50) NOT NULL,
    object_id VARCHAR(255) NOT NULL,
    object_name VARCHAR(255),
    object_state VARCHAR(20) NOT NULL DEFAULT 'MAPPED',
    sector_row INT,
    sector_col INT,
    sub_sector_row INT,
    sub_sector_col INT,
    metadata TEXT,
    planet_name VARCHAR(255),
    solar_system_id BIGINT,
    destroyed_by VARCHAR(255),
    destroyed_at TIMESTAMP NULL,
    rebuilt_at TIMESTAMP NULL,
    rebuild_count INT NOT NULL DEFAULT 0,
    requires_3d BOOLEAN NOT NULL DEFAULT FALSE,
    has_3d_detail BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_galaxy_object UNIQUE (galaxy_name, object_type, object_id)
);

CREATE INDEX idx_galaxy_obj_galaxy ON galaxy_object_states(galaxy_name);
CREATE INDEX idx_galaxy_obj_type ON galaxy_object_states(object_type);
CREATE INDEX idx_galaxy_obj_state ON galaxy_object_states(object_state);
CREATE INDEX idx_galaxy_obj_position ON galaxy_object_states(sector_row, sector_col);
CREATE INDEX idx_galaxy_obj_destroyed ON galaxy_object_states(object_state, destroyed_at);
