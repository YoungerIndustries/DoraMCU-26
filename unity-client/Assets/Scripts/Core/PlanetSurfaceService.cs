using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Service that connects the Unity planet surface renderer to the Java backend REST API.
    /// Fetches planet surface data (settlements, industries, stations, vehicles) and
    /// updates the ExtendedPlanetSurfaceRenderer with the latest state.
    /// </summary>
    public class PlanetSurfaceService : MonoBehaviour
    {
        [Header("API Configuration")]
        [SerializeField] private string apiBaseUrl = "http://localhost:8080/api/v1/campaigns";
        [SerializeField] private float updateInterval = 5f; // Seconds between updates

        [Header("References")]
        [SerializeField] private ExtendedPlanetSurfaceRenderer renderer;

        private string currentCampaignId;
        private int currentPlanetId;
        private bool isUpdating;
        private float lastUpdateTime;

        /// <summary>
        /// Starts monitoring a planet surface for a given campaign.
        /// </summary>
        public void StartMonitoring(string campaignId, int planetId, string planetName)
        {
            currentCampaignId = campaignId;
            currentPlanetId = planetId;
            isUpdating = true;
            lastUpdateTime = Time.time;

            // Initial fetch
            FetchPlanetSurface(planetName);

            Debug.Log($"[PlanetSurfaceService] Started monitoring campaign {campaignId}, planet {planetId} ({planetName})");
        }

        /// <summary>
        /// Stops monitoring the current planet surface.
        /// </summary>
        public void StopMonitoring()
        {
            isUpdating = false;
            currentCampaignId = null;
            Debug.Log("[PlanetSurfaceService] Stopped monitoring");
        }

        private void Update()
        {
            if (isUpdating && currentCampaignId != null && Time.time - lastUpdateTime >= updateInterval)
            {
                lastUpdateTime = Time.time;
                FetchPlanetSurface("Planet_" + currentPlanetId);
            }
        }

        /// <summary>
        /// Fetches planet surface data from the REST API.
        /// </summary>
        private void FetchPlanetSurface(string planetName)
        {
            if (string.IsNullOrEmpty(currentCampaignId)) return;

            string url = $"{apiBaseUrl}/{currentCampaignId}/planet-surface/{currentPlanetId}";
            StartCoroutine(FetchCoroutine(url, planetName));
        }

        private IEnumerator FetchCoroutine(string url, string planetName)
        {
            using (UnityWebRequest request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogError($"[PlanetSurfaceService] Error fetching planet surface: {request.error}");
                    yield break;
                }

                string json = request.downloadHandler.text;
                ProcessPlanetSurfaceData(json, planetName);
            }
        }

        /// <summary>
        /// Processes the JSON response and updates the renderer.
        /// </summary>
        private void ProcessPlanetSurfaceData(string json, string planetName)
        {
            if (renderer == null)
            {
                Debug.LogWarning("[PlanetSurfaceService] No renderer assigned");
                return;
            }

            try
            {
                var data = SimpleJsonParse(json, planetName);
                renderer.RenderPlanetSurface(data);
            }
            catch (Exception e)
            {
                Debug.LogError($"[PlanetSurfaceService] Error processing data: {e.Message}");
            }
        }

        /// <summary>
        /// Simple JSON parser for the planet surface data.
        /// Uses Unity's JsonUtility for basic parsing.
        /// </summary>
        private ExtendedPlanetSurfaceData SimpleJsonParse(string json, string planetName)
        {
            var data = new ExtendedPlanetSurfaceData
            {
                name = planetName,
                seed = UnityEngine.Random.Range(0, 1000000),
                dominantTerrain = "GRASSLAND",
                settlements = new List<SettlementData>(),
                industries = new List<IndustryData>(),
                stations = new List<StationData>(),
                vehicles = new List<VehicleData>(),
                infrastructure = new List<InfrastructureData>()
            };

            // Parse settlements
            ParseArray(json, "settlements", (obj) =>
            {
                data.settlements.Add(new SettlementData
                {
                    id = GetString(obj, "id"),
                    name = GetString(obj, "name"),
                    hexX = GetInt(obj, "hexX"),
                    hexY = GetInt(obj, "hexY"),
                    hexZ = GetInt(obj, "hexZ"),
                    population = GetInt(obj, "population"),
                    size = GetString(obj, "size")
                });
            });

            // Parse industries
            ParseArray(json, "industries", (obj) =>
            {
                data.industries.Add(new IndustryData
                {
                    id = GetString(obj, "id"),
                    type = GetString(obj, "type"),
                    label = GetString(obj, "label"),
                    hexX = GetInt(obj, "hexX"),
                    hexY = GetInt(obj, "hexY"),
                    hexZ = GetInt(obj, "hexZ"),
                    productionLevel = GetFloat(obj, "productionLevel")
                });
            });

            // Parse stations
            ParseArray(json, "stations", (obj) =>
            {
                data.stations.Add(new StationData
                {
                    id = GetString(obj, "id"),
                    name = GetString(obj, "name"),
                    hexX = GetInt(obj, "hexX"),
                    hexY = GetInt(obj, "hexY"),
                    hexZ = GetInt(obj, "hexZ")
                });
            });

            // Parse vehicles
            ParseArray(json, "vehicles", (obj) =>
            {
                data.vehicles.Add(new VehicleData
                {
                    id = GetString(obj, "id"),
                    type = GetString(obj, "type"),
                    hexX = GetInt(obj, "hexX"),
                    hexY = GetInt(obj, "hexY"),
                    hexZ = GetInt(obj, "hexZ"),
                    cargoCount = GetInt(obj, "cargoCount"),
                    passengerCount = GetInt(obj, "passengerCount")
                });
            });

            return data;
        }

        /// <summary>
        /// Parses a JSON array from the response.
        /// </summary>
        private void ParseArray(string json, string key, Action<Dictionary<string, string>> callback)
        {
            // Simple regex-like parsing for JSON arrays
            int start = json.IndexOf($"\"{key}\":");
            if (start < 0) return;

            start = json.IndexOf('[', start);
            if (start < 0) return;

            int depth = 0;
            int end = start;
            for (int i = start; i < json.Length; i++)
            {
                if (json[i] == '[') depth++;
                if (json[i] == ']') depth--;
                if (depth == 0) { end = i; break; }
            }

            string arrayContent = json.Substring(start + 1, end - start - 1).Trim();
            if (string.IsNullOrEmpty(arrayContent)) return;

            // Parse each object in the array
            depth = 0;
            int objStart = -1;
            for (int i = 0; i < arrayContent.Length; i++)
            {
                if (arrayContent[i] == '{')
                {
                    if (depth == 0) objStart = i;
                    depth++;
                }
                if (arrayContent[i] == '}')
                {
                    depth--;
                    if (depth == 0 && objStart >= 0)
                    {
                        string objStr = arrayContent.Substring(objStart, i - objStart + 1);
                        callback(ParseObject(objStr));
                    }
                }
            }
        }

        /// <summary>
        /// Parses a JSON object string into a dictionary.
        /// </summary>
        private Dictionary<string, string> ParseObject(string json)
        {
            var dict = new Dictionary<string, string>();
            json = json.Trim('{', '}').Trim();

            // Simple key-value parsing
            System.Text.RegularExpressions.Regex regex = new System.Text.RegularExpressions.Regex(
                "\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"|\"([^\"]+)\"\\s*:\\s*([^,}\\]]+)");

            foreach (System.Text.RegularExpressions.Match match in regex.Matches(json))
            {
                string key = match.Groups[1].Success ? match.Groups[1].Value : match.Groups[3].Value;
                string value = match.Groups[2].Success ? match.Groups[2].Value : match.Groups[4].Value.Trim();
                dict[key] = value;
            }

            return dict;
        }

        private string GetString(Dictionary<string, string> obj, string key)
        {
            return obj.ContainsKey(key) ? obj[key] : "";
        }

        private int GetInt(Dictionary<string, string> obj, string key)
        {
            if (obj.ContainsKey(key) && int.TryParse(obj[key], out int value))
                return value;
            return 0;
        }

        private float GetFloat(Dictionary<string, string> obj, string key)
        {
            if (obj.ContainsKey(key) && float.TryParse(obj[key], out float value))
                return value;
            return 0f;
        }

        /// <summary>
        /// Manually triggers a data refresh.
        /// </summary>
        public void Refresh()
        {
            if (currentCampaignId != null)
            {
                FetchPlanetSurface("Planet_" + currentPlanetId);
            }
        }

        /// <summary>
        /// Gets the current campaign ID.
        /// </summary>
        public string GetCurrentCampaignId()
        {
            return currentCampaignId;
        }

        /// <summary>
        /// Gets the current planet ID.
        /// </summary>
        public int GetCurrentPlanetId()
        {
            return currentPlanetId;
        }
    }
}
