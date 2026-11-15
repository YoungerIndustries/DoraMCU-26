/**
 * DoraMCU Desktop Integration
 * 
 * This script enhances the web UI with desktop-specific features when running
 * inside the Electron desktop app. It adds:
 * - A desktop status bar showing server connection status
 * - A game client launch button
 * - Desktop notifications for game events
 * 
 * When running in a regular browser, this script is a no-op.
 */

(function() {
    'use strict';

    // Wait for the Electron bridge to be ready
    function initWhenReady() {
        // Check if Electron bridge is available
        if (!window.DoraElectron || !window.DoraElectron.isElectron) {
            console.log('[DoraMCU Desktop] Not running in Electron - skipping desktop integration');
            return;
        }

        console.log('[DoraMCU Desktop] Initializing desktop integration...');

        // Get app info
        window.DoraElectron.appInfo.get().then(function(info) {
            console.log('[DoraMCU Desktop] App info:', info);
            createDesktopStatusBar(info);
            setupGameClientIntegration();
            setupDesktopNotifications();
        }).catch(function(err) {
            console.error('[DoraMCU Desktop] Failed to get app info:', err);
        });
    }

    // Create a desktop status bar at the top of the page
    function createDesktopStatusBar(appInfo) {
        // Remove existing status bar if any
        var existing = document.getElementById('dora-desktop-statusbar');
        if (existing) existing.remove();

        var statusBar = document.createElement('div');
        statusBar.id = 'dora-desktop-statusbar';
        statusBar.style.cssText = 'position:fixed;top:0;left:0;right:0;height:28px;' +
            'background:linear-gradient(90deg,#1a1a2e 0%,#16213e 100%);' +
            'border-bottom:1px solid #0f3460;' +
            'display:flex;align-items:center;justify-content:space-between;' +
            'padding:0 16px;z-index:10000;font-family:Segoe UI,sans-serif;' +
            'font-size:12px;color:#a0a0c0;';

        // Left side: app name and version
        var leftSection = document.createElement('div');
        leftSection.style.cssText = 'display:flex;align-items:center;gap:12px;';

        var appName = document.createElement('span');
        appName.style.cssText = 'font-weight:600;color:#e0e0ff;';
        appName.textContent = 'DoraMCU Desktop v' + (appInfo.version || '1.0.0');
        leftSection.appendChild(appName);

        // Server status indicator
        var serverStatus = document.createElement('span');
        serverStatus.id = 'dora-server-status';
        serverStatus.style.cssText = 'display:flex;align-items:center;gap:4px;';
        var statusDot = document.createElement('span');
        statusDot.id = 'dora-server-dot';
        statusDot.style.cssText = 'width:8px;height:8px;border-radius:50%;background:#00ff88;';
        serverStatus.appendChild(statusDot);
        var statusText = document.createElement('span');
        statusText.id = 'dora-server-text';
        statusText.textContent = appInfo.serverOnline ? 'Server Online' : 'Server Offline';
        serverStatus.appendChild(statusText);
        leftSection.appendChild(serverStatus);

        statusBar.appendChild(leftSection);

        // Right side: game client button and actions
        var rightSection = document.createElement('div');
        rightSection.style.cssText = 'display:flex;align-items:center;gap:12px;';

        // Game client launch button
        var gameBtn = document.createElement('button');
        gameBtn.id = 'dora-game-launch-btn';
        gameBtn.style.cssText = 'background:#0f3460;color:#e0e0ff;border:1px solid #533483;' +
            'padding:4px 12px;border-radius:4px;cursor:pointer;font-size:11px;' +
            'font-family:inherit;transition:background 0.2s;';
        gameBtn.textContent = '🚀 Launch Game Client';
        gameBtn.onmouseenter = function() { this.style.background = '#533483'; };
        gameBtn.onmouseleave = function() { this.style.background = '#0f3460'; };
        rightSection.appendChild(gameBtn);

        // Reload button
        var reloadBtn = document.createElement('button');
        reloadBtn.id = 'dora-reload-btn';
        reloadBtn.style.cssText = 'background:transparent;color:#a0a0c0;border:1px solid #333;' +
            'padding:4px 8px;border-radius:4px;cursor:pointer;font-size:11px;' +
            'font-family:inherit;';
        reloadBtn.textContent = '↻';
        reloadBtn.title = 'Reload Page';
        reloadBtn.onclick = function() {
            window.DoraElectron.appInfo.reload();
        };
        rightSection.appendChild(reloadBtn);

        statusBar.appendChild(rightSection);
        document.body.insertBefore(statusBar, document.body.firstChild);

        // Add padding to body to prevent content from being hidden behind status bar
        document.body.style.paddingTop = '28px';

        // Set up game client button handler
        gameBtn.onclick = function() {
            launchGameClient(gameBtn);
        };

        // Periodically check server health
        setInterval(checkServerStatus, 30000);
    }

    // Check and update server status
    function checkServerStatus() {
        if (!window.DoraElectron) return;

        window.DoraElectron.appInfo.isServerOnline().then(function(result) {
            var dot = document.getElementById('dora-server-dot');
            var text = document.getElementById('dora-server-text');
            if (dot && text) {
                if (result.online) {
                    dot.style.background = '#00ff88';
                    text.textContent = 'Server Online';
                } else {
                    dot.style.background = '#ff4444';
                    text.textContent = 'Server Offline';
                }
            }
        }).catch(function() {
            var dot = document.getElementById('dora-server-dot');
            var text = document.getElementById('dora-server-text');
            if (dot) dot.style.background = '#ff8800';
            if (text) text.textContent = 'Server Unreachable';
        });
    }

    // Launch the game client with status updates
    async function launchGameClient(button) {
        if (!window.DoraElectron) return;

        var originalText = button.textContent;
        button.disabled = true;
        button.textContent = '⏳ Launching...';
        button.style.opacity = '0.7';

        try {
            var result = await window.DoraElectron.gameClient.launch();
            if (result.success) {
                button.textContent = '⏳ Starting...';
                
                // Listen for status updates
                var unsubscribe = window.DoraElectron.gameClient.onStatusChange(function(data) {
                    if (data.status === 'running' || data.status === 'started') {
                        button.textContent = '⏹ Stop Game Client';
                        button.disabled = false;
                        button.style.opacity = '1';
                        button.style.background = '#8b0000';
                        button.onclick = function() {
                            stopGameClient(button);
                        };
                        unsubscribe();
                    } else if (data.status === 'error') {
                        button.textContent = originalText;
                        button.disabled = false;
                        button.style.opacity = '1';
                        window.DoraElectron.notifications.show(
                            'Game Client Error',
                            data.message || 'Failed to start game client'
                        );
                        unsubscribe();
                    }
                });
            } else {
                button.textContent = originalText;
                button.disabled = false;
                button.style.opacity = '1';
                window.DoraElectron.notifications.show(
                    'Game Client Error',
                    result.error || 'Failed to launch game client'
                );
            }
        } catch (err) {
            button.textContent = originalText;
            button.disabled = false;
            button.style.opacity = '1';
            console.error('[DoraMCU Desktop] Game client launch error:', err);
        }
    }

    // Stop the game client
    async function stopGameClient(button) {
        if (!window.DoraElectron) return;

        button.disabled = true;
        button.textContent = '⏳ Stopping...';

        try {
            await window.DoraElectron.gameClient.stop();
            button.textContent = '🚀 Launch Game Client';
            button.disabled = false;
            button.style.background = '';
            button.onclick = function() {
                launchGameClient(button);
            };
        } catch (err) {
            console.error('[DoraMCU Desktop] Failed to stop game client:', err);
            button.disabled = false;
        }
    }

    // Set up game client event integration
    function setupGameClientIntegration() {
        // Listen for game client deployment events
        window.addEventListener('game-client-deployment', function(e) {
            var data = e.detail;
            console.log('[DoraMCU Desktop] Game client deployment:', data);
            
            var button = document.getElementById('dora-game-launch-btn');
            if (button && data.status) {
                switch (data.status) {
                    case 'downloading':
                        button.textContent = '⏳ ' + (data.message || 'Downloading...');
                        break;
                    case 'downloaded':
                        button.textContent = '🚀 Launch Game Client';
                        break;
                    case 'starting':
                        button.textContent = '⏳ Starting...';
                        break;
                    case 'running':
                        button.textContent = '⏹ Stop Game Client';
                        button.style.background = '#8b0000';
                        break;
                    case 'error':
                        button.textContent = '🚀 Launch Game Client';
                        button.style.background = '';
                        window.DoraElectron.notifications.show(
                            'Game Client Error',
                            data.message || 'An error occurred'
                        );
                        break;
                }
            }
        });
    }

    // Set up desktop notifications for game events
    function setupDesktopNotifications() {
        // Listen for SSE events and show desktop notifications
        window.addEventListener('game-event', function(e) {
            var event = e.detail;
            if (event && event.type === 'combat' || event && event.type === 'alert') {
                window.DoraElectron.notifications.show(
                    'DoraMCU Game Event',
                    event.message || 'A game event has occurred'
                );
            }
        });

        // Show notification when page loads in Electron
        window.DoraElectron.notifications.show(
            'DoraMCU Desktop',
            'Welcome to DoraMCU Desktop!'
        );
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWhenReady);
    } else {
        initWhenReady();
    }

    // Also listen for electron-ready event
    window.addEventListener('electron-ready', function() {
        setTimeout(initWhenReady, 100);
    });

})();
