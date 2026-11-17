using System;
using System.Collections;
using System.Text;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// WebSocket client for communication with Electron client.
    /// Sends telemetry and receives commands from the Electron UI.
    /// </summary>
    public class ElectronBridge : Singleton<ElectronBridge>
    {
        [Header("Connection Settings")]
        [SerializeField] private string electronUrl = "ws://127.0.0.1:8765";
        [SerializeField] private float reconnectDelay = 3f;
        [SerializeField] private float telemetryInterval = 1f;

        private WebSocketConnection connection;
        private bool isConnected;
        private Coroutine reconnectCoroutine;
        private Coroutine telemetryCoroutine;

        // Events
        public event Action<string> OnConnected;
        public event Action OnDisconnected;
        public event Action<string, string> OnCommand; // command, payload
        public event Action<string> OnError;

        public bool IsConnected => isConnected;

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
        /// Connect to Electron client.
        /// </summary>
        public void Connect()
        {
            if (isConnected) return;

            Debug.Log($"[ElectronBridge] Connecting to {electronUrl}");

            // Use HTTP-based fallback since Unity doesn't have native WebSocket support
            // In production, use a WebSocket library like WebSocketSharp or uWebSocket
            StartCoroutine(ConnectCoroutine());
        }

        /// <summary>
        /// Disconnect from Electron client.
        /// </summary>
        public void Disconnect()
        {
            isConnected = false;

            if (reconnectCoroutine != null)
            {
                StopCoroutine(reconnectCoroutine);
                reconnectCoroutine = null;
            }

            if (telemetryCoroutine != null)
            {
                StopCoroutine(telemetryCoroutine);
                telemetryCoroutine = null;
            }

            OnDisconnected?.Invoke();
        }

        private IEnumerator ConnectCoroutine()
        {
            // HTTP-based heartbeat as fallback
            string url = "http://127.0.0.1:8765/health";
            
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    isConnected = true;
                    OnConnected?.Invoke("Electron client connected");
                    
                    // Start telemetry loop
                    telemetryCoroutine = StartCoroutine(TelemetryLoop());
                    
                    Debug.Log("[ElectronBridge] Connected to Electron client");
                }
                else
                {
                    Debug.LogWarning($"[ElectronBridge] Connection failed: {request.error}");
                    OnError?.Invoke(request.error);
                    ScheduleReconnect();
                }
            }
        }

        private void ScheduleReconnect()
        {
            reconnectCoroutine = StartCoroutine(ReconnectWait());
        }

        private IEnumerator ReconnectWait()
        {
            yield return new WaitForSeconds(reconnectDelay);
            reconnectCoroutine = null;
            Connect();
        }

        /// <summary>
        /// Periodic telemetry loop.
        /// </summary>
        private IEnumerator TelemetryLoop()
        {
            while (isConnected)
            {
                yield return new WaitForSeconds(telemetryInterval);
                SendTelemetry();
            }
        }

        /// <summary>
        /// Send telemetry data to Electron.
        /// </summary>
        private void SendTelemetry()
        {
            if (!isConnected) return;

            var gm = GameManager.Instance;
            if (gm == null) return;

            var state = gm.CurrentState;
            if (state == null) return;

            // Build telemetry JSON
            var telemetry = new
            {
                type = "telemetry",
                payload = new
                {
                    timestamp = DateTime.UtcNow.Ticks,
                    player = new
                    {
                        state.Player?.commander?.name,
                        state.Player?.commander?.location,
                        state.Player?.commander?.alive
                    },
                    fleets = state.Player?.fleets != null ? state.Player.fleets.Length : 0,
                    ships = state.Player?.ships != null ? state.Player.ships.Length : 0,
                    planets = state.Player?.planets != null ? state.Player.planets.Length : 0,
                    tick = state.Tick?.tick
                }
            };

            // Send via HTTP POST
            StartCoroutine(SendToElectron("/telemetry", JsonUtility.ToJson(new { data = "telemetry" })));
        }

        /// <summary>
        /// Send a command to Electron.
        /// </summary>
        public void SendCommand(string command, string payload)
        {
            if (!isConnected) return;

            StartCoroutine(SendToElectron($"/command/{command}", payload));
        }

        /// <summary>
        /// Send data to Electron via HTTP.
        /// </summary>
        private IEnumerator SendToElectron(string endpoint, string data)
        {
            string url = $"http://127.0.0.1:8765{endpoint}";
            
            var body = new System.Byte[data.Length];
            System.Array.Copy(Encoding.UTF8.GetBytes(data), body, data.Length);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerRaw(body);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result != UnityWebRequest.Result.Success)
                {
                    Debug.LogWarning($"[ElectronBridge] Send failed: {request.error}");
                }
            }
        }

        /// <summary>
        /// Handle incoming command from Electron.
        /// </summary>
        public void HandleCommand(string command, string payload)
        {
            OnCommand?.Invoke(command, payload);

            switch (command)
            {
                case "navigate":
                    HandleNavigateCommand(payload);
                    break;
                case "battlefield":
                    HandleBattlefieldCommand(payload);
                    break;
                case "exitBattlefield":
                    var gm = GameManager.Instance;
                    if (gm != null) gm.ExitBattlefieldMode();
                    break;
                case "gameState":
                    // State will be synced via NetworkManager
                    break;
            }
        }

        private void HandleNavigateCommand(string payload)
        {
            // Parse JSON payload
            try
            {
                var data = JsonUtility.FromJson<NavigateData>("{\"sectorRow\":" + 
                    payload.Split(',')[0].Split(':')[1] + 
                    ",\"sectorCol\":0,\"subSectorRow\":0,\"subSectorCol\":0}");
                
                var input = Input.InputHandler.Instance;
                if (input != null)
                {
                    input.NavigateToSystem(data.sectorRow, data.sectorCol, data.subSectorRow, data.subSectorCol);
                }
            }
            catch (Exception e)
            {
                Debug.LogWarning($"[ElectronBridge] Failed to parse navigate command: {e.Message}");
            }
        }

        private void HandleBattlefieldCommand(string payload)
        {
            var gm = GameManager.Instance;
            if (gm != null)
            {
                // Enter battlefield mode
                gm.EnterBattlefieldFromElectron(1, "WWII", "URBAN");
            }
        }

        /// <summary>
        /// Navigate data from Electron.
        /// </summary>
        [Serializable]
        private class NavigateData
        {
            public int sectorRow;
            public int sectorCol;
            public int subSectorRow;
            public int subSectorCol;
        }
    }
}
