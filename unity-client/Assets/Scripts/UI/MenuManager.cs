using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Manages all menu systems including pause menu, settings, game selection, and main menu.
    /// Responds to Escape key and provides navigation between menu states.
    /// </summary>
    public class MenuManager : Singleton<MenuManager>
    {
        public enum MenuState { Playing, Paused, MainMenu, Settings, GameSelect, Inventory, Map, Login, CharacterCreation }
        
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
        [SerializeField] private InputField serverUrlInput;
        [SerializeField] private Button applyServerUrlButton;
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

            // Delay login screen show to next frame so LoginScreen.Start() has run first
            StartCoroutine(ShowLoginAfterFrame());
        }

        /// <summary>
        /// Show login screen after one frame to ensure LoginScreen has initialized.
        /// </summary>
        private System.Collections.IEnumerator ShowLoginAfterFrame()
        {
            yield return null;
            var loginScreen = LoginScreen.Instance;
            if (loginScreen != null)
            {
                loginScreen.Show();
                loginScreen.OnLoginSuccess += OnLoginSuccess;
                SetMenuState(MenuState.Login);
            }
            else
            {
                SetMenuState(MenuState.Playing);
            }
        }

        void Update()
        {
            // Toggle pause menu on Escape
            if (UnityEngine.Input.GetKeyDown(KeyCode.Escape))
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

                // Add CanvasScaler for proper UI scaling
                var scaler = canvasGo.AddComponent<CanvasScaler>();
                scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
                scaler.referenceResolution = new Vector2(1920, 1080);
                scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
                scaler.matchWidthOrHeight = 0.5f;

                var eventSystem = UnityEngine.Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }
            else
            {
                // Ensure existing canvas has a CanvasScaler
                if (canvas.GetComponent<CanvasScaler>() == null)
                {
                    var scaler = canvas.gameObject.AddComponent<CanvasScaler>();
                    scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
                    scaler.referenceResolution = new Vector2(1920, 1080);
                    scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
                    scaler.matchWidthOrHeight = 0.5f;
                }
            }
            menuCanvas = canvas;

            // Create pause menu
            if (pauseMenuPanel == null)
            {
                pauseMenuGo = CreatePanel("PauseMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                pauseMenuGo.SetActive(false);

                Text pauseTitleRef = null;
                CreateText(ref pauseTitleRef, "PauseTitle", pauseMenuGo.transform, "PAUSED", 48, new Vector2(0, -100));
                if (pauseTitleRef != null) pauseTitleRef.fontStyle = FontStyle.Bold;

                resumeButton = CreateButton(pauseMenuGo.transform, "Resume", "RESUME", new Vector2(0, -30), OnResume);
                settingsButton = CreateButton(pauseMenuGo.transform, "Settings", "SETTINGS", new Vector2(0, -70), OnSettings);
                gameSelectButton = CreateButton(pauseMenuGo.transform, "GameSelect", "GAME SELECT", new Vector2(0, -110), OnGameSelect);
                inventoryButton = CreateButton(pauseMenuGo.transform, "Inventory", "INVENTORY", new Vector2(0, -150), OnInventory);
                mapButton = CreateButton(pauseMenuGo.transform, "Map", "GALAXY MAP", new Vector2(0, -190), OnMap);
                CreateButton(pauseMenuGo.transform, "ShipBuild", "SHIP BUILDER", new Vector2(0, -230), OnShipBuild);
                CreateButton(pauseMenuGo.transform, "ScanSector", "SCAN SECTOR", new Vector2(0, -270), OnScanSector);
                quitButton = CreateButton(pauseMenuGo.transform, "Quit", "QUIT TO LOGIN", new Vector2(0, -310), OnQuitToMenu);
            }

            // Create main menu
            if (mainMenuPanel == null)
            {
                mainMenuGo = CreatePanel("MainMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                mainMenuGo.SetActive(false);

                Text mainTitleRef = null;
                CreateText(ref mainTitleRef, "MainTitle", mainMenuGo.transform, "DoraMCU", 72, new Vector2(0, 50));
                if (mainTitleRef != null) mainTitleRef.fontStyle = FontStyle.Bold;

                CreateButton(mainMenuGo.transform, "NewGame", "NEW GAME", new Vector2(0, -20), OnNewGame);
                CreateButton(mainMenuGo.transform, "Continue", "CONTINUE", new Vector2(0, -60), OnContinue);
                CreateButton(mainMenuGo.transform, "MainSettings", "SETTINGS", new Vector2(0, -100), OnSettings);
                CreateButton(mainMenuGo.transform, "MainQuit", "QUIT", new Vector2(0, -140), OnQuit);
            }

            // Create settings panel
            if (settingsPanel == null)
            {
                settingsGo = CreatePanel("SettingsMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                settingsGo.SetActive(false);

                Text settingsTitleRef = null;
                CreateText(ref settingsTitleRef, "SettingsTitle", settingsGo.transform, "SETTINGS", 48, new Vector2(0, 150));

                // Audio settings
                CreateSlider(settingsGo.transform, "MasterVolume", "Master Volume", ref masterVolumeSlider, new Vector2(-150, 80));
                CreateSlider(settingsGo.transform, "MusicVolume", "Music Volume", ref musicVolumeSlider, new Vector2(-150, 30));
                CreateSlider(settingsGo.transform, "SFXVolume", "SFX Volume", ref sfxVolumeSlider, new Vector2(-150, -20));

                // Graphics settings
                CreateDropdown(settingsGo.transform, "GraphicsQuality", "Graphics Quality", ref graphicsQualityDropdown, new Vector2(-150, -80));
                CreateToggle(settingsGo.transform, "Fullscreen", "Fullscreen", ref fullscreenToggle, new Vector2(-150, -130));

                // Server URL field
                Text serverLabelRef = null;
                var serverLabel = CreateText(ref serverLabelRef, "ServerUrlLabel", settingsGo.transform, "Server URL", 14, new Vector2(-150, -170));
                if (serverLabel != null) serverLabel.alignment = TextAnchor.UpperLeft;

                serverUrlInput = CreateSettingsInputField(settingsGo.transform, "ServerUrlInput", new Vector2(-150, -190));
                var sc = DoraMCU.Client.Core.ServerConfig.Instance;
                if (sc != null && serverUrlInput != null)
                {
                    serverUrlInput.text = sc.ServerUrl;
                }

                applyServerUrlButton = CreateButton(settingsGo.transform, "ApplyServerUrl", "SAVE", new Vector2(100, -195), OnApplyServerUrl);

                applySettingsButton = CreateButton(settingsGo.transform, "Apply", "APPLY SETTINGS", new Vector2(-100, -230), OnApplySettings);
                backFromSettingsButton = CreateButton(settingsGo.transform, "BackSettings", "BACK", new Vector2(100, -230), OnBackFromSettings);
            }

            // Create game select panel
            if (gameSelectPanel == null)
            {
                gameSelectGo = CreatePanel("GameSelectMenu", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                gameSelectGo.SetActive(false);

                Text gameSelectTitleRef = null;
                CreateText(ref gameSelectTitleRef, "GameSelectTitle", gameSelectGo.transform, "SELECT GAME", 48, new Vector2(0, 150));

                // Game list scroll area
                var scrollGo = new GameObject("GameListScroll");
                var scrollRect2 = scrollGo.GetComponent<RectTransform>();
                if (scrollRect2 == null) scrollRect2 = scrollGo.AddComponent<RectTransform>();
                scrollRect2.SetParent(gameSelectGo.transform);
                scrollRect2.anchoredPosition = new Vector2(0, -20);
                scrollRect2.localScale = Vector3.one;
                
                var scrollRect = scrollGo.AddComponent<ScrollRect>();
                var viewport = new GameObject("Viewport");
                var vpRect = viewport.GetComponent<RectTransform>();
                if (vpRect == null) vpRect = viewport.AddComponent<RectTransform>();
                vpRect.SetParent(scrollGo.transform);
                var viewportImage = viewport.AddComponent<Image>();
                viewportImage.color = UIConfig.Instance.ScrollViewportBackground;
                vpRect.sizeDelta = new Vector2(400, 300);
                scrollRect.viewport = vpRect;

                gameListScroll = scrollRect;

                backFromGameSelectButton = CreateButton(gameSelectGo.transform, "BackGameSelect", "BACK", new Vector2(0, -200), OnBackFromGameSelect);

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
                    text.font = FontLoader.Instance.DefaultFont;
                    text.color = UIConfig.Instance.TextDefault;
                    text.alignment = TextAnchor.MiddleLeft;
                    
                    var btnRect2 = btnGo.GetComponent<RectTransform>();
                    if (btnRect2 == null) btnRect2 = btnGo.AddComponent<RectTransform>();
                    btnRect2.sizeDelta = new Vector2(380, 40);
                    
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
                    // Start gameplay systems
                    var gm = GameManager.Instance;
                    if (gm != null) gm.StartPlaying();
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

                case MenuState.Login:
                    HideAllMenus();
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    var ls = LoginScreen.Instance;
                    if (ls != null) ls.Show();
                    break;

                case MenuState.CharacterCreation:
                    HideAllMenus();
                    Time.timeScale = 0f;
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                    var cc = CharacterCreation.Instance;
                    if (cc != null) cc.Show();
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

        private void OnShipBuild()
        {
            var sb = ShipBuildUI.Instance;
            if (sb != null)
            {
                sb.Show();
            }
            // Keep pause menu active so ship build overlays it
        }

        private void OnScanSector()
        {
            var ea = ExploreAction.Instance;
            if (ea != null)
            {
                ea.ScanCurrentSector();
            }
            ResumeGame();
        }

        private void OnQuitToMenu()
        {
            // Show confirmation dialog before logging out
            ModalDialog.Instance.ShowConfirm(
                "Quit to Menu",
                "Are you sure you want to quit to the menu? Unsaved progress will be lost.",
                () =>
                {
                    var nm = DoraMCU.Client.Network.NetworkManager.Instance;
                    if (nm != null) nm.Logout();
                    ShowLogin();
                    ToastNotification.Instance.ShowInfo("Returned to menu");
                },
                null
            );
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
            // Set default galaxy for AI admin polling
            if (nm != null)
            {
                nm.SetCurrentGalaxy("Earth 2026");
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
            // Show confirmation dialog before quitting
            ModalDialog.Instance.ShowConfirm(
                "Quit Game",
                "Are you sure you want to quit? Your game will be auto-saved.",
                () =>
                {
                    // Auto-save before quitting
                    var nm = NetworkManager.Instance;
                    if (nm != null)
                    {
                        nm.SaveGame((success, error) =>
                        {
                            Debug.Log($"[MenuManager] Auto-save on quit: {(success ? "success" : error)}");
                        });
                    }
                    Application.Quit();
                },
                null
            );
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

        private void OnApplyServerUrl()
        {
            if (serverUrlInput != null && !string.IsNullOrEmpty(serverUrlInput.text.Trim()))
            {
                var sc = DoraMCU.Client.Core.ServerConfig.Instance;
                if (sc != null)
                {
                    sc.SetServerUrl(serverUrlInput.text.Trim());
                    Debug.Log($"[MenuManager] Server URL updated to {sc.ServerUrl}");
                }
            }
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
            // Hide overlays that should not persist during gameplay
            var sb = ShipBuildUI.Instance;
            if (sb != null) sb.Hide();
        }

        public bool IsPaused()
        {
            return currentState != MenuState.Playing;
        }

        /// <summary>
        /// Called when login succeeds. Checks for existing characters and routes accordingly.
        /// </summary>
        private void OnLoginSuccess(string username, string token)
        {
            Debug.Log($"[MenuManager] Login success for {username}, checking for existing characters");
            var ls = LoginScreen.Instance;
            if (ls != null)
            {
                ls.OnLoginSuccess -= OnLoginSuccess;
                ls.Hide();
            }

            // Mark as authenticated so sync loop can start
            var gm = GameManager.Instance;
            if (gm != null) gm.SetAuthenticated(true);

            // Show HUD now that login is complete
            var hud = MainHUD.Instance;
            if (hud != null)
            {
                hud.SetVisible(true);
                Debug.Log("[MenuManager] HUD made visible after login");
            }

            // Update HUD with corporation info from login response
            var nm = DoraMCU.Client.Network.NetworkManager.Instance;
            if (nm != null && !string.IsNullOrEmpty(nm.CorporationName))
            {
                if (hud != null)
                {
                    hud.SetCorporationName(nm.CorporationName);
                }
                Debug.Log($"[MenuManager] Corporation set: {nm.CorporationName}");
            }

            // Check if the user has existing characters
            if (nm != null)
            {
                nm.FetchOnboardingConfig((success, hasCharacters, error) =>
                {
                    if (success && !hasCharacters)
                    {
                        // No characters — show character creation
                        Debug.Log("[MenuManager] No existing characters, showing character creation");
                        ShowCharacterCreation();
                    }
                    else
                    {
                        // Has characters or error — go to main menu
                        Debug.Log($"[MenuManager] Has characters or error, showing main menu");
                        SetMenuState(MenuState.MainMenu);
                    }
                });
            }
            else
            {
                SetMenuState(MenuState.MainMenu);
            }
        }

        /// <summary>
        /// Show the login screen (e.g., from Quit to Main Menu).
        /// </summary>
        public void ShowLogin()
        {
            // Hide HUD when returning to login
            var hud = MainHUD.Instance;
            if (hud != null)
            {
                hud.SetVisible(false);
            }

            var ls = LoginScreen.Instance;
            if (ls != null)
            {
                ls.OnLoginSuccess += OnLoginSuccess;
                ls.Show();
            }
            SetMenuState(MenuState.Login);
        }

        /// <summary>
        /// Show the character creation screen.
        /// </summary>
        public void ShowCharacterCreation()
        {
            var cc = CharacterCreation.Instance;
            if (cc != null)
            {
                cc.OnCharacterCreated += OnCharacterCreated;
                cc.Show();
            }
            SetMenuState(MenuState.CharacterCreation);
        }

        /// <summary>
        /// Called when character creation completes. Transitions to Playing state.
        /// </summary>
        private void OnCharacterCreated(CharacterCreation.CharacterData data)
        {
            Debug.Log($"[MenuManager] Character created: {data.name} ({data.species} {data.career})");
            var cc = CharacterCreation.Instance;
            if (cc != null)
            {
                cc.OnCharacterCreated -= OnCharacterCreated;
                cc.Hide();
            }

            // Show and update HUD with character info
            var hud = MainHUD.Instance;
            if (hud != null)
            {
                hud.SetVisible(true);
                if (data.name != null)
                {
                    hud.SetCharacterName(data.name);
                }
                Debug.Log("[MenuManager] HUD made visible after character creation");
            }

            // Start the game
            SetMenuState(MenuState.Playing);
        }

        #endregion

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(menuCanvas.transform);
            
            var rectTransform = panel.AddComponent<RectTransform>();
            
            if (size.x >= Screen.width - 10 && size.y >= Screen.height - 10)
            {
                rectTransform.anchorMin = Vector2.zero;
                rectTransform.anchorMax = Vector2.one;
                rectTransform.anchoredPosition = Vector2.zero;
                rectTransform.sizeDelta = Vector2.zero;
            }
            else
            {
                rectTransform.anchorMin = Vector2.zero;
                rectTransform.anchorMax = Vector2.zero;
                rectTransform.anchoredPosition = position;
                rectTransform.sizeDelta = size;
            }

            var image = panel.AddComponent<Image>();
            image.color = UIConfig.Instance.PanelBackground;

            return panel;
        }

        private Text CreateText(ref Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
        {
            var textGo = new GameObject(name);
            textGo.transform.SetParent(parent);
            var textRect = textGo.GetComponent<RectTransform>();
            if (textRect == null) textRect = textGo.AddComponent<RectTransform>();
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.zero;
            textRect.anchoredPosition = position;
            textRect.sizeDelta = new Vector2(400, 50);
            textRect.localRotation = Quaternion.identity;
            textRect.localScale = Vector3.one;

            textRef = textGo.AddComponent<Text>();
            textRef.text = defaultText;
            textRef.fontSize = fontSize;
            textRef.font = FontLoader.Instance.DefaultFont;
            textRef.color = UIConfig.Instance.TextDefault;
            textRef.horizontalOverflow = HorizontalWrapMode.Overflow;
            textRef.verticalOverflow = VerticalWrapMode.Overflow;
            textRef.alignment = TextAnchor.MiddleCenter;

            return textRef;
        }

        private Button CreateButton(Transform parent, string name, string buttonText, Vector2 position, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var btnRect = btnGo.GetComponent<RectTransform>();
            if (btnRect == null) btnRect = btnGo.AddComponent<RectTransform>();
            btnRect.anchorMin = Vector2.zero;
            btnRect.anchorMax = Vector2.zero;
            btnRect.anchoredPosition = position;
            btnRect.sizeDelta = new Vector2(200, 40);
            btnRect.localRotation = Quaternion.identity;
            btnRect.localScale = Vector3.one;

            var btn = btnGo.AddComponent<Button>();
            var image = btnGo.AddComponent<Image>();
            image.color = UIConfig.Instance.ButtonBackground;

            var textGo = new GameObject("ButtonText");
            textGo.transform.SetParent(btnGo.transform);
            var textRect = textGo.GetComponent<RectTransform>();
            if (textRect == null) textRect = textGo.AddComponent<RectTransform>();
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.one;
            textRect.anchoredPosition = Vector2.zero;
            textRect.sizeDelta = Vector2.zero;
            textRect.localRotation = Quaternion.identity;
            textRect.localScale = Vector3.one;

            var text = textGo.AddComponent<Text>();
            text.text = buttonText;
            text.fontSize = 16;
            text.font = FontLoader.Instance.DefaultFont;
            text.color = UIConfig.Instance.TextDefault;
            text.alignment = TextAnchor.MiddleCenter;

            btn.onClick.AddListener(() => onClick());
            return btn;
        }

        private void CreateSlider(Transform parent, string name, string label, ref Slider sliderRef, Vector2 position)
        {
            var sliderGo = new GameObject(name);
            sliderGo.transform.SetParent(parent);
            var sliderRect = sliderGo.GetComponent<RectTransform>();
            if (sliderRect == null) sliderRect = sliderGo.AddComponent<RectTransform>();
            sliderRect.anchorMin = Vector2.zero;
            sliderRect.anchorMax = Vector2.zero;
            sliderRect.anchoredPosition = position;
            sliderRect.sizeDelta = new Vector2(300, 30);
            sliderRect.localRotation = Quaternion.identity;
            sliderRect.localScale = Vector3.one;

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(sliderGo.transform);
            var labelRect = labelGo.GetComponent<RectTransform>();
            if (labelRect == null) labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.anchorMin = Vector2.zero;
            labelRect.anchorMax = Vector2.zero;
            labelRect.anchoredPosition = new Vector2(-130, 20);
            labelRect.sizeDelta = new Vector2(100, 20);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = FontLoader.Instance.DefaultFont;
            labelText.color = UIConfig.Instance.TextDefault;

            sliderRef = sliderGo.AddComponent<Slider>();
            sliderRef.minValue = 0;
            sliderRef.maxValue = 1;
            sliderRef.value = 0.7f;
        }

        private void CreateDropdown(Transform parent, string name, string label, ref Dropdown dropdownRef, Vector2 position)
        {
            var ddGo = new GameObject(name);
            ddGo.transform.SetParent(parent);
            var ddRect = ddGo.GetComponent<RectTransform>();
            if (ddRect == null) ddRect = ddGo.AddComponent<RectTransform>();
            ddRect.anchorMin = Vector2.zero;
            ddRect.anchorMax = Vector2.zero;
            ddRect.anchoredPosition = position;
            ddRect.sizeDelta = new Vector2(200, 30);
            ddRect.localRotation = Quaternion.identity;
            ddRect.localScale = Vector3.one;

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(ddGo.transform);
            var labelRect = labelGo.GetComponent<RectTransform>();
            if (labelRect == null) labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.anchorMin = Vector2.zero;
            labelRect.anchorMax = Vector2.zero;
            labelRect.anchoredPosition = new Vector2(0, 20);
            labelRect.sizeDelta = new Vector2(200, 20);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = FontLoader.Instance.DefaultFont;
            labelText.color = UIConfig.Instance.TextDefault;

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
            var togRect = togGo.GetComponent<RectTransform>();
            if (togRect == null) togRect = togGo.AddComponent<RectTransform>();
            togRect.anchorMin = Vector2.zero;
            togRect.anchorMax = Vector2.zero;
            togRect.anchoredPosition = position;
            togRect.sizeDelta = new Vector2(200, 30);
            togRect.localRotation = Quaternion.identity;
            togRect.localScale = Vector3.one;

            toggleRef = togGo.AddComponent<Toggle>();
            toggleRef.graphic = togGo.AddComponent<Image>();

            var labelGo = new GameObject("Label");
            labelGo.transform.SetParent(togGo.transform);
            var labelRect = labelGo.GetComponent<RectTransform>();
            if (labelRect == null) labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.anchorMin = Vector2.zero;
            labelRect.anchorMax = Vector2.zero;
            labelRect.anchoredPosition = new Vector2(20, 0);
            labelRect.sizeDelta = new Vector2(150, 30);
            
            var labelText = labelGo.AddComponent<Text>();
            labelText.text = label;
            labelText.fontSize = 12;
            labelText.font = FontLoader.Instance.DefaultFont;
            labelText.color = UIConfig.Instance.TextDefault;
        }

        private InputField CreateSettingsInputField(Transform parent, string name, Vector2 position)
        {
            var inputGo = new GameObject(name);
            inputGo.transform.SetParent(parent);
            var inputRect = inputGo.GetComponent<RectTransform>();
            if (inputRect == null) inputRect = inputGo.AddComponent<RectTransform>();
            inputRect.anchorMin = Vector2.zero;
            inputRect.anchorMax = Vector2.zero;
            inputRect.anchoredPosition = position;
            inputRect.sizeDelta = new Vector2(300, 30);
            inputRect.localRotation = Quaternion.identity;
            inputRect.localScale = Vector3.one;

            var inputField = inputGo.AddComponent<InputField>();
            inputField.lineType = InputField.LineType.SingleLine;

            var image = inputGo.AddComponent<Image>();
            image.color = UIConfig.Instance.ButtonBackground;

            return inputField;
        }

        #endregion
    }
}
