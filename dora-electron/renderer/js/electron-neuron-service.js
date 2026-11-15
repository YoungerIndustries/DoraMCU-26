/**
 * ElectronNeuronService — Integrates the Electron desktop client with the Neuron AI brain network.
 *
 * Features:
 * - Local ONNX inference using onnxruntime-node (when available)
 * - Neuron server registration and heartbeat
 * - Experience data collection and periodic upload
 * - Co-server promotion capability
 * - AI assistance requests for game adapters
 * - Mesh network participation for distributed inference
 *
 * Usage:
 *   const neuronService = new ElectronNeuronService({ neuronBaseUrl: 'http://localhost:8090' });
 *   await neuronService.initialize();
 *   const decision = await neuronService.requestAiAssist(gameState, availableActions);
 */

class ElectronNeuronService {
    /**
     * Create a new Electron Neuron service.
     * @param {Object} options - Configuration options
     * @param {string} [options.neuronBaseUrl='http://localhost:8090'] - Neuron server URL
     * @param {string} [options.nodeId] - Unique node ID (auto-generated if not provided)
     * @param {number} [options.heartbeatInterval=30000] - Heartbeat interval in ms
     * @param {number} [options.experienceFlushInterval=30000] - Experience flush interval in ms
     * @param {number} [options.maxExperienceBuffer=500] - Max experience buffer size
     * @param {string} [options.modelPath] - Path to local ONNX model file
     * @param {boolean} [options.enableLocalInference=true] - Enable local ONNX inference
     * @param {boolean} [options.enableCoServer=true] - Enable co-server promotion
     */
    constructor(options = {}) {
        this.neuronBaseUrl = options.neuronBaseUrl || 'http://localhost:8090';
        this.nodeId = options.nodeId || `electron-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        this.nodeName = `Electron-${process.platform}`;
        this.heartbeatInterval = options.heartbeatInterval || 30000;
        this.experienceFlushInterval = options.experienceFlushInterval || 30000;
        this.maxExperienceBuffer = options.maxExperienceBuffer || 500;
        this.modelPath = options.modelPath || null;
        this.enableLocalInference = options.enableLocalInference !== false;
        this.enableCoServer = options.enableCoServer !== false;

        // State
        this.connected = false;
        this.isCoServer = false;
        this.modelVersion = 0;
        this.inferenceSession = null;
        this.runtime = null;

        // Experience buffer
        this.experienceBuffer = [];
        this.totalInferences = 0;
        this.totalLocalInferences = 0;
        this.totalExperienceUploaded = 0;

        // Timers
        this.heartbeatTimer = null;
        this.experienceFlushTimer = null;
        this.coServerCheckTimer = null;

        // Event handlers
        this.onAiResultHandlers = [];
        this.onConnectionChangeHandlers = [];
    }

    /**
     * Initialize the service: load ONNX runtime, register with Neuron, start timers.
     */
    async initialize() {
        console.log('[ElectronNeuron] Initializing...', this.nodeId);

        // Try to load ONNX Runtime
        if (this.enableLocalInference) {
            await this._loadOnnxRuntime();
        }

        // Register with Neuron
        await this.registerWithNeuron();

        // Start periodic tasks
        this._startHeartbeat();
        this._startExperienceFlush();
        if (this.enableCoServer) {
            this._startCoServerCheck();
        }

        console.log('[ElectronNeuron] Initialized', {
            nodeId: this.nodeId,
            connected: this.connected,
            localInference: this.inferenceSession !== null
        });
    }

    /**
     * Load ONNX Runtime Node.js for local inference.
     */
    async _loadOnnxRuntime() {
        try {
            // Dynamic import to handle cases where package is not installed
            const ort = require('onnxruntime-node');
            this.runtime = ort;

            if (this.modelPath) {
                this.inferenceSession = new ort.InferenceSession();
                await this.inferenceSession.initialize(this.modelPath, {
                    intraOpNumThreads: Math.min(4, require('os').cpus().length),
                    executionMode: ort.ExecutionMode.ORT_SEQUENTIAL
                });
                console.log('[ElectronNeuron] ONNX model loaded:', this.modelPath);
            } else {
                console.log('[ElectronNeuron] ONNX Runtime loaded, no model path specified');
            }
        } catch (err) {
            console.log('[ElectronNeuron] ONNX Runtime not available, using server-side inference:', err.message);
            this.inferenceSession = null;
        }
    }

    /**
     * Register this Electron client with the Neuron server.
     */
    async registerWithNeuron() {
        const url = `${this.neuronBaseUrl}/api/v1/neuron/register`;
        const os = require('os');

        const payload = {
            nodeId: this.nodeId,
            nodeName: this.nodeName,
            clientType: 'ELECTRON',
            supportsInference: this.inferenceSession !== null,
            canBeCoServer: this.enableCoServer,
            cpuCores: os.cpus().length,
            availableMemoryMb: Math.floor(os.totalmem() / (1024 * 1024)),
            hasGpu: this._hasGpu(),
            platform: process.platform,
            modelVersion: this.modelVersion
        };

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const data = await response.json();
                this.connected = true;
                this.modelVersion = data.modelVersion || 0;
                this._notifyConnectionChange(true);
                console.log('[ElectronNeuron] Registered with Neuron, model version:', this.modelVersion);
                return true;
            }
        } catch (err) {
            if (this.connected) {
                console.warn('[ElectronNeuron] Registration failed:', err.message);
            }
        }
        return false;
    }

    /**
     * Run local ONNX inference if available, otherwise request from server.
     * @param {number[]} inputState - 64-dim input state vector
     * @param {string[]} [availableActions] - Optional list of available action names
     * @returns {Promise<Object>} Inference result with predictedAction, confidence, etc.
     */
    async runInference(inputState, availableActions) {
        this.totalInferences++;

        // Try local inference first
        if (this.inferenceSession && this.enableLocalInference) {
            try {
                const result = await this._runLocalInference(inputState);
                this.totalLocalInferences++;
                result.source = 'local';
                return result;
            } catch (err) {
                console.warn('[ElectronNeuron] Local inference failed, falling back to server:', err.message);
            }
        }

        // Fall back to server-side inference
        return this._requestServerInference(inputState, availableActions);
    }

    /**
     * Run local ONNX inference.
     */
    async _runLocalInference(inputState) {
        const feeds = {
            [this.inferenceSession.inputNames[0]]:
                new this.runtime.Tensor('float32', inputState, [1, inputState.length])
        };

        const results = await this.inferenceSession.run(feeds);
        const outputNames = this.inferenceSession.outputNames;
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
            latencyMs: 0,
            source: 'local'
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
                data.source = 'server';
                return data;
            }
        } catch (err) {
            console.warn('[ElectronNeuron] Server inference failed:', err.message);
        }

        // Fallback to heuristic
        return this._heuristicFallback(inputState, availableActions);
    }

    /**
     * Request AI assistance for a specific task (mirrors Unity NeuronClientService).
     * @param {number[]} gameState - Game state vector
     * @param {string[]} availableActions - Available actions
     * @returns {Promise<Object>} AI assistance result
     */
    async requestAiAssist(gameState, availableActions) {
        return this.runInference(gameState, availableActions);
    }

    /**
     * Quick prediction without full state (for simple decisions).
     * @param {number[]} partialState - Partial state vector
     * @returns {Promise<number>} Predicted action index
     */
    async quickPredict(partialState) {
        const result = await this.runInference(partialState);
        return result.predictedAction;
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
    }

    /**
     * Upload experience buffer to Neuron server.
     */
    async _flushExperience() {
        if (!this.connected || this.experienceBuffer.length === 0) return;

        const batch = [...this.experienceBuffer];
        this.experienceBuffer = [];

        const url = `${this.neuronBaseUrl}/api/v1/neuron/experience`;

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(batch)
            });

            if (response.ok) {
                this.totalExperienceUploaded += batch.length;
                console.log(`[ElectronNeuron] Uploaded ${batch.length} experience samples`);
            }
        } catch (err) {
            console.warn('[ElectronNeuron] Experience upload failed:', err.message);
            // Re-add to buffer for retry
            this.experienceBuffer.unshift(...batch);
        }
    }

    /**
     * Request consensus voting from the mesh network.
     * @param {number[]} inputState - Input state for consensus
     * @param {number} minVoters - Minimum number of voters required
     * @returns {Promise<Object>} Consensus result
     */
    async requestConsensus(inputState, minVoters = 3) {
        const url = `${this.neuronBaseUrl}/api/v1/mesh/consensus`;

        const payload = {
            nodeId: this.nodeId,
            inputState: inputState,
            minVoters: minVoters
        };

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (err) {
            console.warn('[ElectronNeuron] Consensus request failed:', err.message);
        }

        // Fall back to single inference
        return this.runInference(inputState);
    }

    /**
     * Check if we should promote to co-server.
     */
    async _checkCoServerPromotion() {
        if (this.isCoServer || !this.connected) return;

        const url = `${this.neuronBaseUrl}/api/v1/neuron/co-server/check`;

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nodeId: this.nodeId })
            });

            if (response.ok) {
                const data = await response.json();
                if (data.promote) {
                    this.isCoServer = true;
                    console.log('[ElectronNeuron] Promoted to co-server');
                }
            }
        } catch (err) {
            // Silent fail for co-server check
        }
    }

    /**
     * Heuristic fallback when no AI service is available.
     */
    _heuristicFallback(inputState, availableActions) {
        // Simple heuristic: use first half of state as "positive" indicators
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

    /**
     * Detect if GPU is available.
     */
    _hasGpu() {
        try {
            const os = require('os');
            const platform = os.platform();
            // Basic GPU detection - in production, use a more robust method
            if (platform === 'darwin') return true; // macOS has integrated GPU
            // For Windows/Linux, check environment variables or use native module
            return !!process.env.GPU_AVAILABLE;
        } catch {
            return false;
        }
    }

    // ── Timer Management ──────────────────────────────────────────────────────

    _startHeartbeat() {
        this._stopHeartbeat();
        this.heartbeatTimer = setInterval(async () => {
            if (!this.connected) {
                await this.registerWithNeuron();
                return;
            }

            const url = `${this.neuronBaseUrl}/api/v1/neuron/heartbeat`;
            const payload = {
                nodeId: this.nodeId,
                modelVersion: this.modelVersion,
                experienceCount: this.experienceBuffer.length,
                isCoServer: this.isCoServer
            };

            try {
                await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            } catch (err) {
                console.warn('[ElectronNeuron] Heartbeat failed:', err.message);
                this.connected = false;
                this._notifyConnectionChange(false);
            }
        }, this.heartbeatInterval);
    }

    _stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    _startExperienceFlush() {
        this._stopExperienceFlush();
        this.experienceFlushTimer = setInterval(() => {
            this._flushExperience();
        }, this.experienceFlushInterval);
    }

    _stopExperienceFlush() {
        if (this.experienceFlushTimer) {
            clearInterval(this.experienceFlushTimer);
            this.experienceFlushTimer = null;
        }
    }

    _startCoServerCheck() {
        this._stopCoServerCheck();
        this.coServerCheckTimer = setInterval(() => {
            this._checkCoServerPromotion();
        }, 60000);
    }

    _stopCoServerCheck() {
        if (this.coServerCheckTimer) {
            clearInterval(this.coServerCheckTimer);
            this.coServerCheckTimer = null;
        }
    }

    // ── Event Handlers ─────────────────────────────────────────────────────────

    onAiResult(handler) {
        this.onAiResultHandlers.push(handler);
    }

    onConnectionChange(handler) {
        this.onConnectionChangeHandlers.push(handler);
    }

    _notifyConnectionChange(connected) {
        for (const handler of this.onConnectionChangeHandlers) {
            try {
                handler(connected);
            } catch (err) {
                console.error('[ElectronNeuron] Connection change handler error:', err);
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
            connected: this.connected,
            isCoServer: this.isCoServer,
            modelVersion: this.modelVersion,
            localInferenceAvailable: this.inferenceSession !== null,
            totalInferences: this.totalInferences,
            totalLocalInferences: this.totalLocalInferences,
            totalExperienceUploaded: this.totalExperienceUploaded,
            experienceBuffered: this.experienceBuffer.length
        };
    }

    /**
     * Destroy the service and clean up resources.
     */
    async destroy() {
        console.log('[ElectronNeuron] Destroying...');
        this._stopHeartbeat();
        this._stopExperienceFlush();
        this._stopCoServerCheck();

        if (this.inferenceSession) {
            try {
                this.inferenceSession.release();
            } catch (err) {
                // Ignore release errors
            }
            this.inferenceSession = null;
        }

        this.connected = false;
        this.isCoServer = false;
        this.experienceBuffer = [];
    }
}

// Expose globally
window.ElectronNeuronService = ElectronNeuronService;
module.exports = ElectronNeuronService;
