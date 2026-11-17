# DoraMCU Comprehensive Economic & Production Database Design

## Overview

This document defines the complete database schema and economic system for the DoraMCU game, covering:
- **Material hierarchy**: Raw resources → Refined resources → Construction modules → Final products
- **Production infrastructure**: Mines → Processing plants → Factories → Assembly bays
- **Labor system**: AI Admin leaders + AI Character crew groups (1 leader + 3 crew per contract)
- **Cost tracking**: Material costs and labor costs for every buildable item
- **Modularized construction**: Space stations and all infrastructure broken into crew-sized contracts

---

## 1. Material Database

### 1.1 Raw Resources (Extracted from Celestial Bodies)

| Resource Type | Source | Description | Base Value (credits/unit) |
|---|---|---|---|
| `ORE` | Planets, Asteroids | Basic metal ore | 10 |
| `CRYSTAL` | Comets, Certain planets | Energy crystals | 25 |
| `GAS` | Gas giants | Volatile gases | 8 |
| `DUST` | Nebulae, Asteroid belts | Cosmic dust | 15 |
| `BIO_MASS` | Habitable planets | Organic material | 12 |

### 1.2 Refined Resources (Processed from Raw)

| Refined Type | Standard Recipe | Processing Time | Base Value (credits/unit) |
|---|---|---|---|
| `METAL_ALLOY` | 2 ORE + 1 CRYSTAL | 2 turns | 45 |
| `POWER_CELL` | 2 CRYSTAL + 1 ORE | 2 turns | 70 |
| `FUEL` | 1 GAS + 1 CRYSTAL | 1 turn | 35 |
| `COMPOSITE` | 1 DUST + 1 ORE + 1 CRYSTAL | 2 turns | 55 |
| `NUTRIENT` | 1 BIO_MASS + 1 CRYSTAL | 1 turn | 30 |

**Alternative Recipes** (when CRYSTAL is scarce):
- METAL_ALLOY: 3 ORE → 3 turns, lower output
- POWER_CELL: 1 CRYSTAL + 1 GAS + 1 ORE → 3 turns
- COMPOSITE: 3 DUST + 1 BIO_MASS → 3 turns
- FUEL: 3 GAS → 2 turns
- NUTRIENT: 3 BIO_MASS → 2 turns

### 1.3 Construction Modules (Assembled from Refined)

| Module Type | Standard Recipe | Assembly Time | Alternative Recipe | Alt Time |
|---|---|---|---|---|
| `HULL_MODULE` | 2 METAL_ALLOY + 1 COMPOSITE | 2 turns | 3 METAL_ALLOY | 3 turns |
| `WEAPON_MODULE` | 1 METAL_ALLOY + 1 POWER_CELL | 1 turn | 2 METAL_ALLOY + 1 FUEL | 2 turns |
| `SHIELD_MODULE` | 1 POWER_CELL + 1 COMPOSITE | 1 turn | 2 COMPOSITE + 1 FUEL | 2 turns |
| `ENGINE_MODULE` | 1 FUEL + 1 POWER_CELL | 1 turn | 2 FUEL + 1 METAL_ALLOY | 2 turns |
| `FACILITY_MODULE` | 2 METAL_ALLOY + 1 NUTRIENT | 3 turns | 3 METAL_ALLOY + 1 COMPOSITE | 4 turns |

### 1.4 Base Materials (Structural)

| Material | Value ID | Description |
|---|---|---|
| `Empty` | 0 | No material |
| `Aluminum` | 1 | Lightweight metal |
| `Steel` | 2 | Standard structural metal |
| `Titanium` | 3 | Strong and lightweight alloy |
| `Composite` | 4 | Advanced blended material |
| `Explosive` | 5 | Volatile material |
| `Crystaliron` | 6 | Advanced crystal-based armor |
| `Superdense` | 7 | Ultra-dense bonded armor |

---

## 2. Production Infrastructure Database

### 2.1 Planet Facilities (StartingFacility)

