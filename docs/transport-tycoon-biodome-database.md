# Transport Tycoon Biodome Database

> Complete object database for populating biodomes in the DoraMCU galaxy, using the D&D Dungeon Building module for 3D interior experiences.

---

## Table of Contents

1. [Overview](#overview)
2. [3D Coordinate System](#3d-coordinate-system)
3. [Biodome Structure](#biodome-structure)
4. [Terrain & Biome Objects](#terrain--biome-objects)
5. [Settlement Objects (Houses, Buildings)](#settlement-objects-houses-buildings)
6. [Industry Objects](#industry-objects)
7. [Transport Points](#transport-points)
8. [Vehicle Objects](#vehicle-objects)
9. [Cargo & Economy Objects](#cargo--economy-objects)
10. [Corporation & Empire Objects](#corporation--empire-objects)
11. [D&D Dungeon Room Integration](#dnd-dungeon-room-integration)
12. [Interior Room Database by Building Type](#interior-room-database-by-building-type)
13. [Database Schema Summary](#database-schema-summary)
14. [Galaxy Overlord Build Flow](#galaxy-overlord-build-flow)

---

## Overview

This document defines the complete database of objects needed to populate each **biodome** (planet surface hex grid) in the Transport Tycoon module. Every object is positioned using the `Hex3D` cubic coordinate system and can be rendered as a 3D experience when the game client displays the character's world.

The **D&D Dungeon Building module** (`DungeonRoomService`) provides the interior room system for every building, station, and structure — enabling the Galaxy Overlord to construct fully explorable 3D interiors.

---

## 3D Coordinate System

All objects use the **Hex3D cubic coordinate system** where `x + y + z = 0`:

| Field | Type | Description |
|-------|------|-------------|
| `x` | `int` | Cubic X coordinate |
| `y` | `int` | Cubic Y coordinate |
| `z` | `int` | Cubic Z coordinate (derived: `-x - y`) |
| `state` | `HexState` | EMPTY, OCCUPIED, SELECTED, BLOCKED, DESTROYED |
| `payload` | `Object` | TerrainData or other cell-specific data |

**Hex3D States:**
- `EMPTY` — No structure, can place objects
- `OCCUPIED` — Has a structure (industry, station, settlement)
- `SELECTED` — Currently selected by player
- `BLOCKED` — Terrain-impassable (mountain, deep ocean)
- `DESTROYED` — Structure destroyed, can be repaired

---

## Biodome Structure

A biodome is a complete planet surface represented as a hex grid. Each biodome contains:

| Component | Model | Description |
|-----------|-------|-------------|
| Planet Surface | `Hex3DMap` | Collection of all hex cells |
| Terrain Cells | `TerrainData` | Biome + infrastructure per hex |
| Settlements | `Settlement` | Towns/cities with population |
| Industries | `Industry` | Production facilities |
| Stations | `Station` | Transport loading points |
| Vehicles | `SurfaceVehicle` | Moving transport units |
| Corporations | `TransportCorporation` | Economic entities |
| Trade Routes | `CampaignTradeRoute` | Connected station paths |
| Contracts | `PlanetContract` | Delivery missions |
| Quests | `RpgQuest` | Narrative objectives |

---

## Terrain & Biome Objects

### TerrainType (15 types)

The physical terrain of each hex cell determines passability, settlement allowance, and movement cost.

| Terrain Type | Color | Land Passable | Water Navigable | Settlement Allowed | Movement Cost |
|-------------|-------|---------------|-----------------|--------------------|---------------|
| `DEEP_OCEAN` | `#1a3a5c` | No | Yes | No | 1.0 |
| `SHALLOW_WATER` | `#2e6b9e` | No | Yes | No | 1.0 |
| `BEACH` | `#d4c5a0` | Yes | No | No | 1.2 |
| `GRASSLAND` | `#4a8c3f` | Yes | No | **Yes** | 1.0 |
| `FOREST` | `#2d5a1e` | Yes | No | Yes | 1.5 |
| `DESERT` | `#c2a661` | Yes | No | Yes | 1.3 |
| `TUNDRA` | `#8a9ba8` | Yes | No | Yes | 1.4 |
| `ICE` | `#d8e8f0` | Yes | No | No | 1.6 |
| `MOUNTAIN` | `#6b6b6b` | No | No | No | 0.0 (blocked) |
| `HIGHLAND` | `#7a7a5a` | Yes | No | Yes | 1.8 |
| `SWAMP` | `#4a5a3a` | Yes | No | No | 2.0 |
| `VOLCANIC` | `#5a2a1a` | No | No | No | 0.0 (blocked) |
| `JUNGLE` | `#1a4a0a` | Yes | No | No | 2.2 |
| `SAVANNA` | `#8a7a3a` | Yes | No | **Yes** | 1.1 |
| `WASTELAND` | `#5a5a4a` | Yes | No | No | 1.5 |

### Biome

Combines terrain with climate data for procedural world generation.

| Field | Type | Description |
|-------|------|-------------|
| `terrainType` | `TerrainType` | Physical terrain |
| `temperature` | `double` | -50°C to +50°C |
| `moisture` | `double` | 0.0 (arid) to 1.0 (humid) |
| `altitude` | `int` | Elevation in meters |
| `name` | `String` | Generated name (e.g., "Tropical Rainforest") |

**Generated Biome Names:**
- Frozen Ocean, Ocean
- Hot Desert, Cold Desert
- Tropical Grassland, Warm Grassland, Temperate Grassland, Cold Steppe
- Tropical Rainforest, Temperate Forest, Boreal Forest, Mixed Forest
- Tundra, Ice Sheet
- High Mountain, Mountain
- Jungle, Savanna
- Tropical Swamp, Swamp

### TerrainData

Payload stored on each Hex3D cell.

| Field | Type | Description |
|-------|------|-------------|
| `biome` | `Biome` | Terrain + climate |
| `planetId` | `int` | Planet reference |
| `settlementId` | `String` | Settlement on this cell (nullable) |
| `industryId` | `String` | Industry on this cell (nullable) |
| `infrastructure` | `InfrastructureType` | Built infrastructure |
| `waterLevel` | `int` | Water elevation |

### InfrastructureType (6 types)

| Type | Description | Required For |
|------|-------------|--------------|
| `NONE` | No infrastructure | — |
| `ROAD` | Road segment | Road vehicles (Truck, Bus, Express Truck) |
| `RAIL` | Railway line | Rail vehicles (Freight Train, Passenger Train) |
| `STATION` | Cargo loading station | All vehicle types |
| `PORT` | Water cargo dock | Water vehicles (Cargo Ship, Passenger Ferry) |
| `AIRPORT` | Air transport hub | Air vehicles (Aircraft) |

---

## Settlement Objects (Houses, Buildings)

### Settlement

Represents a town or city that grows based on cargo delivery.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique settlement ID |
| `name` | `String` | Human-readable name |
| `centerHex` | `Hex3D` | Center position on planet |
| `occupiedHexes` | `List<Hex3D>` | All hex cells the settlement occupies |
| `population` | `int` | Current population |
| `maxPopulation` | `int` | Maximum population for size |
| `growthRate` | `int` | Turns between growth checks |
| `size` | `Settlement.Size` | VILLAGE, TOWN, CITY, METROPOLIS |
| `cargoGoals` | `Map<CargoType, Integer>` | Desired cargo per turn |
| `cargoDelivered` | `Map<CargoType, Integer>` | Cargo received this turn |
| `mailProduction` | `int` | Mail generated per turn |
| `passengerProduction` | `int` | Passengers generated per turn |
| `active` | `boolean` | Whether settlement is active |
| `ownerEmpireId` | `String` | Owning empire (nullable) |

### Settlement Size Categories

| Size | Min Population | Max Population | Growth Interval (turns) |
|------|---------------|----------------|------------------------|
| `VILLAGE` | 100 | 500 | 30 |
| `TOWN` | 500 | 2,000 | 20 |
| `CITY` | 2,000 | 10,000 | 15 |
| `METROPOLIS` | 10,000 | 50,000 | 10 |

### Settlement Building Types (for 3D Interior Generation)

Each settlement generates these building types as dungeon rooms:

| Building Type | Dungeon Theme | Description | 3D Model |
|--------------|---------------|-------------|----------|
| **Residential House** | `Social` | Living quarters for population | Small house mesh |
| **Town Hall** | `Social` | Government and administration | Large civic building |
| **Market** | `Social` | Trading post for goods exchange | Open-air market |
| **Warehouse** | `Storage` | Cargo storage facility | Industrial warehouse |
| **Inn** | `Social` | Rest and lodging for travelers | Multi-story inn |
| **Workshop** | `Crafting` | Small-scale production | Workshop building |
| **Church/Temple** | `Social` | Religious/cultural center | Temple structure |
| **School** | `Social` | Education facility | School building |
| **Hospital** | `Social` | Medical facility | Hospital building |
| **Police Station** | `Combat` | Law enforcement | Police station |
| **Factory** | `Industry` | Manufacturing plant | Factory complex |
| **Farm** | `Industry` | Agricultural production | Farm buildings |

---

## Industry Objects

### Industry

Individual production facility on the planet surface.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique industry ID |
| `type` | `IndustryType` | Defines inputs/outputs |
| `position` | `Hex3D` | Hex cell location |
| `inputInventory` | `Map<CargoType, Integer>` | Raw materials waiting |
| `outputInventory` | `Map<CargoType, Integer>` | Produced goods waiting |
| `productionLevel` | `double` | 0.0 to 1.0 efficiency |
| `turnsSinceBuilt` | `int` | Age of industry |
| `active` | `boolean` | Operational status |
| `ownerEmpireId` | `String` | Owning empire (nullable) |

### IndustryType (20 types across 3 production levels)

#### Primary Industries (Raw Materials — 8 types)

| Type | Display Name | Output | Required Terrain | Build Cost | Maintenance |
|------|-------------|--------|-----------------|------------|-------------|
| `MINE` | Mine | Ore (10) | Mountain, Highland | 500 | 10 |
| `COAL_MINE` | Coal Mine | Coal (10) | Mountain, Highland, Grassland | 400 | 8 |
| `OIL_WELL` | Oil Well | Crude Oil (8) | Desert, Grassland, Deep Ocean | 800 | 15 |
| `LOGGING_CAMP` | Logging Camp | Wood (12) | Forest, Jungle | 300 | 5 |
| `FARM` | Farm | Grain (10) | Grassland, Savanna | 250 | 5 |
| `RANCH` | Ranch | Livestock (6) | Grassland, Savanna | 350 | 7 |
| `FISHING_PORT` | Fishing Port | Fish (8) | Shallow Water, Beach | 400 | 8 |
| `ORCHARD` | Orchard | Fruit (8) | Grassland, Forest, Savanna | 300 | 6 |

#### Secondary Industries (Processed Materials — 8 types)

| Type | Display Name | Output | Input | Required Terrain | Build Cost | Maintenance |
|------|-------------|--------|-------|-----------------|------------|-------------|
| `STEEL_MILL` | Steel Mill | Steel (6) | Ore (8) + Coal (4) | Grassland, Desert, Highland | 1,200 | 20 |
| `CEMENT_PLANT` | Cement Plant | Concrete (8) | Ore (6) | Grassland, Desert, Mountain | 800 | 12 |
| `GLASS_FACTORY` | Glass Factory | Glass (6) | Concrete (4) + Coal (2) | Grassland, Desert | 900 | 15 |
| `SAWMILL` | Sawmill | Lumber (8) | Wood (10) | Forest, Grassland | 500 | 8 |
| `BAKERY` | Bakery | Bread (10) | Grain (8) | Grassland, Savanna | 400 | 6 |
| `SLAUGHTERHOUSE` | Slaughterhouse | Meat (6) | Livestock (8) | Grassland, Savanna | 450 | 7 |
| `REFINERY` | Refinery | Refined Oil (6) | Oil (8) | Desert, Grassland | 1,500 | 25 |
| `CHEMICAL_PLANT` | Chemical Plant | Plastic (5) | Refined Oil (6) | Desert, Grassland | 1,200 | 20 |

#### Tertiary Industries (Advanced Goods — 4 types)

| Type | Display Name | Output | Input | Required Terrain | Build Cost | Maintenance |
|------|-------------|--------|-------|-----------------|------------|-------------|
| `ELECTRONICS_FACTORY` | Electronics Factory | Electronics (3) | Steel (4) + Glass (3) + Plastic (2) | Grassland | 3,000 | 50 |
| `MACHINE_SHOP` | Machine Shop | Machinery (3) | Steel (6) + Refined Oil (2) | Grassland, Desert | 2,500 | 40 |
| `LUXURY_FACTORY` | Luxury Factory | Luxury Goods (2) | Fruit (4) + Bread (2) + Plastic (2) | Grassland | 2,000 | 35 |
| `ADVANCED_CHEMICALS` | Advanced Chemicals | Chemicals (3) | Plastic (4) + Refined Oil (3) | Desert, Grassland | 2,200 | 38 |

### Industry 3D Interior Rooms (D&D Dungeon Integration)

Each industry type generates these interior rooms:

| Industry Type | Interior Rooms | Dungeon Theme |
|--------------|---------------|---------------|
| Mine | Entrance, Shaft, Ore Storage, Equipment Room, Break Room | `Trap` |
| Coal Mine | Entrance, Tunnel, Coal Storage, Ventilation, Break Room | `Trap` |
| Oil Well | Platform, Pump Room, Storage Tank, Control Room | `Trap` |
| Logging Camp | Saw Area, Wood Storage, Tool Shed, Camp Quarters | `Social` |
| Farm | Barn, Grain Silo, Tool Shed, Farmhouse | `Social` |
| Ranch | Barn, Pasture Office, Feed Storage, Ranch House | `Social` |
| Fishing Port | Dock Office, Fish Storage, Net Repair, Boathouse | `Social` |
| Orchard | Greenhouse, Fruit Storage, Packing Room, Farmhouse | `Social` |
| Steel Mill | Furnace Room, Rolling Mill, Steel Storage, Control Room | `Trap` |
| Cement Plant | Crusher Room, Kiln, Storage, Control Room | `Trap` |
| Glass Factory | Melting Room, Shaping Area, Storage, Control Room | `Trap` |
| Sawmill | Saw Area, Lumber Storage, Dry Room, Office | `Social` |
| Bakery | Kitchen, Oven Room, Storage, Shop Front | `Social` |
| Slaughterhouse | Processing Room, Cold Storage, Office | `Trap` |
| Refinery | Distillation Tower, Storage Tanks, Control Room | `Trap` |
| Chemical Plant | Mixing Room, Storage, Lab, Control Room | `Trap` |
| Electronics Factory | Assembly Line, Clean Room, Storage, R&D Lab | `Social` |
| Machine Shop | Machine Bay, Tool Room, Parts Storage, Office | `Social` |
| Luxury Factory | Design Studio, Assembly, Quality Control, Showroom | `Social` |
| Advanced Chemicals | Lab, Mixing Room, Storage, Safety Room | `Trap` |

---

## Transport Points

### Station

Cargo and passenger loading/unloading hub.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique station ID |
| `name` | `String` | Human-readable name |
| `position` | `Hex3D` | Hex cell location |
| `catchmentArea` | `List<Hex3D>` | Adjacent hexes that feed cargo |
| `cargoWaiting` | `Map<CargoType, Integer>` | Cargo awaiting pickup |
| `passengersWaiting` | `int` | Passengers awaiting transport |
| `totalIncome` | `int` | Cumulative income from deliveries |
| `connectedSettlementId` | `String` | Linked settlement (nullable) |
| `connectedIndustryId` | `String` | Linked industry (nullable) |
| `infrastructureType` | `InfrastructureType` | STATION, PORT, or AIRPORT |
| `ownerEmpireId` | `String` | Owning empire (nullable) |

### Transport Point Types (for 3D Rendering)

Each transport point type has a distinct 3D model and interior room layout:

| Transport Point | InfrastructureType | 3D Model | Interior Rooms |
|----------------|-------------------|----------|----------------|
| **Bus Stop** | `STATION` | Simple shelter with bench | Waiting Area, Ticket Booth |
| **Rail Station** | `STATION` | Platform with tracks | Waiting Hall, Ticket Office, Platform, Control Room, Storage |
| **Boat Dock / Port** | `PORT` | Waterfront dock with cranes | Dock Office, Cargo Warehouse, Passenger Terminal, Captain's Quarters |
| **Airport** | `AIRPORT` | Runway with terminal building | Check-in Hall, Security, Gate Area, Control Tower, Hangar, Maintenance Bay |
| **Cargo Station** | `STATION` | Industrial loading dock | Loading Bay, Warehouse, Office, Security Room |
| **Space Port** | `AIRPORT` | Advanced space docking facility | Check-in, Customs, Docking Bay, Hangar, Command Center, Fuel Station |

---

## Vehicle Objects

### SurfaceVehicle

Individual transport unit moving along routes.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique vehicle ID |
| `type` | `VehicleType` | Defines speed, capacity, costs |
| `currentPosition` | `Hex3D` | Current hex location |
| `routeStationIds` | `List<String>` | Ordered station route |
| `currentRouteIndex` | `int` | Current position in route |
| `cargoInventory` | `Map<CargoType, Integer>` | Cargo being carried |
| `passengerCount` | `int` | Passengers on board |
| `totalDistance` | `int` | Lifetime distance traveled |
| `totalCargoDelivered` | `int` | Lifetime cargo delivered |
| `totalPassengersDelivered` | `int` | Lifetime passengers delivered |
| `active` | `boolean` | Operational status |
| `ownerEmpireId` | `String` | Owning empire (nullable) |

### VehicleType (8 types across 4 categories)

#### Road Vehicles (3 types)

| Type | Display Name | Speed (hexes/turn) | Cargo Cap | Passenger Cap | Build Cost | Maintenance | Infrastructure |
|------|-------------|-------------------|-----------|---------------|------------|-------------|----------------|
| `TRUCK` | Truck | 3 | 20 | 0 | 100 | 5 | ROAD |
| `BUS` | Bus | 4 | 0 | 30 | 80 | 4 | ROAD |
| `EXPRESS_TRUCK` | Express Truck | 5 | 15 | 0 | 200 | 8 | ROAD |

#### Rail Vehicles (2 types)

| Type | Display Name | Speed (hexes/turn) | Cargo Cap | Passenger Cap | Build Cost | Maintenance | Infrastructure |
|------|-------------|-------------------|-----------|---------------|------------|-------------|----------------|
| `FREIGHT_TRAIN` | Freight Train | 6 | 100 | 0 | 500 | 15 | RAIL |
| `PASSENGER_TRAIN` | Passenger Train | 8 | 0 | 80 | 400 | 12 | RAIL |

#### Water Vehicles (2 types)

| Type | Display Name | Speed (hexes/turn) | Cargo Cap | Passenger Cap | Build Cost | Maintenance | Infrastructure |
|------|-------------|-------------------|-----------|---------------|------------|-------------|----------------|
| `CARGO_SHIP` | Cargo Ship | 2 | 80 | 0 | 300 | 10 | WATER |
| `PASSENGER_FERRY` | Passenger Ferry | 3 | 10 | 50 | 250 | 8 | WATER |

#### Air Vehicles (1 type)

| Type | Display Name | Speed (hexes/turn) | Cargo Cap | Passenger Cap | Build Cost | Maintenance | Infrastructure |
|------|-------------|-------------------|-----------|---------------|------------|-------------|----------------|
| `AIRCRAFT` | Aircraft | 10 | 10 | 10 | 800 | 25 | AIRPORT |

### Vehicle 3D Models

| Vehicle | 3D Model Description | Interior (D&D Room) |
|---------|---------------------|---------------------|
| Truck | Cargo truck with trailer | Cargo Bay, Driver Cab |
| Bus | Passenger bus | Passenger Area, Driver Cab |
| Express Truck | Streamlined fast truck | Cargo Bay, Driver Cab, Navigation |
| Freight Train | Multi-car train | Engine Cab, Cargo Cars (multiple rooms) |
| Passenger Train | Multi-car train | Engine Cab, Passenger Cars, Dining Car |
| Cargo Ship | Large ocean vessel | Bridge, Cargo Hold, Engine Room, Crew Quarters |
| Passenger Ferry | Medium ferry | Bridge, Passenger Deck, Lower Deck, Crew Quarters |
| Aircraft | Jet aircraft | Cockpit, Cargo/Passenger Bay, Galley |

---

## Cargo & Economy Objects

### CargoType (22 types across 4 categories)

#### Raw Materials (9 types)

| Type | Display Name | Color | Perishable | Base Value |
|------|-------------|-------|------------|------------|
| `ORE` | Ore | `#8B8682` | No | 10 |
| `COAL` | Coal | `#2C2C2C` | No | 12 |
| `OIL` | Crude Oil | `#1A1A2E` | No | 15 |
| `WOOD` | Wood | `#8B4513` | No | 8 |
| `GRAIN` | Grain | `#DAA520` | **Yes** | 6 |
| `LIVESTOCK` | Livestock | `#8FBC8F` | **Yes** | 10 |
| `FISH` | Fish | `#4682B4` | **Yes** | 9 |
| `FRUIT` | Fruit | `#FF6347` | **Yes** | 7 |
| `MAIL` | Mail | `#FFD700` | **Yes** | 5 |

#### Processed Materials (8 types)

| Type | Display Name | Color | Perishable | Base Value |
|------|-------------|-------|------------|------------|
| `STEEL` | Steel | `#708090` | No | 25 |
| `CONCRETE` | Concrete | `#A9A9A9` | No | 15 |
| `GLASS` | Glass | `#87CEEB` | No | 18 |
| `LUMBER` | Lumber | `#DEB887` | No | 12 |
| `BREAD` | Bread | `#F5DEB3` | **Yes** | 10 |
| `MEAT` | Meat | `#CD5C5C` | **Yes** | 14 |
| `REFINED_OIL` | Refined Oil | `#4A4A6A` | No | 30 |
| `PLASTIC` | Plastic | `#FF6B6B` | No | 22 |

#### Advanced Goods (4 types)

| Type | Display Name | Color | Perishable | Base Value |
|------|-------------|-------|------------|------------|
| `ELECTRONICS` | Electronics | `#00CED1` | No | 45 |
| `MACHINERY` | Machinery | `#696969` | No | 40 |
| `LUXURY` | Luxury Goods | `#FFD700` | No | 55 |
| `CHEMICALS` | Chemicals | `#9370DB` | No | 35 |

#### Special (1 type)

| Type | Display Name | Color | Perishable | Base Value |
|------|-------------|-------|------------|------------|
| `PASSENGERS` | Passengers | `#FF69B4` | **Yes** | 20 |

### Cargo Flow Diagram

```
PRIMARY INDUSTRIES          SECONDARY INDUSTRIES          TERTIARY INDUSTRIES
─────────────────          ─────────────────────         ───────────────────
Mine ──Ore──►              Steel Mill ──Steel──►         Electronics Factory ──Electronics──►
Coal Mine ──Coal──►        Cement Plant ──Concrete──►    Machine Shop ──Machinery──►
Oil Well ──Oil──►          Glass Factory ──Glass──►      Luxury Factory ──Luxury Goods──►
Logging ──Wood──►          Sawmill ──Lumber──►           Adv. Chemicals ──Chemicals──►
Farm ──Grain──►            Bakery ──Bread──►
Ranch ──Livestock──►       Slaughterhouse ──Meat──►
Fishing ──Fish──►          Refinery ──Refined Oil──►
Orchard ──Fruit──►         Chemical Plant ──Plastic──►

SETTLEMENTS
───────────
Village ──Mail──►          Consumes: Bread, Meat, Luxury
Town ──Passengers──►       Produces: Mail, Passengers
City ──Mail──►
Metropolis ──Passengers──►
```

---

## Corporation & Empire Objects

### TransportCorporation

Economic entity that owns stations, vehicles, and trade routes.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique corporation ID (UUID) |
| `name` | `String` | Corporation name |
| `ownerId` | `String` | Empire/player owner ID |
| `rank` | `CorporationRank` | Current career rank |
| `credits` | `long` | Available credits |
| `reputation` | `int` | 0-100 reputation score |
| `xp` | `int` | Experience points |
| `ownedStationIds` | `Set<String>` | Station ownership |
| `ownedVehicleIds` | `Set<String>` | Vehicle ownership |
| `ownedRouteIds` | `Set<String>` | Trade route ownership |
| `activeContractIds` | `Set<String>` | Active contracts |
| `planetsOperated` | `int` | Number of active planets |
| `totalCargoDelivered` | `long` | Lifetime cargo value |
| `cargoInventory` | `Map<CargoType, Integer>` | Current cargo holdings |
| `xpToNextRank` | `int` | XP needed for next rank |
| `activeQuestIds` | `List<String>` | Active RPG quests |
| `completedQuestIds` | `List<String>` | Completed quest history |

### CorporationRank (5 ranks)

| Rank | Title | Min Credits | Max Planets | Max Routes | Max Vehicles |
|------|-------|-------------|-------------|------------|--------------|
| `FREELANCER` | Freelancer | 0 | 1 | 2 | 1 |
| `STARTUP` | Startup | 5,000 | 2 | 5 | 3 |
| `REGIONAL` | Regional Carrier | 50,000 | 3 | 15 | 5 |
| `INTERPLANETARY` | Interplanetary Logistics | 500,000 | 5 | 50 | 10 |
| `GALACTIC_MONOPOLY` | Galactic Monopoly | 5,000,000 | 10 | Unlimited | 20 |

### PlanetContract

Delivery mission for corporations.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique contract ID |
| `type` | `PlanetContractType` | Contract category |
| `cargoType` | `CargoType` | Cargo to deliver |
| `amount` | `int` | Units to deliver |
| `fromStationId` | `String` | Pickup station |
| `toStationId` | `String` | Delivery station |
| `reward` | `int` | Credit reward |
| `deadline` | `int` | Turn deadline |
| `status` | `String` | ACTIVE, COMPLETED, FAILED |

### PlanetContractType

| Type | Description |
|------|-------------|
| `CARGO_DELIVERY` | Standard cargo delivery |
| `PASSENGER_DELIVERY` | Passenger transport |
| `MAIL_DELIVERY` | Mail delivery (time-sensitive) |
| `EXPRESS_DELIVERY` | High-value urgent delivery |
| `BULK_DELIVERY` | Large quantity delivery |
| `SPECIAL_DELIVERY` | Rare cargo with bonus reward |

### RpgQuest

Narrative quest from AI empires.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique quest ID |
| `title` | `String` | Quest title |
| `description` | `String` | Quest narrative |
| `issuerEmpireId` | `String` | AI empire that issued the quest |
| `objectives` | `List<String>` | Quest objectives |
| `rewards` | `int` | Credit reward |
| `xpReward` | `int` | XP reward |
| `status` | `String` | ACTIVE, COMPLETED, FAILED |

---

## D&D Dungeon Room Integration

The `DungeonRoomService` provides the interior room system for all 3D structures. Every building, station, and vehicle can have interior rooms that the character can explore.

### DungeonRoom Schema

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Unique room ID |
| `name` | `String` | Room name (e.g., "Loading Bay") |
| `description` | `String` | Room description |
| `dungeonId` | `String` | Parent building/station ID |
| `theme` | `String` | Combat, Trap, Treasure, Social, Crafting, Storage, Industry |
| `size` | `String` | Small, Medium, Large, Huge |
| `connections` | `String` | Comma-separated connected room IDs |
| `trap` | `String` | Trap description (nullable) |
| `trapDc` | `Integer` | Trap difficulty class |
| `trapDamage` | `String` | Trap damage type |
| `trapDamageExpr` | `String` | Trap damage expression (e.g., "2d6") |
| `treasure` | `String` | Treasure description (nullable) |
| `goldPieces` | `Integer` | Gold value |
| `magicRarity` | `String` | Common, Uncommon, Rare, Very Rare, Legendary |
| `encounter` | `String` | Encounter description (nullable) |
| `encounterDifficulty` | `String` | Easy, Medium, Hard, Deadly |
| `xpReward` | `Integer` | XP for clearing room |
| `minLevel` | `Integer` | Minimum character level |
| `maxLevel` | `Integer` | Maximum character level |
| `discovered` | `boolean` | Whether room is revealed |
| `active` | `boolean` | Whether room is active |
| `dmSecrets` | `String` | DM-only secret information |

### Dungeon Service Methods

| Method | Description |
|--------|-------------|
| `createRoom(room)` | Create a new interior room |
| `getRoom(id)` | Get room by ID |
| `listRoomsByDungeon(dungeonId)` | List all rooms in a building |
| `listRoomsByTheme(theme)` | List rooms by theme |
| `discoverRoom(id)` | Mark room as discovered |
| `addConnection(fromId, toId)` | Connect two rooms |
| `getConnectedRooms(roomId)` | Get adjacent rooms |
| `createCircularConnections(dungeonId)` | Link all rooms in a building |
| `makeTrapCheck(roomId, dexMod, profBonus, roll)` | Resolve trap encounter |
| `discoverTreasure(roomId)` | Reveal treasure in room |

---

## Interior Room Database by Building Type

This section defines the complete set of interior rooms for every building type in the biodome. Each room maps to a `DungeonRoom` instance.

### 1. Residential House (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| RH-01 | Front Porch | Social | Small | RH-02 | None | None | None |
| RH-02 | Living Room | Social | Medium | RH-01, RH-03, RH-04 | None | None | None |
| RH-03 | Kitchen | Social | Small | RH-02, RH-05 | None | None | None |
| RH-04 | Bedroom | Social | Small | RH-02 | None | Hidden Cache (Common) | None |
| RH-05 | Storage Closet | Storage | Small | RH-03 | None | None | None |

### 2. Town Hall (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| TH-01 | Grand Entrance | Social | Large | TH-02, TH-03 | None | None | None |
| TH-02 | Council Chamber | Social | Large | TH-01, TH-04 | None | None | NPC Dialogue |
| TH-03 | Public Records | Social | Medium | TH-01 | None | None | None |
| TH-04 | Mayor's Office | Social | Medium | TH-02 | None | None | Quest Giver |
| TH-05 | Vault | Storage | Small | TH-04 | Lock Trap (DC 15) | Gold Reserve (Rare) | Guard Encounter |

### 3. Market (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| MK-01 | Market Entrance | Social | Large | MK-02, MK-03, MK-04 | None | None | None |
| MK-02 | Food Stall Area | Social | Medium | MK-01 | None | None | Merchant NPC |
| MK-03 | Goods Stall Area | Social | Medium | MK-01 | None | None | Merchant NPC |
| MK-04 | Auction House | Social | Medium | MK-01 | None | Rare Item (Uncommon) | Auction Event |

### 4. Warehouse (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| WH-01 | Loading Dock | Storage | Large | WH-02, WH-03 | None | None | None |
| WH-02 | Main Storage | Storage | Large | WH-01, WH-04 | None | None | None |
| WH-03 | Office | Social | Small | WH-01 | None | None | Clerk NPC |
| WH-04 | Secure Storage | Storage | Medium | WH-02 | Lock Trap (DC 12) | Valuable Cargo (Uncommon) | Thief Encounter |

### 5. Bus Stop (Transport Point)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| BS-01 | Waiting Area | Social | Small | BS-02 | None | None | None |
| BS-02 | Ticket Booth | Social | Small | BS-01 | None | None | Ticket Agent NPC |

### 6. Rail Station (Transport Point)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| RS-01 | Main Entrance | Social | Large | RS-02, RS-03 | None | None | None |
| RS-02 | Waiting Hall | Social | Large | RS-01, RS-04, RS-05 | None | None | None |
| RS-03 | Ticket Office | Social | Medium | RS-01 | None | None | Ticket Agent NPC |
| RS-04 | Platform A | Social | Large | RS-02 | None | None | Train Arrival Event |
| RS-05 | Platform B | Social | Large | RS-02 | None | None | Train Arrival Event |
| RS-06 | Control Room | Social | Small | RS-04 | None | None | Station Master NPC |
| RS-07 | Storage Room | Storage | Small | RS-06 | None | None | None |

### 7. Boat Dock / Port (Transport Point)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| BD-01 | Dock Entrance | Social | Large | BD-02, BD-03 | None | None | None |
| BD-02 | Cargo Warehouse | Storage | Large | BD-01, BD-04 | None | None | None |
| BD-03 | Passenger Terminal | Social | Medium | BD-01, BD-05 | None | None | None |
| BD-04 | Dock Office | Social | Small | BD-02 | None | None | Dock Master NPC |
| BD-05 | Captain's Quarters | Social | Small | BD-03 | None | Treasure Map (Uncommon) | Captain NPC |
| BD-06 | Waterfront | Social | Large | BD-02, BD-03 | None | None | Sea Monster Event (Rare) |

### 8. Airport (Transport Point)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| AP-01 | Main Terminal | Social | Large | AP-02, AP-03, AP-04 | None | None | None |
| AP-02 | Check-in Hall | Social | Large | AP-01, AP-05 | None | None | Check-in Agent NPC |
| AP-03 | Security Check | Combat | Medium | AP-01 | Scanner Trap (DC 10) | None | Security Encounter |
| AP-04 | Gate Area | Social | Large | AP-01, AP-06 | None | None | Boarding Event |
| AP-05 | Control Tower | Social | Small | AP-02 | None | None | Air Traffic Controller NPC |
| AP-06 | Hangar | Storage | Large | AP-04 | None | None | None |
| AP-07 | Maintenance Bay | Crafting | Medium | AP-06 | None | None | Mechanic NPC |

### 9. Space Port (Advanced Transport Point)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| SP-01 | Grand Entrance | Social | Huge | SP-02, SP-03, SP-04 | None | None | None |
| SP-02 | Check-in Hall | Social | Large | SP-01, SP-05 | None | None | Check-in Agent NPC |
| SP-03 | Customs & Security | Combat | Large | SP-01 | Scanner Trap (DC 15) | None | Security Encounter |
| SP-04 | Observation Deck | Social | Large | SP-01 | None | None | Space View Event |
| SP-05 | Docking Bay Alpha | Social | Huge | SP-02 | None | None | Ship Arrival Event |
| SP-06 | Docking Bay Beta | Social | Huge | SP-02 | None | None | Ship Departure Event |
| SP-07 | Command Center | Social | Medium | SP-05 | None | None | Port Authority NPC |
| SP-08 | Hangar | Storage | Huge | SP-05, SP-09 | None | None | None |
| SP-09 | Maintenance Bay | Crafting | Large | SP-08 | None | None | Engineer NPC |
| SP-10 | Fuel Station | Storage | Medium | SP-08 | Explosion Trap (DC 18) | None | Hazard Event |

### 10. Inn (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| IN-01 | Entrance | Social | Medium | IN-02, IN-03 | None | None | None |
| IN-02 | Common Room | Social | Large | IN-01, IN-04 | None | None | Rumor Gossip Event |
| IN-03 | Kitchen | Social | Medium | IN-01 | None | None | Cook NPC |
| IN-04 | Room Corridor | Social | Medium | IN-02, IN-05, IN-06, IN-07 | None | None | None |
| IN-05 | Room 1 | Social | Small | IN-04 | None | Hidden Stash (Common) | None |
| IN-06 | Room 2 | Social | Small | IN-04 | None | None | None |
| IN-07 | Room 3 | Social | Small | IN-04 | None | None | None |

### 11. Church / Temple (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| CH-01 | Entrance | Social | Medium | CH-02 | None | None | None |
| CH-02 | Main Hall | Social | Large | CH-01, CH-03, CH-04 | None | None | Prayer Event |
| CH-03 | Chapel | Social | Small | CH-02 | None | Holy Relic (Rare) | None |
| CH-04 | Clergy Quarters | Social | Small | CH-02 | None | None | Priest NPC |

### 12. Police Station (Settlement Building)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| PS-01 | Entrance | Social | Medium | PS-02, PS-03 | None | None | None |
| PS-02 | Main Desk | Social | Medium | PS-01 | None | None | Officer NPC |
| PS-03 | Evidence Room | Storage | Small | PS-01 | Lock Trap (DC 14) | Stolen Goods (Uncommon) | None |
| PS-04 | Holding Cells | Combat | Medium | PS-02 | Alarm Trap (DC 12) | None | Prisoner Break Event |

### 13. Factory (Generic Industry Interior)

| Room ID | Name | Theme | Size | Connections | Trap | Treasure | Encounter |
|---------|------|-------|------|-------------|------|----------|-----------|
| FC-01 | Loading Dock | Storage | Large | FC-02, FC-03 | None | None | None |
| FC-02 | Production Floor | Industry | Large | FC-01, FC-04 | Machinery Trap (DC 13) | None | Accident Event |
| FC-03 | Raw Material Storage | Storage | Medium | FC-01 | None | None | None |
| FC-04 | Control Room | Social | Small | FC-02 | None | None | Foreman NPC |
| FC-05 | Break Room | Social | Small | FC-02 | None | None | Worker NPC |

---

## Database Schema Summary

### Entity Relationship Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        GALAXY OVERLORD                          │
│                  (GalaxyOverlordAI Service)                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         BIODOME                                 │
│                    (Planet Surface)                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Hex3DMap                                │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │  │
│  │  │ Hex3D   │ │ Hex3D   │ │ Hex3D   │ │ Hex3D   │ ...    │  │
│  │  │ (x,y,z) │ │ (x,y,z) │ │ (x,y,z) │ │ (x,y,z) │        │  │
│  │  │ Payload:│ │ Payload:│ │ Payload:│ │ Payload:│        │  │
│  │  │TerrainData│ │TerrainData│ │TerrainData│ │TerrainData│        │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Settlements ──► Stations ──► Industries ──► Vehicles           │
│       │               │              │              │            │
│       │               │              │              │            │
│       ▼               ▼              ▼              ▼            │
│  DungeonRooms    DungeonRooms   DungeonRooms   DungeonRooms     │
│  (Interiors)     (Interiors)    (Interiors)    (Interiors)      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    TRANSPORT NETWORK                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Corporation  │  │ Trade Routes │  │  Contracts   │          │
│  │ (Economy)    │  │ (Paths)      │  │  (Missions)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Cargo Types  │  │  Vehicles    │  │    Quests    │          │
│  │ (22 types)   │  │ (8 types)    │  │  (RPG)       │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### Complete Object Count Summary

| Category | Object Count | Description |
|----------|-------------|-------------|
| **Terrain Types** | 15 | Physical terrain for hex cells |
| **Biome Names** | 20+ | Procedural biome combinations |
| **Infrastructure Types** | 6 | Road, Rail, Station, Port, Airport, None |
| **Settlement Sizes** | 4 | Village, Town, City, Metropolis |
| **Settlement Buildings** | 12 | House, Town Hall, Market, Warehouse, etc. |
| **Industry Types** | 20 | 8 Primary + 8 Secondary + 4 Tertiary |
| **Transport Points** | 6 | Bus Stop, Rail Station, Port, Airport, Cargo Station, Space Port |
| **Vehicle Types** | 8 | 3 Road + 2 Rail + 2 Water + 1 Air |
| **Cargo Types** | 22 | 9 Raw + 8 Processed + 4 Advanced + 1 Special |
| **Corporation Ranks** | 5 | Freelancer → Galactic Monopoly |
| **Contract Types** | 6 | Cargo, Passenger, Mail, Express, Bulk, Special |
| **Encounter Types** | 9 | Economic, Contract, Conflict, Competition, Quest, Disaster, Technology, Diplomacy |
| **Dungeon Themes** | 7 | Combat, Trap, Treasure, Social, Crafting, Storage, Industry |
| **Interior Room Templates** | 13+ | House, Town Hall, Market, Warehouse, Bus Stop, Rail Station, Port, Airport, Space Port, Inn, Church, Police Station, Factory |

---

## Galaxy Overlord Build Flow

The Galaxy Overlord AI uses this database to construct the complete 3D biodome experience:

### Step 1: Generate Planet Surface
1. Create `Hex3DMap` with cubic coordinate grid
2. For each hex, generate `Biome` from noise functions (temperature, moisture, altitude)
3. Place `TerrainData` payload on each hex with biome and infrastructure

### Step 2: Place Settlements
1. Find suitable terrain (Grassland, Savanna with settlement allowed)
2. Create `Settlement` objects with size based on population
3. Generate interior `DungeonRoom` instances for each building type
4. Connect rooms with `addConnection()` for navigation

### Step 3: Place Industries
1. Match `IndustryType` to compatible terrain types
2. Create `Industry` objects at hex positions
3. Generate interior `DungeonRoom` instances for each industry
4. Link production chains (Primary → Secondary → Tertiary)

### Step 4: Build Transport Network
1. Place `Station` objects at strategic hex positions
2. Connect stations to settlements and industries
3. Generate interior `DungeonRoom` instances for each station type
4. Build infrastructure (Road, Rail) between stations

### Step 5: Deploy Vehicles
1. Create `SurfaceVehicle` objects with appropriate `VehicleType`
2. Assign vehicles to routes between stations
3. Generate interior `DungeonRoom` instances for vehicle interiors

### Step 6: Establish Economy
1. Create `TransportCorporation` for player/AI
2. Generate `PlanetContract` missions
3. Set up `RpgQuest` narrative objectives
4. Initialize cargo flow between industries and settlements

### Step 7: Enable 3D Exploration
1. All `DungeonRoom` instances are linked to parent structures
2. Character can enter any building via `discoverRoom()`
3. Interior navigation uses `getConnectedRooms()` for room-to-room movement
4. Traps, treasure, and encounters provide RPG depth

---

## API Integration Points

### GameModule Interface
The `TransportTycoonGameModule` implements all game-core interfaces:

| Interface | Adapter | Purpose |
|-----------|---------|---------|
| `DiceService` | `TycoonDiceAdapter` | d20 skill checks for business operations |
| `CombatService` | `TycoonCombatAdapter` | Economic warfare (market share battles) |
| `InventoryService` | `TycoonInventoryAdapter` | Cargo inventory management |
| `CraftingService` | `TycoonCraftingAdapter` | Building industries and infrastructure |
| `DungeonRoomService` | `TycoonDungeonRoomAdapter` | **Interior room system for all buildings** |
| `EncounterService` | `TycoonEncounterAdapter` | Market events, contracts, quests |
| `CharacterService` | `TycoonCharacterAdapter` | Corporation profiles and rank progression |
| `CampaignService` | `TycoonCampaignAdapter` | Turn-based campaigns with objectives |
| `RulesService` | `TycoonRulesAdapter` | Trade mechanics and cargo flow rules |

### Key Service Classes

| Service | Class | Description |
|---------|-------|-------------|
| Planet Surface | `PlanetSurfaceBuilder` | Generates hex grid and biomes |
| Transport Campaign | `TransportCampaignManager` | Manages turn-based gameplay |
| Trade Routes | `TradeRouteManager` | Manages vehicle routes |
| Contracts | `PlanetContractManager` | Generates and tracks contracts |
| Events | `PlanetEventManager` | Market events and disasters |
| Quests | `RpgQuestSystem` | Narrative quest generation |
| AI Strategy | `AiCorporationStrategy` | AI rival behavior |
| Galaxy Overlord | `GalaxyOverlordAI` | **Master builder for entire galaxy** |
| Market Share | `MarketShareTracker` | Economic competition tracking |
| Reputation | `EmpireReputationTracker` | Empire relationship tracking |
| Trade Policy | `EmpireTradePolicy` | Empire trade rules |

---

## 3D Client Rendering Guide

When the game client displays the character's world:

### Exterior View (Planet Surface)
- Render `Hex3DMap` as a hex grid
- Color each hex by `TerrainType.getColor()`
- Place 3D models at hex positions:
  - Settlements → Building cluster models
  - Industries → Factory/mine models
  - Stations → Transport hub models
  - Vehicles → Animated vehicle models
  - Infrastructure → Road/rail line overlays

### Interior View (Dungeon Rooms)
- When character enters a building, load `DungeonRoom` instances by `dungeonId`
- Render each room as a 3D space
- Connect rooms via `connections` field for navigation
- Display traps, treasure, and encounters as interactive elements
- Use `theme` to determine room aesthetics (Combat = hostile, Social = friendly, etc.)

### Vehicle Interior View
- When character boards a vehicle, load vehicle-specific `DungeonRoom` instances
- Show cargo inventory and passenger count
- Display route progress and next station

---

## Summary

This database provides **everything needed** for the Galaxy Overlord to build a complete 3D biodome experience:

- **15 terrain types** for world generation
- **20+ biome combinations** for climate variety
- **12 settlement building types** with interior rooms
- **20 industry types** across 3 production levels
- **6 transport point types** (bus stops, rail stations, boat docks, airports, cargo stations, space ports)
- **8 vehicle types** across 4 categories (road, rail, water, air)
- **22 cargo types** for economic simulation
- **5 corporation ranks** for progression
- **6 contract types** for missions
- **13+ interior room templates** using D&D dungeon system
- **Full 3D coordinate system** (Hex3D cubic coordinates)
- **DungeonRoomService integration** for all interior exploration

Every object is positioned in 3D space, every building has explorable interior rooms, and every transport point connects to the broader economy — creating a fully immersive 3D experience when the game client renders the character's world.
