using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Manages network communication with the Spring Boot game server.
    /// Handles HTTP REST calls and SSE event streaming.
    /// </summary>
    public class NetworkManager : Singleton<NetworkManager>
    {
        [Header("Server Configuration")]
        [SerializeField] private string serverUrl = "http://localhost:8080";
        [SerializeField] private float reconnectDelay = 5f;
        [SerializeField] private int maxReconnectAttempts = 10;

        private string sessionId;
        private string playerId;
        private bool isConnected;
        private int reconnectAttempts;
        private Coroutine reconnectCoroutine;
        private readonly Queue<Action<UnityWebRequest>> requestQueue = new();
        private bool isProcessingQueue;

        // Events
        public event Action<string> OnConnected;
        public event Action OnDisconnected;
        public event Action<string, GameState> OnStateSync;
        public event Action<string, Dictionary<string, object>> OnEvent;
        public event Action<string> OnError;

        public string SessionId => sessionId;
        public string PlayerId => playerId;
        public bool IsConnected => isConnected;
        public string ServerUrl => serverUrl;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            Connect();
        }

        void OnDestroy()
        {
            Disconnect();
        }

        /// <summary>
        /// Connect to the game server.
        /// </summary>
        public void Connect()
        {
            if (isConnected) return;

            StartCoroutine(ConnectCoroutine());
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
            string url = $"{serverUrl}/api/v1/game{endpoint}";
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
            string url = $"{serverUrl}/api/v1/game{endpoint}";
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

            // Fetch tick
            string tickJson = null;
            var tickRequest = GetRequest("/tick", (json, success) => tickJson = json);
            yield return StartCoroutine(tickRequest);
            if (!string.IsNullOrEmpty(tickJson))
            {
                state.Tick = JsonUtility.FromJson<TickInfo>(tickJson);
            }

            // Fetch player
            string playerJson = null;
            var playerRequest = GetRequest("/player/me", (json, success) => playerJson = json);
            yield return StartCoroutine(playerRequest);
            if (!string.IsNullOrEmpty(playerJson))
            {
                state.Player = JsonUtility.FromJson<PlayerInfo>(playerJson);
            }

            // Fetch galaxy
            string galaxyJson = null;
            var galaxyRequest = GetRequest("/galaxy", (json, success) => galaxyJson = json);
            yield return StartCoroutine(galaxyRequest);
            if (!string.IsNullOrEmpty(galaxyJson))
            {
                state.Galaxy = JsonUtility.FromJson<GalaxyInfo>(galaxyJson);
            }

            // Fetch characters
            string charsJson = null;
            var charsRequest = GetRequest("/characters", (json, success) => charsJson = json);
            yield return StartCoroutine(charsRequest);

            callback?.Invoke(state);
        }

        private IEnumerator ConnectCoroutine()
        {
            // Connect to MMOG engine
            string url = $"{serverUrl}/api/v1/mmog/connect?clientType=native";
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
    }
}