| Facility Type | Mineral Bonus | Energy Bonus | Food Bonus | Research Bonus | Defense Bonus | Credit Income/Turn | Upkeep/Turn |
|---|---|---|---|---|---|---|---|
| `COLONY_DOME` | 0 | 0 | +2 | 0 | 0 | 1,000 | 500 |
| `SHIPYARD` | +3 | 0 | 0 | 0 | 0 | 1,000 | 1,000 |
| `SENSOR_ARRAY` | 0 | 0 | 0 | +1 | 0 | 1,000 | 500 |
| `MINING_FACILITY` | +4 | 0 | 0 | 0 | 0 | 1,000 | 500 |
| `POWER_PLANT` | 0 | +3 | 0 | 0 | 0 | 1,000 | 500 |
| `DEFENSE_TURRET` | 0 | 0 | 0 | 0 | +10 | 1,000 | 500 |
| `RESEARCH_LAB` | 0 | 0 | 0 | +3 | 0 | 1,000 | 750 |
| `TRADE_PORT` | 0 | +1 | 0 | 0 | 0 | 2,000 | 500 |
| `MILITARY_BASE` | +2 | 0 | 0 | 0 | +5 | 1,500 | 1,000 |
| `AGRICULTURAL_CENTER` | 0 | 0 | +4 | 0 | 0 | 1,000 | 500 |
| `WAREHOUSE` | 0 | 0 | 0 | 0 | 0 | 1,000 | 500 |
| `REFINERY` | 0 | 0 | 0 | 0 | 0 | 1,000 | 500 |
| `MODULE_BAY` | 0 | 0 | 0 | 0 | 0 | 1,000 | 500 |

**All bonuses scale by facility level.**

### 2.2 Space Station Types

| Station Type | Hull | Shields | Module Slots | Mineral Cost | Energy Cost | Ship Bay | Raw Production/Turn | Refined Production/Turn | Module Upkeep/Turn |
|---|---|---|---|---|---|---|---|---|---|
| `MILITARY` | 500 | 100 | 4 | 1,000 | 500 | 6 | — | — | 1 HULL + 1 SHIELD |
| `SHIPYARD` | 300 | 50 | 3 | 1,500 | 750 | 10 | — | 1 METAL_ALLOY | 1 ENGINE + 1 HULL |
| `MINING` | 200 | 30 | 2 | 800 | 400 | 2 | 3 ORE + 1 GAS | — | 1 HULL |
| `RESEARCH` | 250 | 40 | 2 | 900 | 450 | 2 | — | 1 COMPOSITE | 1 SHIELD |
| `TRADE` | 150 | 20 | 2 | 600 | 300 | 4 | — | 2 POWER_CELL + 1 FUEL | 1 FACILITY |
| `COLONY` | 200 | 30 | 3 | 700 | 350 | 5 | 2 BIO_MASS | — | 1 FACILITY |

**Total station cost = Base mineral cost + (module slots × 50) minerals + (module slots × 30) energy**

### 2.3 Space Station Module Templates (13 Types)

| Module Template | Hull Contribution | Shield Contribution | Docking Ports | Description |
|---|---|---|---|---|
| `COMMAND_MODULE` | High | Medium | 0 | Command and control center |
| `DOCKING_RING` | Medium | Low | Multiple | Ship berthing |
| `WEAPON_PLATFORM` | Medium | Low | 0 | Turret mounts |
| `SHIELD_ARRAY` | Low | High | 0 | Shield generator |
| `SENSOR_ARRAY` | Low | Low | 0 | Detection and comms |
| `CARGO_MODULE` | Medium | Low | 0 | Storage and transfer |
| `CREW_QUARTERS` | Low | Low | 0 | Living spaces |
| `REPAIR_BAY` | Medium | Medium | 0 | Maintenance |
| `RESEARCH_LAB` | Low | Low | 0 | Science laboratory |
| `CORRIDOR` | Low | Low | Multiple | Connecting sections |
| `POWER_MODULE` | Medium | Low | 0 | Power generation |
| `LIFE_SUPPORT` | Low | Low | 0 | Environmental systems |
| `EMPTY` | 0 | 0 | 0 | Unassigned |

---

## 3. Labor System Database

### 3.1 AI Character Roles

