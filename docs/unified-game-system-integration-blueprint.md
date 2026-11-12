# DoraMCU Unified Game System - Integration Blueprint

## Overview

This document provides the complete integration blueprint for the unified game system built on top of the Earth2026 hub module. It ties together all the designs from Steps 1-6 and provides a concrete migration path.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Earth2026 Hub                            │
│  (Character Creation, Galaxy Transition, Species Registry)  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Unified Character Layer                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  UnifiedCharacter Interface                          │   │
│  │  CharacterMapper<T> Adapters                         │   │
│  │  CharacterTransitionService                          │   │
│  │  SharedCharacter (extended with all game refs)       │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐ ┌──────▼───────┐ ┌──────▼──────┐
│  Rules Engine│ │ Combat Engine│ │Galaxy Gen   │
│  (RulesEngine│ │ (CombatEngine│ │ (GalaxyGen  │
│   Interface) │ │  Interface)  │ │  Interface) │
└───────┬──────┘ └──────┬───────┘ └──────┬──────┘
        │                │                │
┌───────▼────────────────▼────────────────▼──────┐
│           Game Engine Pipeline                  │
│  ┌──────────────────────────────────────────┐   │
│  │  GameEngine Interface (extended)         │   │
│  │  GameEngineRegistry                      │   │
│  │  GameSession                             │   │
│  │  GlobalClockService                      │   │
│  └──────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   ┌────▼────┐    ┌─────▼──────┐  ┌─────▼──────┐
   │Traveller│    │  D&D 5e    │  │Captains Log│
   │  Engine │    │   Engine   │  │   Engine   │
   └─────────┘    └────────────┘  └────────────┘
        │                │                │
   ┌────▼────┐    ┌─────▼──────┐  ┌─────▼──────┐
   │   SFB   │    │ Babylon 5  │  │  Star Trek │
   │  Engine │    │   Engine   │  │     4X     │
   └─────────┘    └────────────┘  └────────────┘
        │                │                │
   ┌────▼────┐    ┌─────▼──────┐  ┌─────▼──────┐
   │  007    │    │Third Reich │  │  Hex 4X    │
   │  Engine │    │   Engine   │  │   Engine   │
   └─────────┘    └────────────┘  └────────────┘
```

## Files Created

### Unified Character Layer (Step 2)
- `unified/UnifiedCharacter.java` — Interface for unified character representation
- `unified/AbstractUnifiedCharacter.java` — Concrete implementation
- `unified/CharacterMapper.java` — Adapter interface for game-specific conversion
- `unified/TravellerCharacterMapper.java` — Traveller ↔ Unified mapper
- `unified/Earth2026CharacterMapper.java` — Earth2026 ↔ Unified mapper
- `unified/Dnd5eCharacterMapper.java` — D&D 5e ↔ Unified mapper
- `unified/CaptainLogCharacterMapper.java` — Captain's Log ↔ Unified mapper
- `unified/CharacterTransitionService.java` — Cross-game character conversion

### Extended Existing Infrastructure
- `shared/model/SharedCharacter.java` — Extended with refs to all game types
- `shared/model/GameSystem.java` — Extended with EARTH_2026, STAR_TREK_4X, ZERO_ZERO_SEVEN, THIRD_REICH
- `unified/GameEngine.java` — Extended with getGameState(), processAction(), getAvailableActions(), isTurnBased(), getTurnDuration(), getGameType(), isActive(), start(), pause(), end()

### Rules Engine (Step 3)
- `unified/RulesEngine.java` — Unified rules engine interface

### Game Engine Pipeline (Step 4)
- `unified/GameEngineRegistry.java` — Maps GameType to GameEngine implementations
- `unified/GameSession.java` — Wraps any GameEngine for session management

### Combat Engine (Step 5)
- `unified/CombatEngine.java` — Unified combat engine interface with DamageType, Combatant, CombatSession

### Galaxy Generator (Step 6)
- `unified/GalaxyGenerator.java` — Unified galaxy generation interface with Galaxy, StarSystem, Planet, WorldProfile

## Phased Migration Plan

### Phase 1: Character Unification
**Goal:** Enable characters to be represented uniformly across all game systems.

**Files to Modify:**
- `shared/model/SharedCharacter.java` — ✅ Done: Added refs to all game types
- `shared/service/SharedCharacterService.java` — Add import methods for Earth2026, D&D 5e, SFB, Babylon5, 007, Third Reich
- `unified/UnifiedCharacterCreationService.java` — Extend to support all game types

**Files to Create:**
- `unified/Earth2026CharacterMapper.java` — ✅ Done
- `unified/Dnd5eCharacterMapper.java` — ✅ Done
- `unified/CaptainLogCharacterMapper.java` — ✅ Done
- `unified/SfbCharacterMapper.java` — To be created
- `unified/Babylon5CharacterMapper.java` — To be created
- `unified/StarTrek4XCharacterMapper.java` — To be created
- `unified/BondCharacterMapper.java` — To be created
- `unified/ThirdReichCharacterMapper.java` — To be created
- `unified/Hex4XCharacterMapper.java` — To be created

### Phase 2: Rules Unification
**Goal:** Normalize rule lookup and dice mechanics across all systems.

**Files to Modify:**
- `shared/model/GameSystem.java` — ✅ Done: Added missing game systems
- `shared/service/RulesService.java` — Already supports all GameSystem values

**Files to Create:**
- `unified/RulesEngine.java` — ✅ Done: Interface created
- `unified/TravellerRulesEngine.java` — To be created (2D6 implementation)
- `unified/CaptainsLogRulesEngine.java` — To be created (2d20 implementation)
- `unified/Dnd5eRulesEngine.java` — To be created (d20 implementation)
- `unified/Earth2026RulesEngine.java` — To be created (2D6 implementation)
- `unified/RulesMigrationService.java` — To be created (imports game-specific rules)

### Phase 3: Combat Unification
**Goal:** Normalize combat mechanics across all game modules.

**Files to Create:**
- `unified/CombatEngine.java` — ✅ Done: Interface created
- `unified/TravellerCombatEngine.java` — To be created (wraps FirearmsCombat, MeleeCombat)
- `unified/SfbCombatEngine.java` — To be created (wraps SfbCombatService)
- `unified/Dnd5eCombatEngine.java` — To be created (wraps Dnd5eCombatService)
- `unified/Earth2026CombatEngine.java` — To be created (modern combat)
- `unified/CaptainsLogCombatEngine.java` — To be created (wraps CombatService)
- `unified/Hex4XCombatEngine.java` — To be created (wraps CombatResolver)

### Phase 4: Galaxy/World Unification
**Goal:** Coordinate planet, system, and galaxy creation across all modules.

**Files to Create:**
- `unified/GalaxyGenerator.java` — ✅ Done: Interface created
- `unified/TravellerGalaxyGenerator.java` — To be created
- `unified/StarTrekGalaxyGenerator.java` — To be created
- `unified/CaptainsLogGalaxyGenerator.java` — To be created
- `unified/BondGalaxyGenerator.java` — To be created
- `unified/Earth2026GalaxyGenerator.java` — To be created
- `unified/SpeciesRegistry.java` — To be created (maps species across systems)

### Phase 5: Cross-Game Transitions
**Goal:** Enable Earth2026 characters to transition to any game galaxy.

**Files to Modify:**
- `earth2026/service/Earth2026Service.java` — Add galaxy transition method
- `earth2026/model/Earth2026Character.java` — Already has targetGalaxy and hasTransitioned

**Files to Create:**
- `unified/GalaxyTransitionService.java` — Handles the full transition workflow
- `unified/PlanetService.java` — Interface for planet operations across systems

## Earth2026 Hub Integration

### Character Transition Flow
1. Player creates character in Earth2026 using `Earth2026Service`
2. Character selects target galaxy via `Earth2026Character.targetGalaxy`
3. `GalaxyTransitionService` converts Earth2026Character to target game's character:
   - Uses `Earth2026CharacterMapper` to convert to `UnifiedCharacter`
   - Uses target `CharacterMapper` to convert to game-specific character
   - Creates the character in the target game system
4. `Earth2026Character.hasTransitioned` is set to true
5. `SharedCharacter` record is updated with the new game-specific reference

### API Contract

#### REST Endpoints
```
GET    /api/unified/characters          — List all shared characters
POST   /api/unified/characters          — Create shared character
GET    /api/unified/characters/{id}     — Get shared character
PUT    /api/unified/characters/{id}     — Update shared character
POST   /api/unified/characters/{id}/travel — Travel to target system

