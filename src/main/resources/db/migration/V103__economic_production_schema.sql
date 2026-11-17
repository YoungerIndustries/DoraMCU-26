-- V103: Create economic production database schema
-- Adds 5 new tables for the AI admin economic production system:
-- material_costs, build_contracts, infrastructure_items, production_chain_tracking, crew_member_skills
--
-- This schema supports the full production chain from mines to space stations,
-- with modularized work contracts executable by 1 leader + 3 crew members.

-- ============================================================
-- 1. material_costs: Cost and recipe data for all materials
-- ============================================================
CREATE TABLE material_costs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_type VARCHAR(50) NOT NULL COMMENT 'RAW_RESOURCE, REFINED_RESOURCE, CONSTRUCTION_MODULE',
    material_name VARCHAR(100) NOT NULL COMMENT 'ORE, METAL_ALLOY, HULL_MODULE, etc.',
    base_value_credits DECIMAL(15,2) NOT NULL COMMENT 'Credit value per unit',
    raw_inputs_json TEXT COMMENT 'JSON: raw resource inputs for production',
    refined_inputs_json TEXT COMMENT 'JSON: refined resource inputs for production',
    processing_time_turns INT COMMENT 'Turns to process/assemble',
    alternative_recipe_json TEXT COMMENT 'JSON: fallback recipe when primary inputs scarce',
    alternative_time_turns INT COMMENT 'Turns for alternative recipe',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_costs_type (material_type),
    UNIQUE INDEX idx_material_costs_name (material_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cost and recipe data for production chain materials';

-- ============================================================
-- 2. build_contracts: Modularized work contracts (1 leader + 3 crew)
-- ============================================================
CREATE TABLE build_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_type VARCHAR(50) NOT NULL COMMENT 'MINE, PROCESS, ASSEMBLE, CONSTRUCT, REPAIR',
    target_item_type VARCHAR(100) NOT NULL COMMENT 'FACILITY, STATION_MODULE, SHIP_COMPONENT, etc.',
    target_item_name VARCHAR(200) NOT NULL COMMENT 'Name of item being built',
    description TEXT COMMENT 'Description of contract work',
    material_requirements_json TEXT NOT NULL COMMENT 'JSON: resources needed for this contract',
    leader_role_required VARCHAR(50) COMMENT 'Required leader role (Engineer, Miner, etc.)',
    leader_min_leadership INT COMMENT 'Minimum leadership rating (1-10)',
    crew_size INT DEFAULT 4 COMMENT 'Crew size (1 leader + 3 members)',
    crew_skills_required_json TEXT COMMENT 'JSON: required crew skills',
    estimated_duration_turns INT NOT NULL COMMENT 'Turns to complete',
    priority INT DEFAULT 5 COMMENT 'Priority 1-10, higher = more urgent',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, FAILED',
    assigned_crew_id BIGINT COMMENT 'Reference to crew table',
    assigned_leader_id BIGINT COMMENT 'Reference to ai_characters table (leader)',
    crew_member_1_id BIGINT COMMENT 'Reference to ai_characters table',
    crew_member_2_id BIGINT COMMENT 'Reference to ai_characters table',
    crew_member_3_id BIGINT COMMENT 'Reference to ai_characters table',
    progress_percent INT DEFAULT 0 COMMENT 'Progress 0-100',
    material_cost_total_credits DECIMAL(15,2) COMMENT 'Total material cost in credits',
    labor_cost_total_credits DECIMAL(15,2) COMMENT 'Total labor cost in credits',
    dependencies_json TEXT COMMENT 'JSON array of contract IDs that must complete first',
    start_turn INT COMMENT 'Game turn when work started',
    completion_turn INT COMMENT 'Game turn when work completes',
    galaxy_id BIGINT COMMENT 'Galaxy where contract takes place',
    planet_id BIGINT COMMENT 'Planet where contract takes place',
    station_id BIGINT COMMENT 'Space station where contract takes place',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT 'When contract was completed',
    INDEX idx_build_contracts_status (status),
    INDEX idx_build_contracts_type (contract_type),
    INDEX idx_build_contracts_crew (assigned_crew_id),
    INDEX idx_build_contracts_leader (assigned_leader_id),
    INDEX idx_build_contracts_priority (priority),
    INDEX idx_build_contracts_galaxy (galaxy_id),
    INDEX idx_build_contracts_created (created_at),
    FOREIGN KEY (assigned_crew_id) REFERENCES crew(id),
    FOREIGN KEY (assigned_leader_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_1_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_2_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_3_id) REFERENCES ai_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Build contracts for modularized production work';

-- ============================================================
-- 3. infrastructure_items: Catalog of all buildable items
-- ============================================================
CREATE TABLE infrastructure_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_type VARCHAR(50) NOT NULL COMMENT 'PLANET_FACILITY, SPACE_STATION, STATION_MODULE',
    item_name VARCHAR(200) NOT NULL COMMENT 'MINING_FACILITY, MILITARY station, etc.',
    description TEXT COMMENT 'Description of the item',
    -- Material costs
    mineral_cost INT COMMENT 'Base mineral cost',
    energy_cost INT COMMENT 'Base energy cost',
    raw_resources_json TEXT COMMENT 'JSON: raw resource requirements',
    refined_resources_json TEXT COMMENT 'JSON: refined resource requirements',
    construction_modules_json TEXT COMMENT 'JSON: construction module requirements',
    -- Labor costs
    total_labor_crew_days INT COMMENT 'Total crew-days to build',
    labor_cost_credits DECIMAL(15,2) COMMENT 'Total labor cost in credits',
    -- Production stats
    raw_production_json TEXT COMMENT 'JSON: raw resource output per turn',
    refined_production_json TEXT COMMENT 'JSON: refined resource output per turn',
    module_upkeep_json TEXT COMMENT 'JSON: module upkeep consumed per turn',
    credit_income_per_turn INT COMMENT 'Credit income per turn',
    upkeep_cost_per_turn INT COMMENT 'Upkeep cost per turn',
    -- Bonuses
    mineral_bonus INT DEFAULT 0 COMMENT 'Mineral production bonus',
    energy_bonus INT DEFAULT 0 COMMENT 'Energy production bonus',
    food_bonus INT DEFAULT 0 COMMENT 'Food production bonus',
    research_bonus INT DEFAULT 0 COMMENT 'Research bonus',
    defense_bonus INT DEFAULT 0 COMMENT 'Defense bonus',
    -- Construction
    build_time_turns INT COMMENT 'Total build time in turns',
    required_crew_groups INT COMMENT 'Parallel crew groups needed',
    contract_breakdown_json TEXT COMMENT 'JSON: sub-contracts for modularized construction',
    -- Stats
    hull_points INT COMMENT 'Hull points (stations/modules)',
    shield_points INT COMMENT 'Shield points (stations/modules)',
    module_slots INT COMMENT 'Module slots (stations)',
    docking_ports INT COMMENT 'Docking ports',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_infrastructure_items_type (item_type),
    INDEX idx_infrastructure_items_name (item_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Catalog of buildable infrastructure items';

-- ============================================================
-- 4. production_chain_tracking: Full production chain tracking
-- ============================================================
CREATE TABLE production_chain_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_name VARCHAR(200) NOT NULL COMMENT 'e.g., "Military Station - Full Chain"',
    target_item_id BIGINT COMMENT 'Reference to infrastructure_items',
    total_phases INT COMMENT 'Number of phases in the chain',
    total_contracts INT COMMENT 'Total contracts across all phases',
    total_material_cost_credits DECIMAL(15,2) COMMENT 'Aggregated material cost',
    total_labor_cost_credits DECIMAL(15,2) COMMENT 'Aggregated labor cost',
    total_duration_turns INT COMMENT 'Total duration (sequential)',
    min_crews_required INT COMMENT 'Parallel crews for optimal execution',
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED' COMMENT 'PLANNED, IN_PROGRESS, COMPLETED, FAILED',
    phase_breakdown_json TEXT COMMENT 'JSON: full production chain with all phases',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT 'When chain was completed',
    INDEX idx_production_chain_status (status),
    INDEX idx_production_chain_target (target_item_id),
    INDEX idx_production_chain_created (created_at),
    FOREIGN KEY (target_item_id) REFERENCES infrastructure_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tracks full production chains from raw materials to final product';

-- ============================================================
-- 5. crew_member_skills: AI character skills for production
-- ============================================================
CREATE TABLE crew_member_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_character_id BIGINT NOT NULL COMMENT 'Reference to ai_characters',
    skill_category VARCHAR(50) NOT NULL COMMENT 'MINING, ENGINEERING, SCIENCE, COMBAT, TRADE',
    skill_level INT NOT NULL COMMENT 'Skill level 1-10',
    experience_points INT DEFAULT 0 COMMENT 'XP for this skill',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_crew_skills_character (ai_character_id),
    INDEX idx_crew_skills_category (skill_category),
    INDEX idx_crew_skills_level (skill_level),
    FOREIGN KEY (ai_character_id) REFERENCES ai_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Skills for AI characters in the production system';

-- ============================================================
-- Seed data: Raw resources
-- ============================================================
INSERT INTO material_costs (material_type, material_name, base_value_credits, processing_time_turns) VALUES
('RAW_RESOURCE', 'ORE', 10.00, NULL),
('RAW_RESOURCE', 'CRYSTAL', 25.00, NULL),
('RAW_RESOURCE', 'GAS', 8.00, NULL),
('RAW_RESOURCE', 'DUST', 15.00, NULL),
('RAW_RESOURCE', 'BIO_MASS', 12.00, NULL);

-- ============================================================
-- Seed data: Refined resources
-- ============================================================
INSERT INTO material_costs (material_type, material_name, base_value_credits, raw_inputs_json, processing_time_turns,
                            alternative_recipe_json, alternative_time_turns) VALUES
('REFINED_RESOURCE', 'METAL_ALLOY', 45.00, '{"ORE": 2, "CRYSTAL": 1}', 2, '{"ORE": 3}', 3),
('REFINED_RESOURCE', 'POWER_CELL', 70.00, '{"CRYSTAL": 2, "ORE": 1}', 2, '{"CRYSTAL": 1, "GAS": 1, "ORE": 1}', 3),
('REFINED_RESOURCE', 'FUEL', 35.00, '{"GAS": 1, "CRYSTAL": 1}', 1, '{"GAS": 3}', 2),
('REFINED_RESOURCE', 'COMPOSITE', 55.00, '{"DUST": 1, "ORE": 1, "CRYSTAL": 1}', 2, '{"DUST": 3, "BIO_MASS": 1}', 3),
('REFINED_RESOURCE', 'NUTRIENT', 30.00, '{"BIO_MASS": 1, "CRYSTAL": 1}', 1, '{"BIO_MASS": 3}', 2);

-- ============================================================
-- Seed data: Construction modules
-- ============================================================
INSERT INTO material_costs (material_type, material_name, base_value_credits, refined_inputs_json, processing_time_turns,
                            alternative_recipe_json, alternative_time_turns) VALUES
('CONSTRUCTION_MODULE', 'HULL_MODULE', 112.50, '{"METAL_ALLOY": 2, "COMPOSITE": 1}', 2, '{"METAL_ALLOY": 3}', 3),
('CONSTRUCTION_MODULE', 'WEAPON_MODULE', 122.50, '{"METAL_ALLOY": 1, "POWER_CELL": 1}', 1, '{"METAL_ALLOY": 2, "FUEL": 1}', 2),
('CONSTRUCTION_MODULE', 'SHIELD_MODULE', 122.50, '{"POWER_CELL": 1, "COMPOSITE": 1}', 1, '{"COMPOSITE": 2, "FUEL": 1}', 2),
('CONSTRUCTION_MODULE', 'ENGINE_MODULE', 105.00, '{"FUEL": 1, "POWER_CELL": 1}', 1, '{"FUEL": 2, "METAL_ALLOY": 1}', 2),
('CONSTRUCTION_MODULE', 'FACILITY_MODULE', 135.00, '{"METAL_ALLOY": 2, "NUTRIENT": 1}', 3, '{"METAL_ALLOY": 3, "COMPOSITE": 1}', 4);

-- ============================================================
-- Seed data: Planet facilities
-- ============================================================
INSERT INTO infrastructure_items (item_type, item_name, description, mineral_cost, energy_cost,
                                  total_labor_crew_days, build_time_turns, required_crew_groups,
                                  credit_income_per_turn, upkeep_cost_per_turn,
                                  mineral_bonus, energy_bonus, food_bonus, research_bonus, defense_bonus) VALUES
('PLANET_FACILITY', 'COLONY_DOME', 'Basic colony habitat dome', 600, 300, 3, 3, 1, 1000, 500, 0, 0, 2, 0, 0),
('PLANET_FACILITY', 'SHIPYARD', 'Ship construction facility', 900, 600, 8, 5, 2, 1000, 1000, 3, 0, 0, 0, 0),
('PLANET_FACILITY', 'SENSOR_ARRAY', 'Detection and communication array', 400, 300, 4, 3, 1, 1000, 500, 0, 0, 0, 1, 0),
('PLANET_FACILITY', 'MINING_FACILITY', 'Resource extraction facility', 700, 400, 5, 4, 1, 1000, 500, 4, 0, 0, 0, 0),
('PLANET_FACILITY', 'POWER_PLANT', 'Energy generation plant', 500, 400, 4, 3, 1, 1000, 500, 0, 3, 0, 0, 0),
('PLANET_FACILITY', 'DEFENSE_TURRET', 'Planetary defense turret', 600, 400, 5, 4, 1, 1000, 500, 0, 0, 0, 0, 10),
('PLANET_FACILITY', 'RESEARCH_LAB', 'Scientific research laboratory', 500, 500, 6, 5, 1, 1000, 750, 0, 0, 0, 3, 0),
('PLANET_FACILITY', 'TRADE_PORT', 'Interstellar trade port', 500, 300, 4, 3, 1, 2000, 500, 0, 1, 0, 0, 0),
('PLANET_FACILITY', 'MILITARY_BASE', 'Military operations base', 800, 600, 8, 5, 2, 1500, 1000, 2, 0, 0, 0, 5),
('PLANET_FACILITY', 'AGRICULTURAL_CENTER', 'Food production center', 400, 300, 3, 3, 1, 1000, 500, 0, 0, 4, 0, 0),
('PLANET_FACILITY', 'WAREHOUSE', 'Resource storage warehouse', 500, 300, 4, 3, 1, 1000, 500, 0, 0, 0, 0, 0),
('PLANET_FACILITY', 'REFINERY', 'Resource processing refinery', 600, 400, 5, 4, 1, 1000, 500, 0, 0, 0, 0, 0),
('PLANET_FACILITY', 'MODULE_BAY', 'Construction module assembly bay', 500, 300, 4, 3, 1, 1000, 500, 0, 0, 0, 0, 0);

-- ============================================================
-- Seed data: Space stations
-- ============================================================
INSERT INTO infrastructure_items (item_type, item_name, description, mineral_cost, energy_cost,
                                  total_labor_crew_days, build_time_turns, required_crew_groups,
                                  credit_income_per_turn, upkeep_cost_per_turn,
                                  hull_points, shield_points, module_slots, docking_ports,
                                  raw_production_json, refined_production_json, module_upkeep_json) VALUES
('SPACE_STATION', 'MILITARY', 'Military operations space station', 1200, 620, 40, 22, 5, 5000, 5000,
 500, 100, 4, 6, NULL, NULL, '{"HULL_MODULE": 1, "SHIELD_MODULE": 1}'),
('SPACE_STATION', 'SHIPYARD', 'Ship construction space station', 1650, 840, 40, 21, 5, 5000, 5000,
 300, 50, 3, 10, NULL, '{"METAL_ALLOY": 1}', '{"ENGINE_MODULE": 1, "HULL_MODULE": 1}'),
('SPACE_STATION', 'MINING', 'Resource extraction space station', 900, 460, 25, 14, 3, 5000, 3000,
 200, 30, 2, 2, '{"ORE": 3, "GAS": 1}', NULL, '{"HULL_MODULE": 1}'),
('SPACE_STATION', 'RESEARCH', 'Scientific research space station', 1000, 510, 28, 15, 3, 5000, 4000,
 250, 40, 2, 2, NULL, '{"COMPOSITE": 1}', '{"SHIELD_MODULE": 1}'),
('SPACE_STATION', 'TRADE', 'Interstellar trade space station', 700, 360, 22, 12, 3, 10000, 4000,
 150, 20, 2, 4, NULL, '{"POWER_CELL": 2, "FUEL": 1}', '{"FACILITY_MODULE": 1}'),
('SPACE_STATION', 'COLONY', 'Colony habitat space station', 850, 440, 30, 17, 3, 5000, 3500,
 200, 30, 3, 5, '{"BIO_MASS": 2}', NULL, '{"FACILITY_MODULE": 1}');

-- ============================================================
-- Seed data: Station module templates
-- ============================================================
INSERT INTO infrastructure_items (item_type, item_name, description, hull_points, shield_points,
                                  docking_ports, build_time_turns, required_crew_groups) VALUES
('STATION_MODULE', 'COMMAND_MODULE', 'Command and control center', 100, 50, 0, 4, 1),
('STATION_MODULE', 'DOCKING_RING', 'Ship berthing ring', 50, 20, 4, 3, 1),
('STATION_MODULE', 'WEAPON_PLATFORM', 'Turret mount platform', 50, 20, 0, 5, 1),
('STATION_MODULE', 'SHIELD_ARRAY', 'Shield generator array', 20, 100, 0, 4, 1),
('STATION_MODULE', 'SENSOR_ARRAY', 'Detection and communications', 20, 20, 0, 3, 1),
('STATION_MODULE', 'CARGO_MODULE', 'Storage and transfer bay', 50, 20, 0, 3, 1),
('STATION_MODULE', 'CREW_QUARTERS', 'Living spaces for crew', 20, 20, 0, 3, 1),
('STATION_MODULE', 'REPAIR_BAY', 'Maintenance and repair bay', 50, 50, 0, 4, 1),
('STATION_MODULE', 'RESEARCH_LAB', 'Science laboratory module', 20, 20, 0, 4, 1),
('STATION_MODULE', 'CORRIDOR', 'Connecting section between modules', 20, 20, 2, 2, 1),
('STATION_MODULE', 'POWER_MODULE', 'Power generation module', 50, 20, 0, 3, 1),
('STATION_MODULE', 'LIFE_SUPPORT', 'Environmental systems module', 20, 20, 0, 3, 1);