| Role | Leadership Range | Primary Skills | Daily Salary | Description |
|---|---|---|---|---|
| `Leader` | 7-10 | Strategy, Management | 500-2000 | Empire/corporation leader |
| `Advisor` | 5-8 | Analysis, Planning | 200-800 | Strategic advisor |
| `General` | 6-10 | Combat, Tactics | 400-1500 | Military commander |
| `Engineer` | 4-8 | Construction, Repair | 200-600 | Infrastructure specialist |
| `Scientist` | 4-8 | Research, Discovery | 200-600 | Research specialist |
| `Trader` | 3-7 | Commerce, Negotiation | 150-500 | Trade specialist |
| `Miner` | 2-6 | Extraction, Operations | 100-300 | Mining specialist |
| `Worker` | 1-5 | General labor | 50-200 | General workforce |

### 3.2 AI Character Experience System

- **XP per battle**: Variable based on outcome
- **Leadership increase**: Every 100 XP (base 5, max 10)
- **Formula**: `leadershipRating = 5 + (totalExperience / 100), clamped to max 10`

### 3.3 Crew Group Structure (1 Leader + 3 Crew Members)

```
Crew Group
├── Leader (1 AI Character)
│   ├── Role: Leader/Engineer/General
│   ├── Leadership Rating: 5-10
│   └── Manages contract execution
├── Crew Member 1 (AI Character)
│   ├── Role: Specialist (Miner, Engineer, Scientist, etc.)
│   └── Contributes skills to contract
├── Crew Member 2 (AI Character)
│   ├── Role: Specialist
│   └── Contributes skills to contract
└── Crew Member 3 (AI Character)
    ├── Role: Specialist
    └── Contributes skills to contract
```

**Crew Status**: ACTIVE → ON_CONTRACT → ACTIVE (cycle)

---

## 4. Complete Cost Database for Every Buildable Item

### 4.1 Planet Facility Construction Costs

| Facility | Material Cost (modules) | Labor Cost (crew-days) | Required Crew Skills | Build Time |
|---|---|---|---|---|
| `COLONY_DOME` | 2 FACILITY_MODULE | 3 crew-days | Engineer (Leader) | 3 turns |
| `SHIPYARD` | 3 HULL_MODULE + 2 ENGINE_MODULE | 8 crew-days | Engineer (Leader) | 5 turns |
| `SENSOR_ARRAY` | 1 SHIELD_MODULE + 1 WEAPON_MODULE | 4 crew-days | Scientist (Leader) | 3 turns |
| `MINING_FACILITY` | 2 HULL_MODULE + 1 FACILITY_MODULE | 5 crew-days | Miner (Leader) | 4 turns |
| `POWER_PLANT` | 2 HULL_MODULE + 1 ENGINE_MODULE | 4 crew-days | Engineer (Leader) | 3 turns |
| `DEFENSE_TURRET` | 2 WEAPON_MODULE + 1 HULL_MODULE | 5 crew-days | General (Leader) | 4 turns |
| `RESEARCH_LAB` | 2 FACILITY_MODULE + 1 SHIELD_MODULE | 6 crew-days | Scientist (Leader) | 5 turns |
| `TRADE_PORT` | 2 FACILITY_MODULE + 1 HULL_MODULE | 4 crew-days | Trader (Leader) | 3 turns |
| `MILITARY_BASE` | 3 HULL_MODULE + 2 WEAPON_MODULE | 8 crew-days | General (Leader) | 5 turns |
| `AGRICULTURAL_CENTER` | 2 FACILITY_MODULE | 3 crew-days | Engineer (Leader) | 3 turns |
| `WAREHOUSE` | 2 HULL_MODULE + 1 FACILITY_MODULE | 4 crew-days | Engineer (Leader) | 3 turns |
| `REFINERY` | 2 HULL_MODULE + 1 ENGINE_MODULE | 5 crew-days | Engineer (Leader) | 4 turns |
| `MODULE_BAY` | 2 HULL_MODULE + 1 FACILITY_MODULE | 4 crew-days | Engineer (Leader) | 3 turns |

### 4.2 Space Station Construction Costs (Full Station)

