/**
 * DoraMCU Game UI - GAME ACTIONS / NEW EMPIRE ACTIONS Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    async function handleAdvanceTurn() {
        if (!GameUIState.sessionId || !GameUIState.gameState?.isMyTurn) return;
        if (!GameUIState.elements.advanceTurnBtn) return;
        GameUIState.elements.advanceTurnBtn.disabled = true;
        try {
            // Start turn phases instead of directly advancing
            startTurnPhases();
        } catch (err) {
            addEventLog('ERROR', 'Advance turn failed: ' + err.message);
        } finally {
            if (GameUIState.gameState?.isMyTurn) {
                GameUIState.elements.advanceTurnBtn.disabled = false;
            }
        }


    function handleBuildStationClick() {
        if (!GameUIState.elements.buildStationModal) return;
        // Populate planet dropdown
        if (GameUIState.elements.stationPlanetSelect) {
            GameUIState.elements.stationPlanetSelect.innerHTML = '<option value="">Home Planet (default)</option>';
            if (GameUIState.gameState?.empireSummary) {
                // We don't have planet list here, so just use default
            }
        }
        if (GameUIState.elements.buildStationError) {
            GameUIState.elements.buildStationError.style.display = 'none';
        }
        GameUIState.elements.buildStationModal.classList.remove('hidden');
    }

    async function handleBuildStationConfirm() {
        if (!GameUIState.sessionId) return;
        const stationType = document.getElementById('stationTypeSelect')?.value || 'MILITARY';
        const planetId = document.getElementById('stationPlanetSelect')?.value;

        try {
            const body = { stationType };
            if (planetId) body.planetId = parseInt(planetId);
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/build-station', body);
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('buildStationModal');
            GameUI.showToast('Station ' + stationType + ' built successfully!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('buildStationError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to build station';
                errorEl.style.display = 'block';
            }
        }
    }

    function handleBuildFacilityClick() {
        if (!GameUIState.elements.buildFacilityModal) return;
        // Populate planet dropdown from subsector data
        if (GameUIState.elements.facilityPlanetSelect) {
            GameUIState.elements.facilityPlanetSelect.innerHTML = '<option value="">Select a planet...</option>';
            // Collect owned planets from subsector data
            const ownedPlanets = [];
            if (GameUIState.gameState?.subSectors) {
                GameUIState.gameState.subSectors.forEach(ss => {
                    if (ss.systems) {
                        ss.systems.forEach(sys => {
                            if (sys.planets) {
                                sys.planets.forEach(p => {
                                    if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                                        ownedPlanets.push(p);
                                    }
                                });
                            }
                        });
                    }
                });
            }
            // Deduplicate by id
            const seen = new Set();
            ownedPlanets.forEach(p => {
                if (!seen.has(p.id)) {
                    seen.add(p.id);
                    const opt = document.createElement('option');
                    opt.value = p.id;
                    opt.textContent = (p.name || 'Planet #' + p.id) + ' (' + (p.planetType || 'Unknown') + ')';
                    GameUIState.elements.facilityPlanetSelect.appendChild(opt);
                }
            });
        }
        if (GameUIState.elements.buildFacilityError) {
            GameUIState.elements.buildFacilityError.style.display = 'none';
        }
        GameUIState.elements.buildFacilityModal.classList.remove('hidden');
    }

    async function handleBuildFacilityConfirm() {
        if (!GameUIState.sessionId) return;
        const planetId = document.getElementById('facilityPlanetSelect')?.value;
        const facilityType = document.getElementById('facilityTypeSelect')?.value || 'COLONY_DOME';

        if (!planetId) {
            const errorEl = document.getElementById('buildFacilityError');
            if (errorEl) {
                errorEl.textContent = 'Please select a planet';
                errorEl.style.display = 'block';
            }
            return;
        }

        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/build-facility',
                { planetId: parseInt(planetId), facilityType });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('buildFacilityModal');
            GameUI.showToast('Facility ' + facilityType + ' built successfully!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('buildFacilityError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to build facility';
                errorEl.style.display = 'block';
            }
        }
    }

    function handleCreateFleetClick() {
        if (!GameUIState.elements.createFleetModal) return;
        if (GameUIState.elements.fleetNameInput) GameUIState.elements.fleetNameInput.value = '';
        if (GameUIState.elements.createFleetError) GameUIState.elements.createFleetError.style.display = 'none';
        GameUIState.elements.createFleetModal.classList.remove('hidden');
    }

    async function handleCreateFleetConfirm() {
        if (!GameUIState.sessionId) return;
        const fleetName = document.getElementById('fleetNameInput')?.value?.trim();

        if (!fleetName) {
            const errorEl = document.getElementById('createFleetError');
            if (errorEl) {
                errorEl.textContent = 'Please enter a fleet name';
                errorEl.style.display = 'block';
            }
            return;
        }

        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/create-fleet',
                { fleetName });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('createFleetModal');
            GameUI.showToast('Fleet "' + fleetName + '" created!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('createFleetError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to create fleet';
                errorEl.style.display = 'block';
            }
        }
    }

    async function handleViewBattlesClick() {
        if (!GameUIState.sessionId || !GameUIState.elements.battleStatusModal) return;
        try {
            const battles = await GameAPI.get('/api/v1/sessions/' + GameUIState.sessionId + '/battles');
            renderBattleStatus(battles);
            GameUIState.elements.battleStatusModal.classList.remove('hidden');
        } catch (err) {
            GameUI.showToast('Failed to load battles: ' + err.message, 'error');
        }
    }

    function renderBattleStatus(battles) {
        const list = document.getElementById('battleStatusList');
        if (!list) return;

        if (!battles || battles.length === 0) {
            list.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No active battles</div>';
            return;
        }

        list.innerHTML = battles.map(b => {
            const atkHullPct = b.attackerRemainingHull > 0 ? Math.round((b.attackerRemainingHull / Math.max(b.attackerRemainingHull + 100, 1)) * 100) : 0;
            const defHullPct = b.defenderRemainingHull > 0 ? Math.round((b.defenderRemainingHull / Math.max(b.defenderRemainingHull + 100, 1)) * 100) : 0;
            const stateClass = b.state === 'IN_PROGRESS' ? 'in-progress' : 'completed';
            const stateLabel = b.state === 'IN_PROGRESS' ? 'In Progress' : (b.outcome || 'Completed');

            let eventsHtml = '';
            if (b.events && b.events.length > 0) {
                eventsHtml = '<div class="battle-card-events">' +
                    b.events.map(e => '<div class="battle-event-item">' + GameUI.escapeHtml(e) + '</div>').join('') +
                    '</div>';
            }

            let actionsHtml = '';
            if (b.state === 'IN_PROGRESS') {
                actionsHtml = '<div class="battle-card-actions">' +
                    '<button class="btn-small" onclick="GameUI.reinforceBattle(' + b.battleId + ')">Reinforce</button>' +
                    '<button class="btn-small danger" onclick="GameUI.retreatBattle(' + b.battleId + ')">Retreat</button>' +
                    '</div>';
            }

            return '<div class="battle-card">' +
                '<div class="battle-card-header">' +
                    '<span class="battle-card-title">Battle #' + b.battleId + ' (Round ' + b.currentRound + '/' + b.maxRounds + ')</span>' +
                    '<span class="battle-card-state ' + stateClass + '">' + stateLabel + '</span>' +
                '</div>' +
                '<div class="battle-card-body">' +
                    '<div class="battle-fleet-row">' +
                        '<span class="battle-fleet-name attacker">Ã¢Å¡â€Ã¯Â¸Â ' + GameUI.escapeHtml(b.attackerName) + '</span>' +
                        '<div class="battle-hull-bar"><div class="battle-hull-fill attacker" style="width:' + atkHullPct + '%"></div></div>' +
                        '<span class="battle-fleet-stats">' + b.attackerShipCount + ' ships, ' + b.totalAttackerDamage + ' dmg</span>' +
                    '</div>' +
                    '<div class="battle-fleet-row">' +
                        '<span class="battle-fleet-name defender">Ã°Å¸â€ºÂ¡Ã¯Â¸Â ' + GameUI.escapeHtml(b.defenderName) + '</span>' +
                        '<div class="battle-hull-bar"><div class="battle-hull-fill defender" style="width:' + defHullPct + '%"></div></div>' +
                        '<span class="battle-fleet-stats">' + b.defenderShipCount + ' ships, ' + b.totalDefenderDamage + ' dmg</span>' +
                    '</div>' +
                '</div>' +
                eventsHtml +
                actionsHtml +
            '</div>';
        }).join('');

    // Register public functions on GameUI
    GameUI.handleAdvanceTurn = handleAdvanceTurn;
    GameUI.handleBuildStationClick = handleBuildStationClick;
    GameUI.handleBuildStationConfirm = handleBuildStationConfirm;
    GameUI.handleBuildFacilityClick = handleBuildFacilityClick;
    GameUI.handleBuildFacilityConfirm = handleBuildFacilityConfirm;
    GameUI.handleCreateFleetClick = handleCreateFleetClick;
    GameUI.handleCreateFleetConfirm = handleCreateFleetConfirm;
    GameUI.handleViewBattlesClick = handleViewBattlesClick;
    GameUI.renderBattleStatus = renderBattleStatus;
}());
