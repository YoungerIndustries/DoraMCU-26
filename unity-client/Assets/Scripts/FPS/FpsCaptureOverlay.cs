using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Displays capture point status and team scores for Capture the Point game mode.
    /// Shows point icons on screen with capture progress bars and team score display.
    /// </summary>
    public class FpsCaptureOverlay : MonoBehaviour
    {
        [Header("Point Status UI")]
        [Tooltip("Panel that contains capture point status prefabs")]
        public Transform pointStatusParent;

        [Tooltip("Prefab for each capture point status display")]
        public GameObject pointStatusPrefab;

        [Header("Team Scores UI")]
        [Tooltip("Text display for Team 0 score")]
        public Text team0ScoreText;

        [Tooltip("Text display for Team 1 score")]
        public Text team1ScoreText;

        [Header("Settings")]
        [Tooltip("Color for neutral points")]
        public Color neutralColor = Color.gray;

        [Tooltip("Color for Team 0 controlled points")]
        public Color team0Color = Color.blue;

        [Tooltip("Color for Team 1 controlled points")]
        public Color team1Color = Color.red;

        [Tooltip("Color for contested points")]
        public Color contestedColor = Color.yellow;

        private Dictionary<string, PointStatusView> pointViews = new Dictionary<string, PointStatusView>();
        private FpsGameState gameState;

        void Start()
        {
            gameState = FindObjectOfType<FpsGameState>();
            if (team0ScoreText != null) team0ScoreText.text = "0";
            if (team1ScoreText != null) team1ScoreText.text = "0";
        }

        /// <summary>
        /// Update the overlay with current capture point states from the game state.
        /// </summary>
        public void UpdateFromGameState()
        {
            if (gameState == null) return;

            // Update team scores
            if (gameState.TeamScores != null)
            {
                if (team0ScoreText != null && gameState.TeamScores.ContainsKey(0))
                    team0ScoreText.text = gameState.TeamScores[0].ToString();
                if (team1ScoreText != null && gameState.TeamScores.ContainsKey(1))
                    team1ScoreText.text = gameState.TeamScores[1].ToString();
            }

            // Update capture point statuses
            if (gameState.CapturePoints != null)
            {
                var currentPointIds = new HashSet<string>();
                foreach (var cp in gameState.CapturePoints)
                {
                    currentPointIds.Add(cp.PointId);
                    if (!pointViews.ContainsKey(cp.PointId))
                    {
                        CreatePointView(cp.PointId);
                    }
                    pointViews[cp.PointId].Update(cp);
                }

                // Remove views for points no longer in game state
                var removedIds = new List<string>();
                foreach (var id in pointViews.Keys)
                {
                    if (!currentPointIds.Contains(id))
                        removedIds.Add(id);
                }
                foreach (var id in removedIds)
                {
                    Destroy(pointViews[id].gameObject);
                    pointViews.Remove(id);
                }
            }
        }

        private void CreatePointView(string pointId)
        {
            if (pointStatusParent == null || pointStatusPrefab == null) return;

            var go = Instantiate(pointStatusPrefab, pointStatusParent);
            var view = go.GetComponent<PointStatusView>();
            if (view == null)
                view = go.AddComponent<PointStatusView>();

            view.Initialize(pointId, neutralColor, team0Color, team1Color, contestedColor);
            pointViews[pointId] = view;
        }

        /// <summary>
        /// Clear all point views (called when match ends).
        /// </summary>
        public void Clear()
        {
            foreach (var view in pointViews.Values)
                Destroy(view.gameObject);
            pointViews.Clear();
            if (team0ScoreText != null) team0ScoreText.text = "0";
            if (team1ScoreText != null) team1ScoreText.text = "0";
        }
    }

    /// <summary>
    /// View component for a single capture point status display.
    /// Shows point label, capture progress bar, and ownership color.
    /// </summary>
    public class PointStatusView : MonoBehaviour
    {
        public Text pointLabel;
        public Slider progressBar;
        public Image backgroundPanel;

        private string pointId;
        private Color neutralColor;
        private Color team0Color;
        private Color team1Color;
        private Color contestedColor;

        public void Initialize(string id, Color neutral, Color t0, Color t1, Color contested)
        {
            pointId = id;
            neutralColor = neutral;
            team0Color = t0;
            team1Color = t1;
            contestedColor = contested;

            if (pointLabel == null)
            {
                var rect = transform.Find("Label")?.gameObject ?? 
                    new GameObject("Label");
                pointLabel = rect.AddComponent<Text>();
                pointLabel.text = id;
            }
            if (progressBar == null)
            {
                var sliderGo = transform.Find("Progress")?.gameObject ??
                    new GameObject("Progress");
                progressBar = sliderGo.AddComponent<Slider>();
            }
            if (backgroundPanel == null)
            {
                var bgGo = transform.Find("Background")?.gameObject ??
                    new GameObject("Background");
                backgroundPanel = bgGo.AddComponent<Image>();
            }
        }

        public void Update(FpsGameState.CapturePointState state)
        {
            if (pointLabel != null)
                pointLabel.text = state.PointId;

            if (progressBar != null)
                progressBar.value = Mathf.Clamp01((float)state.CaptureProgress / 100.0f);

            if (backgroundPanel != null)
            {
                if (state.OwnerTeam < 0)
                    backgroundPanel.color = neutralColor;
                else if (state.OwnerTeam == 0)
                    backgroundPanel.color = team0Color;
                else
                    backgroundPanel.color = team1Color;

                // Flash yellow if contested (progress between 10 and 90)
                if (state.CaptureProgress > 10 && state.CaptureProgress < 90)
                    backgroundPanel.color = contestedColor;
            }
        }
    }
}