#### MILITARY Station (500 hull, 100 shields, 4 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 1,000 minerals + 500 energy | 20 crew-days | 10 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (4 slots)** | **1,200 minerals + 620 energy** | **40 crew-days** | **22 turns** |

**Module Breakdown (4 contracts, 1 crew each):**
1. COMMAND_MODULE: 3 HULL + 2 FACILITY → 8 crew-days
2. WEAPON_PLATFORM: 3 WEAPON + 1 HULL → 10 crew-days
3. SHIELD_ARRAY: 3 SHIELD + 1 POWER → 8 crew-days
4. DOCKING_RING: 2 HULL + 1 FACILITY → 6 crew-days

#### SHIPYARD Station (300 hull, 50 shields, 3 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 1,500 minerals + 750 energy | 25 crew-days | 12 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (3 slots)** | **1,650 minerals + 840 energy** | **40 crew-days** | **21 turns** |

#### MINING Station (200 hull, 30 shields, 2 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 800 minerals + 400 energy | 15 crew-days | 8 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (2 slots)** | **900 minerals + 460 energy** | **25 crew-days** | **14 turns** |

#### RESEARCH Station (250 hull, 40 shields, 2 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 900 minerals + 450 energy | 18 crew-days | 9 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (2 slots)** | **1,000 minerals + 510 energy** | **28 crew-days** | **15 turns** |

#### TRADE Station (150 hull, 20 shields, 2 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 600 minerals + 300 energy | 12 crew-days | 6 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (2 slots)** | **700 minerals + 360 energy** | **22 crew-days** | **12 turns** |

#### COLONY Station (200 hull, 30 shields, 3 slots)
| Component | Material Cost | Labor Cost | Build Time |
|---|---|---|---|
| Base Structure | 700 minerals + 350 energy | 15 crew-days | 8 turns |
| Per Module Slot | 50 minerals + 30 energy | 5 crew-days | 3 turns |
| **Total (3 slots)** | **850 minerals + 440 energy** | **30 crew-days** | **17 turns** |

### 4.3 Spell Components (Magical/Advanced Tech Items)

| Component | Raw Resources | Refined Resources | Modules | Labor | Build Time |
|---|---|---|---|---|---|
| `Basic Focus Crystal` | 2 CRYSTAL | 1 POWER_CELL | — | 2 crew-days | 2 turns |
| `Advanced Focus Crystal` | 3 CRYSTAL + 1 DUST | 2 POWER_CELL + 1 COMPOSITE | — | 4 crew-days | 3 turns |
| `Energy Conduit` | 1 ORE + 1 CRYSTAL | 1 METAL_ALLOY + 1 POWER_CELL | 1 ENGINE | 3 crew-days | 3 turns |
| `Shield Matrix` | 2 CRYSTAL + 1 DUST | 2 POWER_CELL + 1 COMPOSITE | 1 SHIELD | 4 crew-days | 4 turns |
| `Weapon Capacitor` | 1 ORE + 1 CRYSTAL | 1 METAL_ALLOY + 1 POWER_CELL | 1 WEAPON | 3 crew-days | 3 turns |
| `Hull Reinforcement` | 2 ORE + 1 DUST | 2 METAL_ALLOY + 1 COMPOSITE | 1 HULL | 3 crew-days | 3 turns |
| `Life Support Cell` | 1 BIO_MASS + 1 CRYSTAL | 1 NUTRIENT + 1 POWER_CELL | 1 FACILITY | 3 crew-days | 3 turns |

---

## 5. Modularized Work System (1 Leader + 3 Crew per Contract)

### 5.1 Contract Structure

Every buildable item is broken into contracts that a single crew group (1 leader + 3 members) can execute:

