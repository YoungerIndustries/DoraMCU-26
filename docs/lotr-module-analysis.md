# Lord of the Rings Galaxy: Module Analysis & Game Research

## Executive Summary

This document analyzes the existing DoraMCU game modules (Earth2026 and D&D 5e) and researches lore, RPG rules, and FPS/combat mechanics from existing Lord of the Rings games to inform the LOTR galaxy module design.

---

## Part 1: DoraMCU Module Analysis

### 1.1 Earth2026 Module

**Location:** `src/main/java/com/yisy/modules/doramcu/earth2026/`
**Files:** 168 Java files + 43 test files

#### FPS Integration (`Earth2026FpsService`)
Extends `FpsBaseService` to provide modern-themed FPS content:

**Weapons (6):**
| Weapon | Type | Damage | Fire Rate | Range | Mag Size |
|--------|------|--------|-----------|-------|----------|
| M4 Assault Rifle | RIFLE | 30 | 10.0/s | 90m | 30 |
| Desert Eagle | PISTOL | 50 | 3.0/s | 30m | 7 |
| MP5 Submachine Gun | RIFLE | 22 | 15.0/s | 40m | 30 |
| M24 Sniper | SNIPER | 90 | 0.8/s | 250m | 5 |
| M79 Grenade Launcher | ROCKET_LAUNCHER | 120 | 0.6/s | 50m | 1 |
| M249 SAW | MACHINE_GUN | 25 | 12.0/s | 80m | 100 |

**Enemies (4):**
| Enemy | Health | Speed | Behavior | Damage | XP |
|-------|--------|-------|----------|--------|----|
| Corporate Mercenary | 100 | 3.0 | ATTACK | 25 | 120 |
| Riot Police | 80 | 2.5 | PATROL | 15 | 80 |
| Insurgent | 70 | 4.0 | AMBUSH | 20 | 100 |
| Heavy Gunner | 150 | 2.0 | CHASE | 35 | 180 |

**Maps (9):**
- San Diego Streets (urban combat)
- Younger Industries HQ (corporate infiltration)
- San Diego Convention Center (large indoor arena)
- Area 51 (secret military base)
- NYC Rooftops (vertical combat)
- Portal Room (supernatural dimension)
- London Underground (subway combat)
- Tokyo Neon District (cyberpunk streets)
- Cthulhu's Call (Lovecraftian horror)

#### Key Architecture Patterns
- Extends `FpsBaseService` for themed FPS content
- Uses `FpsWeapon`, `FpsEnemy`, `FpsMap` core models
- Weapons have hitScan vs projectile distinction
- Maps include spawn points, collision boxes, power-up locations, capture points, transition points, and scene objects

---

### 1.2 D&D 5e Module

**Location:** `src/main/java/com/yisy/modules/doramcu/dnd5e/`
**Files:** 138 Java files + extensive test coverage

#### Game Module Architecture (`Dnd5eGameModule`)
Implements `GameModule` interface with comprehensive service adapters:

**Services:**
- `Dnd5eDiceService` → DiceService adapter
- `Dnd5eCombatService` → CombatService adapter
- `Dnd5eInventoryService` → Inventory management
- `Dnd5eCraftingService` → Item crafting
- `Dnd5eDungeonRoomService` → Dungeon generation
- `Dnd5eSpellService` → Spell casting
- `Dnd5ePsionService` → Psionic powers
- `Dnd5eEncounterService` → Encounter generation
- `Dnd5eCharacterService` → Character management
- `Dnd5eCampaignService` → Campaign tracking
- `Dnd5eRulesService` → Rules lookup
- `Dnd5eTechLevelAdapter` → Tech level mapping
- `Dnd5eRealEstateService` → Property management
- `Dnd5eBuildingInstanceService` → Building instances
- `Dnd5eRulesEngine` → Rules engine

#### Combat System (`Dnd5eCombatService`)
- **Initiative:** d20 + DEX modifier, sorted descending
- **Attack Resolution:** d20 + attack bonus vs AC
- **Damage:** Formula parsing (e.g., "2d6+3"), critical hits double dice
- **Conditions:** JSON-based condition tracking (prone, restrained, etc.)
- **Round Management:** Round-based combat with turn order

#### FPS Integration (`Dnd5eFpsService`)
Maps D&D concepts to FPS framework:

