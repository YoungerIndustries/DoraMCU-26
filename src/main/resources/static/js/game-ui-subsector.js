/**
 * DoraMCU Game UI - SUBSECTOR PANEL Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';


    function renderSubsectorPanel() {
        if (!GameUIState.gameState || !GameUIState.elements.subsectorList) {
            console.log('renderSubsectorPanel: GameUIState.gameState or subsectorList not available');
            return;
        }

        const subSectors = GameUIState.gameState.subSectors || [];
        const totalSystems = subSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);

        console.log('renderSubsectorPanel:', subSectors.length, 'subsectors,', totalSystems, 'total systems');

        if (GameUIState.elements.subsectorGeneratedCount) {
            GameUIState.elements.subsectorGeneratedCount.textContent = subSectors.length;
        }
        if (GameUIState.elements.subsectorSystemCount) {
            GameUIState.elements.subsectorSystemCount.textContent = totalSystems;
        }

        GameUIState.elements.subsectorList.innerHTML = '';
        if (subSectors.length === 0) {
            GameUIState.elements.subsectorList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">Ã¢ÂÂ³ Waiting for galaxy data...</div>';
            return;
        }

        subSectors.forEach(subSector => {
            const item = document.createElement('div');
            item.className = 'subsector-item' + (GameUIState.selectedSubSector === subSector.id ? ' selected' : '');
            const systemCount = subSector.systemCount || 0;
            item.innerHTML = `
                <div class="subsector-name">
                    <span>Ã°Å¸â€Â¹</span>
                    <span>${GameUI.escapeHtml(subSector.name || 'SubSector ' + subSector.id)}</span>
                </div>
                <div class="subsector-coords">[${subSector.subRow}, ${subSector.subCol}]</div>
                <div class="subsector-systems">Ã¢Â­Â ${systemCount} system(s)</div>
            `;
            item.addEventListener('click', () => {
                GameUIState.selectedSubSector = subSector.id;
                renderSubsectorPanel();
                showSubSectorDetail(subSector);
            });
            GameUIState.elements.subsectorList.appendChild(item);
        });
    }

    function showSubSectorDetail(subSector) {
        if (!subSector || !subSector.systems) return;
        addEventLog('INFO', `Viewing subsector ${subSector.name || subSector.id} with ${subSector.systemCount} systems`);
        subSector.systems.forEach(sys => {
            addEventLog('INFO', `  System ${sys.id}: ${sys.starCount || 0} stars, ${sys.planetCount || 0} planets`);
        });
    }

    function getCurrentPlayerId() {
        // Extract from username or use a hash
        const username = GameAPI.getUsername();
        if (!username) return null;
        // Try to parse as number first
        const num = parseInt(username);
        if (!isNaN(num)) return num;
        // Fallback: hash
        let hash = 0;
        for (let i = 0; i < username.length; i++) {
            hash = ((hash << 5) - hash) + username.charCodeAt(i);
            hash |= 0;
        }
        return Math.abs(hash);

    // Register public functions on GameUI
    GameUI.renderSubsectorPanel = renderSubsectorPanel;
    GameUI.totalSystems = totalSystems;
    GameUI.showSubSectorDetail = showSubSectorDetail;
    GameUI.getCurrentPlayerId = getCurrentPlayerId;
}());
