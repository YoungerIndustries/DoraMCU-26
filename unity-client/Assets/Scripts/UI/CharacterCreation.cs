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
    /// Character creation flow shown after login.
    /// Multi-step UI: name input, species selection, career selection, characteristic allocation.
    /// Follows the existing Singleton + runtime UI pattern.
    /// </summary>
    public class CharacterCreation : Singleton<CharacterCreation>
    {
        public enum CreationStep { Name, Species, Career, Characteristics, Complete }

        [Header("UI Elements")]
        [SerializeField] private GameObject creationPanel;
        [SerializeField] private Text stepTitleText;
        [SerializeField] private Text stepDescriptionText;
        [SerializeField] private InputField nameInput;
        [SerializeField] private Text errorText;
        [SerializeField] private Text loadingText;
        [SerializeField] private Button backButton;
        [SerializeField] private Button nextButton;
        [SerializeField] private Canvas creationCanvas;

        private GameObject creationPanelGo;
        private CreationStep currentStep = CreationStep.Name;
        private bool isLoading;

        // Character data
        private string characterName;
        private string selectedSpecies = "Human";
        private string selectedCareer = "CEO";
        private int age = 25;
        private Dictionary<string, int> characteristics = new();

        // Available options
        private List<string> speciesList = new() { "Human", "Vasili", "Kigyard", "Yehar", "Innitrana", "Kyrk", "Monztrass", "Nden", "Olin", "Sennite" };
        private List<string> careerList = new() { "CEO", "Engineer", "Pilot", "Soldier", "Scientist", "Diplomat", "Trader", "Explorer" };

        // Events
        public event Action<CharacterData> OnCharacterCreated;
        public event Action<string> OnError;

        /// <summary>
        /// Character data passed to the game on completion.
        /// </summary>
        public class CharacterData
        {
            public string name;
            public string species;
            public string career;
            public int age;
            public Dictionary<string, int> characteristics;
        }

        protected override void Awake()
        {
            base.Awake();
            // Initialize default characteristics (Traveller-style: 2d6 for each)
            characteristics["ST"] = 7; // Strength
            characteristics["DX"] = 7; // Dexterity
            characteristics["EN"] = 7; // Endurance
            characteristics["IN"] = 7; // Intelligence
            characteristics["ED"] = 7; // Education
            characteristics["SO"] = 7; // Social Standing
        }

        void Start()
        {
            EnsureUIElements();
            SetupButtons();
            Hide();
        }

        /// <summary>
        /// Create all character creation UI elements programmatically.
        /// </summary>
        private void EnsureUIElements()
        {
            // Get or create canvas
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null)
            {
                var canvasGo = new GameObject("CharacterCreationCanvas");
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
            creationCanvas = canvas;

            // Create panel
            if (creationPanel == null)
            {
                creationPanelGo = CreatePanel("CharacterCreationPanel", new Vector2(0, 0), new Vector2(Screen.width, Screen.height));
                creationPanelGo.SetActive(false);

                // Title (stretched panel - use 0-based center coords)
                stepTitleText = CreateText(null, "StepTitle", creationPanelGo.transform, "", 48, new Vector2(0, 150));
                if (stepTitleText != null) stepTitleText.fontStyle = FontStyle.Bold;

                // Description
                stepDescriptionText = CreateText(null, "StepDescription", creationPanelGo.transform, "", 18, new Vector2(0, 110));

                // Error text
                errorText = CreateText(null, "ErrorText", creationPanelGo.transform, "", 14, new Vector2(0, -140));
                if (errorText != null)
                {
                    errorText.color = UIConfig.Instance.TextError;
                    errorText.horizontalOverflow = HorizontalWrapMode.Overflow;
                }

                // Loading text
                loadingText = CreateText(null, "LoadingText", creationPanelGo.transform, "", 16, new Vector2(0, -160));
                if (loadingText != null) loadingText.color = UIConfig.Instance.TextLoading;

                // Buttons
                backButton = CreateButton(creationPanelGo.transform, "BackBtn", "BACK", new Vector2(-120, -200), OnBack);
                nextButton = CreateButton(creationPanelGo.transform, "NextBtn", "NEXT", new Vector2(120, -200), OnNext);
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
        /// Show the character creation screen.
        /// </summary>
        public void Show()
        {
            if (creationPanelGo != null)
            {
                creationPanelGo.SetActive(true);
            }
            currentStep = CreationStep.Name;
            ClearError();
            SetLoading(false);
            UpdateStepUI();
        }

        /// <summary>
        /// Hide the character creation screen.
        /// </summary>
        public void Hide()
        {
            if (creationPanelGo != null)
            {
                creationPanelGo.SetActive(false);
            }
        }

        /// <summary>
        /// Update the UI based on the current step.
        /// </summary>
        private void UpdateStepUI()
        {
            // Clear previous dynamic content
            ClearDynamicContent();

            switch (currentStep)
            {
                case CreationStep.Name:
                    stepTitleText.text = "Character Name";
                    stepDescriptionText.text = "What is your character's name?";
                    nextButton.GetComponentInChildren<Text>().text = "NEXT";
                    ShowNameInput();
                    break;

                case CreationStep.Species:
                    stepTitleText.text = "Select Species";
                    stepDescriptionText.text = "Choose your character's species";
                    nextButton.GetComponentInChildren<Text>().text = "NEXT";
                    ShowSpeciesSelection();
                    break;

                case CreationStep.Career:
                    stepTitleText.text = "Select Career";
                    stepDescriptionText.text = "Choose your character's career path";
                    nextButton.GetComponentInChildren<Text>().text = "NEXT";
                    ShowCareerSelection();
                    break;

                case CreationStep.Characteristics:
                    stepTitleText.text = "Characteristics";
                    stepDescriptionText.text = "Your character's attributes (Traveller-style 2d6)";
                    nextButton.GetComponentInChildren<Text>().text = "CREATE CHARACTER";
                    ShowCharacteristics();
                    break;

                case CreationStep.Complete:
                    stepTitleText.text = "Character Created!";
                    stepDescriptionText.text = $"Welcome, {characterName} the {selectedSpecies} {selectedCareer}";
                    nextButton.GetComponentInChildren<Text>().text = "ENTER GALAXY";
                    break;
            }

            // Update button states
            if (currentStep == CreationStep.Name)
            {
                backButton.interactable = false;
            }
            else
            {
                backButton.interactable = true;
            }
        }

        /// <summary>
        /// Clear dynamically created UI elements from previous steps.
        /// </summary>
        private void ClearDynamicContent()
        {
            // Remove children between title and error text
            int childCount = creationPanelGo.transform.childCount;
            for (int i = childCount - 1; i >= 0; i--)
            {
                var child = creationPanelGo.transform.GetChild(i);
                string name = child.name;
                if (name.StartsWith("Species_") || name.StartsWith("Career_") ||
                    name.StartsWith("Char_") || name.StartsWith("NameInput") ||
                    name.StartsWith("AgeInput") || name.StartsWith("CharValue"))
                {
                    UnityEngine.Object.Destroy(child.gameObject);
                }
            }
        }

        #region Step UI Methods

        private void ShowNameInput()
        {
            var nameLabel = CreateText(null, "NameLabel", creationPanelGo.transform, "Name", 16, new Vector2(-150, 60));
            if (nameLabel != null) nameLabel.alignment = TextAnchor.UpperLeft;

            nameInput = CreateInputField(creationPanelGo.transform, "NameInput", false, new Vector2(-150, 40));
            if (nameInput != null && !string.IsNullOrEmpty(characterName))
            {
                nameInput.text = characterName;
            }

            var ageLabel = CreateText(null, "AgeLabel", creationPanelGo.transform, "Age", 16, new Vector2(-150, -5));
            if (ageLabel != null) ageLabel.alignment = TextAnchor.UpperLeft;

            var ageInput = CreateInputField(creationPanelGo.transform, "AgeInput", false, new Vector2(-150, -25));
            if (ageInput != null)
            {
                ageInput.text = age.ToString();
                ageInput.contentType = InputField.ContentType.IntegerNumber;
            }
        }

        private void ShowSpeciesSelection()
        {
            int y = 50;
            foreach (var species in speciesList)
            {
                string sp = species;
                bool isSelected = species == selectedSpecies;
                var btn = CreateOptionButton(creationPanelGo.transform, $"Species_{species}", species, isSelected, new Vector2(0, y), () =>
                {
                    selectedSpecies = sp;
                    UpdateStepUI();
                });
                y -= 45;
            }
        }

        private void ShowCareerSelection()
        {
            int y = 50;
            foreach (var career in careerList)
            {
                string cr = career;
                bool isSelected = career == selectedCareer;
                var btn = CreateOptionButton(creationPanelGo.transform, $"Career_{career}", career, isSelected, new Vector2(0, y), () =>
                {
                    selectedCareer = cr;
                    UpdateStepUI();
                });
                y -= 45;
            }
        }

        private void ShowCharacteristics()
        {
            var charNames = new[] { "ST (Strength)", "DX (Dexterity)", "EN (Endurance)", "IN (Intelligence)", "ED (Education)", "SO (Social)" };
            var charKeys = new[] { "ST", "DX", "EN", "IN", "ED", "SO" };

            int y = 50;
            for (int i = 0; i < charNames.Length; i++)
            {
                string key = charKeys[i];
                string label = charNames[i];
                int value = characteristics.ContainsKey(key) ? characteristics[key] : 7;

                var labelText = CreateText(null, $"Char_{label}", creationPanelGo.transform, $"{label}: {value}", 16, new Vector2(-150, y));
                if (labelText != null) labelText.alignment = TextAnchor.UpperLeft;

                // Roll button to randomize
                string k = key;
                CreateButton(creationPanelGo.transform, $"CharValue_{k}", "ROLL", new Vector2(150, y + 5), () =>
                {
                    characteristics[k] = UnityEngine.Random.Range(2, 13); // 2d6
                    UpdateStepUI();
                });

                y -= 40;
            }
        }

        #endregion

        #region Button Callbacks

        private void OnBack()
        {
            if (isLoading) return;

            switch (currentStep)
            {
                case CreationStep.Species:
                    currentStep = CreationStep.Name;
                    break;
                case CreationStep.Career:
                    currentStep = CreationStep.Species;
                    break;
                case CreationStep.Characteristics:
                    currentStep = CreationStep.Career;
                    break;
            }
            UpdateStepUI();
        }

        private void OnNext()
        {
            if (isLoading) return;

            switch (currentStep)
            {
                case CreationStep.Name:
                    ValidateAndProceedFromName();
                    break;
                case CreationStep.Species:
                    currentStep = CreationStep.Career;
                    UpdateStepUI();
                    break;
                case CreationStep.Career:
                    currentStep = CreationStep.Characteristics;
                    UpdateStepUI();
                    break;
                case CreationStep.Characteristics:
                    CreateCharacterOnServer();
                    break;
                case CreationStep.Complete:
                    EnterGalaxy();
                    break;
            }
        }

        private void ValidateAndProceedFromName()
        {
            characterName = nameInput?.text?.Trim();
            string ageText = null;

            // Find age input
            foreach (Transform child in creationPanelGo.transform)
            {
                if (child.name == "AgeInput")
                {
                    var inputField = child.GetComponent<InputField>();
                    if (inputField != null) ageText = inputField.text?.Trim();
                    break;
                }
            }

            if (string.IsNullOrEmpty(characterName))
            {
                SetError("Please enter a character name");
                return;
            }

            if (!string.IsNullOrEmpty(ageText) && int.TryParse(ageText, out int parsedAge))
            {
                age = parsedAge;
            }
            else
            {
                age = 25;
            }

            currentStep = CreationStep.Species;
            UpdateStepUI();
        }

        private void CreateCharacterOnServer()
        {
            SetLoading(true, "Creating character...");
            LoadingManager.Instance.Show("Creating character...");

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                ToastNotification.Instance.ShowError("NetworkManager not found");
                return;
            }

            nm.CreateCharacter(characterName, age, selectedCareer, (success, error, characterId) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success)
                {
                    Debug.Log($"[CharacterCreation] Character created: {characterName} (ID: {characterId})");
                    ToastNotification.Instance.ShowSuccess($"Character created: {characterName}");
                    currentStep = CreationStep.Complete;
                    UpdateStepUI();
                }
                else
                {
                    SetError(error ?? "Character creation failed");
                    ToastNotification.Instance.ShowError(error ?? "Character creation failed");
                }
            });
        }

        private void EnterGalaxy()
        {
            SetLoading(true, "Entering galaxy...");
            LoadingManager.Instance.Show("Entering galaxy...");

            var nm = NetworkManager.Instance;
            if (nm == null)
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                ToastNotification.Instance.ShowError("NetworkManager not found");
                return;
            }

            // Fetch Earth 2026 landing data
            nm.FetchEarthLanding((success, error) =>
            {
                SetLoading(false);
                LoadingManager.Instance.Hide();
                if (success)
                {
                    Debug.Log("[CharacterCreation] Galaxy entry successful");
                    ToastNotification.Instance.ShowSuccess("Welcome to Earth 2026!");

                    var data = new CharacterData
                    {
                        name = characterName,
                        species = selectedSpecies,
                        career = selectedCareer,
                        age = age,
                        characteristics = new Dictionary<string, int>(characteristics)
                    };

                    OnCharacterCreated?.Invoke(data);
                    Hide();
                }
                else
                {
                    SetError(error ?? "Failed to enter galaxy");
                    ToastNotification.Instance.ShowError(error ?? "Failed to enter galaxy");
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
            if (backButton != null) backButton.interactable = !loading;
            if (nextButton != null) nextButton.interactable = !loading;
        }

        #endregion

        #region UI Helper Methods

        private GameObject CreatePanel(string name, Vector2 position, Vector2 size)
        {
            var panel = new GameObject(name);
            panel.transform.SetParent(creationCanvas.transform);

            var rectTransform = panel.AddComponent<RectTransform>();
            
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
            var btnRect = btnGo.GetComponent<RectTransform>();
            if (btnRect == null) btnRect = btnGo.AddComponent<RectTransform>();
            btnRect.anchorMin = Vector2.zero;
            btnRect.anchorMax = Vector2.zero;
            btnRect.anchoredPosition = position;
            btnRect.sizeDelta = new Vector2(300, 35);
            btnRect.localRotation = Quaternion.identity;
            btnRect.localScale = Vector3.one;

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

            return inputField;
        }

        #endregion
    }
}
