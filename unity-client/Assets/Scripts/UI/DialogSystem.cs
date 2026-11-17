using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Dialog system for NPC conversations and narrative events.
    /// Displays character portraits, dialogue text, and response options.
    /// Supports branching conversations and typewriter text animation.
    /// </summary>
    public class DialogSystem : Singleton<DialogSystem>
    {
        [Header("Dialog Panel")]
        [SerializeField] private GameObject dialogPanel;
        [SerializeField] private RawImage portraitImage;
        [SerializeField] private Text nameText;
        [SerializeField] private Text dialogText;
        [SerializeField] private ScrollRect responsesScroll;

        [Header("Settings")]
        [SerializeField] private float typewriterSpeed = 0.03f;
        [SerializeField] private int maxResponses = 4;
        [SerializeField] private Color dialogBackgroundColor = new Color(0, 0, 0, 0.8f);
        [SerializeField] private Color nameColor = Color.cyan;
        [SerializeField] private Color dialogColor = Color.white;
        [SerializeField] private Color responseColor = new Color(0.1f, 0.1f, 0.3f, 0.8f);

        private GameObject dialogPanelGo;
        private GameObject responsesContainer;
        private Text nameTextRef;
        private Text dialogTextRef;
        private string fullDialogText;
        private float typewriterTimer;
        private int typewriterIndex;
        private bool isTyping;
        private List<DialogResponse> currentResponses;
        private DialogCallback onDialogComplete;

        private Coroutine typewriterCoroutine;

        /// <summary>
        /// Dialog response option.
        /// </summary>
        public class DialogResponse
        {
            public string Text;
            public DialogCallback Callback;
            public DialogResponse(string text, DialogCallback callback = null)
            {
                Text = text;
                Callback = callback;
            }
        }

        /// <summary>
        /// Callback for dialog completion.
        /// </summary>
        public System.Action<string> DialogCallback;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            HideDialog();
        }

        /// <summary>
        /// Create dialog UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("DialogCanvas");
                canvas = canvasGo.AddComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay;
                canvasGo.AddComponent<CanvasRenderer>();
                canvasGo.AddComponent<GraphicRaycaster>();

                var eventSystem = Object.FindObjectOfType<EventSystem>();
                if (eventSystem == null)
                {
                    var esGo = new GameObject("EventSystem");
                    esGo.AddComponent<EventSystem>();
                    esGo.AddComponent<StandaloneInputModule>();
                }
            }

            if (dialogPanel == null)
            {
                // Dialog panel at bottom of screen
                dialogPanelGo = new GameObject("DialogPanel");
                dialogPanelGo.transform.SetParent(canvas.transform);
                
                var rect = dialogPanelGo.AddComponent<RectTransform>();
                rect.anchorMin = new Vector2(0, 0);
                rect.anchorMax = new Vector2(1, 0.4f);
                rect.anchoredPosition = Vector2.zero;
                rect.sizeDelta = Vector2.zero;

                var bg = dialogPanelGo.AddComponent<Image>();
                bg.color = dialogBackgroundColor;

                // Portrait (left side)
                var portraitGo = new GameObject("Portrait");
                portraitGo.transform.SetParent(dialogPanelGo.transform);
                var portraitRect = portraitGo.AddComponent<RectTransform>();
                portraitRect.anchorMin = new Vector2(0, 0.5f);
                portraitRect.anchorMax = new Vector2(0, 1);
                portraitRect.anchoredPosition = new Vector2(50, 0);
                portraitRect.sizeDelta = new Vector2(100, 100);
                
                if (portraitImage == null)
                {
                    portraitImage = portraitGo.AddComponent<RawImage>();
                    portraitImage.color = Color.gray;
                }

                // Name text (above dialog)
                var nameGo = new GameObject("NameText");
                nameGo.transform.SetParent(dialogPanelGo.transform);
                var nameRect = nameGo.AddComponent<RectTransform>();
                nameRect.anchorMin = new Vector2(0, 0.85f);
                nameRect.anchorMax = new Vector2(1, 1);
                nameRect.anchoredPosition = new Vector2(60, 0);
                nameRect.sizeDelta = new Vector2(-120, 0);

                nameTextRef = nameGo.AddComponent<Text>();
                nameTextRef.fontSize = 20;
                nameTextRef.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                nameTextRef.color = nameColor;
                nameTextRef.fontStyle = FontStyle.Bold;
                nameTextRef.horizontalOverflow = HorizontalWrapMode.Overflow;

                // Dialog text (middle)
                var dialogGo = new GameObject("DialogText");
                dialogGo.transform.SetParent(dialogPanelGo.transform);
                var dialogRect = dialogGo.AddComponent<RectTransform>();
                dialogRect.anchorMin = new Vector2(0, 0.15f);
                dialogRect.anchorMax = new Vector2(0.7f, 0.8f);
                dialogRect.anchoredPosition = new Vector2(60, 0);
                dialogRect.sizeDelta = new Vector2(-120, 0);

                dialogTextRef = dialogGo.AddComponent<Text>();
                dialogTextRef.fontSize = 16;
                dialogTextRef.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                dialogTextRef.color = dialogColor;
                dialogTextRef.horizontalOverflow = HorizontalWrapMode.Overflow;
                dialogTextRef.verticalOverflow = VerticalWrapMode.Overflow;

                // Responses container (right side)
                responsesContainer = new GameObject("Responses");
                responsesContainer.transform.SetParent(dialogPanelGo.transform);
                var respRect = responsesContainer.AddComponent<RectTransform>();
                respRect.anchorMin = new Vector2(0.7f, 0.15f);
                respRect.anchorMax = new Vector2(1, 0.8f);
                respRect.anchoredPosition = new Vector2(0, 0);
                respRect.sizeDelta = new Vector2(-20, 0);
            }
        }

        /// <summary>
        /// Show a dialog with the given character and text.
        /// </summary>
        public void ShowDialog(string characterName, string text, string portraitPath = null, List<DialogResponse> responses = null)
        {
            fullDialogText = text;
            currentResponses = responses;

            if (nameTextRef != null)
            {
                nameTextRef.text = characterName;
            }

            if (portraitPath != null)
            {
                LoadPortrait(portraitPath);
            }

            // Start typewriter effect
            isTyping = true;
            typewriterIndex = 0;
            typewriterTimer = 0;
            if (dialogTextRef != null)
            {
                dialogTextRef.text = "";
            }

            // Clear previous responses
            ClearResponses();

            // Show panel
            if (dialogPanelGo != null)
            {
                dialogPanelGo.SetActive(true);
            }

            // Pause game
            var menu = MenuManager.Instance;
            if (menu != null)
            {
                menu.PauseGame();
            }

            Debug.Log($"[DialogSystem] {characterName}: {text}");
        }

        /// <summary>
        /// Show a simple notification dialog.
        /// </summary>
        public void ShowNotification(string message, DialogCallback onComplete = null)
        {
            ShowDialog("System", message, null, new List<DialogResponse>
            {
                new DialogResponse("OK", () =>
                {
                    HideDialog();
                    onComplete?.Invoke("OK");
                })
            });
        }

        /// <summary>
        /// Show a confirmation dialog.
        /// </summary>
        public void ShowConfirmation(string message, DialogCallback onConfirm, DialogCallback onCancel = null)
        {
            ShowDialog("Confirm", message, null, new List<DialogResponse>
            {
                new DialogResponse("Confirm", () =>
                {
                    HideDialog();
                    onConfirm?.Invoke("Confirm");
                }),
                new DialogResponse("Cancel", () =>
                {
                    HideDialog();
                    onCancel?.Invoke("Cancel");
                })
            });
        }

        /// <summary>
        /// Show character stats dialog.
        /// </summary>
        public void ShowCharacterStats(DoraMCU.Client.Network.NetworkManager.CharacterInfo character)
        {
            if (character == null) return;

            var lines = new List<string>
            {
                $"Name: {character.name}",
                $"Species: {character.species}",
                $"Career: {character.currentCareer}",
                $"Status: {(character.alive ? "Alive" : "Deceased")}",
                $"Location: {character.location}"
            };

            if (character.characteristics != null)
            {
                lines.Add("Characteristics:");
                foreach (var kvp in character.characteristics)
                {
                    lines.Add($"  {kvp.Key}: {kvp.Value}");
                }
            }

            if (character.skills != null)
            {
                lines.Add("Skills:");
                foreach (var kvp in character.skills)
                {
                    lines.Add($"  {kvp.Key}: {kvp.Value}");
                }
            }

            ShowDialog(character.name, string.Join("\n", lines), null, new List<DialogResponse>
            {
                new DialogResponse("Close", HideDialog)
            });
        }

        /// <summary>
        /// Update typewriter effect and handle responses.
        /// </summary>
        void Update()
        {
            if (isTyping && dialogTextRef != null)
            {
                typewriterTimer += Time.deltaTime;
                
                if (typewriterTimer >= typewriterSpeed && typewriterIndex < fullDialogText.Length)
                {
                    typewriterTimer = 0;
                    typewriterIndex++;
                    dialogTextRef.text = fullDialogText.Substring(0, typewriterIndex);
                }

                if (typewriterIndex >= fullDialogText.Length)
                {
                    isTyping = false;
                    // Show responses if any
                    if (currentResponses != null && currentResponses.Count > 0)
                    {
                        ShowResponses(currentResponses);
                    }
                }

                // Click to skip typewriter
                if (Input.GetMouseButtonDown(0))
                {
                    if (dialogTextRef != null)
                    {
                        dialogTextRef.text = fullDialogText;
                    }
                    isTyping = false;
                    typewriterIndex = fullDialogText.Length;
                    
                    if (currentResponses != null && currentResponses.Count > 0)
                    {
                        ShowResponses(currentResponses);
                    }
                }
            }
            else if (!isTyping)
            {
                // If no responses, click to close
                if (currentResponses == null || currentResponses.Count == 0)
                {
                    if (Input.GetMouseButtonDown(0))
                    {
                        HideDialog();
                    }
                }
            }
        }

        /// <summary>
        /// Show response buttons.
        /// </summary>
        private void ShowResponses(List<DialogResponse> responses)
        {
            if (responsesContainer == null) return;

            float buttonHeight = 40f;
            float spacing = 5f;
            float totalHeight = responses.Count * (buttonHeight + spacing);

            for (int i = 0; i < responses.Count && i < maxResponses; i++)
            {
                var response = responses[i];
                var btnGo = new GameObject($"Response_{i}");
                btnGo.transform.SetParent(responsesContainer.transform);

                var rect = btnGo.AddComponent<RectTransform>();
                rect.anchorMin = new Vector2(0, 1);
                rect.anchorMax = new Vector2(1, 1);
                rect.anchoredPosition = new Vector2(0, -(i * (buttonHeight + spacing)));
                rect.sizeDelta = new Vector2(0, -buttonHeight);

                var btn = btnGo.AddComponent<Button>();
                var img = btnGo.AddComponent<Image>();
                img.color = responseColor;

                var textGo = new GameObject("Text");
                textGo.transform.SetParent(btnGo.transform);
                var textRect = textGo.AddComponent<RectTransform>();
                textRect.anchorMin = Vector2.zero;
                textRect.anchorMax = Vector2.one;
                textRect.sizeDelta = Vector2.zero;

                var text = textGo.AddComponent<Text>();
                text.text = response.Text;
                text.fontSize = 14;
                text.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                text.color = Color.white;
                text.alignment = TextAnchor.MiddleCenter;

                string responseText = response.Text;
                var callback = response.Callback;
                btn.onClick.AddListener(() =>
                {
                    HideDialog();
                    callback?.Invoke(responseText);
                });
            }
        }

        /// <summary>
        /// Clear all response buttons.
        /// </summary>
        private void ClearResponses()
        {
            if (responsesContainer != null)
            {
                foreach (Transform child in responsesContainer.transform)
                {
                    GameObject.Destroy(child.gameObject);
                }
            }
        }

        /// <summary>
        /// Hide the dialog panel.
        /// </summary>
        public void HideDialog()
        {
            isTyping = false;
            currentResponses = null;
            ClearResponses();

            if (dialogPanelGo != null)
            {
                dialogPanelGo.SetActive(false);
            }

            // Resume game
            var menu = MenuManager.Instance;
            if (menu != null && menu.IsPaused())
            {
                menu.ResumeGame();
            }
        }

        /// <summary>
        /// Load a portrait texture.
        /// </summary>
        private void LoadPortrait(string path)
        {
            if (portraitImage == null) return;

            var texture = Resources.Load<Texture>(path);
            if (texture != null)
            {
                portraitImage.texture = texture;
                portraitImage.color = Color.white;
            }
        }

        /// <summary>
        /// Check if dialog is currently active.
        /// </summary>
        public bool IsActive()
        {
            return dialogPanelGo != null && dialogPanelGo.activeSelf;
        }

        /// <summary>
        /// Set dialog visibility.
        /// </summary>
        public void SetVisible(bool visible)
        {
            if (visible)
            {
                if (dialogPanelGo != null) dialogPanelGo.SetActive(true);
            }
            else
            {
                HideDialog();
            }
        }
    }
}
