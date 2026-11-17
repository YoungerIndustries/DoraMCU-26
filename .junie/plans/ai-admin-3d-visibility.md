---
sessionId: session-261117-051457-4ssl
---

# AI Admin 3D Visibility — Complete ✓

All three delivery steps have been implemented, tested, committed, and pushed to branch `Lapis111426`.

# Next: Seed AI Admin Positions

### Overview & Goals
Seed 3D positions for all existing AI admin characters so they appear in the Milky Way 3D galaxy view. Currently, all AI admins have null positions and their `galaxy_name` is set to their empire name (e.g., "United States") rather than "Milky Way", so they never appear in the 3D renderer.

### Scope

**In Scope:**
- Update `galaxy_name` to "Milky Way" for all Earth-based AI admins (34 country leaders + governors + UN Secretary-General)
- Assign 3D positions based on real-world capital city coordinates mapped to the galaxy's coordinate system
- Place country leaders at their capital cities
- Place region governors near their regional capitals
- Place the UN Secretary-General at a central Earth position
- Create V102 migration script

**Out of Scope:**
- Entity-linked AI characters (ship/station/vehicle/facility computers) — they have separate positioning needs
- Younger Industries office managers — not yet seeded as AI characters
- Automatic position updates from order execution

### Coordinate System
The Galaxy3DService uses a coordinate system where:
- Stars span ±500 units in x/z, ±50 units in y
- Planets orbit at 10-50 units from their star
- Earth will be positioned near the galaxy center
- Capital cities will be placed on Earth's surface using a simplified coordinate mapping

### Positioning Strategy
Map real-world capital city coordinates to 3D positions on Earth's surface:
- Earth center at (0, 0, 0) in the Milky Way galaxy
- Capital cities placed on a sphere with radius ~10 units (Earth's "surface")
- Use latitude/longitude to calculate x, y, z positions
- Country leaders at their capital city
- Region governors offset slightly from their capital

### Key Files
| File | Purpose |
|------|---------|---|
| `V102__seed_ai_admin_positions.sql` | New migration that sets galaxy_name and 3D positions |
| `AiCharacter.java` | Entity with posX, posY, posZ, galaxyName fields |
| `Galaxy3DService.java` | Queries AI admins by galaxy_name for 3D rendering |
| `V34__seed_earth_2026_ai_characters.sql` | Reference for AI character IDs |
| `V33__add_earth_2026_planet_regions.sql` | Reference for country/region data |
| `V35__seed_earth_2026_capital_landing_points.sql` | Reference for capital cities |

### Technical Design

### Current State
- **AiCharacter**: Has `posX`, `posY`, `posZ`, `galaxyName` fields (V101 migration)
- **Galaxy3DService**: Queries AI admins by `galaxyName` for 3D rendering
- **V34 migration**: Seeds 34 AI characters (20 country leaders, 12 region governors, 1 UN Secretary-General)
- **V101 backfill**: Set `galaxy_name = empire_name`, so country leaders have galaxy_name = country name, not "Milky Way"
- **Problem**: AI admins never appear in Milky Way 3D view because their `galaxy_name` doesn't match

### Key Decision
- **Galaxy name**: Update `galaxy_name` to "Milky Way" for all Earth-based AI admins
- **Position radius**: Earth surface at radius 10 units from galaxy center (0, 0, 0)
- **Coordinate mapping**: Convert real-world latitude/longitude to 3D sphere coordinates

### Proposed Changes

**V102__seed_ai_admin_positions.sql (new migration):**
- UPDATE all Earth-based AI admins (IDs 2-34): set `galaxy_name = 'Milky Way'`
- SET 3D positions for 20 country leaders at capital city coordinates
- SET 3D positions for 12 region governors near regional capitals
- SET UN Secretary-General at (0, 0, 0) — Earth center

**Position calculation:**
```
x = r * cos(latitude) * sin(longitude)
y = r * sin(latitude)
z = r * cos(latitude) * cos(longitude)
```
Where r = 10 for Earth surface, latitude/longitude in radians.

### Risks
- **Cache**: Galaxy3DService caches galaxy data; call `removeGalaxyCache("Milky Way")` after migration if needed
- **Precision**: Rounded coordinates may cause slight position inaccuracies

# Delivery Steps

### ✓ Step 1: Seed 3D positions for Earth-based AI admins
Assign 3D coordinates to all 34+ Earth-based AI admin characters so they appear in the Milky Way 3D galaxy view.

**Migration (V102__seed_ai_admin_positions.sql):**
- Update `galaxy_name` from empire name to "Milky Way" for all Earth-based AI admins (IDs 2-34)
- Set 3D positions for 20 country leaders at their capital cities using lat/lon mapped to sphere coordinates
- Set 3D positions for 12 region governors near their regional capitals
- Set 3D position for UN Secretary-General at Earth center (0, 0, 0)
- Earth positioned at galaxy center with capital cities on a sphere of radius ~10 units

**Position mapping formula:**
- Convert latitude/longitude to 3D: x = r * cos(lat) * sin(lon), y = r * sin(lat), z = r * cos(lat) * cos(lon)
- Radius r = 10 units for Earth surface
- Region governors offset by 0.5 units from their capital to avoid overlap

**Verification:**
- Query Galaxy3DService for "Milky Way" galaxy, verify AI_ADMIN bodies are included
- Check that country leaders appear at their capital city positions
- Verify proximity queries return nearby admins when player is near a capital