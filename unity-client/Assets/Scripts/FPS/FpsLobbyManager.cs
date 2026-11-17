using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.UI;
using UnityEngine.SceneManagement;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Manages the FPS lobby UI: room creation, joining, ready-up, and matchmaking.
    /// </summary>
    public class FpsLobbyManager : Singleton<FpsLobbyManager>
    {
        [Header("Lobby UI")]
        [SerializeField] private GameObject lobbyPanel;
        [SerializeField] private GameObject matchMakingPanel;
        [SerializeField] private GameObject inGamePanel;

        [Header("Create Match UI")]
        [SerializeField] private InputField matchNameInput;
        [SerializeField] private Dropdown gameModeDropdown;
        [SerializeField] private Dropdown mapDropdown;
        [SerializeField] private Button createMatchButton;
        [SerializeField] private Button joinMatchButton;
        [SerializeField] private Button readyButton;
        [SerializeField] private Text statusText;
        [SerializeField] private Text playerListText;
        [SerializeField] private ScrollRect matchListScroll;
        [SerializeField] private GameObject matchListItemPrefab;

        [Header("Settings")]
        [SerializeField] private string defaultMatchName = "My Match";
        [SerializeField] private string defaultMapId = "arena";

        private enum LobbyState
        {
            Idle,
            Browsing,
            InLobby,
            Ready,
            InMatch
        }

        private LobbyState currentState = LobbyState.Idle;
        private string currentMatchId;
        private bool isReady = false;
        private List<MatchInfo> availableMatches = new();

        [System.Serializable]
        private class MatchInfo
        {
            public string id;
            public string name;
            public string mode;
            public string mapName;
            public int playerCount;
            public int maxPlayers;
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            // Initialize UI
            InitializeUI();

            // Subscribe to network events
            FpsNetworkManager.Instance.OnConnected += OnConnected;
            FpsNetworkManager.Instance.OnDisconnected += OnDisconnected;
        }

        void OnDestroy()
        {
            if (FpsNetworkManager.Instance != null)
            {
                FpsNetworkManager.Instance.OnConnected -= OnConnected;
                FpsNetworkManager.Instance.OnDisconnected -= OnDisconnected;
            }
        }

        private void InitializeUI()
        {
            // Setup game mode dropdown
            if (gameModeDropdown != null)
            {
                gameModeDropdown.ClearOptions();
                gameModeDropdown.AddOptions(new List<string> { "Deathmatch", "Team Deathmatch", "Capture the Point" });
                gameModeDropdown.value = 0;
                gameModeDropdown.RefreshShownValue();
            }

            // Setup buttons
            if (createMatchButton != null)
                createMatchButton.onClick.AddListener(OnCreateMatchClicked);

            if (joinMatchButton != null)
                joinMatchButton.onClick.AddListener(OnJoinMatchClicked);

            if (readyButton != null)
                readyButton.onClick.AddListener(OnReadyClicked);

            // Load available maps
            LoadMaps();

            // Load available matches
            LoadMatches();

            // Show lobby panel
            ShowPanel(lobbyPanel);
            UpdateStatus("Welcome to FPS Lobby");
        }

        private void OnConnected(string sessionId)
        {
            Debug.Log($"[FpsLobbyManager] Connected: {sessionId}");
            UpdateStatus("Connected to server");
        }

        private void OnDisconnected()
        {
            Debug.Log("[FpsLobbyManager] Disconnected from server");
            UpdateStatus("Disconnected - attempting to reconnect...");
            currentState = LobbyState.Idle;
        }

        private void LoadMaps()
        {
            if (mapDropdown == null) return;

            StartCoroutine(FpsNetworkManager.Instance.FetchMaps(maps =>
            {
                if (maps == null) return;

                mapDropdown.ClearOptions();
                var options = new List<string>();

                foreach (var map in maps)
                {
                    string name = map.ContainsKey("name") ? map["name"]?.ToString() : "Unknown";
                    options.Add(name);
                }

                mapDropdown.AddOptions(options);
                mapDropdown.RefreshShownValue();
            }));
        }

        private void LoadMatches()
        {
            StartCoroutine(FpsNetworkManager.Instance.ListMatches(matches =>
            {
                if (matches == null) return;

                availableMatches.Clear();

                foreach (var match in matches)
                {
                    var info = new MatchInfo
                    {
                        id = match.ContainsKey("id") ? match["id"]?.ToString() : "",
                        name = match.ContainsKey("name") ? match["name"]?.ToString() : "Match",
                        mode = match.ContainsKey("mode") ? match["mode"]?.ToString() : "Deathmatch",
                        mapName = match.ContainsKey("mapName") ? match["mapName"]?.ToString() : "Unknown",
                        playerCount = match.ContainsKey("playerCount") ? Convert.ToInt32(match["playerCount"]) : 0,
                        maxPlayers = match.ContainsKey("maxPlayers") ? Convert.ToInt32(match["maxPlayers"]) : 16
                    };
                    availableMatches.Add(info);
                }

                UpdateMatchList();
            }));
        }

        private void UpdateMatchList()
        {
            if (matchListScroll == null || matchListItemPrefab == null) return;

            // Clear existing items
            var content = matchListScroll.content;
            while (content.childCount > 0)
            {
                Destroy(content.GetChild(0).gameObject);
            }

            // Create items for each match
            foreach (var match in availableMatches)
            {
                GameObject item = Instantiate(matchListItemPrefab, content);
                var text = item.GetComponentInChildren<Text>();
                if (text != null)
                {
                    text.text = $"{match.name} ({match.mode}) - {match.playerCount}/{match.maxPlayers} players";
                }

                // Add click handler
                var button = item.GetComponent<Button>();
                if (button != null)
                {
                    string matchId = match.id;
                    button.onClick.AddListener(() => JoinMatchById(matchId));
                }
            }
        }

        private void OnCreateMatchClicked()
        {
            string matchName = matchNameInput?.text ?? defaultMatchName;
            string mode = gameModeDropdown?.options[gameModeDropdown.value].text ?? "Deathmatch";
            string mapId = defaultMapId;

            if (mapDropdown != null && mapDropdown.value >= 0)
            {
                // Map ID would be fetched from the selected map
                mapId = defaultMapId;
            }

            UpdateStatus("Creating match...");

            StartCoroutine(FpsNetworkManager.Instance.CreateMatch(mode, mapId, matchId =>
            {
                if (!string.IsNullOrEmpty(matchId))
                {
                    currentMatchId = matchId;
                    currentState = LobbyState.InLobby;
                    isReady = false;
                    UpdateStatus($"Match created: {matchId}");
                    UpdateReadyButton();
                }
                else
                {
                    UpdateStatus("Failed to create match");
                }
            }));
        }

        private void OnJoinMatchClicked()
        {
            // Join the first available match
            if (availableMatches.Count > 0)
            {
                JoinMatchById(availableMatches[0].id);
            }
            else
            {
                UpdateStatus("No matches available");
            }
        }

        private void JoinMatchById(string matchId)
        {
            UpdateStatus("Joining match...");

            StartCoroutine(FpsNetworkManager.Instance.JoinMatch(matchId, success =>
            {
                if (success)
                {
                    currentMatchId = matchId;
                    currentState = LobbyState.InLobby;
                    isReady = false;
                    UpdateStatus($"Joined match: {matchId}");
                    UpdateReadyButton();
                }
                else
                {
                    UpdateStatus("Failed to join match");
                }
            }));
        }

        private void OnReadyClicked()
        {
            if (currentState != LobbyState.InLobby) return;

            isReady = !isReady;
            currentState = isReady ? LobbyState.Ready : LobbyState.InLobby;

            if (isReady)
            {
                FpsNetworkManager.Instance.SendReady();
                UpdateStatus("Ready! Waiting for other players...");
            }
            else
            {
                UpdateStatus("Not ready");
            }

            UpdateReadyButton();
        }

        private void UpdateReadyButton()
        {
            if (readyButton != null)
            {
                readyButton.interactable = (currentState == LobbyState.InLobby || currentState == LobbyState.Ready);
                if (readyButton.GetComponentInChildren<Text>() != null)
                {
                    readyButton.GetComponentInChildren<Text>().text = isReady ? "Unready" : "Ready";
                }
            }
        }

        private void UpdateStatus(string message)
        {
            if (statusText != null)
            {
                statusText.text = message;
            }
            Debug.Log($"[FpsLobbyManager] {message}");
        }

        private void ShowPanel(GameObject panel)
        {
            if (lobbyPanel != null) lobbyPanel.SetActive(false);
            if (matchMakingPanel != null) matchMakingPanel.SetActive(false);
            if (inGamePanel != null) inGamePanel.SetActive(false);

            if (panel != null)
                panel.SetActive(true);
        }

        /// <summary>
        /// Start the match and load the game scene.
        /// </summary>
        public void StartMatch()
        {
            if (currentState != LobbyState.Ready) return;

            currentState = LobbyState.InMatch;
            FpsNetworkManager.Instance.SendStartMatch();

            UpdateStatus("Match starting...");

            // Load the match scene
            StartCoroutine(LoadMatchScene());
        }

        private IEnumerator LoadMatchScene()
        {
            // Show loading panel
            ShowPanel(matchMakingPanel);

            // Async scene load
            AsyncOperation asyncLoad = SceneManager.LoadSceneAsync("FPS_Match");
            asyncLoad.allowSceneActivation = false;

            while (asyncLoad.progress < 0.9f)
            {
                yield return null;
            }

            asyncLoad.allowSceneActivation = true;
            yield return asyncLoad;
        }

        /// <summary>
        /// Return to lobby from match.
        /// </summary>
        public void ReturnToLobby()
        {
            currentState = LobbyState.Idle;
            currentMatchId = null;
            isReady = false;

            SceneManager.LoadScene("FPS_Lobby");
        }

        /// <summary>
        /// Update player list display.
        /// </summary>
        public void UpdatePlayerList()
        {
            if (playerListText == null) return;

            var players = FpsGameState.Instance.GetPlayersByScore();
            string list = "";
            for (int i = 0; i < players.Count; i++)
            {
                var p = players[i];
                list += $"{i + 1}. {p.Name} - Score: {p.Score}\n";
            }

            playerListText.text = list;
        }

        /// <summary>
        /// Get the current lobby state.
        /// </summary>
        public LobbyState GetState()
        {
            return currentState;
        }

        /// <summary>
        /// Check if the player is ready.
        /// </summary>
        public bool IsReady()
        {
            return isReady;
        }
    }
}
