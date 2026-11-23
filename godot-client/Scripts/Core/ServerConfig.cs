using Godot;
using System;

namespace DoraMCU.Core
{
    /// <summary>
    /// Singleton that manages server configuration.
    /// Replaces Unity's PlayerPrefs with Godot's ConfigFile.
    /// </summary>
    public partial class ServerConfig : Node
    {
        public string ServerUrl { get; private set; } = "http://192.168.1.111:8080";
        public string ServerIp { get; private set; } = "192.168.1.111";
        public int ServerPort { get; private set; } = 8080;

        const string ConfigPath = "user://server_config.cfg";
        const string Section = "server";
        const string KeyUrl = "url";

        public override void _Ready()
        {
            LoadConfig();
        }

        /// <summary>
        /// Loads the server URL from the config file.
        /// Falls back to default if not found.
        /// </summary>
        public void LoadConfig()
        {
            var config = new ConfigFile();
            var error = config.Load(ConfigPath);
            if (error == Error.Ok && config.HasSectionKey(Section, KeyUrl))
            {
                ServerUrl = config.GetValue(Section, KeyUrl).ToString();
            }
            else
            {
                ServerUrl = "http://192.168.1.111:8080";
            }
            ParseServerInfo();
        }

        /// <summary>
        /// Saves the current server URL to the config file.
        /// </summary>
        public void SaveConfig()
        {
            var config = new ConfigFile();
            var error = config.Load(ConfigPath);
            if (error != Error.Ok)
            {
                config = new ConfigFile();
            }
            config.SetValue(Section, KeyUrl, ServerUrl);
            config.Save(ConfigPath);
            ParseServerInfo();
        }

        /// <summary>
        /// Sets a new server URL and persists it.
        /// </summary>
        public void SetServerUrl(string url)
        {
            if (string.IsNullOrWhiteSpace(url))
            {
                GD.PrintErr("Server URL cannot be empty");
                return;
            }
            if (!url.StartsWith("http://") && !url.StartsWith("https://"))
            {
                url = "http://" + url;
            }
            ServerUrl = url.TrimEnd('/');
            SaveConfig();
        }

        /// <summary>
        /// Returns the full API base URL for REST calls.
        /// </summary>
        public string GetApiBaseUrl()
        {
            return ServerUrl;
        }

        /// <summary>
        /// Parses the server URL to extract IP and port.
        /// </summary>
        void ParseServerInfo()
        {
            try
            {
                var uri = new Uri(ServerUrl);
                ServerIp = uri.Host;
                ServerPort = uri.Port;
            }
            catch (UriFormatException)
            {
                GD.PrintErr($"Invalid server URL: {ServerUrl}");
                ServerIp = "192.168.1.111";
                ServerPort = 8080;
            }
        }
    }
}
