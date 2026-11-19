# DoraMCU - Godot Client

3D space strategy game built with Godot 4.7.2 and C#.

## Quick Start

1. **Install Godot 4.7.2 .NET edition** from [godotengine.org/download/windows](https://godotengine.org/download/windows)
2. **Install .NET SDK 8.0** (already installed at `%LOCALAPPDATA%\Microsoft\dotnet`)
3. Open Godot and import the `godot-client` folder
4. Click **Play** (▶) to run the game
5. Press **D** to toggle demo mode (generates procedural galaxy)

## Project Structure

```
godot-client/
├── project.godot          # Project configuration
├── DoraMCU.csproj         # C# project file
├── Scenes/
│   └── MainScene.tscn     # Main 3D scene
├── Scripts/
│   ├── Core/              # GameManager, Singleton, DemoModeManager
│   ├── AI/                # ClientAiService, NeuronClientService
│   ├── Battlefield/       # Terrain, buildings, units, weather
│   ├── FPS/               # Player controller, weapons, HUD
│   ├── Network/           # NetworkManager, ElectronBridge
│   ├── UI/                # HUD, maps, menus, dialogs
│   ├── Procedural/        # Planet, ship, station generators
│   ├── Input/             # InputHandler
│   └── Backup/            # LocalDatabase, CdcConsumer
└── MIGRATION_GUIDE.md     # Unity → Godot migration guide
```

## Features

- **Procedural Galaxy Generation** - 25 sectors with stars, planets, and ships
- **Demo Mode** - Works without backend server (press D to toggle)
- **3D Rendering** - Stars with colored point lights, planets with biome colors
- **Backend Integration** - Connects to DoraMCU server at localhost:8080

## Requirements

- Godot 4.7.2 .NET edition (C# support)
- .NET SDK 8.0
- Vulkan 1.0 compatible GPU

## Migration from Unity

See [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for detailed API mappings and porting strategy.

## Backend Server

The game connects to a Java backend server. To run locally:

```powershell
cd C:\Users\kenba\IdeaProjects\DoraMCU
.\gradlew.bat :dora-ai-server:bootRun
```

## License

MIT
