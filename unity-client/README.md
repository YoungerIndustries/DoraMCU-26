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
    Core/        - Game manager, renderers, interest management
    Input/       - Input handling
    Network/     - Server communication
    Procedural/  - Planet, ship, and station generation
    UI/          - UI scripts (to be implemented)
  Scenes/        - Game scenes (to be created)
  Models/        - 3D models (to be added)
ProjectSettings/ - Unity project configuration
```

## Connecting to Backend

The client connects to the DoraMCU backend server at `http://localhost:8080` by default. Make sure the backend server is running before starting the game client.

## Integration with Electron Client

Once built, place `DoraMCU.Client.exe` in this directory so the Electron client can auto-launch it from the game launcher screen.
