# DoraMCU - Godot Client

3D space strategy game built with Godot 4.7.2 and C#, connecting to the DoraMCU Spring Boot server.

## Quick Start

1. **Install Godot 4.7.2 .NET edition** from [godotengine.org/download/windows](https://godotengine.org/download/windows)
2. **Install .NET SDK 8.0**
3. Open Godot and import the `godot-client` folder
4. Click **Play** (▶) to run the game
5. Login or register, or use **Dev Login** / **Demo Mode** for offline play

## Game Flow

1. **Login** — Enter username/password, register a new account, or use dev login
2. **Character Creation** — Multi-step: name/age → species (10 alien races) → career (8 careers) → review
3. **Galaxy Selection** — Choose a galaxy from the server or enter demo galaxy
4. **Gameplay** — Build ships, scan sectors, manage resources, explore the galaxy

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| V | Toggle Ship Builder |
| X | Toggle Sector Scanner |
| Tab | Toggle Galaxy Map |
| I | Toggle Inventory |
| F1 | Toggle Help |
| Escape | Pause / Resume |

## Project Structure

```
godot-client/
├── project.godot              # Project config, autoloads, input map
├── DoraMCU.csproj             # .NET 8.0, Newtonsoft.Json
├── Scenes/
│   └── MainScene.tscn         # Main scene with 3D + UI layers
├── Scripts/
│   ├── Core/
│   │   ├── GameManager.cs           # Central game state coordinator
│   │   ├── ServerConfig.cs          # Server URL config singleton
│   │   ├── DemoModeManager.cs       # Procedural galaxy generation
│   │   ├── DoraMCUInitializer.cs    # Scene initialization and wiring
│   │   └── Singleton.cs             # Base singleton class
│   ├── Network/
│   │   └── NetworkManager.cs        # HTTP REST + JWT auth, all DTOs
│   ├── UI/
│   │   ├── LoginScreen.cs           # Login/register/dev login UI
│   │   ├── CharacterCreation.cs     # Multi-step character creation
│   │   ├── MenuManager.cs           # Main menu, pause menu, settings
│   │   ├── MainHUD.cs               # Resource display, status bar
│   │   ├── ShipBuildUI.cs           # Ship design and build panel
│   │   ├── ExploreAction.cs         # Sector scanning panel
│   │   ├── HelpOverlay.cs           # F1 controls reference
│   │   ├── InventoryUI.cs           # Personal inventory display
│   │   └── MapUI.cs                 # Galaxy map with sector grid
│   └── Procedural/
│       ├── PlanetGenerator.cs       # Sphere planets with biome colors
│       ├── ShipGenerator.cs         # Ship models (5 types)
│       └── StationGenerator.cs      # Station models (3 types)
├── export_presets.cfg         # Windows Desktop export
├── MIGRATION_GUIDE.md         # Unity → Godot migration guide
└── README.md                  # This file
```

## Features

- **Authentication** — Login, register, dev login with JWT tokens
- **Character Creation** — 10 alien species, 8 careers, 2d6 characteristic rolls
- **Galaxy Browsing** — Select from server galaxies or demo mode
- **Ship Building** — 5 ship types, 6 modules, resource costs
- **Sector Scanning** — Scan 10x10 grid, reveal solar systems
- **Resource Management** — Credits, minerals, energy display
- **Galaxy Map** — Visual sector grid with scan status
- **Inventory** — Items and equipment tracking
- **Demo Mode** — Full offline procedural galaxy (25 sectors)
- **3D Rendering** — Stars, planets with biome colors, ships, stations
- **Backend Integration** — Connects to DoraMCU server at 192.168.1.111:8080

## Architecture

- **Autoload Singletons**: ServerConfig, NetworkManager, GameManager (persist across scenes)
- **Async/await**: All HTTP calls use .NET HttpClient with async/await
- **Godot Signals**: Component communication via signals instead of Unity events
- **Newtonsoft.Json**: JSON serialization matching server DTOs
- **Control-based UI**: All UI built programmatically with Godot Control nodes

## Requirements

- Godot 4.7.2 .NET edition (C# support)
- .NET SDK 8.0
- Vulkan 1.0 compatible GPU

## Backend Server

The game connects to a Spring Boot server. Default: `192.168.1.111:8080`

Change server URL in-game via Settings menu, or edit `user://server_config.cfg`.

## License

MIT
