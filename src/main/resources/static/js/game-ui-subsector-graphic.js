/**
 * DoraMCU Game UI - SUBSECTOR GRAPHIC VIEW Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';


    function showSubsectorGraphicView(subSector) {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        
        // Hide subsector list, show systems/graphic view
        GameUIState.elements.sectorSubsectors.classList.add('hidden');
        GameUIState.elements.sectorSystems.classList.remove('hidden');
        
        if (GameUIState.elements.systemSubsectorTitle) {
            GameUIState.elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render the subsector graphic view with hex grid
        renderSubsectorGraphic(subSector);
        
        addEventLog('INFO', `Viewing subsector ${subSector.name || subSector.id} graphic`);
    }

    function renderSubsectorGraphic(subSector) {
        const container = GameUIState.elements.systemsList;
        if (!container) return;
        
        container.innerHTML = '';
        
        // Create a canvas for the subsector graphic
        const wrapper = document.createElement('div');
        wrapper.className = 'subsector-graphic-wrapper';
        
        const canvas = document.createElement('canvas');
        canvas.id = 'subsectorCanvas';
        canvas.className = 'subsector-canvas';
        canvas.width = 500;
        canvas.height = 400;
        wrapper.appendChild(canvas);
        
        // Add legend below the canvas
        const legend = document.createElement('div');
        legend.className = 'subsector-legend';
        
        const systems = subSector.systems || [];
        const systemCount = systems.length || 0;
        const totalStars = systems.reduce((sum, s) => sum + (s.starCount || 0), 0);
        const totalPlanets = systems.reduce((sum, s) => sum + (s.planetCount || 0), 0);
        const totalBodies = systems.reduce((sum, s) => sum + (s.bodyCount || 0), 0);
        
        legend.innerHTML = `
            <div class="legend-row"><span class="legend-item">Ã¢Â­Â Systems: ${systemCount}</span><span class="legend-item">Ã¢Ëœâ‚¬Ã¯Â¸Â Stars: ${totalStars}</span><span class="legend-item">Ã°Å¸ÂªÂ Planets: ${totalPlanets}</span><span class="legend-item">Ã°Å¸Å’Å’ Bodies: ${totalBodies}</span></div>
            ${subSector.contested ? '<div class="legend-row contested-warning">Ã¢Å¡Â Ã¯Â¸Â Contested Territory Ã¢â‚¬â€ Enemy empires have systems here</div>' : ''}
        `;
        wrapper.appendChild(legend);
        
        container.appendChild(wrapper);
        
        // Draw the subsector hex grid
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;
        
        // Background
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);
        
        // Draw starfield background
        for (let i = 0; i < 100; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.5)`;
            ctx.fillRect(sx, sy, 1, 1);
        }
        
        const hexSize = 22;
        const centerX = w / 2;
        const centerY = h / 2 - 20;
        
        // Draw a 3-ring hex grid representing the subsector
        const radius = 3;
        for (let q = -radius; q <= radius; q++) {
            for (let r = -radius; r <= radius; r++) {
                if (Math.abs(q + r) > radius) continue;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                drawHexOnCtx(ctx, x, y, hexSize, '#1a1535', '#2a2050');
            }
        }
        
        // Draw systems as colored hexes at positions based on system data
        const systemColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];
        systems.forEach((sys, idx) => {
            // Position systems in a ring pattern based on their index
            const angle = (idx / Math.max(systems.length, 1)) * Math.PI * 2 - Math.PI / 2;
            const dist = hexSize * (1 + Math.floor(idx / 3) * 1.5);
            const sx = centerX + Math.cos(angle) * dist;
            const sy = centerY + Math.sin(angle) * dist;
            
            const starCount = sys.starCount || 0;
            const planetCount = sys.planetCount || 0;
            const color = systemColors[idx % systemColors.length];
            
            // Draw system hex
            drawHexOnCtx(ctx, sx, sy, hexSize, color + '99', color);
            
            // Draw star icon if has stars
            if (starCount > 0) {
                ctx.fillStyle = '#fbbf24';
                ctx.font = 'bold 12px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('Ã¢Ëœâ‚¬', sx, sy - 5);
            }
            
            // Draw planet icon if has planets
            if (planetCount > 0) {
                ctx.fillStyle = '#10b981';
                ctx.font = 'bold 10px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('Ã¢â€”Â', sx, sy + 6);
            }
            
            // Draw system name label
            const systemName = sys.name || `System ${sys.id || idx + 1}`;
            ctx.fillStyle = '#ffffff';
            ctx.font = 'bold 9px sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'top';
            // Text stroke for contrast against background
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 3;
            ctx.strokeText(systemName, sx, sy + hexSize + 4);
            ctx.fillStyle = '#ffffff';
            ctx.fillText(systemName, sx, sy + hexSize + 4);
        });
        
        // Draw title
        ctx.fillStyle = '#c084fc';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(subSector.name || `SubSector [${subSector.subRow},${subSector.subCol}]`, centerX, 20);
        
        // Draw contested warning if applicable
        if (subSector.contested) {
            ctx.fillStyle = '#ef4444';
            ctx.font = 'bold 11px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('Ã¢Å¡Â Ã¯Â¸Â CONTESTED TERRITORY Ã¢Å¡Â Ã¯Â¸Â', centerX, 40);
        }
        
        // Draw scale indicator
        ctx.fillStyle = '#6b7280';
        ctx.font = '9px sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(`[${subSector.subRow}, ${subSector.subCol}]`, 10, h - 10);
        
        // Draw ownership info
        if (subSector.ownerEmpireIds && subSector.ownerEmpireIds.size > 0) {
            const empireIds = Array.from(subSector.ownerEmpireIds);
            ctx.fillStyle = '#fcd34d';
            ctx.font = '9px sans-serif';
            ctx.textAlign = 'right';
            ctx.fillText(`Empires: ${empireIds.join(', ')}`, w - 10, h - 10);
        }
    }

    function drawHexOnCtx(ctx, x, y, size, fill, stroke) {
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
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    function showSectorMapView() {
        if (!GameUIState.elements.sectorMapView || !GameUIState.elements.sectorDetailView) return;
        GameUIState.elements.sectorMapView.classList.remove('hidden');
        GameUIState.elements.sectorDetailView.classList.add('hidden');
        
        // Reset systems view if visible
        if (GameUIState.elements.sectorSubsectors) GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        if (GameUIState.elements.sectorSystems) GameUIState.elements.sectorSystems.classList.add('hidden');
        
        // Re-render the galaxy map
        renderGalaxyMap();
    }

    function showSubsectorListInView() {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        GameUIState.elements.sectorSystems.classList.add('hidden');
    }

    function zoomGalaxy(factor) {
        GameUIState.mapScale = Math.max(0.3, Math.min(3, GameUIState.mapScale * factor));
        renderGalaxyMap();
    }

    function resetGalaxyView() {
        GameUIState.mapScale = 1;
        GameUIState.mapOffsetX = 0;
        GameUIState.mapOffsetY = 0;
        renderGalaxyMap();

    // Register public functions on GameUI
    GameUI.showSubsectorGraphicView = showSubsectorGraphicView;
    GameUI.renderSubsectorGraphic = renderSubsectorGraphic;
    GameUI.totalStars = totalStars;
    GameUI.totalPlanets = totalPlanets;
    GameUI.totalBodies = totalBodies;
    GameUI.drawHexOnCtx = drawHexOnCtx;
    GameUI.showSectorMapView = showSectorMapView;
    GameUI.showSubsectorListInView = showSubsectorListInView;
    GameUI.zoomGalaxy = zoomGalaxy;
    GameUI.resetGalaxyView = resetGalaxyView;
}());
