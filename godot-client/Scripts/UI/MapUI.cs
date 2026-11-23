using Godot;
using System.Collections.Generic;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Galaxy map UI — shows sector grid overview.
    /// Toggled with Tab key.
    /// </summary>
    public partial class MapUI : Control
    {
        Panel _mainPanel;
        VBoxContainer _contentBox;
        Label _titleLabel;
        Label _statusLabel;

        // Sector grid (10x10)
        const int GridSize = 10;
        Button[,] _sectorButtons = new Button[GridSize, GridSize];

        // References
        GameManager _gameManager;

        // Current sector
        int _currentRow = 0;
        int _currentCol = 0;

        // Scanned sectors
        HashSet<string> _scannedSectors = new();

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
                Size = new Vector2(500, 520)
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
                Text = "Galaxy Map",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _titleLabel.AddThemeFontSizeOverride("font_size", 22);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            _contentBox.AddChild(_titleLabel);

            // Sector grid
            var grid = new GridContainer
            {
                Columns = GridSize,
                Size = new Vector2(400, 400)
            };

            for (int row = 0; row < GridSize; row++)
            {
                for (int col = 0; col < GridSize; col++)
                {
                    var btn = new Button
                    {
                        Text = $"{row},{col}",
                        CustomMinimumSize = new Vector2(35, 35)
                    };

                    var r = row;
                    var c = col;
                    btn.Connect(Button.SignalName.Pressed, Callable.From(() =>
                    {
                        OnSectorClicked(r, c);
                    }));

                    _sectorButtons[row, col] = btn;
                    grid.AddChild(btn);
                }
            }

            _contentBox.AddChild(grid);

            // Status
            _statusLabel = new Label
            {
                Text = $"Current sector: {_currentRow},{_currentCol} | Scanned: {_scannedSectors.Count}",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _statusLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.8f, 1f));
            _contentBox.AddChild(_statusLabel);

            _mainPanel.AddChild(_contentBox);

            UpdateSectorColors();
        }

        void OnSectorClicked(int row, int col)
        {
            _currentRow = row;
            _currentCol = col;
            _statusLabel.Text = $"Current sector: {_currentRow},{_currentCol} | Scanned: {_scannedSectors.Count}";
            UpdateSectorColors();
            GD.Print($"[MapUI] Selected sector {row},{col}");
        }

        /// <summary>
        /// Mark a sector as scanned and update colors.
        /// </summary>
        public void MarkSectorScanned(int row, int col)
        {
            _scannedSectors.Add($"{row},{col}");
            UpdateSectorColors();
        }

        void UpdateSectorColors()
        {
            for (int row = 0; row < GridSize; row++)
            {
                for (int col = 0; col < GridSize; col++)
                {
                    var btn = _sectorButtons[row, col];
                    var key = $"{row},{col}";

                    StyleBoxFlat style;
                    if (row == _currentRow && col == _currentCol)
                    {
                        // Current sector — bright blue
                        style = new StyleBoxFlat
                        {
                            BgColor = new Color(0.3f, 0.6f, 1f),
                            BorderColor = new Color(0.5f, 0.8f, 1f),
                            BorderWidthLeft = 1,
                            BorderWidthRight = 1,
                            BorderWidthTop = 1,
                            BorderWidthBottom = 1
                        };
                    }
                    else if (_scannedSectors.Contains(key))
                    {
                        // Scanned — green
                        style = new StyleBoxFlat
                        {
                            BgColor = new Color(0.1f, 0.4f, 0.1f),
                            BorderColor = new Color(0.2f, 0.6f, 0.2f),
                            BorderWidthLeft = 1,
                            BorderWidthRight = 1,
                            BorderWidthTop = 1,
                            BorderWidthBottom = 1
                        };
                    }
                    else
                    {
                        // Unknown — dark
                        style = new StyleBoxFlat
                        {
                            BgColor = new Color(0.15f, 0.15f, 0.2f),
                            BorderColor = new Color(0.25f, 0.25f, 0.35f),
                            BorderWidthLeft = 1,
                            BorderWidthRight = 1,
                            BorderWidthTop = 1,
                            BorderWidthBottom = 1
                        };
                    }

                    btn.AddThemeStyleboxOverride("normal", style);
                    btn.AddThemeColorOverride("font_color", Colors.White);
                }
            }
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
