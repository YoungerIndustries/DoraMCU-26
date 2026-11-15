using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Main orchestrator for the 3D battlefield viewer.
    /// <para>
    /// Combines terrain, buildings, units, camera, UI, and weather effects
    /// into a cohesive 3D scene. Handles loading, updating, and clearing
    /// the battlefield.
    /// </para>
    /// </summary>
    public class BattlefieldRenderer : Singleton<BattlefieldRenderer>
    {
        [Header("References")]
        [SerializeField] private Transform battlefieldParent;
        [SerializeField] private Transform buildingsParent;
        [SerializeField] private Transform unitsParent;

        private GameObject terrainObject;
        private List<GameObject> buildingObjects = new();
        private Dictionary<string, GameObject> unitObjects = new();

        private BattlefieldCamera cameraController;
        private BattlefieldUI ui;
        private WeatherEffects weatherEffects;
        private UnitSelector unitSelector;

        private BattlefieldSceneGraph currentSceneGraph;

        public bool IsLoaded => currentSceneGraph != null;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            // Setup parent transforms
            if (battlefieldParent == null)
            {
                var go = new GameObject("Battlefield");
                battlefieldParent = go.transform;
            }
            if (buildingsParent == null)
            {
                var go = new GameObject("Buildings");
                buildingsParent = go.transform;
                buildingsParent.SetParent(battlefieldParent);
            }
            if (unitsParent == null)
            {
                var go = new GameObject("Units");
                unitsParent = go.transform;
                unitsParent.SetParent(battlefieldParent);
            }
        }

        void Start()
        {
            InitializeComponents();
            SubscribeToEvents();
        }

        /// <summary>
        /// Initializes sub-components.
        /// </summary>
        private void InitializeComponents()
        {
            // Get or create camera controller on the main camera
            var mainCam = Camera.main;
            if (mainCam != null)
            {
                cameraController = mainCam.GetComponent<BattlefieldCamera>();
                if (cameraController == null)
                {
                    cameraController = mainCam.gameObject.AddComponent<BattlefieldCamera>();
                }
            }
            else
            {
                // Fallback: create a camera if none exists
                var cameraGo = new GameObject("MainCamera");
                cameraGo.tag = "MainCamera";
                cameraGo.AddComponent<Camera>();
                cameraController = cameraGo.AddComponent<BattlefieldCamera>();
                cameraGo.transform.position = new Vector3(0f, 200f, -300f);
                cameraGo.transform.LookAt(Vector3.zero);
            }

            // UI
            var uiGo = new GameObject("BattlefieldUI");
            ui = uiGo.AddComponent<BattlefieldUI>();

            // Weather effects
            var weatherGo = new GameObject("WeatherEffects");
            weatherEffects = weatherGo.AddComponent<WeatherEffects>();

            // Unit selector
            var selectorGo = new GameObject("UnitSelector");
            unitSelector = selectorGo.AddComponent<UnitSelector>();

            // Wire up unit selection events
            unitSelector.OnUnitSelected += OnUnitSelected;
            unitSelector.OnUnitDeselected += OnUnitDeselected;
        }

        /// <summary>
        /// Subscribes to BattlefieldService events.
        /// </summary>
        private void SubscribeToEvents()
        {
            var service = BattlefieldService.Instance;
            if (service != null)
            {
                service.OnSceneGraphReceived += OnSceneGraphReceived;
            }
        }

        /// <summary>
        /// Loads a battlefield from the scene graph.
        /// </summary>
        public void LoadBattlefield(BattlefieldSceneGraph sceneGraph)
        {
            if (sceneGraph == null)
            {
                Debug.LogWarning("[BattlefieldRenderer] Null scene graph");
                return;
            }

            ClearBattlefield();
            currentSceneGraph = sceneGraph;

            Debug.Log($"[BattlefieldRenderer] Loading battlefield: {sceneGraph.metadata?.name ?? "Unknown"}");

            // Load terrain
            LoadTerrain(sceneGraph.terrain, sceneGraph.metadata?.terrainType ?? "PLAINS");

            // Load buildings
            LoadBuildings(sceneGraph.buildings);

            // Load units
            LoadUnits(sceneGraph.attackerUnits, "attacker");
            LoadUnits(sceneGraph.defenderUnits, "defender");

            // Apply weather effects
            if (weatherEffects != null)
            {
                weatherEffects.ApplyBiosphere(sceneGraph.biosphere, sceneGraph.lighting);
            }

            // Set camera target
            if (cameraController != null && sceneGraph.camera != null)
            {
                cameraController.SetTarget(sceneGraph.camera.TargetVector, 300f);
            }

            // Update UI
            if (ui != null)
            {
                ui.ShowBattlefieldInfo(sceneGraph.metadata);
                ui.ShowWeatherInfo(sceneGraph.biosphere);
            }

            Debug.Log($"[BattlefieldRenderer] Loaded: {buildingObjects.Count} buildings, {unitObjects.Count} units");
        }

        /// <summary>
        /// Updates the battlefield with new scene graph data (for real-time updates).
        /// </summary>
        public void UpdateBattlefield(BattlefieldSceneGraph sceneGraph)
        {
            if (sceneGraph == null) return;

            // Update attacker units
            UpdateUnits(sceneGraph.attackerUnits, "attacker");
            UpdateUnits(sceneGraph.defenderUnits, "defender");

            currentSceneGraph = sceneGraph;
        }

        /// <summary>
        /// Loads terrain from scene graph data.
        /// </summary>
        private void LoadTerrain(SceneTerrain terrain, string terrainType)
        {
            if (terrain != null && terrain.elevations != null && terrain.elevations.Count > 0)
            {
                terrainObject = TerrainMeshGenerator.GenerateTerrain(
                    terrain.width,
                    terrain.height,
                    terrain.cellSize,
                    terrain.elevations,
                    terrainType);

                if (terrainObject != null)
                {
                    terrainObject.transform.SetParent(battlefieldParent);
                    terrainObject.transform.localPosition = Vector3.zero;
                }
            }

            // Fallback: flat ground
            if (terrainObject == null)
            {
                terrainObject = TerrainMeshGenerator.CreateFlatGround(400f);
                terrainObject.transform.SetParent(battlefieldParent);
                terrainObject.transform.localPosition = Vector3.zero;
            }
        }

        /// <summary>
        /// Loads buildings from scene graph data.
        /// </summary>
        private void LoadBuildings(List<SceneBuilding> buildings)
        {
            if (buildings == null) return;

            foreach (var building in buildings)
            {
                var go = BuildingGenerator.GenerateBuilding(building, buildingsParent);
                if (go != null)
                {
                    // Add collider for raycasting
                    var collider = go.AddComponent<BoxCollider>();
                    collider.size = building.Scale;
                    buildingObjects.Add(go);
                }
            }
        }

        /// <summary>
        /// Loads units from scene graph data.
        /// </summary>
        private void LoadUnits(List<SceneUnit> units, string team)
        {
            if (units == null) return;

            foreach (var unit in units)
            {
                var go = UnitGenerator.GenerateUnit(unit, unitsParent);
                if (go != null)
                {
                    // Add collider for raycasting
                    var collider = go.AddComponent<CapsuleCollider>();
                    collider.center = Vector3.up * 0.75f;
                    collider.height = 1.5f;
                    collider.radius = 0.8f;

                    unitObjects[unit.id] = go;
                }
            }
        }

        /// <summary>
        /// Updates existing units with new data.
        /// </summary>
        private void UpdateUnits(List<SceneUnit> units, string team)
        {
            if (units == null) return;

            foreach (var unit in units)
            {
                if (unitObjects.ContainsKey(unit.id))
                {
                    var go = unitObjects[unit.id];

                    // Update position
                    go.transform.position = unit.Position;

                    // Update health bar
                    if (unit.stats != null)
                    {
                        UnitGenerator.UpdateHealthBar(go, unit.stats.HealthPercent);
                    }

                    // Check if unit is destroyed
                    if (unit.stats != null && unit.stats.strength <= 0)
                    {
                        CreateExplosion(go.transform.position);
                        UnityEngine.Object.Destroy(go);
                        unitObjects.Remove(unit.id);
                    }
                }
                else
                {
                    // New unit - add it
                    var go = UnitGenerator.GenerateUnit(unit, unitsParent);
                    if (go != null)
                    {
                        var collider = go.AddComponent<CapsuleCollider>();
                        collider.center = Vector3.up * 0.75f;
                        collider.height = 1.5f;
                        collider.radius = 0.8f;
                        unitObjects[unit.id] = go;
                    }
                }
            }
        }

        /// <summary>
        /// Creates an explosion effect at a position.
        /// </summary>
        private void CreateExplosion(Vector3 position)
        {
            var go = new GameObject("Explosion");
            go.transform.SetParent(battlefieldParent);
            go.transform.position = position;

            var filter = go.AddComponent<MeshFilter>();
            var plane = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            filter.sharedMesh = plane.GetComponent<MeshFilter>().sharedMesh;
            UnityEngine.Object.Destroy(plane);
            var renderer = go.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = new Color(1f, 0.5f, 0.1f);
            renderer.material = mat;
            go.transform.localScale = Vector3.one * 2f;

            // Animate and destroy
            StartCoroutine(AnimateExplosion(go));
        }

        private System.Collections.IEnumerator AnimateExplosion(GameObject go)
        {
            float elapsed = 0;
            var renderer = go.GetComponent<MeshRenderer>();

            while (elapsed < 1f)
            {
                elapsed += Time.deltaTime;
                float progress = elapsed;
                go.transform.localScale = Vector3.one * 2f * (1 + progress * 3);
                if (renderer && renderer.material)
                {
                    renderer.material.color = new Color(1f, 0.5f, 0.1f) * (1 - progress);
                }
                yield return null;
            }

            UnityEngine.Object.Destroy(go);
        }

        /// <summary>
        /// Handles unit selection event.
        /// </summary>
        private void OnUnitSelected(UnitInfo unitInfo)
        {
            if (ui != null)
            {
                ui.ShowUnitInfo(unitInfo);
            }
        }

        /// <summary>
        /// Handles unit deselection event.
        /// </summary>
        private void OnUnitDeselected()
        {
            if (ui != null)
            {
                ui.HideUnitInfo();
            }
        }

        /// <summary>
        /// Handles scene graph received from service.
        /// </summary>
        private void OnSceneGraphReceived(BattlefieldSceneGraph sceneGraph)
        {
            if (currentSceneGraph == null)
            {
                LoadBattlefield(sceneGraph);
            }
            else
            {
                UpdateBattlefield(sceneGraph);
            }
        }

        /// <summary>
        /// Clears the entire battlefield scene.
        /// </summary>
        public void ClearBattlefield()
        {
            // Destroy terrain
            if (terrainObject != null)
            {
                UnityEngine.Object.Destroy(terrainObject);
                terrainObject = null;
            }

            // Destroy buildings
            foreach (var building in buildingObjects)
            {
                if (building != null) UnityEngine.Object.Destroy(building);
            }
            buildingObjects.Clear();

            // Destroy units
            foreach (var unit in unitObjects.Values)
            {
                if (unit != null) UnityEngine.Object.Destroy(unit);
            }
            unitObjects.Clear();

            // Clear weather
            if (weatherEffects != null)
            {
                weatherEffects.ClearWeather();
            }

            // Clear UI
            if (ui != null)
            {
                ui.HideAll();
            }

            // Clear selection
            if (unitSelector != null)
            {
                unitSelector.ClearSelection();
            }

            currentSceneGraph = null;
        }

        /// <summary>
        /// Loads a battlefield by ID from the server.
        /// </summary>
        public void LoadBattlefieldById(string battlefieldId)
        {
            var service = BattlefieldService.Instance;
            if (service != null)
            {
                service.GetSceneGraph(battlefieldId, (sceneGraph, success) =>
                {
                    if (success && sceneGraph != null)
                    {
                        LoadBattlefield(sceneGraph);
                        service.StartPolling(battlefieldId);
                    }
                });
            }
        }

        /// <summary>
        /// Creates and loads a new battlefield.
        /// </summary>
        public void CreateAndLoadBattlefield(
            long locationId,
            string timePeriod,
            string terrain,
            string weather,
            int attackerCount,
            int defenderCount)
        {
            var service = BattlefieldService.Instance;
            if (service != null)
            {
                service.CreateBattlefield(locationId, timePeriod, terrain, weather,
                    attackerCount, defenderCount, (battlefieldId, success) =>
                    {
                        if (success && !string.IsNullOrEmpty(battlefieldId))
                        {
                            LoadBattlefieldById(battlefieldId);
                        }
                    });
            }
        }

        void OnDestroy()
        {
            var service = BattlefieldService.Instance;
            if (service != null)
            {
                service.OnSceneGraphReceived -= OnSceneGraphReceived;
                service.StopPolling();
            }

            ClearBattlefield();
        }
    }
}
