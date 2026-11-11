using UnityEngine;

namespace DoraMCU.Client.Procedural
{
    /// <summary>
    /// Generates 3D space station meshes procedurally.
    /// Creates different station types: trading, military, exploration, repair.
    /// </summary>
    public class StationGenerator
    {
        /// <summary>
        /// Generate a space station mesh.
        /// </summary>
        public GameObject GenerateStation(StationData data, Transform parent = null)
        {
            var go = new GameObject($"Station_{data.Name}")
            {
                transform = { parent = parent }
            };

            // Central hub
            var hubMesh = GenerateHub(data);
            var mf = go.AddComponent<MeshFilter>();
            mf.mesh = hubMesh;

            var mr = go.AddComponent<MeshRenderer>();
            mr.material = CreateStationMaterial(data);

            // Rotating ring
            GenerateRing(go, data);

            // Docking ports
            GenerateDockingPorts(go, data);

            return go;
        }

        private Mesh GenerateHub(StationData data)
        {
            float radius = data.Size * 0.3f;
            float length = data.Size * 0.8f;

            // Cylinder hub
            var vertices = new Vector3[24];
            var triangles = new int[36];

            // Top ring
            for (int i = 0; i < 8; i++)
            {
                float angle = (i * 2 * Mathf.PI) / 8;
                vertices[i] = new Vector3(
                    Mathf.Cos(angle) * radius,
                    length * 0.5f,
                    Mathf.Sin(angle) * radius
                );
            }

            // Bottom ring
            for (int i = 0; i < 8; i++)
            {
                float angle = (i * 2 * Mathf.PI) / 8;
                vertices[i + 8] = new Vector3(
                    Mathf.Cos(angle) * radius,
                    -length * 0.5f,
                    Mathf.Sin(angle) * radius
                );
            }

            // Center top
            vertices[16] = new Vector3(0, length * 0.5f, 0);
            // Center bottom
            vertices[17] = new Vector3(0, -length * 0.5f, 0);

            // Top cap
            for (int i = 0; i < 8; i++)
            {
                int next = (i + 1) % 8;
                triangles[i * 3] = 16;
                triangles[i * 3 + 1] = i;
                triangles[i * 3 + 2] = next;
            }

            // Bottom cap
            for (int i = 0; i < 8; i++)
            {
                int next = (i + 1) % 8;
                triangles[24 + i * 3] = 17;
                triangles[24 + i * 3 + 1] = next + 8;
                triangles[24 + i * 3 + 2] = i + 8;
            }

            var mesh = new Mesh
            {
                vertices = vertices,
                triangles = triangles
            };
            mesh.RecalculateNormals();
            mesh.RecalculateBounds();
            return mesh;
        }

        private void GenerateRing(GameObject station, StationData data)
        {
            var ringGo = new GameObject("Ring")
            {
                transform = { parent = station.transform }
            };

            float ringRadius = data.Size * 0.6f;
            float ringThickness = data.Size * 0.1f;

            int segments = 32;
            var vertices = new Vector3[segments * 4];
            var triangles = new int[segments * 6];

            for (int i = 0; i < segments; i++)
            {
                float angle = (i * 2 * Mathf.PI) / segments;
                float nextAngle = ((i + 1) * 2 * Mathf.PI) / segments;

                float x1 = Mathf.Cos(angle) * ringRadius;
                float z1 = Mathf.Sin(angle) * ringRadius;
                float x2 = Mathf.Cos(nextAngle) * ringRadius;
                float z2 = Mathf.Sin(nextAngle) * ringRadius;

                int vi = i * 4;
                vertices[vi] = new Vector3(x1, ringThickness, z1);
                vertices[vi + 1] = new Vector3(x2, ringThickness, z2);
                vertices[vi + 2] = new Vector3(x2, -ringThickness, z2);
                vertices[vi + 3] = new Vector3(x1, -ringThickness, z1);

                int ti = i * 6;
                triangles[ti] = vi;
                triangles[ti + 1] = vi + 1;
                triangles[ti + 2] = vi + 2;
                triangles[ti + 3] = vi;
                triangles[ti + 4] = vi + 2;
                triangles[ti + 5] = vi + 3;
            }

            var mesh = new Mesh
            {
                vertices = vertices,
                triangles = triangles
            };
            mesh.RecalculateNormals();
            mesh.RecalculateBounds();

            var mf = ringGo.AddComponent<MeshFilter>();
            mf.mesh = mesh;

            var mr = ringGo.AddComponent<MeshRenderer>();
            mr.material = new Material(Shader.Find("Standard"))
            {
                color = new Color(0.6f, 0.6f, 0.7f),
            };
            mr.material.SetFloat("_Metallic", 0.8f);
        }

        private void GenerateDockingPorts(GameObject station, StationData data)
        {
            int ports = data.Type switch
            {
                "Trading" => 4,
                "Military" => 2,
                "Exploration" => 2,
                "Repair" => 3,
                _ => 2
            };

            for (int i = 0; i < ports; i++)
            {
                var portGo = new GameObject("DockingPort")
                {
                    transform = { parent = station.transform }
                };

                float angle = (i * 2 * Mathf.PI) / ports;
                float radius = data.Size * 0.35f;
                portGo.transform.localPosition = new Vector3(
                    Mathf.Cos(angle) * radius,
                    0,
                    Mathf.Sin(angle) * radius
                );

                // Simple port geometry
                var mesh = new Mesh();
                var vertices = new Vector3[]
                {
                    new Vector3(0, 0, 0),
                    new Vector3(data.Size * 0.15f, data.Size * 0.05f, 0),
                    new Vector3(data.Size * 0.15f, -data.Size * 0.05f, 0),
                };
                var triangles = new int[] { 0, 1, 2 };
                mesh.vertices = vertices;
                mesh.triangles = triangles;
                mesh.RecalculateNormals();
                mesh.RecalculateBounds();

                var mf = portGo.AddComponent<MeshFilter>();
                mf.mesh = mesh;

                var mr = portGo.AddComponent<MeshRenderer>();
                mr.material = new Material(Shader.Find("Particles/Alpha Blended"))
                {
                    color = new Color(0.5f, 1f, 0.5f, 0.6f)
                };
            }
        }

        private Material CreateStationMaterial(StationData data)
        {
            var mat = new Material(Shader.Find("Standard"));

            Color color = data.Type switch
            {
                "Trading" => new Color(0.7f, 0.7f, 0.5f),
                "Military" => new Color(0.5f, 0.4f, 0.4f),
                "Exploration" => new Color(0.4f, 0.6f, 0.7f),
                "Repair" => new Color(0.6f, 0.5f, 0.6f),
                _ => new Color(0.5f, 0.5f, 0.6f)
            };

            mat.color = color;
            mat.SetFloat("_Metallic", 0.8f);
            mat.SetFloat("_Smoothness", 0.6f);

            return mat;
        }

        /// <summary>
        /// Station data for procedural generation.
        /// </summary>
        public struct StationData
        {
            public string Name;
            public string Type;
            public int Size;
            public int Fuel;
            public int Repair;
        }
    }
}
