-- V41: Add 4X framework tables for generic 4X game support

CREATE TABLE fourx_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    game_type VARCHAR(50),
    difficulty_level VARCHAR(50),
    current_turn INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_by_player_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    started_at DATETIME,
    ended_at DATETIME,
    exploration_unlocked BOOLEAN NOT NULL DEFAULT TRUE,
    expansion_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    exploitation_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    extermination_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    diplomacy_unlocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE fourx_factions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faction_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    game_module VARCHAR(50) NOT NULL,
    combat_style VARCHAR(20),
    default_stance VARCHAR(20),
    reputation_map_json TEXT,
    controlled_sectors_json TEXT,
    fleet_strength INT NOT NULL DEFAULT 0,
    colony_count INT NOT NULL DEFAULT 0,
    research_points INT NOT NULL DEFAULT 0,
    economy_output INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES fourx_sessions(id) ON DELETE CASCADE
);

CREATE TABLE fourx_colonies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    colony_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    population BIGINT NOT NULL DEFAULT 0,
    minerals_per_turn INT NOT NULL DEFAULT 0,
    energy_per_turn INT NOT NULL DEFAULT 0,
    food_per_turn INT NOT NULL DEFAULT 0,
    research_per_turn INT NOT NULL DEFAULT 0,
    buildings_json TEXT,
    owner_faction_id VARCHAR(100) NOT NULL,
    star_system_id VARCHAR(100) NOT NULL,
    operational BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES fourx_sessions(id) ON DELETE CASCADE
);

CREATE TABLE fourx_trade_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_route_id VARCHAR(100) NOT NULL UNIQUE,
    origin_system_id VARCHAR(100) NOT NULL,
    destination_system_id VARCHAR(100) NOT NULL,
    cargo_type VARCHAR(20) NOT NULL,
    profit INT NOT NULL DEFAULT 0,
    security_level INT NOT NULL DEFAULT 50,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    owner_faction_id VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES fourx_sessions(id) ON DELETE CASCADE
);

CREATE TABLE fourx_diplomatic_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    relation_id VARCHAR(100) NOT NULL UNIQUE,
    faction1_id VARCHAR(100) NOT NULL,
    faction2_id VARCHAR(100) NOT NULL,
    relation_score INT NOT NULL DEFAULT 0,
    at_war BOOLEAN NOT NULL DEFAULT FALSE,
    active_treaties_json TEXT,
    last_event_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES fourx_sessions(id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX idx_fourx_factions_session ON fourx_factions(session_id);
CREATE INDEX idx_fourx_factions_game_module ON fourx_factions(game_module);
CREATE INDEX idx_fourx_colonies_owner ON fourx_colonies(owner_faction_id);
CREATE INDEX idx_fourx_colonies_session ON fourx_colonies(session_id);
CREATE INDEX idx_fourx_trade_routes_owner ON fourx_trade_routes(owner_faction_id);
CREATE INDEX idx_fourx_trade_routes_session ON fourx_trade_routes(session_id);
CREATE INDEX idx_fourx_diplomacy_faction1 ON fourx_diplomatic_relations(faction1_id);
CREATE INDEX idx_fourx_diplomacy_faction2 ON fourx_diplomatic_relations(faction2_id);
CREATE INDEX idx_fourx_diplomacy_session ON fourx_diplomatic_relations(session_id);
CREATE INDEX idx_fourx_sessions_status ON fourx_sessions(status);
