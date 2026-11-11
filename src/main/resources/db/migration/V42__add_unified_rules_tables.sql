-- V42: Create unified rules entries and tables tables
-- Replaces duplicate rules entities in traveller, captainslog, babylon5, and sfb modules

CREATE TABLE IF NOT EXISTS unified_rules_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_system VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    source VARCHAR(100) NOT NULL,
    chapter VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    page INT,
    rule_text TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_rules_game_system ON unified_rules_entries(game_system);
CREATE INDEX idx_rules_category ON unified_rules_entries(category);
CREATE INDEX idx_rules_chapter ON unified_rules_entries(chapter);
CREATE INDEX idx_rules_source ON unified_rules_entries(source);
CREATE INDEX idx_rules_title ON unified_rules_entries(title);

CREATE TABLE IF NOT EXISTS unified_rules_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_system VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    source VARCHAR(100) NOT NULL,
    chapter VARCHAR(100) NOT NULL,
    description TEXT,
    num_sides INT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_tables_game_system ON unified_rules_tables(game_system);
CREATE INDEX idx_tables_name ON unified_rules_tables(name);

-- Migrate existing Traveller rules entries to unified table
INSERT INTO unified_rules_entries (game_system, title, source, chapter, category, page, rule_text, notes, active)
SELECT 'TRAVELLER', title, source, chapter, category, page, rule_text, notes, TRUE
FROM traveller_rules_entries
ON DUPLICATE KEY UPDATE title = title;

-- Migrate existing Captain's Log rules entries to unified table
INSERT INTO unified_rules_entries (game_system, title, source, chapter, category, page, rule_text, notes, active)
SELECT 'CAPTAINS_LOG', title, source, chapter, category, page, rule_text, notes, TRUE
FROM rules_entries
WHERE source LIKE '%Captain%' OR source LIKE '%CL%'
ON DUPLICATE KEY UPDATE title = title;
