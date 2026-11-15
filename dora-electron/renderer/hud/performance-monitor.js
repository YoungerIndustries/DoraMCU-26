/**
 * PerformanceMonitor — Real-time performance monitoring for 3D visualization.
 *
 * Provides:
 * - Frame rate monitoring with FPS counter
 * - Memory usage tracking (JS heap and Three.js geometry count)
 * - Adaptive LOD based on performance thresholds
 * - Performance statistics overlay
 * - Automatic degradation when performance drops
 *
 * Usage:
 *   const monitor = new PerformanceMonitor(sceneRenderer);
 *   monitor.start();
 *   monitor.setCallbacks({
 *     onLowFps: () => { /* reduce quality */ },
 *     onHighMemory: () => { /* cleanup */ }
 *   });
 *   monitor.destroy();
 */

class PerformanceMonitor {
    /**
     * @param {SceneRenderer} sceneRenderer - The SceneRenderer instance
     */
    constructor(sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
        
        // Performance metrics
        this.fps = 60;
        this.frameCount = 0;
        this.lastFpsUpdate = performance.now();
        this.fpsHistory = [];
        this.maxFpsHistory = 120; // 2 minutes at 10 samples/sec
        
        // Memory tracking
        this.memorySamples = [];
        this.maxMemorySamples = 60;
        
        // Three.js object tracking
        this.meshCount = 0;
        this.geometryCount = 0;
        this.materialCount = 0;
        
        // Thresholds
        this.lowFpsThreshold = 30;
        this.criticalFpsThreshold = 15;
        this.highMemoryThreshold = 500; // MB
        
        // State
        this.running = false;
        this.animationFrame = null;
        this.checkInterval = null;
        
        // Callbacks
        this.callbacks = {
            onLowFps: null,
            onCriticalFps: null,
            onHighMemory: null,
            onStatsUpdate: null
        };
        
        // Overlay element
        this.overlay = null;
        this.showOverlay = false;
        
        console.log('[PerfMonitor] Initialized');
    }

    /**
     * Set performance callbacks.
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Start monitoring.
     */
    start() {
        if (this.running) return;
        this.running = true;
        
        // FPS monitoring loop
        this._startFpsLoop();
        
        // Periodic stats check
        this.checkInterval = setInterval(() => this._checkPerformance(), 1000);
        
        console.log('[PerfMonitor] Started');
    }

    /**
     * Stop monitoring.
     */
    stop() {
        this.running = false;
        
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
        
        if (this.checkInterval) {
            clearInterval(this.checkInterval);
            this.checkInterval = null;
        }
        
        console.log('[PerfMonitor] Stopped');
    }

    /**
     * Toggle the performance overlay.
     */
    toggleOverlay() {
        this.showOverlay = !this.showOverlay;
        
        if (this.showOverlay && !this.overlay) {
            this._createOverlay();
        } else if (!this.showOverlay && this.overlay) {
            this._removeOverlay();
        }
    }

    /**
     * Get current performance statistics.
     */
    getStats() {
        const scene = this.sceneRenderer && this.sceneRenderer.scene;
        
        // Count Three.js objects
        if (scene) {
            this.meshCount = 0;
            this.geometryCount = 0;
            this.materialCount = 0;
            
            scene.traverse((obj) => {
                if (obj.isMesh) this.meshCount++;
                if (obj.geometry) this.geometryCount++;
                if (obj.material) this.materialCount++;
            });
        }
        
        // Memory info (Chrome/Firefox only)
        let memoryInfo = null;
        if (performance.memory) {
            memoryInfo = {
                usedHeapMB: Math.round(performance.memory.usedJSHeapSize / 1048576),
                totalHeapMB: Math.round(performance.memory.totalJSHeapSize / 1048576),
                limitMB: Math.round(performance.memory.jsHeapSizeLimit / 1048576)
            };
        }
        
        return {
            fps: this.fps,
            fpsHistory: this.fpsHistory.slice(-60), // Last 60 samples
            meshCount: this.meshCount,
            geometryCount: this.geometryCount,
            materialCount: this.materialCount,
            memory: memoryInfo,
            lowFpsThreshold: this.lowFpsThreshold,
            criticalFpsThreshold: this.criticalFpsThreshold
        };
    }

