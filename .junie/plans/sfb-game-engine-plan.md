---
sessionId: session-261108-211728-bfo8
---

# Requirements

### Overview
Build a digital Star Fleet Battles game engine based on the existing SFB rules database (`docs/sfb-rules-db/`). The engine will support tactical ship-to-ship combat (32-impulse turns on a hex grid) and strategic campaign play (fleet management, shipbuilding, diplomacy). It integrates with the existing Captain's Log program so that characters created in the Captain's Log galaxy can participate in SFB battles as Admirals, Captains, or crew members. The engine is a domain-model-driven Java system with WebSocket-based multiplayer and AI opponent support, following the existing DoraMCU patterns.

### Scope
**In Scope:**
- Tactical combat engine: 32-impulse turns, hex grid movement, energy allocation, weapons fire (direct + seeking), shields, damage allocation, special systems (cloaking, tractors, EW)
- Domain model: SfbShip, SfbSSD, SfbShield, SfbWeaponBank, SfbEnergyAllocation, etc.
- **Captain's Log integration**: Characters from the Captain's Log galaxy participate in SFB battles with roles (Admiral, Captain, crew member). Character attributes, disciplines, talents, and starship systems modify SFB combat outcomes.
- **Galaxy connection**: SFB battles are anchored to the Captain's Log galaxy (`GalaxyExplorationService`). Battle locations map to sectors and star systems. Characters retain their galaxy identity across battles.
- Campaign layer: strategic map, fleet management, shipbuilding, diplomacy, repairs
- AI opponent for single-player mode
- WebSocket API for multiplayer
- Unit tests for all subsystems

**Out of Scope:**
- Graphical UI (engine is headless; clients connect via WebSocket/REST)
- Terrain monsters (space amoebas, dragons) — deferred to Phase 3
- Full campaign economy/diplomacy AI — basic version only

### User Stories
- As a player, I want to control a fleet of ships in tactical combat so I can experience SFB battles digitally.
- As a player, I want to play against an AI opponent so I can practice without a human partner.
- As two players, I want to play over WebSocket so we can compete remotely.
- As a player, I want to manage my empire across campaigns so I can build fleets and fight strategic wars.
- **As a Captain's Log character, I want to join an SFB battle as the ship's Captain so my attributes and disciplines affect the ship's combat performance.**
- **As an Admiral character, I want to direct a fleet in SFB combat so my leadership and command disciplines provide fleet-wide bonuses.**
- **As a crew member, I want to man a specific station on the ship so my skills contribute to system operations (shields, weapons, engineering, sensors).**
- **As a player, I want my SFB battles to take place in the Captain's Log galaxy so my character's exploration history connects to tactical combat.**

### Technical Approach
- Package: `com.yisy.modules.doramcu.sfb` (new package under existing `doramcu` module)
- Domain model with behavior on entities (SfbShip fires weapons, SfbShield absorbs damage)
- SfbCombatEngine orchestrates the 32-impulse turn loop
- SfbCampaignEngine manages strategic play
- **SfbCharacterBridge** connects Captain's Log characters to SFB ships and battles
- **SfbRoleSystem** manages character roles (Admiral, Captain, crew member) and their combat modifiers
- WebSocket controller for real-time multiplayer
- AI service for single-player mode
- Follow existing patterns: `HighGuardShipCombat` structure, `DiceUtil` for randomness, JUnit 5 tests with `@DisplayName`

# Technical Design

### Current Implementation
- **SFB Rules Database**: 12 JSON files in `docs/sfb-rules-db/` covering all core rules (core-mechanics, energy-allocation, movement, shields, weapons, damage-allocation, empires, ship-systems, special-systems, terrain, ship-classes, campaign-rules)
- **Reference Pattern**: `HighGuardShipCombat.java` (645 lines) — round-based combat engine with phases (maneuver, missile firing, beam firing, ship action), event logging, damage resolution
- **Captain's Log program** (`com.yisy.modules.doramcu.captainslog`):
  - **CaptainLogCharacter**: Full character model with attributes (FIT/TEC/STR/REA/INS/PRE), disciplines (Command/Leadership/Science/Security/Medicine/Operations), focuses, talents, rank, assignment, and injury state
  - **CaptainLogStarship**: Ship model with systems (Comms/Computers/Engines/Sensors/Structure/Weapons), departments, stress tracking, and attributes
  - **CaptainLogCrewMember**: Crew NPCs with roles, disciplines, and expertise ratings
  - **SharedCampaign**: Multiplayer campaign support with player roles, turn order, and shared missions
  - **GalaxyExplorationService**: Procedural galaxy generation with sectors, star systems, and hyperlane networks
  - **StarshipCombatService**: 2d20-based starship combat with range bands, system attacks, evasion, and critical hits
  - **CaptainLogCombat**: Combat encounters linked to missions and characters
