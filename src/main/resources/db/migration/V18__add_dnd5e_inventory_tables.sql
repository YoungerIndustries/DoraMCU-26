-- D&D 5e inventory management tables
-- dnd5e_inventory_items: structured inventory items linked to characters
-- Replaces the plain TEXT equipment field with a proper relational model

CREATE TABLE dnd5e_inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    item_type VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) DEFAULT 'Common',
    quantity INT NOT NULL DEFAULT 1,
    weight DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    value INT NOT NULL DEFAULT 0,
    equipped BOOLEAN NOT NULL DEFAULT FALSE,
    attuned BOOLEAN NOT NULL DEFAULT FALSE,
    identified BOOLEAN NOT NULL DEFAULT TRUE,
    charges INT,
    max_charges INT,
    property_ac INT,
    property_damage VARCHAR(50),
    property_range VARCHAR(50),
    property_healing VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES dnd5e_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_inventory_character ON dnd5e_inventory_items(character_id);
CREATE INDEX idx_dnd5e_inventory_type ON dnd5e_inventory_items(item_type);
CREATE INDEX idx_dnd5e_inventory_equipped ON dnd5e_inventory_items(character_id, equipped);
