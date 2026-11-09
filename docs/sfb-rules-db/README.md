# Star Fleet Battles Rules Database

This database contains the core rules for **Star Fleet Battles (SFB)**, the classic game of ship-vs-ship combat in the Star Fleet Universe, published by Amarillo Design Bureau (ADB).

## Overview

Star Fleet Battles is based on four core principles:
1. **Ship System Display (SSD)** — A graphical schematic presenting all systems on your starship
2. **Energy Allocation** — Every turn, count your energy and spend it wisely. There is never enough.
3. **Proportional Movement** — All starships are in continuous and simultaneous maneuver
4. **Damage Allocation** — Penetrate enemy shields, and damage is distributed throughout the ship

## Database Structure

| File | Description |
|------|-------------|
| `core-mechanics.json` | Turn structure, impulses, sequence of play |
| `energy-allocation.json` | Energy system, power sources, allocation categories |
| `movement.json` | Proportional movement, speed, turn modes, special maneuvers |
| `shields.json` | Shield directions, reinforcement, repair, damage |
| `weapons.json` | All weapon types, ranges, damage, firing arcs |
| `damage-allocation.json` | Damage resolution, hull structure, SSD systems |
| `empires.json` | All empires/factions and their characteristics |
| `ship-systems.json` | SSD components, ship types, class designations |
| `special-systems.json` | Cloaking, tractors, transporters, EW, labs |
| `terrain.json` | Planets, asteroids, nebulae, ion storms, webs |
| `ship-classes.json` | Notable ship classes by empire |
| `campaign-rules.json` | Strategic campaign rules overview |

## Rule Reference System

SFB rules use a hierarchical numbering system:
- Lettered major chapters (e.g., D = Combat)
- Number for major rule (e.g., D6 = Fire Control Systems)
- Decimal and sub-numbers for subsections (e.g., D6.683)

## Sources

- Star Fleet Battles Master Rulebook (ADB)
- Cadet Training Manual (starfleetgames.com)
- Star Fleet Battles: A New Beginning Basic Rules
- Wikipedia — Star Fleet Battles
- Star Fleet Battles community resources