- **Utilities**: `DiceUtil.java` — shared dice rolling (rollD6, roll2D6, rollNdX, etc.)
- **Testing**: `HighGuardShipCombatTest.java` (502 lines) — JUnit 5 with `@DisplayName`, helper methods for ship creation
- **Build**: Gradle, Spring Boot 4.1.2, Java 27, Jackson for JSON

### Key Decisions
1. **Domain model + thin engine** (user-selected): Rich entities (SfbShip, SfbShield, SfbWeaponBank) with behavior, coordinated by SfbCombatEngine. Most object-oriented approach, matches SFB's SSD-centric design.
2. **Tactical + Campaign** (user-selected): Both layers built together. Tactical engine first within each stage, campaign layer added as extension.
3. **Multiplayer + AI** (user-selected): WebSocket API for real-time play plus AI opponent service.
4. **JSON-driven ship definitions**: Ships defined in JSON (extending `ship-classes.json`) rather than hardcoded — enables adding new ships without code changes.
5. **Impulse-based turn loop**: Unlike HighGuard's round-based approach, SFB uses 32 impulses per turn — the engine will step through impulses.
6. **Captain's Log character integration**: SFB ships are crewed by Captain's Log characters. Character attributes and disciplines modify SFB combat outcomes (e.g., Command discipline improves energy allocation efficiency, Security discipline improves weapon accuracy, Engineering discipline improves DamCon repairs).
7. **Role-based combat modifiers**: Characters play specific roles in SFB battles:
   - **Admiral**: Fleet-level commander. Provides fleet-wide bonuses based on Command/Leadership disciplines. Does not control a specific ship but affects all ships in the fleet.
   - **Captain**: Ship-level commander. Assigned to one SfbShip. Character attributes modify ship system performance (e.g., high PRE improves shield efficiency, high REA improves sensor range).
   - **Crew member**: Station-specific role. Assigned to a specific system (weapons officer, helm, tactical, engineering). Character discipline bonuses apply to that system's operations.
8. **Galaxy anchoring**: SFB battles take place within the Captain's Log galaxy. Battle locations map to sectors and star systems from `GalaxyExplorationService`. This connects exploration history to tactical combat.

### Architecture Overview
```
graph LR
  WC[SfbWebSocketController] --> CE[SfbCombatEngine]
  RC[SfbRestController] --> CE
  CE --> IM[ImpulseManager]
  CE --> EAS[EnergyAllocationService]
  CE --> MVS[MovementService]
  CE --> WFS[WeaponsFireService]
  CE --> DAS[DamageAllocationService]
  CE --> SSS[SpecialSystemsService]
  CE --> SS[SfbShip]
  SS --> SH[SfbShield]
  SS --> WB[SfbWeaponBank]
  SS --> SSD[SfbSSD]
  CE --> AI[SfbAIService]
  CE --> CAM[SfbCampaignEngine]
  
  CB[SfbCharacterBridge] --> CL[CaptainLogCharacter]
  CB --> SS
  CB --> ROLE[SfbRoleSystem]
  ROLE --> ADM[Admiral modifiers]
  ROLE --> CAP[Captain modifiers]
  ROLE --> CREW[Crew station modifiers]
  
  GAL[GalaxyExplorationService] --> LOC[SfbBattleLocation]
  LOC --> CE
  CAM --> GAL
```

### Proposed Changes

