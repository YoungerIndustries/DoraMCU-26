# Earth 2026 Unified Hub Galaxy Analysis

**Date:** 2026-11-22
**Version:** v0.6.9
**Author:** Junie (DoraMCU Agent)

---

## Executive Summary

The DoraMCU vision is that **Earth 2026 is the master hub galaxy** containing all game systems layered on top — starting with an MMORPG RPG base with FPS combat for resolution, then 4X economic tycoon, Third Reich ground combat, and Transport Tycoon trade routes. Each other galaxy (Traveller, Star Wars, D&D 5E, etc.) uses Earth 2026 as a template and is created with game-specific lore and special rule implementations.

This document analyzes:
1. The **economic system** (shared across all game types)
2. The **combat system** (layered combat resolution)
3. Each **game galaxy's creation parameters** based on the Earth 2026 template
4. The **current vs. ideal architecture** for the unified hub

---

## 1. Current Architecture vs. Vision

### 1.1 Current Architecture (Separate Galaxies)

Currently, `GalaxyStartupRegistry.java` creates **16 separate full galaxies** — one per game type:

| Priority | Game Type | Template | Galaxy Name | Systems |
|----------|-----------|----------|-------------|---------|
| 1 | Earth 2026 | EARTH_2026_TEMPLATE | Earth 2026 | ~19,700 |
| 2 | Traveller | TRAVELLER_CLASSIC | Traveller Galaxy | ~19,700 |
| 3 | D&D 5E | TRAVELLER_CLASSIC | Forgotten Realms | ~19,700 |
| 4 | Star Trek 4X | STAR_TREK | Star Trek Galaxy | ~19,700 |
| 5 | Babylon 5 | BABYLON5 | Babylon 5 Galaxy | ~19,700 |
| 6 | Star Wars | TRAVELLER_CLASSIC | Star Wars Galaxy | ~19,700 |
| 7 | DoraMCU 4X | DORAMCU_4X_TEMPLATE | DoraMCU 4X Galaxy | ~19,700 |
| 8 | Warhammer 40K | VOID | Warhammer 40K Galaxy | ~19,700 |
| 9 | Captain's Log | STAR_TREK | Captain's Log Galaxy | ~19,700 |
| 10 | Star Frontiers | FRONTIER | Star Frontiers Galaxy | ~19,700 |
| 11 | MechWarrior | FRONTIER | MechWarrior Galaxy | ~19,700 |
| 12 | Starship Troopers | FRONTIER | Starship Troopers Galaxy | ~19,700 |
| 13 | Third Reich | EARTH_2026_TEMPLATE | Third Reich Galaxy | ~19,700 |
| 14 | 007 | EARTH_2026_TEMPLATE | 007 Espionage Galaxy | ~19,700 |
| 15 | Transport Tycoon | DORAMCU_4X_TEMPLATE | Transport Tycoon Galaxy | ~19,700 |
| 16 | FPS | EARTH_2026_TEMPLATE | FPS Combat Galaxy | ~19,700 |

**Total: ~315,200 systems across 16 galaxies** — each with ~103 MB heap usage = ~32 GB total (OOM on Pi with 4 GB RAM).

### 1.2 Vision: Earth 2026 as Unified Hub

The vision is a **single Earth 2026 galaxy** with all game systems as **layers** on top:

