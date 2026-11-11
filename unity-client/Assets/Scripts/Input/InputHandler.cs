using UnityEngine;

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
            float horizontal = Input.GetAxis("Horizontal");
            float vertical = Input.GetAxis("Vertical");
            float roll = Input.GetAxis("Mouse X") * 0.1f;

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
            float mouseX = Input.GetAxis("Mouse X") * mouseSensitivity;
            float mouseY = Input.GetAxis("Mouse Y") * mouseSensitivity;

            currentYaw += mouseX;
            currentPitch -= mouseY;
            currentPitch = Mathf.Clamp(currentPitch, minPitch, maxPitch);

            OnRotate?.Invoke(currentYaw);
        }

        void HandleActions()
        {
            // Fire weapons
            if (Input.GetMouseButton(0))
            {
                OnFire?.Invoke();
            }

            // Scan
            if (Input.GetKeyDown(KeyCode.Q))
            {
                OnScan?.Invoke();
            }

            // Interact
            if (Input.GetKeyDown(KeyCode.E))
            {
                OnInteract?.Invoke();
            }

            // Toggle cursor
            if (Input.GetKeyDown(KeyCode.Escape))
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
                if (Input.GetKeyDown((KeyCode)(KeyCode.Alpha1 + (i - 1))))
                {
                    OnNavigateToSystem?.Invoke(0, 0, 0, i);
                }
            }

            // Controller support
            if (Input.GetButtonDown("Fire1"))
            {
                OnFire?.Invoke();
            }

            if (Input.GetButtonDown("Submit"))
            {
                OnInteract?.Invoke();
            }

            if (Input.GetButtonDown("Start"))
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

    // Action delegate
    public delegate void Action<T>(T obj);
    public delegate void Action();
}
