using Godot;
using DoraMCU.Core;
using DoraMCU.UI;

namespace DoraMCU.Core
{
    /// <summary>
    /// Initializer — wires all UI panels to GameManager on scene load.
    /// Equivalent to Unity's DoraMCUInitializer.
    /// </summary>
    public partial class DoraMCUInitializer : Node
    {
        public override void _Ready()
        {
            GD.Print("[DoraMCUInitializer] Initializing game...");

            var mainScene = GetParent();
            if (mainScene == null) return;

            // Get GameManager autoload
            var gameManager = GetTree().Root.GetNodeOrNull("GameManager") as GameManager;
            if (gameManager == null)
            {
                GD.PrintErr("[DoraMCUInitializer] GameManager not found!");
                return;
            }

            // Find UI layer
            var uiLayer = mainScene.GetNodeOrNull("UILayer") as CanvasLayer;
            if (uiLayer == null)
            {
                GD.PrintErr("[DoraMCUInitializer] UILayer not found!");
                return;
            }

            // Get all UI panels
            var loginScreen = uiLayer.GetNodeOrNull("LoginScreen") as Control;
            var characterCreation = uiLayer.GetNodeOrNull("CharacterCreation") as Control;
            var menuManager = uiLayer.GetNodeOrNull("MenuManager") as Control;
            var mainHUD = uiLayer.GetNodeOrNull("MainHUD") as Control;
            var shipBuildUI = uiLayer.GetNodeOrNull("ShipBuildUI") as Control;
            var exploreAction = uiLayer.GetNodeOrNull("ExploreAction") as Control;
            var helpOverlay = uiLayer.GetNodeOrNull("HelpOverlay") as Control;
            var inventoryUI = uiLayer.GetNodeOrNull("InventoryUI") as Control;
            var mapUI = uiLayer.GetNodeOrNull("MapUI") as Control;

            // Wire all panels to GameManager
            gameManager.SetLoginScreen(loginScreen);
            gameManager.SetCharacterCreation(characterCreation);
            gameManager.SetMainHUD(mainHUD);
            gameManager.SetMenuManager(menuManager);
            gameManager.SetShipBuildUI(shipBuildUI);
            gameManager.SetExploreAction(exploreAction);
            gameManager.SetHelpOverlay(helpOverlay);
            gameManager.SetInventoryUI(inventoryUI);
            gameManager.SetMapUI(mapUI);

            GD.Print("[DoraMCUInitializer] All UI panels wired to GameManager");
            GD.Print("[DoraMCUInitializer] Game initialization complete");
        }
    }
}
