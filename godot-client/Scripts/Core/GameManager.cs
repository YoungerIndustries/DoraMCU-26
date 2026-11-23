using Godot;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Newtonsoft.Json;
using DoraMCU.Network;
using DoraMCU.UI;

namespace DoraMCU.Core
{
    /// <summary>
    /// Main game manager - equivalent to Unity's GameManager.
    /// Manages game state, connects to backend server, and initializes systems.
    /// </summary>
    public partial class GameManager : Node
    {
        [Signal] public delegate void LoginSuccessEventHandler();
        [Signal] public delegate void LoginErrorEventHandler(string error);
        [Signal] public delegate void GalaxyEnteredEventHandler();
        [Signal] public delegate void ResourcesUpdatedEventHandler();
        [Signal] public delegate void CharacterCreatedEventHandler();

        [Export]
        public string ServerUrl = "http://localhost:8080";

        private bool _isConnected;
        private bool _demoMode;
        private NetworkManager _networkManager;

        // Game state
        public string Username { get; private set; }
        public string EmpireName { get; private set; }
        public string CharacterName { get; private set; }
        public Dictionary<string, long> Resources { get; private set; } = new Dictionary<string, long>();
        public string GalaxyName { get; private set; }
        public int Turn { get; private set; }

        // UI panel references
        LoginScreen _loginScreen;
        CharacterCreation _characterCreation;
        MainHUD _mainHUD;
        MenuManager _menuManager;
        ShipBuildUI _shipBuildUI;
        ExploreAction _exploreAction;
        HelpOverlay _helpOverlay;
        InventoryUI _inventoryUI;
        MapUI _mapUI;

        public new bool IsConnected => _isConnected;
        public bool DemoMode => _demoMode;

        public void SetLoginScreen(Control c) { _loginScreen = c as LoginScreen; }
        public void SetCharacterCreation(Control c) { _characterCreation = c as CharacterCreation; }
        public void SetMainHUD(Control c) { _mainHUD = c as MainHUD; }
        public void SetMenuManager(Control c) { _menuManager = c as MenuManager; }
        public void SetShipBuildUI(Control c) { _shipBuildUI = c as ShipBuildUI; }
        public void SetExploreAction(Control c) { _exploreAction = c as ExploreAction; }
        public void SetHelpOverlay(Control c) { _helpOverlay = c as HelpOverlay; }
        public void SetInventoryUI(Control c) { _inventoryUI = c as InventoryUI; }
        public void SetMapUI(Control c) { _mapUI = c as MapUI; }

        public override void _Ready()
        {
            GD.Print("[GameManager] Initializing...");
            _networkManager = GetTree().Root.GetNodeOrNull("NetworkManager") as NetworkManager;
            if (_networkManager != null)
            {
                _networkManager.Connect(NetworkManager.SignalName.LoginSuccess, new Callable(this, "OnLoginSuccess"));
                _networkManager.Connect(NetworkManager.SignalName.LoginError, new Callable(this, "OnLoginError"));
            }
            ConnectToServer();
        }

        public override void _Process(double delta)
        {
        }

        public override void _Input(InputEvent @event)
        {
            if (@event is InputEventKey keyEvent && keyEvent.Pressed && !keyEvent.Echo)
            {
                if (keyEvent.Keycode == Key.V) ToggleShipBuilder();
                else if (keyEvent.Keycode == Key.X) ToggleScanPanel();
                else if (keyEvent.Keycode == Key.Tab) ToggleMap();
                else if (keyEvent.Keycode == Key.I) ToggleInventory();
                else if (keyEvent.Keycode == Key.F1) ToggleHelp();
            }
        }

        private async void ConnectToServer()
        {
            try
            {
                using var client = new System.Net.Http.HttpClient();
                client.Timeout = TimeSpan.FromSeconds(5);

                var response = await client.GetAsync($"{ServerUrl}/api/status");
                if (response.IsSuccessStatusCode)
                {
                    _isConnected = true;
                    GD.Print("[GameManager] Connected to backend server");
                }
                else
                {
                    GD.PrintErr("[GameManager] Server not available, showing login screen");
                }
            }
            catch (Exception e)
            {
                GD.PrintErr($"[GameManager] Connection failed: {e.Message}");
            }
            // Show login screen regardless of connection status (dev login / demo mode available)
            ShowLoginScreen();
        }

        void ShowLoginScreen()
        {
            _loginScreen?.ShowScreen();
        }

        void ShowMainMenu()
        {
            _loginScreen?.HideScreen();
            _characterCreation?.HideScreen();
            _mainHUD?.ShowScreen();
            _menuManager?.ShowMainMenu();
        }

        #region Authentication

        public async Task LoginAsync(string username, string password)
        {
            if (_networkManager == null)
            {
                GD.PrintErr("[GameManager] NetworkManager not available");
                return;
            }
            var result = await _networkManager.LoginAsync(username, password);
            if (result != null)
            {
                Username = result.username;
                EmpireName = result.empireName;
            }
        }

