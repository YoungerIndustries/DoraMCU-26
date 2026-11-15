using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// Cross-game 3D hub world connecting all game worlds.
    /// Players navigate between games through 3D portals.
    /// Features shared inventory, persistent avatar, and game world previews.
    /// </summary>
    public class HubWorld : Singleton<HubWorld>
    {
        [Header("Hub Settings")]
        [SerializeField] private float portalSize = 3f;
        [SerializeField] private float portalRotationSpeed = 0.5f;
        [SerializeField] private int maxPortals = 12;
        [SerializeField] private float portalRadius = 15f;

        [Header("Avatar")]
        [SerializeField] private GameObject avatarPrefab;
        private GameObject playerAvatar;
        private CharacterController avatarController;

        [Header("Hub Environment")]
        private GameObject hubEnvironment;
        private List<GameObject> portals = new();
        private Dictionary<string, GameObject> portalMap = new();

        [Header("Game List")]
        private List<GameWorld> gameWorlds = new();

        /// <summary>
        /// Represents a game world in the hub.
        /// </summary>
        public class GameWorld
        {
            public string Id;
            public string Name;
            public string Description;
            public string IconPath;
            public Color Color;
            public int ActiveSessions;
            public Vector3 Position;
            public bool IsAvailable;

            public GameWorld(string id, string name, string description, Color color)
            {
                Id = id;
                Name = name;
                Description = description;
                Color = color;
                IsAvailable = true;
            }
        }

        // Events
        public System.Action<string> OnEnterGame;
        public System.Action<string> OnExitGame;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            InitializeHub();
        }

        /// <summary>
        /// Initialize the hub world.
        /// </summary>
        private void InitializeHub()
        {
            // Create hub environment
            CreateHubEnvironment();

            // Create player avatar
            CreatePlayerAvatar();

            // Register default game worlds
            RegisterDefaultGames();

            // Create portals
            CreatePortals();

            Debug.Log("[HubWorld] Hub world initialized");
        }

        /// <summary>
        /// Create the hub environment (central space station).
        /// </summary>
        private void CreateHubEnvironment()
        {
            hubEnvironment = new GameObject("HubEnvironment");

            // Central platform
            var platform = new GameObject("CentralPlatform");
            platform.transform.SetParent(hubEnvironment.transform);
            platform.transform.position = Vector3.zero;

            var platformMesh = platform.AddComponent<CylinderCollider>();
            platformMesh.radius = 20f;
            platformMesh.height = 1f;

            var platformVisual = new GameObject("PlatformVisual");
            platformVisual.transform.SetParent(platform.transform);
            var platformGeo = platformVisual.AddComponent<MeshRenderer>();
            var platformFilter = platformVisual.AddComponent<MeshFilter>();
            platformFilter.mesh = new Mesh(); // Placeholder

            // Hub walls (circular corridor)
            var walls = new GameObject("HubWalls");
            walls.transform.SetParent(hubEnvironment.transform);
            var wallGeo = walls.AddComponent<MeshRenderer>();
            var wallFilter = walls.AddComponent<MeshFilter>();
            wallFilter.mesh = new Mesh();

            // Lighting
            var ambientLight = new GameObject("AmbientLight");
            ambientLight.transform.SetParent(hubEnvironment.transform);
            var light = ambientLight.AddComponent<Light>();
            light.type = LightType.Directional;
            light.color = Color.white;
            light.intensity = 0.8f;
            light.transform.rotation = Quaternion.Euler(60, 0, 0);

            // Starfield background
            CreateStarfield();
        }

        /// <summary>
        /// Create starfield background for hub.
        /// </summary>
        private void CreateStarfield()
        {
            var starfield = new GameObject("Starfield");
            starfield.transform.SetParent(hubEnvironment.transform);

            var ps = starfield.AddComponent<ParticleSystem>();
            var main = ps.main;
            main.loop = true;
            main.playOnAwake = true;
            main.maxParticles = 1000;
            main.startColor = Color.white;
            main.startSize = 0.1f;
            main.startLifetime = 10f;
            main.simulationSpace = ParticleSystemSimulationSpace.World;

            var emit = ps.emission;
            emit.rateOverTime = 50;

            var shape = ps.shape;
            shape.enabled = true;
            shape.type = ParticleSystemShapeType.Sphere;
            shape.radius = 100f;

            ps.Play();
        }

        /// <summary>
        /// Create the player avatar.
        /// </summary>
        private void CreatePlayerAvatar()
        {
            if (avatarPrefab != null)
            {
                playerAvatar = Instantiate(avatarPrefab, Vector3.zero, Quaternion.identity);
            }
            else
            {
                // Create simple avatar
                playerAvatar = new GameObject("PlayerAvatar");
                playerAvatar.transform.position = Vector3.zero;

                var capsule = playerAvatar.AddComponent<CapsuleCollider>();
                capsule.height = 2f;
                capsule.radius = 0.5f;

                var visual = new GameObject("AvatarVisual");
                visual.transform.SetParent(playerAvatar.transform);
                visual.transform.localPosition = Vector3.up;
                
                var meshRenderer = visual.AddComponent<MeshRenderer>();
                meshRenderer.material = new Material(Shader.Find("Standard"))
                {
                    color = Color.cyan
                };
                var meshFilter = visual.AddComponent<MeshFilter>();
                meshFilter.mesh = new Mesh();
            }

            avatarController = playerAvatar.GetComponent<CharacterController>();
            if (avatarController == null)
            {
                avatarController = playerAvatar.AddComponent<CharacterController>();
            }
        }

        /// <summary>
        /// Register default game worlds.
        /// </summary>
        private void RegisterDefaultGames()
        {
            gameWorlds.Clear();

            gameWorlds.Add(new GameWorld("traveller", "Traveller", "Classic space RPG", Color.blue));
            gameWorlds.Add(new GameWorld("dnd5e", "D&D 5e", "Fantasy adventure", Color.red));
            gameWorlds.Add(new GameWorld("startrek", "Star Trek 4X", "Space strategy", Color.cyan));
            gameWorlds.Add(new GameWorld("babylon5", "Babylon 5", "Space station RPG", Color.green));
            gameWorlds.Add(new GameWorld("captainslog", "Captain's Log", "Starship simulation", Color.yellow));
            gameWorlds.Add(new GameWorld("starwars", "Star Wars", "Galactic adventure", Color.orange));
            gameWorlds.Add(new GameWorld("starshipTroopers", "Starship Troopers", "Military sci-fi", Color.gray));
            gameWorlds.Add(new GameWorld("generic", "Generic RPG", "Custom RPG system", Color.magenta));
        }

        /// <summary>
        /// Create portals for each game world.
        /// </summary>
        private void CreatePortals()
        {
            // Clear existing portals
            foreach (var portal in portals)
            {
                Destroy(portal);
            }
            portals.Clear();
            portalMap.Clear();

            float angleStep = (2 * Mathf.PI) / Mathf.Min(gameWorlds.Count, maxPortals);

            for (int i = 0; i < gameWorlds.Count && i < maxPortals; i++)
            {
                var game = gameWorlds[i];
                float angle = i * angleStep;

                var position = new Vector3(
                    Mathf.Sin(angle) * portalRadius,
                    0,
                    -Mathf.Cos(angle) * portalRadius
                );
                game.Position = position;

                var portal = CreatePortal(position, game);
                portals.Add(portal);
                portalMap[game.Id] = portal;
            }
        }

        /// <summary>
        /// Create a single portal for a game world.
        /// </summary>
        private GameObject CreatePortal(Vector3 position, GameWorld game)
        {
            var portalGroup = new GameObject($"Portal_{game.Id}");
            portalGroup.transform.position = position;
            portalGroup.transform.SetParent(hubEnvironment.transform);

            // Portal ring
            var ring = new GameObject("Ring");
            ring.transform.SetParent(portalGroup.transform);
            var ringMesh = ring.AddComponent<MeshRenderer>();
            ringMesh.material = new Material(Shader.Find("Standard"))
            {
                color = game.Color,
                emissiveColor = game.Color,
                enableInstancing = false
            };
            var ringFilter = ring.AddComponent<MeshFilter>();
            ringFilter.mesh = new Mesh();

            // Portal center (glowing disc)
            var center = new GameObject("Center");
            center.transform.SetParent(portalGroup.transform);
            var centerMesh = center.AddComponent<MeshRenderer>();
            centerMesh.material = new Material(Shader.Find("Standard"))
            {
                color = game.Color,
                emissiveColor = game.Color,
                enableInstancing = false,
                transparent = true
            };
            centerMesh.material.SetFloat("_Mode", 3); // Transparent
            centerMesh.material.globalIlluminationFlags = UnityEngine.Rendering.GlobalIlluminationFlags.BakedIndirect;
            centerMesh.material.disablePixelLights = true;
            centerMesh.material.globalOpacityMetaMode = UnityEngine.Rendering.GlobalOpacityMetaMode.Standard;
            var centerFilter = center.AddComponent<MeshFilter>();
            centerFilter.mesh = new Mesh();

            // Portal light
            var light = new GameObject("Light");
            light.transform.SetParent(portalGroup.transform);
            var lightComp = light.AddComponent<Light>();
            lightComp.type = LightType.Point;
            lightComp.color = game.Color;
            lightComp.intensity = 1f;
            lightComp.range = 10f;

            // Trigger collider
            var trigger = portalGroup.AddComponent<SphereCollider>();
            trigger.radius = portalSize / 2;
            trigger.isTrigger = true;

            // Store game data
            portalGroup.tag = "Portal";
            portalGroup.name = $"Portal_{game.Id}";

            // Add label
            var labelCanvas = new GameObject("LabelCanvas");
            labelCanvas.transform.SetParent(portalGroup.transform);
            labelCanvas.transform.position = Vector3.up * (portalSize / 2 + 1);
            
            var canvas = labelCanvas.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.WorldSpace;
            labelCanvas.AddComponent<CanvasRenderer>();
            labelCanvas.AddComponent<GraphicRaycaster>();

            var labelGo = new GameObject("LabelText");
            labelGo.transform.SetParent(labelCanvas.transform);
            var labelRect = labelGo.AddComponent<RectTransform>();
            labelRect.sizeDelta = new Vector2(10, 2);

            var labelText = labelGo.AddComponent<Text>();
            labelText.text = game.Name;
            labelText.fontSize = 32;
            labelText.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            labelText.color = Color.white;
            labelText.alignment = TextAnchor.MiddleCenter;

            // Particle effects
            var particles = portalGroup.AddComponent<ParticleSystem>();
            var main = particles.main;
            main.loop = true;
            main.playOnAwake = true;
            main.maxParticles = 50;
            main.startColor = game.Color;
            main.startSize = 0.2f;
            main.startLifetime = 2f;

            var emit = particles.emission;
            emit.rateOverTime = 10;

            var shape = particles.shape;
            shape.enabled = true;
            shape.type = ParticleSystemShapeType.Circle;
            shape.radius = portalSize / 2;

            particles.Play();

            Debug.Log($"[HubWorld] Created portal for {game.Name} at {position}");
            return portalGroup;
        }

        /// <summary>
        /// Handle portal trigger enter.
        /// </summary>
        private void OnTriggerEnter(Collider other)
        {
            if (other.CompareTag("Player") || other.gameObject == playerAvatar)
            {
                // Find which portal was triggered
                foreach (var kvp in portalMap)
                {
                    if (kvp.Value.collider != null && kvp.Value.collider == other)
                    {
                        EnterGame(kvp.Key);
                        break;
                    }
                }
            }
        }

        /// <summary>
        /// Enter a game world.
        /// </summary>
        public void EnterGame(string gameId)
        {
            var game = gameWorlds.Find(g => g.Id == gameId);
            if (game == null)
            {
                Debug.LogWarning($"[HubWorld] Game not found: {gameId}");
                return;
            }

            Debug.Log($"[HubWorld] Entering game: {game.Name}");
            OnEnterGame?.Invoke(gameId);

            // Show transition effect
            StartCoroutine(TransitionToGame(game));

            // Notify GameManager
            var gm = Core.GameManager.Instance;
            if (gm != null)
            {
                gm.SwitchGame(game.Name);
            }
        }

        /// <summary>
        /// Transition effect when entering a game.
        /// </summary>
        private System.Collections.IEnumerator TransitionToGame(GameWorld game)
        {
            // Fade to black
            var fadeCanvas = new GameObject("FadeCanvas");
            var canvas = fadeCanvas.AddComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            fadeCanvas.AddComponent<CanvasRenderer>();

            var fadeGo = new GameObject("FadeImage");
            fadeGo.transform.SetParent(fadeCanvas.transform);
            var fadeRect = fadeGo.AddComponent<RectTransform>();
            fadeRect.anchorMin = Vector2.zero;
            fadeRect.anchorMax = Vector2.one;
            fadeRect.sizeDelta = Vector2.zero;

            var fadeImage = fadeGo.AddComponent<Image>();
            fadeImage.color = new Color(0, 0, 0, 0);

            // Fade in
            float t = 0;
            while (t < 1)
            {
                t += Time.deltaTime * 2f;
                fadeImage.color = new Color(0, 0, 0, t);
                yield return null;
            }
            fadeImage.color = Color.black;

            yield return new WaitForSeconds(1f);

            // Show game loading dialog
            var dialog = DialogSystem.Instance;
            if (dialog != null)
            {
                dialog.ShowNotification($"Loading {game.Name}...", () =>
                {
                    Destroy(fadeCanvas);
                });
            }
        }

        /// <summary>
        /// Return to hub from a game.
        /// </summary>
        public void ReturnToHub()
        {
            Debug.Log("[HubWorld] Returning to hub");
            OnExitGame?.Invoke("");

            var menu = MenuManager.Instance;
            if (menu != null)
            {
                menu.SetMenuState(MenuManager.MenuState.Playing);
            }
        }

        /// <summary>
        /// Add a custom game world.
        /// </summary>
        public void AddGameWorld(GameWorld game)
        {
            if (!gameWorlds.Exists(g => g.Id == game.Id))
            {
                gameWorlds.Add(game);
                CreatePortals(); // Recreate all portals
                Debug.Log($"[HubWorld] Added game world: {game.Name}");
            }
        }

        /// <summary>
        /// Remove a game world.
        /// </summary>
        public void RemoveGameWorld(string gameId)
        {
            gameWorlds.RemoveAll(g => g.Id == gameId);
            CreatePortals();
            Debug.Log($"[HubWorld] Removed game world: {gameId}");
        }

        /// <summary>
        /// Get list of available game worlds.
        /// </summary>
        public List<GameWorld> GetGameWorlds()
        {
            return gameWorlds;
        }

        /// <summary>
        /// Update portal animations.
        /// </summary>
        void Update()
        {
            // Rotate portals
            foreach (var portal in portals)
            {
                if (portal != null)
                {
                    portal.transform.Rotate(Vector3.up, portalRotationSpeed * Time.deltaTime);
                }
            }

            // Avatar movement
            if (playerAvatar != null && avatarController != null)
            {
                float horizontal = Input.GetAxis("Horizontal");
                float vertical = Input.GetAxis("Vertical");

                var movement = new Vector3(horizontal, 0, vertical);
                if (movement.magnitude > 0.1f)
                {
                    avatarController.Move(movement * 5f * Time.deltaTime);
                    playerAvatar.transform.rotation = Quaternion.LookRotation(movement);
                }

                // Check portal proximity
                foreach (var kvp in portalMap)
                {
                    var distance = Vector3.Distance(playerAvatar.transform.position, kvp.Value.transform.position);
                    if (distance < portalSize)
                    {
                        // Show enter prompt
                        var dialog = DialogSystem.Instance;
                        if (dialog != null && !dialog.IsActive())
                        {
                            var game = gameWorlds.Find(g => g.Id == kvp.Key);
                            if (game != null)
                            {
                                // Could show hover tooltip here
                            }
                        }
                    }
                }

                // E key to enter nearby portal
                if (Input.GetKeyDown(KeyCode.E))
                {
                    foreach (var kvp in portalMap)
                    {
                        var distance = Vector3.Distance(playerAvatar.transform.position, kvp.Value.transform.position);
                        if (distance < portalSize)
                        {
                            EnterGame(kvp.Key);
                            break;
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Show or hide the hub world.
        /// </summary>
        public void SetVisible(bool visible)
        {
            if (hubEnvironment != null)
            {
                hubEnvironment.SetActive(visible);
            }
            if (playerAvatar != null)
            {
                playerAvatar.SetActive(visible);
            }
        }

        /// <summary>
        /// Clean up hub world.
        /// </summary>
        public void DestroyHub()
        {
            if (hubEnvironment != null)
            {
                Destroy(hubEnvironment);
            }
            if (playerAvatar != null)
            {
                Destroy(playerAvatar);
            }
            portals.Clear();
            portalMap.Clear();
            gameWorlds.Clear();
        }
    }
}
