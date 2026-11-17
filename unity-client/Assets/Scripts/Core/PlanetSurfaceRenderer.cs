using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;

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
        private GameObject aiAdminsContainer;

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
        /// Creates AI admin markers on the planet surface.
        /// </summary>
        public void CreateAiAdminMarkers(List<AiAdminData> admins)
        {
            if (admins == null || admins.Count == 0) return;

            ClearAiAdmins();

            aiAdminsContainer = new GameObject("AiAdmins");
            aiAdminsContainer.transform.SetParent(transform);

            foreach (var admin in admins)
            {
                if (!admin.hasPosition) continue;

                var adminGo = new GameObject($"AiAdmin_{admin.id}_{admin.name}");
                adminGo.transform.SetParent(aiAdminsContainer.transform);

                // AI admin marker (small capsule)
                var capsule = GameObject.CreatePrimitive(PrimitiveType.Capsule);
                capsule.transform.SetParent(adminGo.transform);
                capsule.transform.localPosition = Vector3.zero;
                capsule.transform.localScale = new Vector3(0.05f, 0.1f, 0.05f);

                // Color based on role
                var renderer = capsule.GetComponent<Renderer>();
                var mat = new Material(Shader.Find("Standard"));
                mat.color = GetAiAdminColor(admin);
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", GetAiAdminColor(admin) * 0.5f);
                renderer.material = mat;

                // Position on planet surface using the admin's coordinates
                // Map posX/posZ to latitude/longitude for planet surface placement
                float lat = Mathf.Clamp((admin.posZ / 10f) * 90f, -90f, 90f);
                float lon = Mathf.Clamp((admin.posX / 10f) * 180f, -180f, 180f);
                Vector3 pos = LatitudeLongitudeToVector3(lat, lon, planetRadius * 1.02f);
                adminGo.transform.position = pos;
                adminGo.transform.rotation = Quaternion.LookRotation(pos);

                // Add a label
                try
                {
                    var labelGo = new GameObject("Label");
                    labelGo.transform.SetParent(adminGo.transform);
                    labelGo.transform.localPosition = new Vector3(0, 0.15f, 0);

                    var textMesh = labelGo.AddComponent<TextMesh>();
                    textMesh.text = $"{admin.name}\n({admin.role})";
                    textMesh.fontSize = 10;
                    textMesh.anchor = TextAnchor.MiddleCenter;
                    textMesh.alignment = TextAlignment.Center;
                    textMesh.characterSize = 0.03f;
                    textMesh.color = Color.white;
                }
                catch (System.Exception e)
                {
                    Debug.LogWarning($"Could not create label for {admin.name}: {e.Message}");
                }
            }
        }

        /// <summary>
        /// Get the color for an AI admin based on their role.
        /// </summary>
        private Color GetAiAdminColor(AiAdminData admin)
        {
            if (admin.isGalaxyOverseer) return new Color(1f, 0.84f, 0f, 1f); // Gold

            var role = admin.role?.ToLower() ?? "";
            if (role.Contains("leader")) return new Color(0.25f, 0.41f, 0.88f, 1f); // Blue
            if (role.Contains("governor")) return new Color(0.2f, 0.8f, 0.2f, 1f); // Green
            if (role.Contains("manager")) return new Color(1f, 1f, 0f, 1f); // Yellow
            if (admin.characterType != null && admin.characterType.Contains("ENTITY"))
                return new Color(0.5f, 0.2f, 0.8f, 1f); // Purple

            return new Color(0.7f, 0.7f, 0.7f, 1f); // Gray
        }

        /// <summary>
        /// Clear AI admin markers.
        /// </summary>
        public void ClearAiAdmins()
        {
            if (aiAdminsContainer != null) Destroy(aiAdminsContainer);
            aiAdminsContainer = null;
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
            ClearAiAdmins();

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
