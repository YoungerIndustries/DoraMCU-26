using Godot;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DoraMCU.Core;

namespace DoraMCU.UI
{
    /// <summary>
    /// Character creation UI — multi-step process for creating a new character.
    /// Includes name/age, species selection, career selection, and 2d6 characteristic rolls.
    /// Godot Control-based UI replacing Unity's CharacterCreation.
    /// </summary>
    public partial class CharacterCreation : Control
    {
        // Steps
        enum Step
        {
            NameAge,
            Species,
            Career,
            Review
        }

        Step _currentStep = Step.NameAge;

        // UI nodes
        Panel _mainPanel;
        Label _stepTitle;
        Label _stepDescription;
        ScrollContainer _scrollContainer;
        VBoxContainer _contentBox;
        Button _nextButton;
        Button _backButton;
        Label _statusLabel;

        // Input fields
        LineEdit _nameField;
        SpinBox _ageSpinner;

        // Selection data
        string _characterName;
        int _age = 25;
        string _selectedSpecies = "Human";
        string _selectedCareer = "None";

        // Characteristics (2d6 rolls)
        Dictionary<string, int> _characteristics = new();

        // Species list (10 alien races from Traveller)
        static readonly string[] SpeciesList =
        {
            "Human", "Aslan", "Myrmidins", "Vargr", "Lilac Sucker",
            "Loxodonta", "Mellanni", "Hivemind", "Kilrathi", "Vasari"
        };

        static readonly string[] SpeciesDescriptions =
        {
            "Baseline humans from Earth and colonies.",
            "Feline humanoids from the Aslan Hegemony.",
            "Insectoid collective beings.",
            "Wolf-like humanoids from the Vargr territories.",
            "Amphibious humanoids with unique biology.",
            "Elephant-like humanoids, strong and wise.",
            "Tall, elegant humanoids with enhanced senses.",
            "Collective consciousness beings.",
            "Reptilian warriors from conquered worlds.",
            "Ancient alien race with advanced technology."
        };

        // Career list (8 careers)
        static readonly string[] CareerList =
        {
            "None", "Army", "Navy", "Merchant Service", "Explorers",
            "Rangers", "Secret Service", "Temple"
        };

        // References
        GameManager _gameManager;

        public override void _Ready()
        {
            _gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            BuildUI();
            RollCharacteristics();
            Visible = false;
        }