```
Contract
├── Contract ID
├── Contract Type (MINE / PROCESS / ASSEMBLE / CONSTRUCT / REPAIR)
├── Target Item (facility, station module, ship component, etc.)
├── Material Requirements (JSON: {resource_type: quantity})
├── Labor Requirements
│   ├── Leader Role Required (Engineer, Miner, Scientist, etc.)
│   ├── Leader Minimum Leadership (1-10)
│   ├── Crew Size (fixed: 4 = 1 leader + 3 members)
│   └── Crew Skills Required (list of roles)
├── Estimated Duration (turns)
├── Priority (1-10)
├── Status (PENDING → ASSIGNED → IN_PROGRESS → COMPLETED / FAILED)
├── Assigned Crew ID
├── Assigned Leader ID
├── Crew Members [3]
├── Progress (%)
├── Start Turn
├── Completion Turn
├── Material Cost Total (credits)
├── Labor Cost Total (credits)
└── Dependencies (other contracts that must complete first)
```

### 5.2 Space Station Modularization Example

A MILITARY space station is broken into **5 contracts**:

| Contract # | Contract Name | Type | Materials Needed | Labor | Duration |
|---|---|---|---|---|---|
| 1 | Foundation Frame | CONSTRUCT | 400 minerals + 200 energy | Engineer Leader + 3 Workers | 5 turns |
| 2 | Command Module | CONSTRUCT | 3 HULL + 2 FACILITY | Engineer Leader + 2 Engineers + 1 Scientist | 4 turns |
| 3 | Weapon Platform | CONSTRUCT | 3 WEAPON + 1 HULL | General Leader + 2 Engineers + 1 Miner | 5 turns |
| 4 | Shield Array | CONSTRUCT | 3 SHIELD + 1 POWER | Scientist Leader + 2 Engineers + 1 Worker | 4 turns |
| 5 | Docking Ring | CONSTRUCT | 2 HULL + 1 FACILITY | Engineer Leader + 3 Workers | 3 turns |

**Total**: 21 turns sequential, or 5 turns with 5 parallel crews

### 5.3 Production Chain Contracts

For a complete production chain from ore to space station:

```
Phase 1: Mining (3 parallel contracts)
├── Contract: Mine ORE (Miner Leader + 3 Workers)
├── Contract: Mine CRYSTAL (Miner Leader + 3 Workers)
└── Contract: Collect GAS (Miner Leader + 3 Workers)

Phase 2: Processing (3 parallel contracts)
├── Contract: Produce METAL_ALLOY (Engineer Leader + 3 Workers)
├── Contract: Produce POWER_CELL (Scientist Leader + 3 Workers)
└── Contract: Produce COMPOSITE (Engineer Leader + 3 Workers)

Phase 3: Module Assembly (3 parallel contracts)
├── Contract: Assemble HULL_MODULE (Engineer Leader + 3 Workers)
├── Contract: Assemble WEAPON_MODULE (Engineer Leader + 3 Workers)
└── Contract: Assemble SHIELD_MODULE (Engineer Leader + 3 Workers)

Phase 4: Station Construction (5 sequential or parallel contracts)
├── Contract: Build Foundation Frame
├── Contract: Build Command Module
├── Contract: Build Weapon Platform
├── Contract: Build Shield Array
└── Contract: Build Docking Ring
```

---

## 6. Database Schema (New Tables)

### 6.1 Material_Costs Table

