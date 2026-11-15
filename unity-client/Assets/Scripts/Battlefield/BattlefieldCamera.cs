using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Orbit camera controller for the battlefield viewer.
    /// <para>
    /// Supports mouse drag to orbit, scroll to zoom, and right-click drag to pan.
    /// Uses smooth damping for fluid camera movement.
    /// </para>
    /// </summary>
    public class BattlefieldCamera : MonoBehaviour
    {
        [Header("Camera Settings")]
        [SerializeField] private float orbitSpeed = 5f;
        [SerializeField] private float zoomSpeed = 10f;
        [SerializeField] private float panSpeed = 5f;
        [SerializeField] private float dampingSpeed = 10f;

        [Header("Limits")]
        [SerializeField] private float minDistance = 10f;
        [SerializeField] private float maxDistance = 1000f;
        [SerializeField] private float minVerticalAngle = -85f;
        [SerializeField] private float maxVerticalAngle = 85f;

        private Vector3 target;
        private Vector3 desiredPosition;
        private float distance;
        private float xAngle = 0f;
        private float yAngle = 45f;

        private bool isOrbiting;
        private bool isPanning;
        private Vector3 lastMousePosition;

        public Vector3 Target => target;

        void Start()
        {
            // Initialize camera position
            distance = Vector3.Distance(transform.position, target);
            UpdateDesiredPosition();
        }

        void Update()
        {
            HandleInput();
            SmoothMove();
        }

        /// <summary>
        /// Sets the camera target point.
        /// </summary>
        public void SetTarget(Vector3 newTarget)
        {
            target = newTarget;
            UpdateDesiredPosition();
        }

        /// <summary>
        /// Sets the camera target and distance.
        /// </summary>
        public void SetTarget(Vector3 newTarget, float newDistance)
        {
            target = newTarget;
            distance = Mathf.Clamp(newDistance, minDistance, maxDistance);
            UpdateDesiredPosition();
        }

        /// <summary>
        /// Handles mouse input for orbit, zoom, and pan.
        /// </summary>
        private void HandleInput()
        {
            // Left mouse button: orbit
            if (Input.GetMouseButton(0))
            {
                if (!isOrbiting)
                {
                    isOrbiting = true;
                    lastMousePosition = Input.mousePosition;
                }

                Vector3 delta = Input.mousePosition - lastMousePosition;
                lastMousePosition = Input.mousePosition;

                xAngle -= delta.x * orbitSpeed * 0.1f;
                yAngle -= delta.y * orbitSpeed * 0.1f;

                // Clamp vertical angle
                yAngle = Mathf.Clamp(yAngle, minVerticalAngle, maxVerticalAngle);

                UpdateDesiredPosition();
            }
            else if (Input.GetMouseButtonUp(0))
            {
                isOrbiting = false;
            }

            // Right mouse button: pan
            if (Input.GetMouseButton(1))
            {
                if (!isPanning)
                {
                    isPanning = true;
                    lastMousePosition = Input.mousePosition;
                }

                Vector3 delta = Input.mousePosition - lastMousePosition;
                lastMousePosition = Input.mousePosition;

                // Pan perpendicular to camera view
                Vector3 right = Vector3.Cross(transform.up, transform.forward).normalized;
                Vector3 up = transform.up;

                Vector3 panDelta = (right * delta.x + up * delta.y) * panSpeed * 0.1f;
                target -= panDelta;
                UpdateDesiredPosition();
            }
            else if (Input.GetMouseButtonUp(1))
            {
                isPanning = false;
            }

            // Scroll wheel: zoom
            float scroll = Input.mouseScrollDelta.y;
            if (scroll != 0f)
            {
                distance -= scroll * zoomSpeed;
                distance = Mathf.Clamp(distance, minDistance, maxDistance);
                UpdateDesiredPosition();
            }
        }

        /// <summary>
        /// Calculates the desired camera position based on angles and distance.
        /// </summary>
        private void UpdateDesiredPosition()
        {
            float xRad = xAngle * Mathf.Deg2Rad;
            float yRad = yAngle * Mathf.Deg2Rad;

            desiredPosition = new Vector3(
                target.x + distance * Mathf.Sin(xRad) * Mathf.Cos(yRad),
                target.y + distance * Mathf.Sin(yRad),
                target.z + distance * Mathf.Cos(xRad) * Mathf.Cos(yRad)
            );
        }

        /// <summary>
        /// Smoothly moves the camera toward the desired position.
        /// </summary>
        private void SmoothMove()
        {
            transform.position = Vector3.Lerp(transform.position, desiredPosition, dampingSpeed * Time.deltaTime);
            transform.LookAt(target);
        }

        /// <summary>
        /// Resets the camera to default position.
        /// </summary>
        public void ResetCamera()
        {
            xAngle = 0f;
            yAngle = 45f;
            distance = Mathf.Clamp(distance, minDistance, maxDistance);
            UpdateDesiredPosition();
        }
    }
}
