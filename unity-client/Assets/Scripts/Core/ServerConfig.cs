using UnityEngine;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Singleton that holds the configurable server URL.
    /// Default is the Pi server at 192.168.1.111:8080.
    /// Editable in Unity Inspector or via the settings panel.
    /// </summary>
    public class ServerConfig : Singleton<ServerConfig>
    {
        [Header("Server Configuration")]
        [SerializeField] private string serverUrl = "http://192.168.1.111:8080";
        [SerializeField] private int serverPort = 8080;

        private string fullServerUrl;

        public string ServerUrl
        {
            get => fullServerUrl;
            set
            {
                serverUrl = value;
                UpdateFullUrl();
            }
        }

        public int ServerPort
        {
            get => serverPort;
            set
            {
                serverPort = value;
                UpdateFullUrl();
            }
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
            UpdateFullUrl();
        }

        void Start()
        {
            // Load saved server URL from PlayerPrefs if available
            string savedUrl = PlayerPrefs.GetString("ServerUrl", "");
            if (!string.IsNullOrEmpty(savedUrl))
            {
                serverUrl = savedUrl;
            }
            int savedPort = PlayerPrefs.GetInt("ServerPort", 8080);
            if (savedPort > 0)
            {
                serverPort = savedPort;
            }
            UpdateFullUrl();
        }

        /// <summary>
        /// Save the current server configuration to PlayerPrefs.
        /// </summary>
        public void SaveConfiguration()
        {
            PlayerPrefs.SetString("ServerUrl", serverUrl);
            PlayerPrefs.SetInt("ServerPort", serverPort);
            PlayerPrefs.Save();
            Debug.Log($"[ServerConfig] Saved server URL: {fullServerUrl}");
        }

        /// <summary>
        /// Update the full server URL from the base URL and port.
        /// </summary>
        private void UpdateFullUrl()
        {
            // If serverUrl already contains the port, use it as-is
            if (serverUrl.Contains(":"))
            {
                fullServerUrl = serverUrl;
            }
            else
            {
                fullServerUrl = $"{serverUrl}:{serverPort}";
            }
        }

        /// <summary>
        /// Set the server URL from a full URL string (e.g., from settings panel).
        /// </summary>
        public void SetServerUrl(string url)
        {
            // Strip trailing slash if present
            if (url.EndsWith("/"))
            {
                url = url.Substring(0, url.Length - 1);
            }

            // Extract port if present
            int colonIndex = url.LastIndexOf(':');
            if (colonIndex > 0)
            {
                string portPart = url.Substring(colonIndex + 1);
                if (int.TryParse(portPart, out int port))
                {
                    serverPort = port;
                    serverUrl = url.Substring(0, colonIndex);
                }
                else
                {
                    serverUrl = url;
                }
            }
            else
            {
                serverUrl = url;
            }

            UpdateFullUrl();
            SaveConfiguration();
        }
    }
}
