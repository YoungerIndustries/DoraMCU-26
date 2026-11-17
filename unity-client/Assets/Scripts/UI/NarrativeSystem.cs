using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using UnityEngine.UI;

namespace DoraMCU.Client.UI
{
    /// <summary>
    /// 3D Narrative system for AI-driven story events.
    /// Displays narrative events as 3D cutscenes with character emotions via animations.
    /// Integrates with DialogSystem for text and voice-activated 3D commands.
    /// </summary>
    public class NarrativeSystem : Singleton<NarrativeSystem>
    {
        [Header("Narrative Settings")]
        [SerializeField] private float cutsceneDuration = 5f;
        [SerializeField] private float transitionSpeed = 2f;
        [SerializeField] private bool enableVoiceCommands = true;

        [Header("Cutscene Camera")]
        [SerializeField] private Camera cutsceneCamera;
        private Camera mainCamera;
        private Vector3 originalCameraPosition;
        private Quaternion originalCameraRotation;

        [Header("Narrative Events")]
        private Queue<NarrativeEvent> eventQueue = new();
        private NarrativeEvent currentEvent;
        private bool isPlayingCutscene;
        private Coroutine cutsceneCoroutine;

        [Header("3D Effects")]
        private ParticleSystem[] activeParticles;
        private Light[] narrativeLights;

        /// <summary>
        /// Represents a narrative event.
        /// </summary>
        public class NarrativeEvent
        {
            public string Id;
            public string Title;
            public string Description;
            public EventType Type;
            public Vector3 Position;
            public Color EffectColor;
            public string CharacterName;
            public string Dialog;
            public float Duration;
            public Dictionary<string, object> Data;

            public enum EventType
            {
                Combat,
                Discovery,
                Diplomacy,
                Trade,
                Exploration,
                Emergency,
                Victory,
                Defeat
            }

            public NarrativeEvent(string id, string title, string description, EventType type)
            {
                Id = id;
                Title = title;
                Description = description;
                Type = type;
                EffectColor = GetEventTypeColor(type);
                Duration = 5f;
                Data = new Dictionary<string, object>();
            }

            private static Color GetEventTypeColor(EventType type)
            {
                switch (type)
                {
                    case EventType.Combat: return Color.red;
                    case EventType.Discovery: return Color.cyan;
                    case EventType.Diplomacy: return Color.green;
                    case EventType.Trade: return Color.yellow;
                    case EventType.Exploration: return Color.blue;
                    case EventType.Emergency: return new Color(1f, 0.3f, 0f);
                    case EventType.Victory: return Color.magenta;
                    case EventType.Defeat: return Color.gray;
                    default: return Color.white;
                }
            }
        }

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            mainCamera = Camera.main;
            if (mainCamera != null)
            {
                originalCameraPosition = mainCamera.transform.position;
                originalCameraRotation = mainCamera.transform.rotation;
            }
        }

        /// <summary>
        /// Queue a narrative event for display.
        /// </summary>
        public void QueueEvent(NarrativeEvent @event)
        {
            if (@event != null)
            {
                eventQueue.Enqueue(@event);
                Debug.Log($"[NarrativeSystem] Queued event: {@event.Title}");

                if (!isPlayingCutscene)
                {
                    PlayNextEvent();
                }
            }
        }

        /// <summary>
        /// Queue a combat event.
        /// </summary>
        public void QueueCombatEvent(string title, string description, Vector3 position = default)
        {
            QueueEvent(new NarrativeEvent(
                $"combat_{System.DateTime.Now.Ticks}",
                title,
                description,
                NarrativeEvent.EventType.Combat
            ) { Position = position });
        }

        /// <summary>
        /// Queue a discovery event.
        /// </summary>
        public void QueueDiscoveryEvent(string title, string description, Vector3 position = default)
        {
            QueueEvent(new NarrativeEvent(
                $"discovery_{System.DateTime.Now.Ticks}",
                title,
                description,
                NarrativeEvent.EventType.Discovery
            ) { Position = position });
        }

        /// <summary>
        /// Queue an emergency event.
        /// </summary>
        public void QueueEmergencyEvent(string title, string description)
        {
            var @event = new NarrativeEvent(
                $"emergency_{System.DateTime.Now.Ticks}",
                title,
                description,
                NarrativeEvent.EventType.Emergency
            ) { Duration = 8f };
            
            QueueEvent(@event);

            // Trigger red alert
            var hud = MainHUD.Instance;
            if (hud != null)
            {
                // Flash connection status red
            }
        }

