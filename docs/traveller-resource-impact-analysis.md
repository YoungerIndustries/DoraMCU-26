# Traveller Galaxy Creation: Resource Impact Analysis & Lazy-Loading Design

**Date:** 2026-11-22
**Version:** v0.6.8
**Author:** Junie (DoraMCU Agent)

---

## Executive Summary

Switching from the current random 1-3 systems per subsector to Traveller-style 15-35 systems per subsector would increase galaxy size by **8-17x**, making eager generation at startup **infeasible on a Raspberry Pi** (the current server platform). This document analyzes the resource impact and proposes a **lazy-loading system** where galaxies are mapped initially but built on-demand as players explore via the Younger Industries wormhole transport network.

---

## 1. Current System Resource Footprint

### 1.1 Galaxy Structure

| Component | Count | Notes |
|-----------|-------|-------|
| Galaxy | 1 | In-memory `hex.Galaxy` instance |
| Sectors | 100 | 10×10 grid |
| SubSectors | 10,000 | 100 per sector, 10×10 grid each |
| Solar Systems | ~19,700 | 1-3 per subsector, random |
| Stars per system | 1 | `SolarSystemEntity` with star registry |
| Planets per system | ~3 (avg) | 1-5 random |
| Moons per planet | 0-1 | 40% chance |
| Asteroid belts | 0-1 | 50% chance |
| Gas giants | 0-1 | 30% chance |
| **Total celestial bodies** | **~80,000** | Per galaxy |

### 1.2 Memory Footprint Per Object

Estimated Java heap usage per object (based on field analysis):

| Object | Fields | Estimated Size |
|--------|--------|---------------|
| `Hex3D` | 3 int, 1 enum, 1 Object | ~64 bytes |
| `Hex3DMap` | 1 HashMap | ~64 bytes + entries |
| `CelestialBody` | 7 fields + Hex3DMap | ~200 bytes |
| `Star` | 7 fields + CelestialBody | ~256 bytes |
| `Planet` | 15 fields + geometry + defense + UWP | ~512 bytes |
| `SolarSystemEntity` | 11 registries + Hex3DMap + positionIndex | ~1,024 bytes |
| `SolarSystem` (hex) | 2 maps | ~256 bytes |
| `SubSector` | 6 fields + 2 maps | ~256 bytes + systems |
| `GalacticEntry` | 8 fields | ~96 bytes |
| `Galaxy.Sector` | 7 fields + subGrid + allSystems | ~512 bytes + subsectors |

### 1.3 Total Memory Per Galaxy (Current)

```
Solar Systems:       19,700 × (256 + 1,024 + 256) = ~28 MB
Celestial Bodies:    80,000 × 300 (avg) = ~24 MB
SubSectors:          10,000 × (256 + systems) = ~5 MB
Sectors:             100 × 512 = ~51 KB
GalacticEntries:     19,700 × 96 = ~1.9 MB
Hex3DMaps:           ~50,000 × 64 = ~3.2 MB
HashMap overhead:    ~40 MB (entry nodes, arrays)
Total per galaxy:    ~103 MB
```

**17 galaxies × 103 MB = ~1.75 GB heap** (plus DB entities loaded via JPA)

### 1.4 Database Footprint (Current)

Per galaxy, `persistGalaxy()` saves:
- 1 Galaxy row
- 100 Sector rows
- 10,000 SubSector rows
- ~19,700 SolarSystem rows
- ~19,700 Star rows
- ~59,100 Planet rows (3 per system avg)
- ~7,880 Moon rows (40% of planets)
- ~9,850 AsteroidBelt rows (50%)
- ~5,910 GasGiant rows (30%)
- ~2,955 Nebula rows (15%)
- ~985 BlackHole rows (5%)
- ~3,940 Comet rows (20%)
- ~1,970 SpaceStation rows (10%)

**~122,000 DB rows per galaxy × 17 = ~2.07 million rows**

---

## 2. Traveller-Style Impact Analysis

### 2.1 System Count Comparison

