-- V88: Universal Object Registry Schema
-- Comprehensive database schema for all game systems (economic, 4X, RPG, FPS)
-- Every renderable object in the game can be referenced through this registry

-- ============================================
-- UNIVERSAL OBJECT REGISTRY
-- ============================================

-- Core object table - every renderable object
CREATE TABLE IF NOT EXISTS game_object (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    object_uuid VARCHAR(36) NOT NULL,
    object_type VARCHAR(50) NOT NULL,
    object_subtype VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),

    -- 3D rendering data
    model_3d_path VARCHAR(500),
    texture_path VARCHAR(500),
    skin_id BIGINT,
    decal_ids JSON,
    scale_factor DECIMAL(5,2) DEFAULT 1.00,
    color_hex VARCHAR(7),

    -- Resource costs for production
    resource_costs JSON,
    refined_resource_costs JSON,
    strategic_resource_requirements JSON,
    cargo_costs JSON,
    construction_cost INT,
    maintenance_cost INT,
    build_time_turns INT,

    -- Production chain metadata
    production_level VARCHAR(20),
    requires_industry VARCHAR(50),
    produced_by_industry VARCHAR(50),

    -- Cross-system references
    equivalent_objects JSON,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_object_uuid (object_uuid),
    INDEX idx_go_object_type (object_type),
    INDEX idx_go_object_subtype (object_subtype),
    INDEX idx_go_game_module (game_module)
);

-- ============================================
-- RESOURCE SYSTEM
-- ============================================

-- Raw resources (space)
CREATE TABLE IF NOT EXISTS raw_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    color_hex VARCHAR(7),
    celestial_sources JSON,

    UNIQUE KEY uk_raw_resource_type (resource_type),
    INDEX idx_rr_resource_type (resource_type)
);

-- Refined resources (space)
CREATE TABLE IF NOT EXISTS refined_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    color_hex VARCHAR(7),
    primary_use TEXT,

    UNIQUE KEY uk_refined_resource_type (resource_type),
    INDEX idx_rf_resource_type (resource_type)
);

-- Processing recipes
CREATE TABLE IF NOT EXISTS processing_recipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    output_resource VARCHAR(50) NOT NULL,
    output_amount INT NOT NULL,
    recipe_variant VARCHAR(20) DEFAULT 'STANDARD',
    processing_time_turns INT NOT NULL,

    INDEX idx_pr_output (output_resource),
    INDEX idx_pr_variant (recipe_variant)
);

-- Recipe inputs (many-to-many)
CREATE TABLE IF NOT EXISTS processing_recipe_input (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    raw_resource_type VARCHAR(50) NOT NULL,
    required_amount INT NOT NULL,

    INDEX idx_pri_recipe (recipe_id),
    INDEX idx_pri_resource (raw_resource_type)
);

-- Strategic resources
CREATE TABLE IF NOT EXISTS strategic_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    gates_capabilities TEXT,

    UNIQUE KEY uk_strategic_resource_type (resource_type),
    INDEX idx_sr_resource_type (resource_type)
);

-- Cargo types (planet surface)
CREATE TABLE IF NOT EXISTS cargo_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cargo_type_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    is_perishable BOOLEAN DEFAULT FALSE,
    base_value INT NOT NULL,

    UNIQUE KEY uk_cargo_type_code (cargo_type_code),
    INDEX idx_ct_category (category)
);

-- ============================================
-- INDUSTRY SYSTEM
-- ============================================

-- Industry types
CREATE TABLE IF NOT EXISTS industry_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    industry_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    production_level VARCHAR(20) NOT NULL,
    construction_cost INT NOT NULL,
    maintenance_cost INT NOT NULL,
    required_terrain JSON,

    UNIQUE KEY uk_industry_code (industry_code),
    INDEX idx_it_production_level (production_level)
);

-- Industry outputs
CREATE TABLE IF NOT EXISTS industry_output (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    industry_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    amount_per_turn INT NOT NULL,

    INDEX idx_io_industry (industry_code),
    INDEX idx_io_cargo (cargo_type_code)
);

-- Industry inputs
CREATE TABLE IF NOT EXISTS industry_input (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    industry_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    amount_per_turn INT NOT NULL,

    INDEX idx_ii_industry (industry_code),
    INDEX idx_ii_cargo (cargo_type_code)
);

