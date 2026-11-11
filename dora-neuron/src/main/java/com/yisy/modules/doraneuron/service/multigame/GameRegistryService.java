package com.yisy.modules.doraneuron.service.multigame;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.model.multigame.GameStatus;
import com.yisy.modules.doraneuron.protocol.multigame.RegisterGameRequest;
import com.yisy.modules.doraneuron.protocol.multigame.UpdateGameRequest;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import com.yisy.modules.doraneuron.util.OnnxInferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages multiple game profiles on a single Neuron.
 *
 * Each game has its own GameProfile with isolated ONNX model, game server URL,
 * and inference engine. This service handles registration, updates, removal,
 * and lifecycle management of game profiles.
 *
 * The service maintains a map of gameId → GameProfile and creates a separate
 * InferenceEngine instance for each game that has an ONNX model configured.
 */
@Service
public class GameRegistryService {

    private static final Logger log = LoggerFactory.getLogger(GameRegistryService.class);

    private final NeuronConfig config;

    /** Registry of game ID → GameProfile. */
    private final ConcurrentHashMap<String, GameProfile> games = new ConcurrentHashMap<>();

    /** Map of game ID → InferenceEngine for games with loaded models. */
    private final ConcurrentHashMap<String, InferenceEngine> engines = new ConcurrentHashMap<>();

    public GameRegistryService(NeuronConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        // If a default game server URL is configured, register it as the default game
        String defaultGameUrl = config.getGameServerUrl();
        if (defaultGameUrl != null && !defaultGameUrl.isEmpty()) {
            RegisterGameRequest defaultRequest = new RegisterGameRequest();
            defaultRequest.setGameId("default");
            defaultRequest.setGameName(config.getPortalTitle());
            defaultRequest.setGameServerUrl(defaultGameUrl);
            if (config.getOnnxModelPath() != null && !config.getOnnxModelPath().isEmpty()) {
                defaultRequest.setOnnxModelPath(config.getOnnxModelPath());
            }
            defaultRequest.setInferenceThreads(config.getInferenceThreads());
            defaultRequest.setConfidenceThreshold(config.getConfidenceThreshold());
            registerGame(defaultRequest);
            log.info("Registered default game from NeuronConfig");
        }
    }

    /**
     * Register a new game profile.
     *
     * @param request the registration request
     * @return the created game profile
     */
    public GameProfile registerGame(RegisterGameRequest request) {
        String gameId = request.getGameId();
        if (gameId == null || gameId.isEmpty()) {
            throw new IllegalArgumentException("gameId is required");
        }
        if (games.containsKey(gameId)) {
            log.warn("Game '{}' is already registered", gameId);
            return games.get(gameId);
        }

        GameProfile profile = new GameProfile(request.getGameId(), request.getGameName(),
                request.getGameServerUrl());
        profile.setOnnxModelPath(request.getOnnxModelPath());
        profile.setFeatureVectorSize(request.getFeatureVectorSize());
        profile.setOutputActionSize(request.getOutputActionSize());
        profile.setConfidenceThreshold(request.getConfidenceThreshold());
        profile.setInferenceThreads(request.getInferenceThreads());

        games.put(gameId, profile);
        log.info("Registered game: {} ({})", gameId, profile.getGameName());

        // Load ONNX model if path is provided
        if (profile.getOnnxModelPath() != null && !profile.getOnnxModelPath().isEmpty()) {
            loadEngineForGame(profile);
        } else {
            profile.activate();
        }

        return profile;
    }

    /**
     * Update an existing game profile.
     *
     * @param gameId the game to update
     * @param request the update request
     * @return the updated profile, or empty if not found
     */
    public Optional<GameProfile> updateGame(String gameId, UpdateGameRequest request) {
        GameProfile profile = games.get(gameId);
        if (profile == null) {
            return Optional.empty();
        }

        if (request.getGameName() != null) {
            profile.setGameName(request.getGameName());
        }
        if (request.getGameServerUrl() != null) {
            profile.setGameServerUrl(request.getGameServerUrl());
        }
        if (request.getFeatureVectorSize() > 0) {
            profile.setFeatureVectorSize(request.getFeatureVectorSize());
        }
        if (request.getOutputActionSize() > 0) {
            profile.setOutputActionSize(request.getOutputActionSize());
        }
        if (request.getConfidenceThreshold() > 0) {
            profile.setConfidenceThreshold(request.getConfidenceThreshold());
        }
        if (request.getInferenceThreads() > 0) {
            profile.setInferenceThreads(request.getInferenceThreads());
        }

        // Handle model path change
        if (request.getOnnxModelPath() != null && !request.getOnnxModelPath().isEmpty()) {
            if (!request.getOnnxModelPath().equals(profile.getOnnxModelPath())) {
                profile.setOnnxModelPath(request.getOnnxModelPath());
                reloadEngineForGame(profile);
            }
        }

        // Handle pause/resume
        if (request.isPauseGame()) {
            profile.setStatus(GameStatus.PAUSED);
        } else if (profile.getStatus() == GameStatus.PAUSED) {
            profile.setStatus(GameStatus.ACTIVE);
        }

        log.info("Updated game: {}", gameId);
        return Optional.of(profile);
    }