        /// <summary>
        /// Build the character creation UI.
        /// </summary>
        void BuildUI()
        {
            SetAnchorsPreset(Control.LayoutPreset.FullRect);

            var centerContainer = new CenterContainer
            {
            };
            AddChild(centerContainer);

            _mainPanel = new Panel
            {
                Size = new Vector2(500, 550)
            };
            centerContainer.AddChild(_mainPanel);

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

            var vbox = new VBoxContainer();
            vbox.AddThemeConstantOverride("separation", 10);

            // Step title
            _stepTitle = new Label
            {
                Text = "Character Creation",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _stepTitle.AddThemeFontSizeOverride("font_size", 24);
            _stepTitle.AddThemeColorOverride("font_color", new Color(0.4f, 0.7f, 1f));
            vbox.AddChild(_stepTitle);

            // Step description
            _stepDescription = new Label
            {
                Text = "",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            vbox.AddChild(_stepDescription);

            // Scrollable content area
            _scrollContainer = new ScrollContainer
            {
                Size = new Vector2(460, 380),
                VerticalScrollMode = ScrollContainer.ScrollMode.Auto,
                HorizontalScrollMode = ScrollContainer.ScrollMode.Disabled
            };
            _contentBox = new VBoxContainer();
            _contentBox.AddThemeConstantOverride("separation", 8);
            _scrollContainer.AddChild(_contentBox);
            vbox.AddChild(_scrollContainer);

            // Status label
            _statusLabel = new Label
            {
                Text = "",
                HorizontalAlignment = HorizontalAlignment.Center
            };
            _statusLabel.AddThemeColorOverride("font_color", new Color(1f, 0.4f, 0.4f));
            vbox.AddChild(_statusLabel);

            // Button row
            var hbox = new HBoxContainer();
            hbox.AddThemeConstantOverride("separation", 10);

            _backButton = new Button { Text = "BACK" };
            _backButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnBackPressed"));
            hbox.AddChild(_backButton);

            hbox.AddChild(new Control { SizeFlagsHorizontal = SizeFlags.Fill });

            _nextButton = new Button { Text = "NEXT" };
            _nextButton.Connect(Button.SignalName.Pressed, new Callable(this, "OnNextPressed"));
            hbox.AddChild(_nextButton);

            vbox.AddChild(hbox);

            _mainPanel.AddChild(vbox);

            // Show first step
            ShowStep(_currentStep);
        }

        /// <summary>
        /// Roll 2d6 for all 6 characteristics (Traveller style).
        /// </summary>
        void RollCharacteristics()
        {
            var rand = new System.Random();
            _characteristics["Strength"] = Roll2d6(rand);
            _characteristics["Dexterity"] = Roll2d6(rand);
            _characteristics["Endurance"] = Roll2d6(rand);
            _characteristics["Intelligence"] = Roll2d6(rand);
            _characteristics["Education"] = Roll2d6(rand);
            _characteristics["Social Standing"] = Roll2d6(rand);
        }

        int Roll2d6(System.Random rand)
        {
            return (rand.Next(1, 7) + rand.Next(1, 7));
        }

        /// <summary>
        /// Show a specific step.
        /// </summary>
        void ShowStep(Step step)
        {
            _currentStep = step;

            // Clear content
            while (_contentBox.GetChildCount() > 0)
            {
                var child = _contentBox.GetChild(0);
                _contentBox.RemoveChild(child);
                child.QueueFree();
            }

            switch (step)
            {
                case Step.NameAge:
                    ShowNameAgeStep();
                    break;
                case Step.Species:
                    ShowSpeciesStep();
                    break;
                case Step.Career:
                    ShowCareerStep();
                    break;
                case Step.Review:
                    ShowReviewStep();
                    break;
            }

            UpdateButtons();
        }

        /// <summary>
        /// Step 1: Name and Age.
        /// </summary>
        void ShowNameAgeStep()
        {
            _stepTitle.Text = "Step 1: Identity";
            _stepDescription.Text = "Enter your character's name and age";

            var nameLabel = new Label { Text = "Character Name:" };
            _contentBox.AddChild(nameLabel);

            _nameField = new LineEdit
            {
                PlaceholderText = "Enter character name",
                SizeFlagsHorizontal = SizeFlags.Fill
            };
            _contentBox.AddChild(_nameField);

            var ageLabel = new Label { Text = "Age:" };
            _contentBox.AddChild(ageLabel);

            _ageSpinner = new SpinBox
            {
                MinValue = 16,
                MaxValue = 100,
                Step = 1,
                Value = _age,
                SizeFlagsHorizontal = SizeFlags.Fill
            };
            _contentBox.AddChild(_ageSpinner);

            // Show characteristics preview
            var charLabel = new Label { Text = "\nCharacteristics (2d6 rolls):" };
            charLabel.AddThemeFontSizeOverride("font_size", 14);
            _contentBox.AddChild(charLabel);

            foreach (var kvp in _characteristics)
            {
                var row = new HBoxContainer();
                var keyLabel = new Label { Text = $"{kvp.Key}:" };
                keyLabel.CustomMinimumSize = new Vector2(120, 0);
                row.AddChild(keyLabel);

                var valueLabel = new Label { Text = kvp.Value.ToString() };
                var color = kvp.Value >= 10 ? new Color(0.4f, 1f, 0.4f) :
                            kvp.Value >= 7 ? new Color(1f, 1f, 0.4f) :
                            new Color(1f, 0.4f, 0.4f);
                valueLabel.AddThemeColorOverride("font_color", color);
                row.AddChild(valueLabel);

                _contentBox.AddChild(row);
            }

            var rerollBtn = new Button { Text = "Re-roll Characteristics" };
            rerollBtn.Connect(Button.SignalName.Pressed, new Callable(this, "OnRerollPressed"));
            _contentBox.AddChild(rerollBtn);
        }

        /// <summary>
        /// Step 2: Species selection.
        /// </summary>
        void ShowSpeciesStep()
        {
            _stepTitle.Text = "Step 2: Species";
            _stepDescription.Text = "Choose your character's species";

            for (int i = 0; i < SpeciesList.Length; i++)
            {
                var species = SpeciesList[i];
                var desc = SpeciesDescriptions[i];

                var btn = new Button
                {
                    Text = species,
                    SizeFlagsHorizontal = SizeFlags.Fill,
                    CustomMinimumSize = new Vector2(0, 40)
                };

                if (species == _selectedSpecies)
                {
                    btn.Disabled = true;
                    btn.Text += " ✓";
                }

                var idx = i; // capture for lambda
                btn.Connect(Button.SignalName.Pressed, Callable.From(() =>
                {
                    _selectedSpecies = SpeciesList[idx];
                    ShowStep(Step.Species);
                }));

                _contentBox.AddChild(btn);

                var descLabel = new Label { Text = desc };
                descLabel.AddThemeFontSizeOverride("font_size", 12);
                descLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.7f, 0.9f));
                _contentBox.AddChild(descLabel);
            }
        }

