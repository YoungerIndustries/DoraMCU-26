using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Centralized loading state manager for all async operations.
    /// Shows a full-screen overlay with a progress spinner and message.
    /// Supports multiple concurrent loading operations via reference counting.
    /// </summary>
    public class LoadingManager : Singleton<LoadingManager>
    {
        [Header("UI Elements")]
        [SerializeField] private Canvas loadingCanvas;
        [SerializeField] private Image overlayBackground;
        [SerializeField] private Image spinner;
        [SerializeField] private Text loadingText;

        private GameObject loadingPanelGo;
        private int activeLoaders = 0;
        private Queue<string> messageQueue = new Queue<string>();
        private Coroutine rotateCoroutine;
        private float spinnerRotation = 0f;

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
        /// Create all loading UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            // Create dedicated canvas for loading overlay
            var canvasGo = new GameObject("LoadingCanvas");
            Canvas canvas = canvasGo.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.sortingOrder = 200; // Above everything else

            canvasGo.AddComponent<CanvasRenderer>();
            canvasGo.AddComponent<GraphicRaycaster>();

            var scaler = canvasGo.AddComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1920, 1080);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            loadingCanvas = canvas;

            // Create loading panel
            loadingPanelGo = new GameObject("LoadingPanel");
            var panelRect = loadingPanelGo.AddComponent<RectTransform>();
            panelRect.SetParent(canvasGo.transform);
            panelRect.anchorMin = Vector2.zero;
            panelRect.anchorMax = Vector2.one;
            panelRect.anchoredPosition = Vector2.zero;
            panelRect.sizeDelta = Vector2.zero;
            panelRect.localScale = Vector3.one;

            // Semi-transparent overlay background
            overlayBackground = loadingPanelGo.AddComponent<Image>();
            overlayBackground.color = new Color(0f, 0f, 0f, 0.7f);

            // Spinner container (centered)
            var spinnerGo = new GameObject("Spinner");
            var spinnerRect = spinnerGo.AddComponent<RectTransform>();
            spinnerRect.SetParent(panelRect);
            spinnerRect.anchorMin = new Vector2(0.5f, 0.5f);
            spinnerRect.anchorMax = new Vector2(0.5f, 0.5f);
            spinnerRect.anchoredPosition = Vector2.zero;
            spinnerRect.sizeDelta = new Vector2(64f, 64f);
            spinnerRect.localScale = Vector3.one;

            spinner = spinnerGo.AddComponent<Image>();
            spinner.color = UIConfig.Instance.TextLoading;

            // Create a simple circle sprite for the spinner
            var tex = new Texture2D(64, 64);
            var center = new Vector2(32f, 32f);
            for (int y = 0; y < 64; y++)
            {
                for (int x = 0; x < 64; x++)
                {
                    var dist = Vector2.Distance(new Vector2(x, y), center);
                    if (dist > 10f && dist < 28f)
                    {
                        tex.SetPixel(x, y, Color.white);
                    }
                    else
                    {
                        tex.SetPixel(x, y, new Color(1f, 1f, 1f, 0f));
                    }
                }
            }
            tex.Apply();
            var sprite = Sprite.Create(tex, new Rect(0, 0, 64, 64), new Vector2(0.5f, 0.5f));
            spinner.sprite = sprite;

            // Loading text below spinner
            var textGo = new GameObject("LoadingText");
            var textRect = textGo.AddComponent<RectTransform>();
            textRect.SetParent(panelRect);
            textRect.anchorMin = new Vector2(0.5f, 0.5f);
            textRect.anchorMax = new Vector2(0.5f, 0.5f);
            textRect.anchoredPosition = new Vector2(0f, -50f);
            textRect.sizeDelta = new Vector2(300f, 30f);
            textRect.localScale = Vector3.one;

            loadingText = textGo.AddComponent<Text>();
            loadingText.font = FontLoader.Instance.DefaultFont;
            loadingText.fontSize = UIConfig.Instance.FontSizeNormal;
            loadingText.color = UIConfig.Instance.TextDefault;
            loadingText.alignment = TextAnchor.MiddleCenter;
            loadingText.horizontalOverflow = HorizontalWrapMode.Overflow;
            loadingText.verticalOverflow = VerticalWrapMode.Overflow;

            loadingPanelGo.SetActive(false);
        }

        /// <summary>
        /// Show loading overlay with the given message.
        /// Supports multiple concurrent loaders via reference counting.
        /// </summary>
        public void Show(string message = "Loading...")
        {
            activeLoaders++;
            messageQueue.Enqueue(message);

            if (activeLoaders == 1)
            {
                // First loader — show the overlay
                UpdateMessage();
                if (loadingPanelGo != null)
                {
                    loadingPanelGo.SetActive(true);
                }
                // Start spinner rotation
                if (rotateCoroutine == null)
                {
                    rotateCoroutine = StartCoroutine(RotateSpinner());
                }
            }
            else
            {
                // Additional loader — just update the message
                UpdateMessage();
            }

            Debug.Log($"[LoadingManager] Show: \"{message}\" (active: {activeLoaders})");
        }

        /// <summary>
        /// Hide loading overlay. When all loaders complete, hides the overlay.
        /// </summary>
        public void Hide()
        {
            if (activeLoaders <= 0) return;

            activeLoaders--;

            if (activeLoaders <= 0)
            {
                activeLoaders = 0;
                // All loaders done — hide the overlay
                if (loadingPanelGo != null)
                {
                    loadingPanelGo.SetActive(false);
                }
                if (rotateCoroutine != null)
                {
                    StopCoroutine(rotateCoroutine);
                    rotateCoroutine = null;
                }
                spinnerRotation = 0f;
                if (spinner != null)
                {
                    spinner.transform.localRotation = Quaternion.identity;
                }
                messageQueue.Clear();
                if (loadingText != null)
                {
                    loadingText.text = "";
                }
            }
            else
            {
                // Still have active loaders — update message
                UpdateMessage();
            }

            Debug.Log($"[LoadingManager] Hide (active: {activeLoaders})");
        }

        /// <summary>
        /// Update the displayed message to the most recent one in the queue.
        /// </summary>
        private void UpdateMessage()
        {
            if (loadingText == null || messageQueue.Count == 0) return;

            // Show the most recent message
            string currentMessage = messageQueue.Peek();
            loadingText.text = currentMessage;
        }

        /// <summary>
        /// Coroutine that rotates the spinner continuously.
        /// </summary>
        private IEnumerator RotateSpinner()
        {
            while (loadingPanelGo != null && loadingPanelGo.activeSelf)
            {
                spinnerRotation += 180f * Time.deltaTime;
                if (spinner != null)
                {
                    spinner.transform.localRotation = Quaternion.Euler(0f, 0f, spinnerRotation);
                }
                yield return null;
            }
        }

        /// <summary>
        /// Check if any loading operations are currently active.
        /// </summary>
        public bool IsLoading => activeLoaders > 0;

        /// <summary>
        /// Get the number of active loading operations.
        /// </summary>
        public int ActiveLoaders => activeLoaders;
    }
}