-- ============================================
-- GALAXY SYSTEM
-- ============================================

-- Celestial body types
CREATE TABLE IF NOT EXISTS celestial_body_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    body_type VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    can_have_resources BOOLEAN DEFAULT FALSE,
    can_have_industries BOOLEAN DEFAULT FALSE,
    can_have_stations BOOLEAN DEFAULT FALSE,

    UNIQUE KEY uk_celestial_body_type (body_type),
    INDEX idx_cbt_body_type (body_type)
);

-- Terrain types
CREATE TABLE IF NOT EXISTS terrain_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    terrain_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    color_hex VARCHAR(7),
    supports_industry BOOLEAN DEFAULT FALSE,

    UNIQUE KEY uk_terrain_code (terrain_code),
    INDEX idx_tt_terrain_code (terrain_code)
);

-- ============================================
-- SHIP SYSTEM
-- ============================================

-- Ship types
CREATE TABLE IF NOT EXISTS ship_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ship_type_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    base_hull_points INT NOT NULL,
    component_slots INT NOT NULL,
    build_time_turns INT NOT NULL,
    m_drive_speed INT NOT NULL,
    j_drive_speed INT NOT NULL,
    base_evasion DECIMAL(3,2) NOT NULL,
    cubic_meters INT NOT NULL,
    hull_module_count INT NOT NULL,
    module_grid_dimensions JSON,

    UNIQUE KEY uk_ship_type_code (ship_type_code),
    INDEX idx_st_ship_type_code (ship_type_code)
);

-- Module types
CREATE TABLE IF NOT EXISTS module_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,
    power_requirement INT NOT NULL,
    base_cost_per_ton INT NOT NULL,
    is_critical BOOLEAN DEFAULT FALSE,

    UNIQUE KEY uk_module_code (module_code),
    INDEX idx_mt_category (category)
);

-- Weapon types (space combat)
CREATE TABLE IF NOT EXISTS weapon_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    weapon_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    strategic_resource VARCHAR(50),
    base_damage INT NOT NULL,
    range_tier INT NOT NULL,
    range_description VARCHAR(20),

    UNIQUE KEY uk_weapon_code (weapon_code),
    INDEX idx_wt_weapon_code (weapon_code),
    INDEX idx_wt_strategic (strategic_resource)
);

-- ============================================
-- RPG SYSTEM
-- ============================================

-- Character characteristics (universal)
CREATE TABLE IF NOT EXISTS character_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_code VARCHAR(20) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    min_value INT DEFAULT 0,
    max_value INT DEFAULT 100,

    UNIQUE KEY uk_stat_code (stat_code),
    INDEX idx_cs_stat_code (stat_code)
);

-- B5-specific characteristics
CREATE TABLE IF NOT EXISTS b5_characteristic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    char_code VARCHAR(10) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,

    UNIQUE KEY uk_b5_char_code (char_code),
    INDEX idx_b5c_char_code (char_code)
);

-- Skills
CREATE TABLE IF NOT EXISTS skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),
    related_stats JSON,

    UNIQUE KEY uk_skill_code (skill_code),
    INDEX idx_sk_game_module (game_module)
);

-- Species/Races
CREATE TABLE IF NOT EXISTS species (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    species_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),
    stat_modifiers JSON,

    UNIQUE KEY uk_species_code (species_code),
    INDEX idx_sp_game_module (game_module)
);

-- ============================================
-- FPS SYSTEM
-- ============================================

-- FPS weapon types
CREATE TABLE IF NOT EXISTS fps_weapon_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    weapon_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,

    UNIQUE KEY uk_fps_weapon_type_code (weapon_code),
    INDEX idx_fwt_weapon_code (weapon_code)
);

-- FPS weapons
CREATE TABLE IF NOT EXISTS fps_weapon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    weapon_uuid VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    weapon_type_code VARCHAR(50) NOT NULL,
    damage INT NOT NULL,
    fire_rate DECIMAL(5,2) NOT NULL,
    reload_time DECIMAL(5,2) NOT NULL,
    mag_size INT NOT NULL,
    reserve_ammo INT NOT NULL,
    is_hit_scan BOOLEAN DEFAULT TRUE,
    projectile_speed DECIMAL(7,2),
    recoil_pitch DECIMAL(5,2),
    recoil_yaw DECIMAL(5,2),
    range DECIMAL(7,2),
    description TEXT,
    model_3d_path VARCHAR(500),
    texture_path VARCHAR(500),

    UNIQUE KEY uk_fps_weapon_uuid (weapon_uuid),
    INDEX idx_fw_weapon_type (weapon_type_code)
);

