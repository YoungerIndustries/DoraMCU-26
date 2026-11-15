using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// UI overlay for the battlefield viewer.
    /// <para>
    /// Displays unit stats, battlefield info, and environmental data.
    /// Uses Unity UI Canvas with text elements for information display.
    /// </para>
    /// </summary>
    public class BattlefieldUI : MonoBehaviour
    {
        [Header("UI References")]
        [SerializeField] private Text unitNameText;
        [SerializeField] private Text unitTypeText;
        [SerializeField] private Text unitTeamText;
        [SerializeField] private Text unitStrengthText;
        [SerializeField] private Text unitMoraleText;
        [SerializeField] private Text unitAttackText;
        [SerializeField] private Text unitDefenseText;
        [SerializeField] private Text unitMobilityText;
        [SerializeField] private Slider unitHealthBar;
        [SerializeField] private GameObject unitInfoPanel;

        [SerializeField] private Text locationText;
        [SerializeField] private Text terrainText;
        [SerializeField] private Text weatherText;
        [SerializeField] private Text timePeriodText;
        [SerializeField] private Text techLevelText;
        [SerializeField] private GameObject battlefieldInfoPanel;

        [SerializeField] private Text weatherDetailText;

        private GameObject unitInfoPanelGo;
        private GameObject battlefieldInfoPanelGo;
        private GameObject weatherPanelGo;

        void Start()
        {
            EnsureUIElements();
        }

        /// <summary>
        /// Ensures UI elements exist, creating them if needed.
        /// </summary>
        private void EnsureUIElements()
        {
            // Get or create canvas
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("BattlefieldCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                var rt = canvasGo.AddComponent<CanvasRenderer>();
                rt.canvas = canvas;
            }

            // Create unit info panel
            if (unitInfoPanel == null)
            {
                unitInfoPanelGo = CreatePanel("UnitInfoPanel", new Vector2(10, Screen.height - 200), new Vector2(250, 180));
                CreateText(ref unitNameText, "UnitNameText", unitInfoPanelGo.transform, "Unit Name", 16, new Vector2(10, 160));
                CreateText(ref unitTypeText, "UnitTypeText", unitInfoPanelGo.transform, "Type", 12, new Vector2(10, 140));
                CreateText(ref unitTeamText, "UnitTeamText", unitInfoPanelGo.transform, "Team", 12, new Vector2(10, 120));
                CreateText(ref unitStrengthText, "UnitStrengthText", unitInfoPanelGo.transform, "Strength", 12, new Vector2(10, 95));
                CreateText(ref unitMoraleText, "UnitMoraleText", unitInfoPanelGo.transform, "Morale", 12, new Vector2(10, 75));
                CreateText(ref unitAttackText, "UnitAttackText", unitInfoPanelGo.transform, "Attack", 12, new Vector2(10, 55));
                CreateText(ref unitDefenseText, "UnitDefenseText", unitInfoPanelGo.transform, "Defense", 12, new Vector2(10, 35));
                CreateText(ref unitMobilityText, "UnitMobilityText", unitInfoPanelGo.transform, "Mobility", 12, new Vector2(10, 15));

                // Health bar
                var barGo = new GameObject("HealthBar") { transform = { parent = unitInfoPanelGo.transform, anchoredPosition = new Vector2(10, 110), localScale = Vector3.one } };
                unitHealthBar = barGo.AddComponent<Slider>();
                unitHealthBar.minValue = 0;
                unitHealthBar.maxValue = 1;
                unitHealthBar.direction = Slider.Direction.LeftToRight;
            }

            // Create battlefield info panel
            if (battlefieldInfoPanel == null)
            {
                battlefieldInfoPanelGo = CreatePanel("BattlefieldInfoPanel", new Vector2(10, Screen.height - 100), new Vector2(300, 80));
                CreateText(ref locationText, "LocationText", battlefieldInfoPanelGo.transform, "Location", 14, new Vector2(10, 60));
                CreateText(ref terrainText, "TerrainText", battlefieldInfoPanelGo.transform, "Terrain", 12, new Vector2(10, 40));
                CreateText(ref weatherText, "WeatherText", battlefieldInfoPanelGo.transform, "Weather", 12, new Vector2(150, 40));
                CreateText(ref timePeriodText, "TimePeriodText", battlefieldInfoPanelGo.transform, "Time Period", 12, new Vector2(10, 20));
                CreateText(ref techLevelText, "TechLevelText", battlefieldInfoPanelGo.transform, "Tech Level", 12, new Vector2(150, 20));
            }

            // Create weather detail panel
            weatherPanelGo = CreatePanel("WeatherPanel", new Vector2(Screen.width - 210, Screen.height - 100), new Vector2(200, 80));
            CreateText(ref weatherDetailText, "WeatherDetailText", weatherPanelGo.transform, "Weather Details", 12, new Vector2(10, 60));
        }

        /// <summary>
        /// Shows unit info in the UI panel.
        /// </summary>
        public void ShowUnitInfo(UnitInfo unitInfo)
        {
            if (unitInfo == null) return;

            if (unitNameText != null) unitNameText.text = unitInfo.UnitName;
            if (unitTypeText != null) unitTypeText.text = $"Type: {unitInfo.UnitType}";
            if (unitTeamText != null) unitTeamText.text = $"Team: {unitInfo.Team}";

            if (unitInfo.Stats != null)
            {
                if (unitStrengthText != null) unitStrengthText.text = $"Strength: {unitInfo.Stats.strength}/{unitInfo.Stats.maxStrength}";
                if (unitMoraleText != null) unitMoraleText.text = $"Morale: {unitInfo.Stats.morale}";
                if (unitAttackText != null) unitAttackText.text = $"Attack: {unitInfo.Stats.attack}";
                if (unitDefenseText != null) unitDefenseText.text = $"Defense: {unitInfo.Stats.defense}";
                if (unitMobilityText != null) unitMobilityText.text = $"Mobility: {unitInfo.Stats.mobility}";
                if (unitHealthBar != null) unitHealthBar.value = unitInfo.Stats.HealthPercent;
            }

            if (unitInfoPanelGo != null) unitInfoPanelGo.SetActive(true);
        }

        /// <summary>
        /// Hides the unit info panel.
        /// </summary>
        public void HideUnitInfo()
        {
            if (unitInfoPanelGo != null) unitInfoPanelGo.SetActive(false);
        }

        /// <summary>
        /// Shows battlefield metadata in the info bar.
        /// </summary>
        public void ShowBattlefieldInfo(SceneMetadata metadata)
        {
            if (metadata == null) return;

            if (locationText != null) locationText.text = $"{metadata.city}, {metadata.country}";
            if (terrainText != null) terrainText.text = $"Terrain: {metadata.terrainType}";
            if (timePeriodText != null) timePeriodText.text = $"Period: {metadata.timePeriod}";
            if (techLevelText != null) techLevelText.text = $"Tech: TL{metadata.techLevel}";

            if (battlefieldInfoPanelGo != null) battlefieldInfoPanelGo.SetActive(true);
        }

        /// <summary>
        /// Shows weather and environmental details.
        /// </summary>
        public void ShowWeatherInfo(SceneBiosphere biosphere)
        {
            if (biosphere == null || weatherDetailText == null) return;

            string weatherInfo = $"Weather: {biosphere.weather}\n" +
                                $"Temp: {biosphere.temperature:F1}°C\n" +
                                $"Humidity: {biosphere.humidity:F0}%\n" +
                                $"Wind: {biosphere.windSpeed:F1} m/s\n" +
                                $"Season: {biosphere.season}";

            if (biosphere.isNight)
                weatherInfo += "\nTime: Night";
            else
                weatherInfo += "\nTime: Day";

            weatherDetailText.text = weatherInfo;

            if (weatherPanelGo != null) weatherPanelGo.SetActive(true);
        }

        /// <summary>
        /// Updates the unit health bar dynamically.
        /// </summary>
        public void UpdateUnitHealth(float healthPercent)
        {
            if (unitHealthBar != null)
                unitHealthBar.value = healthPercent;
        }

        /// <summary>
        /// Hides all UI panels.
        /// </summary>
        public void HideAll()
        {
            if (unitInfoPanelGo != null) unitInfoPanelGo.SetActive(false);
            if (battlefieldInfoPanelGo != null) battlefieldInfoPanelGo.SetActive(false);
            if (weatherPanelGo != null) weatherPanelGo.SetActive(false);
        }

        /// <summary>
        /// Creates a panel GameObject with background.
        /// </summary>
        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(transform);

            var rect = panel.AddComponent<RectTransform>();
            rect.anchoredPosition = position;
            rect.sizeDelta = size;

            var image = panel.AddComponent<Image>();
            image.color = new Color(0f, 0f, 0f, 0.7f);

            panel.AddComponent<CanvasRenderer>();

            return panel;
        }

        /// <summary>
        /// Creates a Text UI element.
        /// </summary>
        private void CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
        {
            var textGo = new GameObject(name);
            textGo.transform.SetParent(parent);

            var rect = textGo.AddComponent<RectTransform>();
            rect.anchoredPosition = position;
            rect.sizeDelta = new Vector2(230, 20);

            textRef = textGo.AddComponent<Text>();
            textRef.text = defaultText;
            textRef.fontSize = fontSize;
            textRef.color = Color.white;
            textRef.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");

            textGo.AddComponent<CanvasRenderer>();
        }
    }
}
