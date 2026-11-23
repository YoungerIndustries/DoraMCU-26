using System;
using System.Collections;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles combat API calls: start battle, resolve combat.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class CombatService
    {
        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        public CombatService(string serverUrl, Func<string> getJwtToken,
            Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Start a battle with an enemy fleet.
        /// Calls POST /api/v1/battle/start
        /// </summary>
        public void StartBattle(long fleetId, long targetId, Action<bool, string> callback)
        {
            coroutineRunner(StartBattleCoroutine(fleetId, targetId, callback));
        }

        private IEnumerator StartBattleCoroutine(long fleetId, long targetId, Action<bool, string> callback)
        {
            string url = $"{serverUrl}/api/v1/battle/start";
            string json = $"{{\"fleetId\":{fleetId},\"targetId\":{targetId}}}";

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
                    Debug.Log($"[CombatService] Battle started: fleet {fleetId} vs target {targetId}");
                    callback?.Invoke(true, null);
                }
                else
                {
                    Debug.LogError($"[CombatService] Battle start failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Battle start failed");
                }
            }
        }

        /// <summary>
        /// Resolve a combat turn.
        /// Calls POST /api/v1/battle/resolve
        /// </summary>
        public void ResolveCombat(string action, Action<string, bool> callback)
        {
            coroutineRunner(ResolveCombatCoroutine(action, callback));
        }

        private IEnumerator ResolveCombatCoroutine(string action, Action<string, bool> callback)
        {
            string url = $"{serverUrl}/api/v1/battle/resolve";
            string json = $"{{\"action\":\"{EscapeJson(action)}\"}}";

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
                    callback?.Invoke(request.downloadHandler.text, true);
                }
                else
                {
                    Debug.LogError($"[CombatService] Combat resolve failed: {request.error}");
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
