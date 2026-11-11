using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.Procedural
{
    /// <summary>
    /// Generates 3D planet meshes procedurally based on Traveller planet data.
    /// Uses noise-based terrain generation with atmosphere effects.
    /// </summary>
    public class PlanetGenerator
    {
        private readonly System.Random random;

        public PlanetGenerator(System.Random random = null)
        {
            this.random = random ?? new System.Random();
        }

        /// <summary>
        /// Generate a planet mesh based on Traveller planet data.
        /// </summary>
        public GameObject GeneratePlanet(PlanetData data, Transform parent = null)
        {
            var go = new GameObject($"Planet_{data.Name}")
            {
                transform = { parent = parent }
            };

            // Main planet sphere
            var planetMesh = GeneratePlanetMesh(data);
            var meshFilter = go.AddComponent<MeshFilter>();
            meshFilter.mesh = planetMesh;

            var meshRenderer = go.AddComponent<MeshRenderer>();
            meshRenderer.material = CreatePlanetMaterial(data);

            // Atmosphere glow
            if (data.Atmosphere > 0)
            {
                GenerateAtmosphere(go, data);
            }

            // Orbit ring
            GenerateOrbitRing(go, data);

            return go;
        }

        /// <summary>
        /// Generate a planet mesh with terrain variation.
        /// </summary>
        private Mesh GeneratePlanetMesh(PlanetData data)
        {
            int segments = 32;
            int rings = 16;
            float baseRadius = data.Size * 0.5f;

            var vertices = new List<Vector3>();
            var normals = new List<Vector3>();
            var uv = new List<Vector2>();
            var triangles = new List<int>();

            for (int r = 0; r <= rings; r++)
            {
                float phi = (r * Mathf.PI) / rings;

                for (int s = 0; s <= segments; s++)
                {
                    float theta = (s * 2 * Mathf.PI) / segments;

                    // Base sphere position
                    float x = Mathf.Cos(theta) * Mathf.Sin(phi);
                    float y = Mathf.Cos(phi);
                    float z = Mathf.Sin(theta) * Mathf.Sin(phi);

                    // Terrain noise based on planet type
                    float noise = Mathf.PerlinNoise(x * 3f, z * 3f) * 0.1f;
                    float hydroNoise = Mathf.PerlinNoise(x * 2f + 100f, z * 2f + 100f);

                    // Adjust for hydrographics (water coverage)
                    float waterLevel = data.Hydrographics / 10f;
                    if (hydroNoise < waterLevel && data.Atmosphere >= 3 && data.Atmosphere <= 8)
                    {
                        noise *= 0.5f; // Flatten water areas
                    }

                    float radius = baseRadius * (1f + noise);

                    vertices.Add(new Vector3(x, y, z) * radius);
                    normals.Add(new Vector3(x, y, z).normalized);
                    uv.Add(new Vector2((float)s / segments, (float)r / rings));
                }
            }

            // Generate triangles
            for (int r = 0; r < rings; r++)
            {
                for (int s = 0; s < segments; s++)
                {
                    int i1 = r * (segments + 1) + s;
                    int i2 = i1 + segments + 1;

                    triangles.Add(i1);
                    triangles.Add(i2);
                    triangles.Add(i1 + 1);

                    triangles.Add(i2);
                    triangles.Add(i2 + 1);
                    triangles.Add(i1 + 1);
                }
            }

            var mesh = new Mesh
            {
                vertices = vertices.ToArray(),
                normals = normals.ToArray(),
                uv = uv.ToArray(),
                triangles = triangles.ToArray()
            };

            mesh.RecalculateBounds();
            return mesh;
        }

        /// <summary>
        /// Create a material based on planet type.
        /// </summary>
        private Material CreatePlanetMaterial(PlanetData data)
        {
            var mat = new Material(Shader.Find("Standard"));

            // Color based on planet type
            Color baseColor = GetPlanetColor(data);
            mat.color = baseColor;

            // Emission for gas giants
            if (data.Size >= 8)
            {
                mat.EnableKeyword("_EMISSION");
                mat.SetColor("_EmissionColor", baseColor * 0.2f);
            }

            return mat;
        }

        /// <summary>
        /// Get planet color based on Traveller type.
        /// </summary>
        private Color GetPlanetColor(PlanetData data)
        {
            // Gas giant
            if (data.Size >= 8)
            {
                switch (data.Type)
                {
                    case 'A': return new Color(0.9f, 0.8f, 0.6f); // Yellow-brown
                    case 'B': return new Color(0.8f, 0.7f, 0.5f); // Orange-brown
                    case 'C': return new Color(0.7f, 0.6f, 0.4f); // Red-brown
                    case 'D': return new Color(0.6f, 0.5f, 0.3f); // Dark brown
                    default: return new Color(0.8f, 0.7f, 0.6f);
                }
            }

            // Rocky planet
            switch (data.Type)
            {
                case 'A': return new Color(0.2f, 0.6f, 0.2f); // Green
                case 'B': return new Color(0.3f, 0.7f, 0.3f); // Light green
                case 'C': return new Color(0.4f, 0.8f, 0.4f); // Bright green
                case 'D': return new Color(0.5f, 0.5f, 0.4f); // Brown
                case 'E': return new Color(0.6f, 0.6f, 0.5f); // Gray-brown
                case 'F': return new Color(0.7f, 0.7f, 0.6f); // Gray
                case 'G': return new Color(0.8f, 0.8f, 0.7f); // Light gray
                case 'H': return new Color(0.3f, 0.5f, 0.7f); // Blue (water world)
                case 'I': return new Color(0.7f, 0.8f, 0.9f); // Ice
                case 'J': return new Color(0.2f, 0.3f, 0.4f); // Dark
                case 'K': return new Color(0.9f, 0.9f, 0.9f); // White (hot)
                case 'L': return new Color(0.1f, 0.1f, 0.1f); // Black (lava)
                case 'M': return new Color(0.4f, 0.6f, 0.8f); // Earth-like
                case 'N': return new Color(0.3f, 0.4f, 0.6f); // Cool Earth-like
                case 'O': return new Color(0.5f, 0.7f, 0.9f); // Hot Earth-like
                case 'P': return new Color(0.6f, 0.4f, 0.3f); // Desert
                case 'Q': return new Color(0.8f, 0.6f, 0.4f); // Sandy
                case 'R': return new Color(0.7f, 0.5f, 0.3f); // Rocky
                case 'S': return new Color(0.9f, 0.8f, 0.5f); // Yellow
                case 'T': return new Color(0.2f, 0.4f, 0.3f); // Swamp
                case 'U': return new Color(0.5f, 0.5f, 0.5f); // Unknown
                case 'V': return new Color(0.6f, 0.3f, 0.6f); // Exotic
                case 'W': return new Color(0.3f, 0.6f, 0.6f); // Ocean
                case 'X': return new Color(0.8f, 0.5f, 0.5f); // Red
                case 'Y': return new Color(0.5f, 0.8f, 0.5f); // Lush
                case 'Z': return new Color(0.4f, 0.4f, 0.6f); // Purple
                default: return new Color(0.5f, 0.5f, 0.5f);
            }
        }

        /// <summary>
        /// Generate atmosphere glow effect.
        /// </summary>
        private void GenerateAtmosphere(GameObject planet, PlanetData data)
        {
            var atmosGo = new GameObject("Atmosphere")
            {
                transform = { parent = planet.transform }
            };

            float atmosSize = data.Size * 0.55f;
            var mesh = new Mesh();
            // Simple sphere for atmosphere
            mesh = GenerateSimpleSphere(atmosSize, 16, 8);

            var mf = atmosGo.AddComponent<MeshFilter>();
            mf.mesh = mesh;

            var mr = atmosGo.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"))
            {
                color = new Color(0.5f, 0.7f, 1f, 0.3f)
            };
            mr.material = mat;
        }

        /// <summary>
        /// Generate orbit ring visualization.
        /// </summary>
        private void GenerateOrbitRing(GameObject planet, PlanetData data)
        {
            if (data.OrbitRadius <= 0) return;

            var ringGo = new GameObject("OrbitRing")
            {
                transform = { parent = planet.transform.parent }
            };

            var lineRenderer = ringGo.AddComponent<LineRenderer>();
            lineRenderer.positionCount = 64;
            lineRenderer.startWidth = 0.05f;
            lineRenderer.endWidth = 0.05f;
            lineRenderer.startColor = new Color(1f, 1f, 1f, 0.3f);
            lineRenderer.endColor = new Color(1f, 1f, 1f, 0.3f);

            var positions = new Vector3[64];
            for (int i = 0; i < 64; i++)
            {
                float angle = (i * 2 * Mathf.PI) / 64;
                positions[i] = new Vector3(
                    Mathf.Cos(angle) * data.OrbitRadius,
                    0,
                    Mathf.Sin(angle) * data.OrbitRadius
                );
            }
            lineRenderer.SetPositions(positions);
        }

        /// <summary>
        /// Generate a simple sphere mesh.
        /// </summary>
        private Mesh GenerateSimpleSphere(float radius, int segments, int rings)
        {
            var vertices = new List<Vector3>();
            var triangles = new List<int>();

            for (int r = 0; r <= rings; r++)
            {
                float phi = (r * Mathf.PI) / rings;
                for (int s = 0; s <= segments; s++)
                {
                    float theta = (s * 2 * Mathf.PI) / segments;
                    float x = Mathf.Cos(theta) * Mathf.Sin(phi);
                    float y = Mathf.Cos(phi);
                    float z = Mathf.Sin(theta) * Mathf.Sin(phi);
                    vertices.Add(new Vector3(x, y, z) * radius);
                }
            }

            for (int r = 0; r < rings; r++)
            {
                for (int s = 0; s < segments; s++)
                {
                    int i1 = r * (segments + 1) + s;
                    int i2 = i1 + segments + 1;
                    triangles.Add(i1);
                    triangles.Add(i2);
                    triangles.Add(i1 + 1);
                    triangles.Add(i2);
                    triangles.Add(i2 + 1);
                    triangles.Add(i1 + 1);
                }
            }

            var mesh = new Mesh
            {
                vertices = vertices.ToArray(),
                triangles = triangles.ToArray()
            };
            mesh.RecalculateNormals();
            mesh.RecalculateBounds();
            return mesh;
        }

        /// <summary>
        /// Planet data for procedural generation.
        /// </summary>
        public struct PlanetData
        {
            public string Name;
            public char Type;
            public int Size;
            public int Atmosphere;
            public int Temperature;
            public int Hydrographics;
            public float OrbitRadius;
            public float OrbitAngle;
            public float RotationPeriod;
            public float AxialTilt;
        }
    }
}