        /// <summary>
        /// Step 3: Career selection.
        /// </summary>
        void ShowCareerStep()
        {
            _stepTitle.Text = "Step 3: Career";
            _stepDescription.Text = "Choose your character's career path";

            var careerDescriptions = new Dictionary<string, string>
            {
                { "None", "No formal career — civilian or independent." },
                { "Army", "Ground forces — planetary defense and conquest." },
                { "Navy", "Space forces — fleet operations and exploration." },
                { "Merchant Service", "Trade and commerce across the galaxy." },
                { "Explorers", "Survey and map uncharted space." },
                { "Rangers", "Frontier law enforcement and survival." },
                { "Secret Service", "Intelligence and covert operations." },
                { "Temple", "Religious and scholarly pursuits." }
            };

            foreach (var career in CareerList)
            {
                var btn = new Button
                {
                    Text = career,
                    SizeFlagsHorizontal = SizeFlags.Fill,
                    CustomMinimumSize = new Vector2(0, 40)
                };

                if (career == _selectedCareer)
                {
                    btn.Disabled = true;
                    btn.Text += " ✓";
                }

                btn.Connect(Button.SignalName.Pressed, Callable.From(() =>
                {
                    _selectedCareer = career;
                    ShowStep(Step.Career);
                }));

                _contentBox.AddChild(btn);

                var descLabel = new Label { Text = careerDescriptions[career] };
                descLabel.AddThemeFontSizeOverride("font_size", 12);
                descLabel.AddThemeColorOverride("font_color", new Color(0.6f, 0.7f, 0.9f));
                _contentBox.AddChild(descLabel);
            }
        }

        /// <summary>
        /// Step 4: Review and confirm.
        /// </summary>
        void ShowReviewStep()
        {
            _stepTitle.Text = "Step 4: Review";
            _stepDescription.Text = "Confirm your character details";

            var info = new Label
            {
                Text = $"Name: {_characterName}\n" +
                       $"Age: {_age}\n" +
                       $"Species: {_selectedSpecies}\n" +
                       $"Career: {_selectedCareer}"
            };
            info.AddThemeFontSizeOverride("font_size", 16);
            _contentBox.AddChild(info);

            _contentBox.AddChild(new Control { CustomMinimumSize = new Vector2(0, 10) });

            var charTitle = new Label { Text = "Characteristics:" };
            charTitle.AddThemeFontSizeOverride("font_size", 14);
            _contentBox.AddChild(charTitle);

            foreach (var kvp in _characteristics)
            {
                var row = new HBoxContainer();
                var keyLabel = new Label { Text = $"{kvp.Key}:" };
                keyLabel.CustomMinimumSize = new Vector2(120, 0);
                row.AddChild(keyLabel);

                var valueLabel = new Label { Text = kvp.Value.ToString() };
                var color = kvp.Value >= 10 ? new Color(0.4f, 1f, 0.4f) :
                            kvp.Value >= 7 ? new Color(1f, 1f, 0.4f) :
                            new Color(1f, 0.4f, 0.4f);
                valueLabel.AddThemeColorOverride("font_color", color);
                row.AddChild(valueLabel);

                _contentBox.AddChild(row);
            }
        }

        /// <summary>
        /// Update button visibility and text.
        /// </summary>
        void UpdateButtons()
        {
            _backButton.Visible = _currentStep != Step.NameAge;

            if (_currentStep == Step.Review)
            {
                _nextButton.Text = "CREATE CHARACTER";
            }
            else
            {
                _nextButton.Text = "NEXT";
            }
        }

        /// <summary>
        /// Handle Next button press.
        /// </summary>
        async void OnNextPressed()
        {
            switch (_currentStep)
            {
                case Step.NameAge:
                    if (string.IsNullOrWhiteSpace(_nameField?.Text))
                    {
                        SetStatus("Please enter a character name");
                        return;
                    }
                    _characterName = _nameField.Text;
                    _age = (int)(_ageSpinner?.Value ?? 25);
                    ShowStep(Step.Species);
                    break;

                case Step.Species:
                    ShowStep(Step.Career);
                    break;

                case Step.Career:
                    ShowStep(Step.Review);
                    break;

                case Step.Review:
                    await CreateCharacter();
                    break;
            }
        }

        /// <summary>
        /// Handle Back button press.
        /// </summary>
        void OnBackPressed()
        {
            switch (_currentStep)
            {
                case Step.Species:
                    ShowStep(Step.NameAge);
                    break;
                case Step.Career:
                    ShowStep(Step.Species);
                    break;
                case Step.Review:
                    ShowStep(Step.Career);
                    break;
            }
        }

        /// <summary>
        /// Handle re-roll button press.
        /// </summary>
        void OnRerollPressed()
        {
            RollCharacteristics();
            ShowStep(Step.NameAge);
        }

        /// <summary>
        /// Create the character on the server.
        /// </summary>
        async Task CreateCharacter()
        {
            SetStatus("Creating character...");
            _nextButton.Disabled = true;
            _backButton.Disabled = true;

            if (_gameManager != null)
            {
                await _gameManager.CreateCharacterAsync(_characterName, _age, _selectedCareer);
            }

            _nextButton.Disabled = false;
            _backButton.Disabled = false;
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
            GD.Print($"[CharacterCreation] {message}");
        }

        /// <summary>
        /// Show the character creation screen.
        /// </summary>
        public void ShowScreen()
        {
            Visible = true;
            ShowStep(Step.NameAge);
        }

        /// <summary>
        /// Hide the character creation screen.
        /// </summary>
        public void HideScreen()
        {
            Visible = false;
        }
    }
}
