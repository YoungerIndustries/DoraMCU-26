using Godot;

namespace DoraMCU.UI
{
    /// <summary>
    /// Help overlay — shows all keyboard controls and shortcuts.
    /// Toggled with F1 key.
    /// </summary>
    public partial class HelpOverlay : Control
    {
        Panel _mainPanel;

        public override void _Ready()
        {
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
                Size = new Vector2(400, 450)
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

            var vbox = new VBoxContainer();
            vbox.AddThemeConstantOverride("separation", 8);

            // Title
            var title = new Label
            {
                Text = "Controls",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            title.AddThemeFontSizeOverride("font_size", 24);
            title.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            vbox.AddChild(title);

            vbox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 5) });

            // Controls list
            var controls = new (string key, string action)[]
            {
                ("V", "Toggle Ship Builder"),
                ("X", "Toggle Sector Scanner"),
                ("Tab", "Toggle Galaxy Map"),
                ("I", "Toggle Inventory"),
                ("F1", "Toggle Help (this screen)"),
                ("Escape", "Pause / Resume"),
                ("WASD", "Move camera"),
                ("Mouse", "Rotate camera"),
                ("Scroll", "Zoom in/out")
            };

            foreach (var (key, action) in controls)
            {
                var row = new HBoxContainer();

                var keyLabel = new Label { Text = key };
                keyLabel.CustomMinimumSize = new Vector2(60, 0);
                keyLabel.AddThemeColorOverride("font_color", new Color(1f, 0.85f, 0.2f));
                row.AddChild(keyLabel);

                var actionLabel = new Label { Text = action };
                row.AddChild(actionLabel);

                vbox.AddChild(row);
            }

            vbox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            var closeBtn = new Button
            {
                Text = "CLOSE (F1)",
                SizeFlagsHorizontal = SizeFlags.Fill,
                CustomMinimumSize = new Vector2(0, 35)
            };
            closeBtn.Connect(Button.SignalName.Pressed, new Callable(this, "OnClosePressed"));
            vbox.AddChild(closeBtn);

            _mainPanel.AddChild(vbox);
        }

        void OnClosePressed()
        {
            Visible = false;
        }

        public void ShowHelp()
        {
            Visible = true;
        }

        public void HideHelp()
        {
            Visible = false;
        }
        public void TogglePanel() { Visible = !Visible; }
    }
}
