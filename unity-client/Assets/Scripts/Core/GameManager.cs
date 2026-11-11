using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Network;
using DoraMCU.Client.Procedural;
using DoraMCU.Client.Input;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Main game manager that coordinates all systems.
    /// Handles state synchronization between server and client.
    /// </summary>
    public class GameManager : Singleton<GameManager>
    {
        [Header("Scene References")]
        [SerializeField] private Transform solarSystemParent;
        [SerializeField] private Transform fleetParent;
        [SerializeField] private Transform stationParent;

        [Header("Sync Settings")]
        [SerializeField] private float stateSyncInterval = 5f;
        [SerializeField] private float interestCheckInterval = 2f;

        private PlanetGenerator planetGenerator;
        private ShipGenerator shipGenerator;
        private StationGenerator stationGenerator;
        private InterestManager interestManager;

        private Dictionary<long, GameObject> planetObjects = new();
        private Dictionary<long, GameObject> shipObjects = new();
        private Dictionary<long, GameObject> stationObjects = new();
        private Dictionary<int, GameObject> sectorObjects = new();

        private NetworkManager.GameState currentState;
        private Coroutine syncCoroutine;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            planetGenerator = new PlanetGenerator();
            shipGenerator = new ShipGenerator();
            stationGenerator = new StationGenerator();
            interestManager = new InterestManager();

            // Setup parents
            if (solarSystemParent == null)
            {
                var go = new GameObject("SolarSystems");
                solarSystemParent = go.transform;
            }
            if (fleetParent == null)
            {
                go = new GameObject("Fleets");
                fleetParent = go.transform;
            }
            if (stationParent == null)
            {
                go = new GameObject("Stations");
                stationParent = go.transform;
            }
        }

        void Start()
        {
            // Subscribe to network events
            NetworkManager.Instance.OnConnected += OnConnected;
            NetworkManager.Instance.OnStateSync += OnStateSync;
            NetworkManager.Instance.OnError += OnError;

            // Subscribe to input events
            InputHandler.Instance.OnNavigateToSystem += OnNavigateToSystem;
            InputHandler.Instance.OnScan += OnScan;

            // Start periodic sync
            syncCoroutine = StartCoroutine(SyncLoop());
        }

        void OnDestroy()
        {
            if (syncCoroutine != null)
            {
                StopCoroutine(syncCoroutine);
            }
        }

        /// <summary>
        /// Handle connection to server.
        /// </summary>
        private void OnConnected(string sessionId)
        {
            Debug.Log($"[GameManager] Connected: {sessionId}");
            FetchInitialState();
        }

        /// <summary>
        /// Handle state synchronization from server.
        /// </summary>
        private void OnStateSync(string sessionId, NetworkManager.GameState state)
        {
            currentState = state;
            UpdateSceneFromState(state);
        }

        /// <summary>
        /// Handle connection error.
        /// </summary>
        private void OnError(string error)
        {
            Debug.LogError($"[GameManager] Error: {error}");
        }

        /// <summary>
        /// Fetch initial game state from server.
        /// </summary>
        private void FetchInitialState()
        {
            NetworkManager.Instance.StartCoroutine(FetchStateCoroutine());
        }

        private IEnumerator FetchStateCoroutine()
        {
            yield return NetworkManager.Instance.FetchGameState((state) =>
            {
                currentState = state;
                UpdateSceneFromState(state);
            });
        }

        /// <summary>
        /// Update the 3D scene based on game state.
        /// </summary>
        private void UpdateSceneFromState(NetworkManager.GameState state)
        {
            if (state == null) return;

            // Update galaxy sectors
            if (state.Galaxy != null && state.Galaxy.sectors != null)
            {
                UpdateSectors(state.Galaxy.sectors);
            }

            // Update player's planets
            if (state.Player != null && state.Player.planets != null)
            {
                UpdatePlanets(state.Player.planets);
            }

            // Update player's ships
            if (state.Player != null && state.Player.ships != null)
            {
                UpdateShips(state.Player.ships);
            }

            // Update player's fleets
            if (state.Player != null && state.Player.fleets != null)
            {
                UpdateFleets(state.Player.fleets);
            }
        }

        private void UpdateSectors(NetworkManager.SectorInfo[] sectors)
        {
            foreach (var sector in sectors)
            {
                int sectorKey = sector.row * 10 + sector.col;

                if (!sectorObjects.ContainsKey(sectorKey))
                {
                    var go = new GameObject($"Sector_{sector.row}_{sector.col}")
                    {
                        transform = { parent = solarSystemParent }
                    };
                    go.transform.localPosition = new Vector3(sector.col * 100f, 0, sector.row * 100f);
                    sectorObjects[sectorKey] = go;
                }

                // Update sub-sectors
                if (sector.subSectors != null)
                {
                    foreach (var subSector in sector.subSectors)
                    {
                        if (subSector.solarSystem != null)
                        {
                            UpdateSolarSystem(subSector.solarSystem, sectorObjects[sectorKey].transform);
                        }
                    }
                }
            }
        }

        private void UpdateSolarSystem(NetworkManager.SolarSystemInfo system, Transform parent)
        {
            string systemKey = $"System_{system.id}";
            GameObject systemGo;

            if (!parent.Find(systemKey))
            {
                systemGo = new GameObject(systemKey)
                {
                    transform = { parent = parent }
                };

                // Generate star
                if (system.star != null)
                {
                    GenerateStar(systemGo.transform, system.star);
                }

                // Generate planets
                if (system.planets != null)
                {
                    foreach (var planet in system.planets)
                    {
                        GeneratePlanetFromInfo(systemGo.transform, planet);
                    }
                }

                // Generate stations
                if (system.stations != null)
                {
                    foreach (var station in system.stations)
                    {
                        GenerateStationFromInfo(systemGo.transform, station);
                    }
                }
            }
        }

        private void GenerateStar(Transform parent, NetworkManager.StarInfo star)
        {
            var starGo = new GameObject("Star")
            {
                transform = { parent = parent, position = Vector3.zero }
            };

            float radius = star.radius * 2f;
            var mesh = UnityEngine.Mesh.CreateSphere(radius, 16, 16);
            var mf = starGo.AddComponent<MeshFilter>();
            mf.mesh = mesh;

            var mr = starGo.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"))
            {
                color = GetStarColor(star.spectralClass)
            };
            mr.material = mat;
            mat.EnableKeyword("_EMISSION");
            mat.SetColor("_EmissionColor", mat.color);

            // Add light
            var light = starGo.AddComponent<Light>();
            light.type = LightType.Point;
            light.intensity = 2f;
            light.range = radius * 10f;
        }

        private Color GetStarColor(string spectralClass)
        {
            return spectralClass[0] switch
            {
                'O' => new Color(0.5f, 0.7f, 1f), // Blue
                'B' => new Color(0.6f, 0.8f, 1f), // Blue-white
                'A' => new Color(0.8f, 0.85f, 1f), // White
                'F' => new Color(0.9f, 0.95f, 1f), // Yellow-white
                'G' => new Color(1f, 1f, 0.8f), // Yellow (Sun)
                'K' => new Color(1f, 0.8f, 0.5f), // Orange
                'M' => new Color(1f, 0.4f, 0.2f), // Red
                _ => new Color(1f, 1f, 0.9f)
            };
        }

        private void GeneratePlanetFromInfo(Transform parent, NetworkManager.PlanetInfo planet)
        {
            var data = new PlanetGenerator.PlanetData
            {
                Name = planet.name,
                Type = planet.type != null ? planet.type[0] : 'U',
                Size = planet.size,
                Atmosphere = planet.atmosphere,
                Temperature = planet.temperature,
                Hydrographics = planet.hydrographics,
                OrbitRadius = planet.orbitRadius,
                OrbitAngle = planet.orbitAngle,
                RotationPeriod = planet.rotationPeriod,
                AxialTilt = planet.axialTilt
            };

            var go = planetGenerator.GeneratePlanet(data, parent);
            planetObjects[planet.id] = go;
        }

        private void GenerateStationFromInfo(Transform parent, NetworkManager.StationInfo station)
        {
            var data = new StationGenerator.StationData
            {
                Name = station.name,
                Type = station.type,
                Size = 5,
                Fuel = station.fuel,
                Repair = station.repair
            };

            var go = stationGenerator.GenerateStation(data, parent);
            stationObjects[station.id] = go;
        }

        private void UpdatePlanets(NetworkManager.PlanetInfo[] planets)
        {
            foreach (var planet in planets)
            {
                if (!planetObjects.ContainsKey(planet.id))
                {
                    // Planet already generated in solar system
                }
                else
                {
                    // Update existing planet
                }
            }
        }

        private void UpdateShips(NetworkManager.ShipInfo[] ships)
        {
            foreach (var ship in ships)
            {
                if (!shipObjects.ContainsKey(ship.id))
                {
                    var data = new ShipGenerator.ShipData
                    {
                        Name = ship.name,
                        Design = ship.design,
                        Hull = ship.hull,
                        Drives = ship.drives,
                        Cargo = ship.cargo,
                        Fuel = ship.fuel,
                        Weapons = ship.combat?.weapons ?? 0,
                        Armor = ship.combat?.armor ?? 0,
                        Shields = ship.combat?.shields ?? 0
                    };

                    var go = shipGenerator.GenerateShip(data, fleetParent);
                    shipObjects[ship.id] = go;
                }
            }
        }

        private void UpdateFleets(NetworkManager.FleetInfo[] fleets)
        {
            foreach (var fleet in fleets)
            {
                // Update fleet positions
                if (fleet.ships != null)
                {
                    foreach (var ship in fleet.ships)
                    {
                        if (shipObjects.ContainsKey(ship.id))
                        {
                            var go = shipObjects[ship.id];
                            go.transform.position = new Vector3(
                                fleet.sectorCol * 10f + ship.sectorCol,
                                0,
                                fleet.sectorRow * 10f + ship.sectorRow
                            );
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Periodic state sync loop.
        /// </summary>
        private IEnumerator SyncLoop()
        {
            while (true)
            {
                yield return new WaitForSeconds(stateSyncInterval);
                FetchInitialState();
            }
        }

        /// <summary>
        /// Handle navigation to a solar system.
        /// </summary>
        private void OnNavigateToSystem(int sectorRow, int sectorCol, int subSectorRow, int subSectorCol)
        {
            Debug.Log($"[GameManager] Navigating to {sectorRow},{sectorCol},{subSectorRow},{subSectorCol}");
            // Camera transition to target system
        }

        /// <summary>
        /// Handle scan action.
        /// </summary>
        private void OnScan()
        {
            Debug.Log("[GameManager] Scanning...");
            // Trigger scan of nearby objects
        }

        public NetworkManager.GameState CurrentState => currentState;
    }
}