        /// <summary>
        /// Play the next queued event.
        /// </summary>
        private void PlayNextEvent()
        {
            if (eventQueue.Count == 0)
            {
                isPlayingCutscene = false;
                return;
            }

            isPlayingCutscene = true;
            currentEvent = eventQueue.Dequeue();

            if (cutsceneCoroutine != null)
            {
                StopCoroutine(cutsceneCoroutine);
            }

            cutsceneCoroutine = StartCoroutine(PlayCutscene(currentEvent));
        }

        /// <summary>
        /// Play a cutscene for the given event.
        /// </summary>
        private System.Collections.IEnumerator PlayCutscene(NarrativeEvent @event)
        {
            // Pause game
            var menu = MenuManager.Instance;
            if (menu != null)
            {
                menu.PauseGame();
            }

            // Move camera to event position
            if (mainCamera != null && @event.Position != Vector3.zero)
            {
                var targetPos = @event.Position + new Vector3(0, 5, 10);
                var startPos = mainCamera.transform.position;
                var startRot = mainCamera.transform.rotation;
                var targetRot = Quaternion.LookRotation(@event.Position - targetPos);

                float t = 0;
                while (t < 1)
                {
                    t += Time.deltaTime * transitionSpeed;
                    mainCamera.transform.position = Vector3.Lerp(startPos, targetPos, t);
                    mainCamera.transform.rotation = Quaternion.Slerp(startRot, targetRot, t);
                    yield return null;
                }
                mainCamera.transform.position = targetPos;
                mainCamera.transform.rotation = targetRot;
            }

            // Create 3D effects
            CreateEventEffects(@event);

            // Show dialog
            var dialog = DialogSystem.Instance;
            if (dialog != null && !string.IsNullOrEmpty(@event.Dialog))
            {
                dialog.ShowDialog(@event.CharacterName ?? "Narrator", @event.Dialog);
            }
            else if (dialog != null)
            {
                dialog.ShowDialog("System", $"*** {@event.Title} ***\n\n{@event.Description}", 
                    null, new List<DialogSystem.DialogResponse>
                    {
                        new DialogSystem.DialogResponse("Continue", () => dialog.HideDialog())
                    });
            }

            // Wait for event duration
            yield return new WaitForSeconds(@event.Duration);

            // Clean up effects
            CleanupEffects();

            // Restore camera
            if (mainCamera != null)
            {
                t = 0;
                var startPos = mainCamera.transform.position;
                var startRot = mainCamera.transform.rotation;
                while (t < 1)
                {
                    t += Time.deltaTime * transitionSpeed;
                    mainCamera.transform.position = Vector3.Lerp(startPos, originalCameraPosition, t);
                    mainCamera.transform.rotation = Quaternion.Slerp(startRot, originalCameraRotation, t);
                    yield return null;
                }
                mainCamera.transform.position = originalCameraPosition;
                mainCamera.transform.rotation = originalCameraRotation;
            }

            // Resume game
            if (menu != null)
            {
                menu.ResumeGame();
            }

            currentEvent = null;
            isPlayingCutscene = false;

            // Play next event if any
            PlayNextEvent();
        }

        /// <summary>
        /// Create 3D effects for the event.
        /// </summary>
        private void CreateEventEffects(NarrativeEvent @event)
        {
            // Create particle system
            var particleGo = new GameObject($"NarrativeParticles_{@event.Id}");
            particleGo.transform.position = @event.Position;
            
            var ps = particleGo.AddComponent<ParticleSystem>();
            var main = ps.main;
            main.duration = @event.Duration;
            main.loop = false;
            main.playOnAwake = true;
            main.maxParticles = 100;
            main.startColor = @event.EffectColor;
            main.startSize = 0.5f;
            main.startLifetime = 2f;

            var emit = ps.emission;
            emit.enabled = true;
            emit.rateOverTime = 20;

            ps.Play();
            activeParticles = new[] { ps };

            // Create narrative light
            var lightGo = new GameObject($"NarrativeLight_{@event.Id}");
            lightGo.transform.position = @event.Position + Vector3.up * 5;
            
            var light = lightGo.AddComponent<Light>();
            light.type = LightType.Point;
            light.color = @event.EffectColor;
            light.intensity = 2;
            light.range = 20;

            narrativeLights = new[] { light };

            // Flash effect for emergency events
            if (@event.Type == NarrativeEvent.EventType.Emergency)
            {
                StartCoroutine(FlashEmergencyEffect(@event.Duration));
            }
        }

