/**
 * DoraMCU Game UI - BATTLE MANAGEMENT Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';

    // BATTLE MANAGEMENT (called from battle card onclick handlers)

    async function reinforceBattle(battleId) {
        if (!GameUIState.sessionId) return;
        // Find a fleet to reinforce with
        const fleetId = GameUIState.selectedFleet || (GameUIState.gameState?.fleets?.[0]?.id);
        if (!fleetId) {
            GameUI.showToast('Select a fleet first to reinforce with', 'error');
            return;
        }
        try {
            const response = await GameAPI.post('/api/v1/4x/sessions/' + GameUIState.sessionId + '/battles/' + battleId + '/reinforce',
                { fleetId });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.showToast('Battle reinforced!', 'success');
            // Refresh battle modal
            handleViewBattlesClick();
        } catch (err) {
            GameUI.showToast('Failed to reinforce: ' + err.message, 'error');
        }
    }

    async function retreatBattle(battleId) {
        if (!GameUIState.sessionId) return;
        try {
            const response = await GameAPI.post('/api/v1/4x/sessions/' + GameUIState.sessionId + '/battles/' + battleId + '/retreat');
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.showToast('Retreated from battle!', 'success');
            // Refresh battle modal
            handleViewBattlesClick();
        } catch (err) {
            GameUI.showToast('Failed to retreat: ' + err.message, 'error');
        }
    }

    // Register public functions on GameUI
    GameUI.reinforceBattle = reinforceBattle;
    GameUI.retreatBattle = retreatBattle;
}());