| Metric | Current | Traveller (15-35/subsector) | Increase |
|--------|---------|----------------------------|----------|
| Systems per subsector | 1-3 (avg 2) | 15-35 (avg 25) | **12.5x** |
| Systems per galaxy | ~19,700 | ~250,000 | **12.7x** |
| Celestial bodies | ~80,000 | ~1,000,000 | **12.5x** |
| DB rows per galaxy | ~122,000 | ~1,525,000 | **12.5x** |
| DB rows (17 galaxies) | ~2.07M | ~25.9M | **12.5x** |

### 2.2 Memory Impact (Traveller)

```
Per galaxy (Traveller):  ~103 MB × 12.5 = ~1.29 GB
17 galaxies:             ~1.29 GB × 17 = ~21.9 GB heap
```

**This exceeds the Pi's available memory (24.9 GB virtual, ~4 GB physical).** Even with aggressive GC, loading all 17 Traveller galaxies into memory simultaneously would cause OOM errors.

### 2.3 CPU Impact

| Operation | Current | Traveller | Notes |
|-----------|---------|-----------|-------|
| Galaxy generation time | ~30s | ~6-12 min | More systems to generate |
| persistGalaxy() DB writes | ~122K rows | ~1.5M rows | Single transaction |
| Startup (17 galaxies) | ~8 min | ~2-3 hours | Sequential generation |
| Game state serialization | 146 MB (full) | ~1.8 GB | JSON output |
| Summary mode | 1.9 MB | ~24 MB | Still manageable |

### 2.4 Pi Hardware Constraints

| Resource | Pi Spec | Current Usage | Traveller Usage | Verdict |
|----------|---------|--------------|-----------------|---------|
| RAM | 4 GB physical | ~2 GB heap | ~22 GB heap | **❌ OOM** |
| Swap | 18 GB total | Minimal | Heavy swapping | **❌ Too slow** |
| CPU | Quad-core ARM | ~30% during gen | 100% for hours | **❌ Unusable** |
| Disk I/O | 500GB USB SSD | Moderate | Extreme | **⚠️ Degraded** |
| H2 DB size | ~500 MB | ~500 MB | ~6 GB | **⚠️ Large** |

---

## 3. Lazy-Loading System Design

### 3.1 Core Concept

**Galaxies are mapped initially but built as needed.** Instead of generating all ~250,000 systems at startup, only the sector structure and subsector metadata are created. Solar systems are generated on-demand when a player's fleet enters an ungenerated subsector.

This mirrors the Younger Industries wormhole transport network: the HQ basement has 16 portals (all mapped), but each destination galaxy is only "built" as the player explores it.

### 3.2 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Younger Industries HQ                     │
│                     (Earth 2026 Hub)                         │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     ┌──────────┐ │
│  │Portal B1 │  │Portal B1 │  │Portal B1 │ ... │Portal B3 │ │
│  │Traveller │  │Dragon's  │  │Babylon 5 │     │007 Tunnel│ │
│  │Gate      │  │Maw       │  │Airlock   │     │          │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘     └────┬─────┘ │
│       │              │              │                │       │
└───────┼──────────────┼──────────────┼────────────────┼───────┘
        │              │              │                │
        ▼              ▼              ▼                ▼
   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
   │ Galaxy 1 │   │ Galaxy 2 │   │ Galaxy 3 │   │ Galaxy N │
   │ (Mapped) │   │ (Mapped) │   │ (Mapped) │   │ (Mapped) │
   │          │   │          │   │          │   │          │
   │ Sector 0 │   │ Sector 0 │   │ Sector 0 │   │ Sector 0 │
   │  [✓Built]│   │  [ ]     │   │  [ ]     │   │  [ ]     │
   │ Sector 1 │   │ Sector 1 │   │ Sector 1 │   │ Sector 1 │
   │  [ ]     │   │  [ ]     │   │  [ ]     │   │  [ ]     │
   │ ...      │   │ ...      │   │ ...      │   │ ...      │
   └──────────┘   └──────────┘   └──────────┘   └──────────┘
