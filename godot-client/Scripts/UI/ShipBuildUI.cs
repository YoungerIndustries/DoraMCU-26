using Godot;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Ship build UI — panel for designing and building ships.
    /// Shows ship types, modules, resource costs, and build queue.
    /// Toggled with V key.
    /// </summary>
    public partial class ShipBuildUI : Control
    {
        // Ship types
        static readonly string[] ShipTypes = { "Fighter", "Corvette", "Frigate", "Cruiser", "Dreadnought" };

        // Modules
        static readonly string[] Modules = { "Weapons", "Shields", "Armor", "Engines", "Cargo", "Sensors" };

        // Costs per ship type (credits, minerals, energy)
        static readonly Dictionary<string, (long credits, long minerals, long energy)> ShipCosts = new()
        {
            { "Fighter", (100, 50, 25) },
            { "Corvette", (300, 150, 75) },
            { "Frigate", (800, 400, 200) },
            { "Cruiser", (2000, 1000, 500) },
            { "Dreadnought", (5000, 2500, 1250) }
        };

        // UI nodes
        Panel _mainPanel;
        VBoxContainer _contentBox;
        Label _titleLabel;
        Label _statusLabel;
        Button _buildButton;

        // Selection
        string _selectedShipType = "Fighter";
        List<string> _selectedModules = new();

        // References
        GameManager _gameManager;

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            BuildUI();
            Visible = false;
        }

        void BuildUI()
        {
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            var centerContainer = new CenterContainer
            {
            };
            AddChild(centerContainer);

            _mainPanel = new Panel
            {
                Size = new Vector2(450, 500)
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
                Text = "Ship Builder",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _titleLabel.AddThemeFontSizeOverride("font_size", 24);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            _contentBox.AddChild(_titleLabel);

            // Ship type selection
            var typeLabel = new Label { Text = "Ship Type:" };
            _contentBox.AddChild(typeLabel);

            foreach (var type in ShipTypes)
            {
                var btn = new Button
                {
                    Text = $"{type} — {ShipCosts[type].credits} credits, {ShipCosts[type].minerals} minerals, {ShipCosts[type].energy} energy",
                    SizeFlagsHorizontal = SizeFlags.Fill
                };

                if (type == _selectedShipType)
                    btn.Disabled = true;

                var t = type;
                btn.Connect(Button.SignalName.Pressed, Callable.From(() =>
                {
                    _selectedShipType = t;
                    ShowShipType();
                }));

                _contentBox.AddChild(btn);
            }

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 5) });

            // Module selection
            var modLabel = new Label { Text = "Modules (select up to 3):" };
            _contentBox.AddChild(modLabel);

            foreach (var module in Modules)
            {
                var check = new CheckBox { Text = module };
                var m = module;
                check.Connect(CheckButton.SignalName.Toggled, Callable.From(() => OnModuleToggled(check.ButtonPressed, m)));
                _contentBox.AddChild(check);
            }

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 5) });

            // Status
            _statusLabel = new Label
            {
                Text = "",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _statusLabel.AddThemeColorOverride("font_color", new Color(1f, 0.4f, 0.4f));
            _contentBox.AddChild(_statusLabel);

            // Build button
            _buildButton = new Button
            {
                Text = "BUILD SHIP",
                SizeFlagsHorizontal = SizeFlags.Fill,
                CustomMinimumSize = new Vector2(0, 40)
            };
            _buildButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnBuildPressed"));
            _contentBox.AddChild(_buildButton);

            _mainPanel.AddChild(_contentBox);
        }

        void ShowShipType()
        {
            // Rebuild ship type buttons to show selection
            var children = new List<Node>();
            for (int i = 0; i < _contentBox.GetChildCount(); i++)
            {
                children.Add(_contentBox.GetChild(i));
            }

            // Simple approach: just update status
            var cost = ShipCosts[_selectedShipType];
            SetStatus($"Selected: {_selectedShipType} ({cost.credits} credits, {cost.minerals} minerals, {cost.energy} energy)");
        }

        void OnModuleToggled(bool toggled, string module)
        {
            
            if (toggled)
            {
                if (_selectedModules.Count < 3)
                {
                    _selectedModules.Add(module);
                }
                else
                {
                    
                    SetStatus("Maximum 3 modules allowed");
                }
            }
            else
            {
                _selectedModules.Remove(module);
            }
        }

        async void OnBuildPressed()
        {
            if (_selectedModules.Count == 0)
            {
                SetStatus("Select at least one module");
                return;
            }

            SetStatus("Building ship...");
            _buildButton.Disabled = true;

            if (_gameManager != null)
            {
                await _gameManager.BuildShipAsync(_selectedShipType, new List<string>(_selectedModules));
                SetStatus($"Ship {_selectedShipType} built successfully!");
            }

            _buildButton.Disabled = false;
        }

        void SetStatus(string message)
        {
            if (_statusLabel != null)
                _statusLabel.Text = message;
            GD.Print($"[ShipBuildUI] {message}");
        }

        public void ShowPanel()
        {
            Visible = true;
        }

        public void HidePanel()
        {
            Visible = false;
        }
        public void TogglePanel() { Visible = !Visible; }
    }
}
