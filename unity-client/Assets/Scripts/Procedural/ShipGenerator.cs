using UnityEngine;

namespace DoraMCU.Client.Procedural
{
    /// <summary>
    /// Generates 3D ship meshes procedurally based on ship design data.
    /// Creates different hull shapes for different ship classes.
    /// </summary>
    public class ShipGenerator
    {
        /// <summary>
        /// Generate a ship mesh based on design.
        /// </summary>
        public GameObject GenerateShip(ShipData data, Transform parent = null)
        {
            var go = new GameObject($"Ship_{data.Name}")
            {
                transform = { parent = parent }
            };

            // Hull
            var hullMesh = GenerateHull(data);
            var meshFilter = go.AddComponent<MeshFilter>();
            meshFilter.mesh = hullMesh;

            var meshRenderer = go.AddComponent<MeshRenderer>();
            meshRenderer.material = CreateShipMaterial(data);

            // Engines
            GenerateEngines(go, data);

            // Weapons
            GenerateWeapons(go, data);

            return go;
        }

        private Mesh GenerateHull(ShipData data)
        {
            float length = data.Hull * 0.5f;
            float width = data.Hull * 0.2f;
            float height = data.Hull * 0.15f;

            // Simple box hull with tapered ends
            var vertices = new Vector3[24];
            var triangles = new int[36];
            var normals = new Vector3[24];

            // Front face (tapered)
            vertices[0] = new Vector3(length * 0.5f, height * 0.5f, width * 0.3f);
            vertices[1] = new Vector3(length * 0.5f, height * 0.5f, -width * 0.3f);
            vertices[2] = new Vector3(length * 0.5f, -height * 0.5f, -width * 0.3f);
            vertices[3] = new Vector3(length * 0.5f, -height * 0.5f, width * 0.3f);

            // Back face
            vertices[4] = new Vector3(-length * 0.5f, height * 0.5f, width * 0.5f);
            vertices[5] = new Vector3(-length * 0.5f, height * 0.5f, -width * 0.5f);
            vertices[6] = new Vector3(-length * 0.5f, -height * 0.5f, -width * 0.5f);
            vertices[7] = new Vector3(-length * 0.5f, -height * 0.5f, width * 0.5f);

            // Top front
            vertices[8] = new Vector3(length * 0.3f, height * 0.7f, width * 0.2f);
            vertices[9] = new Vector3(length * 0.3f, height * 0.7f, -width * 0.2f);

            // Top back
            vertices[10] = new Vector3(-length * 0.3f, height * 0.7f, width * 0.3f);
            vertices[11] = new Vector3(-length * 0.3f, height * 0.7f, -width * 0.3f);

            // Bottom front
            vertices[12] = new Vector3(length * 0.3f, -height * 0.7f, width * 0.2f);
            vertices[13] = new Vector3(length * 0.3f, -height * 0.7f, -width * 0.2f);

            // Bottom back
            vertices[14] = new Vector3(-length * 0.3f, -height * 0.7f, width * 0.3f);
            vertices[15] = new Vector3(-length * 0.3f, -height * 0.7f, -width * 0.3f);

            // Engine mounts
            vertices[16] = new Vector3(-length * 0.6f, height * 0.2f, width * 0.6f);
            vertices[17] = new Vector3(-length * 0.6f, height * 0.2f, -width * 0.6f);
            vertices[18] = new Vector3(-length * 0.6f, -height * 0.2f, -width * 0.6f);
            vertices[19] = new Vector3(-length * 0.6f, -height * 0.2f, width * 0.6f);

            // Nose cone
            vertices[20] = new Vector3(length * 0.7f, 0, 0);
            vertices[21] = new Vector3(length * 0.6f, height * 0.1f, width * 0.1f);
            vertices[22] = new Vector3(length * 0.6f, height * 0.1f, -width * 0.1f);
            vertices[23] = new Vector3(length * 0.6f, -height * 0.1f, 0);

            // Normals
            for (int i = 0; i < vertices.Length; i++)
            {
                normals[i] = vertices[i].normalized;
            }

            // Triangles for hull
            triangles[0] = 0; triangles[1] = 1; triangles[2] = 2;
            triangles[3] = 0; triangles[4] = 2; triangles[5] = 3;
            triangles[6] = 4; triangles[7] = 7; triangles[8] = 6;
            triangles[9] = 4; triangles[10] = 6; triangles[11] = 5;
            triangles[12] = 0; triangles[13] = 4; triangles[14] = 7;
            triangles[13] = 0; triangles[14] = 7; triangles[15] = 3;
            triangles[16] = 1; triangles[17] = 5; triangles[18] = 6;
            triangles[19] = 1; triangles[20] = 6; triangles[21] = 2;
            triangles[22] = 8; triangles[23] = 10; triangles[24] = 11;
            triangles[25] = 8; triangles[26] = 11; triangles[27] = 9;
            triangles[28] = 12; triangles[29] = 14; triangles[30] = 15;
            triangles[31] = 12; triangles[32] = 15; triangles[33] = 13;
            triangles[34] = 16; triangles[35] = 19; triangles[36] = 18;

            var mesh = new Mesh
            {
                vertices = vertices,
                normals = normals,
                triangles = triangles
            };
            mesh.RecalculateBounds();
            return mesh;
        }

