/**
 * DoraMCU Game UI - TURN PHASES Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    // TURN GameUIState.PHASES
    function startTurnPhases() {
        GameUIState.currentPhase = 1;
        showPhaseModal(GameUIState.PHASES[0]);
    }

    function showPhaseModal(phaseName) {
        // Hide all phase modals
        Object.values(GameUIState.PHASE_MODAL_IDS).forEach(id => GameUI.hideModal(id));

        const modalId = GameUIState.PHASE_MODAL_IDS[phaseName];
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('hidden');
        }

        // Populate phase data
        switch (phaseName) {
            case 'resource':
                populateResourcePhase();
                break;
            case 'shipBuild':
                populateShipBuildPhase();
                break;
            case 'fleetMove':
                populateFleetMovePhase();
                break;
            case 'combat':
                populateCombatPhase();
                break;
        }

        addEventLog('PHASE', `Entered ${GameUIState.PHASE_NAMES[phaseName]} phase`);
    }

    function nextPhase() {
        if (GameUIState.currentPhase >= GameUIState.PHASES.length) {
            endTurnPhases();
            return;
        }
        const nextPhaseName = GameUIState.PHASES[GameUIState.currentPhase];
        GameUIState.currentPhase++;
        showPhaseModal(nextPhaseName);
    }

    async function endTurnPhases() {
        // Hide all phase modals
        Object.values(GameUIState.PHASE_MODAL_IDS).forEach(id => GameUI.hideModal(id));
        GameUIState.currentPhase = 0;

        // Now advance the turn on the server
        if (!GameUIState.sessionId) return;
        try {
            await GameAPI.advanceTurn(GameUIState.sessionId);
            addEventLog('ACTION', 'Turn completed and advanced');
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'End turn failed: ' + err.message);
        }
    }

    function handlePhaseAction(action) {
        const phaseName = GameUIState.PHASES[GameUIState.currentPhase - 1];
        if (!phaseName) return;

        switch (phaseName) {
            case 'resource':
                handleResourcePhaseAction(action);
                break;
            case 'shipBuild':
                handleShipBuildPhaseAction(action);
                break;
            case 'fleetMove':
                handleFleetMovePhaseAction(action);
                break;
            case 'combat':
                handleCombatPhaseAction(action);
                break;
        }
    }

    function handleResourcePhaseAction(action) {
        switch (action) {
            case 'view-production':
                addEventLog('INFO', 'Viewing production details...');
                break;
            case 'allocate-resources':
                addEventLog('INFO', 'Resource allocation coming soon...');
                break;
        }
    }

    async function handleShipBuildPhaseAction(action) {
        const shipTypeMap = {
            'build-frigate': 'FRIGATE',
            'build-destroyer': 'DESTROYER',
            'build-cruiser': 'CRUISER',
            'build-battleship': 'BATTLESHIP'
        };
        const shipType = shipTypeMap[action];
        if (!shipType || !GameUIState.sessionId) return;

        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
            addEventLog('ACTION', `Queued ${shipType} for construction`);
            await loadGameState();
            populateShipBuildPhase();
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
        }
    }

    function handleFleetMovePhaseAction(action) {
        switch (action) {
            case 'move-selected-fleet':
                if (!GameUIState.selectedFleet) {
                    addEventLog('WARN', 'No fleet selected');
                    return;
                }
                const coords = GameUIState.elements.targetCoords?.value;
                if (!coords) {
                    addEventLog('WARN', 'Select a target on the map first');
                    return;
                }
                const parts = coords.split(',').map(s => parseInt(s.trim()));
                if (parts.length === 2) {
                    moveFleetAction(parts[0], parts[1]);
                }
                break;
            case 'scout-sector':
                addEventLog('INFO', 'Scout sector coming soon...');
                break;
        }
    }

    // Combat state
    let selectedAttackerFleet = null;
    let selectedDefenderFleet = null;

    function handleCombatPhaseAction(action) {
        switch (action) {
            case 'engage-combat':
                showCombatModal();
                break;
            case 'defensive-stance':
                addEventLog('INFO', 'Defensive stance activated for all fleets');
                GameUI.showToast('Defensive stance activated', 'info');
                break;
        }
    }

    function showCombatModal() {
        // Create combat modal dynamically
        let modal = document.getElementById('combatModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'combatModal';
            modal.className = 'modal-overlay hidden';
            modal.innerHTML = `
                <div class="modal-content combat-modal">
                    <div class="modal-header">
                        <h3><svg class="icon"><use href="#icon-sword"></use></svg> Engage Combat</h3>
                        <button class="modal-close" id="combatModalClose">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="combat-selectors">
                            <div class="combat-selector-group">
                                <label>Attacker Fleet (Yours)</label>
                                <select class="form-input" id="combatAttackerSelect">
                                    <option value="">Ã¢â‚¬â€ Select attacker fleet Ã¢â‚¬â€</option>
                                </select>
                            </div>
                            <div class="combat-vs-divider">VS</div>
                            <div class="combat-selector-group">
                                <label>Defender Fleet (Enemy)</label>
                                <select class="form-input" id="combatDefenderSelect">
                                    <option value="">Ã¢â‚¬â€ Select enemy fleet Ã¢â‚¬â€</option>
                                </select>
                            </div>
                        </div>
                        <div class="combat-odds" id="combatOdds">
                            <div class="combat-odds-header">Combat Odds</div>
                            <div class="combat-odds-body" id="combatOddsBody">
                                <p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>
                            </div>
                        </div>
                        <div class="combat-result" id="combatResult" style="display:none;"></div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn-small" id="combatModalCancel">Cancel</button>
                        <button class="btn-small danger" id="combatModalEngage" disabled>Ã¢Å¡â€Ã¯Â¸Â Engage Combat</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            // Bind events
            document.getElementById('combatModalClose').addEventListener('click', () => GameUI.hideModal('combatModal'));
            document.getElementById('combatModalCancel').addEventListener('click', () => GameUI.hideModal('combatModal'));
            document.getElementById('combatModalEngage').addEventListener('click', () => executeCombat());
            document.getElementById('combatAttackerSelect').addEventListener('change', () => onCombatFleetChange());
            document.getElementById('combatDefenderSelect').addEventListener('change', () => onCombatFleetChange());
        }
        // Populate fleet selectors
        populateCombatSelectors();
        // Reset state
        selectedAttackerFleet = null;
        selectedDefenderFleet = null;
        document.getElementById('combatResult').style.display = 'none';
        document.getElementById('combatModalEngage').disabled = true;
        document.getElementById('combatOddsBody').innerHTML = '<p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>';
        showModal('combatModal');
    }

    function populateCombatSelectors() {
        const attackerSelect = document.getElementById('combatAttackerSelect');
        const defenderSelect = document.getElementById('combatDefenderSelect');
        if (!attackerSelect || !defenderSelect) return;

        const myPlayerId = getCurrentPlayerId();
        const allFleets = GameUIState.gameState?.fleets || [];
        const myFleets = allFleets.filter(f => f.ownerId === myPlayerId);
        const enemyFleets = allFleets.filter(f => f.ownerId !== myPlayerId);

        // Populate attacker dropdown
        attackerSelect.innerHTML = '<option value="">Ã¢â‚¬â€ Select attacker fleet Ã¢â‚¬â€</option>';
        myFleets.forEach(fleet => {
            const option = document.createElement('option');
            option.value = fleet.id;
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            option.textContent = `${fleet.name || 'Fleet ' + fleet.id} (${fleet.shipCount || 0} ships, ${healthPercent}% HP)`;
            attackerSelect.appendChild(option);
        });

        // Populate defender dropdown
        defenderSelect.innerHTML = '<option value="">Ã¢â‚¬â€ Select enemy fleet Ã¢â‚¬â€</option>';
        enemyFleets.forEach(fleet => {
            const option = document.createElement('option');
            option.value = fleet.id;
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            option.textContent = `${fleet.name || 'Enemy Fleet ' + fleet.id} (${fleet.shipCount || '?'} ships, ${healthPercent}% HP)`;
            defenderSelect.appendChild(option);
        });

        if (myFleets.length === 0) {
            attackerSelect.innerHTML = '<option value="">Ã¢â‚¬â€ No fleets available Ã¢â‚¬â€</option>';
        }
        if (enemyFleets.length === 0) {
            defenderSelect.innerHTML = '<option value="">Ã¢â‚¬â€ No enemies detected Ã¢â‚¬â€</option>';
        }
    }

    function onCombatFleetChange() {
        const attackerSelect = document.getElementById('combatAttackerSelect');
        const defenderSelect = document.getElementById('combatDefenderSelect');
        const engageBtn = document.getElementById('combatModalEngage');

        selectedAttackerFleet = attackerSelect?.value ? parseInt(attackerSelect.value) : null;
        selectedDefenderFleet = defenderSelect?.value ? parseInt(defenderSelect.value) : null;

        engageBtn.disabled = !(selectedAttackerFleet && selectedDefenderFleet);

        if (selectedAttackerFleet && selectedDefenderFleet) {
            calculateCombatOdds();
        } else {
            document.getElementById('combatOddsBody').innerHTML = '<p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>';
        }
    }

    function calculateCombatOdds() {
        const allFleets = GameUIState.gameState?.fleets || [];
        const attacker = allFleets.find(f => f.id === selectedAttackerFleet);
        const defender = allFleets.find(f => f.id === selectedDefenderFleet);
        if (!attacker || !defender) return;

        const attackerDamage = attacker.totalDamage || 0;
        const defenderDamage = defender.totalDamage || 0;
        const attackerShips = attacker.shipCount || 0;
        const defenderShips = defender.shipCount || 0;
        const attackerShields = attacker.currentShieldPoints || 0;
        const defenderShields = defender.currentShieldPoints || 0;
        const attackerHull = attacker.currentHullPoints || 0;
        const defenderHull = defender.currentHullPoints || 0;

        // Calculate combat strength score
        const attackerStrength = attackerDamage * attackerShips + attackerShields * 0.5 + attackerHull * 0.3;
        const defenderStrength = defenderDamage * defenderShips + defenderShields * 0.5 + defenderHull * 0.3;
        const totalStrength = attackerStrength + defenderStrength;
        const winChance = totalStrength > 0 ? Math.round((attackerStrength / totalStrength) * 100) : 50;

        // Determine advantage
        let advantageClass = 'even';
        let advantageText = 'Even match';
        if (winChance >= 60) {
            advantageClass = 'favorable';
            advantageText = 'Favorable odds';
        } else if (winChance <= 40) {
            advantageClass = 'unfavorable';
            advantageText = 'Unfavorable odds';
        }

        document.getElementById('combatOddsBody').innerHTML = `
            <div class="combat-odds-comparison">
                <div class="combat-side attacker-side">
                    <div class="combat-side-label">Attacker</div>
                    <div class="combat-side-name">${GameUI.escapeHtml(attacker.name || 'Fleet')}</div>
                    <div class="combat-stat-row"><span>Ships:</span><span>${attackerShips}</span></div>
                    <div class="combat-stat-row"><span>Damage:</span><span>${attackerDamage}</span></div>
                    <div class="combat-stat-row"><span>Shields:</span><span>${attackerShields}</span></div>
                    <div class="combat-stat-row"><span>Hull:</span><span>${attackerHull}</span></div>
                    <div class="combat-stat-row"><span>Strength:</span><span>${Math.round(attackerStrength)}</span></div>
                </div>
                <div class="combat-odds-center">
                    <div class="combat-win-chance ${advantageClass}">${winChance}%</div>
                    <div class="combat-advantage ${advantageClass}">${advantageText}</div>
                </div>
                <div class="combat-side defender-side">
                    <div class="combat-side-label">Defender</div>
                    <div class="combat-side-name">${GameUI.escapeHtml(defender.name || 'Enemy Fleet')}</div>
                    <div class="combat-stat-row"><span>Ships:</span><span>${defenderShips}</span></div>
                    <div class="combat-stat-row"><span>Damage:</span><span>${defenderDamage}</span></div>
                    <div class="combat-stat-row"><span>Shields:</span><span>${defenderShields}</span></div>
                    <div class="combat-stat-row"><span>Hull:</span><span>${defenderHull}</span></div>
                    <div class="combat-stat-row"><span>Strength:</span><span>${Math.round(defenderStrength)}</span></div>
                </div>
            </div>
        `;
    }

    async function executeCombat() {
        if (!GameUIState.sessionId || !selectedAttackerFleet || !selectedDefenderFleet) return;

        const engageBtn = document.getElementById('combatModalEngage');
        engageBtn.disabled = true;
        engageBtn.textContent = 'Ã¢Å¡â€Ã¯Â¸Â Engaging...';

        try {
            addEventLog('ACTION', `Combat: Fleet ${selectedAttackerFleet} attacks Fleet ${selectedDefenderFleet}`);
            await GameAPI.engageCombat(GameUIState.sessionId, selectedAttackerFleet, selectedDefenderFleet);
            await loadGameState();

            // Show battle result
            showBattleResult();
            GameUI.showToast('Combat resolved!', 'success');
        } catch (err) {
            addEventLog('ERROR', 'Combat failed: ' + err.message);
            GameUI.showToast('Combat failed: ' + err.message, 'error');
        } finally {
            engageBtn.disabled = false;
            engageBtn.textContent = 'Ã¢Å¡â€Ã¯Â¸Â Engage Combat';
        }
    }

    function showBattleResult() {
        const resultDiv = document.getElementById('combatResult');
        const allFleets = GameUIState.gameState?.fleets || [];
        const attacker = allFleets.find(f => f.id === selectedAttackerFleet);
        const defender = allFleets.find(f => f.id === selectedDefenderFleet);

        if (!attacker && !defender) {
            resultDiv.style.display = 'none';
            return;
        }

        const attackerAlive = attacker && attacker.shipCount > 0;
        const defenderAlive = defender && defender.shipCount > 0;

        let outcomeText = 'Battle Complete';
        let outcomeClass = 'draw';
        if (attackerAlive && !defenderAlive) {
            outcomeText = 'Victory!';
            outcomeClass = 'victory';
        } else if (!attackerAlive && defenderAlive) {
            outcomeText = 'Defeat!';
            outcomeClass = 'defeat';
        } else if (!attackerAlive && !defenderAlive) {
            outcomeText = 'Mutual Destruction';
            outcomeClass = 'draw';
        } else {
            outcomeClass = 'draw';
        }

        resultDiv.style.display = 'block';
        resultDiv.innerHTML = `
            <div class="battle-result ${outcomeClass}">
                <div class="battle-result-outcome">${outcomeText}</div>
                <div class="battle-result-details">
                    ${attacker ? `
                        <div class="battle-fleet-status">
                            <div class="battle-fleet-name">${GameUI.escapeHtml(attacker.name || 'Attacker')}</div>
                            <div class="battle-fleet-stats">
                                <span>Ships: ${attacker.shipCount || 0}</span>
                                <span>HP: ${Math.round((attacker.healthRatio || 0) * 100)}%</span>
                                <span>Damage: ${attacker.totalDamage || 0}</span>
                            </div>
                        </div>
                    ` : ''}
                    ${defender ? `
                        <div class="battle-fleet-status">
                            <div class="battle-fleet-name">${GameUI.escapeHtml(defender.name || 'Defender')}</div>
                            <div class="battle-fleet-stats">
                                <span>Ships: ${defender.shipCount || 0}</span>
                                <span>HP: ${Math.round((defender.healthRatio || 0) * 100)}%</span>
                                <span>Damage: ${defender.totalDamage || 0}</span>
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }

    function populateResourcePhase() {
        const container = GameUIState.elements.phaseResourceSummary;
        if (!container || !GameUIState.gameState) return;

        const pipeline = GameUIState.gameState.pipelineSummary;
        if (pipeline) {
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            const mining = pipeline.miningRate || {};
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull Modules</span>
                    <span class="phase-resource-value">${modules.HULL_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon Modules</span>
                    <span class="phase-resource-value">${modules.WEAPON_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield Modules</span>
                    <span class="phase-resource-value">${modules.SHIELD_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine Modules</span>
                    <span class="phase-resource-value">${modules.ENGINE_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸Ââ€”Ã¯Â¸Â Facility Modules</span>
                    <span class="phase-resource-value">${modules.FACILITY_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢â€ºÂÃ¯Â¸Â Raw Resources</span>
                    <span class="phase-resource-value">${Object.values(raw).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â§ Refined Resources</span>
                    <span class="phase-resource-value">${Object.values(refined).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸ÂÂ­ Mining Ops</span>
                    <span class="phase-resource-value">${pipeline.miningOperations} (+${Object.values(mining).reduce((a, b) => a + b, 0)}/turn)</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â¬ Processing</span>
                    <span class="phase-resource-value">${pipeline.activeProcessing}/${pipeline.miningOperations}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€œÂ¦ Construction Queue</span>
                    <span class="phase-resource-value">${pipeline.constructionQueueSize}</span>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
            `;
        }
    }

    async function loadPipelineDetails() {
        const container = GameUIState.elements.phasePipelineDetails;
        if (!container || !currentSessionId) return;

        const isHidden = container.classList.contains('hidden');
        if (!isHidden) {
            container.classList.add('hidden');
            return;
        }

        try {
            const response = await gameApi.get(`/sessions/${currentSessionId}/pipeline/details`);
            const details = response.data;

            // Calculate pipeline rates for bottleneck analysis
            const miningOps = details.miningOperations || [];
            const transportOrders = details.transportOrders || [];
            const constructionOrders = details.constructionOrders || [];

            const totalMiningRate = miningOps.reduce((sum, op) => {
                const rates = op.productionRate || {};
                return sum + Object.values(rates).reduce((a, b) => a + b, 0);
            }, 0);
            const activeTransports = transportOrders.length;
            const activeConstruction = constructionOrders.length;

            // Identify bottlenecks
            const bottlenecks = [];
            if (miningOps.length > 0 && activeTransports === 0) {
                bottlenecks.push('Mining output not being transported');
            }
            if (activeTransports > 0 && activeConstruction === 0) {
                bottlenecks.push('Resources in transit but no construction');
            }
            if (miningOps.length === 0) {
                bottlenecks.push('No mining operations active');
            }

            let html = '';

            // Pipeline Flow Diagram
            html += '<div class="pipeline-flow-diagram">';
            html += `
                <div class="pipeline-flow-stage ${miningOps.length > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã¢â€ºÂÃ¯Â¸Â</div>
                    <div class="pipeline-flow-name">Mining</div>
                    <div class="pipeline-flow-count">${miningOps.length} ops</div>
                    <div class="pipeline-flow-rate">${totalMiningRate.toFixed(1)}/turn</div>
                </div>
                <div class="pipeline-flow-arrow">Ã¢â€ â€™</div>
                <div class="pipeline-flow-stage ${activeTransports > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã°Å¸Å¡Å¡</div>
                    <div class="pipeline-flow-name">Transport</div>
                    <div class="pipeline-flow-count">${activeTransports} orders</div>
                </div>
                <div class="pipeline-flow-arrow">Ã¢â€ â€™</div>
                <div class="pipeline-flow-stage ${activeConstruction > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã°Å¸Ââ€”Ã¯Â¸Â</div>
                    <div class="pipeline-flow-name">Construction</div>
                    <div class="pipeline-flow-count">${activeConstruction} items</div>
                </div>
            `;
            html += '</div>';

            // Bottleneck warnings
            if (bottlenecks.length > 0) {
                html += '<div class="pipeline-bottlenecks">';
                bottlenecks.forEach(bn => {
                    html += `<div class="pipeline-bottleneck">Ã¢Å¡Â Ã¯Â¸Â ${bn}</div>`;
                });
                html += '</div>';
            }

            let detailsHtml = '<div class="pipeline-details-grid">';

            // Mining Operations
            detailsHtml += '<div class="pipeline-section"><h4>Ã¢â€ºÂÃ¯Â¸Â Mining Operations</h4>';
            if (miningOps.length > 0) {
                miningOps.forEach(op => {
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${op.celestialBodyName} (${op.celestialBodyType})</span>
                        <span class="pipeline-item-value">`;
                    const rates = op.productionRate || {};
                    detailsHtml += Object.entries(rates).map(([k, v]) => `${k}=${v}`).join(', ');
                    detailsHtml += `</span>
                        <span class="pipeline-item-meta">Storage: ${op.storage ? Object.entries(op.storage).map(([k,v]) => `${k}=${v}`).join(', ') : 'N/A'}</span>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active mining</div>';
            }
            detailsHtml += '</div>';

            // Transport Orders
            detailsHtml += '<div class="pipeline-section"><h4>Ã°Å¸Å¡Å¡ Transport Orders</h4>';
            if (transportOrders.length > 0) {
                transportOrders.forEach(order => {
                    const progress = Math.round((order.progress || 0) * 100);
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${order.resourceType}: ${order.totalAmount}</span>
                        <span class="pipeline-item-value">${order.turnsCompleted || 0}/${order.turnsTotal} turns</span>
                        <span class="pipeline-item-meta">${progress}%</span>
                        <div class="pipeline-progress-bar"><div class="pipeline-progress-fill" style="width: ${progress}%"></div></div>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active transports</div>';
            }
            detailsHtml += '</div>';

            // Construction Queue
            detailsHtml += '<div class="pipeline-section"><h4>Ã°Å¸Ââ€”Ã¯Â¸Â Construction Queue</h4>';
            if (constructionOrders.length > 0) {
                constructionOrders.forEach(order => {
                    const name = order.stationType || order.planetFacility || 'Unknown';
                    const progress = Math.round((order.progress || 0) * 100);
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${name} (${order.constructionType})</span>
                        <span class="pipeline-item-value">${order.turnsCompleted}/${order.totalTurns} turns</span>
                        <span class="pipeline-item-meta">${progress}%</span>
                        <div class="pipeline-progress-bar"><div class="pipeline-progress-fill" style="width: ${progress}%"></div></div>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active construction</div>';
            }
            detailsHtml += '</div></div>';

            container.innerHTML = html + detailsHtml;
            container.classList.remove('hidden');
        } catch (err) {
            addEventLog('ERROR', 'Failed to load pipeline details: ' + err.message);
        }
    }

    // Ship type definitions matching backend ShipType enum
    const SHIP_TYPES = [
        { id: 'FRIGATE', name: 'Frigate', icon: 'Ã°Å¸Å¡â‚¬', hull: 50, slots: 3, buildTime: 2, mDrive: 10, jDrive: 12, evasion: 20, modules: 1, desc: 'Light, fast scout ship' },
        { id: 'DESTROYER', name: 'Destroyer', icon: 'Ã¢Å¡â€Ã¯Â¸Â', hull: 100, slots: 5, buildTime: 3, mDrive: 8, jDrive: 10, evasion: 15, modules: 2, desc: 'Medium escort ship' },
        { id: 'CRUISER', name: 'Cruiser', icon: 'Ã°Å¸â€ºÂ¡Ã¯Â¸Â', hull: 200, slots: 8, buildTime: 5, mDrive: 6, jDrive: 8, evasion: 10, modules: 8, desc: 'Heavy multi-role combat ship' },
        { id: 'BATTLESHIP', name: 'Battleship', icon: 'Ã°Å¸â€™Â¥', hull: 400, slots: 12, buildTime: 8, mDrive: 4, jDrive: 5, evasion: 5, modules: 27, desc: 'Capital warship' },
        { id: 'CARRIER', name: 'Carrier', icon: 'Ã¢Å“Ë†Ã¯Â¸Â', hull: 300, slots: 6, buildTime: 7, mDrive: 5, jDrive: 6, evasion: 8, modules: 12, desc: 'Deploys fighter wings' },
        { id: 'MOTHERSHIP', name: 'Mothership', icon: 'Ã°Å¸Å’Å¸', hull: 800, slots: 20, buildTime: 15, mDrive: 2, jDrive: 3, evasion: 2, modules: 64, desc: 'Ultimate superweapon' }
    ];

    function populateShipBuildPhase() {
        const container = GameUIState.elements.phaseShipQueue;
        if (!container) return;

        const pipeline = GameUIState.gameState?.pipelineSummary;
        const modules = pipeline?.modules || {};
        const hullModules = modules.HULL_MODULE || 0;
        const weaponModules = modules.WEAPON_MODULE || 0;
        const shieldModules = modules.SHIELD_MODULE || 0;
        const engineModules = modules.ENGINE_MODULE || 0;

        // Calculate total available modules for cost estimation
        const totalModules = hullModules + weaponModules + shieldModules + engineModules;

        let html = `
            <div class="ship-build-resources">
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull</span>
                    <span class="phase-resource-value">${hullModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon</span>
                    <span class="phase-resource-value">${weaponModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield</span>
                    <span class="phase-resource-value">${shieldModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine</span>
                    <span class="phase-resource-value">${engineModules}</span>
                </div>
                <div class="ship-build-total">Total modules: ${totalModules}</div>
            </div>
            <div class="ship-type-cards">
        `;

        SHIP_TYPES.forEach(ship => {
            // Estimate cost as hull modules needed (simplified: 1 module per hull point / 10)
            const estimatedCost = Math.max(1, Math.ceil(ship.modules / 2));
            const canAfford = totalModules >= estimatedCost;
            const affordableClass = canAfford ? '' : ' unaffordable';

            html += `
                <div class="ship-type-card${affordableClass}" data-ship-type="${ship.id}" data-cost="${estimatedCost}">
                    <div class="ship-type-header">
                        <span class="ship-type-icon">${ship.icon}</span>
                        <span class="ship-type-name">${ship.name}</span>
                        <span class="ship-type-cost">${estimatedCost} modules</span>
                    </div>
                    <div class="ship-type-desc">${ship.desc}</div>
                    <div class="ship-type-stats">
                        <div class="ship-stat"><span class="ship-stat-label">Hull:</span><span>${ship.hull}</span></div>
                        <div class="ship-stat"><span class="ship-stat-label">Slots:</span><span>${ship.slots}</span></div>
                        <div class="ship-stat"><span class="ship-stat-label">Build:</span><span>${ship.buildTime} turns</span></div>
                        <div class="ship-stat drive-info mdrive">Ã°Å¸Å½Â¯ M:${ship.mDrive}</div>
                        <div class="ship-stat drive-info jdrive">Ã°Å¸Å’Å’ J:${ship.jDrive}</div>
                        <div class="ship-stat"><span class="ship-stat-label">Evasion:</span><span>${ship.evasion}%</span></div>
                    </div>
                    <button class="btn-small primary ship-build-btn${canAfford ? '' : ' disabled'}" ${canAfford ? '' : 'disabled'}>
                        Build ${ship.name}
                    </button>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;

        // Bind build button clicks
        container.querySelectorAll('.ship-build-btn:not(.disabled)').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const card = btn.closest('.ship-type-card');
                const shipType = card.dataset.shipType;
                handleBuildShipFromPhase(shipType);
            });
        });
    }

    async function handleBuildShipFromPhase(shipType) {
        if (!GameUIState.sessionId) return;
        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
            addEventLog('ACTION', `Building ${shipType}`);
            GameUI.showToast(`Queued ${shipType} for construction`, 'success');
            await loadGameState();
            // Refresh the phase if still open
            if (GameUIState.currentPhase > 0 && GameUIState.PHASES[GameUIState.currentPhase - 1] === 'shipBuild') {
                populateShipBuildPhase();
            }
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
            GameUI.showToast('Build failed: ' + err.message, 'error');
        }
    }

    function populateFleetMovePhase() {
        const container = GameUIState.elements.phaseFleetList;
        if (!container) return;

        const fleets = GameUIState.gameState?.fleets || [];
        if (!fleets.length) {
            container.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No fleets available</div>';
            return;
        }

        container.innerHTML = '';
        fleets.forEach(fleet => {
            const item = document.createElement('div');
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            let healthClass = 'health-good';
            if (healthPercent < 20) healthClass = 'health-critical';
            else if (healthPercent < 50) healthClass = 'health-warning';
            else if (healthPercent < 80) healthClass = 'health-damaged';
            const jDriveOk = (fleet.effectiveJDriveSpeed || 0) > 0;
            item.className = 'phase-fleet-item' + (GameUIState.selectedFleet === fleet.id ? ' selected' : '');
            item.innerHTML = `
                <div class="phase-fleet-header">
                    <span class="phase-option-name">Ã°Å¸Å¡â‚¬ ${GameUI.escapeHtml(fleet.name || 'Fleet ' + fleet.id)}</span>
                    <span class="phase-option-desc">${fleet.shipCount || 0} ships</span>
                </div>
                <div class="phase-fleet-stats">
                    <span class="drive-badge mdrive" title="MDrive range: ${fleet.effectiveMDriveSpeed || 0}">
                        Ã°Å¸Å½Â¯ M:${fleet.effectiveMDriveSpeed || 0}
                    </span>
                    <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}" title="JDrive: ${fleet.effectiveJDriveSpeed || 0}">
                        Ã°Å¸Å’Å’ J:${fleet.effectiveJDriveSpeed || 0}
                    </span>
                    <span class="phase-option-desc">Ã¢Å¡â€Ã¯Â¸Â ${fleet.totalDamage || 0} dmg</span>
                </div>
                <div class="phase-fleet-health ${healthClass}">
                    <div class="health-bar-bg">
                        <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                    </div>
                    <span class="health-text">${healthPercent}% hull</span>
                </div>
                <div class="phase-fleet-position">
                    <span class="phase-option-desc">Pos: (${fleet.positionX || 0}, ${fleet.positionY || 0}, ${fleet.positionZ || 0})</span>
                </div>
            `;
            item.addEventListener('click', () => {
                GameUIState.selectedFleet = fleet.id;
                populateFleetMovePhase();
                addEventLog('SYSTEM', `Selected fleet: ${fleet.name || fleet.id}`);
            });
            container.appendChild(item);
        });
    }

    function populateCombatPhase() {
        const container = GameUIState.elements.phaseEnemyList;
        if (!container) return;

        // Show enemy fleets (from other players)
        const allFleets = GameUIState.gameState?.fleets || [];
        const myFleetIds = new Set(allFleets.filter(f => f.ownerId === getCurrentPlayerId()).map(f => f.id));
        const enemyFleets = allFleets.filter(f => !myFleetIds.has(f.id));

        if (!enemyFleets.length) {
            container.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No enemy fleets detected</div>';
            return;
        }

        container.innerHTML = '';
        enemyFleets.forEach(fleet => {
            const item = document.createElement('div');
            item.className = 'phase-enemy-item';
            item.innerHTML = `
                <span class="phase-option-name">Ã°Å¸â€˜Â¾ ${GameUI.escapeHtml(fleet.name || 'Enemy Fleet')}</span>
                <span class="phase-option-desc">${fleet.shipCount || '?'} ships</span>
            `;
            container.appendChild(item);
        });

    // Register public functions on GameUI
    GameUI.startTurnPhases = startTurnPhases;
    GameUI.showPhaseModal = showPhaseModal;
    GameUI.nextPhase = nextPhase;
    GameUI.endTurnPhases = endTurnPhases;
    GameUI.handlePhaseAction = handlePhaseAction;
    GameUI.handleResourcePhaseAction = handleResourcePhaseAction;
    GameUI.handleShipBuildPhaseAction = handleShipBuildPhaseAction;
    GameUI.handleFleetMovePhaseAction = handleFleetMovePhaseAction;
    GameUI.handleCombatPhaseAction = handleCombatPhaseAction;
    GameUI.showCombatModal = showCombatModal;
    GameUI.populateCombatSelectors = populateCombatSelectors;
    GameUI.myFleets = myFleets;
    GameUI.enemyFleets = enemyFleets;
    GameUI.onCombatFleetChange = onCombatFleetChange;
    GameUI.calculateCombatOdds = calculateCombatOdds;
    GameUI.attacker = attacker;
    GameUI.defender = defender;
    GameUI.executeCombat = executeCombat;
    GameUI.showBattleResult = showBattleResult;
    GameUI.populateResourcePhase = populateResourcePhase;
    GameUI.loadPipelineDetails = loadPipelineDetails;
    GameUI.totalMiningRate = totalMiningRate;
    GameUI.populateShipBuildPhase = populateShipBuildPhase;
    GameUI.handleBuildShipFromPhase = handleBuildShipFromPhase;
    GameUI.populateFleetMovePhase = populateFleetMovePhase;
    GameUI.populateCombatPhase = populateCombatPhase;
    GameUI.myFleetIds = myFleetIds;
}());
