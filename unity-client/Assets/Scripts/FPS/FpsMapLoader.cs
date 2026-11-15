using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Loads and generates FPS maps from server data.
    /// Creates procedural geometry for walls, floors, and obstacles.
    /// </summary>
    public class FpsMapLoader : MonoBehaviour
    {
        [Header("Map Settings")]
        [SerializeField] private LayerMask mapLayer;
        [SerializeField] private Material floorMaterial;
        [SerializeField] private Material wallMaterial;
        [SerializeField] private Material obstacleMaterial;

        [Header("Spawn Points")]
        [SerializeField] private GameObject spawnPointPrefab;

        private Transform mapParent;
        private Transform spawnParent;
        private List<Vector3> spawnPoints = new();
        private List<Bounds> collisionBounds = new();

        public List<Vector3> SpawnPoints => spawnPoints;
        public List<Bounds> CollisionBounds => collisionBounds;

        void Awake()
        {
            // Create parent transforms
            mapParent = new GameObject("Map").transform;
            mapParent.SetParent(transform);

            spawnParent = new GameObject("SpawnPoints").transform;
            spawnParent.SetParent(transform);
        }

        /// <summary>
        /// Load a map from server data.
        /// </summary>
        public IEnumerator LoadMapFromServer(string mapId, Action<bool> callback)
        {
            // Fetch map data from REST API
            string url = $"http://localhost:8080/api/v1/fps/maps/{mapId}";

            using (var request = UnityEngine.Networking.UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityEngine.Networking.UnityWebRequest.Result.Success)
                {
                    var mapData = System.Text.Json.JsonSerializer.Deserialize<Dictionary<string, object>>(request.downloadHandler.text);
                    GenerateMap(mapData);
                    callback?.Invoke(true);
                }
                else
                {
                    Debug.LogError($"[FpsMapLoader] Failed to load map: {request.error}");
                    // Generate default map
                    GenerateDefaultMap();
                    callback?.Invoke(false);
                }
            }
        }

        /// <summary>
        /// Generate a map from JSON data.
        /// </summary>
        public void GenerateMap(Dictionary<string, object> mapData)
        {
            // Clear existing map
            ClearMap();

            // Parse map dimensions
            double width = 100, depth = 100, height = 20;
            if (mapData.ContainsKey("mapWidth"))
                width = Convert.ToDouble(mapData["mapWidth"]);
            if (mapData.ContainsKey("mapDepth"))
                depth = Convert.ToDouble(mapData["mapDepth"]);
            if (mapData.ContainsKey("mapHeight"))
                height = Convert.ToDouble(mapData["mapHeight"]);

            // Generate floor
            GenerateFloor((float)width, (float)depth);

            // Generate walls
            GenerateWalls((float)width, (float)depth, (float)height);

            // Parse spawn points
            if (mapData.ContainsKey("spawnPoints"))
            {
                var spawnPointsJson = mapData["spawnPoints"] as List<object>;
                if (spawnPointsJson != null)
                {
                    foreach (var sp in spawnPointsJson)
                    {
                        if (sp is Dictionary<string, object> spDict)
                        {
                            float x = Convert.ToSingle(spDict.ContainsKey("x") ? spDict["x"] : 0);
                            float y = Convert.ToSingle(spDict.ContainsKey("y") ? spDict["y"] : 1.7f);
                            float z = Convert.ToSingle(spDict.ContainsKey("z") ? spDict["z"] : 0);
                            Vector3 pos = new Vector3(x, y, z);
                            spawnPoints.Add(pos);
                            CreateSpawnPointVisual(pos);
                        }
                    }
                }
            }

            // Parse collision boxes
            if (mapData.ContainsKey("collisionBoxes"))
            {
                var collisionJson = mapData["collisionBoxes"] as List<object>;
                if (collisionJson != null)
                {
                    foreach (var cb in collisionJson)
                    {
                        if (cb is Dictionary<string, object> cbDict)
                        {
                            float minX = Convert.ToSingle(cbDict.ContainsKey("minX") ? cbDict["minX"] : 0);
                            float maxX = Convert.ToSingle(cbDict.ContainsKey("maxX") ? cbDict["maxX"] : 0);
                            float minY = Convert.ToSingle(cbDict.ContainsKey("minY") ? cbDict["minY"] : 0);
                            float maxY = Convert.ToSingle(cbDict.ContainsKey("maxY") ? cbDict["maxY"] : 0);
                            float minZ = Convert.ToSingle(cbDict.ContainsKey("minZ") ? cbDict["minZ"] : 0);
                            float maxZ = Convert.ToSingle(cbDict.ContainsKey("maxZ") ? cbDict["maxZ"] : 0);

                            Vector3 center = new Vector3(
                                (minX + maxX) / 2f,
                                (minY + maxY) / 2f,
                                (minZ + maxZ) / 2f
                            );
                            Vector3 size = new Vector3(
                                maxX - minX,
                                maxY - minY,
                                maxZ - minZ
                            );

                            CreateObstacle(center, size);
                            collisionBounds.Add(new Bounds(center, size));
                        }
                    }
                }
            }

            // If no spawn points, create defaults
            if (spawnPoints.Count == 0)
            {
                CreateDefaultSpawnPoints((float)width, (float)depth);
            }

            Debug.Log($"[FpsMapLoader] Generated map: {spawnPoints.Count} spawn points, {collisionBounds.Count} collision boxes");
        }

        private void GenerateFloor(float width, float depth)
        {
            GameObject floor = GameObject.CreatePrimitive(PrimitiveType.Plane);
            floor.transform.SetParent(mapParent);
            floor.transform.position = new Vector3(0, 0, 0);
            floor.transform.localScale = new Vector3(width / 10f, 1f, depth / 10f);
            floor.transform.name = "Floor";

            if (floorMaterial != null)
            {
                var renderer = floor.GetComponent<Renderer>();
                if (renderer != null)
                    renderer.material = floorMaterial;
            }

            // Add collider
            if (floor.GetComponent<Collider>() == null)
            {
                floor.AddComponent<BoxCollider>();
            }
        }

        private void GenerateWalls(float width, float depth, float height)
        {
            float halfWidth = width / 2f;
            float halfDepth = depth / 2f;
            float wallThickness = 1f;

            // Create 4 boundary walls
            CreateWall(new Vector3(0, height / 2f, -halfDepth), new Vector3(width, height, wallThickness), "NorthWall");
            CreateWall(new Vector3(0, height / 2f, halfDepth), new Vector3(width, height, wallThickness), "SouthWall");
            CreateWall(new Vector3(-halfWidth, height / 2f, 0), new Vector3(wallThickness, height, depth), "WestWall");
            CreateWall(new Vector3(halfWidth, height / 2f, 0), new Vector3(wallThickness, height, depth), "EastWall");
        }

        private void CreateWall(Vector3 position, Vector3 scale, string name)
        {
            GameObject wall = GameObject.CreatePrimitive(PrimitiveType.Cube);
            wall.transform.SetParent(mapParent);
            wall.transform.position = position;
            wall.transform.localScale = scale;
            wall.transform.name = name;

            if (wallMaterial != null)
            {
                var renderer = wall.GetComponent<Renderer>();
                if (renderer != null)
                    renderer.material = wallMaterial;
            }
        }

        private void CreateObstacle(Vector3 center, Vector3 size)
        {
            GameObject obstacle = GameObject.CreatePrimitive(PrimitiveType.Cube);
            obstacle.transform.SetParent(mapParent);
            obstacle.transform.position = center;
            obstacle.transform.localScale = size;
            obstacle.transform.name = "Obstacle";

            if (obstacleMaterial != null)
            {
                var renderer = obstacle.GetComponent<Renderer>();
                if (renderer != null)
                    renderer.material = obstacleMaterial;
            }
        }

        private void CreateSpawnPointVisual(Vector3 position)
        {
            if (spawnPointPrefab != null)
            {
                GameObject spawn = Instantiate(spawnPointPrefab, position, Quaternion.identity);
                spawn.transform.SetParent(spawnParent);
                spawn.name = $"Spawn_{spawnPoints.Count}";
            }
            else
            {
                // Simple visual marker
                GameObject marker = GameObject.CreatePrimitive(PrimitiveType.Sphere);
                marker.transform.SetParent(spawnParent);
                marker.transform.position = position;
                marker.transform.localScale = Vector3.one * 0.5f;
                marker.GetComponent<Renderer>().material.color = Color.yellow;
                marker.name = $"Spawn_{spawnPoints.Count}";
            }
        }

        private void CreateDefaultSpawnPoints(float width, float depth)
        {
            float halfWidth = width / 2f;
            float halfDepth = depth / 2f;

            // 4 corner spawn points
            spawnPoints.Add(new Vector3(-halfWidth + 5f, 1.7f, -halfDepth + 5f));
            spawnPoints.Add(new Vector3(halfWidth - 5f, 1.7f, -halfDepth + 5f));
            spawnPoints.Add(new Vector3(-halfWidth + 5f, 1.7f, halfDepth - 5f));
            spawnPoints.Add(new Vector3(halfWidth - 5f, 1.7f, halfDepth - 5f));

            foreach (var sp in spawnPoints)
            {
                CreateSpawnPointVisual(sp);
            }
        }

        /// <summary>
        /// Generate a default arena map.
        /// </summary>
        public void GenerateDefaultMap()
        {
            ClearMap();

            float width = 100f;
            float depth = 100f;
            float height = 10f;

            GenerateFloor(width, depth);
            GenerateWalls(width, depth, height);

            // Add some obstacles
            CreateObstacle(new Vector3(0, 2.5f, 0), new Vector3(10f, 5f, 10f));
            CreateObstacle(new Vector3(-25f, 1.5f, -25f), new Vector3(6f, 3f, 6f));
            CreateObstacle(new Vector3(25f, 1.5f, 25f), new Vector3(6f, 3f, 6f));
            CreateObstacle(new Vector3(-25f, 1.5f, 25f), new Vector3(6f, 3f, 6f));
            CreateObstacle(new Vector3(25f, 1.5f, -25f), new Vector3(6f, 3f, 6f));

            collisionBounds.Add(new Bounds(Vector3.zero, new Vector3(10f, 5f, 10f)));
            collisionBounds.Add(new Bounds(new Vector3(-25f, 1.5f, -25f), new Vector3(6f, 3f, 6f)));
            collisionBounds.Add(new Bounds(new Vector3(25f, 1.5f, 25f), new Vector3(6f, 3f, 6f)));
            collisionBounds.Add(new Bounds(new Vector3(-25f, 1.5f, 25f), new Vector3(6f, 3f, 6f)));
            collisionBounds.Add(new Bounds(new Vector3(25f, 1.5f, -25f), new Vector3(6f, 3f, 6f)));

            CreateDefaultSpawnPoints(width, depth);

            Debug.Log("[FpsMapLoader] Generated default arena map");
        }

        /// <summary>
        /// Get a random spawn point.
        /// </summary>
        public Vector3 GetRandomSpawnPoint()
        {
            if (spawnPoints.Count == 0)
                return Vector3.zero;

            return spawnPoints[UnityEngine.Random.Range(0, spawnPoints.Count)];
        }

        /// <summary>
        /// Get a spawn point for a specific team.
        /// </summary>
        public Vector3 GetSpawnPointForTeam(int team)
        {
            if (spawnPoints.Count == 0)
                return Vector3.zero;

            // Simple team-based spawn: team 0 = first half, team 1 = second half
            int index = team * (spawnPoints.Count / 2);
            if (index >= spawnPoints.Count)
                index = 0;

            return spawnPoints[index];
        }

        /// <summary>
        /// Check if a position is within map bounds.
        /// </summary>
        public bool IsWithinBounds(Vector3 position)
        {
            foreach (var bounds in collisionBounds)
            {
                if (bounds.Contains(position))
                    return false; // Inside an obstacle
            }
            return true;
        }

        /// <summary>
        /// Clear the current map.
        /// </summary>
        public void ClearMap()
        {
            // Destroy all children of mapParent
            for (int i = mapParent.childCount - 1; i >= 0; i--)
            {
                Destroy(mapParent.GetChild(i).gameObject);
            }

            // Destroy all children of spawnParent
            for (int i = spawnParent.childCount - 1; i >= 0; i--)
            {
                Destroy(spawnParent.GetChild(i).gameObject);
            }

            spawnPoints.Clear();
            collisionBounds.Clear();
        }
    }
}