-- ============================================
-- MILITARY UNIT SYSTEM
-- ============================================

-- Military unit types
CREATE TABLE IF NOT EXISTS military_unit_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    game_module VARCHAR(50),

    UNIQUE KEY uk_military_unit_code (unit_code),
    INDEX idx_mut_game_module (game_module)
);

-- Military unit production costs
CREATE TABLE IF NOT EXISTS military_unit_cost (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    required_amount INT NOT NULL,

    INDEX idx_muc_unit (unit_code),
    INDEX idx_muc_cargo (cargo_type_code)
);

-- ============================================
-- VISUAL CUSTOMIZATION
-- ============================================

-- Skins
CREATE TABLE IF NOT EXISTS skin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skin_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    texture_path VARCHAR(500),
    color_palette JSON,
    applicable_object_types JSON,

    UNIQUE KEY uk_skin_code (skin_code),
    INDEX idx_sk_skin_code (skin_code)
);

-- Decals
CREATE TABLE IF NOT EXISTS decal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    decal_code VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    texture_path VARCHAR(500),
    decal_type VARCHAR(20),

    UNIQUE KEY uk_decal_code (decal_code),
    INDEX idx_dc_decal_code (decal_code)
);

-- ============================================
-- PRODUCTION CHAIN TRACKER
-- ============================================

-- Complete production chain from raw resources to final products
CREATE TABLE IF NOT EXISTS production_chain (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_name VARCHAR(100) NOT NULL,
    final_product_object_id BIGINT,
    chain_level INT NOT NULL,
    description TEXT,

    INDEX idx_pc_name (chain_name),
    INDEX idx_pc_final_product (final_product_object_id),
    INDEX idx_pc_level (chain_level)
);

-- Production chain steps
CREATE TABLE IF NOT EXISTS production_chain_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    input_type VARCHAR(50),
    input_code VARCHAR(50),
    input_amount INT,
    output_type VARCHAR(50),
    output_code VARCHAR(50),
    output_amount INT,
    processing_time_turns INT,
    required_industry VARCHAR(50),

    INDEX idx_pcs_chain (chain_id),
    INDEX idx_pcs_step_order (chain_id, step_order)
);

-- ============================================
-- OBJECT RELATIONSHIPS
-- ============================================

-- Generic object relationships
CREATE TABLE IF NOT EXISTS object_relationship (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_object_id BIGINT NOT NULL,
    target_object_id BIGINT NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    metadata JSON,

    INDEX idx_or_source (source_object_id),
    INDEX idx_or_target (target_object_id),
    INDEX idx_or_relationship_type (relationship_type)
);

-- ============================================
-- SEED DATA
-- ============================================

-- Raw resources (space)
INSERT IGNORE INTO raw_resource (resource_type, display_name, description, color_hex, celestial_sources) VALUES
('ORE', 'Ore', 'Metallic minerals extracted from planets and asteroids', '#8B4513', '["PLANET", "ASTEROID", "ASTEROID_BELT"]'),
('CRYSTAL', 'Crystal', 'Crystalline structures harvested from comets and planets', '#9370DB', '["COMET", "PLANET"]'),
('GAS', 'Gas', 'Gaseous compounds collected from gas giants', '#87CEEB', '["GAS_GIANT"]'),
('DUST', 'Dust', 'Fine particulate matter gathered from nebulae and asteroid belts', '#D2B48C', '["NEBULA", "ASTEROID_BELT"]'),
('BIO_MASS', 'Bio Mass', 'Organic material harvested from habitable planets', '#228B22', '["PLANET"]');

