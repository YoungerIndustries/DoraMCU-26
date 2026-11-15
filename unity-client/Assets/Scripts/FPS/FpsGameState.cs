using System;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.FPS
{
    /// <summary>
    /// Manages client-side game state, syncing with server state updates.
    /// Tracks all players, scores, and match information.
    /// </summary>
    public class FpsGameState : Singleton<FpsGameState>
    {
        private Dictionary<string, PlayerStateData> players = new();
        private PlayerStateData localPlayer;
        private MatchInfoData matchInfo;
        private List<GameStateEvent> recentEvents = new();

        // Capture the Point state
        private List<CapturePointState> capturePoints = new();
        private Dictionary<int, int> teamScores = new();

        // Events
        public event Action<PlayerStateData> OnLocalPlayerUpdated;
        public event Action<string, PlayerStateData> OnPlayerJoined;
        public event Action<string> OnPlayerLeft;
        public event Action<GameStateEvent> OnGameEvent;
        public event Action<MatchInfoData> OnMatchInfoUpdated;
        public event Action OnMatchStarted;
        public event Action OnMatchEnded;

        public Dictionary<string, PlayerStateData> Players => players;
        public PlayerStateData LocalPlayer => localPlayer;
        public MatchInfoData MatchInfo => matchInfo;
        public List<GameStateEvent> RecentEvents => recentEvents;
        public int PlayerCount => players.Count;
        public List<CapturePointState> CapturePoints => capturePoints;
        public Dictionary<int, int> TeamScores => teamScores;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            // Subscribe to network events
            FpsNetworkManager.Instance.OnGameStateReceived += OnGameStateReceived;
            FpsNetworkManager.Instance.OnGameEvent += OnGameEventReceived;
            FpsNetworkManager.Instance.OnConnected += OnConnected;
            FpsNetworkManager.Instance.OnDisconnected += OnDisconnected;
        }

        void OnDestroy()
        {
            FpsNetworkManager.Instance.OnGameStateReceived -= OnGameStateReceived;
            FpsNetworkManager.Instance.OnGameEvent -= OnGameEventReceived;
            FpsNetworkManager.Instance.OnConnected -= OnConnected;
            FpsNetworkManager.Instance.OnDisconnected -= OnDisconnected;
        }

        private void OnConnected(string sessionId)
        {
            Debug.Log($"[FpsGameState] Connected to session: {sessionId}");
        }

        private void OnDisconnected()
        {
            Debug.Log("[FpsGameState] Disconnected from server");
            players.Clear();
            localPlayer = null;
        }

        private void OnGameStateReceived(FpsGameStateMessage state)
        {
            // Track new players and remove disconnected ones
            var currentIds = new HashSet<string>();

            foreach (var ps in state.Players)
            {
                currentIds.Add(ps.Id);

                if (players.ContainsKey(ps.Id))
                {
                    // Update existing player
                    players[ps.Id] = ps;
                }
                else
                {
                    // New player joined
                    players[ps.Id] = ps;
                    OnPlayerJoined?.Invoke(ps.Id, ps);
                }
            }

            // Remove players that are no longer in the state
            var removedIds = new List<string>();
            foreach (var id in players.Keys)
            {
                if (!currentIds.Contains(id))
                {
                    removedIds.Add(id);
                    OnPlayerLeft?.Invoke(id);
                }
            }
            foreach (var id in removedIds)
            {
                players.Remove(id);
            }

            // Update local player
            if (!string.IsNullOrEmpty(FpsNetworkManager.Instance.PlayerId))
            {
                string localId = FpsNetworkManager.Instance.PlayerId;
                if (players.ContainsKey(localId))
                {
                    localPlayer = players[localId];
                    OnLocalPlayerUpdated?.Invoke(localPlayer);
                }
            }

            // Update match info
            if (state.MatchInfo != null)
            {
                matchInfo = state.MatchInfo;
                OnMatchInfoUpdated?.Invoke(matchInfo);
            }

            // Process events
            if (state.Events != null)
            {
                foreach (var evt in state.Events)
                {
                    recentEvents.Add(evt);
                    OnGameEvent?.Invoke(evt);

                    // Check for match end
                    if (evt.Type == "MATCH_END")
                    {
                        OnMatchEnded?.Invoke();
                    }
                    if (evt.Type == "MATCH_START")
                    {
                        OnMatchStarted?.Invoke();
                    }
                }
            }

            // Process capture points (CTP mode)
            if (state.CapturePoints != null)
            {
                capturePoints.Clear();
                foreach (var cp in state.CapturePoints)
                {
                    capturePoints.Add(cp);
                }
            }

            // Process team scores (CTP mode)
            if (state.TeamScores != null)
            {
                teamScores.Clear();
                foreach (var kvp in state.TeamScores)
                {
                    teamScores[kvp.Key] = kvp.Value;
                }
            }

            // Keep only last 50 events
            while (recentEvents.Count > 50)
                recentEvents.RemoveAt(0);
        }

        private void OnGameEventReceived(GameStateEvent evt)
        {
            recentEvents.Add(evt);
            OnGameEvent?.Invoke(evt);

            while (recentEvents.Count > 50)
                recentEvents.RemoveAt(0);
        }

        /// <summary>
        /// Get a player by ID.
        /// </summary>
        public PlayerStateData GetPlayer(string playerId)
        {
            players.TryGetValue(playerId, out PlayerStateData player);
            return player;
        }

        /// <summary>
        /// Get all alive players.
        /// </summary>
        public List<PlayerStateData> GetAlivePlayers()
        {
            var alive = new List<PlayerStateData>();
            foreach (var ps in players.Values)
            {
                if (ps.Alive)
                    alive.Add(ps);
            }
            return alive;
        }

        /// <summary>
        /// Get players sorted by score (for leaderboard).
        /// </summary>
        public List<PlayerStateData> GetPlayersByScore()
        {
            var sorted = new List<PlayerStateData>(players.Values);
            sorted.Sort((a, b) => b.Score.CompareTo(a.Score));
            return sorted;
        }

        /// <summary>
        /// Get kill events for the kill feed.
        /// </summary>
        public List<GameStateEvent> GetKillEvents()
        {
            var kills = new List<GameStateEvent>();
            foreach (var evt in recentEvents)
            {
                if (evt.Type == "KILL" || evt.Type == "HIT")
                    kills.Add(evt);
            }
            return kills;
        }

        /// <summary>
        /// Get the local player's health percentage.
        /// </summary>
        public float GetLocalHealthPercent()
        {
            if (localPlayer == null) return 1f;
            return Mathf.Clamp01(localPlayer.Health / 100f);
        }

        /// <summary>
        /// Get the local player's ammo percentage.
        /// </summary>
        public float GetLocalAmmoPercent()
        {
            if (localPlayer == null) return 1f;
            // Find the weapon definition to get mag size
            return Mathf.Clamp01((float)localPlayer.Ammo / 30f); // Default mag size
        }
    }
}
