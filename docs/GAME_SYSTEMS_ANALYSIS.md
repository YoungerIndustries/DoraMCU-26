# DoraMCU Game Systems Analysis & Object Database Schema

## Executive Summary

DoraMCU is a massive multi-universe game platform supporting 15+ game universes (Babylon 5, Star Trek, DnD5e, Earth 2026, Traveller, Warhammer 40k, Star Wars, MechWarrior, and more) with interconnected economic, 4X, RPG, and FPS systems. This document analyzes all game systems and proposes a comprehensive object database schema.

---

## 1. Game Universe Modules

| Module | Domain | Key Systems |
|--------|--------|-------------|
| `babylon5` | Sci-fi RPG | Characters, ships, firearms, melee, psionics, trade |
| `captainslog` | Star Trek RPG | Characters, starships, missions, combat, crew |
| `dnd5e` | Fantasy RPG | Characters, classes, spells, crafting, monsters, dungeons |
| `earth2026` | Modern sci-fi | Characters, economy, organizations, properties, planets |
| `eve` | Space MMO | Corporations, treasury, AI commanders |
| `fourx` | 4X strategy | Factions, colonies, diplomacy, trade, exploration |
| `fps` | First-person shooter | Weapons, maps, matches, enemies, matchmaking |
| `hex` | Core 4X engine | Galaxy, planets, ships, resources, industries, military |
| `mechwarrior` | Mech combat | Mechs, components, weapons, pilots, missions |
| `sfb` | Star Frontiers | Ships, weapons, armor, trade, exploration |
| `star_trek_4x` | Star Trek 4X | Colonies, factions, diplomacy, trade routes |
| `starwars` | Star Wars | Factions, units, scenarios, combat |
| `starshiptroopers` | Starship Troopers | Units, combat, scenarios |
| `traveller` | Space RPG | Characters, ships, monsters |
| `warhammer40k` | Warhammer 40k | Factions, units, scenarios |
| `warcraft` | Warcraft | Units, scenarios |
| `zero_zero_seven` | James Bond 007 | Agents, empires, spy networks, covert ops |
| `thirdreich` | WWII strategy | Units, military production |
| `corporation` | Business sim | Contracts, stocks, hiring, treasury |

---

## 2. Economic System

### 2.1 Resource Hierarchy

The economic system has **3 layers** of resources:

#### Layer 1: Raw Resources (Space)
Extracted directly from celestial bodies:

| Resource | Source | Primary Use |
|----------|--------|-------------|
| `ORE` | Planets, Asteroids | Metal alloy production |
| `CRYSTAL` | Comets, Planets | Power cell, fuel production |
| `GAS` | Gas Giants | Fuel production |
| `DUST` | Nebulae, Asteroid Belts | Composite material production |
| `BIO_MASS` | Habitable Planets | Nutrient production |

#### Layer 2: Refined Resources (Space)
Produced by processing raw resources:

| Resource | Recipe (Standard) | Recipe (Alternative) | Primary Use |
|----------|-------------------|---------------------|-------------|
| `METAL_ALLOY` | 2 ORE + 1 CRYSTAL (2 turns) | 3 ORE (3 turns) | Hull, weapon modules |
| `POWER_CELL` | 2 CRYSTAL + 1 ORE (2 turns) | 1 CRYSTAL + 1 GAS + 1 ORE (3 turns) | Weapons, shields, engines |
| `FUEL` | 2 GAS + 1 CRYSTAL (1 turn) | 3 GAS (2 turns) | Engine modules |
| `COMPOSITE` | 2 DUST + 1 ORE + 1 CRYSTAL (3 turns) | 3 DUST + 1 BIO_MASS (3 turns) | Hull, shield modules |
| `NUTRIENT` | 2 BIO_MASS + 1 CRYSTAL (1 turn) | 3 BIO_MASS (2 turns) | Facility modules |

#### Layer 3: Strategic Resources (Space)
Rare deposits that gate advanced capabilities:

| Resource | Use |
|----------|-----|
| `ELERIUM` | Beam weapons, shields |
| `ANTI_MATTER` | Missile weapons |
| `THULIUM` | Mass driver weapons |
| `DURANTIUM` | Elite manufacturing |
| `PROMETHION` | Elite research |

#### Layer 4: Cargo Types (Planet Surface)
25 cargo types across 4 categories (OpenTTD-inspired):

**Raw Materials (Primary Industries):**
- `ORE`, `COAL`, `OIL`, `WOOD`, `GRAIN`, `LIVESTOCK`, `FISH`, `FRUIT`, `MAIL`, `PASSENGERS`