```

### 3.3 Three-Tier Lazy Loading

**Tier 1: Galaxy Map (Always Loaded)**
- Galaxy metadata (name, game type, seed, hyperlane density)
- 100 Sector entries (row, col, name, trade type)
- 10,000 SubSector entries (row, col, name, generated flag)
- **Memory: ~5 MB per galaxy** (metadata only, no systems)

**Tier 2: Sector Build (On First Entry)**
- When a fleet enters a sector for the first time, generate all subsectors in that sector
- Each subsector generates 15-35 systems (Traveller-style)
- Systems are persisted to DB immediately
- **Memory: ~103 MB per sector** (1% of galaxy)

**Tier 3: SubSector Detail (On Fleet Proximity)**
- When a fleet approaches a subsector, load its systems into memory
- Celestial body hex geometry is generated lazily (already implemented via `hexGeometryGenerated` flag)
- **Memory: ~10 MB per subsector**

### 3.4 Implementation Plan

#### Phase 1: SubSector Lazy Generation (Already Partially Implemented)

The `SubSector` class already has:
- `generated` boolean flag
- `generateSystems()` method that creates systems on demand
- Empty constructor that creates ungenerated subsectors

**Changes needed:**
1. In `GalaxyGenerator.generateSectorSystems()`, skip system generation for unentered subsectors
2. Instead, create empty `SubSector` instances with `generated=false`
3. Add `ensureGenerated()` method that triggers `generateSystems()` when a fleet enters

```java
// In SubSector.java
public List<SolarSystem> ensureGenerated(Random random, StarNameDatabase starNames, PlanetNameDatabase planetNames) {
    if (!generated) {
        generateSystems(random, starNames, planetNames);
        // Persist newly generated systems to DB
        galaxyService.persistSubSectorSystems(this);
    }
    return new ArrayList<>(systems.values());
}
```

#### Phase 2: Sector-Level Lazy Building

**New class: `SectorBuilderService`**

```java
@Service
public class SectorBuilderService {
    
