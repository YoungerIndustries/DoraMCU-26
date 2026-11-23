using Godot;
using System.Threading.Tasks;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Explore action panel — sector scanning interface.
    /// Shows sector coordinates and scan button.
    /// Toggled with X key.
    /// </summary>
    public partial class ExploreAction : Control
    {
        // UI nodes
        Panel _mainPanel;
        VBoxContainer _contentBox;
        Label _titleLabel;
        Label _sectorLabel;
        Label _statusLabel;
        SpinBox _rowSpinner;
        SpinBox _colSpinner;
        Button _scanButton;

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
                Size = new Vector2(350, 300)
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
                Text = "Sector Scanner",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _titleLabel.AddThemeFontSizeOverride("font_size", 22);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            _contentBox.AddChild(_titleLabel);

            // Sector coordinates
            var coordLabel = new Label { Text = "Sector Coordinates:" };
            _contentBox.AddChild(coordLabel);

            var hbox = new HBoxContainer();
            hbox.AddThemeConstantOverride("separation", 10);

            var rowLabel = new Label { Text = "Row:" };
            rowLabel.CustomMinimumSize = new Vector2(40, 0);
            hbox.AddChild(rowLabel);

            _rowSpinner = new SpinBox
            {
                MinValue = 0,
                MaxValue = 9,
                Step = 1,
                Value = 0,
                SizeFlagsHorizontal = SizeFlags.Fill
            };
            hbox.AddChild(_rowSpinner);

            _contentBox.AddChild(hbox);

            var hbox2 = new HBoxContainer();
            hbox2.AddThemeConstantOverride("separation", 10);

            var colLabel = new Label { Text = "Col:" };
            colLabel.CustomMinimumSize = new Vector2(40, 0);
            hbox2.AddChild(colLabel);

            _colSpinner = new SpinBox
            {
                MinValue = 0,
                MaxValue = 9,
                Step = 1,
                Value = 0,
                SizeFlagsHorizontal = SizeFlags.Fill
            };
            hbox2.AddChild(_colSpinner);

            _contentBox.AddChild(hbox2);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 5) });

            // Sector info
            _sectorLabel = new Label
            {
                Text = "Sector: 0,0",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _sectorLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.8f, 1f));
            _contentBox.AddChild(_sectorLabel);

            // Status
            _statusLabel = new Label
            {
                Text = "",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _statusLabel.AddThemeColorOverride("font_color", new Color(0.4f, 1f, 0.4f));
            _contentBox.AddChild(_statusLabel);

            // Scan button
            _scanButton = new Button
            {
                Text = "SCAN SECTOR",
                SizeFlagsHorizontal = SizeFlags.Fill,
                CustomMinimumSize = new Vector2(0, 40)
            };
            _scanButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnScanPressed"));
            _contentBox.AddChild(_scanButton);

            _mainPanel.AddChild(_contentBox);

            // Update sector label on spinner change
            _rowSpinner.Connect(SpinBox.SignalName.ValueChanged, new Callable(this, "OnCoordinateChanged"));
            _colSpinner.Connect(SpinBox.SignalName.ValueChanged, new Callable(this, "OnCoordinateChanged"));
        }

        void OnCoordinateChanged(double value)
        {
            int row = (int)_rowSpinner.Value;
            int col = (int)_colSpinner.Value;
            _sectorLabel.Text = $"Sector: {row},{col}";
        }

        async void OnScanPressed()
        {
            int row = (int)_rowSpinner.Value;
            int col = (int)_colSpinner.Value;

            SetStatus("Scanning sector...");
            _scanButton.Disabled = true;

            if (_gameManager != null)
            {
                await _gameManager.ScanSectorAsync(row, col);
                SetStatus("Scan complete");
            }

            _scanButton.Disabled = false;
        }

        void SetStatus(string message)
        {
            if (_statusLabel != null)
                _statusLabel.Text = message;
            GD.Print($"[ExploreAction] {message}");
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