**Weapons (8):**
| Weapon | Type | Damage | Fire Rate | Notes |
|--------|------|--------|-----------|-------|
| Longsword | RIFLE | 8 | 1.5 | Melee mapped to rifle |
| Longbow | RIFLE | 10 | 1.0 | Ranged weapon |
| Warhammer | RIFLE | 10 | 1.5 | Blunt melee |
| Dagger | PISTOL | 4 | 2.0 | Sidearm equivalent |
| Crossbow | RIFLE | 8 | 0.8 | Slow but accurate |
| Fireball | ROCKET_LAUNCHER | 25 | 0.5 | Area spell |
| Lightning Bolt | ROCKET_LAUNCHER | 20 | 0.5 | Line spell |
| Magic Missile | ROCKET_LAUNCHER | 12 | 1.0 | Auto-hit spell |

**Enemies (7):**
| Enemy | Health | Speed | Behavior | Damage |
|-------|--------|-------|----------|--------|
| Goblin | 7 | 3.0 | PATROL | 3 |
| Orc | 15 | 2.0 | CHASE | 6 |
| Skeleton | 10 | 2.0 | ATTACK | 4 |
| Zombie | 22 | 1.0 | CHASE | 5 |
| Troll | 50 | 2.0 | ATTACK | 10 |
| Dragon | 200 | 4.0 | AMBUSH | 25 |
| Giant | 80 | 2.0 | CHASE | 15 |

**Maps (3):**
- Dragon's Lair (100x50x30)
- Forgotten Forest (200x200x50)
- Crimson Keep (80x80x40)

---

### 1.3 FPS Core Framework

**Location:** `src/main/java/com/yisy/modules/doramcu/fps/`

#### FpsBaseService (Abstract Base Class)
```java
public abstract class FpsBaseService {
    abstract String getTheme();
    List<FpsWeapon> getWeapons();
    List<FpsEnemy> getEnemies();
    List<FpsMap> getMaps();
    void registerContent(FpsWeaponService, FpsMapService);
}
```

#### FpsWeapon Model
| Field | Type | Description |
|-------|------|-------------|
| damage | int | Base damage per hit |
| fireRate | double | Shots per second |
| reloadTime | double | Seconds to reload |
| magSize | int | Rounds per magazine |
| reserveAmmo | int | Total reserve ammo |
| hitScan | boolean | Instant hit vs projectile |
| projectileSpeed | double | Speed if not hitScan |
| recoilPitch | double | Vertical recoil |
| recoilYaw | double | Horizontal recoil |
| range | double | Effective range in meters |

**Weapon Types:** PISTOL, RIFLE, SHOTGUN, MACHINE_GUN, SNIPER, ROCKET_LAUNCHER, GRENADE

#### FpsEnemy Model
| Field | Type | Description |
|-------|------|-------------|
| health | int | Hit points |
| speed | double | Movement speed |
| behavior | EnemyBehavior | AI behavior pattern |
| damage | int | Attack damage |
| attackRange | double | Effective attack range |
| attackCooldown | double | Seconds between attacks |
| dropItem | String | Item dropped on death |
| dropChance | double | Probability of drop |
| experienceReward | int | XP awarded on kill |

**Enemy Behaviors:** PATROL, CHASE, ATTACK, FLANK, RETREAT, AMBUSH

#### FpsMap Model
| Field | Type | Description |
|-------|------|-------------|
| mapWidth/Depth/Height | double | Map dimensions |
| spawnPoints | List<SpawnPoint> | Team spawn locations |
| collisionBoxes | List<CollisionBox> | Solid geometry |
| powerUpLocations | List<PowerUpLocation> | Item spawns |
| capturePoints | List<CapturePoint> | CTF control points |
| transitionPoints | List<TransitionPoint> | Map connections |
| sceneObjects | List<SceneObject> | NPCs, items, triggers |

---

## Part 2: Lord of the Rings Game Research

### 2.1 War of the Ring (Board Game)

**Publisher:** CMON / Ares Games (2nd Edition, 2012)
**Players:** 2-4
**Play Time:** ~180 minutes