#### Domain Model (`com.yisy.modules.doramcu.sfb.model`)
- **SfbShip**: Core entity representing a starship. Holds position (hex coordinates), facing, speed, turn mode, energy allocation, shields, weapons, SSD state. Behaviors: move(), fireWeapons(), allocateEnergy(), takeDamage()
- **SfbSSD**: Ship System Display. Tracks all system boxes (warp engines, impulse engines, shields, weapons, control spaces, DamCon, hull). Methods: markOutBox(), getDamConRating(), isCrippled()
- **SfbShield**: One of 6 directional shields. Tracks boxes, reinforcement points. Methods: absorbDamage(), reinforce(), isDown()
- **SfbWeaponBank**: Represents a weapon mount (phasers, disruptors, torpedoes, etc.). Tracks firing arc, energy cost, ammo. Methods: canFireAt(), fire(), getDamageAtRange()
- **SfbEnergyAllocation**: Per-turn energy form. Tracks available power, allocations to shields/weapons/special systems. Methods: validate(), getReservePower()
- **SfbHexPosition**: Hex grid coordinates with facing. Methods: distanceTo(), relativeDirection()
- **SfbSeekingWeapon**: Plasma torpedoes and drones. Tracks target, warhead strength, range traveled. Methods: pursue(), degrade(), intercept()
- **SfbEmpire**: Empire/faction data loaded from `empires.json`. Tracks weapons, doctrines, ship classes.
- **SfbScenario**: Represents a tactical battle scenario. Tracks participating ships, turn/impulse state, event log.
- **SfbBattleLocation**: Links a scenario to a Captain's Log galaxy sector and star system. Tracks sector coordinates, system name, and terrain effects.

#### Captain's Log Integration Model (`com.yisy.modules.doramcu.sfb.model`)
- **SfbCharacterAssignment**: Links a `CaptainLogCharacter` to an SFB battle. Tracks role (Admiral/Captain/Crew), assigned ship (for Captain/Crew), assigned station (for Crew), and active modifiers.
- **SfbFleetCommand**: Represents an Admiral's fleet-level command. Tracks the Admiral character, fleet-wide bonuses, and affected ships.
- **SfbCrewStation**: Represents a crew member's station assignment. Tracks character, station type (Helm/Tactical/Engineering/Sensors/Medical), and performance modifiers.

#### Services (`com.yisy.modules.doramcu.sfb.service`)
- **SfbCombatEngine**: Orchestrates the 32-impulse turn loop. Coordinates energy allocation phase, impulse procedure (movement, weapons, seeking weapons, damage), and turn completion.
- **ImpulseManager**: Manages the 32-impulse sequence. Determines which ships move on which impulse based on speed.
- **EnergyAllocationService**: Validates and processes energy allocation forms. Calculates available power from warp/impulse/auxiliary engines, batteries, capacitors.
- **MovementService**: Handles proportional movement on hex grid. Speed calculation, turn modes, HET, sideslip, acceleration.
- **WeaponsFireService**: Resolves direct-fire weapons (phasers, disruptors, torpedoes). Range band calculation, hit probability, damage.
- **DamageAllocationService**: Processes damage through shields to internal systems. Damage Allocation Chart rolls, system damage, excess damage, crippled check.
- **SpecialSystemsService**: Handles cloaking, tractor beams, transporters, EW, sensors, scanners.
- **SfbAIService**: AI opponent that makes decisions for energy allocation, movement, targeting, and special systems.
- **SfbCampaignEngine**: Strategic campaign management. Fleet deployment, shipbuilding, diplomacy, repairs.
- **SfbShipLoader**: Loads ship definitions from JSON files (extends `ship-classes.json`).

#### Captain's Log Integration Services (`com.yisy.modules.doramcu.sfb.service`)
- **SfbCharacterBridge**: Connects Captain's Log characters to SFB battles. Loads characters from `CaptainLogCharacterRepository`, validates character status (injury, availability), and creates `SfbCharacterAssignment` entries. Acts as the gateway between the two systems.
- **SfbRoleSystem**: Manages character roles and calculates combat modifiers:
  - **Admiral modifiers**: Fleet-wide bonuses based on Command/Leadership disciplines. Affects all ships' energy allocation efficiency, weapon accuracy, and shield reinforcement.
  - **Captain modifiers**: Ship-specific bonuses. Character attributes modify ship systems:
    - PRE → shield efficiency bonus
    - REA → sensor range bonus
    - STR → weapon damage bonus
    - INS → evasion bonus
    - TEC → engineering/DamCon bonus
    - FIT → crew morale bonus
  - **Crew station modifiers**: Station-specific bonuses. Character discipline and expertise ratings modify system performance:
    - Helm station: Movement accuracy and speed bonuses (Operations discipline)
    - Tactical station: Weapon hit probability and damage bonuses (Security discipline)
    - Engineering station: DamCon repair speed and energy allocation bonuses (Operations discipline)
    - Sensors station: Detection range and cloaked ship detection (Science discipline)
    - Medical station: Crew injury recovery and damage mitigation (Medicine discipline)
- **SfbGalaxyIntegration**: Connects SFB battles to the Captain's Log galaxy. Uses `GalaxyExplorationService` to look up battle locations, apply sector-based terrain effects, and track battle outcomes in the galaxy history.