```sql
CREATE TABLE material_costs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_type VARCHAR(50) NOT NULL,  -- RAW_RESOURCE, REFINED_RESOURCE, CONSTRUCTION_MODULE
    material_name VARCHAR(100) NOT NULL,  -- ORE, METAL_ALLOY, HULL_MODULE, etc.
    base_value_credits DECIMAL(15,2) NOT NULL,
    raw_inputs_json TEXT,  -- {"ORE": 2, "CRYSTAL": 1}
    refined_inputs_json TEXT,  -- {"METAL_ALLOY": 2, "COMPOSITE": 1}
    processing_time_turns INT,
    alternative_recipe_json TEXT,
    alternative_time_turns INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 6.2 Build_Contracts Table

```sql
CREATE TABLE build_contracts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_type VARCHAR(50) NOT NULL,  -- MINE, PROCESS, ASSEMBLE, CONSTRUCT, REPAIR
    target_item_type VARCHAR(100) NOT NULL,  -- FACILITY, STATION_MODULE, SHIP_COMPONENT, etc.
    target_item_name VARCHAR(200) NOT NULL,
    description TEXT,
    material_requirements_json TEXT NOT NULL,
    leader_role_required VARCHAR(50),
    leader_min_leadership INT,
    crew_size INT DEFAULT 4,  -- 1 leader + 3 members
    crew_skills_required_json TEXT,
    estimated_duration_turns INT NOT NULL,
    priority INT DEFAULT 5,
    status VARCHAR(20) DEFAULT 'PENDING',
    assigned_crew_id BIGINT,
    assigned_leader_id BIGINT,
    crew_member_1_id BIGINT,
    crew_member_2_id BIGINT,
    crew_member_3_id BIGINT,
    progress_percent INT DEFAULT 0,
    material_cost_total_credits DECIMAL(15,2),
    labor_cost_total_credits DECIMAL(15,2),
    dependencies_json TEXT,  -- ["contract_id_1", "contract_id_2"]
    start_turn INT,
    completion_turn INT,
    galaxy_id BIGINT,
    planet_id BIGINT,
    station_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (assigned_crew_id) REFERENCES crew(id),
    FOREIGN KEY (assigned_leader_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_1_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_2_id) REFERENCES ai_characters(id),
    FOREIGN KEY (crew_member_3_id) REFERENCES ai_characters(id)
);
```

### 6.3 Infrastructure_Items Table

```sql
CREATE TABLE infrastructure_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_type VARCHAR(50) NOT NULL,  -- PLANET_FACILITY, SPACE_STATION, STATION_MODULE
    item_name VARCHAR(200) NOT NULL,
    description TEXT,
    -- Material costs
    mineral_cost INT,
    energy_cost INT,
    raw_resources_json TEXT,
    refined_resources_json TEXT,
    construction_modules_json TEXT,
    -- Labor costs
    total_labor_crew_days INT,
    labor_cost_credits DECIMAL(15,2),
    -- Production stats
    raw_production_json TEXT,  -- {"ORE": 3, "GAS": 1}
    refined_production_json TEXT,  -- {"METAL_ALLOY": 1}
    module_upkeep_json TEXT,  -- {"HULL_MODULE": 1, "SHIELD_MODULE": 1}
    credit_income_per_turn INT,
    upkeep_cost_per_turn INT,
    -- Bonuses
    mineral_bonus INT DEFAULT 0,
    energy_bonus INT DEFAULT 0,
    food_bonus INT DEFAULT 0,
    research_bonus INT DEFAULT 0,
    defense_bonus INT DEFAULT 0,
    -- Construction
    build_time_turns INT,
    required_crew_groups INT,  -- Number of parallel crews needed
    contract_breakdown_json TEXT,  -- Array of sub-contracts
    -- Stats
    hull_points INT,
    shield_points INT,
    module_slots INT,
    docking_ports INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6.4 Production_Chain_Tracking Table

```sql
CREATE TABLE production_chain_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_name VARCHAR(200) NOT NULL,  -- "Military Station - Full Chain"
    target_item_id BIGINT,
    total_phases INT,
    total_contracts INT,
    total_material_cost_credits DECIMAL(15,2),
    total_labor_cost_credits DECIMAL(15,2),
    total_duration_turns INT,
    min_crews_required INT,
    status VARCHAR(20) DEFAULT 'PLANNED',
    phase_breakdown_json TEXT,  -- Full production chain with all phases
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (target_item_id) REFERENCES infrastructure_items(id)
);
```

### 6.5 Crew_Skills Table (Extension)

```sql
CREATE TABLE crew_member_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ai_character_id BIGINT NOT NULL,
    skill_category VARCHAR(50) NOT NULL,  -- MINING, ENGINEERING, SCIENCE, COMBAT, TRADE
    skill_level INT NOT NULL,  -- 1-10
    experience_points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ai_character_id) REFERENCES ai_characters(id)
);
```

---

## 7. Economic Flow Summary

### 7.1 Complete Production Chain: Ore → Space Station

