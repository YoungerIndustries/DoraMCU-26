using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Renders ship interiors (bridge, cargo, weapons).
    /// <para>
    /// Creates a first-person view of a ship's interior with different rooms
    /// and interactive elements.
    /// </para>
    /// </summary>
    public class ShipInteriorRenderer : MonoBehaviour
    {
        [Header("Ship Settings")]
        [SerializeField] private float shipLength = 20f;
        [SerializeField] private float shipWidth = 8f;
        [SerializeField] private float shipHeight = 6f;

        [Header("Room Colors")]
        [SerializeField] private Color bridgeColor = new Color(0.2f, 0.3f, 0.4f);
        [SerializeField] private Color cargoColor = new Color(0.3f, 0.3f, 0.2f);
        [SerializeField] private Color weaponsColor = new Color(0.4f, 0.2f, 0.2f);
        [SerializeField] private Color engineColor = new Color(0.3f, 0.2f, 0.1f);

        // Rendered rooms
        private Dictionary<string, GameObject> rooms;

        public Dictionary<string, GameObject> Rooms => rooms;

        private void Awake()
        {
            rooms = new Dictionary<string, GameObject>();
        }

        /// <summary>
        /// Renders the ship interior with all rooms.
        /// </summary>
        public void RenderInterior(ShipInteriorData data)
        {
            Clear();

            if (data == null) return;

            // Hull
            CreateHull();

            // Bridge (front)
            CreateRoom("bridge", new Vector3(shipLength * 0.35f, 0, 0), bridgeColor);

            // Cargo (middle)
            CreateRoom("cargo", Vector3.zero, cargoColor);

            // Weapons (side)
            CreateRoom("weapons", new Vector3(0, 0, shipWidth * 0.35f), weaponsColor);

            // Engine (back)
            CreateRoom("engine", new Vector3(-shipLength * 0.35f, 0, 0), engineColor);

            // Add interactive elements
            if (data.hasBridgeConsole)
            {
                CreateBridgeConsole();
            }

            Debug.Log($"[ShipInteriorRenderer] Rendered ship interior: {data.shipName}");
        }

        /// <summary>
        /// Creates the ship hull.
        /// </summary>
        private void CreateHull()
        {
            var hull = new GameObject("Hull");
            hull.transform.SetParent(transform);

            var filter = hull.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitive.Cube();

            var renderer = hull.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard"));
            mat.color = new Color(0.15f, 0.15f, 0.2f);
            renderer.material = mat;

            hull.transform.localScale = new Vector3(shipLength, shipHeight, shipWidth);
        }

        /// <summary>
        /// Creates a room inside the ship.
        /// </summary>
        private void CreateRoom(string name, Vector3 localPos, Color color)
        {
            var room = new GameObject($"Room_{name}");
            room.transform.SetParent(transform);
            room.transform.localPosition = localPos;

            // Floor
            var floor = new GameObject("Floor");
            floor.transform.SetParent(room.transform);
            var floorFilter = floor.AddComponent<MeshFilter>();
            floorFilter.mesh = CreatePrimitive.Cube();
            var floorRenderer = floor.AddComponent<MeshRenderer>();
            var floorMat = new Material(Shader.Find("Standard"));
            floorMat.color = color;
            floorRenderer.material = floorMat;
            floor.transform.localScale = new Vector3(shipWidth * 0.8f, 0.1f, shipWidth * 0.8f);
            floor.transform.localPosition = new Vector3(0, -shipHeight * 0.4f, 0);

            // Walls
            CreateWall(room.transform, new Vector3(0, 0, shipWidth * 0.4f), new Vector3(shipWidth * 0.8f, shipHeight * 0.8f, 0.1f));
            CreateWall(room.transform, new Vector3(0, 0, -shipWidth * 0.4f), new Vector3(shipWidth * 0.8f, shipHeight * 0.8f, 0.1f));

            rooms[name] = room;
        }

        /// <summary>
        /// Creates a wall.
        /// </summary>
        private void CreateWall(Transform parent, Vector3 pos, Vector3 scale)
        {
            var wall = new GameObject("Wall");
            wall.transform.SetParent(parent);
            wall.transform.localPosition = pos;

            var filter = wall.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitive.Cube();
            var renderer = wall.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard"));
            mat.color = new Color(0.2f, 0.2f, 0.25f);
            renderer.material = mat;
            wall.transform.localScale = scale;
        }

        /// <summary>
        /// Creates the bridge console.
        /// </summary>
        private void CreateBridgeConsole()
        {
            if (!rooms.ContainsKey("bridge")) return;

            var console = new GameObject("Console");
            console.transform.SetParent(rooms["bridge"].transform);
            console.transform.localPosition = new Vector3(0, -shipHeight * 0.3f, 0);

            var filter = console.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitive.Cube();
            var renderer = console.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard"));
            mat.color = new Color(0.1f, 0.5f, 0.8f);
            mat.EnableKeyword("_EMISSION");
            mat.SetColor("_EmissionColor", new Color(0.1f, 0.3f, 0.5f));
            renderer.material = mat;
            console.transform.localScale = new Vector3(2f, 1f, 1f);
        }

        /// <summary>
        /// Clears all rendered objects.
        /// </summary>
        public void Clear()
        {
            foreach (var room in rooms.Values)
            {
                Destroy(room);
            }
            rooms.Clear();

            // Destroy hull
            foreach (Transform child in transform)
            {
                Destroy(child.gameObject);
            }
        }

        private void OnDestroy()
        {
            Clear();
        }
    }

    /// <summary>
    /// Data structure for ship interior rendering.
    /// </summary>
    public class ShipInteriorData
    {
        public string shipName;
        public bool hasBridgeConsole;
        public int cargoCapacity;
        public int weaponCount;
    }
}
