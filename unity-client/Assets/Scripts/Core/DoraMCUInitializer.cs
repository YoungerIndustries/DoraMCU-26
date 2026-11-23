using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.Input;
// using DoraMCU.Client.Battlefield; // Disabled for build
using DoraMCU.Client.UI;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Auto-initializes all DoraMCU systems when the game starts.
    /// Creates the main camera, lighting, and all Singleton managers.
    /// Uses AfterSceneLoad to ensure Unity's game loop is running.
    /// </summary>
    public class DoraMCUInitializer : MonoBehaviour
    {
        private static bool isInitialized = false;

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.AfterSceneLoad)]
        private static void InitializeAfterScene()
        {
            if (isInitialized)
            {
                Debug.Log("[DoraMCUInitializer] Already initialized, skipping");
                return;
            }

            // Create a bootstrap GameObject that will run in the game loop
            var bootstrap = new GameObject("DoraMCUBootstrap");
            bootstrap.AddComponent<DoraMCUInitializer>();
            Object.DontDestroyOnLoad(bootstrap);
        }

        void Awake()
        {
            Debug.Log("[DoraMCUInitializer] Bootstrap Awake() — creating systems");
            InitializeCamera();
            InitializeLighting();
            InitializeSingletons();

            isInitialized = true;
            Debug.Log("[DoraMCUInitializer] All systems initialized");
            Debug.Log("[DoraMCUInitializer] MainHUD.Instance null: " + (MainHUD.Instance == null));
            Debug.Log("[DoraMCUInitializer] MenuManager.Instance null: " + (MenuManager.Instance == null));
        }

        void Start()
        {
            Debug.Log("[DoraMCUInitializer] Bootstrap Start() — game loop is running");
        }

        void Update()
        {
            // Keep the bootstrap alive; game loop is confirmed running
        }

        /// <summary>
        /// Creates the main camera with BattlefieldCamera controller.
        /// </summary>
        private static void InitializeCamera()
        {
            // Check if a main camera already exists
            var existingCamera = Camera.main;
            if (existingCamera != null)
            {
                Debug.Log("[DoraMCUInitializer] Main camera already exists");
                return;
            }

            // Create the main camera GameObject
            var cameraGo = new GameObject("MainCamera");
            cameraGo.tag = "MainCamera";
            Object.DontDestroyOnLoad(cameraGo);

            // Add Camera component
            var camera = cameraGo.AddComponent<Camera>();
            camera.nearClipPlane = 0.1f;
            camera.farClipPlane = 10000f;
            camera.fieldOfView = 60f;
            camera.clearFlags = CameraClearFlags.Skybox;

            // BattlefieldCamera disabled for build

            // Position camera above the battlefield
            cameraGo.transform.position = new Vector3(0f, 200f, -300f);
            cameraGo.transform.LookAt(Vector3.zero);

            Debug.Log("[DoraMCUInitializer] Main camera created at " + cameraGo.transform.position);
        }

        /// <summary>
        /// Creates directional and ambient lighting.
        /// </summary>
        private static void InitializeLighting()
        {
            // Check if directional light already exists
            var existingLights = UnityEngine.Object.FindObjectsByType<Light>(UnityEngine.FindObjectsSortMode.None);
            foreach (var l in existingLights)
            {
                if (l.type == LightType.Directional)
                {
                    Debug.Log("[DoraMCUInitializer] Directional light already exists");
                    return;
                }
            }

            // Create directional light (sun)
            var lightGo = new GameObject("DirectionalLight");
            Object.DontDestroyOnLoad(lightGo);
            var light = lightGo.AddComponent<Light>();
            light.type = LightType.Directional;
            light.color = Color.white;
            light.intensity = 1f;
            light.shadows = LightShadows.Soft;
            lightGo.transform.rotation = Quaternion.Euler(50f, 0f, 0f);

            // Set ambient lighting
            RenderSettings.ambientMode = UnityEngine.Rendering.AmbientMode.Skybox;
            RenderSettings.ambientSkyColor = new Color(0.5f, 0.5f, 0.6f);
            RenderSettings.fog = false;

            Debug.Log("[DoraMCUInitializer] Directional light created");
        }

        /// <summary>
        /// Creates all Singleton manager GameObjects.
        /// </summary>
        private static void InitializeSingletons()
        {
            // GameManager
            if (GameManager.Instance == null)
            {
                var go = new GameObject("GameManager");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<GameManager>();
                Debug.Log("[DoraMCUInitializer] GameManager created");
            }

            // NetworkManager
            if (NetworkManager.Instance == null)
            {
                var go = new GameObject("NetworkManager");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<NetworkManager>();
                Debug.Log("[DoraMCUInitializer] NetworkManager created");
            }

            // InputHandler
            if (InputHandler.Instance == null)
            {
                var go = new GameObject("InputHandler");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<InputHandler>();
                Debug.Log("[DoraMCUInitializer] InputHandler created");
            }

            // BattlefieldRenderer - disabled for build
            // if (BattlefieldRenderer.Instance == null)
            // {
            //     var go = new GameObject("BattlefieldRenderer");
            //     Object.DontDestroyOnLoad(go);
            //     go.AddComponent<BattlefieldRenderer>();
            //     Debug.Log("[DoraMCUInitializer] BattlefieldRenderer created");
            // }

            // BattlefieldService - disabled for build
            // if (BattlefieldService.Instance == null)
            // {
            //     var go = new GameObject("BattlefieldService");
            //     Object.DontDestroyOnLoad(go);
            //     go.AddComponent<BattlefieldService>();
            //     Debug.Log("[DoraMCUInitializer] BattlefieldService created");
            // }

            // FontLoader (must be before UI components)
            if (FontLoader.Instance == null)
            {
                var go = new GameObject("FontLoader");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<FontLoader>();
                Debug.Log("[DoraMCUInitializer] FontLoader created");
            }

            // ServerConfig
            if (ServerConfig.Instance == null)
            {
                var go = new GameObject("ServerConfig");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<ServerConfig>();
                Debug.Log("[DoraMCUInitializer] ServerConfig created");
            }

            // MainHUD
            if (MainHUD.Instance == null)
            {
                var go = new GameObject("MainHUD");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<MainHUD>();
                Debug.Log("[DoraMCUInitializer] MainHUD created");
            }

            // MenuManager
            if (MenuManager.Instance == null)
            {
                var go = new GameObject("MenuManager");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<MenuManager>();
                Debug.Log("[DoraMCUInitializer] MenuManager created");
            }

            // LoginScreen
            if (LoginScreen.Instance == null)
            {
                var go = new GameObject("LoginScreen");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<LoginScreen>();
                Debug.Log("[DoraMCUInitializer] LoginScreen created");
            }

            // CharacterCreation
            if (CharacterCreation.Instance == null)
            {
                var go = new GameObject("CharacterCreation");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<CharacterCreation>();
                Debug.Log("[DoraMCUInitializer] CharacterCreation created");
            }

            // ShipBuildUI
            if (ShipBuildUI.Instance == null)
            {
                var go = new GameObject("ShipBuildUI");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<ShipBuildUI>();
                Debug.Log("[DoraMCUInitializer] ShipBuildUI created");
            }

            // ExploreAction
            if (ExploreAction.Instance == null)
            {
                var go = new GameObject("ExploreAction");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<ExploreAction>();
                Debug.Log("[DoraMCUInitializer] ExploreAction created");
            }

            // InventoryUI
            if (InventoryUI.Instance == null)
            {
                var go = new GameObject("InventoryUI");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<InventoryUI>();
                Debug.Log("[DoraMCUInitializer] InventoryUI created");
            }

            // MapUI
            if (MapUI.Instance == null)
            {
                var go = new GameObject("MapUI");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<MapUI>();
                Debug.Log("[DoraMCUInitializer] MapUI created");
            }

            // HelpOverlay
            if (HelpOverlay.Instance == null)
            {
                var go = new GameObject("HelpOverlay");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<HelpOverlay>();
                Debug.Log("[DoraMCUInitializer] HelpOverlay created");
            }
        }
    }
}
