/**
 * MusicPlayer — Background music player using an embedded web player.
 *
 * Embeds a music streaming service (Pandora, Spotify, etc.) in a floating
 * Electron BrowserView so players can listen to music while playing.
 *
 * Features:
 * - Floating player window that overlays the game UI
 * - Toggle visibility via game shell button
 * - Volume control independent of voice chat
 * - Supports multiple music services (Pandora by default)
 * - Draggable and resizable player window
 *
 * Usage:
 *   const player = new MusicPlayer();
 *   player.init();
 *   player.show();
 *   player.hide();
 *   player.setVolume(0.5);
 *   player.destroy();
 */

class MusicPlayer {
    /**
     * @param {Object} options - Configuration options
     * @param {string} options.service - Music service name ('pandora', 'spotify', 'soundcloud')
     * @param {string} options.url - Custom URL for the music service
     * @param {number} options.width - Initial width of the player window
     * @param {number} options.height - Initial height of the player window
     */
    constructor(options = {}) {
        this.service = options.service || 'pandora';
        this.width = options.width || 400;
        this.height = options.height || 600;
        
        // Service URLs
        this.serviceUrls = {
            pandora: 'https://www.pandora.com',
            spotify: 'https://open.spotify.com',
            soundcloud: 'https://soundcloud.com',
            youtubemusic: 'https://music.youtube.com',
            tunedin: 'https://tunein.com'
        };
        
        // Override with custom URL if provided
        this.url = options.url || this.serviceUrls[this.service];
        
        // BrowserView state
        this.browserView = null;
        this.isVisible = false;
        this.isReady = false;
        
        // Position and size
        this.position = { x: 0, y: 0 };
        this.isDragging = false;
        this.dragOffset = { x: 0, y: 0 };
        
        // Volume control
        this.volume = 1.0;
        this.audioContext = null;
        this.gainNode = null;
        
        // UI elements
        this.container = null;
        this.controls = null;
        
        // Callbacks
        this.onReady = null;
        this.onError = null;
    }

    /**
     * Initialize the music player.
     * Creates the BrowserView and sets up the web player.
     */
    async init() {
        try {
            console.log(`[MusicPlayer] Initializing ${this.service} player...`);
            
            // Check if running in Electron
            if (!window.electronAPI || !window.electronAPI.isElectron) {
                console.warn('[MusicPlayer] Not running in Electron - creating fallback iframe player');
                await this._createFallbackPlayer();
                return;
            }
            
            // Request BrowserView creation from main process
            const result = await window.electronAPI.createMusicPlayer({
                url: this.url,
                width: this.width,
                height: this.height
            });
            
            if (result.success) {
                this.isReady = true;
                console.log('[MusicPlayer] BrowserView created successfully');
                if (this.onReady) this.onReady();
            } else {
                throw new Error(result.error || 'Failed to create music player');
            }
        } catch (error) {
            console.error('[MusicPlayer] Init failed:', error);
            if (this.onError) this.onError(error);
            
            // Fallback to iframe player
            console.warn('[MusicPlayer] Falling back to iframe player');
            await this._createFallbackPlayer();
        }
    }

    /**
     * Create a fallback iframe-based player for non-Electron environments.
     */
    async _createFallbackPlayer() {
        // Create a container for the iframe player
        this.container = document.createElement('div');
        this.container.id = 'music-player-container';
        this.container.className = 'music-player-container';
        this.container.style.display = 'none';
        
        // Create the iframe
        const iframe = document.createElement('iframe');
        iframe.id = 'music-player-iframe';
        iframe.className = 'music-player-iframe';
        iframe.src = this.url;
        iframe.allow = 'autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture';
        iframe.allowFullscreen = true;
        iframe.sandbox = 'allow-scripts allow-same-origin allow-popups allow-forms';
        
        // Create controls bar
        this.controls = document.createElement('div');
        this.controls.className = 'music-player-controls';
        this.controls.innerHTML = `
            <div class="music-player-header">
                <span class="music-player-title">${this._getServiceDisplayName()}</span>
                <div class="music-player-buttons">
                    <button class="music-player-btn" id="musicVolumeBtn" title="Volume">🔊</button>
                    <button class="music-player-btn" id="musicMinimizeBtn" title="Minimize">➖</button>
                    <button class="music-player-btn" id="musicCloseBtn" title="Close">✖</button>
                </div>
            </div>
            <div class="music-player-volume" id="musicVolumeSlider" style="display:none;">
                <input type="range" min="0" max="100" value="${this.volume * 100}" id="musicVolumeInput">
                <span class="music-volume-value" id="musicVolumeValue">${Math.round(this.volume * 100)}%</span>
            </div>
        `;
        
        this.container.appendChild(this.controls);
        this.container.appendChild(iframe);
        document.body.appendChild(this.container);
        
        // Bind events
        this._bindFallbackEvents();
        
        this.isReady = true;
        console.log('[MusicPlayer] Fallback iframe player created');
        if (this.onReady) this.onReady();
    }

