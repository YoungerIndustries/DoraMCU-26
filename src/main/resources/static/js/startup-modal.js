/**
 * Startup Modal — polls the server for startup progress and displays
 * each step with status icons, a progress bar, and auto-dismisses
 * when all steps are complete.
 */
(function () {
    'use strict';

    const PROGRESS_URL = '/api/v1/startup/progress';
    const POLL_INTERVAL = 1000; // ms
    let pollTimer = null;
    let previousSteps = {};

    function showModal() {
        const overlay = document.createElement('div');
        overlay.className = 'startup-overlay';
        overlay.id = 'startupOverlay';

        overlay.innerHTML = `
            <div class="startup-modal">
                <h2>DoraMCU Starting Up</h2>
                <div class="startup-subtitle">Initializing server and game systems...</div>
                <div class="startup-progress-bar">
                    <div class="startup-progress-bar-fill" id="startupProgressBarFill"></div>
                </div>
                <ul class="startup-steps" id="startupSteps"></ul>
                <div class="startup-footer" id="startupFooter">Please wait...</div>
            </div>
        `;

        document.body.appendChild(overlay);
        startPolling();
    }

    function hideModal() {
        const overlay = document.getElementById('startupOverlay');
        if (overlay) {
            overlay.remove();
        }
        stopPolling();
    }

    function startPolling() {
        pollTimer = setInterval(fetchProgress, POLL_INTERVAL);
        fetchProgress(); // immediate first fetch
    }

    function stopPolling() {
        if (pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    async function fetchProgress() {
        try {
            const response = await fetch(PROGRESS_URL);
            if (!response.ok) return;

            const steps = await response.json();
            renderSteps(steps);

            // Check if all steps are done
            const allDone = steps.every(s => s.status === 'COMPLETED' || s.status === 'SKIPPED');
            const hasErrors = steps.some(s => s.status === 'ERROR');

            if (allDone) {
                const footer = document.getElementById('startupFooter');
                if (footer) {
                    footer.textContent = hasErrors
                        ? 'Startup completed with errors. Check logs for details.'
                        : 'All systems ready!';
                }
                stopPolling();
                // Auto-dismiss after 2 seconds
                setTimeout(hideModal, 2000);
            }
        } catch (e) {
            // Server may not be ready yet; keep polling
        }
    }

    function renderSteps(steps) {
        const container = document.getElementById('startupSteps');
        const progressBar = document.getElementById('startupProgressBarFill');
        if (!container) return;

        // Build step map for diffing
        const stepMap = {};
        steps.forEach(s => stepMap[s.key] = s);

        // Render each step
        let html = '';
        steps.forEach(step => {
            const prev = previousSteps[step.key];
            const iconClass = step.status.toLowerCase().replace('_', '-');
            const iconContent = getStepIcon(step.status);

            html += `
                <li class="startup-step" data-key="${step.key}">
                    <div class="startup-step-icon ${iconClass}">${iconContent}</div>
                    <div class="startup-step-content">
                        <div class="startup-step-label">${step.label}</div>
                        ${step.message ? `<div class="startup-step-message">${step.message}</div>` : ''}
                    </div>
                </li>
            `;
        });

        container.innerHTML = html;

        // Update progress bar
        if (progressBar) {
            const completed = steps.filter(s => s.status === 'COMPLETED' || s.status === 'SKIPPED').length;
            const total = steps.length;
            const percent = Math.round((completed / total) * 100);
            progressBar.style.width = percent + '%';
        }

        // Scroll to the last in-progress step
        const activeStep = container.querySelector('.in-progress');
        if (activeStep) {
            activeStep.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }

        previousSteps = stepMap;
    }

    function getStepIcon(status) {
        switch (status) {
            case 'COMPLETED': return '✓';
            case 'SKIPPED': return '—';
            case 'ERROR': return '✕';
            case 'IN_PROGRESS': return '';
            default: return '·';
        }
    }

    // Expose globally for manual triggering
    window.startupModal = {
        show: showModal,
        hide: hideModal
    };

    // Auto-show on page load if server is starting
    document.addEventListener('DOMContentLoaded', () => {
        // Check if server is in startup mode
        fetch(PROGRESS_URL)
            .then(res => res.ok ? res.json() : null)
            .then(steps => {
                if (steps && steps.length > 0) {
                    // Check if any step is not yet complete
                    const incomplete = steps.some(s => s.status !== 'COMPLETED' && s.status !== 'SKIPPED');
                    if (incomplete) {
                        showModal();
                    }
                }
            })
            .catch(() => {
                // Server not ready yet — show modal anyway
                showModal();
            });
    });
})();
