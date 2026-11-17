using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.EventSystems;
using DoraMCU.Client.Procedural;
using DoraMCU.Rendering;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Generates a fully playable demo scene without requiring a backend server.
    /// Creates a galaxy view with sectors, stars, planets, and ships using procedural generation.
    /// Press D key to toggle demo mode on/off at runtime.
    /// </summary>
    public class DemoModeManager : Singleton<DemoModeManager>
    {
        [Header("Demo Settings")]
        [SerializeField] private bool enableDemoMode = true;
        [SerializeField] private int sectorCount = 5;
        [SerializeField] private int starsPerSector = 3;

        private PlanetGenerator planetGenerator;
        private ShipGenerator shipGenerator;
        private GalaxyRenderer galaxyRenderer;
        private Transform demoRoot;
        private bool isDemoActive = false;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
            planetGenerator = new PlanetGenerator();
            shipGenerator = new ShipGenerator();
        }

        void Start()
        {
            if (enableDemoMode)
            {
                StartCoroutine(DelayedStartDemo());
            }
        }

        // Note: D key toggle disabled - Unity 6000.6 forces Input System
        // which breaks InputSafe.GetKeyDown at runtime.

        IEnumerator DelayedStartDemo()
        {
            yield return new WaitForSeconds(1f);
            StartDemo();
        }

        public void StartDemo()
        {
            if (isDemoActive)
            {
                Debug.Log("[DemoModeManager] Demo is already active");
                return;
            }

            Debug.Log("[DemoModeManager] Starting standalone demo mode...");

            // Disable EventSystem to prevent Input System errors
            var eventSystems = Object.FindObjectsByType<EventSystem>(UnityEngine.FindObjectsInactive.Include);
            foreach (var es in eventSystems)
            {
                es.enabled = false;
                Debug.Log("[DemoModeManager] Disabled EventSystem to prevent Input errors");
            }

            // Create camera since MainScene is empty
            var cameraGo = new GameObject("MainCamera");
            cameraGo.tag = "MainCamera";
            var cam = cameraGo.AddComponent<Camera>();
            cam.clearFlags = CameraClearFlags.Skybox;
            cam.backgroundColor = new Color(0.02f, 0.02f, 0.05f);
            cameraGo.transform.position = new Vector3(0f, 150f, -200f);
            cameraGo.transform.LookAt(Vector3.zero);

            // Add directional light
            var lightGo = new GameObject("DirectionalLight");
            var dirLight = lightGo.AddComponent<Light>();
            dirLight.type = LightType.Directional;
            dirLight.color = Color.white;
            dirLight.intensity = 1f;
            dirLight.transform.rotation = Quaternion.Euler(60f, 0f, 0f);

            demoRoot = new GameObject("DemoContent").transform;

            // Create GalaxyRenderer for sector/star visualization
            var galaxyGo = new GameObject("GalaxyRenderer");
            galaxyRenderer = galaxyGo.AddComponent<GalaxyRenderer>();
            galaxyRenderer.transform.SetParent(demoRoot);

            var galaxyData = GenerateDemoGalaxyData();
            galaxyRenderer.RenderGalaxy(galaxyData);

            GenerateDemoPlanetsAndShips(galaxyData);
            CreateStarfield();
            CreateCentralSun();

            RenderSettings.skybox = CreateSpaceSkybox();
            RenderSettings.ambientMode = UnityEngine.Rendering.AmbientMode.Skybox;
            RenderSettings.ambientSkyColor = new Color(0.05f, 0.05f, 0.1f);
            RenderSettings.fog = true;
            RenderSettings.fogDensity = 0.001f;
            RenderSettings.fogColor = new Color(0.02f, 0.02f, 0.05f);

            isDemoActive = true;
            Debug.Log("[DemoModeManager] Demo mode active! Press D to toggle.");
        }

        public void StopDemo()
        {
            if (!isDemoActive) return;

            Debug.Log("[DemoModeManager] Stopping demo mode...");

            if (demoRoot != null)
            {
                Object.Destroy(demoRoot.gameObject);
                demoRoot = null;
            }

            RenderSettings.skybox = null;
            RenderSettings.fog = false;

            isDemoActive = false;
        }

        public void ToggleDemo()
        {
            if (isDemoActive)
                StopDemo();
            else
                StartDemo();
        }

        GalaxyData GenerateDemoGalaxyData()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            var random = new System.Random(42);
            int halfSize = sectorCount / 2;

            for (int x = -halfSize; x <= halfSize; x++)
            {
                for (int z = -halfSize; z <= halfSize; z++)
                {
                    data.sectors.Add(new SectorData
                    {
                        id = $"Sector_{x}_{z}",
                        name = $"Sector {x},{z}",
                        x = x,
                        z = z
                    });

                    for (int s = 0; s < starsPerSector; s++)
                    {
                        data.stars.Add(new StarData
                        {
                            id = $"Star_{x}_{z}_{s}",
                            sectorX = x,
                            sectorZ = z,
                            localX = ((float)random.NextDouble() - 0.5f) * 8f,
                            localZ = ((float)random.NextDouble() - 0.5f) * 8f
                        });
                    }

                    if (x < halfSize)
                    {
                        data.hyperlanes.Add(new HyperlaneData
                        {
                            from = new Vector3(x * 10f, 0f, z * 10f),
                            to = new Vector3((x + 1) * 10f, 0f, z * 10f)
                        });
                    }
                    if (z < halfSize)
                    {
                        data.hyperlanes.Add(new HyperlaneData
                        {
                            from = new Vector3(x * 10f, 0f, z * 10f),
                            to = new Vector3(x * 10f, 0f, (z + 1) * 10f)
                        });
                    }
                }
            }

            return data;
        }

        void GenerateDemoPlanetsAndShips(GalaxyData galaxyData)
        {
            var random = new System.Random(123);
            var planetNames = new[] { "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta" };
            var shipNames = new[] { "Explorer", "Vanguard", "Sentinel", "Pioneer", "Nebula", "Stardust" };

            foreach (var star in galaxyData.stars)
            {
                float starWorldX = star.sectorX * 10f + star.localX;
                float starWorldZ = star.sectorZ * 10f + star.localZ;
                var systemGo = new GameObject($"System_{star.id}");
                systemGo.transform.SetParent(demoRoot);
                systemGo.transform.position = new Vector3(starWorldX, 0f, starWorldZ);

                // Create the star itself
                var starSphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
                starSphere.transform.SetParent(systemGo.transform);
                starSphere.transform.localPosition = Vector3.zero;
                float starSize = 1f + (float)random.NextDouble() * 2f;
                starSphere.transform.localScale = new Vector3(starSize, starSize, starSize);

                var starRenderer = starSphere.GetComponent<MeshRenderer>();
                var starMat = new Material(Shader.Find("Standard"));
                starMat.color = GetStarColor(random);
                starMat.EnableKeyword("_EMISSION");
                starMat.SetColor("_EmissionColor", starMat.color * 2f);
                starRenderer.material = starMat;

                var starLight = starSphere.AddComponent<Light>();
                starLight.type = LightType.Point;
                starLight.color = starMat.color;
                starLight.intensity = 2f;
                starLight.range = 15f;

                // Generate planets using PlanetGenerator
                for (int p = 0; p < 4; p++)
                {
                    float orbitRadius = 3f + p * 2.5f;
                    float orbitAngle = (p * 2 * Mathf.PI) / 4f + (float)random.NextDouble() * 0.5f;

                    var pd = new PlanetGenerator.PlanetData
                    {
                        Name = $"{planetNames[p % planetNames.Length]}-{star.id}",
                        Type = 'H',
                        Size = 3 + random.Next(5),
                        Atmosphere = random.Next(9),
                        Temperature = random.Next(10),
                        Hydrographics = random.Next(10),
                        OrbitRadius = orbitRadius,
                        OrbitAngle = orbitAngle,
                        RotationPeriod = 1f + (float)random.NextDouble() * 5f,
                        AxialTilt = (float)random.NextDouble() * 30f
                    };

                    var planetGo = planetGenerator.GeneratePlanet(pd, systemGo.transform);
                    float px = Mathf.Cos(orbitAngle) * orbitRadius;
                    float pz = Mathf.Sin(orbitAngle) * orbitRadius;
                    planetGo.transform.localPosition = new Vector3(px, 0f, pz);
                }

                // Generate ships using ShipGenerator
                for (int sh = 0; sh < 2; sh++)
                {
                    var sd = new ShipGenerator.ShipData
                    {
                        Name = $"{shipNames[sh % shipNames.Length]}-{star.id}",
                        Design = "CRUISER",
                        Hull = 4 + random.Next(4),
                        Drives = 2 + random.Next(4),
                        Cargo = 1 + random.Next(4),
                        Fuel = 1 + random.Next(4),
                        Weapons = 1 + random.Next(3),
                        Armor = 1 + random.Next(3),
                        Shields = 1 + random.Next(3)
                    };

                    var shipGo = shipGenerator.GenerateShip(sd, systemGo.transform);
                    float shipAngle = (float)random.NextDouble() * 2 * Mathf.PI;
                    float shipRadius = 2f + (float)random.NextDouble() * 8f;
                    shipGo.transform.localPosition = new Vector3(
                        Mathf.Cos(shipAngle) * shipRadius,
                        ((float)random.NextDouble() - 0.5f) * 3f,
                        Mathf.Sin(shipAngle) * shipRadius
                    );
                    shipGo.transform.rotation = Quaternion.Euler(0, (float)random.NextDouble() * 360f, 0);
                }
            }
        }

        void CreateStarfield()
        {
            var starfieldGo = new GameObject("Starfield");
            starfieldGo.transform.SetParent(demoRoot);

            var random = new System.Random(999);
            int starCount = 200;

            for (int i = 0; i < starCount; i++)
            {
                var starGo = new GameObject("Star");
                starGo.transform.SetParent(starfieldGo.transform);

                float theta = (float)random.NextDouble() * 2 * Mathf.PI;
                float phi = (float)random.NextDouble() * Mathf.PI;
                float radius = 300f + (float)random.NextDouble() * 200f;

                starGo.transform.localPosition = new Vector3(
                    radius * Mathf.Sin(phi) * Mathf.Cos(theta),
                    radius * Mathf.Cos(phi),
                    radius * Mathf.Sin(phi) * Mathf.Sin(theta)
                );

                float starSize = 0.1f + (float)random.NextDouble() * 0.4f;
                starGo.transform.localScale = new Vector3(starSize, starSize, starSize);

                var primitive = GameObject.CreatePrimitive(PrimitiveType.Sphere);
                var mf = primitive.GetComponent<MeshFilter>();
                var mesh = mf.sharedMesh;
                Object.Destroy(primitive);

                var filter = starGo.AddComponent<MeshFilter>();
                filter.mesh = mesh;

                var renderer = starGo.AddComponent<MeshRenderer>();
                var mat = new Material(Shader.Find("Standard"));
                mat.color = new Color(
                    0.8f + (float)random.NextDouble() * 0.2f,
                    0.8f + (float)random.NextDouble() * 0.2f,
                    0.9f + (float)random.NextDouble() * 0.1f
                );
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", mat.color);
                renderer.material = mat;
            }

            Debug.Log("[DemoModeManager] Starfield created with " + starCount + " stars");
        }

        void CreateCentralSun()
        {
            var sunGo = new GameObject("CentralSun");
            sunGo.transform.SetParent(demoRoot);
            sunGo.transform.position = Vector3.zero;

            var sunMesh = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            sunMesh.transform.SetParent(sunGo.transform);
            sunMesh.transform.localScale = new Vector3(5f, 5f, 5f);

            var sunRenderer = sunMesh.GetComponent<MeshRenderer>();
            var sunMat = new Material(Shader.Find("Standard"));
            sunMat.color = new Color(1f, 0.8f, 0.2f);
            sunMat.EnableKeyword("_EMISSION");
            sunMat.SetColor("_EmissionColor", new Color(3f, 2f, 0.5f));
            sunRenderer.material = sunMat;

            var sunLight = sunGo.AddComponent<Light>();
            sunLight.type = LightType.Point;
            sunLight.color = new Color(1f, 0.9f, 0.5f);
            sunLight.intensity = 3f;
            sunLight.range = 100f;

            Debug.Log("[DemoModeManager] Central sun created");
        }

        Material CreateSpaceSkybox()
        {
            var skyboxMat = new Material(Shader.Find("Skybox/6 Sided"));
            skyboxMat.SetColor("_Tint", new Color(0.02f, 0.02f, 0.05f));
            return skyboxMat;
        }

        Color GetStarColor(System.Random random)
        {
            int type = random.Next(5);
            switch (type)
            {
                case 0: return Color.white;
                case 1: return new Color(1f, 0.9f, 0.7f);
                case 2: return new Color(0.7f, 0.8f, 1f);
                case 3: return new Color(1f, 0.5f, 0.3f);
                default: return new Color(0.8f, 0.85f, 1f);
            }
        }

        public bool IsDemoActive => isDemoActive;
    }
}
