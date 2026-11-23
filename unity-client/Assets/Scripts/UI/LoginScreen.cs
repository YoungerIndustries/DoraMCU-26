using System;
using System.Collections;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Login screen shown before the main menu.
    /// Provides username/password login and registration.
    /// Follows the existing Singleton + runtime UI pattern.
    /// </summary>
    public class LoginScreen : Singleton<LoginScreen>
    {
        [Header("UI Elements")]
        [SerializeField] private GameObject loginPanel;
        [SerializeField] private InputField usernameField;
        [SerializeField] private InputField passwordField;
        [SerializeField] private Button loginButton;
        [SerializeField] private Button registerButton;
        [SerializeField] private Button devLoginButton;
        [SerializeField] private Text errorText;
        [SerializeField] private Text loadingText;
        [SerializeField] private Canvas loginCanvas;

        private GameObject loginPanelGo;
        private bool isLoading;

        // Events
        public event Action<string, string> OnLoginSuccess; // username, token
        public event Action<string> OnLoginError;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            SetupButtons();
            Hide();
        }

        /// <summary>
        /// Create all login UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            // Create dedicated canvas for login screen with high sorting order
            // so it always renders above other UI panels
            var canvasGo = new GameObject("LoginCanvas");
            Canvas canvas = canvasGo.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.sortingOrder = 100; // Above all other UI
            canvasGo.AddComponent<CanvasRenderer>();
            canvasGo.AddComponent<GraphicRaycaster>();

            // Add CanvasScaler for proper UI scaling
            var scaler = canvasGo.AddComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1920, 1080);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            // Ensure EventSystem exists (shared)
            var eventSystem = UnityEngine.Object.FindObjectOfType<EventSystem>();
            if (eventSystem == null)
            {
                var esGo = new GameObject("EventSystem");
                esGo.AddComponent<EventSystem>();
                esGo.AddComponent<StandaloneInputModule>();
            }
            loginCanvas = canvas;

            // Create login panel
            if (loginPanel == null)
            {
                loginPanelGo = CreatePanel("LoginPanel", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                loginPanelGo.SetActive(false);

                // Center offset for positioning children (stretched panel uses localPosition)
                float cx = 0f;
                float cy = 0f;

                // Title
                CreateText(null, "LoginTitle", loginPanelGo.transform, "DoraMCU", 72, new Vector2(cx, cy + 120));
                CreateText(null, "LoginSubtitle", loginPanelGo.transform, "Enter the Galaxy", 24, new Vector2(cx, cy + 80));

                // Username field
                var usernameLabel = CreateText(null, "UsernameLabel", loginPanelGo.transform, "Username", 16, new Vector2(cx - 150, cy + 30));
                if (usernameLabel != null) usernameLabel.alignment = TextAnchor.UpperLeft;

                usernameField = CreateInputField(loginPanelGo.transform, "UsernameInput", false, new Vector2(cx - 150, cy));

                // Password field
                var passwordLabel = CreateText(null, "PasswordLabel", loginPanelGo.transform, "Password", 16, new Vector2(cx - 150, cy - 45));
                if (passwordLabel != null) passwordLabel.alignment = TextAnchor.UpperLeft;

                passwordField = CreateInputField(loginPanelGo.transform, "PasswordInput", true, new Vector2(cx - 150, cy - 60));

                // Error text
                errorText = CreateText(null, "ErrorText", loginPanelGo.transform, "", 14, new Vector2(cx, cy - 100));
                if (errorText != null)
                {
                    errorText.color = UIConfig.Instance.TextError;
                    errorText.horizontalOverflow = HorizontalWrapMode.Overflow;
                }

                // Loading text
                loadingText = CreateText(null, "LoadingText", loginPanelGo.transform, "", 16, new Vector2(cx, cy - 120));
                if (loadingText != null) loadingText.color = UIConfig.Instance.TextLoading;

                // Buttons
                loginButton = CreateButton(loginPanelGo.transform, "LoginBtn", "LOGIN", new Vector2(cx - 100, cy - 160), OnLogin);
                registerButton = CreateButton(loginPanelGo.transform, "RegisterBtn", "REGISTER", new Vector2(cx + 100, cy - 160), OnRegister);
                devLoginButton = CreateButton(loginPanelGo.transform, "DevLoginBtn", "DEV LOGIN", new Vector2(cx, cy - 200), OnDevLogin);
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
        /// Show the login screen.
        /// </summary>
        public void Show()
        {
            if (loginPanelGo != null)
            {
                loginPanelGo.SetActive(true);
                // Move to top of canvas render order so it appears above other panels
                loginPanelGo.transform.SetAsLastSibling();
            }
            ClearError();
            if (usernameField != null) usernameField.text = "";
            if (passwordField != null) passwordField.text = "";
        }

        /// <summary>
        /// Hide the login screen.
        /// </summary>
        public void Hide()
        {
            if (loginPanelGo != null)
            {
                loginPanelGo.SetActive(false);
            }
        }

        /// <summary>
        /// Set an error message.
        /// </summary>
        public void SetError(string message)
        {
            if (errorText != null)
            {
                errorText.text = message;
            }
            OnLoginError?.Invoke(message);
        }

        /// <summary>
        /// Clear the error message.
        /// </summary>
        public void ClearError()
        {
            if (errorText != null)
            {
                errorText.text = "";
            }
        }

        /// <summary>
        /// Set loading state.
        /// </summary>
        private void SetLoading(bool loading, string message = "Connecting...")
        {
            isLoading = loading;
            if (loadingText != null)
            {
                loadingText.text = message;
            }
            if (loginButton != null) loginButton.interactable = !loading;
            if (registerButton != null) registerButton.interactable = !loading;
            if (devLoginButton != null) devLoginButton.interactable = !loading;
        }

        #region Button Callbacks

        private void OnLogin()
        {
            if (isLoading) return;

            string username = usernameField?.text?.Trim();
            string password = passwordField?.text;

            if (string.IsNullOrEmpty(username))
            {
                SetError("Please enter a username");
                return;
            }
            if (string.IsNullOrEmpty(password))
            {
                SetError("Please enter a password");
                return;
            }

            ClearError();
            StartCoroutine(LoginCoroutine(username, password));
        }

        private void OnRegister()
        {
            if (isLoading) return;

            string username = usernameField?.text?.Trim();
            string password = passwordField?.text;

            if (string.IsNullOrEmpty(username))
            {
                SetError("Please enter a username");
                return;
            }
            if (string.IsNullOrEmpty(password))
            {
                SetError("Please enter a password");
                return;
            }
            if (password.Length < 4)
            {
                SetError("Password must be at least 4 characters");
                return;
            }

            ClearError();
            StartCoroutine(RegisterCoroutine(username, password));
        }

        private void OnDevLogin()
        {
            if (isLoading) return;
            ClearError();
            StartCoroutine(DevLoginCoroutine());
        }

        #endregion

        #region Coroutines

        private IEnumerator LoginCoroutine(string username, string password)
        {
            SetLoading(true, "Logging in...");
            LoadingManager.Instance.Show("Logging in...");

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                ToastNotification.Instance.ShowError("NetworkManager not found");
                yield break;
            }

            nm.Login(username, password, (token, respUsername, pin, empireName, corpId, corpName, success, error) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success && !string.IsNullOrEmpty(token))
                {
                    Debug.Log($"[LoginScreen] Login successful for {respUsername}, corp: {corpName}");
                    ToastNotification.Instance.ShowSuccess($"Welcome, {respUsername}!");
                    OnLoginSuccess?.Invoke(respUsername, token);
                    Hide();
                }
                else
                {
                    SetError(error ?? "Login failed. Check credentials and server connection.");
                    ToastNotification.Instance.ShowError(error ?? "Login failed");
                }
            });

            // Wait for callback
            yield return new WaitUntil(() => !isLoading);
        }

        private IEnumerator RegisterCoroutine(string username, string password)
        {
            SetLoading(true, "Creating account...");
            LoadingManager.Instance.Show("Creating account...");

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                ToastNotification.Instance.ShowError("NetworkManager not found");
                yield break;
            }

            nm.Register(username, password, (token, respUsername, pin, empireName, corpId, corpName, success, error) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success && !string.IsNullOrEmpty(token))
                {
                    Debug.Log($"[LoginScreen] Registration successful for {respUsername}, corp: {corpName}");
                    ToastNotification.Instance.ShowSuccess($"Account created for {respUsername}!");
                    OnLoginSuccess?.Invoke(respUsername, token);
                    Hide();
                }
                else
                {
                    SetError(error ?? "Registration failed. Username may already exist.");
                    ToastNotification.Instance.ShowError(error ?? "Registration failed");
                }
            });

            yield return new WaitUntil(() => !isLoading);
        }

        private IEnumerator DevLoginCoroutine()
        {
            SetLoading(true, "Dev login...");
            LoadingManager.Instance.Show("Dev login...");

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                ToastNotification.Instance.ShowError("NetworkManager not found");
                yield break;
            }

            nm.DevLogin((token, respUsername, pin, empireName, corpId, corpName, success, error) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success && !string.IsNullOrEmpty(token))
                {
                    Debug.Log($"[LoginScreen] Dev login successful for {respUsername}, corp: {corpName}");
                    ToastNotification.Instance.ShowSuccess($"Dev login: {respUsername}");
                    OnLoginSuccess?.Invoke(respUsername, token);
                    Hide();
                }
                else
                {
                    SetError(error ?? "Dev login failed.");
                    ToastNotification.Instance.ShowError(error ?? "Dev login failed");
                }
            });

            yield return new WaitUntil(() => !isLoading);
        }

        #endregion

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(loginCanvas.transform);

            var rectTransform = panel.AddComponent<RectTransform>();
            
            // Full-screen panels use stretch anchors; sized panels use bottom-left anchor
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
            image.color = UIConfig.Instance.DarkPanelBackground;

            return panel;
        }

        private Text CreateText(Text textRef, string name, Transform parent, string defaultText, int fontSize, Vector2 position)
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

            var text = textGo.AddComponent<Text>();
            text.text = defaultText;
            text.fontSize = fontSize;
            text.font = FontLoader.Instance.DefaultFont;
            text.color = UIConfig.Instance.TextDefault;
            text.horizontalOverflow = HorizontalWrapMode.Overflow;
            text.verticalOverflow = VerticalWrapMode.Overflow;
            text.alignment = TextAnchor.MiddleCenter;

            return text;
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
            btnRect.sizeDelta = new Vector2(180, 40);
            btnRect.localRotation = Quaternion.identity;
            btnRect.localScale = Vector3.one;

            var btn = btnGo.AddComponent<Button>();
            var image = btnGo.AddComponent<Image>();
            image.color = UIConfig.Instance.ButtonBackground;

            var textGo = new GameObject("ButtonText");
            textGo.transform.SetParent(btnGo.transform, false);
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

        private InputField CreateInputField(Transform parent, string name, bool isPassword, Vector2 position)
        {
            var inputGo = new GameObject(name);
            inputGo.transform.SetParent(parent);
            var inputRect = inputGo.GetComponent<RectTransform>();
            if (inputRect == null) inputRect = inputGo.AddComponent<RectTransform>();
            inputRect.anchorMin = Vector2.zero;
            inputRect.anchorMax = Vector2.zero;
            inputRect.anchoredPosition = position;
            inputRect.sizeDelta = new Vector2(300, 35);
            inputRect.localRotation = Quaternion.identity;
            inputRect.localScale = Vector3.one;

            var inputField = inputGo.AddComponent<InputField>();
            inputField.lineType = InputField.LineType.SingleLine;
            inputField.contentType = isPassword ? InputField.ContentType.Password : InputField.ContentType.Standard;

            var image = inputGo.AddComponent<Image>();
            image.color = UIConfig.Instance.ButtonBackground;

            // Placeholder
            var placeholder = new GameObject("Placeholder");
            placeholder.transform.SetParent(inputGo.transform);
            var placeholderRect = placeholder.GetComponent<RectTransform>();
            if (placeholderRect == null) placeholderRect = placeholder.AddComponent<RectTransform>();
            placeholderRect.anchorMin = Vector2.zero;
            placeholderRect.anchorMax = Vector2.one;
            placeholderRect.anchoredPosition = Vector2.zero;
            placeholderRect.sizeDelta = Vector2.zero;
            var placeholderText = placeholder.AddComponent<Text>();
            placeholderText.text = isPassword ? "••••••••" : "Enter username";
            placeholderText.fontSize = 14;
            placeholderText.font = FontLoader.Instance.DefaultFont;
            placeholderText.color = UIConfig.Instance.TextDisabled;
            inputField.placeholder = placeholderText;

            return inputField;
        }

        #endregion
    }
}
