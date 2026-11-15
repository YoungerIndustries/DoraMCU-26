/* DoraMCU 4X Economy HUD — Dashboard for economic state, pipeline, trade, income, technology, tax, transports, and market */

const EconomyHUD = (function() {
    let currentSessionId = null;
    let refreshInterval = null;
    const REFRESH_SECONDS = 10;

    // ---- Utility ----

    function formatNumber(n) {
        if (n === null || n === undefined) return '—';
        if (Number.isInteger(n)) return n.toLocaleString();
        return n.toLocaleString(undefined, { maximumFractionDigits: 1 });
    }

    function formatCredits(c) {
        return formatNumber(c) + ' Cr';
    }

    function setElementText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }

    function setElementHTML(id, html) {
        const el = document.getElementById(id);
        if (el) el.innerHTML = html;
    }

    function showLoading(containerId) {
        setElementHTML(containerId, '<p class="empty-state">Loading...</p>');
    }

    function showError(containerId, message) {
        setElementHTML(containerId, `<p class="empty-state" style="color:var(--accent-danger);">${message || 'Failed to load data'}</p>`);
    }

    // ---- Session Selection ----

    async function populateSessionSelect() {
        const select = document.getElementById('economySessionSelect');
        if (!select) return;
        try {
            const sessions = await GameAPI.listSessions();
            select.innerHTML = '<option value="">Select a session...</option>';
            if (Array.isArray(sessions)) {
                sessions.forEach(s => {
                    const opt = document.createElement('option');
                    opt.value = s.sessionId || s.id;
                    opt.textContent = (s.sessionName || s.name || s.sessionId || s.id) + ' (Turn ' + (s.currentTurn || '?') + ')';
                    select.appendChild(opt);
                });
            }
            // Restore last selected session
            const saved = localStorage.getItem('economy_session_id');
            if (saved) {
                for (const opt of select.options) {
                    if (opt.value === saved) {
                        select.value = saved;
                        loadEconomyData(saved);
                        break;
                    }
                }
            }
        } catch (e) {
            console.warn('Failed to load sessions for economy HUD:', e);
        }
    }

    function selectSession() {
        const select = document.getElementById('economySessionSelect');
        if (!select || !select.value) return;
        currentSessionId = select.value;
        localStorage.setItem('economy_session_id', currentSessionId);
        loadEconomyData(currentSessionId);
        startAutoRefresh();
    }

    // ---- Auto Refresh ----

    function startAutoRefresh() {
        stopAutoRefresh();
        if (!currentSessionId) return;
        refreshInterval = setInterval(() => {
            if (currentSessionId) {
                loadEconomyData(currentSessionId);
            }
        }, REFRESH_SECONDS * 1000);
    }

    function stopAutoRefresh() {
        if (refreshInterval) {
            clearInterval(refreshInterval);
            refreshInterval = null;
        }
    }

    // ---- Main Data Loading ----

    async function loadEconomyData(sessionId) {
        currentSessionId = sessionId;
        // Load all sections in parallel
        await Promise.allSettled([
            loadOverview(sessionId),
            loadPipelineStatus(sessionId),
            loadTransportStatus(sessionId),
            loadTradeSummary(sessionId),
            loadIncomeProjection(sessionId),
            loadTechnologyProgress(sessionId),
            loadTaxSummary(sessionId),
            loadRecipes(sessionId),
            loadMarketBoard(sessionId)
        ]);
    }

    // ---- Overview ----

    async function loadOverview(sessionId) {
        try {
            const data = await GameAPI.getEconomicDashboard(sessionId);
            if (!data) return;

            // Try to extract from dashboard or fallback to economic state
            const econ = data.economicState || data;
            setElementText('econNetWorth', formatCredits(econ.netWorth || econ.networth || 0));
            setElementText('econCredits', formatCredits(econ.creditBalance || econ.credits || 0));

            // Income rate from projection
            const income = data.incomeProjection || data.income;
            if (income) {
                setElementText('econIncomeRate', formatCredits(income.currentIncome || income.currentIncomeRate || 0));
                const trend = income.trendDirection || income.trend;
                const trendIcon = trend === 'GROWING' ? '↑' : trend === 'DECLINING' ? '↓' : '→';
                setElementText('econIncomeTrend', trendIcon + ' ' + (trend || '—'));
            }

            // Research points
            const tech = data.technologySummary || data.technology;
            setElementText('econResearchPoints', formatNumber(tech.researchPoints || tech.availablePoints || 0));
            setElementText('econResearchSub', 'Level ' + (tech.researchLevel || tech.level || 1));

        } catch (e) {
            console.warn('Failed to load economy overview:', e);
            // Fallback to basic economic state
            try {
                const data = await GameAPI.getEconomicState(sessionId);
                if (data) {
                    setElementText('econNetWorth', formatCredits(data.netWorth || data.networth || 0));
                    setElementText('econCredits', formatCredits(data.creditBalance || data.credits || 0));
                }
            } catch (e2) {
                console.warn('Fallback economy state also failed:', e2);
            }
        }
    }

    // ---- Pipeline Status ----

    async function loadPipelineStatus(sessionId) {
        const container = 'pipelineStatus';
        try {
            const data = await GameAPI.getEconomicDashboard(sessionId);
            const pipeline = data.pipelineStatus || data.resourcePipeline || data;
            if (!pipeline) {
                setElementHTML(container, '<p class="empty-state">No pipeline data</p>');
                return;
            }

            let html = '';

            // Raw resources
            const rawStorage = pipeline.rawStorage || pipeline.raw;
            if (rawStorage) {
                html += renderStorageSection('Raw Resources', rawStorage);
            }

            // Refined resources
            const refinedStorage = pipeline.refinedStorage || pipeline.refined;
            if (refinedStorage) {
                html += renderStorageSection('Refined Resources', refinedStorage);
            }

            // Modules
            const moduleStorage = pipeline.moduleStorage || pipeline.modules;
            if (moduleStorage) {
                html += renderStorageSection('Construction Modules', moduleStorage);
            }

            // Active operations
            const miningOps = pipeline.miningOperations || pipeline.mining;
            const processingOps = pipeline.processingFacilities || pipeline.processing;
            const assemblyOps = pipeline.assemblyFacilities || pipeline.assembly;

            if (miningOps && miningOps.length) {
                html += '<div style="margin-top:12px;"><strong>Mining Operations:</strong> ' + miningOps.length + ' active</div>';
            }
            if (processingOps && processingOps.length) {
                html += '<div style="margin-top:4px;"><strong>Processing:</strong> ' + processingOps.length + ' active</div>';
            }
            if (assemblyOps && assemblyOps.length) {
                html += '<div style="margin-top:4px;"><strong>Assembly:</strong> ' + assemblyOps.length + ' active</div>';
            }

            setElementHTML(container, html || '<p class="empty-state">Pipeline idle</p>');

        } catch (e) {
            console.warn('Failed to load pipeline status:', e);
            showError(container, 'Failed to load pipeline data');
        }
    }

    function renderStorageSection(title, storage) {
        let html = '<div style="margin-top:8px;"><strong>' + title + '</strong>';

        // Try to iterate over resource entries
        const entries = storage.resources || storage.entries || storage;
        const capacity = storage.capacity || storage.totalCapacity || 0;

        if (typeof entries === 'object' && !Array.isArray(entries)) {
            let totalUsed = 0;
            html += '<div style="margin-top:4px;">';
            for (const [key, val] of Object.entries(entries)) {
                const amount = typeof val === 'number' ? val : (val.amount || val.quantity || 0);
                totalUsed += amount;
                const pct = capacity > 0 ? Math.round((amount / capacity) * 100) : 0;
                html += '<div class="resource-bar" style="margin:2px 0;">';
                html += '<span style="display:inline-block;width:100px;font-size:0.85em;">' + key + '</span> ';
                html += '<div class="progress-bar" style="display:inline-block;width:120px;height:12px;background:var(--bg-primary);border-radius:6px;overflow:hidden;vertical-align:middle;">';
                html += '<div class="progress-fill" style="width:' + pct + '%;height:100%;background:var(--accent-primary);border-radius:6px;"></div>';
                html += '</div> ';
                html += '<span style="font-size:0.85em;">' + formatNumber(amount) + '</span>';
                html += '</div>';
            }
            html += '</div>';
            const fillPct = capacity > 0 ? Math.round((totalUsed / capacity) * 100) : 0;
            html += '<div style="font-size:0.8em;color:var(--text-secondary);">Total: ' + formatNumber(totalUsed) + '/' + formatNumber(capacity) + ' (' + fillPct + '%)</div>';
        }

        html += '</div>';
        return html;
    }

    // ---- Transport Status ----

    async function loadTransportStatus(sessionId) {
        const container = 'transportStatus';
        try {
            const data = await GameAPI.getTransportSummary(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No transport data</p>');
                return;
            }

            const transports = data.transportOrders || data.orders || data;
            if (!transports || !transports.length) {
                setElementHTML(container, '<p class="empty-state">No active transports</p>');
                return;
            }

            let html = '';
            // Summary by priority
            const byPriority = { NORMAL: 0, URGENT: 0, BULK: 0 };
            transports.forEach(t => {
                const p = t.priority || 'NORMAL';
                if (byPriority.hasOwnProperty(p)) byPriority[p]++;
            });

            html += '<div style="margin-bottom:8px;">';
            html += '<span style="color:var(--accent-primary);">URGENT: ' + byPriority.URGENT + '</span> | ';
            html += '<span style="color:var(--text-primary);">NORMAL: ' + byPriority.NORMAL + '</span> | ';
            html += '<span style="color:var(--accent-warning);">BULK: ' + byPriority.BULK + '</span>';
            html += '</div>';

            // List active transports
            const active = transports.filter(t => !t.completed && !t.cancelled);
            if (active.length) {
                html += '<div style="max-height:200px;overflow-y:auto;">';
                active.slice(0, 10).forEach(t => {
                    const progress = t.turnsTotal > 0 ? Math.round(((t.turnsTotal - t.turnsRemaining) / t.turnsTotal) * 100) : 0;
                    const priorityColor = t.priority === 'URGENT' ? 'var(--accent-primary)' : t.priority === 'BULK' ? 'var(--accent-warning)' : 'var(--text-secondary)';
                    html += '<div style="padding:4px 0;border-bottom:1px solid var(--border-color);font-size:0.85em;">';
                    html += '<span style="color:' + priorityColor + ';">' + (t.priority || 'NORMAL') + '</span> ';
                    html += t.resourceType || 'Mixed';
                    if (t.consolidated) html += ' (consolidated)';
                    html += ' — ' + formatNumber(t.amountLoaded || 0) + '/' + formatNumber(t.totalAmount || 0);
                    html += ' [' + progress + '%]';
                    if (t.assignedFleetId != null) html += ' Fleet #' + t.assignedFleetId;
                    html += '</div>';
                });
                if (active.length > 10) {
                    html += '<div style="font-size:0.8em;color:var(--text-secondary);">... and ' + (active.length - 10) + ' more</div>';
                }
                html += '</div>';
            }

            setElementHTML(container, html);

        } catch (e) {
            console.warn('Failed to load transport status:', e);
            showError(container, 'Failed to load transport data');
        }
    }

    // ---- Trade Summary ----

    async function loadTradeSummary(sessionId) {
        const container = 'tradeSummary';
        try {
            const data = await GameAPI.getTradeSummary(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No trade data</p>');
                return;
            }

            let html = '';
            html += '<div class="metric"><span class="metric-label">Total Trades:</span> <span class="metric-value">' + formatNumber(data.totalTrades || data.tradeCount || 0) + '</span></div>';
            html += '<div class="metric"><span class="metric-label">Total Profit:</span> <span class="metric-value">' + formatCredits(data.totalProfit || 0) + '</span></div>';
            html += '<div class="metric"><span class="metric-label">Average Profit:</span> <span class="metric-value">' + formatCredits(data.averageProfit || 0) + '</span></div>';
            html += '<div class="metric"><span class="metric-label">Win Rate:</span> <span class="metric-value">' + formatNumber(data.winRate || data.profitableRate || 0) + '%</span></div>';

            // Recent trades
            const recent = data.recentTrades || data.recentMissions || [];
            if (recent.length) {
                html += '<div style="margin-top:8px;"><strong>Recent Trades:</strong></div>';
                html += '<div style="max-height:150px;overflow-y:auto;">';
                recent.slice(0, 5).forEach(trade => {
                    const profitColor = (trade.profit || 0) >= 0 ? 'var(--accent-primary)' : 'var(--accent-danger)';
                    html += '<div style="padding:2px 0;font-size:0.85em;border-bottom:1px solid var(--border-color);">';
                    html += (trade.sourcePlanet || trade.source || '?') + ' → ' + (trade.destPlanet || trade.destination || '?');
                    html += ' — ' + (trade.commodity || '?');
                    html += ' <span style="color:' + profitColor + ';">' + formatCredits(trade.profit || 0) + '</span>';
                    html += '</div>';
                });
                html += '</div>';
            }

            setElementHTML(container, html);

        } catch (e) {
            console.warn('Failed to load trade summary:', e);
            showError(container, 'Failed to load trade data');
        }
    }

    // ---- Income Projection ----

    async function loadIncomeProjection(sessionId) {
        const container = 'incomeProjection';
        try {
            const data = await GameAPI.getIncomeProjection(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No projection data</p>');
                return;
            }

            let html = '';
            const trend = data.trendDirection || data.trend || '—';
            const trendIcon = trend === 'GROWING' ? '↑' : trend === 'DECLINING' ? '↓' : '→';
            const trendColor = trend === 'GROWING' ? 'var(--accent-primary)' : trend === 'DECLINING' ? 'var(--accent-danger)' : 'var(--text-secondary)';

            html += '<div class="metric"><span class="metric-label">Current Income:</span> <span class="metric-value">' + formatCredits(data.currentIncome || data.currentIncomeRate || 0) + '/turn</span></div>';
            html += '<div class="metric"><span class="metric-label">Growth Rate:</span> <span class="metric-value" style="color:' + trendColor + ';">' + trendIcon + ' ' + formatNumber(data.growthRate || 0) + '%</span></div>';
            html += '<div class="metric"><span class="metric-label">Trend:</span> <span class="metric-value" style="color:' + trendColor + ';">' + trend + '</span></div>';

            // 5-turn projection
            const projections = data.projections || data.turnProjections || [];
            if (projections.length) {
                html += '<div style="margin-top:8px;"><strong>Next 5 Turns Projection:</strong></div>';
                html += '<div class="projection-chart" style="display:flex;align-items:flex-end;gap:4px;height:80px;margin-top:4px;padding:4px;background:var(--bg-primary);border-radius:4px;">';
                const maxVal = Math.max(...projections.map(p => p.income || p.projectedIncome || 0), 1);
                projections.forEach((p, i) => {
                    const val = p.income || p.projectedIncome || 0;
                    const heightPct = Math.max(5, (val / maxVal) * 100);
                    const barColor = i === 0 ? 'var(--accent-primary)' : 'var(--accent-secondary)';
                    html += '<div style="flex:1;display:flex;flex-direction:column;align-items:center;">';
                    html += '<div style="font-size:0.7em;color:var(--text-secondary);">' + formatNumber(val) + '</div>';
                    html += '<div style="width:100%;height:' + heightPct + '%;background:' + barColor + ';border-radius:2px 2px 0 0;min-height:4px;"></div>';
                    html += '<div style="font-size:0.7em;color:var(--text-secondary);">T' + (p.turn || (i + 1)) + '</div>';
                    html += '</div>';
                });
                html += '</div>';
            }

            setElementHTML(container, html);

        } catch (e) {
            console.warn('Failed to load income projection:', e);
            showError(container, 'Failed to load projection data');
        }
    }

    // ---- Technology Progress ----

    async function loadTechnologyProgress(sessionId) {
        const container = 'technologyProgress';
        try {
            const data = await GameAPI.getTechnologyTree(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No technology data</p>');
                return;
            }

            let html = '';
            const upgrades = data.upgrades || data.technologies || data;

            if (Array.isArray(upgrades)) {
                upgrades.forEach(tech => {
                    const level = tech.currentLevel || tech.level || 0;
                    const maxLevel = tech.maxLevel || 3;
                    const isResearched = level > 0;
                    const canResearch = tech.canResearch || tech.available || false;

                    const barWidth = maxLevel > 0 ? (level / maxLevel) * 100 : 0;
                    html += '<div style="padding:4px 0;border-bottom:1px solid var(--border-color);">';
                    html += '<div style="display:flex;justify-content:space-between;align-items:center;">';
                    html += '<span style="font-size:0.9em;' + (isResearched ? '' : 'color:var(--text-secondary);') + '">' + (tech.name || tech.upgrade || '?') + '</span>';
                    html += '<span style="font-size:0.8em;">';
                    for (let i = 0; i < maxLevel; i++) {
                        html += '<span style="color:' + (i < level ? 'var(--accent-primary)' : 'var(--text-secondary)') + ';">●</span>';
                    }
                    html += '</span>';
                    html += '</div>';
                    if (canResearch && level < maxLevel) {
                        const cost = tech.researchCost || tech.cost || '?';
                        html += '<button class="btn btn-small" style="margin-top:4px;" onclick="EconomyHUD.research(\'' + (tech.name || tech.upgrade) + '\')">Research (' + cost + ' RP)</button>';
                    }
                    if (tech.description) {
                        html += '<div style="font-size:0.75em;color:var(--text-secondary);margin-top:2px;">' + tech.description + '</div>';
                    }
                    html += '</div>';
                });
            } else if (typeof upgrades === 'object') {
                for (const [key, val] of Object.entries(upgrades)) {
                    const level = typeof val === 'number' ? val : (val.level || 0);
                    html += '<div style="padding:2px 0;font-size:0.85em;">' + key + ': Level ' + level + '</div>';
                }
            }

            setElementHTML(container, html || '<p class="empty-state">No technologies</p>');

        } catch (e) {
            console.warn('Failed to load technology progress:', e);
            showError(container, 'Failed to load technology data');
        }
    }

    // ---- Tax Summary ----

    async function loadTaxSummary(sessionId) {
        const container = 'taxSummary';
        try {
            const data = await GameAPI.getTaxSummary(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No tax data</p>');
                return;
            }

            let html = '';
            html += '<div class="metric"><span class="metric-label">Total Tax Income:</span> <span class="metric-value">' + formatCredits(data.totalTax || data.totalIncome || 0) + '/turn</span></div>';
            html += '<div class="metric"><span class="metric-label">Tax Policy:</span> <span class="metric-value">' + (data.taxPolicy || data.policy || 'MEDIUM') + '</span></div>';
            html += '<div class="metric"><span class="metric-label">Taxed Planets:</span> <span class="metric-value">' + formatNumber(data.taxablePlanets || data.planetCount || 0) + '</span></div>';

            // Per-planet breakdown
            const planets = data.planetBreakdown || data.planets || [];
            if (planets.length) {
                html += '<div style="margin-top:8px;"><strong>Per-Planet Breakdown:</strong></div>';
                html += '<div style="max-height:120px;overflow-y:auto;">';
                planets.slice(0, 8).forEach(p => {
                    html += '<div style="padding:2px 0;font-size:0.85em;border-bottom:1px solid var(--border-color);">';
                    html += (p.planetName || p.name || '?');
                    html += ' — <span style="color:var(--accent-primary);">' + formatCredits(p.taxAmount || p.income || 0) + '</span>';
                    html += '</div>';
                });
                html += '</div>';
            }

            setElementHTML(container, html);

        } catch (e) {
            console.warn('Failed to load tax summary:', e);
            showError(container, 'Failed to load tax data');
        }
    }

    // ---- Recipes ----

    async function loadRecipes(sessionId) {
        const container = 'recipeList';
        try {
            const data = await GameAPI.getRecipes(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No recipe data</p>');
                return;
            }

            let html = '';

            // Processing recipes
            const processing = data.processingRecipes || [];
            if (processing.length > 0) {
                html += '<div style="margin-bottom:12px;"><strong>⚗️ Processing Recipes</strong>';
                for (const recipe of processing) {
                    const inputs = recipe.inputs || {};
                    const inputStr = Object.entries(inputs).map(([k, v]) => `${v} ${k}`).join(' + ');
                    html += `<div style="margin:4px 0;padding:4px 8px;background:var(--bg-dark);border-radius:4px;">`;
                    html += `<span style="color:var(--accent-primary);">${recipe.outputDisplay || recipe.output}</span>: `;
                    html += `<span style="opacity:0.7;">${inputStr}</span>`;
                    html += ` <span style="opacity:0.5;">(${recipe.processingTimeTurns} turns)</span>`;
                    if (recipe.alternative) {
                        const altInputs = recipe.alternative.inputs || {};
                        const altInputStr = Object.entries(altInputs).map(([k, v]) => `${v} ${k}`).join(' + ');
                        const unlocked = recipe.alternative.isUnlocked;
                        const lockIcon = unlocked ? '🔓' : '🔒';
                        html += ` <span style="opacity:0.5;">| Alt:</span> `;
                        html += `<span style="opacity:${unlocked ? 0.7 : 0.4};">${lockIcon} ${altInputStr} (${recipe.alternative.processingTimeTurns} turns)</span>`;
                    }
                    html += '</div>';
                }
                html += '</div>';
            }

            // Module recipes
            const modules = data.moduleRecipes || [];
            if (modules.length > 0) {
                html += '<div><strong>🔧 Module Assembly Recipes</strong>';
                for (const recipe of modules) {
                    const stdInputs = recipe.standardRecipe || {};
                    const stdInputStr = Object.entries(stdInputs).map(([k, v]) => `${v} ${k}`).join(' + ');
                    html += `<div style="margin:4px 0;padding:4px 8px;background:var(--bg-dark);border-radius:4px;">`;
                    html += `<span style="color:var(--accent-primary);">${recipe.moduleDisplay || recipe.module}</span>: `;
                    html += `<span style="opacity:0.7;">${stdInputStr}</span>`;
                    html += ` <span style="opacity:0.5;">(${recipe.standardTimeTurns} turns)</span>`;
                    const altUnlocked = recipe.alternativeUnlocked;
                    const altInputs = recipe.alternativeRecipe || {};
                    const altInputStr = Object.entries(altInputs).map(([k, v]) => `${v} ${k}`).join(' + ');
                    const lockIcon = altUnlocked ? '🔓' : '🔒';
                    html += ` <span style="opacity:0.5;">| Alt:</span> `;
                    html += `<span style="opacity:${altUnlocked ? 0.7 : 0.4};">${lockIcon} ${altInputStr} (${recipe.alternativeTimeTurns} turns)</span>`;
                    html += '</div>';
                }
                html += '</div>';
            }

            // Unlock status
            const unlocks = data.unlocks || {};
            if (unlocks) {
                html += '<div style="margin-top:8px;font-size:0.85em;opacity:0.6;">';
                html += '🔬 Advanced Processing: ' + (unlocks.advancedProcessing ? '✅ Unlocked' : '🔒 Locked') + ' | ';
                html += 'Advanced Assembly: ' + (unlocks.advancedAssembly ? '✅ Unlocked' : '🔒 Locked');
                html += '</div>';
            }

            setElementHTML(container, html || '<p class="empty-state">No recipes available</p>');

        } catch (e) {
            console.warn('Failed to load recipes:', e);
            showError(container, 'Failed to load recipe data');
        }
    }

    // ---- Market Board ----

    async function loadMarketBoard(sessionId) {
        const container = 'marketBoard';
        try {
            const data = await GameAPI.getMarketBoard(sessionId);
            if (!data) {
                setElementHTML(container, '<p class="empty-state">No market data</p>');
                return;
            }

            const listings = data.listings || data.orders || data;
            if (!listings || !listings.length) {
                setElementHTML(container, '<p class="empty-state">No active market listings</p>');
                return;
            }

            let html = '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:8px;">';
            listings.slice(0, 12).forEach(listing => {
                const typeColor = listing.listingType === 'BUY' ? 'var(--accent-primary)' : 'var(--accent-warning)';
                html += '<div style="padding:8px;background:var(--bg-primary);border-radius:4px;border-left:3px solid ' + typeColor + ';">';
                html += '<div style="display:flex;justify-content:space-between;">';
                html += '<span style="color:' + typeColor + ';font-weight:bold;">' + (listing.listingType || '?') + '</span>';
                html += '<span style="font-size:0.8em;color:var(--text-secondary);">' + (listing.empireName || listing.seller || '?') + '</span>';
                html += '</div>';
                html += '<div style="margin:4px 0;">' + (listing.commodity || '?') + ' × ' + formatNumber(listing.quantity || 0) + '</div>';
                html += '<div style="color:var(--accent-primary);">' + formatCredits(listing.price || 0) + '</div>';
                if (listing.expiresIn != null) {
                    html += '<div style="font-size:0.75em;color:var(--text-secondary);">Expires in ' + listing.expiresIn + ' turns</div>';
                }
                html += '</div>';
            });
            html += '</div>';

            setElementHTML(container, html);

        } catch (e) {
            console.warn('Failed to load market board:', e);
            showError(container, 'Failed to load market data');
        }
    }

    // ---- Actions ----

    async function research(technologyName) {
        if (!currentSessionId) return;
        try {
            await GameAPI.researchTechnology(currentSessionId, technologyName);
            await loadTechnologyProgress(currentSessionId);
        } catch (e) {
            console.warn('Research failed:', e);
            alert('Research failed: ' + (e.message || 'Unknown error'));
        }
    }

    async function setTaxPolicy() {
        if (!currentSessionId) return;
        const select = document.getElementById('taxPolicySelect');
        if (!select || !select.value) return;
        try {
            await GameAPI.setTaxPolicy(currentSessionId, select.value);
            await loadTaxSummary(currentSessionId);
        } catch (e) {
            console.warn('Set tax policy failed:', e);
            alert('Failed to set tax policy: ' + (e.message || 'Unknown error'));
        }
    }

    async function consolidateTransports() {
        if (!currentSessionId) return;
        try {
            await GameAPI.consolidateTransports(currentSessionId);
            await loadTransportStatus(currentSessionId);
        } catch (e) {
            console.warn('Consolidate transports failed:', e);
        }
    }

    async function optimizeTransports() {
        if (!currentSessionId) return;
        try {
            await GameAPI.optimizeTransports(currentSessionId);
            await loadTransportStatus(currentSessionId);
        } catch (e) {
            console.warn('Optimize transports failed:', e);
        }
    }

    async function refreshAll() {
        const select = document.getElementById('economySessionSelect');
        if (select && select.value) {
            await loadEconomyData(select.value);
        }
    }

    // ---- Initialization ----

    function init() {
        // Tab navigation: load sessions when economy tab is shown
        document.addEventListener('tabshown', function(e) {
            if (e.detail && e.detail.tab === 'economy') {
                populateSessionSelect();
            } else {
                stopAutoRefresh();
            }
        });

        // Session select change
        const sessionSelect = document.getElementById('economySessionSelect');
        if (sessionSelect) {
            sessionSelect.addEventListener('change', selectSession);
        }

        // Refresh button
        const refreshBtn = document.getElementById('refreshEconomyBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', refreshAll);
        }

        // Tax policy
        const setTaxBtn = document.getElementById('setTaxPolicyBtn');
        if (setTaxBtn) {
            setTaxBtn.addEventListener('click', setTaxPolicy);
        }

        // Transport actions
        const consolidateBtn = document.getElementById('consolidateTransportsBtn');
        if (consolidateBtn) {
            consolidateBtn.addEventListener('click', consolidateTransports);
        }

        const optimizeBtn = document.getElementById('optimizeTransportsBtn');
        if (optimizeBtn) {
            optimizeBtn.addEventListener('click', optimizeTransports);
        }

        // Initial load if session already selected
        populateSessionSelect();
    }

    // Public API
    return {
        init,
        refresh: refreshAll,
        research,
        loadEconomyData
    };
})();

// Auto-initialize
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => EconomyHUD.init());
} else {
    EconomyHUD.init();
}