#### Controllers (`com.yisy.modules.doramcu.sfb.controller`)
- **SfbWebSocketController**: Real-time multiplayer via WebSocket. Handles game state synchronization, player actions, impulse progression.
- **SfbRestController**: REST API for scenario setup, ship selection, campaign management.
- **SfbCharacterController**: REST endpoints for Captain's Log character integration:
  - `POST /api/v1/sfb/battles/{battleId}/assign-character`: Assign a character to a battle with a role
  - `GET /api/v1/sfb/battles/{battleId}/crew`: List all characters assigned to a battle
  - `POST /api/v1/sfb/battles/{battleId}/crew/{characterId}/station`: Assign crew to a station
  - `GET /api/v1/sfb/characters/{characterId}/modifiers`: Get combat modifiers for a character
  - `POST /api/v1/sfb/battles/{battleId}/location`: Set battle location from galaxy
  - `GET /api/v1/sfb/galaxy/sectors/{sectorId}/battles`: List battles in a sector

#### Configuration
- **SfbConfig**: Spring configuration for SFB module. Loads rules database JSON files, configures WebSocket handlers.

### Data Models / Contracts

#### SfbShip (core entity)
```java
public class SfbShip {
    private String id;
    private String name;
    private SfbEmpire empire;
    private String shipClass;
    private SfbHexPosition position;
    private int facing; // 0-5 (hex directions)
    private int speed; // 1-32
    private int turnMode; // 1-3
    private SfbSSD ssd;
    private SfbEnergyAllocation energyAllocation;
    private List<SfbSeekingWeapon> launchedWeapons;
    private boolean cloaked;
    private boolean crippled;
    private boolean destroyed;
    // Behaviors: move(), fireWeapons(), allocateEnergy(), takeDamage(), etc.
}
```

#### SfbSSD (Ship System Display)
```java
public class SfbSSD {
    private Map<String, SsdBox> systems; // warp engines, impulse, shields, weapons, etc.
    private int damConRating;
    private int hullBoxes;
    private int excessDamage;
    // Methods: markOutBox(), getDamConRating(), isCrippled(), getAvailableWarpPower()
}
```

#### WebSocket Messages
```java
// Client -> Server
public class SfbActionRequest {
    private String scenarioId;
    private String shipId;
    private ActionType type; // ENERGY_ALLOCATE, MOVE, FIRE, SPECIAL_SYSTEM
    private Map<String, Object> parameters;
}

// Server -> Client
public class SfbGameState {
    private String scenarioId;
    private int turn;
    private int impulse;
    private List<SfbShipSnapshot> ships;
    private List<SfbEvent> events;
    private Phase currentPhase;
}

// Character assignment request
public class SfbCharacterAssignmentRequest {
    private String battleId;
    private Long characterId; // CaptainLogCharacter ID
    private SfbRole role; // ADMIRAL, CAPTAIN, CREW
    private String shipId; // required for CAPTAIN and CREW
    private String station; // required for CREW: HELM, TACTICAL, ENGINEERING, SENSORS, MEDICAL
}

// Character modifiers response
public class SfbCharacterModifiers {
    private Long characterId;
    private String characterName;
    private SfbRole role;
    private Map<String, Integer> attributeBonuses; // attribute -> bonus value
    private Map<String, Integer> disciplineBonuses; // discipline -> bonus value
    private List<String> activeTalents; // talents that affect combat
}

// Battle location from galaxy
public class SfbBattleLocationRequest {
    private String battleId;
    private int galaxyId;
    private int sectorRow;
    private int sectorCol;
    private String systemName; // optional, for specific system within sector
}
```

#### Character Role Enum
```java
public enum SfbRole {
    ADMIRAL("Fleet commander, provides fleet-wide bonuses"),
    CAPTAIN("Ship commander, modifies ship systems"),
    CREW("Station operator, modifies specific system performance");
    
    private final String description;
    SfbRole(String description) { this.description = description; }
    public String getDescription() { return description; }
}

public enum SfbCrewStation {
    HELM("Navigation and movement", CaptainLogCharacter.Discipline.OPERATIONS),
    TACTICAL("Weapons and targeting", CaptainLogCharacter.Discipline.SECURITY),
    ENGINEERING("Shields, power, and repairs", CaptainLogCharacter.Discipline.OPERATIONS),
    SENSORS("Detection and scanning", CaptainLogCharacter.Discipline.SCIENCE),
    MEDICAL("Crew injury recovery", CaptainLogCharacter.Discipline.MEDICINE);
    
    private final String description;
    private final CaptainLogCharacter.Discipline primaryDiscipline;
    SfbCrewStation(String description, CaptainLogCharacter.Discipline primaryDiscipline) {
        this.description = description;
        this.primaryDiscipline = primaryDiscipline;
    }
    public String getDescription() { return description; }
    public CaptainLogCharacter.Discipline getPrimaryDiscipline() { return primaryDiscipline; }
}
```

