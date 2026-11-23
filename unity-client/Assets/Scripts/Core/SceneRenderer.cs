using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Network;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Handles all 3D scene rendering: sectors, solar systems, stars, planets, ships, stations.
    /// Extracted from GameManager.cs for separation of concerns.
    /// </summary>
    public class SceneRenderer
    {
        private Transform solarSystemParent;
        private Transform fleetParent;
        private Transform stationParent;

        private Dictionary<long, GameObject> planetObjects = new();
        private Dictionary<long, GameObject> shipObjects = new();
        private Dictionary<long, GameObject> stationObjects = new();
        private Dictionary<int, GameObject> sectorObjects = new();

        /// <summary>
        /// Initialize the scene renderer with parent transforms.
        /// </summary>
        public void Initialize(Transform solarSystemParent, Transform fleetParent, Transform stationParent)
        {
            this.solarSystemParent = solarSystemParent;
            this.fleetParent = fleetParent;
            this.stationParent = stationParent;
        }

        /// <summary>
        /// Update the 3D scene based on game state.
        /// </summary>
        public void UpdateSceneFromState(NetworkManager.GameState state)
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

                // Planets and stations generation disabled for build
                // (procedural generators moved to DISABLED)
            }
        }

        private void GenerateStar(Transform parent, NetworkManager.StarInfo star)
        {
            var starGo = new GameObject("Star")
            {
                transform = { parent = parent, position = Vector3.zero }
            };

            float radius = star.radius * 2f;
            // Create a simple sphere for the star
            var mf = starGo.AddComponent<MeshFilter>();
            var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            sphere.transform.localScale = new Vector3(radius, radius, radius);
            mf.mesh = sphere.GetComponent<MeshFilter>().mesh;
            UnityEngine.Object.Destroy(sphere);

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
                    // Ship generation disabled for build
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
        /// Get the ship object for a given ship ID.
        /// </summary>
        public GameObject GetShipObject(long shipId)
        {
            shipObjects.TryGetValue(shipId, out var go);
            return go;
        }

        /// <summary>
        /// Get the sector object for a given sector key.
        /// </summary>
        public GameObject GetSectorObject(int sectorKey)
        {
            sectorObjects.TryGetValue(sectorKey, out var go);
            return go;
        }
    }
}