#### Core Mechanics
- **Asymmetric Gameplay:** Free Peoples vs Shadow with different win conditions
- **Action Dice Drafting:** Dice determine available actions each round
- **Area Control:** Territory-based map of Middle-earth
- **Corruption Track:** Ring-bearer's corruption level determines Shadow victory
- **Political Track:** Nations move toward war over time

#### Factions & Nations

**Free Peoples:**
- **Gondor:** Primary military force, limited reinforcements
- **Rohan:** Cavalry-focused, fast movement
- **Elves:** Rivendell, Lórien, Woodland Realm, Gray Havens
- **Dwarves:** Erebor, Iron Hills, Blue Mountains
- **The North:** Dale, Carrock, Bree
- **Shire:** Hobbit homeland

**Shadow Armies:**
- **Sauron:** Mordor, Moria, Angmar, Dol Guldur
- **Isengard:** Uruk-hai production
- **Southrons & Easterlings:** Harad and Rhûn forces

#### Unit Types
- **Leaders:** Single figures, command armies
- **Regulars:** Foot soldiers (blue for Free Peoples)
- **Elites:** Cavalry/special troops (mounted for Free Peoples)
- **Factions:** Special characters with unique abilities (2nd Edition)

#### Combat Resolution
- **Standard Combat:** Hit on 5+ and 6s (d6)
- **Siege Combat:** Hit on 6s only
- **Combat Cards:** Modify dice results
- **Up to 5d6** per combat based on unit count

#### Key Mechanics for DoraMCU
1. **Corruption System:** Ring-bearer corruption track
2. **Asymmetric Victory Conditions:** Military vs narrative victory
3. **Action Dice:** Resource management for actions
4. **Nation Political Tracks:** Diplomacy and war declaration
5. **Faction Event Cards:** Character-specific abilities

---

### 2.2 The One Ring RPG (2nd Edition)

**Publisher:** Free League Publishing
**System:** Proprietary dice pool with d12

#### Core Mechanics
- **Dice Pool:** d6 pool + 1d12 (Eye of Sauron die)
- **Successes:** 6s count as successes, compared to difficulty
- **Hope Points:** Spend for attribute bonuses and cultural virtues
- **Shadow Points:** Accumulate from misdeeds, disturbing events, blighted areas
- **Madness:** When Shadow ≥ Hope, character experiences madness bout

#### Character Creation
- **Attributes:** Body, Heart, Wits (dice pool size)
- **Skills:** Under each attribute (Melee, Stealth, Lore, etc.)
- **Callings:** Class equivalents (Warrior, Scout, Ranger, Bard, Mage, Loremaster, etc.)
- **Cultures:** Background with unique virtues
- **Shadow Weakness:** Personal corruption trigger

#### The Eye Mechanic
- **Party Attention:** Group attracts Sauron's attention
- **Hunts:** When attention is high, Shadow pursues the party
- **Feat Face:** d12 shows special outcomes (Hope, Shadow, Feat, Eye)

#### Combat System
- **Attack Rolls:** Dice pool vs opponent's defense
- **Damage:** Wounds reduce Endurance
- **Critical Hits:** Multiple failures cause serious injury
- **Morale:** Fear and courage checks

#### Key Mechanics for DoraMCU
1. **Hope/Shadow Dual Track:** Core moral mechanic
2. **Eye of Sauron Die:** Random narrative intervention
3. **Callings System:** Class-like character roles
4. **Culture Virtues:** Racial/cultural bonuses
5. **Madness System:** Corruption consequences

---

### 2.3 MERP (Middle-earth Role Playing)

**Publisher:** Iron Crown Enterprises (1984)
**System:** Rolemaster-derived d100 percentile

#### Core Mechanics
- **Attributes:** 1-100 scale (Strength, Quickness, Constitution, etc.)
- **Skills:** 1-100 percentile, can exceed limits
- **Open-End Rolling:** Explosive d100 rolls

#### Combat System
- **Attack Roll:** d100 + skill + attribute - opponent's dodge
- **Armor Types:** Leather/Plate modify hit results
- **Critical Hits:** Extensive critical tables by weapon type
- **Initiative:** Action type determines sequence (spells → missiles → melee)
- **Parrying:** Split skill between offense and defense

#### Character Classes
- **Warrior:** Fighter archetype
- **Scout:** Thief/rogue archetype
- **Ranger:** Fighter/thief hybrid
- **Mage:** Spellcaster
- **Animist:** Cleric archetype
- **Bard:** Performance-based magic