-- Refined resources (space)
INSERT IGNORE INTO refined_resource (resource_type, display_name, description, color_hex, primary_use) VALUES
('METAL_ALLOY', 'Metal Alloy', 'Processed metal compound for hull and weapon construction', '#708090', 'Hull and weapon modules'),
('POWER_CELL', 'Power Cell', 'Energy storage unit for weapons, shields, and engines', '#FFD700', 'Weapons, shields, engines'),
('FUEL', 'Fuel', 'Propellant for engine modules', '#FF4500', 'Engine modules'),
('COMPOSITE', 'Composite', 'Advanced material for hull and shield modules', '#4682B4', 'Hull and shield modules'),
('NUTRIENT', 'Nutrient', 'Organic compound for facility modules', '#32CD32', 'Facility modules');

-- Strategic resources
INSERT IGNORE INTO strategic_resource (resource_type, display_name, description, gates_capabilities) VALUES
('ELERIUM', 'Elerium', 'Rare crystalline mineral', 'Beam weapons, shields'),
('ANTI_MATTER', 'Anti-Matter', 'Exotic matter-antimatter compound', 'Missile weapons'),
('THULIUM', 'Thulium', 'Rare earth element', 'Mass driver weapons'),
('DURANTIUM', 'Durantium', 'Ultra-rare metallic deposit', 'Elite manufacturing'),
('PROMETHION', 'Promethion', 'Extremely rare research material', 'Elite research');

-- Cargo types (planet surface)
INSERT IGNORE INTO cargo_type (cargo_type_code, display_name, category, color_hex, is_perishable, base_value) VALUES
('ORE', 'Ore', 'RAW', '#8B4513', FALSE, 10),
('COAL', 'Coal', 'RAW', '#2F4F4F', FALSE, 10),
('OIL', 'Oil', 'RAW', '#1C1C1C', FALSE, 15),
('WOOD', 'Wood', 'RAW', '#8B6914', FALSE, 8),
('GRAIN', 'Grain', 'RAW', '#DAA520', TRUE, 10),
('LIVESTOCK', 'Livestock', 'RAW', '#8FBC8F', TRUE, 15),
('FISH', 'Fish', 'RAW', '#4682B4', TRUE, 12),
('FRUIT', 'Fruit', 'RAW', '#FF6347', TRUE, 10),
('MAIL', 'Mail', 'MAIL', '#FFB6C1', FALSE, 5),
('PASSENGERS', 'Passengers', 'MAIL', '#FFDAB9', FALSE, 5),
('STEEL', 'Steel', 'PROCESSED', '#778899', FALSE, 25),
('CONCRETE', 'Concrete', 'PROCESSED', '#A9A9A9', FALSE, 15),
('GLASS', 'Glass', 'PROCESSED', '#E0FFFF', FALSE, 20),
('LUMBER', 'Lumber', 'PROCESSED', '#DEB887', FALSE, 15),
('BREAD', 'Bread', 'PROCESSED', '#F5DEB3', TRUE, 18),
('MEAT', 'Meat', 'PROCESSED', '#CD5C5C', TRUE, 20),
('REFINED_OIL', 'Refined Oil', 'PROCESSED', '#696969', FALSE, 30),
('PLASTIC', 'Plastic', 'PROCESSED', '#FF69B4', FALSE, 25),
('ELECTRONICS', 'Electronics', 'ADVANCED', '#00CED1', FALSE, 50),
('MACHINERY', 'Machinery', 'ADVANCED', '#483D8B', FALSE, 45),
('LUXURY', 'Luxury Goods', 'ADVANCED', '#FFD700', FALSE, 60),
('CHEMICALS', 'Chemicals', 'ADVANCED', '#9370DB', FALSE, 40),
('ROCKET_PARTS', 'Rocket Parts', 'ADVANCED', '#DC143C', FALSE, 80),
('SPACECRAFT', 'Spacecraft', 'ADVANCED', '#4169E1', FALSE, 150),
('LANDERS', 'Landers', 'ADVANCED', '#2E8B57', FALSE, 100);

