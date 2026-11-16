# DoraMCU Character Sheet Guide

> **How to create characters and add them to the galaxy lore**
> This guide explains how to use the character sheet templates to create player characters and AI characters for the DoraMCU galaxy.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Character Sheet Templates](#2-character-sheet-templates)
3. [Creating a Player Character](#3-creating-a-player-character)
4. [Creating an AI Character](#4-creating-an-ai-character)
5. [Filling Out the Sheets](#5-filling-out-the-sheets)
6. [Adding Characters to Galaxy Lore](#6-adding-characters-to-galaxy-lore)
7. [API Submission](#7-api-submission)
8. [JSON Import](#8-json-import)
9. [Game System Mapping](#9-game-system-mapping)
10. [Tips & Best Practices](#10-tips--best-practices)

---

## 1. Overview

DoraMCU supports multiple game systems (Traveller, D&D 5e, Earth 2026, Captain's Log, Babylon 5, Star Wars, Warhammer 40K, and more). Each system has its own character model, but all characters share a common set of universal attributes that make it possible to transition between game systems.

The character sheet templates provide a **unified, printable form** that works across all game systems. You can:

- **Print and fill out** the sheets by hand for tabletop play
- **Digitally complete** the sheets and submit via the DoraMCU API
- **Export as JSON** using the provided schema for programmatic import
- **Create AI characters** that populate the galaxy with interactive NPCs

---

## 2. Character Sheet Templates

Two templates are provided:

| Template | File | Use Case |
|----------|------|----------|
| **Universal Character Sheet** | `docs/character-sheet-template.md` | Player characters and general character creation |
| **AI Character Sheet** | `docs/ai-character-sheet-template.md` | AI-controlled NPCs with personality and dialogue |
| **JSON Schema** | `docs/character-sheet-schema.json` | Programmatic validation and import |

### When to Use Each Template

- **Universal Character Sheet**: Use this for player characters (PCs) and any character that might transition between game systems. It includes all fields from all supported game systems.

- **AI Character Sheet**: Use this when creating AI-controlled characters. It emphasizes personality, dialogue, behavior rules, and neuron configuration — the fields that matter most for AI NPCs.

---

## 3. Creating a Player Character

### Step 1: Choose Your Game System

Decide which game system your character originates from. This determines which sections of the character sheet are most relevant:

| Game System | Key Sections |
|-------------|--------------|
| **Traveller** | III (Abilities), IX (Career), VIII (Psionics) |
| **D&D 5e** | III (Abilities), IV (Combat), V (Skills), VIII (Spellcasting) |
| **Earth 2026** | I (Identity), III (Abilities), VI (Background) |
| **Captain's Log** | III (Abilities), IX (Career Traits), V (Disciplines) |
| **Babylon 5** | I (Identity), II (Type), XI (Lore) |
| **Star Wars** | I (Identity), VIII (Force), XI (Lore) |
| **Warhammer 40K** | I (Identity), II (Type), VI (Background) |

### Step 2: Fill Out Section I — Character Identity

Start with the basics:

- **Name**: Give your character a name that fits the game system and lore
- **Species/Race**: Choose from the available species in your game system
- **Homeworld**: Select or create a planet of origin
- **Physical Description**: Fill in height, weight, eye/hair color, and distinguishing features

### Step 3: Fill Out Section III — Ability Scores

All characters use the universal 6-attribute system (STR, DEX, CON, INT, WIS, CHA). Additional attributes are game-system-specific:

- **Traveller**: Uses 2D6 rolls (range 2–12) for each ability
- **D&D 5e**: Uses standard array or 4d6-drop-lowest (range 3–18, or 1–20 with racial bonuses)
- **Captain's Log**: Uses FIT, TEC, STR, REA, INS, PRE (0–5 scale)
- **Earth 2026**: Uses standard 1–20 scale

### Step 4: Fill Out the Remaining Sections

Complete all sections relevant to your game system. Use the checklist at the end of the sheet to ensure nothing is missed.

### Step 5: Submit Your Character

See [Section 7](#7-api-submission) for API submission or [Section 8](#8-json-import) for JSON import.

---

## 4. Creating an AI Character

AI characters are NPCs controlled by the DoraMCU AI neuron system. They need detailed personality profiles and dialogue to feel alive.

### Step 1: Define the Character's Role

Decide what role this AI character plays in the galaxy:

- **Quest Giver**: Provides missions and story hooks
- **Merchant**: Sells goods and services
- **Rival**: Competes with or opposes the player
- **Ally**: Helps the player character
- **Antagonist**: Main villain or obstacle
- **Mentor**: Teaches and guides the player
- **Informant**: Provides information and lore
- **Companion**: Travels with the player

### Step 2: Build the Personality Profile

The personality profile (Section III of the AI sheet) is the most important part:

- **Personality Type**: Choose from Heroic, Villainous, Neutral, Chaotic, or Lawful
- **Behavioral Archetype**: Leader, Scholar, Warrior, Diplomat, Scout, Merchant, Spy, or Healer
- **Speech Style**: How the character speaks — formal, casual, archaic, technical, etc.
- **Decision-Making Pattern**: Logical, Emotional, Intuitive, or Random

### Step 3: Write Dialogue

Provide at least 5 sample dialogue lines that capture the character's voice. These are used by the AI neuron to generate consistent responses.

### Step 4: Configure the AI Neuron

Fill out Section X (AI Neuron Configuration) with:

- **System Prompt**: A template that defines the AI's identity and behavior
- **Behavior Rules**: Specific rules for how the character should act
- **Memory & State**: Whether the character remembers players, adapts, and forms relationships

### Step 5: Submit Your AI Character

Use the same submission methods as player characters, but ensure `characterType.type` is set to `"ai"`.

---

## 5. Filling Out the Sheets

### Printable Format

The character sheets are designed to be printed on standard letter/A4 paper. To print:

1. Open the Markdown file in any Markdown viewer
2. Use your browser's print function (Ctrl+P / Cmd+P)
3. Select "Save as PDF" or print directly

### Digital Format

You can also fill out the sheets digitally:

1. Copy the Markdown file
2. Replace the `_____` and `☐` placeholders with your values
3. Save the completed sheet
4. Submit via API or convert to JSON

### Checkbox Conventions

- `☐` = Unchecked option
- `☑` = Checked option (use when filling digitally)
- `_____` = Fill-in-the-blank field
- `✓` = Proficiency marker
- `★` = Expertise marker

---

## 6. Adding Characters to Galaxy Lore

Every character in DoraMCU is connected to the galaxy's lore. Section XI of the character sheet (Galaxy Lore Integration) establishes these connections.

### Lore Categories

| Category | Description |
|----------|-------------|
| **Character** | The character is a notable figure in the galaxy |
| **Faction** | The character represents or leads a faction |
| **Event** | The character was involved in a key historical event |
| **Species** | The character is notable for their species |
| **Planet** | The character is tied to a specific planet |

### Lore ID (Slug)

Every lore entry needs a URL-friendly slug. Examples:

- `darth-vader`
- `james-t-kirk`
- `argentina-kingdom`
- `the-clone-wars`

### Significance

Describe why this character matters to the galaxy. Examples:

- "Last surviving Jedi Knight of the Old Republic"
- "Founder of the Merchant Guild that controls trade in the Core Worlds"
- "The only person to survive the destruction of their homeworld"

### Key Events

List the major events the character was involved in. These connect the character to the timeline of the galaxy.

### Notable Locations

List the planets, stations, and locations the character has visited. This helps with encounter generation and story placement.

---

## 7. API Submission

### Submit a Player Character

```bash
# Traveller character
curl -X POST "http://localhost:8080/api/v1/traveller/characters?name=CharacterName"

# Shared character (cross-game)
curl -X POST "http://localhost:8080/api/v1/shared-characters" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Character Name",
    "species": "Human",
    "originSystem": "traveller",
    "dexterity": 8,
    "technical": 7,
    "strength": 6,
    "intelligence": 9,
    "wisdom": 8,
    "charisma": 10,
    "constitution": 7
  }'

# D&D 5e character
curl -X POST "http://localhost:8080/api/v1/dnd5e/characters" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Character Name",
    "race": "Human",
    "characterClass": "Fighter",
    "level": 1
  }'
```

### Submit an AI Character

```bash
curl -X POST "http://localhost:8080/api/v1/shared-characters" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AI Character Name",
    "species": "Human",
    "originSystem": "star_wars",
    "description": "A Force-sensitive smuggler",
    "dexterity": 12,
    "technical": 8,
    "strength": 10,
    "intelligence": 11,
    "wisdom": 13,
    "charisma": 14,
    "constitution": 9
  }'
```

### Full Character Import (JSON Schema)

```bash
curl -X POST "http://localhost:8080/api/v1/characters/import" \
  -H "Content-Type: application/json" \
  -d @character.json
```

Where `character.json` follows the schema in `docs/character-sheet-schema.json`.

---

## 8. JSON Import

### Converting a Filled Sheet to JSON

1. Use the JSON schema at `docs/character-sheet-schema.json` as your template
2. Fill in the fields that correspond to your character sheet
3. Validate your JSON against the schema
4. Submit via the API

### Example JSON Character

```json
{
  "identity": {
    "name": "Kira Voss",
    "alias": "The Ghost",
    "pronouns": "she/her",
    "age": 28,
    "species": "Human",
    "homeworld": "Coruscant",
    "eyeColor": "Green",
    "hairColor": "Black"
  },
  "characterType": {
    "type": "player",
    "originSystem": "star_wars",
    "currentGalaxy": "Star Wars Galaxy",
    "affiliation": "Rebel Alliance",
    "occupation": "Smuggler"
  },
  "abilities": {
    "strength": 10,
    "dexterity": 14,
    "constitution": 12,
    "intelligence": 13,
    "wisdom": 11,
    "charisma": 12
  },
  "lore": {
    "category": "character",
    "loreId": "kira-voss",
    "significance": "Smuggler who became a key Rebel intelligence asset",
    "keyEvents": ["Battle of Yavin", "Rescue of Princess Leia"],
    "notableLocations": ["Tatooine", "Yavin 4", "Death Star"]
  }
}
```

### Example AI Character JSON

```json
{
  "identity": {
    "name": "Captain Rex",
    "species": "Human (Clone)",
    "homeworld": "Kamino"
  },
  "characterType": {
    "type": "ai",
    "originSystem": "star_wars",
    "affiliation": "Galactic Republic",
    "rank": "Captain"
  },
  "aiConfig": {
    "personalityType": "heroic",
    "behavioralArchetype": "warrior",
    "speechStyle": "Military formal with occasional humor",
    "decisionPattern": "logical",
    "loyaltyTarget": "Anakin Skywalker and the Republic",
    "motivation": "Protect his troops and serve the Republic",
    "fear": "Losing his men",
    "difficultyLevel": "hard",
    "dialoguePrompts": [
      "Let's get this done, men.",
      "I've got your back, General.",
      "These aren't the droids you're looking for."
    ]
  }
}
```

---

## 9. Game System Mapping

### Universal Attribute Mapping

| Universal Attribute | Traveller | D&D 5e | Captain's Log | Earth 2026 |
|---------------------|-----------|--------|---------------|------------|
| Strength | ST | STR | STR | STR |
| Dexterity | DX | DEX | FIT (partial) | DEX |
| Constitution | — | CON | CON | CON |
| Intelligence | IN | INT | REA | INT |
| Wisdom | — | WIS | INS | WIS |
| Charisma | EN | CHA | PRE | CHA |
| Technical | ED | — | TEC | — |

### Skill Mapping

When a character transitions between game systems, skills are mapped as follows:

- **Traveller → D&D 5e**: Traveller skills map to the closest D&D skill (e.g., Pilot → Athletics, Gunnee → Perception)
- **D&D 5e → Traveller**: D&D skills map to Traveller equivalents (e.g., Stealth → Steward, Perception → Guard)
- **Captain's Log → Universal**: Disciplines map to universal skills based on their focus areas

The `SharedCharacter` entity handles this mapping automatically when a character transitions between systems.

---

## 10. Tips & Best Practices

### For Player Characters

1. **Start with the story**: Before filling in numbers, write a short background story. The numbers should support the story, not define it.
2. **Balance your abilities**: Don't put all points in one attribute. A well-rounded character is more fun to play.
3. **Connect to the lore**: Use Section XI to tie your character to existing galaxy events and locations.
4. **Leave room to grow**: Don't max out everything at character creation. Leave room for development.

### For AI Characters

1. **Give them a voice**: The dialogue lines are crucial. Write lines that sound unique to the character.
2. **Define clear motivations**: An AI character without clear goals feels hollow. Give them something they want.
3. **Add contradictions**: A character who is brave but afraid of heights is more interesting than one who is just brave.
4. **Connect to other characters**: Relationships make the galaxy feel alive. Define allies, enemies, and neutral contacts.
5. **Test the system prompt**: After creating an AI character, test the neuron to make sure the character behaves as expected.

### General Tips

1. **Use the checklist**: Both sheets include creation checklists. Use them to ensure completeness.
2. **Validate JSON**: If submitting via JSON, validate against the schema before submitting.
3. **Keep backups**: Save copies of your filled-out character sheets.
4. **Update regularly**: As your character develops, update the sheet with new skills, equipment, and relationships.
5. **Respect the lore**: When adding characters to the galaxy, make sure they fit the established timeline and rules of the universe.

---

## Appendix A: Character Sheet Files

| File | Location | Purpose |
|------|----------|---------|
| Universal Character Sheet | `docs/character-sheet-template.md` | Printable PC template |
| AI Character Sheet | `docs/ai-character-sheet-template.md` | Printable AI NPC template |
| JSON Schema | `docs/character-sheet-schema.json` | Validation schema |
| This Guide | `docs/CHARACTER_SHEET_GUIDE.md` | Usage instructions |

## Appendix B: Related API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/traveller/characters` | POST | Create Traveller character |
| `/api/v1/shared-characters` | POST | Create shared character |
| `/api/v1/dnd5e/characters` | POST | Create D&D 5e character |
| `/api/v1/captains-log/characters` | POST | Create Captain's Log character |
| `/api/v1/characters` | GET | List all characters (unified) |
| `/api/v1/characters/{id}` | GET | Get character by ID |

## Appendix C: GameLore Integration

When a character is added to the galaxy, a corresponding `GameLore` entry can be created:

```java
// Example: Creating a lore entry for a character
GameLore lore = new GameLore();
lore.setName("Kira Voss");
lore.setSlug("kira-voss");
lore.setGenre("SCI_FI");
lore.setCategory("CHARACTER");
lore.setDescription("Smuggler turned Rebel intelligence asset");
```

The `GameLoreService` handles creating and managing these entries. Characters are linked to lore through the `lore` section of the character sheet.

---

*© DoraMCU — Multi-Game Universe Character Creation System*
*Guide Version 1.0*