    /**
     * Bind events for the fallback iframe player.
     */
    _bindFallbackEvents() {
        const closeBtn = document.getElementById('musicCloseBtn');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.hide());
        }
        
        const minimizeBtn = document.getElementById('musicMinimizeBtn');
        if (minimizeBtn) {
            minimizeBtn.addEventListener('click', () => this._toggleMinimized());
        }
        
        const volumeBtn = document.getElementById('musicVolumeBtn');
        if (volumeBtn) {
            volumeBtn.addEventListener('click', () => this._toggleVolumeSlider());
        }
        
        const volumeInput = document.getElementById('musicVolumeInput');
        if (volumeInput) {
            volumeInput.addEventListener('input', (e) => {
                this.setVolume(e.target.value / 100);
            });
        }
        
        // Make draggable
        this._makeDraggable();
    }

    /**
     * Make the player container draggable.
     */
    _makeDraggable() {
        if (!this.controls) return;
        
        const header = this.controls.querySelector('.music-player-header');
        if (!header) return;
        
        header.style.cursor = 'move';
        
        const onmousedown = (e) => {
            if (e.target.closest('.music-player-buttons')) return;
            
            this.isDragging = true;
            const rect = this.container.getBoundingClientRect();
            this.dragOffset.x = e.clientX - rect.left;
            this.dragOffset.y = e.clientY - rect.top;
            
            const onmousemove = (e) => {
                if (!this.isDragging) return;
                
                this.position.x = e.clientX - this.dragOffset.x;
                this.position.y = e.clientY - this.dragOffset.y;
                
                this.container.style.left = this.position.x + 'px';
                this.container.style.top = this.position.y + 'px';
            };
            
            const onmouseup = () => {
                this.isDragging = false;
                document.removeEventListener('mousemove', onmousemove);
                document.removeEventListener('mouseup', onmouseup);
            };
            
            document.addEventListener('mousemove', onmousemove);
            document.addEventListener('mouseup', onmouseup);
        };
        
        header.addEventListener('mousedown', onmousedown);
    }

    /**
     * Show the music player.
     */
    show() {
        if (!this.isReady) {
            console.warn('[MusicPlayer] Not ready yet');
            return;
        }
        
        if (this.browserView) {
            // Electron BrowserView mode
            window.electronAPI.showMusicPlayer();
        } else if (this.container) {
            // Fallback iframe mode
            this.container.style.display = 'block';
        }
        
        this.isVisible = true;
        console.log('[MusicPlayer] Shown');
    }

    /**
     * Hide the music player.
     */
    hide() {
        if (this.browserView) {
            // Electron BrowserView mode
            window.electronAPI.hideMusicPlayer();
        } else if (this.container) {
            // Fallback iframe mode
            this.container.style.display = 'none';
        }
        
        this.isVisible = false;
        console.log('[MusicPlayer] Hidden');
    }

    /**
     * Toggle the music player visibility.
     */
    toggle() {
        if (this.isVisible) {
            this.hide();
        } else {
            this.show();
        }
    }

    /**
     * Set the volume level (0.0 to 1.0).
     * @param {number} volume - Volume level between 0 and 1
     */
    setVolume(volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        
        // Update UI
        const volumeValue = document.getElementById('musicVolumeValue');
        if (volumeValue) {
            volumeValue.textContent = Math.round(this.volume * 100) + '%';
        }
        
        const volumeInput = document.getElementById('musicVolumeInput');
        if (volumeInput) {
            volumeInput.value = this.volume * 100;
        }
        
        // Send to main process for BrowserView audio control
        if (window.electronAPI && window.electronAPI.isElectron) {
            window.electronAPI.setMusicPlayerVolume(this.volume);
        }
        
        console.log(`[MusicPlayer] Volume set to ${Math.round(this.volume * 100)}%`);
    }

    /**
     * Get the current volume level.
     * @returns {number} Current volume (0.0 to 1.0)
     */
    getVolume() {
        return this.volume;
    }

    /**
     * Toggle the volume slider visibility.
     */
    _toggleVolumeSlider() {
        const slider = document.getElementById('musicVolumeSlider');
        if (slider) {
            slider.style.display = slider.style.display === 'none' ? 'block' : 'none';
        }
    }

    /**
     * Toggle minimized state (show only controls).
     */
    _toggleMinimized() {
        const iframe = document.getElementById('music-player-iframe');
        if (iframe) {
            const isMinimized = iframe.style.display === 'none';
            iframe.style.display = isMinimized ? 'block' : 'none';
        }
    }

    /**
     * Navigate to a specific URL.
     * @param {string} url - URL to navigate to
     */
    navigate(url) {
        if (this.browserView) {
            window.electronAPI.navigateMusicPlayer(url);
        } else {
            const iframe = document.getElementById('music-player-iframe');
            if (iframe) {
                iframe.src = url;
            }
        }
    }

    /**
     * Reload the player.
     */
    reload() {
        if (this.browserView) {
            window.electronAPI.reloadMusicPlayer();
        } else {
            const iframe = document.getElementById('music-player-iframe');
            if (iframe) {
                iframe.src = iframe.src;
            }
        }
    }

    /**
     * Get the display name for the current service.
     * @returns {string} Service display name
     */
    _getServiceDisplayName() {
        const names = {
            pandora: 'Pandora',
            spotify: 'Spotify',
            soundcloud: 'SoundCloud',
            youtubemusic: 'YouTube Music',
            tunedin: 'TuneIn Radio'
        };
        return names[this.service] || 'Music Player';
    }

    /**
     * Set callbacks for events.
     * @param {Object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        if (callbacks.onReady) this.onReady = callbacks.onReady;
        if (callbacks.onError) this.onError = callbacks.onError;
    }

    /**
     * Destroy the music player and clean up resources.
     */
    destroy() {
        if (this.browserView) {
            window.electronAPI.destroyMusicPlayer();
        }
        
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        if (this.audioContext) {
            this.audioContext.close();
            this.audioContext = null;
        }
        
        this.isReady = false;
        this.isVisible = false;
        console.log('[MusicPlayer] Destroyed');
    }
}

// Expose globally
window.MusicPlayer = MusicPlayer;