```
┌─────────────────────────────────────────────────────────────────┐
│                   Earth 2026 Hub Galaxy                        │
│  (Single galaxy with all game mechanics layered on top)         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Layer 1: MMORPG RPG Base (Earth2026Service)                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Characters, Locations, Scenarios, Organizations         │    │
│  │ Family, Companions, Contracts, Jobs                     │    │
│  │ Younger Industries HQ with 16 portals                   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                           │                                     │
│  Layer 2: Combat Resolution (FPS + Game-Specific)              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ CombatService interface                                 │    │
│  │ DelegatingCombatService routes to game-specific impl    │    │
│  │ FPS: FirearmsCombatController, MeleeCombatController    │    │
│  │ Third Reich: GroundCombatAdapter                        │    │
│  │ D&D 5E: Dnd5eCombatService                             │    │
│  │ Traveller: B5FirearmsCombat / B5MeleeCombat             │    │
│  └─────────────────────────────────────────────────────────┘    │
│                           │                                     │
│  Layer 3: Economic System (Transport Tycoon)                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ EconomyBridge: Earth2026 → Transport Tycoon             │    │
│  │ TransportBridge: Routes → CampaignTradeRoutes           │    │
│  │ 4 resource layers: Raw → Refined → Strategic → Cargo   │    │
│  │ 21 industry types, 25 cargo types                       │    │
│  │ TradeRouteManager, MarketShareTracker                   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                           │                                     │
│  Layer 4: 4X Strategy Layer (DoraMCU 4X)                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Galaxy, Sectors, SubSectors, Solar Systems              │    │
│  │ Empires, Fleets, Planets, Resources                     │    │
│  │ Hyperlanes, Space Stations                              │    │
│  │ TaxManager, TurnManager, SessionActionHandler           │    │
│  └─────────────────────────────────────────────────────────┘    │
│                           │                                     │
│  Layer 5: Game-Specific Overlays                               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ OverlayBuilderRegistry maps GameType → OverlayBuilder   │    │
│  │ Traveller: UWP data, hex coordinates, named subsectors  │    │
│  │ Star Wars: Region-based sectors, canonical systems      │    │
│  │ D&D 5E: Planes of existence, dragon empires            │    │
│  │ Babylon 5: 13 race-based empires, Babylon station      │    │
│  │ (Each overlay adds game-specific rules on top)          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 Key Architectural Insight

The **infrastructure already supports this vision**:

- **`GameModule` interface** — unified contract for all game modules (combat, inventory, crafting, spells, encounters, etc.)
- **`DelegatingCombatService`** — routes combat to game-specific implementations
- **`OverlayBuilderRegistry`** — maps GameType to game-specific overlay builders
- **`EconomyBridge`** — connects Earth 2026 economy to Transport Tycoon
- **`TransportBridge`** — connects Earth 2026 transport routes to Tycoon
- **`PlanetBuilderBridge`** — connects Earth 2026 planets to surface building
- **`Earth2026GameBridgeService`** — cross-module feature sharing

The **gap** is that `GalaxyStartupRegistry` creates separate galaxies instead of layering game types on the single Earth 2026 hub.

---

## 2. Economic System Analysis

### 2.1 Shared Economic Infrastructure

The economic system is **already designed as a shared layer** via `EconomyBridge` and `TransportBridge`:

#### EconomyBridge (`earth2026/service/EconomyBridge.java`)

Connects Earth 2026 country data to Transport Tycoon cargo system:

- **Maps countries to industries** based on GDP and dominant sectors
- **Sets cargo demand** based on population and GDP per capita
- **Creates trade routes** between connected countries
- **Processes turn-based economy simulation**

```
Earth2026Country → IndustryType mapping:
  High GDP (>2T) → Electronics, Machine Shop, Luxury Factory, Steel Mill
  Medium GDP (0.5-2T) → Steel Mill, Bakery, Sawmill
  Low GDP (<0.5T) → Farm, Ranch, Logging Camp
  Oil-rich (SA, RU, MX, NG) → Oil Well, Refinery
  Mining (AU, BR, ZA, RU) → Mine, Coal Mine
  Agricultural (US, BR, AR, IN, FR) → Farm, Bakery, Ranch
  Forest (CA, RU, BR, ID) → Logging Camp, Sawmill
  Fishing (JP, KR, NO) → Fishing Port
