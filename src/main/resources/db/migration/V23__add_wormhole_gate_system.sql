-- V23: Add wormhole gate system for inter-galaxy travel
-- Enables creating paired wormholes between galaxies,
-- tracking wormhole state, and logging galaxy travel events.

CREATE TABLE wormholes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_name VARCHAR(255) NOT NULL UNIQUE,
    solar_system_id BIGINT NOT NULL,
    sector_row INT NOT NULL,
    sector_col INT NOT NULL,
    sub_sector_row INT NOT NULL,
    sub_sector_col INT NOT NULL,
    pos_x INT NOT NULL,
    pos_y INT NOT NULL,
    pos_z INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    connected_wormhole_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wormhole_galaxy ON wormholes(galaxy_name);
CREATE INDEX idx_wormhole_connected ON wormholes(connected_wormhole_id);

CREATE TABLE galaxy_travel_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(255) NOT NULL,
    from_galaxy VARCHAR(255) NOT NULL,
    to_galaxy VARCHAR(255) NOT NULL,
    travel_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    initiated_by VARCHAR(50) NOT NULL DEFAULT 'PLAYER'
);

CREATE INDEX idx_travel_log_player ON galaxy_travel_log(player_name);
CREATE INDEX idx_travel_log_timestamp ON galaxy_travel_log(travel_timestamp);
