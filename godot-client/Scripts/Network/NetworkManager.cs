using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Godot;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace DoraMCU.Network
{
    /// <summary>
    /// Manages network communication with the Spring Boot game server.
    /// Handles HTTP REST calls with JWT authentication.
    /// Godot port of Unity's NetworkManager using async/await and HttpClient.
    /// </summary>
    public partial class NetworkManager : Node
    {
        // Signals (replace Unity events)
        [Signal] public delegate void ConnectedEventHandler(string sessionId);
        [Signal] public delegate void DisconnectedEventHandler();
        [Signal] public delegate void ErrorEventHandler(string error);
        [Signal] public delegate void AiAdminsFetchedEventHandler();

        // Auth signals
        [Signal] public delegate void LoginSuccessEventHandler();
        [Signal] public delegate void LoginErrorEventHandler(string error);

        // Character signals
        [Signal] public delegate void CharacterCreatedEventHandler();
        [Signal] public delegate void CharacterCreationErrorEventHandler(string error);

        // Game signals
        [Signal] public delegate void ShipBuiltEventHandler();
        [Signal] public delegate void ShipBuildErrorEventHandler(string error);
        [Signal] public delegate void SectorScannedEventHandler(int row, int col, int systemsFound);
        [Signal] public delegate void SectorScanErrorEventHandler(string error);
        [Signal] public delegate void ResourcesFetchedEventHandler();
        [Signal] public delegate void ResourcesFetchErrorEventHandler(string error);
        [Signal] public delegate void GameSavedEventHandler();
        [Signal] public delegate void GameSaveErrorEventHandler(string error);
        [Signal] public delegate void GameLoadedEventHandler();
        [Signal] public delegate void GameLoadErrorEventHandler(string error);

        // Server configuration
        string _serverUrl = "http://192.168.1.111:8080";

        // Authentication state
        string _jwtToken;
        string _loggedInUsername;
        string _playerPin;
        string _empireName;
        long _corporationId;
        string _corporationName;
        string _corporationTicker;
        bool _isConnected;

        // HTTP client
        System.Net.Http.HttpClient _httpClient;

        // Properties
        public string JwtToken => _jwtToken;
        public string LoggedInUsername => _loggedInUsername;
        public string PlayerPin => _playerPin;
        public string EmpireName => _empireName;
        public long CorporationId => _corporationId;
        public string CorporationName => _corporationName;
        public string CorporationTicker => _corporationTicker;
        public bool IsAuthenticated => !string.IsNullOrEmpty(_jwtToken);
        public new bool IsConnected => _isConnected;

        public override void _Ready()
        {
            _httpClient = new System.Net.Http.HttpClient
            {
                Timeout = TimeSpan.FromSeconds(30)
            };
        }

        public override void _ExitTree()
        {
            _httpClient?.Dispose();
        }

        /// <summary>
        /// Get the server URL from ServerConfig if available.
        /// </summary>
        string GetServerUrl()
        {
            var sc = GetTree().Root.GetNodeOrNull("ServerConfig") as DoraMCU.Core.ServerConfig;
            if (sc != null && !string.IsNullOrEmpty(sc.ServerUrl))
            {
                return sc.ServerUrl;
            }
            return _serverUrl;
        }

        /// <summary>
        /// Create an HTTP request message with optional auth header.
        /// </summary>
        System.Net.Http.HttpRequestMessage CreateRequest(string method, string endpoint, string body = null)
        {
            var request = new System.Net.Http.HttpRequestMessage { Method = new HttpMethod(method) };
            request.RequestUri = new Uri(endpoint);
            if (!string.IsNullOrEmpty(_jwtToken))
            {
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _jwtToken);
            }
            if (body != null)
            {
                request.Content = new StringContent(body, Encoding.UTF8, "application/json");
            }
            return request;
        }

        /// <summary>
        /// Send an HTTP request and return the response content as a string.
        /// </summary>
        async Task<string> SendRequestAsync(string method, string endpoint, string body = null)
        {
            var request = CreateRequest(method, endpoint, body);
            var response = await _httpClient.SendAsync(request);
            response.EnsureSuccessStatusCode();
            return await response.Content.ReadAsStringAsync();
        }

        #region Authentication

        /// <summary>
        /// Login with username and password.
        /// Calls POST /api/v1/4x/auth/login
        /// </summary>
        public async Task<LoginResponse> LoginAsync(string username, string password)
        {
            string url = $"{GetServerUrl()}/api/v1/4x/auth/login";
            var payload = new { username = username, password = password };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                string responseText = await SendRequestAsync("POST", url, json);
                var response = JsonConvert.DeserializeObject<LoginResponse>(responseText);
                if (response != null)
                {
                    ApplyLoginResponse(response);
                    GD.Print($"[NetworkManager] Login successful for {response.username}, corp: {response.corporationName}");
                    EmitSignal(SignalName.LoginSuccess);
                    return response;
                }
                else
                {
                    EmitSignal(SignalName.LoginError, "Invalid server response");
                    return null;
                }
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Login failed: {ex.Message}");
                EmitSignal(SignalName.LoginError, ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Register a new account.
        /// Calls POST /api/v1/4x/auth/register
        /// </summary>
        public async Task<LoginResponse> RegisterAsync(string username, string password)
        {
            string url = $"{GetServerUrl()}/api/v1/4x/auth/register";
            var payload = new { username = username, password = password };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                string responseText = await SendRequestAsync("POST", url, json);
                var response = JsonConvert.DeserializeObject<LoginResponse>(responseText);
                if (response != null)
                {
                    ApplyLoginResponse(response);
                    GD.Print($"[NetworkManager] Registration successful for {response.username}, corp: {response.corporationName}");
                    EmitSignal(SignalName.LoginSuccess);
                    return response;
                }
                else
                {
                    EmitSignal(SignalName.LoginError, "Invalid server response");
                    return null;
                }
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Registration failed: {ex.Message}");
                EmitSignal(SignalName.LoginError, ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Dev login (bypass auth, for development only).
        /// Calls GET /api/v1/4x/auth/dev-login
        /// </summary>
        public async Task<LoginResponse> DevLoginAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/4x/auth/dev-login";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var response = JsonConvert.DeserializeObject<LoginResponse>(responseText);
                if (response != null)
                {
                    ApplyLoginResponse(response);
                    GD.Print($"[NetworkManager] Dev login successful for {response.username}, corp: {response.corporationName}");
                    EmitSignal(SignalName.LoginSuccess);
                    return response;
                }
                else
                {
                    EmitSignal(SignalName.LoginError, "Invalid server response");
                    return null;
                }
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Dev login failed: {ex.Message}");
                EmitSignal(SignalName.LoginError, ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Apply login response to local state.
        /// </summary>
        void ApplyLoginResponse(LoginResponse response)
        {
            _jwtToken = response.token;
            _loggedInUsername = response.username;
            _playerPin = response.pin;
            _empireName = response.empireName;
            _corporationId = response.corporationId;
            _corporationName = response.corporationName;
            _corporationTicker = response.corporationTicker;
            _isConnected = true;
        }

        /// <summary>
        /// Logout and clear stored credentials.
        /// </summary>
        public void Logout()
        {
            _jwtToken = null;
            _loggedInUsername = null;
            _playerPin = null;
            _empireName = null;
            _corporationId = 0;
            _corporationName = null;
            _corporationTicker = null;
            _isConnected = false;
            EmitSignal(SignalName.Disconnected);
            GD.Print("[NetworkManager] Logged out");
        }

        #endregion

        #region Character Creation & Onboarding

        /// <summary>
        /// Fetch onboarding config to check for existing characters.
        /// Calls GET /api/v1/onboarding/config?username=...
        /// Returns true if user has existing characters.
        /// </summary>
        public async Task<bool> FetchOnboardingConfigAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/onboarding/config";
            if (!string.IsNullOrEmpty(_loggedInUsername))
            {
                url += $"?username={Uri.EscapeDataString(_loggedInUsername)}";
            }

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var jObj = JObject.Parse(responseText);
                bool hasCharacters = jObj["hasCharacters"]?.Value<bool>() ?? false;
                GD.Print($"[NetworkManager] Onboarding config fetched, hasCharacters={hasCharacters}");
                return hasCharacters;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Onboarding config failed: {ex.Message}");
                return false;
            }
        }

        /// <summary>
        /// Create a character on the server.
        /// Calls POST /api/v1/onboarding/character
        /// </summary>
        public async Task<bool> CreateCharacterAsync(string characterName, int age, string occupation)
        {
            string url = $"{GetServerUrl()}/api/v1/onboarding/character";
            var payload = new
            {
                username = _loggedInUsername,
                characterName = characterName,
                age = age,
                occupation = occupation
            };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                await SendRequestAsync("POST", url, json);
                GD.Print($"[NetworkManager] Character created: {characterName}");
                EmitSignal(SignalName.CharacterCreated);
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Character creation failed: {ex.Message}");
                EmitSignal(SignalName.CharacterCreationError, ex.Message);
                return false;
            }
        }

        /// <summary>
        /// Fetch Earth 2026 landing data.
        /// Calls GET /api/v1/onboarding/earth?username=...
        /// </summary>
        public async Task<bool> FetchEarthLandingAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/onboarding/earth?username={Uri.EscapeDataString(_loggedInUsername ?? "")}";

            try
            {
                await SendRequestAsync("GET", url);
                GD.Print("[NetworkManager] Earth landing data fetched");
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Earth landing failed: {ex.Message}");
                return false;
            }
        }

        /// <summary>
        /// Fetch the current user's info (player ID, etc.).
        /// Calls GET /api/v1/4x/auth/me
        /// </summary>
        public async Task<long?> FetchCurrentUserAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/4x/auth/me";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var jObj = JObject.Parse(responseText);
                long? playerId = jObj["playerId"]?.Value<long>();
                GD.Print($"[NetworkManager] Current user fetched, playerId={playerId}");
                return playerId;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch current user failed: {ex.Message}");
                return null;
            }
        }

        #endregion

        #region Ship Building & Exploration

        /// <summary>
        /// Build a ship on the server.
        /// Calls POST /api/v1/game/ships
        /// </summary>
        public async Task<bool> BuildShipAsync(string shipType, List<string> modules)
        {
            string url = $"{GetServerUrl()}/api/v1/game/ships";
            var payload = new { type = shipType, modules = modules };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                await SendRequestAsync("POST", url, json);
                GD.Print($"[NetworkManager] Ship built: {shipType}");
                EmitSignal(SignalName.ShipBuilt);
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Ship build failed: {ex.Message}");
                EmitSignal(SignalName.ShipBuildError, ex.Message);
                return false;
            }
        }

        /// <summary>
        /// Scan a sector to reveal solar systems.
        /// Calls GET /api/v1/galaxy-3d/sector/{row}/{col}
        /// Returns number of systems found.
        /// </summary>
        public async Task<int> ScanSectorAsync(int row, int col)
        {
            string url = $"{GetServerUrl()}/api/v1/galaxy-3d/sector/{row}/{col}";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                int systemsFound = 0;
                string solarSystemMarker = "\"solarSystem\"";
                int index = 0;
                while ((index = responseText.IndexOf(solarSystemMarker, index)) != -1)
                {
                    systemsFound++;
                    index += solarSystemMarker.Length;
                }
                GD.Print($"[NetworkManager] Sector {row},{col} scanned: {systemsFound} systems found");
                EmitSignal(SignalName.SectorScanned, row, col, systemsFound);
                return systemsFound;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Sector scan failed: {ex.Message}");
                EmitSignal(SignalName.SectorScanError, ex.Message);
                return 0;
            }
        }

        /// <summary>
        /// Fetch player resources.
        /// Calls GET /api/v1/game/player/me
        /// </summary>
        public async Task<Dictionary<string, long>> FetchResourcesAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/game/player/me";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var player = JsonConvert.DeserializeObject<PlayerInfo>(responseText);
                if (player != null && player.resources != null)
                {
                    EmitSignal(SignalName.ResourcesFetched);
                    return player.resources;
                }
                var empty = new Dictionary<string, long>();
                EmitSignal(SignalName.ResourcesFetched);
                return empty;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch resources failed: {ex.Message}");
                EmitSignal(SignalName.ResourcesFetchError, ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Fetch ship designs from the server.
        /// Calls GET /api/v1/game/ships/designs
        /// </summary>
        public async Task<string> FetchShipDesignsAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/game/ships/designs";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                return responseText;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch ship designs failed: {ex.Message}");
                return null;
            }
        }

        #endregion

        #region Combat & Save/Load

        /// <summary>
        /// Start a battle with an enemy fleet.
        /// Calls POST /api/v1/battle/start
        /// </summary>
        public async Task<bool> StartBattleAsync(long fleetId, long targetId)
        {
            string url = $"{GetServerUrl()}/api/v1/battle/start";
            var payload = new { fleetId = fleetId, targetId = targetId };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                await SendRequestAsync("POST", url, json);
                GD.Print($"[NetworkManager] Battle started: fleet {fleetId} vs target {targetId}");
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Battle start failed: {ex.Message}");
                return false;
            }
        }

        /// <summary>
        /// Resolve a combat turn.
        /// Calls POST /api/v1/battle/resolve
        /// </summary>
        public async Task<string> ResolveCombatAsync(string action)
        {
            string url = $"{GetServerUrl()}/api/v1/battle/resolve";
            var payload = new { action = action };
            string json = JsonConvert.SerializeObject(payload);

            try
            {
                string responseText = await SendRequestAsync("POST", url, json);
                return responseText;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Combat resolve failed: {ex.Message}");
                return null;
            }
        }

        /// <summary>
        /// Save game state to server.
        /// Calls POST /api/v1/game/save
        /// </summary>
        public async Task<bool> SaveGameAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/game/save";

            try
            {
                await SendRequestAsync("POST", url);
                GD.Print("[NetworkManager] Game saved");
                EmitSignal(SignalName.GameSaved);
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Game save failed: {ex.Message}");
                EmitSignal(SignalName.GameSaveError, ex.Message);
                return false;
            }
        }

        /// <summary>
        /// Load game state from server.
        /// Calls GET /api/v1/game/load/{sessionId}
        /// </summary>
        public async Task<GameState> LoadGameAsync(string sessionId)
        {
            string url = $"{GetServerUrl()}/api/v1/game/load/{Uri.EscapeDataString(sessionId)}";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var state = JsonConvert.DeserializeObject<GameState>(responseText);
                GD.Print("[NetworkManager] Game loaded");
                EmitSignal(SignalName.GameLoaded);
                return state;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Game load failed: {ex.Message}");
                EmitSignal(SignalName.GameLoadError, ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Fetch player XP and level info.
        /// Calls GET /api/v1/game/player/me/stats
        /// </summary>
        public async Task<string> FetchPlayerStatsAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/game/player/me/stats";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                return responseText;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch player stats failed: {ex.Message}");
                return null;
            }
        }

        #endregion

        #region Galaxy & AI Admin

        /// <summary>
        /// Fetch AI admin positions for a specific galaxy.
        /// Calls GET /api/v1/galaxy-3d/galaxy/{name}/ai-admins
        /// </summary>
        public async Task<AiAdminData[]> FetchAiAdminsAsync(string galaxyName)
        {
            string url = $"{GetServerUrl()}/api/v1/galaxy-3d/galaxy/{Uri.EscapeDataString(galaxyName)}/ai-admins";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var response = JsonConvert.DeserializeObject<AiAdminResponse>(responseText);
                if (response != null && response.admins != null)
                {
                    EmitSignal(SignalName.AiAdminsFetched);
                    return response.admins;
                }
                var empty = Array.Empty<AiAdminData>();
                EmitSignal(SignalName.AiAdminsFetched);
                return empty;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch AI admins failed: {ex.Message}");
                return Array.Empty<AiAdminData>();
            }
        }

        /// <summary>
        /// Fetch the full galaxy 3D data.
        /// Calls GET /api/v1/galaxy-3d/galaxy/{name}
        /// </summary>
        public async Task<GalaxyInfo> FetchGalaxy3DAsync(string galaxyName)
        {
            string url = $"{GetServerUrl()}/api/v1/galaxy-3d/galaxy/{Uri.EscapeDataString(galaxyName)}";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var galaxy = JsonConvert.DeserializeObject<GalaxyInfo>(responseText);
                return galaxy;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch galaxy 3D failed: {ex.Message}");
                return null;
            }
        }

        /// <summary>
        /// Fetch a list of available galaxies.
        /// Calls GET /api/v1/galaxy-3d/galaxies
        /// </summary>
        public async Task<List<GalaxySummary>> FetchGalaxiesAsync()
        {
            string url = $"{GetServerUrl()}/api/v1/galaxy-3d/galaxies";

            try
            {
                string responseText = await SendRequestAsync("GET", url);
                var galaxies = JsonConvert.DeserializeObject<List<GalaxySummary>>(responseText);
                return galaxies ?? new List<GalaxySummary>();
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Fetch galaxies failed: {ex.Message}");
                return new List<GalaxySummary>();
            }
        }

        #endregion

        #region Generic Authenticated Requests

        /// <summary>
        /// Send an authenticated GET request.
        /// </summary>
        public async Task<string> AuthenticatedGetAsync(string endpoint)
        {
            string url = $"{GetServerUrl()}{endpoint}";
            return await SendRequestAsync("GET", url);
        }

        /// <summary>
        /// Send an authenticated POST request.
        /// </summary>
        public async Task<string> AuthenticatedPostAsync(string endpoint, string jsonBody)
        {
            string url = $"{GetServerUrl()}{endpoint}";
            return await SendRequestAsync("POST", url, jsonBody);
        }

        /// <summary>
        /// Ping the server to check connectivity.
        /// </summary>
        public async Task<bool> PingServerAsync()
        {
            try
            {
                string url = $"{GetServerUrl()}/api/v1/mmog/connect?clientType=native";
                await SendRequestAsync("GET", url);
                _isConnected = true;
                EmitSignal(SignalName.Connected, "");
                GD.Print("[NetworkManager] Server ping successful");
                return true;
            }
            catch (Exception ex)
            {
                GD.PrintErr($"[NetworkManager] Server ping failed: {ex.Message}");
                EmitSignal(SignalName.Error, ex.Message);
                return false;
            }
        }

        #endregion

        #region DTOs

        /// <summary>
        /// Login response DTO.
        /// </summary>
        public class LoginResponse
        {
            [JsonProperty("token")] public string token { get; set; }
            [JsonProperty("username")] public string username { get; set; }
            [JsonProperty("pin")] public string pin { get; set; }
            [JsonProperty("empireName")] public string empireName { get; set; }
            [JsonProperty("governmentType")] public string governmentType { get; set; }
            [JsonProperty("ethos")] public string ethos { get; set; }
            [JsonProperty("admin")] public bool admin { get; set; }
            [JsonProperty("corporationId")] public long corporationId { get; set; }
            [JsonProperty("corporationName")] public string corporationName { get; set; }
            [JsonProperty("corporationTicker")] public string corporationTicker { get; set; }
        }

        /// <summary>
        /// Game state container.
        /// </summary>
        public class GameState
        {
            [JsonProperty("tick")] public TickInfo Tick { get; set; }
            [JsonProperty("player")] public PlayerInfo Player { get; set; }
            [JsonProperty("galaxy")] public GalaxyInfo Galaxy { get; set; }
        }

        /// <summary>
        /// Server tick info.
        /// </summary>
        public class TickInfo
        {
            [JsonProperty("tick")] public long tick { get; set; }
            [JsonProperty("running")] public bool running { get; set; }
            [JsonProperty("entityCount")] public int entityCount { get; set; }
            [JsonProperty("activeSessions")] public int activeSessions { get; set; }
            [JsonProperty("connectedPlayers")] public int connectedPlayers { get; set; }
        }

        /// <summary>
        /// Player info with resources and entities.
        /// </summary>
        public class PlayerInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("email")] public string email { get; set; }
            [JsonProperty("government")] public string government { get; set; }
            [JsonProperty("ethos")] public string ethos { get; set; }
            [JsonProperty("resources")] public Dictionary<string, long> resources { get; set; }
            [JsonProperty("rawResources")] public Dictionary<string, long> rawResources { get; set; }
            [JsonProperty("refinedResources")] public Dictionary<string, long> refinedResources { get; set; }
            [JsonProperty("modules")] public Dictionary<string, long> modules { get; set; }
            [JsonProperty("empires")] public EmpireInfo[] empires { get; set; }
            [JsonProperty("fleets")] public FleetInfo[] fleets { get; set; }
            [JsonProperty("planets")] public PlanetInfo[] planets { get; set; }
            [JsonProperty("ships")] public ShipInfo[] ships { get; set; }
            [JsonProperty("commander")] public CharacterInfo commander { get; set; }
        }

        /// <summary>
        /// Empire info.
        /// </summary>
        public class EmpireInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("government")] public string government { get; set; }
            [JsonProperty("ethos")] public string ethos { get; set; }
            [JsonProperty("integrity")] public float integrity { get; set; }
            [JsonProperty("militaryStrength")] public int militaryStrength { get; set; }
            [JsonProperty("memberCount")] public int memberCount { get; set; }
        }

        /// <summary>
        /// Fleet info.
        /// </summary>
        public class FleetInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("sectorRow")] public int sectorRow { get; set; }
            [JsonProperty("sectorCol")] public int sectorCol { get; set; }
            [JsonProperty("subSectorRow")] public int subSectorRow { get; set; }
            [JsonProperty("subSectorCol")] public int subSectorCol { get; set; }
            [JsonProperty("ships")] public ShipInfo[] ships { get; set; }
            [JsonProperty("combatPower")] public int combatPower { get; set; }
            [JsonProperty("evasion")] public float evasion { get; set; }
            [JsonProperty("fuel")] public int fuel { get; set; }
            [JsonProperty("order")] public string order { get; set; }
        }

        /// <summary>
        /// Planet info.
        /// </summary>
        public class PlanetInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("type")] public string type { get; set; }
            [JsonProperty("size")] public int size { get; set; }
            [JsonProperty("atmosphere")] public int atmosphere { get; set; }
            [JsonProperty("temperature")] public int temperature { get; set; }
            [JsonProperty("hydrographics")] public int hydrographics { get; set; }
            [JsonProperty("population")] public int population { get; set; }
            [JsonProperty("government")] public int government { get; set; }
            [JsonProperty("lawLevel")] public int lawLevel { get; set; }
            [JsonProperty("starPosition")] public int starPosition { get; set; }
            [JsonProperty("orbitRadius")] public float orbitRadius { get; set; }
            [JsonProperty("orbitAngle")] public float orbitAngle { get; set; }
            [JsonProperty("rotationPeriod")] public float rotationPeriod { get; set; }
            [JsonProperty("axialTilt")] public float axialTilt { get; set; }
            [JsonProperty("stats")] public PlanetStats stats { get; set; }
            [JsonProperty("facilities")] public PlanetFacilities facilities { get; set; }
            [JsonProperty("ownerId")] public long? ownerId { get; set; }
        }

        /// <summary>
        /// Planet stats.
        /// </summary>
        public class PlanetStats
        {
            [JsonProperty("population")] public long population { get; set; }
            [JsonProperty("food")] public long food { get; set; }
            [JsonProperty("industry")] public long industry { get; set; }
            [JsonProperty("combat")] public long combat { get; set; }
        }

        /// <summary>
        /// Planet facilities.
        /// </summary>
        public class PlanetFacilities
        {
            [JsonProperty("shipyard")] public int shipyard { get; set; }
            [JsonProperty("mine")] public int mine { get; set; }
            [JsonProperty("factory")] public int factory { get; set; }
            [JsonProperty("farm")] public int farm { get; set; }
            [JsonProperty("port")] public int port { get; set; }
            [JsonProperty("communications")] public int communications { get; set; }
        }

        /// <summary>
        /// Ship info.
        /// </summary>
        public class ShipInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("design")] public string design { get; set; }
            [JsonProperty("hull")] public int hull { get; set; }
            [JsonProperty("drives")] public int drives { get; set; }
            [JsonProperty("cargo")] public int cargo { get; set; }
            [JsonProperty("fuel")] public int fuel { get; set; }
            [JsonProperty("combat")] public ShipCombat combat { get; set; }
            [JsonProperty("sectorRow")] public int sectorRow { get; set; }
            [JsonProperty("sectorCol")] public int sectorCol { get; set; }
            [JsonProperty("subSectorRow")] public int subSectorRow { get; set; }
            [JsonProperty("subSectorCol")] public int subSectorCol { get; set; }
        }

        /// <summary>
        /// Ship combat stats.
        /// </summary>
        public class ShipCombat
        {
            [JsonProperty("armor")] public int armor { get; set; }
            [JsonProperty("weapons")] public int weapons { get; set; }
            [JsonProperty("shields")] public int shields { get; set; }
        }

        /// <summary>
        /// Character info.
        /// </summary>
        public class CharacterInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("species")] public string species { get; set; }
            [JsonProperty("characteristics")] public Dictionary<string, int> characteristics { get; set; }
            [JsonProperty("skills")] public Dictionary<string, int> skills { get; set; }
            [JsonProperty("careers")] public string[] careers { get; set; }
            [JsonProperty("currentCareer")] public string currentCareer { get; set; }
            [JsonProperty("location")] public string location { get; set; }
            [JsonProperty("equipment")] public string equipment { get; set; }
            [JsonProperty("boonBane")] public string boonBane { get; set; }
            [JsonProperty("alive")] public bool alive { get; set; }
            [JsonProperty("status")] public string status { get; set; }
        }

        /// <summary>
        /// Galaxy info with sectors.
        /// </summary>
        public class GalaxyInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("sectors")] public SectorInfo[] sectors { get; set; }
            [JsonProperty("turn")] public int turn { get; set; }
            [JsonProperty("timestamp")] public long timestamp { get; set; }
        }

        /// <summary>
        /// Galaxy summary (for list view).
        /// </summary>
        public class GalaxySummary
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("turn")] public int turn { get; set; }
            [JsonProperty("playerCount")] public int playerCount { get; set; }
        }

        /// <summary>
        /// Sector info with sub-sectors.
        /// </summary>
        public class SectorInfo
        {
            [JsonProperty("id")] public int id { get; set; }
            [JsonProperty("row")] public int row { get; set; }
            [JsonProperty("col")] public int col { get; set; }
            [JsonProperty("subSectors")] public SubSectorInfo[] subSectors { get; set; }
            [JsonProperty("controller")] public long? controller { get; set; }
            [JsonProperty("tradeType")] public string tradeType { get; set; }
        }

        /// <summary>
        /// Sub-sector info with solar system.
        /// </summary>
        public class SubSectorInfo
        {
            [JsonProperty("id")] public int id { get; set; }
            [JsonProperty("row")] public int row { get; set; }
            [JsonProperty("col")] public int col { get; set; }
            [JsonProperty("solarSystem")] public SolarSystemInfo solarSystem { get; set; }
        }

        /// <summary>
        /// Solar system info.
        /// </summary>
        public class SolarSystemInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("star")] public StarInfo star { get; set; }
            [JsonProperty("planets")] public PlanetInfo[] planets { get; set; }
            [JsonProperty("stations")] public StationInfo[] stations { get; set; }
            [JsonProperty("explored")] public bool explored { get; set; }
        }

        /// <summary>
        /// Star info.
        /// </summary>
        public class StarInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("spectralClass")] public string spectralClass { get; set; }
            [JsonProperty("mass")] public float mass { get; set; }
            [JsonProperty("radius")] public float radius { get; set; }
        }

        /// <summary>
        /// Station info.
        /// </summary>
        public class StationInfo
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("type")] public string type { get; set; }
            [JsonProperty("sectorRow")] public int sectorRow { get; set; }
            [JsonProperty("sectorCol")] public int sectorCol { get; set; }
            [JsonProperty("subSectorRow")] public int subSectorRow { get; set; }
            [JsonProperty("subSectorCol")] public int subSectorCol { get; set; }
            [JsonProperty("fuel")] public int fuel { get; set; }
            [JsonProperty("repair")] public int repair { get; set; }
        }

        /// <summary>
        /// AI Admin data.
        /// </summary>
        public class AiAdminData
        {
            [JsonProperty("id")] public long id { get; set; }
            [JsonProperty("name")] public string name { get; set; }
            [JsonProperty("positionX")] public float positionX { get; set; }
            [JsonProperty("positionY")] public float positionY { get; set; }
            [JsonProperty("positionZ")] public float positionZ { get; set; }
            [JsonProperty("sectorRow")] public int sectorRow { get; set; }
            [JsonProperty("sectorCol")] public int sectorCol { get; set; }
        }

        /// <summary>
        /// AI Admin response wrapper.
        /// </summary>
        public class AiAdminResponse
        {
            [JsonProperty("admins")] public AiAdminData[] admins { get; set; }
        }

        #endregion
    }
}
