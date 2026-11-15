/**
 * AiInferenceAdapter — Base class for game adapters to integrate with the Neuron AI brain network.
 *
 * Provides a unified interface for game-specific adapters to:
 * - Request AI-assisted decisions using local or server-side inference
 * - Collect gameplay experience data for model retraining
 * - Handle game-specific state encoding and action decoding
 * - Gracefully degrade when AI services are unavailable
 *
 * Usage:
 *   class StarTrekAdapter extends AiInferenceAdapter {
 *     encodeGameState(gameState) { /* convert to 64-dim vector */ }
 *     decodeAction(actionIndex, availableActions) { /* convert to game action */ }
 *   }
 */
class AiInferenceAdapter {
    /**
     * Create a new AI inference adapter.
     * @param {Object} options - Configuration options
     * @param {string} options.gameId - Unique game identifier (e.g., 'star-trek-4x', 'star-wars', 'fps')
     * @param {ElectronNeuronService|WebInferenceService} [options.neuronService] - Optional pre-configured neuron service
     * @param {string} [options.neuronBaseUrl] - Neuron server URL (used if neuronService not provided)
     * @param {boolean} [options.enableAi=true] - Enable AI-assisted decisions
     * @param {number} [options.aiFallbackThreshold=0.3] - Confidence threshold for fallback to heuristic
     */
    constructor(options = {}) {
        this.gameId = options.gameId || 'unknown';
        this.neuronService = options.neuronService || null;
        this.neuronBaseUrl = options.neuronBaseUrl || 'http://localhost:8090';
        this.enableAi = options.enableAi !== false;
        this.aiFallbackThreshold = options.aiFallbackThreshold || 0.3;

        // Statistics
        this.totalAiRequests = 0;
        this.totalAiResponses = 0;
        this.totalFallbacks = 0;
        this.totalExperienceCollected = 0;

        // Experience buffer for this adapter
        this.experienceBuffer = [];
        this.maxExperienceBuffer = 100;
    }

    /**
     * Initialize the adapter and connect to AI services.
     */
    async initialize() {
        console.log(`[AiInferenceAdapter] Initializing for game: ${this.gameId}`);

        if (!this.neuronService) {
            this.neuronService = await this._createNeuronService();
        }

        if (this.neuronService && typeof this.neuronService.initialize === 'function') {
            await this.neuronService.initialize();
        }

        console.log(`[AiInferenceAdapter] Initialized for ${this.gameId}`, {
            aiEnabled: this.enableAi,
            hasNeuronService: !!this.neuronService
        });
    }

    /**
     * Create the appropriate neuron service based on the runtime environment.
     * @returns {Promise<ElectronNeuronService|WebInferenceService|null>}
     * @private
     */
    async _createNeuronService() {
        // Electron environment
        if (typeof window !== 'undefined' && window.ElectronNeuronService) {
            return new window.ElectronNeuronService({
                neuronBaseUrl: this.neuronBaseUrl
            });
        }

        // Browser/PWA environment
        if (typeof window !== 'undefined' && window.WebInferenceService) {
            return new window.WebInferenceService({
                neuronBaseUrl: this.neuronBaseUrl
            });
        }

        // Node.js environment (server-side)
        if (typeof module !== 'undefined' && module.exports) {
            try {
                const ElectronNeuronService = require('./electron-neuron-service');
                return new ElectronNeuronService({
                    neuronBaseUrl: this.neuronBaseUrl
                });
            } catch (err) {
                console.log(`[AiInferenceAdapter] No neuron service available for ${this.gameId}`);
                return null;
            }
        }

        return null;
    }

    /**
     * Request AI-assisted decision for the current game state.
     * @param {Object} gameState - Game-specific state object
     * @param {string[]} availableActions - List of available action names
     * @returns {Promise<Object>} Decision result with action, confidence, and metadata
     */
    async requestAiDecision(gameState, availableActions) {
        if (!this.enableAi) {
            this.totalFallbacks++;
            return this._heuristicDecision(gameState, availableActions);
        }

        this.totalAiRequests++;

        // Encode game state to 64-dim vector
        const inputState = this.encodeGameState(gameState);

        // Request AI inference
        let result;
        if (this.neuronService && typeof this.neuronService.requestAiAssist === 'function') {
            result = await this.neuronService.requestAiAssist(inputState, availableActions);
        } else if (this.neuronService && typeof this.neuronService.predict === 'function') {
            result = await this.neuronService.predict(inputState, availableActions);
        } else {
            this.totalFallbacks++;
            return this._heuristicDecision(gameState, availableActions);
        }

        this.totalAiResponses++;

        // Check confidence threshold
        if (result.confidence < this.aiFallbackThreshold) {
            console.log(`[AiInferenceAdapter] Low confidence (${result.confidence}), using fallback`);
            this.totalFallbacks++;
            return this._heuristicDecision(gameState, availableActions);
        }

        // Decode action index to game-specific action
        const decodedAction = this.decodeAction(result.predictedAction, availableActions);

        return {
            action: decodedAction,
            actionIndex: result.predictedAction,
            confidence: result.confidence,
            probabilities: result.probabilities,
            source: result.source || 'unknown',
            latencyMs: result.latencyMs || 0,
            modelVersion: result.modelVersion || 0,
            gameId: this.gameId
        };
    }

