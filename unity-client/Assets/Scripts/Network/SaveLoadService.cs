using System;
using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles save/load API calls: save game, load game.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class SaveLoadService
    {
        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        public SaveLoadService(string serverUrl, Func<string> getJwtToken,
            Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Save game state to server.
        /// Calls POST /api/v1/game/save
        /// </summary>
        public void SaveGame(Action<bool, string> callback)
        {
            coroutineRunner(SaveGameCoroutine(callback));
        }

        private IEnumerator SaveGameCoroutine(Action<bool, string> callback)
        {
            string url = $"{serverUrl}/api/v1/game/save";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerRaw(new byte[0]);
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
                    Debug.Log("[SaveLoadService] Game saved");
                    callback?.Invoke(true, null);
                }
                else
                {
                    Debug.LogError($"[SaveLoadService] Game save failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Game save failed");
                }
            }
        }

        /// <summary>
        /// Load game state from server.
        /// Calls GET /api/v1/game/load/{sessionId}
        /// </summary>
        public void LoadGame(string sessionId, Action<NetworkManager.GameState, bool> callback)
        {
            coroutineRunner(LoadGameCoroutine(sessionId, callback));
        }

        private IEnumerator LoadGameCoroutine(string sessionId, Action<NetworkManager.GameState, bool> callback)
        {
            string url = $"{serverUrl}/api/v1/game/load/{Uri.EscapeDataString(sessionId)}";

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
                    var state = JsonUtility.FromJson<NetworkManager.GameState>(request.downloadHandler.text);
                    Debug.Log("[SaveLoadService] Game loaded");
                    callback?.Invoke(state, true);
                }
                else
                {
                    Debug.LogError($"[SaveLoadService] Game load failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }
    }
}
