-- Star Trek 4x game tables

CREATE TABLE IF NOT EXISTS st4x_game_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    galaxy_id INT NOT NULL,
    current_turn INT NOT NULL DEFAULT 0,
    mode VARCHAR(20) NOT NULL DEFAULT 'FOUR_X',
    galaxy_seed BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS st4x_factions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sfb_empire_name VARCHAR(100),
    galaxy VARCHAR(50),
    combat_style VARCHAR(50),
    default_stance VARCHAR(20) NOT NULL DEFAULT 'NEUTRAL',
    game_session_id BIGINT NOT NULL,
    fleet_strength INT NOT NULL DEFAULT 0,
    colony_count INT NOT NULL DEFAULT 0,
    research_points INT NOT NULL DEFAULT 0,
    economy_output INT NOT NULL DEFAULT 0,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id)
);

CREATE TABLE IF NOT EXISTS st4x_faction_reputation (
    faction_id BIGINT NOT NULL,
    faction_name VARCHAR(100) NOT NULL,
    reputation_score INT NOT NULL DEFAULT 0,
    PRIMARY KEY (faction_id, faction_name),
    FOREIGN KEY (faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_faction_sectors (
    faction_id BIGINT NOT NULL,
    sector_id INT NOT NULL,
    PRIMARY KEY (faction_id, sector_id),
    FOREIGN KEY (faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_star_systems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sector_id INT NOT NULL,
    sub_sector_id INT NOT NULL,
    pos_x INT NOT NULL,
    pos_y INT NOT NULL,
    pos_z INT NOT NULL,
    uwp_atmosphere VARCHAR(5),
    uwp_gravity VARCHAR(5),
    uwp_size VARCHAR(5),
    uwp_hydrosphere VARCHAR(5),
    uwp_population VARCHAR(5),
    uwp_government VARCHAR(5),
    uwp_law VARCHAR(5),
    uwp_trade VARCHAR(50),
    alignment VARCHAR(20),
    has_warp_deposits BOOLEAN NOT NULL DEFAULT FALSE,
    notable_features VARCHAR(500),
    trade_value INT NOT NULL DEFAULT 0,
    earth2026_planet_id VARCHAR(50),
    discovered BOOLEAN NOT NULL DEFAULT FALSE,
    owner_faction_id BIGINT,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (owner_faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_system_species (
    system_id BIGINT NOT NULL,
    species_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (system_id, species_name),
    FOREIGN KEY (system_id) REFERENCES st4x_star_systems(id)
);

CREATE TABLE IF NOT EXISTS st4x_colonies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    population BIGINT NOT NULL DEFAULT 0,
    minerals_per_turn INT NOT NULL DEFAULT 0,
    energy_per_turn INT NOT NULL DEFAULT 0,
    food_per_turn INT NOT NULL DEFAULT 0,
    research_per_turn INT NOT NULL DEFAULT 0,
    owner_faction_id BIGINT NOT NULL,
    star_system_id BIGINT NOT NULL,
    earth2026_country_id VARCHAR(50),
    operational BOOLEAN NOT NULL DEFAULT TRUE,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (owner_faction_id) REFERENCES st4x_factions(id),
    FOREIGN KEY (star_system_id) REFERENCES st4x_star_systems(id)
);

CREATE TABLE IF NOT EXISTS st4x_colony_buildings (
    colony_id BIGINT NOT NULL,
    building_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (colony_id, building_name),
    FOREIGN KEY (colony_id) REFERENCES st4x_colonies(id)
);

CREATE TABLE IF NOT EXISTS st4x_trade_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origin_system_id BIGINT NOT NULL,
    destination_system_id BIGINT NOT NULL,
    cargo_type VARCHAR(20) NOT NULL,
    profit INT NOT NULL DEFAULT 0,
    security_level INT NOT NULL DEFAULT 50,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    owner_faction_id BIGINT NOT NULL,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (owner_faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_diplomatic_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faction1_name VARCHAR(100) NOT NULL,
    faction2_name VARCHAR(100) NOT NULL,
    relation_score INT NOT NULL DEFAULT 0,
    at_war BOOLEAN NOT NULL DEFAULT FALSE,
    last_event_at DATETIME,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id)
);

CREATE TABLE IF NOT EXISTS st4x_relation_treaties (
    relation_id BIGINT NOT NULL,
    treaty_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (relation_id, treaty_type),
    FOREIGN KEY (relation_id) REFERENCES st4x_diplomatic_relations(id)
);

CREATE TABLE IF NOT EXISTS st4x_ground_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    strength INT NOT NULL DEFAULT 100,
    max_strength INT NOT NULL DEFAULT 100,
    morale INT NOT NULL DEFAULT 80,
    attack_value INT NOT NULL DEFAULT 3,
    defense_value INT NOT NULL DEFAULT 3,
    character_id BIGINT,
    owner_faction_id BIGINT NOT NULL,
    weapon_loadout VARCHAR(200),
    in_combat BOOLEAN NOT NULL DEFAULT FALSE,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (owner_faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_planetary_battles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    star_system_id BIGINT NOT NULL,
    attacker_faction_id BIGINT NOT NULL,
    defender_faction_id BIGINT NOT NULL,
    third_reich_battle_id VARCHAR(50),
    outcome VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    attacker_casualties INT NOT NULL DEFAULT 0,
    defender_casualties INT NOT NULL DEFAULT 0,
    rounds_fought INT NOT NULL DEFAULT 0,
    narrative_summary VARCHAR(2000),
    started_at DATETIME NOT NULL,
    ended_at DATETIME,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (star_system_id) REFERENCES st4x_star_systems(id),
    FOREIGN KEY (attacker_faction_id) REFERENCES st4x_factions(id),
    FOREIGN KEY (defender_faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_battle_events (
    battle_id BIGINT NOT NULL,
    event_description VARCHAR(500) NOT NULL,
    PRIMARY KEY (battle_id, event_description),
    FOREIGN KEY (battle_id) REFERENCES st4x_planetary_battles(id)
);

CREATE TABLE IF NOT EXISTS st4x_tech_research (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    target_tech_level INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    research_points_spent INT NOT NULL DEFAULT 0,
    research_points_needed INT NOT NULL DEFAULT 100,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    owner_faction_id BIGINT NOT NULL,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id),
    FOREIGN KEY (owner_faction_id) REFERENCES st4x_factions(id)
);

CREATE TABLE IF NOT EXISTS st4x_mission_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captain_log_mission_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    action_description VARCHAR(500),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    game_session_id BIGINT NOT NULL,
    FOREIGN KEY (game_session_id) REFERENCES st4x_game_sessions(id)
);