    /**
     * Generates all systems in a sector when a fleet first enters.
     * Uses the galaxy's seed to ensure deterministic generation.
     */
    public void buildSector(Galaxy galaxy, int sectorRow, int sectorCol) {
        Galaxy.Sector sector = galaxy.getSector(sectorRow, sectorCol);
        if (sector.isBuilt()) return;
        
        Random random = new Random(galaxy.getSeed() ^ (sectorRow * 10 + sectorCol));
        int systemsPerSector = galaxy.getConfig().getSystemCount() / Galaxy.TOTAL_SECTORS;
        
        // Generate systems using Traveller-style distribution
        // 15-35 per subsector instead of 1-3
        generateSectorSystemsTraveller(sector, systemsPerSector, random);
        
        sector.markBuilt();
        galaxyService.persistSector(sector);
    }
}
```

#### Phase 3: Fleet Entry Trigger

**In `SessionActionHandler.java` (or equivalent fleet movement handler):**

```java
public void onFleetMove(Fleet fleet, int targetSectorRow, int targetSectorCol) {
    Galaxy galaxy = fleet.getGalaxy();
    
    // Trigger lazy sector build if needed
    if (!galaxy.getSector(targetSectorRow, targetSectorCol).isBuilt()) {
        sectorBuilderService.buildSector(galaxy, targetSectorRow, targetSectorCol);
    }
    
    // Trigger lazy subsector generation if needed
    SubSector targetSubSector = galaxy.getSubSector(...);
    if (!targetSubSector.isGenerated()) {
        targetSubSector.ensureGenerated(galaxy.getRandom(), ...);
    }
}
```

#### Phase 4: Startup Optimization

**In `MultiGalaxyBuildQueue.processEntry()`:**

```java
private Galaxy processEntry(GalaxyStartupEntry entry) {
    // Instead of generating all systems:
    // Galaxy galaxy = galaxyService.generateAndSaveGalaxyFromTemplate(...);
    
    // Create galaxy structure only (sectors + subsectors, no systems):
    Galaxy galaxy = galaxyService.createGalaxyStructure(entry.getGalaxyName(), ...);
    
    // Pre-build only the Sol system sector (Earth landing point):
    sectorBuilderService.buildSector(galaxy, solSectorRow, solSectorCol);
    
    return galaxy;
}
```

### 3.5 Database Schema Changes

**New table: `sector_build_status`**

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `galaxy_id` | BIGINT | FK to galaxies |
| `sector_row` | INT | 0-9 |
| `sector_col` | INT | 0-9 |
| `built` | BOOLEAN | Whether systems are generated |
| `system_count` | INT | Number of systems in this sector |
| `built_at` | TIMESTAMP | When the sector was built |

**New table: `subsector_build_status`**

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `sector_id` | BIGINT | FK to sectors |
| `sub_sector_row` | INT | 0-9 |
| `sub_sector_col` | INT | 0-9 |
| `generated` | BOOLEAN | Whether systems are generated |
| `system_count` | INT | Number of systems |
| `generated_at` | TIMESTAMP | When generated |

### 3.6 Younger Industries Portal Integration

The portal system provides the natural trigger for lazy loading:

1. **Player at HQ** → Sees 16 portals (all mapped, destinations known)
2. **Player enters portal** → Triggers galaxy sector build for the Sol system (Earth landing point)
3. **Player explores** → Each new sector triggers lazy generation
4. **Progress tracking** → Galaxy completion percentage reflects built sectors

**Portal-to-Galaxy Mapping:**

| Portal | Galaxy | Pre-built Sector |
|--------|--------|-----------------|
| Traveller Spinward Gate | Traveller Galaxy | Sol sector (Earth) |
| Dragon's Maw Portal | D&D Galaxy | Prime Material sector |
| Babylon Station Airlock | Babylon 5 Galaxy | Central Command sector |
| ... | ... | ... |

### 3.7 Memory Budget with Lazy Loading

| Scenario | Galaxies Mapped | Sectors Built | Memory Usage |
|----------|----------------|--------------|--------------|
| Startup | 17 | 17 (Sol only) | ~5 MB × 17 + 103 MB × 17 = ~2 MB |
| Player enters 1 galaxy | 17 | 17 + 10 (1 galaxy) | ~2 MB + 1 GB = ~1 GB |
| Player explores 5 galaxies | 17 | 17 + 50 (5 galaxies) | ~2 MB + 5 GB = ~5 GB |
| All galaxies fully built | 17 | 1,700 (all) | ~22 GB (same as eager) |

**Key insight:** Lazy loading defers the memory cost until the player actually explores. Most players will only explore a few galaxies, keeping memory well within Pi limits.

---

## 4. Traveller UWP Integration with Lazy Loading

### 4.1 UWP Data Generation

When a subsector is lazily generated, each planet gets UWP data:

```java
// In SubSector.generateSystems() with Traveller mode
for (int i = 0; i < systemCount; i++) {
    SolarSystem system = new SolarSystem(id * 1000 + i + 1);
    
    // Generate star
    Star star = bodyGenerator.generateStar();
    
    // Generate planets with UWP
    int planetCount = 1 + random.nextInt(5);
    for (int p = 0; p < planetCount; p++) {
        Planet planet = bodyGenerator.generatePlanet();
        
        // Traveller UWP generation
        if (useTravellerUWP) {
            String uwp = sectorGenerator.generateRandomUwp();
            planet.setUwp(uwp);
            planet.setTradeCodes(deriveTradeCodes(uwp));
        }
    }
}
```

### 4.2 Named Subsectors

Instead of generic names (Alphauri, Bellerophon), use Traveller-style names:

```java
// Traveller subsector names from Solomani Rim
String[] travellerSubsectorNames = {
    "Ultima", "Suleiman", "Concord", "Harlequin", "Alderamin",
    "Esperance", "Vega", "Banasdan", "Albadawi", "Dingir",
    "Sol", "Arcturus", "Jardin", "Capella", "Gemini", "Kukulcan"
};

