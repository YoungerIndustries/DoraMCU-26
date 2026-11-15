# DoraMCU Unity Client

3D space strategy game client built with Unity. Connects to the DoraMCU backend server for multiplayer gameplay.

## Prerequisites

- **Unity Editor 2022.3 LTS** (recommended) or later
- **Windows Build Support (IL2CPP)** module
- **.NET Framework 4.7.1** or later

## Installation

1. Install **Unity Hub** from [unity.com](https://unity.com/download)
2. In Unity Hub, go to **Installs** → **Install Editor**
3. Select **Unity 2022.3.x LTS**
4. Check **Windows Build Support (IL2CPP)** module
5. Install to your preferred location

## Opening the Project

1. Open **Unity Hub**
2. Click **Add** → **Add Project from Disk**
3. Navigate to this folder (`unity-client/`) and select it
4. Click **Select Folder**
5. Open the project from the Hub

## Building for Windows

1. Open the project in Unity Editor
2. Go to **File** → **Build Settings**
3. Select **PC, Mac & Linux Standalone**
4. Set **Target Platform** to **Windows**
5. Click **Add Open Scenes** (you'll need to create at least one scene first)
6. Click **Build**
7. Choose an output folder and name the executable `DoraMCU.Client`

## First-Time Setup

On first open, Unity will:
- Import all assets and generate `.meta` files
- Compile C# scripts
- Generate project solution files

This may take several minutes.

## Project Structure

```
Assets/
  Scripts/
    AI/          - Client-side AI and neural network inference
    Backup/      - Local database and CDC sync
    Battlefield/ - 3D ground battlefield viewer (new)
    Core/        - Game manager, renderers, interest management
    Input/       - Input handling
    Network/     - Server communication
    Procedural/  - Planet, ship, and station generation
    UI/          - UI scripts (to be implemented)
  Scenes/        - Game scenes (to be created)
  Models/        - 3D models (to be added)
ProjectSettings/ - Unity project configuration
```

## 3D Battlefield Viewer

The Unity client includes a full interactive 3D battlefield viewer for ground combat scenarios powered by the Third Reich (3R4) module.

### Features

- **Terrain Rendering**: Procedural heightmap terrain from elevation grid data with terrain-type-based coloring
- **Building Generation**: Procedural buildings (boxes, cylinders, bridges, docks) at 3D positions
- **Unit Markers**: Type-specific unit shapes (infantry, tanks, artillery, APCs) with team colors and health bars
- **Orbit Camera**: Mouse drag to orbit, scroll to zoom, right-click drag to pan
- **Unit Selection**: Click units to see stats (strength, morale, attack, defense, mobility)
- **Weather Effects**: Fog, lighting changes, and particle effects based on biosphere data
- **Real-Time Updates**: Automatic polling for battlefield state changes

### Controls

| Action | Control |
|--------|---------|
| Orbit camera | Left mouse button + drag |
| Zoom | Mouse scroll wheel |
| Pan camera | Right mouse button + drag |
| Select unit | Left click on unit |
| Deselect unit | Click empty space or Escape |
| Enter battlefield mode | Press **B** key |
| Exit battlefield mode | Press **Escape** (twice) |

### How It Works

1. The `BattlefieldService` connects to the backend API at `/api/v1/thirdreich/ground/battlefields`
2. It fetches the JSON scene graph containing terrain, buildings, units, and biosphere data
3. The `BattlefieldRenderer` orchestrates procedural generation of all 3D elements
4. Real-time polling keeps the battlefield updated with unit health and position changes

### API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/v1/thirdreich/ground/battlefields` | POST | Create battlefield from planet location |
| `/api/v1/thirdreich/ground/battlefields/{id}` | GET | Get battlefield status |
| `/api/v1/thirdreich/ground/battlefields/{id}/scene-graph` | GET | Get full JSON scene graph |
| `/api/v1/thirdreich/ground/battlefields/{id}/model?format=obj` | GET | Download OBJ 3D model |
| `/api/v1/thirdreich/ground/battlefields/{id}/model?format=gltf` | GET | Download GLTF 3D model |

### Scene Graph JSON Format

The scene graph contains:
- **metadata**: Battlefield ID, name, location, terrain type, tech level, time period
- **camera**: Initial camera position, target, and FOV
- **lighting**: Ambient/directional intensity, sun angle, fog density
- **terrain**: Grid dimensions, cell size, elevation array
- **buildings**: Array of buildings with positions, scales, types, and defense ratings
- **attackerUnits** / **defenderUnits**: Arrays of units with positions, stats, and types
- **biosphere**: Climate zone, weather, temperature, humidity, wind, combat modifiers

### Configuration

Server URL is read from `NetworkManager.ServerUrl` (default: `http://localhost:8080`).

Polling interval defaults to 5 seconds, configurable in `BattlefieldService`.

Camera settings (orbit speed, zoom range, damping) are configurable in `BattlefieldCamera` inspector fields.

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Battlefield doesn't load | Ensure backend server is running on port 8080 |
| Terrain is flat gray | Check that elevation data is present in the scene graph |
| Units not visible | Verify unit positions are within camera view range |
| Fog too dense | Adjust fog density in `WeatherEffects` based on weather type |
| Camera stuck | Press Escape to unlock cursor, then try orbiting again |
| UI not showing | Ensure Unity UI module is included in the build |

## Connecting to Backend

The client connects to the DoraMCU backend server at `http://localhost:8080` by default. Make sure the backend server is running before starting the game client.

## Integration with Electron Client

Once built, place `DoraMCU.Client.exe` in this directory so the Electron client can auto-launch it from the game launcher screen.