#### Key Mechanics for DoraMCU
1. **Percentile Skill System:** Granular skill resolution
2. **Detailed Combat:** Weapon-specific critical hits
3. **Armor Interaction:** Armor type affects hit location
4. **Class System:** Traditional fantasy classes
5. **Travel Rules:** Weather, encounters, healing

---

### 2.4 LOTR Roleplaying Game (CODA System)

**Publisher:** Decipher (2002)
**System:** CODA (2d6 skill-based)

#### Core Mechanics
- **2d6 Resolution:** Roll 2d6 + skill vs target number
- **Skill-Based:** No levels, skills improve through use
- **Orders:** Class equivalents with unique abilities

#### Orders (Classes)
- **Scout:** Hobbit or Elf, stealth and agility
- **Warrior:** Human, Dwarf, or Elf, combat focus
- **Ranger:** Human or Half-elf, tracking and survival
- **Bard:** Human or Elf, performance magic
- **Mage:** Spellcaster with limited power
- **Loremaster:** Knowledge and healing
- **Rogue:** Stealth and subterfuge

#### Key Mechanics for DoraMCU
1. **Level-less Progression:** Skill-based advancement
2. **Order System:** Simple class structure
3. **2d6 Resolution:** Simple dice mechanic
4. **Limited Magic:** Reflects Tolkien's subtle magic

---

### 2.5 Video Game Combat Systems

#### Middle-earth: Shadow of Mordor/War

**Developer:** Monolith Productions
**Genre:** Action-adventure, third-person

**Combat Mechanics:**
- **Three Weapons:** Sword (melee), bow (ranged), knife (stealth)
- **Combo System:** Light/heavy attacks, counters, dodges
- **Wraith Abilities:** Supernatural powers (brand, possess, shroud)
- **Skill Trees:** Combat, wraith, stealth progression

**Nemesis System:**
- **Orc Hierarchy:** Soldiers → Captains → Warchiefs → Overlords
- **Procedural Generation:** Unique orc captains with traits
- **Relationships:** Orcs remember encounters with player
- **Branding:** Convert orcs to player's side
- **Tribes:** 8 orc tribes with unique combat styles

**Orc Roles:**
- Warrior (melee), Archer (ranged), Hunter (traps), Savage (brute), Defender (tank), Olog (heavy)

#### The Lord of the Rings: War in the North

**Developer:** Snowblind Studios
**Genre:** Action RPG, third-person

**Combat Mechanics:**
- **Three Playable Heroes:** Ranger (stealth/ranged), Elf-mage (magic), Dwarf (melee tank)
- **Light/Heavy Attacks:** Basic combo system
- **Critical Strikes:** Timed heavy attacks
- **Blocking/Rolling:** Defensive maneuvers
- **Skill Trees:** Melee, ranged, defensive stances
- **Great Eagle Summon:** Beleram provides aerial support

**Character Stats:**
- Strength, Will, Stamina, Dexterity

#### The Two Towers / Return of the King Games

**Developer:** EA / Legend Entertainment
**Genre:** Third-person action

**Combat Mechanics:**
- **Character-Specific Combos:** Aragorn (melee), Legolas (ranged), Gimli (balanced)
- **Quick/Fierce Attacks:** Speed vs power tradeoff
- **Killing Moves:** Instant kill on downed enemies
- **Parry/Knockback:** Defensive options
- **Shield Breaking:** Fierce attacks break shields

---

## Part 3: LOTR Galaxy Module Design Recommendations

### 3.1 RPG System Design

Based on the research, the LOTR galaxy module should incorporate:

#### Core Mechanics (from The One Ring RPG)
1. **Hope/Shadow Dual Track:**
   - Hope points represent courage and light
   - Shadow points accumulate from dark actions
   - When Shadow ≥ Hope: corruption event
   - Maps to DoraMCU's corruption track in combat-rules.json

2. **Eye of Sauron Mechanic:**
   - Random narrative intervention die
   - Party attention system
   - Hunts when attention is too high

3. **Callings System (Classes):**
   - Warrior (Men, Dwarves)
   - Scout (Hobbits, Elves)
   - Ranger (Dúnedain)
   - Mage (Istari, Elf-mages)
   - Bard (Elves, Men)
   - Loremaster (Scholars)

