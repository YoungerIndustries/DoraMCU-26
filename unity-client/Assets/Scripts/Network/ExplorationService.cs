using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles exploration API calls: sector scan, discovered systems, exploration reports.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class ExplorationService
    {
        /// <summary>
        /// Scan sector callback: (success, error, systemsFound)
        /// </summary>
        public delegate void ScanCallback(bool success, string error, int systemsFound);

        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        public ExplorationService(string serverUrl, Func<string> getJwtToken,
            Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Scan a sector to reveal solar systems.
        /// Calls GET /api/v1/galaxy-3d/sector/{row}/{col}
        /// </summary>
        public void ScanSector(int row, int col, ScanCallback callback)
        {
            coroutineRunner(ScanSectorCoroutine(row, col, callback));
        }

        private IEnumerator ScanSectorCoroutine(int row, int col, ScanCallback callback)
        {
            string url = $"{serverUrl}/api/v1/galaxy-3d/sector/{row}/{col}";

            using (var request = UnityWebRequest.Get(url))
            {
                string token = getJwtToken();
                if (!string.IsNullOrEmpty(token))
                {
                    request.SetRequestHeader("Authorization", $"Bearer {token}");
                }

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    string responseText = request.downloadHandler.text;
                    int systemsFound = 0;
                    string solarSystemMarker = "\"solarSystem\"";
                    int index = 0;
                    while ((index = responseText.IndexOf(solarSystemMarker, index)) != -1)
                    {
                        systemsFound++;
                        index += solarSystemMarker.Length;
                    }

                    Debug.Log($"[ExplorationService] Sector {row},{col} scanned: {systemsFound} systems found");
                    callback?.Invoke(true, null, systemsFound);
                }
                else
                {
                    Debug.LogError($"[ExplorationService] Sector scan failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Sector scan failed", 0);
                }
            }
        }

        /// <summary>
        /// Fetch player resources.
        /// Calls GET /api/v1/game/player/me
        /// </summary>
        public void FetchResources(Action<Dictionary<string, long>, bool> callback)
        {
            coroutineRunner(FetchResourcesCoroutine(callback));
        }

        private IEnumerator FetchResourcesCoroutine(Action<Dictionary<string, long>, bool> callback)
        {
            string url = $"{serverUrl}/api/v1/game/player/me";

            using (var request = UnityWebRequest.Get(url))
            {
                string token = getJwtToken();
                if (!string.IsNullOrEmpty(token))
                {
                    request.SetRequestHeader("Authorization", $"Bearer {token}");
                }

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var player = JsonUtility.FromJson<NetworkManager.PlayerInfo>(request.downloadHandler.text);
                    if (player != null && player.resources != null)
                    {
                        callback?.Invoke(player.resources, true);
                    }
                    else
                    {
                        callback?.Invoke(new Dictionary<string, long>(), true);
                    }
                }
                else
                {
                    Debug.LogError($"[ExplorationService] Fetch resources failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Fetch player XP and level info.
        /// Calls GET /api/v1/game/player/me/stats
        /// </summary>
        public void FetchPlayerStats(Action<string, bool> callback)
        {
            coroutineRunner(FetchPlayerStatsCoroutine(callback));
        }

        private IEnumerator FetchPlayerStatsCoroutine(Action<string, bool> callback)
        {
            string url = $"{serverUrl}/api/v1/game/player/me/stats";

            using (var request = UnityWebRequest.Get(url))
            {
                string token = getJwtToken();
                if (!string.IsNullOrEmpty(token))
                {
                    request.SetRequestHeader("Authorization", $"Bearer {token}");
                }

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    callback?.Invoke(request.downloadHandler.text, true);
                }
                else
                {
                    Debug.LogError($"[ExplorationService] Fetch player stats failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }
    }
}
