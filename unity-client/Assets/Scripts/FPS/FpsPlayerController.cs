using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// First-person player controller with walk, run, jump, and crouch.
    /// Uses Unity's CharacterController for physics-based movement.
    /// </summary>
    [RequireComponent(typeof(CharacterController))]
    public class FpsPlayerController : MonoBehaviour
    {
        [Header("Movement Settings")]
        [SerializeField] private float walkSpeed = 5f;
        [SerializeField] private float runSpeed = 9f;
        [SerializeField] private float crouchSpeed = 2.5f;
        [SerializeField] private float jumpForce = 8f;
        [SerializeField] private float gravity = -20f;

        [Header("Mouse Settings")]
        [SerializeField] private float mouseSensitivity = 2f;
        [SerializeField] private float lookClamp = 80f;

        [Header("Crouch Settings")]
        [SerializeField] private float standingHeight = 1.7f;
        [SerializeField] private float crouchingHeight = 0.9f;
        [SerializeField] private float crouchTransitionSpeed = 10f;

        private CharacterController characterController;
        private Camera playerCamera;
        private Vector3 velocity;
        private Vector2 mouseInput;
        private float currentRotationY = 0f;
        private float currentRotationX = 0f;

        private bool isGrounded;
        private bool isRunning;
        private bool isCrouching;
        private float currentHeight;
        private float targetHeight;

        // Events
        public event System.Action<bool> OnCrouchChanged;
        public event System.Action<bool> OnSprintChanged;

        public bool IsGrounded => isGrounded;
        public bool IsRunning => isRunning;
        public bool IsCrouching => isCrouching;
        public Vector3 Velocity => velocity;

        void Awake()
        {
            characterController = GetComponent<CharacterController>();
            playerCamera = GetComponentInChildren<Camera>();
            currentHeight = standingHeight;
            targetHeight = standingHeight;
        }

        void Start()
        {
            // Lock cursor to center
            Cursor.lockState = CursorLockMode.Locked;
            Cursor.visible = false;
        }

        void Update()
        {
            HandleInput();
            HandleMouseLook();
            HandleMovement();
            HandleCrouch();
        }

        private void HandleInput()
        {
            // Sprint toggle
            if (Input.GetKeyDown(KeyCode.LeftShift))
            {
                isRunning = !isRunning;
                OnSprintChanged?.Invoke(isRunning);
            }

            // Crouch toggle
            if (Input.GetKeyDown(KeyCode.C))
            {
                isCrouching = !isCrouching;
                targetHeight = isCrouching ? crouchingHeight : standingHeight;
                OnCrouchChanged?.Invoke(isCrouching);
            }

            // Jump
            if (Input.GetButtonDown("Jump") && isGrounded)
            {
                velocity.y = Mathf.Sqrt(-2f * gravity * jumpForce);
            }
        }

        private void HandleMouseLook()
        {
            float mouseX = Input.GetAxis("Mouse X") * mouseSensitivity;
            float mouseY = Input.GetAxis("Mouse Y") * mouseSensitivity;

            currentRotationY += mouseX;
            currentRotationX -= mouseY;
            currentRotationX = Mathf.Clamp(currentRotationX, -lookClamp, lookClamp);

            transform.eulerAngles = new Vector3(0f, currentRotationY, 0f);

            if (playerCamera != null)
            {
                playerCamera.transform.localRotation = Quaternion.Euler(currentRotationX, 0f, 0f);
            }
        }

        private void HandleMovement()
        {
            float moveX = Input.GetAxis("Horizontal");
            float moveZ = Input.GetAxis("Vertical");

            // Determine current speed
            float speed = walkSpeed;
            if (isCrouching)
                speed = crouchSpeed;
            else if (isRunning)
                speed = runSpeed;

            // Calculate movement direction relative to player
            Vector3 moveDirection = new Vector3(moveX, 0f, moveZ);
            moveDirection = transform.TransformDirection(moveDirection).normalized * speed;

            // Apply gravity
            velocity.y += gravity * Time.deltaTime;

            // Move character
            if (characterController != null)
            {
                characterController.Move(moveDirection * Time.deltaTime + Vector3.up * velocity.y * Time.deltaTime);
            }

            // Check if grounded
            isGrounded = characterController.isGrounded;
            if (isGrounded && velocity.y < 0f)
            {
                velocity.y = -2f;
            }
        }

        private void HandleCrouch()
        {
            if (Mathf.Abs(currentHeight - targetHeight) > 0.01f)
            {
                currentHeight = Mathf.Lerp(currentHeight, targetHeight, crouchTransitionSpeed * Time.deltaTime);
                characterController.height = currentHeight;

                // Adjust center to keep feet on ground
                characterController.center = new Vector3(0f, currentHeight / 2f - 0.5f, 0f);
            }
        }

        /// <summary>
        /// Get the current aim direction for shooting.
        /// </summary>
        public Vector3 GetAimDirection()
        {
            if (playerCamera != null)
                return playerCamera.transform.forward;
            return transform.forward;
        }

        /// <summary>
        /// Get the camera position for raycast origin.
        /// </summary>
        public Vector3 GetCameraPosition()
        {
            if (playerCamera != null)
                return playerCamera.transform.position;
            return transform.position;
        }

        /// <summary>
        /// Apply server-reconciled position from network state.
        /// </summary>
        public void SetPosition(Vector3 position)
        {
            transform.position = position;
        }

        /// <summary>
        /// Apply server-reconciled rotation from network state.
        /// </summary>
        public void SetRotation(Vector3 eulerAngles)
        {
            transform.eulerAngles = new Vector3(0f, eulerAngles.y, 0f);
            currentRotationY = eulerAngles.y;
        }
    }
}
