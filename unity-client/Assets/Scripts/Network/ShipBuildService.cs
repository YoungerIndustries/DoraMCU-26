using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles ship building API calls.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class ShipBuildService
    {
        /// <summary>
        /// Simple callback: (success, error)
        /// </summary>
        public delegate void SimpleCallback(bool success, string error);

        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        public ShipBuildService(string serverUrl, Func<string> getJwtToken,
            Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Build a ship on the server.
        /// Calls POST /api/v1/game/ships
        /// </summary>
        public void BuildShip(string shipType, List<string> modules, SimpleCallback callback)
        {
            coroutineRunner(BuildShipCoroutine(shipType, modules, callback));
        }

        private IEnumerator BuildShipCoroutine(string shipType, List<string> modules, SimpleCallback callback)
        {
            string url = $"{serverUrl}/api/v1/game/ships";

            string modulesJson = "[";
            for (int i = 0; i < modules.Count; i++)
            {
                if (i > 0) modulesJson += ",";
                modulesJson += $"\"{EscapeJson(modules[i])}\"";
            }
            modulesJson += "]";

            string json = $"{{\"type\":\"{EscapeJson(shipType)}\",\"modules\":{modulesJson}}}";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                string token = getJwtToken();
                if (!string.IsNullOrEmpty(token))
                {
                    request.SetRequestHeader("Authorization", $"Bearer {token}");
                }

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    Debug.Log($"[ShipBuildService] Ship built: {shipType}");
                    callback?.Invoke(true, null);
                }
                else
                {
                    Debug.LogError($"[ShipBuildService] Ship build failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Ship build failed");
                }
            }
        }

        /// <summary>
        /// Fetch ship designs from the server.
        /// Calls GET /api/v1/game/ships/designs
        /// </summary>
        public void FetchShipDesigns(Action<string, bool> callback)
        {
            coroutineRunner(FetchShipDesignsCoroutine(callback));
        }

        private IEnumerator FetchShipDesignsCoroutine(Action<string, bool> callback)
        {
            string url = $"{serverUrl}/api/v1/game/ships/designs";

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
                    Debug.LogError($"[ShipBuildService] Fetch ship designs failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        private string EscapeJson(string value)
        {
            if (value == null) return "";
            return value.Replace("\\", "\\\\").Replace("\"", "\\\"").Replace("\n", "\\n")
                .Replace("\r", "\\r").Replace("\t", "\\t");
        }
    }
}
