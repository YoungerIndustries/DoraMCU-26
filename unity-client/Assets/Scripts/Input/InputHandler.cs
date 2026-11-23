using System;
using UnityEngine;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.Input
{
    /// <summary>
    /// Handles player input for the Unity native client.
    /// Supports keyboard, mouse, and game controller input.
    /// </summary>
    public class InputHandler : Singleton<InputHandler>
    {
        [Header("Movement Settings")]
        [SerializeField] private float moveSpeed = 10f;
        [SerializeField] private float rotationSpeed = 180f;
        [SerializeField] private float thrustMultiplier = 1.5f;

        [Header("Camera Settings")]
        [SerializeField] private float mouseSensitivity = 2f;
        [SerializeField] private float minPitch = -89f;
        [SerializeField] private float maxPitch = 89f;

        private float currentPitch;
        private float currentYaw;
        private bool isThrusting;
        private bool isBraking;

        // Input events
        public event Action<Vector3> OnMove;
        public event Action<float> OnRotate;
        public event Action OnThrust;
        public event Action OnBrake;
        public event Action OnFire;
        public event Action OnScan;
        public event Action OnInteract;
        public event Action<int, int, int, int> OnNavigateToSystem;
        public event Action<long, string, string> OnNavigateToBattlefield;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
            Cursor.lockState = CursorLockMode.Locked;
            Cursor.visible = false;
        }

        void Update()
        {
            HandleMovement();
            HandleRotation();
            HandleActions();
        }

        void HandleMovement()
        {
            float horizontal = UnityEngine.Input.GetAxis("Horizontal");
            float vertical = UnityEngine.Input.GetAxis("Vertical");
            float roll = UnityEngine.Input.GetAxis("Mouse X") * 0.1f;

            Vector3 move = new Vector3(horizontal, roll, vertical).normalized;

            if (move.magnitude > 0.1f)
            {
                isThrusting = vertical > 0.1f;
                isBraking = vertical < -0.1f;

                float speed = moveSpeed * (isThrusting ? thrustMultiplier : 1f);
                OnMove?.Invoke(move * speed);
            }
            else
            {
                isThrusting = false;
                isBraking = false;
            }
        }

        void HandleRotation()
        {
            // Mouse look
            float mouseX = UnityEngine.Input.GetAxis("Mouse X") * mouseSensitivity;
            float mouseY = UnityEngine.Input.GetAxis("Mouse Y") * mouseSensitivity;

            currentYaw += mouseX;
            currentPitch -= mouseY;
            currentPitch = Mathf.Clamp(currentPitch, minPitch, maxPitch);

            OnRotate?.Invoke(currentYaw);
        }

        void HandleActions()
        {
            // Fire weapons
            if (UnityEngine.Input.GetMouseButton(0))
            {
                OnFire?.Invoke();
            }

            // Scan
            if (UnityEngine.Input.GetKeyDown(KeyCode.Q))
            {
                OnScan?.Invoke();
            }

            // Interact
            if (UnityEngine.Input.GetKeyDown(KeyCode.E))
            {
                OnInteract?.Invoke();
            }

            // Toggle cursor
            if (UnityEngine.Input.GetKeyDown(KeyCode.Escape))
            {
                if (Cursor.lockState == CursorLockMode.Locked)
                {
                    Cursor.lockState = CursorLockMode.None;
                    Cursor.visible = true;
                }
                else
                {
                    Cursor.lockState = CursorLockMode.Locked;
                    Cursor.visible = false;
                }
            }

            // Navigate to system (number keys)
            for (int i = 1; i <= 9; i++)
            {
                if (UnityEngine.Input.GetKeyDown((KeyCode)(KeyCode.Alpha1 + (i - 1))))
                {
                    OnNavigateToSystem?.Invoke(0, 0, 0, i);
                }
            }

            // Enter battlefield mode (B key)
            if (UnityEngine.Input.GetKeyDown(KeyCode.B))
            {
                OnNavigateToBattlefield?.Invoke(1, "WWII", "URBAN");
            }

            // Controller support
            if (UnityEngine.Input.GetButtonDown("Fire1"))
            {
                OnFire?.Invoke();
            }

            if (UnityEngine.Input.GetButtonDown("Submit"))
            {
                OnInteract?.Invoke();
            }

            if (UnityEngine.Input.GetButtonDown("Start"))
            {
                OnScan?.Invoke();
            }
        }

        /// <summary>
        /// Navigate to a specific solar system.
        /// </summary>
        public void NavigateToSystem(int sectorRow, int sectorCol, int subSectorRow, int subSectorCol)
        {
            OnNavigateToSystem?.Invoke(sectorRow, sectorCol, subSectorRow, subSectorCol);
        }

        public bool IsThrusting => isThrusting;
        public bool IsBraking => isBraking;
    }

}
