/**
 * EconomyViz — Economic data visualization overlay for the galaxy map.
 *
 * Provides:
 * - Color-coded galaxy bodies showing treasury health (green/yellow/red)
 * - Animated bond flow lines between galaxies with GalaxyBond data
 * - Fiscal cycle timeline overlay showing revenue/expenses over time
 * - Click-to-inspect: show detailed economic stats (credits, bonds, interest rates)
 * - REST API integration with GalaxyEconomicService
 *
 * Usage:
 *   const viz = new EconomyViz(sceneRenderer, apiBaseUrl);
 *   await viz.loadEconomicData(galaxyId);
 *   viz.destroy();
 */

class EconomyViz {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Economic data
        this.galaxyEconomies = new Map(); // galaxyId -> economic data
        this.bondFlows = new Map(); // bondId -> bond flow data
        this.fiscalCycles = [];
        
        // 3D objects
        this.healthIndicators = new Map(); // galaxyId -> THREE.Group
        this.bondFlowLines = new Map(); // bondId -> THREE.Line
        this.particles = [];
        
        // UI elements
        this.container = null;
        this.infoPanel = null;
        this.timelinePanel = null;
        this.statsOverlay = null;
        
        // Animation
        this.animationId = null;
        this.clock = new THREE.Clock();
        
        // Raycaster for interaction
        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        this.hoveredIndicator = null;
        