        /// <summary>
        /// Flash emergency effect.
        /// </summary>
        private System.Collections.IEnumerator FlashEmergencyEffect(float duration)
        {
            float elapsed = 0;
            while (elapsed < duration)
            {
                elapsed += Time.deltaTime;
                
                // Flash red
                if (narrativeLights.Length > 0)
                {
                    narrativeLights[0].intensity = Mathf.Sin(elapsed * 5) * 2 + 2;
                }

                yield return null;
            }
        }

        /// <summary>
        /// Clean up narrative effects.
        /// </summary>
        private void CleanupEffects()
        {
            if (activeParticles != null)
            {
                foreach (var ps in activeParticles)
                {
                    if (ps != null)
                    {
                        GameObject.Destroy(ps.gameObject);
                    }
                }
                activeParticles = null;
            }

            if (narrativeLights != null)
            {
                foreach (var light in narrativeLights)
                {
                    if (light != null)
                    {
                        GameObject.Destroy(light.gameObject);
                    }
                }
                narrativeLights = null;
            }
        }

        /// <summary>
        /// Check for voice commands.
        /// </summary>
        void Update()
        {
            if (!enableVoiceCommands) return;

            // Check for voice command input
            if (Input.GetKeyDown(KeyCode.V))
            {
                ProcessVoiceCommand("help");
            }
        }

        /// <summary>
        /// Process a voice command.
        /// </summary>
        public void ProcessVoiceCommand(string command)
        {
            var cmd = command.ToLower().Trim();

            switch (cmd)
            {
                case "help":
                    Debug.Log("[NarrativeSystem] Available commands: scan, attack, defend, explore, trade");
                    break;

                case "scan":
                    var input = Input.InputHandler.Instance;
                    if (input != null) input.OnScan?.Invoke();
                    break;

                case "attack":
                case "fire":
                    input = Input.InputHandler.Instance;
                    if (input != null) input.OnFire?.Invoke();
                    break;

                case "defend":
                case "defend position":
                    QueueEvent(new NarrativeEvent(
                        "defend_cmd",
                        "Defensive Position",
                        "Orders received: hold position and defend.",
                        NarrativeEvent.EventType.Combat
                    ));
                    break;

                case "explore":
                    QueueEvent(new NarrativeEvent(
                        "explore_cmd",
                        "Exploration Ordered",
                        "Scanning nearby sectors for points of interest.",
                        NarrativeEvent.EventType.Exploration
                    ));
                    break;

                case "trade":
                    QueueEvent(new NarrativeEvent(
                        "trade_cmd",
                        "Trade Opportunity",
                        "Searching for nearby trading partners.",
                        NarrativeEvent.EventType.Trade
                    ));
                    break;
            }
        }

        /// <summary>
        /// Skip current cutscene.
        /// </summary>
        public void SkipCutscene()
        {
            if (isPlayingCutscene && cutsceneCoroutine != null)
            {
                StopCoroutine(cutsceneCoroutine);
                CleanupEffects();
                isPlayingCutscene = false;
                currentEvent = null;

                var dialog = DialogSystem.Instance;
                if (dialog != null && dialog.IsActive())
                {
                    dialog.HideDialog();
                }

                var menu = MenuManager.Instance;
                if (menu != null && menu.IsPaused())
                {
                    menu.ResumeGame();
                }
            }
        }

        /// <summary>
        /// Check if a cutscene is currently playing.
        /// </summary>
        public bool IsPlaying()
        {
            return isPlayingCutscene;
        }

        /// <summary>
        /// Get the number of queued events.
        /// </summary>
        public int QueuedEventsCount()
        {
            return eventQueue.Count;
        }

        /// <summary>
        /// Clear all queued events.
        /// </summary>
        public void ClearQueue()
        {
            eventQueue.Clear();
        }
    }
}
