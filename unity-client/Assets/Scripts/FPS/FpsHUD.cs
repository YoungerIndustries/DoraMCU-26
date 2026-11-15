using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// HUD component displaying health, ammo, crosshair, kill feed, and minimap.
    /// </summary>
    public class FpsHUD : MonoBehaviour
    {
        [Header("Health UI")]
        [SerializeField] private Image healthBar;
        [SerializeField] private Text healthText;
        [SerializeField] private Color healthyColor = Color.green;
        [SerializeField] private Color criticalColor = Color.red;

        [Header("Ammo UI")]
        [SerializeField] private Text ammoText;
        [SerializeField] private Text weaponNameText;

        [Header("Crosshair")]
        [SerializeField] private Image crosshair;
        [SerializeField] private float crosshairSpread = 5f;

        [Header("Kill Feed")]
        [SerializeField] private Transform killFeedParent;
        [SerializeField] private GameObject killFeedItemPrefab;
        [SerializeField] private int maxKillFeedItems = 5;
        [SerializeField] private float killFeedDisplayTime = 5f;

        [Header("Score UI")]
        [SerializeField] private Text scoreText;
        [SerializeField] private Text playerCountText;
        [SerializeField] private Text matchTimerText;

        [Header("CTP Team Scores")]
        [SerializeField] private Text team0ScoreText;
        [SerializeField] private Text team1ScoreText;
        [SerializeField] private Text ctpModeLabel;

        [Header("CTP Capture Points")]
        [SerializeField] private Transform capturePointParent;
        [SerializeField] private FpsCaptureOverlay captureOverlay;

        [Header("Reload Indicator")]
        [SerializeField] private Image reloadBar;
        [SerializeField] private Text reloadText;

        [Header("Damage Indicator")]
        [SerializeField] private Image damageOverlay;
        [SerializeField] private float damageFadeSpeed = 3f;

        [Header("Minimap")]
        [SerializeField] private RawImage minimap;
        [SerializeField] private Transform minimapCamera;

        private List<KillFeedItem> killFeedItems = new();
        private float damageAlpha;
        private bool isReloading;
        private float reloadProgress;

        protected void Start()
        {
            // Subscribe to game state events
            FpsGameState.Instance.OnLocalPlayerUpdated += OnLocalPlayerUpdated;
            FpsGameState.Instance.OnGameEvent += OnGameEvent;
            FpsGameState.Instance.OnMatchInfoUpdated += OnMatchInfoUpdated;

            // Subscribe to weapon events
            var weaponController = FindObjectOfType<FpsWeaponController>();
            if (weaponController != null)
            {
                weaponController.OnAmmoChanged += OnAmmoChanged;
                weaponController.OnWeaponChanged += OnWeaponChanged;
                weaponController.OnReloadStarted += OnReloadStarted;
            }

            // Initialize UI
            if (damageOverlay != null)
                damageOverlay.color = new Color(1f, 0f, 0f, 0f);

            if (reloadBar != null)
                reloadBar.gameObject.SetActive(false);

            // Initialize CTP UI
            if (team0ScoreText != null) team0ScoreText.text = "0";
            if (team1ScoreText != null) team1ScoreText.text = "0";

            UpdatePlayerCount();
        }

        void Update()
        {
            UpdateCrosshair();
            UpdateDamageOverlay();
            UpdateReloadIndicator();
            UpdateCTPOverlay();
        }

        private void UpdateCTPOverlay()
        {
            if (captureOverlay != null)
                captureOverlay.UpdateFromGameState();

            // Update team score texts directly
            var gs = FpsGameState.Instance;
            if (gs == null) return;

            if (team0ScoreText != null && gs.TeamScores.ContainsKey(0))
                team0ScoreText.text = gs.TeamScores[0].ToString();
            if (team1ScoreText != null && gs.TeamScores.ContainsKey(1))
                team1ScoreText.text = gs.TeamScores[1].ToString();

            // Show/hide CTP UI based on game mode
            bool isCtp = gs.CapturePoints != null && gs.CapturePoints.Count > 0;
            if (team0ScoreText != null) team0ScoreText.gameObject.SetActive(isCtp);
            if (team1ScoreText != null) team1ScoreText.gameObject.SetActive(isCtp);
            if (ctpModeLabel != null) ctpModeLabel.gameObject.SetActive(isCtp);
        }

        private void OnLocalPlayerUpdated(PlayerStateData player)
        {
            UpdateHealth(player.Health);
            UpdateScore(player.Score);
            UpdatePlayerCount();
        }

        private void UpdateHealth(int health)
        {
            if (healthBar != null)
            {
                float healthPercent = Mathf.Clamp01(health / 100f);
                healthBar.fillAmount = healthPercent;
                healthBar.color = Color.Lerp(criticalColor, healthyColor, healthPercent);
            }

            if (healthText != null)
            {
                healthText.text = health.ToString();
            }
        }

        private void OnAmmoChanged(int ammo, int reserve)
        {
            if (ammoText != null)
            {
                ammoText.text = $"{ammo} / {reserve}";
            }
        }

        private void OnWeaponChanged(string weaponName)
        {
            if (weaponNameText != null)
            {
                weaponNameText.text = weaponName;
            }
        }

        private void OnReloadStarted(bool reloading)
        {
            isReloading = reloading;
            reloadProgress = 0f;

            if (reloadBar != null)
                reloadBar.gameObject.SetActive(reloading);

            if (reloadText != null)
                reloadText.gameObject.SetActive(reloading);
        }

        private void UpdateReloadIndicator()
        {
            if (!isReloading) return;

            reloadProgress += Time.deltaTime;
            float maxTime = 2f; // Default reload time
            float progress = Mathf.Clamp01(reloadProgress / maxTime);

            if (reloadBar != null)
                reloadBar.fillAmount = progress;

            if (reloadText != null)
                reloadText.text = "RELOADING";

            if (progress >= 1f)
            {
                isReloading = false;
                if (reloadBar != null)
                    reloadBar.gameObject.SetActive(false);
                if (reloadText != null)
                    reloadText.gameObject.SetActive(false);
            }
        }

        private void UpdateCrosshair()
        {
            if (crosshair == null) return;

            // Adjust crosshair spread based on movement and aiming
            float spread = crosshairSpread;

            var playerController = FindObjectOfType<FpsPlayerController>();
            if (playerController != null)
            {
                if (playerController.IsRunning)
                    spread *= 1.5f;
                if (playerController.IsCrouching)
                    spread *= 0.7f;
            }

            var weaponController = FindObjectOfType<FpsWeaponController>();
            if (weaponController != null)
            {
                if (weaponController.IsAiming)
                    spread *= 0.5f;
                if (weaponController.IsReloading)
                    spread *= 2f;
            }

            crosshair.rectTransform.localScale = Vector3.one * spread;
        }

        private void UpdateDamageOverlay()
        {
            if (damageOverlay == null) return;

            if (damageAlpha > 0f)
            {
                damageAlpha -= damageFadeSpeed * Time.deltaTime;
                damageAlpha = Mathf.Max(0f, damageAlpha);
            }

            damageOverlay.color = new Color(1f, 0f, 0f, damageAlpha);
        }

        private void OnGameEvent(GameStateEvent evt)
        {
            switch (evt.Type)
            {
                case "HIT":
                    // Show damage indicator if local player was hit
                    if (evt.TargetId == FpsNetworkManager.Instance.PlayerId)
                    {
                        damageAlpha = 0.5f;
                    }
                    break;

                case "KILL":
                    AddKillFeedItem(evt.PlayerId, evt.TargetId);
                    break;

                case "DEATH":
                    if (evt.PlayerId == FpsNetworkManager.Instance.PlayerId)
                    {
                        ShowDeathScreen();
                    }
                    break;

                case "RESPAWN":
                    if (evt.PlayerId == FpsNetworkManager.Instance.PlayerId)
                    {
                        HideDeathScreen();
                    }
                    break;
            }
        }

        private void AddKillFeedItem(string killerId, string victimId)
        {
            if (killFeedItemPrefab == null || killFeedParent == null) return;

            // Get player names
            string killerName = FpsGameState.Instance.GetPlayer(killerId)?.Name ?? killerId;
            string victimName = FpsGameState.Instance.GetPlayer(victimId)?.Name ?? victimId;

            // Create kill feed item
            GameObject itemObj = Instantiate(killFeedItemPrefab, killFeedParent);
            var killItem = itemObj.GetComponent<KillFeedItem>();
            if (killItem == null)
                killItem = itemObj.AddComponent<KillFeedItem>();

            killItem.SetNames(killerName, victimName);
            killItemObj.transform.SetAsLastSibling();

            killFeedItems.Add(killItem);

            // Remove old items
            while (killFeedItems.Count > maxKillFeedItems)
            {
                var oldItem = killFeedItems[0];
                killFeedItems.RemoveAt(0);
                Destroy(oldItem.gameObject);
            }

            // Auto-remove after display time
            UnityEngine.Object.Destroy(itemObj, killFeedDisplayTime);
        }

        private GameObject killFeedItemObj; // For the last created item

        private void ShowDeathScreen()
        {
            // Show death/respawn UI
            if (damageOverlay != null)
            {
                damageOverlay.color = new Color(0.5f, 0f, 0f, 0.7f);
            }
        }

        private void HideDeathScreen()
        {
            if (damageOverlay != null)
            {
                damageOverlay.color = new Color(1f, 0f, 0f, 0f);
            }
        }

        private void UpdateScore(int score)
        {
            if (scoreText != null)
            {
                scoreText.text = $"Score: {score}";
            }
        }

        private void UpdatePlayerCount()
        {
            if (playerCountText != null)
            {
                int count = FpsGameState.Instance.PlayerCount;
                playerCountText.text = $"Players: {count}";
            }
        }

        private void OnMatchInfoUpdated(MatchInfoData info)
        {
            if (matchTimerText != null && info.TimeRemaining > 0)
            {
                int minutes = info.TimeRemaining / 60;
                int seconds = info.TimeRemaining % 60;
                matchTimerText.text = $"{minutes:D2}:{seconds:D2}";
            }
        }

        /// <summary>
        /// Show a damage flash effect.
        /// </summary>
        public void ShowDamageFlash()
        {
            damageAlpha = 0.5f;
        }
    }

    /// <summary>
    /// Kill feed item component.
    /// </summary>
    public class KillFeedItem : MonoBehaviour
    {
        [SerializeField] private Text killerText;
        [SerializeField] private Text victimText;
        [SerializeField] private Text iconText;

        public void SetNames(string killer, string victim)
        {
            if (killerText != null)
                killerText.text = killer;
            if (victimText != null)
                victimText.text = victim;
            if (iconText != null)
                iconText.text = "🔫";
        }
    }
}
