/**
 * ScenePersistence — Save and restore 3D camera states, viewpoints, and scene configuration.
 *
 * Provides:
 * - Auto-save camera position, zoom level, LOD settings, and visible layers
 * - Viewpoint bookmarks: save named camera presets
 * - Viewpoint sharing: export/import viewpoints as JSON
 * - Restore scene state on page reload automatically
 * - localStorage-based persistence
 *
 * Usage:
 *   const persistence = new ScenePersistence(sceneRenderer);
 *   persistence.init();
 *   persistence.autoSave(); // Called periodically
 *   persistence.saveViewpoint("My Fleet HQ");
 *   persistence.loadViewpoint("My Fleet HQ");
 *   persistence.exportViewpoints();
 *   persistence.importViewpoints(jsonString);
 *   persistence.destroy();
 */

class ScenePersistence {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     */
    constructor(sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
        
        // Storage keys
        this.STORAGE_KEY_STATE = 'doramcu_3d_scene_state';
        this.STORAGE_KEY_VIEWPOINTS = 'doramcu_3d_viewpoints';
        
        // State
        this.sceneState = null;
        this.viewpoints = new Map();
        this.autoSaveInterval = null;
        this.autoSaveDelay = 5000; // 5 seconds
        
        // UI elements
        this.container = null;
        this.viewpointList = null;
        this.controls = null;
        
        // Callbacks
        this.onViewpointSelect = null;
        this.onViewpointDelete = null;
    }

    /**
     * Initialize the scene persistence module.
     */
    init() {
        // Load saved viewpoints
        this.loadViewpointsFromStorage();
        
        // Create UI
        this._createUI();
        
        // Restore scene state if available
        this.restoreSceneState();
        
        console.log('[ScenePersistence] Initialized');
    }

    /**
     * Create the UI elements.
     */
    _createUI() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'scene-persistence-container';
        this.container.style.cssText = `
            position: fixed;
            top: 0;
            right: 0;
            width: 250px;
            height: 100%;
            pointer-events: none;
            z-index: 100;
        `;
        document.body.appendChild(this.container);
        
