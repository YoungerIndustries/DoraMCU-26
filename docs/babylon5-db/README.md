# Babylon 5 Universe — DoraMCU Format Database

## Overview

This directory contains a complete **Babylon 5** universe database formatted for integration with the **DoraMCU** game engine. The database includes characters, alien races, storylines, dialogues, and locations from the classic sci-fi series (1993–1998) created by J. Michael Straczynski.

## Files

| File | Description |
|------|-------------|
| `babylon5-universe.json` | Complete database with all data in DoraMCU-compatible JSON format |
| `README.md` | This file — documentation and integration guide |

## Database Structure

### Races (13 entries)

Each race follows the DoraMCU `Species` / `AlienRace` pattern with:

- **ID**: Unique identifier (e.g., `HUMAN`, `NARN`, `MINBARI`)
- **Name**: Display name
- **Description**: Lore description
- **Homeworld**: Planet or sector of origin
- **Government**: Political structure
- **Lifespan**: Maximum age in years
- **Characteristics**: ST, DX, EN, IN, ED, SO modifiers (DoraMCU 2E format)
- **Special Abilities**: Race-specific powers
- **Combat Style**: Fighting doctrine
- **Trade Bonus**: Percentage bonus to trade profit
- **Social DM**: Die modifier for social interactions
- **Navigation DM**: Die modifier for piloting/navigation
- **Armor Bonus**: Extra armor class

**Races included:**
- Human / Earth Alliance
- Narn
- Centauri
- Minbari
- Vorlon
- Shadow
- Drazi
- Vree
- Hyach
- Brakiri
- Pak'ma'ra
- Drakh
- Lorien / First Ones

### Characters (20 entries)

Each character follows the DoraMCU `DoraTCharacter` + `CharacterPersonality` pattern with:

- **ID**: Unique identifier (e.g., `SHERIDAN`, `GKAR`, `DELN`)
- **Name**: Full character name
- **Species**: Race ID (links to races section)
- **Actor**: Portrayed by
- **Role**: In-universe position
- **Description**: Character biography
- **First/Last Appearance**: Episode range
- **Personality**: 12-axis personality profile (0.0–1.0) matching `CharacterPersonality`:
  - Aggression, Diplomacy, Economic Focus, Scientific Focus
  - Expansion Drive, Risk Tolerance, Altruism, Determination
  - Adaptability, Leadership, Cunning, Honor
- **Archetype**: Character role label (LEADER, DIPLOMAT, WARRIOR, etc.)
- **Characteristics**: ST, DX, EN, IN, ED, SO values (2–12 scale)
- **Key Skills**: Primary abilities
- **Story Arc**: Character development summary
- **Relationships**: Links to other characters with relationship type
- **Famous Quotes**: Iconic lines from the series

