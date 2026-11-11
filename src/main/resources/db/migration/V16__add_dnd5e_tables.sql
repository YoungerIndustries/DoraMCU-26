-- D&D 5e game system tables
-- dnd5e_characters: character data with ability scores, race, class, level, HP, AC, etc.
-- dnd5e_character_skills: skill proficiencies for characters (18 D&D 5e skills)
-- dnd5e_spells: spell list with level, school, casting time, range, components, effects
-- dnd5e_spell_slots: spell slot tracking by level (0-9) per character
-- dnd5e_combats: combat encounters with initiative and round tracking
-- dnd5e_combatants: participants in a combat (characters or monsters)

CREATE TABLE dnd5e_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    race VARCHAR(50) NOT NULL,
    character_class VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    background VARCHAR(50),
    str_score INT NOT NULL DEFAULT 10,
    dex_score INT NOT NULL DEFAULT 10,
    con_score INT NOT NULL DEFAULT 10,
    int_score INT NOT NULL DEFAULT 10,
    wis_score INT NOT NULL DEFAULT 10,
    cha_score INT NOT NULL DEFAULT 10,
    hit_points INT NOT NULL DEFAULT 0,
    max_hit_points INT NOT NULL DEFAULT 0,
    armor_class INT NOT NULL DEFAULT 10,
    speed INT NOT NULL DEFAULT 30,
    hit_dice VARCHAR(50),
    proficiency_bonus INT NOT NULL DEFAULT 2,
    death_save_successes INT NOT NULL DEFAULT 0,
    death_save_failures INT NOT NULL DEFAULT 0,
    temp_hit_points INT NOT NULL DEFAULT 0,
    exhaustion_level INT NOT NULL DEFAULT 0,
    equipment TEXT,
    conditions JSON,
    experience_points INT NOT NULL DEFAULT 0,
    inspiration BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_characters_name ON dnd5e_characters(name);
CREATE INDEX idx_dnd5e_characters_class ON dnd5e_characters(character_class);
CREATE INDEX idx_dnd5e_characters_race ON dnd5e_characters(race);

CREATE TABLE dnd5e_character_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    skill_name VARCHAR(50) NOT NULL,
    proficiency_bonus INT NOT NULL DEFAULT 0,
    expertise BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (character_id) REFERENCES dnd5e_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_skills_character ON dnd5e_character_skills(character_id);
CREATE INDEX idx_dnd5e_skills_name ON dnd5e_character_skills(skill_name);

CREATE TABLE dnd5e_spells (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level INT NOT NULL DEFAULT 0,
    school VARCHAR(50) NOT NULL,
    casting_time VARCHAR(50) NOT NULL,
    range VARCHAR(50) NOT NULL,
    components VARCHAR(100) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    concentration BOOLEAN NOT NULL DEFAULT FALSE,
    ritual BOOLEAN NOT NULL DEFAULT FALSE,
    damage_type VARCHAR(50),
    damage_formula VARCHAR(100),
    healing_formula VARCHAR(100),
    description TEXT,
    classes JSON,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_dnd5e_spells_name ON dnd5e_spells(name);
CREATE INDEX idx_dnd5e_spells_level ON dnd5e_spells(level);
CREATE INDEX idx_dnd5e_spells_school ON dnd5e_spells(school);

CREATE TABLE dnd5e_spell_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    slot_level INT NOT NULL,
    total_slots INT NOT NULL DEFAULT 0,
    used_slots INT NOT NULL DEFAULT 0,
    FOREIGN KEY (character_id) REFERENCES dnd5e_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_spell_slots_character ON dnd5e_spell_slots(character_id);

CREATE TABLE dnd5e_combats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    encounter_name VARCHAR(200) NOT NULL,
    current_round INT NOT NULL DEFAULT 1,
    initiative_order JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_combats_status ON dnd5e_combats(status);

CREATE TABLE dnd5e_combatants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    combat_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    armor_class INT NOT NULL DEFAULT 10,
    current_hp INT NOT NULL DEFAULT 0,
    max_hp INT NOT NULL DEFAULT 0,
    initiative INT,
    conditions JSON,
    is_character BOOLEAN NOT NULL DEFAULT FALSE,
    turn_order INT,
    FOREIGN KEY (combat_id) REFERENCES dnd5e_combats(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_combatants_combat ON dnd5e_combatants(combat_id);
CREATE INDEX idx_dnd5e_combatants_name ON dnd5e_combatants(name);