GET    /api/unified/rules/{gameSystem}  — Get rules for game system
GET    /api/unified/rules/{gameSystem}/category/{category} — Get rules by category

GET    /api/unified/engines             — List all game engines
GET    /api/unified/engines/{gameType}  — Get game engine state
POST   /api/unified/engines/{gameType}/action — Process action

GET    /api/unified/sessions            — List all sessions
POST   /api/unified/sessions            — Create session
GET    /api/unified/sessions/{id}       — Get session state
POST   /api/unified/sessions/{id}/start — Start session
POST   /api/unified/sessions/{id}/pause — Pause session
POST   /api/unified/sessions/{id}/end   — End session
POST   /api/unified/sessions/{id}/turn  — Advance turn
POST   /api/unified/sessions/{id}/action — Process action

GET    /api/earth2026/characters        — List Earth2026 characters
POST   /api/earth2026/characters        — Create Earth2026 character
POST   /api/earth2026/characters/{id}/transition — Transition to galaxy
```

#### WebSocket Events
```
game.session.started    — Session started
game.session.paused     — Session paused
game.session.ended      — Session ended
game.turn.advanced      — Turn advanced
game.action.processed   — Action processed
character.travelled     — Character traveled between systems
```

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data loss during migration | High | Use dual-write pattern during transition period; keep old services as fallback |
| Backward compatibility | Medium | All new interfaces use default methods; existing code continues to work |
| Performance impact | Low | Lazy loading for mappers; cache normalized ability scores |
| Incomplete mapper coverage | Medium | Start with Traveller, Earth2026, D&D 5e, Captain's Log; add others incrementally |
| Dice mechanic differences | Low | RulesEngine interface abstracts dice; each implementation handles its own mechanics |

## Summary

The unified game system provides:
1. **Unified Character Layer** — `UnifiedCharacter` interface with `CharacterMapper` adapters for each game type
2. **Unified Rules Engine** — `RulesEngine` interface normalizing dice mechanics and task resolution
3. **Unified Game Engine Pipeline** — Extended `GameEngine` interface with `GameEngineRegistry` and `GameSession`
4. **Unified Combat System** — `CombatEngine` interface with `DamageType`, `Combatant`, and `CombatSession`
5. **Unified Galaxy Generation** — `GalaxyGenerator` interface with `WorldProfile` abstraction
6. **Earth2026 Hub** — Character creation and galaxy transition entry point

The foundation is in place. The remaining work is to implement the concrete adapters for each game module (SFB, Babylon5, Star Trek 4X, 007, Third Reich, Hex 4X) and the concrete implementations of RulesEngine, CombatEngine, and GalaxyGenerator for each system.
