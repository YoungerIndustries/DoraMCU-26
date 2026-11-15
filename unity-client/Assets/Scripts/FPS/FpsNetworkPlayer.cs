using System;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Represents a remote player in the FPS game.
    /// Handles position interpolation, health display, and hit effects.
    /// </summary>
    public class FpsNetworkPlayer : MonoBehaviour
    {
        [Header("Player Model")]
        [SerializeField] private GameObject playerModel;
        [SerializeField] private Transform headBone;
        [SerializeField] private Transform weaponBone;

        [Header("Interpolation")]
        [SerializeField] private float interpolationSpeed = 10f;
        [SerializeField] private float rotationSpeed = 15f;

        [Header("Health Bar")]
        [SerializeField] private Transform healthBarWorld;
        [SerializeField] private Renderer healthBarFill;
        [SerializeField] private Color healthyColor = Color.green;
        [SerializeField] private Color criticalColor = Color.red;

        [Header("Name Tag")]
        [SerializeField] private Transform nameTagCanvas;
        [SerializeField] private UnityEngine.UI.Text nameTagText;

        [Header("Hit Effects")]
        [SerializeField] private ParticleSystem hitEffect;
        [SerializeField] private float hitFlashDuration = 0.1f;
        [SerializeField] private Material hitFlashMaterial;

        private string playerId;
        private string playerName;
        private Vector3 targetPosition;
        private Vector3 targetRotation;
        private int targetHealth;
        private bool isAlive = true;

        private Material originalMaterial;
        private Coroutine hitFlashCoroutine;

        public string PlayerId => playerId;
        public string PlayerName => playerName;
        public bool IsAlive => isAlive;

        /// <summary>
        /// Initialize this network player with server data.
        /// </summary>
        public void Initialize(string id, string name, Vector3 position, Vector3 rotation, int health)
        {
            playerId = id;
            playerName = name;
            targetPosition = position;
            targetRotation = rotation;
            targetHealth = health;
            isAlive = health > 0;

            transform.position = position;
            transform.eulerAngles = new Vector3(0f, rotation.y, 0f);

            // Update name tag
            if (nameTagText != null)
            {
                nameTagText.text = name;
                nameTagCanvas.gameObject.SetActive(true);
            }

            // Show/hide model based on alive state
            if (playerModel != null)
            {
                playerModel.SetActive(isAlive);
            }

            UpdateHealthBar(health);
        }

        void Update()
        {
            // Interpolate position
            transform.position = Vector3.Lerp(transform.position, targetPosition, interpolationSpeed * Time.deltaTime);

            // Interpolate rotation
            Quaternion targetQuat = Quaternion.Euler(0f, targetRotation.y, 0f);
            transform.rotation = Quaternion.Slerp(transform.rotation, targetQuat, rotationSpeed * Time.deltaTime);

            // Make health bar face camera
            if (healthBarWorld != null)
            {
                healthBarWorld.LookAt(Camera.main.transform);
            }

            // Make name tag face camera
            if (nameTagCanvas != null)
            {
                nameTagCanvas.LookAt(Camera.main.transform);
            }
        }

        /// <summary>
        /// Update from server state.
        /// </summary>
        public void UpdateState(PlayerStateData state)
        {
            if (state.Position != null)
            {
                targetPosition = state.Position.ToVector3();
            }

            if (state.Rotation != null)
            {
                targetRotation = state.Rotation.ToVector3();
            }

            targetHealth = state.Health;
            isAlive = state.Alive;

            UpdateHealthBar(state.Health);

            if (playerModel != null)
            {
                playerModel.SetActive(isAlive);
            }
        }

        private void UpdateHealthBar(int health)
        {
            if (healthBarFill != null)
            {
                float healthPercent = Mathf.Clamp01(health / 100f);
                healthBarFill.transform.localScale = new Vector3(healthPercent, 1f, 1f);
                healthBarFill.material.color = Color.Lerp(criticalColor, healthyColor, healthPercent);
            }
        }

        /// <summary>
        /// Show hit effect when this player is hit.
        /// </summary>
        public void ShowHitEffect(Vector3 hitPoint)
        {
            // Play hit particle effect
            if (hitEffect != null)
            {
                var effect = Instantiate(hitEffect, hitPoint, Quaternion.identity);
                Destroy(effect.gameObject, 1f);
            }

            // Flash material
            if (hitFlashCoroutine != null)
                StopCoroutine(hitFlashCoroutine);

            hitFlashCoroutine = StartCoroutine(HitFlashRoutine());
        }

        private System.Collections.IEnumerator HitFlashRoutine()
        {
            if (playerModel == null) yield break;

            var renderer = playerModel.GetComponentInChildren<Renderer>();
            if (renderer == null) yield break;

            originalMaterial = renderer.material;
            renderer.material = hitFlashMaterial;

            yield return new WaitForSeconds(hitFlashDuration);

            renderer.material = originalMaterial;
            hitFlashCoroutine = null;
        }

        /// <summary>
        /// Show death effect.
        /// </summary>
        public void ShowDeathEffect()
        {
            isAlive = false;

            if (playerModel != null)
            {
                // Animate death (simple fade out)
                var renderer = playerModel.GetComponentInChildren<Renderer>();
                if (renderer != null)
                {
                    var fadeCoroutine = StartCoroutine(FadeOutRoutine(renderer));
                }
            }
        }

        private System.Collections.IEnumerator FadeOutRoutine(Renderer renderer)
        {
            float duration = 1f;
            float elapsed = 0f;
            Color originalColor = renderer.material.color;

            while (elapsed < duration)
            {
                elapsed += Time.deltaTime;
                renderer.material.color = new Color(
                    originalColor.r, originalColor.g, originalColor.b,
                    1f - (elapsed / duration)
                );
                yield return null;
            }

            if (playerModel != null)
                playerModel.SetActive(false);
        }

        /// <summary>
        /// Respawn this player.
        /// </summary>
        public void Respawn(Vector3 position)
        {
            isAlive = true;
            targetPosition = position;
            targetHealth = 100;

            if (playerModel != null)
            {
                playerModel.SetActive(true);
                var renderer = playerModel.GetComponentInChildren<Renderer>();
                if (renderer != null)
                    renderer.material.color = Color.white;
            }

            UpdateHealthBar(100);
        }

        /// <summary>
        /// Get the player's head position for hit detection.
        /// </summary>
        public Vector3 GetHeadPosition()
        {
            if (headBone != null)
                return headBone.position;
            return transform.position + Vector3.up * 1.6f;
        }

        /// <summary>
        /// Get the player's body bounds for hit detection.
        /// </summary>
        public Bounds GetBodyBounds()
        {
            return new Bounds(transform.position + Vector3.up * 0.8f, new Vector3(0.8f, 1.8f, 0.8f));
        }
    }
}
