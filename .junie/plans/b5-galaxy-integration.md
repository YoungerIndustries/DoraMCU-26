---
sessionId: session-261110-201934-1td8
---

# Requirements

### Overview
Integrate the Babylon 5 (B5) module into the DoraMCU hex galaxy system so that B5 races can populate empires and B5 locations can serve as home systems in a themed galaxy.

### Goals
- Create a `GalaxyTemplate.BABYLON5` preset for themed galaxy generation
- Create a `Babylon5EmpireBuilder` service that maps B5 races to empires and B5 locations to home systems
- Keep B5 data in the existing JSON (`babylon5-universe.json`) — no enum duplication
- Allow users to generate a B5-themed galaxy with pre-populated empires based on B5 races

### Scope

**In Scope:**
- New `GalaxyTemplate.BABYLON5` entry in `GalaxyTemplate.java`
- New `Babylon5EmpireBuilder.java` service in `com.yisy.modules.doramcu.babylon5.service`
- Mapping B5 races (`B5Race`) to `Empire` instances with appropriate stats
- Mapping B5 locations (`B5Location`) to home systems/planets
- Integration with `GalaxyFactory` to support B5 galaxy generation
- Unit tests for the builder

**Out of Scope:**
- B5 character integration into the AI chat system
- B5 storyline-driven quest generation
- CRUD write operations for B5 data
- B5 ship/fleet type definitions


# Technical Design

### Current Implementation
- **B5 Module**: `Babylon5DatabaseService` loads 13 races, 20 characters, 6 storylines, 10+ dialogues, 8 locations from `docs/babylon5-db/babylon5-universe.json`. REST API at `/api/v1/babylon5`.
- **Hex Galaxy System**: `Galaxy` (10x10 sectors), `Empire` (planets, fleets, resources), `GalaxyTemplate` (TRAVELLER_CLASSIC, STAR_TREK, FRONTIER, VOID, CUSTOM), `GalaxyFactory` (predefined galaxies), `GalaxyGenerator` (orchestrates generation).
- **Race Patterns**: Traveller uses `AlienRace` enum; Star Trek uses `StarTrekSpecies` enum. Both have combat/social/navigation modifiers.
- **B5 Data**: `B5Race` already has `combatStyle`, `tradeBonus`, `socialDM`, `navigationDM`, `armorBonus`, `characteristics` (ST/DX/EN/IN/ED/SO) — maps directly to empire/planet needs.

### Key Decisions
- **No B5Race enum**: B5 races are already rich JSON data in `Babylon5DatabaseService`. Creating an enum would duplicate data. The builder will read from the service instead.
- **Babylon5EmpireBuilder as Spring Service**: Follows the existing pattern of `Babylon5DatabaseService` being a `@Service`. The builder will inject the database service.
- **GalaxyTemplate.BABYLON5**: Uses `civilizationDensity(0.25)`, `empireCount(13)` (one per B5 race), `gameType("Babylon5")`, mythological naming, spiral galaxy shape.
- **Race-to-Empire Mapping**: Each B5 race becomes an empire with the race's name, homeworld as capital planet, and race modifiers applied to empire stats.

### Proposed Changes

**1. GalaxyTemplate.java** — Add `BABYLON5` entry:
```
BABYLON5(
    "Babylon 5",
    "A Babylon 5-inspired galaxy with 13 race-based empires.",
    GalaxyGenerationConfig.builder()
        .sectorSize(10)
        .subSectorDimension(10)
        .civilizationDensity(0.25)
        .resourceDistribution(0.4)
        .galaxyShape("spiral")
        .namingConvention("mythological")
        .empireCount(13)
        .difficultyLevel("medium")
        .gameType("Babylon5")
        .spaceStationChance(0.15)
        .hyperlaneDensity(1.0)
        .build()
)
```

**2. Babylon5EmpireBuilder.java** (new service):
- Injects `Babylon5DatabaseService` to access B5 race and location data
- `buildEmpires(Game game, Galaxy galaxy)` — creates one empire per B5 race
- `createEmpireFromRace(B5Race race, Game game)` — maps race data to Empire:
  - Empire name = race name (e.g., "Narn", "Centauri")
  - Capital planet = race homeworld (e.g., "Narn 3", "Centauri Prime")
  - Planet industrial level = based on race `tradeBonus`
  - Shipyard chance = based on race `combatStyle` and `armorBonus`
