/**
 * DoraMCU Game UI - CARD-BASED GALAXY DISPLAY Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';

    let currentSectorKey = null; // Tracks which sector's subsectors are being shown
    let currentSubSectorSystems = []; // Systems in the current subsector for the switcher
    let currentSubSectorRef = null; // Reference to current subsector for system switcher
    let currentHexGridSubSector = null; // Reference to the subsector shown in the hex grid view

    // Generate a sector name from row/col (mirrors backend SectorBuilder.generateSectorName)
    function generateSectorName(row, col) {
        const prefixes = ['Alpha', 'Beta', 'Gamma', 'Delta', 'Epsilon', 'Zeta', 'Eta', 'Theta',
                          'Iota', 'Kappa', 'Lambda', 'Mu', 'Nu', 'Xi', 'Omicron', 'Pi',
                          'Rho', 'Sigma', 'Tau', 'Upsilon', 'Phi', 'Chi', 'Psi', 'Omega'];
        const suffixes = ['Prime', 'Nova', 'Major', 'Minor', 'Centauri', 'Vega', 'Rigel', 'Altair',
                          'Deneb', 'Sirius', 'Polaris', 'Antares', 'Betelgeuse', 'Aldebaran',
                          'Capella', 'Arcturus', 'Procyon', 'Achernar', 'Canopus', 'Fomalhaut',
                          'Regulus', 'Castor', 'Pollux', 'Spica'];
        const prefixIdx = (row * 4 + col) % prefixes.length;
        const suffixIdx = (row + col * 3) % suffixes.length;
        return `${prefixes[prefixIdx]}-${suffixes[suffixIdx]}`;
    }

    // Group subsectors by sector key
    function groupSubSectorsBySector(subSectors) {
        const map = {};
        subSectors.forEach(ss => {
            const key = `${ss.sectorRow}-${ss.sectorCol}`;
            if (!map[key]) {
                map[key] = {
                    key: key,
                    sectorRow: ss.sectorRow,
                    sectorCol: ss.sectorCol,
                    name: generateSectorName(ss.sectorRow, ss.sectorCol),
                    subSectors: [],
                    systemCount: 0,
                    contested: false,
                    ownerEmpireIds: new Set(),
                    tradeType: null
                };
            }
            map[key].subSectors.push(ss);
            map[key].systemCount += (ss.systemCount || 0);
            if (ss.contested) map[key].contested = true;
            if (ss.ownerEmpireIds) {
                ss.ownerEmpireIds.forEach(id => map[key].ownerEmpireIds.add(id));
            }
            // Use the trade type from the first subsector (all subsectors in a sector share the same trade type)
            if (!map[key].tradeType && ss.tradeType) {
                map[key].tradeType = ss.tradeType;
            }
        });
        return Object.values(map).sort((a, b) => {
            if (a.sectorRow !== b.sectorRow) return a.sectorRow - b.sectorRow;
            return a.sectorCol - b.sectorCol;
        });
    }

    // Show the sector cards view (top-level galaxy view)
    function showSectorCardsView() {
        if (!GameUIState.elements.sectorCardsView) return;
        GameUIState.elements.sectorCardsView.classList.remove('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentSectorKey = null;
        currentHexGridSubSector = null;
        // Clear search when returning to sector view
        if (GameUIState.elements.sectorSearchInput) {
            GameUIState.elements.sectorSearchInput.value = '';
            filterSectorCards('');
        }
    }

    // Show the subsector cards view (drill down from a sector)
    function showSubsectorCardsView(sectorKey) {
        if (!GameUIState.elements.subsectorCardsView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        GameUIState.elements.subsectorCardsView.classList.remove('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentSectorKey = sectorKey;
        currentHexGridSubSector = null;
        renderSubsectorCardsView(sectorKey);
    }

    // Show the 10x10 hex grid view (drill down from a subsector card)
    function showSubsectorHexGridView(subSector) {
        if (!GameUIState.elements.subsectorHexGridView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        GameUIState.elements.subsectorHexGridView.classList.remove('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentHexGridSubSector = subSector;
        renderSubsectorHexGridView(subSector);
    }

    // Show the system detail view (drill down from hex grid)
    async function showSystemDetailView(system, subSector) {
        if (!GameUIState.elements.systemDetailView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        GameUIState.elements.systemDetailView.classList.remove('hidden');
        // Store the current system ID for trade and other actions
        GameUIState.currentSystemId = system.id;
        // Show/hide trade button based on system availability
        const tradeBtn = document.getElementById('tradeSystemBtn');
        if (tradeBtn) {
            tradeBtn.style.display = 'inline-flex';
        }
        // Store the systems list for the switcher
        currentSubSectorSystems = subSector?.systems || [];
        currentSubSectorRef = subSector;
        // Populate the system selector dropdown
        if (GameUIState.elements.systemSelector && currentSubSectorSystems.length > 1) {
            GameUIState.elements.systemSelector.style.display = 'inline-block';
            GameUIState.elements.systemSelector.innerHTML = '';
            currentSubSectorSystems.forEach(sys => {
                const opt = document.createElement('option');
                opt.value = sys.id;
                opt.textContent = sys.name || `System ${sys.id}`;
                if (sys.id === system.id) opt.selected = true;
                GameUIState.elements.systemSelector.appendChild(opt);
            });
        } else if (GameUIState.elements.systemSelector) {
            GameUIState.elements.systemSelector.style.display = 'none';
        }
        // Fetch full system details from the backend
        try {
            const sessionId = localStorage.getItem('doramcu_session_id');
            if (GameUIState.sessionId) {
                const details = await GameAPI.getSystemDetails(GameUIState.sessionId, system.id);
                if (details) {
                    // Merge fetched details with the summary system data
                    const merged = { ...system, ...details };
                    renderSystemDetailView(merged, subSector);
                    return;
                }
            }
        } catch (e) {
            console.warn('Failed to fetch system details, using summary data:', e.message);
        }
        // Fallback to summary data
        renderSystemDetailView(system, subSector);
    }

    // Render the sector cards grid
    function renderSectorCardsView() {
        if (!GameUIState.elements.sectorCardsGrid || !GameUIState.gameState) return;
        const subSectors = GameUIState.gameState.subSectors || [];
        if (subSectors.length === 0) {
            GameUIState.elements.sectorCardsGrid.innerHTML = '<div class="empty-galaxy">Ã¢ÂÂ³ Galaxy is being generated...</div>';
            if (GameUIState.elements.galaxyViewStats) GameUIState.elements.galaxyViewStats.innerHTML = '';
            return;
        }

        const sectors = groupSubSectorsBySector(subSectors);
        const totalSystems = subSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);

        // Update stats
        if (GameUIState.elements.galaxyViewStats) {
            GameUIState.elements.galaxyViewStats.innerHTML = `
                <span class="galaxy-stat">Ã°Å¸Å’Å’ ${sectors.length} Sectors</span>
                <span class="galaxy-stat">Ã°Å¸â€Â¹ ${subSectors.length} Subsectors</span>
                <span class="galaxy-stat">Ã¢Â­Â ${totalSystems} Systems</span>
            `;
        }

        // Render sector cards
        GameUIState.elements.sectorCardsGrid.innerHTML = '';
        sectors.forEach(sector => {
            const card = document.createElement('div');

            // Determine ownership status with empire names
            let ownershipBadge = '';
            let ownershipClass = '';
            if (sector.ownerEmpireIds.size > 0) {
                const empireIds = Array.from(sector.ownerEmpireIds);
                const playerId = getCurrentPlayerId();
                const empireNames = empireIds.map(id => {
                    const name = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[id]) || `Empire #${id}`;
                    const isPlayer = id === playerId;
                    return { name, isPlayer, id };
                });
                const playerOwns = empireIds.includes(playerId);
                if (playerOwns && empireIds.length === 1) {
                    ownershipBadge = `<span class="ownership-badge owned" title="${GameUI.escapeHtml(empireNames[0].name)}">Ã°Å¸ÂÂ  ${GameUI.escapeHtml(empireNames[0].name)}</span>`;
                    ownershipClass = ' owned';
                } else if (playerOwns) {
                    const names = empireNames.map(e => e.isPlayer ? 'You' : GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge shared" title="${GameUI.escapeHtml(empireNames.map(e => e.name).join(', '))}">Ã°Å¸Â¤Â ${names}</span>`;
                    ownershipClass = ' shared';
                } else {
                    const names = empireNames.map(e => GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge enemy" title="${names}">Ã¢Å¡â€Ã¯Â¸Â ${names}</span>`;
                    ownershipClass = ' enemy';
                }
            } else {
                ownershipBadge = '<span class="ownership-badge neutral">Ã°Å¸Å’â€˜ Unclaimed</span>';
            }

            card.className = 'sector-card' + (sector.contested ? ' contested' : '') + ownershipClass;

            const contestedBadge = sector.contested ? '<span class="contested-badge">Ã¢Å¡Â Ã¯Â¸Â Contested</span>' : '';
            const tradeTypeBadge = sector.tradeType ? `<span class="sector-trade-type">Ã°Å¸â€œÂ¦ ${GameUI.escapeHtml(sector.tradeType)}</span>` : '';

            card.innerHTML = `
                <div class="sector-card-header">
                    <div class="sector-card-name">${GameUI.escapeHtml(sector.name)}</div>
                    ${ownershipBadge}
                </div>
                <div class="sector-card-coords">[${sector.sectorRow}, ${sector.sectorCol}]</div>
                <div class="sector-card-stats">
                    <span class="sector-stat">Ã°Å¸â€Â¹ ${sector.subSectors.length} subsector(s)</span>
                    <span class="sector-stat">Ã¢Â­Â ${sector.systemCount} system(s)</span>
                </div>
                ${tradeTypeBadge}
                ${contestedBadge}
            `;
            // Store data for search filtering
            card.dataset.searchText = (sector.name + ' ' + sector.sectorRow + ' ' + sector.sectorCol + ' ' + (sector.tradeType || '')).toLowerCase();

            card.addEventListener('click', () => {
                showSubsectorCardsView(sector.key);
            });

            GameUIState.elements.sectorCardsGrid.appendChild(card);
        });
    }

    // Filter sector cards by search query
    function filterSectorCards(query) {
        if (!GameUIState.elements.sectorCardsGrid) return;
        const cards = GameUIState.elements.sectorCardsGrid.querySelectorAll('.sector-card');
        const q = query.toLowerCase().trim();
        let visible = 0;
        cards.forEach(card => {
            if (!q || card.dataset.searchText.includes(q)) {
                card.classList.remove('galaxy-card-hidden');
                visible++;
            } else {
                card.classList.add('galaxy-card-hidden');
            }
        });
        if (GameUIState.elements.sectorSearchResults) {
            if (q) {
                GameUIState.elements.sectorSearchResults.textContent = visible + ' of ' + cards.length + ' sectors';
            } else {
                GameUIState.elements.sectorSearchResults.textContent = '';
            }
        }
    }

    // Render the subsector cards grid for a specific sector
    function renderSubsectorCardsView(sectorKey) {
        if (!GameUIState.elements.subsectorCardsGrid || !GameUIState.gameState) return;
        const subSectors = GameUIState.gameState.subSectors || [];

        // Find the sector
        const [rowStr, colStr] = sectorKey.split('-');
        const sectorRow = parseInt(rowStr);
        const sectorCol = parseInt(colStr);
        const sectorSubSectors = subSectors.filter(s =>
            s.sectorRow === sectorRow && s.sectorCol === sectorCol
        );

        // Filter: show only owned subsectors if toggle is checked
        const showOnlyOwned = GameUIState.elements.showOnlyOwnedSubsectors && GameUIState.elements.showOnlyOwnedSubsectors.checked;
        let filteredSubSectors = sectorSubSectors;
        if (showOnlyOwned) {
            const playerId = getCurrentPlayerId();
            filteredSubSectors = sectorSubSectors.filter(ss => {
                const ownerIds = (ss.ownerEmpireIds && ss.ownerEmpireIds.length > 0) ? ss.ownerEmpireIds : [];
                return ownerIds.includes(playerId);
            });
        }

        // Set title
        if (GameUIState.elements.subsectorViewTitle) {
            GameUIState.elements.subsectorViewTitle.textContent = generateSectorName(sectorRow, sectorCol) + ' Ã¢â‚¬â€ Subsectors';
        }

        // Set stats bar
        if (GameUIState.elements.subsectorViewStats) {
            const displaySubSectors = showOnlyOwned && GameUIState.elements.showOnlyOwnedSubsectors.checked ? filteredSubSectors : sectorSubSectors;
            const totalSystems = displaySubSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);
            const contestedCount = displaySubSectors.filter(s => s.contested).length;
            GameUIState.elements.subsectorViewStats.innerHTML = `
                <span class="galaxy-stat">Ã°Å¸â€Â¹ ${displaySubSectors.length} Subsectors</span>
                <span class="galaxy-stat">Ã¢Â­Â ${totalSystems} Systems</span>
                ${contestedCount > 0 ? '<span class="galaxy-stat">Ã¢Å¡Â Ã¯Â¸Â ' + contestedCount + ' Contested</span>' : ''}
            `;
        }

        if (filteredSubSectors.length === 0) {
            GameUIState.elements.subsectorCardsGrid.innerHTML = '<div class="empty-galaxy">No subsectors in this sector</div>';
            return;
        }

        // Render subsector cards
        GameUIState.elements.subsectorCardsGrid.innerHTML = '';
        filteredSubSectors.forEach(ss => {
            const card = document.createElement('div');
            card.className = 'subsector-card' + (ss.contested ? ' contested' : '');

            // Ownership Ã¢â‚¬â€ use .length for JSON arrays (backend Set serializes to array)
            let ownershipBadge = '';
            const ownerIds = (ss.ownerEmpireIds && ss.ownerEmpireIds.length > 0) ? ss.ownerEmpireIds : [];
            if (ownerIds.length > 0) {
                const playerId = getCurrentPlayerId();
                const empireNames = ownerIds.map(id => {
                    const name = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[id]) || `Empire #${id}`;
                    const isPlayer = id === playerId;
                    return { name, isPlayer, id };
                });
                const playerOwns = ownerIds.includes(playerId);
                if (playerOwns && ownerIds.length === 1) {
                    ownershipBadge = `<span class="ownership-badge owned" title="${GameUI.escapeHtml(empireNames[0].name)}">Ã°Å¸ÂÂ  ${GameUI.escapeHtml(empireNames[0].name)}</span>`;
                } else if (playerOwns) {
                    const names = empireNames.map(e => e.isPlayer ? 'You' : GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge shared" title="${GameUI.escapeHtml(empireNames.map(e => e.name).join(', '))}">Ã°Å¸Â¤Â ${names}</span>`;
                } else {
                    const names = empireNames.map(e => GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge enemy" title="${names}">Ã¢Å¡â€Ã¯Â¸Â ${names}</span>`;
                }
            } else {
                ownershipBadge = '<span class="ownership-badge neutral">Ã°Å¸Å’â€˜ Unclaimed</span>';
            }

            const contestedBadge = ss.contested ? '<span class="contested-badge">Ã¢Å¡Â Ã¯Â¸Â Contested</span>' : '';
            const systemCount = ss.systemCount || 0;
            const systems = ss.systems || [];

            // Collect system types for display
            const systemTypes = systems.map(s => s.systemType || 'Unknown');
            const dominantType = systemTypes.length > 0 ? systemTypes[0] : '';
            const systemTypeBadge = dominantType ? `<span class="system-type-badge">${GameUI.escapeHtml(dominantType)}</span>` : '';

            card.innerHTML = `
                <div class="subsector-card-header">
                    <div class="subsector-card-name">Ã°Å¸â€Â¹ ${GameUI.escapeHtml(ss.name || 'SubSector ' + ss.id)}</div>
                    ${ownershipBadge}
                </div>
                <div class="subsector-card-coords">[${ss.subRow}, ${ss.subCol}]</div>
                <div class="subsector-card-stats">
                    <span class="subsector-stat">Ã¢Â­Â ${systemCount} system(s)</span>
                    ${ss.tradeType ? `<span class="subsector-stat">Ã°Å¸â€œÂ¦ ${GameUI.escapeHtml(ss.tradeType)}</span>` : ''}
                </div>
                ${systemTypeBadge}
                ${contestedBadge}
                ${systems.length > 0 ? `
                    <div class="subsector-systems-preview">
                        ${systems.map(sys => {
                            const sysName = sys.name || `System ${sys.id || '?'}`;
                            const planetCount = sys.planetCount || 0;
                            // Determine system ownership from its planets
                            const sysPlanets = sys.planets || [];
                            const playerId = getCurrentPlayerId();
                            let sysOwned = false;
                            let sysContested = false;
                            let sysOwnerName = null;
                            const sysOwnerIds = new Set();
                            sysPlanets.forEach(p => {
                                if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                                    sysOwnerIds.add(p.ownerEmpireId);
                                    if (p.ownerEmpireId === playerId) sysOwned = true;
                                    else sysContested = true;
                                }
                            });
                            // Pick the dominant owner name
                            if (sysOwnerIds.size > 0) {
                                const dominantId = Array.from(sysOwnerIds)[0];
                                sysOwnerName = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[dominantId]) || `Empire #${dominantId}`;
                            }
                            let ownerIcon = 'Ã°Å¸Å’â€˜';
                            if (sysOwned && !sysContested) ownerIcon = 'Ã°Å¸ÂÂ ';
                            else if (sysOwned) ownerIcon = 'Ã°Å¸Â¤Â';
                            else if (sysContested) ownerIcon = 'Ã¢Å¡â€Ã¯Â¸Â';
                            const title = sysOwnerName ? `${GameUI.escapeHtml(sysName)} Ã¢â‚¬â€ ${ownerIcon} ${GameUI.escapeHtml(sysOwnerName)}` : GameUI.escapeHtml(sysName);
                            return `<span class="system-preview-item" data-system-id="${sys.id}" title="${title}">${ownerIcon} ${GameUI.escapeHtml(sysName)} (${planetCount})</span>`;
                        }).join('')}
                    </div>
                ` : ''}
            `;

            // Make individual system preview items clickable to open that specific system
            const systemItems = card.querySelectorAll('.system-preview-item');
            systemItems.forEach(item => {
                item.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const sysId = parseInt(item.getAttribute('data-system-id'));
                    const sys = systems.find(s => s.id === sysId);
                    if (sys) {
                        showSystemDetailView(sys, ss);
                    }
                });
                item.style.cursor = 'pointer';
            });

            // Clicking the card itself opens the hex grid view for this subsector
            card.addEventListener('click', () => {
                showSubsectorHexGridView(ss);
            });

            GameUIState.elements.subsectorCardsGrid.appendChild(card);
        });
    }

    // 10x10 HEX GRID VIEW Ã¢â‚¬â€ Shows all systems in a subsector

    // Render the 10x10 hex grid for a subsector
    function renderSubsectorHexGridView(subSector) {
        const canvas = GameUIState.elements.subsectorHexGridCanvas;
        if (!canvas) return;

        const subSectorName = subSector.name || `SubSector ${subSector.id}`;
        if (GameUIState.elements.subsectorHexGridTitle) {
            GameUIState.elements.subsectorHexGridTitle.textContent = `${subSectorName} Ã¢â‚¬â€ System Map`;
        }
        const systems = subSector.systems || [];
        const systemCount = systems.length;
        const totalPlanets = systems.reduce((sum, s) => sum + (s.planetCount || 0), 0);
        const totalStars = systems.reduce((sum, s) => sum + (s.starCount || 0), 0);
        if (GameUIState.elements.subsectorHexGridStats) {
            GameUIState.elements.subsectorHexGridStats.innerHTML = `
                <span class="system-stat">Ã¢Â­Â ${systemCount} Systems</span>
                <span class="system-stat">Ã¢Ëœâ‚¬Ã¯Â¸Â ${totalStars} Stars</span>
                <span class="system-stat">Ã°Å¸ÂªÂ ${totalPlanets} Planets</span>
            `;
        }

        // Size the canvas
        const container = GameUIState.elements.hexGridContainer;
        if (!container) return;
        const rect = container.getBoundingClientRect();
        canvas.width = Math.max(500, rect.width - 20);
        canvas.height = Math.max(400, rect.height - 20);

        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Background
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        // Starfield
        for (let i = 0; i < 200; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.3)`;
            ctx.fillRect(sx, sy, 1, 1);
        }

        // Draw 10x10 hex grid
        const gridSize = 10;
        const hexSize = Math.min(w, h) / (gridSize * 2.2) + 10;
        const offsetX = (w - gridSize * hexSize * 1.8) / 2;
        const offsetY = (h - gridSize * hexSize * 1.8) / 2;

        // Build a map of systems by position for quick lookup
        const systemByPos = {};
        systems.forEach(sys => {
            const x = sys.posX ?? -1;
            const y = sys.posY ?? -1;
            const z = sys.posZ ?? 0;
            const key = `${x},${y},${z}`;
            if (x >= 0 && y >= 0) {
                systemByPos[key] = sys;
            }
        });

        // Draw hex grid cells
        for (let row = 0; row < gridSize; row++) {
            for (let col = 0; col < gridSize; col++) {
                const x = offsetX + col * hexSize * 1.8 + (row % 2) * hexSize * 0.9;
                const y = offsetY + row * hexSize * 1.6;

                // Check if there's a system at this position (any z level)
                let hasSystem = false;
                let sysAtPos = null;
                for (let z = 0; z < gridSize; z++) {
                    const key = `${col},${row},${z}`;
                    if (systemByPos[key]) {
                        hasSystem = true;
                        sysAtPos = systemByPos[key];
                        break;
                    }
                }

                // Draw hex
                drawHex(ctx, x, y, hexSize * 0.55,
                    hasSystem ? 'rgba(99, 102, 241, 0.3)' : 'rgba(26, 21, 53, 0.5)',
                    hasSystem ? 'rgba(139, 92, 246, 0.6)' : 'rgba(42, 32, 80, 0.3)');

                if (hasSystem && sysAtPos) {
                    // System name on the hex
                    const sysName = sysAtPos.name || `System ${sysAtPos.id}`;
                    ctx.fillStyle = '#ffffff';
                    ctx.font = `bold ${Math.max(8, hexSize * 0.28)}px sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 2;
                    ctx.strokeText(sysName, x, y - 2);
                    ctx.fillText(sysName, x, y - 2);

                    // Planet count
                    const pc = sysAtPos.planetCount || 0;
                    const sc = sysAtPos.starCount || 0;
                    ctx.fillStyle = '#a78bfa';
                    ctx.font = `${Math.max(7, hexSize * 0.22)}px sans-serif`;
                    ctx.strokeText(`Ã¢Ëœâ‚¬${sc} Ã°Å¸ÂªÂ${pc}`, x, y + hexSize * 0.2);
                    ctx.fillText(`Ã¢Ëœâ‚¬${sc} Ã°Å¸ÂªÂ${pc}`, x, y + hexSize * 0.2);
                    ctx.lineWidth = 0.5;

                    // Ownership indicator
                    const planets = sysAtPos.planets || [];
                    const playerId = getCurrentPlayerId();
                    let owned = false;
                    let contested = false;
                    let isCapital = false;
                    planets.forEach(p => {
                        if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                            if (p.ownerEmpireId === playerId) owned = true;
                            else contested = true;
                            if (p.isCapital) isCapital = true;
                        }
                    });
                    if (isCapital) {
                        // Capital system Ã¢â‚¬â€ pulsing red ring
                        ctx.strokeStyle = '#ef4444';
                        ctx.lineWidth = 3;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        // Inner glow
                        ctx.beginPath();
                        ctx.arc(x, y, hexSize * 0.3, 0, Math.PI * 2);
                        ctx.fillStyle = 'rgba(239, 68, 68, 0.15)';
                        ctx.fill();
                        // Crown icon
                        ctx.fillStyle = '#ef4444';
                        ctx.font = `bold ${Math.max(10, hexSize * 0.35)}px sans-serif`;
                        ctx.textAlign = 'center';
                        ctx.fillText('Ã°Å¸â€˜â€˜', x, y - hexSize * 0.4);
                        ctx.lineWidth = 0.5;
                    } else if (owned) {
                        ctx.strokeStyle = '#fbbf24';
                        ctx.lineWidth = 2;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        ctx.lineWidth = 0.5;
                    } else if (contested) {
                        ctx.strokeStyle = '#ef4444';
                        ctx.lineWidth = 2;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        ctx.lineWidth = 0.5;
                    }

                    // Make hex clickable
                    canvas.onclick = (e) => {
                        const canvasRect = canvas.getBoundingClientRect();
                        const mx = e.clientX - canvasRect.left;
                        const my = e.clientY - canvasRect.top;
                        // Check all hexes for click
                        for (let r = 0; r < gridSize; r++) {
                            for (let c = 0; c < gridSize; c++) {
                                const hx = offsetX + c * hexSize * 1.8 + (r % 2) * hexSize * 0.9;
                                const hy = offsetY + r * hexSize * 1.6;
                                const dist = Math.sqrt((mx - hx) ** 2 + (my - hy) ** 2);
                                if (dist < hexSize * 0.55) {
                                    // Find system at this position
                                    for (let z = 0; z < gridSize; z++) {
                                        const key = `${c},${r},${z}`;
                                        if (systemByPos[key]) {
                                            showSystemDetailView(systemByPos[key], subSector);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    };
                } else {
                    // Empty hex label
                    ctx.fillStyle = 'rgba(100, 100, 140, 0.4)';
                    ctx.font = `${Math.max(7, hexSize * 0.2)}px sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.fillText(`${col},${row}`, x, y + 3);
                }
            }
        }

        // Legend
        if (GameUIState.elements.hexGridLegend) {
            GameUIState.elements.hexGridLegend.innerHTML = `
                <div class="legend-item"><div class="legend-dot" style="background: rgba(99, 102, 241, 0.5);"></div><span>System</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 3px solid #ef4444;"></div><span>Ã°Å¸â€˜â€˜ Capital</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 2px solid #fbbf24;"></div><span>Owned</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 2px solid #ef4444;"></div><span>Contested</span></div>
                <div class="legend-item"><div class="legend-dot" style="background: rgba(26, 21, 53, 0.5);"></div><span>Empty</span></div>
            `;
        }
    }

    // Draw a filled hexagon
    function drawHex(ctx, cx, cy, size, fillColor, strokeColor) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const x = cx + size * Math.cos(angle);
            const y = cy + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.fillStyle = fillColor;
        ctx.fill();
        ctx.strokeStyle = strokeColor;
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    // Draw just the hex stroke outline
    function drawHexStroke(ctx, cx, cy, size) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const x = cx + size * Math.cos(angle);
            const y = cy + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.stroke();
    }

    // Render the system detail view with ALL galactic entities
    function renderSystemDetailView(system, subSector) {
        if (!GameUIState.elements.systemDetailTitle || !GameUIState.elements.systemMapCanvas) return;

        const systemName = system.name || `System ${system.id || '?'}`;
        const systemType = system.systemType || 'Unknown';
        const starCount = system.starCount || 0;
        const planetCount = system.planetCount || 0;
        const bodyCount = system.bodyCount || 0;
        const planets = system.planets || [];
        const stars = system.stars || [];
        const moons = system.moons || [];
        const asteroids = system.asteroids || [];
        const comets = system.comets || [];
        const systemFleets = system.fleets || [];
        const stations = system.stations || [];
        const gasGiants = system.gasGiants || [];
        const asteroidBelts = system.asteroidBelts || [];
        const nebulas = system.nebulas || [];
        const blackHoles = system.blackHoles || [];

        // Use DB enrichment counts when in-memory lists are empty
        const dbMoons = system.dbMoonCount || 0;
        const dbAsteroids = system.dbAsteroidCount || 0;
        const dbComets = system.dbCometCount || 0;
        const dbGasGiants = system.dbGasGiantCount || 0;
        const dbAsteroidBelts = system.dbAsteroidBeltCount || 0;
        const dbNebulas = system.dbNebulaCount || 0;
        const dbBlackHoles = system.dbBlackHoleCount || 0;
        const dbStations = system.dbSpaceStationCount || 0;
        const dbStars = system.dbStarCount || 0;

        // Display counts: use in-memory list lengths if available, otherwise DB counts
        const displayMoons = moons.length > 0 ? moons.length : dbMoons;
        const displayAsteroids = asteroids.length > 0 ? asteroids.length : dbAsteroids;
        const displayComets = comets.length > 0 ? comets.length : dbComets;
        const displayGasGiants = gasGiants.length > 0 ? gasGiants.length : dbGasGiants;
        const displayAsteroidBelts = asteroidBelts.length > 0 ? asteroidBelts.length : dbAsteroidBelts;
        const displayNebulas = nebulas.length > 0 ? nebulas.length : dbNebulas;
        const displayBlackHoles = blackHoles.length > 0 ? blackHoles.length : dbBlackHoles;
        const displayStations = stations.length > 0 ? stations.length : dbStations;
        const displayStars = stars.length > 0 ? stars.length : dbStars;

        // Set title
        GameUIState.elements.systemDetailTitle.textContent = systemName;

        // Set stats
        if (GameUIState.elements.systemDetailStats) {
            GameUIState.elements.systemDetailStats.innerHTML = `
                <span class="system-stat">Ã°Å¸â€œâ€¹ ${GameUI.escapeHtml(systemType)}</span>
                <span class="system-stat">Ã¢Ëœâ‚¬Ã¯Â¸Â ${displayStars} Stars</span>
                <span class="system-stat">Ã°Å¸ÂªÂ ${planetCount} Planets</span>
                <span class="system-stat">Ã°Å¸Å’â€˜ ${displayMoons} Moons</span>
                <span class="system-stat">Ã°Å¸ÂªÂ¨ ${displayAsteroids} Asteroids</span>
                <span class="system-stat">Ã¢Ëœâ€žÃ¯Â¸Â ${displayComets} Comets</span>
                <span class="system-stat">Ã°Å¸Å’â‚¬ ${displayGasGiants} Gas Giants</span>
                <span class="system-stat">Ã°Å¸â€™Â« ${displayAsteroidBelts} Asteroid Belts</span>
                <span class="system-stat">Ã°Å¸Å’Å’ ${displayNebulas} Nebulas</span>
                <span class="system-stat">Ã°Å¸â€¢Â³Ã¯Â¸Â ${displayBlackHoles} Black Holes</span>
                <span class="system-stat">Ã°Å¸Å¡â‚¬ ${systemFleets.length} Fleets</span>
                <span class="system-stat">Ã°Å¸â€ºÂ°Ã¯Â¸Â ${displayStations} Stations</span>
            `;
        }

        // Render all entities in the info panel
        if (GameUIState.elements.systemInfoPanel) {
            let panelHtml = '';

            // Stars
            if (stars.length > 0) {
                panelHtml += '<h3>Ã¢Ëœâ‚¬Ã¯Â¸Â Stars</h3><div class="entity-list">';
                stars.forEach(star => {
                    const name = star.name || `Star ${star.id}`;
                    panelHtml += `<div class="entity-item star-entity" title="Position: (${star.x}, ${star.y}, ${star.z})">
                        <span class="entity-icon">Ã¢Ëœâ‚¬Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${star.x}, ${star.y}, ${star.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Planets
            if (planets.length > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ Planets</h3><div class="entity-list">';
                planets.forEach(planet => {
                    const name = planet.name || `Planet ${planet.id}`;
                    const type = planet.planetType || 'Unknown';
                    const playerId = getCurrentPlayerId();
                    const ownerEmpireId = planet.ownerEmpireId;
                    let ownerIcon = 'Ã°Å¸Å’â€˜';
                    let ownerName = 'Unclaimed';
                    if (ownerEmpireId !== null && ownerEmpireId !== undefined) {
                        ownerName = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[ownerEmpireId]) || `Empire #${ownerEmpireId}`;
                        if (ownerEmpireId === playerId) ownerIcon = 'Ã°Å¸ÂÂ ';
                        else ownerIcon = 'Ã¢Å¡â€Ã¯Â¸Â';
                    }
                    const isCapital = planet.isCapital === true || (ownerEmpireId === playerId && planet.isCapital);
                    const sizeBadge = planet.planetSize ? `<span class="planet-size-badge">${GameUI.escapeHtml(planet.planetSize)}</span>` : '';
                    const mineralBadge = planet.mineralQuality ? `<span class="planet-mineral-badge">${GameUI.escapeHtml(planet.mineralQuality)}</span>` : '';
                    const capitalBadge = isCapital ? '<span class="planet-capital-badge">Ã°Å¸â€˜â€˜ CAPITAL</span>' : '';
                    const ownerBadge = `<span class="planet-owner-badge" title="${GameUI.escapeHtml(ownerName)}">${ownerIcon} ${GameUI.escapeHtml(ownerName)}</span>`;
                    panelHtml += `<div class="entity-item planet-entity" title="Position: (${planet.x}, ${planet.y}, ${planet.z})">
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-type">${GameUI.escapeHtml(type)}</span>
                        <span class="planet-detail-info">
                            ${capitalBadge}${sizeBadge}${mineralBadge}
                        </span>
                        <span class="planet-owner-info">${ownerBadge}</span>
                        <span class="entity-pos">(${planet.x}, ${planet.y}, ${planet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Moons
            if (moons.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â€˜ Moons</h3><div class="entity-list">';
                moons.forEach(moon => {
                    const name = moon.name || `Moon ${moon.id}`;
                    panelHtml += `<div class="entity-item moon-entity" title="Position: (${moon.x}, ${moon.y}, ${moon.z})">
                        <span class="entity-icon">Ã°Å¸Å’â€˜</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${moon.x}, ${moon.y}, ${moon.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Asteroids
            if (asteroids.length > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ¨ Asteroids</h3><div class="entity-list">';
                asteroids.forEach(ast => {
                    const name = ast.name || `Asteroid ${ast.id}`;
                    panelHtml += `<div class="entity-item asteroid-entity" title="Position: (${ast.x}, ${ast.y}, ${ast.z})">
                        <span class="entity-icon">Ã°Å¸ÂªÂ¨</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${ast.x}, ${ast.y}, ${ast.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Comets
            if (comets.length > 0) {
                panelHtml += '<h3>Ã¢Ëœâ€žÃ¯Â¸Â Comets</h3><div class="entity-list">';
                comets.forEach(comet => {
                    const name = comet.name || `Comet ${comet.id}`;
                    panelHtml += `<div class="entity-item comet-entity" title="Position: (${comet.x}, ${comet.y}, ${comet.z})">
                        <span class="entity-icon">Ã¢Ëœâ€žÃ¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${comet.x}, ${comet.y}, ${comet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Fleets
            if (systemFleets.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å¡â‚¬ Fleets</h3><div class="entity-list">';
                systemFleets.forEach(fleet => {
                    const name = fleet.name || `Fleet ${fleet.id}`;
                    panelHtml += `<div class="entity-item fleet-entity" title="Position: (${fleet.x}, ${fleet.y}, ${fleet.z})">
                        <span class="entity-icon">Ã°Å¸Å¡â‚¬</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${fleet.x}, ${fleet.y}, ${fleet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Space Stations
            if (stations.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€ºÂ°Ã¯Â¸Â Space Stations</h3><div class="entity-list">';
                stations.forEach(station => {
                    const name = station.name || `Station ${station.id}`;
                    panelHtml += `<div class="entity-item station-entity" title="Position: (${station.x}, ${station.y}, ${station.z})">
                        <span class="entity-icon">Ã°Å¸â€ºÂ°Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${station.x}, ${station.y}, ${station.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Gas Giants
            if (gasGiants.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â‚¬ Gas Giants</h3><div class="entity-list">';
                gasGiants.forEach(gg => {
                    const name = gg.name || `Gas Giant ${gg.id}`;
                    panelHtml += `<div class="entity-item gas-giant-entity" title="Position: (${gg.x}, ${gg.y}, ${gg.z})">
                        <span class="entity-icon">Ã°Å¸Å’â‚¬</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${gg.x}, ${gg.y}, ${gg.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Asteroid Belts
            if (asteroidBelts.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€™Â« Asteroid Belts</h3><div class="entity-list">';
                asteroidBelts.forEach(belt => {
                    const name = belt.name || `Asteroid Belt ${belt.id}`;
                    panelHtml += `<div class="entity-item asteroid-belt-entity" title="Position: (${belt.x}, ${belt.y}, ${belt.z})">
                        <span class="entity-icon">Ã°Å¸â€™Â«</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${belt.x}, ${belt.y}, ${belt.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Nebulas
            if (nebulas.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’Å’ Nebulas</h3><div class="entity-list">';
                nebulas.forEach(neb => {
                    const name = neb.name || `Nebula ${neb.id}`;
                    panelHtml += `<div class="entity-item nebula-entity" title="Position: (${neb.x}, ${neb.y}, ${neb.z})">
                        <span class="entity-icon">Ã°Å¸Å’Å’</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${neb.x}, ${neb.y}, ${neb.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Black Holes
            if (blackHoles.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€¢Â³Ã¯Â¸Â Black Holes</h3><div class="entity-list">';
                blackHoles.forEach(bh => {
                    const name = bh.name || `Black Hole ${bh.id}`;
                    panelHtml += `<div class="entity-item black-hole-entity" title="Position: (${bh.x}, ${bh.y}, ${bh.z})">
                        <span class="entity-icon">Ã°Å¸â€¢Â³Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${bh.x}, ${bh.y}, ${bh.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Show DB enrichment info when in-memory lists are empty
            if (moons.length === 0 && dbMoons > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â€˜ Moons</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’â€˜</span><span class="entity-name">${dbMoons} moon(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (asteroids.length === 0 && dbAsteroids > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ¨ Asteroids</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸ÂªÂ¨</span><span class="entity-name">${dbAsteroids} asteroid(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (comets.length === 0 && dbComets > 0) {
                panelHtml += '<h3>Ã¢Ëœâ€žÃ¯Â¸Â Comets</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã¢Ëœâ€žÃ¯Â¸Â</span><span class="entity-name">${dbComets} comet(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (gasGiants.length === 0 && dbGasGiants > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â‚¬ Gas Giants</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’â‚¬</span><span class="entity-name">${dbGasGiants} gas giant(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (asteroidBelts.length === 0 && dbAsteroidBelts > 0) {
                panelHtml += '<h3>Ã°Å¸â€™Â« Asteroid Belts</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€™Â«</span><span class="entity-name">${dbAsteroidBelts} asteroid belt(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (nebulas.length === 0 && dbNebulas > 0) {
                panelHtml += '<h3>Ã°Å¸Å’Å’ Nebulas</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’Å’</span><span class="entity-name">${dbNebulas} nebula(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (blackHoles.length === 0 && dbBlackHoles > 0) {
                panelHtml += '<h3>Ã°Å¸â€¢Â³Ã¯Â¸Â Black Holes</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€¢Â³Ã¯Â¸Â</span><span class="entity-name">${dbBlackHoles} black hole(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (stations.length === 0 && dbStations > 0) {
                panelHtml += '<h3>Ã°Å¸â€ºÂ°Ã¯Â¸Â Space Stations</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€ºÂ°Ã¯Â¸Â</span><span class="entity-name">${dbStations} station(s) in this system</span></div>`;
                panelHtml += '</div>';
            }

            // Empty state
            if (planets.length === 0 && stars.length === 0 && moons.length === 0 &&
                asteroids.length === 0 && comets.length === 0 && systemFleets.length === 0 &&
                stations.length === 0 && gasGiants.length === 0 && asteroidBelts.length === 0 &&
                nebulas.length === 0 && blackHoles.length === 0 &&
                dbMoons === 0 && dbAsteroids === 0 && dbComets === 0 && dbGasGiants === 0 &&
                dbAsteroidBelts === 0 && dbNebulas === 0 && dbBlackHoles === 0 && dbStations === 0) {
                panelHtml = '<div class="empty-list">No entity data for this system</div>';
            }

            GameUIState.elements.systemInfoPanel.innerHTML = panelHtml;
        }

        // Render system map with ALL entities (deferred for layout)
        requestAnimationFrame(() => renderSystemMap(system, subSector));
    }

    // Render the system map canvas showing ALL galactic entities
    function renderSystemMap(system, subSector) {
        const canvas = GameUIState.elements.systemMapCanvas;
        if (!canvas) return;

        const container = canvas.parentElement;
        if (!container) return;

        // Size the canvas to fit the container
        const rect = container.getBoundingClientRect();
        canvas.width = Math.max(400, rect.width - 20);
        canvas.height = Math.max(300, rect.height - 20);

        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Background
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        // Draw starfield
        for (let i = 0; i < 150; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.4)`;
            ctx.fillRect(sx, sy, 1, 1);
        }

        const centerX = w / 2;
        const centerY = h / 2;
        const systemName = system.name || `System ${system.id}`;

        // Collect all entities from the system data
        const stars = system.stars || [];
        const planets = system.planets || [];
        const moons = system.moons || [];
        const asteroids = system.asteroids || [];
        const comets = system.comets || [];
        const systemFleets = system.fleets || [];
        const stations = system.stations || [];
        const gasGiants = system.gasGiants || [];
        const asteroidBelts = system.asteroidBelts || [];
        const nebulas = system.nebulas || [];
        const blackHoles = system.blackHoles || [];

        // Calculate a scale factor based on entity positions
        // Use the max coordinate range to determine scale
        let maxCoord = 1;
        const allEntities = [...stars, ...planets, ...moons, ...asteroids, ...comets, ...systemFleets, ...stations, ...gasGiants, ...asteroidBelts, ...nebulas, ...blackHoles];
        allEntities.forEach(e => {
            if (e.x !== undefined) maxCoord = Math.max(maxCoord, Math.abs(e.x));
            if (e.y !== undefined) maxCoord = Math.max(maxCoord, Math.abs(e.y));
        });
        const scale = Math.min(w, h) / 4 / Math.max(maxCoord, 1);
        const toX = (x) => centerX + x * scale;
        const toY = (y) => centerY + y * scale;

        // Draw orbit rings for visual structure
        const orbitRings = new Set();
        planets.forEach(p => {
            if (p.x !== undefined && p.y !== undefined) {
                const dist = Math.sqrt(p.x * p.x + p.y * p.y);
                if (dist > 0) orbitRings.add(dist);
            }
        });
        orbitRings.forEach(dist => {
            ctx.beginPath();
            ctx.arc(centerX, centerY, dist * scale, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.06)';
            ctx.lineWidth = 0.5;
            ctx.stroke();
        });

        // Draw asteroids (small gray dots)
        asteroids.forEach((ast, idx) => {
            const ax = ast.x !== undefined ? toX(ast.x) : centerX + (idx - asteroids.length / 2) * 15;
            const ay = ast.y !== undefined ? toY(ast.y) : centerY + (idx % 3 - 1) * 15;
            ctx.beginPath();
            ctx.arc(ax, ay, 2, 0, Math.PI * 2);
            ctx.fillStyle = '#9ca3af';
            ctx.fill();
        });

        // Draw comets (small blue dots with tail)
        comets.forEach((comet, idx) => {
            const cx = comet.x !== undefined ? toX(comet.x) : centerX + (idx - comets.length / 2) * 20;
            const cy = comet.y !== undefined ? toY(comet.y) : centerY + (idx % 3 - 1) * 20;
            // Tail
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.lineTo(cx + 8, cy - 4);
            ctx.strokeStyle = 'rgba(96, 165, 250, 0.5)';
            ctx.lineWidth = 1;
            ctx.stroke();
            // Body
            ctx.beginPath();
            ctx.arc(cx, cy, 3, 0, Math.PI * 2);
            ctx.fillStyle = '#60a5fa';
            ctx.fill();
        });

        // Draw stars
        stars.forEach((star, idx) => {
            const sx = star.x !== undefined ? toX(star.x) : centerX;
            const sy = star.y !== undefined ? toY(star.y) : centerY;
            // Star glow
            ctx.beginPath();
            ctx.arc(sx, sy, 20, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(252, 211, 77, 0.1)';
            ctx.fill();
            // Star body
            ctx.beginPath();
            ctx.arc(sx, sy, 10, 0, Math.PI * 2);
            const starGrad = ctx.createRadialGradient(sx, sy, 0, sx, sy, 10);
            starGrad.addColorStop(0, '#fcd34d');
            starGrad.addColorStop(0.5, '#fb923c');
            starGrad.addColorStop(1, 'rgba(251, 146, 60, 0)');
            ctx.fillStyle = starGrad;
            ctx.fill();
            // Star name
            const starName = star.name || `Star ${star.id}`;
            ctx.fillStyle = '#fcd34d';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(starName, sx, sy - 14);
            ctx.fillText(starName, sx, sy - 14);
            ctx.lineWidth = 0.5;
        });

        // Draw planets
        planets.forEach((planet, idx) => {
            const px = planet.x !== undefined ? toX(planet.x) : centerX + Math.cos((idx / Math.max(planets.length, 1)) * Math.PI * 2 - Math.PI / 2) * (50 + idx * 35);
            const py = planet.y !== undefined ? toY(planet.y) : centerY + Math.sin((idx / Math.max(planets.length, 1)) * Math.PI * 2 - Math.PI / 2) * (50 + idx * 35);
            const planetName = planet.name || `Planet ${planet.id}`;
            const planetType = planet.planetType || 'Unknown';

            // Planet color by type
            let planetColor = '#6366f1';
            if (planetType.includes('Desert')) planetColor = '#fb923c';
            else if (planetType.includes('Water')) planetColor = '#3b82f6';
            else if (planetType.includes('Ice')) planetColor = '#22d3ee';
            else if (planetType.includes('Lava')) planetColor = '#f87171';
            else if (planetType.includes('Garden')) planetColor = '#34d399';
            else if (planetType.includes('Gas')) planetColor = '#a78bfa';

            // Planet body
            const planetSize = Math.max(4, Math.min(8, (planet.size || 5)));
            ctx.beginPath();
            ctx.arc(px, py, planetSize, 0, Math.PI * 2);
            ctx.fillStyle = planetColor;
            ctx.fill();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
            ctx.lineWidth = 1;
            ctx.stroke();

            // Ownership ring
            if (planet.ownerEmpireId !== null && planet.ownerEmpireId !== undefined) {
                const isPlayer = planet.ownerEmpireId === getCurrentPlayerId();
                ctx.beginPath();
                ctx.arc(px, py, planetSize + 3, 0, Math.PI * 2);
                ctx.strokeStyle = isPlayer ? '#fbbf24' : '#ef4444';
                ctx.lineWidth = 1.5;
                ctx.stroke();
                ctx.lineWidth = 0.5;
            }

            // Planet name
            ctx.fillStyle = '#ffffff';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(planetName, px, py - planetSize - 5);
            ctx.fillText(planetName, px, py - planetSize - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw moons (small circles near their planets)
        moons.forEach((moon, idx) => {
            const mx = moon.x !== undefined ? toX(moon.x) : centerX + (idx - moons.length / 2) * 12;
            const my = moon.y !== undefined ? toY(moon.y) : centerY + (idx % 3 - 1) * 12;
            ctx.beginPath();
            ctx.arc(mx, my, 2.5, 0, Math.PI * 2);
            ctx.fillStyle = '#d1d5db';
            ctx.fill();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)';
            ctx.lineWidth = 0.5;
            ctx.stroke();
        });

        // Draw space stations (diamond shape)
        stations.forEach((station) => {
            const sx = station.x !== undefined ? toX(station.x) : centerX + 40;
            const sy = station.y !== undefined ? toY(station.y) : centerY + 40;
            const size = 5;
            ctx.beginPath();
            ctx.moveTo(sx, sy - size);
            ctx.lineTo(sx + size, sy);
            ctx.lineTo(sx, sy + size);
            ctx.lineTo(sx - size, sy);
            ctx.closePath();
            ctx.fillStyle = '#f59e0b';
            ctx.fill();
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 1;
            ctx.stroke();
            // Station name
            const stationName = station.name || `Station ${station.id}`;
            ctx.fillStyle = '#f59e0b';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(stationName, sx, sy - size - 4);
            ctx.fillText(stationName, sx, sy - size - 4);
            ctx.lineWidth = 0.5;
        });

        // Draw gas giants (large striped circles)
        gasGiants.forEach((gg) => {
            const gx = gg.x !== undefined ? toX(gg.x) : centerX + 60;
            const gy = gg.y !== undefined ? toY(gg.y) : centerY + 60;
            const ggSize = 12;
            // Glow
            ctx.beginPath();
            ctx.arc(gx, gy, ggSize + 6, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(168, 85, 247, 0.1)';
            ctx.fill();
            // Body
            ctx.beginPath();
            ctx.arc(gx, gy, ggSize, 0, Math.PI * 2);
            const ggGrad = ctx.createRadialGradient(gx, gy, 0, gx, gy, ggSize);
            ggGrad.addColorStop(0, '#c084fc');
            ggGrad.addColorStop(0.5, '#a855f7');
            ggGrad.addColorStop(1, '#7e22ce');
            ctx.fillStyle = ggGrad;
            ctx.fill();
            // Bands
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
            ctx.lineWidth = 1;
            for (let b = -1; b <= 1; b++) {
                ctx.beginPath();
                ctx.ellipse(gx, gy + b * 4, ggSize * 0.8, 2, 0, 0, Math.PI * 2);
                ctx.stroke();
            }
            ctx.lineWidth = 0.5;
            // Name
            const ggName = gg.name || `Gas Giant ${gg.id}`;
            ctx.fillStyle = '#c084fc';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(ggName, gx, gy - ggSize - 5);
            ctx.fillText(ggName, gx, gy - ggSize - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw asteroid belts (ring of small dots)
        asteroidBelts.forEach((belt) => {
            const bx = belt.x !== undefined ? toX(belt.x) : centerX + 80;
            const by = belt.y !== undefined ? toY(belt.y) : centerY + 80;
            const beltRadius = 15;
            // Ring
            ctx.beginPath();
            ctx.ellipse(bx, by, beltRadius, beltRadius * 0.4, 0.3, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(156, 163, 175, 0.4)';
            ctx.lineWidth = 3;
            ctx.stroke();
            // Dots along the ring
            for (let d = 0; d < 12; d++) {
                const angle = (d / 12) * Math.PI * 2;
                const dx = bx + Math.cos(angle + 0.3) * beltRadius;
                const dy = by + Math.sin(angle + 0.3) * beltRadius * 0.4;
                ctx.beginPath();
                ctx.arc(dx, dy, 1.5, 0, Math.PI * 2);
                ctx.fillStyle = '#9ca3af';
                ctx.fill();
            }
            ctx.lineWidth = 0.5;
            // Name
            const beltName = belt.name || `Asteroid Belt ${belt.id}`;
            ctx.fillStyle = '#9ca3af';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(beltName, bx, by - beltRadius - 5);
            ctx.fillText(beltName, bx, by - beltRadius - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw nebulas (cloud-like shapes)
        nebulas.forEach((neb) => {
            const nx = neb.x !== undefined ? toX(neb.x) : centerX - 70;
            const ny = neb.y !== undefined ? toY(neb.y) : centerY - 70;
            // Cloud layers
            for (let layer = 3; layer >= 0; layer--) {
                ctx.beginPath();
                ctx.arc(nx + layer * 3 - 6, ny + layer * 2 - 4, 18 - layer * 2, 0, Math.PI * 2);
                const nebGrad = ctx.createRadialGradient(nx, ny, 0, nx, ny, 18 - layer * 2);
                nebGrad.addColorStop(0, `rgba(56, 189, 248, ${0.15 - layer * 0.03})`);
                nebGrad.addColorStop(1, 'rgba(56, 189, 248, 0)');
                ctx.fillStyle = nebGrad;
                ctx.fill();
            }
            // Name
            const nebName = neb.name || `Nebula ${neb.id}`;
            ctx.fillStyle = '#38bdf8';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(nebName, nx, ny - 20);
            ctx.fillText(nebName, nx, ny - 20);
            ctx.lineWidth = 0.5;
        });

        // Draw black holes (dark circles with accretion disk)
        blackHoles.forEach((bh) => {
            const bhx = bh.x !== undefined ? toX(bh.x) : centerX - 90;
            const bhy = bh.y !== undefined ? toY(bh.y) : centerY + 90;
            // Accretion disk
            ctx.beginPath();
            ctx.ellipse(bhx, bhy, 20, 6, 0.5, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(251, 146, 60, 0.5)';
            ctx.lineWidth = 2;
            ctx.stroke();
            // Event horizon
            ctx.beginPath();
            ctx.arc(bhx, bhy, 8, 0, Math.PI * 2);
            ctx.fillStyle = '#000000';
            ctx.fill();
            ctx.strokeStyle = 'rgba(251, 146, 60, 0.8)';
            ctx.lineWidth = 1.5;
            ctx.stroke();
            ctx.lineWidth = 0.5;
            // Name
            const bhName = bh.name || `Black Hole ${bh.id}`;
            ctx.fillStyle = '#fb923c';
            ctx.font = 'bold 7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(bhName, bhx, bhy - 14);
            ctx.fillText(bhName, bhx, bhy - 14);
            ctx.lineWidth = 0.5;
        });

        // Draw fleets with MDrive range
        systemFleets.forEach((fleet, idx) => {
            const fx = fleet.x !== undefined ? toX(fleet.x) : centerX + Math.cos((idx / Math.max(systemFleets.length, 1)) * Math.PI * 2) * 60;
            const fy = fleet.y !== undefined ? toY(fleet.y) : centerY + Math.sin((idx / Math.max(systemFleets.length, 1)) * Math.PI * 2) * 60;

            // Fleet marker (triangle)
            ctx.save();
            ctx.translate(fx, fy);
            ctx.beginPath();
            ctx.moveTo(0, -7);
            ctx.lineTo(-5, 5);
            ctx.lineTo(5, 5);
            ctx.closePath();
            ctx.fillStyle = '#34d399';
            ctx.fill();
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 1.5;
            ctx.stroke();
            ctx.restore();

            // Fleet name
            const fleetName = fleet.name || `Fleet ${fleet.id}`;
            ctx.fillStyle = '#34d399';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(fleetName, fx, fy + 14);
            ctx.fillText(fleetName, fx, fy + 14);
            ctx.lineWidth = 0.5;
        });

        // Draw system name
        ctx.fillStyle = '#c084fc';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 3;
        ctx.strokeText(systemName, centerX, 20);
        ctx.fillStyle = '#c084fc';
        ctx.fillText(systemName, centerX, 20);
        ctx.lineWidth = 0.5;

        // Draw legend
        if (GameUIState.elements.systemMapLegend) {
            let legendHtml = '';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #fcd34d;"></div><span>Star</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #6366f1;"></div><span>Planet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #d1d5db;"></div><span>Moon</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #9ca3af;"></div><span>Asteroid</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #60a5fa;"></div><span>Comet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #34d399;"></div><span>Fleet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #f59e0b;"></div><span>Station</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="border: 2px solid #fbbf24;"></div><span>Owned</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="border: 2px solid #ef4444;"></div><span>Enemy</span></div>';
            GameUIState.elements.systemMapLegend.innerHTML = legendHtml;
        }

    // Register public functions on GameUI
    GameUI.generateSectorName = generateSectorName;
    GameUI.groupSubSectorsBySector = groupSubSectorsBySector;
    GameUI.showSectorCardsView = showSectorCardsView;
    GameUI.showSubsectorCardsView = showSubsectorCardsView;
    GameUI.showSubsectorHexGridView = showSubsectorHexGridView;
    GameUI.showSystemDetailView = showSystemDetailView;
    GameUI.renderSectorCardsView = renderSectorCardsView;
    GameUI.totalSystems = totalSystems;
    GameUI.empireNames = empireNames;
    GameUI.names = names;
    GameUI.filterSectorCards = filterSectorCards;
    GameUI.renderSubsectorCardsView = renderSubsectorCardsView;
    GameUI.sectorSubSectors = sectorSubSectors;
    GameUI.contestedCount = contestedCount;
    GameUI.systemTypes = systemTypes;
    GameUI.sys = sys;
    GameUI.renderSubsectorHexGridView = renderSubsectorHexGridView;
    GameUI.totalPlanets = totalPlanets;
    GameUI.totalStars = totalStars;
    GameUI.drawHex = drawHex;
    GameUI.drawHexStroke = drawHexStroke;
    GameUI.renderSystemDetailView = renderSystemDetailView;
    GameUI.renderSystemMap = renderSystemMap;
    GameUI.toX = toX;
    GameUI.toY = toY;
}());
