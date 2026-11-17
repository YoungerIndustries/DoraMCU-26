using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Tooltip UI for displaying AI admin details when clicked.
    /// Shows name, role, galaxy, current activity, workload, and position.
    /// </summary>
    public class AiAdminTooltip : MonoBehaviour
    {
        [Header("UI References")]
        [SerializeField] private GameObject tooltipPanel;
        [SerializeField] private Text nameText;
        [SerializeField] private Text roleText;
        [SerializeField] private Text galaxyText;
        [SerializeField] private Text statusText;
        [SerializeField] private Text workloadText;
        [SerializeField] private Text positionText;

        private AiAdminData currentAdmin;
        private bool isVisible = false;

        /// <summary>
        /// Show the tooltip with AI admin details.
        /// </summary>
        public void Show(AiAdminData admin, Vector3 worldPosition)
        {
            if (admin == null || tooltipPanel == null) return;

            currentAdmin = admin;

            // Set text content
            if (nameText != null) nameText.text = admin.name;
            if (roleText != null) roleText.text = $"Role: {admin.role}";
            if (galaxyText != null) galaxyText.text = $"Galaxy: {admin.galaxyName}";
            if (statusText != null) statusText.text = admin.IsBusy ? $"Busy (Order: {admin.currentOrderId})" : "Available";
            if (workloadText != null) workloadText.text = $"Active Orders: {admin.activeOrderCount}/3";
            if (positionText != null) positionText.text = $"Position: ({admin.posX:F1}, {admin.posY:F1}, {admin.posZ:F1})";

            // Position the tooltip at the screen position of the marker
            Vector3 screenPos = Camera.main.WorldToScreenPoint(worldPosition);
            screenPos.y += 50f; // Offset above the marker
            tooltipPanel.transform.position = screenPos;

            tooltipPanel.SetActive(true);
            isVisible = true;
        }

        /// <summary>
        /// Hide the tooltip.
        /// </summary>
        public void Hide()
        {
            if (tooltipPanel != null)
            {
                tooltipPanel.SetActive(false);
            }
            isVisible = false;
            currentAdmin = null;
        }

        /// <summary>
        /// Toggle the tooltip visibility.
        /// </summary>
        public void Toggle()
        {
            if (isVisible)
            {
                Hide();
            }
            else if (currentAdmin != null)
            {
                Show(currentAdmin, Vector3.zero);
            }
        }

        /// <summary>
        /// Check if the tooltip is currently visible.
        /// </summary>
        public bool IsVisible => isVisible;

        /// <summary>
        /// Get the current admin data being displayed.
        /// </summary>
        public AiAdminData CurrentAdmin => currentAdmin;
    }
}
