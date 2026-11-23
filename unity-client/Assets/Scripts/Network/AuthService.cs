using System;
using System.Collections;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles all authentication API calls: login, register, dev-login, logout.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class AuthService
    {
        /// <summary>
        /// Login callback: (token, username, pin, empireName, corporationId, corporationName, success, error)
        /// </summary>
        public delegate void AuthCallback(string token, string username, string pin, string empireName,
            long corporationId, string corporationName, bool success, string error);

        private readonly string serverUrl;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;

        // Auth state
        private string jwtToken;
        private string loggedInUsername;
        private string playerPin;
        private string empireName;
        private long corporationId;
        private string corporationName;
        private string corporationTicker;

        public string JwtToken => jwtToken;
        public string LoggedInUsername => loggedInUsername;
        public string PlayerPin => playerPin;
        public string EmpireName => empireName;
        public long CorporationId => corporationId;
        public string CorporationName => corporationName;
        public string CorporationTicker => corporationTicker;
        public bool IsAuthenticated => !string.IsNullOrEmpty(jwtToken);

        /// <summary>
        /// Creates a new AuthService.
        /// </summary>
        /// <param name="serverUrl">Base URL of the game server</param>
        /// <param name="coroutineRunner">Function to start a coroutine (from NetworkManager)</param>
        public AuthService(string serverUrl, Func<IEnumerator, Coroutine> coroutineRunner)
        {
            this.serverUrl = serverUrl;
            this.coroutineRunner = coroutineRunner;
        }

        /// <summary>
        /// Login with username and password.
        /// Calls POST /api/v1/4x/auth/login
        /// </summary>
        public void Login(string username, string password, AuthCallback callback)
        {
            coroutineRunner(LoginCoroutine(username, password, callback));
        }

        /// <summary>
        /// Register a new account.
        /// Calls POST /api/v1/4x/auth/register
        /// </summary>
        public void Register(string username, string password, AuthCallback callback)
        {
            coroutineRunner(RegisterCoroutine(username, password, callback));
        }

        /// <summary>
        /// Dev login (bypass auth, for development only).
        /// Calls GET /api/v1/4x/auth/dev-login
        /// </summary>
        public void DevLogin(AuthCallback callback)
        {
            coroutineRunner(DevLoginCoroutine(callback));
        }

        /// <summary>
        /// Logout and clear stored credentials.
        /// </summary>
        public void Logout()
        {
            jwtToken = null;
            loggedInUsername = null;
            playerPin = null;
            empireName = null;
            corporationId = 0;
            corporationName = null;
            corporationTicker = null;
            Debug.Log("[AuthService] Logged out");
        }

        /// <summary>
        /// Login coroutine.
        /// </summary>
        private IEnumerator LoginCoroutine(string username, string password, AuthCallback callback)
        {
            string url = $"{serverUrl}/api/v1/4x/auth/login";
            string json = $"{{\"username\":\"{EscapeJson(username)}\",\"password\":\"{EscapeJson(password)}\"}}";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var response = JsonUtility.FromJson<LoginResponse>(request.downloadHandler.text);
                    if (response != null)
                    {
                        SetAuthState(response);
                        Debug.Log($"[AuthService] Login successful for {response.username}, corp: {corporationName}");
                        callback?.Invoke(response.token, response.username, response.pin, response.empireName,
                            response.corporationId, response.corporationName, true, null);
                    }
                    else
                    {
                        callback?.Invoke(null, null, null, null, 0, null, false, "Invalid server response");
                    }
                }
                else
                {
                    Debug.LogError($"[AuthService] Login failed: {request.error}");
                    callback?.Invoke(null, null, null, null, 0, null, false, request.error ?? "Connection failed");
                }
            }
        }

        /// <summary>
        /// Register coroutine.
        /// </summary>
        private IEnumerator RegisterCoroutine(string username, string password, AuthCallback callback)
        {
            string url = $"{serverUrl}/api/v1/4x/auth/register";
            string json = $"{{\"username\":\"{EscapeJson(username)}\",\"password\":\"{EscapeJson(password)}\"}}";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var response = JsonUtility.FromJson<LoginResponse>(request.downloadHandler.text);
                    if (response != null)
                    {
                        SetAuthState(response);
                        Debug.Log($"[AuthService] Registration successful for {response.username}, corp: {corporationName}");
                        callback?.Invoke(response.token, response.username, response.pin, response.empireName,
                            response.corporationId, response.corporationName, true, null);
                    }
                    else
                    {
                        callback?.Invoke(null, null, null, null, 0, null, false, "Invalid server response");
                    }
                }
                else
                {
                    Debug.LogError($"[AuthService] Registration failed: {request.error}");
                    callback?.Invoke(null, null, null, null, 0, null, false, request.error ?? "Registration failed");
                }
            }
        }

        /// <summary>
        /// Dev login coroutine.
        /// </summary>
        private IEnumerator DevLoginCoroutine(AuthCallback callback)
        {
            string url = $"{serverUrl}/api/v1/4x/auth/dev-login";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var response = JsonUtility.FromJson<LoginResponse>(request.downloadHandler.text);
                    if (response != null)
                    {
                        SetAuthState(response);
                        Debug.Log($"[AuthService] Dev login successful for {response.username}, corp: {corporationName}");
                        callback?.Invoke(response.token, response.username, response.pin, response.empireName,
                            response.corporationId, response.corporationName, true, null);
                    }
                    else
                    {
                        callback?.Invoke(null, null, null, null, 0, null, false, "Invalid server response");
                    }
                }
                else
                {
                    Debug.LogError($"[AuthService] Dev login failed: {request.error}");
                    callback?.Invoke(null, null, null, null, 0, null, false, request.error ?? "Connection failed");
                }
            }
        }

        /// <summary>
        /// Set auth state from a login response.
        /// </summary>
        private void SetAuthState(LoginResponse response)
        {
            jwtToken = response.token;
            loggedInUsername = response.username;
            playerPin = response.pin;
            empireName = response.empireName;
            corporationId = response.corporationId;
            corporationName = response.corporationName;
            corporationTicker = response.corporationTicker;
        }

        /// <summary>
        /// Escape special characters in JSON strings.
        /// </summary>
        private string EscapeJson(string value)
        {
            if (value == null) return "";
            return value.Replace("\\", "\\\\").Replace("\"", "\\\"").Replace("\n", "\\n")
                .Replace("\r", "\\r").Replace("\t", "\\t");
        }

        /// <summary>
        /// Login response DTO.
        /// </summary>
        [Serializable]
        public class LoginResponse
        {
            public string token;
            public string username;
            public string pin;
            public string empireName;
            public string governmentType;
            public string ethos;
            public bool admin;
            public long corporationId;
            public string corporationName;
            public string corporationTicker;
        }
    }
}
