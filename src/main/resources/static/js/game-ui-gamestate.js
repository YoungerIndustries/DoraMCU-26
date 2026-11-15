/**
 * DoraMCU Game UI - GAME STATE Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';


    // Connection status indicator
    let connectionStatusEl = null;
    let connectionStatusTextEl = null;
    let connectionProgressBarEl = null;
    let connectionErrorEl = null;
    let retryConnectionBtnEl = null;
    let backFromConnectionBtnEl = null;

    function showConnectionStatus(message, progress) {
        if (!connectionStatusEl) {
            connectionStatusEl = document.getElementById('connectionStatus');
            connectionStatusTextEl = document.getElementById('connectionStatusText');
            connectionProgressBarEl = document.getElementById('connectionProgressBar');
            connectionErrorEl = document.getElementById('connectionError');
            retryConnectionBtnEl = document.getElementById('retryConnectionBtn');
            backFromConnectionBtnEl = document.getElementById('backFromConnectionBtn');
        }
        if (connectionStatusEl) connectionStatusEl.style.display = 'flex';
        if (connectionStatusTextEl) connectionStatusTextEl.textContent = message || 'Connecting...';
        if (connectionProgressBarEl && progress !== undefined) connectionProgressBarEl.style.width = progress + '%';
        if (connectionErrorEl) connectionErrorEl.style.display = 'none';
        if (retryConnectionBtnEl) retryConnectionBtnEl.style.display = 'none';
        if (backFromConnectionBtnEl) backFromConnectionBtnEl.style.display = 'none';
    }

    function hideConnectionStatus() {
        if (connectionStatusEl) connectionStatusEl.style.display = 'none';
    }

    function showConnectionError(message, retryCallback) {
        // Don't call showConnectionStatus() here — keep the overlay visible
        if (connectionStatusTextEl) connectionStatusTextEl.textContent = 'Connection Failed';
        if (connectionProgressBarEl) connectionProgressBarEl.style.width = '100%';
        if (connectionErrorEl) {
            connectionErrorEl.textContent = message;
            connectionErrorEl.style.display = 'block';
        }
        if (retryConnectionBtnEl) {
            retryConnectionBtnEl.style.display = 'inline-block';
            retryConnectionBtnEl.onclick = function() {
                hideConnectionStatus();
                if (retryCallback) retryCallback();
            };
        }
        // Always show the Go Back button so the user can escape
        if (backFromConnectionBtnEl) {
            backFromConnectionBtnEl.style.display = 'inline-block';
            backFromConnectionBtnEl.onclick = function() {
                hideConnectionStatus();
                // Clear session and redirect to DNN News page
                localStorage.removeItem('doramcu_session_id');
                GameUIState.sessionId = null;
                window.location.href = 'index.html';
            };
        }
    }

    async function loadGameState() {
        if (!GameUIState.sessionId) return;
        showConnectionStatus('Loading game data...', 20);
        try {
            const state = await GameAPI.getGameState(GameUIState.sessionId, true);
            showConnectionStatus('Rendering galaxy...', 70);
            if (state) {
                GameUIState.gameState = state;
                renderGameState();
                loadGameModes();
                hideConnectionStatus();
                // If subSectors are still empty, poll for up to 30 seconds (galaxy may be generating)
                if (!state.subSectors || state.subSectors.length === 0) {
                    console.log('SubSectors empty, polling for galaxy generation...');
                    startSubsectorPolling();
                } else {
                    console.log('SubSectors loaded:', state.subSectors.length, 'subsectors');
                    stopSubsectorPolling();
                }
            }
        } catch (err) {
            console.error('Failed to load game state:', err);
            hideConnectionStatus();
            if (err.timeout) {
                showConnectionError('Request timed out. The server may be slow. Please try again.', function() { loadGameState(); });
            } else if (err.message && err.message.includes('Failed to fetch')) {
                showConnectionError('Cannot reach the server. Make sure the game server is running on port 8080.', function() { loadGameState(); });
            } else if (err.message && err.message.includes('Session not found')) {
                // Session was deleted (e.g., server restart with in-memory DB) Ã¢â‚¬â€ refresh via ensureSession
                console.warn('[DEV] Session not found, refreshing session...');
                localStorage.removeItem('doramcu_session_id');
                GameUIState.sessionId = null;
                showConnectionError('Session expired. Refreshing connection...', function() { checkAuth(); });
            } else {
                showConnectionError('Failed to load game data: ' + err.message, function() { loadGameState(); });
            }
            addEventLog('ERROR', 'Failed to load game state: ' + err.message);
        }
    }

    function startSubsectorPolling() {
        stopSubsectorPolling();
        let attempts = 0;
        const maxAttempts = 30; // Poll for up to 30 seconds
        GameUIState.gameStatePollTimer = setInterval(async () => {
            attempts++;
            if (attempts >= maxAttempts) {
                stopSubsectorPolling();
                return;
            }
            try {
                const state = await GameAPI.getGameState(GameUIState.sessionId);
                if (state && state.subSectors && state.subSectors.length > 0) {
                    console.log('SubSectors now available:', state.subSectors.length, 'subsectors');
                    GameUIState.gameState = state;
                    renderGameState();
                    stopSubsectorPolling();
                }
            } catch (err) {
                console.error('Polling error:', err);
            }
        }, 1000);
    }

    function stopSubsectorPolling() {
        if (GameUIState.gameStatePollTimer) {
            clearInterval(GameUIState.gameStatePollTimer);
            GameUIState.gameStatePollTimer = null;
        }
    }

    function renderGameState() {
        if (!GameUIState.gameState) return;

        // Turn number
        if (GameUIState.elements.turnNumber) GameUIState.elements.turnNumber.textContent = GameUIState.gameState.currentTurn;
        if (GameUIState.elements.currentTurn) {
            // Keep the icon, update the number span
            const turnNumEl = GameUIState.elements.currentTurn.querySelector('#turnNumber');
            if (turnNumEl) turnNumEl.textContent = GameUIState.gameState.currentTurn;
        }

        // Simultaneous turns: all players can act at any time
        if (GameUIState.elements.currentPlayerLabel) GameUIState.elements.currentPlayerLabel.textContent = 'All Players';
        if (GameUIState.elements.currentPlayerName) {
            const labelEl = GameUIState.elements.currentPlayerName.querySelector('#currentPlayerLabel');
            if (labelEl) labelEl.textContent = 'All Players';
        }

        // Turn indicator - always active for simultaneous turns
        if (GameUIState.elements.turnIndicator) {
            if (GameUIState.elements.turnIndicatorText) GameUIState.elements.turnIndicatorText.textContent = 'ALL PLAYERS ACTIVE';
            GameUIState.elements.turnIndicator.className = 'turn-indicator my-turn';
        }

        // Turn timer and progress bar
        if (GameUIState.gameState.schedulerEnabled && GameUIState.gameState.turnIntervalSeconds > 0) {
            GameUIState.turnTimerTotal = GameUIState.gameState.turnIntervalSeconds;
            GameUIState.turnTimerRemaining = Math.max(0, GameUIState.turnTimerTotal - GameUIState.gameState.accumulatedSeconds);
            updateTurnTimerDisplay();
            updateTurnProgressBar();
            updateTurnIntervalDisplay();
            updateSchedulerStatus();
            startTurnTimerCountdown();
        } else {
            stopTurnTimerCountdown();
            if (GameUIState.elements.turnTimerValue) GameUIState.elements.turnTimerValue.textContent = '--';
            if (GameUIState.elements.turnProgressFill) GameUIState.elements.turnProgressFill.style.width = '0%';
        }

        // Resources Ã¢â‚¬â€ use pipeline data if available, otherwise fall back to player resources
        const myPlayer = GameUIState.gameState.players?.find(p => p.id === getCurrentPlayerId());
        const pipeline = GameUIState.gameState.pipelineSummary;
        if (pipeline) {
            // Pipeline modules displayed as resources
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            if (GameUIState.elements.resourceMinerals) GameUIState.elements.resourceMinerals.textContent = modules.HULL_MODULE || 0;
            if (GameUIState.elements.resourceEnergy) GameUIState.elements.resourceEnergy.textContent = modules.WEAPON_MODULE || 0;
            if (GameUIState.elements.resourceInfluence) GameUIState.elements.resourceInfluence.textContent = modules.SHIELD_MODULE || 0;
            if (GameUIState.elements.resourceScience) GameUIState.elements.resourceScience.textContent = modules.ENGINE_MODULE || 0;
            if (GameUIState.elements.resourceFood) GameUIState.elements.resourceFood.textContent = modules.FACILITY_MODULE || 0;
            // Update resource change indicators with mining rates
            const mining = pipeline.miningRate || {};
            if (GameUIState.elements.resourceMineralsChange) {
                GameUIState.elements.resourceMineralsChange.textContent = '+' + (mining.ORE || 0);
                GameUIState.elements.resourceMineralsChange.className = 'resource-change positive';
            }
            if (GameUIState.elements.resourceEnergyChange) {
                GameUIState.elements.resourceEnergyChange.textContent = '+' + (mining.GAS || 0);
                GameUIState.elements.resourceEnergyChange.className = 'resource-change positive';
            }
            if (GameUIState.elements.resourceInfluenceChange) {
                GameUIState.elements.resourceInfluenceChange.textContent = pipeline.activeProcessing + '/' + pipeline.miningOperations;
                GameUIState.elements.resourceInfluenceChange.className = 'resource-change';
            }
            if (GameUIState.elements.resourceScienceChange) {
                GameUIState.elements.resourceScienceChange.textContent = pipeline.constructionQueueSize + ' queue';
                GameUIState.elements.resourceScienceChange.className = 'resource-change';
            }
            if (GameUIState.elements.resourceFoodChange) {
                GameUIState.elements.resourceFoodChange.textContent = pipeline.activeAssemblers + ' assemblers';
                GameUIState.elements.resourceFoodChange.className = 'resource-change';
            }
        } else if (myPlayer && myPlayer.resources) {
            // Fallback: show pipeline resources from player summary
            if (GameUIState.elements.resourceMinerals) GameUIState.elements.resourceMinerals.textContent = myPlayer.resources.HULL_MODULE || myPlayer.resources.ORE || 0;
            if (GameUIState.elements.resourceEnergy) GameUIState.elements.resourceEnergy.textContent = myPlayer.resources.WEAPON_MODULE || myPlayer.resources.GAS || 0;
            if (GameUIState.elements.resourceInfluence) GameUIState.elements.resourceInfluence.textContent = myPlayer.resources.SHIELD_MODULE || 0;
            if (GameUIState.elements.resourceScience) GameUIState.elements.resourceScience.textContent = myPlayer.resources.ENGINE_MODULE || 0;
            if (GameUIState.elements.resourceFood) GameUIState.elements.resourceFood.textContent = myPlayer.resources.FACILITY_MODULE || 0;
        }

        // Empire Info Panel
        const empire = GameUIState.gameState.empireSummary;
        if (empire) {
            if (GameUIState.elements.empireName) GameUIState.elements.empireName.textContent = empire.empireName || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empireGovernment) GameUIState.elements.empireGovernment.textContent = empire.governmentType || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empireEthos) GameUIState.elements.empireEthos.textContent = empire.ethos || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empirePlanets) GameUIState.elements.empirePlanets.textContent = empire.planetCount || 0;
            if (GameUIState.elements.empireFleets) GameUIState.elements.empireFleets.textContent = empire.fleetCount || 0;
            if (GameUIState.elements.empireCapital) GameUIState.elements.empireCapital.textContent = empire.capitalPlanetId != null ? 'Planet #' + empire.capitalPlanetId : 'Ã¢â‚¬â€';

            // Location information
            if (GameUIState.elements.empireSector) {
                if (empire.capitalSectorRow != null && empire.capitalSectorCol != null) {
                    GameUIState.elements.empireSector.textContent = '(' + empire.capitalSectorRow + ', ' + empire.capitalSectorCol + ')';
                } else {
                    GameUIState.elements.empireSector.textContent = 'Ã¢â‚¬â€';
                }
            }
            if (GameUIState.elements.empireSubSector) {
                if (empire.capitalSubSectorRow != null && empire.capitalSubSectorCol != null) {
                    GameUIState.elements.empireSubSector.textContent = '(' + empire.capitalSubSectorRow + ', ' + empire.capitalSubSectorCol + ')';
                } else {
                    GameUIState.elements.empireSubSector.textContent = 'Ã¢â‚¬â€';
                }
            }
            if (GameUIState.elements.empireSystem) {
                if (empire.capitalSystemId != null) {
                    GameUIState.elements.empireSystem.textContent = 'System #' + empire.capitalSystemId;
                } else {
                    GameUIState.elements.empireSystem.textContent = 'Ã¢â‚¬â€';
                }
            }

            // Army component
            if (GameUIState.elements.armyUnitCount) GameUIState.elements.armyUnitCount.textContent = empire.armyUnitCount || 0;
            if (GameUIState.elements.armyOffense) GameUIState.elements.armyOffense.textContent = empire.armyTotalOffense || 0;
            if (GameUIState.elements.armyDefense) GameUIState.elements.armyDefense.textContent = empire.armyTotalDefense || 0;
            if (GameUIState.elements.armySupplyCost) GameUIState.elements.armySupplyCost.textContent = empire.armySupplyCost || 0;

            // Navy component
            if (GameUIState.elements.navyFleetCount) GameUIState.elements.navyFleetCount.textContent = empire.navyFleetCount || 0;
            if (GameUIState.elements.navyShipCount) GameUIState.elements.navyShipCount.textContent = empire.navyShipCount || 0;
            if (GameUIState.elements.navyDamage) GameUIState.elements.navyDamage.textContent = empire.navyTotalDamage || 0;
            if (GameUIState.elements.navyShields) GameUIState.elements.navyShields.textContent = empire.navyTotalShieldPoints || 0;
            if (GameUIState.elements.navyHull) GameUIState.elements.navyHull.textContent = empire.navyTotalHullPoints || 0;

            // Facilities component
            if (GameUIState.elements.facilityCount) GameUIState.elements.facilityCount.textContent = empire.facilityCount || 0;
            if (GameUIState.elements.spaceStationCount) GameUIState.elements.spaceStationCount.textContent = empire.spaceStationCount || 0;
            if (GameUIState.elements.mineralProduction) GameUIState.elements.mineralProduction.textContent = empire.mineralProduction || 0;
            if (GameUIState.elements.energyProduction) GameUIState.elements.energyProduction.textContent = empire.energyProduction || 0;
            if (GameUIState.elements.foodProduction) GameUIState.elements.foodProduction.textContent = empire.foodProduction || 0;
            if (GameUIState.elements.researchOutput) GameUIState.elements.researchOutput.textContent = empire.researchOutput || 0;
            if (GameUIState.elements.facilityDefenseBonus) GameUIState.elements.facilityDefenseBonus.textContent = empire.facilityDefenseBonus || 0;
            if (GameUIState.elements.creditIncome) GameUIState.elements.creditIncome.textContent = empire.creditIncome || 0;

            // Empire Status Bar
            if (GameUIState.elements.empireIntegrity) {
                const integrity = Math.round(empire.empireIntegrity || 100);
                GameUIState.elements.empireIntegrity.textContent = integrity + '%';
                if (GameUIState.elements.empireIntegrityFill) {
                    GameUIState.elements.empireIntegrityFill.style.width = integrity + '%';
                    GameUIState.elements.empireIntegrityFill.className = 'empire-integrity-fill' +
                        (integrity < 30 ? ' danger' : integrity < 60 ? ' warning' : '');
                }
            }
            if (GameUIState.elements.empireMilitaryPower) GameUIState.elements.empireMilitaryPower.textContent = empire.militaryPower || 0;
            if (GameUIState.elements.empireShipQueue) GameUIState.elements.empireShipQueue.textContent = empire.shipQueueSize || 0;
            if (GameUIState.elements.empireActiveBattles) GameUIState.elements.empireActiveBattles.textContent = empire.activeBattlesCount || 0;

            // Battle count badge
            if (GameUIState.elements.battleCountBadge) {
                const battleCount = empire.activeBattlesCount || 0;
                if (battleCount > 0) {
                    GameUIState.elements.battleCountBadge.style.display = 'inline-flex';
                    GameUIState.elements.battleCountBadge.textContent = battleCount;
                } else {
                    GameUIState.elements.battleCountBadge.style.display = 'none';
                }
            }

            // Traveller RPG: Commander display
            if (typeof CharacterUI !== 'undefined' && CharacterUI.displayCommander) {
                CharacterUI.displayCommander(GameUIState.gameState.commander);
            }

            // Traveller RPG: Mercenary display
            if (typeof TradeUI !== 'undefined' && TradeUI.displayMercenary) {
                TradeUI.displayMercenary(empire.mercenarySummary);
            }
        }

        // Player list
        if (GameUIState.elements.playerList) {
            GameUIState.elements.playerList.innerHTML = '';
            // Simultaneous turns: no current player highlighting
            (GameUIState.gameState.players || []).forEach(player => {
                const item = document.createElement('div');
                const isAi = player.isAi === true;
                let className = 'item-list-item';
                if (isAi) className += ' ai-player';
                item.className = className;

                let namePrefix = '';
                if (isAi) {
                    const diff = player.difficulty || 'MEDIUM';
                    namePrefix = `<span class="ai-badge" title="AI - ${diff}">Ã°Å¸Â¤â€“ ${diff}</span> `;
                }
                item.innerHTML = `
                    <div class="player-item-header">
                        ${namePrefix}<span class="item-name">${GameUI.escapeHtml(player.name)}</span>
                    </div>
                    <span class="item-detail">${player.planetCount} planets, ${player.fleetCount} fleets</span>
                `;
                GameUIState.elements.playerList.appendChild(item);
            });
        }

        // Fleet list Ã¢â‚¬â€ use fleet data from game state
        if (GameUIState.elements.fleetList) {
            GameUIState.elements.fleetList.innerHTML = '';
            const fleets = GameUIState.gameState.fleets || [];
            if (fleets.length > 0) {
                fleets.forEach(fleet => {
                    const item = document.createElement('div');
                    item.className = 'item-list-item' + (GameUIState.selectedFleet === fleet.id ? ' selected' : '');
                    const healthPercent = Math.round(fleet.healthRatio * 100);
                    let healthClass = 'health-good';
                    if (healthPercent < 20) healthClass = 'health-critical';
                    else if (healthPercent < 50) healthClass = 'health-warning';
                    else if (healthPercent < 80) healthClass = 'health-damaged';
                    const jDriveStatus = fleet.effectiveJDriveSpeed > 0
                        ? '<span class="drive-status drive-ok" title="JDrive operational">Ã°Å¸Å¡â‚¬</span>'
                        : '<span class="drive-status drive-disabled" title="JDrive disabled Ã¢â‚¬â€ health too low">Ã°Å¸Å¡Â«</span>';
                    item.innerHTML = `
                        <div class="fleet-item-header">
                            <span class="item-name">${GameUI.escapeHtml(fleet.name)}</span>
                            ${jDriveStatus}
                        </div>
                        <div class="fleet-item-stats">
                            <span class="item-detail">Ã°Å¸Å¡Â¢ ${fleet.shipCount} ship(s)</span>
                            <span class="item-detail">Ã¢Å¡â€Ã¯Â¸Â ${fleet.totalDamage} dmg</span>
                        </div>
                        <div class="fleet-item-drives">
                            <span class="drive-info mdrive" title="MDrive (within-system): ${fleet.effectiveMDriveSpeed} range">
                                Ã°Å¸Å½Â¯ M:${fleet.effectiveMDriveSpeed}
                            </span>
                            <span class="drive-info jdrive" title="JDrive (between-system): ${fleet.effectiveJDriveSpeed}">
                                Ã°Å¸Å’Å’ J:${fleet.effectiveJDriveSpeed}
                            </span>
                        </div>
                        <div class="fleet-item-health ${healthClass}">
                            <div class="health-bar-bg">
                                <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                            </div>
                            <span class="health-text">${healthPercent}%</span>
                        </div>
                        <div class="fleet-item-position">
                            <span class="item-detail">Pos: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})</span>
                        </div>
                    `;
                    item.addEventListener('click', () => {
                        GameUIState.selectedFleet = fleet.id;
                        renderGameState();
                        showFleetDetail(fleet);
                    });
                    GameUIState.elements.fleetList.appendChild(item);
                });
            } else {
                GameUIState.elements.fleetList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No fleets</div>';
            }
        }

        // Simultaneous turns: all action buttons are always enabled
        if (GameUIState.elements.advanceTurnBtn) GameUIState.elements.advanceTurnBtn.disabled = false;
        if (GameUIState.elements.buildShipBtn) GameUIState.elements.buildShipBtn.disabled = false;
        if (GameUIState.elements.moveFleetBtn) GameUIState.elements.moveFleetBtn.disabled = false;
        if (GameUIState.elements.engageCombatBtn) GameUIState.elements.engageCombatBtn.disabled = false;
        if (GameUIState.elements.buildStationBtn) GameUIState.elements.buildStationBtn.disabled = false;
        if (GameUIState.elements.buildFacilityBtn) GameUIState.elements.buildFacilityBtn.disabled = false;
        if (GameUIState.elements.createFleetBtn) GameUIState.elements.createFleetBtn.disabled = false;
        if (GameUIState.elements.viewBattlesBtn) GameUIState.elements.viewBattlesBtn.disabled = false;

        // Render card-based galaxy view
        renderSectorCardsView();

        // Render subsector panel
        renderSubsectorPanel();

    // Register public functions on GameUI
    GameUI.showConnectionStatus = showConnectionStatus;
    GameUI.hideConnectionStatus = hideConnectionStatus;
    GameUI.showConnectionError = showConnectionError;
    GameUI.loadGameState = loadGameState;
    GameUI.startSubsectorPolling = startSubsectorPolling;
    GameUI.stopSubsectorPolling = stopSubsectorPolling;
    GameUI.renderGameState = renderGameState;
    GameUI.myPlayer = myPlayer;
}());