#### Combat Resolution (hybrid approach)
- **Skill Checks:** d100 percentile (MERP-inspired) for granularity
- **Attack Resolution:** d20 + bonus vs AC (D&D 5e-inspired for simplicity)
- **Damage:** Formula-based (e.g., "1d8+2") with critical tables
- **Morale:** Fear checks for cowardice/courage

#### Faction System (from War of the Ring)
- **Asymmetric Factions:** Free Peoples vs Shadow
- **Political Tracks:** Nation war declarations
- **Action Dice:** Resource management
- **Corruption Track:** Ring-bearer mechanic

### 3.2 FPS Integration Design

Based on Earth2026 and D&D patterns, LOTR FPS should include:

#### Weapons
| Weapon | Type | Damage | Fire Rate | Range | Notes |
|--------|------|--------|-----------|-------|-------|
| Elven Longbow | RIFLE | 15 | 1.2 | 120m | High accuracy |
| Gondor War Bow | RIFLE | 12 | 1.5 | 80m | Standard issue |
| Rohirrim Longsword | RIFLE | 10 | 2.0 | 5m | Melee as rapid-fire |
| Dwarf Warhammer | RIFLE | 14 | 1.5 | 5m | Heavy melee |
| Uruk-hai Axe | RIFLE | 12 | 1.8 | 5m | Fast melee |
| Hobbit Dagger | PISTOL | 5 | 3.0 | 3m | Quick sidearm |
| Wizard Staff | ROCKET_LAUNCHER | 30 | 0.5 | 60m | Spell projectile |
| Ent Mace | MACHINE_GUN | 20 | 0.8 | 10m | Slow but devastating |
| Crossbow | RIFLE | 10 | 0.8 | 60m | Slow reload, accurate |
| Fireball Spell | ROCKET_LAUNCHER | 40 | 0.3 | 50m | Area damage |

#### Enemies
| Enemy | Health | Speed | Behavior | Damage | XP |
|-------|--------|-------|----------|--------|----|
| Orc Grunt | 30 | 3.5 | CHASE | 8 | 50 |
| Uruk-hai | 60 | 3.0 | ATTACK | 15 | 120 |
| Easterling | 40 | 3.2 | PATROL | 10 | 70 |
| Haradrim | 45 | 2.8 | FLANK | 12 | 80 |
| Nazgûl | 200 | 5.0 | AMBUSH | 30 | 500 |
| Troll | 150 | 1.5 | ATTACK | 25 | 300 |
| Balrog | 500 | 4.0 | CHASE | 50 | 1000 |
| Warg | 50 | 6.0 | AMBUSH | 12 | 100 |
| Giant Spider | 80 | 4.5 | AMBUSH | 18 | 200 |
| Ent | 300 | 1.0 | PATROL | 35 | 400 |

#### Maps
| Map | Theme | Dimensions | Description |
|-----|-------|------------|-------------|
| Helm's Deep | Fortress Defense | 120x100x40 | Cliff-side fortress under siege |
| Minas Tirith | City Siege | 150x120x60 | White city with multiple walls |
| Moria | Dungeon Crawl | 100x80x30 | Underground dwarf kingdom |
| Fangorn Forest | Forest Ambush | 200x200x50 | Ancient forest with Ents |
| Mordor Plains | Open Battlefield | 250x200x40 | Volcanic wasteland |
| Shire | Peaceful Homeland | 80x80x20 | Rolling green hills |
| Rivendell | Elven Sanctuary | 100x80x30 | Hidden valley fortress |
| Isengard | Industrial Complex | 120x100x50 | Tree-lined fortress with industry |
| Dagorlad | River Battlefield | 180x100x30 | Plains before Mordor's gates |
| Cirith Ungol | Mountain Pass | 60x40x80 | Spider-infested mountain pass |

### 3.3 Special LOTR Mechanics

#### Ring of Power System
- **One Ring:** Corruption track, invisibility, power bonus
- **Three Elven Rings:** Vilya (air), Narya (fire), Narya (water)
- **Ring Corruption:** Gradual stat changes, madness events

#### Palantír Network
- **Seeing Stones:** Long-range communication/scouting
- **Corruption Risk:** Sauron can dominate users
- **Strategic Advantage:** Information warfare

