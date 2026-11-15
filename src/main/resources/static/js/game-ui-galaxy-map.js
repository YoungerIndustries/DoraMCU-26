/**
 * DoraMCU Game UI - GALAXY MAP Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';







    let galaxyMouseX = 0;
    let galaxyMouseY = 0;

    function setupGalaxyMap() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;

        canvas.addEventListener('mousedown', (e) => {
            GameUIState.isDragging = true;
            GameUIState.dragStartX = e.clientX - GameUIState.mapOffsetX;
            GameUIState.dragStartY = e.clientY - GameUIState.mapOffsetY;
        });

        canvas.addEventListener('mousemove', (e) => {
            if (GameUIState.isDragging) {
                GameUIState.mapOffsetX = e.clientX - GameUIState.dragStartX;
                GameUIState.mapOffsetY = e.clientY - GameUIState.dragStartY;
            }
            // Track mouse position for hover tooltips
            const rect = canvas.getBoundingClientRect();
            galaxyMouseX = (e.clientX - rect.left - GameUIState.mapOffsetX) / GameUIState.mapScale;
            galaxyMouseY = (e.clientY - rect.top - GameUIState.mapOffsetY) / GameUIState.mapScale;
            renderGalaxyMap();
        });

        canvas.addEventListener('mouseup', () => { GameUIState.isDragging = false; });
        canvas.addEventListener('mouseleave', () => { GameUIState.isDragging = false; });

        // Back to sector map button (legacy)
        if (GameUIState.elements.backToSectorMapBtn) {
            GameUIState.elements.backToSectorMapBtn.addEventListener('click', () => {
                showSectorMapView();
            });
        }

        // New card-based navigation buttons
        if (GameUIState.elements.backToSectorsBtn) {
            GameUIState.elements.backToSectorsBtn.addEventListener('click', () => {
                showSectorCardsView();
            });
        }
        // Back from hex grid to subsector cards
        if (GameUIState.elements.backToSubsectorCardsBtn) {
            GameUIState.elements.backToSubsectorCardsBtn.addEventListener('click', () => {
                showSubsectorCardsView(currentSectorKey);
            });
        }
        // Back from system detail to hex grid
        if (GameUIState.elements.backToSubsectorsBtn2) {
            GameUIState.elements.backToSubsectorsBtn2.addEventListener('click', () => {
                // Hide trade button when leaving system detail
                const tradeBtn = document.getElementById('tradeSystemBtn');
                if (tradeBtn) tradeBtn.style.display = 'none';
                if (currentHexGridSubSector) {
                    showSubsectorHexGridView(currentHexGridSubSector);
                } else {
                    showSubsectorCardsView(currentSectorKey);
                }
            });
        }
        // System selector dropdown — switch between systems in the current subsector
        if (GameUIState.elements.systemSelector) {
            GameUIState.elements.systemSelector.addEventListener('change', () => {
                const sysId = parseInt(GameUIState.elements.systemSelector.value);
                const sys = currentSubSectorSystems.find(s => s.id === sysId);
                if (sys && currentSubSectorRef) {
                    GameUIState.currentSystemId = sys.id;
                    renderSystemDetailView(sys, currentSubSectorRef);
                }
            });
        }
        // Trade button in system detail view
        const tradeSystemBtn = document.getElementById('tradeSystemBtn');
        if (tradeSystemBtn) {
            tradeSystemBtn.addEventListener('click', () => {
                if (typeof TradeUI !== 'undefined' && TradeUI.showTradeModal && GameUIState.sessionId && GameUIState.currentSystemId) {
                    TradeUI.showTradeModal(GameUIState.sessionId, GameUIState.currentSystemId);
                } else {
                    GameUI.showToast('Navigate to a system first', 'warning');
                }
            });
        }
        // Show Only Owned toggle — re-render subsector cards when changed
        if (GameUIState.elements.showOnlyOwnedSubsectors) {
            GameUIState.elements.showOnlyOwnedSubsectors.addEventListener('change', () => {
                if (currentSectorKey) {
                    renderSubsectorCardsView(currentSectorKey);
                }
            });
        }

        // Back to subsectors button
        if (GameUIState.elements.backToSubsectorsBtn) {
            GameUIState.elements.backToSubsectorsBtn.addEventListener('click', () => {
                showSubsectorListInView();
            });
        }

        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const factor = e.deltaY < 0 ? 1.1 : 0.9;
            zoomGalaxy(factor);
        });

        canvas.addEventListener('click', (e) => {
            if (Math.abs(e.clientX - GameUIState.dragStartX - GameUIState.mapOffsetX) > 5 || Math.abs(e.clientY - GameUIState.dragStartY - GameUIState.mapOffsetY) > 5) return;
            // Handle hex click
            const rect = canvas.getBoundingClientRect();
            const x = (e.clientX - rect.left - GameUIState.mapOffsetX) / GameUIState.mapScale;
            const y = (e.clientY - rect.top - GameUIState.mapOffsetY) / GameUIState.mapScale;
            handleHexClick(x, y);
        });

        window.addEventListener('resize', () => {
            resizeCanvas();
            renderGalaxyMap();
        });

        resizeCanvas();
    }

    function resizeCanvas() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;
        const parent = canvas.parentElement;
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;
    }

    function renderGalaxyMap() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Clear
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        ctx.save();
        ctx.translate(GameUIState.mapOffsetX, GameUIState.mapOffsetY);
        ctx.scale(GameUIState.mapScale, GameUIState.mapScale);

        // Draw hex grid
        const hexSize = 30;
        const centerX = w / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale;
        const centerY = h / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale;
        const radius = 8;

        for (let q = -radius; q <= radius; q++) {
            for (let r = -radius; r <= radius; r++) {
                if (Math.abs(q + r) > radius) continue;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                drawHex(ctx, x, y, hexSize, '#1a1535', '#2a2050');
            }
        }

        // Draw subsectors from real game state data
        const subSectors = GameUIState.gameState?.subSectors || [];
        if (subSectors.length > 0) {
            // Color palette for subsectors
            const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];
            
            subSectors.forEach((subSector, index) => {
                // Map subsector coordinates to hex positions
                const q = subSector.subCol - 5; // Center around (5,5)
                const r = subSector.subRow - 5;
                
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                
                // Color based on system count
                const systemCount = subSector.systemCount || 0;
                let color = colors[index % colors.length];
                let alpha = 0.3 + Math.min(0.7, systemCount * 0.2); // More systems = brighter
                
                // Contested subsectors get a red border
                const isContested = subSector.contested === true;
                const borderColor = isContested ? '#ef4444' : color;
                
                // Draw subsector hex with color
                drawHex(ctx, x, y, hexSize, 
                    color + Math.round(alpha * 255).toString(16).padStart(2, '0'), 
                    borderColor);
                
                // Draw a thicker red outline for contested subsectors
                if (isContested) {
                    drawHex(ctx, x, y, hexSize - 2, 'transparent', '#ef4444');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
                
                // Draw subsector name inside the hex
                const subSectorName = subSector.name || '';
                if (subSectorName) {
                    ctx.fillStyle = '#ffffff';
                    ctx.font = 'bold 10px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    // Text stroke for contrast
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    // Truncate long names to fit the hex
                    const maxLen = 10;
                    const displayName = subSectorName.length > maxLen ? subSectorName.substring(0, maxLen) + 'Ã¢â‚¬Â¦' : subSectorName;
                    ctx.strokeText(displayName, x, y - 4);
                    ctx.fillStyle = '#ffffff';
                    ctx.fillText(displayName, x, y - 4);
                    ctx.lineWidth = 0.5;
                }

                // Draw system count below the name
                if (systemCount > 0) {
                    ctx.fillStyle = '#fbbf24';
                    ctx.font = 'bold 9px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    ctx.strokeText(systemCount.toString(), x, y + 6);
                    ctx.fillStyle = '#fbbf24';
                    ctx.fillText(systemCount.toString(), x, y + 6);
                    ctx.lineWidth = 0.5;
                }

                // Draw trade type label below the hex
                if (subSector.tradeType) {
                    ctx.fillStyle = '#a78bfa';
                    ctx.font = '8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'top';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    ctx.strokeText(subSector.tradeType, x, y + hexSize + 2);
                    ctx.fillStyle = '#a78bfa';
                    ctx.fillText(subSector.tradeType, x, y + hexSize + 2);
                    ctx.lineWidth = 0.5;
                }
            });
        } else {
            // Fallback: show loading message
            ctx.fillStyle = '#6b7280';
            ctx.font = '14px sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText('Waiting for galaxy data...', centerX, centerY);
        }

        // Outline sectors containing the player's facilities
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        if (myEmpireId != null && subSectors.length > 0) {
            subSectors.forEach((subSector) => {
                const ownerIds = subSector.ownerEmpireIds;
                if (ownerIds && Array.isArray(ownerIds) && ownerIds.includes(myEmpireId)) {
                    // This subsector has the player's facilities Ã¢â‚¬â€ draw a gold outline
                    const q = subSector.subCol - 5;
                    const r = subSector.subRow - 5;
                    const x = centerX + hexSize * (3/2 * q);
                    const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                    drawHex(ctx, x, y, hexSize - 1, 'transparent', '#f59e0b');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
            });
        }

        // Draw MDrive range circle for selected fleet
        const allFleets = GameUIState.gameState?.fleets || [];
        if (GameUIState.selectedFleet && allFleets.length > 0) {
            const selectedFleetData = allFleets.find(f => f.id === GameUIState.selectedFleet);
            if (selectedFleetData && selectedFleetData.effectiveMDriveSpeed > 0) {
                const sq = (selectedFleetData.subSectorCol || 5) - 5;
                const sr = (selectedFleetData.subSectorRow || 5) - 5;
                const sx = centerX + hexSize * (3/2 * sq);
                const sy = centerY + hexSize * (Math.sqrt(3)/2 * sq + Math.sqrt(3) * sr);
                const rangeRadius = selectedFleetData.effectiveMDriveSpeed * hexSize * 1.5;

                // Draw range circle with dashed line
                ctx.beginPath();
                ctx.arc(sx, sy, rangeRadius, 0, Math.PI * 2);
                ctx.strokeStyle = 'rgba(0, 212, 255, 0.4)';
                ctx.lineWidth = 2;
                ctx.setLineDash([6, 4]);
                ctx.stroke();
                ctx.setLineDash([]);
                ctx.lineWidth = 0.5;

                // Draw range fill
                ctx.beginPath();
                ctx.arc(sx, sy, rangeRadius, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(0, 212, 255, 0.05)';
                ctx.fill();

                // Draw range label
                ctx.fillStyle = 'rgba(0, 212, 255, 0.7)';
                ctx.font = 'bold 7px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText(`MDrive: ${selectedFleetData.effectiveMDriveSpeed}`, sx, sy - rangeRadius - 6);
            }
        }

        // Draw player fleets from game state at their actual subsector positions
        const fleets = GameUIState.gameState?.fleets || [];
        if (fleets.length > 0) {
            fleets.forEach((fleet) => {
                const q = (fleet.subSectorCol || 5) - 5;
                const r = (fleet.subSectorRow || 5) - 5;
                const fx = centerX + hexSize * (3/2 * q);
                const fy = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Highlight selected fleet
                const isSelected = fleet.id === GameUIState.selectedFleet;
                if (isSelected) {
                    ctx.beginPath();
                    ctx.arc(fx, fy, 14, 0, Math.PI * 2);
                    ctx.strokeStyle = 'rgba(0, 212, 255, 0.6)';
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }

                // Draw fleet triangle
                ctx.beginPath();
                ctx.moveTo(fx, fy - 10);
                ctx.lineTo(fx - 7, fy + 5);
                ctx.lineTo(fx + 7, fy + 5);
                ctx.closePath();
                ctx.fillStyle = isSelected ? '#00d4ff' : '#06b6d4';
                ctx.fill();
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = isSelected ? 2 : 1;
                ctx.stroke();
                ctx.lineWidth = 0.5;

                // Draw ship count label
                if (fleet.shipCount > 0) {
                    ctx.fillStyle = '#fff';
                    ctx.font = 'bold 8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(fleet.shipCount.toString(), fx, fy + 12);
                }
            });
        }

        // Draw enemy indicators at their actual fleet positions
        const enemies = GameUIState.gameState?.players?.filter(p => p.id !== getCurrentPlayerId()) || [];
        if (enemies.length > 0 && fleets.length === 0) {
            // Fallback: show enemy indicators around center when no fleet data available
            enemies.forEach((enemy, i) => {
                const angle = (i / enemies.length) * Math.PI * 2;
                const dist = hexSize * 4;
                const ex = centerX + Math.cos(angle) * dist;
                const ey = centerY + Math.sin(angle) * dist;

                ctx.beginPath();
                ctx.moveTo(ex, ey - 8);
                ctx.lineTo(ex - 6, ey + 4);
                ctx.lineTo(ex + 6, ey + 4);
                ctx.closePath();
                ctx.fillStyle = '#ef4444';
                ctx.fill();

                // Label
                ctx.fillStyle = '#ef4444';
                ctx.font = '9px sans-serif';
                ctx.textAlign = 'center';
                ctx.fillText(enemy.name ? enemy.name.substring(0, 6) : 'Enemy', ex, ey + 14);
            });
        }

        // Draw enemy fleets when fleet data IS available
        if (fleets.length > 0) {
            const myFleetIds = new Set(fleets.filter(f => f.ownerId === getCurrentPlayerId()).map(f => f.id));
            const enemyFleets = fleets.filter(f => !myFleetIds.has(f.id));
            enemyFleets.forEach((fleet) => {
                const q = (fleet.subSectorCol || 5) - 5;
                const r = (fleet.subSectorRow || 5) - 5;
                const fx = centerX + hexSize * (3/2 * q);
                const fy = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Draw enemy fleet triangle (red)
                ctx.beginPath();
                ctx.moveTo(fx, fy - 10);
                ctx.lineTo(fx - 7, fy + 5);
                ctx.lineTo(fx + 7, fy + 5);
                ctx.closePath();
                ctx.fillStyle = '#ef4444';
                ctx.fill();
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = 1;
                ctx.stroke();
                ctx.lineWidth = 0.5;

                // Draw ship count label
                if (fleet.shipCount > 0) {
                    ctx.fillStyle = '#ef4444';
                    ctx.font = 'bold 8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(fleet.shipCount.toString(), fx, fy + 12);
                }
            });
        }

        // Hover tooltip for subsectors
        GameUIState.hoveredSubSector = null;
        if (subSectors.length > 0) {
            subSectors.forEach((subSector) => {
                const q = subSector.subCol - 5;
                const r = subSector.subRow - 5;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Check if mouse is near this hex
                const dx = galaxyMouseX - x;
                const dy = galaxyMouseY - y;
                const dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < hexSize) {
                    GameUIState.hoveredSubSector = subSector;
                    // Highlight the hovered hex
                    drawHex(ctx, x, y, hexSize, 'rgba(255, 255, 255, 0.15)', '#ffffff');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
            });
        }

        ctx.restore();

        // Draw hover tooltip (outside transform so it stays at screen coords)
        if (GameUIState.hoveredSubSector) {
            drawSubSectorTooltip(ctx, GameUIState.hoveredSubSector, w, h);
        }

        // Update minimap
        renderMinimap();
    }

    function drawSubSectorTooltip(ctx, subSector, canvasW, canvasH) {
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        const isOwned = subSector.ownerEmpireIds && Array.isArray(subSector.ownerEmpireIds)
            && subSector.ownerEmpireIds.includes(myEmpireId);
        const systemCount = subSector.systemCount || 0;
        const contested = subSector.contested === true;
        const name = subSector.name || `SubSector [${subSector.subRow},${subSector.subCol}]`;

        // Build tooltip lines
        const lines = [];
        lines.push(name);
        lines.push(`Systems: ${systemCount}`);
        if (subSector.tradeType) lines.push(`Trade: ${subSector.tradeType}`);
        if (isOwned) lines.push('Ã°Å¸ÂÂ  Your Territory');
        if (contested) lines.push('Ã¢Å¡â€Ã¯Â¸Â Contested');

        // Measure tooltip size
        ctx.font = 'bold 11px sans-serif';
        let maxWidth = 0;
        lines.forEach(line => {
            const metrics = ctx.measureText(line);
            if (metrics.width > maxWidth) maxWidth = metrics.width;
        });

        const padding = 8;
        const tooltipW = maxWidth + padding * 2;
        const tooltipH = lines.length * 14 + padding * 2;

        // Position tooltip near cursor (in screen coords)
        let tx = galaxyMouseX * GameUIState.mapScale + GameUIState.mapOffsetX + 15;
        let ty = galaxyMouseY * GameUIState.mapScale + GameUIState.mapOffsetY - 10;

        // Keep tooltip within canvas bounds
        if (tx + tooltipW > canvasW - 10) tx = tx - tooltipW - 30;
        if (ty + tooltipH > canvasH - 10) ty = ty - tooltipH;
        if (ty < 10) ty = 10;

        // Draw tooltip background
        ctx.fillStyle = 'rgba(10, 14, 23, 0.95)';
        ctx.strokeStyle = contested ? '#ef4444' : (isOwned ? '#f59e0b' : '#06b6d4');
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.roundRect(tx, ty, tooltipW, tooltipH, 4);
        ctx.fill();
        ctx.stroke();
        ctx.lineWidth = 0.5;

        // Draw tooltip text
        ctx.fillStyle = '#e5e7eb';
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'top';
        lines.forEach((line, i) => {
            if (i === 0) {
                ctx.fillStyle = '#06b6d4';
                ctx.font = 'bold 11px sans-serif';
            } else {
                ctx.fillStyle = '#e5e7eb';
                ctx.font = '10px sans-serif';
            }
            ctx.fillText(line, tx + padding, ty + padding + i * 14);
        });
    }

    function renderMinimap() {
        const minimapCanvas = document.getElementById('minimapCanvas');
        if (!minimapCanvas) return;
        const ctx = minimapCanvas.getContext('2d');
        const w = minimapCanvas.width;
        const h = minimapCanvas.height;

        // Clear
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        const subSectors = GameUIState.gameState?.subSectors || [];
        if (subSectors.length === 0) return;

        // Find bounds
        let minCol = Infinity, maxCol = -Infinity, minRow = Infinity, maxRow = -Infinity;
        subSectors.forEach(ss => {
            if (ss.subCol < minCol) minCol = ss.subCol;
            if (ss.subCol > maxCol) maxCol = ss.subCol;
            if (ss.subRow < minRow) minRow = ss.subRow;
            if (ss.subRow > maxRow) maxRow = ss.subRow;
        });

        const rangeCol = maxCol - minCol || 1;
        const rangeRow = maxRow - minRow || 1;
        const padding = 10;
        const drawW = w - padding * 2;
        const drawH = h - padding * 2;

        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;

        // Draw subsector dots
        subSectors.forEach(ss => {
            const nx = padding + ((ss.subCol - minCol) / rangeCol) * drawW;
            const ny = padding + ((ss.subRow - minRow) / rangeRow) * drawH;
            const isOwned = ss.ownerEmpireIds && Array.isArray(ss.ownerEmpireIds) && ss.ownerEmpireIds.includes(myEmpireId);
            const contested = ss.contested === true;

            ctx.beginPath();
            ctx.arc(nx, ny, 3, 0, Math.PI * 2);
            ctx.fillStyle = contested ? '#ef4444' : (isOwned ? '#f59e0b' : '#3b82f6');
            ctx.fill();
        });

        // Draw fleets
        const fleets = GameUIState.gameState?.fleets || [];
        fleets.forEach(fleet => {
            const col = fleet.subSectorCol || 5;
            const row = fleet.subSectorRow || 5;
            const nx = padding + ((col - minCol) / rangeCol) * drawW;
            const ny = padding + ((row - minRow) / rangeRow) * drawH;

            ctx.beginPath();
            ctx.arc(nx, ny, 2, 0, Math.PI * 2);
            ctx.fillStyle = fleet.ownerId === getCurrentPlayerId() ? '#00d4ff' : '#ef4444';
            ctx.fill();
        });

        // Draw viewport rectangle
        const galaxyCanvas = GameUIState.elements.galaxyCanvas;
        if (galaxyCanvas) {
            const hexSize = 30;
            const centerX = galaxyCanvas.width / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale;
            const centerY = galaxyCanvas.height / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale;

            // Calculate visible area in hex coords
            const visibleLeft = (-GameUIState.mapOffsetX / GameUIState.mapScale - centerX) / (hexSize * 1.5) + 5;
            const visibleTop = (-GameUIState.mapOffsetY / GameUIState.mapScale - centerY) / (hexSize * Math.sqrt(3)) + 5;
            const visibleW = (galaxyCanvas.width / GameUIState.mapScale) / (hexSize * 1.5);
            const visibleH = (galaxyCanvas.height / GameUIState.mapScale) / (hexSize * Math.sqrt(3));

            const vx = padding + ((visibleLeft - minCol) / rangeCol) * drawW;
            const vy = padding + ((visibleTop - minRow) / rangeRow) * drawH;
            const vw = (visibleW / rangeCol) * drawW;
            const vh = (visibleH / rangeRow) * drawH;

            ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)';
            ctx.lineWidth = 1;
            ctx.strokeRect(vx, vy, vw, vh);
        }
    }

    function drawHex(ctx, x, y, size, fill, stroke) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const hx = x + size * Math.cos(angle);
            const hy = y + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(hx, hy);
            else ctx.lineTo(hx, hy);
        }
        ctx.closePath();
        ctx.fillStyle = fill;
        ctx.fill();
        ctx.strokeStyle = stroke;
        ctx.lineWidth = 0.5;
        ctx.stroke();
    }

    function handleHexClick(x, y) {
        // Convert pixel to hex coordinates
        const hexSize = 30;
        const centerX = (GameUIState.elements.galaxyCanvas.width / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale);
        const centerY = (GameUIState.elements.galaxyCanvas.height / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale);
        
        // Calculate relative position from center
        const relX = x - centerX;
        const relY = y - centerY;
        
        // Convert to axial hex coordinates
        const col = Math.round(relX / (hexSize * 1.5));
        const row = Math.round(relY / (hexSize * Math.sqrt(3)) - col / 2);
        
        // Map back to subsector coordinates (centered around 5,5)
        const subCol = col + 5;
        const subRow = row + 5;
        
        // Find matching subsector
        const subSectors = GameUIState.gameState?.subSectors || [];
        const matched = subSectors.find(s => s.subCol === subCol && s.subRow === subRow);
        
        if (matched) {
            // Show sector detail view with this subsector's data
            showSectorDetailView(matched);
        } else {
            // No subsector at this hex, show all subsectors for the sector
            addEventLog('SYSTEM', `Selected hex (${col}, ${row}) - no subsector data`);
            if (GameUIState.elements.targetCoords) {
                GameUIState.elements.targetCoords.value = `${col}, ${row}`;
            }
        }

    // Register public functions on GameUI
    GameUI.setupGalaxyMap = setupGalaxyMap;
    GameUI.sys = sys;
    GameUI.resizeCanvas = resizeCanvas;
    GameUI.renderGalaxyMap = renderGalaxyMap;
    GameUI.selectedFleetData = selectedFleetData;
    GameUI.enemies = enemies;
    GameUI.myFleetIds = myFleetIds;
    GameUI.enemyFleets = enemyFleets;
    GameUI.drawSubSectorTooltip = drawSubSectorTooltip;
    GameUI.renderMinimap = renderMinimap;
    GameUI.drawHex = drawHex;
    GameUI.handleHexClick = handleHexClick;
    GameUI.matched = matched;
}());
