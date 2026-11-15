using UnityEngine;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// VR support manager using OpenXR integration.
    /// Enables VR headset support with hand tracking and spatial audio.
    /// </summary>
    public class VRManager : Singleton<VRManager>
    {
        [Header("VR Settings")]
        [SerializeField] private bool enableVR = false;
        [SerializeField] private bool enableHandTracking = true;
        [SerializeField] private bool enableSpatialAudio = true;
        [SerializeField] private float teleportDistance = 5f;
        [SerializeField] private float teleportCooldown = 0.5f;

        [Header("VR Rig")]
        private GameObject vrRig;
        private GameObject leftController;
        private GameObject rightController;
        private Camera vrCamera;

        [Header("Teleportation")]
        private bool isTeleporting;
        private float teleportTimer;
        private GameObject teleportIndicator;
        private Ray teleportRay;

        [Header("Hand Tracking")]
        private bool leftHandTracking;
        private bool rightHandTracking;
        private Vector3[] leftHandJoints = new Vector3[25];
        private Vector3[] rightHandJoints = new Vector3[25];

        public bool IsVRActive => enableVR && vrRig != null;
        public bool IsLeftHandTracking => leftHandTracking;
        public bool IsRightHandTracking => rightHandTracking;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            if (enableVR)
            {
                InitializeVR();
            }
        }

        /// <summary>
        /// Initialize VR support.
        /// </summary>
        public void InitializeVR()
        {
            #if UNITY_XR_QUEST_SUPPORT || UNITY_XR_OPENXR_SUPPORT
            // Create VR rig
            vrRig = new GameObject("VRRig");
            vrRig.transform.position = Vector3.zero;

            // VR Camera
            var cameraGo = new GameObject("VRCamera");
            cameraGo.transform.SetParent(vrRig.transform);
            cameraGo.transform.localPosition = new Vector3(0, 1.6f, 0);
            vrCamera = cameraGo.AddComponent<Camera>();
            vrCamera.stereoTargetEye = StereoTargetEyeMask.Both;

            // Left controller
            leftController = new GameObject("LeftController");
            leftController.transform.SetParent(vrRig.transform);
            leftController.AddComponent<XRController>();

            // Right controller
            rightController = new GameObject("RightController");
            rightController.transform.SetParent(vrRig.transform);
            rightController.AddComponent<XRController>();

            // Teleport indicator
            CreateTeleportIndicator();

            // Enable XR loading
            UnityEngine.XR.XRSettings.loadedDeviceName = "OpenXR";
            UnityEngine.XR.XRSettings.enabled = true;

            enableVR = true;
            Debug.Log("[VRManager] VR initialized with OpenXR");
            #else
            Debug.LogWarning("[VRManager] XR support not available in this build");
            enableVR = false;
            #endif
        }

        /// <summary>
        /// Create teleportation indicator.
        /// </summary>
        private void CreateTeleportIndicator()
        {
            teleportIndicator = new GameObject("TeleportIndicator");
            var circle = teleportIndicator.AddComponent<MeshRenderer>();
            circle.material = new Material(Shader.Find("Standard"))
            {
                color = new Color(0, 1, 1, 0.5f),
                transparent = true
            };
            var filter = teleportIndicator.AddComponent<MeshFilter>();
            filter.mesh = new Mesh();
            teleportIndicator.SetActive(false);
        }

        /// <summary>
        /// Toggle VR mode.
        /// </summary>
        public void ToggleVR()
        {
            if (enableVR)
            {
                DisableVR();
            }
            else
            {
                InitializeVR();
            }
        }

        /// <summary>
        /// Disable VR mode.
        /// </summary>
        public void DisableVR()
        {
            enableVR = false;
            
            #if UNITY_XR_QUEST_SUPPORT || UNITY_XR_OPENXR_SUPPORT
            UnityEngine.XR.XRSettings.enabled = false;
            #endif

            if (vrRig != null)
            {
                GameObject.Destroy(vrRig);
                vrRig = null;
            }

            Debug.Log("[VRManager] VR disabled");
        }

        /// <summary>
        /// Update VR input and teleportation.
        /// </summary>
        void Update()
        {
            if (!enableVR) return;

            UpdateControllers();
            UpdateTeleportation();
            UpdateHandTracking();
        }

        /// <summary>
        /// Update controller input.
        /// </summary>
        private void UpdateControllers()
        {
            if (rightController == null) return;

            // Teleportation with right controller
            if (Input.GetKey(KeyCode.JoystickButton7)) // Trigger
            {
                if (!isTeleporting && teleportTimer <= 0)
                {
                    StartTeleport();
                }
            }
            else
            {
                if (isTeleporting)
                {
                    CompleteTeleport();
                }
            }

            // Menu with left controller
            if (Input.GetKey(KeyCode.JoystickButton6))
            {
                var menu = MenuManager.Instance;
                if (menu != null)
                {
                    menu.PauseGame();
                }
            }
        }

        /// <summary>
        /// Update teleportation.
        /// </summary>
        private void UpdateTeleportation()
        {
            if (teleportTimer > 0)
            {
                teleportTimer -= Time.deltaTime;
            }

            if (isTeleporting && teleportIndicator != null)
            {
                teleportIndicator.SetActive(true);
                
                // Cast ray from right controller
                if (rightController != null)
                {
                    teleportRay.origin = rightController.transform.position;
                    teleportRay.direction = rightController.transform.forward;

                    if (Physics.Raycast(teleportRay, out RaycastHit hit, 20f))
                    {
                        teleportIndicator.transform.position = new Vector3(hit.point.x, hit.point.y, hit.point.z);
                        teleportIndicator.transform.rotation = Quaternion.LookRotation(rightController.transform.forward);
                    }
                }
            }
            else if (teleportIndicator != null)
            {
                teleportIndicator.SetActive(false);
            }
        }

        /// <summary>
        /// Start teleportation.
        /// </summary>
        private void StartTeleport()
        {
            isTeleporting = true;
            Debug.Log("[VRManager] Teleport started");
        }

        /// <summary>
        /// Complete teleportation.
        /// </summary>
        private void CompleteTeleport()
        {
            isTeleporting = false;
            teleportTimer = teleportCooldown;

            if (teleportIndicator != null && teleportIndicator.activeSelf)
            {
                if (vrRig != null)
                {
                    vrRig.transform.position = new Vector3(
                        teleportIndicator.transform.position.x,
                        vrRig.transform.position.y,
                        teleportIndicator.transform.position.z
                    );
                }
                Debug.Log($"[VRManager] Teleported to {teleportIndicator.transform.position}");
            }
        }

        /// <summary>
        /// Update hand tracking.
        /// </summary>
        private void UpdateHandTracking()
        {
            if (!enableHandTracking) return;

            // Hand tracking would use XR input system
            // This is a placeholder for actual hand tracking implementation
            leftHandTracking = false;
            rightHandTracking = false;
        }

        /// <summary>
        /// Get left hand joint positions.
        /// </summary>
        public Vector3[] GetLeftHandJoints()
        {
            return leftHandJoints;
        }

        /// <summary>
        /// Get right hand joint positions.
        /// </summary>
        public Vector3[] GetRightHandJoints()
        {
            return rightHandJoints;
        }

        /// <summary>
        /// Enable spatial audio.
        /// </summary>
        public void EnableSpatialAudio(bool enabled)
        {
            enableSpatialAudio = enabled;
            
            if (vrCamera != null)
            {
                var audioListener = vrCamera.GetComponent<AudioListener>();
                if (audioListener == null && enabled)
                {
                    audioListener = vrCamera.AddComponent<AudioListener>();
                }
                else if (audioListener != null && !enabled)
                {
                    GameObject.Destroy(audioListener);
                }
            }
        }

        /// <summary>
        /// Get VR camera.
        /// </summary>
        public Camera GetVRCamera()
        {
            return vrCamera;
        }

        /// <summary>
        /// Get left controller transform.
        /// </summary>
        public Transform GetLeftController()
        {
            return leftController?.transform;
        }

        /// <summary>
        /// Get right controller transform.
        /// </summary>
        public Transform GetRightController()
        {
            return rightController?.transform;
        }

        /// <summary>
        /// Clean up VR resources.
        /// </summary>
        public void Cleanup()
        {
            DisableVR();
        }
    }
}
