-- V43: Add extended D&D 5e tables for feats, exhaustion, heroic inspiration, and crafting
-- Based on the 2024 PHB systems

-- Feats: optional benefits characters can select instead of ability score improvements
CREATE TABLE IF NOT EXISTS dnd5e_feats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    ability_requirement VARCHAR(20),
    ability_score_required INT NOT NULL DEFAULT 0,
    prerequisite VARCHAR(100),
    repeatable BOOLEAN NOT NULL DEFAULT FALSE,
    ability_increase INT NOT NULL DEFAULT 0,
    ability_increased VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_feat_name ON dnd5e_feats(name);
CREATE INDEX idx_dnd5e_feat_category ON dnd5e_feats(category);

-- Character-to-feat linkage: tracks which feats a character has acquired
CREATE TABLE IF NOT EXISTS dnd5e_character_feats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(100) NOT NULL,
    feat_name VARCHAR(100) NOT NULL,
    level_acquired INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_character_feat_character ON dnd5e_character_feats(character_id);
CREATE INDEX idx_dnd5e_character_feat_name ON dnd5e_character_feats(character_id, feat_name);

-- Exhaustion: tracks exhaustion events for characters (levels 0-6)
CREATE TABLE IF NOT EXISTS dnd5e_exhaustion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(100) NOT NULL,
    level_change INT NOT NULL,
    resulting_level INT NOT NULL,
    source VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_exhaustion_character ON dnd5e_exhaustion(character_id);
CREATE INDEX idx_dnd5e_exhaustion_created ON dnd5e_exhaustion(created_at);

-- Heroic Inspiration: replaces old Inspiration, allows die rerolls
CREATE TABLE IF NOT EXISTS dnd5e_heroic_inspiration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(100) NOT NULL,
    points_available INT NOT NULL DEFAULT 0,
    max_points INT NOT NULL DEFAULT 1,
    source VARCHAR(100) NOT NULL,
    last_awarded_at TIMESTAMP NULL,
    last_reroll_description TEXT,
    last_original_roll INT NULL,
    last_reroll_value INT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_heroic_inspiration_character ON dnd5e_heroic_inspiration(character_id);
CREATE INDEX idx_dnd5e_heroic_inspiration_used ON dnd5e_heroic_inspiration(is_used);

-- Crafting tools: defines tool proficiency and associated abilities
CREATE TABLE IF NOT EXISTS dnd5e_crafting_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    associated_ability VARCHAR(20) NOT NULL,
    tool_type VARCHAR(50) NOT NULL,
    craftable_items TEXT,
    combat_use TEXT,
    exploration_use TEXT,
    use_action TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_crafting_tool_name ON dnd5e_crafting_tools(name);
CREATE INDEX idx_dnd5e_crafting_tool_ability ON dnd5e_crafting_tools(associated_ability);

-- Crafting recipes: defines what can be crafted, requirements, and results
CREATE TABLE IF NOT EXISTS dnd5e_crafting_recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) NOT NULL DEFAULT 'Common',
    required_tool VARCHAR(100) NOT NULL,
    required_ability VARCHAR(20) NOT NULL,
    dc INT NOT NULL,
    gold_cost INT NOT NULL,
    hours_required INT NOT NULL,
    material_components TEXT,
    result_item_name VARCHAR(200),
    result_item_type VARCHAR(50),
    result_quantity INT NOT NULL DEFAULT 1,
    is_magic_item BOOLEAN NOT NULL DEFAULT FALSE,
    prerequisites TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_crafting_recipe_category ON dnd5e_crafting_recipes(category);
CREATE INDEX idx_dnd5e_crafting_recipe_tool ON dnd5e_crafting_recipes(required_tool);
CREATE INDEX idx_dnd5e_crafting_recipe_rarity ON dnd5e_crafting_recipes(rarity);

-- Crafting projects: tracks active crafting progress
CREATE TABLE IF NOT EXISTS dnd5e_crafting_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    hours_worked INT NOT NULL DEFAULT 0,
    hours_required INT NOT NULL,
    check_rolls TEXT,
    last_check_result VARCHAR(50),
    gold_spent INT NOT NULL DEFAULT 0,
    gold_required INT NOT NULL,
    is_group_craft BOOLEAN NOT NULL DEFAULT FALSE,
    group_members TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_crafting_project_character ON dnd5e_crafting_projects(character_id);
CREATE INDEX idx_dnd5e_crafting_project_recipe ON dnd5e_crafting_projects(recipe_id);
CREATE INDEX idx_dnd5e_crafting_project_status ON dnd5e_crafting_projects(status);

-- Psions: psionic character abilities (power points instead of spell slots)
CREATE TABLE IF NOT EXISTS dnd5e_psions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    character_name VARCHAR(100) NOT NULL,
    subclass VARCHAR(50) NOT NULL,
    psion_level INT NOT NULL DEFAULT 1,
    power_points INT NOT NULL DEFAULT 0,
    max_power_points INT NOT NULL DEFAULT 0,
    surge_points INT NOT NULL DEFAULT 0,
    max_surge_points INT NOT NULL DEFAULT 0,
    primary_ability VARCHAR(10) NOT NULL DEFAULT 'INT',
    known_powers INT NOT NULL DEFAULT 0,
    is_surge_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_psion_character ON dnd5e_psions(character_id);
CREATE INDEX idx_dnd5e_psion_subclass ON dnd5e_psions(subclass);

-- Psionic powers: psion's version of spells, fueled by power points
CREATE TABLE IF NOT EXISTS dnd5e_psionic_powers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    power_level INT NOT NULL,
    power_point_cost INT NOT NULL,
    casting_time VARCHAR(50) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    range VARCHAR(50) NOT NULL,
    concentration BOOLEAN NOT NULL DEFAULT FALSE,
    ritual BOOLEAN NOT NULL DEFAULT FALSE,
    subclass VARCHAR(50) NOT NULL,
    school VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_dnd5e_psionic_power_name ON dnd5e_psionic_powers(name);
CREATE INDEX idx_dnd5e_psionic_power_level ON dnd5e_psionic_powers(power_level);
CREATE INDEX idx_dnd5e_psionic_power_subclass ON dnd5e_psionic_powers(subclass);

-- Character tool proficiencies: links characters to crafting tools
CREATE TABLE IF NOT EXISTS dnd5e_character_tool_proficiencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(100) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    level_acquired INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expertise BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_char_tool_prof_char ON dnd5e_character_tool_proficiencies(character_id);
CREATE INDEX idx_dnd5e_char_tool_prof_tool ON dnd5e_character_tool_proficiencies(character_id, tool_name);
