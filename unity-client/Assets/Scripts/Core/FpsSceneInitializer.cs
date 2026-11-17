using System.Linq;
using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.FPS;
using DoraMCU.Client.Network;
using DoraMCU.Client.Input;
using DoraMCU.Client.Battlefield;
using DoraMCU.Client.UI;
using UnityEngine.UI;
using UnityEngine.EventSystems;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Initializes the Earth2026 RPG world scene with FPS combat overlay.
    /// <para>
    /// Primary mode: RPG world exploration at Younger Industries HQ with third-person camera,
    /// inventory, dialogue, and interaction systems.
    /// Overlay mode: FPS combat that activates during combat encounters with first-person camera,
    /// weapon controller, and combat HUD.
    /// </para>
    /// <para>
    /// Runs automatically via RuntimeInitializeLoadType.BeforeSceneLoad.
    /// </para>
    /// </summary>
    public class FpsSceneInitializer
    {
        private static bool isInitialized = false;
        private static bool combatModeActive = false;

        /// <summary>
        /// Current game mode: Exploration (RPG) or Combat (FPS overlay).
        /// </summary>
        public enum WorldMode { Exploration, Combat }
        private static WorldMode currentMode = WorldMode.Exploration;

        /// <summary>
        /// Get the current world mode.
        /// </summary>
        public static WorldMode CurrentMode => currentMode;

        /// <summary>
        /// Check if combat mode is currently active.
        /// </summary>
        public static bool IsCombatMode => currentMode == WorldMode.Combat;

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
        private static void InitializeFpsScene()
        {
            if (isInitialized)
            {
                Debug.Log("[FpsSceneInitializer] Already initialized, skipping");
                return;
            }

            Debug.Log("[FpsSceneInitializer] Initializing Earth2026 RPG world with FPS combat overlay...");

            // Core singletons for both RPG and FPS modes
            CreateFpsSingletons();
            
            // Player and dual camera system (third-person for exploration, first-person for combat)
            CreatePlayerAndDualCamera();
            
            // UI canvas shared by both modes
            CreateUICanvas();
            
            // RPG exploration systems
            CreateRpgExplorationSystems();
            
            // FPS combat overlay systems (initially hidden)
            CreateFpsCombatOverlay();
            
            // Map loader for the Earth2026 world
            CreateMapLoader();

            // Start in exploration mode
            SetMode(WorldMode.Exploration);

            isInitialized = true;
            Debug.Log("[FpsSceneInitializer] Earth2026 RPG world initialization complete - starting in exploration mode");
        }

        /// <summary>
        /// Switch between exploration and combat modes.
        /// </summary>
        public static void SetMode(WorldMode mode)
        {
            if (currentMode == mode) return;
            currentMode = mode;

            if (mode == WorldMode.Combat)
            {
                EnterCombatMode();
            }
            else
            {
                EnterExplorationMode();
            }
        }

        /// <summary>
        /// Enter FPS combat overlay mode.
        /// </summary>
        private static void EnterCombatMode()
        {
            Debug.Log("[FpsSceneInitializer] Entering FPS combat mode");
            combatModeActive = true;

            // Lock cursor for FPS control
            Cursor.lockState = CursorLockMode.Locked;
            Cursor.visible = false;

            // Show FPS HUD elements
            ShowFpsHud(true);

            // Hide RPG exploration UI
            ShowRpgHud(false);

            // Switch to first-person camera
            SwitchToFpsCamera();

            Debug.Log("[FpsSceneInitializer] FPS combat mode active");
        }

        /// <summary>
        /// Return to RPG exploration mode.
        /// </summary>
        private static void EnterExplorationMode()
        {
            Debug.Log("[FpsSceneInitializer] Returning to RPG exploration mode");
            combatModeActive = false;

            // Unlock cursor for exploration
            Cursor.lockState = CursorLockMode.None;
            Cursor.visible = true;

            // Hide FPS HUD elements
            ShowFpsHud(false);

            // Show RPG exploration UI
            ShowRpgHud(true);

            // Switch to third-person camera
            SwitchToExplorationCamera();

            Debug.Log("[FpsSceneInitializer] RPG exploration mode active");
        }

        /// <summary>
        /// Toggle between exploration and combat modes.
        /// Call this method when combat is triggered or resolved.
        /// </summary>
        public static void ToggleCombatMode()
        {
            SetMode(currentMode == WorldMode.Exploration ? WorldMode.Combat : WorldMode.Exploration);
        }

        /// <summary>
        /// Force enter combat mode (called by combat triggers).
        /// </summary>
        public static void ForceCombatMode()
        {
            SetMode(WorldMode.Combat);
        }

        /// <summary>
        /// Force return to exploration mode (called when combat ends).
        /// </summary>
        public static void ForceExplorationMode()
        {
            SetMode(WorldMode.Exploration);
        }

        /// <summary>
        /// Creates all FPS-specific singleton managers.
        /// </summary>
        private static void CreateFpsSingletons()
        {
            // FpsNetworkManager
            if (FpsNetworkManager.Instance == null)
            {
                var go = new GameObject("FpsNetworkManager");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<FpsNetworkManager>();
                Debug.Log("[FpsSceneInitializer] FpsNetworkManager created");
            }

            // FpsGameState
            if (FpsGameState.Instance == null)
            {
                var go = new GameObject("FpsGameState");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<FpsGameState>();
                Debug.Log("[FpsSceneInitializer] FpsGameState created");
            }
        }

        // References to cameras for mode switching
        private static GameObject explorationCameraGo;
        private static GameObject fpsCameraGo;
        private static GameObject playerGo;

        /// <summary>
        /// Creates the player with a dual camera system:
        /// - Third-person camera for RPG exploration mode
        /// - First-person camera for FPS combat mode
        /// </summary>
        private static void CreatePlayerAndDualCamera()
        {
            // Check if player already exists
            if (GameObject.FindGameObjectWithTag("Player") != null)
            {
                Debug.Log("[FpsSceneInitializer] Player already exists");
                return;
            }

            // Create player GameObject
            playerGo = new GameObject("Player");
            playerGo.tag = "Player";
            playerGo.transform.position = new Vector3(0f, 1.7f, 0f);
            Object.DontDestroyOnLoad(playerGo);

            // Add CharacterController
            var characterController = playerGo.AddComponent<CharacterController>();
            characterController.height = 1.7f;
            characterController.radius = 0.25f;
            characterController.slopeLimit = 45f;
            characterController.stepOffset = 0.3f;
            characterController.skinWidth = 0.02f;
            characterController.minMoveDistance = 0.001f;
            characterController.center = new Vector3(0f, 0.85f, 0f);

            // Add FpsPlayerController for movement (works in both modes)
            var fpsController = playerGo.AddComponent<FpsPlayerController>();
            fpsController.walkSpeed = 5f;
            fpsController.runSpeed = 9f;
            fpsController.crouchSpeed = 2.5f;
            fpsController.jumpForce = 8f;
            fpsController.gravity = -20f;
            fpsController.mouseSensitivity = 2f;
            fpsController.lookClamp = 80f;
            fpsController.standingHeight = 1.7f;
            fpsController.crouchingHeight = 0.9f;
            fpsController.crouchTransitionSpeed = 10f;

            // Create third-person exploration camera (parented to player)
            explorationCameraGo = new GameObject("ExplorationCamera");
            explorationCameraGo.tag = "MainCamera";
            explorationCameraGo.transform.SetParent(playerGo.transform);
            explorationCameraGo.transform.localPosition = new Vector3(0f, 2.5f, -4f);
            explorationCameraGo.transform.localRotation = Quaternion.Euler(15f, 0f, 0f);
            Object.DontDestroyOnLoad(explorationCameraGo);

            var explorationCamera = explorationCameraGo.AddComponent<Camera>();
            explorationCamera.nearClipPlane = 0.1f;
            explorationCamera.farClipPlane = 1000f;
            explorationCamera.fieldOfView = 60f;
            explorationCamera.clearFlags = CameraClearFlags.Skybox;
            explorationCamera.backgroundColor = new Color(0.192f, 0.302f, 0.475f);
            explorationCameraGo.AddComponent<AudioListener>();

            // Create first-person combat camera (child of player, initially disabled)
            fpsCameraGo = new GameObject("FpsCamera");
            fpsCameraGo.transform.SetParent(playerGo.transform);
            fpsCameraGo.transform.localPosition = new Vector3(0f, 1.6f, 0f);
            fpsCameraGo.transform.localRotation = Quaternion.identity;
            fpsCameraGo.SetActive(false);
            Object.DontDestroyOnLoad(fpsCameraGo);

            var fpsCamera = fpsCameraGo.AddComponent<Camera>();
            fpsCamera.nearClipPlane = 0.1f;
            fpsCamera.farClipPlane = 1000f;
            fpsCamera.fieldOfView = 75f;
            fpsCamera.clearFlags = CameraClearFlags.Skybox;
            fpsCamera.backgroundColor = new Color(0.192f, 0.302f, 0.475f);

            // Add FpsWeaponController for combat mode
            playerGo.AddComponent<FpsWeaponController>();

            // Add FpsPlayerPrediction for client-side prediction
            playerGo.AddComponent<FpsPlayerPrediction>();

            Debug.Log("[FpsSceneInitializer] Player and dual camera system created");
        }

        /// <summary>
        /// Switch to the first-person FPS camera.
        /// </summary>
        private static void SwitchToFpsCamera()
        {
            if (explorationCameraGo != null) explorationCameraGo.SetActive(false);
            if (fpsCameraGo != null)
            {
                fpsCameraGo.SetActive(true);
                var cam = fpsCameraGo.GetComponent<Camera>();
                if (cam != null) cam.tag = "MainCamera";
            }
        }

        /// <summary>
        /// Switch to the third-person exploration camera.
        /// </summary>
        private static void SwitchToExplorationCamera()
        {
            if (fpsCameraGo != null) fpsCameraGo.SetActive(false);
            if (explorationCameraGo != null)
            {
                explorationCameraGo.SetActive(true);
                var cam = explorationCameraGo.GetComponent<Camera>();
                if (cam != null) cam.tag = "MainCamera";
            }
        }

        // References to UI groups for mode switching
        private static GameObject fpsHudGroup;
        private static GameObject rpgHudGroup;

        /// <summary>
        /// Creates the UI canvas with both RPG exploration and FPS combat HUD elements.
        /// </summary>
        private static void CreateUICanvas()
        {
            // Check if canvas already exists
            var existingCanvas = Object.FindObjectOfType<Canvas>();
            if (existingCanvas != null)
            {
                Debug.Log("[FpsSceneInitializer] Canvas already exists");
                return;
            }

            // Create canvas
            var canvasGo = new GameObject("UICanvas");
            var canvas = canvasGo.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.pixelPerfect = false;

            // Add CanvasScaler
            var scaler = canvasGo.AddComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1920, 1080);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            // Add GraphicRaycaster
            canvasGo.AddComponent<GraphicRaycaster>();

            // Create EventSystem
            var eventSystem = new GameObject("EventSystem");
            eventSystem.AddComponent<EventSystem>();
            eventSystem.AddComponent<StandaloneInputModule>();

            // Create FPS HUD group (initially hidden)
            fpsHudGroup = new GameObject("FpsHudGroup");
            fpsHudGroup.transform.SetParent(canvasGo.transform);
            fpsHudGroup.SetActive(false);

            // Create crosshair (FPS only)
            var crosshairGo = new GameObject("Crosshair");
            crosshairGo.transform.SetParent(fpsHudGroup.transform);
            var crosshairRect = crosshairGo.AddComponent<RectTransform>();
            crosshairRect.anchorMin = new Vector2(0.5f, 0.5f);
            crosshairRect.anchorMax = new Vector2(0.5f, 0.5f);
            crosshairRect.anchoredPosition = Vector2.zero;
            crosshairRect.sizeDelta = new Vector2(20f, 20f);
            crosshairGo.AddComponent<CanvasRenderer>();
            var crosshairImage = crosshairGo.AddComponent<Image>();
            crosshairImage.color = Color.white;
            crosshairImage.raycastTarget = false;

            // Create FPS health bar background
            var healthBgGo = new GameObject("HealthBarBackground");
            healthBgGo.transform.SetParent(fpsHudGroup.transform);
            var healthBgRect = healthBgGo.AddComponent<RectTransform>();
            healthBgRect.anchorMin = new Vector2(0f, 0f);
            healthBgRect.anchorMax = new Vector2(0f, 0f);
            healthBgRect.anchoredPosition = new Vector2(200f, 50f);
            healthBgRect.sizeDelta = new Vector2(300f, 30f);
            healthBgGo.AddComponent<CanvasRenderer>();
            var healthBgImage = healthBgGo.AddComponent<Image>();
            healthBgImage.color = new Color(0.3f, 0.3f, 0.3f, 1f);
            healthBgImage.raycastTarget = false;

            // Create FPS health bar fill
            var healthBarGo = new GameObject("HealthBar");
            healthBarGo.transform.SetParent(healthBgGo.transform);
            var healthBarRect = healthBarGo.AddComponent<RectTransform>();
            healthBarRect.anchorMin = Vector2.zero;
            healthBarRect.anchorMax = Vector2.one;
            healthBarRect.anchoredPosition = Vector2.zero;
            healthBarRect.sizeDelta = Vector2.zero;
            healthBarGo.AddComponent<CanvasRenderer>();
            var healthBarImage = healthBarGo.AddComponent<Image>();
            healthBarImage.color = Color.green;
            healthBarImage.fillMethod = Image.FillMethod.Horizontal;
            healthBarImage.fillAmount = 1f;
            healthBarImage.raycastTarget = false;

            // Create FPS ammo text
            var ammoTextGo = new GameObject("AmmoText");
            ammoTextGo.transform.SetParent(fpsHudGroup.transform);
            var ammoTextRect = ammoTextGo.AddComponent<RectTransform>();
            ammoTextRect.anchorMin = new Vector2(1f, 0f);
            ammoTextRect.anchorMax = new Vector2(1f, 0f);
            ammoTextRect.anchoredPosition = new Vector2(-50f, 50f);
            ammoTextRect.sizeDelta = new Vector2(200f, 50f);
            ammoTextGo.AddComponent<CanvasRenderer>();
            var ammoText = ammoTextGo.AddComponent<Text>();
            ammoText.text = "30 / 30";
            ammoText.fontSize = 24;
            ammoText.fontStyle = FontStyle.Bold;
            ammoText.alignment = TextAnchor.MiddleRight;
            ammoText.color = Color.white;
            ammoText.raycastTarget = false;

            // Create RPG HUD group (visible by default)
            rpgHudGroup = new GameObject("RpgHudGroup");
            rpgHudGroup.transform.SetParent(canvasGo.transform);
            rpgHudGroup.SetActive(true);

            // Create RPG location text
            var locationTextGo = new GameObject("LocationText");
            locationTextGo.transform.SetParent(rpgHudGroup.transform);
            var locationTextRect = locationTextGo.AddComponent<RectTransform>();
            locationTextRect.anchorMin = new Vector2(0f, 1f);
            locationTextRect.anchorMax = new Vector2(1f, 1f);
            locationTextRect.anchoredPosition = Vector2.zero;
            locationTextRect.sizeDelta = new Vector2(-400f, 50f);
            locationTextGo.AddComponent<CanvasRenderer>();
            var locationText = locationTextGo.AddComponent<Text>();
            locationText.text = "Younger Industries HQ — San Diego, CA";
            locationText.fontSize = 18;
            locationText.fontStyle = FontStyle.Bold;
            locationText.alignment = TextAnchor.UpperCenter;
            locationText.color = new Color(1f, 1f, 1f, 0.8f);
            locationText.raycastTarget = false;

            // Create RPG interaction prompt
            var interactionPromptGo = new GameObject("InteractionPrompt");
            interactionPromptGo.transform.SetParent(rpgHudGroup.transform);
            var interactionPromptRect = interactionPromptGo.AddComponent<RectTransform>();
            interactionPromptRect.anchorMin = new Vector2(0.5f, 0f);
            interactionPromptRect.anchorMax = new Vector2(0.5f, 0f);
            interactionPromptRect.anchoredPosition = new Vector2(0f, 100f);
            interactionPromptRect.sizeDelta = new Vector2(400f, 50f);
            interactionPromptGo.AddComponent<CanvasRenderer>();
            var interactionPromptText = interactionPromptGo.AddComponent<Text>();
            interactionPromptText.text = "";
            interactionPromptText.fontSize = 16;
            interactionPromptText.alignment = TextAnchor.MiddleCenter;
            interactionPromptText.color = new Color(1f, 1f, 0f, 0.9f);
            interactionPromptText.raycastTarget = false;
            interactionPromptGo.SetActive(false);

            // Create RPG minimap panel
            var minimapGo = new GameObject("MinimapPanel");
            minimapGo.transform.SetParent(rpgHudGroup.transform);
            var minimapRect = minimapGo.AddComponent<RectTransform>();
            minimapRect.anchorMin = new Vector2(1f, 1f);
            minimapRect.anchorMax = new Vector2(1f, 1f);
            minimapRect.anchoredPosition = new Vector2(-100f, -100f);
            minimapRect.sizeDelta = new Vector2(200f, 200f);
            minimapGo.AddComponent<CanvasRenderer>();
            var minimapImage = minimapGo.AddComponent<Image>();
            minimapImage.color = new Color(0f, 0f, 0f, 0.6f);
            minimapImage.raycastTarget = false;

            Debug.Log("[FpsSceneInitializer] UI Canvas with RPG and FPS HUD groups created");
        }

        /// <summary>
        /// Show or hide the FPS combat HUD.
        /// </summary>
        private static void ShowFpsHud(bool show)
        {
            if (fpsHudGroup != null) fpsHudGroup.SetActive(show);
        }

        /// <summary>
        /// Show or hide the RPG exploration HUD.
        /// </summary>
        private static void ShowRpgHud(bool show)
        {
            if (rpgHudGroup != null) rpgHudGroup.SetActive(show);
        }

        /// <summary>
        /// Creates the RPG exploration systems: interaction manager, dialogue system,
        /// inventory, and narrative system.
        /// </summary>
        private static void CreateRpgExplorationSystems()
        {
            // Interaction manager for handling object interactions in the world
            var interactionGo = new GameObject("RpgInteractionManager");
            Object.DontDestroyOnLoad(interactionGo);
            var interactionManager = interactionGo.AddComponent<RpgInteractionManager>();
            interactionManager.interactionDistance = 3f;
            interactionManager.interactionKey = KeyCode.E;
            Debug.Log("[FpsSceneInitializer] RPG interaction manager created");

            // Dialogue system for NPC conversations
            if (DoraMCU.Client.UI.DialogSystem.Instance == null)
            {
                var dialogGo = new GameObject("DialogSystem");
                Object.DontDestroyOnLoad(dialogGo);
                dialogGo.AddComponent<DoraMCU.Client.UI.DialogSystem>();
                Debug.Log("[FpsSceneInitializer] Dialogue system created");
            }

            // Inventory system for item management
            if (DoraMCU.Client.UI.InventoryUI.Instance == null)
            {
                var inventoryGo = new GameObject("InventoryUI");
                Object.DontDestroyOnLoad(inventoryGo);
                inventoryGo.AddComponent<DoraMCU.Client.UI.InventoryUI>();
                Debug.Log("[FpsSceneInitializer] Inventory system created");
            }

            // Narrative system for quests and story progression
            if (DoraMCU.Client.UI.NarrativeSystem.Instance == null)
            {
                var narrativeGo = new GameObject("NarrativeSystem");
                Object.DontDestroyOnLoad(narrativeGo);
                narrativeGo.AddComponent<DoraMCU.Client.UI.NarrativeSystem>();
                Debug.Log("[FpsSceneInitializer] Narrative system created");
            }

            Debug.Log("[FpsSceneInitializer] RPG exploration systems initialized");
        }

        /// <summary>
        /// Creates the FPS combat overlay systems that activate during combat.
        /// </summary>
        private static void CreateFpsCombatOverlay()
        {
            // FpsHUD for combat UI
            var existingHud = Object.FindObjectOfType<FpsHUD>();
            if (existingHud != null)
            {
                Debug.Log("[FpsSceneInitializer] FpsHUD already exists");
                return;
            }

            var hudGo = new GameObject("FpsHUD");
            Object.DontDestroyOnLoad(hudGo);
            var hud = hudGo.AddComponent<FpsHUD>();

            // Wire up UI references from the FPS HUD group
            var crosshair = Object.FindObjectsOfType<Image>()
                .FirstOrDefault(img => img.gameObject.name == "Crosshair");
            if (crosshair != null)
            {
                hud.crosshair = crosshair;
            }

            var healthBar = Object.FindObjectsOfType<Image>()
                .FirstOrDefault(img => img.gameObject.name == "HealthBar");
            if (healthBar != null)
            {
                hud.healthBar = healthBar;
            }

            var ammoText = Object.FindObjectsOfType<Text>()
                .FirstOrDefault(txt => txt.gameObject.name == "AmmoText");
            if (ammoText != null)
            {
                hud.ammoText = ammoText;
            }

            hud.crosshairSpread = 5f;
            hud.maxKillFeedItems = 5;
            hud.killFeedDisplayTime = 5f;

            Debug.Log("[FpsSceneInitializer] FPS combat overlay HUD created");
        }

        /// <summary>
        /// Creates the map loader for the Earth2026 Younger Industries HQ world.
        /// </summary>
        private static void CreateMapLoader()
        {
            var loaderGo = new GameObject("FpsMapLoader");
            Object.DontDestroyOnLoad(loaderGo);
            var mapLoader = loaderGo.AddComponent<FpsMapLoader>();

            // Generate the default map immediately so the player has a playable environment
            mapLoader.GenerateDefaultMap();

            Debug.Log("[FpsSceneInitializer] Map loader created for Earth2026 world");
        }
    }

    /// <summary>
    /// Manages player interactions with objects and NPCs in the RPG world.
    /// Handles proximity detection and interaction triggers.
    /// </summary>
    public class RpgInteractionManager : MonoBehaviour
    {
        [Header("Interaction Settings")]
        [SerializeField] internal float interactionDistance = 3f;
        [SerializeField] internal KeyCode interactionKey = KeyCode.E;

        [Header("Interaction Layer")]
        [SerializeField] private LayerMask interactableLayer;

        private GameObject currentInteractable;
        private string currentInteractionPrompt;
        private GameObject player;

        /// <summary>
        /// Event fired when an interaction is triggered.
        /// </summary>
        public System.Action<GameObject, string> OnInteraction;

        void Start()
        {
            player = GameObject.FindGameObjectWithTag("Player");
        }

        void Update()
        {
            if (FpsSceneInitializer.IsCombatMode) return;

            // Check for interactable objects
            UpdateInteractable();

            // Handle interaction input
            if (currentInteractable != null && InputSafe.GetKeyDown(interactionKey))
            {
                TriggerInteraction();
            }
        }

        private void UpdateInteractable()
        {
            if (player == null) return;

            var playerPos = player.transform.position;
            var forward = player.transform.forward;

            RaycastHit hit;
            if (Physics.Raycast(playerPos + Vector3.up, forward, out hit, interactionDistance, interactableLayer))
            {
                currentInteractable = hit.collider.gameObject;
                currentInteractionPrompt = GetInteractionPrompt(currentInteractable);
            }
            else
            {
                currentInteractable = null;
            }
        }

        private void TriggerInteraction()
        {
            if (currentInteractable != null)
            {
                Debug.Log($"[RpgInteractionManager] Interacting with: {currentInteractable.name}");
                OnInteraction?.Invoke(currentInteractable, currentInteractionPrompt);

                // Check if interaction triggers combat
                if (currentInteractable.CompareTag("Hostile") || currentInteractable.CompareTag("CombatTrigger"))
                {
                    FpsSceneInitializer.ForceCombatMode();
                }
            }
        }

        private string GetInteractionPrompt(GameObject target)
        {
            if (target.CompareTag("NPC")) return "[E] Talk to NPC";
            if (target.CompareTag("Door")) return "[E] Open Door";
            if (target.CompareTag("Item")) return "[E] Pick Up Item";
            if (target.CompareTag("Terminal")) return "[E] Use Terminal";
            if (target.CompareTag("Hostile")) return "[E] Engage Combat";
            if (target.CompareTag("CombatTrigger")) return "[E] Enter Combat Zone";
            return "[E] Interact";
        }
    }
}
