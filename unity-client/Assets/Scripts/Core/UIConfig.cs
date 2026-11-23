using UnityEngine;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Centralized UI configuration for the Unity client.
    /// Holds all colors, sizes, and font settings used across UI components.
    /// Follows the web client config.js pattern for externalized configuration.
    /// </summary>
    public class UIConfig : Singleton<UIConfig>
    {
        [Header("Panel Colors")]
        [Tooltip("Default panel background color (semi-transparent dark)")]
        [SerializeField] private Color panelBackground = new Color(0f, 0f, 0f, 0.7f);
        
        [Tooltip("Dark panel background for special panels (deep blue-black)")]
        [SerializeField] private Color darkPanelBackground = new Color(0.05f, 0.05f, 0.15f, 0.95f);

        [Tooltip("Button background color")]
        [SerializeField] private Color buttonBackground = new Color(0.1f, 0.1f, 0.3f, 0.8f);

        [Tooltip("Scroll viewport background")]
        [SerializeField] private Color scrollViewportBackground = new Color(0f, 0f, 0f, 0.5f);

        [Header("Text Colors")]
        [Tooltip("Default text color")]
        [SerializeField] private Color textDefault = Color.white;
        
        [Tooltip("Error text color")]
        [SerializeField] private Color textError = Color.red;
        
        [Tooltip("Success/connected text color")]
        [SerializeField] private Color textSuccess = Color.green;
        
        [Tooltip("Warning text color")]
        [SerializeField] private Color textWarning = Color.yellow;
        
        [Tooltip("Loading/secondary text color (light blue)")]
        [SerializeField] private Color textLoading = new Color(0.5f, 0.5f, 1f);
        
        [Tooltip("Disabled text color (gray)")]
        [SerializeField] private Color textDisabled = new Color(0.53f, 0.53f, 0.53f, 1f);

        [Header("Status Colors")]
        [Tooltip("Alive/connected status color")]
        [SerializeField] private Color statusAlive = Color.green;
        
        [Tooltip("Deceased/disconnected status color")]
        [SerializeField] private Color statusDead = Color.red;

        [Header("Font Sizes")]
        [Tooltip("Title font size")]
        [SerializeField] private int fontSizeTitle = 48;
        
        [Tooltip("Heading font size")]
        [SerializeField] private int fontSizeHeading = 18;
        
        [Tooltip("Normal text font size")]
        [SerializeField] private int fontSizeNormal = 14;
        
        [Tooltip("Small text font size")]
        [SerializeField] private int fontSizeSmall = 12;
        
        [Tooltip("Tiny text font size")]
        [SerializeField] private int fontSizeTiny = 10;

        [Header("Panel Sizes")]
        [Tooltip("Player stats panel size")]
        [SerializeField] private Vector2 playerStatsPanelSize = new Vector2(280, 180);
        
        [Tooltip("Resources panel size")]
        [SerializeField] private Vector2 resourcesPanelSize = new Vector2(200, 150);
        
        [Tooltip("Fleet panel size")]
        [SerializeField] private Vector2 fleetPanelSize = new Vector2(250, 100);
        
        [Tooltip("Connection panel size")]
        [SerializeField] private Vector2 connectionPanelSize = new Vector2(200, 40);
        
        [Tooltip("Minimap size")]
        [SerializeField] private float minimapSize = 200f;

        [Header("HUD Settings")]
        [Tooltip("HUD update interval in seconds")]
        [SerializeField] private float hudUpdateInterval = 2f;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        #region Panel Colors

        public Color PanelBackground => panelBackground;
        public Color DarkPanelBackground => darkPanelBackground;
        public Color ButtonBackground => buttonBackground;
        public Color ScrollViewportBackground => scrollViewportBackground;

        #endregion

        #region Text Colors

        public Color TextDefault => textDefault;
        public Color TextError => textError;
        public Color TextSuccess => textSuccess;
        public Color TextWarning => textWarning;
        public Color TextLoading => textLoading;
        public Color TextDisabled => textDisabled;

        #endregion

        #region Status Colors

        public Color StatusAlive => statusAlive;
        public Color StatusDead => statusDead;

        #endregion

        #region Font Sizes

        public int FontSizeTitle => fontSizeTitle;
        public int FontSizeHeading => fontSizeHeading;
        public int FontSizeNormal => fontSizeNormal;
        public int FontSizeSmall => fontSizeSmall;
        public int FontSizeTiny => fontSizeTiny;

        #endregion

        #region Panel Sizes

        public Vector2 PlayerStatsPanelSize => playerStatsPanelSize;
        public Vector2 ResourcesPanelSize => resourcesPanelSize;
        public Vector2 FleetPanelSize => fleetPanelSize;
        public Vector2 ConnectionPanelSize => connectionPanelSize;
        public float MinimapSize => minimapSize;

        #endregion

        #region HUD Settings

        public float HudUpdateInterval => hudUpdateInterval;

        #endregion
    }
}
