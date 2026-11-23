using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Central network coordinator — thin facade that delegates to focused services.
    /// Handles connection state, shared infrastructure, and DTOs.
    /// </summary>
    public class NetworkManager : Singleton<NetworkManager>
    {
        [Header("Server Configuration")]
        [SerializeField] private float reconnectDelay = 5f;
        [SerializeField] private int maxReconnectAttempts = 10;

        [Header("AI Admin Configuration")]
        [SerializeField] private float aiAdminPollInterval = 30f;

        private string sessionId;
        private string playerId;
        private bool isConnected;
        private int reconnectAttempts;
        private Coroutine reconnectCoroutine;
        private Coroutine aiAdminPollCoroutine;
        private readonly Queue<Action<UnityWebRequest>> requestQueue = new();
        private bool isProcessingQueue;

        // Services
        private AuthService authService;
        private OnboardingService onboardingService;
        private ExplorationService explorationService;
        private ShipBuildService shipBuildService;
        private CombatService combatService;
        private SaveLoadService saveLoadService;
        private GalaxyService galaxyService;

        // Events
        public event Action<string> OnConnected;
        public event Action OnDisconnected;
        public event Action<string, GameState> OnStateSync;
        public event Action<string, Dictionary<string, object>> OnEvent;
        public event Action<string> OnError;
        public event Action<List<AiAdminData>> OnAiAdminsFetched;

        public string SessionId => sessionId;
        public string PlayerId => playerId;
        public bool IsConnected => isConnected;
        public string ServerUrl => GetServerUrl();

        // Auth state delegates (forwarded from AuthService)
        public string JwtToken => authService?.JwtToken;
        public string LoggedInUsername => authService?.LoggedInUsername;
        public string PlayerPin => authService?.PlayerPin;
        public string EmpireName => authService?.EmpireName;
        public long CorporationId => authService?.CorporationId ?? 0;
        public string CorporationName => authService?.CorporationName;
        public string CorporationTicker => authService?.CorporationTicker;
        public bool IsAuthenticated => authService?.IsAuthenticated ?? false;

        /// <summary>
        /// Get the server URL from ServerConfig if available, otherwise use the default.
        /// </summary>
        private string GetServerUrl()
        {
            var sc = ServerConfig.Instance;
            if (sc != null && !string.IsNullOrEmpty(sc.ServerUrl))
            {
                return sc.ServerUrl;
            }
            return "http://192.168.1.111:8080";
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            string url = GetServerUrl();
            Func<IEnumerator, Coroutine> runner = StartCoroutine;

            authService = new AuthService(url, runner);
            onboardingService = new OnboardingService(url, () => authService?.JwtToken, () => authService?.LoggedInUsername, runner);
            explorationService = new ExplorationService(url, () => authService?.JwtToken, runner);
            shipBuildService = new ShipBuildService(url, () => authService?.JwtToken, runner);
            combatService = new CombatService(url, () => authService?.JwtToken, runner);
            saveLoadService = new SaveLoadService(url, () => authService?.JwtToken, runner);
            galaxyService = new GalaxyService(url, () => authService?.JwtToken, runner, _ => { });
            galaxyService.OnAiAdminsFetched += (admins) => OnAiAdminsFetched?.Invoke(admins);
        }

        void Start()
        {
            // Don't auto-connect on start; wait for login
        }

        void OnDestroy()
        {
            Disconnect();
            StopAiAdminPolling();
        }

        /// <summary>
        /// Set the current galaxy name for AI admin queries.
        /// </summary>
        public void SetCurrentGalaxy(string galaxyName)
        {
            galaxyService?.SetCurrentGalaxy(galaxyName);
        }

        /// <summary>
        /// Start polling for AI admin positions.
        /// </summary>
        public void StartAiAdminPolling()
        {
            galaxyService?.StartAiAdminPolling(aiAdminPollInterval);
        }

        /// <summary>
        /// Stop polling for AI admin positions.
        /// </summary>
        public void StopAiAdminPolling()
        {
            // Handled by galaxy service
        }

        /// <summary>
        /// Fetch AI admin positions for a specific galaxy.
        /// </summary>
        public void FetchAiAdminsForGalaxy(string galaxyName, Action<List<AiAdminData>> callback)
        {
            galaxyService?.FetchAiAdminsForGalaxy(galaxyName, callback);
        }

        /// <summary>
        /// Connect to the game server (ping check only, no auth required).
        /// </summary>
        public void Connect()
        {
            if (isConnected) return;
            StartCoroutine(ConnectCoroutine());
        }

        #region Auth Delegation

        /// <summary>
        /// Login callback: (token, username, pin, empireName, corporationId, corporationName, success, error)
        /// </summary>
        public delegate void AuthCallback(string token, string username, string pin, string empireName,
            long corporationId, string corporationName, bool success, string error);

        public void Login(string username, string password, AuthCallback callback)
        {
            authService?.Login(username, password, callback);
        }

        public void Register(string username, string password, AuthCallback callback)
        {
            authService?.Register(username, password, callback);
        }

        public void DevLogin(AuthCallback callback)
        {
            authService?.DevLogin(callback);
        }

        public void Logout()
        {
            authService?.Logout();
            isConnected = false;
        }

        #endregion

        #region Onboarding Delegation

        /// <summary>
        /// Character creation callback: (success, error, characterId)
        /// </summary>
        public delegate void CharacterCallback(bool success, string error, long? characterId);

        public void FetchOnboardingConfig(Action<bool, bool, string> callback)
        {
            onboardingService?.FetchOnboardingConfig(callback);
        }

        public void CreateCharacter(string characterName, int age, string occupation, CharacterCallback callback)
        {
            onboardingService?.CreateCharacter(characterName, age, occupation, callback);
        }

        public void FetchEarthLanding(Action<bool, string> callback)
        {
            onboardingService?.FetchEarthLanding(callback);
        }

        public void FetchCurrentUser(Action<long?, string> callback)
        {
            onboardingService?.FetchCurrentUser(callback);
        }

        #endregion

        #region Exploration Delegation

        /// <summary>
        /// Scan sector callback: (success, error, systemsFound)
        /// </summary>
        public delegate void ScanCallback(bool success, string error, int systemsFound);

        public void ScanSector(int row, int col, ScanCallback callback)
        {
            explorationService?.ScanSector(row, col, callback);
        }

        public void FetchResources(Action<Dictionary<string, long>, bool> callback)
        {
            explorationService?.FetchResources(callback);
        }

        public void FetchPlayerStats(Action<string, bool> callback)
        {
            explorationService?.FetchPlayerStats(callback);
        }

        #endregion

        #region Ship Build Delegation

        public void BuildShip(string shipType, List<string> modules, Action<bool, string> callback)
        {
            shipBuildService?.BuildShip(shipType, modules, callback);
        }

        public void FetchShipDesigns(Action<string, bool> callback)
        {
            shipBuildService?.FetchShipDesigns(callback);
        }

        #endregion

        #region Combat Delegation

        public void StartBattle(long fleetId, long targetId, Action<bool, string> callback)
        {
            combatService?.StartBattle(fleetId, targetId, callback);
        }

        public void ResolveCombat(string action, Action<string, bool> callback)
        {
            combatService?.ResolveCombat(action, callback);
        }

        #endregion

        #region Save/Load Delegation

        public void SaveGame(Action<bool, string> callback)
        {
            saveLoadService?.SaveGame(callback);
        }

        public void LoadGame(string sessionId, Action<GameState, bool> callback)
        {
            saveLoadService?.LoadGame(sessionId, callback);
        }

        #endregion

        #region Shared Infrastructure

        /// <summary>
        /// Send an authenticated GET request with JWT token.
        /// </summary>
        public IEnumerator AuthenticatedGet(string endpoint, Action<string, bool> callback)
        {
            string url = $"{GetServerUrl()}{endpoint}";
            string token = authService?.JwtToken;
            using (var request = UnityWebRequest.Get(url))
            {
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
                    Debug.LogError($"[NetworkManager] Authenticated GET {endpoint} failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Send an authenticated POST request with JWT token.
        /// </summary>
        public IEnumerator AuthenticatedPost(string endpoint, string jsonBody, Action<string, bool> callback)
        {
            string url = $"{GetServerUrl()}{endpoint}";
            string token = authService?.JwtToken;
            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(jsonBody);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

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
                    Debug.LogError($"[NetworkManager] Authenticated POST {endpoint} failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Disconnect from the server.
        /// </summary>
        public void Disconnect()
        {
            isConnected = false;
            sessionId = null;
            playerId = null;

            if (reconnectCoroutine != null)
            {
                StopCoroutine(reconnectCoroutine);
                reconnectCoroutine = null;
            }

            OnDisconnected?.Invoke();
        }

        /// <summary>
        /// Send a REST API request to the server.
        /// </summary>
        public void SendRequest(string endpoint, Action<UnityWebRequest> callback)
        {
            requestQueue.Enqueue(callback);
            if (!isProcessingQueue)
            {
                ProcessRequestQueue();
            }
        }

        /// <summary>
        /// GET request helper.
        /// </summary>
        public IEnumerator GetRequest(string endpoint, Action<string, bool> callback)
        {
            string url = $"{GetServerUrl()}/api/v1/game{endpoint}";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    callback?.Invoke(request.downloadHandler.text, true);
                }
                else
                {
                    Debug.LogError($"GET {endpoint} failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// POST request helper.
        /// </summary>
        public IEnumerator PostRequest(string endpoint, Dictionary<string, string> parameters, Action<string, bool> callback)
        {
            string url = $"{GetServerUrl()}/api/v1/game{endpoint}";
            if (parameters != null && parameters.Count > 0)
            {
                var queryBuilder = new StringBuilder();
                bool first = true;
                foreach (var kvp in parameters)
                {
                    if (!first) queryBuilder.Append("&");
                    queryBuilder.Append($"{Uri.EscapeDataString(kvp.Key)}={Uri.EscapeDataString(kvp.Value)}");
                    first = false;
                }
                url += $"?{queryBuilder}";
            }

            using (var request = new UnityWebRequest(url, "POST"))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    callback?.Invoke(request.downloadHandler.text, true);
                }
                else
                {
                    Debug.LogError($"POST {endpoint} failed: {request.error}");
                    callback?.Invoke(null, false);
                }
            }
        }

        /// <summary>
        /// Fetch the full game state from the server.
        /// </summary>
        public IEnumerator FetchGameState(Action<GameState> callback)
        {
            var state = new GameState();

            string tickJson = null;
            var tickRequest = GetRequest("/tick", (json, success) => tickJson = json);
            yield return StartCoroutine(tickRequest);
            if (!string.IsNullOrEmpty(tickJson))
            {
                state.Tick = JsonUtility.FromJson<TickInfo>(tickJson);
            }

            string playerJson = null;
            var playerRequest = GetRequest("/player/me", (json, success) => playerJson = json);
            yield return StartCoroutine(playerRequest);
            if (!string.IsNullOrEmpty(playerJson))
            {
                state.Player = JsonUtility.FromJson<PlayerInfo>(playerJson);
            }

            string galaxyJson = null;
            var galaxyRequest = GetRequest("/galaxy", (json, success) => galaxyJson = json);
            yield return StartCoroutine(galaxyRequest);
            if (!string.IsNullOrEmpty(galaxyJson))
            {
                state.Galaxy = JsonUtility.FromJson<GalaxyInfo>(galaxyJson);
            }

            string charsJson = null;
            var charsRequest = GetRequest("/characters", (json, success) => charsJson = json);
            yield return StartCoroutine(charsRequest);

            callback?.Invoke(state);
        }

        private IEnumerator ConnectCoroutine()
        {
            string url = $"{GetServerUrl()}/api/v1/mmog/connect?clientType=native";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    isConnected = true;
                    reconnectAttempts = 0;
                    OnConnected?.Invoke(sessionId);
                    Debug.Log($"[NetworkManager] Connected to server");
                }
                else
                {
                    Debug.LogError($"[NetworkManager] Connection failed: {request.error}");
                    OnError?.Invoke(request.error);
                    ScheduleReconnect();
                }
            }
        }

        private void ScheduleReconnect()
        {
            if (reconnectAttempts >= maxReconnectAttempts)
            {
                Debug.LogError("[NetworkManager] Max reconnect attempts reached");
                return;
            }

            reconnectAttempts++;
            reconnectCoroutine = StartCoroutine(ReconnectWait());
        }

        private IEnumerator ReconnectWait()
        {
            yield return new WaitForSeconds(reconnectDelay * reconnectAttempts);
            reconnectCoroutine = null;
            Connect();
        }

        private void ProcessRequestQueue()
        {
            isProcessingQueue = true;
            if (requestQueue.Count > 0)
            {
                var callback = requestQueue.Dequeue();
                callback?.Invoke(null);
            }
            isProcessingQueue = false;
        }

        #endregion

        #region DTOs

        [Serializable]
        public class TickInfo
        {
            public long tick;
            public bool running;
            public int entityCount;
            public int activeSessions;
            public int connectedPlayers;
        }

        [Serializable]
        public class PlayerInfo
        {
            public long id;
            public string email;
            public string government;
            public string ethos;
            public Dictionary<string, long> resources;
            public Dictionary<string, long> rawResources;
            public Dictionary<string, long> refinedResources;
            public Dictionary<string, long> modules;
            public EmpireInfo[] empires;
            public FleetInfo[] fleets;
            public PlanetInfo[] planets;
            public ShipInfo[] ships;
            public CharacterInfo commander;
        }

        [Serializable]
        public class EmpireInfo
        {
            public long id;
            public string name;
            public string government;
            public string ethos;
            public float integrity;
            public int militaryStrength;
            public int memberCount;
        }

        [Serializable]
        public class FleetInfo
        {
            public long id;
            public string name;
            public int sectorRow;
            public int sectorCol;
            public int subSectorRow;
            public int subSectorCol;
            public ShipInfo[] ships;
            public int combatPower;
            public float evasion;
            public int fuel;
            public string order;
        }

        [Serializable]
        public class PlanetInfo
        {
            public long id;
            public string name;
            public string type;
            public int size;
            public int atmosphere;
            public int temperature;
            public int hydrographics;
            public int population;
            public int government;
            public int lawLevel;
            public int starPosition;
            public float orbitRadius;
            public float orbitAngle;
            public float rotationPeriod;
            public float axialTilt;
            public PlanetStats stats;
            public PlanetFacilities facilities;
            public long? ownerId;
        }

        [Serializable]
        public class PlanetStats
        {
            public long population;
            public long food;
            public long industry;
            public long combat;
        }

        [Serializable]
        public class PlanetFacilities
        {
            public int shipyard;
            public int mine;
            public int factory;
            public int farm;
            public int port;
            public int communications;
        }

        [Serializable]
        public class ShipInfo
        {
            public long id;
            public string name;
            public string design;
            public int hull;
            public int drives;
            public int cargo;
            public int fuel;
            public ShipCombat combat;
            public int sectorRow;
            public int sectorCol;
            public int subSectorRow;
            public int subSectorCol;
        }

        [Serializable]
        public class ShipCombat
        {
            public int armor;
            public int weapons;
            public int shields;
        }

        [Serializable]
        public class CharacterInfo
        {
            public long id;
            public string name;
            public string species;
            public Dictionary<string, int> characteristics;
            public Dictionary<string, int> skills;
            public string[] careers;
            public string currentCareer;
            public string location;
            public string equipment;
            public string boonBane;
            public bool alive;
            public string status;
        }

        [Serializable]
        public class GalaxyInfo
        {
            public long id;
            public SectorInfo[] sectors;
            public int turn;
            public long timestamp;
        }

        [Serializable]
        public class SectorInfo
        {
            public int id;
            public int row;
            public int col;
            public SubSectorInfo[] subSectors;
            public long? controller;
            public string tradeType;
        }

        [Serializable]
        public class SubSectorInfo
        {
            public int id;
            public int row;
            public int col;
            public SolarSystemInfo solarSystem;
        }

        [Serializable]
        public class SolarSystemInfo
        {
            public long id;
            public StarInfo star;
            public PlanetInfo[] planets;
            public StationInfo[] stations;
            public bool explored;
        }

        [Serializable]
        public class StarInfo
        {
            public long id;
            public string spectralClass;
            public float mass;
            public float radius;
        }

        [Serializable]
        public class StationInfo
        {
            public long id;
            public string name;
            public string type;
            public int sectorRow;
            public int sectorCol;
            public int subSectorRow;
            public int subSectorCol;
            public int fuel;
            public int repair;
        }

        [Serializable]
        public class GameState
        {
            public TickInfo Tick;
            public PlayerInfo Player;
            public GalaxyInfo Galaxy;
        }

        #endregion
    }
}
