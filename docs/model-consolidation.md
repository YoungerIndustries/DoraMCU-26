# Three-Way Model Duplication Resolution

## Problem

DoraMCU had three parallel model layers representing the same domain concepts:

| Layer | Package | Files | Purpose | JPA? |
|-------|---------|-------|---------|------|
| **model** | `com.yisy.modules.doramcu.model` | 21 | Basic JPA persistence entities | Yes |
| **hex** | `com.yisy.modules.doramcu.hex` | 134 | Rich domain models with 3D hex geometry | No |
| **game.model** | `com.yisy.modules.doramcu.game.model` | 30 | Unified 4X game entities | Yes |

### Examples of Duplication

- **Planet**: `model.Planet` (286 lines, simple JPA) vs `hex.Planet` (789 lines, rich domain with geometry) vs `game.model.GamePlanet` (238 lines, SRP-compliant JPA)
- **Player**: `model.Player` (178 lines, identity only) vs `hex.Player` (807 lines, identity + game state) vs `game.model.GamePlayer` (350 lines, unified identity + game state + resources)
- **Galaxy**: `model.Galaxy` (141 lines) vs `hex.Galaxy` (domain model) vs `game.model.GameGalaxy` (156 lines, with turn tracking)

## Solution

The `game.model` layer was chosen as the **canonical model layer** because:

1. It was explicitly designed as the replacement (Javadoc: "This replaces the duplicate Player classes")
2. It follows SRP (Single Responsibility Principle) with delegated stats/facilities
3. It has comprehensive JPA mappings with proper relationships
4. It includes resource pipeline integration (RawResourceType, RefinedResourceType, ConstructionModuleType)
5. It's the most recent and well-organized layer

### What Was Done

#### 1. Created Missing Repositories (12 new files)

The `game.model` layer lacked repository infrastructure. Created:

- `GameGalaxyRepository` — replaces `GalaxyRepository`
- `GamePlayerRepository` — replaces `PlayerRepository`
- `GamePlanetRepository` — replaces `PlanetRepository`
- `GameStarRepository` — replaces `StarRepository`
- `GameSolarSystemRepository` — replaces `SolarSystemRepository`
- `GameSectorRepository` — replaces `SectorRepository`
- `GameSubSectorRepository` — replaces `SubSectorRepository`
- `GameFleetRepository` — new (no equivalent in model package)
- `GameShipRepository` — new (no equivalent in model package)
- `GameSpaceStationRepository` — replaces `SpaceStationRepository`
- `GameEmpireRepository` — new (no equivalent in model package)
- `DoraTCharacterRepository` — new (no equivalent in model package)

All located in `com.yisy.modules.doramcu.game.repository`.

#### 2. Created Converter Class

`HexToGameModelConverter` bridges the `hex` domain model (used by GalaxyGenerator) with the `game.model` JPA layer:

- `convertGalaxy()` — hex.Galaxy → GameGalaxy
- `convertSector()` — hex.Galaxy.Sector → GameSector
- `convertSubSector()` — hex.SubSector → GameSubSector
- `convertSolarSystem()` — hex.SolarSystem → GameSolarSystem
- `convertStar()` — hex.Star → GameStar (maps StarType enum to spectral type)
- `convertPlanet()` — hex.Planet → GamePlanet (maps PlanetSize, UWP)
- `convertSpaceStation()` — hex.SpaceStation → GameSpaceStation

#### 3. Deprecated Old Model Entities

Added `@Deprecated` annotations with migration guidance to:

- `model.Planet` → use `game.model.GamePlanet`
- `model.Player` → use `game.model.GamePlayer`
- `model.Galaxy` → use `game.model.GameGalaxy`
- `model.Star` → use `game.model.GameStar`
- `model.SolarSystem` → use `game.model.GameSolarSystem`
- `model.Sector` → use `game.model.GameSector`
- `model.SubSector` → use `game.model.GameSubSector`
- `model.SpaceStation` → use `game.model.GameSpaceStation`

### What Remains

The `hex` package **remains as the domain logic layer**. It provides:

- 3D hex geometry (Hex3DMap, PlanetGeometry, StarGeometry)
- Galaxy generation (GalaxyGenerator, SolarSystemGenerator)
- Combat resolution (CombatResolver, GroundBattleResolver)
- Resource management (ResourceManager, ResourcePipelineManager)
- Game state management (TurnManager, FleetManager, PlanetManager)

The `hex` layer is **not JPA-persistent** — it's an in-memory domain model. The converter class bridges hex → game.model for persistence.

### Migration Path for Services

Services that currently use `model` package entities should migrate to `game.model`:

1. **GalaxyService** (844 lines) — currently converts hex → model. Should use HexToGameModelConverter to convert hex → game.model instead.
2. **PlayerService** — currently uses model.Player. Should use game.model.GamePlayer.
3. **GalaxyCreationService** — currently uses model.Galaxy. Should use game.model.GameGalaxy.
4. **AuthenticationController** — currently uses model.Player for PIN login. Should use game.model.GamePlayer.

### Files Created

| File | Purpose |
|------|---------|
| `game/repository/GameGalaxyRepository.java` | Galaxy persistence |
| `game/repository/GamePlayerRepository.java` | Player persistence |
| `game/repository/GamePlanetRepository.java` | Planet persistence |
| `game/repository/GameStarRepository.java` | Star persistence |
| `game/repository/GameSolarSystemRepository.java` | Solar system persistence |
| `game/repository/GameSectorRepository.java` | Sector persistence |
| `game/repository/GameSubSectorRepository.java` | Subsector persistence |
| `game/repository/GameFleetRepository.java` | Fleet persistence |
| `game/repository/GameShipRepository.java` | Ship persistence |
| `game/repository/GameSpaceStationRepository.java` | Space station persistence |
| `game/repository/GameEmpireRepository.java` | Empire persistence |
| `game/repository/DoraTCharacterRepository.java` | Character persistence |
| `game/converter/HexToGameModelConverter.java` | hex → game.model conversion |

### Files Modified

| File | Change |
|------|--------|
| `model/Planet.java` | Added `@Deprecated` annotation |
| `model/Player.java` | Added `@Deprecated` annotation |
| `model/Galaxy.java` | Added `@Deprecated` annotation |
| `model/Star.java` | Added `@Deprecated` annotation |
| `model/SolarSystem.java` | Added `@Deprecated` annotation |
| `model/Sector.java` | Added `@Deprecated` annotation |
| `model/SubSector.java` | Added `@Deprecated` annotation |
| `model/SpaceStation.java` | Added `@Deprecated` annotation |

### Next Steps

1. **Migrate GalaxyService** to use HexToGameModelConverter and game.model repositories
2. **Migrate PlayerService** to use GamePlayerRepository
3. **Migrate authentication** to use GamePlayer instead of model.Player
4. **Add Flyway migration** to create the game.model tables (game_galaxies, game_players, game_planets, etc.)
5. **Remove old model package** once all consumers are migrated
6. **Update tests** to use game.model entities

### Architecture After Consolidation

```
┌─────────────────────────────────────────────────────┐
│                    Controllers                       │
│  (GalaxyAdminController, GameController, etc.)      │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│                   Services                          │
│  (GalaxyService, GameService, PlayerService, etc.)  │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
┌────────▼────────┐    ┌────────▼────────────────┐
│    hex package   │    │    game.model package    │
│  (Domain Logic)  │    │  (Canonical JPA Layer)   │
│                  │    │                          │
│ GalaxyGenerator  │    │ GameGalaxy               │
│ Planet (789 loc) │    │ GamePlanet               │
│ Star (903 loc)   │    │ GamePlayer               │
│ Player (807 loc) │    │ GameSolarSystem          │
│ CombatResolver   │    │ GameFleet, GameShip      │
│ ...              │    │ GameEmpire               │
└──────────────────┘    └──────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │  game.repository   │
                    │  (14 repositories) │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │    Database        │
                    │  (game_* tables)   │
                    └───────────────────┘
```

The `hex` package stays as the in-memory domain logic layer. The `game.model` package is the single source of truth for persistence. The converter bridges them.