**Main characters included:**
- John Sheridan (Bruce Boxleitner)
- Susan Ivanova (Claudia Christian)
- Delenn (Mira Furlan)
- G'Kar (Andreas Katsulas)
- Londo Mollari (Peter Jurasik)
- Michael Garibaldi (Jerry Doyle)
- Jeffrey Sinclair (Michael O'Hare)
- Stephen Franklin (Richard Biggs)
- Lyta Alexander (Patricia Tallman)
- Alfred Bester (Walter Koenig)
- Vir Cotto (Stephen Furst)
- Elizabeth Lochley (Tracy Scoggins)
- Lennier (Bill Mumy)
- Kosh Naranek (Ardwight Chamberlain)
- Byron (Robin Atkin Downes)
- Mr. Morden (Ed Wasser)
- Zathras (Tim Choate)
- Zack Allan (Jeff Conaway)
- Marcus Cole (Jason Carter)
- Lorien (Wayne Alexander)

### Storylines (6 entries)

Each storyline follows the DoraMCU `CharacterArc` pattern with:

- **ID**: Unique identifier (e.g., `SHADOW_WAR`, `EARTH_DICTATORSHIP`)
- **Title**: Storyline name
- **Description**: Plot summary
- **Seasons**: Seasons covered
- **Key Characters**: Character IDs involved
- **Chapters**: Season-by-season breakdown with key events

**Storylines included:**
1. **The Shadow War** — The central conflict between Shadows, Vorlons, and the younger races
2. **Earth's Descent into Dictatorship** — Clark's rise and the Mars conflict
3. **The Telepath Crisis** — Psi Corps abuse and the telepath underground
4. **The Centauri Tragedy** — Londo Mollari's Faustian bargain
5. **The Narn Redemption** — G'Kar's journey from survivor to prophet
6. **The Minbari Spiritual Journey** — Delenn's transformation and leadership

### Dialogues (10 entries)

Iconic scenes with full dialogue, formatted for AI character voice training:

- **ID**: Unique identifier
- **Title**: Scene name
- **Scene**: Episode reference
- **Characters**: Character IDs involved
- **Dialogue**: Speaker-line pairs

**Key dialogues included:**
- "The Third Age" — Sheridan and Delenn after the Shadow War
- "The Apology" — Sheridan's famous apology
- "The Burden" — G'Kar and Delenn's redemption scene
- "The Final Monologue" — Delenn's series-ending narration
- And 6 more iconic scenes

### Locations (8 entries)

Key locations with faction and significance:

- Babylon 5 (space station)
- Earth (planet)
- Mars (planet)
- Minbar (planet)
- Centauri Prime (planet)
- Narn 3 (planet)
- Babylon 4 (space station)
- Skarrin (planet)

## Integration with DoraMCU

### Adding Races to the Game

To add Babylon 5 races to the DoraMCU `Species` enum:

```java
// Example: Adding Narn to Species.java
NARN("Narn", "Proud, resilient species scarred by Centauri occupation",
        1, 0, 1, 0, -1, -1, 60),
```

Map the JSON `characteristics` fields to the `Species` constructor parameters:
- `ST` → `stMod`
- `DX` → `dxMod`
- `EN` → `enMod`
- `IN` → `inMod`
- `ED` → `edMod`
- `SO` → `soMod`

### Creating Characters

To create a Babylon 5 character in DoraMCU:

```java
DoraTCharacter character = new DoraTCharacter();
character.setName("John Sheridan");
character.setSpecies(Species.HUMAN);
character.setCharacteristics(Map.of(
    Characteristic.ST, 8,
    Characteristic.DX, 9,
    Characteristic.EN, 8,
    Characteristic.IN, 9,
    Characteristic.ED, 10,
    Characteristic.SO, 10
));

CharacterPersonality personality = new CharacterPersonality();
personality.setAggression(0.7);
personality.setDiplomacy(0.6);
personality.setLeadership(0.95);
// ... set all 12 axes from the JSON
personality.setArchetype("LEADER");
```

### Using Storylines for AI Narratives

The storyline data can drive AI-generated narratives:

```java
// Load storyline chapters for AI context
Storyline shadowWar = loadStoryline("SHADOW_WAR");
for (Chapter chapter : shadowWar.getChapters()) {
    aiService.setNarrativeContext(chapter.getTitle(), chapter.getDescription());
    aiService.addKeyEvents(chapter.getKeyEvents());
}
```

### Using Dialogues for Voice Training

The dialogue data can train AI character voices:

```java
// Load dialogues for a character's voice profile
List<Dialogue> dialogues = loadDialoguesForCharacter("SHERIDAN");
for (Dialogue dialogue : dialogues) {
    for (Line line : dialogue.getDialogue()) {
        if (line.getSpeaker().equals("SHERIDAN")) {
            voiceTrainer.addSample(line.getLine());
        }
    }
}
```

## Data Format Compatibility

This database is designed to be directly compatible with DoraMCU's existing data structures:

| Babylon 5 JSON | DoraMCU Java Class | Notes |
|----------------|-------------------|-------|
| `races[].characteristics` | `Species` enum modifiers | Direct mapping |
| `characters[].personality` | `CharacterPersonality` entity | 12-axis match |
| `characters[].characteristics` | `DoraTCharacter.characteristics` | 2D6 scale (2–12) |
| `storylines[]` | `CharacterArc` service | Chapter-based arcs |
| `dialogues[]` | AI voice training data | Speaker-line format |
| `locations[]` | `HomeLocation` entities | Faction-based |

## Extending the Database

To add more content:

1. **More characters**: Add entries to the `characters` array following the existing pattern
2. **More dialogues**: Add entries to the `dialogues` array with speaker-line pairs
3. **More storylines**: Add entries to the `storylines` array with chapter breakdowns
4. **More locations**: Add entries to the `locations` array with faction info

## Sources

- Babylon 5 TV series (1993–1998), created by J. Michael Straczynski
- Wikipedia: List of Babylon 5 characters
- Wikipedia: Civilizations in Babylon 5
- The Babylon Project (Fandom wiki)
- Chronology.org — Speeches and Monologues from Babylon 5
- IMDb quotes database

## License

This database is a fan-created reference work. Babylon 5 is a trademark of Warner Bros. The data is provided for educational and game development purposes only.
