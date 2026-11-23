using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Galaxy map UI overlay showing sectors, solar systems, and fleet positions.
    /// Allows navigation to different sectors and systems via click interaction.
    /// </summary>
    public class MapUI : Singleton<MapUI>
    {
        [Header("Map Panel")]
        [SerializeField] private GameObject mapPanel;
        [SerializeField] private Text mapTitle;
        [SerializeField] private Text sectorInfoText;
        [SerializeField] private Text systemInfoText;
        [SerializeField] private RawImage mapImage;
        [SerializeField] private ScrollRect mapScroll;

        [Header("Map Settings")]
        [SerializeField] private int mapSize = 1000;
        [SerializeField] private float sectorSize = 50f;
        [SerializeField] private float systemSize = 10f;
        [SerializeField] private float fleetMarkerSize = 15f;
        [SerializeField] private Color sectorColor = new Color(0.1f, 0.2f, 0.3f, 0.5f);
        [SerializeField] private Color playerSectorColor = new Color(0.2f, 0.5f, 0.2f, 0.7f);
        [SerializeField] private Color systemColor = Color.white;
        [SerializeField] private Color fleetColor = Color.yellow;
        [SerializeField] private Color enemyColor = Color.red;
        [SerializeField] private Color stationColor = Color.cyan;

        [Header("Legend")]
        [SerializeField] private GameObject legendPanel;
        [SerializeField] private Text legendText;

        private GameObject mapPanelGo;
        private GameObject mapContent;
        private GameObject legendPanelGo;
        private RenderTexture mapTexture;
        private Camera mapCamera;
        private GameObject mapRenderObject;

        private Dictionary<int, GameObject> sectorMarkers = new();
        private Dictionary<long, GameObject> systemMarkers = new();
        private Dictionary<long, GameObject> fleetMarkers = new();

        private int selectedSectorId = -1;
        private long selectedSystemId = -1;

        /// <summary>
        /// Event when a sector is clicked.
        /// </summary>
        public System.Action<int, int> OnSectorClicked;

        /// <summary>
        /// Event when a system is clicked.
        /// </summary>
        public System.Action<long> OnSystemClicked;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            HideMap();
        }

        void Update()
        {
            // Close map with Escape
            if (UnityEngine.Input.GetKeyDown(KeyCode.Escape) && IsVisible())
            {
                HideMap();
                var menu = MenuManager.Instance;
                if (menu != null)
                {
                    menu.SetMenuState(MenuManager.MenuState.Paused);
                }
            }
        }

        /// <summary>
        /// Create map UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("MapCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                canvasGo.AddComponent<CanvasRenderer>();
                canvasGo.AddComponent<GraphicRaycaster>();

                var eventSystem = Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }

            if (mapPanel == null)
            {
                // Main map panel (large, centered)
                mapPanelGo = new GameObject("MapPanel");
                mapPanelGo.transform.SetParent(canvas.transform);
                
                var rect = mapPanelGo.AddComponent<RectTransform>();
                rect.anchorMin = Vector2.zero;
                rect.anchorMax = Vector2.one;
                rect.anchoredPosition = Vector2.zero;
                rect.sizeDelta = new Vector2(-100, -100);

                var bg = mapPanelGo.AddComponent<Image>();
                bg.color = UIConfig.Instance.PanelBackground;

                // Title
                var titleGo = new GameObject("Title");
                titleGo.transform.SetParent(mapPanelGo.transform);
                var titleRect = titleGo.AddComponent<RectTransform>();
                titleRect.anchorMin = new Vector2(0, 1);
                titleRect.anchorMax = new Vector2(1, 1);
                titleRect.anchoredPosition = new Vector2(0, -10);
                titleRect.sizeDelta = new Vector2(0, -30);

                var titleText = titleGo.AddComponent<Text>();
                titleText.text = "GALAXY MAP";
                titleText.fontSize = 28;
                titleText.font = FontLoader.Instance.DefaultFont;
                titleText.color = Color.cyan;
                titleText.fontStyle = FontStyle.Bold;
                titleText.alignment = TextAnchor.MiddleCenter;

                // Sector info
                var sectorInfoGo = new GameObject("SectorInfo");
                sectorInfoGo.transform.SetParent(mapPanelGo.transform);
                var sectorInfoRect = sectorInfoGo.AddComponent<RectTransform>();
                sectorInfoRect.anchorMin = new Vector2(0, 0.9f);
                sectorInfoRect.anchorMax = new Vector2(0.5f, 0.95f);
                sectorInfoRect.anchoredPosition = Vector2.zero;
                sectorInfoRect.sizeDelta = Vector2.zero;

                sectorInfoText = sectorInfoGo.AddComponent<Text>();
                sectorInfoText.text = "Sector: ---";
                sectorInfoText.fontSize = 14;
                sectorInfoText.font = FontLoader.Instance.DefaultFont;
                sectorInfoText.color = UIConfig.Instance.TextDefault;
                sectorInfoText.horizontalOverflow = HorizontalWrapMode.Overflow;

                // System info
                var systemInfoGo = new GameObject("SystemInfo");
                systemInfoGo.transform.SetParent(mapPanelGo.transform);
                var systemInfoRect = systemInfoGo.AddComponent<RectTransform>();
                systemInfoRect.anchorMin = new Vector2(0.5f, 0.9f);
                systemInfoRect.anchorMax = new Vector2(1, 0.95f);
                systemInfoRect.anchoredPosition = Vector2.zero;
                systemInfoRect.sizeDelta = Vector2.zero;

                systemInfoText = systemInfoGo.AddComponent<Text>();
                systemInfoText.text = "System: ---";
                systemInfoText.fontSize = 14;
                systemInfoText.font = FontLoader.Instance.DefaultFont;
                systemInfoText.color = UIConfig.Instance.TextDefault;
                systemInfoText.horizontalOverflow = HorizontalWrapMode.Overflow;
                systemInfoText.alignment = TextAnchor.MiddleRight;

                // Map content area
                mapContent = new GameObject("MapContent");
                mapContent.transform.SetParent(mapPanelGo.transform);
                var mapRect = mapContent.AddComponent<RectTransform>();
                mapRect.anchorMin = new Vector2(0, 0);
                mapRect.anchorMax = new Vector2(0.8f, 0.88f);
                mapRect.anchoredPosition = Vector2.zero;
                mapRect.sizeDelta = Vector2.zero;

                var mapBg = mapContent.AddComponent<Image>();
                mapBg.color = UIConfig.Instance.DarkPanelBackground;

                // Create sectors grid
                CreateSectorsGrid();

                // Legend panel (right side)
                legendPanelGo = new GameObject("LegendPanel");
                legendPanelGo.transform.SetParent(mapPanelGo.transform);
                var legendRect = legendPanelGo.AddComponent<RectTransform>();
                legendRect.anchorMin = new Vector2(0.8f, 0);
                legendRect.anchorMax = new Vector2(1, 0.88f);
                legendRect.anchoredPosition = Vector2.zero;
                legendRect.sizeDelta = Vector2.zero;

                var legendBg = legendPanelGo.AddComponent<Image>();
                legendBg.color = UIConfig.Instance.ButtonBackground;

                Text legendTitleRef = null;
                var legendTitle = CreateText(ref legendTitleRef, "LegendTitle", legendPanelGo.transform, "LEGEND", 16, new Vector2(0, 0.9f));
                if (legendTitle != null) legendTitle.fontStyle = FontStyle.Bold;

                var legendLines = new[]
                {
                    "■ Player Sector",
                    "■ Fleet",
                    "■ Enemy",
                    "■ Station",
                    "● Solar System",
                    "○ Unexplored"
                };

                for (int i = 0; i < legendLines.Length; i++)
                {
                    float y = 0.7f - (i * 0.1f);
                    Text legendRef = null;
                    CreateText(ref legendRef, $"Legend_{i}", legendPanelGo.transform, legendLines[i], 12, new Vector2(0, y));
                }

                // Navigation buttons
                CreateButton(legendPanelGo.transform, "Navigate", "NAVIGATE", new Vector2(0, 0.15f), new Vector2(1, 0.3f), OnNavigate);
                CreateButton(legendPanelGo.transform, "Center", "CENTER VIEW", new Vector2(0, 0.05f), new Vector2(1, 0.12f), OnCenterView);
            }
        }

        /// <summary>
        /// Create sector grid markers.
        /// </summary>
        private void CreateSectorsGrid()
        {
            // Clear existing markers
            foreach (var marker in sectorMarkers.Values)
            {
                GameObject.Destroy(marker);
            }
            sectorMarkers.Clear();

            // Create a 20x20 grid of sectors
            for (int row = 0; row < 20; row++)
            {
                for (int col = 0; col < 20; col++)
                {
                    int sectorId = row * 20 + col;
                    var sectorGo = new GameObject($"Sector_{sectorId}");
                    sectorGo.transform.SetParent(mapContent.transform);

                    var rect = sectorGo.AddComponent<RectTransform>();
                    rect.anchoredPosition = new Vector2(
                        col * sectorSize - (20 * sectorSize) / 2,
                        -(row * sectorSize) + (20 * sectorSize) / 2
                    );
                    rect.sizeDelta = new Vector2(sectorSize - 2, sectorSize - 2);

                    var img = sectorGo.AddComponent<Image>();
                    img.color = sectorColor;

                    var btn = sectorGo.AddComponent<Button>();
                    int sId = sectorId;
                    int sRow = row;
                    int sCol = col;
                    btn.onClick.AddListener(() => OnSectorClick(sId, sRow, sCol));

                    // Sector label
                    var labelGo = new GameObject("Label");
                    labelGo.transform.SetParent(sectorGo.transform);
                    var labelRect = labelGo.AddComponent<RectTransform>();
                    labelRect.anchorMin = Vector2.zero;
                    labelRect.anchorMax = Vector2.one;
                    labelRect.sizeDelta = Vector2.zero;

                    var label = labelGo.AddComponent<Text>();
                    label.text = $"{row},{col}";
                    label.fontSize = 8;
                    label.font = FontLoader.Instance.DefaultFont;
                    label.color = UIConfig.Instance.TextDisabled;
                    label.alignment = TextAnchor.MiddleCenter;

                    sectorMarkers[sectorId] = sectorGo;
                }
            }
        }

        /// <summary>
        /// Handle sector click.
        /// </summary>
        private void OnSectorClick(int sectorId, int row, int col)
        {
            selectedSectorId = sectorId;
            selectedSystemId = -1;

            if (sectorInfoText != null)
            {
                sectorInfoText.text = $"Sector: {row},{col} (ID: {sectorId})";
            }

            // Highlight selected sector
            foreach (var kvp in sectorMarkers)
            {
                var img = kvp.Value.GetComponent<Image>();
                if (kvp.Key == sectorId)
                {
                    img.color = playerSectorColor;
                }
                else
                {
                    img.color = sectorColor;
                }
            }

            OnSectorClicked?.Invoke(row, col);
            Debug.Log($"[MapUI] Sector clicked: {row},{col}");
        }

        /// <summary>
        /// Update map with galaxy data.
        /// </summary>
        public void UpdateMap(DoraMCU.Client.Network.NetworkManager.GalaxyInfo galaxy, DoraMCU.Client.Network.NetworkManager.PlayerInfo player)
        {
            if (galaxy == null) return;

            // Update sector markers with galaxy data
            if (galaxy.sectors != null)
            {
                foreach (var sector in galaxy.sectors)
                {
                    int sectorId = sector.id;
                    if (sectorMarkers.ContainsKey(sectorId))
                    {
                        var marker = sectorMarkers[sectorId];
                        var img = marker.GetComponent<Image>();

                        // Color based on controller
                        if (sector.controller != null && player != null && player.id == sector.controller)
                        {
                            img.color = playerSectorColor;
                        }
                        else if (sector.controller != null)
                        {
                            img.color = enemyColor;
                        }

                        // Update label with trade type
                        var label = marker.GetComponentInChildren<Text>();
                        if (label != null && !string.IsNullOrEmpty(sector.tradeType))
                        {
                            label.text = sector.tradeType.Substring(0, System.Math.Min(3, sector.tradeType.Length));
                        }
                    }

                    // Add system markers
                    if (sector.subSectors != null)
                    {
                        foreach (var subSector in sector.subSectors)
                        {
                            if (subSector.solarSystem != null)
                            {
                                AddSystemMarker(subSector.solarSystem, sector.row, sector.col);
                            }
                        }
                    }
                }
            }

            // Add fleet markers
            if (player != null && player.fleets != null)
            {
                foreach (var fleet in player.fleets)
                {
                    AddFleetMarker(fleet);
                }
            }
        }

        /// <summary>
        /// Add a solar system marker.
        /// </summary>
        private void AddSystemMarker(DoraMCU.Client.Network.NetworkManager.SolarSystemInfo system, int sectorRow, int sectorCol)
        {
            if (systemMarkers.ContainsKey(system.id)) return;

            var systemGo = new GameObject($"System_{system.id}");
            systemGo.transform.SetParent(mapContent.transform);

            var rect = systemGo.AddComponent<RectTransform>();
            float x = (sectorCol * sectorSize) - (20 * sectorSize) / 2;
            float y = -((sectorRow * sectorSize)) + (20 * sectorSize) / 2;
            rect.anchoredPosition = new Vector2(x, y);
            rect.sizeDelta = new Vector2(systemSize, systemSize);

            var img = systemGo.AddComponent<Image>();
            img.color = system.explored ? systemColor : UIConfig.Instance.TextDisabled;
            img.type = Image.Type.Filled;
            img.fillMethod = Image.FillMethod.Radial360;
            img.fillAmount = 1f;

            var btn = systemGo.AddComponent<Button>();
            long sysId = system.id;
            btn.onClick.AddListener(() => OnSystemClick(sysId));

            systemMarkers[system.id] = systemGo;
        }

        /// <summary>
        /// Add a fleet marker.
        /// </summary>
        private void AddFleetMarker(DoraMCU.Client.Network.NetworkManager.FleetInfo fleet)
        {
            if (fleetMarkers.ContainsKey(fleet.id)) return;

            var fleetGo = new GameObject($"Fleet_{fleet.id}");
            fleetGo.transform.SetParent(mapContent.transform);

            var rect = fleetGo.AddComponent<RectTransform>();
            float x = (fleet.sectorCol * sectorSize) + (fleet.subSectorCol * (sectorSize / 4)) - (20 * sectorSize) / 2;
            float y = -((fleet.sectorRow * sectorSize) + (fleet.subSectorRow * (sectorSize / 4))) + (20 * sectorSize) / 2;
            rect.anchoredPosition = new Vector2(x, y);
            rect.sizeDelta = new Vector2(fleetMarkerSize, fleetMarkerSize);

            var img = fleetGo.AddComponent<Image>();
            img.color = fleetColor;

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(fleetGo.transform);
            var labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.anchorMin = Vector2.zero;
            labelRect.anchorMax = Vector2.one;
            labelRect.sizeDelta = Vector2.zero;

            var label = labelGo.AddComponent<Text>();
            label.text = fleet.name?.Substring(0, 1) ?? "?";
            label.fontSize = 8;
            label.font = FontLoader.Instance.DefaultFont;
            label.color = Color.black;
            label.alignment = TextAnchor.MiddleCenter;

            fleetMarkers[fleet.id] = fleetGo;
        }

        /// <summary>
        /// Handle system click.
        /// </summary>
        private void OnSystemClick(long systemId)
        {
            selectedSystemId = systemId;

            if (systemInfoText != null)
            {
                systemInfoText.text = $"System: {systemId}";
            }

            OnSystemClicked?.Invoke(systemId);
            Debug.Log($"[MapUI] System clicked: {systemId}");
        }

        #region Button Callbacks

        private void OnNavigate()
        {
            if (selectedSectorId >= 0)
            {
                int row = selectedSectorId / 20;
                int col = selectedSectorId % 20;
                
                var input = DoraMCU.Client.Input.InputHandler.Instance;
                if (input != null)
                {
                    input.NavigateToSystem(row, col, 0, 0);
                }

                HideMap();
                var menu = MenuManager.Instance;
                if (menu != null)
                {
                    menu.ResumeGame();
                }
            }
        }

        private void OnCenterView()
        {
            // Center map view on player's current position
            var gm = GameManager.Instance;
            if (gm != null && gm.CurrentState != null && gm.CurrentState.Player != null)
            {
                var player = gm.CurrentState.Player;
                if (player.fleets != null && player.fleets.Length > 0)
                {
                    var fleet = player.fleets[0];
                    // Center on fleet position
                    Debug.Log($"[MapUI] Centering on fleet at {fleet.sectorRow},{fleet.sectorCol}");
                }
            }
        }

        #endregion

        #region Public API

        public void ShowMap()
        {
            if (mapPanelGo != null)
            {
                mapPanelGo.SetActive(true);
            }

            // Update map with current data
            var gm = GameManager.Instance;
            if (gm != null && gm.CurrentState != null)
            {
                UpdateMap(gm.CurrentState.Galaxy, gm.CurrentState.Player);
            }
        }

        public void HideMap()
        {
            if (mapPanelGo != null)
            {
                mapPanelGo.SetActive(false);
            }
        }

        public void SetVisible(bool visible)
        {
            if (visible)
            {
                ShowMap();
            }
            else
            {
                HideMap();
            }
        }

        public bool IsVisible()
        {
            return mapPanelGo != null && mapPanelGo.activeSelf;
        }

        public void RefreshMap()
        {
            var gm = GameManager.Instance;
            if (gm != null && gm.CurrentState != null)
            {
                UpdateMap(gm.CurrentState.Galaxy, gm.CurrentState.Player);
            }
        }

        #endregion

        #region UI Helpers

        private Text CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 anchorPos)
        {
            var textGo = new GameObject(name);
            textGo.transform.SetParent(parent);
            var rect = textGo.AddComponent<RectTransform>();
            rect.anchorMin = anchorPos;
            rect.anchorMax = new Vector2(anchorPos.x + 0.5f, anchorPos.y + 0.05f);
            rect.anchoredPosition = Vector2.zero;
            rect.sizeDelta = Vector2.zero;

            textRef = textGo.AddComponent<Text>();
            textRef.text = defaultText;
            textRef.fontSize = fontSize;
            textRef.font = FontLoader.Instance.DefaultFont;
            textRef.color = UIConfig.Instance.TextDefault;
            textRef.horizontalOverflow = HorizontalWrapMode.Overflow;

            return textRef;
        }

        private Button CreateButton(Transform parent, string name, string text, Vector2 anchorMin, Vector2 anchorMax, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var rect = btnGo.AddComponent<RectTransform>();
            rect.anchorMin = anchorMin;
            rect.anchorMax = anchorMax;
            rect.anchoredPosition = Vector2.zero;
            rect.sizeDelta = Vector2.zero;

            var btn = btnGo.AddComponent<Button>();
            var img = btnGo.AddComponent<Image>();
            img.color = UIConfig.Instance.ButtonBackground;

            var textGo = new GameObject("Text");
            textGo.transform.SetParent(btnGo.transform);
            var textRect = textGo.AddComponent<RectTransform>();
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.one;
            textRect.anchoredPosition = Vector2.zero;
            textRect.sizeDelta = Vector2.zero;

            var t = textGo.AddComponent<Text>();
            t.text = text;
            t.fontSize = 12;
            t.font = FontLoader.Instance.DefaultFont;
            t.color = UIConfig.Instance.TextDefault;
            t.alignment = TextAnchor.MiddleCenter;

            btn.onClick.AddListener(() => onClick());
            return btn;
        }

        #endregion
    }
}
