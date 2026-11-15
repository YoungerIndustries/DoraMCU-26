using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Generates procedural building meshes based on structure type.
    /// <para>
    /// Creates box, cylinder, and custom shapes for different building types
    /// with type-based coloring.
    /// </para>
    /// </summary>
    public static class BuildingGenerator
    {
        /// <summary>
        /// Generates a building GameObject based on type and position.
        /// </summary>
        public static GameObject GenerateBuilding(SceneBuilding buildingData, Transform parent)
        {
            var go = new GameObject($"Building_{buildingData.id}")
            {
                transform = 
                {
                    parent = parent,
                    position = buildingData.Position,
                    rotation = Quaternion.Euler(
                        buildingData.transform?.rotation?[0] ?? 0f,
                        buildingData.transform?.rotation?[1] ?? 0f,
                        buildingData.transform?.rotation?[2] ?? 0f
                    )
                }
            };

            string meshType = buildingData.mesh ?? "box";
            
            switch (meshType.ToLower())
            {
                case "cylinder":
                    CreateCylinder(go, buildingData.Scale);
                    break;
                case "bridge":
                    CreateBridge(go, buildingData.Scale);
                    break;
                case "dock":
                    CreateDock(go, buildingData.Scale);
                    break;
                case "plane":
                    CreateAirfield(go, buildingData.Scale);
                    break;
                default:
                    CreateBox(go, buildingData.Scale);
                    break;
            }

            return go;
        }

        /// <summary>
        /// Creates a box-shaped building.
        /// </summary>
        private static void CreateBox(GameObject go, Vector3 scale)
        {
            var body = new GameObject("Body") { transform = { parent = go.transform } };
            var filter = body.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveBox();
            var renderer = body.AddComponent<MeshRenderer>();
            renderer.material = new Material(Shader.Find("Standard")) { color = GetBuildingColor(go.name) };
            body.transform.localScale = scale;
        }

        /// <summary>
        /// Creates a cylinder-shaped building (silos, towers).
        /// </summary>
        private static void CreateCylinder(GameObject go, Vector3 scale)
        {
            var body = new GameObject("Body") { transform = { parent = go.transform } };
            var filter = body.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveCylinder();
            var renderer = body.AddComponent<MeshRenderer>();
            renderer.material = new Material(Shader.Find("Standard")) { color = GetBuildingColor(go.name) };
            body.transform.localScale = scale;
        }

        /// <summary>
        /// Creates a bridge structure.
        /// </summary>
        private static void CreateBridge(GameObject go, Vector3 scale)
        {
            // Main deck
            var deck = new GameObject("Deck") { transform = { parent = go.transform } };
            var filter = deck.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveBox();
            var renderer = deck.AddComponent<MeshRenderer>();
            renderer.material = new Material(Shader.Find("Standard")) { color = new Color(0.4f, 0.4f, 0.45f) };
            deck.transform.localScale = new Vector3(scale.x, scale.y * 0.2f, scale.z);
            deck.transform.localPosition = Vector3.up * scale.y * 0.3f;

            // Support pillars
            for (int i = -1; i <= 1; i += 2)
            {
                var pillar = new GameObject($"Pillar_{i}") { transform = { parent = go.transform } };
                var pFilter = pillar.AddComponent<MeshFilter>();
                pFilter.mesh = CreatePrimitiveBox();
                var pRenderer = pillar.AddComponent<MeshRenderer>();
                pRenderer.material = new Material(Shader.Find("Standard")) { color = new Color(0.35f, 0.35f, 0.4f) };
                pillar.transform.localScale = new Vector3(scale.x * 0.1f, scale.y, scale.z * 0.1f);
                pillar.transform.localPosition = new Vector3(i * scale.x * 0.4f, 0, 0);
            }
        }

        /// <summary>
        /// Creates a dock/harbor structure.
        /// </summary>
        private static void CreateDock(GameObject go, Vector3 scale)
        {
            var platform = new GameObject("Platform") { transform = { parent = go.transform } };
            var filter = platform.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveBox();
            var renderer = platform.AddComponent<MeshRenderer>();
            renderer.material = new Material(Shader.Find("Standard")) { color = new Color(0.5f, 0.4f, 0.3f) };
            platform.transform.localScale = scale;

            // Mooring posts
            for (int i = -1; i <= 1; i += 2)
            {
                var post = new GameObject($"Post_{i}") { transform = { parent = go.transform } };
                var pFilter = post.AddComponent<MeshFilter>();
                pFilter.mesh = CreatePrimitiveCylinder();
                var pRenderer = post.AddComponent<MeshRenderer>();
                pRenderer.material = new Material(Shader.Find("Standard")) { color = new Color(0.4f, 0.35f, 0.25f) };
                post.transform.localScale = new Vector3(0.5f, scale.y * 1.5f, 0.5f);
                post.transform.localPosition = new Vector3(i * scale.x * 0.4f, scale.y * 0.75f, 0);
            }
        }

        /// <summary>
        /// Creates an airfield/runway marker.
        /// </summary>
        private static void CreateAirfield(GameObject go, Vector3 scale)
        {
            var runway = new GameObject("Runway") { transform = { parent = go.transform } };
            var filter = runway.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveBox();
            var renderer = runway.AddComponent<MeshRenderer>();
            renderer.material = new Material(Shader.Find("Standard")) { color = new Color(0.35f, 0.35f, 0.35f) };
            runway.transform.localScale = new Vector3(scale.x, 0.1f, scale.z);
            runway.transform.localPosition = Vector3.down * 0.05f;
        }

        /// <summary>
        /// Gets the color for a building based on its type.
        /// </summary>
        private static Color GetBuildingColor(string buildingName)
        {
            string lower = buildingName.ToLower();
            if (lower.Contains("bunker")) return new Color(0.4f, 0.45f, 0.4f);
            if (lower.Contains("factory")) return new Color(0.5f, 0.4f, 0.3f);
            if (lower.Contains("hospital")) return new Color(0.85f, 0.85f, 0.85f);
            if (lower.Contains("command")) return new Color(0.3f, 0.4f, 0.5f);
            if (lower.Contains("bridge")) return new Color(0.4f, 0.4f, 0.45f);
            if (lower.Contains("port") || lower.Contains("harbor")) return new Color(0.5f, 0.4f, 0.3f);
            if (lower.Contains("airfield")) return new Color(0.35f, 0.35f, 0.35f);
            return new Color(0.5f, 0.5f, 0.5f);
        }

        /// <summary>
        /// Creates a simple box mesh.
        /// </summary>
        private static Mesh CreatePrimitiveBox()
        {
            var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
            var mesh = cube.GetComponent<MeshFilter>().sharedMesh;
            Destroy(cube);
            return mesh;
        }

        /// <summary>
        /// Creates a simple cylinder mesh.
        /// </summary>
        private static Mesh CreatePrimitiveCylinder()
        {
            var cyl = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
            var mesh = cyl.GetComponent<MeshFilter>().sharedMesh;
            Destroy(cyl);
            return mesh;
        }
    }
}
