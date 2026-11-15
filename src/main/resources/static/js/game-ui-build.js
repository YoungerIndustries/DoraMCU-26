/**
 * DoraMCU Game UI - BUILD SHIP & FLEET MOVE Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    function handleBuildShipClick() {
        // Open the Traveller ship design editor instead of the old modal
        if (typeof ShipDesignUI !== 'undefined' && ShipDesignUI.showShipDesignModal) {
            ShipDesignUI.showShipDesignModal(GameUIState.sessionId);
        } else if (GameUIState.elements.buildShipModal) {
            // Fallback to old modal if ship design module is unavailable
            GameUIState.elements.buildShipModal.classList.remove('hidden');
            if (GameUIState.elements.shipTypeSelect) {
                GameUIState.elements.shipTypeSelect.value = 'FRIGATE';
            }
        }
    }

    async function handleBuildShipConfirm() {
        if (!GameUIState.sessionId) return;
        const shipType = GameUIState.elements.shipTypeSelect?.value || 'FRIGATE';
        GameUI.hideModal('buildShipModal');
        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
            addEventLog('ACTION', `Building ${shipType}`);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
        }
    }

    function handleMoveFleetClick() {
        if (!GameUIState.selectedFleet) {
            GameUI.showToast('Select a fleet first', 'warning');
            return;
        }
        // Find the selected fleet data
        const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
        if (!fleet) {
            GameUI.showToast('Selected fleet not found', 'error');
            return;
        }
        showMoveFleetModal(fleet);
    }

    function showMoveFleetModal(fleet) {
        // Create modal if it doesn't exist
        let modal = document.getElementById('moveFleetModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'moveFleetModal';
            modal.className = 'modal-overlay hidden';
            modal.innerHTML = `
                <div class="modal-content move-fleet-modal">
                    <div class="modal-header">
                        <h3><svg class="icon"><use href="#icon-target"></use></svg> Move Fleet</h3>
                        <button class="modal-close" id="moveFleetModalClose">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="fleet-move-info">
                            <div class="fleet-move-name" id="moveFleetName"></div>
                            <div class="fleet-move-position" id="moveFleetPosition"></div>
                            <div class="fleet-move-drives" id="moveFleetDrives"></div>
                        </div>
                        <div class="move-type-selector">
                            <button class="move-type-btn active" data-type="mdrive" id="moveTypeMDrive">
                                <span class="move-type-icon">Ã°Å¸Å½Â¯</span>
                                <span class="move-type-label">MDrive</span>
                                <span class="move-type-desc">Within-system movement</span>
                            </button>
                            <button class="move-type-btn" data-type="jdrive" id="moveTypeJDrive">
                                <span class="move-type-icon">Ã°Å¸Å’Å’</span>
                                <span class="move-type-label">JDrive</span>
                                <span class="move-type-desc">Between-system jump</span>
                            </button>
                        </div>
                        <div class="move-form" id="moveForm">
                            <div class="form-row" id="moveCoordRow">
                                <div class="form-group">
                                    <label for="moveTargetX">Target X</label>
                                    <input type="number" class="form-input" id="moveTargetX" value="0">
                                </div>
                                <div class="form-group">
                                    <label for="moveTargetY">Target Y</label>
                                    <input type="number" class="form-input" id="moveTargetY" value="0">
                                </div>
                            </div>
                            <div class="form-row hidden" id="moveSystemRow">
                                <div class="form-group">
                                    <label for="moveTargetSystemSelect">Target Solar System</label>
                                    <select class="form-input" id="moveTargetSystemSelect">
                                        <option value="">Ã¢â‚¬â€ Select a target system Ã¢â‚¬â€</option>
                                    </select>
                                    <div class="form-help" id="moveSystemHelp">Choose a system from the dropdown or enter ID manually below</div>
                                </div>
                                <div class="form-group">
                                    <label for="moveTargetSystem">Or enter System ID</label>
                                    <input type="number" class="form-input" id="moveTargetSystem" min="1" placeholder="System ID">
                                </div>
                            </div>
                            <div class="move-range-info" id="moveRangeInfo"></div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn-small" id="moveFleetCancel">Cancel</button>
                        <button class="btn-small primary" id="moveFleetExecute">Execute Move</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            // Bind events
            document.getElementById('moveFleetModalClose').addEventListener('click', () => GameUI.hideModal('moveFleetModal'));
            document.getElementById('moveFleetCancel').addEventListener('click', () => GameUI.hideModal('moveFleetModal'));
            document.getElementById('moveFleetExecute').addEventListener('click', () => executeMoveFleet(fleet));
            document.getElementById('moveTypeMDrive').addEventListener('click', () => setMoveType('mdrive'));
            document.getElementById('moveTypeJDrive').addEventListener('click', () => setMoveType('jdrive'));
        }
        // Populate fleet info
        document.getElementById('moveFleetName').textContent = fleet.name;
        document.getElementById('moveFleetPosition').textContent = `Position: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})`;
        const jDriveOk = fleet.effectiveJDriveSpeed > 0;
        document.getElementById('moveFleetDrives').innerHTML = `
            <span class="drive-badge mdrive">Ã°Å¸Å½Â¯ MDrive: ${fleet.effectiveMDriveSpeed} range</span>
            <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}">Ã°Å¸Å’Å’ JDrive: ${fleet.effectiveJDriveSpeed}${jDriveOk ? '' : ' (disabled)'}</span>
        `;
        // Populate JDrive target dropdown from game state subSectors
        populateJDriveTargets();
        // Set initial move type
        setMoveType('mdrive');
        // Set default target coords
        document.getElementById('moveTargetX').value = fleet.positionX;
        document.getElementById('moveTargetY').value = fleet.positionY;
        showModal('moveFleetModal');
    }

    function populateJDriveTargets() {
        const select = document.getElementById('moveTargetSystemSelect');
        if (!select) return;
        select.innerHTML = '<option value="">Ã¢â‚¬â€ Select a target system Ã¢â‚¬â€</option>';
        
        const subSectors = GameUIState.gameState?.subSectors || [];
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        
        // Group systems by subsector for organized dropdown
        subSectors.forEach(subSector => {
            if (!subSector.systems || subSector.systems.length === 0) return;
            
            // Add a group label for each subsector
            const group = document.createElement('optgroup');
            const isOwned = subSector.ownerEmpireIds && Array.isArray(subSector.ownerEmpireIds) 
                && subSector.ownerEmpireIds.includes(myEmpireId);
            const contestedLabel = subSector.contested ? ' Ã¢Å¡â€Ã¯Â¸Â' : '';
            const ownedLabel = isOwned ? ' Ã°Å¸ÂÂ ' : '';
            group.label = `${subSector.name} [${subSector.subRow},${subSector.subCol}]${ownedLabel}${contestedLabel}`;
            
            subSector.systems.forEach(sys => {
                const option = document.createElement('option');
                option.value = sys.id || sys.systemId || '';
                const name = sys.name || `System ${sys.id}`;
                const planetCount = sys.planetCount || sys.planets?.length || 0;
                option.textContent = `${name} (${planetCount} planets)`;
                option.title = `ID: ${sys.id}, Planets: ${planetCount}`;
                group.appendChild(option);
            });
            
            select.appendChild(group);
        });
        
        // If no systems found, show a message
        if (select.options.length <= 1) {
            select.innerHTML = '<option value="">Ã¢â‚¬â€ No systems available Ã¢â‚¬â€</option>';
        }
    }

    let currentMoveType = 'mdrive';

    function setMoveType(type) {
        currentMoveType = type;
        document.querySelectorAll('.move-type-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.type === type);
        });
        const coordRow = document.getElementById('moveCoordRow');
        const systemRow = document.getElementById('moveSystemRow');
        const rangeInfo = document.getElementById('moveRangeInfo');
        if (type === 'mdrive') {
            coordRow.classList.remove('hidden');
            systemRow.classList.add('hidden');
            const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
            if (fleet) {
                rangeInfo.textContent = `MDrive range: ${fleet.effectiveMDriveSpeed} hexes from current position`;
                rangeInfo.className = 'move-range-info';
            }
        } else {
            coordRow.classList.add('hidden');
            systemRow.classList.remove('hidden');
            const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
            if (fleet) {
                if (fleet.effectiveJDriveSpeed <= 0) {
                    rangeInfo.textContent = 'Ã¢Å¡Â Ã¯Â¸Â JDrive is disabled Ã¢â‚¬â€ fleet health is below 20%';
                    rangeInfo.className = 'move-range-info warning';
                } else {
                    rangeInfo.textContent = 'JDrive: Enter target solar system ID (must have hyperlane connection)';
                    rangeInfo.className = 'move-range-info';
                }
            }
        }
    }

    async function executeMoveFleet(fleet) {
        GameUI.hideModal('moveFleetModal');
        if (!GameUIState.sessionId) return;
        try {
            let targetX, targetY, solarSystemId;
            if (currentMoveType === 'mdrive') {
                targetX = parseInt(document.getElementById('moveTargetX').value) || 0;
                targetY = parseInt(document.getElementById('moveTargetY').value) || 0;
                solarSystemId = null;
                addEventLog('ACTION', `MDrive: Moving ${fleet.name} to (${targetX}, ${targetY})`);
            } else {
                targetX = 0;
                targetY = 0;
                // Prefer dropdown selection, fall back to manual ID input
                const select = document.getElementById('moveTargetSystemSelect');
                const manualInput = document.getElementById('moveTargetSystem');
                solarSystemId = select && select.value ? parseInt(select.value) 
                    : (manualInput && manualInput.value ? parseInt(manualInput.value) : null);
                if (!solarSystemId) {
                    GameUI.showToast('Select a target system or enter a valid ID', 'warning');
                    return;
                }
                // Look up the system name for the log
                const systemName = findSystemName(solarSystemId);
                addEventLog('ACTION', `JDrive: Jumping ${fleet.name} to ${systemName || 'system ' + solarSystemId}`);
            }
            await GameAPI.moveFleet(GameUIState.sessionId, GameUIState.selectedFleet, targetX, targetY, solarSystemId);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Move fleet failed: ' + err.message);
            GameUI.showToast('Move failed: ' + err.message, 'error');
        }
    }

    function findSystemName(systemId) {
        const subSectors = GameUIState.gameState?.subSectors || [];
        for (const subSector of subSectors) {
            for (const sys of (subSector.systems || [])) {
                if ((sys.id || sys.systemId) === systemId) {
                    return sys.name || `System ${systemId}`;
                }
            }
        }
        return null;
    }

    function showFleetDetail(fleet) {
        // Update the right panel with fleet details
        const healthPercent = Math.round(fleet.healthRatio * 100);
        const shieldPercent = fleet.totalShieldPoints > 0 ? Math.round(fleet.currentShieldPoints / fleet.totalShieldPoints * 100) : 0;
        addEventLog('INFO', `Selected ${fleet.name}: MDrive=${fleet.effectiveMDriveSpeed}, JDrive=${fleet.effectiveJDriveSpeed}, Health=${healthPercent}%, Shields=${shieldPercent}%, Damage=${fleet.totalDamage}`);
        
        // Update the target panel to show fleet detail
        const targetPanel = document.getElementById('targetPanel');
        if (!targetPanel) return;
        
        const jDriveOk = fleet.effectiveJDriveSpeed > 0;
        const healthClass = healthPercent < 20 ? 'health-critical' 
            : healthPercent < 50 ? 'health-warning' 
            : healthPercent < 80 ? 'health-damaged' : 'health-good';
        
        let shipsHtml = '';
        if (fleet.ships && fleet.ships.length > 0) {
            shipsHtml = '<div class="fleet-detail-ships"><h4>Ships</h4>';
            fleet.ships.forEach(ship => {
                const shipHealth = Math.round(ship.hullHealthRatio * 100);
                const destroyedClass = ship.destroyed ? ' destroyed' : '';
                shipsHtml += `
                    <div class="ship-detail-item${destroyedClass}">
                        <div class="ship-detail-name">${GameUI.escapeHtml(ship.name || ship.shipType)}</div>
                        <div class="ship-detail-stats">
                            <span class="drive-info mdrive">Ã°Å¸Å½Â¯ M:${ship.mDriveSpeed}</span>
                            <span class="drive-info jdrive">Ã°Å¸Å’Å’ J:${ship.jDriveSpeed}</span>
                        </div>
                        <div class="ship-detail-health">
                            <div class="health-bar-bg">
                                <div class="health-bar-fill" style="width: ${shipHealth}%"></div>
                            </div>
                            <span class="health-text">${shipHealth}%</span>
                        </div>
                        <div class="ship-detail-points">
                            <span>Ã°Å¸â€ºÂ¡Ã¯Â¸Â ${ship.currentShieldPoints}/${ship.totalShieldPoints}</span>
                            <span>Ã°Å¸â€Â§ ${ship.currentHullPoints}/${ship.totalHullPoints}</span>
                        </div>
                        ${ship.totalDamage > 0 ? `<div class="ship-detail-damage">Ã¢Å¡â€Ã¯Â¸Â ${ship.totalDamage} dmg</div>` : ''}
                    </div>
                `;
            });
            shipsHtml += '</div>';
        }
        
        targetPanel.innerHTML = `
            <h3><svg class="icon"><use href="#icon-shield"></use></svg> Fleet Detail</h3>
            <div class="fleet-detail-panel">
                <div class="fleet-detail-name">${GameUI.escapeHtml(fleet.name)}</div>
                <div class="fleet-detail-position">Position: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})</div>
                <div class="fleet-detail-drives">
                    <span class="drive-badge mdrive">Ã°Å¸Å½Â¯ MDrive: ${fleet.effectiveMDriveSpeed} range</span>
                    <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}">Ã°Å¸Å’Å’ JDrive: ${fleet.effectiveJDriveSpeed}${jDriveOk ? '' : ' (disabled)'}</span>
                </div>
                <div class="fleet-detail-stats">
                    <div class="stat-row">
                        <span class="stat-label">Ships:</span>
                        <span class="stat-value">${fleet.shipCount}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Damage:</span>
                        <span class="stat-value">${fleet.totalDamage}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Shields:</span>
                        <span class="stat-value">${fleet.currentShieldPoints}/${fleet.totalShieldPoints} (${shieldPercent}%)</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Hull:</span>
                        <span class="stat-value">${fleet.currentHullPoints}/${fleet.totalHullPoints} (${healthPercent}%)</span>
                    </div>
                </div>
                <div class="fleet-detail-health ${healthClass}">
                    <div class="health-bar-bg">
                        <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                    </div>
                    <span class="health-text">Health: ${healthPercent}%</span>
                </div>
                ${shipsHtml}
            </div>
        `;
    }

    async function handleLeaveSession() {
        if (!GameUIState.sessionId) return;
        stopTurnTimerCountdown();
        try {
            await GameAPI.leaveSession(GameUIState.sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        showSessionSelection();

    // Register public functions on GameUI
    GameUI.handleBuildShipClick = handleBuildShipClick;
    GameUI.handleBuildShipConfirm = handleBuildShipConfirm;
    GameUI.handleMoveFleetClick = handleMoveFleetClick;
    GameUI.showMoveFleetModal = showMoveFleetModal;
    GameUI.populateJDriveTargets = populateJDriveTargets;
    GameUI.setMoveType = setMoveType;
    GameUI.executeMoveFleet = executeMoveFleet;
    GameUI.findSystemName = findSystemName;
    GameUI.showFleetDetail = showFleetDetail;
    GameUI.handleLeaveSession = handleLeaveSession;
}());
