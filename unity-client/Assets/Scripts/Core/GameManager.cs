using System.Collections;
using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;
using DoraMCU.Client.Input;
using DoraMCU.Client.UI;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Thin game coordinator — delegates scene rendering to SceneRenderer,
    /// network calls to NetworkManager, and input to InputHandler.
    /// </summary>
    public class GameManager : Singleton<GameManager>
    {
        [Header("Scene References")]
        [SerializeField] private Transform solarSystemParent;
        [SerializeField] private Transform fleetParent;
        [SerializeField] private Transform stationParent;

        [Header("Sync Settings")]
        [SerializeField] private float stateSyncInterval = 5f;

        private SceneRenderer sceneRenderer;
        private InterestManager interestManager;

        private enum GameMode { Space, Battlefield }
        private GameMode currentMode = GameMode.Space;

        private NetworkManager.GameState currentState;
        private Coroutine syncCoroutine;
        private bool isAuthenticated = false;

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            interestManager = new InterestManager();
            sceneRenderer = new SceneRenderer();

            // Setup parent transforms
            if (solarSystemParent == null)
            {
                var go = new GameObject("SolarSystems");
                solarSystemParent = go.transform;
            }
            if (fleetParent == null)
            {
                var fleetGo = new GameObject("Fleets");
                fleetParent = fleetGo.transform;
            }
            if (stationParent == null)
            {
                var stationGo = new GameObject("Stations");
                stationParent = stationGo.transform;
            }

            sceneRenderer.Initialize(solarSystemParent, fleetParent, stationParent);
        }

        void Start()
        {
            // Subscribe to network events
            NetworkManager.Instance.OnConnected += OnConnected;
            NetworkManager.Instance.OnStateSync += OnStateSync;
            NetworkManager.Instance.OnError += OnError;

            // Subscribe to input events
            InputHandler.Instance.OnNavigateToSystem += OnNavigateToSystem;
            InputHandler.Instance.OnScan += OnScan;
            InputHandler.Instance.OnNavigateToBattlefield += OnNavigateToBattlefield;

            // Subscribe to UI events
            var explore = ExploreAction.Instance;
            if (explore != null)
            {
                explore.OnSectorScanned += OnSectorScanned;
            }

            var shipBuild = ShipBuildUI.Instance;
            if (shipBuild != null)
            {
                shipBuild.OnShipBuilt += OnShipBuilt;
            }
        }

        void Update()
        {
            // Keyboard shortcuts for gameplay actions
            if (UnityEngine.Input.GetKeyDown(KeyCode.V))
            {
                var sb = ShipBuildUI.Instance;
                if (sb != null) sb.Toggle();
            }
            if (UnityEngine.Input.GetKeyDown(KeyCode.X))
            {
                var ea = ExploreAction.Instance;
                if (ea != null) ea.ScanCurrentSector();
            }
            if (UnityEngine.Input.GetKeyDown(KeyCode.Tab))
            {
                var map = MapUI.Instance;
                if (map != null)
                {
                    bool isVisible = map.gameObject != null && map.gameObject.activeSelf;
                    map.SetVisible(!isVisible);
                }
            }
            if (UnityEngine.Input.GetKeyDown(KeyCode.I))
            {
                var inv = InventoryUI.Instance;
                if (inv != null)
                {
                    bool isVisible = inv.gameObject != null && inv.gameObject.activeSelf;
                    inv.SetVisible(!isVisible);
                }
            }
        }

        void OnDestroy()
        {
            if (syncCoroutine != null)
            {
                StopCoroutine(syncCoroutine);
            }
        }

        /// <summary>
        /// Handle connection to server.
        /// </summary>
        private void OnConnected(string sessionId)
        {
            Debug.Log($"[GameManager] Connected: {sessionId}");
            FetchInitialState();
        }

        /// <summary>
        /// Start playing after character creation or main menu.
        /// </summary>
        public void StartPlaying()
        {
            Debug.Log("[GameManager] Starting gameplay");

            var hud = MainHUD.Instance;
            if (hud != null)
            {
                hud.SetVisible(true);
            }

            FetchInitialState();

            var nm = NetworkManager.Instance;
            if (nm != null)
            {
                nm.StartAiAdminPolling();
            }
        }

        /// <summary>
        /// Called after successful login to enable sync loop.
        /// </summary>
        public void SetAuthenticated(bool authenticated)
        {
            isAuthenticated = authenticated;
            if (authenticated && syncCoroutine == null)
            {
                syncCoroutine = StartCoroutine(SyncLoop());
            }
        }

        /// <summary>
        /// Handle state synchronization from server.
        /// </summary>
        private void OnStateSync(string sessionId, NetworkManager.GameState state)
        {
            currentState = state;
            sceneRenderer.UpdateSceneFromState(state);
        }

        /// <summary>
        /// Handle connection error.
        /// </summary>
        private void OnError(string error)
        {
            Debug.LogError($"[GameManager] Error: {error}");
        }

        /// <summary>
        /// Fetch initial game state from server.
        /// </summary>
        private void FetchInitialState()
        {
            NetworkManager.Instance.StartCoroutine(FetchStateCoroutine());
        }

        private IEnumerator FetchStateCoroutine()
        {
            yield return NetworkManager.Instance.FetchGameState((state) =>
            {
                currentState = state;
                sceneRenderer.UpdateSceneFromState(state);
            });
        }

        /// <summary>
        /// Periodic state sync loop — only runs after authentication.
        /// </summary>
        private IEnumerator SyncLoop()
        {
            while (true)
            {
                yield return new WaitForSeconds(stateSyncInterval);
                if (isAuthenticated)
                {
                    FetchInitialState();
                }
            }
        }

        /// <summary>
        /// Handle navigation to a solar system.
        /// </summary>
        private void OnNavigateToSystem(int sectorRow, int sectorCol, int subSectorRow, int subSectorCol)
        {
            Debug.Log($"[GameManager] Navigating to {sectorRow},{sectorCol},{subSectorRow},{subSectorCol}");
            var ea = ExploreAction.Instance;
            if (ea != null)
            {
                ea.SetCurrentSector(sectorRow, sectorCol);
            }
        }

        /// <summary>
        /// Handle scan action.
        /// </summary>
        private void OnScan()
        {
            Debug.Log("[GameManager] Scanning...");
            var ea = ExploreAction.Instance;
            if (ea != null)
            {
                ea.ScanCurrentSector();
            }
        }

        /// <summary>
        /// Handle sector scanned event.
        /// </summary>
        private void OnSectorScanned(int row, int col)
        {
            Debug.Log($"[GameManager] Sector {row},{col} scanned, refreshing game state");
            FetchInitialState();
        }

        /// <summary>
        /// Handle ship built event.
        /// </summary>
        private void OnShipBuilt(string shipType)
        {
            Debug.Log($"[GameManager] Ship built: {shipType}, refreshing game state");
            FetchInitialState();
        }

        /// <summary>
        /// Handle navigation to a battlefield.
        /// </summary>
        private void OnNavigateToBattlefield(long locationId, string timePeriod, string terrain)
        {
            Debug.Log($"[GameManager] Entering battlefield mode: location={locationId}, period={timePeriod}, terrain={terrain}");
            EnterBattlefieldMode(locationId, timePeriod, terrain);
        }

        /// <summary>
        /// Enter battlefield mode and load a battlefield.
        /// </summary>
        private void EnterBattlefieldMode(long locationId, string timePeriod, string terrain)
        {
            currentMode = GameMode.Battlefield;
            Cursor.lockState = CursorLockMode.None;
            Cursor.visible = true;
            Debug.Log("[GameManager] Battlefield mode active (renderer disabled)");
        }

        /// <summary>
        /// Exit battlefield mode and return to space view.
        /// </summary>
        public void ExitBattlefieldMode()
        {
            if (currentMode == GameMode.Battlefield)
            {
                currentMode = GameMode.Space;
                Cursor.lockState = CursorLockMode.Locked;
                Cursor.visible = false;
                Debug.Log("[GameManager] Returned to space mode");
            }
        }

        public NetworkManager.GameState CurrentState => currentState;

        /// <summary>
        /// Switch to a different game system.
        /// </summary>
        public void SwitchGame(string gameName)
        {
            Debug.Log($"[GameManager] Switching to game: {gameName}");
            var nm = NetworkManager.Instance;
            if (nm != null && nm.IsConnected)
            {
                nm.SendRequest($"/game/switch?game={gameName}", null);
            }
            FetchInitialState();
        }

        /// <summary>
        /// Enter battlefield mode from Electron client command.
        /// </summary>
        public void EnterBattlefieldFromElectron(long locationId, string timePeriod, string terrain)
        {
            EnterBattlefieldMode(locationId, timePeriod, terrain);
        }
    }
}