### File Structure
```
src/main/java/com/yisy/modules/doramcu/sfb/
├── model/
│   ├── SfbShip.java
│   ├── SfbSSD.java
│   ├── SfbShield.java
│   ├── SfbWeaponBank.java
│   ├── SfbEnergyAllocation.java
│   ├── SfbHexPosition.java
│   ├── SfbSeekingWeapon.java
│   ├── SfbEmpire.java
│   ├── SfbScenario.java
│   ├── SfbEvent.java
│   ├── SfbBattleLocation.java              // Links scenario to galaxy sector
│   ├── SfbCharacterAssignment.java        // Links CaptainLogCharacter to battle
│   ├── SfbFleetCommand.java              // Admiral fleet-level command
│   ├── SfbCrewStation.java               // Crew station assignment
│   ├── SfbRole.java                      // Enum: ADMIRAL, CAPTAIN, CREW
│   └── SfbCrewStationType.java           // Enum: HELM, TACTICAL, ENGINEERING, SENSORS, MEDICAL
├── service/
│   ├── SfbCombatEngine.java
│   ├── ImpulseManager.java
│   ├── EnergyAllocationService.java
│   ├── MovementService.java
│   ├── WeaponsFireService.java
│   ├── DamageAllocationService.java
│   ├── SpecialSystemsService.java
│   ├── SfbAIService.java
│   ├── SfbCampaignEngine.java
│   ├── SfbShipLoader.java
│   ├── SfbCharacterBridge.java           // Connects Captain's Log characters to SFB
│   ├── SfbRoleSystem.java                // Calculates role-based combat modifiers
│   └── SfbGalaxyIntegration.java         // Connects battles to Captain's Log galaxy
├── controller/
│   ├── SfbWebSocketController.java
│   ├── SfbRestController.java
│   └── SfbCharacterController.java       // Character assignment and galaxy endpoints
├── config/
│   └── SfbConfig.java
└── dto/
    ├── SfbActionRequest.java
    ├── SfbGameState.java
    ├── SfbShipSnapshot.java
    ├── SfbCharacterAssignmentRequest.java
    ├── SfbCharacterModifiers.java
    └── SfbBattleLocationRequest.java

src/test/java/com/yisy/modules/doramcu/sfb/
├── SfbCombatEngineTest.java
├── SfbShipTest.java
├── EnergyAllocationServiceTest.java
├── MovementServiceTest.java
├── WeaponsFireServiceTest.java
├── DamageAllocationServiceTest.java
├── SpecialSystemsServiceTest.java
├── SfbAIServiceTest.java
├── SfbCampaignEngineTest.java
├── SfbShipLoaderTest.java
├── SfbCharacterBridgeTest.java           // Character assignment and validation
├── SfbRoleSystemTest.java                // Modifier calculations for all roles
├── SfbGalaxyIntegrationTest.java         // Battle location and sector effects
└── SfbScenarioIntegrationTest.java       // Full scenario with characters
```

### Risks
- **Complexity**: SFB rules are extremely detailed. The initial implementation will cover core mechanics; edge cases (breakdown, tactical maneuvers, detailed EW) may need iterative refinement.
- **JSON ship definitions**: Need to design a flexible schema that covers all ship types without being overly complex.
- **AI quality**: Basic AI will use heuristic rules; advanced AI (fleet tactics, energy optimization) is a long-term goal.
- **WebSocket state sync**: Real-time multiplayer requires careful state management to handle disconnected clients and action ordering.
- **Captain's Log coupling**: The SFB module depends on Captain's Log entities. Character state changes (injury, death) in Captain's Log must propagate to SFB battles. Need to handle cases where a character becomes unavailable mid-battle.
- **Modifier balancing**: Character modifiers must enhance gameplay without making SFB rules trivial. Need to tune bonus values so that character skills provide meaningful but not overwhelming advantages.

# Testing

### Testing Strategy
Follow existing project conventions: JUnit 5 with `@DisplayName` annotations, `DiceUtil` for randomness, helper methods for creating test fixtures.