```
Step 1: MINE (Raw Resources)
    Mining Station or Mining Facility
    Output: ORE, CRYSTAL, GAS, DUST, BIO_MASS
    Crew: Miner Leader + 3 Workers
    Time: 1-2 turns per batch

Step 2: PROCESS (Refined Resources)
    Refinery facility
    Output: METAL_ALLOY, POWER_CELL, FUEL, COMPOSITE, NUTRIENT
    Crew: Engineer/Scientist Leader + 3 Workers
    Time: 1-3 turns per batch

Step 3: ASSEMBLE (Construction Modules)
    Factory/Module Bay
    Output: HULL, WEAPON, SHIELD, ENGINE, FACILITY modules
    Crew: Engineer Leader + 3 Workers
    Time: 1-4 turns per batch

Step 4: CONSTRUCT (Final Product)
    Shipyard or Orbital Assembly
    Output: Space Station, Ship, Facility
    Crew: Specialist Leader + 3 Specialists
    Time: 3-12 turns per contract
```

### 7.2 Cost Calculation Formula

For any item:
```
Total Material Cost = Σ(raw_resource_qty × raw_resource_value)
                    + Σ(refined_resource_qty × refined_resource_value)
                    + Σ(module_qty × module_value)

Module Value = Σ(refined_inputs × refined_value) × 1.5  (assembly markup)
Refined Value = Σ(raw_inputs × raw_value) × 1.3  (processing markup)

Total Labor Cost = crew_size × daily_salary × duration_turns
                 × leadership_modifier

Leadership Modifier = 1.0 - ((leader_leadership - 5) × 0.05)
(Higher leadership = lower labor cost through efficiency)

Total Cost = Total Material Cost + Total Labor Cost
```

---

## 8. Integration with Existing Systems

### 8.1 AiAdmin Dispatch Integration

The `AiAdminDispatchService` routes orders to available AI admins. The new contract system extends this:
- Each `AiAdminOrder` creates one or more `Build_Contracts`
- Dispatch service assigns contracts to available crew groups
- Progress tracked through `WorkOrder` status flow

### 8.2 Resource Pipeline Integration

The `ResourcePipelineManager` tracks:
- Raw resource inventory → consumed by processing contracts
- Refined resource inventory → consumed by assembly contracts
- Module inventory → consumed by construction contracts

### 8.3 Crew System Integration

Existing `Crew` and `CrewMember` entities:
- Each crew group executes one contract at a time
- `Crew.status`: ACTIVE → ON_CONTRACT → ACTIVE
- Contract completion grants XP to all crew members

### 8.4 Space Station Builder Integration

`SpaceStationBuilder` service:
- Uses `StationModuleTemplate` for module definitions
- New system adds material/labor cost tracking
- Each module becomes a separate contract

---

## 9. Implementation Priority

### Phase 1: Core Database (Week 1-2)
1. Create `material_costs` table and populate with all materials
2. Create `infrastructure_items` table with all buildable items
3. Create `build_contracts` table for contract tracking

### Phase 2: Contract System (Week 3-4)
1. Implement `BuildContractService` for contract creation
2. Implement contract assignment to crew groups
3. Implement progress tracking and completion

### Phase 3: Production Chain (Week 5-6)
1. Implement `ProductionChainService` for full chain planning
2. Auto-generate contracts from production chains
3. Implement dependency tracking

### Phase 4: Cost Tracking (Week 7-8)
1. Implement material cost calculator
2. Implement labor cost calculator with leadership modifiers
3. Add cost reports and dashboards

### Phase 5: Modularization (Week 9-10)
1. Break all space stations into module contracts
2. Break all facilities into construction contracts
3. Implement parallel crew assignment for parallel contracts

---

## 10. Summary Statistics

| Category | Count |
|---|---|
| Raw Resource Types | 5 |
| Refined Resource Types | 5 |
| Construction Module Types | 5 |
| Base Materials | 8 |
| Planet Facility Types | 13 |
| Space Station Types | 6 |
| Station Module Templates | 13 |
| AI Character Roles | 8 |
| Processing Recipes (standard) | 5 |
| Processing Recipes (alternative) | 5 |
| Module Assembly Recipes (standard) | 5 |
| Module Assembly Recipes (alternative) | 5 |
| Spell Components | 7 |
| **Total unique buildable items** | **~50+** |
| **Contracts per space station** | **4-5** |
| **Crew groups per full station** | **1-5 (parallel)** |
