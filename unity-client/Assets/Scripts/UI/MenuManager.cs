using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Manages all menu systems including pause menu, settings, game selection, and main menu.
    /// Responds to Escape key and provides navigation between menu states.
    /// </summary>
    public class MenuManager : Singleton<MenuManager>
    {
        public enum MenuState { Playing, Paused, MainMenu, Settings, GameSelect, Inventory, Map }
        
        private MenuState currentState = MenuState.Playing;
        public MenuState CurrentState => currentState;

        [Header("Menu Panels")]
        [SerializeField] private GameObject pauseMenuPanel;
        [SerializeField] private GameObject mainMenuPanel;
        [SerializeField] private GameObject settingsPanel;
        [SerializeField] private GameObject gameSelectPanel;

        [Header("Pause Menu Elements")]
        [SerializeField] private Button resumeButton;
        [SerializeField] private Button settingsButton;
        [SerializeField] private Button gameSelectButton;
        [SerializeField] private Button inventoryButton;
        [SerializeField] private Button mapButton;
        [SerializeField] private Button quitButton;

        [Header("Settings Elements")]
        [SerializeField] private Slider masterVolumeSlider;
        [SerializeField] private Slider musicVolumeSlider;
        [SerializeField] private Slider sfxVolumeSlider;
        [SerializeField] private Dropdown graphicsQualityDropdown;
        [SerializeField] private Toggle fullscreenToggle;
        [SerializeField] private Button applySettingsButton;
        [SerializeField] private Button backFromSettingsButton;

        [Header("Game Select Elements")]
        [SerializeField] private ScrollRect gameListScroll;
        [SerializeField] private Button backFromGameSelectButton;

        [Header("Canvas")]
        [SerializeField] private Canvas menuCanvas;

        private GameObject pauseMenuGo;
        private GameObject mainMenuGo;
        private GameObject settingsGo;
        private GameObject gameSelectGo;
        private List<string> availableGames = new();

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            SetupButtons();
            HideAllMenus();
            SetMenuState(MenuState.Playing);
        }

        void Update()
        {
            // Toggle pause menu on Escape
            if (Input.GetKeyDown(KeyCode.Escape))
            {
                if (currentState == MenuState.Playing)
                {
                    PauseGame();
                }
                else if (currentState == MenuState.Paused)
                {
                    ResumeGame();
                }
                else if (currentState == MenuState.Settings || 
                         currentState == MenuState.GameSelect ||
                         currentState == MenuState.Inventory ||
                         currentState == MenuState.Map)
                {
                    SetMenuState(MenuState.Paused);
                }
            }
        }

        /// <summary>
        /// Create all menu UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            // Get or create canvas
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("MenuCanvas");
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
            menuCanvas = canvas;

            // Create pause menu
            if (pauseMenuPanel == null)
            {
                pauseMenuGo = CreatePanel("PauseMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                pauseMenuGo.SetActive(false);

                var title = CreateText(null, "PauseTitle", pauseMenuGo.transform, "PAUSED", 48, new Vector2(0, Screen.height / 2 - 100));
                if (title != null) title.fontStyle = FontStyle.Bold;

                resumeButton = CreateButton(pauseMenuGo.transform, "Resume", "RESUME", new Vector2(0, Screen.height / 2 - 30), OnResume);
                settingsButton = CreateButton(pauseMenuGo.transform, "Settings", "SETTINGS", new Vector2(0, Screen.height / 2 - 70), OnSettings);
                gameSelectButton = CreateButton(pauseMenuGo.transform, "GameSelect", "GAME SELECT", new Vector2(0, Screen.height / 2 - 110), OnGameSelect);
                inventoryButton = CreateButton(pauseMenuGo.transform, "Inventory", "INVENTORY", new Vector2(0, Screen.height / 2 - 150), OnInventory);
                mapButton = CreateButton(pauseMenuGo.transform, "Map", "GALAXY MAP", new Vector2(0, Screen.height / 2 - 190), OnMap);
                quitButton = CreateButton(pauseMenuGo.transform, "Quit", "QUIT TO MAIN MENU", new Vector2(0, Screen.height / 2 - 230), OnQuitToMenu);
            }

            // Create main menu
            if (mainMenuPanel == null)
            {
                mainMenuGo = CreatePanel("MainMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                mainMenuGo.SetActive(false);

                var title = CreateText(null, "MainTitle", mainMenuGo.transform, "DoraMCU", 72, new Vector2(0, Screen.height / 2 + 50));
                if (title != null) title.fontStyle = FontStyle.Bold;

                CreateButton(mainMenuGo.transform, "NewGame", "NEW GAME", new Vector2(0, Screen.height / 2 - 20), OnNewGame);
                CreateButton(mainMenuGo.transform, "Continue", "CONTINUE", new Vector2(0, Screen.height / 2 - 60), OnContinue);
                CreateButton(mainMenuGo.transform, "MainSettings", "SETTINGS", new Vector2(0, Screen.height / 2 - 100), OnSettings);
                CreateButton(mainMenuGo.transform, "MainQuit", "QUIT", new Vector2(0, Screen.height / 2 - 140), OnQuit);
            }

            // Create settings panel
            if (settingsPanel == null)
            {
                settingsGo = CreatePanel("SettingsMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                settingsGo.SetActive(false);

                CreateText(null, "SettingsTitle", settingsGo.transform, "SETTINGS", 48, new Vector2(0, Screen.height / 2 + 150));

                // Audio settings
                CreateSlider(settingsGo.transform, "MasterVolume", "Master Volume", ref masterVolumeSlider, new Vector2(-150, Screen.height / 2 + 80));
                CreateSlider(settingsGo.transform, "MusicVolume", "Music Volume", ref musicVolumeSlider, new Vector2(-150, Screen.height / 2 + 30));
                CreateSlider(settingsGo.transform, "SFXVolume", "SFX Volume", ref sfxVolumeSlider, new Vector2(-150, Screen.height / 2 - 20));

                // Graphics settings
                CreateDropdown(settingsGo.transform, "GraphicsQuality", "Graphics Quality", ref graphicsQualityDropdown, new Vector2(-150, Screen.height / 2 - 80));
                CreateToggle(settingsGo.transform, "Fullscreen", "Fullscreen", ref fullscreenToggle, new Vector2(-150, Screen.height / 2 - 130));

                applySettingsButton = CreateButton(settingsGo.transform, "Apply", "APPLY", new Vector2(-100, Screen.height / 2 - 180), OnApplySettings);
                backFromSettingsButton = CreateButton(settingsGo.transform, "BackSettings", "BACK", new Vector2(100, Screen.height / 2 - 180), OnBackFromSettings);
            }

            // Create game select panel
            if (gameSelectPanel == null)
            {
                gameSelectGo = CreatePanel("GameSelectMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                gameSelectGo.SetActive(false);

                CreateText(null, "GameSelectTitle", gameSelectGo.transform, "SELECT GAME", 48, new Vector2(0, Screen.height / 2 + 150));

                // Game list scroll area
                var scrollGo = new GameObject("GameListScroll");
                scrollGo.transform.SetParent(gameSelectGo.transform);
                scrollGo.transform.anchoredPosition = new Vector2(0, Screen.height / 2 - 20);
                scrollGo.transform.localScale = Vector3.one;
                
                var scrollRect = scrollGo.AddComponent<ScrollRect>();
                var viewport = new GameObject("Viewport");
                viewport.transform.SetParent(scrollGo.transform);
                var viewportImage = viewport.AddComponent<Image>();
                viewportImage.color = new Color(0, 0, 0, 0.5f);
                var viewportRect = viewport.AddComponent<RectTransform>();
                viewportRect.sizeDelta = new Vector2(400, 300);
                scrollRect.viewport = viewportRect;

                gameListScroll = scrollRect;

                backFromGameSelectButton = CreateButton(gameSelectGo.transform, "BackGameSelect", "BACK", new Vector2(0, Screen.height / 2 - 200), OnBackFromGameSelect);

                // Populate game list
                PopulateGameList();
            }
        }

        /// <summary>
        /// Setup button event listeners.
        /// </summary>
        private void SetupButtons()
        {
            // Buttons are created with callbacks in EnsureUIElements
        }

        /// <summary>
        /// Populate the game selection list with available games.
        /// </summary>
        private void PopulateGameList()
        {
            availableGames.Clear();
            availableGames.Add("Traveller");
            availableGames.Add("D&D 5e");
            availableGames.Add("Star Trek 4X");
            availableGames.Add("Babylon 5");
            availableGames.Add("Captain's Log");
            availableGames.Add("Star Wars");
            availableGames.Add("Starship Troopers");
            availableGames.Add("Generic RPG");

            if (gameListScroll != null)
            {
                foreach (var game in availableGames)
                {
                    var btnGo = new GameObject($"Game_{game}");
                    btnGo.transform.SetParent(gameListScroll.viewport.transform);
                    btnGo.transform.localScale = Vector3.one;
                    
                    var btn = btnGo.AddComponent<Button>();
                    var text = btnGo.AddComponent<Text>();
                    text.text = game;
                    text.fontSize = 16;
                    text.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                    text.color = Color.white;
                    text.alignment = TextAnchor.MiddleLeft;
                    
                    var rect = btnGo.GetComponent<RectTransform>();
                    rect.sizeDelta = new Vector2(380, 40);
                    
                    string gameName = game;
                    btn.onClick.AddListener(() => OnSelectGame(gameName));
                }
            }
        }

        #region Menu State Management

        public void SetMenuState(MenuState state)
        {
            currentState = state;

            switch (state)
            {
                case MenuState.Playing:
                    HideAllMenus();
                    Time.timeScale = 1f;
                    Cursor.lockState = CursorLockMode.Locked;
                    Cursor.visible = false;
                    break;

                case MenuState.Paused:
                    HideAllMenus();
                    if (pauseMenuGo != null) pauseMenuGo.SetActive(true);
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;

                case MenuState.MainMenu:
                    HideAllMenus();
                    if (mainMenuGo != null) mainMenuGo.SetActive(true);
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;

                case MenuState.Settings:
                    HideAllMenus();
                    if (settingsGo != null) settingsGo.SetActive(true);
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;

                case MenuState.GameSelect:
                    HideAllMenus();
                    if (gameSelectGo != null) gameSelectGo.SetActive(true);
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;

                case MenuState.Inventory:
                    HideAllMenus();
                    // Inventory UI handled by InventoryUI
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;

                case MenuState.Map:
                    HideAllMenus();
                    // Map UI handled by MapUI
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    break;
            }
        }

        private void HideAllMenus()
        {
            if (pauseMenuGo != null) pauseMenuGo.SetActive(false);
            if (mainMenuGo != null) mainMenuGo.SetActive(false);
            if (settingsGo != null) settingsGo.SetActive(false);
            if (gameSelectGo != null) gameSelectGo.SetActive(false);
        }

        #endregion

        #region Button Callbacks

        private void OnResume()
        {
            ResumeGame();
        }

        private void OnSettings()
        {
            SetMenuState(MenuState.Settings);
        }

        private void OnGameSelect()
        {
            SetMenuState(MenuState.GameSelect);
        }

        private void OnInventory()
        {
            var inv = InventoryUI.Instance;
            if (inv != null)
            {
                inv.SetVisible(true);
            }
            SetMenuState(MenuState.Inventory);
        }

        private void OnMap()
        {
            var map = MapUI.Instance;
            if (map != null)
            {
                map.SetVisible(true);
            }
            SetMenuState(MenuState.Map);
        }

        private void OnQuitToMenu()
        {
            SetMenuState(MenuState.MainMenu);
        }

        private void OnNewGame()
        {
            Debug.Log("[MenuManager] Starting new game");
            // Connect to server and start new game
            var nm = DoraMCU.Client.Network.NetworkManager.Instance;
            if (nm != null && !nm.IsConnected)
            {
                nm.Connect();
            }
            SetMenuState(MenuState.Playing);
        }

        private void OnContinue()
        {
            Debug.Log("[MenuManager] Continuing game");
            SetMenuState(MenuState.Playing);
        }

        private void OnQuit()
        {
            Application.Quit();
        }

        private void OnApplySettings()
        {
            // Apply audio settings
            AudioListener.volume = masterVolumeSlider?.value ?? 1f;
            
            // Apply graphics quality
            if (graphicsQualityDropdown != null)
            {
                switch (graphicsQualityDropdown.value)
                {
                    case 0: QualitySettings.SetQualityLevel(0); break; // Low
                    case 1: QualitySettings.SetQualityLevel(1); break; // Medium
                    case 2: QualitySettings.SetQualityLevel(2); break; // High
                    case 3: QualitySettings.SetQualityLevel(3); break; // Ultra
                }
            }

            // Apply fullscreen
            if (fullscreenToggle != null)
            {
                Screen.fullScreen = fullscreenToggle.isOn;
            }

            Debug.Log("[MenuManager] Settings applied");
        }

        private void OnBackFromSettings()
        {
            SetMenuState(MenuState.Paused);
        }

        private void OnBackFromGameSelect()
        {
            SetMenuState(MenuState.Paused);
        }

        private void OnSelectGame(string gameName)
        {
            Debug.Log($"[MenuManager] Selected game: {gameName}");
            // Notify GameManager to switch to selected game
            var gm = GameManager.Instance;
            if (gm != null)
            {
                gm.SwitchGame(gameName);
            }
            SetMenuState(MenuState.Playing);
        }

        #endregion

        #region Public API

        public void PauseGame()
        {
            SetMenuState(MenuState.Paused);
        }

        public void ResumeGame()
        {
            SetMenuState(MenuState.Playing);
        }

        public bool IsPaused()
        {
            return currentState != MenuState.Playing;
        }

        #endregion

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(menuCanvas.transform);
            
            var rectTransform = panel.AddComponent<RectTransform>();
            rectTransform.anchorMin = Vector2.zero;
            rectTransform.anchorMax = Vector2.one;
            rectTransform.anchoredPosition = position;
            rectTransform.sizeDelta = -size;

            var image = panel.AddComponent<Image>();
            image.color = new Color(0, 0, 0, 0.85f);

            return panel;
        }

        private Text CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
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
            textRef.alignment = TextAnchor.MiddleCenter;

            return textRef;
        }

        private Button CreateButton(Transform parent, string name, string buttonText, Vector2 position, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            btnGo.transform.anchoredPosition = position;
            btnGo.transform.localRotation = Quaternion.identity;
            btnGo.transform.localScale = Vector3.one;

            var rect = btnGo.GetComponent<RectTransform>();
            if (rect == null) rect = btnGo.AddComponent<RectTransform>();
            rect.sizeDelta = new Vector2(200, 40);

            var btn = btnGo.AddComponent<Button>();
            var image = btnGo.AddComponent<Image>();
            image.color = new Color(0.1f, 0.1f, 0.3f, 0.8f);

            var textGo = new GameObject("ButtonText");
            textGo.transform.SetParent(btnGo.transform);
            textGo.transform.localPosition = Vector3.zero;
            textGo.transform.localRotation = Quaternion.identity;
            textGo.transform.localScale = Vector3.one;

            var text = textGo.AddComponent<Text>();
            text.text = buttonText;
            text.fontSize = 16;
            text.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            text.color = Color.white;
            text.alignment = TextAnchor.MiddleCenter;

            var textRect = textGo.GetComponent<RectTransform>();
            textRect.sizeDelta = new Vector2(200, 40);

            btn.onClick.AddListener(onClick);
            return btn;
        }

        private void CreateSlider(Transform parent, string name, string label, ref Slider sliderRef, Vector2 position)
        {
            var sliderGo = new GameObject(name);
            sliderGo.transform.SetParent(parent);
            sliderGo.transform.anchoredPosition = position;
            sliderGo.transform.localRotation = Quaternion.identity;
            sliderGo.transform.localScale = Vector3.one;

            var rect = sliderGo.GetComponent<RectTransform>();
            if (rect == null) rect = sliderGo.AddComponent<RectTransform>();
            rect.sizeDelta = new Vector2(300, 30);

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(sliderGo.transform);
            labelGo.transform.localPosition = new Vector3(-130, 20, 0);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            labelText.color = Color.white;

            sliderRef = sliderGo.AddComponent<Slider>();
            sliderRef.minValue = 0;
            sliderRef.maxValue = 1;
            sliderRef.value = 0.7f;
        }

        private void CreateDropdown(Transform parent, string name, string label, ref Dropdown dropdownRef, Vector2 position)
        {
            var ddGo = new GameObject(name);
            ddGo.transform.SetParent(parent);
            ddGo.transform.anchoredPosition = position;
            ddGo.transform.localRotation = Quaternion.identity;
            ddGo.transform.localScale = Vector3.one;

            var rect = ddGo.GetComponent<RectTransform>();
            if (rect == null) rect = ddGo.AddComponent<RectTransform>();
            rect.sizeDelta = new Vector2(200, 30);

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(ddGo.transform);
            labelGo.transform.localPosition = new Vector3(0, 20, 0);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            labelText.color = Color.white;

            dropdownRef = ddGo.AddComponent<Dropdown>();
            dropdownRef.options.Add(new Dropdown.OptionData("Low"));
            dropdownRef.options.Add(new Dropdown.OptionData("Medium"));
            dropdownRef.options.Add(new Dropdown.OptionData("High"));
            dropdownRef.options.Add(new Dropdown.OptionData("Ultra"));
            dropdownRef.value = 2;
        }

        private void CreateToggle(Transform parent, string name, string label, ref Toggle toggleRef, Vector2 position)
        {
            var togGo = new GameObject(name);
            togGo.transform.SetParent(parent);
            togGo.transform.anchoredPosition = position;
            togGo.transform.localRotation = Quaternion.identity;
            togGo.transform.localScale = Vector3.one;

            var rect = togGo.GetComponent<RectTransform>();
            if (rect == null) rect = togGo.AddComponent<RectTransform>();
            rect.sizeDelta = new Vector2(200, 30);

            toggleRef = togGo.AddComponent<Toggle>();
            toggleRef.graphic = togGo.AddComponent<Image>();

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(togGo.transform);
            labelGo.transform.localPosition = new Vector3(20, 0, 0);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            labelText.color = Color.white;
        }

        #endregion
    }
}
