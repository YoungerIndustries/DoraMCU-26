using UnityEngine;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Renders planet surfaces with terrain, atmosphere, and cities.
    /// <para>
    /// Creates a detailed 3D planet with procedural terrain based on planet type
    /// and adds atmospheric effects and city markers.
    /// </para>
    /// </summary>
    public class PlanetSurfaceRenderer : MonoBehaviour
    {
        [Header("Visual Settings")]
        [SerializeField] private float planetRadius = 5f;
        [SerializeField] private float atmosphereScale = 1.1f;
        [SerializeField] private int terrainResolution = 32;

        [Header("Colors by Planet Type")]
        [SerializeField] private Color terrestrialColor = new Color(0.2f, 0.5f, 0.2f);
        [SerializeField] private Color desertColor = new Color(0.8f, 0.6f, 0.3f);
        [SerializeField] private Color iceColor = new Color(0.7f, 0.8f, 0.9f);
        [SerializeField] private Color gasGiantColor = new Color(0.6f, 0.4f, 0.3f);
        [SerializeField] private Color atmosphereColor = new Color(0.3f, 0.6f, 1f, 0.3f);

        // Rendered objects
        private GameObject planetMesh;
        private GameObject atmosphere;
        private GameObject citiesContainer;

        /// <summary>
        /// Renders a planet surface with terrain and atmosphere.
        /// </summary>
        public void RenderPlanet(PlanetSurfaceData data)
        {
            Clear();

            if (data == null) return;

            // Create planet sphere
            CreatePlanetSphere(data);

            // Create atmosphere
            CreateAtmosphere();

            // Create cities
            if (data.cities != null)
            {
                CreateCities(data.cities);
            }

            Debug.Log($"[PlanetSurfaceRenderer] Rendered planet: {data.name}");
        }

        /// <summary>
        /// Creates the planet sphere with terrain coloring.
        /// </summary>
        private void CreatePlanetSphere(PlanetSurfaceData data)
        {
            planetMesh = new GameObject($"Planet_{data.name}");
            planetMesh.transform.SetParent(transform);

            var filter = planetMesh.AddComponent<MeshFilter>();
            var renderer = planetMesh.AddComponent<MeshRenderer>();

            // Create sphere mesh
            filter.mesh = CreateTerrainMesh(data);

            // Color based on planet type
            Color color = GetPlanetColor(data.type);
            var mat = new Material(Shader.Find("Standard"));
            mat.color = color;
            renderer.material = mat;

            planetMesh.transform.localScale = Vector3.one * planetRadius;
        }

        /// <summary>
        /// Creates a terrain mesh with slight height variation.
        /// </summary>
        private Mesh CreateTerrainMesh(PlanetSurfaceData data)
        {
            var mesh = new Mesh();
            mesh.name = $"Terrain_{data.name}";

            // Use Unity's built-in sphere as base
            var baseSphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            var baseMesh = baseSphere.GetComponent<MeshFilter>().sharedMesh;

            // Apply slight vertex displacement for terrain
            var positions = baseMesh.vertices;
            System.Random rand = new System.Random(data.seed.GetHashCode());

            for (int i = 0; i < positions.Length; i++)
            {
                // Add slight terrain variation
                float noise = (rand.NextFloat() - 0.5f) * 0.05f;
                positions[i] += positions[i].normalized * noise;
            }

            mesh.vertices = positions;
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
            filter.mesh = GameObject.CreatePrimitive(PrimitiveType.Sphere).GetComponent<MeshFilter>().sharedMesh;

            var renderer = atmosphere.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = atmosphereColor;
            renderer.material = mat;

            atmosphere.transform.localScale = Vector3.one * planetRadius * atmosphereScale;

            Destroy(GameObject.FindPrimitive(PrimitiveType.Sphere));
        }

        /// <summary>
        /// Creates city markers on the planet surface.
        /// </summary>
        private void CreateCities(System.Collections.Generic.List<CityData> cities)
        {
            citiesContainer = new GameObject("Cities");
            citiesContainer.transform.SetParent(transform);

            foreach (var city in cities)
            {
                var cityGo = new GameObject($"City_{city.name}");
                cityGo.transform.SetParent(citiesContainer.transform);

                // City marker (small cylinder)
                var cylinder = cityGo.AddComponent<Cylinder>();
                var mat = new Material(Shader.Find("Standard"));
                mat.color = Color.yellow;
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", Color.yellow * 0.5f);
                cityGo.GetComponent<MeshRenderer>().material = mat;

                // Position on planet surface
                Vector3 pos = LatitudeLongitudeToVector3(city.latitude, city.longitude, planetRadius * 1.01f);
                cityGo.transform.position = pos;
                cityGo.transform.localScale = new Vector3(0.05f, 0.1f, 0.05f);
                cityGo.transform.rotation = Quaternion.LookRotation(pos);
            }
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
        /// Gets the color for a planet type.
        /// </summary>
        private Color GetPlanetColor(string type)
        {
            return type.ToLower() switch
            {
                "terrestrial" => terrestrialColor,
                "desert" => desertColor,
                "ice" => iceColor,
                "gas_giant" => gasGiantColor,
                _ => terrestrialColor
            };
        }

        /// <summary>
        /// Clears all rendered objects.
        /// </summary>
        public void Clear()
        {
            if (planetMesh != null) Destroy(planetMesh);
            if (atmosphere != null) Destroy(atmosphere);
            if (citiesContainer != null) Destroy(citiesContainer);

            planetMesh = null;
            atmosphere = null;
            citiesContainer = null;
        }

        private void OnDestroy()
        {
            Clear();
        }
    }

    /// <summary>
    /// Data structure for planet surface rendering.
    /// </summary>
    public class PlanetSurfaceData
    {
        public string name;
        public string type;
        public int seed;
        public System.Collections.Generic.List<CityData> cities;
    }

    public class CityData
    {
        public string name;
        public float latitude;
        public float longitude;
        public int population;
    }
}