-- Industry types
INSERT IGNORE INTO industry_type (industry_code, display_name, production_level, construction_cost, maintenance_cost, required_terrain) VALUES
('MINE', 'Mine', 'PRIMARY', 100, 10, '["MOUNTAIN"]'),
('COAL_MINE', 'Coal Mine', 'PRIMARY', 120, 12, '["MOUNTAIN"]'),
('OIL_WELL', 'Oil Well', 'PRIMARY', 150, 15, '["FLATLANDS", "DESERT"]'),
('LOGGING_CAMP', 'Logging Camp', 'PRIMARY', 80, 8, '["FOREST"]'),
('FARM', 'Farm', 'PRIMARY', 60, 6, '["FLATLANDS", "GRASSLAND"]'),
('RANCH', 'Ranch', 'PRIMARY', 70, 7, '["GRASSLAND"]'),
('FISHING_PORT', 'Fishing Port', 'PRIMARY', 90, 9, '["COAST"]'),
('ORCHARD', 'Orchard', 'PRIMARY', 65, 6, '["FLATLANDS"]'),
('STEEL_MILL', 'Steel Mill', 'SECONDARY', 200, 20, '["FLATLANDS"]'),
('CEMENT_PLANT', 'Cement Plant', 'SECONDARY', 180, 18, '["FLATLANDS"]'),
('GLASS_FACTORY', 'Glass Factory', 'SECONDARY', 190, 19, '["FLATLANDS"]'),
('SAWMILL', 'Sawmill', 'SECONDARY', 100, 10, '["FOREST"]'),
('BAKERY', 'Bakery', 'SECONDARY', 80, 8, '["FLATLANDS"]'),
('SLAUGHTERHOUSE', 'Slaughterhouse', 'SECONDARY', 90, 9, '["FLATLANDS"]'),
('REFINERY', 'Refinery', 'SECONDARY', 220, 22, '["FLATLANDS"]'),
('CHEMICAL_PLANT', 'Chemical Plant', 'SECONDARY', 210, 21, '["FLATLANDS"]'),
('ELECTRONICS_FACTORY', 'Electronics Factory', 'TERTIARY', 300, 30, '["FLATLANDS"]'),
('MACHINE_SHOP', 'Machine Shop', 'TERTIARY', 280, 28, '["FLATLANDS"]'),
('LUXURY_FACTORY', 'Luxury Factory', 'TERTIARY', 250, 25, '["FLATLANDS"]'),
('ADVANCED_CHEMICALS', 'Advanced Chemicals', 'TERTIARY', 290, 29, '["FLATLANDS"]'),
('ROCKET_PARTS_FACTORY', 'Rocket Parts Factory', 'TERTIARY', 400, 40, '["FLATLANDS"]'),
('SPACECRAFT_ASSEMBLY', 'Spacecraft Assembly', 'TERTIARY', 500, 50, '["FLATLANDS"]'),
('LANDER_FACTORY', 'Lander Factory', 'TERTIARY', 350, 35, '["FLATLANDS"]'),
('ROCKET_LAUNCH_PAD', 'Rocket Launch Pad', 'TERTIARY', 600, 60, '["FLATLANDS"]');

-- Industry outputs
INSERT IGNORE INTO industry_output (industry_code, cargo_type_code, amount_per_turn) VALUES
('MINE', 'ORE', 10),
('COAL_MINE', 'COAL', 10),
('OIL_WELL', 'OIL', 8),
('LOGGING_CAMP', 'WOOD', 12),
('FARM', 'GRAIN', 10),
('RANCH', 'LIVESTOCK', 6),
('FISHING_PORT', 'FISH', 8),
('ORCHARD', 'FRUIT', 8),
('STEEL_MILL', 'STEEL', 6),
('CEMENT_PLANT', 'CONCRETE', 8),
('GLASS_FACTORY', 'GLASS', 6),
('SAWMILL', 'LUMBER', 8),
('BAKERY', 'BREAD', 10),
('SLAUGHTERHOUSE', 'MEAT', 6),
('REFINERY', 'REFINED_OIL', 6),
('CHEMICAL_PLANT', 'PLASTIC', 5),
('ELECTRONICS_FACTORY', 'ELECTRONICS', 3),
('MACHINE_SHOP', 'MACHINERY', 3),
('LUXURY_FACTORY', 'LUXURY', 2),
('ADVANCED_CHEMICALS', 'CHEMICALS', 3),
('ROCKET_PARTS_FACTORY', 'ROCKET_PARTS', 2),
('SPACECRAFT_ASSEMBLY', 'SPACECRAFT', 1),
('LANDER_FACTORY', 'LANDERS', 2);