```

#### TransportBridge (`earth2026/service/TransportBridge.java`)

Converts Earth 2026 transport routes to Tycoon trade routes:

- **Maps vehicle types**: Airliner → AIRCRAFT, Rail → PASSENGER_TRAIN, Freight → FREIGHT_TRAIN, Ship → CARGO_SHIP
- **Deploys vehicles** on routes with cargo types
- **Processes turn-based vehicle movement** and cargo delivery
- **Passenger booking** between capital cities via YoungerOffice stations

### 2.2 Resource Hierarchy (4 Layers)

From `GAME_SYSTEMS_ANALYSIS.md`, the economic system has 4 resource layers:

| Layer | Type | Examples | Shared Across |
|-------|------|----------|---------------|
| 1 | Raw Resources (Space) | ORE, CRYSTAL, GAS, DUST, BIO_MASS | All space-faring games |
| 2 | Refined Resources (Space) | METAL_ALLOY, POWER_CELL, FUEL, COMPOSITE, NUTRIENT | All space-faring games |
| 3 | Strategic Resources (Space) | ELERIUM, ANTI_MATTER, THULIUM, DURANTIUM, PROMETHION | 4X strategy games |
| 4 | Cargo Types (Planet) | 25 types: ORE, STEEL, ELECTRONICS, PASSENGERS, etc. | Transport Tycoon layer |

### 2.3 Industry Types (21 Total)

**Primary (8):** MINE, COAL_MINE, OIL_WELL, LOGGING_CAMP, FARM, RANCH, FISHING_PORT, ORCHARD
**Secondary (8):** STEEL_MILL, CEMENT_PLANT, BAKERY, SAWMILL, RANCH_PROCESSING, REFINERY, PLANT_FACTORY, CHEMICAL_PLANT
**Tertiary (5):** ELECTRONICS_FACTORY, MACHINE_SHOP, LUXURY_FACTORY, TEXTILE_MILL, PHARMACEUTICAL

### 2.4 Economic System Applicability by Game Type

| Game Type | Economic Layer | Shared Economy? | Notes |
|-----------|---------------|-----------------|-------|
| Earth 2026 | Full (all 4 layers) | ✅ Base | Country GDP, trade routes, industries |
| Transport Tycoon | Layer 4 (cargo) | ✅ Shared | Primary economic game |
| DoraMCU 4X | Layers 1-3 (space) | ✅ Shared | Planet resources, fleet production |
| Star Trek 4X | Layers 1-3 (space) | ✅ Shared | Federation trade, resource management |
| Star Wars | Layers 1-3 (space) | ✅ Shared | Hutt trade, Imperial resources |
| Traveller | Layers 1-3 (space) | ✅ Shared | Trade codes from UWP |
| Babylon 5 | Layers 1-3 (space) | ✅ Shared | Interstellar trade |
| Warhammer 40K | Layers 1-3 (space) | ✅ Shared | Imperium tithes, resource extraction |
| MechWarrior | Layer 4 (planet) | ⚠️ Partial | Planet industry, not space resources |
| Star Frontiers | Layers 1-3 (space) | ✅ Shared | Kzinti trade routes |
| 007 | Layer 4 (planet) | ⚠️ Partial | Espionage economy, not full trade |
| Third Reich | Layer 4 (planet) | ❌ No | Ground combat, no economy |
| FPS | None | ❌ No | Combat only, no economy |
| D&D 5E | Layer 4 (planet) | ⚠️ Partial | Dungeon loot, not trade routes |
| LOTR | Layer 4 (planet) | ⚠️ Partial | Single planet, minimal trade |
| Starship Troopers | Layers 1-2 (space) | ⚠️ Partial | Federated Solar System resources |
| Captain's Log | Layers 1-3 (space) | ✅ Shared | Star Trek trade, mission rewards |

---

## 3. Combat System Analysis

### 3.1 Unified Combat Architecture

The combat system is **already designed as a layered system**:

#### CombatService Interface (`gamecore/CombatService.java`)

Unified interface with game-specific implementations:
- `createCombat()` — create encounter
- `addCombatant()` — add combatant with AC, HP
- `rollInitiative()` — determine turn order
- `makeAttack()` — resolve attack with damage
- `healCombatant()` — heal during combat
- `endCombat()` — conclude encounter

#### DelegatingCombatService (`gamecore/DelegatingCombatService.java`)

Routes to the first registered `GameModule` with a `CombatService`:
- Falls back to simple 2d6+bonus vs 8 system
- Uses `GameModuleRegistry.findModuleWithCombatService()`

#### Game-Specific Combat Implementations

| Game Type | Combat Service | Combat Style | Key File |
|-----------|---------------|--------------|----------|
| D&D 5E | Dnd5eCombatService | Tactical, grid-based, spells | `dnd5e/service/Dnd5eCombatService.java` |
| Traveller | B5FirearmsCombat / B5MeleeCombat | Firearms, melee, psionics | `babylon5/model/B5FirearmsCombat.java` |
| Babylon 5 | B5CombatService | Firearms, melee, psionics | `babylon5/service/B5CombatService.java` |
| Captain's Log | StarshipCombatService | Starship-to-starship | `captainslog/service/StarshipCombatService.java` |
| Third Reich | GroundCombatAdapter | Ground combat, WWII | `thirdreich/adapter/GroundCombatAdapter.java` |
| FPS | FirearmsCombatController | First-person shooter | `controller/battle/FirearmsCombatController.java` |
| 4X | FourxCombatService | Fleet-to-fleet space combat | `fourx/FourxCombatService.java` |
| LOTR | LotrCombatService | Tactical, melee-focused | `lord_of_the_rings/service/LotrCombatService.java` |
| MechWarrior | (pending) | Mech-to-mech combat | Not yet implemented |
| Starship Troopers | (pending) | Mobile Infantry vs Bugs | Not yet implemented |

### 3.2 Combat Resolution Flow

```
Player Action
     │
     ▼
