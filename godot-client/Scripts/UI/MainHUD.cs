using Godot;
using System.Collections.Generic;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Main HUD — displays resources, character info, and session data.
    /// Shown during gameplay with a top bar for resources and status.
    /// Godot Control-based UI replacing Unity's MainHUD.
    /// </summary>
    public partial class MainHUD : Control
    {
        // UI nodes
        Panel _topBar;
        HBoxContainer _resourceBox;
        Label _creditsLabel;
        Label _mineralsLabel;
        Label _energyLabel;
        Label _characterLabel;
        Label _galaxyLabel;
        Label _turnLabel;

        // References
        GameManager _gameManager;

        // Resources
        Dictionary<string, long> _resources = new();

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            BuildUI();
            Visible = false;

            // Connect to resource updates
            if (_gameManager != null)
            {
                _gameManager.Connect(GameManager.SignalName.ResourcesUpdated, new Callable(this, "OnResourcesUpdated"));
                _gameManager.Connect(GameManager.SignalName.GalaxyEntered, new Callable(this, "OnGalaxyEntered"));
            }
        }

        /// <summary>
        /// Build the HUD UI.
        /// </summary>
        void BuildUI()
        {
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            // Top bar
            _topBar = new Panel
            {
                AnchorBottom = 0f,
                OffsetBottom = 50
            };
            AddChild(_topBar);

            var barStyle = new StyleBoxFlat
            {
                BgColor = new Color(0.05f, 0.05f, 0.1f, 0.85f),
                BorderColor = new Color(0.2f, 0.4f, 0.8f),
                BorderWidthLeft = 0,
                BorderWidthRight = 0,
                BorderWidthTop = 0,
                BorderWidthBottom = 2
            };
            _topBar.AddThemeStyleboxOverride("panel", barStyle);

            // Horizontal layout
            var hbox = new HBoxContainer();
            hbox.AddThemeConstantOverride("separation", 20);
            hbox.AddChild(new Control { CustomMinimumSize = new Vector2(10, 0) });

            // Character name
            _characterLabel = new Label { Text = "Player" };
            _characterLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.8f, 1f));
            hbox.AddChild(_characterLabel);

            // Galaxy name
            _galaxyLabel = new Label { Text = "" };
            _galaxyLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 0.6f));
            hbox.AddChild(_galaxyLabel);

            // Turn counter
            _turnLabel = new Label { Text = "" };
            _turnLabel.AddThemeColorOverride("font_color", new Color(0.8f, 0.7f, 0.4f));
            hbox.AddChild(_turnLabel);

            // Spacer
            hbox.AddChild(new Control { SizeFlagsHorizontal = SizeFlags.Fill });

            // Resources section
            _resourceBox = new HBoxContainer();
            _resourceBox.AddThemeConstantOverride("separation", 15);

            // Credits
            _creditsLabel = new Label { Text = "Credits: 0" };
            _creditsLabel.AddThemeColorOverride("font_color", new Color(1f, 0.85f, 0.2f));
            _resourceBox.AddChild(_creditsLabel);

            // Minerals
            _mineralsLabel = new Label { Text = "Minerals: 0" };
            _mineralsLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.8f, 1f));
            _resourceBox.AddChild(_mineralsLabel);

            // Energy
            _energyLabel = new Label { Text = "Energy: 0" };
            _energyLabel.AddThemeColorOverride("font_color", new Color(0.4f, 1f, 0.4f));
            _resourceBox.AddChild(_energyLabel);

            hbox.AddChild(_resourceBox);

            hbox.AddChild(new Control { CustomMinimumSize = new Vector2(10, 0) });

            _topBar.AddChild(hbox);
        }

        /// <summary>
        /// Update resource display.
        /// </summary>
        public void UpdateResources(Dictionary<string, long> resources)
        {
            _resources = resources ?? new Dictionary<string, long>();

            long credits = 0;
            long minerals = 0;
            long energy = 0;

            if (_resources.TryGetValue("credits", out long c)) credits = c;
            if (_resources.TryGetValue("minerals", out long m)) minerals = m;
            if (_resources.TryGetValue("energy", out long e)) energy = e;

            _creditsLabel.Text = $"Credits: {credits:N0}";
            _mineralsLabel.Text = $"Minerals: {minerals:N0}";
            _energyLabel.Text = $"Energy: {energy:N0}";
        }

        /// <summary>
        /// Called when resources are updated from server.
        /// </summary>
        void OnResourcesUpdated(Dictionary<string, long> resources)
        {
            UpdateResources(resources);
        }

        /// <summary>
        /// Called when galaxy is entered.
        /// </summary>
        void OnGalaxyEntered(string galaxyName)
        {
            _galaxyLabel.Text = $"| {galaxyName}";
        }

        /// <summary>
        /// Update character info display.
        /// </summary>
        public void UpdateCharacterInfo(string name, string corporation)
        {
            if (!string.IsNullOrEmpty(name))
            {
                _characterLabel.Text = name;
            }
            if (!string.IsNullOrEmpty(corporation))
            {
                _characterLabel.Text += $" [{corporation}]";
            }
        }

        /// <summary>
        /// Update turn counter.
        /// </summary>
        public void UpdateTurn(int turn)
        {
            _turnLabel.Text = $"| Turn {turn}";
        }

        /// <summary>
        /// Show the HUD.
        /// </summary>
        public void ShowScreen()
        {
            ShowHUD();
        }

        /// <summary>
        /// Show the HUD.
        /// </summary>
        public void ShowHUD()
        {
            Visible = true;
            if (_gameManager != null)
            {
                UpdateCharacterInfo(_gameManager.CharacterName, _gameManager.EmpireName);
                UpdateResources(_gameManager.Resources);
            }
        }

        /// <summary>
        /// Hide the HUD.
        /// </summary>
        public void HideHUD()
        {
            Visible = false;
        }

        /// <summary>
        /// Toggle panel visibility.
        /// </summary>
        public void TogglePanel()
        {
            Visible = !Visible;
        }
    }
}
