using System;
using System.Collections;
using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Exploration action handler.
    /// Provides scan button in MainHUD to explore current sector, revealing solar systems and resources.
    /// </summary>
    public class ExploreAction : Singleton<ExploreAction>
    {
        [Header("UI Elements")]
        [SerializeField] private Button scanButton;
        [SerializeField] private Text scanStatusText;
        [SerializeField] private GameObject scanPanel;

        private GameObject scanPanelGo;
        private bool isScanning;
        private int currentSectorRow;
        private int currentSectorCol;

        // Events
        public event Action<int, int> OnSectorScanned; // row, col
        public event Action<string> OnDiscovered; // discovery message
        public event Action<string> OnError;

        protected override void Awake()
        {
            base.Awake();
        }

        void Start()
        {
            EnsureUIElements();
        }

        /// <summary>
        /// Create scan UI elements.
        /// </summary>
        private void EnsureUIElements()
        {
            // Get or create canvas
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null) return;

            // Create scan panel (bottom-right corner)
            if (scanPanel == null)
            {
                scanPanelGo = CreatePanel("ScanPanel", new Vector2(Screen.width - 10, 10), new Vector2(180, 80));

                scanButton = CreateButton(scanPanelGo.transform, "ScanBtn", "SCAN SECTOR", new Vector2(0, 25), OnScan);
                scanStatusText = CreateText(null, "ScanStatus", scanPanelGo.transform, "Ready to scan", 12, new Vector2(0, -10));
                if (scanStatusText != null)
                {
                    scanStatusText.color = UIConfig.Instance.TextSuccess;
                    scanStatusText.horizontalOverflow = HorizontalWrapMode.Overflow;
                }
            }
        }

        /// <summary>
        /// Set the current sector coordinates.
        /// </summary>
        public void SetCurrentSector(int row, int col)
        {
            currentSectorRow = row;
            currentSectorCol = col;
        }

        /// <summary>
        /// Scan the current sector.
        /// </summary>
        public void ScanCurrentSector()
        {
            OnScan();
        }

        /// <summary>
        /// Scan a specific sector.
        /// </summary>
        public void ScanSector(int row, int col)
        {
            currentSectorRow = row;
            currentSectorCol = col;
            OnScan();
        }

        private void OnScan()
        {
            if (isScanning) return;

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                ToastNotification.Instance.ShowError("NetworkManager not found");
                return;
            }

            isScanning = true;
            if (scanButton != null) scanButton.interactable = false;
            if (scanStatusText != null)
            {
                scanStatusText.text = "Scanning...";
                scanStatusText.color = UIConfig.Instance.TextWarning;
            }

            LoadingManager.Instance.Show("Scanning sector...");
            nm.ScanSector(currentSectorRow, currentSectorCol, (success, error, systemsFound) =>
            {
                LoadingManager.Instance.Hide();
                isScanning = false;
                if (scanButton != null) scanButton.interactable = true;

                if (success)
                {
                    if (scanStatusText != null)
                    {
                        scanStatusText.text = $"Found {systemsFound} systems";
                        scanStatusText.color = UIConfig.Instance.TextSuccess;
                    }
                    Debug.Log($"[ExploreAction] Sector {currentSectorRow},{currentSectorCol} scanned: {systemsFound} systems found");
                    OnSectorScanned?.Invoke(currentSectorRow, currentSectorCol);

                    if (systemsFound > 0)
                    {
                        ToastNotification.Instance.ShowSuccess($"Discovered {systemsFound} systems in sector {currentSectorRow},{currentSectorCol}");
                        OnDiscovered?.Invoke($"Discovered {systemsFound} solar systems in sector {currentSectorRow},{currentSectorCol}");
                    }
                    else
                    {
                        ToastNotification.Instance.ShowInfo("Sector scanned - no systems found");
                    }
                }
                else
                {
                    if (scanStatusText != null)
                    {
                        scanStatusText.text = "Scan failed";
                        scanStatusText.color = UIConfig.Instance.TextError;
                    }
                    ToastNotification.Instance.ShowError(error ?? "Scan failed");
                }
            });
        }

        public void SetError(string message)
        {
            OnError?.Invoke(message);
        }

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null) return null;

            var panel = new GameObject(name);
            panel.transform.SetParent(canvas.transform);

            var rectTransform = panel.AddComponent<RectTransform>();
            rectTransform.anchorMin = Vector2.one;
            rectTransform.anchorMax = Vector2.one;
            rectTransform.anchoredPosition = position;
            rectTransform.sizeDelta = size;

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
            textRect.sizeDelta = new Vector2(300, 40);
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

        private Button CreateButton(Transform parent, string name, string buttonText, Vector2 position, Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var btnRect = btnGo.GetComponent<RectTransform>();
            if (btnRect == null) btnRect = btnGo.AddComponent<RectTransform>();
            btnRect.anchorMin = Vector2.zero;
            btnRect.anchorMax = Vector2.zero;
            btnRect.anchoredPosition = position;
            btnRect.sizeDelta = new Vector2(160, 35);
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