    /**
     * Submit gameplay experience for AI model retraining.
     * @param {Object} gameState - Game state at decision time
     * @param {string} action - Action taken
     * @param {number} reward - Outcome reward (positive = good, negative = bad)
     */
    submitExperience(gameState, action, reward) {
        const experience = {
            gameId: this.gameId,
            state: this.encodeGameState(gameState),
            action: action,
            reward: reward,
            timestamp: Date.now()
        };

        if (this.experienceBuffer.length >= this.maxExperienceBuffer) {
            this.experienceBuffer.shift();
        }
        this.experienceBuffer.push(experience);
        this.totalExperienceCollected++;

        // Forward to neuron service if available
        if (this.neuronService && typeof this.neuronService.addExperience === 'function') {
            this.neuronService.addExperience(
                this.encodeGameState(gameState),
                action,
                reward
            );
        }
    }

    /**
     * Flush experience buffer to AI service.
     */
    async flushExperience() {
        if (this.experienceBuffer.length === 0) return;

        const batch = [...this.experienceBuffer];
        this.experienceBuffer = [];

        if (this.neuronService && typeof this.neuronService.flushExperience === 'function') {
            await this.neuronService.flushExperience();
        } else if (this.neuronService) {
            // Add each experience individually
            for (const exp of batch) {
                if (typeof this.neuronService.addExperience === 'function') {
                    this.neuronService.addExperience(exp.state, exp.action, exp.reward);
                }
            }
        }
    }

    /**
     * Encode game-specific state to 64-dim input vector for ONNX model.
     * Override this method in subclasses for game-specific encoding.
     * @param {Object} gameState - Game-specific state object
     * @returns {number[]} 64-dim normalized state vector
     */
    encodeGameState(gameState) {
        // Default: create 64-dim zero vector
        // Override in subclasses for game-specific encoding
        const vector = new Array(64).fill(0);

        if (gameState) {
            // Extract numeric values from game state
            const values = Object.values(gameState).filter(v => typeof v === 'number');
            for (let i = 0; i < Math.min(values.length, 64); i++) {
                // Normalize to [-1, 1] range
                vector[i] = Math.max(-1, Math.min(1, values[i]));
            }
        }

        return vector;
    }

    /**
     * Decode action index to game-specific action.
     * Override this method in subclasses for game-specific action mapping.
     * @param {number} actionIndex - Predicted action index
     * @param {string[]} availableActions - Available action names
     * @returns {string} Game-specific action name
     */
    decodeAction(actionIndex, availableActions) {
        if (availableActions && actionIndex < availableActions.length) {
            return availableActions[actionIndex];
        }
        return `action_${actionIndex}`;
    }

    /**
     * Heuristic fallback decision when AI is unavailable or confidence is low.
     * Override this method in subclasses for game-specific heuristics.
     * @param {Object} gameState - Game state
     * @param {string[]} availableActions - Available actions
     * @returns {Object} Fallback decision
     */
    _heuristicDecision(gameState, availableActions) {
        // Simple heuristic: random selection from available actions
        const actionIndex = availableActions && availableActions.length > 0
            ? Math.floor(Math.random() * availableActions.length)
            : 0;

        const action = this.decodeAction(actionIndex, availableActions);

        return {
            action: action,
            actionIndex: actionIndex,
            confidence: 0.2,
            probabilities: null,
            source: 'heuristic',
            latencyMs: 0,
            modelVersion: -1,
            gameId: this.gameId
        };
    }

    /**
     * Get adapter status and statistics.
     * @returns {Object} Status information
     */
    getStatus() {
        return {
            gameId: this.gameId,
            aiEnabled: this.enableAi,
            hasNeuronService: !!this.neuronService,
            totalAiRequests: this.totalAiRequests,
            totalAiResponses: this.totalAiResponses,
            totalFallbacks: this.totalFallbacks,
            totalExperienceCollected: this.totalExperienceCollected,
            experienceBuffered: this.experienceBuffer.length
        };
    }

    /**
     * Destroy the adapter and clean up resources.
     */
    async destroy() {
        console.log(`[AiInferenceAdapter] Destroying for ${this.gameId}`);

        if (this.neuronService && typeof this.neuronService.destroy === 'function') {
            await this.neuronService.destroy();
        }

        this.experienceBuffer = [];
        this.neuronService = null;
    }
}

// Expose globally
if (typeof window !== 'undefined') {
    window.AiInferenceAdapter = AiInferenceAdapter;
}

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AiInferenceAdapter;
}