    /**
     * Force a memory cleanup suggestion.
     */
    suggestCleanup() {
        const stats = this.getStats();
        
        if (performance.memory && stats.memory.usedHeapMB > this.highMemoryThreshold) {
            if (this.callbacks.onHighMemory) {
                this.callbacks.onHighMemory(stats);
            }
            console.warn('[PerfMonitor] High memory usage:', stats.memory.usedHeapMB, 'MB');
        }
    }

    /**
     * Get recommended LOD level based on current performance.
     * Returns: 'high', 'medium', 'low', or 'minimal'
     */
    getRecommendedLod() {
        if (this.fps >= 50) return 'high';
        if (this.fps >= this.lowFpsThreshold) return 'medium';
        if (this.fps >= this.criticalFpsThreshold) return 'low';
        return 'minimal';
    }

    /**
     * FPS monitoring loop.
     * @private
     */
    _startFpsLoop() {
        let lastTime = performance.now();
        let frames = 0;
        
        const loop = (time) => {
            if (!this.running) return;
            
            frames++;
            
            if (time - lastTime >= 1000) {
                this.fps = Math.round((frames * 1000) / (time - lastTime));
                this.fpsHistory.push({ time: Date.now(), fps: this.fps });
                
                if (this.fpsHistory.length > this.maxFpsHistory) {
                    this.fpsHistory.shift();
                }
                
                frames = 0;
                lastTime = time;
                
                // Update overlay
                if (this.showOverlay && this.overlay) {
                    this._updateOverlay();
                }
            }
            
            this.animationFrame = requestAnimationFrame(loop);
        };
        
        this.animationFrame = requestAnimationFrame(loop);
    }

    /**
     * Check performance thresholds and trigger callbacks.
     * @private
     */
    _checkPerformance() {
        if (this.fps <= this.criticalFpsThreshold) {
            if (this.callbacks.onCriticalFps) {
                this.callbacks.onCriticalFps(this.getStats());
            }
        } else if (this.fps <= this.lowFpsThreshold) {
            if (this.callbacks.onLowFps) {
                this.callbacks.onLowFps(this.getStats());
            }
        }
        
        this.suggestCleanup();
        
        if (this.callbacks.onStatsUpdate) {
            this.callbacks.onStatsUpdate(this.getStats());
        }
    }

    /**
     * Create the performance overlay element.
     * @private
     */
    _createOverlay() {
        this.overlay = document.createElement('div');
        this.overlay.id = 'perf-monitor-overlay';
        this.overlay.style.cssText = `
            position: fixed;
            top: 10px;
            right: 10px;
            background: rgba(0, 0, 0, 0.7);
            color: #0f0;
            font-family: monospace;
            font-size: 11px;
            padding: 8px 12px;
            border-radius: 4px;
            z-index: 10000;
            pointer-events: none;
            min-width: 180px;
        `;
        document.body.appendChild(this.overlay);
        this._updateOverlay();
    }

    /**
     * Update the overlay content.
     * @private
     */
    _updateOverlay() {
        if (!this.overlay) return;
        
        const stats = this.getStats();
        const lod = this.getRecommendedLod();
        const fpsColor = stats.fps >= 50 ? '#0f0' : stats.fps >= 30 ? '#ff0' : '#f00';
        
        let html = `<div style="color: ${fpsColor}; font-weight: bold;">FPS: ${stats.fps}</div>`;
        html += `<div>Meshes: ${stats.meshCount}</div>`;
        html += `<div>Geometries: ${stats.geometryCount}</div>`;
        html += `<div>LOD: ${lod}</div>`;
        
        if (stats.memory) {
            html += `<div>Memory: ${stats.memory.usedHeapMB} / ${stats.memory.totalHeapMB} MB</div>`;
        }
        
        this.overlay.innerHTML = html;
    }

    /**
     * Remove the overlay element.
     * @private
     */
    _removeOverlay() {
        if (this.overlay && this.overlay.parentNode) {
            this.overlay.parentNode.removeChild(this.overlay);
            this.overlay = null;
        }
    }

    /**
     * Destroy the monitor and clean up.
     */
    destroy() {
        this.stop();
        this._removeOverlay();
        this.fpsHistory = [];
        this.memorySamples = [];
        this.callbacks = {};
        console.log('[PerfMonitor] Destroyed');
    }
}

// Expose globally
window.PerformanceMonitor = PerformanceMonitor;
