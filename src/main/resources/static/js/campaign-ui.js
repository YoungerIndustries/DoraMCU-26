/* ============================================
   Business Tycoon — Campaign UI Controller
   Connects campaign.html to the REST API
   ============================================ */

const CampaignUI = (function() {
    'use strict';

    let apiBase = '';
    let campaignId = null;
    let eventLogEntries = [];

    // API helpers
    function getApiUrl(path) {
        return apiBase + '/api/v1/campaigns' + path;
    }

    function apiGet(path) {
        return fetch(getApiUrl(path))
            .then(r => {
                if (!r.ok) throw new Error('API error: ' + r.status);
                return r.json();
            });
    }

    function apiPost(path, body) {
        return fetch(getApiUrl(path), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body || {})
        }).then(r => {
            if (!r.ok) throw new Error('API error: ' + r.status);
            return r.json();
        });
    }

    function apiPut(path, body) {
        return fetch(getApiUrl(path), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body || {})
        }).then(r => {
            if (!r.ok) throw new Error('API error: ' + r.status);
            return r.json();
        });
    }

    // Initialize
    function init() {
        apiBase = window.location.origin;
        const page = document.getElementById('campaignPage');
        if (!page) return;

        // Check if campaign ID is in hash
        const hash = window.location.hash;
        if (hash.startsWith('#game/business-tycoon/')) {
            campaignId = hash.replace('#game/business-tycoon/', '');
            loadCampaign();
        } else if (hash === '#game/business-tycoon') {
            showNewCampaignForm();
        }

        // Event listeners
        document.getElementById('backBtn').addEventListener('click', goBack);
        document.getElementById('processTurnBtn').addEventListener('click', processTurn);
        document.getElementById('newCampaignBtn').addEventListener('click', showNewCampaignForm);
        document.getElementById('overlayBtn').addEventListener('click', () => {
            document.getElementById('campaignOverlay').classList.remove('active');
            showNewCampaignForm();
        });

        // Sidebar navigation
        document.querySelectorAll('.campaign-nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const section = item.dataset.section;
                switchSection(section);
            });
        });

        page.classList.add('active');
    }

    function goBack() {
        window.location.hash = '#';
    }

    function switchSection(sectionName) {
        document.querySelectorAll('.campaign-nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.section === sectionName);
        });
        document.querySelectorAll('.campaign-section').forEach(sec => {
            sec.classList.toggle('active', sec.id === 'section-' + sectionName);
        });
    }

    // New campaign form
    function showNewCampaignForm() {
        switchSection('dashboard');
        const overview = document.getElementById('corpOverview');
        overview.innerHTML = `
            <div class="campaign-form">
                <div class="form-group">
                    <label>Corporation Name</label>
                    <input type="text" class="form-input" id="corpName" value="Stellar Express" placeholder="Your transport company name">
                </div>
                <div class="form-group">
                    <label>Starting Credits</label>
                    <input type="number" class="form-input" id="startingCredits" value="10000" min="1000" step="1000">
                </div>
                <div class="form-group">
                    <label>Number of AI Empires</label>
                    <select class="form-select" id="empireCount">
                        <option value="3">3 Empires (Easy)</option>
                        <option value="5" selected>5 Empires (Normal)</option>
                        <option value="7">7 Empires (Hard)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>AI Rival Corporations</label>
                    <select class="form-select" id="rivalCount">
                        <option value="1">1 Rival</option>
                        <option value="2" selected>2 Rivals</option>
                        <option value="3">3 Rivals</option>
                    </select>
                </div>
                <button class="turn-btn" id="startCampaignBtn">Start Campaign</button>
            </div>
        `;
        document.getElementById('startCampaignBtn').addEventListener('click', startCampaign);
        // Clear other sections
        document.getElementById('marketShareOverview').innerHTML = '<p style="color: var(--text-muted);">Start a campaign to see market share</p>';
        document.getElementById('activeContractsOverview').innerHTML = '<p style="color: var(--text-muted);">No active contracts</p>';
        document.getElementById('activeQuestsOverview').innerHTML = '<p style="color: var(--text-muted);">No active quests</p>';
    }

    function startCampaign() {
        const name = document.getElementById('corpName').value.trim() || 'Stellar Express';
        const credits = parseInt(document.getElementById('startingCredits').value) || 10000;
        const empireCount = parseInt(document.getElementById('empireCount').value) || 5;
        const rivalCount = parseInt(document.getElementById('rivalCount').value) || 2;

        apiPost('/', {
            corpName: name,
            ownerId: 'player1',
            startingCredits: credits,
            numEmpires: empireCount,
            numRivals: rivalCount
        }).then(data => {
            campaignId = data.campaignId || data.id;
            window.location.hash = '#game/business-tycoon/' + campaignId;
            loadCampaign();
        }).catch(err => {
            console.error('Failed to start campaign:', err);
            alert('Failed to start campaign: ' + err.message);
        });
    }

    // Load campaign state
    function loadCampaign() {
        if (!campaignId) return;
        apiGet('/' + campaignId).then(state => {
            updateHeader(state);
            updateDashboard(state);
            loadContracts();
            loadQuests();
            loadRoutes();
            loadReputation();
            loadMarketShare();
            loadObjectives(state);
        }).catch(err => {
            console.error('Failed to load campaign:', err);
            alert('Campaign not found. Starting new one.');
            showNewCampaignForm();
        });
    }

    function updateHeader(state) {
        const corp = state.playerCorp || state;
        document.getElementById('headerTurn').textContent = state.currentTurn || 0;
        document.getElementById('headerCredits').textContent = formatNumber(corp.credits || 0);
        document.getElementById('headerRank').textContent = corp.rank || 'Freelancer';
        const ms = state.marketShare || 0;
        document.getElementById('headerMarketShare').textContent = (ms * 100).toFixed(1) + '%';

        // Check win/lose
        if (state.gameEnded) {
            showEndOverlay(state);
        }
    }

    function updateDashboard(state) {
        const corp = state.playerCorp || state;

        // Corporation overview
        const overview = document.getElementById('corpOverview');
        if (campaignId && !document.getElementById('startCampaignBtn')) {
            overview.innerHTML = `
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                    <div><span style="color: var(--text-muted);">Name</span><br><strong>${corp.name || 'N/A'}</strong></div>
                    <div><span style="color: var(--text-muted);">Rank</span><br><strong style="color: var(--accent-cyan);">${corp.rank || 'Freelancer'}</strong></div>
                    <div><span style="color: var(--text-muted);">Credits</span><br><strong style="color: var(--accent-gold);">${formatNumber(corp.credits || 0)}</strong></div>
                    <div><span style="color: var(--text-muted);">Reputation</span><br><strong>${corp.reputation || 0}/100</strong></div>
                    <div><span style="color: var(--text-muted);">XP</span><br><strong>${corp.xp || 0}/${corp.xpToNextRank || 100}</strong></div>
                    <div><span style="color: var(--text-muted);">Assets</span><br><strong>${corp.totalAssetCount || 0}</strong></div>
                    <div><span style="color: var(--text-muted);">Planets</span><br><strong>${corp.planetsOperated || 0}</strong></div>
                    <div><span style="color: var(--text-muted);">Cargo Delivered</span><br><strong>${formatNumber(corp.totalCargoDelivered || 0)}</strong></div>
                </div>
            `;
        }

        // Market share overview
        const msOverview = document.getElementById('marketShareOverview');
        const ms = state.marketShare || 0;
        msOverview.innerHTML = `
            <div style="font-size: 2rem; font-weight: 700; color: var(--accent-green);">${(ms * 100).toFixed(1)}%</div>
            <div style="color: var(--text-muted); font-size: 0.85rem;">of total cargo flow</div>
            <div class="market-share-bar">
                <div class="market-share-segment" style="width: ${ms * 100}%; background: var(--accent-green);">${(ms * 100).toFixed(0)}%</div>
                <div class="market-share-segment" style="width: ${(1 - ms) * 100}%; background: var(--bg-tertiary);"></div>
            </div>
            <div style="color: var(--text-muted); font-size: 0.8rem;">Win at 60% for 3 consecutive turns</div>
        `;

        // Active contracts overview
        const acOverview = document.getElementById('activeContractsOverview');
        const activeContracts = state.activeContracts || [];
        if (activeContracts.length === 0) {
            acOverview.innerHTML = '<p style="color: var(--text-muted);">No active contracts</p>';
        } else {
            acOverview.innerHTML = activeContracts.slice(0, 3).map(c => `
                <div style="padding: 6px 0; border-bottom: 1px solid var(--glass-border);">
                    <div style="font-weight: 600; font-size: 0.85rem;">${c.contractType || c.name || 'Contract'}</div>
                    <div style="color: var(--text-muted); font-size: 0.75rem;">Turns left: ${c.turnsRemaining || '?'}</div>
                </div>
            `).join('');
        }

        // Active quests overview
        const aqOverview = document.getElementById('activeQuestsOverview');
        const activeQuests = state.activeQuests || [];
        if (activeQuests.length === 0) {
            aqOverview.innerHTML = '<p style="color: var(--text-muted);">No active quests</p>';
        } else {
            aqOverview.innerHTML = activeQuests.slice(0, 3).map(q => `
                <div style="padding: 6px 0; border-bottom: 1px solid var(--glass-border);">
                    <div style="font-weight: 600; font-size: 0.85rem;">${q.title || 'Quest'}</div>
                    <div style="color: var(--text-muted); font-size: 0.75rem;">${q.type || ''} — ${q.currentValue || 0}/${q.targetValue || '?'}</div>
                </div>
            `).join('');
        }
    }

    function loadObjectives(state) {
        // Objectives are shown in the dashboard
    }

    // Process turn
    function processTurn() {
        const btn = document.getElementById('processTurnBtn');
        btn.disabled = true;
        btn.textContent = 'Processing...';

        apiPut('/' + campaignId + '/turn', {}).then(state => {
            updateHeader(state);
            updateDashboard(state);
            loadContracts();
            loadQuests();
            loadRoutes();
            loadReputation();
            loadMarketShare();
            addEventLogEntry(state.currentTurn, 'Turn processed successfully');
        }).catch(err => {
            console.error('Turn processing failed:', err);
            alert('Turn processing failed: ' + err.message);
        }).finally(() => {
            btn.disabled = false;
            btn.textContent = 'Process Turn';
        });
    }

    // Contracts
    function loadContracts() {
        apiGet('/' + campaignId + '/contracts').then(data => {
            const available = data.available || [];
            const active = data.active || [];

            // Available contracts
            const avContainer = document.getElementById('availableContracts');
            if (available.length === 0) {
                avContainer.innerHTML = '<p style="color: var(--text-muted);">No available contracts. Process turns to generate new contracts.</p>';
            } else {
                avContainer.innerHTML = available.map(c => `
                    <div class="contract-card">
                        <h4>${c.contractType || c.name || 'Contract'}</h4>
                        <p>${c.description || 'Complete this contract for rewards.'}</p>
                        <div class="contract-meta">
                            <span class="meta-tag">${c.issuingEmpire || 'Empire'}</span>
                            <span class="meta-tag reward">💰 ${formatNumber(c.payment || c.rewardCredits || 0)}</span>
                            <span class="meta-tag">⏱ ${c.turnsRequired || '?'} turns</span>
                        </div>
                        <button class="accept-btn" data-contract-id="${c.id}">Accept Contract</button>
                    </div>
                `).join('');

                avContainer.querySelectorAll('.accept-btn').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const contractId = btn.dataset.contractId;
                        acceptContract(contractId);
                    });
                });
            }

            // Active contracts
            const acContainer = document.getElementById('activeContracts');
            if (active.length === 0) {
                acContainer.innerHTML = '<p style="color: var(--text-muted);">No active contracts</p>';
            } else {
                acContainer.innerHTML = active.map(c => `
                    <div class="contract-card">
                        <h4>${c.contractType || c.name || 'Contract'}</h4>
                        <div class="contract-meta">
                            <span class="meta-tag">Turns left: ${c.turnsRemaining || '?'}</span>
                            <span class="meta-tag reward">💰 ${formatNumber(c.payment || 0)}</span>
                        </div>
                    </div>
                `).join('');
            }
        }).catch(() => {});
    }

    function acceptContract(contractId) {
        apiPut('/' + campaignId + '/contract/' + contractId + '/accept', {}).then(() => {
            loadContracts();
            loadCampaign();
            addEventLogEntry(null, 'Accepted contract: ' + contractId);
        }).catch(err => {
            alert('Failed to accept contract: ' + err.message);
        });
    }

    // Quests
    function loadQuests() {
        apiGet('/' + campaignId + '/quests').then(data => {
            const available = data.available || [];
            const active = data.active || [];

            const avContainer = document.getElementById('availableQuests');
            if (available.length === 0) {
                avContainer.innerHTML = '<p style="color: var(--text-muted);">No available quests. Process turns to generate quests from AI empires.</p>';
            } else {
                avContainer.innerHTML = available.map(q => `
                    <div class="quest-card">
                        <h4>${q.title || 'Quest'}</h4>
                        <p>${q.description || 'Complete this quest for rewards.'}</p>
                        <div class="quest-meta">
                            <span class="meta-tag">${q.type || 'General'}</span>
                            <span class="meta-tag">${q.issuingEmpireName || 'Empire'}</span>
                            <span class="meta-tag reward">💰 ${formatNumber(q.rewardCredits || 0)}</span>
                            <span class="meta-tag reward">⭐ ${q.rewardXp || 0} XP</span>
                            <span class="meta-tag reward">👍 +${q.rewardReputation || 0} Rep</span>
                        </div>
                        ${q.minReputation ? `<div style="color: var(--text-muted); font-size: 0.75rem; margin-bottom: 8px;">Min reputation: ${q.minReputation}</div>` : ''}
                        <button class="accept-btn" data-quest-id="${q.id}">Accept Quest</button>
                    </div>
                `).join('');

                avContainer.querySelectorAll('.accept-btn').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const questId = btn.dataset.questId;
                        acceptQuest(questId);
                    });
                });
            }

            const acContainer = document.getElementById('activeQuests');
            if (active.length === 0) {
                acContainer.innerHTML = '<p style="color: var(--text-muted);">No active quests</p>';
            } else {
                acContainer.innerHTML = active.map(q => `
                    <div class="quest-card">
                        <h4>${q.title || 'Quest'}</h4>
                        <div class="quest-meta">
                            <span class="meta-tag">${q.type || ''}</span>
                            <span class="meta-tag">Progress: ${q.currentValue || 0}/${q.targetValue || '?'}</span>
                            <span class="meta-tag">Turns left: ${q.turnsRemaining || '?'}</span>
                        </div>
                        <div style="background: var(--bg-tertiary); height: 6px; border-radius: 3px; margin-top: 8px;">
                            <div style="background: var(--accent-cyan); height: 100%; border-radius: 3px; width: ${q.progressPercentage || 0}%;"></div>
                        </div>
                    </div>
                `).join('');
            }
        }).catch(() => {});
    }

    function acceptQuest(questId) {
        apiPut('/' + campaignId + '/quest/' + questId + '/accept', {}).then(() => {
            loadQuests();
            loadCampaign();
            addEventLogEntry(null, 'Accepted quest: ' + questId);
        }).catch(err => {
            alert('Failed to accept quest: ' + err.message);
        });
    }

    // Routes
    function loadRoutes() {
        apiGet('/' + campaignId + '/routes').then(routes => {
            const container = document.getElementById('tradeRoutes');
            if (!routes || routes.length === 0) {
                container.innerHTML = '<p style="color: var(--text-muted);">No trade routes established yet.</p>';
                return;
            }
            container.innerHTML = routes.map(r => `
                <div class="route-item">
                    <div>
                        <div class="route-name">${r.name || r.fromPlanet || ''} → ${r.toPlanet || ''}</div>
                        <div style="color: var(--text-muted); font-size: 0.8rem;">${r.routeType || 'Space'} · ${r.cargoType || 'Mixed'}</div>
                    </div>
                    <div class="route-revenue">💰 ${formatNumber(r.revenuePerTurn || 0)}/turn</div>
                </div>
            `).join('');
        }).catch(() => {});
    }

    // Reputation
    function loadReputation() {
        apiGet('/' + campaignId + '/reputation').then(data => {
            const container = document.getElementById('reputationList');
            const entries = data.reputation || data;
            if (!entries || Object.keys(entries).length === 0) {
                container.innerHTML = '<p style="color: var(--text-muted);">No empire relationships yet.</p>';
                return;
            }
            container.innerHTML = Object.entries(entries).map(([empireId, rep]) => {
                const value = typeof rep === 'object' ? rep.value : rep;
                const category = typeof rep === 'object' ? rep.category : '';
                const color = value >= 70 ? 'var(--accent-green)' : value >= 40 ? 'var(--accent-gold)' : value >= 20 ? 'var(--accent-orange)' : 'var(--accent-red)';
                return `
                    <div class="reputation-item">
                        <div class="reputation-header">
                            <span class="reputation-name">Empire ${empireId} ${category ? '(' + category + ')' : ''}</span>
                            <span class="reputation-value">${value}/100</span>
                        </div>
                        <div class="reputation-bar">
                            <div class="reputation-fill" style="width: ${value}%; background: ${color};"></div>
                        </div>
                    </div>
                `;
            }).join('');
        }).catch(() => {});
    }

    // Market Share
    function loadMarketShare() {
        apiGet('/' + campaignId + '/market-share').then(data => {
            const container = document.getElementById('marketShareLeaderboard');
            const leaderboard = data.leaderboard || data;
            if (!leaderboard || leaderboard.length === 0) {
                container.innerHTML = '<p style="color: var(--text-muted);">No market share data yet.</p>';
                return;
            }
            const colors = ['var(--accent-green)', 'var(--accent-blue)', 'var(--accent-purple)', 'var(--accent-orange)', 'var(--accent-red)'];
            const total = leaderboard.reduce((sum, entry) => sum + (entry.marketShare || 0), 0);

            container.innerHTML = `
                <div class="market-share-bar" style="height: 40px; margin-bottom: 20px;">
                    ${leaderboard.map((entry, i) => `
                        <div class="market-share-segment" style="width: ${(entry.marketShare || 0) * 100}%; background: ${colors[i % colors.length]};">
                            ${(entry.marketShare * 100).toFixed(1)}%
                        </div>
                    `).join('')}
                </div>
                ${leaderboard.map((entry, i) => `
                    <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--glass-border);">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="width: 12px; height: 12px; border-radius: 50%; background: ${colors[i % colors.length]};"></div>
                            <div>
                                <div style="font-weight: 600;">${entry.corporationName || entry.name || 'Unknown'}</div>
                                <div style="color: var(--text-muted); font-size: 0.8rem;">${entry.rank || ''} · ${formatNumber(entry.cargoDelivered || 0)} cargo</div>
                            </div>
                        </div>
                        <div style="font-weight: 700; color: ${colors[i % colors.length]};">${(entry.marketShare * 100).toFixed(1)}%</div>
                    </div>
                `).join('')}
            `;
        }).catch(() => {});
    }

    // Event log
    function addEventLogEntry(turn, message) {
        const entry = { turn: turn || '?', message, time: new Date().toLocaleTimeString() };
        eventLogEntries.unshift(entry);
        if (eventLogEntries.length > 100) eventLogEntries.pop();

        const container = document.getElementById('eventLog');
        container.innerHTML = eventLogEntries.map(e => `
            <div class="event-entry">
                <span class="turn">[Turn ${e.turn}]</span> ${e.message}
                <span style="color: var(--text-muted); float: right;">${e.time}</span>
            </div>
        `).join('');
    }

    // Win/Lose overlay
    function showEndOverlay(state) {
        const overlay = document.getElementById('campaignOverlay');
        const title = document.getElementById('overlayTitle');
        const message = document.getElementById('overlayMessage');

        if (state.winner) {
            title.textContent = '🏆 Victory!';
            title.className = 'win';
            message.textContent = state.winner + ' achieved galactic dominance!';
        } else if (state.loseReason) {
            title.textContent = '💀 Defeat';
            title.className = 'lose';
            message.textContent = state.loseReason;
        } else {
            title.textContent = 'Campaign Ended';
            title.className = '';
            message.textContent = 'The campaign has ended.';
        }

        overlay.classList.add('active');
    }

    // Utilities
    function formatNumber(num) {
        if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
        if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
        return num.toString();
    }

    return {
        init
    };
})();

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', CampaignUI.init);