**Processed Materials (Secondary Industries):**
- `STEEL`, `CONCRETE`, `GLASS`, `LUMBER`, `BREAD`, `MEAT`, `REFINED_OIL`, `PLASTIC`

**Advanced Goods (Tertiary Industries):**
- `ELECTRONICS`, `MACHINERY`, `LUXURY`, `CHEMICALS`

**Space Cargo (Apollo Rocket Set):**
- `ROCKET_PARTS`, `SPACECRAFT`, `LANDERS`

### 2.2 Production Chain

```
Celestial Bodies → Raw Resources → Processing → Refined Resources → Modules → Ships
     ↓
Planetary Surface → Primary Industries → Secondary → Tertiary → Military Units
```

### 2.3 Industry Types (21 total)

**Primary (8 types):**
- `MINE` → ORE (10/turn)
- `COAL_MINE` → COAL (10/turn)
- `OIL_WELL` → OIL (8/turn)
- `LOGGING_CAMP` → WOOD (12/turn)
- `FARM` → GRAIN (10/turn)
- `RANCH` → LIVESTOCK (6/turn)
- `FISHING_PORT` → FISH (8/turn)
- `ORCHARD` → FRUIT (8/turn)

**Secondary (8 types):**
- `STEEL_MILL`: ORE(8) + COAL(4) → STEEL(6)
- `CEMENT_PLANT`: ORE(6) → CONCRETE(8)
- `GLASS_FACTORY`: CONCRETE(4) + COAL(2) → GLASS(6)
- `SAWMILL`: WOOD(10) → LUMBER(8)
- `BAKERY`: GRAIN(8) → BREAD(10)
- `SLAUGHTERHOUSE`: LIVESTOCK(8) → MEAT(6)
- `REFINERY`: OIL(8) → REFINED_OIL(6)
- `CHEMICAL_PLANT`: REFINED_OIL(6) → PLASTIC(5)

**Tertiary (5 types):**
- `ELECTRONICS_FACTORY`: STEEL(4) + GLASS(3) + PLASTIC(2) → ELECTRONICS(3)
- `MACHINE_SHOP`: STEEL(6) + REFINED_OIL(2) → MACHINERY(3)
- `LUXURY_FACTORY`: FRUIT(4) + BREAD(2) + PLASTIC(2) → LUXURY(2)
- `ADVANCED_CHEMICALS`: PLASTIC(4) + REFINED_OIL(3) → CHEMICALS(3)

**Space Industries (4 types):**
- `ROCKET_PARTS_FACTORY`: STEEL(6) + ELECTRONICS(4) → ROCKET_PARTS(2)
- `SPACECRAFT_ASSEMBLY`: ROCKET_PARTS(4) + ELECTRONICS(6) → SPACECRAFT(1)
- `LANDER_FACTORY`: ROCKET_PARTS(3) + MACHINERY(2) → LANDERS(2)
- `ROCKET_LAUNCH_PAD`: SPACECRAFT(1) + LANDERS(1) → launch

### 2.4 Military Production Chain

```
Mines → STEEL, OIL, COAL → Factories → ELECTRONICS, MACHINERY → Military Bases → Units
```

| Unit Type | Steel | Refined Oil | Electronics | Machinery |
|-----------|-------|-------------|-------------|-----------|
| INFANTRY | 2 | - | - | - |
| MECHANIZED | 5 | 2 | - | - |
| TANK | 10 | 2 | - | 3 |
| ARTILLERY | 8 | 2 | - | - |
| ANTI_AIR | 5 | - | 2 | - |
| RECON | 2 | 1 | - | - |
| ENGINEER | 2 | - | - | - |
| AIR_SUPPORT | - | 5 | 2 | - |
| MECH | 20 | - | 10 | 5 |

### 2.5 Market System

- `MarketBoard` - Planet-level trading
- `ResourceManager` - Resource tracking
- `ResourcePipelineManager` - Resource flow between stations
- `B5MarketConditions` / `SfbMarketConditions` - Universe-specific market conditions
- `StockMarketController` - Corporate stock trading

---

## 3. 4X System

### 3.1 Galaxy Structure

```
Galaxy → Sector → SubSector → SolarSystem → Celestial Bodies
```

**Celestial Body Types:**
- `Star` (sun)
- `Planet` (with terrain, industries, settlements)
- `Moon` (orbiting planets)
- `Asteroid` / `AsteroidBelt` (mining)
- `Comet` (crystal harvesting)
- `GasGiant` (gas collection)
- `Nebula` (dust gathering)
- `BlackHole` (hazard)
- `Wormhole` (travel)
- `Hyperlane` (FTL travel)
- `SpaceStation` (trading, repair)