// Assign names during sector build
String name = travellerSubsectorNames[subsectorId % travellerSubsectorNames.length];
subSector.setName(name);
```

### 4.3 Capital World Designation

After generating all worlds in a subsector, designate the highest-population world as the capital:

```java
// In SectorBuilderService after subsector generation
SolarSystem capital = subSector.getAllSystems().values().stream()
    .flatMap(sys -> sys.getAllPlanets().values().stream())
    .max(Comparator.comparingInt(Planet::getCurrentPopulation))
    .ifPresent(planet -> planet.setCapital(true));
```

---

## 5. Implementation Priority

### Priority 1: Core Lazy Loading (Week 1)

1. **Add `isBuilt()` flag to `Galaxy.Sector`** — Track whether a sector's systems are generated
2. **Modify `GalaxyGenerator`** — Add `generateStructureOnly()` mode that creates sectors/subsectors without systems
3. **Add `SectorBuilderService`** — Handles on-demand sector generation
4. **Modify `MultiGalaxyBuildQueue`** — Use structure-only mode at startup, pre-build Sol sector only

### Priority 2: Fleet Entry Trigger (Week 2)

1. **Add fleet movement hook** — Detect when fleet enters new sector/subsector
2. **Trigger lazy generation** — Call `ensureGenerated()` on entry
3. **Persist generated systems** — Save to DB immediately after generation
4. **Add progress tracking** — Update galaxy completion percentage as sectors are built

### Priority 3: Traveller UWP Integration (Week 3)

1. **Increase systems per subsector** — Change `MIN_SYSTEMS_PER_SUBSECTOR` to 15, `MAX` to 35
2. **Integrate UWP generation** — Call `SectorGenerator.generateRandomUwp()` during lazy build
3. **Named subsectors** — Use Traveller-style names
4. **Capital world** — Designate capital after generation

### Priority 4: Portal Integration (Week 4)

1. **Map portals to galaxies** — Each portal targets a specific galaxy
2. **Pre-build Sol sector** — When portal is first used, ensure Sol sector is built
3. **Progress display** — Show galaxy exploration percentage in portal UI
4. **Galaxy health update** — Reflect built sectors in health score

---

## 6. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Existing galaxies lose data | Medium | High | Migration script to mark existing sectors as built |
| Concurrent fleet entry race condition | Medium | Medium | Synchronized sector build per galaxy |
| DB transaction too large | Low | High | Batch persist in chunks of 1,000 systems |
| Pi disk space exhaustion | Low | Medium | Monitor H2 DB size, archive old data |
| Game state API changes | High | Medium | Keep summary mode compatible, add `sectorsBuilt` field |

---

## 7. Migration Strategy

### 7.1 Existing Galaxies

For the 17 existing galaxies with ~19,700 systems each:

1. **Mark all sectors as built** — Existing systems are already generated
2. **Keep existing data** — No data loss
3. **New galaxies use lazy mode** — Only structure at creation, systems on demand

### 7.2 Configuration Flag

Add to `GalaxyGenerationConfig`:

```java
public Builder lazyLoading(boolean lazy) {
    this.lazyLoading = lazy;
    return this;
}
```

When `lazyLoading=true`, only the Sol sector is pre-built. When `false`, full eager generation (for testing/small galaxies).

---

## 8. Summary

| Metric | Current (Eager) | Traveller (Eager) | Traveller (Lazy) |
|--------|-----------------|-------------------|------------------|
| Startup time | ~8 min | ~2-3 hours | ~30 sec |
| Peak memory | ~2 GB | ~22 GB (OOM) | ~500 MB |
| DB rows at startup | ~2M | ~26M | ~170K |
| Systems per galaxy | ~19,700 | ~250,000 | ~250,000 (on demand) |
| Pi feasibility | ✅ Works | ❌ OOM | ✅ Works |

**Recommendation:** Implement lazy loading (Phase 1-2) before increasing systems per subsector to Traveller levels. This keeps the system functional on Pi hardware while enabling the richer Traveller-style galaxy structure.
