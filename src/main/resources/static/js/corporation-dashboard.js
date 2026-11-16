/**
 * Corporation Dashboard UI Module
 * Handles the Earth Corporation Office dashboard: tab navigation,
 * data loading from tycoon API, and rendering of all sections.
 */
const CorporationDashboard = (function() {
    'use strict';

    // API base URL
    const API_BASE = '/api/v1/tycoon';
    
    // Corporation ID (default to 1 if not set)
    let corporationId = parseInt(localStorage.getItem('corporationId') || '1', 10);
    
    // DOM elements
    let elements = {};
    
    // Current dashboard data
    let dashboardData = null;

    // Building type icons
    const buildingIcons = {
        'HEADQUARTERS': '🏢',
        'RESEARCH_LAB': '🔬',
        'MANUFACTURING_PLANT': '🏭',
        'TRADING_POST': '🏪',
        'SECURITY_OFFICE': '🔒',
        'DATA_CENTER': '💻',
        'RECRUITING_OFFICE': '👥',
        'BANK_BRANCH': '🏦',
        'MEDIA_OFFICE': '📺',
        'PORT_FACILITY': '⚓'
    };

    // Employee role icons
    const roleIcons = {
        'ANALYST': '📊',
        'ENGINEER': '⚙️',
        'MARKETER': '📢',
        'SECURITY': '🔒',
        'RESEARCHER': '🔬',
        'MANAGER': '👔'
    };

    function init() {
        cacheElements();
        bindNavigationEvents();
        bindActionEvents();
        bindModalEvents();
        loadDashboard();
    }

    function cacheElements() {
        elements = {
            // Navigation
            navBtns: document.querySelectorAll('.nav-btn'),
            
            // Tabs
            tabs: document.querySelectorAll('.corp-tab'),
            
            // Header resources
            corporationName: document.getElementById('corporationName'),
            corporationLevel: document.getElementById('corporationLevel'),
            resourceCredits: document.getElementById('resourceCredits'),
            resourceInfluence: document.getElementById('resourceInfluence'),
            resourceTech: document.getElementById('resourceTech'),
            resourceMaterials: document.getElementById('resourceMaterials'),
            
            // Dashboard stats
            statReputation: document.getElementById('statReputation'),
            statLevel: document.getElementById('statLevel'),
            statExperience: document.getElementById('statExperience'),
            statMissions: document.getElementById('statMissions'),
            experienceFill: document.getElementById('experienceFill'),
            experienceText: document.getElementById('experienceText'),
            
            // Resource generation
            genCredits: document.getElementById('genCredits'),
            genBuildings: document.getElementById('genBuildings'),
            genEmployees: document.getElementById('genEmployees'),
            genResearch: document.getElementById('genResearch'),
            
            // Grids
            buildingsGrid: document.getElementById('buildingsGrid'),
            availableBuildingsGrid: document.getElementById('availableBuildingsGrid'),
            employeesGrid: document.getElementById('employeesGrid'),
            researchGrid: document.getElementById('researchGrid'),
            availableResearchGrid: document.getElementById('availableResearchGrid'),
            
            // Buttons
            btnRefresh: document.getElementById('btnRefresh'),
            btnTick: document.getElementById('btnTick'),
            btnBuild: document.getElementById('btnBuild'),
            btnHire: document.getElementById('btnHire'),
            btnMission: document.getElementById('btnMission'),
            btnLogout: document.getElementById('btnLogout'),
            btnEarth2026: document.getElementById('btnEarth2026'),
            btnYoungerHQ: document.getElementById('btnYoungerHQ'),
            btnHireEmployee: document.getElementById('btnHireEmployee'),
            btnCompleteMission: document.getElementById('btnCompleteMission'),
            
            // Modal
            hireModal: document.getElementById('hireModal'),
            closeHireModal: document.getElementById('closeHireModal'),
            cancelHire: document.getElementById('cancelHire'),
            confirmHire: document.getElementById('confirmHire'),
            employeeName: document.getElementById('employeeName'),
            employeeRole: document.getElementById('employeeRole'),
            employeeSalary: document.getElementById('employeeSalary'),
            
            // Mission form
            missionDifficulty: document.getElementById('missionDifficulty'),
            missionKills: document.getElementById('missionKills'),
            missionScore: document.getElementById('missionScore'),
            
            // Loading overlay
            loadingOverlay: document.getElementById('loadingOverlay'),
            
            // Toast container
            toastContainer: document.getElementById('toastContainer')
        };
    }

    // ============================================================
    // Navigation
    // ============================================================

    function bindNavigationEvents() {
        elements.navBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const tab = this.dataset.tab;
                switchTab(tab);
            });
        });
    }

    function switchTab(tabName) {
        // Update nav buttons
        elements.navBtns.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabName);
        });
        
        // Update tabs
        elements.tabs.forEach(tab => {
            tab.classList.toggle('active', tab.id === 'tab-' + tabName);
        });
    }

    // ============================================================
    // Action Events
    // ============================================================

    function bindActionEvents() {
        elements.btnRefresh.addEventListener('click', loadDashboard);
        elements.btnTick.addEventListener('click', processTick);
        elements.btnBuild.addEventListener('click', () => switchTab('buildings'));
        elements.btnHire.addEventListener('click', openHireModal);
        elements.btnMission.addEventListener('click', () => switchTab('missions'));
        elements.btnLogout.addEventListener('click', logout);
        elements.btnEarth2026.addEventListener('click', goToEarth2026);
        elements.btnYoungerHQ.addEventListener('click', goToYoungerHQ);
        elements.btnHireEmployee.addEventListener('click', openHireModal);
        elements.btnCompleteMission.addEventListener('click', completeMission);
    }

    // ============================================================
    // Modal Events
    // ============================================================

    function bindModalEvents() {
        elements.closeHireModal.addEventListener('click', closeHireModal);
        elements.cancelHire.addEventListener('click', closeHireModal);
        elements.confirmHire.addEventListener('click', hireEmployee);
    }

    function openHireModal() {
        elements.hireModal.classList.remove('hidden');
    }

    function closeHireModal() {
        elements.hireModal.classList.add('hidden');
        elements.employeeName.value = '';
        elements.employeeSalary.value = '1000';
    }

    // ============================================================
    // API Calls
    // ============================================================

    async function loadDashboard() {
        showLoading(true);
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}`);
            if (!response.ok) throw new Error('Failed to load dashboard');
            
            dashboardData = await response.json();
            renderDashboard();
            
            // Load available buildings and research
            loadAvailableBuildings();
            loadAvailableResearch();
        } catch (error) {
            console.error('Error loading dashboard:', error);
            showToast('Failed to load corporation data', 'error');
        } finally {
            showLoading(false);
        }
    }

    async function loadAvailableBuildings() {
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/buildings-available`);
            if (!response.ok) return;
            
            const available = await response.json();
            renderAvailableBuildings(available);
        } catch (error) {
            console.error('Error loading available buildings:', error);
        }
    }

    async function loadAvailableResearch() {
        try {
            const response = await fetch(`${API_BASE}/research-available`);
            if (!response.ok) return;
            
            const available = await response.json();
            renderAvailableResearch(available);
        } catch (error) {
            console.error('Error loading available research:', error);
        }
    }

    async function processTick() {
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/tick`, {
                method: 'POST'
            });
            if (!response.ok) throw new Error('Tick failed');
            
            dashboardData = await response.json();
            renderDashboard();
            showToast('Game tick processed successfully', 'success');
        } catch (error) {
            console.error('Error processing tick:', error);
            showToast('Failed to process tick', 'error');
        }
    }

    async function constructBuilding(type) {
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/buildings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ type })
            });
            
            const result = await response.json();
            if (result.success) {
                showToast(`Building ${type} constructed!`, 'success');
                loadDashboard();
            } else {
                showToast(result.message || 'Failed to construct building', 'error');
            }
        } catch (error) {
            console.error('Error constructing building:', error);
            showToast('Failed to construct building', 'error');
        }
    }

    async function upgradeBuilding(type) {
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/buildings/${type}`, {
                method: 'PUT'
            });
            
            const result = await response.json();
            if (result.success) {
                showToast(`Building ${type} upgraded!`, 'success');
                loadDashboard();
            } else {
                showToast(result.message || 'Failed to upgrade building', 'error');
            }
        } catch (error) {
            console.error('Error upgrading building:', error);
            showToast('Failed to upgrade building', 'error');
        }
    }

    async function hireEmployee() {
        const name = elements.employeeName.value.trim();
        const role = elements.employeeRole.value;
        const salary = parseInt(elements.employeeSalary.value, 10);
        
        if (!name) {
            showToast('Please enter employee name', 'error');
            return;
        }
        
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/employees`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, role, salary })
            });
            
            const result = await response.json();
            if (result.success) {
                showToast(`Employee ${name} hired!`, 'success');
                closeHireModal();
                loadDashboard();
            } else {
                showToast(result.message || 'Failed to hire employee', 'error');
            }
        } catch (error) {
            console.error('Error hiring employee:', error);
            showToast('Failed to hire employee', 'error');
        }
    }

    async function completeMission() {
        const difficulty = parseInt(elements.missionDifficulty.value, 10);
        const kills = parseInt(elements.missionKills.value, 10) || 0;
        const score = parseInt(elements.missionScore.value, 10) || 0;
        
        try {
            const response = await fetch(`${API_BASE}/corporations/${corporationId}/missions`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ difficulty, kills, score })
            });
            
            const result = await response.json();
            if (result.success) {
                showToast(`Mission completed! Earned ${result.credits || 0} credits`, 'success');
                loadDashboard();
            } else {
                showToast('Mission failed', 'error');
            }
        } catch (error) {
            console.error('Error completing mission:', error);
            showToast('Failed to complete mission', 'error');
        }
    }

    // ============================================================
    // Rendering
    // ============================================================

    function renderDashboard() {
        if (!dashboardData) return;
        
        // Header resources
        if (elements.corporationName) {
            elements.corporationName.textContent = dashboardData.corporationName || 'Corporation';
        }
        if (elements.corporationLevel) {
            elements.corporationLevel.textContent = `Level ${dashboardData.level || 1}`;
        }
        if (elements.resourceCredits) {
            elements.resourceCredits.textContent = formatNumber(dashboardData.credits || 0);
        }
        if (elements.resourceInfluence) {
            elements.resourceInfluence.textContent = formatNumber(dashboardData.influence || 0);
        }
        if (elements.resourceTech) {
            elements.resourceTech.textContent = formatNumber(dashboardData.tech || 0);
        }
        if (elements.resourceMaterials) {
            elements.resourceMaterials.textContent = formatNumber(dashboardData.materials || 0);
        }
        
        // Dashboard stats
        if (elements.statReputation) {
            elements.statReputation.textContent = dashboardData.reputation || 0;
        }
        if (elements.statLevel) {
            elements.statLevel.textContent = dashboardData.level || 1;
        }
        if (elements.statExperience) {
            elements.statExperience.textContent = `${formatNumber(dashboardData.experience || 0)} / ${formatNumber(dashboardData.experienceToNextLevel || 1000)}`;
        }
        if (elements.statMissions) {
            elements.statMissions.textContent = dashboardData.missionsCompleted || 0;
        }
        
        // Experience bar
        if (elements.experienceFill) {
            const xp = dashboardData.experience || 0;
            const xpNeeded = dashboardData.experienceToNextLevel || 1000;
            const percentage = Math.min((xp / xpNeeded) * 100, 100);
            elements.experienceFill.style.width = percentage + '%';
        }
        if (elements.experienceText) {
            const xp = dashboardData.experience || 0;
            const xpNeeded = dashboardData.experienceToNextLevel || 1000;
            elements.experienceText.textContent = `${formatNumber(xp)} / ${formatNumber(xpNeeded)} XP to next level`;
        }
        
        // Resource generation
        if (elements.genCredits) {
            elements.genCredits.textContent = formatNumber(dashboardData.creditsPerHour || 0);
        }
        if (elements.genBuildings) {
            elements.genBuildings.textContent = (dashboardData.buildings || []).length;
        }
        if (elements.genEmployees) {
            elements.genEmployees.textContent = (dashboardData.employees || []).length;
        }
        if (elements.genResearch) {
            const research = dashboardData.researchProjects || [];
            elements.genResearch.textContent = research.filter(r => !r.completed).length;
        }
        
        // Render buildings
        renderBuildings(dashboardData.buildings || []);
        
        // Render employees
        renderEmployees(dashboardData.employees || []);
        
        // Render research
        renderResearch(dashboardData.researchProjects || []);
    }

    function renderBuildings(buildings) {
        if (!elements.buildingsGrid) return;
        
        if (buildings.length === 0) {
            elements.buildingsGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">🏗️</div>
                    <div class="empty-text">No buildings yet</div>
                    <div class="empty-hint">Construct your first building to get started</div>
                </div>
            `;
            return;
        }
        
        elements.buildingsGrid.innerHTML = buildings.map(building => {
            const icon = buildingIcons[building.type] || '🏢';
            return `
                <div class="building-card">
                    <div class="building-header">
                        <span class="building-icon">${icon}</span>
                        <div>
                            <div class="building-name">${building.displayName || building.type}</div>
                            <div class="building-level">Level ${building.level || 1}</div>
                        </div>
                    </div>
                    <div class="building-stats">
                        <div class="building-stat">
                            <span class="building-stat-label">Credits/Hour</span>
                            <span class="building-stat-value">${formatNumber(building.creditsPerHour || 0)}</span>
                        </div>
                        <div class="building-stat">
                            <span class="building-stat-label">Status</span>
                            <span class="building-stat-value">${building.constructed ? '✅ Active' : '🚧 Building'}</span>
                        </div>
                    </div>
                    <div class="building-actions">
                        <button class="btn-primary" onclick="CorporationDashboard.upgradeBuilding('${building.type}')">
                            <span class="btn-icon">⬆️</span> Upgrade
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderAvailableBuildings(available) {
        if (!elements.availableBuildingsGrid) return;
        
        if (available.length === 0) {
            elements.availableBuildingsGrid.innerHTML = '';
            return;
        }
        
        elements.availableBuildingsGrid.innerHTML = available.map(building => {
            const icon = buildingIcons[building.type] || '🏢';
            return `
                <div class="building-card">
                    <div class="building-header">
                        <span class="building-icon">${icon}</span>
                        <div>
                            <div class="building-name">${building.displayName || building.type}</div>
                            <div class="building-level">Cost: ${formatNumber(building.cost || 0)} credits</div>
                        </div>
                    </div>
                    <div class="building-actions">
                        <button class="btn-primary" onclick="CorporationDashboard.constructBuilding('${building.type}')">
                            <span class="btn-icon">🏗️</span> Construct
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderEmployees(employees) {
        if (!elements.employeesGrid) return;
        
        if (employees.length === 0) {
            elements.employeesGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">👥</div>
                    <div class="empty-text">No employees yet</div>
                    <div class="empty-hint">Hire your first employee to build your team</div>
                </div>
            `;
            return;
        }
        
        elements.employeesGrid.innerHTML = employees.map(employee => {
            const icon = roleIcons[employee.role] || '👤';
            return `
                <div class="employee-card">
                    <div class="employee-avatar">${icon}</div>
                    <div class="employee-info">
                        <div class="employee-name">${escapeHtml(employee.name || 'Unknown')}</div>
                        <div class="employee-role">${employee.role || 'Employee'}</div>
                        <div class="employee-salary">💰 ${formatNumber(employee.salary || 0)} credits/month</div>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderResearch(projects) {
        if (!elements.researchGrid) return;
        
        const activeProjects = projects.filter(p => !p.completed);
        
        if (activeProjects.length === 0) {
            elements.researchGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">🔬</div>
                    <div class="empty-text">No active research</div>
                    <div class="empty-hint">Start a new research project</div>
                </div>
            `;
            return;
        }
        
        elements.researchGrid.innerHTML = activeProjects.map(project => {
            const progress = project.progress || 0;
            return `
                <div class="research-card">
                    <div class="research-name">${escapeHtml(project.name || 'Research Project')}</div>
                    <div class="research-progress">
                        <div class="research-progress-fill" style="width: ${progress}%"></div>
                    </div>
                    <div class="research-status">${progress}% complete</div>
                </div>
            `;
        }).join('');
    }

    function renderAvailableResearch(available) {
        if (!elements.availableResearchGrid) return;
        
        if (available.length === 0) {
            elements.availableResearchGrid.innerHTML = '';
            return;
        }
        
        elements.availableResearchGrid.innerHTML = available.map(research => {
            return `
                <div class="building-card">
                    <div class="building-header">
                        <span class="building-icon">🔬</span>
                        <div>
                            <div class="building-name">${research.displayName || research.type}</div>
                            <div class="building-level">Cost: ${formatNumber(research.baseCost || 0)} credits</div>
                        </div>
                    </div>
                    <p style="font-size: 0.85rem; color: var(--corp-text-secondary); margin-bottom: 12px;">
                        ${escapeHtml(research.description || '')}
                    </p>
                </div>
            `;
        }).join('');
    }

    // ============================================================
    // Utilities
    // ============================================================

    function formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text || '';
        return div.innerHTML;
    }

    function showLoading(show) {
        if (elements.loadingOverlay) {
            elements.loadingOverlay.classList.toggle('hidden', !show);
        }
    }

    function showToast(message, type = 'success') {
        if (!elements.toastContainer) return;
        
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        elements.toastContainer.appendChild(toast);
        
        // Auto-remove after 3 seconds
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    function logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        window.location.href = 'index.html';
    }

    function goToEarth2026() {
        // Navigate to Earth 2026 landing page
        window.location.href = 'earth2026.html';
    }

    function goToYoungerHQ() {
        // Navigate to Younger Industries HQ Portal Hub
        window.location.href = 'younger-hq.html';
    }

    // ============================================================
    // Public API
    // ============================================================

    return {
        init,
        switchTab,
        loadDashboard,
        constructBuilding,
        upgradeBuilding,
        openHireModal
    };
})();

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', CorporationDashboard.init);
} else {
    CorporationDashboard.init();
}
