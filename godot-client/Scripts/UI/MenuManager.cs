using Godot;
using System.Collections.Generic;
using DoraMCU.Core;
using DoraMCU.Network;

namespace DoraMCU.UI
{
    /// <summary>
    /// Menu manager — handles main menu, pause menu, and settings panel.
    /// Manages state transitions: Login → Character Check → Creation → Main Menu → Playing.
    /// Godot Control-based UI replacing Unity's MenuManager.
    /// </summary>
    public partial class MenuManager : Control
    {
        // Menu states
        enum MenuState
        {
            Hidden,
            MainMenu,
            PauseMenu,
            Settings,
            GalaxySelect
        }

        MenuState _currentState = MenuState.Hidden;

        // UI nodes
        Panel _mainPanel;
        VBoxContainer _contentBox;
        Label _titleLabel;

        // References
        GameManager _gameManager;
        NetworkManager _networkManager;

        // Galaxy list
        List<NetworkManager.GalaxySummary> _galaxies = new();

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            _networkManager = GetTree().Root.GetNodeOrNull("NetworkManager") as NetworkManager;

            BuildUI();
            Visible = false;
        }

        /// <summary>
        /// Build the menu UI.
        /// </summary>
        void BuildUI()
        {
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            var centerContainer = new CenterContainer
            {
            };
            AddChild(centerContainer);

            _mainPanel = new Panel
            {
                Size = new Vector2(400, 500)
            };
            centerContainer.AddChild(_mainPanel);

            var panelStyle = new StyleBoxFlat
            {
                BgColor = new Color(0.05f, 0.05f, 0.1f, 0.95f),
                BorderColor = new Color(0.2f, 0.4f, 0.8f),
                BorderWidthLeft = 2,
                BorderWidthRight = 2,
                BorderWidthTop = 2,
                BorderWidthBottom = 2,
                CornerRadiusBottomLeft = 8,
                CornerRadiusTopLeft = 8,
                CornerRadiusBottomRight = 8,
                CornerRadiusTopRight = 8
            };
            _mainPanel.AddThemeStyleboxOverride("panel", panelStyle);

            _contentBox = new VBoxContainer();
            _contentBox.AddThemeConstantOverride("separation", 10);

            // Title
            _titleLabel = new Label
            {
                Text = "Main Menu",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _titleLabel.AddThemeFontSizeOverride("font_size", 28);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            _contentBox.AddChild(_titleLabel);

            _mainPanel.AddChild(_contentBox);
        }

        /// <summary>
        /// Clear all content except the title.
        /// </summary>
        void ClearContent()
        {
            while (_contentBox.GetChildCount() > 1)
            {
                var child = _contentBox.GetChild(1);
                _contentBox.RemoveChild(child);
                child.QueueFree();
            }
        }

        /// <summary>
        /// Show the main menu.
        /// </summary>
        public void ShowMainMenu()
        {
            _currentState = MenuState.MainMenu;
            _titleLabel.Text = "Main Menu";
            ClearContent();

            // Player info
            var infoLabel = new Label
            {
                Text = $"Welcome, {_gameManager?.Username ?? "Player"}",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            infoLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.8f, 1f));
            _contentBox.AddChild(infoLabel);

            if (!string.IsNullOrEmpty(_gameManager?.EmpireName))
            {
                var empireLabel = new Label
                {
                    Text = $"Corporation: {_gameManager.EmpireName}",
                    HorizontalAlignment = HorizontalAlignment.Center
                };
                empireLabel.AddThemeFontSizeOverride("font_size", 12);
                _contentBox.AddChild(empireLabel);
            }

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Select Galaxy button
            AddButton("SELECT GALAXY", OnSelectGalaxy);

            // Demo galaxy button
            AddButton("DEMO GALAXY", OnDemoGalaxy);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Settings button
            AddButton("SETTINGS", OnSettings);

            // Logout button
            AddButton("LOGOUT", OnLogout);
        }