-- Industry inputs
INSERT IGNORE INTO industry_input (industry_code, cargo_type_code, amount_per_turn) VALUES
('STEEL_MILL', 'ORE', 8),
('STEEL_MILL', 'COAL', 4),
('CEMENT_PLANT', 'ORE', 6),
('GLASS_FACTORY', 'CONCRETE', 4),
('GLASS_FACTORY', 'COAL', 2),
('SAWMILL', 'WOOD', 10),
('BAKERY', 'GRAIN', 8),
('SLAUGHTERHOUSE', 'LIVESTOCK', 8),
('REFINERY', 'OIL', 8),
('CHEMICAL_PLANT', 'REFINED_OIL', 6),
('ELECTRONICS_FACTORY', 'STEEL', 4),
('ELECTRONICS_FACTORY', 'GLASS', 3),
('ELECTRONICS_FACTORY', 'PLASTIC', 2),
('MACHINE_SHOP', 'STEEL', 6),
('MACHINE_SHOP', 'REFINED_OIL', 2),
('LUXURY_FACTORY', 'FRUIT', 4),
('LUXURY_FACTORY', 'BREAD', 2),
('LUXURY_FACTORY', 'PLASTIC', 2),
('ADVANCED_CHEMICALS', 'PLASTIC', 4),
('ADVANCED_CHEMICALS', 'REFINED_OIL', 3),
('ROCKET_PARTS_FACTORY', 'STEEL', 6),
('ROCKET_PARTS_FACTORY', 'ELECTRONICS', 4),
('SPACECRAFT_ASSEMBLY', 'ROCKET_PARTS', 4),
('SPACECRAFT_ASSEMBLY', 'ELECTRONICS', 6),
('LANDER_FACTORY', 'ROCKET_PARTS', 3),
('LANDER_FACTORY', 'MACHINERY', 2);

-- Processing recipes (space resources)
INSERT IGNORE INTO processing_recipe (output_resource, output_amount, recipe_variant, processing_time_turns) VALUES
('METAL_ALLOY', 1, 'STANDARD', 2),
('METAL_ALLOY', 1, 'ALTERNATIVE', 3),
('POWER_CELL', 1, 'STANDARD', 2),
('POWER_CELL', 1, 'ALTERNATIVE', 3),
('FUEL', 1, 'STANDARD', 1),
('FUEL', 1, 'ALTERNATIVE', 2),
('COMPOSITE', 1, 'STANDARD', 3),
('COMPOSITE', 1, 'ALTERNATIVE', 3),
('NUTRIENT', 1, 'STANDARD', 1),
('NUTRIENT', 1, 'ALTERNATIVE', 2);

-- Processing recipe inputs
INSERT IGNORE INTO processing_recipe_input (recipe_id, raw_resource_type, required_amount) VALUES
(1, 'ORE', 2),
(1, 'CRYSTAL', 1),
(2, 'ORE', 3),
(3, 'CRYSTAL', 2),
(3, 'ORE', 1),
(4, 'CRYSTAL', 1),
(4, 'GAS', 1),
(4, 'ORE', 1),
(5, 'GAS', 2),
(5, 'CRYSTAL', 1),
(6, 'GAS', 3),
(7, 'DUST', 2),
(7, 'ORE', 1),
(7, 'CRYSTAL', 1),
(8, 'DUST', 3),
(8, 'BIO_MASS', 1),
(9, 'BIO_MASS', 2),
(9, 'CRYSTAL', 1),
(10, 'BIO_MASS', 3);

-- Celestial body types
INSERT IGNORE INTO celestial_body_type (body_type, display_name, can_have_resources, can_have_industries, can_have_stations) VALUES
('STAR', 'Star', FALSE, FALSE, FALSE),
('PLANET', 'Planet', TRUE, TRUE, TRUE),
('MOON', 'Moon', TRUE, FALSE, TRUE),
('ASTEROID', 'Asteroid', TRUE, FALSE, FALSE),
('ASTEROID_BELT', 'Asteroid Belt', TRUE, FALSE, FALSE),
('COMET', 'Comet', TRUE, FALSE, FALSE),
('GAS_GIANT', 'Gas Giant', TRUE, FALSE, FALSE),
('NEBULA', 'Nebula', TRUE, FALSE, FALSE),
('BLACK_HOLE', 'Black Hole', FALSE, FALSE, FALSE),
('WORMHOLE', 'Wormhole', FALSE, FALSE, FALSE),
('HYPERLANE', 'Hyperlane', FALSE, FALSE, FALSE);

