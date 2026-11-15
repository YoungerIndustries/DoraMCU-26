using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Client-side prediction and server reconciliation.
    /// Predicts player movement locally for responsive feel, then corrects
    /// when server state arrives.
    /// </summary>
    public class FpsPlayerPrediction : MonoBehaviour
    {
        [Header("Prediction Settings")]
        [SerializeField] private float correctionSpeed = 15f;
        [SerializeField] private float maxCorrectionDistance = 0.5f;
        [SerializeField] private bool enablePrediction = true;

        private FpsPlayerController playerController;
        private Dictionary<int, PredictedState> inputHistory = new();
        private Dictionary<int, ServerState> serverHistory = new();
        private int lastConfirmedTick = -1;

        private Vector3 predictedPosition;
        private Vector3 predictedVelocity;
        private float predictedRotationY;

        private Vector3 lastServerPosition;
        private Vector3 lastServerRotation;

        [System.Serializable]
        private class PredictedState
        {
            public Vector3 position;
            public Vector3 velocity;
            public float rotationY;
            public long timestamp;
        }

        [System.Serializable]
        private class ServerState
        {
            public Vector3 position;
            public Vector3 rotation;
            public int tick;
        }

        void Awake()
        {
            playerController = GetComponentInChildren<FpsPlayerController>();
            if (playerController == null)
            {
                playerController = GetComponentInParent<FpsPlayerController>();
            }

            predictedPosition = transform.position;
            predictedRotationY = transform.eulerAngles.y;
        }

        void Start()
        {
            if (FpsNetworkManager.Instance != null)
            {
                FpsNetworkManager.Instance.OnGameStateReceived += OnServerStateReceived;
            }
        }

        void OnDestroy()
        {
            if (FpsNetworkManager.Instance != null)
            {
                FpsNetworkManager.Instance.OnGameStateReceived -= OnServerStateReceived;
            }
        }

        void Update()
        {
            if (!enablePrediction || playerController == null) return;

            // Apply predicted movement
            ApplyPredictedMovement();

            // Clean up old history
            CleanupHistory();
        }

        private void ApplyPredictedMovement()
        {
            // Get input
            float moveX = Input.GetAxis("Horizontal");
            float moveZ = Input.GetAxis("Vertical");

            float speed = playerController.IsRunning ? 9f : 5f;
            if (playerController.IsCrouching) speed = 2.5f;

            Vector3 moveDir = new Vector3(moveX, 0f, moveZ);
            moveDir = transform.TransformDirection(moveDir).normalized * speed;

            // Predict velocity
            predictedVelocity = moveDir;

            // Predict position
            predictedPosition += predictedVelocity * Time.deltaTime;

            // Apply gravity
            if (!playerController.IsGrounded)
            {
                predictedVelocity.y -= 20f * Time.deltaTime;
            }

            // Predict rotation from mouse
            float mouseX = Input.GetAxis("Mouse X") * 2f;
            predictedRotationY += mouseX;

            // Apply predicted state
            if (lastConfirmedTick < 0)
            {
                // No server state yet, use prediction directly
                transform.position = predictedPosition;
                transform.eulerAngles = new Vector3(0f, predictedRotationY, 0f);
            }
        }

        private void OnServerStateReceived(FpsGameStateMessage state)
        {
            string localId = FpsNetworkManager.Instance.PlayerId;
            if (string.IsNullOrEmpty(localId)) return;

            // Find local player in server state
            PlayerStateData localState = null;
            foreach (var ps in state.Players)
            {
                if (ps.Id == localId)
                {
                    localState = ps;
                    break;
                }
            }

            if (localState == null || localState.Position == null) return;

            // Store server state
            var serverSt = new ServerState
            {
                position = localState.Position.ToVector3(),
                rotation = localState.Rotation?.ToVector3() ?? Vector3.zero,
                tick = state.Tick
            };
            serverHistory[state.Tick] = serverSt;
            lastConfirmedTick = state.Tick;

            // Reconcile: compare predicted position with server position
            Reconcile(serverSt);
        }

        private void Reconcile(ServerState serverState)
        {
            Vector3 serverPos = serverState.position;
            Vector3 correction = serverPos - predictedPosition;

            // Only correct if the difference is significant
            if (correction.magnitude > maxCorrectionDistance)
            {
                // Smoothly correct position
                predictedPosition = serverPos;
                lastServerPosition = serverPos;
                lastServerRotation = serverState.rotation;

                // Apply correction immediately but smoothly
                transform.position = Vector3.Lerp(transform.position, serverPos, correctionSpeed * Time.deltaTime);
            }

            // Correct rotation
            float serverYaw = serverState.rotation.y;
            float yawDiff = serverYaw - predictedRotationY;

            // Handle angle wrapping
            if (yawDiff > 180f) yawDiff -= 360f;
            if (yawDiff < -180f) yawDiff += 360f;

            if (Mathf.Abs(yawDiff) > 1f)
            {
                predictedRotationY = serverYaw;
            }
        }

        private void CleanupHistory()
        {
            // Keep only last 20 entries
            while (inputHistory.Count > 20)
            {
                int minTick = int.MaxValue;
                foreach (var tick in inputHistory.Keys)
                {
                    if (tick < minTick) minTick = tick;
                }
                if (minTick != int.MaxValue)
                    inputHistory.Remove(minTick);
            }

            while (serverHistory.Count > 20)
            {
                int minTick = int.MaxValue;
                foreach (var tick in serverHistory.Keys)
                {
                    if (tick < minTick) minTick = tick;
                }
                if (minTick != int.MaxValue)
                    serverHistory.Remove(minTick);
            }
        }

        /// <summary>
        /// Get the predicted position for network input.
        /// </summary>
        public Vector3 GetPredictedPosition()
        {
            return predictedPosition;
        }

        /// <summary>
        /// Get the predicted rotation for network input.
        /// </summary>
        public float GetPredictedRotation()
        {
            return predictedRotationY;
        }

        /// <summary>
        /// Reset prediction state (e.g., after respawn).
        /// </summary>
        public void ResetPrediction(Vector3 position, Vector3 rotation)
        {
            predictedPosition = position;
            predictedVelocity = Vector3.zero;
            predictedRotationY = rotation.y;
            lastConfirmedTick = -1;
            inputHistory.Clear();
            serverHistory.Clear();
        }
    }
}
