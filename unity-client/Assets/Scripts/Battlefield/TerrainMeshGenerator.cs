using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Generates terrain meshes from elevation grid data.
    /// <para>
    /// Creates procedural heightmap geometry with proper normals, UVs,
    /// and terrain-type-based vertex colors.
    /// </para>
    /// </summary>
    public static class TerrainMeshGenerator
    {
        /// <summary>
        /// Generates a terrain mesh from elevation data.
        /// </summary>
        /// <param name="width">Grid width in cells</param>
        /// <param name="height">Grid height in cells</param>
        /// <param name="cellSize">Size of each cell in meters</param>
        /// <param name="elevations">Flat array of elevation values (row-major order)</param>
        /// <param name="terrainType">Terrain type for coloring</param>
        /// <returns>A configured GameObject with the terrain mesh</returns>
        public static GameObject GenerateTerrain(
            int width,
            int height,
            float cellSize,
            List<float> elevations,
            string terrainType)
        {
            if (width <= 0 || height <= 0)
            {
                Debug.LogWarning("[TerrainMeshGenerator] Invalid dimensions");
                return null;
            }

            var go = new GameObject("Terrain");
            var filter = go.AddComponent<MeshFilter>();
            var renderer = go.AddComponent<MeshRenderer>();

            filter.mesh = CreateMesh(width, height, cellSize, elevations, terrainType);

            var mat = new Material(Shader.Find("Standard"));
            mat.EnableKeyword("_VERTEXCOLOR");
            renderer.material = mat;

            return go;
        }

        /// <summary>
        /// Creates a Mesh from elevation data.
        /// </summary>
        private static Mesh CreateMesh(
            int width,
            int height,
            float cellSize,
            List<float> elevations,
            string terrainType)
        {
            int vertexCount = width * height;
            int triangleCount = (width - 1) * (height - 1) * 6;

            var mesh = new Mesh
            {
                name = "BattlefieldTerrain"
            };

            // Vertices
            var vertices = new Vector3[vertexCount];
            var uvs = new Vector2[vertexCount];
            var colors = new Color[vertexCount];

            float minElevation = float.MaxValue;
            float maxElevation = float.MinValue;

            // First pass: calculate positions and find elevation range
            for (int row = 0; row < height; row++)
            {
                for (int col = 0; col < width; col++)
                {
                    int index = row * width + col;
                    float elevation = GetElevation(elevations, row, col, width);

                    vertices[index] = new Vector3(
                        col * cellSize,
                        elevation,
                        row * cellSize
                    );

                    uvs[index] = new Vector2((float)col / (width - 1), (float)row / (height - 1));

                    if (elevation < minElevation) minElevation = elevation;
                    if (elevation > maxElevation) maxElevation = elevation;
                }
            }

            // Second pass: calculate colors based on elevation and terrain type
            float elevationRange = maxElevation - minElevation;
            if (elevationRange < 0.01f) elevationRange = 1f;

            for (int row = 0; row < height; row++)
            {
                for (int col = 0; col < width; col++)
                {
                    int index = row * width + col;
                    float elevation = GetElevation(elevations, row, col, width);
                    float normalizedHeight = (elevation - minElevation) / elevationRange;

                    colors[index] = GetTerrainColor(terrainType, normalizedHeight);
                }
            }

            // Triangles
            var triangles = new int[triangleCount];
            int triIndex = 0;

            for (int row = 0; row < height - 1; row++)
            {
                for (int col = 0; col < width - 1; col++)
                {
                    int topLeft = row * width + col;
                    int topRight = topLeft + 1;
                    int bottomLeft = (row + 1) * width + col;
                    int bottomRight = bottomLeft + 1;

                    // Two triangles per quad
                    triangles[triIndex++] = topLeft;
                    triangles[triIndex++] = bottomLeft;
                    triangles[triIndex++] = topRight;

                    triangles[triIndex++] = topRight;
                    triangles[triIndex++] = bottomLeft;
                    triangles[triIndex++] = bottomRight;
                }
            }

            mesh.vertices = vertices;
            mesh.uv = uvs;
            mesh.colors = colors;
            mesh.triangles = triangles;
            mesh.RecalculateNormals();

            return mesh;
        }

        /// <summary>
        /// Gets elevation value at a grid position, with bounds checking.
        /// </summary>
        private static float GetElevation(List<float> elevations, int row, int col, int width)
        {
            if (elevations == null) return 0f;
            int index = row * width + col;
            if (index < 0 || index >= elevations.Count) return 0f;
            return elevations[index];
        }

        /// <summary>
        /// Gets the color for a terrain type at a given normalized height.
        /// </summary>
        private static Color GetTerrainColor(string terrainType, float normalizedHeight)
        {
            Color baseColor = terrainType?.ToLower() switch
            {
                "plains" => new Color(0.3f, 0.6f, 0.2f),
                "urban" => new Color(0.5f, 0.5f, 0.5f),
                "mountain" => new Color(0.4f, 0.35f, 0.3f),
                "desert" => new Color(0.8f, 0.7f, 0.4f),
                "jungle" => new Color(0.1f, 0.5f, 0.1f),
                "arctic" => new Color(0.9f, 0.92f, 0.95f),
                "forest" => new Color(0.15f, 0.4f, 0.15f),
                "swamp" => new Color(0.3f, 0.4f, 0.2f),
                "coastal" => new Color(0.7f, 0.75f, 0.5f),
                _ => new Color(0.3f, 0.5f, 0.2f)
            };

            // Blend toward rock/white at higher elevations
            Color highElevationColor = terrainType?.ToLower() switch
            {
                "arctic" => new Color(0.95f, 0.95f, 0.98f),
                "mountain" => new Color(0.6f, 0.55f, 0.5f),
                "desert" => new Color(0.9f, 0.85f, 0.7f),
                _ => new Color(0.5f, 0.45f, 0.4f)
            };

            float blendFactor = Mathf.Pow(normalizedHeight, 2f);
            return Color.Lerp(baseColor, highElevationColor, blendFactor);
        }

        /// <summary>
        /// Creates a simple flat ground plane as a fallback.
        /// </summary>
        public static GameObject CreateFlatGround(float size = 400f)
        {
            var go = new GameObject("FlatGround");
            var filter = go.AddComponent<MeshFilter>();
            var renderer = go.AddComponent<MeshRenderer>();

            // Create a simple plane
            var plane = GameObject.CreatePrimitive(PrimitiveType.Plane);
            filter.mesh = plane.GetComponent<MeshFilter>().sharedMesh;
            go.transform.localScale = new Vector3(size / 10f, 1f, size / 10f);

            var mat = new Material(Shader.Find("Standard"));
            mat.color = new Color(0.3f, 0.5f, 0.2f);
            renderer.material = mat;

            Destroy(plane);

            return go;
        }
    }
}