-- Terrain types
INSERT IGNORE INTO terrain_type (terrain_code, display_name, color_hex, supports_industry) VALUES
('MOUNTAIN', 'Mountain', '#8B7355', TRUE),
('GRASSLAND', 'Grassland', '#7CFC00', TRUE),
('FLATLANDS', 'Flatlands', '#90EE90', TRUE),
('DESERT', 'Desert', '#F4A460', TRUE),
('FOREST', 'Forest', '#228B22', TRUE),
('COAST', 'Coast', '#4682B4', TRUE),
('ICE', 'Ice', '#F0F8FF', FALSE),
('SWAMP', 'Swamp', '#8FBC8F', FALSE),
('VOLCANIC', 'Volcanic', '#8B0000', FALSE),
('OCEAN', 'Ocean', '#1E90FF', FALSE);

-- Ship types
INSERT IGNORE INTO ship_type (ship_type_code, display_name, base_hull_points, component_slots, build_time_turns, m_drive_speed, j_drive_speed, base_evasion, cubic_meters, hull_module_count, module_grid_dimensions) VALUES
('FRIGATE', 'Frigate', 50, 3, 2, 10, 12, 20.00, 1000, 1, '[1,1,1]'),
('DESTROYER', 'Destroyer', 100, 5, 3, 8, 10, 15.00, 2000, 2, '[2,1,1]'),
('CRUISER', 'Cruiser', 200, 8, 5, 6, 8, 10.00, 8000, 8, '[2,2,2]'),
('BATTLESHIP', 'Battleship', 400, 12, 8, 4, 5, 5.00, 27000, 27, '[3,3,3]'),
('CARRIER', 'Carrier', 300, 6, 7, 5, 6, 8.00, 12000, 12, '[3,2,2]'),
('MOTHERSHIP', 'Mothership', 800, 20, 15, 2, 3, 2.00, 64000, 64, '[4,4,4]');

-- Module types
INSERT IGNORE INTO module_type (module_code, display_name, category, power_requirement, base_cost_per_ton, is_critical) VALUES
('ENGINE', 'Engine', 'PROPULSION', 10, 50, TRUE),
('THRUSTER', 'Thruster', 'PROPULSION', 5, 30, FALSE),
('WARP_DRIVE', 'Warp Drive', 'PROPULSION', 20, 100, TRUE),
('BRIDGE', 'Bridge', 'COMMAND', 8, 40, TRUE),
('SENSORS', 'Sensors', 'COMMAND', 6, 35, FALSE),
('COMMS', 'Communications', 'COMMAND', 4, 25, FALSE),
('AI_CORE_BASIC', 'AI Core (Basic)', 'COMMAND', 12, 80, FALSE),
('AI_CORE_ADVANCED', 'AI Core (Advanced)', 'COMMAND', 18, 150, FALSE),
('AI_CORE_ELITE', 'AI Core (Elite)', 'COMMAND', 25, 300, FALSE),
('LASER', 'Laser', 'WEAPONS', 15, 60, FALSE),
('MISSILE', 'Missile Launcher', 'WEAPONS', 10, 55, FALSE),
('PLASMA_CANNON', 'Plasma Cannon', 'WEAPONS', 20, 75, FALSE),
('SHIELD_GENERATOR', 'Shield Generator', 'DEFENSE', 25, 90, FALSE),
('ARMOR', 'Armor Plating', 'DEFENSE', 0, 45, FALSE),
('POINT_DEFENSE', 'Point Defense', 'DEFENSE', 8, 40, FALSE),
('POWER_PLANT', 'Power Plant', 'SUPPORT', 0, 70, TRUE),
('HANGAR', 'Hangar Bay', 'SUPPORT', 5, 50, FALSE),
('FACTORY', 'Factory Module', 'SUPPORT', 10, 65, FALSE),
('MEDICAL', 'Medical Bay', 'SUPPORT', 6, 35, FALSE);