        /// <summary>
        /// Show the pause menu.
        /// </summary>
        public void ShowPauseMenu()
        {
            _currentState = MenuState.PauseMenu;
            _titleLabel.Text = "Paused";
            ClearContent();

            AddButton("RESUME", OnResume);
            AddButton("SHIP BUILDER", OnShipBuilder);
            AddButton("SCAN SECTOR", OnScanSector);
            AddButton("SETTINGS", OnSettings);
            AddButton("SAVE GAME", OnSaveGame);
            AddButton("QUIT TO MENU", OnQuitToMenu);
        }

        /// <summary>
        /// Show the settings panel.
        /// </summary>
        void ShowSettings()
        {
            _currentState = MenuState.Settings;
            _titleLabel.Text = "Settings";
            ClearContent();

            var desc = new Label
            {
                Text = "Game Settings",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _contentBox.AddChild(desc);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Server URL
            var serverLabel = new Label { Text = "Server URL:" };
            _contentBox.AddChild(serverLabel);

            var serverField = new LineEdit
            {
                Text = _networkManager != null ? "" : "http://192.168.1.111:8080",
                PlaceholderText = "http://192.168.1.111:8080",
                SizeFlagsHorizontal = SizeFlags.Fill
            };
            _contentBox.AddChild(serverField);

            var applyBtn = new Button { Text = "Apply Server URL" };
            applyBtn.Connect(Button.SignalName.Pressed, Callable.From(() =>
            {
                var sc = GetTree().Root.GetNodeOrNull("ServerConfig") as DoraMCU.Core.ServerConfig;
                if (sc != null && !string.IsNullOrWhiteSpace(serverField.Text))
                {
                    sc.SetServerUrl(serverField.Text);
                    GD.Print($"[MenuManager] Server URL updated to {serverField.Text}");
                }
            }));
            _contentBox.AddChild(applyBtn);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Fullscreen toggle
            var fullscreenLabel = new Label { Text = "Display:" };
            _contentBox.AddChild(fullscreenLabel);

            var fullscreenBtn = new Button { Text = "Toggle Fullscreen" };
            fullscreenBtn.Connect(Button.SignalName.Pressed, Callable.From(() =>
            {
                DisplayServer.WindowSetMode(DisplayServer.WindowMode.ExclusiveFullscreen);
            }));
            _contentBox.AddChild(fullscreenBtn);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Back button
            AddButton("BACK", OnBackFromSettings);
        }

        /// <summary>
        /// Show galaxy selection screen.
        /// </summary>
        async void ShowGalaxySelect()
        {
            _currentState = MenuState.GalaxySelect;
            _titleLabel.Text = "Select Galaxy";
            ClearContent();

            var loadingLabel = new Label
            {
                Text = "Loading galaxies...",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _contentBox.AddChild(loadingLabel);

            // Fetch galaxies
            if (_networkManager != null)
            {
                _galaxies = await _networkManager.FetchGalaxiesAsync();
            }

            // Clear loading
            ClearContent();

            if (_galaxies.Count == 0)
            {
                var noGalaxies = new Label
                {
                    Text = "No galaxies available.\nUse DEMO GALAXY from main menu.",
                    HorizontalAlignment = HorizontalAlignment.Center
                };
                _contentBox.AddChild(noGalaxies);
            }
            else
            {
                foreach (var galaxy in _galaxies)
                {
                    var btn = new Button
                    {
                        Text = $"{galaxy.name} (Turn {galaxy.turn}, Players: {galaxy.playerCount})",
                        SizeFlagsHorizontal = SizeFlags.Fill,
                        CustomMinimumSize = new Vector2(0, 40)
                    };

                    var gName = galaxy.name; // capture
                    btn.Connect(Button.SignalName.Pressed, Callable.From(() =>
                    {
                        EnterGalaxy(gName);
                    }));

                    _contentBox.AddChild(btn);
                }
            }

            AddButton("BACK", OnBackFromGalaxySelect);
        }

        /// <summary>
        /// Enter a galaxy and start playing.
        /// </summary>
        void EnterGalaxy(string galaxyName)
        {
            if (_gameManager != null)
            {
                _gameManager.EnterGalaxy(galaxyName);
                Visible = false;
            }
        }

        /// <summary>
        /// Add a styled button to the menu.
        /// </summary>
        void AddButton(string text, System.Action onPressed)
        {
            var btn = new Button
            {
                Text = text,
                SizeFlagsHorizontal = SizeFlags.Fill,
                CustomMinimumSize = new Vector2(0, 35)
            };

            var btnStyle = new StyleBoxFlat
            {
                BgColor = new Color(0.15f, 0.25f, 0.45f),
                BorderColor = new Color(0.3f, 0.5f, 0.8f),
                BorderWidthLeft = 1,
                BorderWidthRight = 1,
                BorderWidthTop = 1,
                BorderWidthBottom = 1,
                CornerRadiusBottomLeft = 4,
                CornerRadiusTopLeft = 4,
                CornerRadiusBottomRight = 4,
                CornerRadiusTopRight = 4
            };
            btn.AddThemeStyleboxOverride("normal", btnStyle);

            var hoverStyle = new StyleBoxFlat
            {
                BgColor = new Color(0.25f, 0.4f, 0.65f),
                BorderColor = new Color(0.4f, 0.6f, 0.9f),
                BorderWidthLeft = 1,
                BorderWidthRight = 1,
                BorderWidthTop = 1,
                BorderWidthBottom = 1,
                CornerRadiusBottomLeft = 4,
                CornerRadiusTopLeft = 4,
                CornerRadiusBottomRight = 4,
                CornerRadiusTopRight = 4
            };
            btn.AddThemeStyleboxOverride("hover", hoverStyle);

            btn.AddThemeColorOverride("font_color", Colors.White);

            btn.Connect(Button.SignalName.Pressed, Callable.From(onPressed));
            _contentBox.AddChild(btn);
        }

        #region Button Handlers

        void OnSelectGalaxy()
        {
            ShowGalaxySelect();
        }

        void OnDemoGalaxy()
        {
            EnterGalaxy("Demo Galaxy");
        }

        void OnSettings()
        {
            ShowSettings();
        }

        void OnLogout()
        {
            if (_gameManager != null)
            {
                _gameManager.Logout();
                Visible = false;
            }
        }

        void OnResume()
        {
            if (_gameManager != null)
            {
                _gameManager.ResumeGame();
                Visible = false;
            }
        }

        void OnShipBuilder()
        {
            if (_gameManager != null)
            {
                _gameManager.ToggleShipBuild();
            }
        }

        void OnScanSector()
        {
            if (_gameManager != null)
            {
                _gameManager.ToggleExplore();
            }
        }

        async void OnSaveGame()
        {
            if (_gameManager != null)
            {
                await _gameManager.SaveGameAsync();
            }
        }

        void OnQuitToMenu()
        {
            if (_gameManager != null)
            {
                _gameManager.Logout();
                Visible = false;
            }
        }

        void OnBackFromSettings()
        {
            if (_currentState == MenuState.PauseMenu)
            {
                ShowPauseMenu();
            }
            else
            {
                ShowMainMenu();
            }
        }

        void OnBackFromGalaxySelect()
        {
            ShowMainMenu();
        }

        #endregion

        /// <summary>
        /// Show the menu manager.
        /// </summary>
        public void ShowMenu()
        {
            Visible = true;
            if (_currentState == MenuState.Hidden)
            {
                ShowMainMenu();
            }
        }

        /// <summary>
        /// Hide the menu manager.
        /// </summary>
        public void HideMenu()
        {
            Visible = false;
            _currentState = MenuState.Hidden;
        }

        /// <summary>
        /// Show the pause menu specifically.
        /// </summary>
        public void ShowPause()
        {
            Visible = true;
            ShowPauseMenu();
        }
    }
}