### Test Categories

#### Domain Model Tests
- **SfbShipTest**: Ship creation, position/facing, speed validation, crippled/destroyed states
- **SfbSSDTest**: Box marking, DamCon rating calculation, crippled detection, warp power calculation
- **SfbShieldTest**: Damage absorption, reinforcement, shield down detection
- **SfbWeaponBankTest**: Firing arc validation, range band calculation, ammo tracking
- **SfbEnergyAllocationTest**: Power validation, allocation limits, reserve power

#### Service Tests
- **SfbCombatEngineTest**: Turn/impulse progression, phase ordering, scenario completion
- **EnergyAllocationServiceTest**: Power calculation from engines, battery/capacitor usage, validation
- **MovementServiceTest**: Speed-to-impulse mapping, hex movement, turn modes, acceleration
- **WeaponsFireServiceTest**: Hit probability by range band, damage calculation, overload/dial-a-yield
- **DamageAllocationServiceTest**: Shield penetration, DAC rolls, system damage, excess damage, crippled
- **SpecialSystemsServiceTest**: Cloaking detection, tractor beam usage, EW effects
- **SfbAIServiceTest**: AI energy allocation decisions, targeting choices, movement decisions
- **SfbCampaignEngineTest**: Fleet deployment, shipbuilding, repairs
- **SfbCharacterBridgeTest**: Character loading, validation, injury state checks, assignment creation
- **SfbRoleSystemTest**: Admiral fleet-wide modifier calculation, Captain attribute-to-system mapping, Crew station discipline bonuses
- **SfbGalaxyIntegrationTest**: Battle location lookup from galaxy, sector effects application, battle outcome tracking

#### Integration Tests
- **SfbScenarioIntegrationTest**: Full scenario flow — setup, energy allocation, impulse loop, damage, victory
- **SfbShipLoaderTest**: JSON loading from `ship-classes.json`, empire data from `empires.json`
- **SfbCharacterIntegrationTest**: Full character assignment flow — assign character, calculate modifiers, apply to combat, validate outcomes
- **SfbGalaxyBattleTest**: Battle location setup from galaxy, sector effects on combat, battle outcome tracking

### Validation Approach
- Each service tested in isolation with mock dependencies
- Integration tests validate the full impulse loop
- Cross-file consistency tests (extending `SfbRulesDatabaseValidationTest`) ensure game data matches rules database
- AI tests validate decision-making against known scenarios
- Character integration tests validate that modifiers are correctly calculated and applied to combat outcomes

# Delivery Steps

### ✓ Step 1: Domain model & SSD system
Create the core domain model classes that represent ships, shields, weapons, and the Ship System Display.
- Create `SfbShip.java` with position, facing, speed, SSD reference, energy allocation, and core behaviors (move, fire, takeDamage)
- Create `SfbSSD.java` with system box tracking, DamCon rating, hull/excess damage, crippled detection
- Create `SfbShield.java` with 6-direction shield system, damage absorption, reinforcement, shield down
- Create `SfbWeaponBank.java` with firing arcs, energy costs, ammo tracking, range band calculations
- Create `SfbEnergyAllocation.java` with power sources, allocation categories, validation
- Create `SfbHexPosition.java` with hex grid coordinates, distance, relative direction
- Create `SfbSeekingWeapon.java` for plasma torpedoes and drones with pursuit, degradation, interception
- Create `SfbEmpire.java` and `SfbScenario.java` for empire data and scenario state
- Create `SfbShipLoader.java` to load ship definitions from JSON files
- Add unit tests for all model classes validating construction, state transitions, and edge cases

### ✓ Step 2: Combat engine & impulse loop
Build the SfbCombatEngine that orchestrates the 32-impulse turn loop and the services it coordinates.
- Create `SfbCombatEngine.java` with turn/impulse progression, phase management (energy allocation → impulse procedure → damage)
- Create `ImpulseManager.java` that maps ship speeds to movement impulses using the movement chart
- Create `EnergyAllocationService.java` that calculates available power from warp/impulse/auxiliary engines, validates allocations
- Create `MovementService.java` with proportional movement on hex grid, turn modes, acceleration
- Wire services into the combat engine's impulse loop
- Add unit tests for combat engine turn progression, impulse ordering, and energy allocation validation

