/**
 * DoraMCU Game UI - SECTOR DETAIL VIEW Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';


    function showSectorDetailView(subSector) {
        if (!GameUIState.elements.sectorMapView || !GameUIState.elements.sectorDetailView) return;
        
        // Hide map, show detail
        GameUIState.elements.sectorMapView.classList.add('hidden');
        GameUIState.elements.sectorDetailView.classList.remove('hidden');
        
        // Set title
        const sectorName = subSector.name || `Sector [${subSector.sectorRow},${subSector.sectorCol}]`;
        if (GameUIState.elements.sectorDetailTitle) {
            GameUIState.elements.sectorDetailTitle.textContent = sectorName;
        }
        
        // Show subsector list, hide systems
        if (GameUIState.elements.sectorSubsectors) GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        if (GameUIState.elements.sectorSystems) GameUIState.elements.sectorSystems.classList.add('hidden');
        
        // Find all subsectors in the same sector
        const subSectors = GameUIState.gameState?.subSectors || [];
        const sectorSubSectors = subSectors.filter(s => 
            s.sectorRow === subSector.sectorRow && s.sectorCol === subSector.sectorCol
        );
        
        // Render subsector cards
        if (GameUIState.elements.sectorSubsectors) {
            GameUIState.elements.sectorSubsectors.innerHTML = '';
            
            if (sectorSubSectors.length === 0) {
                GameUIState.elements.sectorSubsectors.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No subsectors in this sector</div>';
                return;
            }
            
            sectorSubSectors.forEach(ss => {
                const card = document.createElement('div');
                card.className = 'sector-subsector-card' + (ss.contested ? ' contested' : '');
                const systemCount = ss.systemCount || 0;
                const contestedIcon = ss.contested ? ' Ã¢Å¡Â Ã¯Â¸Â' : '';
                card.innerHTML = `
                    <div class="sector-subsector-name">Ã°Å¸â€Â¹ ${GameUI.escapeHtml(ss.name || 'SubSector ' + ss.id)}${contestedIcon}</div>
                    <div class="sector-subsector-coords">[${ss.subRow}, ${ss.subCol}]</div>
                    <div class="sector-subsector-systems">Ã¢Â­Â ${systemCount} system(s)</div>
                    ${ss.contested ? '<div class="sector-subsector-contested">Ã¢Å¡â€Ã¯Â¸Â Contested Territory</div>' : ''}
                `;
                card.addEventListener('click', () => {
                    showSubsectorGraphicView(ss);
                });
                GameUIState.elements.sectorSubsectors.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing sector ${sectorName} with ${sectorSubSectors.length} subsectors`);
    }

    function showSystemsInView(subSector) {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        
        // Hide subsector list, show systems
        GameUIState.elements.sectorSubsectors.classList.add('hidden');
        GameUIState.elements.sectorSystems.classList.remove('hidden');
        
        if (GameUIState.elements.systemSubsectorTitle) {
            GameUIState.elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render systems
        if (GameUIState.elements.systemsList) {
            GameUIState.elements.systemsList.innerHTML = '';
            
            const systems = subSector.systems || [];
            if (systems.length === 0) {
                GameUIState.elements.systemsList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No systems generated yet</div>';
                return;
            }
            
            systems.forEach(sys => {
                const card = document.createElement('div');
                card.className = 'system-card';
                
                const starCount = sys.starCount || 0;
                const planetCount = sys.planetCount || 0;
                const systemName = sys.name || `System ${sys.id || '?'}`;
                const systemType = sys.systemType || 'Unknown';
                
                let html = `
                    <div class="system-name">Ã¢Â­Â ${GameUI.escapeHtml(systemName)}</div>
                    <div class="system-details">
                        <div class="system-detail">
                            <span class="system-detail-label">Type:</span> ${GameUI.escapeHtml(systemType)}
                        </div>
                        <div class="system-detail">
                            <span class="system-detail-label">Stars:</span> ${starCount}
                        </div>
                        <div class="system-detail">
                            <span class="system-detail-label">Planets:</span> ${planetCount}
                        </div>
                    </div>
                `;
                
                // Show planets if available
                if (sys.planets && sys.planets.length > 0) {
                    html += '<div class="system-planets"><div class="system-planets-title">Planets</div>';
                    sys.planets.forEach(planet => {
                        const planetName = planet.name || `Planet ${planet.id || '?'}`;
                        const planetType = planet.planetType || planet.type || 'Unknown';
                        html += `
                            <div class="planet-item">
                                Ã°Å¸ÂªÂ ${GameUI.escapeHtml(planetName)}
                                <span class="planet-type-badge">${GameUI.escapeHtml(planetType)}</span>
                            </div>
                        `;
                    });
                    html += '</div>';
                }
                
                card.innerHTML = html;
                GameUIState.elements.systemsList.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing ${systems.length} systems in ${subSector.name || subSector.id}`);

    // Register public functions on GameUI
    GameUI.showSectorDetailView = showSectorDetailView;
    GameUI.sectorSubSectors = sectorSubSectors;
    GameUI.showSystemsInView = showSystemsInView;
}());
