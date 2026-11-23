using Godot;
using System.Threading.Tasks;
using DoraMCU.Core;
using DoraMCU.Network;

namespace DoraMCU.UI
{
    /// <summary>
    /// Login screen UI — the first screen shown to the player.
    /// Provides login, register, and dev login options.
    /// Godot Control-based UI replacing Unity's runtime-generated LoginScreen.
    /// </summary>
    public partial class LoginScreen : Control
    {
        // UI nodes
        LineEdit _usernameField;
        LineEdit _passwordField;
        Button _loginButton;
        Button _registerButton;
        Button _devLoginButton;
        Button _demoModeButton;
        Label _statusLabel;
        Label _titleLabel;
        Panel _mainPanel;

        // References
        GameManager _gameManager;
        NetworkManager _networkManager;

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            _networkManager = GetTree().Root.GetNodeOrNull("NetworkManager") as NetworkManager;

            BuildUI();
        }

        /// <summary>
        /// Build the entire login UI programmatically.
        /// </summary>
        void BuildUI()
        {
            // Full-screen background
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            // Center container
            var centerContainer = new CenterContainer();
            centerContainer.SetAnchorsPreset(Control.LayoutPreset.FullRect);
            AddChild(centerContainer);

            // Main panel
            _mainPanel = new Panel();
            _mainPanel.Size = new Vector2(400, 450);
            _mainPanel.SetAnchorsPreset(Control.LayoutPreset.TopWide);
            _mainPanel.AnchorRight = 1f;
            _mainPanel.AnchorBottom = 1f;
            _mainPanel.OffsetLeft = -200;
            _mainPanel.OffsetTop = -225;
            _mainPanel.OffsetRight = 200;
            _mainPanel.OffsetBottom = 225;
            centerContainer.AddChild(_mainPanel);

            // Panel style
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

            // Vertical box for content
            var vbox = new VBoxContainer();
            vbox.AddThemeConstantOverride("separation", 10);

            // Title
            _titleLabel = new Label();
            _titleLabel.Text = "DoraMCU";
            _titleLabel.HorizontalAlignment = HorizontalAlignment.Center;
            _titleLabel.AddThemeFontSizeOverride("font_size", 32);
            _titleLabel.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            vbox.AddChild(_titleLabel);

            // Subtitle
            var subtitle = new Label();
            subtitle.Text = "Space Strategy Game";
            subtitle.HorizontalAlignment = HorizontalAlignment.Center;
            subtitle.AddThemeFontSizeOverride("font_size", 14);
            subtitle.AddThemeColorOverride("font_color", new Color(0.6f, 0.7f, 0.9f));
            vbox.AddChild(subtitle);

            // Spacer
            vbox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            // Username label
            var userLabel = new Label { Text = "Username:" };
            vbox.AddChild(userLabel);

            // Username field
            _usernameField = new LineEdit();
            _usernameField.PlaceholderText = "Enter username";
            _usernameField.SizeFlagsHorizontal = SizeFlags.Fill;
            _usernameField.Connect(LineEdit.SignalName.TextSubmitted, new Callable(this, "OnLoginPressed"));
            vbox.AddChild(_usernameField);

            // Password label
            var passLabel = new Label { Text = "Password:" };
            vbox.AddChild(passLabel);

            // Password field
            _passwordField = new LineEdit();
            _passwordField.PlaceholderText = "Enter password";
            _passwordField.Secret = true;
            _passwordField.SizeFlagsHorizontal = SizeFlags.Fill;
            _passwordField.Connect(LineEdit.SignalName.TextSubmitted, new Callable(this, "OnLoginPressed"));
            vbox.AddChild(_passwordField);

            // Status label
            _statusLabel = new Label();
            _statusLabel.Text = "";
            _statusLabel.HorizontalAlignment = HorizontalAlignment.Center;
            _statusLabel.AddThemeColorOverride("font_color", new Color(1f, 0.4f, 0.4f));
            vbox.AddChild(_statusLabel);

            // Spacer
            vbox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 5) });

            // Login button
            _loginButton = CreateButton("LOGIN", new Color(0.2f, 0.6f, 0.2f));
            _loginButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnLoginPressed"));
            vbox.AddChild(_loginButton);

            // Register button
            _registerButton = CreateButton("REGISTER", new Color(0.2f, 0.4f, 0.6f));
            _registerButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnRegisterPressed"));
            vbox.AddChild(_registerButton);

            // Dev login button
            _devLoginButton = CreateButton("DEV LOGIN", new Color(0.5f, 0.5f, 0.5f));
            _devLoginButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnDevLoginPressed"));
            vbox.AddChild(_devLoginButton);

            // Demo mode button
            _demoModeButton = CreateButton("DEMO MODE (Offline)", new Color(0.4f, 0.3f, 0.5f));
            _demoModeButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnDemoModePressed"));
            vbox.AddChild(_demoModeButton);

            _mainPanel.AddChild(vbox);

            // Initially hidden — GameManager shows us
            Visible = false;
        }

        /// <summary>
        /// Create a styled button.
        /// </summary>
        Button CreateButton(string text, Color color)
        {
            var button = new Button
            {
                Text = text,
                SizeFlagsHorizontal = SizeFlags.Fill,
                CustomMinimumSize = new Vector2(0, 35)
            };

            var btnStyle = new StyleBoxFlat
            {
                BgColor = color,
                BorderColor = color.Lightened(0.2f),
                BorderWidthLeft = 1,
                BorderWidthRight = 1,
                BorderWidthTop = 1,
                BorderWidthBottom = 1,
                CornerRadiusBottomLeft = 4,
                CornerRadiusTopLeft = 4,
                CornerRadiusBottomRight = 4,
                CornerRadiusTopRight = 4
            };
            button.AddThemeStyleboxOverride("normal", btnStyle);

            var hoverStyle = new StyleBoxFlat
            {
                BgColor = color.Lightened(0.15f),
                BorderColor = color.Lightened(0.3f),
                BorderWidthLeft = 1,
                BorderWidthRight = 1,
                BorderWidthTop = 1,
                BorderWidthBottom = 1,
                CornerRadiusBottomLeft = 4,
                CornerRadiusTopLeft = 4,
                CornerRadiusBottomRight = 4,
                CornerRadiusTopRight = 4
            };
            button.AddThemeStyleboxOverride("hover", hoverStyle);

            button.AddThemeColorOverride("font_color", Colors.White);
            return button;
        }

        /// <summary>
        /// Handle login button press.
        /// </summary>
        async void OnLoginPressed()
        {
            string username = _usernameField.Text;
            string password = _passwordField.Text;

            if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
            {
                SetStatus("Please enter username and password");
                return;
            }

            SetStatus("Logging in...");
            SetButtonsEnabled(false);

            if (_gameManager != null)
            {
                await _gameManager.LoginAsync(username, password);
            }

            SetButtonsEnabled(true);
        }

        /// <summary>
        /// Handle register button press.
        /// </summary>
        async void OnRegisterPressed()
        {
            string username = _usernameField.Text;
            string password = _passwordField.Text;

            if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
            {
                SetStatus("Please enter username and password");
                return;
            }

            if (password.Length < 4)
            {
                SetStatus("Password must be at least 4 characters");
                return;
            }

            SetStatus("Registering...");
            SetButtonsEnabled(false);

            if (_gameManager != null)
            {
                await _gameManager.RegisterAsync(username, password);
            }

            SetButtonsEnabled(true);
        }

        /// <summary>
        /// Handle dev login button press.
        /// </summary>
        async void OnDevLoginPressed()
        {
            SetStatus("Dev login...");
            SetButtonsEnabled(false);

            if (_gameManager != null)
            {
                await _gameManager.DevLoginAsync();
            }

            SetButtonsEnabled(true);
        }

        /// <summary>
        /// Handle demo mode button press.
        /// </summary>
        void OnDemoModePressed()
        {
            SetStatus("Starting demo mode...");
            if (_gameManager != null)
            {
                _gameManager.StartDemoMode();
                Visible = false;
            }
        }

        /// <summary>
        /// Set status message.
        /// </summary>
        void SetStatus(string message)
        {
            if (_statusLabel != null)
            {
                _statusLabel.Text = message;
            }
            GD.Print($"[LoginScreen] {message}");
        }

        /// <summary>
        /// Enable/disable all buttons.
        /// </summary>
        void SetButtonsEnabled(bool enabled)
        {
            if (_loginButton != null) _loginButton.Disabled = !enabled;
            if (_registerButton != null) _registerButton.Disabled = !enabled;
            if (_devLoginButton != null) _devLoginButton.Disabled = !enabled;
            if (_demoModeButton != null) _demoModeButton.Disabled = !enabled;
        }

        /// <summary>
        /// Show the login screen.
        /// </summary>
        public void ShowScreen()
        {
            Visible = true;
            _usernameField?.GrabFocus();
            SetStatus("");
        }

        /// <summary>
        /// Hide the login screen.
        /// </summary>
        public void HideScreen()
        {
            Visible = false;
        }
    }
}