        public async Task RegisterAsync(string username, string password)
        {
            if (_networkManager == null)
            {
                GD.PrintErr("[GameManager] NetworkManager not available");
                return;
            }
            var result = await _networkManager.RegisterAsync(username, password);
            if (result != null)
            {
                Username = result.username;
                EmpireName = result.empireName;
            }
        }

        public async Task DevLoginAsync()
        {
            if (_networkManager == null)
            {
                GD.PrintErr("[GameManager] NetworkManager not available");
                return;
            }
            var result = await _networkManager.DevLoginAsync();
            if (result != null)
            {
                Username = result.username;
                EmpireName = result.empireName;
            }
        }

        void OnLoginSuccess()
        {
            EmitSignal(SignalName.LoginSuccess);
            GD.Print("[GameManager] Login success — checking character");
            CheckCharacterAndProceed();
        }

        void OnLoginError(string error)
        {
            EmitSignal(SignalName.LoginError, error);
            GD.PrintErr($"[GameManager] Login error: {error}");
        }

        async void CheckCharacterAndProceed()
        {
            if (_networkManager == null) return;
            bool hasCharacters = await _networkManager.FetchOnboardingConfigAsync();
            if (hasCharacters)
            {
                ShowMainMenu();
            }
            else
            {
                _characterCreation?.ShowScreen();
            }
        }

        #endregion

        #region Character Creation

        public async Task CreateCharacterAsync(string characterName, int age, string occupation)
        {
            if (_networkManager == null) return;
            bool success = await _networkManager.CreateCharacterAsync(characterName, age, occupation);
            if (success)
            {
                CharacterName = characterName;
                EmitSignal(SignalName.CharacterCreated);
                GD.Print($"[GameManager] Character created: {characterName}");
                ShowMainMenu();
            }
        }

        #endregion

        #region Ship Building

        public async Task BuildShipAsync(string shipType, List<string> modules)
        {
            if (_networkManager == null) return;
            await _networkManager.BuildShipAsync(shipType, modules);
        }

        #endregion

        #region Exploration

        public async Task ScanSectorAsync(int row, int col)
        {
            if (_networkManager == null) return;
            int systemsFound = await _networkManager.ScanSectorAsync(row, col);
            GD.Print($"[GameManager] Sector {row},{col}: {systemsFound} systems found");
        }

        #endregion

        #region Resources

        public async Task FetchResourcesAsync()
        {
            if (_networkManager == null) return;
            Resources = await _networkManager.FetchResourcesAsync() ?? new Dictionary<string, long>();
            EmitSignal(SignalName.ResourcesUpdated);
            _mainHUD?.UpdateResources(Resources);
        }

        #endregion

        #region Demo Mode

        public void StartDemoMode()
        {
            _demoMode = true;
            GD.Print("[GameManager] Demo mode activated");

            var root = GetTree().Root;
            var mainScene = root.GetNodeOrNull("MainScene");
            if (mainScene != null)
            {
                var demoManager = mainScene.GetNodeOrNull("DemoModeManager");
                if (demoManager is DoraMCU.Core.DemoModeManager demo)
                {
                    demo.StartDemo();
                }
            }
            ShowMainMenu();
        }

        #endregion

        #region Panel Toggles

        public void ToggleShipBuilder()
        {
            _shipBuildUI?.TogglePanel();
        }

        public void ToggleScanPanel()
        {
            _exploreAction?.TogglePanel();
        }

        public void ToggleMap()
        {
            _mapUI?.TogglePanel();
        }

        public void ToggleInventory()
        {
            _inventoryUI?.TogglePanel();
        }

        public void ToggleHelp()
        {
            _helpOverlay?.TogglePanel();
        }

        public void ToggleShipBuild()
        {
            ToggleShipBuilder();
        }

        public void ToggleExplore()
        {
            ToggleScanPanel();
        }

        #endregion

        #region Menu Actions

        public void EnterGalaxy(string galaxyName)
        {
            GalaxyName = galaxyName;
            EmitSignal(SignalName.GalaxyEntered, galaxyName);
            ShowMainMenu();
            GD.Print($"[GameManager] Entered galaxy: {galaxyName}");
        }

        public void Logout()
        {
            _networkManager?.Logout();
            Username = null;
            EmpireName = null;
            CharacterName = null;
            Resources.Clear();
            _mainHUD?.HideHUD();
            _menuManager?.HideMenu();
            ShowLoginScreen();
        }

        public void ResumeGame()
        {
            _menuManager?.HideMenu();
        }

        public async Task SaveGameAsync()
        {
            if (_networkManager == null) return;
            await _networkManager.SaveGameAsync();
        }

        #endregion
    }
}