#### Wizard Abilities
- **Gandalf:** Light beams, healing, leadership aura
- **Saruman:** Corruption, army enhancement
- **Radagast:** Nature control, animal summoning

#### Race-Specific Traits
- **Elves:** Enhanced accuracy, magic resistance, longevity
- **Men:** Leadership, courage, versatility
- **Dwarves:** Toughness, mining expertise, resilience
- **Hobbits:** Stealth, ring resistance, morale bonus
- **Ents:** Slow but devastating, nature control
- **Orcs:** Numbers, ferocity, darkness adaptation

### 3.4 Scenario Design (from War of the Ring)

#### Military Scenarios
1. **Battle of Helm's Deep:** Defense scenario, wave-based attacks
2. **Siege of Minas Tirith:** Multi-wall defense, Nazgûl assault
3. **Battle of the Pelennor Fields:** Large-scale cavalry engagement
4. **Battle of Dagorlad:** Final confrontation before Mordor
5. **Siege of Erebor:** Dwarf defense against goblins

#### Narrative Scenarios
1. **Journey to Mount Doom:** Stealth and survival, corruption mechanic
2. **Fellowship of the Ring:** Party management, Moria encounter
3. **Scouring of the Shire:** Post-war cleanup, Saruman's influence
4. **Last Alliance:** Elven-human alliance, Sauron's downfall

---

## Part 4: Implementation Recommendations

### 4.1 Service Architecture

Following the D&D 5e pattern:

```
LordOfTheRingsGameModule (GameModule)
├── LordOfTheRingsDiceService → DiceService
├── LordOfTheRingsCombatService → CombatService
├── LordOfTheRingsEncounterService → EncounterService
├── LordOfTheRingsRulesService → RulesService
├── LordOfTheRingsCharacterService → Character management
├── LordOfTheRingsRingService → Ring mechanics
├── LordOfTheRingsPalantirService → Seeing stone network
├── LordOfTheRingsWizardService → Wizard abilities
├── GroundCombatAdapter → Combat adapter
└── TechLevelAdapter → Tech level mapping
```

### 4.2 FPS Service

Following the D&D/Earth2026 pattern:

```java
@Service
public class LordOfTheRingsFpsService extends FpsBaseService {
    @Override
    public String getTheme() { return "LordOfTheRings"; }
    
    @Override
    public List<FpsWeapon> getWeapons() {
        // Elven bows, swords, wizard staffs, etc.
    }
    
    @Override
    public List<FpsEnemy> getEnemies() {
        // Orcs, Uruk-hai, Nazgûl, Trolls, etc.
    }
    
    @Override
    public List<FpsMap> getMaps() {
        // Helm's Deep, Minas Tirith, Moria, etc.
    }
}
```

### 4.3 Key JSON Data Files

The existing `lord-of-the-rings-rules/` directory already contains:
- **lore.json:** Realms, species, constellations, Valar planets, artifacts
- **factions.json:** 8 factions with military branches
- **units.json:** Ground units, starships, vehicles, characters
- **scenarios.json:** 13 scenarios
- **special-abilities.json:** Ring powers, wizard abilities, racial traits
- **combat-rules.json:** Combat resolution rules
- **combat-tables.json:** Dice resolution tables

### 4.4 Integration Points

1. **Corruption Track:** Implement from War of the Ring board game
2. **Hope/Shadow System:** Implement from The One Ring RPG
3. **Nemesis System:** Implement orc hierarchy from Shadow of Mordor
4. **Ring Mechanics:** One Ring corruption, Elven Ring powers
5. **Palantír Network:** Strategic communication system
6. **Wizard Abilities:** Unique spell-like powers for Maiar

---

## Conclusion

The LOTR galaxy module can draw from multiple established LOTR game systems:
- **War of the Ring** for asymmetric faction gameplay and corruption mechanics
- **The One Ring RPG** for Hope/Shadow dual track and narrative dice
- **MERP** for detailed percentile combat
- **Shadow of Mordor** for Nemesis system and orc hierarchy
- **War in the North** for hero-based combat with skill trees

The existing DoraMCU architecture (Earth2026 FPS + D&D 5e RPG) provides a solid foundation for implementing both FPS and RPG components for the LOTR galaxy module.