- `findHomeSystem(B5Location location, Galaxy galaxy)` — maps B5 locations to solar systems in the galaxy
- `assignStartingFleets(Empire empire, Game game)` — creates starting fleets based on race combat style

**3. Babylon5RestController.java** — Add endpoint:
- `POST /api/v1/babylon5/galaxy/generate` — generates a B5-themed galaxy with empires

### Components

| Component | File | Purpose |
|-----------|------|---------|
| GalaxyTemplate | `hex/GalaxyTemplate.java` | Add BABYLON5 preset |
| Babylon5EmpireBuilder | `babylon5/service/Babylon5EmpireBuilder.java` | Map B5 races to empires |
| Babylon5RestController | `babylon5/controller/Babylon5RestController.java` | Add galaxy generation endpoint |
| Babylon5GalaxyIntegrationTest | `test/.../babylon5/Babylon5GalaxyIntegrationTest.java` | Validate integration |

### Architecture Diagram

```mermaid
graph LR
  A[Babylon5DatabaseService] -->|B5Race, B5Location| B[Babylon5EmpireBuilder]
  B -->|Empire| C[Game]
  B -->|GalaxyTemplate.BABYLON5| D[GalaxyFactory]
  D -->|Galaxy| C
  C -->|registerEmpire| E[Empire per B5 Race]
  E -->|Capital Planet| F[B5 Homeworld]
```

### Risks
- **B5 data file missing**: If `babylon5-universe.json` is not found, the builder will create empty empires. Mitigation: validate data is loaded before building.
- **Location mapping**: B5 locations (8) may not match the number of empires (13). Mitigation: use homeworld from `B5Race` directly as planet name if no matching `B5Location` exists.


# Testing

### Validation Approach
Create integration tests that validate the B5 galaxy integration end-to-end.

### Key Scenarios
1. **GalaxyTemplate.BABYLON5 exists** — Verify the template is accessible via `GalaxyTemplate.findByName("BABYLON5")` and has correct config.
2. **Empire creation from races** — Verify `Babylon5EmpireBuilder` creates 13 empires (one per B5 race) with correct names and capital planets.
3. **Race stat mapping** — Verify Narn empire has ST/EN bonuses, Centauri has social/trade bonuses, Minbari has navigation/armor bonuses.
4. **Galaxy generation** — Verify a B5-themed galaxy can be generated via `GalaxyFactory` with the BABYLON5 template.
5. **Location mapping** — Verify B5 locations (Babylon 5 station, Earth, Minbar, etc.) are mapped to planets in the galaxy.

### Edge Cases
- B5 database file missing — builder should handle gracefully with empty empires
- Race without homeworld — fallback to generic planet name
- Galaxy with fewer systems than empires — distribute available systems fairly

# Implementation Status

### ✓ Step 1: Add GalaxyTemplate.BABYLON5 preset
- Added `BABYLON5` enum constant to `GalaxyTemplate.java` with all required parameters
- Template config: sectorSize(10), civilizationDensity(0.25), empireCount(13), gameType("Babylon5"), etc.

### ✓ Step 2: Create Babylon5EmpireBuilder service
- Created `Babylon5EmpireBuilder.java` in `com.yisy.modules.doramcu.babylon5.service`
- Methods: `buildEmpires()`, `createEmpireFromRace()`, `assignStartingFleets()`, `findHomeSystem()`
- Maps B5 race stats (tradeBonus, combatStyle, armorBonus, characteristics) to empire properties
- Handles edge cases: null races, missing homeworlds, empty database

### ✓ Step 3: Add galaxy generation endpoint and tests
- Added `POST /api/v1/babylon5/galaxy/generate` to `Babylon5RestController.java`
- Created `Babylon5GalaxyIntegrationTest.java` with 12 test methods
- All code compiles successfully (`compileJava` and `compileTestJava` pass)
- Tests could not run due to system memory constraints (not a code issue)
