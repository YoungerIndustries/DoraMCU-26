using Godot;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace DoraMCU.Core
{
    /// <summary>
    /// Main game manager - equivalent to Unity's GameManager.
    /// Manages game state, connects to backend server, and initializes systems.
    /// </summary>
    public partial class GameManager : Node
    {
        [Export]
        public string ServerUrl = "http://localhost:8080";

        private bool _isConnected;
        private bool _demoMode;

        public bool IsConnected => _isConnected;
        public bool DemoMode => _demoMode;

        public override void _Ready()
        {
            GD.Print("[GameManager] Initializing...");
            ConnectToServer();
        }

        public override void _Process(double delta)
        {
            if (!_isConnected && !_demoMode)
            {
                // Try to reconnect
            }
        }

        private async void ConnectToServer()
        {
            try
            {
                using var client = new HttpClient();
                client.Timeout = TimeSpan.FromSeconds(5);
                
                var response = await client.GetAsync($"{ServerUrl}/api/status");
                if (response.IsSuccessStatusCode)
                {
                    _isConnected = true;
                    GD.Print("[GameManager] Connected to backend server");
                }
                else
                {
                    GD.PrintErr("[GameManager] Server not available, starting demo mode");
                    StartDemoMode();
                }
            }
            catch (Exception e)
            {
                GD.PrintErr($"[GameManager] Connection failed: {e.Message}");
                StartDemoMode();
            }
        }

        public void StartDemoMode()
        {
            _demoMode = true;
            GD.Print("[GameManager] Demo mode activated");
            
            // Find and start DemoModeManager
            var root = GetTree().Root;
            var mainScene = root.GetNode("MainScene");
            if (mainScene != null)
            {
                var demoManager = mainScene.GetNodeOrNull("DemoModeManager");
                if (demoManager is DoraMCU.Core.DemoModeManager demo)
                {
                    demo.StartDemo();
                }
            }
        }
    }
}
