/**
 * WebInferenceService — Browser-based ONNX inference for the DoraMCU PWA.
 *
 * Features:
 * - Local ONNX inference using ONNX Runtime Web (WebAssembly)
 * - Offline AI predictions when network is unavailable
 * - Experience data collection with service worker sync
 * - Graceful fallback to server-side inference
 * - Model download and caching via service worker
 *
 * Usage:
 *   const inference = new WebInferenceService({ modelUrl: '/models/dora-ai.onnx' });
 *   await inference.initialize();
 *   const result = await inference.predict(gameState);
 *   inference.addExperience(gameState, 'attack', 0.8);
 */

class WebInferenceService {
    /**
     * Create a new Web Inference service.
     * @param {Object} options - Configuration options
     * @param {string} [options.modelUrl] - URL to download ONNX model from
     * @param {string} [options.neuronBaseUrl='http://localhost:8090'] - Neuron server URL for fallback
     * @param {string} [options.nodeId] - Unique node ID (auto-generated if not provided)
     * @param {number} [options.maxExperienceBuffer=100] - Max experience buffer size
     * @param {boolean} [options.enableWebAssembly=true] - Enable WebAssembly backend
     * @param {number} [options.inferenceThreads=1] - Number of inference threads
     */
    constructor(options = {}) {
        this.modelUrl = options.modelUrl || null;
        this.neuronBaseUrl = options.neuronBaseUrl || 'http://localhost:8090';
        this.nodeId = options.nodeId || `pwa-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        this.maxExperienceBuffer = options.maxExperienceBuffer || 100;
        this.enableWebAssembly = options.enableWebAssembly !== false;
        this.inferenceThreads = options.inferenceThreads || 1;

        // State
        this.session = null;
        this.runtime = null;
        this.modelLoaded = false;
        this.modelVersion = 0;

        // Experience buffer
        this.experienceBuffer = [];
        this.totalInferences = 0;
        this.totalLocalInferences = 0;
        this.totalServerInferences = 0;

        // Event handlers
        this.onModelLoadedHandlers = [];
        this.onInferenceResultHandlers = [];
    }

    /**
     * Initialize the service: load ONNX Runtime Web and download model.
     */
    async initialize() {
        console.log('[WebInference] Initializing...', this.nodeId);

        // Try to load ONNX Runtime Web
        if (this.enableWebAssembly) {
            await this._loadOnnxRuntimeWeb();
        }

        // Download and load model if URL provided
        if (this.modelUrl && this.session) {
            await this._loadModel(this.modelUrl);
        }

        // Restore experience buffer from localStorage
        this._restoreExperienceBuffer();

        console.log('[WebInference] Initialized', {
            nodeId: this.nodeId,
            modelLoaded: this.modelLoaded,
            webAssembly: this.enableWebAssembly
        });
    }

    /**
     * Load ONNX Runtime Web library dynamically.
     */
    async _loadOnnxRuntimeWeb() {
        try {
            // Check if ort is already loaded globally
            if (typeof ort !== 'undefined') {
                this.runtime = ort;
                console.log('[WebInference] ONNX Runtime Web already loaded');
                return;
            }

            // Dynamic import for ES module
            if (typeof importScripts === 'function') {
                // Service worker context
                console.log('[WebInference] Running in service worker, skipping ONNX Runtime Web');
                return;
            }

            // Browser context - try dynamic import
            if (typeof require !== 'undefined') {
                this.runtime = require('onnxruntime-web');
            } else {
                // Check for CDN-loaded version
                console.log('[WebInference] ONNX Runtime Web not available via require, checking global');
                if (typeof window.ort !== 'undefined') {
                    this.runtime = window.ort;
                }
            }

            if (this.runtime) {
                console.log('[WebInference] ONNX Runtime Web loaded');
            } else {
                console.log('[WebInference] ONNX Runtime Web not available, using server-side fallback');
            }
        } catch (err) {
            console.log('[WebInference] ONNX Runtime Web load failed:', err.message);
            this.runtime = null;
        }
    }

    /**
     * Download and load the ONNX model.
     * @param {string} modelUrl - URL to the ONNX model file
     */
    async _loadModel(modelUrl) {
        if (!this.runtime) {
            console.log('[WebInference] No ONNX Runtime available, skipping model load');
            return;
        }

        try {
            this.session = await this.runtime.InferenceSession.create(modelUrl, {
                executionProviders: ['webgl', 'wasm'],
                intraOpNumThreads: this.inferenceThreads
            });

            this.modelLoaded = true;
            console.log('[WebInference] Model loaded from:', modelUrl);

            // Notify handlers
            for (const handler of this.onModelLoadedHandlers) {
                try {
                    handler(this.session);
                } catch (err) {
                    console.error('[WebInference] Model loaded handler error:', err);
                }
            }
        } catch (err) {
            console.warn('[WebInference] Model load failed:', err.message);
            this.session = null;
            this.modelLoaded = false;
        }
    }

    /**
     * Run inference on the given input state.
     * Tries local inference first, falls back to server.
     * @param {number[]} inputState - Input state vector (64-dim)
     * @param {string[]} [availableActions] - Optional list of available action names
     * @returns {Promise<Object>} Inference result
     */
    async predict(inputState, availableActions) {
        this.totalInferences++;

        // Try local inference first
        if (this.modelLoaded && this.session) {
            try {
                const result = await this._runLocalInference(inputState);
                this.totalLocalInferences++;
                result.source = 'local';

                // Notify handlers
                this._notifyInferenceResult(result);

                return result;
            } catch (err) {
                console.warn('[WebInference] Local inference failed:', err.message);
            }
        }

        // Fall back to server-side inference
        const result = await this._requestServerInference(inputState, availableActions);
        this.totalServerInferences++;
        result.source = 'server';

        this._notifyInferenceResult(result);
        return result;
    }

    /**
     * Run local ONNX inference.
     */
    async _runLocalInference(inputState) {
        const inputTensor = new this.runtime.Tensor('float32', inputState, [1, inputState.length]);
        const feed = {};
        feed[this.session.inputNames[0]] = inputTensor;

        const startTime = performance.now();
        const results = await this.session.run(feed);
        const latencyMs = performance.now() - startTime;

        const outputNames = this.session.outputNames;
        const probabilities = Array.from(results[outputNames[0]].data);

        // Find predicted action (argmax)
        let maxIdx = 0;
        let maxVal = probabilities[0];
        for (let i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxVal) {
                maxVal = probabilities[i];
                maxIdx = i;
            }
        }

        return {
            predictedAction: maxIdx,
            predictedActionName: `action_${maxIdx}`,
            confidence: maxVal,
            probabilities: probabilities,
            modelVersion: this.modelVersion,
            latencyMs: Math.round(latencyMs)
        };
    }

    /**
     * Request AI inference from the Neuron server.
     */
    async _requestServerInference(inputState, availableActions) {
        const url = `${this.neuronBaseUrl}/api/v1/neuron/ai-assist`;

        const payload = {
            nodeId: this.nodeId,
            taskType: 'inference',
            inputState: inputState,
            availableActions: availableActions || []
        };

        try {
            const startTime = Date.now();
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const data = await response.json();
                data.latencyMs = Date.now() - startTime;
                return data;
            }
        } catch (err) {
            console.warn('[WebInference] Server inference failed:', err.message);
        }

        // Fallback to heuristic
        return this._heuristicFallback(inputState, availableActions);
    }

    /**
     * Submit an experience sample for AI model retraining.
     * @param {number[]} state - Game state at decision time
     * @param {string} action - Action taken
     * @param {number} reward - Outcome reward
     */
    addExperience(state, action, reward) {
        const experience = {
            source: this.nodeId,
            clientId: this.nodeId,
            state: state,
            action: action,
            reward: reward,
            timestamp: Date.now()
        };

        if (this.experienceBuffer.length >= this.maxExperienceBuffer) {
            this.experienceBuffer.shift();
        }
        this.experienceBuffer.push(experience);

        // Cache to localStorage for persistence
        this._saveExperienceBuffer();

        // Notify service worker to sync when online
        this._notifyServiceWorker(experience);
    }

    /**
     * Notify service worker to cache experience for background sync.
     */
    _notifyServiceWorker(experience) {
        if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
            navigator.serviceWorker.controller.postMessage({
                type: 'CACHE_EXPERIENCE',
                experience: experience
            });
        }
    }

    /**
     * Flush experience buffer to Neuron server.
     */
    async flushExperience() {
        if (this.experienceBuffer.length === 0) return;

        const batch = [...this.experienceBuffer];
        this.experienceBuffer = [];
        this._saveExperienceBuffer();

        const url = `${this.neuronBaseUrl}/api/v1/neuron/experience`;

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(batch)
            });

            if (response.ok) {
                console.log(`[WebInference] Uploaded ${batch.length} experience samples`);
                return true;
            }
        } catch (err) {
            console.warn('[WebInference] Experience upload failed:', err.message);
            // Re-add to buffer for retry
            this.experienceBuffer.unshift(...batch);
            this._saveExperienceBuffer();
        }
        return false;
    }

    /**
     * Save experience buffer to localStorage.
     */
    _saveExperienceBuffer() {
        try {
            localStorage.setItem('dora-experience-buffer', JSON.stringify(this.experienceBuffer));
        } catch (err) {
            // Ignore storage errors
        }
    }

    /**
     * Restore experience buffer from localStorage.
     */
    _restoreExperienceBuffer() {
        try {
            const saved = localStorage.getItem('dora-experience-buffer');
            if (saved) {
                this.experienceBuffer = JSON.parse(saved);
                console.log(`[WebInference] Restored ${this.experienceBuffer.length} experience samples`);
            }
        } catch (err) {
            // Ignore restore errors
        }
    }

    /**
     * Heuristic fallback when no AI service is available.
     */
    _heuristicFallback(inputState, availableActions) {
        let score = 0;
        const half = Math.floor(inputState.length / 2);
        for (let i = 0; i < half; i++) {
            score += inputState[i];
        }

        let actionIndex = 0;
        if (score > 0.5) actionIndex = 1;
        else if (score < -0.5) actionIndex = 2;

        if (availableActions && actionIndex >= availableActions.length) {
            actionIndex = 0;
        }

        return {
            predictedAction: actionIndex,
            predictedActionName: availableActions ? (availableActions[actionIndex] || 'idle') : `action_${actionIndex}`,
            confidence: 0.2,
            probabilities: null,
            modelVersion: -1,
            latencyMs: 0,
            source: 'heuristic'
        };
    }

    // ── Event Handlers ─────────────────────────────────────────────────────────

    onModelLoaded(handler) {
        this.onModelLoadedHandlers.push(handler);
    }

    onInferenceResult(handler) {
        this.onInferenceResultHandlers.push(handler);
    }

    _notifyInferenceResult(result) {
        for (const handler of this.onInferenceResultHandlers) {
            try {
                handler(result);
            } catch (err) {
                console.error('[WebInference] Inference result handler error:', err);
            }
        }
    }

    // ── Status & Cleanup ───────────────────────────────────────────────────────

    /**
     * Get service status and statistics.
     */
    getStatus() {
        return {
            nodeId: this.nodeId,
            modelLoaded: this.modelLoaded,
            webAssembly: this.enableWebAssembly,
            totalInferences: this.totalInferences,
            totalLocalInferences: this.totalLocalInferences,
            totalServerInferences: this.totalServerInferences,
            experienceBuffered: this.experienceBuffer.length
        };
    }

    /**
     * Destroy the service and clean up resources.
     */
    async destroy() {
        console.log('[WebInference] Destroying...');

        if (this.session) {
            try {
                this.session.release();
            } catch (err) {
                // Ignore release errors
            }
            this.session = null;
        }

        this.modelLoaded = false;
        this.experienceBuffer = [];
        this.runtime = null;
    }
}

// Expose globally for browser use
if (typeof window !== 'undefined') {
    window.WebInferenceService = WebInferenceService;
}

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = WebInferenceService;
}