-- Weapon types (space combat)
INSERT IGNORE INTO weapon_type (weapon_code, display_name, strategic_resource, base_damage, range_tier, range_description) VALUES
('BEAM', 'Beam Weapon', 'ELERIUM', 10, 1, 'SHORT'),
('MISSILE', 'Missile', 'ANTI_MATTER', 15, 3, 'LONG'),
('MASS_DRIVER', 'Mass Driver', 'THULIUM', 12, 2, 'MEDIUM'),
('PULSION', 'Pulsion Driver', 'ELERIUM', 18, 1, 'SHORT'),
('PHOTON', 'Photon Torpedo', 'ELERIUM', 8, 3, 'LONG'),
('TORPEDO', 'Torpedo', 'ANTI_MATTER', 25, 3, 'LONG'),
('COUNTERMEASURE', 'Countermeasure', 'THULIUM', 5, 2, 'MEDIUM');

-- Character stats (universal)
INSERT IGNORE INTO character_stat (stat_code, display_name, description, min_value, max_value) VALUES
('DEXTERITY', 'Dexterity', 'Agility and fine motor skills', 0, 100),
('TECHNICAL', 'Technical', 'Technical aptitude and knowledge', 0, 100),
('STRENGTH', 'Strength', 'Physical power and endurance', 0, 100),
('INTELLIGENCE', 'Intelligence', 'Mental acuity and reasoning', 0, 100),
('WISDOM', 'Wisdom', 'Judgment and insight', 0, 100),
('CHARISMA', 'Charisma', 'Social influence and presence', 0, 100),
('CONSTITUTION', 'Constitution', 'Health and resilience', 0, 100);

-- B5 characteristics
INSERT IGNORE INTO b5_characteristic (char_code, display_name, description) VALUES
('ST', 'Strength', 'Physical power and athletic ability'),
('DX', 'Dexterity', 'Agility, coordination, and reflexes'),
('EN', 'Endurance', 'Stamina, health, and resistance'),
('IN', 'Intelligence', 'Mental acuity and knowledge'),
('ED', 'Empathy', 'Emotional awareness and sensitivity'),
('SO', 'Social', 'Social skills and interpersonal ability');

-- FPS weapon types
INSERT IGNORE INTO fps_weapon_type (weapon_code, display_name) VALUES
('PISTOL', 'Pistol'),
('RIFLE', 'Rifle'),
('SHOTGUN', 'Shotgun'),
('MACHINE_GUN', 'Machine Gun'),
('SNIPER', 'Sniper Rifle'),
('ROCKET_LAUNCHER', 'Rocket Launcher'),
('GRENADE', 'Grenade');

-- Military unit types
INSERT IGNORE INTO military_unit_type (unit_code, display_name, game_module) VALUES
('INFANTRY', 'Infantry', 'hex'),
('MECHANIZED', 'Mechanized Infantry', 'hex'),
('TANK', 'Tank', 'hex'),
('ARTILLERY', 'Artillery', 'hex'),
('ANTI_AIR', 'Anti-Air', 'hex'),
('RECON', 'Reconnaissance', 'hex'),
('ENGINEER', 'Engineer', 'hex'),
('AIR_SUPPORT', 'Air Support', 'hex'),
('MECH', 'Mech', 'hex');

-- Military unit production costs
INSERT IGNORE INTO military_unit_cost (unit_code, cargo_type_code, required_amount) VALUES
('INFANTRY', 'STEEL', 2),
('MECHANIZED', 'STEEL', 5),
('MECHANIZED', 'REFINED_OIL', 2),
('TANK', 'STEEL', 10),
('TANK', 'REFINED_OIL', 2),
('TANK', 'MACHINERY', 3),
('ARTILLERY', 'STEEL', 8),
('ARTILLERY', 'REFINED_OIL', 2),
('ANTI_AIR', 'STEEL', 5),
('ANTI_AIR', 'ELECTRONICS', 2),
('RECON', 'STEEL', 2),
('RECON', 'REFINED_OIL', 1),
('ENGINEER', 'STEEL', 2),
('AIR_SUPPORT', 'REFINED_OIL', 5),
('AIR_SUPPORT', 'ELECTRONICS', 2),
('MECH', 'STEEL', 20),
('MECH', 'ELECTRONICS', 10),
('MECH', 'MACHINERY', 5);
