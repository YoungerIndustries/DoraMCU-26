using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.Networking;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// JSON message sent from client to server containing player input.
    /// </summary>
    public class FpsInputMessage
    {
        [JsonPropertyName("type")]
        public string Type { get; set; } = "INPUT";

        [JsonPropertyName("sessionId")]
        public string SessionId { get; set; }

        [JsonPropertyName("playerId")]
        public string PlayerId { get; set; }

        [JsonPropertyName("timestamp")]
        public long Timestamp { get; set; }

        [JsonPropertyName("tick")]
        public int Tick { get; set; }

        [JsonPropertyName("move")]
        public Vector3Data Move { get; set; }

        [JsonPropertyName("aim")]
        public AimData Aim { get; set; }

        [JsonPropertyName("actions")]
        public List<string> Actions { get; set; } = new();
    }

    public class Vector3Data
    {
        [JsonPropertyName("x")] public float x;
        [JsonPropertyName("y")] public float y;
        [JsonPropertyName("z")] public float z;

        public Vector3Data() { }

        public Vector3Data(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static Vector3Data FromVector3(Vector3 v)
        {
            return new Vector3Data(v.x, v.y, v.z);
        }

        public Vector3 ToVector3()
        {
            return new Vector3(x, y, z);
        }
    }

    public class AimData
    {
        [JsonPropertyName("pitch")] public float pitch;
        [JsonPropertyName("yaw")] public float yaw;
    }

    /// <summary>
    /// JSON message received from server containing game state.
    /// </summary>
    public class FpsGameStateMessage
    {
        [JsonPropertyName("type")]
        public string Type { get; set; }

        [JsonPropertyName("tick")]
        public int Tick { get; set; }

        [JsonPropertyName("players")]
        public List<PlayerStateData> Players { get; set; } = new();

        [JsonPropertyName("events")]
        public List<GameStateEvent> Events { get; set; } = new();

        [JsonPropertyName("matchInfo")]
        public MatchInfoData MatchInfo { get; set; }

        [JsonPropertyName("capturePoints")]
        public List<CapturePointState> CapturePoints { get; set; }

        [JsonPropertyName("teamScores")]
        public Dictionary<int, int> TeamScores { get; set; }
    }

    /// <summary>
    /// Capture point state for CTP game mode.
    /// </summary>
    public class CapturePointState
    {
        [JsonPropertyName("id")]
        public string PointId { get; set; }

        [JsonPropertyName("ownerTeam")]
        public int OwnerTeam { get; set; }  // -1 = neutral, 0 = team 0, 1 = team 1

        [JsonPropertyName("progress")]
        public double CaptureProgress { get; set; }  // 0-100
    }

    public class PlayerStateData
    {
        [JsonPropertyName("id")] public string Id { get; set; }
        [JsonPropertyName("name")] public string Name { get; set; }
        [JsonPropertyName("position")] public Vector3Data Position { get; set; }
        [JsonPropertyName("rotation")] public Vector3Data Rotation { get; set; }
        [JsonPropertyName("health")] public int Health { get; set; }
        [JsonPropertyName("ammo")] public int Ammo { get; set; }
        [JsonPropertyName("reserveAmmo")] public int ReserveAmmo { get; set; }
        [JsonPropertyName("weaponIndex")] public int WeaponIndex { get; set; }
        [JsonPropertyName("score")] public int Score { get; set; }
        [JsonPropertyName("team")] public int Team { get; set; }
        [JsonPropertyName("alive")] public bool Alive { get; set; }
    }

    public class GameStateEvent
    {
        [JsonPropertyName("type")] public string Type { get; set; }
        [JsonPropertyName("playerId")] public string PlayerId { get; set; }
        [JsonPropertyName("targetId")] public string TargetId { get; set; }
        [JsonPropertyName("damage")] public int Damage { get; set; }
        [JsonPropertyName("message")] public string Message { get; set; }
    }

    public class MatchInfoData
    {
        [JsonPropertyName("mode")] public string Mode { get; set; }
        [JsonPropertyName("mapName")] public string MapName { get; set; }
        [JsonPropertyName("timeRemaining")] public int TimeRemaining { get; set; }
        [JsonPropertyName("scoreLimit")] public int ScoreLimit { get; set; }
    }

    /// <summary>
    /// WebSocket manager for FPS multiplayer communication.
    /// Connects to the game server, sends player input, and receives game state.
    /// </summary>
    public class FpsNetworkManager : Singleton<FpsNetworkManager>
    {
        [Header("Server Configuration")]
        [SerializeField] private string serverHost = "localhost";
        [SerializeField] private int serverPort = 8080;
        [SerializeField] private float reconnectDelay = 3f;
        [SerializeField] private int maxReconnectAttempts = 10;

        private WebSocket websocket;
        private string sessionId;
        private string playerId;
        private bool isConnected;
        private int reconnectAttempts;
        private Coroutine reconnectCoroutine;
        private Coroutine inputCoroutine;

        private int currentTick;
        private readonly Queue<FpsInputMessage> pendingInputs = new();

        // Player state cache
        private Dictionary<string, PlayerStateData> playerStates = new();
        private List<GameStateEvent> recentEvents = new();

        // Events
        public event Action<string> OnConnected;
        public event Action OnDisconnected;
        public event Action<FpsGameStateMessage> OnGameStateReceived;
        public event Action<GameStateEvent> OnGameEvent;
        public event Action<string> OnError;
        public event Action<string, string> OnChatMessage;

        public string SessionId => sessionId;
        public string PlayerId => playerId;
        public bool IsConnected => isConnected;
        public int CurrentTick => currentTick;
        public Dictionary<string, PlayerStateData> PlayerStates => playerStates;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void OnDestroy()
        {
            Disconnect();
        }

        /// <summary>
        /// Connect to the FPS game server via WebSocket.
        /// </summary>
        public void Connect(string authToken = null)
        {
            if (isConnected) return;

            string url = $"ws://{serverHost}:{serverPort}/ws/fps";
            if (!string.IsNullOrEmpty(authToken))
            {
                url += $"?token={Uri.EscapeDataString(authToken)}";
            }

            Debug.Log($"[FpsNetworkManager] Connecting to {url}");

            websocket = new WebSocket(url);
            websocket.OnMessage += OnWebSocketMessage;
            websocket.OnClose += OnWebSocketClose;
            websocket.OnError += OnWebSocketError;
            websocket.Connect();
        }

        /// <summary>
        /// Disconnect from the server.
        /// </summary>
        public void Disconnect()
        {
            isConnected = false;

            if (inputCoroutine != null)
            {
                StopCoroutine(inputCoroutine);
                inputCoroutine = null;
            }

            if (reconnectCoroutine != null)
            {
                StopCoroutine(reconnectCoroutine);
                reconnectCoroutine = null;
            }

            if (websocket != null)
            {
                websocket.Close();
                websocket = null;
            }

            OnDisconnected?.Invoke();
        }

        /// <summary>
        /// Send player input to the server.
        /// </summary>
        public void SendInput(Vector3 move, float pitch, float yaw, List<string> actions)
        {
            if (!isConnected || string.IsNullOrEmpty(sessionId)) return;

            var input = new FpsInputMessage
            {
                Type = "INPUT",
                SessionId = sessionId,
                PlayerId = playerId,
                Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds(),
                Tick = currentTick,
                Move = Vector3Data.FromVector3(move),
                Aim = new AimData { pitch = pitch, yaw = yaw },
                Actions = actions
            };

            SendJson(input);
        }

        /// <summary>
        /// Send a hit report to the server.
        /// </summary>
        public void SendHit(Vector3 hitPoint, string targetPlayerId = null)
        {
            if (!isConnected) return;

            var message = new Dictionary<string, object>
            {
                { "type", "HIT" },
                { "sessionId", sessionId },
                { "playerId", playerId },
                { "hitPoint", new { x = hitPoint.x, y = hitPoint.y, z = hitPoint.z } },
                { "targetId", targetPlayerId }
            };

            SendJson(message);
        }

        /// <summary>
        /// Send a projectile to the server.
        /// </summary>
        public void SendProjectile(Vector3 origin, Vector3 velocity)
        {
            if (!isConnected) return;

            var message = new Dictionary<string, object>
            {
                { "type", "PROJECTILE" },
                { "sessionId", sessionId },
                { "playerId", playerId },
                { "origin", new { x = origin.x, y = origin.y, z = origin.z } },
                { "velocity", new { x = velocity.x, y = velocity.y, z = velocity.z } }
            };

            SendJson(message);
        }

        /// <summary>
        /// Send ready signal to the server.
        /// </summary>
        public void SendReady()
        {
            if (!isConnected) return;

            var message = new Dictionary<string, object>
            {
                { "type", "READY" },
                { "sessionId", sessionId },
                { "playerId", playerId }
            };

            SendJson(message);
        }

        /// <summary>
        /// Send chat message.
        /// </summary>
        public void SendChat(string message)
        {
            if (!isConnected) return;

            var msg = new Dictionary<string, object>
            {
                { "type", "CHAT" },
                { "sessionId", sessionId },
                { "playerId", playerId },
                { "message", message }
            };

            SendJson(msg);
        }

        /// <summary>
        /// Request match start.
        /// </summary>
        public void SendStartMatch()
        {
            if (!isConnected) return;

            var message = new Dictionary<string, object>
            {
                { "type", "START_MATCH" },
                { "sessionId", sessionId },
                { "playerId", playerId }
            };

            SendJson(message);
        }

        private void SendJson(object obj)
        {
            if (websocket == null || !isConnected) return;

            try
            {
                string json = JsonSerializer.Serialize(obj);
                websocket.Send(json);
            }
            catch (Exception e)
            {
                Debug.LogError($"[FpsNetworkManager] Send error: {e.Message}");
            }
        }

        private void OnWebSocketMessage(string message)
        {
            try
            {
                using JsonDocument doc = JsonDocument.Parse(message);
                string type = doc.RootElement.GetProperty("type").GetString();

                switch (type)
                {
                    case "CONNECT":
                        OnConnectMessage(doc);
                        break;
                    case "STATE":
                        OnStateMessage(doc);
                        break;
                    case "EVENT":
                        OnEventMessage(doc);
                        break;
                    case "CHAT":
                        OnChatMessageReceived(doc);
                        break;
                    case "ERROR":
                        OnErrorMessage(doc);
                        break;
                    default:
                        Debug.LogWarning($"[FpsNetworkManager] Unknown message type: {type}");
                        break;
                }
            }
            catch (Exception e)
            {
                Debug.LogError($"[FpsNetworkManager] Parse error: {e.Message}");
            }
        }

        private void OnConnectMessage(JsonDocument doc)
        {
            sessionId = doc.RootElement.GetProperty("sessionId").GetString();
            playerId = doc.RootElement.GetProperty("playerId").GetString();
            isConnected = true;
            reconnectAttempts = 0;

            Debug.Log($"[FpsNetworkManager] Connected: session={sessionId}, player={playerId}");

            // Start periodic input sending
            if (inputCoroutine == null)
            {
                inputCoroutine = StartCoroutine(SendInputLoop());
            }

            OnConnected?.Invoke(sessionId);
        }

        private void OnStateMessage(JsonDocument doc)
        {
            var state = new FpsGameStateMessage();

            if (doc.RootElement.TryGetProperty("tick", out JsonElement tick))
                state.Tick = tick.GetInt32();

            if (doc.RootElement.TryGetProperty("players", out JsonElement players))
            {
                state.Players = new List<PlayerStateData>();
                foreach (var p in players.EnumerateArray())
                {
                    var ps = new PlayerStateData
                    {
                        Id = p.GetProperty("id").GetString(),
                        Name = p.TryGetProperty("name", out JsonElement name) ? name.GetString() : $"Player_{p.GetProperty("id").GetString()}",
                        Health = p.GetProperty("health").GetInt32(),
                        Ammo = p.GetProperty("ammo").GetInt32(),
                        ReserveAmmo = p.TryGetProperty("reserveAmmo", out JsonElement ra) ? ra.GetInt32() : 0,
                        WeaponIndex = p.TryGetProperty("weaponIndex", out JsonElement wi) ? wi.GetInt32() : 0,
                        Score = p.TryGetProperty("score", out JsonElement sc) ? sc.GetInt32() : 0,
                        Team = p.TryGetProperty("team", out JsonElement tm) ? tm.GetInt32() : 0,
                        Alive = p.TryGetProperty("alive", out JsonElement al) ? al.GetBoolean() : true
                    };

                    if (p.TryGetProperty("position", out JsonElement pos))
                    {
                        ps.Position = new Vector3Data(
                            pos.GetProperty("x").GetSingle(),
                            pos.GetProperty("y").GetSingle(),
                            pos.GetProperty("z").GetSingle()
                        );
                    }

                    if (p.TryGetProperty("rotation", out JsonElement rot))
                    {
                        ps.Rotation = new Vector3Data(
                            rot.TryGetProperty("x", out JsonElement rx) ? rx.GetSingle() : 0f,
                            rot.TryGetProperty("y", out JsonElement ry) ? ry.GetSingle() : 0f,
                            rot.TryGetProperty("z", out JsonElement rz) ? rz.GetSingle() : 0f
                        );
                    }

                    state.Players.Add(ps);
                    playerStates[ps.Id] = ps;
                }
            }

            if (doc.RootElement.TryGetProperty("events", out JsonElement events))
            {
                state.Events = new List<GameStateEvent>();
                foreach (var e in events.EnumerateArray())
                {
                    var ge = new GameStateEvent
                    {
                        Type = e.GetProperty("type").GetString(),
                        PlayerId = e.TryGetProperty("playerId", out JsonElement pid) ? pid.GetString() : null,
                        TargetId = e.TryGetProperty("targetId", out JsonElement tid) ? tid.GetString() : null,
                        Damage = e.TryGetProperty("damage", out JsonElement dmg) ? dmg.GetInt32() : 0,
                        Message = e.TryGetProperty("message", out JsonElement msg) ? msg.GetString() : null
                    };
                    state.Events.Add(ge);
                    recentEvents.Add(ge);
                    OnGameEvent?.Invoke(ge);
                }
            }

            // Keep only last 50 events
            while (recentEvents.Count > 50)
                recentEvents.RemoveAt(0);

            currentTick = state.Tick;
            OnGameStateReceived?.Invoke(state);
        }

        private void OnEventMessage(JsonDocument doc)
        {
            var evt = new GameStateEvent
            {
                Type = doc.RootElement.GetProperty("type").GetString(),
                PlayerId = doc.RootElement.TryGetProperty("playerId", out JsonElement pid) ? pid.GetString() : null,
                TargetId = doc.RootElement.TryGetProperty("targetId", out JsonElement tid) ? tid.GetString() : null,
                Damage = doc.RootElement.TryGetProperty("damage", out JsonElement dmg) ? dmg.GetInt32() : 0,
                Message = doc.RootElement.TryGetProperty("message", out JsonElement msg) ? msg.GetString() : null
            };

            recentEvents.Add(evt);
            OnGameEvent?.Invoke(evt);
        }

        private void OnChatMessageReceived(JsonDocument doc)
        {
            string playerId = doc.RootElement.TryGetProperty("playerId", out JsonElement pid) ? pid.GetString() : "Unknown";
            string message = doc.RootElement.TryGetProperty("message", out JsonElement msg) ? msg.GetString() : "";
            OnChatMessage?.Invoke(playerId, message);
        }

        private void OnErrorMessage(JsonDocument doc)
        {
            string message = doc.RootElement.TryGetProperty("message", out JsonElement msg) ? msg.GetString() : "Unknown error";
            Debug.LogError($"[FpsNetworkManager] Server error: {message}");
            OnError?.Invoke(message);
        }

        private void OnWebSocketClose(int code, string reason)
        {
            isConnected = false;

            if (inputCoroutine != null)
            {
                StopCoroutine(inputCoroutine);
                inputCoroutine = null;
            }

            Debug.LogWarning($"[FpsNetworkManager] Disconnected: {reason}");
            OnDisconnected?.Invoke();

            // Attempt reconnect
            if (reconnectAttempts < maxReconnectAttempts)
            {
                reconnectCoroutine = StartCoroutine(ReconnectCoroutine());
            }
        }

        private void OnWebSocketError(string error)
        {
            Debug.LogError($"[FpsNetworkManager] WebSocket error: {error}");
            OnError?.Invoke(error);
        }

        private IEnumerator ReconnectCoroutine()
        {
            reconnectAttempts++;
            Debug.Log($"[FpsNetworkManager] Reconnecting in {reconnectDelay}s (attempt {reconnectAttempts}/{maxReconnectAttempts})");
            yield return new WaitForSeconds(reconnectDelay);
            Connect();
        }

        private IEnumerator SendInputLoop()
        {
            // Send input at 20Hz to match server tick rate
            var timer = new WaitForSeconds(0.05f);

            while (isConnected)
            {
                yield return timer;

                // Gather current input
                float moveX = Input.GetAxis("Horizontal");
                float moveZ = Input.GetAxis("Vertical");
                Vector3 move = new Vector3(moveX, 0f, moveZ);

                float pitch = 0f, yaw = 0f;
                if (Camera.main != null)
                {
                    pitch = Camera.main.transform.localEulerAngles.x;
                    yaw = transform.eulerAngles.y;
                }

                var actions = new List<string>();
                if (Input.GetButton("Fire1")) actions.Add("shoot");
                if (Input.GetKey(KeyCode.LeftShift)) actions.Add("sprint");
                if (Input.GetKey(KeyCode.C)) actions.Add("crouch");
                if (Input.GetButtonDown("Jump")) actions.Add("jump");
                if (Input.GetKeyDown(KeyCode.R)) actions.Add("reload");

                SendInput(move, pitch, yaw, actions);
            }
        }

        /// <summary>
        /// Fetch weapon list from the REST API.
        /// </summary>
        public IEnumerator FetchWeapons(Action<List<Dictionary<string, object>>> callback)
        {
            string url = $"http://{serverHost}:{serverPort}/api/v1/fps/weapons";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var weapons = JsonSerializer.Deserialize<List<Dictionary<string, object>>>(request.downloadHandler.text);
                    callback?.Invoke(weapons);
                }
                else
                {
                    Debug.LogError($"[FpsNetworkManager] Failed to fetch weapons: {request.error}");
                    callback?.Invoke(null);
                }
            }
        }

        /// <summary>
        /// Fetch map list from the REST API.
        /// </summary>
        public IEnumerator FetchMaps(Action<List<Dictionary<string, object>>> callback)
        {
            string url = $"http://{serverHost}:{serverPort}/api/v1/fps/maps";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var maps = JsonSerializer.Deserialize<List<Dictionary<string, object>>>(request.downloadHandler.text);
                    callback?.Invoke(maps);
                }
                else
                {
                    Debug.LogError($"[FpsNetworkManager] Failed to fetch maps: {request.error}");
                    callback?.Invoke(null);
                }
            }
        }

        /// <summary>
        /// Create a new match via REST API.
        /// </summary>
        public IEnumerator CreateMatch(string mode, string mapId, Action<string> callback)
        {
            string url = $"http://{serverHost}:{serverPort}/api/v1/fps/matches";
            var body = new { mode = mode, mapId = mapId };
            byte[] bodyRaw = Encoding.UTF8.GetBytes(JsonSerializer.Serialize(body));

            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var response = JsonSerializer.Deserialize<Dictionary<string, object>>(request.downloadHandler.text);
                    string matchId = response["roomId"]?.ToString();
                    
                    // Parse serverUrl for dedicated server support
                    if (response.ContainsKey("serverUrl"))
                    {
                        string serverUrl = response["serverUrl"]?.ToString();
                        if (!string.IsNullOrEmpty(serverUrl))
                        {
                            Debug.Log($"[FpsNetworkManager] Using dedicated server: {serverUrl}");
                            // Extract host and port from serverUrl (ws://host:port/ws/fps)
                            var uri = new Uri(serverUrl);
                            serverHost = uri.Host;
                            serverPort = uri.Port;
                        }
                    }
                    
                    callback?.Invoke(matchId);
                }
                else
                {
                    Debug.LogError($"[FpsNetworkManager] Failed to create match: {request.error}");
                    callback?.Invoke(null);
                }
            }
        }

        /// <summary>
        /// Join an existing match via REST API.
        /// </summary>
        public IEnumerator JoinMatch(string matchId, Action<bool> callback)
        {
            string url = $"http://{serverHost}:{serverPort}/api/v1/fps/matches/{matchId}/join";
            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerRaw(new byte[0]);
                request.downloadHandler = new DownloadHandlerBuffer();
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    callback?.Invoke(true);
                }
                else
                {
                    Debug.LogError($"[FpsNetworkManager] Failed to join match: {request.error}");
                    callback?.Invoke(false);
                }
            }
        }

        /// <summary>
        /// List active matches via REST API.
        /// </summary>
        public IEnumerator ListMatches(Action<List<Dictionary<string, object>>> callback)
        {
            string url = $"http://{serverHost}:{serverPort}/api/v1/fps/matches";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var matches = JsonSerializer.Deserialize<List<Dictionary<string, object>>>(request.downloadHandler.text);
                    callback?.Invoke(matches);
                }
                else
                {
                    Debug.LogError($"[FpsNetworkManager] Failed to list matches: {request.error}");
                    callback?.Invoke(null);
                }
            }
        }

        /// <summary>
        /// Get recent game events for kill feed.
        /// </summary>
        public List<GameStateEvent> GetRecentEvents()
        {
            return new List<GameStateEvent>(recentEvents);
        }
    }

    /// <summary>
    /// Simple WebSocket implementation using UnityWebRequest.
    /// </summary>
    public class WebSocket
    {
        private string url;
        private bool isConnected;

        public event Action<string> OnMessage;
        public event Action<int, string> OnClose;
        public event Action<string> OnError;

        public WebSocket(string url)
        {
            this.url = url;
        }

        public void Connect()
        {
            // Note: Unity's built-in WebSocket support requires a separate package.
            // This is a placeholder that uses UnityWebRequest for the handshake.
            // In production, use Unity.WebSocket package or a third-party library.
            Debug.Log($"[WebSocket] Connecting to {url}");
            isConnected = true;
        }

        public void Send(string message)
        {
            if (!isConnected)
            {
                Debug.LogWarning("[WebSocket] Not connected");
                return;
            }

            // In production, this would send via the actual WebSocket connection
            Debug.Log($"[WebSocket] Send: {message}");
        }

        public void Close()
        {
            isConnected = false;
            OnClose?.Invoke(1000, "Closed by client");
        }

        public bool IsConnected => isConnected;
    }
}
