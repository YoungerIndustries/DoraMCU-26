using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Ship building UI panel.
    /// Allows designing ships with module selection, showing resource costs, and submitting build orders.
    /// Follows the existing Singleton + runtime UI pattern.
    /// </summary>
    public class ShipBuildUI : Singleton<ShipBuildUI>
    {
        [Header("UI Elements")]
        [SerializeField] private GameObject buildPanel;
        [SerializeField] private Text titleText;
        [SerializeField] private Text errorText;
        [SerializeField] private Text loadingText;
        [SerializeField] private Text costText;
        [SerializeField] private Button closeButton;
        [SerializeField] private Button buildButton;
        [SerializeField] private Canvas buildCanvas;

        private GameObject buildPanelGo;
        private bool isLoading;

        // Ship types
        private List<string> shipTypes = new() { "Frigate", "Destroyer", "Cruiser", "Dreadnought", "Mothership" };
        private string selectedShipType = "Frigate";

        // Modules
        private List<string> moduleTypes = new() { "Engine", "Laser", "Shield", "Missile", "Cargo", "Sensor" };
        private HashSet<string> selectedModules = new() { "Engine" }; // Engine is required

        // Resource costs (approximate)
        private Dictionary<string, long> baseCosts = new()
        {
            { "Frigate", 100 },
            { "Destroyer", 250 },
            { "Cruiser", 500 },
            { "Dreadnought", 1000 },
            { "Mothership", 2500 }
        };
        private Dictionary<string, long> moduleCosts = new()
        {
            { "Engine", 50 },
            { "Laser", 75 },
            { "Shield", 60 },
            { "Missile", 80 },
            { "Cargo", 40 },
            { "Sensor", 30 }
        };

        // Events
        public event Action<bool> OnVisibilityChanged;
        public event Action<string> OnShipBuilt;
        public event Action<string> OnError;

        protected override void Awake()
        {
            base.Awake();
        }

        void Start()
        {
            EnsureUIElements();
            Hide();
        }

        /// <summary>
        /// Create all ship build UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("ShipBuildCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                canvasGo.AddComponent<CanvasRenderer>();
                canvasGo.AddComponent<GraphicRaycaster>();

                var eventSystem = UnityEngine.Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }
            buildCanvas = canvas;

            if (buildPanel == null)
            {
                buildPanelGo = CreatePanel("ShipBuildPanel", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                buildPanelGo.SetActive(false);

                titleText = CreateText(null, "BuildTitle", buildPanelGo.transform, "SHIP BUILDER", 48, new Vector2(0, Screen.height / 2 + 160));
                if (titleText != null) titleText.fontStyle = FontStyle.Bold;

                CreateText(null, "ShipTypeLabel", buildPanelGo.transform, "Select Ship Type:", 18, new Vector2(-150, Screen.height / 2 + 110));
                CreateText(null, "ModuleLabel", buildPanelGo.transform, "Select Modules:", 18, new Vector2(-150, Screen.height / 2 + 10));

                errorText = CreateText(null, "BuildError", buildPanelGo.transform, "", 14, new Vector2(0, Screen.height / 2 - 130));
                if (errorText != null) errorText.color = UIConfig.Instance.TextError;

                loadingText = CreateText(null, "BuildLoading", buildPanelGo.transform, "", 16, new Vector2(0, Screen.height / 2 - 150));
                if (loadingText != null) loadingText.color = UIConfig.Instance.TextLoading;

                costText = CreateText(null, "BuildCost", buildPanelGo.transform, "Cost: 0 credits", 16, new Vector2(0, Screen.height / 2 - 170));

                closeButton = CreateButton(buildPanelGo.transform, "CloseBtn", "CLOSE", new Vector2(-120, Screen.height / 2 - 210), OnClose);
                buildButton = CreateButton(buildPanelGo.transform, "BuildBtn", "BUILD SHIP", new Vector2(120, Screen.height / 2 - 210), OnBuild);

                // Populate ship types
                PopulateShipTypes();
                // Populate modules
                PopulateModules();
                // Update cost display
                UpdateCostDisplay();
            }
        }

        private void PopulateShipTypes()
        {
            int x = -150;
            foreach (var shipType in shipTypes)
            {
                string st = shipType;
                bool isSelected = shipType == selectedShipType;
                CreateOptionButton(buildPanelGo.transform, $"ShipType_{shipType}", shipType, isSelected, new Vector2(x, Screen.height / 2 + 70), () =>
                {
                    selectedShipType = st;
                    UpdateCostDisplay();
                    UpdateStepUI();
                });
                x += 110;
            }
        }

        private void PopulateModules()
        {
            int x = -150;
            int y = Screen.height / 2 + 50;
            foreach (var module in moduleTypes)
            {
                string m = module;
                bool isSelected = selectedModules.Contains(module);
                CreateOptionButton(buildPanelGo.transform, $"Module_{module}", module, isSelected, new Vector2(x, y), () =>
                {
                    if (m == "Engine") return; // Engine is required, can't deselect
                    if (selectedModules.Contains(m))
                    {
                        selectedModules.Remove(m);
                    }
                    else
                    {
                        selectedModules.Add(m);
                    }
                    UpdateCostDisplay();
                    UpdateStepUI();
                });
                x += 110;
                if (x > 150)
                {
                    x = -150;
                    y -= 45;
                }
            }
        }

        private void UpdateStepUI()
        {
            // Update ship type buttons
            foreach (var shipType in shipTypes)
            {
                var child = FindChild(buildPanelGo.transform, $"ShipType_{shipType}");
                if (child != null)
                {
                    var img = child.GetComponent<Image>();
                    if (img != null)
                    {
                        img.color = shipType == selectedShipType ? new Color(0.2f, 0.4f, 0.8f, 0.8f) : UIConfig.Instance.ButtonBackground;
                    }
                }
            }

            // Update module buttons
            foreach (var module in moduleTypes)
            {
                var child = FindChild(buildPanelGo.transform, $"Module_{module}");
                if (child != null)
                {
                    var img = child.GetComponent<Image>();
                    if (img != null)
                    {
                        bool isSelected = selectedModules.Contains(module);
                        img.color = isSelected ? new Color(0.2f, 0.4f, 0.8f, 0.8f) : UIConfig.Instance.ButtonBackground;
                    }
                }
            }
        }

        private void UpdateCostDisplay()
        {
            long totalCost = 0;
            if (baseCosts.TryGetValue(selectedShipType, out long baseCost))
            {
                totalCost += baseCost;
            }
            foreach (var module in selectedModules)
            {
                if (moduleCosts.TryGetValue(module, out long moduleCost))
                {
                    totalCost += moduleCost;
                }
            }
            if (costText != null)
            {
                costText.text = $"Cost: {totalCost} credits";
            }
        }

        /// <summary>
        /// Show the ship build panel.
        /// </summary>
        public void Show()
        {
            if (buildPanelGo != null)
            {
                buildPanelGo.SetActive(true);
            }
            OnVisibilityChanged?.Invoke(true);
        }

        /// <summary>
        /// Hide the ship build panel.
        /// </summary>
        public void Hide()
        {
            if (buildPanelGo != null)
            {
                buildPanelGo.SetActive(false);
            }
            OnVisibilityChanged?.Invoke(false);
        }

        /// <summary>
        /// Toggle visibility.
        /// </summary>
        public void Toggle()
        {
            if (buildPanelGo != null && buildPanelGo.activeSelf)
            {
                Hide();
            }
            else
            {
                Show();
            }
        }

        /// <summary>
        /// Set visible state.
        /// </summary>
        public void SetVisible(bool visible)
        {
            if (visible) Show();
            else Hide();
        }

        #region Button Callbacks

        private void OnClose()
        {
            Hide();
        }

        private void OnBuild()
        {
            if (isLoading) return;

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                ToastNotification.Instance.ShowError("NetworkManager not found");
                return;
            }

            SetLoading(true, "Building ship...");
            LoadingManager.Instance.Show("Building ship...");

            nm.BuildShip(selectedShipType, new List<string>(selectedModules), (success, error) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success)
                {
                    Debug.Log($"[ShipBuildUI] Ship built: {selectedShipType} with modules {string.Join(", ", selectedModules)}");
                    ToastNotification.Instance.ShowSuccess($"Ship built: {selectedShipType}");
                    OnShipBuilt?.Invoke(selectedShipType);
                    Hide();
                }
                else
                {
                    SetError(error ?? "Ship build failed");
                    ToastNotification.Instance.ShowError(error ?? "Ship build failed");
                }
            });
        }

        #endregion

        #region Error & Loading

        public void SetError(string message)
        {
            if (errorText != null) errorText.text = message;
            OnError?.Invoke(message);
        }

        public void ClearError()
        {
            if (errorText != null) errorText.text = "";
        }

        private void SetLoading(bool loading, string message = "Loading...")
        {
            isLoading = loading;
            if (loadingText != null) loadingText.text = message;
            if (closeButton != null) closeButton.interactable = !loading;
            if (buildButton != null) buildButton.interactable = !loading;
        }

        #endregion

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(buildCanvas.transform);

            var rectTransform = panel.AddComponent<RectTransform>();
            rectTransform.anchorMin = Vector2.zero;
            rectTransform.anchorMax = Vector2.zero;
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

        private Button CreateButton(Transform parent, string name, string buttonText, Vector2 position, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var btnRect = btnGo.GetComponent<RectTransform>();
            if (btnRect == null) btnRect = btnGo.AddComponent<RectTransform>();
            btnRect.anchorMin = Vector2.zero;
            btnRect.anchorMax = Vector2.zero;
            btnRect.anchoredPosition = position;
            btnRect.sizeDelta = new Vector2(120, 35);
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

        private Button CreateOptionButton(Transform parent, string name, string optionText, bool isSelected, Vector2 position, System.Action onClick)
        {
            var btnGo = new GameObject(name);
            btnGo.transform.SetParent(parent);
            var optRect = btnGo.GetComponent<RectTransform>();
            if (optRect == null) optRect = btnGo.AddComponent<RectTransform>();
            optRect.anchorMin = Vector2.zero;
            optRect.anchorMax = Vector2.zero;
            optRect.anchoredPosition = position;
            optRect.sizeDelta = new Vector2(100, 35);
            optRect.localRotation = Quaternion.identity;
            optRect.localScale = Vector3.one;

            var btn = btnGo.AddComponent<Button>();
            var image = btnGo.AddComponent<Image>();
            image.color = isSelected ? new Color(0.2f, 0.4f, 0.8f, 0.8f) : UIConfig.Instance.ButtonBackground;

            var textGo = new GameObject("OptionText");
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
            text.text = optionText;
            text.fontSize = 12;
            text.font = FontLoader.Instance.DefaultFont;
            text.color = UIConfig.Instance.TextDefault;
            text.alignment = TextAnchor.MiddleCenter;

            btn.onClick.AddListener(() => onClick());
            return btn;
        }

        private Transform FindChild(Transform parent, string name)
        {
            foreach (Transform child in parent)
            {
                if (child.name == name) return child;
            }
            return null;
        }

        #endregion
    }
}
