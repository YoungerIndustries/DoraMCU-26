/* DoraMCU - Smuggling UI Module (B07 Merchant Prince) */

const SmugglingUI = (function() {
    let contrabandTypes = [];
    let customsOutcomes = [];
    let actions = [];
    let operations = [];
    let selectedOperationId = null;

    function showSmugglingModal() {
        const modal = document.getElementById('smugglingModal');
        if (modal) {
            modal.classList.remove('hidden');
            loadAllSmugglingData();
        }
    }

    function hideSmugglingModal() {
        const modal = document.getElementById('smugglingModal');
        if (modal) modal.classList.add('hidden');
    }

    async function loadAllSmugglingData() {
        try {
            try {
                contrabandTypes = await GameAPI.getContraband();
            } catch (e) {
                contrabandTypes = [];
            }
            try {
                customsOutcomes = await GameAPI.getCustomsOutcomes();
            } catch (e) {
                customsOutcomes = [];
            }
            try {
                actions = await GameAPI.getSmugglingActions();
            } catch (e) {
                actions = [];
            }
            try {
                operations = await GameAPI.listSmugglingOperations();
            } catch (e) {
                operations = [];
            }
            renderAllPanels();
        } catch (e) {
            console.error('Smuggling data load failed:', e);
        }
    }

    function renderAllPanels() {
        renderContrabandPanel();
        renderOperationsPanel();
        renderEstimatePanel();
    }

    function switchTab(tabName) {
        document.querySelectorAll('.smuggling-panel').forEach(p => p.classList.add('hidden'));
        document.querySelectorAll('.smuggling-tab').forEach(t => {
            t.style.background = 'rgba(255,255,255,0.05)';
            t.style.color = 'var(--text-secondary)';
            t.classList.remove('active');
        });
        const panelMap = {
            'contraband': 'contrabandPanel',
            'operations': 'operationsPanel',
            'estimate': 'estimatePanel'
        };
        const panel = document.getElementById(panelMap[tabName]);
        if (panel) panel.classList.remove('hidden');
        const tab = document.querySelector(`.smuggling-tab[data-tab="${tabName}"]`);
        if (tab) {
            tab.style.background = 'rgba(234,179,8,0.2)';
            tab.style.color = '#eab308';
            tab.classList.add('active');
        }
    }

    function renderContrabandPanel() {
        const panel = document.getElementById('contrabandPanel');
        if (!panel) return;

        if (contrabandTypes.length === 0) {
            panel.innerHTML = '<div style="text-align:center;padding:20px;color:var(--text-secondary);">No contraband data available</div>';
            return;
        }

        let html = '<div class="contraband-list">';
        for (const c of contrabandTypes) {
            const riskColor = c.riskLevel >= 4 ? '#ef4444' : c.riskLevel >= 3 ? '#f97316' : c.riskLevel >= 2 ? '#eab308' : '#22c55e';
            html += `
                <div class="contraband-item" style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:12px;margin-bottom:8px;">
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                        <div>
                            <strong style="color:#eab308;">${c.displayName}</strong>
                            <span style="color:var(--text-secondary);font-size:0.85rem;margin-left:8px;">${c.description}</span>
                        </div>
                        <span style="color:${riskColor};font-weight:bold;">Risk: ${c.riskLevel}</span>
                    </div>
                    <div style="display:flex;gap:16px;margin-top:8px;font-size:0.85rem;color:var(--text-secondary);">
                        <span>Price: ${c.basePrice} cr</span>
                        <span>Volume: ${c.volumePerUnit} m³</span>
                        <span>Profit: ${c.profitMultiplier}x</span>
                    </div>
                </div>
            `;
        }
        html += '</div>';
        panel.innerHTML = html;
    }

    function renderOperationsPanel() {
        const panel = document.getElementById('operationsPanel');
        if (!panel) return;

        let html = `
            <div class="create-operation-form" style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:12px;margin-bottom:16px;">
                <h4 style="color:#eab308;margin-bottom:12px;">New Smuggling Operation</h4>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
                    <select id="smuggleContraband" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                        <option value="">Select contraband...</option>
                        ${contrabandTypes.map(c => `<option value="${c.name}">${c.displayName} (Risk ${c.riskLevel})</option>`).join('')}
                    </select>
                    <input type="number" id="smuggleQuantity" placeholder="Quantity" min="1" value="1" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                    <input type="text" id="smuggleSource" placeholder="Source world" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                    <input type="text" id="smuggleDestination" placeholder="Destination world" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                    <input type="number" id="smuggleLaw" placeholder="Law level (0-8)" min="0" max="8" value="5" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                    <input type="text" id="smuggleStarport" placeholder="Starport (A-E)" maxlength="1" value="E" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                </div>
                <button onclick="SmugglingUI.createOperation()" style="margin-top:8px;background:rgba(234,179,8,0.2);color:#eab308;border:1px solid #eab308;border-radius:4px;padding:6px 16px;cursor:pointer;">Create Operation</button>
            </div>
        `;

        if (operations.length > 0) {
            html += '<h4 style="color:#eab308;margin-bottom:8px;">Active Operations</h4>';
            for (const op of operations) {
                const statusColor = op.status === 'Completed' ? '#22c55e' : op.status === 'Failed' ? '#ef4444' : op.status === 'Combat' ? '#f97316' : '#eab308';
                html += `
                    <div class="operation-item" style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:12px;margin-bottom:8px;">
                        <div style="display:flex;justify-content:space-between;align-items:center;">
                            <div>
                                <strong>Op #${op.id}</strong>: ${op.contraband} x${op.quantity}
                                <span style="color:var(--text-secondary);font-size:0.85rem;">(${op.sourceWorld} → ${op.destinationWorld})</span>
                            </div>
                            <span style="color:${statusColor};font-weight:bold;">${op.status}</span>
                        </div>
                        <div style="display:flex;gap:16px;margin-top:8px;font-size:0.85rem;color:var(--text-secondary);">
                            <span>Cost: ${op.purchaseCost} cr</span>
                            <span>Revenue: ${op.sellRevenue} cr</span>
                            <span>Profit: <span style="color:${op.profit >= 0 ? '#22c55e' : '#ef4444'}">${op.profit} cr</span></span>
                        </div>
                        ${op.status === 'Planned' ? `
                            <div style="margin-top:8px;display:flex;gap:8px;">
                                <select id="action-${op.id}" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:4px;">
                                    ${actions.map(a => `<option value="${a.name}">${a.displayName}</option>`).join('')}
                                </select>
                                <input type="number" id="pilot-${op.id}" placeholder="Pilot skill" min="0" max="5" value="0" style="width:80px;background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:4px;">
                                <input type="number" id="credits-${op.id}" placeholder="Credits" min="0" value="0" style="width:80px;background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:4px;">
                                <button onclick="SmugglingUI.executeCustoms(${op.id})" style="background:rgba(239,68,68,0.2);color:#ef4444;border:1px solid #ef4444;border-radius:4px;padding:4px 12px;cursor:pointer;">Customs Check</button>
                            </div>
                        ` : ''}
                        ${op.log && op.log.length > 0 ? `
                            <div style="margin-top:8px;padding:8px;background:rgba(0,0,0,0.3);border-radius:4px;font-size:0.8rem;color:var(--text-secondary);">
                                ${op.log.map(l => `<div>• ${l}</div>`).join('')}
                            </div>
                        ` : ''}
                    </div>
                `;
            }
        } else {
            html += '<div style="text-align:center;padding:20px;color:var(--text-secondary);">No active operations. Create one above.</div>';
        }

        panel.innerHTML = html;
    }

    function renderEstimatePanel() {
        const panel = document.getElementById('estimatePanel');
        if (!panel) return;

        let html = `
            <div style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:12px;">
                <h4 style="color:#eab308;margin-bottom:12px;">Profit Estimator</h4>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
                    <select id="estimateContraband" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                        <option value="">Select contraband...</option>
                        ${contrabandTypes.map(c => `<option value="${c.name}">${c.displayName}</option>`).join('')}
                    </select>
                    <input type="number" id="estimateQuantity" placeholder="Quantity" min="1" value="1" style="background:#1a1a2e;color:#e0e0e0;border:1px solid #2a2a3e;border-radius:4px;padding:6px;">
                </div>
                <button onclick="SmugglingUI.estimateProfit()" style="margin-top:8px;background:rgba(234,179,8,0.2);color:#eab308;border:1px solid #eab308;border-radius:4px;padding:6px 16px;cursor:pointer;">Estimate</button>
                <div id="estimateResult" style="margin-top:12px;"></div>
            </div>
        `;
        panel.innerHTML = html;
    }

    async function createOperation() {
        const contrabandType = document.getElementById('smuggleContraband').value;
        const quantity = parseInt(document.getElementById('smuggleQuantity').value) || 1;
        const sourceWorld = document.getElementById('smuggleSource').value || 'Unknown';
        const destinationWorld = document.getElementById('smuggleDestination').value || 'Unknown';
        const destinationLawLevel = parseInt(document.getElementById('smuggleLaw').value) || 5;
        const destinationStarport = document.getElementById('smuggleStarport').value || 'E';

        if (!contrabandType) {
            alert('Please select a contraband type');
            return;
        }

        try {
            const op = await GameAPI.createSmugglingOperation({
                contrabandType,
                quantity,
                sourceWorld,
                destinationWorld,
                destinationLawLevel,
                destinationStarport
            });
            operations.push(op);
            renderOperationsPanel();
        } catch (e) {
            console.error('Create operation failed:', e);
            alert('Failed to create operation');
        }
    }

    async function executeCustoms(id) {
        const actionEl = document.getElementById(`action-${id}`);
        const pilotEl = document.getElementById(`pilot-${id}`);
        const creditsEl = document.getElementById(`credits-${id}`);

        if (!actionEl) return;

        try {
            const op = await GameAPI.executeCustomsCheck(id, {
                action: actionEl.value,
                pilotSkill: parseInt(pilotEl.value) || 0,
                credits: parseInt(creditsEl.value) || 0
            });
            const idx = operations.findIndex(o => o.id === id);
            if (idx >= 0) operations[idx] = op;
            renderOperationsPanel();
        } catch (e) {
            console.error('Customs check failed:', e);
            alert('Customs check failed');
        }
    }

    async function estimateProfit() {
        const contrabandType = document.getElementById('estimateContraband').value;
        const quantity = parseInt(document.getElementById('estimateQuantity').value) || 1;

        if (!contrabandType) {
            alert('Please select a contraband type');
            return;
        }

        try {
            const result = await GameAPI.estimateSmugglingProfit({
                contrabandType,
                quantity
            });
            const resultEl = document.getElementById('estimateResult');
            if (resultEl) {
                resultEl.innerHTML = `
                    <div style="background:rgba(0,0,0,0.3);border-radius:4px;padding:12px;">
                        <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;font-size:0.9rem;">
                            <div>Contraband: <strong style="color:#eab308;">${result.contraband}</strong></div>
                            <div>Quantity: <strong>${result.quantity}</strong></div>
                            <div>Purchase Cost: <strong>${result.purchaseCost} cr</strong></div>
                            <div>Est. Revenue: <strong>${result.estimatedRevenue} cr</strong></div>
                            <div>Est. Profit: <strong style="color:${result.estimatedProfit >= 0 ? '#22c55e' : '#ef4444'}">${result.estimatedProfit} cr</strong></div>
                            <div>Risk Level: <strong style="color:${result.riskLevel >= 4 ? '#ef4444' : result.riskLevel >= 3 ? '#f97316' : '#eab308'}">${result.riskLevel}</strong></div>
                        </div>
                    </div>
                `;
            }
        } catch (e) {
            console.error('Estimate failed:', e);
            alert('Estimate failed');
        }
    }

    function createModalHTML() {
        if (document.getElementById('smugglingModal')) return;

        const modal = document.createElement('div');
        modal.id = 'smugglingModal';
        modal.className = 'hidden';
        modal.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);z-index:1000;display:flex;align-items:center;justify-content:center;';
        modal.innerHTML = `
            <div style="background:#1a1a2e;border:1px solid #2a2a3e;border-radius:12px;width:90%;max-width:900px;max-height:80vh;overflow:hidden;display:flex;flex-direction:column;">
                <div style="display:flex;justify-content:space-between;align-items:center;padding:16px;border-bottom:1px solid #2a2a3e;">
                    <h3 style="color:#eab308;margin:0;">🚢 Smuggling (B07)</h3>
                    <button id="smugglingModalClose" style="background:none;border:none;color:#888;font-size:1.5rem;cursor:pointer;">&times;</button>
                </div>
                <div style="display:flex;gap:4px;padding:12px 16px 0;border-bottom:1px solid #2a2a3e;">
                    <button class="smuggling-tab" data-tab="contraband" style="background:rgba(234,179,8,0.2);color:#eab308;border:1px solid rgba(234,179,8,0.3);border-radius:4px;padding:6px 12px;cursor:pointer;font-size:0.85rem;">Contraband</button>
                    <button class="smuggling-tab" data-tab="operations" style="background:rgba(255,255,255,0.05);color:var(--text-secondary);border:1px solid transparent;border-radius:4px;padding:6px 12px;cursor:pointer;font-size:0.85rem;">Operations</button>
                    <button class="smuggling-tab" data-tab="estimate" style="background:rgba(255,255,255,0.05);color:var(--text-secondary);border:1px solid transparent;border-radius:4px;padding:6px 12px;cursor:pointer;font-size:0.85rem;">Estimate</button>
                </div>
                <div style="padding:16px;overflow-y:auto;flex:1;">
                    <div id="contrabandPanel" class="smuggling-panel"></div>
                    <div id="operationsPanel" class="smuggling-panel hidden"></div>
                    <div id="estimatePanel" class="smuggling-panel hidden"></div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        modal.querySelectorAll('.smuggling-tab').forEach(tab => {
            tab.addEventListener('click', () => switchTab(tab.dataset.tab));
        });
    }

    function init() {
        createModalHTML();
        const closeBtn = document.getElementById('smugglingModalClose');
        if (closeBtn) closeBtn.addEventListener('click', hideSmugglingModal);
    }

    return {
        showSmugglingModal,
        hideSmugglingModal,
        loadAllSmugglingData,
        switchTab,
        createOperation,
        executeCustoms,
        estimateProfit,
        init
    };
})();

document.addEventListener('DOMContentLoaded', () => SmugglingUI.init());
