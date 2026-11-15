using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.Input;
using DoraMCU.Client.Battlefield;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Auto-initializes all DoraMCU systems when the game starts.
    /// <para>
    /// Creates the main camera, lighting, and all Singleton managers
    /// (GameManager, NetworkManager, InputHandler, BattlefieldRenderer, BattlefieldService).
    /// Runs automatically via RuntimeInitializeLoadType before scene loading.
    /// No need to place this in a scene.
    /// </para>
    /// </summary>
    public class DoraMCUInitializer
    {
        private static bool isInitialized = false;

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
        private static void InitializeOnLoad()
        {
            if (isInitialized)
            {
                Debug.Log("[DoraMCUInitializer] Already initialized, skipping");
                return;
            }

            InitializeCamera();
            InitializeLighting();
            InitializeSingletons();

            isInitialized = true;
            Debug.Log("[DoraMCUInitializer] All systems initialized");
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

            // Add BattlefieldCamera controller
            cameraGo.AddComponent<BattlefieldCamera>();

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

            // BattlefieldRenderer
            if (BattlefieldRenderer.Instance == null)
            {
                var go = new GameObject("BattlefieldRenderer");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<BattlefieldRenderer>();
                Debug.Log("[DoraMCUInitializer] BattlefieldRenderer created");
            }

            // BattlefieldService
            if (BattlefieldService.Instance == null)
            {
                var go = new GameObject("BattlefieldService");
                Object.DontDestroyOnLoad(go);
                go.AddComponent<BattlefieldService>();
                Debug.Log("[DoraMCUInitializer] BattlefieldService created");
            }
        }
    }
}