    /**
     * Remove a game profile and clean up resources.
     *
     * @param gameId the game to remove
     * @return true if the game was removed
     */
    public boolean removeGame(String gameId) {
        GameProfile profile = games.remove(gameId);
        if (profile == null) {
            return false;
        }

        profile.setStatus(GameStatus.REMOVED);

        // Close and remove inference engine
        InferenceEngine engine = engines.remove(gameId);
        if (engine != null) {
            try {
                engine.close();
                log.info("Closed inference engine for game: {}", gameId);
            } catch (Exception e) {
                log.warn("Error closing engine for game {}: {}", gameId, e.getMessage());
            }
        }

        log.info("Removed game: {}", gameId);
        return true;
    }

    /**
     * Get a game profile by ID.
     */
    public Optional<GameProfile> getGame(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    /**
     * Get all registered game profiles.
     */
    public List<GameProfile> getAllGames() {
        return List.copyOf(games.values());
    }

    /**
     * Get all active game profiles.
     */
    public List<GameProfile> getActiveGames() {
        return games.values().stream()
                .filter(p -> p.getStatus() == GameStatus.ACTIVE)
                .toList();
    }

    /**
     * Get the inference engine for a specific game.
     */
    public Optional<InferenceEngine> getEngine(String gameId) {
        return Optional.ofNullable(engines.get(gameId));
    }

    /**
     * Get the game server URL for a specific game.
     */
    public Optional<String> getGameServerUrl(String gameId) {
        GameProfile profile = games.get(gameId);
        if (profile != null && profile.getGameServerUrl() != null) {
            return Optional.of(profile.getGameServerUrl());
        }
        return Optional.empty();
    }

    /**
     * Get the default game (first registered or "default" ID).
     */
    public Optional<GameProfile> getDefaultGame() {
        // Prefer explicit "default" game
        Optional<GameProfile> defaultGame = getGame("default");
        if (defaultGame.isPresent()) {
            return defaultGame;
        }
        // Fall back to first registered game
        if (!games.isEmpty()) {
            return Optional.of(games.values().iterator().next());
        }
        return Optional.empty();
    }

    /**
     * Load or reload the inference engine for a game.
     */
    private void loadEngineForGame(GameProfile profile) {
        try {
            InferenceEngine engine = new OnnxInferenceEngine();
            engine.loadModel(profile.getOnnxModelPath());
            engines.put(profile.getGameId(), engine);
            profile.activate();
            log.info("Loaded ONNX engine for game: {} ({})", profile.getGameId(), profile.getOnnxModelPath());
        } catch (Exception e) {
            log.error("Failed to load ONNX engine for game {}: {}", profile.getGameId(), e.getMessage());
            profile.setStatus(GameStatus.ACTIVE); // Activate without model
        }
    }

    /**
     * Reload the inference engine for a game (model path changed).
     */
    private void reloadEngineForGame(GameProfile profile) {
        InferenceEngine existing = engines.get(profile.getGameId());
        if (existing != null) {
            try {
                existing.close();
            } catch (Exception e) {
                log.warn("Error closing old engine for {}: {}", profile.getGameId(), e.getMessage());
            }
        }
        loadEngineForGame(profile);
    }

    /**
     * Get the number of registered games.
     */
    public int getGameCount() {
        return games.size();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down GameRegistryService — closing {} engines", engines.size());
        engines.values().forEach(engine -> {
            try {
                engine.close();
            } catch (Exception e) {
                log.warn("Error closing engine: {}", e.getMessage());
            }
        });
        engines.clear();
        games.clear();
    }
}
