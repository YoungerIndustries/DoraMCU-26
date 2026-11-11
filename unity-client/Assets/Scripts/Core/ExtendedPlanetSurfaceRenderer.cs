using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Extended planet surface renderer that displays biomes, settlements, industries,
    /// stations, and vehicles on a 3D hex sphere.
    /// <para>
    /// Connects to the Java backend via REST API to fetch planet surface data
    /// and renders all elements on the planet surface.
    /// </para>
    /// </summary>
    public class ExtendedPlanetSurfaceRenderer : MonoBehaviour
    {
        [Header("Visual Settings")]
        [SerializeField] private float planetRadius = 5f;
        [SerializeField] private float atmosphereScale = 1.1f;
        [SerializeField] private int terrainResolution = 32;

        [Header("Biome Colors")]
        [SerializeField] private Color deepOceanColor = new Color(0.102f, 0.227f, 0.361f);
        [SerializeField] private Color shallowWaterColor = new Color(0.180f, 0.420f, 0.616f);
        [SerializeField] private Color beachColor = new Color(0.831f, 0.773f, 0.627f);
        [SerializeField] private Color grasslandColor = new Color(0.290f, 0.549f, 0.247f);
        [SerializeField] private Color forestColor = new Color(0.176f, 0.353f, 0.118f);
        [SerializeField] private Color desertColor = new Color(0.761f, 0.651f, 0.380f);
        [SerializeField] private Color tundraColor = new Color(0.541f, 0.608f, 0.659f);
        [SerializeField] private Color iceColor = new Color(0.847f, 0.910f, 0.941f);
        [SerializeField] private Color mountainColor = new Color(0.420f, 0.420f, 0.420f);
        [SerializeField] private Color highlandColor = new Color(0.478f, 0.478f, 0.353f);
        [SerializeField] private Color swampColor = new Color(0.290f, 0.353f, 0.227f);
        [SerializeField] private Color volcanicColor = new Color(0.353f, 0.165f, 0.102f);
        [SerializeField] private Color jungleColor = new Color(0.102f, 0.290f, 0.039f);
        [SerializeField] private Color savannaColor = new Color(0.541f, 0.478f, 0.227f);
        [SerializeField] private Color wastelandColor = new Color(0.353f, 0.353f, 0.290f);

        [Header("Entity Colors")]
        [SerializeField] private Color settlementColor = Color.yellow;
        [SerializeField] private Color industryColor = Color.orange;
        [SerializeField] private Color stationColor = Color.cyan;
        [SerializeField] private Color vehicleColor = Color.magenta;
        [SerializeField] private Color roadColor = new Color(0.5f, 0.5f, 0.5f, 0.6f);
        [SerializeField] private Color railColor = new Color(0.7f, 0.7f, 0.7f, 0.8f);

        [Header("Atmosphere")]
        [SerializeField] private Color atmosphereColor = new Color(0.3f, 0.6f, 1f, 0.3f);

        // Rendered objects
        private GameObject planetMesh;
        private GameObject atmosphere;
        private GameObject settlementsContainer;
        private GameObject industriesContainer;
        private GameObject stationsContainer;
        private GameObject vehiclesContainer;
        private GameObject infrastructureContainer;
        private Dictionary<string, GameObject> vehicleObjects;

        private void Awake()
        {
            vehicleObjects = new Dictionary<string, GameObject>();
        }

        /// <summary>
        /// Renders the full planet surface with all elements.
        /// </summary>
        public void RenderPlanetSurface(ExtendedPlanetSurfaceData data)
        {
            Clear();

            if (data == null) return;

            // Create planet sphere with biome coloring
            CreatePlanetSphere(data);

            // Create atmosphere
            CreateAtmosphere();

            // Create infrastructure (roads and rails)
            if (data.infrastructure != null && data.infrastructure.Count > 0)
            {
                CreateInfrastructure(data.infrastructure);
            }

            // Create settlements
            if (data.settlements != null && data.settlements.Count > 0)
            {
                CreateSettlements(data.settlements);
            }

            // Create industries
            if (data.industries != null && data.industries.Count > 0)
            {
                CreateIndustries(data.industries);
            }

            // Create stations
            if (data.stations != null && data.stations.Count > 0)
            {
                CreateStations(data.stations);
            }

            // Create vehicles
            if (data.vehicles != null && data.vehicles.Count > 0)
            {
                CreateVehicles(data.vehicles);
            }

            Debug.Log($"[ExtendedPlanetSurfaceRenderer] Rendered planet: {data.name} " +
                      $"({data.settlements?.Count ?? 0} settlements, {data.industries?.Count ?? 0} industries, " +
                      $"{data.stations?.Count ?? 0} stations, {data.vehicles?.Count ?? 0} vehicles)");
        }

        /// <summary>
        /// Creates the planet sphere with biome-based vertex coloring.
        /// </summary>
        private void CreatePlanetSphere(ExtendedPlanetSurfaceData data)
        {
            planetMesh = new GameObject($"Planet_{data.name}");
            planetMesh.transform.SetParent(transform);

            var filter = planetMesh.AddComponent<MeshFilter>();
            var renderer = planetMesh.AddComponent<MeshRenderer>();

            // Create sphere mesh with biome coloring
            filter.mesh = CreateBiomeMesh(data);

            // Material with vertex colors
            var mat = new Material(Shader.Find("Standard"));
            mat.EnableKeyword("_VERTEXCOLOR");
            mat.color = Color.white;
            renderer.material = mat;

            planetMesh.transform.localScale = Vector3.one * planetRadius;
        }

        /// <summary>
        /// Creates a terrain mesh with biome-based vertex colors.
        /// </summary>
        private Mesh CreateBiomeMesh(ExtendedPlanetSurfaceData data)
        {
            var mesh = new Mesh();
            mesh.name = $"BiomeTerrain_{data.name}";

            // Use Unity's built-in sphere as base
            var baseSphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            var baseMesh = baseSphere.GetComponent<MeshFilter>().sharedMesh();

            var positions = baseMesh.vertices;
            var colors = new Color[positions.Length];
            System.Random rand = new System.Random(data.seed.GetHashCode());

            // Determine dominant biome for coloring
            TerrainType dominantTerrain = TerrainType.GRASSLAND;
            if (data.dominantTerrain != null)
            {
                dominantTerrain = StringToTerrainType(data.dominantTerrain);
            }

            Color baseColor = GetTerrainColor(dominantTerrain);

            for (int i = 0; i < positions.Length; i++)
            {
                // Add slight terrain variation
                float noise = (rand.NextFloat() - 0.5f) * 0.05f;
                positions[i] += positions[i].normalized * noise;

                // Color based on latitude and noise for biome variation
                float latitude = positions[i].y; // Y axis is up/down on sphere
                
                // Create biome bands based on latitude
                if (Mathf.Abs(latitude) > 0.8f)
                {
                    // Polar regions - ice
                    colors[i] = iceColor;
                }
                else if (Mathf.Abs(latitude) > 0.6f)
                {
                    // Tundra regions
                    colors[i] = tundraColor;
                }
                else if (Mathf.Abs(latitude) < 0.3f && rand.NextFloat() > 0.5f)
                {
                    // Equatorial - mix of jungle and desert
                    colors[i] = rand.NextFloat() > 0.5f ? jungleColor : desertColor;
                }
                else
                {
                    // Temperate - grassland and forest
                    colors[i] = rand.NextFloat() > 0.5f ? grasslandColor : forestColor;
                }

                // Add water patches
                float waterNoise = Mathf.PerlinNoise(positions[i].x * 3f, positions[i].z * 3f);
                if (waterNoise < 0.3f)
                {
                    colors[i] = waterNoise < 0.15f ? deepOceanColor : shallowWaterColor;
                }
            }

            mesh.vertices = positions;
            mesh.colors = colors;
            mesh.triangles = baseMesh.triangles;
            mesh.normals = baseMesh.normals;
            mesh.uv = baseMesh.uv;

            Destroy(baseSphere);

            return mesh;
        }

        /// <summary>
        /// Creates the atmospheric glow effect.
        /// </summary>
        private void CreateAtmosphere()
        {
            atmosphere = new GameObject("Atmosphere");
            atmosphere.transform.SetParent(transform);

            var filter = atmosphere.AddComponent<MeshFilter>();
            var sphereGo = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            filter.mesh = sphereGo.GetComponent<MeshFilter>().sharedMesh;

            var renderer = atmosphere.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = atmosphereColor;
            renderer.material = mat;

            atmosphere.transform.localScale = Vector3.one * planetRadius * atmosphereScale;

            Destroy(sphereGo);
        }

        /// <summary>
        /// Creates infrastructure (roads and rails) on the planet surface.
        /// </summary>
        private void CreateInfrastructure(List<InfrastructureData> infrastructure)
        {
            infrastructureContainer = new GameObject("Infrastructure");
            infrastructureContainer.transform.SetParent(transform);

            foreach (var infra in infrastructure)
            {
                Vector3 pos = HexToVector3(infra.hexX, infra.hexY, infra.hexZ, planetRadius * 1.005f);
                Vector3 neighborPos = HexToVector3(infra.hexX + 1, infra.hexY, infra.hexZ - 1, planetRadius * 1.005f);

                var lineGo = new GameObject($"Infra_{infra.hexX}_{infra.hexY}_{infra.hexZ}");
                lineGo.transform.SetParent(infrastructureContainer.transform);

                var line = lineGo.AddComponent<LineRenderer>();
                Color lineColor = infra.type == "RAIL" ? railColor : roadColor;
                var mat = new Material(Shader.Find("Particles/Alpha Blended"));
                mat.color = lineColor;
                line.material = mat;
                line.startWidth = infra.type == "RAIL" ? 0.02f : 0.03f;
                line.endWidth = infra.type == "RAIL" ? 0.02f : 0.03f;
                line.positionCount = 2;
                line.SetPosition(0, pos);
                line.SetPosition(1, neighborPos);
            }
        }

        /// <summary>
        /// Creates settlement markers on the planet surface.
        /// </summary>
        private void CreateSettlements(List<SettlementData> settlements)
        {
            settlementsContainer = new GameObject("Settlements");
            settlementsContainer.transform.SetParent(transform);

            foreach (var settlement in settlements)
            {
                var settleGo = new GameObject($"Settlement_{settlement.name}");
                settleGo.transform.SetParent(settlementsContainer.transform);

                // Settlement marker (cylinder for buildings)
                var cylinderGo = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
                cylinderGo.transform.SetParent(settleGo.transform);
                cylinderGo.transform.localPosition = Vector3.zero;

                var mat = new Material(Shader.Find("Standard"));
                mat.color = settlementColor;
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", settlementColor * 0.3f);
                cylinderGo.GetComponent<MeshRenderer>().material = mat;

                // Position on planet surface
                Vector3 pos = HexToVector3(settlement.hexX, settlement.hexY, settlement.hexZ, planetRadius * 1.01f);
                settleGo.transform.position = pos;
                
                // Scale based on population
                float scale = 0.05f + (settlement.population / 100000f) * 0.1f;
                settleGo.transform.localScale = Vector3.one * scale;
                settleGo.transform.rotation = Quaternion.LookRotation(pos);

                // Label
                var labelGo = new GameObject("Label");
                var textMesh = labelGo.AddComponent<TextMesh>();
                textMesh.text = settlement.name;
                textMesh.fontSize = 10;
                textMesh.anchor = TextAnchor.MiddleCenter;
                textMesh.color = settlementColor;
                labelGo.transform.SetParent(settleGo.transform);
                labelGo.transform.localPosition = Vector3.up * 0.3f;
            }
        }

        /// <summary>
        /// Creates industry markers on the planet surface.
        /// </summary>
        private void CreateIndustries(List<IndustryData> industries)
        {
            industriesContainer = new GameObject("Industries");
            industriesContainer.transform.SetParent(transform);

            foreach (var industry in industries)
            {
                var indGo = new GameObject($"Industry_{industry.id}");
                indGo.transform.SetParent(industriesContainer.transform);

                // Industry marker (cube for factory)
                var cubeGo = GameObject.CreatePrimitive(PrimitiveType.Cube);
                cubeGo.transform.SetParent(indGo.transform);
                cubeGo.transform.localPosition = Vector3.zero;

                var mat = new Material(Shader.Find("Standard"));
                mat.color = industryColor;
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", industryColor * 0.2f);
                cubeGo.GetComponent<MeshRenderer>().material = mat;

                // Position on planet surface
                Vector3 pos = HexToVector3(industry.hexX, industry.hexY, industry.hexZ, planetRadius * 1.01f);
                indGo.transform.position = pos;
                indGo.transform.localScale = Vector3.one * 0.06f;
                indGo.transform.rotation = Quaternion.LookRotation(pos);

                // Label
                var labelGo = new GameObject("Label");
                var textMesh = labelGo.AddComponent<TextMesh>();
                textMesh.text = industry.label;
                textMesh.fontSize = 8;
                textMesh.anchor = TextAnchor.MiddleCenter;
                textMesh.color = industryColor;
                labelGo.transform.SetParent(indGo.transform);
                labelGo.transform.localPosition = Vector3.up * 0.25f;
            }
        }

        /// <summary>
        /// Creates station markers on the planet surface.
        /// </summary>
        private void CreateStations(List<StationData> stations)
        {
            stationsContainer = new GameObject("Stations");
            stationsContainer.transform.SetParent(transform);

            foreach (var station in stations)
            {
                var statGo = new GameObject($"Station_{station.name}");
                statGo.transform.SetParent(stationsContainer.transform);

                // Station marker (sphere for loading point)
                var sphereGo = GameObject.CreatePrimitive(PrimitiveType.Sphere);
                sphereGo.transform.SetParent(statGo.transform);
                sphereGo.transform.localPosition = Vector3.zero;

                var mat = new Material(Shader.Find("Standard"));
                mat.color = stationColor;
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", stationColor * 0.4f);
                sphereGo.GetComponent<MeshRenderer>().material = mat;

                // Position on planet surface
                Vector3 pos = HexToVector3(station.hexX, station.hexY, station.hexZ, planetRadius * 1.015f);
                statGo.transform.position = pos;
                statGo.transform.localScale = Vector3.one * 0.04f;

                // Label
                var labelGo = new GameObject("Label");
                var textMesh = labelGo.AddComponent<TextMesh>();
                textMesh.text = station.name;
                textMesh.fontSize = 8;
                textMesh.anchor = TextAnchor.MiddleCenter;
                textMesh.color = stationColor;
                labelGo.transform.SetParent(statGo.transform);
                labelGo.transform.localPosition = Vector3.up * 0.2f;
            }
        }

        /// <summary>
        /// Creates vehicle markers on the planet surface.
        /// </summary>
        private void CreateVehicles(List<VehicleData> vehicles)
        {
            vehiclesContainer = new GameObject("Vehicles");
            vehiclesContainer.transform.SetParent(transform);

            foreach (var vehicle in vehicles)
            {
                var vehGo = new GameObject($"Vehicle_{vehicle.id}");
                vehGo.transform.SetParent(vehiclesContainer.transform);

                // Vehicle marker (small capsule)
                var capsuleGo = GameObject.CreatePrimitive(PrimitiveType.Capsule);
                capsuleGo.transform.SetParent(vehGo.transform);
                capsuleGo.transform.localPosition = Vector3.zero;

                var mat = new Material(Shader.Find("Standard"));
                mat.color = vehicleColor;
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", vehicleColor * 0.5f);
                capsuleGo.GetComponent<MeshRenderer>().material = mat;

                // Position on planet surface
                Vector3 pos = HexToVector3(vehicle.hexX, vehicle.hexY, vehicle.hexZ, planetRadius * 1.02f);
                vehGo.transform.position = pos;
                vehGo.transform.localScale = new Vector3(0.02f, 0.03f, 0.02f);
                vehGo.transform.rotation = Quaternion.LookRotation(pos);

                // Store reference for animation
                vehicleObjects[vehicle.id] = vehGo;
            }
        }

        /// <summary>
        /// Updates vehicle positions for animation.
        /// </summary>
        public void UpdateVehiclePositions(List<VehicleData> vehicles)
        {
            if (vehicles == null || vehiclesContainer == null) return;

            foreach (var vehicle in vehicles)
            {
                if (vehicleObjects.ContainsKey(vehicle.id))
                {
                    Vector3 pos = HexToVector3(vehicle.hexX, vehicle.hexY, vehicle.hexZ, planetRadius * 1.02f);
                    vehicleObjects[vehicle.id].transform.position = pos;
                }
            }
        }

        /// <summary>
        /// Converts Hex3D coordinates to 3D position on sphere.
        /// Maps cube coordinates (x, y, z) to sphere surface.
        /// </summary>
        private Vector3 HexToVector3(int x, int y, int z, float radius)
        {
            // Normalize hex coordinates to sphere surface
            // Using axial coordinates: x and z map to sphere, y maps to height
            float magnitude = Mathf.Sqrt(x * x + y * y + z * z);
            if (magnitude < 0.001f) magnitude = 1f;

            // Convert to spherical coordinates
            float normalizedX = x / magnitude;
            float normalizedY = y / magnitude;
            float normalizedZ = z / magnitude;

            return new Vector3(normalizedX, normalizedY, normalizedZ) * radius;
        }

        /// <summary>
        /// Converts latitude/longitude to 3D position on sphere.
        /// </summary>
        private Vector3 LatitudeLongitudeToVector3(float lat, float lon, float radius)
        {
            float phi = (90f - lat) * Mathf.Deg2Rad;
            float theta = (lon + 180f) * Mathf.Deg2Rad;

            return new Vector3(
                -radius * Mathf.Sin(phi) * Mathf.Cos(theta),
                radius * Mathf.Cos(phi),
                radius * Mathf.Sin(phi) * Mathf.Sin(theta)
            );
        }

        /// <summary>
        /// Gets the color for a terrain type.
        /// </summary>
        private Color GetTerrainColor(TerrainType type)
        {
            return type switch
            {
                TerrainType.DEEP_OCEAN => deepOceanColor,
                TerrainType.SHALLOW_WATER => shallowWaterColor,
                TerrainType.BEACH => beachColor,
                TerrainType.GRASSLAND => grasslandColor,
                TerrainType.FOREST => forestColor,
                TerrainType.DESERT => desertColor,
                TerrainType.TUNDRA => tundraColor,
                TerrainType.ICE => iceColor,
                TerrainType.MOUNTAIN => mountainColor,
                TerrainType.HIGHLAND => highlandColor,
                TerrainType.SWAMP => swampColor,
                TerrainType.VOLCANIC => volcanicColor,
                TerrainType.JUNGLE => jungleColor,
                TerrainType.SAVANNA => savannaColor,
                TerrainType.WASTELAND => wastelandColor,
                _ => grasslandColor
            };
        }

        /// <summary>
        /// Converts a string terrain type name to the enum.
        /// </summary>
        private TerrainType StringToTerrainType(string name)
        {
            if (string.IsNullOrEmpty(name)) return TerrainType.GRASSLAND;
            
            switch (name.ToUpper())
            {
                case "DEEP_OCEAN": return TerrainType.DEEP_OCEAN;
                case "SHALLOW_WATER": return TerrainType.SHALLOW_WATER;
                case "BEACH": return TerrainType.BEACH;
                case "GRASSLAND": return TerrainType.GRASSLAND;
                case "FOREST": return TerrainType.FOREST;
                case "DESERT": return TerrainType.DESERT;
                case "TUNDRA": return TerrainType.TUNDRA;
                case "ICE": return TerrainType.ICE;
                case "MOUNTAIN": return TerrainType.MOUNTAIN;
                case "HIGHLAND": return TerrainType.HIGHLAND;
                case "SWAMP": return TerrainType.SWAMP;
                case "VOLCANIC": return TerrainType.VOLCANIC;
                case "JUNGLE": return TerrainType.JUNGLE;
                case "SAVANNA": return TerrainType.SAVANNA;
                case "WASTELAND": return TerrainType.WASTELAND;
                default: return TerrainType.GRASSLAND;
            }
        }

        /// <summary>
        /// Clears all rendered objects.
        /// </summary>
        public void Clear()
        {
            if (planetMesh != null) Destroy(planetMesh);
            if (atmosphere != null) Destroy(atmosphere);
            if (settlementsContainer != null) Destroy(settlementsContainer);
            if (industriesContainer != null) Destroy(industriesContainer);
            if (stationsContainer != null) Destroy(stationsContainer);
            if (vehiclesContainer != null) Destroy(vehiclesContainer);
            if (infrastructureContainer != null) Destroy(infrastructureContainer);

            planetMesh = null;
            atmosphere = null;
            settlementsContainer = null;
            industriesContainer = null;
            stationsContainer = null;
            vehiclesContainer = null;
            infrastructureContainer = null;
            vehicleObjects.Clear();
        }

        private void OnDestroy()
        {
            Clear();
        }
    }

    /// <summary>
    /// Terrain types matching the Java TerrainType enum.
    /// </summary>
    public enum TerrainType
    {
        DEEP_OCEAN,
        SHALLOW_WATER,
        BEACH,
        GRASSLAND,
        FOREST,
        DESERT,
        TUNDRA,
        ICE,
        MOUNTAIN,
        HIGHLAND,
        SWAMP,
        VOLCANIC,
        JUNGLE,
        SAVANNA,
        WASTELAND
    }

    /// <summary>
    /// Extended data structure for planet surface rendering.
    /// </summary>
    public class ExtendedPlanetSurfaceData
    {
        public string name;
        public int seed;
        public string dominantTerrain;
        public List<SettlementData> settlements;
        public List<IndustryData> industries;
        public List<StationData> stations;
        public List<VehicleData> vehicles;
        public List<InfrastructureData> infrastructure;
    }

    /// <summary>
    /// Settlement data for rendering.
    /// </summary>
    public class SettlementData
    {
        public string id;
        public string name;
        public int hexX;
        public int hexY;
        public int hexZ;
        public int population;
        public string size;
    }

    /// <summary>
    /// Industry data for rendering.
    /// </summary>
    public class IndustryData
    {
        public string id;
        public string type;
        public string label;
        public int hexX;
        public int hexY;
        public int hexZ;
        public float productionLevel;
    }

    /// <summary>
    /// Station data for rendering.
    /// </summary>
    public class StationData
    {
        public string id;
        public string name;
        public int hexX;
        public int hexY;
        public int hexZ;
    }

    /// <summary>
    /// Vehicle data for rendering.
    /// </summary>
    public class VehicleData
    {
        public string id;
        public string type;
        public int hexX;
        public int hexY;
        public int hexZ;
        public int cargoCount;
        public int passengerCount;
    }

    /// <summary>
    /// Infrastructure data for rendering (roads, rails).
    /// </summary>
    public class InfrastructureData
    {
        public int hexX;
        public int hexY;
        public int hexZ;
        public string type; // "ROAD" or "RAIL"
    }
}