        // Callbacks
        this.onGalaxySelect = null;
        this.onBondClick = null;
    }

    /**
     * Initialize the economy visualization UI.
     */
    init() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'economy-viz-container';
        this.container.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 100;
        `;
        document.body.appendChild(this.container);
        
        // Create info panel
        this.infoPanel = document.createElement('div');
        this.infoPanel.className = 'economy-info-panel';
        this.infoPanel.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            width: 320px;
            max-height: 500px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 200, 100, 0.3);
            border-radius: 8px;
            padding: 15px;
            pointer-events: auto;
            color: #aaffaa;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.container.appendChild(this.infoPanel);
        
        // Create timeline panel
        this.timelinePanel = document.createElement('div');
        this.timelinePanel.className = 'economy-timeline';
        this.timelinePanel.style.cssText = `
            position: absolute;
            bottom: 10px;
            left: 10px;
            right: 10px;
            height: 150px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 200, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #aaffaa;
            font-family: 'Courier New', monospace;
            font-size: 11px;
            display: none;
        `;
        this.timelinePanel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 8px; color: #88ff88;">
                📊 Fiscal Cycle Timeline
            </div>
            <div id="economy-timeline-chart" style="height: 100px; position: relative;"></div>
        `;
        this.container.appendChild(this.timelinePanel);
        
        // Create stats overlay
        this.statsOverlay = document.createElement('div');
        this.statsOverlay.className = 'economy-stats';
        this.statsOverlay.style.cssText = `
            position: absolute;
            top: 10px;
            left: 10px;
            width: 200px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 200, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #aaffaa;
            font-family: 'Courier New', monospace;
            font-size: 11px;
        `;
        this.statsOverlay.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 8px; color: #88ff88;">
                💰 Economy Overview
            </div>
            <div id="economy-stats-content">
                <div style="color: #88aa88;">No data loaded</div>
            </div>
            <button id="economy-toggle-timeline" style="
                width: 100%;
                margin-top: 10px;
                background: rgba(50, 150, 50, 0.3);
                border: 1px solid rgba(100, 200, 100, 0.5);
                color: #aaffaa;
                padding: 5px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Show Timeline</button>
            <button id="economy-close-btn" style="
                width: 100%;
                margin-top: 5px;
                background: rgba(200, 50, 50, 0.3);
                border: 1px solid rgba(255, 100, 100, 0.5);
                color: #ffaaaa;
                padding: 5px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Close</button>
        `;
        this.container.appendChild(this.statsOverlay);
        
        // Event listeners
        document.getElementById('economy-toggle-timeline').addEventListener('click', () => {
            this.toggleTimeline();
        });
        document.getElementById('economy-close-btn').addEventListener('click', () => {
            this.destroy();
        });
        
        // Mouse events for raycasting
        this.container.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.container.addEventListener('click', (e) => this.onMouseClick(e));
        
        console.log('[EconomyViz] Initialized');
    }

    /**
     * Load economic data for a galaxy.
     * @param {number} galaxyId - The galaxy ID
     */
    async loadEconomicData(galaxyId) {
        try {
            // Load fiscal summary
            const summaryResponse = await fetch(`${this.apiBaseUrl}/galaxy-economic/${galaxyId}/fiscal-summary`);
            if (summaryResponse.ok) {
                const summary = await summaryResponse.json();
                this.galaxyEconomies.set(galaxyId, summary);
            }
            
            // Load fiscal cycles
            const cyclesResponse = await fetch(`${this.apiBaseUrl}/galaxy-economic/${galaxyId}/cycles`);
            if (cyclesResponse.ok) {
                this.fiscalCycles = await cyclesResponse.json();
            }
            
            // Load bonds
            const bondsResponse = await fetch(`${this.apiBaseUrl}/galaxy-economic/${galaxyId}/bonds`);
            if (bondsResponse.ok) {
                const bonds = await bondsResponse.json();
                this.bondFlows.clear();
                bonds.forEach(bond => this.bondFlows.set(bond.id, bond));
            }
            
            this.updateHealthIndicators();
            this.updateBondFlows();
            this.updateStatsOverlay();
            console.log(`[EconomyViz] Loaded economic data for galaxy ${galaxyId}`);
        } catch (error) {
            console.warn('[EconomyViz] Failed to load economic data, using simulated data:', error.message);
            this.loadSimulatedData(galaxyId);
        }
    }

    /**
     * Load simulated economic data as fallback.
     * @param {number} galaxyId - The galaxy ID
     */
    loadSimulatedData(galaxyId) {
        // Simulated fiscal summary
        this.galaxyEconomies.set(galaxyId, {
            balance: 1500000,
            totalTaxesCollected: 5000000,
            totalImprovementCosts: 3000000,
            totalSurplusTransferred: 1000000,
            totalDeficitBonds: 500000,
            taxRate: 0.05,
            bondInterestRate: 0.10,
            lastProcessedCycle: 12,
            totalOutstandingDebt: 500000,
            outstandingBondCount: 5
        });
        
        // Simulated fiscal cycles
        this.fiscalCycles = [];
        for (let i = 1; i <= 12; i++) {
            this.fiscalCycles.push({
                cycleNumber: i,
                taxesCollected: 400000 + Math.random() * 200000,
                improvementCosts: 200000 + Math.random() * 150000,
                surplusTransferred: 100000 + Math.random() * 100000,
                deficitBonds: Math.random() > 0.7 ? 50000 : 0
            });
        }
        
        // Simulated bonds
        this.bondFlows.clear();
        for (let i = 0; i < 5; i++) {
            this.bondFlows.set(`bond-${i}`, {
                id: `bond-${i}`,
                amount: 100000,
                interestRate: 0.10,
                issuerGalaxy: galaxyId,
                holderGalaxy: galaxyId + i + 1,
                status: 'OUTSTANDING'
            });
        }
        
        this.updateHealthIndicators();
        this.updateBondFlows();
        this.updateStatsOverlay();
    }

    /**
     * Update health indicators for all galaxies.
     */
    updateHealthIndicators() {
        // Clear existing indicators
        this.clearHealthIndicators();
        
        for (const [galaxyId, economy] of this.galaxyEconomies) {
            const health = this.calculateHealth(economy);
            const indicator = this.createHealthIndicator(galaxyId, health);
            this.healthIndicators.set(galaxyId, indicator);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(indicator);
            }
        }
    }

    /**
     * Calculate health score from economic data.
     * @param {Object} economy - Economic data
     * @returns {number} Health score 0-1
     */
    calculateHealth(economy) {
        const balance = economy.balance || 0;
        const debt = economy.totalOutstandingDebt || 0;
        const taxes = economy.totalTaxesCollected || 1;
        
        // Health based on balance-to-debt ratio and tax collection
        const balanceRatio = balance / (debt + 1);
        const taxHealth = Math.min(taxes / 1000000, 1);
        
        return Math.min((balanceRatio * 0.6 + taxHealth * 0.4), 1);
    }

    /**
     * Create a health indicator for a galaxy.
     * @param {number} galaxyId - The galaxy ID
     * @param {number} health - Health score 0-1
     * @returns {THREE.Group} The indicator group
     */
    createHealthIndicator(galaxyId, health) {
        const group = new THREE.Group();
        group.userData = { galaxyId, health, type: 'economy_indicator' };
        
        // Position based on galaxy ID (simulated)
        const angle = (galaxyId % 10) * Math.PI * 2 / 10;
        const radius = 5 + (galaxyId % 5);
        group.position.set(
            Math.cos(angle) * radius,
            (galaxyId % 3) * 2 - 2,
            Math.sin(angle) * radius
        );
        
        // Color based on health
        let color;
        if (health > 0.6) {
            color = 0x44ff44; // Green - healthy
        } else if (health > 0.3) {
            color = 0xffff44; // Yellow - warning
        } else {
            color = 0xff4444; // Red - critical
        }
        
        // Main sphere
        const sphereGeometry = new THREE.SphereGeometry(0.5, 16, 16);
        const sphereMaterial = new THREE.MeshPhongMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.3,
            transparent: true,
            opacity: 0.7
        });
        const sphere = new THREE.Mesh(sphereGeometry, sphereMaterial);
        group.add(sphere);
        
        // Ring indicator
        const ringGeometry = new THREE.RingGeometry(0.6, 0.7, 32);
        const ringMaterial = new THREE.MeshPhongMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.5,
            side: THREE.DoubleSide
        });
        const ring = new THREE.Mesh(ringGeometry, ringMaterial);
        ring.rotation.x = Math.PI / 2;
        group.add(ring);
        
        // Health bar
        const barGeometry = new THREE.PlaneGeometry(1.5, 0.1);
        const barMaterial = new THREE.MeshPhongMaterial({
            color: 0x333333,
            side: THREE.DoubleSide
        });
        const bar = new THREE.Mesh(barGeometry, barMaterial);
        bar.position.y = -1;
        group.add(bar);
        
        // Health fill
        const fillGeometry = new THREE.PlaneGeometry(1.5 * health, 0.1);
        const fillMaterial = new THREE.MeshPhongMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.3,
            side: THREE.DoubleSide
        });
        const fill = new THREE.Mesh(fillGeometry, fillMaterial);
        fill.position.y = -1;
        fill.position.x = -(1 - health) * 0.75;
        group.add(fill);
        
        return group;
    }

    /**
     * Clear all health indicators.
     */
    clearHealthIndicators() {
        if (this.sceneRenderer?.scene) {
            this.healthIndicators.forEach(indicator => {
                this.sceneRenderer.scene.remove(indicator);
            });
        }
        this.healthIndicators.clear();
    }

    /**
     * Update bond flow lines.
     */
    updateBondFlows() {
        this.clearBondFlows();
        
        for (const [bondId, bond] of this.bondFlows) {
            const line = this.createBondFlowLine(bondId, bond);
            this.bondFlowLines.set(bondId, line);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(line);
            }
        }
    }

    /**
     * Create a bond flow line between galaxies.
     * @param {string} bondId - The bond ID
     * @param {Object} bond - Bond data
     * @returns {THREE.Group} The line group
     */
    createBondFlowLine(bondId, bond) {
        const group = new THREE.Group();
        group.userData = { bondId, type: 'bond_flow' };
        
        // Simulated positions for issuer and holder galaxies
        const issuerAngle = (bond.issuerGalaxy || 0) * Math.PI * 2 / 10;
        const holderAngle = (bond.holderGalaxy || 1) * Math.PI * 2 / 10;
        const radius = 7;
        
        const start = new THREE.Vector3(
            Math.cos(issuerAngle) * radius,
            0,
            Math.sin(issuerAngle) * radius
        );
        const end = new THREE.Vector3(
            Math.cos(holderAngle) * radius,
            0,
            Math.sin(holderAngle) * radius
        );
        
        // Create curved line
        const mid = start.clone().add(end).multiplyScalar(0.5);
        mid.y = 2; // Arc height
        
        const curve = new THREE.QuadraticBezierCurve3(start, mid, end);
        const points = curve.getPoints(50);
        
        const lineGeometry = new THREE.BufferGeometry().setFromPoints(points);
        const lineMaterial = new THREE.LineBasicMaterial({
            color: 0xffaa00,
            transparent: true,
            opacity: 0.6
        });
        const line = new THREE.Line(lineGeometry, lineMaterial);
        group.add(line);
        
        // Animated particles along the line
        const particleCount = 5;
        for (let i = 0; i < particleCount; i++) {
            const particleGeometry = new THREE.SphereGeometry(0.08, 8, 8);
            const particleMaterial = new THREE.MeshPhongMaterial({
                color: 0xffdd44,
                emissive: 0xffaa00,
                emissiveIntensity: 0.8
            });
            const particle = new THREE.Mesh(particleGeometry, particleMaterial);
            particle.userData = {
                curve: curve,
                progress: i / particleCount,
                speed: 0.1 + Math.random() * 0.1
            };
            group.add(particle);
            this.particles.push(particle);
        }
        
        return group;
    }

    /**
     * Clear all bond flow lines.
     */
    clearBondFlows() {
        if (this.sceneRenderer?.scene) {
            this.bondFlowLines.forEach(line => {
                this.sceneRenderer.scene.remove(line);
            });
        }
        this.bondFlowLines.clear();
        this.particles = [];
    }

    /**
     * Update the stats overlay.
     */
    updateStatsOverlay() {
        const content = document.getElementById('economy-stats-content');
        if (!content) return;
        
        let totalBalance = 0;
        let totalDebt = 0;
        let totalTaxes = 0;
        let galaxyCount = 0;
        
        for (const economy of this.galaxyEconomies.values()) {
            totalBalance += economy.balance || 0;
            totalDebt += economy.totalOutstandingDebt || 0;
            totalTaxes += economy.totalTaxesCollected || 0;
            galaxyCount++;
        }
        
        if (galaxyCount === 0) {
            content.innerHTML = '<div style="color: #88aa88;">No data loaded</div>';
            return;
        }
        
        content.innerHTML = `
            <div>Galaxies: ${galaxyCount}</div>
            <div>Balance: ${this.formatCredits(totalBalance)}</div>
            <div>Debt: ${this.formatCredits(totalDebt)}</div>
            <div>Taxes: ${this.formatCredits(totalTaxes)}</div>
            <div>Bonds: ${this.bondFlows.size}</div>
            <div style="margin-top: 8px; color: #88aa88;">
                Health: ${this.getHealthLabel(totalBalance, totalDebt)}
            </div>
        `;
    }

    /**
     * Format credits with commas.
     * @param {number} credits - Credit amount
     * @returns {string} Formatted string
     */
    formatCredits(credits) {
        return Math.round(credits).toLocaleString() + ' Ccr';
    }

    /**
     * Get health label based on balance and debt.
     * @param {number} balance - Total balance
     * @param {number} debt - Total debt
     * @returns {string} Health label
     */
    getHealthLabel(balance, debt) {
        const ratio = balance / (debt + 1);
        if (ratio > 2) return '🟢 Healthy';
        if (ratio > 0.5) return '🟡 Warning';
        return '🔴 Critical';
    }

    /**
     * Show the info panel for a galaxy.
     * @param {number} galaxyId - The galaxy ID
     */
    showInfoPanel(galaxyId) {
        const economy = this.galaxyEconomies.get(galaxyId);
        if (!economy || !this.infoPanel) return;
        
        const health = this.calculateHealth(economy);
        const healthColor = health > 0.6 ? '#44ff44' : health > 0.3 ? '#ffff44' : '#ff4444';
        
        this.infoPanel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #88ff88; font-size: 14px;">
                Galaxy ${galaxyId} Economy
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: ${healthColor}; font-weight: bold;">
                    Health: ${Math.round(health * 100)}%
                </div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #88aa88; font-size: 10px;">TREASURY</div>
                <div>Balance: ${this.formatCredits(economy.balance || 0)}</div>
                <div>Tax Rate: ${(economy.taxRate || 0) * 100}%</div>
                <div>Taxes Collected: ${this.formatCredits(economy.totalTaxesCollected || 0)}</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #88aa88; font-size: 10px;">EXPENSES</div>
                <div>Improvements: ${this.formatCredits(economy.totalImprovementCosts || 0)}</div>
                <div>Surplus Transferred: ${this.formatCredits(economy.totalSurplusTransferred || 0)}</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #88aa88; font-size: 10px;">BONDS</div>
                <div>Outstanding: ${this.formatCredits(economy.totalOutstandingDebt || 0)}</div>
                <div>Bond Count: ${economy.outstandingBondCount || 0}</div>
                <div>Interest Rate: ${(economy.bondInterestRate || 0) * 100}%</div>
            </div>
            <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(100, 200, 100, 0.3);">
                <div style="color: #88aa88; font-size: 10px;">FISCAL CYCLES</div>
                <div>Last Processed: ${economy.lastProcessedCycle || 'N/A'}</div>
            </div>
        `;
        this.infoPanel.style.display = 'block';
    }

    /**
     * Toggle the timeline panel.
     */
    toggleTimeline() {
        if (!this.timelinePanel) return;
        
        if (this.timelinePanel.style.display === 'none') {
            this.timelinePanel.style.display = 'block';
            this.renderTimeline();
        } else {
            this.timelinePanel.style.display = 'none';
        }
    }

    /**
     * Render the fiscal cycle timeline chart.
     */
    renderTimeline() {
        const chart = document.getElementById('economy-timeline-chart');
        if (!chart || this.fiscalCycles.length === 0) return;
        
        const maxCycle = this.fiscalCycles.length;
        const chartWidth = chart.clientWidth || 600;
        const chartHeight = chart.clientHeight || 100;
        
        // Find max values for scaling
        let maxTaxes = 0;
        let maxCosts = 0;
        this.fiscalCycles.forEach(cycle => {
            maxTaxes = Math.max(maxTaxes, cycle.taxesCollected || 0);
            maxCosts = Math.max(maxCosts, cycle.improvementCosts || 0);
        });
        const maxValue = Math.max(maxTaxes, maxCosts, 1);
        
        // Create SVG chart
        let svg = `<svg width="${chartWidth}" height="${chartHeight}" style="background: rgba(0,0,0,0.3); border-radius: 4px;">`;
        
        // Grid lines
        for (let i = 0; i <= 4; i++) {
            const y = (i / 4) * chartHeight;
            svg += `<line x1="0" y1="${y}" x2="${chartWidth}" y2="${y}" stroke="rgba(100,200,100,0.2)" stroke-width="1"/>`;
        }
        
        // Taxes line (green)
        svg += '<polyline fill="none" stroke="#44ff44" stroke-width="2" points="';
        this.fiscalCycles.forEach((cycle, idx) => {
            const x = (idx / (maxCycle - 1)) * chartWidth;
            const y = chartHeight - (cycle.taxesCollected / maxValue) * chartHeight;
            svg += `${x},${y} `;
        });
        svg += '"/>';
        
        // Costs line (orange)
        svg += '<polyline fill="none" stroke="#ffaa44" stroke-width="2" points="';
        this.fiscalCycles.forEach((cycle, idx) => {
            const x = (idx / (maxCycle - 1)) * chartWidth;
            const y = chartHeight - (cycle.improvementCosts / maxValue) * chartHeight;
            svg += `${x},${y} `;
        });
        svg += '"/>';
        
        // Cycle labels
        for (let i = 0; i < maxCycle; i += 2) {
            const x = (i / (maxCycle - 1)) * chartWidth;
            svg += `<text x="${x}" y="${chartHeight - 5}" fill="#88aa88" font-size="10" text-anchor="middle">${i + 1}</text>`;
        }
        
        svg += '</svg>';
        
        // Legend
        svg += `
            <div style="display: flex; gap: 20px; margin-top: 5px; justify-content: center;">
                <span style="color: #44ff44;">● Taxes</span>
                <span style="color: #ffaa44;">● Costs</span>
            </div>
        `;
        
        chart.innerHTML = svg;
    }

    /**
     * Handle mouse move for hover effects.
     * @param {MouseEvent} event
     */
    onMouseMove(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.healthIndicators.forEach(group => {
            group.traverse(child => {
                if (child.isMesh) {
                    allMeshes.push(child);
                }
            });
        });
        
        const intersects = this.raycaster.intersectObjects(allMeshes);
        
        if (intersects.length > 0) {
            const hit = intersects[0].object;
            let parentGroup = hit;
            while (parentGroup.parent && !parentGroup.userData.galaxyId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.galaxyId) {
                if (this.hoveredIndicator !== parentGroup) {
                    if (this.hoveredIndicator) {
                        this.hoveredIndicator.scale.setScalar(1);
                    }
                    this.hoveredIndicator = parentGroup;
                    this.hoveredIndicator.scale.setScalar(1.2);
                }
            }
        } else {
            if (this.hoveredIndicator) {
                this.hoveredIndicator.scale.setScalar(1);
                this.hoveredIndicator = null;
            }
        }
    }

    /**
     * Handle mouse click for selection.
     * @param {MouseEvent} event
     */
    onMouseClick(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.healthIndicators.forEach(group => {
            group.traverse(child => {
                if (child.isMesh) {
                    allMeshes.push(child);
                }
            });
        });
        
        const intersects = this.raycaster.intersectObjects(allMeshes);
        
        if (intersects.length > 0) {
            const hit = intersects[0].object;
            let parentGroup = hit;
            while (parentGroup.parent && !parentGroup.userData.galaxyId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.galaxyId) {
                const galaxyId = parentGroup.userData.galaxyId;
                this.showInfoPanel(galaxyId);
                
                if (this.onGalaxySelect) {
                    this.onGalaxySelect(galaxyId);
                }
            }
        }
    }

    /**
     * Start the animation loop.
     */
    startAnimation() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);
            
            const delta = this.clock.getDelta();
            const time = this.clock.getElapsedTime();
            
            // Animate health indicators
            this.healthIndicators.forEach(group => {
                group.rotation.y += delta * 0.3;
                
                // Pulse effect
                group.traverse(child => {
                    if (child.isMesh && child.material.emissive) {
                        child.material.emissiveIntensity = 0.2 + Math.sin(time * 2) * 0.1;
                    }
                });
            });
            
            // Animate bond flow particles
            this.particles.forEach(particle => {
                if (particle.userData.curve) {
                    particle.userData.progress += delta * particle.userData.speed;
                    if (particle.userData.progress > 1) {
                        particle.userData.progress -= 1;
                    }
                    const point = particle.userData.curve.getPoint(particle.userData.progress);
                    particle.position.copy(point);
                }
            });
        };
        
        animate();
    }

    /**
     * Stop the animation loop.
     */
    stopAnimation() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
    }

    /**
     * Set callbacks for events.
     * @param {Object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        if (callbacks.onGalaxySelect) this.onGalaxySelect = callbacks.onGalaxySelect;
        if (callbacks.onBondClick) this.onBondClick = callbacks.onBondClick;
    }

    /**
     * Destroy the economy visualization and clean up resources.
     */
    destroy() {
        this.stopAnimation();
        this.clearHealthIndicators();
        this.clearBondFlows();
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.galaxyEconomies.clear();
        this.bondFlows.clear();
        this.fiscalCycles = [];
        this.hoveredIndicator = null;
        
        console.log('[EconomyViz] Destroyed');
    }
}

// Expose globally
window.EconomyViz = EconomyViz;