### ✓ Step 3: Weapons fire & damage allocation
Implement weapons firing resolution and damage allocation — the core combat mechanics.
- Create `WeaponsFireService.java` with direct-fire resolution (phasers, disruptors, torpedoes), range band hit probability, damage calculation, overload/dial-a-yield
- Create `DamageAllocationService.java` with shield penetration, Damage Allocation Chart rolls, system damage, excess damage, crippled/self-destruction checks
- Implement seeking weapon resolution (plasma torpedo pursuit, drone targeting, interception)
- Integrate weapons and damage services into the combat engine's impulse procedure
- Add unit tests for fire charts (using data from `weapons.json`), damage allocation paths, and seeking weapon behavior

### ✓ Step 4: Special systems & AI opponent
Add special systems (cloaking, tractors, EW, sensors) and AI opponent for single-player mode.
- Create `SpecialSystemsService.java` with cloaking detection, tractor beam usage, transporter rules, EW (ECM/ECCM), sensor/scanner ratings
- Create `SfbAIService.java` with heuristic-based AI for energy allocation decisions, movement choices, targeting, and special system usage
- Integrate special systems into the impulse loop (sensor lock-on, tractor beam phase, EW effects on weapons)
- Add unit tests for special system interactions and AI decision-making against known scenarios

### ✓ Step 5: Captain's Log character integration & role system
Connect Captain's Log characters to SFB battles with role-based combat modifiers.
- Create `SfbCharacterBridge.java` to load and validate Captain's Log characters for SFB battles
- Create `SfbRoleSystem.java` with modifier calculations for Admiral, Captain, and Crew roles
- Create `SfbCharacterAssignment.java`, `SfbFleetCommand.java`, and `SfbCrewStation.java` models
- Create `SfbRole` and `SfbCrewStationType` enums
- Create `SfbCharacterController.java` with REST endpoints for character assignment
- Wire character modifiers into combat services (energy allocation, weapons fire, damage allocation, movement)
- Add unit tests for character assignment, modifier calculations, and modifier application to combat outcomes

### ✓ Step 6: Galaxy integration & battle locations
Anchor SFB battles to the Captain's Log galaxy and apply sector-based effects.
- Create `SfbGalaxyIntegration.java` to connect battles to `GalaxyExplorationService`
- Create `SfbBattleLocation.java` model linking scenarios to galaxy sectors and star systems
- Add galaxy location endpoints to `SfbCharacterController.java`
- Apply sector-based terrain effects to combat (e.g., nebula sectors reduce sensor range, asteroid sectors add collision risk)
- Track battle outcomes in galaxy history for character progression
- Add unit tests for battle location setup, sector effects, and galaxy history tracking

### * Step 7: Campaign engine & API layer
Add strategic campaign management and the WebSocket/REST API for multiplayer access.
- Create `SfbCampaignEngine.java` with fleet deployment, strategic map management, shipbuilding, repairs, Zone of Control
- Create `SfbRestController.java` for scenario setup, ship selection, campaign management endpoints
- Create DTO classes: `SfbActionRequest`, `SfbGameState`, `SfbShipSnapshot`, `SfbCharacterAssignmentRequest`, `SfbCharacterModifiers`, `SfbBattleLocationRequest`
- Create `SfbConfig.java` for Spring configuration
- Add integration tests for full scenario flow with characters and API endpoints

---

## Current Status: Phase 1 Complete, Phase 2 Priority 1 Complete, Phase 3 Stages 1-2 Complete

**Phase 1 (Steps 1-7):** All implemented and pushed to `Lapis111426`.
- **17 model classes** in `com.yisy.modules.doramcu.sfb.model`
- **12 service classes** in `com.yisy.modules.doramcu.sfb.service` (added SfbShipLoader)
- **1 REST controller** with basic endpoints
- **6 DTO classes** for API communication
- **Spring configuration** with bean definitions
- **16 test files** with 282 tests (all passing)

**Phase 2 Priority 1 (Integration & Wiring):** All 3 stages implemented and pushed.
- Character modifiers wired into combat engine ✓
- Special systems integrated into impulse loop ✓
- AI integrated into combat engine ✓

**Phase 3 Stage 1 (Compilation Fix):** Compilation error resolved ✓
- `SPECIAL_SYSTEM` enum value added to `SfbEvent.java` ✓
- All 282 tests pass (16 test classes, 0 failures, 0 errors) ✓

**Phase 3 Stage 2 (SfbShipLoader):** Ship loading from JSON implemented ✓
- `SfbShipLoader.java` reads `ship-classes.json` and `empires.json` ✓
- Creates fully configured `SfbShip` objects with SSD, shields, weapons ✓
- 15 new tests pass ✓

