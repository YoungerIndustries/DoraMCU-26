using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Main HUD overlay for the Unity client.
    /// Displays player character stats, resources, fleet status, and connection state.
    /// Uses Unity UI Canvas with screen-space overlay rendering.
    /// </summary>
    public class MainHUD : Singleton<MainHUD>
    {
        [Header("Player Stats Panel")]
        [SerializeField] private Text playerNameText;
        [SerializeField] private Text playerSpeciesText;
        [SerializeField] private Text playerCareerText;
        [SerializeField] private Text playerStatusText;
        [SerializeField] private Text playerLocationText;
        [SerializeField] private Slider playerHealthBar;
        [SerializeField] private GameObject playerStatsPanel;

        [Header("Resources Panel")]
        [SerializeField] private Text resourcesText;
        [SerializeField] private GameObject resourcesPanel;

        [Header("Fleet Panel")]
        [SerializeField] private Text fleetText;
        [SerializeField] private Text shipsText;
        [SerializeField] private Text planetsText;
        [SerializeField] private GameObject fleetPanel;

        [Header("Connection Panel")]
        [SerializeField] private Text connectionStatusText;
        [SerializeField] private Text tickInfoText;
        [SerializeField] private GameObject connectionPanel;

        [Header("Minimap")]
        [SerializeField] private RawImage minimapImage;
        [SerializeField] private GameObject minimapPanel;
        [SerializeField] private float minimapSize = 200f;

        [Header("Settings")]
        [SerializeField] private float hudUpdateInterval = 2f;
        [SerializeField] private Color connectedColor = Color.green;
        [SerializeField] private Color disconnectedColor = Color.red;

        private GameObject playerStatsPanelGo;
        private GameObject resourcesPanelGo;
        private GameObject fleetPanelGo;
        private GameObject connectionPanelGo;
        private GameObject minimapPanelGo;
        private Coroutine updateCoroutine;
        private RenderTexture minimapTexture;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            SubscribeToEvents();
            updateCoroutine = StartCoroutine(PeriodicUpdate());
        }

        void OnDestroy()
        {
            if (updateCoroutine != null)
            {
                StopCoroutine(updateCoroutine);
            }
            UnsubscribeFromEvents();
        }

        /// <summary>
        /// Creates all UI elements programmatically if not assigned in inspector.
        /// </summary>
        private void EnsureUIElements()
        {
            // Get or create canvas
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("MainCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                canvasGo.AddComponent<CanvasRenderer>();
                canvasGo.AddComponent<GraphicRaycaster>();

                // Event system
                var eventSystem = Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }

            // Create player stats panel (top-left)
            if (playerStatsPanel == null)
            {
                playerStatsPanelGo = CreatePanel("PlayerStatsPanel", new Vector2(10, Screen.height - 10), new Vector2(280, 160));
                CreateText(ref playerNameText, "PlayerNameText", playerStatsPanelGo.transform, "Player Name", 18, new Vector2(10, 140));
                CreateText(ref playerSpeciesText, "PlayerSpeciesText", playerStatsPanelGo.transform, "Species", 12, new Vector2(10, 120));
                CreateText(ref playerCareerText, "PlayerCareerText", playerStatsPanelGo.transform, "Career", 12, new Vector2(10, 100));
                CreateText(ref playerStatusText, "PlayerStatusText", playerStatsPanelGo.transform, "Status", 12, new Vector2(10, 80));
                CreateText(ref playerLocationText, "PlayerLocationText", playerStatsPanelGo.transform, "Location", 12, new Vector2(10, 60));

                // Health bar
                var barGo = new GameObject("HealthBar") { transform = { parent = playerStatsPanelGo.transform, anchoredPosition = new Vector2(10, 30), localScale = Vector3.one } };
                playerHealthBar = barGo.AddComponent<Slider>();
                playerHealthBar.minValue = 0;
                playerHealthBar.maxValue = 1;
                playerHealthBar.value = 1;
                playerHealthBar.direction = Slider.Direction.LeftToRight;
            }

            // Create resources panel (top-right)
            if (resourcesPanel == null)
            {
                resourcesPanelGo = CreatePanel("ResourcesPanel", new Vector2(Screen.width - 210, Screen.height - 10), new Vector2(200, 150));
                CreateText(ref resourcesText, "ResourcesText", resourcesPanelGo.transform, "Resources", 14, new Vector2(10, 130));
            }

            // Create fleet panel (bottom-left)
            if (fleetPanel == null)
            {
                fleetPanelGo = CreatePanel("FleetPanel", new Vector2(10, 10), new Vector2(250, 100));
                CreateText(ref fleetText, "FleetText", fleetPanelGo.transform, "Fleets", 14, new Vector2(10, 80));
                CreateText(ref shipsText, "ShipsText", fleetPanelGo.transform, "Ships", 12, new Vector2(10, 60));
                CreateText(ref planetsText, "PlanetsText", fleetPanelGo.transform, "Planets", 12, new Vector2(10, 40));
            }

            // Create connection panel (top-center)
            if (connectionPanel == null)
            {
                connectionPanelGo = CreatePanel("ConnectionPanel", new Vector2(Screen.width / 2 - 100, Screen.height - 10), new Vector2(200, 40));
                CreateText(ref connectionStatusText, "ConnectionStatusText", connectionPanelGo.transform, "Connecting...", 12, new Vector2(0, 10));
                CreateText(ref tickInfoText, "TickInfoText", connectionPanelGo.transform, "Tick", 10, new Vector2(0, -10));
            }

            // Create minimap panel (bottom-right)
            if (minimapPanel == null)
            {
                minimapPanelGo = CreatePanel("MinimapPanel", new Vector2(Screen.width - minimapSize - 10, 10), new Vector2(minimapSize, minimapSize));
                
                // Minimap texture
                minimapTexture = new RenderTexture((int)minimapSize, (int)minimapSize, 16);
                minimapTexture.Create();
                
                if (minimapImage == null)
                {
                    var imgGo = new GameObject("MinimapImage") { transform = { parent = minimapPanelGo.transform, anchoredPosition = Vector2.zero, localScale = Vector3.one } };
                    minimapImage = imgGo.AddComponent<RawImage>();
                    minimapImage.texture = minimapTexture;
                    minimapImage.rectTransform.sizeDelta = new Vector2(minimapSize, minimapSize);
                }
            }
        }

        /// <summary>
        /// Subscribe to network and game events.
        /// </summary>
        private void SubscribeToEvents()
        {
            var nm = Core.Singleton<DoraMCU.Client.Network.NetworkManager>.Instance;
            if (nm != null)
            {
                nm.OnConnected += OnConnected;
                nm.OnDisconnected += OnDisconnected;
                nm.OnError += OnError;
            }
        }

        private void UnsubscribeFromEvents()
        {
            var nm = Core.Singleton<DoraMCU.Client.Network.NetworkManager>.Instance;
            if (nm != null)
            {
                nm.OnConnected -= OnConnected;
                nm.OnDisconnected -= OnDisconnected;
                nm.OnError -= OnError;
            }
        }

        /// <summary>
        /// Periodically fetch and update game state.
        /// </summary>
        private System.Collections.IEnumerator PeriodicUpdate()
        {
            while (true)
            {
                yield return new WaitForSeconds(hudUpdateInterval);
                UpdateFromGameState();
            }
        }

        private void UpdateFromGameState()
        {
            var gm = GameManager.Instance;
            if (gm != null)
            {
                // Update from GameManager's current state
                var state = gm.CurrentState;
                if (state != null)
                {
                    UpdatePlayerStats(state.Player);
                    UpdateResources(state.Player);
                    UpdateFleetInfo(state.Player);
                    UpdateTickInfo(state.Tick);
                }
            }
        }

        /// <summary>
        /// Update player character stats display.
        /// </summary>
        public void UpdatePlayerStats(DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (player == null) return;

            if (player.commander != null)
            {
                if (playerNameText != null) playerNameText.text = $"Commander: {player.commander.name}";
                if (playerSpeciesText != null) playerSpeciesText.text = $"Species: {player.commander.species}";
                if (playerCareerText != null) playerCareerText.text = $"Career: {player.commander.currentCareer}";
                if (playerStatusText != null)
                {
                    playerStatusText.text = $"Status: {(player.commander.alive ? "Alive" : "Deceased")}";
                    playerStatusText.color = player.commander.alive ? Color.white : Color.red;
                }
                if (playerLocationText != null) playerLocationText.text = $"Location: {player.commander.location}";
            }

            if (playerHealthBar != null)
            {
                playerHealthBar.value = player.commander?.alive == true ? 1f : 0f;
            }
        }

        /// <summary>
        /// Update resources display.
        /// </summary>
        public void UpdateResources(DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (player == null || resourcesText == null) return;

            var lines = new List<string> { "Resources:" };
            
            if (player.resources != null)
            {
                foreach (var kvp in player.resources)
                {
                    lines.Add($"  {kvp.Key}: {kvp.Value}");
                }
            }

            if (player.rawResources != null)
            {
                foreach (var kvp in player.rawResources)
                {
                    lines.Add($"  Raw {kvp.Key}: {kvp.Value}");
                }
            }

            if (player.refinedResources != null)
            {
                foreach (var kvp in player.refinedResources)
                {
                    lines.Add($"  Refined {kvp.Key}: {kvp.Value}");
                }
            }

            resourcesText.text = string.Join("\n", lines);
        }

        /// <summary>
        /// Update fleet and asset information.
        /// </summary>
        public void UpdateFleetInfo(DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (player == null) return;

            if (fleetText != null)
            {
                var fleetCount = player.fleets?.Length ?? 0;
                fleetText.text = $"Fleets: {fleetCount}";
            }

            if (shipsText != null)
            {
                var shipCount = player.ships?.Length ?? 0;
                shipsText.text = $"Ships: {shipCount}";
            }

            if (planetsText != null)
            {
                var planetCount = player.planets?.Length ?? 0;
                planetsText.text = $"Planets: {planetCount}";
            }
        }

        /// <summary>
        /// Update tick and server status.
        /// </summary>
        public void UpdateTickInfo(DoraMCU.Client.Network.NetworkManager.TickInfo tick)
        {
            if (tick == null || tickInfoText == null) return;

            tickInfoText.text = $"Tick: {tick.tick} | Entities: {tick.entityCount} | Players: {tick.connectedPlayers}";
        }

        /// <summary>
        /// Handle connection event.
        /// </summary>
        private void OnConnected(string sessionId)
        {
            if (connectionStatusText != null)
            {
                connectionStatusText.text = "Connected";
                connectionStatusText.color = connectedColor;
            }
            Debug.Log($"[MainHUD] Connected: {sessionId}");
        }

        /// <summary>
        /// Handle disconnection event.
        /// </summary>
        private void OnDisconnected()
        {
            if (connectionStatusText != null)
            {
                connectionStatusText.text = "Disconnected";
                connectionStatusText.color = disconnectedColor;
            }
        }

        /// <summary>
        /// Handle error event.
        /// </summary>
        private void OnError(string error)
        {
            if (connectionStatusText != null)
            {
                connectionStatusText.text = $"Error: {error}";
                connectionStatusText.color = Color.yellow;
            }
        }

        /// <summary>
        /// Show or hide the entire HUD.
        /// </summary>
        public void SetVisible(bool visible)
        {
            if (playerStatsPanelGo != null) playerStatsPanelGo.SetActive(visible);
            if (resourcesPanelGo != null) resourcesPanelGo.SetActive(visible);
            if (fleetPanelGo != null) fleetPanelGo.SetActive(visible);
            if (connectionPanelGo != null) connectionPanelGo.SetActive(visible);
            if (minimapPanelGo != null) minimapPanelGo.SetActive(visible);
        }

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(FindObjectOfType<Canvas>().transform);
            
            var rectTransform = panel.AddComponent<RectTransform>();
            rectTransform.anchorMin = Vector2.zero;
            rectTransform.anchorMax = Vector2.one;
            rectTransform.anchoredPosition = position;
            rectTransform.sizeDelta = -size;

            var image = panel.AddComponent<Image>();
            image.color = new Color(0, 0, 0, 0.7f);

            var outline = panel.AddComponent<Outline>();
            outline.effectColor = Color.cyan;
            outline.effectDistance = new Vector2(2, 2);

            return panel;
        }

        private void CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
        {
            var textGo = new GameObject(name);
            textGo.transform.SetParent(parent);
            textGo.transform.anchoredPosition = position;
            textGo.transform.localRotation = Quaternion.identity;
            textGo.transform.localScale = Vector3.one;

            textRef = textGo.AddComponent<Text>();
            textRef.text = defaultText;
            textRef.fontSize = fontSize;
            textRef.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            textRef.color = Color.white;
            textRef.horizontalOverflow = HorizontalWrapMode.Overflow;
            textRef.verticalOverflow = VerticalWrapMode.Overflow;
        }

        #endregion
    }
}
