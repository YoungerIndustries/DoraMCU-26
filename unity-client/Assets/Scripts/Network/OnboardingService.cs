using System;
using System.Collections;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles character creation and onboarding API calls.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class OnboardingService
    {
        /// <summary>
        /// Character creation callback: (success, error, characterId)
        /// </summary>
        public delegate void CharacterCallback(bool success, string error, long? characterId);

        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<string> getLoggedInUsername;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        public OnboardingService(string serverUrl, Func<string> getJwtToken, Func<string> getLoggedInUsername,
            Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.getLoggedInUsername = getLoggedInUsername;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Fetch onboarding config to check for existing characters.
        /// Calls GET /api/v1/onboarding/config?username=...
        /// </summary>
        public void FetchOnboardingConfig(Action<bool, bool, string> callback)
        {
            // callback(success, hasCharacters, error)
            coroutineRunner(FetchOnboardingConfigCoroutine(callback));
        }

        private IEnumerator FetchOnboardingConfigCoroutine(Action<bool, bool, string> callback)
        {
            string url = $"{serverUrl}/api/v1/onboarding/config";
            string username = getLoggedInUsername();
            if (!string.IsNullOrEmpty(username))
            {
                url += $"?username={Uri.EscapeDataString(username)}";
            }

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
                    bool hasCharacters = false;
                    string responseText = request.downloadHandler.text;
                    if (responseText.Contains("\"hasCharacters\":true"))
                    {
                        hasCharacters = true;
                    }
                    Debug.Log($"[OnboardingService] Onboarding config fetched, hasCharacters={hasCharacters}");
                    callback?.Invoke(true, hasCharacters, null);
                }
                else
                {
                    Debug.LogError($"[OnboardingService] Onboarding config failed: {request.error}");
                    callback?.Invoke(false, false, request.error ?? "Connection failed");
                }
            }
        }

        /// <summary>
        /// Create a character on the server.
        /// Calls POST /api/v1/onboarding/character
        /// </summary>
        public void CreateCharacter(string characterName, int age, string occupation, CharacterCallback callback)
        {
            coroutineRunner(CreateCharacterCoroutine(characterName, age, occupation, callback));
        }

        private IEnumerator CreateCharacterCoroutine(string characterName, int age, string occupation, CharacterCallback callback)
        {
            string url = $"{serverUrl}/api/v1/onboarding/character";
            string username = getLoggedInUsername();
            string json = $"{{\"username\":\"{EscapeJson(username)}\",\"characterName\":\"{EscapeJson(characterName)}\",\"age\":{age},\"occupation\":\"{EscapeJson(occupation)}\"}}";

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
                    Debug.Log($"[OnboardingService] Character created: {characterName}");
                    callback?.Invoke(true, null, null);
                }
                else
                {
                    Debug.LogError($"[OnboardingService] Character creation failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Character creation failed", null);
                }
            }
        }

        /// <summary>
        /// Fetch Earth 2026 landing data.
        /// Calls GET /api/v1/onboarding/earth?username=...
        /// </summary>
        public void FetchEarthLanding(Action<bool, string> callback)
        {
            coroutineRunner(FetchEarthLandingCoroutine(callback));
        }

        private IEnumerator FetchEarthLandingCoroutine(Action<bool, string> callback)
        {
            string username = getLoggedInUsername() ?? "";
            string url = $"{serverUrl}/api/v1/onboarding/earth?username={Uri.EscapeDataString(username)}";

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
                    Debug.Log("[OnboardingService] Earth landing data fetched");
                    callback?.Invoke(true, null);
                }
                else
                {
                    Debug.LogError($"[OnboardingService] Earth landing failed: {request.error}");
                    callback?.Invoke(false, request.error ?? "Failed to fetch Earth landing data");
                }
            }
        }

        /// <summary>
        /// Fetch the current user's info (player ID, etc.).
        /// Calls GET /api/v1/4x/auth/me
        /// </summary>
        public void FetchCurrentUser(Action<long?, string> callback)
        {
            coroutineRunner(FetchCurrentUserCoroutine(callback));
        }

        private IEnumerator FetchCurrentUserCoroutine(Action<long?, string> callback)
        {
            string url = $"{serverUrl}/api/v1/4x/auth/me";

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
                    long? playerId = null;
                    int pidIndex = responseText.IndexOf("\"playerId\":");
                    if (pidIndex >= 0)
                    {
                        string after = responseText.Substring(pidIndex + 11);
                        int commaIndex = after.IndexOf(',');
                        int braceIndex = after.IndexOf('}');
                        int endIndex = commaIndex >= 0 && commaIndex < braceIndex ? commaIndex : braceIndex;
                        if (endIndex > 0)
                        {
                            string pidStr = after.Substring(0, endIndex).Trim();
                            long.TryParse(pidStr, out long pid);
                            playerId = pid;
                        }
                    }
                    Debug.Log($"[OnboardingService] Current user fetched, playerId={playerId}");
                    callback?.Invoke(playerId, null);
                }
                else
                {
                    Debug.LogError($"[OnboardingService] Fetch current user failed: {request.error}");
                    callback?.Invoke(null, request.error ?? "Failed to fetch user info");
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
