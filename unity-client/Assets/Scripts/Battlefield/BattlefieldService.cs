using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;
using DoraMCU.Client.Network;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Handles all communication with the Third Reich battlefield API endpoints.
    /// <para>
    /// Follows the NetworkManager pattern with coroutines and callbacks.
    /// Provides battlefield creation, scene graph fetching, status polling, and updates.
    /// </para>
    /// </summary>
    public class BattlefieldService : Singleton<BattlefieldService>
    {
        [Header("Polling Settings")]
        [SerializeField] private float defaultPollInterval = 5f;

        private string currentBattlefieldId;
        private Coroutine pollingCoroutine;
        private float pollInterval;

        // Events
        public event Action<BattlefieldSceneGraph> OnSceneGraphReceived;
        public event Action<string, Dictionary<string, object>> OnBattlefieldStatus;
        public event Action<string> OnBattlefieldCreated;
        public event Action<string> OnError;

        public string CurrentBattlefieldId => currentBattlefieldId;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        /// <summary>
        /// Creates a new battlefield from a planet location.
        /// </summary>
        public void CreateBattlefield(
            long locationId,
            string timePeriod,
            string terrain,
            string weather,
            int attackerCount,
            int defenderCount,
            Action<string, bool> callback)
        {
            StartCoroutine(CreateBattlefieldCoroutine(locationId, timePeriod, terrain, weather,
                attackerCount, defenderCount, callback));
        }

        private IEnumerator CreateBattlefieldCoroutine(
            long locationId,
            string timePeriod,
            string terrain,
            string weather,
            int attackerCount,
            int defenderCount,
            Action<string, bool> callback)
        {
            string serverUrl = NetworkManager.Instance?.ServerUrl ?? "http://localhost:8080";
            string url = $"{serverUrl}/api/v1/thirdreich/ground/battlefields";

            var request = new
            {
                locationId,
                timePeriod,
                terrain,
                weather,
                attackerCount,
                defenderCount
            };

            string json = JsonUtility.ToJson(request);
            var bytes = System.Text.Encoding.UTF8.GetBytes(json);

            using (var unityWebRequest = new UnityWebRequest(url, "POST"))
            {
                unityWebRequest.uploadHandler = new UploadHandlerRaw(bytes);
                unityWebRequest.downloadHandler = new DownloadHandlerBuffer();
                unityWebRequest.SetRequestHeader("Content-Type", "application/json");

                yield return unityWebRequest.SendWebRequest();

                if (unityWebRequest.result == UnityWebRequest.Result.Success)
                {
                    var result = JsonUtility.FromJson<CreateBattlefieldResponse>(unityWebRequest.downloadHandler.text);
                    currentBattlefieldId = result.id;
                    Debug.Log($"[BattlefieldService] Created battlefield: {result.id} at {result.name}");
                    OnBattlefieldCreated?.Invoke(result.id);
                    callback?.Invoke(result.id, true);
                }
                else
                {
                    Debug.LogError($"[BattlefieldService] Create battlefield failed: {unityWebRequest.error}");
                    OnError?.Invoke(unityWebRequest.error);
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Fetches the full scene graph for a battlefield.
        /// </summary>
        public void GetSceneGraph(string battlefieldId, Action<BattlefieldSceneGraph, bool> callback)
        {
            StartCoroutine(GetSceneGraphCoroutine(battlefieldId, callback));
        }

        private IEnumerator GetSceneGraphCoroutine(string battlefieldId, Action<BattlefieldSceneGraph, bool> callback)
        {
            string serverUrl = NetworkManager.Instance?.ServerUrl ?? "http://localhost:8080";
            string url = $"{serverUrl}/api/v1/thirdreich/ground/battlefields/{battlefieldId}/scene-graph";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    // Use SimpleJSON-style parsing for complex nested structures
                    var sceneGraph = ParseSceneGraph(request.downloadHandler.text);
                    OnSceneGraphReceived?.Invoke(sceneGraph);
                    callback?.Invoke(sceneGraph, true);
                }
                else
                {
                    Debug.LogError($"[BattlefieldService] Scene graph fetch failed: {request.error}");
                    OnError?.Invoke(request.error);
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Gets the current battlefield status.
        /// </summary>
        public void GetBattlefieldStatus(string battlefieldId, Action<Dictionary<string, object>, bool> callback)
        {
            StartCoroutine(GetStatusCoroutine(battlefieldId, callback));
        }

        private IEnumerator GetStatusCoroutine(string battlefieldId, Action<Dictionary<string, object>, bool> callback)
        {
            string serverUrl = NetworkManager.Instance?.ServerUrl ?? "http://localhost:8080";
            string url = $"{serverUrl}/api/v1/thirdreich/ground/battlefields/{battlefieldId}";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var status = ParseStatus(request.downloadHandler.text);
                    OnBattlefieldStatus?.Invoke(battlefieldId, status);
                    callback?.Invoke(status, true);
                }
                else
                {
                    Debug.LogError($"[BattlefieldService] Status fetch failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Starts periodic polling for battlefield updates.
        /// </summary>
        public void StartPolling(string battlefieldId, float interval = 5f)
        {
            currentBattlefieldId = battlefieldId;
            pollInterval = interval;

            if (pollingCoroutine != null)
            {
                StopCoroutine(pollingCoroutine);
            }

            pollingCoroutine = StartCoroutine(PollingLoop());
            Debug.Log($"[BattlefieldService] Started polling battlefield {battlefieldId} every {interval}s");
        }

        /// <summary>
        /// Stops periodic polling.
        /// </summary>
        public void StopPolling()
        {
            if (pollingCoroutine != null)
            {
                StopCoroutine(pollingCoroutine);
                pollingCoroutine = null;
            }
            Debug.Log("[BattlefieldService] Stopped polling");
        }

        private IEnumerator PollingLoop()
        {
            while (!string.IsNullOrEmpty(currentBattlefieldId))
            {
                yield return new WaitForSeconds(pollInterval);
                GetSceneGraph(currentBattlefieldId, (sceneGraph, success) =>
                {
                    if (success && sceneGraph != null)
                    {
                        OnSceneGraphReceived?.Invoke(sceneGraph);
                    }
                });
            }
        }

        /// <summary>
        /// Parses the scene graph JSON response into a BattlefieldSceneGraph object.
        /// Handles the complex nested structure that Unity's JsonUtility can't parse directly.
        /// </summary>
        private BattlefieldSceneGraph ParseSceneGraph(string json)
        {
            var graph = new BattlefieldSceneGraph();

            // Remove outer braces for easier parsing
            string inner = json.TrimStart('{').TrimEnd('}');

            // Parse metadata
            graph.metadata = ExtractObject<SceneMetadata>(json, "metadata");
            graph.camera = ExtractObject<SceneCamera>(json, "camera");
            graph.lighting = ExtractObject<SceneLighting>(json, "lighting");
            graph.terrain = ExtractObject<SceneTerrain>(json, "terrain");
            graph.biosphere = ExtractObject<SceneBiosphere>(json, "biosphere");

            // Parse buildings array
            graph.buildings = ExtractArray<SceneBuilding>(json, "buildings");

            // Parse unit arrays
            graph.attackerUnits = ExtractArray<SceneUnit>(json, "attackerUnits");
            graph.defenderUnits = ExtractArray<SceneUnit>(json, "defenderUnits");

            return graph;
        }

        /// <summary>
        /// Extracts a nested object from JSON by key.
        /// </summary>
        private T ExtractObject<T>(string json, string key)
        {
            try
            {
                int start = json.IndexOf($"\"{key}\"");
                if (start < 0) return null;

                start = json.IndexOf('{', start);
                if (start < 0) return null;

                int depth = 0;
                int end = start;
                for (int i = start; i < json.Length; i++)
                {
                    if (json[i] == '{') depth++;
                    if (json[i] == '}') depth--;
                    if (depth == 0) { end = i; break; }
                }

                string objectJson = json.Substring(start, end - start + 1);
                return JsonUtility.FromJson<T>(objectJson);
            }
            catch (Exception e)
            {
                Debug.LogWarning($"[BattlefieldService] Failed to extract {key}: {e.Message}");
                return null;
            }
        }

        /// <summary>
        /// Extracts a nested array from JSON by key.
        /// </summary>
        private List<T> ExtractArray<T>(string json, string key)
        {
            try
            {
                int start = json.IndexOf($"\"{key}\"");
                if (start < 0) return new List<T>();

                start = json.IndexOf('[', start);
                if (start < 0) return new List<T>();

                int depth = 0;
                int end = start;
                for (int i = start; i < json.Length; i++)
                {
                    if (json[i] == '[') depth++;
                    if (json[i] == ']') depth--;
                    if (depth == 0) { end = i; break; }
                }

                string arrayJson = json.Substring(start, end - start + 1);

                // Wrap in an object for JsonUtility
                string wrapped = $"{{\"array\":{arrayJson}}}";
                var wrapper = JsonUtility.FromJson<ArrayWrapper<T>>(wrapped);
                return wrapper?.array ?? new List<T>();
            }
            catch (Exception e)
            {
                Debug.LogWarning($"[BattlefieldService] Failed to extract array {key}: {e.Message}");
                return new List<T>();
            }
        }

        /// <summary>
        /// Parses battlefield status JSON into a dictionary.
        /// </summary>
        private Dictionary<string, object> ParseStatus(string json)
        {
            var status = new StatusResponse();
            status = JsonUtility.FromJson<StatusResponse>(json);

            var dict = new Dictionary<string, object>();
            if (status != null)
            {
                dict["id"] = status.id;
                dict["name"] = status.name;
                dict["locationName"] = status.locationName;
                dict["city"] = status.city;
                dict["country"] = status.country;
                dict["terrainType"] = status.terrainType;
                dict["baseTechLevel"] = status.baseTechLevel;
                dict["timePeriod"] = status.timePeriod;
                dict["attackerCount"] = status.attackerCount;
                dict["defenderCount"] = status.defenderCount;
                dict["structureCount"] = status.structureCount;
                dict["hasElevationMap"] = status.hasElevationMap;
                dict["biosphere"] = status.biosphere;
            }
            return dict;
        }

        void OnDestroy()
        {
            StopPolling();
        }

        // ------------------------------------------------------------------
        // Helper classes for JSON parsing
        // ------------------------------------------------------------------

        [Serializable]
        private class ArrayWrapper<T>
        {
            public List<T> array;
        }

        [Serializable]
        private class CreateBattlefieldResponse
        {
            public string id;
            public string name;
            public long locationId;
            public string locationName;
            public string city;
            public string country;
            public string terrainType;
            public int baseTechLevel;
            public string timePeriod;
            public int attackerCount;
            public int defenderCount;
            public int structureCount;
            public bool hasElevationMap;
            public string biosphere;
        }

        [Serializable]
        private class StatusResponse
        {
            public string id;
            public string name;
            public long locationId;
            public string locationName;
            public string city;
            public string country;
            public string terrainType;
            public int baseTechLevel;
            public string timePeriod;
            public int attackerCount;
            public int defenderCount;
            public int structureCount;
            public bool hasElevationMap;
            public string biosphere;
        }
    }
}
