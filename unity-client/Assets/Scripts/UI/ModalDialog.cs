using System;
using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Modal dialog system for confirmations and alerts.
    /// Shows centered modal with title, message, and OK/Cancel buttons.
    /// Blocks interaction with the rest of the UI while open.
    /// </summary>
    public class ModalDialog : Singleton<ModalDialog>
    {
        [Header("Settings")]
        [SerializeField] private float dialogWidth = 400f;
        [SerializeField] private float dialogHeight = 250f;
        [SerializeField] private float buttonWidth = 100f;
        [SerializeField] private float buttonHeight = 40f;

        [Header("UI Elements")]
        [SerializeField] private Canvas dialogCanvas;

        private GameObject dialogPanelGo;
        private Text titleText;
        private Text messageText;
        private Button confirmButton;
        private Button cancelButton;
        private Text confirmButtonText;
        private Text cancelButtonText;

        private Action onConfirm;
        private Action onCancel;
        private bool isOpen;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            Hide();
        }

        /// <summary>
        /// Create all dialog UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            // Create dedicated canvas for modal dialogs
            var canvasGo = new GameObject("DialogCanvas");
            Canvas canvas = canvasGo.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.sortingOrder = 250; // Above loading, below toasts

            canvasGo.AddComponent<CanvasRenderer>();
            canvasGo.AddComponent<GraphicRaycaster>();

            var scaler = canvasGo.AddComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1920, 1080);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            dialogCanvas = canvas;

            // Dialog panel (centered)
            dialogPanelGo = new GameObject("DialogPanel");
            var panelRect = dialogPanelGo.AddComponent<RectTransform>();
            panelRect.SetParent(canvasGo.transform);
            panelRect.anchorMin = new Vector2(0.5f, 0.5f);
            panelRect.anchorMax = new Vector2(0.5f, 0.5f);
            panelRect.anchoredPosition = Vector2.zero;
            panelRect.sizeDelta = new Vector2(dialogWidth, dialogHeight);
            panelRect.localScale = Vector3.one;

            // Dark background panel
            var panelImage = dialogPanelGo.AddComponent<Image>();
            panelImage.color = UIConfig.Instance.DarkPanelBackground;

            // Title text
            var titleGo = new GameObject("DialogTitle");
            var titleRect = titleGo.AddComponent<RectTransform>();
            titleRect.SetParent(panelRect);
            titleRect.anchorMin = new Vector2(0, 1);
            titleRect.anchorMax = new Vector2(1, 1);
            titleRect.anchoredPosition = new Vector2(0f, -20f);
            titleRect.sizeDelta = new Vector2(-40f, 30f);
            titleRect.localScale = Vector3.one;

            titleText = titleGo.AddComponent<Text>();
            titleText.font = FontLoader.Instance.DefaultFont;
            titleText.fontSize = UIConfig.Instance.FontSizeHeading;
            titleText.color = UIConfig.Instance.TextDefault;
            titleText.alignment = TextAnchor.MiddleCenter;
            titleText.horizontalOverflow = HorizontalWrapMode.Overflow;

            // Message text
            var msgGo = new GameObject("DialogMessage");
            var msgRect = msgGo.AddComponent<RectTransform>();
            msgRect.SetParent(panelRect);
            msgRect.anchorMin = new Vector2(0, 0);
            msgRect.anchorMax = new Vector2(1, 1);
            msgRect.anchoredPosition = new Vector2(0f, 20f);
            msgRect.sizeDelta = new Vector2(-40f, -100f);
            msgRect.localScale = Vector3.one;

            messageText = msgGo.AddComponent<Text>();
            messageText.font = FontLoader.Instance.DefaultFont;
            messageText.fontSize = UIConfig.Instance.FontSizeNormal;
            messageText.color = UIConfig.Instance.TextDefault;
            messageText.alignment = TextAnchor.MiddleCenter;
            messageText.horizontalOverflow = HorizontalWrapMode.Overflow;
            messageText.verticalOverflow = VerticalWrapMode.Overflow;

            // Confirm button (right)
            var confirmGo = new GameObject("ConfirmButton");
            var confirmRect = confirmGo.AddComponent<RectTransform>();
            confirmRect.SetParent(panelRect);
            confirmRect.anchorMin = new Vector2(0.5f, 0);
            confirmRect.anchorMax = new Vector2(1, 0);
            confirmRect.anchoredPosition = new Vector2(-15f, 20f);
            confirmRect.sizeDelta = new Vector2(-30f, buttonHeight);
            confirmRect.localScale = Vector3.one;

            confirmButton = confirmGo.AddComponent<Button>();
            var confirmImage = confirmGo.AddComponent<Image>();
            confirmImage.color = new Color(0.1f, 0.4f, 0.1f, 0.9f); // Green-ish

            var confirmTextGo = new GameObject("ConfirmButtonText");
            var confirmTextRect = confirmTextGo.AddComponent<RectTransform>();
            confirmTextRect.SetParent(confirmRect);
            confirmTextRect.anchorMin = Vector2.zero;
            confirmTextRect.anchorMax = Vector2.one;
            confirmTextRect.anchoredPosition = Vector2.zero;
            confirmTextRect.sizeDelta = Vector2.zero;
            confirmTextRect.localScale = Vector3.one;

            confirmButtonText = confirmTextGo.AddComponent<Text>();
            confirmButtonText.font = FontLoader.Instance.DefaultFont;
            confirmButtonText.fontSize = UIConfig.Instance.FontSizeNormal;
            confirmButtonText.color = Color.white;
            confirmButtonText.text = "OK";
            confirmButtonText.alignment = TextAnchor.MiddleCenter;

            // Cancel button (left)
            var cancelGo = new GameObject("CancelButton");
            var cancelRect = cancelGo.AddComponent<RectTransform>();
            cancelRect.SetParent(panelRect);
            cancelRect.anchorMin = new Vector2(0, 0);
            cancelRect.anchorMax = new Vector2(0.5f, 0);
            cancelRect.anchoredPosition = new Vector2(15f, 20f);
            cancelRect.sizeDelta = new Vector2(-30f, buttonHeight);
            cancelRect.localScale = Vector3.one;

            cancelButton = cancelGo.AddComponent<Button>();
            var cancelImage = cancelGo.AddComponent<Image>();
            cancelImage.color = UIConfig.Instance.ButtonBackground;

            var cancelTextGo = new GameObject("CancelButtonText");
            var cancelTextRect = cancelTextGo.AddComponent<RectTransform>();
            cancelTextRect.SetParent(cancelRect);
            cancelTextRect.anchorMin = Vector2.zero;
            cancelTextRect.anchorMax = Vector2.one;
            cancelTextRect.anchoredPosition = Vector2.zero;
            cancelTextRect.sizeDelta = Vector2.zero;
            cancelTextRect.localScale = Vector3.one;

            cancelButtonText = cancelTextGo.AddComponent<Text>();
            cancelButtonText.font = FontLoader.Instance.DefaultFont;
            cancelButtonText.fontSize = UIConfig.Instance.FontSizeNormal;
            cancelButtonText.color = Color.white;
            cancelButtonText.text = "Cancel";
            cancelButtonText.alignment = TextAnchor.MiddleCenter;

            // Setup button callbacks
            confirmButton.onClick.AddListener(OnConfirmClicked);
            cancelButton.onClick.AddListener(OnCancelClicked);

            dialogPanelGo.SetActive(false);
        }

        /// <summary>
        /// Show a simple alert dialog with OK button only.
        /// </summary>
        public void ShowAlert(string title, string message)
        {
            ShowDialog(title, message, confirmText: "OK", cancelText: null, onConfirm: null, onCancel: null);
        }

        /// <summary>
        /// Show a confirmation dialog with OK and Cancel buttons.
        /// </summary>
        public void ShowConfirm(string title, string message, Action onConfirm, Action onCancel = null)
        {
            ShowDialog(title, message, confirmText: "Confirm", cancelText: "Cancel", onConfirm: onConfirm, onCancel: onCancel);
        }

        /// <summary>
        /// Show a custom dialog with custom button text.
        /// </summary>
        public void ShowDialog(string title, string message, string confirmText = "OK", string cancelText = "Cancel", Action onConfirm = null, Action onCancel = null)
        {
            if (titleText != null) titleText.text = title;
            if (messageText != null) messageText.text = message;
            if (confirmButtonText != null) confirmButtonText.text = confirmText;

            this.onConfirm = onConfirm;
            this.onCancel = onCancel;

            // Show/hide cancel button based on whether cancel text is provided
            if (cancelText == null && cancelButton != null)
            {
                cancelButton.gameObject.SetActive(false);
                // Center the confirm button
                if (confirmButton != null)
                {
                    var rect = confirmButton.GetComponent<RectTransform>();
                    rect.anchorMin = new Vector2(0.5f, 0);
                    rect.anchorMax = new Vector2(0.5f, 0);
                    rect.anchoredPosition = new Vector2(0f, 20f);
                    rect.sizeDelta = new Vector2(buttonWidth * 2, buttonHeight);
                }
            }
            else
            {
                if (cancelButtonText != null) cancelButtonText.text = cancelText;
                if (cancelButton != null) cancelButton.gameObject.SetActive(true);
                // Restore original button positions
                if (confirmButton != null)
                {
                    var confirmRect = confirmButton.GetComponent<RectTransform>();
                    confirmRect.anchorMin = new Vector2(0.5f, 0);
                    confirmRect.anchorMax = new Vector2(1, 0);
                    confirmRect.anchoredPosition = new Vector2(-15f, 20f);
                    confirmRect.sizeDelta = new Vector2(-30f, buttonHeight);
                }
                if (cancelButton != null)
                {
                    var cancelRect = cancelButton.GetComponent<RectTransform>();
                    cancelRect.anchorMin = new Vector2(0, 0);
                    cancelRect.anchorMax = new Vector2(0.5f, 0);
                    cancelRect.anchoredPosition = new Vector2(15f, 20f);
                    cancelRect.sizeDelta = new Vector2(-30f, buttonHeight);
                }
            }

            isOpen = true;
            if (dialogPanelGo != null)
            {
                dialogPanelGo.SetActive(true);
                dialogPanelGo.transform.SetAsLastSibling();
            }

            Debug.Log($"[ModalDialog] Show: \"{title}\"");
        }

        /// <summary>
        /// Hide the dialog.
        /// </summary>
        public void Hide()
        {
            isOpen = false;
            if (dialogPanelGo != null)
            {
                dialogPanelGo.SetActive(false);
            }
            onConfirm = null;
            onCancel = null;
        }

        private void OnConfirmClicked()
        {
            Debug.Log("[ModalDialog] Confirm clicked");
            var callback = onConfirm;
            Hide();
            callback?.Invoke();
        }

        private void OnCancelClicked()
        {
            Debug.Log("[ModalDialog] Cancel clicked");
            var callback = onCancel;
            Hide();
            callback?.Invoke();
        }

        /// <summary>
        /// Check if a dialog is currently open.
        /// </summary>
        public bool IsOpen => isOpen;

        /// <summary>
        /// Force close the dialog without triggering callbacks.
        /// </summary>
        public void ForceClose()
        {
            Hide();
        }
    }
}