┌─────────────────────┐
│  CombatService      │  (Unified interface)
│  (gamecore)         │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│  DelegatingCombat   │  (Routes to game-specific)
│  Service            │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│  GameModule         │  (Game-specific implementation)
│  CombatService      │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│  Combat Resolver    │  (Dice, damage, status effects)
│  (hex/manager)      │
└─────────────────────┘
```

### 3.3 Combat System Applicability by Game Type

| Game Type | Combat Layer | Shared Combat? | Notes |
|-----------|-------------|----------------|-------|
| FPS | Firearms + Melee | ✅ Base | Primary combat resolution for Earth 2026 |
| Third Reich | Ground Combat | ✅ Shared | WWII ground combat adapter |
| D&D 5E | Tactical + Spells | ✅ Shared | Grid-based with spell system |
| Traveller | Firearms + Melee | ✅ Shared | Space and ground combat |
| Babylon 5 | Firearms + Psionics | ✅ Shared | Psionics adds supernatural layer |
| Captain's Log | Starship | ✅ Shared | Ship-to-ship combat |
| 4X | Fleet Combat | ✅ Shared | Space fleet battles |
| Star Wars | Firearms + Lightsaber | ⚠️ Partial | Needs lightsaber combat type |
| Warhammer 40K | Firearms + Melee | ⚠️ Partial | Needs power weapon combat |
| MechWarrior | Mech Combat | ❌ No | Needs dedicated mech combat system |
| Starship Troopers | Infantry + Power Armor | ❌ No | Needs power armor combat |
| LOTR | Melee + Magic | ⚠️ Partial | Needs magic combat integration |
| 007 | Firearms + Stealth | ⚠️ Partial | Needs stealth mechanics |
| Star Frontiers | Firearms | ✅ Shared | Traveller-like combat |
| Transport Tycoon | None | ❌ No | Economic game, no combat |
| D&D 5E | Tactical | ✅ Shared | Already implemented |

---

## 4. Game Galaxy Creation Parameters

### 4.1 Earth 2026 as Template

The `EARTH_2026_TEMPLATE` in `GalaxyTemplate.java` is documented as:

> "The default template galaxy with all game mechanics installed. Used as the foundation for new galaxy creation."

**Template Parameters:**
- Sector size: 10×10
- Subsector dimension: 10×10
- Civilization density: 0.4
- Resource distribution: 0.6
- Galaxy shape: spiral
- Naming convention: astronomical
- Empire count: 4
- Difficulty: medium
- Game type: "Earth2026"
- Moon chance: 0.5
- Space station chance: 0.2
- Gas giant chance: 0.3
- Black hole chance: 0.02
- Hyperlane density: 1.0
- Asteroid belt chance: 0.5

### 4.2 Creation Parameters by Game Type

Each galaxy should be created from the Earth 2026 template with game-specific modifications:

#### Tier 1: Full Galaxy Games (Space-Faring, Multi-Sector)

These games need the full galaxy structure with all layers:

| Game Type | Template Base | Systems | Key Modifications | Overlay Builder |
|-----------|--------------|---------|-------------------|-----------------|
| **Traveller** | EARTH_2026 | ~250,000 | 15-35 systems/subsector, UWP data, hex coordinates, named subsectors, capital worlds | TravellerOverlayBuilder ✅ |
| **Star Wars** | EARTH_2026 | ~50,000-75,000 | Region-based sectors (Outer Rim, Core Worlds), canonical systems (Coruscant, Tatooine) | StarWarsOverlayBuilder ✅ |
| **Star Trek 4X** | EARTH_2026 | ~30,000-50,000 | Quadrant-based sectors (Alpha, Beta, Gamma, Delta), canonical systems (Sol, Qo'noS) | (pending) |
| **Warhammer 40K** | EARTH_2026 | ~50,000-100,000 | Segmentum-based sectors, world types (Death World, Hive World, Forge World) | (pending) |
| **Babylon 5** | EARTH_2026 | ~20,000-30,000 | 13 race-based empires, Babylon 5 station as fixed point | Babylon5EmpireBuilder ✅ |
| **DoraMCU 4X** | EARTH_2026 | ~19,700 | Base 4X template, generic empires | (none needed) |

#### Tier 2: Modified Galaxy Games (Space-Faring, Reduced Scale)

These games need a galaxy but with modified parameters:

| Game Type | Template Base | Systems | Key Modifications | Overlay Builder |
|-----------|--------------|---------|-------------------|-----------------|
| **Star Frontiers** | EARTH_2026 | ~50,000 | Kzinti Syndicate sector, Star Frontiers rules | (pending) |
| **MechWarrior** | EARTH_2026 | ~20,000 | Jump points instead of hyperlanes, Inner Sphere systems | (pending) |
| **007** | EARTH_2026 | ~5,000-10,000 | Minimal galaxy, mission locations as fixed points | (pending) |
| **Captain's Log** | EARTH_2026 | ~25 | SUBSECTOR_RPG template, mission-based generation | (pending) |

#### Tier 3: Single System Games (Planetary Focus)

These games don't need a full galaxy — just a solar system:

| Game Type | Template Base | Systems | Key Modifications | Overlay Builder |
|-----------|--------------|---------|-------------------|-----------------|
| **D&D 5E** | SINGLE_SYSTEM_RPG | 1-25 | Material Plane + planes of existence, dragon empires | Dnd5eOverlayBuilder ✅ |
| **LOTR** | SINGLE_SYSTEM_RPG | 1 | Single planet (Middle-earth), 8 regions as planets | LordOfTheRingsOverlayBuilder ✅ |
| **Starship Troopers** | SINGLE_SYSTEM_RPG | 2 | Solar system + Bug Home World | (pending) |

#### Tier 4: No Galaxy Needed (Earth-Only)

These games don't need galaxy generation at all:

| Game Type | Galaxy? | Reason | Creation Method |
|-----------|---------|--------|-----------------|
| **Earth 2026** | Hub only | The hub itself — no separate galaxy | Earth2026Service initializes locations |
| **Third Reich** | No | Ground combat on Earth | Earth 2026 locations + terrain data |
| **Transport Tycoon** | No | Economic layer on Earth 2026 | EconomyBridge + TransportBridge |
| **FPS** | No | Combat layer on Earth 2026 | FirearmsCombatController + maps |

### 4.3 Corrected GalaxyStartupRegistry Mapping

Based on the analysis, the `GalaxyStartupRegistry.DEFAULT_REGISTRATIONS` should be:

```java
// Tier 1: Full galaxy games
Map.entry(GameType.TRAVELLER, new GalaxyRegistration(2, "Traveller Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "traveller")),
Map.entry(GameType.STAR_WARS, new GalaxyRegistration(3, "Star Wars Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "star-wars")),
Map.entry(GameType.STAR_TREK_4X, new GalaxyRegistration(4, "Star Trek Galaxy", GalaxyTemplate.STAR_TREK, "star-trek")),
Map.entry(GameType.WARHAMMER_40K, new GalaxyRegistration(5, "Warhammer 40K Galaxy", GalaxyTemplate.VOID, "warhammer-40k")),
Map.entry(GameType.BABYLON_5, new GalaxyRegistration(6, "Babylon 5 Galaxy", GalaxyTemplate.BABYLON5, "babylon5")),
Map.entry(GameType.DORAMCU_4X, new GalaxyRegistration(7, "DoraMCU 4X Galaxy", GalaxyTemplate.DORAMCU_4X_TEMPLATE, "doramcu-4x")),

// Tier 2: Modified galaxy games
Map.entry(GameType.SFB, new GalaxyRegistration(8, "Star Frontiers Galaxy", GalaxyTemplate.FRONTIER, "star-frontiers")),
Map.entry(GameType.MECHWARRIOR, new GalaxyRegistration(9, "MechWarrior Galaxy", GalaxyTemplate.FRONTIER, "mechwarrior")),
Map.entry(GameType.ZERO_ZERO_SEVEN, new GalaxyRegistration(10, "007 Espionage", GalaxyTemplate.EARTH_2026_TEMPLATE, "007")),
Map.entry(GameType.CAPTAINS_LOG, new GalaxyRegistration(11, "Captain's Log", GalaxyTemplate.SUBSECTOR_RPG, "captains-log")),

// Tier 3: Single system games
Map.entry(GameType.DND_5E, new GalaxyRegistration(12, "Forgotten Realms", GalaxyTemplate.SINGLE_SYSTEM_RPG, "dnd-5e")),
Map.entry(GameType.LORD_OF_THE_RINGS, new GalaxyRegistration(13, "Middle-earth", GalaxyTemplate.SINGLE_SYSTEM_RPG, "lotr")),
Map.entry(GameType.STARSHIP_TROOPERS, new GalaxyRegistration(14, "Starship Troopers", GalaxyTemplate.SINGLE_SYSTEM_RPG, "starship-troopers")),

// Tier 4: No galaxy needed — these are layers on Earth 2026
// GameType.EARTH_2026 — Hub galaxy (priority 1, already registered)
// GameType.THIRD_REICH — Ground combat layer (no galaxy)
// GameType.TRANSPORT_TYCOON — Economic layer (no galaxy)
// GameType.FPS — Combat layer (no galaxy)
```

### 4.4 Resource Impact Comparison

| Scenario | Galaxies | Total Systems | Heap Usage | Startup Time |
|----------|----------|--------------|------------|--------------|
| Current (16 separate) | 16 | ~315,200 | ~32 GB | ~8 minutes |
| Corrected (10 galaxies) | 10 | ~450,000* | ~46 GB* | ~12 minutes* |
| With lazy loading | 10 | ~450,000* | ~500 MB | ~30 seconds |
| Earth 2026 hub only | 1 | ~19,700 | ~103 MB | ~30 seconds |

*Traveller at 250K systems with lazy loading; others at current levels.

---

## 5. Layered Architecture Design

### 5.1 Layer Dependencies

```
Layer 0: Earth 2026 Hub (Always loaded)
  ├── Earth2026Service (characters, locations, scenarios)
  ├── YoungerIndustriesHQ (16 portals to other galaxies)
  └── Earth2026RulesDatabaseService (game rules database)

Layer 1: Combat Resolution (Loaded on demand)
  ├── CombatService interface
  ├── DelegatingCombatService
  ├── FPS: FirearmsCombatController
  ├── Third Reich: GroundCombatAdapter
  └── Game-specific: Dnd5eCombatService, etc.

Layer 2: Economic System (Loaded on demand)
  ├── EconomyBridge (Earth 2026 → Transport Tycoon)
  ├── TransportBridge (routes → trade routes)
  ├── TradeRouteManager
  └── MarketShareTracker

Layer 3: 4X Strategy (Loaded per galaxy)
  ├── GalaxyGenerator
  ├── SectorBuilder
  ├── TaxManager, TurnManager
  └── SessionActionHandler

Layer 4: Game-Specific Overlays (Loaded per galaxy)
  ├── OverlayBuilderRegistry
  ├── TravellerOverlayBuilder
  ├── StarWarsOverlayBuilder
  └── (etc.)
```

### 5.2 Portal Network as Galaxy Router

The 16 HQ portals in `HqPortal.java` serve as the gateway between Earth 2026 and other galaxies:

| Portal | Destination Galaxy | Game Type |
|--------|-------------------|-----------|
| 🚀 Traveller Spinward Gate | Traveller Galaxy | TRAVELLER |
| ✨ Hyperdrive Portal | Star Wars Galaxy | STAR_WARS |
| 🖖 Transporter Room | Star Trek Galaxy | STAR_TREK_4X |
| ⚔️ Warp Rift Gateway | Warhammer 40K Galaxy | WARHAMMER_40K |
| 🛸 Babylon Station Airlock | Babylon 5 Galaxy | BABYLON_5 |
| 🌌 DoraMCU Empire Gate | DoraMCU 4X Galaxy | DORAMCU_4X |
| ⭐ Star Frontiers Jump Gate | Star Frontiers Galaxy | SFB |
| 🤖 Mech Bay Airlock | MechWarrior Galaxy | MECHWARRIOR |
| 📋 Captain's Log Transport | Captain's Log | CAPTAINS_LOG |
| 🐉 Dragon's Maw Portal | D&D 5E (Forgotten Realms) | DND_5E |
| 🪖 Mobile Infantry Drop Pod | Starship Troopers | STARSHIP_TROOPERS |
| 🎖️ Ground Combat Simulator | Third Reich (Earth-only) | THIRD_REICH |
| 🕵️ 007 Espionage Tunnel | 007 (Earth-only) | ZERO_ZERO_SEVEN |
| 🚂 Trade Route Gateway | Transport Tycoon (Earth-only) | TRANSPORT_TYCOON |
| 🔫 Battlefield Entry Point | FPS (Earth-only) | FPS |
| 🌍 Earth 2026 Reflection Loop | Home base | EARTH_2026 |

**Key insight:** Portals 13-16 (Third Reich, 007, Transport Tycoon, FPS) don't lead to separate galaxies — they lead to **game modes within Earth 2026**.

---

## 6. Implementation Recommendations

### 6.1 Immediate Changes (Priority 1)

**Fix GalaxyStartupRegistry to use correct templates:**

1. Change D&D 5E from `TRAVELLER_CLASSIC` to `SINGLE_SYSTEM_RPG`
2. Change LOTR to `SINGLE_SYSTEM_RPG` (not currently registered)
3. Remove Third Reich, Transport Tycoon, and FPS from galaxy registry — they are Earth 2026 layers
4. Change Captain's Log to `SUBSECTOR_RPG`
5. Change Starship Troopers to `SINGLE_SYSTEM_RPG`

**Expected impact:**
- Reduces from 16 to 12 galaxies
- Saves ~49,100 systems (3 × 19,700 for Third Reich, Transport Tycoon, FPS)
- D&D 5E drops from ~19,700 to 1 system
- LOTR drops from ~19,700 to 1 system
- Starship Troopers drops from ~19,700 to 2 systems
- Captain's Log drops from ~19,700 to 25 systems

### 6.2 Medium-Term Changes (Priority 2)

**Implement lazy loading for Traveller-style galaxies:**

1. Tier 1: Galaxy map always loaded (~5 MB/galaxy)
2. Tier 2: Sector build on first fleet entry (~103 MB/sector)
3. Tier 3: Subsector detail on fleet proximity

**Expected impact:**
- Startup time: ~8 min → ~30 sec
- Peak memory: ~32 GB → ~500 MB
- Traveller galaxy: 250K systems with lazy loading

### 6.3 Long-Term Changes (Priority 3)

**Implement Earth 2026 as true unified hub:**

1. Single Earth 2026 galaxy with all game mechanics
2. Game modes (Third Reich, FPS, Transport Tycoon) as layers within Earth 2026
3. Portal network as galaxy router
4. Cross-module feature sharing via GameModule interface

---

## 7. Summary

### 7.1 Current State

- 16 separate galaxies, each with ~19,700 systems
- ~315,200 total systems, ~32 GB heap usage
- Economic and combat systems are already designed as shared layers
- The infrastructure supports the unified hub vision

### 7.2 Vision

- Earth 2026 as master hub with all game systems layered on top
- 4 game types (Third Reich, FPS, Transport Tycoon, 007) as Earth 2026 layers, not separate galaxies
- 3 game types (D&D 5E, LOTR, Starship Troopers) as single-system games
- 9 game types as full or modified galaxies
- Lazy loading for Traveller-style galaxies

### 7.3 Key Files

| File | Role |
|------|------|
| `GalaxyStartupRegistry.java` | Maps game types to galaxy templates — needs fix |
| `GalaxyTemplate.java` | 8 templates including EARTH_2026_TEMPLATE |
| `GalaxyGenerationConfig.java` | 27 generation parameters |
| `EconomyBridge.java` | Earth 2026 → Transport Tycoon economic layer |
| `TransportBridge.java` | Earth 2026 → Transport Tycoon transport layer |
| `CombatService.java` | Unified combat interface |
| `DelegatingCombatService.java` | Routes to game-specific combat |
| `OverlayBuilderRegistry.java` | Maps GameType to overlay builders |
| `GameModule.java` | Unified interface for all game modules |
| `HqPortal.java` | 16 portals connecting Earth 2026 to other galaxies |

### 7.4 Next Steps

1. **Fix GalaxyStartupRegistry** — use correct templates for each game type
2. **Remove non-galaxy game types** from galaxy registry (Third Reich, FPS, Transport Tycoon)
3. **Implement lazy loading** for Traveller-style galaxies
4. **Wire up portal network** as galaxy router
5. **Complete overlay builders** for remaining game types (11 of 16 need builders)

---

*This analysis is based on the current codebase state as of v0.6.9 (2026-11-22). The infrastructure for the unified hub vision is already in place — the main change needed is updating GalaxyStartupRegistry to use correct templates and removing game types that should be Earth 2026 layers rather than separate galaxies.*