using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using DoraMCU.Client.Core;
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
        [SerializeField] private Text playerCorporationText;
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
        [SerializeField] private ScrollRect fleetScroll;
        [SerializeField] private Transform fleetContent;

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
        [SerializeField] private Color connectedColor; // Initialized from UIConfig
        [SerializeField] private Color disconnectedColor; // Initialized from UIConfig

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
            Debug.Log("[MainHUD] Awake() called");
        }

        void Start()
        {
            Debug.Log("[MainHUD] Start() called");
            EnsureUIElements();
            SubscribeToEvents();
            // Hide HUD initially — show only after login
            SetVisible(false);
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
                var uiCfg = UIConfig.Instance;
                playerStatsPanelGo = CreatePanel("PlayerStatsPanel", new Vector2(10, Screen.height - 10), uiCfg.PlayerStatsPanelSize);
                CreateText(ref playerNameText, "PlayerNameText", playerStatsPanelGo.transform, "Player Name", uiCfg.FontSizeHeading, new Vector2(10, 140));
                CreateText(ref playerSpeciesText, "PlayerSpeciesText", playerStatsPanelGo.transform, "Species", uiCfg.FontSizeSmall, new Vector2(10, 120));
                CreateText(ref playerCareerText, "PlayerCareerText", playerStatsPanelGo.transform, "Career", uiCfg.FontSizeSmall, new Vector2(10, 100));
                CreateText(ref playerCorporationText, "PlayerCorporationText", playerStatsPanelGo.transform, "Corporation", uiCfg.FontSizeSmall, new Vector2(10, 80));
                CreateText(ref playerStatusText, "PlayerStatusText", playerStatsPanelGo.transform, "Status", uiCfg.FontSizeSmall, new Vector2(10, 60));
                CreateText(ref playerLocationText, "PlayerLocationText", playerStatsPanelGo.transform, "Location", uiCfg.FontSizeSmall, new Vector2(10, 40));

                // Health bar
                var barGo = new GameObject("HealthBar");
                var barRect = barGo.AddComponent<RectTransform>();
                barRect.SetParent(playerStatsPanelGo.transform);
                barRect.anchoredPosition = new Vector2(10, 30);
                barRect.localScale = Vector3.one;
                playerHealthBar = barGo.AddComponent<Slider>();
                playerHealthBar.minValue = 0;
                playerHealthBar.maxValue = 1;
                playerHealthBar.value = 1;
                playerHealthBar.direction = Slider.Direction.LeftToRight;
            }

            // Create resources panel (top-right)
            if (resourcesPanel == null)
            {
                var uiCfg2 = UIConfig.Instance;
                resourcesPanelGo = CreatePanel("ResourcesPanel", new Vector2(Screen.width - 210, Screen.height - 10), uiCfg2.ResourcesPanelSize);
                CreateText(ref resourcesText, "ResourcesText", resourcesPanelGo.transform, "Resources", uiCfg2.FontSizeNormal, new Vector2(10, 130));
            }

            // Create fleet panel (bottom-left) with scroll support
            if (fleetPanel == null)
            {
                var uiCfg3 = UIConfig.Instance;
                fleetPanelGo = CreatePanel("FleetPanel", new Vector2(10, 10), new Vector2(uiCfg3.FleetPanelSize.x, 200));
                
                // Title text at top
                CreateText(ref fleetText, "FleetText", fleetPanelGo.transform, "Fleets", uiCfg3.FontSizeNormal, new Vector2(10, 180));
                
                // Scrollable content area for fleet items
                var scrollGo = new GameObject("FleetScroll");
                var scrollRect = scrollGo.AddComponent<RectTransform>();
                scrollRect.SetParent(fleetPanelGo.transform);
                scrollRect.anchorMin = Vector2.zero;
                scrollRect.anchorMax = Vector2.one;
                scrollRect.anchoredPosition = Vector2.zero;
                scrollRect.sizeDelta = new Vector2(-20, -40); // Padding from edges
                scrollRect.localScale = Vector3.one;
                
                var scrollRectComp = scrollGo.AddComponent<ScrollRect>();
                
                // Viewport
                var viewport = new GameObject("Viewport");
                var vpRect = viewport.AddComponent<RectTransform>();
                vpRect.SetParent(scrollGo.transform);
                vpRect.anchorMin = Vector2.zero;
                vpRect.anchorMax = Vector2.one;
                vpRect.anchoredPosition = Vector2.zero;
                vpRect.sizeDelta = Vector2.zero;
                vpRect.localScale = Vector3.one;
                var viewportImage = viewport.AddComponent<Image>();
                viewportImage.color = UIConfig.Instance.ScrollViewportBackground;
                scrollRectComp.viewport = vpRect;
                
                // Content area
                var contentGo = new GameObject("Content");
                var contentRect = contentGo.AddComponent<RectTransform>();
                contentRect.SetParent(vpRect);
                contentRect.anchorMin = new Vector2(0.5f, 1);
                contentRect.anchorMax = new Vector2(0.5f, 1);
                contentRect.anchoredPosition = new Vector2(0, 0);
                contentRect.sizeDelta = new Vector2(vpRect.sizeDelta.x, 0);
                contentRect.localScale = Vector3.one;
                contentGo.AddComponent<ContentSizeFitter>().verticalFit = ContentSizeFitter.FitMode.PreferredSize;
                
                scrollRectComp.content = contentRect;
                fleetContent = contentRect;
                
                // Vertical scrollbar
                var scrollbarGo = new GameObject("Scrollbar");
                var sbRect = scrollbarGo.AddComponent<RectTransform>();
                sbRect.SetParent(scrollGo.transform);
                sbRect.anchorMin = new Vector2(1, 0);
                sbRect.anchorMax = new Vector2(1, 1);
                sbRect.anchoredPosition = Vector2.zero;
                sbRect.sizeDelta = new Vector2(8, 0);
                sbRect.localScale = Vector3.one;
                var scrollbar = scrollbarGo.AddComponent<Scrollbar>();
                scrollbar.direction = Scrollbar.Direction.TopToBottom;
                scrollRectComp.scrollbar = scrollbar;
                
                fleetScroll = scrollRectComp;
                
                // Summary texts at bottom
                CreateText(ref shipsText, "ShipsText", fleetPanelGo.transform, "Ships: 0", uiCfg3.FontSizeSmall, new Vector2(10, 20));
                CreateText(ref planetsText, "PlanetsText", fleetPanelGo.transform, "Planets: 0", uiCfg3.FontSizeSmall, new Vector2(10, 5));
            }

            // Create connection panel (top-center)
            if (connectionPanel == null)
            {
                var uiCfg4 = UIConfig.Instance;
                connectionPanelGo = CreatePanel("ConnectionPanel", new Vector2(Screen.width / 2 - 100, Screen.height - 10), uiCfg4.ConnectionPanelSize);
                CreateText(ref connectionStatusText, "ConnectionStatusText", connectionPanelGo.transform, "Connecting...", uiCfg4.FontSizeSmall, new Vector2(0, 10));
                CreateText(ref tickInfoText, "TickInfoText", connectionPanelGo.transform, "Tick", uiCfg4.FontSizeTiny, new Vector2(0, -10));
            }

            // Create minimap panel (bottom-right)
            if (minimapPanel == null)
            {
                var uiCfg5 = UIConfig.Instance;
                minimapSize = uiCfg5.MinimapSize;
                minimapPanelGo = CreatePanel("MinimapPanel", new Vector2(Screen.width - minimapSize - 10, 10), new Vector2(minimapSize, minimapSize));
                
                // Minimap texture
                minimapTexture = new RenderTexture((int)minimapSize, (int)minimapSize, 16);
                minimapTexture.Create();
                
                if (minimapImage == null)
                {
                    var imgGo = new GameObject("MinimapImage");
                    var imgRect = imgGo.AddComponent<RectTransform>();
                    imgRect.SetParent(minimapPanelGo.transform);
                    imgRect.anchoredPosition = Vector2.zero;
                    imgRect.localScale = Vector3.one;
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
        /// Set the character name directly (e.g., after character creation).
        /// </summary>
        public void SetCharacterName(string name)
        {
            if (playerNameText != null && !string.IsNullOrEmpty(name))
            {
                playerNameText.text = $"Commander: {name}";
            }
        }

        /// <summary>
        /// Set the corporation name directly (e.g., after login/registration).
        /// </summary>
        public void SetCorporationName(string name)
        {
            if (playerCorporationText != null && !string.IsNullOrEmpty(name))
            {
                playerCorporationText.text = $"Corporation: {name}";
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
                // Show corporation from NetworkManager if available
                var nm = DoraMCU.Client.Network.NetworkManager.Instance;
                if (playerCorporationText != null && nm != null && !string.IsNullOrEmpty(nm.CorporationName))
                {
                    playerCorporationText.text = $"Corporation: {nm.CorporationName}";
                }
                if (playerStatusText != null)
                {
                    playerStatusText.text = $"Status: {(player.commander.alive ? "Alive" : "Deceased")}";
                    playerStatusText.color = player.commander.alive ? UIConfig.Instance.StatusAlive : UIConfig.Instance.StatusDead;
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
        /// Update fleet and asset information with scrollable fleet items.
        /// </summary>
        public void UpdateFleetInfo(DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (player == null) return;

            var fleetCount = player.fleets?.Length ?? 0;
            var shipCount = player.ships?.Length ?? 0;
            var planetCount = player.planets?.Length ?? 0;

            if (fleetText != null)
            {
                fleetText.text = $"Fleets: {fleetCount}";
            }

            if (shipsText != null)
            {
                shipsText.text = $"Ships: {shipCount}";
            }

            if (planetsText != null)
            {
                planetsText.text = $"Planets: {planetCount}";
            }

            // Update scrollable fleet items
            if (fleetContent != null)
            {
                // Clear existing fleet items
                foreach (Transform child in fleetContent)
                {
                    Object.Destroy(child.gameObject);
                }

                // Add fleet items to scroll content
                if (player.fleets != null)
                {
                    foreach (var fleet in player.fleets)
                    {
                        var itemGo = new GameObject("FleetItem");
                        var itemRect = itemGo.AddComponent<RectTransform>();
                        itemRect.SetParent(fleetContent);
                        itemRect.anchorMin = new Vector2(0, 1);
                        itemRect.anchorMax = new Vector2(1, 1);
                        itemRect.anchoredPosition = Vector2.zero;
                        itemRect.sizeDelta = new Vector2(0, 20);
                        itemRect.localScale = Vector3.one;

                        var itemText = itemGo.AddComponent<Text>();
                        itemText.text = fleet.name ?? "Unnamed Fleet";
                        itemText.fontSize = UIConfig.Instance.FontSizeSmall;
                        itemText.font = FontLoader.Instance.DefaultFont;
                        itemText.color = UIConfig.Instance.TextDefault;
                        itemText.horizontalOverflow = HorizontalWrapMode.Overflow;
                    }
                }

                // Add ship items to scroll content
                if (player.ships != null)
                {
                    foreach (var ship in player.ships)
                    {
                        var itemGo = new GameObject("ShipItem");
                        var itemRect = itemGo.AddComponent<RectTransform>();
                        itemRect.SetParent(fleetContent);
                        itemRect.anchorMin = new Vector2(0, 1);
                        itemRect.anchorMax = new Vector2(1, 1);
                        itemRect.anchoredPosition = Vector2.zero;
                        itemRect.sizeDelta = new Vector2(0, 20);
                        itemRect.localScale = Vector3.one;

                        var itemText = itemGo.AddComponent<Text>();
                        itemText.text = $"  {ship.name ?? "Unnamed Ship"}";
                        itemText.fontSize = UIConfig.Instance.FontSizeTiny;
                        itemText.font = FontLoader.Instance.DefaultFont;
                        itemText.color = UIConfig.Instance.TextLoading;
                        itemText.horizontalOverflow = HorizontalWrapMode.Overflow;
                    }
                }
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
                connectionStatusText.color = UIConfig.Instance.TextSuccess;
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
                connectionStatusText.color = UIConfig.Instance.TextError;
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
                connectionStatusText.color = UIConfig.Instance.TextWarning;
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
            rectTransform.anchorMax = Vector2.zero;
            rectTransform.anchoredPosition = position;
            rectTransform.sizeDelta = size;

            var image = panel.AddComponent<Image>();
            image.color = UIConfig.Instance.PanelBackground;

            return panel;
        }

        private void CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
        {
            var textGo = new GameObject(name);
            textGo.transform.SetParent(parent);
            var textRect = textGo.GetComponent<RectTransform>();
            if (textRect == null) textRect = textGo.AddComponent<RectTransform>();
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.zero;
            textRect.anchoredPosition = position;
            textRect.sizeDelta = new Vector2(200, 30);
            textRect.localRotation = Quaternion.identity;
            textRect.localScale = Vector3.one;

            textRef = textGo.AddComponent<Text>();
            textRef.text = defaultText;
            textRef.fontSize = fontSize;
            textRef.font = FontLoader.Instance.DefaultFont;
            textRef.color = UIConfig.Instance.TextDefault;
            textRef.horizontalOverflow = HorizontalWrapMode.Overflow;
            textRef.verticalOverflow = VerticalWrapMode.Overflow;
        }

        #endregion
    }
}
