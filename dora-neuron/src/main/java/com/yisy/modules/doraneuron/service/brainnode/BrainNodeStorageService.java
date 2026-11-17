package com.yisy.modules.doraneuron.service.brainnode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.GameStateEntity;
import com.yisy.modules.doraneuron.model.brainnode.ExperienceSampleEntity;
import com.yisy.modules.doraneuron.model.brainnode.AiModelCacheEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for persistent local storage on the Brain Node.
 * <p>
 * Manages H2/SQLite database for game state persistence, experience sample storage,
 * and AI model caching, synchronized with the Pi via DataSyncService.
 */
@Service
@Conditional({BrainNodeConfig.class})
@EnableConfigurationProperties(BrainNodeConfig.class)
public class BrainNodeStorageService {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeStorageService.class);

    private final BrainNodeConfig brainNodeConfig;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /** In-memory cache for frequently accessed game states */
    private final Map<String, GameStateEntity> gameStateCache = new ConcurrentHashMap<>();

    /** In-memory cache for AI model metadata */
    private final Map<String, AiModelCacheEntity> modelCache = new ConcurrentHashMap<>();

    public BrainNodeStorageService(BrainNodeConfig brainNodeConfig, ObjectMapper objectMapper) {
        this.brainNodeConfig = brainNodeConfig;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing BrainNodeStorageService with storage type: {}", brainNodeConfig.getStorageType());
        ensureStorageDirectory();
        log.info("Brain Node storage initialized at: {}", brainNodeConfig.getStoragePath());
    }

    /**
     * Ensure the storage directory exists.
     */
    private void ensureStorageDirectory() {
        try {
            Path path = Paths.get(brainNodeConfig.getStoragePath());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created storage directory: {}", path);
            }
        } catch (IOException e) {
            log.error("Failed to create storage directory: {}", e.getMessage(), e);
        }
    }

    // ── Game State Persistence ────────────────────────────────────────────────

    /**
     * Save game state to the local database.
     *
     * @param gameId the unique game identifier
     * @param gameStateJson the game state as JSON
     * @return the saved game state entity
     */
    @Transactional
    public GameStateEntity saveGameState(String gameId, String gameStateJson) {
        GameStateEntity entity = getOrCreateGameState(gameId);
        entity.setGameStateJson(gameStateJson);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setSyncedWithPi(false);

        entityManager.merge(entity);
        gameStateCache.put(gameId, entity);

        log.debug("Saved game state for game: {} ({} bytes)", gameId, gameStateJson.length());
        return entity;
    }

    /**
     * Get game state from cache or database.
     *
     * @param gameId the unique game identifier
     * @return the game state entity or empty if not found
     */
    @Transactional
    public Optional<GameStateEntity> getGameState(String gameId) {
        // Check cache first
        GameStateEntity cached = gameStateCache.get(gameId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Fall back to database
        TypedQuery<GameStateEntity> query = entityManager
                .createQuery("SELECT g FROM GameStateEntity g WHERE g.gameId = :gameId", GameStateEntity.class);
        query.setParameter("gameId", gameId);
        List<GameStateEntity> results = query.getResultList();

        if (!results.isEmpty()) {
            GameStateEntity entity = results.get(0);
            gameStateCache.put(gameId, entity);
            return Optional.of(entity);
        }

        return Optional.empty();
    }

    /**
     * Delete game state from database and cache.
     *
     * @param gameId the unique game identifier
     * @return true if the game state was deleted
     */
    @Transactional
    public boolean deleteGameState(String gameId) {
        TypedQuery<GameStateEntity> query = entityManager
                .createQuery("SELECT g FROM GameStateEntity g WHERE g.gameId = :gameId", GameStateEntity.class);
        query.setParameter("gameId", gameId);
        List<GameStateEntity> results = query.getResultList();

        if (!results.isEmpty()) {
            GameStateEntity entity = results.get(0);
            entityManager.remove(entity);
            gameStateCache.remove(gameId);
            log.info("Deleted game state for game: {}", gameId);
            return true;
        }

        log.warn("Game state not found for deletion: {}", gameId);
        return false;
    }

    /**
     * Get all game states.
     *
     * @return list of all game state entities
     */
    @Transactional
    public List<GameStateEntity> getAllGameStates() {
        TypedQuery<GameStateEntity> query = entityManager
                .createQuery("SELECT g FROM GameStateEntity g ORDER BY g.updatedAt DESC", GameStateEntity.class);
        return query.getResultList();
    }

    /**
     * Get unsynced game states that need to be synced with the Pi.
     *
     * @return list of unsynced game state entities
     */
    @Transactional
    public List<GameStateEntity> getUnsyncedGameStates() {
        TypedQuery<GameStateEntity> query = entityManager
                .createQuery("SELECT g FROM GameStateEntity g WHERE g.syncedWithPi = false", GameStateEntity.class);
        return query.getResultList();
    }

    /**
     * Mark a game state as synced with the Pi.
     *
     * @param gameId the unique game identifier
     */
    @Transactional
    public void markGameStateSynced(String gameId) {
        getGameState(gameId).ifPresent(entity -> {
            entity.setSyncedWithPi(true);
            entityManager.merge(entity);
            log.debug("Marked game state as synced: {}", gameId);
        });
    }

    // ── Experience Sample Storage ─────────────────────────────────────────────

    /**
     * Save an experience sample to the local database.
     *
     * @param gameId the game this sample belongs to
     * @param inputJson the input data as JSON
     * @param outputJson the output data as JSON
     * @param reward the reward value
     * @return the saved experience sample entity
     */
    @Transactional
    public ExperienceSampleEntity saveExperienceSample(String gameId, String inputJson, String outputJson, double reward) {
        ExperienceSampleEntity entity = new ExperienceSampleEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setGameId(gameId);
        entity.setInputJson(inputJson);
        entity.setOutputJson(outputJson);
        entity.setReward(reward);
        entity.setTimestamp(LocalDateTime.now());
        entity.setSyncedWithPi(false);

        entityManager.persist(entity);
        log.debug("Saved experience sample for game: {} (reward: {})", gameId, reward);
        return entity;
    }

    /**
     * Save multiple experience samples in a batch.
     *
     * @param gameId the game these samples belong to
     * @param inputJsons list of input data as JSON
     * @param outputJsons list of output data as JSON
     * @param rewards list of reward values
     * @return the number of samples saved
     */
    @Transactional
    public int saveExperienceSamples(String gameId, List<String> inputJsons, List<String> outputJsons, List<Double> rewards) {
        int count = 0;
        for (int i = 0; i < inputJsons.size(); i++) {
            if (i >= outputJsons.size() || i >= rewards.size()) break;
            saveExperienceSample(gameId, inputJsons.get(i), outputJsons.get(i), rewards.get(i));
            count++;
        }
        log.info("Saved {} experience samples for game: {}", count, gameId);
        return count;
    }

    /**
     * Get experience samples for a game.
     *
     * @param gameId the unique game identifier
     * @param limit maximum number of samples to return
     * @return list of experience sample entities
     */
    @Transactional
    public List<ExperienceSampleEntity> getExperienceSamples(String gameId, int limit) {
        TypedQuery<ExperienceSampleEntity> query = entityManager.createQuery(
                "SELECT e FROM ExperienceSampleEntity e WHERE e.gameId = :gameId ORDER BY e.timestamp DESC",
                ExperienceSampleEntity.class);
        query.setParameter("gameId", gameId);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Get unsynced experience samples.
     *
     * @return list of unsynced experience sample entities
     */
    @Transactional
    public List<ExperienceSampleEntity> getUnsyncedExperienceSamples() {
        TypedQuery<ExperienceSampleEntity> query = entityManager.createQuery(
                "SELECT e FROM ExperienceSampleEntity e WHERE e.syncedWithPi = false",
                ExperienceSampleEntity.class);
        return query.getResultList();
    }

    /**
     * Mark experience samples as synced.
     *
     * @param sampleIds list of sample IDs to mark as synced
     */
    @Transactional
    public void markExperienceSamplesSynced(List<String> sampleIds) {
        for (String sampleId : sampleIds) {
            TypedQuery<ExperienceSampleEntity> query = entityManager.createQuery(
                    "SELECT e FROM ExperienceSampleEntity e WHERE e.id = :id",
                    ExperienceSampleEntity.class);
            query.setParameter("id", sampleId);
            query.getResultList().forEach(entity -> {
                entity.setSyncedWithPi(true);
                entityManager.merge(entity);
            });
        }
        log.debug("Marked {} experience samples as synced", sampleIds.size());
    }

    /**
     * Clear old experience samples beyond the buffer size.
     */
    @Transactional
    public void clearOldExperienceSamples() {
        int bufferSize = brainNodeConfig.getExperienceBufferSize();
        List<ExperienceSampleEntity> allSamples = entityManager.createQuery(
                "SELECT e FROM ExperienceSampleEntity e ORDER BY e.timestamp DESC",
                ExperienceSampleEntity.class).getResultList();

        if (allSamples.size() > bufferSize) {
            int toDelete = allSamples.size() - bufferSize;
            for (int i = bufferSize; i < allSamples.size(); i++) {
                entityManager.remove(allSamples.get(i));
            }
            log.info("Cleared {} old experience samples", toDelete);
        }
    }

    // ── AI Model Caching ──────────────────────────────────────────────────────

    /**
     * Cache AI model metadata locally.
     *
     * @param modelId the unique model identifier
     * @param modelPath the path to the model file
     * @param version the model version
     * @param metadataJson additional metadata as JSON
     * @return the cached model entity
     */
    @Transactional
    public AiModelCacheEntity cacheAiModel(String modelId, String modelPath, int version, String metadataJson) {
        AiModelCacheEntity entity = getOrCreateModel(modelId);
        entity.setModelPath(modelPath);
        entity.setVersion(version);
        entity.setMetadataJson(metadataJson);
        entity.setCachedAt(LocalDateTime.now());

        entityManager.merge(entity);
        modelCache.put(modelId, entity);

        log.debug("Cached AI model: {} (version: {})", modelId, version);
        return entity;
    }

    /**
     * Get cached AI model metadata.
     *
     * @param modelId the unique model identifier
     * @return the cached model entity or empty if not found
     */
    @Transactional
    public Optional<AiModelCacheEntity> getCachedAiModel(String modelId) {
        AiModelCacheEntity cached = modelCache.get(modelId);
        if (cached != null) {
            return Optional.of(cached);
        }

        TypedQuery<AiModelCacheEntity> query = entityManager
                .createQuery("SELECT m FROM AiModelCacheEntity m WHERE m.modelId = :modelId", AiModelCacheEntity.class);
        query.setParameter("modelId", modelId);
        List<AiModelCacheEntity> results = query.getResultList();

        if (!results.isEmpty()) {
            AiModelCacheEntity entity = results.get(0);
            modelCache.put(modelId, entity);
            return Optional.of(entity);
        }

        return Optional.empty();
    }

    /**
     * Remove cached AI model.
     *
     * @param modelId the unique model identifier
     */
    @Transactional
    public void removeCachedAiModel(String modelId) {
        TypedQuery<AiModelCacheEntity> query = entityManager
                .createQuery("SELECT m FROM AiModelCacheEntity m WHERE m.modelId = :modelId", AiModelCacheEntity.class);
        query.setParameter("modelId", modelId);
        query.getResultList().forEach(entity -> {
            entityManager.remove(entity);
            modelCache.remove(modelId);
        });
        log.info("Removed cached AI model: {}", modelId);
    }

    /**
     * Get all cached AI models.
     *
     * @return list of all cached model entities
     */
    @Transactional
    public List<AiModelCacheEntity> getAllCachedModels() {
        TypedQuery<AiModelCacheEntity> query = entityManager
                .createQuery("SELECT m FROM AiModelCacheEntity m ORDER BY m.cachedAt DESC", AiModelCacheEntity.class);
        return query.getResultList();
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private GameStateEntity getOrCreateGameState(String gameId) {
        return getGameState(gameId).orElseGet(() -> {
            GameStateEntity entity = new GameStateEntity();
            entity.setGameId(gameId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setSyncedWithPi(false);
            return entity;
        });
    }

    private AiModelCacheEntity getOrCreateModel(String modelId) {
        return getCachedAiModel(modelId).orElseGet(() -> {
            AiModelCacheEntity entity = new AiModelCacheEntity();
            entity.setModelId(modelId);
            entity.setCachedAt(LocalDateTime.now());
            return entity;
        });
    }

    /**
     * Get storage statistics.
     *
     * @return map of storage statistics
     */
    @Transactional
    public Map<String, Object> getStorageStats() {
        long gameStateCount = entityManager.createQuery(
                "SELECT COUNT(g) FROM GameStateEntity g", Long.class).getSingleResult();
        long experienceCount = entityManager.createQuery(
                "SELECT COUNT(e) FROM ExperienceSampleEntity e", Long.class).getSingleResult();
        long modelCount = entityManager.createQuery(
                "SELECT COUNT(m) FROM AiModelCacheEntity m", Long.class).getSingleResult();
        long unsyncedGameStates = entityManager.createQuery(
                "SELECT COUNT(g) FROM GameStateEntity g WHERE g.syncedWithPi = false", Long.class).getSingleResult();
        long unsyncedExperience = entityManager.createQuery(
                "SELECT COUNT(e) FROM ExperienceSampleEntity e WHERE e.syncedWithPi = false", Long.class).getSingleResult();

        return Map.of(
                "gameStateCount", gameStateCount,
                "experienceCount", experienceCount,
                "modelCount", modelCount,
                "unsyncedGameStates", unsyncedGameStates,
                "unsyncedExperience", unsyncedExperience,
                "storagePath", brainNodeConfig.getStoragePath(),
                "storageType", brainNodeConfig.getStorageType()
        );
    }
}
