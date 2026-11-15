# OpenTTD Transport Tycoon Rules Database

## Overview
This database contains comprehensive vehicle, station, and infrastructure data from **OpenTTD** (Open Transport Tycoon Deluxe) and its **NewGRF** mod ecosystem. It provides a unified reference for transport vehicles organized by tech level progression — from steam locomotives on primitive planets to interplanetary spacecraft for space-faring civilizations.

This mod integrates with the **DoraMCU Transport Tycoon Biodome** system, enabling planet-appropriate transport based on tech levels.

## Source Games & Mods
| Source | Author | License | Type |
|--------|--------|---------|------|
| OpenTTD Core | OpenTTD Team | GPL v2 | Base Game |
| NUTS (Unrealistic Train Set) | V453000 | GPL v3 | Train Set |
| FRIMS (Future Reality Maglev) | Community | CC-BY v3.0 | Maglev Set |
| SpaceY | Community | GPL v3 | Space Vehicles |
| Kaaskrokets Fictional Aircraft | Kaaskroket | GPL v2 | Aircraft Set |
| NewStations | Michael Blunck | GPL | Station Set |
| OpenGFX Mars | Community | GPL v2 | Landscape |
| Apollo Rocket Set | Community | GPL v3 | Industry Set |
| eGRVTS (Extended Generic Road Vehicle Set) | Zephyris | GPL v2 | Horse-Drawn Vehicles |
| Sailing Ships | J Mitchell | GPL v2 | Wind-Powered Ships |

## Database Structure
| File | Description |
|------|-------------|
| `vehicles.json` | All vehicle types from steam trains to spacecraft, organized by tech level |
| `medieval-vehicles.json` | Pre-industrial vehicles for fantasy worlds (D&D, medieval) — horses, carriages, sailing ships, creature mounts |
| `stations.json` | Station types including rail stations, airports, spaceports, and docks |
| `infrastructure.json` | Roads, rails, monorail, maglev tracks, and their properties |
| `cargo-types.json` | All cargo types: raw materials, processed goods, advanced goods, passengers |
| `industries.json` | Primary, secondary, and tertiary industries with cargo chains |
| `landscapes.json` | Planet landscapes including Mars and climate variations |
| `tech-levels.json` | Tech level progression mapping vehicles and stations to planet development |
| `scenarios.json` | Pre-built scenarios and campaign setups |

## Tech Level Progression
The core concept is matching transport technology to planet development levels:

| Tech Level | Era | Vehicles | Stations |
|------------|-----|----------|----------|
| 0 | Medieval / Fantasy | Horses, carriages, sailing ships, creature mounts | Taverns, market squares, wharfs, castle gates |
| 1-2 | Primitive | Steam trains, trucks, buses | Bus stops, basic rail stations |
| 3-4 | Industrial | Diesel/electric trains, ships | Rail stations, ports |
| 5-6 | Advanced | Maglev trains, aircraft | Maglev stations, airports |
| 7-8 | Space-faring | Advanced aircraft, rockets | Airports, rocket pads |
| 9-10 | Interplanetary | Spacecraft, space stations | Spaceports, orbital stations |

### Medieval / Fantasy Worlds (Tech Level 0)
For low-tech fantasy worlds like D&D campaigns, set the planet tech level to **0** to use only pre-industrial transport:

- **Animal-drawn vehicles**: Horse carts, stagecoaches, ox carts, pack animals (from eGRVTS mod)
- **Sailing ships**: Brigs, schooners, merchantmen, cogs, caravels (from Sailing Ships mod by J Mitchell)
- **Creature mounts**: Riding horses, griffons, hippogriffs, direwolves, pegasi, drakes, war elephants
- **Medieval infrastructure**: Dirt paths, cobblestone streets, horse-drawn trams
- **Fantasy cargo**: Herbs, gems, enchanted items, potions, weapons, scrolls
- **Medieval industries**: Blacksmiths, apothecaries, gem mines, adventurers' guilds

See `medieval-vehicles.json` for the complete vehicle and industry database for tech level 0 worlds.

## Key Features
- **8 base vehicle types** from OpenTTD core (truck, bus, train, ship, aircraft)
- **Extended vehicle sets** from NewGRF mods (NUTS, FRIMS, SpaceY)
- **Tech-level gating** — vehicles unlock as planets develop
- **Cargo chain integration** — industries produce and consume cargo types
- **Planet landscapes** — Mars and other environments with appropriate transport
- **Station catchment areas** — hex-based cargo collection zones

## Integration with DoraMCU
This database feeds the `SurfaceVehicle` and `Station` models in the Transport Tycoon Biodome system. Each vehicle type maps to a `VehicleType` enum, and stations map to `InfrastructureType` (STATION, PORT, AIRPORT).

## Reception of OpenTTD
- **OpenTTD**: Beloved open-source transport simulation with active modding community since 2000
- **NewGRF System**: Powerful modding framework allowing custom vehicles, stations, and industries
- **BaNaNaS**: Content browser with hundreds of community-created mods
- **NUTS**: Most comprehensive train set with full tech progression
- **FRIMS**: Futuristic maglev designs for advanced civilizations
