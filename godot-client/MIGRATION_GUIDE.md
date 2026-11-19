# DoraMCU Unity → Godot Migration Guide

## Overview

This guide helps port the DoraMCU 3D space strategy game from Unity to Godot 4.7.2.

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
└── icon/                  # Project icons
```

## Key API Differences

### 1. Base Classes

| Unity | Godot | Notes |
|-------|-------|-------|
| `MonoBehaviour` | `Node` / `Node3D` / `Control` | Choose based on use case |
| `GameObject` | `Node3D` / `Node` | 3D objects use Node3D |
| `Component` | `Node` | All Godot objects are nodes |
| `Transform` | `Node3D` | Position/rotation/scale are properties |
| `Camera` | `Camera3D` | Similar API |
| `Light` | `DirectionalLight3D` / `PointLight3D` | Separate types |

### 2. Lifecycle Methods

| Unity | Godot | Notes |
|-------|-------|-------|
| `Start()` | `_Ready()` | Called when node enters scene tree |
| `Update()` | `_Process(double delta)` | Called every frame |
| `FixedUpdate()` | `_PhysicsProcess(double delta)` | Physics timestep |
| `Awake()` | `_EnterTree()` | Called before _Ready |
| `OnDestroy()` | `_ExitTree()` | Called when node is removed |
| `OnEnable()` | `_EnterTree()` | Node enters scene |
| `OnDisable()` | `_ExitTree()` | Node leaves scene |

### 3. Input Handling

| Unity | Godot | Notes |
|-------|-------|-------|
| `Input.GetAxis("Horizontal")` | `Input.GetAxis("horizontal")` | Define in Project Settings → Input Map |
| `Input.GetKey(KeyCode.W)` | `Input.IsKeyPressed(Key.W)` | Direct key check |
| `Input.GetButtonDown("Fire1")` | `Input.IsActionJustPressed("fire")` | Define actions in Input Map |
| `Input.mousePosition` | `Input.GetMousePosition()` | Returns Vector2I |
| `Input.mouseScrollDelta` | `Input.GetMouseMotion()` | Use InputEvent with relative |

**Setting up Input Map in Godot:**
1. Project → Project Settings → Input Map
2. Add action names: `horizontal`, `vertical`, `fire`, `jump`, etc.
3. Assign keys to each action

### 4. Scene Management

| Unity | Godot | Notes |
|-------|-------|-------|
| `SceneManager.LoadScene()` | `GetTree().ChangeSceneToFile()` | Change scene |
| `GameObject.Instantiate()` | `PackedScene.Instantiate()` | Instantiate from scene file |
| `Object.Destroy()` | `QueueFree()` | Mark for deletion |
| `DontDestroyOnLoad()` | Autoload in project.godot | Register as singleton |

### 5. Materials & Rendering

| Unity | Godot | Notes |
|-------|-------|-------|
| `Material` | `StandardMaterial3D` | PBR material |
| `material.color` | `material.AlbedoColor` | Base color |
| `material.emissiveColor` | `material.Emission` | Emission color |
| `material.SetColor()` | Set property directly | No shader property names |
| `Shader` | `ShaderMaterial` | Custom shaders |
| `Mesh.CreateSphere()` | `new SphereMesh()` | Create mesh directly |
| `CreatePrimitive()` | `new BoxMesh()` / `new SphereMesh()` | Create mesh types |

### 6. Coroutines & Async

| Unity | Godot | Notes |
|-------|-------|-------|
| `StartCoroutine()` | `await ToSignal()` | Use signals for events |
| `yield return new WaitForSeconds()` | `await ToSignal(GetTree().CreateTimer(), "timeout")` | Timer signal |
| `IEnumerator` | `async/await` | C# async/await |

### 7. JSON Serialization

| Unity | Godot | Notes |
|-------|-------|-------|
| `JsonUtility.ToJson()` | `JsonConvert.SerializeObject()` | Newtonsoft.Json |
| `JsonUtility.FromJson()` | `JsonConvert.DeserializeObject()` | Newtonsoft.Json |
| `System.Text.Json` | `Newtonsoft.Json` | Add NuGet package |

### 8. Networking

| Unity | Godot | Notes |
|-------|-------|-------|
| `UnityWebRequest` | `HTTPClient` / `HttpClient` | Use .NET HttpClient |
| `WebSocket` | `WebSocketPeer` | Godot's WebSocket |
| `NetworkTransport` | `ENetMultiplayerPeer` | Built-in multiplayer |

### 9. UI System

| Unity | Godot | Notes |
|-------|-------|-------|
| `Canvas` | `Control` | Base UI node |
| `Text` | `Label` | Text display |
| `Button` | `Button` | Clickable button |
| `Image` | `TextureRect` | Image display |
| `Slider` | `HSlider` / `VSlider` | Slider control |
| `RectTransform` | `Control` layout | Anchors and margins |
| `anchoredPosition` | `Offset` properties | Positioning |

### 10. Physics

| Unity | Godot | Notes |
|-------|-------|-------|
| `Rigidbody` | `RigidBody3D` | Physics body |
| `Collider` | `CollisionShape3D` | Collision shape |
| `SphereCollider` | `SphereShape3D` | Sphere shape |
| `BoxCollider` | `BoxShape3D` | Box shape |
| `Physics.Raycast()` | `PhysicsDirectSpaceState3D.intersect_ray()` | Raycast |
| `OnCollisionEnter()` | `_on_body_entered()` | Signal-based |

### 11. Resources

| Unity | Godot | Notes |
|-------|-------|-------|
| `Resources.Load()` | `ResourceLoader.Load()` | Load resources |
| `prefab` | `PackedScene` | Scene instances |
| `ScriptableObject` | `Resource` | Custom resources |

## Porting Strategy

### Phase 1: Core Systems (Week 1)
1. ✅ GameManager - Ported
2. ✅ DemoModeManager - Ported
3. NetworkManager - Port HTTP/WebSocket calls
4. InputHandler - Map input actions

### Phase 2: 3D Rendering (Week 2)
1. GalaxyRenderer - Port sector/star rendering
2. PlanetSurfaceRenderer - Port planet generation
3. ShipGenerator - Port ship models
4. CombatRenderer - Port combat visualization

### Phase 3: FPS Module (Week 3)
1. FpsPlayerController - Port movement/controls
2. FpsWeaponController - Port weapons
3. FpsHUD - Port UI elements
4. FpsNetworkManager - Port networking

### Phase 4: UI & Polish (Week 4)
1. MainHUD - Port main HUD
2. MapUI - Port galaxy map
3. DialogSystem - Port dialogs
4. MenuManager - Port menus

## Common Pitfalls

### 1. Node vs Component
In Unity, you add components to GameObjects. In Godot, everything is a node in a tree. Think in terms of node hierarchies, not component compositions.

### 2. Signals vs Events
Godot uses signals instead of C# events. Instead of:
```csharp
// Unity
public event Action<string> OnMessage;
OnMessage?.Invoke("hello");
```
Use:
```csharp
// Godot
[Signal]
public signal MessageReceived(string message);
EmitSignal("message_received", "hello");
```

### 3. Memory Management
Godot uses reference counting. Call `QueueFree()` instead of `Destroy()`. Objects are freed at the end of the frame.

### 4. Coordinate System
Godot uses Y-up coordinate system (same as Unity). However, the default camera looks along -Z axis.

### 5. Export Properties
Use `[Export]` attribute instead of `[SerializeField]`:
```csharp
// Unity
[SerializeField] private int health = 100;

// Godot
[Export] public int Health = 100;
```

## Running the Project

1. Download Godot 4.7.2 .NET edition from https://godotengine.org/download/windows
2. Open Godot
3. Click "Import" on the `godot-client` folder
4. Click "Play" (▶) to run the game
5. Press **D** to toggle demo mode

## Next Steps

1. Port NetworkManager for HTTP/WebSocket communication
2. Port InputHandler with Godot's Input Map
3. Port GalaxyRenderer for 3D galaxy visualization
4. Port FPS module for first-person gameplay
5. Port UI modules for HUD and menus

## Resources

- [Godot Documentation](https://docs.godotengine.org/)
- [Godot C# Documentation](https://docs.godotengine.org/en/stable/tutorials/scripting/c_sharp/c_sharp.html)
- [Godot Migration Guide from Unity](https://docs.godotengine.org/en/stable/tutorials/scripting/c_sharp/c_sharp_differences.html)
- [Godot Asset Library](https://godotengine.org/asset-library/asset)