### 3.2 Faction System

Each faction tracks:
- Reputation map (-100 to 100 with other factions)
- Controlled sectors
- Fleet strength
- Colony count
- Research points
- Economy output per turn
- Combat doctrine (OFFENSIVE, DEFENSIVE, BALANCED, GUERRILLA)
- Diplomatic stance (HOSTILE, NEUTRAL, FRIENDLY, ALLIED)

### 3.3 Colony System

- Colonies on planets with production capabilities
- Trade routes between colonies
- Diplomatic relations between factions

### 3.4 Empire System

- `Empire` - Top-level entity controlling sectors, fleets, armies
- `EmpireNavy` - Space fleet management
- `EmpireArmy` - Ground force management
- `EmpireFacilities` - Infrastructure management

---

## 4. RPG System

### 4.1 Universal Character (SharedCharacter)

7 universal attributes:
- `Dexterity`, `Technical`, `Strength`, `Intelligence`, `Wisdom`, `Charisma`, `Constitution`

System-specific references for all game modules:
- `clCharacterId` (Captain's Log)
- `travellerCharacterId` (Traveller)
- `doramcu4xPlayerId` (4X)
- `earth2026CharacterId` (Earth 2026)
- `sfbCharacterId` (Star Frontiers)
- `b5CharacterId` (Babylon 5)
- `dnd5eCharacterId` (DnD 5e)
- `starTrek4xPlayerId` (Star Trek 4X)
- `bondAgentId` (007)
- `thirdReichUnitId` (Third Reich)

### 4.2 Babylon 5 Character System

6 characteristics (DoraMCU 2E):
- `ST` (Strength), `DX` (Dexterity), `EN` (Endurance)
- `IN` (Intelligence), `ED` (Empathy), `SO` (Social)

Plus: personality, archetype, skills, relationships, story arcs

### 4.3 DnD 5e System

- Characters with classes, subclasses, backgrounds, feats
- Spells and spell slots
- Inventory items and magic items
- Crafting recipes, projects, and tools
- Monsters with abilities
- Dungeons with rooms
- Campaigns and sessions

### 4.4 Equipment Systems

**Babylon 5:**
- `B5Firearm` - Firearms with damage, range, ammo
- `B5MeleeWeapon` - Melee weapons
- `B5Armor` - Protective gear
- `B5PsionicAbility` / `B5PsionicDiscipline` - Psionic powers

**Captain's Log:**
- `CaptainLogEquipment` - Star Trek equipment
- `CaptainLogStarship` - Starship designs

**MechWarrior:**
- `MechChassis` - Mech frames
- `MechComponent` - Mech parts
- `MechWeapon` - Mech-mounted weapons

**SFB (Star Frontiers):**
- `SfbFirearm`, `SfbMeleeWeapon`, `SfbArmor`, `SfbShield`
- `SfbSeekingWeapon`, `SfbWeaponBank`

### 4.5 Skill Systems

- `B5Skill` - Babylon 5 skills
- `SharedCharacterSkill` - Universal skills
- `Dnd5eCharacterSkill` - DnD skills
- `SfbSkill` - Star Frontiers skills
- `HireSkill` - Corporate hiring skills

---

## 5. FPS System

### 5.1 Weapon Types

7 weapon types with damage, fire rate, reload, ammo, recoil, range:
- `PISTOL`, `RIFLE`, `SHOTGUN`, `MACHINE_GUN`, `SNIPER`, `ROCKET_LAUNCHER`, `GRENADE`

### 5.2 Core Components

- `FpsMap` - Game maps
- `FpsMatch` - Match management
- `FpsEnemy` - Enemy AI
- `FpsPlayerState` - Player state
- `FpsSession` - Game sessions

### 5.3 Integration Bridges

- `FpsRpgBridge` - Links FPS to RPG character progression
- `FpsEconomyBridge` - Links FPS to economic system
- `FpsMilitaryBridge` - Links FPS to military production
- `FpsQuestIntegration` - Links FPS to quest system

---

## 6. Ship System

### 6.1 Ship Types (6 types)

| Type | Hull | Slots | Build Time | MDrive | JDrive | Evasion | Cubic Meters | Modules |
|------|------|-------|------------|--------|--------|---------|--------------|---------|
| FRIGATE | 50 | 3 | 2 turns | 10 | 12 | 20% | 1,000 | 1 |
| DESTROYER | 100 | 5 | 3 turns | 8 | 10 | 15% | 2,000 | 2 |
| CRUISER | 200 | 8 | 5 turns | 6 | 8 | 10% | 8,000 | 8 |
| BATTLESHIP | 400 | 12 | 8 turns | 4 | 5 | 5% | 27,000 | 27 |
| CARRIER | 300 | 6 | 7 turns | 5 | 6 | 8% | 12,000 | 12 |
| MOTHERSHIP | 800 | 20 | 15 turns | 2 | 3 | 2% | 64,000 | 64 |

### 6.2 Module Types (17 types)

**Propulsion:** `ENGINE`, `THRUSTER`, `WARP_DRIVE`
**Command:** `BRIDGE`, `SENSORS`, `COMMS`, `AI_CORE_BASIC`, `AI_CORE_ADVANCED`, `AI_CORE_ELITE`
**Weapons:** `LASER`, `MISSILE`, `PLASMA_CANNON`
**Defense:** `SHIELD_GENERATOR`, `ARMOR`, `POINT_DEFENSE`
**Support:** `POWER_PLANT`, `HANGAR`, `FACTORY`, `MEDICAL`

### 6.3 Weapon Types (7 types)

| Weapon | Strategic Resource | Damage | Range |
|--------|-------------------|--------|-------|
| BEAM | Elerium | 10 | Short |
| MISSILE | Anti-Matter | 15 | Long |
| MASS_DRIVER | Thulium | 12 | Medium |
| PULSION | Elerium | 18 | Short |
| PHOTON | Elerium | 8 | Long |
| TORPEDO | Anti-Matter | 25 | Long |
| COUNTERMEASURE | Thulium | 5 | Medium |

### 6.4 3D Hull System

- Each ship has hull modules based on cubic meters
- Each hull module is a 10×10×10 grid (1,000 cells)
- Modules arranged in 3D grid (e.g., 8 modules = 2×2×2)
- Components installed in specific hull module positions
- Damage tracking per compartment

---

## 7. Visual Customization System

- `SkinEntity` - Visual skins for objects
- `SkinVariantEntity` - Skin variants
- `DecalEntity` - Decals/stickers
- `FactionSkinThemeEntity` - Faction-specific themes

---

## 8. Proposed Object Database Schema

### 8.1 Core Design Principles

1. **Universal Object Registry** - Every renderable object has a unique ID and type
2. **Hierarchical Relationships** - Objects reference parent/child relationships
3. **Resource Dependencies** - All objects track resource costs and production chains
4. **Cross-System References** - Objects can reference equivalents in other game systems
5. **Visual Rendering Data** - All objects include 3D model, texture, and skin references

### 8.2 Database Schema

```sql
-- ============================================
-- UNIVERSAL OBJECT REGISTRY
-- ============================================

-- Core object table - every renderable object
CREATE TABLE game_object (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    object_uuid VARCHAR(36) UNIQUE NOT NULL,
    object_type VARCHAR(50) NOT NULL,  -- 'PLANET', 'SHIP', 'WEAPON', 'CHARACTER', etc.
    object_subtype VARCHAR(50),         -- 'FRIGATE', 'BATTLESHIP', 'PISTOL', etc.
    name VARCHAR(255) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),            -- 'babylon5', 'dnd5e', 'hex', etc.
    
    -- 3D rendering data
    model_3d_path VARCHAR(500),         -- Path to 3D model file
    texture_path VARCHAR(500),          -- Path to texture file
    skin_id BIGINT,                     -- Reference to skin
    decal_ids JSON,                     -- Array of decal references
    scale_factor DECIMAL(5,2) DEFAULT 1.0,
    color_hex VARCHAR(7),               -- Base color override
    
    -- Resource costs for production
    resource_costs JSON,                -- Raw resource costs
    refined_resource_costs JSON,        -- Refined resource costs
    strategic_resource_requirements JSON, -- Strategic resources needed
    cargo_costs JSON,                   -- Planet cargo costs
    construction_cost INT,
    maintenance_cost INT,
    build_time_turns INT,
    
    -- Production chain metadata
    production_level VARCHAR(20),       -- 'PRIMARY', 'SECONDARY', 'TERTIARY'
    requires_industry VARCHAR(50),      -- Industry type needed to produce
    produced_by_industry VARCHAR(50),   -- Industry type that produces this
    
    -- Cross-system references
    equivalent_objects JSON,            -- Objects in other game systems
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_object_type (object_type),
    INDEX idx_object_subtype (object_subtype),
    INDEX idx_game_module (game_module)
);

-- ============================================
-- RESOURCE SYSTEM
-- ============================================

-- Raw resources (space)
CREATE TABLE raw_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(50) UNIQUE NOT NULL,  -- 'ORE', 'CRYSTAL', 'GAS', 'DUST', 'BIO_MASS'
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    color_hex VARCHAR(7),
    celestial_sources JSON,  -- Types of celestial bodies that produce this
    
    INDEX idx_resource_type (resource_type)
);

-- Refined resources (space)
CREATE TABLE refined_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(50) UNIQUE NOT NULL,  -- 'METAL_ALLOY', 'POWER_CELL', etc.
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    color_hex VARCHAR(7),
    primary_use TEXT,
    
    INDEX idx_resource_type (resource_type)
);

-- Processing recipes
CREATE TABLE processing_recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    output_resource VARCHAR(50) NOT NULL,
    output_amount INT NOT NULL,
    recipe_variant VARCHAR(20) DEFAULT 'STANDARD',  -- 'STANDARD' or 'ALTERNATIVE'
    processing_time_turns INT NOT NULL,
    
    FOREIGN KEY (output_resource) REFERENCES refined_resource(resource_type)
);

-- Recipe inputs (many-to-many)
CREATE TABLE processing_recipe_input (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    raw_resource_type VARCHAR(50) NOT NULL,
    required_amount INT NOT NULL,
    
    FOREIGN KEY (recipe_id) REFERENCES processing_recipe(id),
    FOREIGN KEY (raw_resource_type) REFERENCES raw_resource(resource_type)
);

-- Strategic resources
CREATE TABLE strategic_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(50) UNIQUE NOT NULL,  -- 'ELERIUM', 'ANTI_MATTER', etc.
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    gates_capabilities TEXT,  -- What this resource enables
    
    INDEX idx_resource_type (resource_type)
);

-- Cargo types (planet surface)
CREATE TABLE cargo_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cargo_type_code VARCHAR(50) UNIQUE NOT NULL,  -- 'STEEL', 'OIL', etc.
    display_name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,  -- 'RAW', 'PROCESSED', 'ADVANCED', 'MAIL'
    color_hex VARCHAR(7) NOT NULL,
    is_perishable BOOLEAN DEFAULT FALSE,
    base_value INT NOT NULL,
    
    INDEX idx_category (category)
);

-- ============================================
-- INDUSTRY SYSTEM
-- ============================================

-- Industry types
CREATE TABLE industry_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    industry_code VARCHAR(50) UNIQUE NOT NULL,  -- 'MINE', 'STEEL_MILL', etc.
    display_name VARCHAR(100) NOT NULL,
    production_level VARCHAR(20) NOT NULL,  -- 'PRIMARY', 'SECONDARY', 'TERTIARY'
    construction_cost INT NOT NULL,
    maintenance_cost INT NOT NULL,
    required_terrain JSON,  -- Array of terrain types
    
    INDEX idx_production_level (production_level)
);

-- Industry outputs
CREATE TABLE industry_output (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    industry_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    amount_per_turn INT NOT NULL,
    
    FOREIGN KEY (industry_code) REFERENCES industry_type(industry_code),
    FOREIGN KEY (cargo_type_code) REFERENCES cargo_type(cargo_type_code)
);

-- Industry inputs
CREATE TABLE industry_input (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    industry_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    amount_per_turn INT NOT NULL,
    
    FOREIGN KEY (industry_code) REFERENCES industry_type(industry_code),
    FOREIGN KEY (cargo_type_code) REFERENCES cargo_type(cargo_type_code)
);

-- ============================================
-- GALAXY SYSTEM
-- ============================================

-- Celestial body types
CREATE TABLE celestial_body_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    body_type VARCHAR(50) UNIQUE NOT NULL,  -- 'STAR', 'PLANET', 'MOON', etc.
    display_name VARCHAR(100) NOT NULL,
    can_have_resources BOOLEAN DEFAULT FALSE,
    can_have_industries BOOLEAN DEFAULT FALSE,
    can_have_stations BOOLEAN DEFAULT FALSE,
    
    INDEX idx_body_type (body_type)
);

-- Terrain types
CREATE TABLE terrain_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terrain_code VARCHAR(50) UNIQUE NOT NULL,  -- 'MOUNTAIN', 'GRASSLAND', etc.
    display_name VARCHAR(100) NOT NULL,
    color_hex VARCHAR(7),
    supports_industry BOOLEAN DEFAULT FALSE,
    
    INDEX idx_terrain_code (terrain_code)
);

-- ============================================
-- SHIP SYSTEM
-- ============================================

-- Ship types
CREATE TABLE ship_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ship_type_code VARCHAR(50) UNIQUE NOT NULL,  -- 'FRIGATE', 'DESTROYER', etc.
    display_name VARCHAR(100) NOT NULL,
    base_hull_points INT NOT NULL,
    component_slots INT NOT NULL,
    build_time_turns INT NOT NULL,
    m_drive_speed INT NOT NULL,
    j_drive_speed INT NOT NULL,
    base_evasion DECIMAL(3,2) NOT NULL,
    cubic_meters INT NOT NULL,
    hull_module_count INT NOT NULL,
    module_grid_dimensions JSON,  -- [x, y, z]
    
    INDEX idx_ship_type_code (ship_type_code)
);

-- Module types
CREATE TABLE module_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_code VARCHAR(50) UNIQUE NOT NULL,  -- 'ENGINE', 'LASER', etc.
    display_name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,  -- 'PROPULSION', 'COMMAND', 'WEAPONS', 'DEFENSE', 'SUPPORT'
    power_requirement INT NOT NULL,
    base_cost_per_ton INT NOT NULL,
    is_critical BOOLEAN DEFAULT FALSE,
    
    INDEX idx_category (category)
);

-- Weapon types (space combat)
CREATE TABLE weapon_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weapon_code VARCHAR(50) UNIQUE NOT NULL,  -- 'BEAM', 'MISSILE', etc.
    display_name VARCHAR(100) NOT NULL,
    strategic_resource VARCHAR(50),
    base_damage INT NOT NULL,
    range_tier INT NOT NULL,  -- 1=short, 2=medium, 3=long
    range_description VARCHAR(20),
    
    FOREIGN KEY (strategic_resource) REFERENCES strategic_resource(resource_type),
    INDEX idx_weapon_code (weapon_code)
);

-- ============================================
-- RPG SYSTEM
-- ============================================

-- Character characteristics (universal)
CREATE TABLE character_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_code VARCHAR(20) UNIQUE NOT NULL,  -- 'STRENGTH', 'DEXTERITY', etc.
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    min_value INT DEFAULT 0,
    max_value INT DEFAULT 100,
    
    INDEX idx_stat_code (stat_code)
);

-- B5-specific characteristics
CREATE TABLE b5_characteristic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    char_code VARCHAR(10) UNIQUE NOT NULL,  -- 'ST', 'DX', 'EN', 'IN', 'ED', 'SO'
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    INDEX idx_char_code (char_code)
);

-- Skills
CREATE TABLE skill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    skill_code VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),
    related_stats JSON,  -- Stats that affect this skill
    
    INDEX idx_game_module (game_module)
);

-- Species/Races
CREATE TABLE species (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    species_code VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    game_module VARCHAR(50),
    stat_modifiers JSON,  -- Stat bonuses/penalties
    
    INDEX idx_game_module (game_module)
);

-- ============================================
-- FPS SYSTEM
-- ============================================

-- FPS weapon types
CREATE TABLE fps_weapon_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weapon_code VARCHAR(50) UNIQUE NOT NULL,  -- 'PISTOL', 'RIFLE', etc.
    display_name VARCHAR(100) NOT NULL,
    
    INDEX idx_weapon_code (weapon_code)
);

-- FPS weapons
CREATE TABLE fps_weapon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weapon_uuid VARCHAR(36) UNIQUE NOT NULL,
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
    
    FOREIGN KEY (weapon_type_code) REFERENCES fps_weapon_type(weapon_code),
    INDEX idx_weapon_type (weapon_type_code)
);

-- ============================================
-- MILITARY UNIT SYSTEM
-- ============================================

-- Military unit types
CREATE TABLE military_unit_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_code VARCHAR(50) UNIQUE NOT NULL,  -- 'INFANTRY', 'TANK', 'MECH', etc.
    display_name VARCHAR(100) NOT NULL,
    game_module VARCHAR(50),
    
    INDEX idx_game_module (game_module)
);

-- Military unit production costs
CREATE TABLE military_unit_cost (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_code VARCHAR(50) NOT NULL,
    cargo_type_code VARCHAR(50) NOT NULL,
    required_amount INT NOT NULL,
    
    FOREIGN KEY (unit_code) REFERENCES military_unit_type(unit_code),
    FOREIGN KEY (cargo_type_code) REFERENCES cargo_type(cargo_type_code)
);

-- ============================================
-- VISUAL CUSTOMIZATION
-- ============================================

-- Skins
CREATE TABLE skin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    skin_code VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    texture_path VARCHAR(500),
    color_palette JSON,
    applicable_object_types JSON,  -- Types of objects this skin can be applied to
    
    INDEX idx_skin_code (skin_code)
);

-- Decals
CREATE TABLE decal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    decal_code VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    texture_path VARCHAR(500),
    decal_type VARCHAR(20),  -- 'FACTION', 'WEAR', 'CUSTOM'
    
    INDEX idx_decal_code (decal_code)
);

-- ============================================
-- PRODUCTION CHAIN TRACKER
-- ============================================

-- Complete production chain from raw resources to final products
CREATE TABLE production_chain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_name VARCHAR(100) NOT NULL,
    final_product_object_id BIGINT,
    chain_level INT NOT NULL,  -- 1=raw, 2=refined, 3=component, 4=final
    description TEXT,
    
    FOREIGN KEY (final_product_object_id) REFERENCES game_object(id)
);

-- Production chain steps
CREATE TABLE production_chain_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    input_type VARCHAR(50),  -- 'RAW_RESOURCE', 'REFINED_RESOURCE', 'CARGO', 'MODULE'
    input_code VARCHAR(50),
    input_amount INT,
    output_type VARCHAR(50),
    output_code VARCHAR(50),
    output_amount INT,
    processing_time_turns INT,
    required_industry VARCHAR(50),
    
    FOREIGN KEY (chain_id) REFERENCES production_chain(id)
);

-- ============================================
-- OBJECT RELATIONSHIPS
-- ============================================

-- Generic object relationships
CREATE TABLE object_relationship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_object_id BIGINT NOT NULL,
    target_object_id BIGINT NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,  -- 'PART_OF', 'REQUIRES', 'PRODUCES', 'USES', etc.
    metadata JSON,
    
    FOREIGN KEY (source_object_id) REFERENCES game_object(id),
    FOREIGN KEY (target_object_id) REFERENCES game_object(id),
    INDEX idx_relationship_type (relationship_type)
);
```

### 8.3 Object Type Taxonomy

```
GAME_OBJECT
├── CELESTIAL_BODY
│   ├── STAR
│   ├── PLANET
│   │   ├── TERRAIN_CELL (hex grid)
│   │   ├── INDUSTRY
│   │   ├── SETTLEMENT
│   │   └── STATION
│   ├── MOON
│   ├── ASTEROID
│   ├── ASTEROID_BELT
│   ├── COMET
│   ├── GAS_GIANT
│   ├── NEBULA
│   ├── BLACK_HOLE
│   └── WORMHOLE
│
├── SPACE_STRUCTURE
│   ├── SPACE_STATION
│   │   └── STATION_MODULE
│   ├── STARBASE
│   └── COLONY
│
├── VEHICLE
│   ├── SHIP
│   │   ├── SHIP_COMPONENT
│   │   │   ├── WEAPON_BANK
│   │   │   ├── ENGINE
│   │   │   ├── SHIELD_GENERATOR
│   │   │   └── MODULE
│   │   └── HULL_MODULE (3D grid)
│   │       └── COMPARTMENT
│   ├── SPACECRAFT
│   ├── LANDER
│   └── MECH
│   │   └── MECH_COMPONENT
│
├── GROUND_UNIT
│   ├── INFANTRY
│   ├── VEHICLE
│   │   ├── TANK
│   │   ├── MECHANIZED
│   │   └── ARTILLERY
│   └── AIR_SUPPORT
│
├── WEAPON
│   ├── SPACE_WEAPON (BEAM, MISSILE, TORPEDO, etc.)
│   ├── FIREARM (B5/SFB universe-specific)
│   ├── MELEE_WEAPON
│   ├── FPS_WEAPON (PISTOL, RIFLE, etc.)
│   └── SPELL (DnD magic)
│
├── ARMOR
│   ├── SPACE_ARMOR
│   ├── PERSONAL_ARMOR
│   └── SHIELD
│
├── CHARACTER
│   ├── SHARED_CHARACTER (universal)
│   │   └── SYSTEM_CHARACTER (game-specific)
│   │       ├── B5_CHARACTER
│   │       ├── DND5E_CHARACTER
│   │       ├── EARTH2026_CHARACTER
│   │       └── ...
│   └── NPC
│
├── RESOURCE
│   ├── RAW_RESOURCE (ORE, CRYSTAL, GAS, DUST, BIO_MASS)
│   ├── REFINED_RESOURCE (METAL_ALLOY, POWER_CELL, etc.)
│   ├── STRATEGIC_RESOURCE (ELERIUM, ANTI_MATTER, etc.)
│   └── CARGO (STEEL, OIL, ELECTRONICS, etc.)
│
├── INDUSTRY
│   ├── PRIMARY_INDUSTRY (MINE, FARM, etc.)
│   ├── SECONDARY_INDUSTRY (STEEL_MILL, REFINERY, etc.)
│   └── TERTIARY_INDUSTRY (ELECTRONICS, SPACECRAFT, etc.)
│
├── FACILITY
│   ├── PROCESSING_PLANT
│   ├── SHIPYARD
│   ├── MILITARY_BASE
│   └── RESEARCH_LAB
│
├── EQUIPMENT
│   ├── PSIONIC_ABILITY
│   ├── MAGIC_ITEM
│   ├── CRAFTING_TOOL
│   └── SPECIAL_EQUIPMENT
│
├── FACTION
│   ├── EMPIRE
│   ├── CORPORATION
│   └── ORGANIZATION
│
├── MAP
│   ├── GALAXY_MAP
│   ├── PLANET_SURFACE (hex grid)
│   ├── FPS_MAP
│   └── DUNGEON
│   └── DUNGEON_ROOM
│
└── VISUAL
    ├── SKIN
    ├── DECAL
    └── FACTION_THEME
```

### 8.4 Production Chain Examples

**Example 1: Raw Ore → Battleship**
```
ORE (raw) → METAL_ALLOY (refined) → HULL_MODULE (component) → BATTLESHIP (final)
CRYSTAL (raw) → POWER_CELL (refined) → WEAPON_MODULE (component) → BATTLESHIP (final)
GAS (raw) → FUEL (refined) → ENGINE_MODULE (component) → BATTLESHIP (final)
ELERIUM (strategic) → BEAM_WEAPON → BATTLESHIP (final)
```

**Example 2: Ore → Tank**
```
ORE (cargo) + COAL (cargo) → STEEL (cargo) → TANK (military unit)
OIL (cargo) → REFINED_OIL (cargo) → TANK (military unit)
STEEL (cargo) + GLASS (cargo) + PLASTIC (cargo) → ELECTRONICS (cargo) → MECH (military unit)
```

**Example 3: Raw Materials → Spacecraft**
```
ORE + COAL → STEEL
OIL → REFINED_OIL
STEEL + REFINED_OIL → MACHINERY
STEEL + GLASS + PLASTIC → ELECTRONICS
STEEL + ELECTRONICS → ROCKET_PARTS
ROCKET_PARTS + ELECTRONICS → SPACECRAFT
```

---

## 9. Key Connections Across Systems

### 9.1 Economic → Military
- Raw resources → Refined resources → Modules → Ships
- Cargo → Industries → Military units
- Strategic resources gate weapon types

### 9.2 4X → FPS
- Empire controls sectors → FPS maps are in those sectors
- Military production → FPS weapons and equipment
- Character progression → FPS player stats

### 9.3 RPG → Economic
- Character skills affect industry efficiency
- Crafting systems use same resource chains
- Corporate positions provide income for equipment

### 9.4 Cross-System Characters
- SharedCharacter references all game-specific characters
- Travel logs track character movement between systems
- Universal stats map to system-specific stats

---

## 10. Summary Statistics

| Category | Count | Examples |
|----------|-------|----------|
| Game Universe Modules | 18+ | B5, DnD5e, Earth2026, Traveller, etc. |
| Raw Resources (Space) | 5 | ORE, CRYSTAL, GAS, DUST, BIO_MASS |
| Refined Resources (Space) | 5 | METAL_ALLOY, POWER_CELL, FUEL, COMPOSITE, NUTRIENT |
| Strategic Resources | 5 | ELERIUM, ANTI_MATTER, THULIUM, DURANTIUM, PROMETHION |
| Cargo Types | 25 | STEEL, OIL, ELECTRONICS, SPACECRAFT, etc. |
| Industry Types | 21 | MINE, STEEL_MILL, ELECTRONICS_FACTORY, etc. |
| Processing Recipes | 10 | 5 standard + 5 alternative |
| Ship Types | 6 | FRIGATE through MOTHERSHIP |
| Module Types | 17 | Engine, Laser, Shield, etc. |
| Space Weapon Types | 7 | BEAM, MISSILE, TORPEDO, etc. |
| FPS Weapon Types | 7 | PISTOL, RIFLE, SNIPER, etc. |
| Military Unit Types | 9 | INFANTRY, TANK, MECH, etc. |
| Celestial Body Types | 11 | STAR, PLANET, MOON, etc. |
| Character Stat Systems | 2 | 7 universal + 6 B5-specific |
| Terrain Types | 10+ | MOUNTAIN, GRASSLAND, DESERT, etc. |
