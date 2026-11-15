/* DoraMCU - Alien Races UI Module (S03 Spinward Marches) */

const AlienRaceUI = (function() {
    let races = [];
    let selectedRace = null;

    function showAlienRaceModal() {
        const modal = document.getElementById('alienRaceModal');
        if (modal) {
            modal.classList.remove('hidden');
            loadAlienRaces();
        }
    }

    function hideAlienRaceModal() {
        const modal = document.getElementById('alienRaceModal');
        if (modal) modal.classList.add('hidden');
    }

    async function loadAlienRaces() {
        try {
            races = await GameAPI.getAlienRaces();
        } catch (e) {
            races = [];
            console.error('Alien races load failed:', e);
        }
        renderAllPanels();
    }

    function renderAllPanels() {
        renderRacesPanel();
        renderDetailPanel();
        renderComparePanel();
    }

    function switchTab(tabName) {
        document.querySelectorAll('.alien-race-panel').forEach(p => p.classList.add('hidden'));
        document.querySelectorAll('.alien-race-tab').forEach(t => {
            t.style.background = 'rgba(255,255,255,0.05)';
            t.style.color = 'var(--text-secondary)';
            t.classList.remove('active');
        });
        const panelMap = {
            'races': 'racesPanel',
            'detail': 'detailPanel',
            'compare': 'comparePanel'
        };
        const panel = document.getElementById(panelMap[tabName]);
        if (panel) panel.classList.remove('hidden');
        const tab = document.querySelector(`.alien-race-tab[data-tab="${tabName}"]`);
        if (tab) {
            tab.style.background = 'rgba(59,130,246,0.2)';
            tab.style.color = '#3b82f6';
            tab.classList.add('active');
        }
    }

    function renderRacesPanel() {
        const panel = document.getElementById('racesPanel');
        if (!panel) return;

        if (races.length === 0) {
            panel.innerHTML = '<div style="text-align:center;padding:20px;color:var(--text-secondary);">No alien race data available</div>';
            return;
        }

        let html = '<div class="races-grid" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:12px;">';
        for (const r of races) {
            const hasBonuses = r.combatDM > 0 || r.socialDM > 0 || r.navigationDM > 0 || r.armorBonus > 0 || r.tradeBonusPercent > 0;
            html += `
                <div class="race-card" onclick="AlienRaceUI.selectRace('${r.name}')" 
                    style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:16px;cursor:pointer;transition:border-color 0.2s;"
                    onmouseover="this.style.borderColor='rgba(59,130,246,0.5)'" 
                    onmouseout="this.style.borderColor='rgba(255,255,255,0.1)'">
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                        <strong style="color:#3b82f6;font-size:1.1rem;">${r.displayName}</strong>
                        ${r.name === 'HUMAN' ? '<span style="font-size:0.7rem;color:var(--text-secondary);">Baseline</span>' : ''}
                    </div>
                    <div style="font-size:0.85rem;color:var(--text-secondary);margin-top:4px;">${r.description}</div>
                    <div style="display:flex;flex-wrap:wrap;gap:6px;margin-top:10px;">
                        ${formatModifier('Combat', r.combatDM)}
                        ${formatModifier('Social', r.socialDM)}
                        ${formatModifier('Nav', r.navigationDM)}
                        ${formatModifier('Armor', r.armorBonus)}
                        ${formatModifier('Trade', r.tradeBonusPercent + '%')}
                    </div>
                </div>
            `;
        }
        html += '</div>';
        panel.innerHTML = html;
    }

    function formatModifier(label, value) {
        if (value === 0 || value === '0%') {
            return `<span style="font-size:0.75rem;padding:2px 6px;background:rgba(255,255,255,0.05);border-radius:4px;color:var(--text-secondary);">${label}: 0</span>`;
        }
        const isPositive = String(value).startsWith('+') || (!String(value).startsWith('-') && value !== '0%');
        const color = isPositive ? '#22c55e' : '#ef4444';
        return `<span style="font-size:0.75rem;padding:2px 6px;background:rgba(255,255,255,0.05);border-radius:4px;color:${color};">${label}: ${value}</span>`;
    }

    function renderDetailPanel() {
        const panel = document.getElementById('detailPanel');
        if (!panel) return;

        if (!selectedRace) {
            panel.innerHTML = `
                <div style="text-align:center;padding:20px;color:var(--text-secondary);">
                    <div style="font-size:2rem;margin-bottom:8px;">👽</div>
                    <div>Select a race from the list to see details</div>
                </div>
            `;
            return;
        }

        // Load detailed info
        loadRaceDetail(selectedRace);
    }

    async function loadRaceDetail(raceName) {
        const panel = document.getElementById('detailPanel');
        if (!panel) return;

        try {
            const detail = await GameAPI.getAlienRace(raceName);
            const dmFormat = (dm) => dm > 0 ? `+${dm}` : dm.toString();

            panel.innerHTML = `
                <div style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:16px;">
                    <h3 style="color:#3b82f6;margin-bottom:8px;">${detail.displayName}</h3>
                    <p style="color:var(--text-secondary);margin-bottom:16px;">${detail.description}</p>
                    
                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
                        <div style="background:rgba(0,0,0,0.3);border-radius:6px;padding:10px;">
                            <div style="font-size:0.8rem;color:var(--text-secondary);">Combat DM</div>
                            <div style="font-size:1.5rem;font-weight:bold;color:${detail.combatDM > 0 ? '#22c55e' : detail.combatDM < 0 ? '#ef4444' : '#888'};">${dmFormat(detail.combatDM)}</div>
                        </div>
                        <div style="background:rgba(0,0,0,0.3);border-radius:6px;padding:10px;">
                            <div style="font-size:0.8rem;color:var(--text-secondary);">Social DM</div>
                            <div style="font-size:1.5rem;font-weight:bold;color:${detail.socialDM > 0 ? '#22c55e' : detail.socialDM < 0 ? '#ef4444' : '#888'};">${dmFormat(detail.socialDM)}</div>
                        </div>
                        <div style="background:rgba(0,0,0,0.3);border-radius:6px;padding:10px;">
                            <div style="font-size:0.8rem;color:var(--text-secondary);">Navigation DM</div>
                            <div style="font-size:1.5rem;font-weight:bold;color:${detail.navigationDM > 0 ? '#22c55e' : detail.navigationDM < 0 ? '#ef4444' : '#888'};">${dmFormat(detail.navigationDM)}</div>
                        </div>
                        <div style="background:rgba(0,0,0,0.3);border-radius:6px;padding:10px;">
                            <div style="font-size:0.8rem;color:var(--text-secondary);">Armor Bonus</div>
                            <div style="font-size:1.5rem;font-weight:bold;color:${detail.armorBonus > 0 ? '#22c55e' : detail.armorBonus < 0 ? '#ef4444' : '#888'};">${dmFormat(detail.armorBonus)}</div>
                        </div>
                    </div>

                    <div style="margin-top:16px;">
                        <div style="font-size:0.8rem;color:var(--text-secondary);margin-bottom:8px;">Trade Bonus: <strong style="color:#22c55e;">+${detail.tradeBonusPercent}%</strong></div>
                    </div>

                    ${detail.traits && detail.traits.length > 0 ? `
                        <div style="margin-top:16px;">
                            <div style="font-size:0.8rem;color:var(--text-secondary);margin-bottom:8px;">Traits</div>
                            ${detail.traits.map(t => `<div style="font-size:0.85rem;padding:4px 0;color:#e0e0e0;">• ${t}</div>`).join('')}
                        </div>
                    ` : ''}
                </div>
            `;
        } catch (e) {
            console.error('Load race detail failed:', e);
            panel.innerHTML = '<div style="text-align:center;padding:20px;color:#ef4444;">Failed to load race details</div>';
        }
    }

    function renderComparePanel() {
        const panel = document.getElementById('comparePanel');
        if (!panel) return;

        const raceOptions = races.map(r => `<option value="${r.name}">${r.displayName}</option>`).join('');

        let html = `
            <div style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:16px;">
                <h4 style="color:#3b82f6;margin-bottom:12px;">Compare Two Races</h4>
                <div style="display:flex;gap:12px;align-items:center;">
                    <select id="compareRace1" style="flex:1;background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                        <option value="">Select race 1...</option>
                        ${raceOptions}
                    </select>
                    <span style="color:var(--text-secondary);">vs</span>
                    <select id="compareRace2" style="flex:1;background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                        <option value="">Select race 2...</option>
                        ${raceOptions}
                    </select>
                    <button onclick="AlienRaceUI.compare()" style="background:rgba(59,130,246,0.2);color:#3b82f6;border:1px solid #3b82f6;border-radius:4px;padding:6px 16px;cursor:pointer;">Compare</button>
                </div>
                <div id="compareResult" style="margin-top:16px;"></div>
            </div>
        `;
        panel.innerHTML = html;
    }

    function selectRace(raceName) {
        selectedRace = raceName;
        switchTab('detail');
    }

    async function compare() {
        const name1 = document.getElementById('compareRace1').value;
        const name2 = document.getElementById('compareRace2').value;

        if (!name1 || !name2) {
            alert('Please select two races to compare');
            return;
        }

        if (name1 === name2) {
            alert('Please select two different races');
            return;
        }

        try {
            const result = await GameAPI.compareAlienRaces(name1, name2);
            const resultEl = document.getElementById('compareResult');
            if (!resultEl) return;

            const r1 = result.race1;
            const r2 = result.race2;
            const dmBar = (label, v1, v2) => {
                const winner = v1 > v2 ? 1 : v2 > v1 ? 2 : 0;
                const c1 = winner === 1 ? '#22c55e' : '#888';
                const c2 = winner === 2 ? '#22c55e' : '#888';
                return `
                    <div style="display:flex;align-items:center;gap:8px;margin:4px 0;">
                        <span style="width:80px;text-align:right;font-size:0.85rem;color:${c1};">${v1 > 0 ? '+' : ''}${v1}</span>
                        <span style="flex:1;text-align:center;font-size:0.8rem;color:var(--text-secondary);">${label}</span>
                        <span style="width:80px;font-size:0.85rem;color:${c2};">${v2 > 0 ? '+' : ''}${v2}</span>
                    </div>
                `;
            };

            resultEl.innerHTML = `
                <div style="display:grid;grid-template-columns:1fr auto 1fr;gap:12px;align-items:start;">
                    <div style="text-align:center;padding:12px;background:rgba(0,0,0,0.3);border-radius:6px;">
                        <div style="font-size:1.2rem;font-weight:bold;color:#3b82f6;">${r1.displayName}</div>
                    </div>
                    <div style="padding:12px;">
                        ${dmBar('Combat', r1.combatDM, r2.combatDM)}
                        ${dmBar('Social', r1.socialDM, r2.socialDM)}
                        ${dmBar('Nav', r1.navigationDM, r2.navigationDM)}
                        ${dmBar('Armor', r1.armorBonus, r2.armorBonus)}
                        ${dmBar('Trade%', r1.tradeBonusPercent, r2.tradeBonusPercent)}
                    </div>
                    <div style="text-align:center;padding:12px;background:rgba(0,0,0,0.3);border-radius:6px;">
                        <div style="font-size:1.2rem;font-weight:bold;color:#3b82f6;">${r2.displayName}</div>
                    </div>
                </div>
                <div style="margin-top:16px;display:grid;grid-template-columns:1fr 1fr;gap:12px;">
                    <div style="padding:10px;background:rgba(34,197,94,0.1);border-radius:6px;">
                        <div style="font-size:0.8rem;color:#22c55e;margin-bottom:4px;">${r1.displayName} Advantages</div>
                        ${result.race1Advantages && result.race1Advantages.length > 0 
                            ? result.race1Advantages.map(a => `<div style="font-size:0.85rem;">✓ ${a}</div>`).join('')
                            : '<div style="font-size:0.85rem;color:var(--text-secondary);">None</div>'}
                    </div>
                    <div style="padding:10px;background:rgba(34,197,94,0.1);border-radius:6px;">
                        <div style="font-size:0.8rem;color:#22c55e;margin-bottom:4px;">${r2.displayName} Advantages</div>
                        ${result.race2Advantages && result.race2Advantages.length > 0 
                            ? result.race2Advantages.map(a => `<div style="font-size:0.85rem;">✓ ${a}</div>`).join('')
                            : '<div style="font-size:0.85rem;color:var(--text-secondary);">None</div>'}
                    </div>
                </div>
            `;
        } catch (e) {
            console.error('Compare failed:', e);
            alert('Comparison failed');
        }
    }

    return {
        showAlienRaceModal,
        hideAlienRaceModal,
        loadAlienRaces,
        switchTab,
        selectRace,
        compare
    };
})();
