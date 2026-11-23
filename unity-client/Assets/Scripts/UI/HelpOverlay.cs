using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Shows keyboard shortcuts overlay when F1 is pressed.
    /// Helps players discover gameplay controls.
    /// </summary>
    public class HelpOverlay : Singleton<HelpOverlay>
    {
        [Header("UI Elements")]
        [SerializeField] private GameObject helpPanel;
        [SerializeField] private Text helpText;
        [SerializeField] private Canvas helpCanvas;

        private GameObject helpPanelGo;
        private bool isVisible;

        protected override void Awake()
        {
            base.Awake();
        }

        void Start()
        {
            EnsureUIElements();
            Hide();
        }

        void Update()
        {
            if (UnityEngine.Input.GetKeyDown(KeyCode.F1))
            {
                Toggle();
            }
        }

        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null) return;
            helpCanvas = canvas;

            if (helpPanel == null)
            {
                helpPanelGo = CreatePanel("HelpOverlay", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                helpPanelGo.SetActive(false);

                string helpContent = "=== DoraMCU Controls ===\n\n" +
                    "ESC  - Pause Menu\n" +
                    "Q    - Scan Sector\n" +
                    "X    - Scan Current Sector\n" +
                    "V    - Ship Builder\n" +
                    "Tab  - Galaxy Map\n" +
                    "I    - Inventory\n" +
                    "F1   - This Help Screen\n" +
                    "E    - Interact\n" +
                    "1-9  - Navigate to System\n" +
                    "B    - Enter Battlefield Mode\n\n" +
                    "WASD - Move\n" +
                    "Mouse - Look Around\n" +
                    "Left Click - Fire Weapons";

                helpText = CreateText(null, "HelpText", helpPanelGo.transform, helpContent, 16, new Vector2(0, 0));
                if (helpText != null)
                {
                    helpText.alignment = TextAnchor.UpperLeft;
                    helpText.horizontalOverflow = HorizontalWrapMode.Overflow;
                    helpText.verticalOverflow = VerticalWrapMode.Overflow;
                }

                // Close button
                CreateButton(helpPanelGo.transform, "CloseHelp", "CLOSE", new Vector2(Screen.width / 2 - 50, Screen.height / 2 + 100), OnClose);
            }
        }

        public void Show()
        {
            if (helpPanelGo != null)
            {
                helpPanelGo.SetActive(true);
                isVisible = true;
            }
        }

        public void Hide()
        {
            if (helpPanelGo != null)
            {
                helpPanelGo.SetActive(false);
                isVisible = false;
            }
        }

        public void Toggle()
        {
            if (isVisible) Hide();
            else Show();
        }

        private void OnClose()
        {
            Hide();
        }

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(helpCanvas.transform);

            var rectTransform = panel.AddComponent<RectTransform>();
            rectTransform.anchorMin = Vector2.zero;
            rectTransform.anchorMax = Vector2.one;
            rectTransform.anchoredPosition = position;
            rectTransform.sizeDelta = Vector2.zero;

            var image = panel.AddComponent<Image>();
            image.color = UIConfig.Instance.PanelBackground;

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
            textRect.sizeDelta = new Vector2(400, 30);
            textRect.localRotation = Quaternion.identity;
            textRect.localScale = Vector3.one;

            var text = textGo.AddComponent<Text>();
            text.text = defaultText;
            text.fontSize = fontSize;
            text.font = FontLoader.Instance.DefaultFont;
            text.color = UIConfig.Instance.TextDefault;
            text.horizontalOverflow = HorizontalWrapMode.Overflow;
            text.verticalOverflow = VerticalWrapMode.Overflow;

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
            btnRect.sizeDelta = new Vector2(100, 35);
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
            text.fontSize = 14;
            text.font = FontLoader.Instance.DefaultFont;
            text.color = UIConfig.Instance.TextDefault;
            text.alignment = TextAnchor.MiddleCenter;

            btn.onClick.AddListener(() => onClick());
            return btn;
        }

        #endregion
    }
}
