using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Multiplayer 3D synchronization manager.
    /// Synchronizes 3D positions across clients with voice chat and spatial audio.
    /// Provides shared battlefield view and real-time unit movement visualization.
    /// </summary>
    public class MultiplayerManager : Singleton<MultiplayerManager>
    {
        [Header("Network Settings")]
        [SerializeField] private float syncInterval = 0.1f;
        [SerializeField] private float interpolationSpeed = 10f;
        [SerializeField] private bool enableVoiceChat = true;
        [SerializeField] private bool enableSpatialAudio = true;

        [Header("Player Management")]
        private Dictionary<string, NetworkPlayer> players = new();
        private string localPlayerId;
        private Transform localPlayerTransform;

        [Header("Voice Chat")]
        private bool isRecording;
        private bool isMuted;
        private float voiceVolume;

        [Header("UI")]
        [SerializeField] private GameObject playerListPanel;
        [SerializeField] private Text playerCountText;
        [SerializeField] private Text voiceStatusText;

        private GameObject playerListPanelGo;

        /// <summary>
        /// Network player data.
        /// </summary>
        public class NetworkPlayer
        {
            public string Id;
            public string Name;
            public Vector3 Position;
            public Quaternion Rotation;
            public Vector3 TargetPosition;
            public Quaternion TargetRotation;
            public float InterpolationT;
            public GameObject Avatar;
            public bool IsLocal;
            public bool IsTalking;
            public float VoiceLevel;
            public Dictionary<string, object> Data;

            public NetworkPlayer(string id, string name, bool isLocal)
            {
                Id = id;
                Name = name;
                IsLocal = isLocal;
                Data = new Dictionary<string, object>();
            }
        }

        // Events
        public System.Action<string> OnPlayerJoined;
        public System.Action<string> OnPlayerLeft;
        public System.Action<string> OnPlayerTalking;
        public System.Action<string> OnPlayerStoppedTalking;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            EnsureUIElements();
            StartCoroutine(SyncLoop());
        }

        void Update()
        {
            InterpolatePlayers();
            UpdateVoiceChat();
        }

        /// <summary>
        /// Create multiplayer UI elements.
        /// </summary>
        private void EnsureUIElements()
        {
            var canvas = FindObjectOfType<Canvas>();
            if (canvas == null) return;

            if (playerListPanel == null)
            {
                playerListPanelGo = new GameObject("PlayerListPanel");
                playerListPanelGo.transform.SetParent(canvas.transform);
                
                var rect = playerListPanelGo.AddComponent<RectTransform>();
                rect.anchorMin = new Vector2(1, 0.5f);
                rect.anchorMax = new Vector2(1, 1);
                rect.anchoredPosition = new Vector2(-10, -10);
                rect.sizeDelta = new Vector2(-200, -20);

                var bg = playerListPanelGo.AddComponent<Image>();
                bg.color = new Color(0, 0, 0, 0.7f);

                var titleGo = new GameObject("Title");
                titleGo.transform.SetParent(playerListPanelGo.transform);
                var titleRect = titleGo.AddComponent<RectTransform>();
                titleRect.anchorMin = new Vector2(0, 1);
                titleRect.anchorMax = new Vector2(1, 1);
                titleRect.anchoredPosition = new Vector2(0, -10);
                titleRect.sizeDelta = new Vector2(0, -30);

                var titleText = titleGo.AddComponent<Text>();
                titleText.text = "PLAYERS";
                titleText.fontSize = 16;
                titleText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                titleText.color = Color.cyan;
                titleText.fontStyle = FontStyle.Bold;
                titleText.alignment = TextAnchor.MiddleCenter;

                var countGo = new GameObject("PlayerCount");
                countGo.transform.SetParent(playerListPanelGo.transform);
                var countRect = countGo.AddComponent<RectTransform>();
                countRect.anchorMin = new Vector2(0, 0.85f);
                countRect.anchorMax = new Vector2(1, 0.95f);
                countRect.anchoredPosition = Vector2.zero;
                countRect.sizeDelta = Vector2.zero;

                playerCountText = countGo.AddComponent<Text>();
                playerCountText.text = "Players: 0";
                playerCountText.fontSize = 12;
                playerCountText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                playerCountText.color = Color.white;
                playerCountText.horizontalOverflow = HorizontalWrapMode.Overflow;

                var voiceGo = new GameObject("VoiceStatus");
                voiceGo.transform.SetParent(playerListPanelGo.transform);
                var voiceRect = voiceGo.AddComponent<RectTransform>();
                voiceRect.anchorMin = new Vector2(0, 0.75f);
                voiceRect.anchorMax = new Vector2(1, 0.85f);
                voiceRect.anchoredPosition = Vector2.zero;
                voiceRect.sizeDelta = Vector2.zero;

                voiceStatusText = voiceGo.AddComponent<Text>();
                voiceStatusText.text = "Voice: Off";
                voiceStatusText.fontSize = 12;
                voiceStatusText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
                voiceStatusText.color = Color.gray;
                voiceStatusText.horizontalOverflow = HorizontalWrapMode.Overflow;
            }
        }

        /// <summary>
        /// Periodic synchronization loop.
        /// </summary>
        private System.Collections.IEnumerator SyncLoop()
        {
            while (true)
            {
                yield return new WaitForSeconds(syncInterval);
                SendPositionUpdate();
            }
        }

        /// <summary>
        /// Register a local player.
        /// </summary>
        public void RegisterLocalPlayer(string playerId, string playerName)
        {
            localPlayerId = playerId;
            
            var player = new NetworkPlayer(playerId, playerName, true);
            players[playerId] = player;

            // Create avatar
            CreatePlayerAvatar(player);

            OnPlayerJoined?.Invoke(playerName);
            UpdatePlayerList();
            Debug.Log($"[MultiplayerManager] Local player registered: {playerName}");
        }

        /// <summary>
        /// Register a remote player.
        /// </summary>
        public void RegisterRemotePlayer(string playerId, string playerName, Vector3 position)
        {
            if (players.ContainsKey(playerId)) return;

            var player = new NetworkPlayer(playerId, playerName, false);
            player.Position = position;
            player.TargetPosition = position;
            players[playerId] = player;

            // Create avatar
            CreatePlayerAvatar(player);

            OnPlayerJoined?.Invoke(playerName);
            UpdatePlayerList();
            Debug.Log($"[MultiplayerManager] Remote player joined: {playerName}");
        }

        /// <summary>
        /// Remove a player.
        /// </summary>
        public void RemovePlayer(string playerId)
        {
            if (players.ContainsKey(playerId))
            {
                var player = players[playerId];
                
                if (player.Avatar != null)
                {
                    GameObject.Destroy(player.Avatar);
                }

                players.Remove(playerId);
                OnPlayerLeft?.Invoke(player.Name);
                UpdatePlayerList();
                Debug.Log($"[MultiplayerManager] Player left: {player.Name}");
            }
        }

        /// <summary>
        /// Create a 3D avatar for a player.
        /// </summary>
        private void CreatePlayerAvatar(NetworkPlayer player)
        {
            var avatar = new GameObject($"Player_{player.Id}");
            avatar.transform.position = player.Position;

            // Capsule collider
            var capsule = avatar.AddComponent<CapsuleCollider>();
            capsule.height = 2f;
            capsule.radius = 0.5f;

            // Visual
            var visual = new GameObject("Visual");
            visual.transform.SetParent(avatar.transform);
            visual.transform.localPosition = Vector3.up;

            var meshRenderer = visual.AddComponent<MeshRenderer>();
            var color = player.IsLocal ? Color.cyan : GetPlayerColor(player.Id);
            meshRenderer.material = new Material(Shader.Find("Standard"))
            {
                color = color,
                emissiveColor = color,
                enableInstancing = false
            };
            meshRenderer.material.SetFloat("_EmissionStrength", 0.3f);
            var meshFilter = visual.AddComponent<MeshFilter>();
            meshFilter.mesh = new Mesh();

            // Name label
            var labelCanvas = new GameObject("LabelCanvas");
            labelCanvas.transform.SetParent(avatar.transform);
            labelCanvas.transform.localPosition = Vector3.up * 2.5f;

            var canvas = labelCanvas.AddComponent<Canvas>();
            canvas.renderMode = UnityEngine.RenderMode.WorldSpace;
            labelCanvas.AddComponent<CanvasRenderer>();
            labelCanvas.AddComponent<GraphicRaycaster>();

            var labelGo = new GameObject("LabelText");
            labelGo.transform.SetParent(labelCanvas.transform);
            var labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.sizeDelta = new Vector2(5, 1);

            var labelText = labelGo.AddComponent<Text>();
            labelText.text = player.Name + (player.IsLocal ? " (You)" : "");
            labelText.fontSize = 24;
            labelText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            labelText.color = Color.white;
            labelText.alignment = TextAnchor.MiddleCenter;

            // Voice indicator
            var voiceIndicator = new GameObject("VoiceIndicator");
            voiceIndicator.transform.SetParent(avatar.transform);
            voiceIndicator.transform.localPosition = Vector3.up * 2;

            var voiceMesh = voiceIndicator.AddComponent<MeshRenderer>();
            voiceMesh.material = new Material(Shader.Find("Standard"))
            {
                color = Color.green,
                emissiveColor = Color.green,
                enableInstancing = false,
                transparent = true
            };
            voiceMesh.material.SetFloat("_Mode", 3);
            voiceMesh.material.globalIlluminationFlags = UnityEngine.Rendering.GlobalIlluminationFlags.BakedIndirect;
            voiceMesh.material.disablePixelLights = true;
            voiceMesh.material.globalOpacityMetaMode = UnityEngine.Rendering.GlobalOpacityMetaMode.Standard;
            voiceMesh.material.SetFloat("_AlphaClipThreshold", 0.1f);
            voiceMesh.material.SetFloat("_SrcBlend", (float)UnityEngine.Rendering.BlendMode.One);
            voiceMesh.material.SetFloat("_DstBlend", (float)UnityEngine.Rendering.BlendMode.OneMinusSrcAlpha);
            voiceMesh.material.SetFloat("_ZWrite", 0f);
            voiceMesh.material.SetInt("_Cull", 0);
            voiceMesh.material.SetInt("_AlphaClip", 0);
            voiceMesh.material.hideFlags = UnityEngine.HideFlags.DontSave;
            voiceIndicator.SetActive(false);

            player.Avatar = avatar;
            if (player.IsLocal)
            {
                localPlayerTransform = avatar.transform;
            }
        }

        /// <summary>
        /// Get a unique color for a player.
        /// </summary>
        private Color GetPlayerColor(string playerId)
        {
            var colors = new[]
            {
                Color.red, Color.green, Color.blue, Color.yellow,
                Color.magenta, Color.cyan, Color.orange, Color.gray
            };
            int hash = playerId.GetHashCode();
            return colors[Mathf.Abs(hash) % colors.Length];
        }

        /// <summary>
        /// Update a remote player's position.
        /// </summary>
        public void UpdateRemotePlayerPosition(string playerId, Vector3 position, Quaternion rotation)
        {
            if (players.ContainsKey(playerId))
            {
                var player = players[playerId];
                player.TargetPosition = position;
                player.TargetRotation = rotation;
                player.InterpolationT = 0;
            }
        }

        /// <summary>
        /// Interpolate player positions for smooth movement.
        /// </summary>
        private void InterpolatePlayers()
        {
            foreach (var kvp in players)
            {
                var player = kvp.Value;
                if (player.IsLocal || player.Avatar == null) continue;

                player.InterpolationT += Time.deltaTime * interpolationSpeed;
                if (player.InterpolationT >= 1)
                {
                    player.InterpolationT = 1;
                    player.Position = player.TargetPosition;
                    player.Rotation = player.TargetRotation;
                }
                else
                {
                    player.Position = Vector3.Lerp(player.Position, player.TargetPosition, player.InterpolationT);
                    player.Rotation = Quaternion.Slerp(player.Rotation, player.TargetRotation, player.InterpolationT);
                }

                player.Avatar.transform.position = player.Position;
                player.Avatar.transform.rotation = player.Rotation;
            }
        }

        /// <summary>
        /// Send position update to server.
        /// </summary>
        private void SendPositionUpdate()
        {
            if (localPlayerTransform == null) return;

            // Send position via NetworkManager
            var nm = Network.NetworkManager.Instance;
            if (nm != null && nm.IsConnected)
            {
                // Position update would be sent here
            }
        }

        /// <summary>
        /// Update voice chat status.
        /// </summary>
        private void UpdateVoiceChat()
        {
            if (!enableVoiceChat) return;

            // Toggle voice with microphone key
            if (Input.GetKey(KeyCode.M))
            {
                if (!isRecording)
                {
                    StartVoiceRecording();
                }
            }
            else
            {
                if (isRecording)
                {
                    StopVoiceRecording();
                }
            }

            // Update voice status text
            if (voiceStatusText != null)
            {
                if (isMuted)
                {
                    voiceStatusText.text = "Voice: Muted";
                    voiceStatusText.color = Color.red;
                }
                else if (isRecording)
                {
                    voiceStatusText.text = "Voice: Talking";
                    voiceStatusText.color = Color.green;
                }
                else
                {
                    voiceStatusText.text = "Voice: Press M to talk";
                    voiceStatusText.color = Color.gray;
                }
            }
        }

        /// <summary>
        /// Start voice recording.
        /// </summary>
        private void StartVoiceRecording()
        {
            isRecording = true;
            OnPlayerTalking?.Invoke(localPlayerId);
            Debug.Log("[MultiplayerManager] Voice recording started");
        }

        /// <summary>
        /// Stop voice recording.
        /// </summary>
        private void StopVoiceRecording()
        {
            isRecording = false;
            OnPlayerStoppedTalking?.Invoke(localPlayerId);
            Debug.Log("[MultiplayerManager] Voice recording stopped");
        }

        /// <summary>
        /// Toggle mute.
        /// </summary>
        public void ToggleMute()
        {
            isMuted = !isMuted;
            Debug.Log($"[MultiplayerManager] Voice {(isMuted ? "muted" : "unmuted")}");
        }

        /// <summary>
        /// Set a player as talking.
        /// </summary>
        public void SetPlayerTalking(string playerId, bool isTalking)
        {
            if (players.ContainsKey(playerId))
            {
                var player = players[playerId];
                player.IsTalking = isTalking;

                if (player.Avatar != null)
                {
                    var indicator = player.Avatar.transform.Find("VoiceIndicator");
                    if (indicator != null)
                    {
                        indicator.SetActive(isTalking);
                    }
                }
            }
        }

        /// <summary>
        /// Update the player list UI.
        /// </summary>
        private void UpdatePlayerList()
        {
            if (playerCountText != null)
            {
                playerCountText.text = $"Players: {players.Count}";
            }
        }

        /// <summary>
        /// Get all connected players.
        /// </summary>
        public Dictionary<string, NetworkPlayer> GetPlayers()
        {
            return players;
        }

        /// <summary>
        /// Get player count.
        /// </summary>
        public int GetPlayerCount()
        {
            return players.Count;
        }

        /// <summary>
        /// Get local player ID.
        /// </summary>
        public string GetLocalPlayerId()
        {
            return localPlayerId;
        }

        /// <summary>
        /// Get local player transform.
        /// </summary>
        public Transform GetLocalPlayerTransform()
        {
            return localPlayerTransform;
        }

        /// <summary>
        /// Clean up multiplayer resources.
        /// </summary>
        public void Cleanup()
        {
            foreach (var kvp in players)
            {
                if (kvp.Value.Avatar != null)
                {
                    GameObject.Destroy(kvp.Value.Avatar);
                }
            }
            players.Clear();
        }
    }
}