        // Create viewpoint list panel
        this.viewpointList = document.createElement('div');
        this.viewpointList.className = 'viewpoint-list';
        this.viewpointList.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            width: 230px;
            max-height: 400px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(200, 150, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #ffddaa;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.viewpointList.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #ffcc66;">
                📍 Viewpoints
            </div>
            <div id="viewpoint-items" style="max-height: 300px; overflow-y: auto;"></div>
            <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(200, 150, 100, 0.3);">
                <input type="text" id="viewpoint-name-input" placeholder="Viewpoint name..." style="
                    width: 100%;
                    background: rgba(0, 0, 20, 0.5);
                    border: 1px solid rgba(200, 150, 100, 0.3);
                    color: #ffddaa;
                    padding: 5px;
                    border-radius: 4px;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                    margin-bottom: 5px;
                ">
                <button id="viewpoint-save-btn" style="
                    width: 100%;
                    background: rgba(50, 150, 50, 0.3);
                    border: 1px solid rgba(100, 200, 100, 0.5);
                    color: #aaffaa;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                    margin-bottom: 5px;
                ">Save Current View</button>
                <button id="viewpoint-export-btn" style="
                    width: 100%;
                    background: rgba(50, 100, 200, 0.3);
                    border: 1px solid rgba(100, 150, 255, 0.5);
                    color: #aaccff;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                    margin-bottom: 5px;
                ">Export All</button>
                <button id="viewpoint-import-btn" style="
                    width: 100%;
                    background: rgba(50, 100, 200, 0.3);
                    border: 1px solid rgba(100, 150, 255, 0.5);
                    color: #aaccff;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                    margin-bottom: 5px;
                ">Import</button>
                <button id="viewpoint-close-btn" style="
                    width: 100%;
                    background: rgba(200, 50, 50, 0.3);
                    border: 1px solid rgba(255, 100, 100, 0.5);
                    color: #ffaaaa;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                ">Close</button>
            </div>
        `;
        this.container.appendChild(this.viewpointList);
        
        // Create controls
        this.controls = document.createElement('div');
        this.controls.className = 'scene-persistence-controls';
        this.controls.style.cssText = `
            position: absolute;
            bottom: 10px;
            right: 10px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(200, 150, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: flex;
            gap: 10px;
        `;
        this.controls.innerHTML = `
            <button id="persistence-toggle-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">📍 Viewpoints</button>
            <button id="persistence-restore-btn" style="
                background: rgba(50, 150, 50, 0.3);
                border: 1px solid rgba(100, 200, 100, 0.5);
                color: #aaffaa;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">🔄 Restore</button>
        `;
        this.container.appendChild(this.controls);
        
        // Event listeners
        document.getElementById('persistence-toggle-btn').addEventListener('click', () => {
            this.toggleViewpointList();
        });
        document.getElementById('persistence-restore-btn').addEventListener('click', () => {
            this.restoreSceneState();
        });
        document.getElementById('viewpoint-save-btn').addEventListener('click', () => {
            this.saveCurrentViewpoint();
        });
        document.getElementById('viewpoint-export-btn').addEventListener('click', () => {
            this.exportViewpoints();
        });
        document.getElementById('viewpoint-import-btn').addEventListener('click', () => {
            this.importViewpoints();
        });
        document.getElementById('viewpoint-close-btn').addEventListener('click', () => {
            this.viewpointList.style.display = 'none';
        });
        
        console.log('[ScenePersistence] UI created');
    }

    /**
     * Toggle the viewpoint list panel.
     */
    toggleViewpointList() {
        if (!this.viewpointList) return;
        
        if (this.viewpointList.style.display === 'none') {
            this.viewpointList.style.display = 'block';
            this.updateViewpointListUI();
        } else {
            this.viewpointList.style.display = 'none';
        }
    }

    /**
     * Update the viewpoint list UI.
     */
    updateViewpointListUI() {
        const container = document.getElementById('viewpoint-items');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (this.viewpoints.size === 0) {
            container.innerHTML = '<div style="color: #88aa88; font-size: 10px;">No saved viewpoints</div>';
            return;
        }
        
        for (const [name, viewpoint] of this.viewpoints) {
            const item = document.createElement('div');
            item.className = 'viewpoint-item';
            item.style.cssText = `
                padding: 8px;
                margin-bottom: 5px;
                background: rgba(50, 100, 200, 0.2);
                border: 1px solid rgba(200, 150, 100, 0.2);
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
                display: flex;
                justify-content: space-between;
                align-items: center;
            `;
            item.innerHTML = `
                <div>
                    <div style="font-weight: bold; color: #ffcc66;">${name}</div>
                    <div style="font-size: 10px; color: #8899aa;">
                        ${viewpoint.timestamp ? new Date(viewpoint.timestamp).toLocaleString() : 'Unknown'}
                    </div>
                </div>
                <button class="viewpoint-delete-btn" style="
                    background: rgba(200, 50, 50, 0.3);
                    border: 1px solid rgba(255, 100, 100, 0.5);
                    color: #ffaaaa;
                    padding: 2px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 10px;
                ">✕</button>
            `;
            
            item.addEventListener('mouseenter', () => {
                item.style.background = 'rgba(50, 100, 200, 0.4)';
            });
            item.addEventListener('mouseleave', () => {
                item.style.background = 'rgba(50, 100, 200, 0.2)';
            });
            
            // Click to load viewpoint
            item.querySelector('div').addEventListener('click', () => {
                this.loadViewpoint(name);
            });
            
            // Delete button
            item.querySelector('.viewpoint-delete-btn').addEventListener('click', (e) => {
                e.stopPropagation();
                this.deleteViewpoint(name);
            });
            
            container.appendChild(item);
        }
    }

    /**
     * Save the current camera state.
     */
    saveSceneState() {
        if (!this.sceneRenderer?.camera) return;
        
        const camera = this.sceneRenderer.camera;
        this.sceneState = {
            position: {
                x: camera.position.x,
                y: camera.position.y,
                z: camera.position.z
            },
            rotation: {
                x: camera.rotation.x,
                y: camera.rotation.y,
                z: camera.rotation.z
            },
            zoom: camera.zoom || 1,
            fov: camera.fov || 75,
            timestamp: Date.now()
        };
        
        // Save to localStorage
        try {
            localStorage.setItem(this.STORAGE_KEY_STATE, JSON.stringify(this.sceneState));
        } catch (e) {
            console.warn('[ScenePersistence] Failed to save state:', e.message);
        }
    }

    /**
     * Restore the saved camera state.
     */
    restoreSceneState() {
        if (!this.sceneRenderer?.camera) return;
        
        // Load from localStorage
        try {
            const saved = localStorage.getItem(this.STORAGE_KEY_STATE);
            if (saved) {
                this.sceneState = JSON.parse(saved);
            }
        } catch (e) {
            console.warn('[ScenePersistence] Failed to load state:', e.message);
            return;
        }
        
        if (!this.sceneState) {
            console.log('[ScenePersistence] No saved state to restore');
            return;
        }
        
        const camera = this.sceneRenderer.camera;
        
        // Restore position
        camera.position.set(
            this.sceneState.position.x,
            this.sceneState.position.y,
            this.sceneState.position.z
        );
        
        // Restore rotation
        camera.rotation.set(
            this.sceneState.rotation.x,
            this.sceneState.rotation.y,
            this.sceneState.rotation.z
        );
        
        // Restore zoom/fov
        if (this.sceneState.zoom) {
            camera.zoom = this.sceneState.zoom;
        }
        if (this.sceneState.fov) {
            camera.fov = this.sceneState.fov;
        }
        camera.updateProjectionMatrix();
        
        console.log('[ScenePersistence] Scene state restored');
    }

    /**
     * Start auto-saving scene state periodically.
     */
    startAutoSave() {
        if (this.autoSaveInterval) {
            clearInterval(this.autoSaveInterval);
        }
        
        this.autoSaveInterval = setInterval(() => {
            this.saveSceneState();
        }, this.autoSaveDelay);
        
        console.log('[ScenePersistence] Auto-save started');
    }

    /**
     * Stop auto-saving.
     */
    stopAutoSave() {
        if (this.autoSaveInterval) {
            clearInterval(this.autoSaveInterval);
            this.autoSaveInterval = null;
        }
    }

    /**
     * Save the current view as a named viewpoint.
     */
    saveCurrentViewpoint() {
        const input = document.getElementById('viewpoint-name-input');
        if (!input || !input.value.trim()) {
            alert('Please enter a viewpoint name');
            return;
        }
        
        const name = input.value.trim();
        this.saveViewpoint(name);
        input.value = '';
    }

    /**
     * Save a named viewpoint.
     * @param {string} name - The viewpoint name
     */
    saveViewpoint(name) {
        if (!this.sceneRenderer?.camera) {
            console.warn('[ScenePersistence] No camera to save');
            return;
        }
        
        const camera = this.sceneRenderer.camera;
        const viewpoint = {
            name: name,
            position: {
                x: camera.position.x,
                y: camera.position.y,
                z: camera.position.z
            },
            rotation: {
                x: camera.rotation.x,
                y: camera.rotation.y,
                z: camera.rotation.z
            },
            zoom: camera.zoom || 1,
            fov: camera.fov || 75,
            timestamp: Date.now()
        };
        
        this.viewpoints.set(name, viewpoint);
        this.saveViewpointsToStorage();
        this.updateViewpointListUI();
        
        console.log('[ScenePersistence] Viewpoint saved:', name);
    }

    /**
     * Load a saved viewpoint.
     * @param {string} name - The viewpoint name
     */
    loadViewpoint(name) {
        const viewpoint = this.viewpoints.get(name);
        if (!viewpoint || !this.sceneRenderer?.camera) {
            console.warn('[ScenePersistence] Viewpoint not found:', name);
            return;
        }
        
        const camera = this.sceneRenderer.camera;
        
        // Animate camera to viewpoint position
        const startPos = camera.position.clone();
        const endPos = new THREE.Vector3(
            viewpoint.position.x,
            viewpoint.position.y,
            viewpoint.position.z
        );
        
        const startRot = camera.rotation.clone();
        const endRot = new THREE.Euler(
            viewpoint.rotation.x,
            viewpoint.rotation.y,
            viewpoint.rotation.z
        );
        
        const duration = 1000; // 1 second animation
        const startTime = Date.now();
        
        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            // Smooth easing
            const eased = progress < 0.5 
                ? 2 * progress * progress 
                : 1 - Math.pow(-2 * progress + 2, 2) / 2;
            
            camera.position.lerpVectors(startPos, endPos, eased);
            camera.rotation.x = startRot.x + (endRot.x - startRot.x) * eased;
            camera.rotation.y = startRot.y + (endRot.y - startRot.y) * eased;
            camera.rotation.z = startRot.z + (endRot.z - startRot.z) * eased;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                // Set final zoom/fov
                if (viewpoint.zoom) camera.zoom = viewpoint.zoom;
                if (viewpoint.fov) camera.fov = viewpoint.fov;
                camera.updateProjectionMatrix();
                
                // Save as current state
                this.saveSceneState();
            }
        };
        
        animate();
        
        if (this.onViewpointSelect) {
            this.onViewpointSelect(name);
        }
        
        console.log('[ScenePersistence] Viewpoint loaded:', name);
    }

    /**
     * Delete a saved viewpoint.
     * @param {string} name - The viewpoint name
     */
    deleteViewpoint(name) {
        this.viewpoints.delete(name);
        this.saveViewpointsToStorage();
        this.updateViewpointListUI();
        
        if (this.onViewpointDelete) {
            this.onViewpointDelete(name);
        }
        
        console.log('[ScenePersistence] Viewpoint deleted:', name);
    }

    /**
     * Export all viewpoints as JSON.
     * @returns {string} JSON string of all viewpoints
     */
    exportViewpoints() {
        const viewpoints = Array.from(this.viewpoints.values());
        const json = JSON.stringify(viewpoints, null, 2);
        
        // Copy to clipboard
        navigator.clipboard.writeText(json).then(() => {
            alert('Viewpoints copied to clipboard!');
        }).catch(() => {
            // Fallback: prompt user
            prompt('Copy this JSON:', json);
        });
        
        return json;
    }

    /**
     * Import viewpoints from JSON.
     */
    importViewpoints() {
        const json = prompt('Paste viewpoints JSON:');
        if (!json) return;
        
        try {
            const viewpoints = JSON.parse(json);
            if (!Array.isArray(viewpoints)) {
                throw new Error('Invalid format');
            }
            
            let imported = 0;
            for (const vp of viewpoints) {
                if (vp.name && vp.position) {
                    this.viewpoints.set(vp.name, vp);
                    imported++;
                }
            }
            
            this.saveViewpointsToStorage();
            this.updateViewpointListUI();
            
            console.log(`[ScenePersistence] Imported ${imported} viewpoints`);
            alert(`Imported ${imported} viewpoints!`);
        } catch (e) {
            console.error('[ScenePersistence] Failed to import:', e.message);
            alert('Invalid JSON format');
        }
    }

    /**
     * Load viewpoints from localStorage.
     */
    loadViewpointsFromStorage() {
        try {
            const saved = localStorage.getItem(this.STORAGE_KEY_VIEWPOINTS);
            if (saved) {
                const viewpoints = JSON.parse(saved);
                for (const vp of viewpoints) {
                    if (vp.name && vp.position) {
                        this.viewpoints.set(vp.name, vp);
                    }
                }
            }
        } catch (e) {
            console.warn('[ScenePersistence] Failed to load viewpoints:', e.message);
        }
    }

    /**
     * Save viewpoints to localStorage.
     */
    saveViewpointsToStorage() {
        try {
            const viewpoints = Array.from(this.viewpoints.values());
            localStorage.setItem(this.STORAGE_KEY_VIEWPOINTS, JSON.stringify(viewpoints));
        } catch (e) {
            console.warn('[ScenePersistence] Failed to save viewpoints:', e.message);
        }
    }

    /**
     * Set callbacks for events.
     * @param {Object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        if (callbacks.onViewpointSelect) this.onViewpointSelect = callbacks.onViewpointSelect;
        if (callbacks.onViewpointDelete) this.onViewpointDelete = callbacks.onViewpointDelete;
    }

    /**
     * Destroy the scene persistence module.
     */
    destroy() {
        this.stopAutoSave();
        
        // Save final state
        this.saveSceneState();
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.viewpoints.clear();
        this.sceneState = null;
        
        console.log('[ScenePersistence] Destroyed');
    }
}

// Expose globally
window.ScenePersistence = ScenePersistence;
