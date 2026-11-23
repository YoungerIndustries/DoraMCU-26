using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Toast notification type with associated color.
    /// </summary>
    public enum ToastType
    {
        Success,
        Error,
        Warning,
        Info
    }

    /// <summary>
    /// Transient toast notification system for success/error/warning messages.
    /// Shows auto-dismissing toast messages at the top of the screen.
    /// Queue-based: shows one toast at a time, auto-advances to the next.
    /// </summary>
    public class ToastNotification : Singleton<ToastNotification>
    {
        [Header("Settings")]
        [SerializeField] private float toastDuration = 3f;
        [SerializeField] private float toastHeight = 40f;
        [SerializeField] private float toastMarginTop = 10f;
        [SerializeField] private float toastMarginSide = 200f;
        [SerializeField] private float toastFadeTime = 0.3f;

        [Header("UI Elements")]
        [SerializeField] private Canvas toastCanvas;

        private GameObject toastPanelGo;
        private Queue<ToastData> toastQueue = new Queue<ToastData>();
        private ToastData currentToast;
        private Coroutine displayCoroutine;

        private struct ToastData
        {
            public string message;
            public ToastType type;
            public GameObject toastGo;
            public Text toastText;
            public Image toastImage;
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
        }

        /// <summary>
        /// Create the toast canvas and panel.
        /// </summary>
        private void EnsureUIElements()
        {
            // Create dedicated canvas for toast notifications
            var canvasGo = new GameObject("ToastCanvas");
            Canvas canvas = canvasGo.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.sortingOrder = 300; // Above loading overlay

            canvasGo.AddComponent<CanvasRenderer>();
            canvasGo.AddComponent<GraphicRaycaster>();

            var scaler = canvasGo.AddComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1920, 1080);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            toastCanvas = canvas;

            // Container panel for all toasts
            toastPanelGo = new GameObject("ToastPanel");
            var panelRect = toastPanelGo.AddComponent<RectTransform>();
            panelRect.SetParent(canvasGo.transform);
            panelRect.anchorMin = Vector2.zero;
            panelRect.anchorMax = Vector2.one;
            panelRect.anchoredPosition = Vector2.zero;
            panelRect.sizeDelta = Vector2.zero;
            panelRect.localScale = Vector3.one;
        }

        /// <summary>
        /// Show a success toast message.
        /// </summary>
        public void ShowSuccess(string message)
        {
            EnqueueToast(message, ToastType.Success);
        }

        /// <summary>
        /// Show an error toast message.
        /// </summary>
        public void ShowError(string message)
        {
            EnqueueToast(message, ToastType.Error);
        }

        /// <summary>
        /// Show a warning toast message.
        /// </summary>
        public void ShowWarning(string message)
        {
            EnqueueToast(message, ToastType.Warning);
        }

        /// <summary>
        /// Show an info toast message.
        /// </summary>
        public void ShowInfo(string message)
        {
            EnqueueToast(message, ToastType.Info);
        }

        /// <summary>
        /// Enqueue a toast message for display.
        /// </summary>
        private void EnqueueToast(string message, ToastType type)
        {
            var data = new ToastData
            {
                message = message,
                type = type
            };
            toastQueue.Enqueue(data);

            Debug.Log($"[ToastNotification] Enqueued: \"{message}\" ({type})");

            // If no toast is currently showing, start displaying
            if (currentToast.message == null && displayCoroutine == null)
            {
                DisplayNextToast();
            }
        }

        /// <summary>
        /// Start displaying the next toast in the queue.
        /// </summary>
        private void DisplayNextToast()
        {
            if (toastQueue.Count == 0)
            {
                currentToast = new ToastData { message = null };
                return;
            }

            currentToast = toastQueue.Dequeue();
            CreateToastUI(currentToast);
            displayCoroutine = StartCoroutine(AnimateToast(currentToast));
        }

        /// <summary>
        /// Create the UI elements for a single toast.
        /// </summary>
        private void CreateToastUI(ToastData data)
        {
            var toastGo = new GameObject("Toast");
            var toastRect = toastGo.AddComponent<RectTransform>();
            toastRect.SetParent(toastPanelGo.transform);
            toastRect.anchorMin = new Vector2(0.5f, 1f);
            toastRect.anchorMax = new Vector2(0.5f, 1f);
            toastRect.anchoredPosition = new Vector2(0f, -toastMarginTop - toastHeight / 2f);
            toastRect.sizeDelta = new Vector2(Screen.width - toastMarginSide * 2f, toastHeight);
            toastRect.localScale = Vector3.one;

            var toastImage = toastGo.AddComponent<Image>();
            toastImage.color = GetToastColor(data.type);
            toastImage.color = new Color(toastImage.color.r, toastImage.color.g, toastImage.color.b, 0.85f);

            var textGo = new GameObject("ToastText");
            var textRect = textGo.AddComponent<RectTransform>();
            textRect.SetParent(toastRect);
            textRect.anchorMin = Vector2.zero;
            textRect.anchorMax = Vector2.one;
            textRect.anchoredPosition = Vector2.zero;
            textRect.sizeDelta = new Vector2(-20f, 0f);
            textRect.localScale = Vector3.one;

            var toastText = textGo.AddComponent<Text>();
            toastText.font = FontLoader.Instance.DefaultFont;
            toastText.fontSize = UIConfig.Instance.FontSizeNormal;
            toastText.color = Color.white;
            toastText.text = data.message;
            toastText.alignment = TextAnchor.MiddleCenter;
            toastText.horizontalOverflow = HorizontalWrapMode.Overflow;
            toastText.verticalOverflow = VerticalWrapMode.Overflow;

            data.toastGo = toastGo;
            data.toastText = toastText;
            data.toastImage = toastImage;
            currentToast = data;
        }

        /// <summary>
        /// Animate a toast: fade in, display, fade out, destroy.
        /// </summary>
        private IEnumerator AnimateToast(ToastData data)
        {
            if (data.toastImage == null) yield break;

            // Fade in
            var targetColor = data.toastImage.color;
            targetColor.a = 0f;
            data.toastImage.color = targetColor;

            while (data.toastImage.color.a < 0.85f)
            {
                data.toastImage.color = new Color(
                    targetColor.r, targetColor.g, targetColor.b,
                    Mathf.Min(data.toastImage.color.a + Time.deltaTime / toastFadeTime, 0.85f)
                );
                yield return null;
            }

            // Display for duration
            yield return new WaitForSeconds(toastDuration);

            // Fade out
            while (data.toastImage.color.a > 0f)
            {
                data.toastImage.color = new Color(
                    data.toastImage.color.r, data.toastImage.color.g, data.toastImage.color.b,
                    Mathf.Max(data.toastImage.color.a - Time.deltaTime / toastFadeTime, 0f)
                );
                yield return null;
            }

            // Destroy toast GameObject
            if (data.toastGo != null)
            {
                UnityEngine.Object.Destroy(data.toastGo);
            }

            displayCoroutine = null;
            currentToast = new ToastData { message = null };

            // Show next toast in queue
            if (toastQueue.Count > 0)
            {
                DisplayNextToast();
            }
        }

        /// <summary>
        /// Get the color for a toast type.
        /// </summary>
        private Color GetToastColor(ToastType type)
        {
            switch (type)
            {
                case ToastType.Success:
                    return UIConfig.Instance.TextSuccess;
                case ToastType.Error:
                    return UIConfig.Instance.TextError;
                case ToastType.Warning:
                    return UIConfig.Instance.TextWarning;
                case ToastType.Info:
                    return UIConfig.Instance.TextLoading;
                default:
                    return Color.gray;
            }
        }

        /// <summary>
        /// Check if there are any toasts pending or currently displaying.
        /// </summary>
        public bool HasToasts => toastQueue.Count > 0 || currentToast.message != null;

        /// <summary>
        /// Get the number of queued toasts.
        /// </summary>
        public int QueuedCount => toastQueue.Count;
    }
}