        private Material CreateShipMaterial(ShipData data)
        {
            var mat = new Material(Shader.Find("Standard"));

            // Color based on ship design
            Color color = data.Design switch
            {
                "Cutter" => new Color(0.6f, 0.7f, 0.8f),
                "Corvette" => new Color(0.5f, 0.6f, 0.7f),
                "Frigate" => new Color(0.4f, 0.5f, 0.6f),
                "Destroyer" => new Color(0.3f, 0.4f, 0.5f),
                "Cruiser" => new Color(0.35f, 0.45f, 0.55f),
                "Dreadnought" => new Color(0.2f, 0.3f, 0.4f),
                "Merchant" => new Color(0.7f, 0.6f, 0.5f),
                "Explorer" => new Color(0.5f, 0.7f, 0.5f),
                _ => new Color(0.5f, 0.5f, 0.6f)
            };

            mat.color = color;
            mat.SetFloat("_Metallic", 0.7f);
            mat.SetFloat("_Smoothness", 0.5f);

            return mat;
        }

        private void GenerateEngines(GameObject ship, ShipData data)
        {
            float engineSize = data.Hull * 0.15f;

            for (int i = 0; i < 2; i++)
            {
                var engineGo = new GameObject("Engine")
                {
                    transform = { parent = ship.transform }
                };

                float zOffset = (i == 0 ? 1 : -1) * data.Hull * 0.3f;
                engineGo.transform.localPosition = new Vector3(-data.Hull * 0.55f, 0, zOffset);

                var mesh = UnityEngine.Mesh.CreateSphere(engineSize, 8, 8);
                var mf = engineGo.AddComponent<MeshFilter>();
                mf.mesh = mesh;

                var mr = engineGo.AddComponent<MeshRenderer>();
                var mat = new Material(Shader.Find("Particles/Alpha Blended"))
                {
                    color = new Color(0.3f, 0.6f, 1f, 0.8f)
                };
                mr.material = mat;
            }
        }

        private void GenerateWeapons(GameObject ship, ShipData data)
        {
            if (data.Weapons <= 0) return;

            float weaponSize = data.Hull * 0.05f;

            for (int i = 0; i < data.Weapons && i < 4; i++)
            {
                var weaponGo = new GameObject("Weapon")
                {
                    transform = { parent = ship.transform }
                };

                float xOffset = data.Hull * 0.2f - (i * data.Hull * 0.1f);
                float zOffset = (i % 2 == 0 ? 1 : -1) * data.Hull * 0.25f;
                weaponGo.transform.localPosition = new Vector3(xOffset, data.Hull * 0.15f, zOffset);

                var mesh = new UnityEngine.Mesh();
                var vertices = new Vector3[]
                {
                    new Vector3(0, 0, 0),
                    new Vector3(weaponSize * 2, 0, 0),
                    new Vector3(weaponSize, weaponSize * 0.5f, 0),
                    new Vector3(weaponSize, -weaponSize * 0.5f, 0)
                };
                var triangles = new int[] { 0, 1, 2, 0, 2, 3, 0, 1, 3, 1, 2, 3 };
                mesh.vertices = vertices;
                mesh.triangles = triangles;
                mesh.RecalculateNormals();
                mesh.RecalculateBounds();

                var mf = weaponGo.AddComponent<MeshFilter>();
                mf.mesh = mesh;

                var mr = weaponGo.AddComponent<MeshRenderer>();
                mr.material = new Material(Shader.Find("Standard"))
                {
                    color = new Color(0.8f, 0.2f, 0.2f)
                };
            }
        }

        /// <summary>
        /// Ship data for procedural generation.
        /// </summary>
        public struct ShipData
        {
            public string Name;
            public string Design;
            public int Hull;
            public int Drives;
            public int Cargo;
            public int Fuel;
            public int Weapons;
            public int Armor;
            public int Shields;
        }
    }
}
