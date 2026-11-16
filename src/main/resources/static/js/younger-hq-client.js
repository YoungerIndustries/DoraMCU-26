/**
 * Younger Industries HQ Portal Hub Client
 * Loads and displays all game module portals, handles navigation and portal entry
 */

(function() {
    'use strict';

    // ============================================================
    // State
    // ============================================================
    let allPortals = [];
    let currentFloor = 'all';
    let selectedPortal = null;
    let characterLevel = 1;

    // ============================================================
    // DOM Elements
    // ============================================================
    const portalsGrid = document.getElementById('portalsGrid');
    const floorMap = document.getElementById('floorMap');
    const activePortalCount = document.getElementById('activePortalCount');
    const characterNameEl = document.getElementById('characterName');
    const characterLevelEl = document.getElementById('characterLevel');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const loadingText = document.getElementById('loadingText');
    const toastContainer = document.getElementById('toastContainer');
    const portalEntryModal = document.getElementById('portalEntryModal');
    const hqParticles = document.getElementById('hqParticles');

    // ============================================================
    // Initialization
    // ============================================================
    document.addEventListener('DOMContentLoaded', () => {
        showLoading('Initializing portal systems...');
        createParticles();
        loadPortals();
        setupNavigation();
        setupModal();
        setupButtons();
    });

    // ============================================================
    // Particle Effects
    // ============================================================
    function createParticles() {
        for (let i = 0; i < 30; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.animationDuration = (Math.random() * 10 + 8) + 's';
            particle.style.animationDelay = (Math.random() * 10) + 's';
            hqParticles.appendChild(particle);
        }
    }

    // ============================================================
    // API Calls
    // ============================================================
    async function loadPortals() {
        try {
            loadingText.textContent = 'Loading portal data from HQ database...';
            const response = await fetch('/api/v1/earth2026/hq-portals?hqId=1&activeOnly=false');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            allPortals = await response.json();
            updateActivePortalCount();
            renderPortals();
            renderFloorMap();
            hideLoading();
            showToast(`Loaded ${allPortals.length} portals`, 'success');
        } catch (error) {
            console.error('Failed to load portals:', error);
            hideLoading();
            portalsGrid.innerHTML = `
                <div class="portals-empty">
                    <p>⚠️ Failed to load portal data</p>
                    <p style="font-size:0.85rem; margin-top:10px; color:var(--hq-text-dim);">${error.message}</p>
                    <button class="btn-primary" style="margin-top:20px;" onclick="location.reload()">
                        <span class="btn-icon">🔄</span> Retry
                    </button>
                </div>
            `;
            showToast('Failed to load portals', 'error');
        }
    }

    // ============================================================
    // Render Portals
    // ============================================================
    function renderPortals() {
        const filtered = currentFloor === 'all'
            ? allPortals
            : allPortals.filter(p => p.basementLevel === parseInt(currentFloor));

        if (filtered.length === 0) {
            portalsGrid.innerHTML = '<div class="portals-empty">No portals on this level</div>';
            return;
        }

        portalsGrid.innerHTML = filtered.map(portal => createPortalCard(portal)).join('');

        // Attach click handlers
        portalsGrid.querySelectorAll('.portal-card').forEach(card => {
            card.addEventListener('click', () => {
                const portalId = parseInt(card.dataset.portalId);
                const portal = allPortals.find(p => p.id === portalId);
                if (portal) openPortalModal(portal);
            });
        });
    }

    function createPortalCard(portal) {
        const isActive = portal.active;
        const isLocked = portal.requiresClearance && !hasClearance(portal.requiresClearance);
        const levelMet = characterLevel >= portal.requiresLevel;
        const cardClasses = ['portal-card'];
        if (!isActive) cardClasses.push('inactive');
        if (isLocked || !levelMet) cardClasses.push('locked');

        const statusClass = isActive ? 'active' : 'inactive';
        const statusText = isActive ? 'Online' : 'Offline';
        const portalStyle = portal.portalStyle || '';
        const styleAttr = portalStyle ? ` data-style="${portalStyle}"` : '';

        return `
            <div class="${cardClasses.join(' ')}" data-portal-id="${portal.id}"${styleAttr}>
                <div class="portal-header">
                    <div class="portal-icon">${portal.portalIcon || '🌀'}</div>
                    <div class="portal-title">
                        <div class="portal-name">${escapeHtml(portal.portalName)}</div>
                        <div class="portal-game">${escapeHtml(portal.gameType)}</div>
                    </div>
                    <span class="portal-status ${statusClass}">${statusText}</span>
                </div>
                <div class="portal-description">${escapeHtml(portal.description || '')}</div>
                <div class="portal-meta">
                    <span class="portal-basement">Level ${portal.basementLevel >= 0 ? portal.basementLevel : 'B' + Math.abs(portal.basementLevel)}</span>
                    <span class="portal-level">Req: <span>Level ${portal.requiresLevel}</span></span>
                    ${portal.requiresClearance ? `<span class="portal-clearance">${escapeHtml(portal.requiresClearance)}</span>` : ''}
                </div>
            </div>
        `;
    }

    // ============================================================
    // Render Floor Map
    // ============================================================
    function renderFloorMap() {
        const floors = [
            { level: 0, name: 'Ground Floor — Reception' },
            { level: -1, name: 'Basement B1 — Classic RPG Portals' },
            { level: -2, name: 'Basement B2 — Sci-Fi Strategy Portals' },
            { level: -3, name: 'Basement B3 — Combat & Tactical Portals' }
        ];

        floorMap.innerHTML = floors.map(floor => {
            const floorPortals = allPortals.filter(p => p.basementLevel === floor.level);
            if (floorPortals.length === 0) return '';

            return `
                <div class="floor-panel">
                    <div class="floor-title">${floor.name}</div>
                    <div class="floor-portals">
                        ${floorPortals.map(p => `
                            <div class="floor-portal-item" data-portal-id="${p.id}">
                                <span class="floor-portal-icon">${p.portalIcon || '🌀'}</span>
                                <span class="floor-portal-name">${escapeHtml(p.portalName)}</span>
                                <span class="floor-portal-game">${escapeHtml(p.gameType)}</span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }).filter(Boolean).join('');

        // Attach click handlers
        floorMap.querySelectorAll('.floor-portal-item').forEach(item => {
            item.addEventListener('click', () => {
                const portalId = parseInt(item.dataset.portalId);
                const portal = allPortals.find(p => p.id === portalId);
                if (portal) openPortalModal(portal);
            });
        });
    }

    // ============================================================
    // Navigation
    // ============================================================
    function setupNavigation() {
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentFloor = btn.dataset.floor;
                renderPortals();
            });
        });
    }

    function setupButtons() {
        document.getElementById('btnBackToEarth').addEventListener('click', () => {
            window.location.href = 'earth2026.html';
        });

        document.getElementById('btnBackToOffice').addEventListener('click', () => {
            window.location.href = 'game.html';
        });
    }

    // ============================================================
    // Modal
    // ============================================================
    function setupModal() {
        document.getElementById('closePortalModal').addEventListener('click', closePortalModal);
        document.getElementById('cancelPortalEntry').addEventListener('click', closePortalModal);
        document.getElementById('confirmPortalEntry').addEventListener('click', enterPortal);

        portalEntryModal.addEventListener('click', (e) => {
            if (e.target === portalEntryModal) closePortalModal();
        });
    }

    function openPortalModal(portal) {
        selectedPortal = portal;

        document.getElementById('modalPortalIcon').textContent = portal.portalIcon || '🌀';
        document.getElementById('modalPortalName').textContent = portal.portalName;
        document.getElementById('modalDestination').textContent = portal.gameType;
        document.getElementById('modalBasement').textContent = portal.basementLevel >= 0
            ? `Ground Floor (${portal.basementLevel})`
            : `Basement B${Math.abs(portal.basementLevel)}`;
        document.getElementById('modalRequiredLevel').textContent = `Level ${portal.requiresLevel}`;
        document.getElementById('modalClearance').textContent = portal.requiresClearance || 'None';
        document.getElementById('modalStatus').textContent = portal.active ? '✅ Online' : '❌ Offline';
        document.getElementById('modalStatus').style.color = portal.active ? 'var(--hq-accent)' : 'var(--hq-danger)';
        document.getElementById('modalDescription').textContent = portal.description || 'No description available.';

        const enterBtn = document.getElementById('confirmPortalEntry');
        const canEnter = portal.active && characterLevel >= portal.requiresLevel;
        enterBtn.disabled = !canEnter;
        enterBtn.style.opacity = canEnter ? '1' : '0.5';
        enterBtn.title = canEnter ? 'Enter this portal' : 'Requirements not met';

        portalEntryModal.classList.remove('hidden');
    }

    function closePortalModal() {
        portalEntryModal.classList.add('hidden');
        selectedPortal = null;
    }

    function enterPortal() {
        if (!selectedPortal) return;

        if (!selectedPortal.active) {
            showToast('This portal is currently offline', 'error');
            return;
        }

        if (characterLevel < selectedPortal.requiresLevel) {
            showToast(`Requires level ${selectedPortal.requiresLevel}`, 'error');
            return;
        }

        closePortalModal();
        showLoading(`Entering ${selectedPortal.portalName}...`);

        // Call the backend travel endpoint to join the galaxy session
        const playerName = localStorage.getItem('playerName') || 'Guest';
        const playerId = localStorage.getItem('playerId') || null;

        fetch(`/api/v1/earth2026/hq-portals/${selectedPortal.id}/travel`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: playerId,
                playerName: playerName
            })
        })
        .then(response => response.json())
        .then(result => {
            if (result.success && result.gameUrl) {
                showToast(`Transitioning to ${selectedPortal.portalName}...`, 'success');
                setTimeout(() => {
                    window.location.href = result.gameUrl;
                }, 500);
            } else {
                hideLoading();
                showToast(result.message || 'Portal travel failed', 'error');
            }
        })
        .catch(error => {
            console.error('Portal travel failed:', error);
            hideLoading();
            showToast('Failed to connect to portal system', 'error');
        });
    }

    // ============================================================
    // Helpers
    // ============================================================
    function updateActivePortalCount() {
        const active = allPortals.filter(p => p.active).length;
        activePortalCount.textContent = `${active} / ${allPortals.length}`;
    }

    function hasClearance(clearance) {
        // Placeholder: in a real app, check user's clearance level
        return false;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function showLoading(text) {
        loadingText.textContent = text;
        loadingOverlay.classList.remove('hidden');
    }

    function hideLoading() {
        loadingOverlay.classList.add('hidden');
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
})();
