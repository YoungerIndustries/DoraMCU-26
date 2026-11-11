# Starship Troopers Rules Database

## Overview
This database contains the complete rules for the **Starship Troopers** board wargame by **Avalon Hill** (1976), designed by **Randall C. Reed**. The game is based on Robert A. Heinlein's 1959 novel of the same name.

## Game Description
A hexagon-based wargame for two players where:
- **Terran Player**: Commands the Mobile Infantry (M.I.) dropped from orbit to battle alien forces
- **Arachnid Player**: Controls the Bug hive with hidden underground tunnel systems
- **Humanoid (Skinny) Player**: A third faction that starts on the Bug side but switches to the human side in later scenarios

## Database Structure
| File | Description |
|------|-------------|
| `combat-rules.json` | Combat resolution, turn sequence, movement, and phase rules |
| `terran-units.json` | All Terran Mobile Infantry units with stats and abilities |
| `arachnid-units.json` | All Arachnid (Bug) units with stats and abilities |
| `humanoid-units.json` | All Humanoid (Skinny) units with stats and abilities |
| `scenarios.json` | All seven scenarios with setup and victory conditions |
| `special-equipment.json` | Special Weapons & Equipment (SW&E) catalog |
| `combat-tables.json` | Attack resolution tables for Terran and Alien attacks |

## Key Mechanics
- **Combat Resolution**: Attack vs Defense ratios rounded down, then die roll on appropriate table
- **Outcomes**: Miss, Disrupted, Wounded (WIA), Killed (KIA), Heavy Damage (HVY)
- **Hidden Movement**: Arachnid player uses Alien Control Pad to track underground positions
- **Tunnel Systems**: Bugs move through hidden tunnels and surface through breaches
- **Road Movement**: Units on roads move double their normal movement allowance

## Scenarios
1. **Feint against the humanoids** - Introductory scenario; single squad vs Skinny colony
2. **Operation Bughouse** - Complete platoon vs Arachnid hive
3. **Invasion of Skinny-5** - Platoon attack on Skinny industrial center
4. **Revolt!** - Skinnies change sides and attack Arachnids with M.I. help
5. **Sheol** (Part 1: Operation Corkscrew) - Combat engineers enter the battle
6. **Sheol** (Part 2: Retreat and Evacuation) - Rescue wounded M.I.
7. **Operation Royalty** - Reinforced platoon vs two Arachnid complexes to capture Brain Bug
8. **Klendathu** - Two platoons vs two Arachnid complexes to free human POWs

## Reception
- **White Dwarf (1977)**: 9/10 — "probably the best SF game currently on the market"
- **Space Gamer**: "faithfully recreates the heroic battles of the Terran Mobile Infantry"
- **Charles S. Roberts Award**: Nominated for Best Tactical Game of 1976