## Remaining Gaps

### Gap 1: Compilation Error (FIXED)
**Fix:** Added `SPECIAL_SYSTEM` to `SfbEvent.EventType` enum in `SfbEvent.java`. ✓

### Gap 2: SfbShipLoader Missing (FIXED)
**Fix:** Created `SfbShipLoader.java` that reads `ship-classes.json` and `empires.json`. ✓

### Gap 3: Movement is Oversimplified
**Current state:** `SfbCombatEngine.processMovement()` calls `movementService.moveOneHex()` regardless of ship speed. No proportional movement based on speed 1-32.
**Impact:** All ships move at the same rate regardless of speed setting. Speed has no tactical value.

### Gap 4: No WebSocket Support
**Current state:** Only REST API exists. No real-time multiplayer capability.
**Impact:** Cannot play remote multiplayer games. Players must use REST endpoints manually.

### Gap 5: Campaign Engine is Skeletal
**Current state:** `SfbCampaignEngine` has basic fleet creation and battle lifecycle. Shipbuilding, repairs, and Zone of Control are stubs.
**Impact:** No strategic campaign play. Fleets cannot be built, repaired, or managed between battles.

### Gap 6: Missing Tests
**Current state:** Several test files mentioned in the plan don't exist: `MovementServiceTest`, `SfbCharacterBridgeTest`, `SfbGalaxyIntegrationTest`, `SfbCampaignEngineTest`.
**Impact:** Reduced test coverage for critical services. 10 test files exist but 4 planned tests are missing.

---

## Proposed Next Steps (Phase 4)

### ✓ Stage 1: Proportional Movement System
### ✓ Stage 2: WebSocket Controller for Real-Time Multiplayer
### ✓ Stage 3: Campaign Engine Enhancements
### * Stage 4: Missing Test Coverage
Implement speed-based proportional movement so ships move at different rates.
- Enhance `MovementService` with `calculateMovementDistance()` that uses ship speed and current impulse to determine hexes moved
- Use movement chart from `movement.json`: speed 32 moves 31 hexes/turn, speed 16 moves 15 hexes/turn, speed 8 moves 4 hexes/turn
- Implement `moveShip()` method that calculates distance based on speed and turn mode
- Update `SfbCombatEngine.processMovement()` to use `movementService.moveShip(ship, impulse)` instead of `moveOneHex()`
- Add acceleration/deceleration rules (max 4 speed points change per turn)
- Add `MovementServiceTest` with 15+ tests for speed-to-distance mapping, turn modes, and acceleration
- **Files affected:** `MovementService.java`, `SfbCombatEngine.java`, new `MovementServiceTest.java`

### Stage 2: WebSocket Controller for Real-Time Multiplayer
Enable remote multiplayer with real-time game state synchronization.
- Create `SfbWebSocketController.java` with WebSocket endpoints (`/ws/sfb/{battleId}`)
- Implement game state broadcast to connected clients on impulse/turn changes
- Handle player actions (energy allocation, movement, weapons fire) via WebSocket messages
- Manage impulse progression and turn synchronization between two players
- Add session management for player connections (join, leave, reconnect)
- Update `SfbConfig.java` to register WebSocket handler
- **Files affected:** New `SfbWebSocketController.java`, `SfbConfig.java`

### Stage 3: Campaign Engine Enhancements
Flesh out the campaign engine with shipbuilding, repairs, and fleet management.
- Enhance `SfbCampaignEngine` with shipbuilding system (create ships from JSON definitions)
- Implement repair system that restores ship systems between battles based on DamCon rating
- Add fleet composition management (add/remove ships from fleets)
- Implement battle outcome tracking and galaxy history integration
- Add `SfbCampaignEngineTest` with 15+ tests for fleet management, shipbuilding, and repairs
- **Files affected:** `SfbCampaignEngine.java`, new `SfbCampaignEngineTest.java`

### Stage 4: Missing Test Coverage
Add the missing test files to improve coverage.
- Create `SfbCharacterBridgeTest` with 15+ tests for character assignment, validation, and injury checks
- Create `SfbGalaxyIntegrationTest` with 10+ tests for battle location setup and sector effects
- Create `MovementServiceTest` (also part of Stage 1)
- Create `SfbCampaignEngineTest` (also part of Stage 3)
- **Files affected:** New `SfbCharacterBridgeTest.java`, `SfbGalaxyIntegrationTest.java`, `MovementServiceTest.java`, `SfbCampaignEngineTest.java`