package com.yisy.modules.doraneuron.service.brainnode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.GameStateEntity;
import com.yisy.modules.doraneuron.model.brainnode.ExperienceSampleEntity;
import com.yisy.modules.doraneuron.model.brainnode.AiModelCacheEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrainNodeStorageServiceTest {

    private BrainNodeStorageService service;

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = new BrainNodeStorageService(brainNodeConfig, objectMapper);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        when(brainNodeConfig.getStorageType()).thenReturn("H2");
        when(brainNodeConfig.getStoragePath()).thenReturn("backups/brain-node/");
        when(brainNodeConfig.getExperienceBufferSize()).thenReturn(5000);
    }

    @Test
    @DisplayName("Save game state should create and persist entity")
    void saveGameState() {
        String gameId = "test-game-1";
        String gameStateJson = "{\"level\": 1}";

        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of()));
        when(entityManager.merge(any(GameStateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GameStateEntity result = service.saveGameState(gameId, gameStateJson);

        assertNotNull(result);
        assertEquals(gameId, result.getGameId());
        assertEquals(gameStateJson, result.getGameStateJson());
        assertFalse(result.isSyncedWithPi());
        verify(entityManager).merge(any(GameStateEntity.class));
    }

    @Test
    @DisplayName("Get game state should return entity from database")
    void getGameState() {
        String gameId = "test-game-1";
        GameStateEntity entity = new GameStateEntity();
        entity.setGameId(gameId);
        entity.setGameStateJson("{\"level\": 2}");

        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity)));

        Optional<GameStateEntity> result = service.getGameState(gameId);

        assertTrue(result.isPresent());
        assertEquals(gameId, result.get().getGameId());
    }

    @Test
    @DisplayName("Get game state should return empty when not found")
    void getGameStateNotFound() {
        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of()));

        Optional<GameStateEntity> result = service.getGameState("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Delete game state should remove entity")
    void deleteGameState() {
        String gameId = "test-game-1";
        GameStateEntity entity = new GameStateEntity();
        entity.setGameId(gameId);

        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity)));

        boolean result = service.deleteGameState(gameId);

        assertTrue(result);
        verify(entityManager).remove(entity);
    }

    @Test
    @DisplayName("Delete game state should return false when not found")
    void deleteGameStateNotFound() {
        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of()));

        boolean result = service.deleteGameState("nonexistent");

        assertFalse(result);
    }

    @Test
    @DisplayName("Save experience sample should create and persist entity")
    void saveExperienceSample() {
        String gameId = "test-game-1";
        String inputJson = "{\"action\": \"move\"}";
        String outputJson = "{\"result\": \"success\"}";
        double reward = 0.5;

        when(entityManager.persist(any(ExperienceSampleEntity.class))).thenAnswer(inv -> {
            ExperienceSampleEntity e = inv.getArgument(0);
            e.setId("sample-1");
            return null;
        });

        ExperienceSampleEntity result = service.saveExperienceSample(gameId, inputJson, outputJson, reward);

        assertNotNull(result);
        assertEquals(gameId, result.getGameId());
        assertEquals(inputJson, result.getInputJson());
        assertEquals(outputJson, result.getOutputJson());
        assertEquals(reward, result.getReward());
        assertFalse(result.isSyncedWithPi());
    }

    @Test
    @DisplayName("Save experience samples should save multiple samples")
    void saveExperienceSamples() {
        List<String> inputs = Arrays.asList("{\"a\": 1}", "{\"a\": 2}");
        List<String> outputs = Arrays.asList("{\"b\": 1}", "{\"b\": 2}");
        List<Double> rewards = Arrays.asList(0.5, 0.8);

        when(entityManager.persist(any(ExperienceSampleEntity.class))).thenAnswer(inv -> {
            ExperienceSampleEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID().toString());
            return null;
        });

        int count = service.saveExperienceSamples("game-1", inputs, outputs, rewards);

        assertEquals(2, count);
        verify(entityManager, times(2)).persist(any(ExperienceSampleEntity.class));
    }

    @Test
    @DisplayName("Get experience samples should return limited results")
    void getExperienceSamples() {
        ExperienceSampleEntity sample1 = new ExperienceSampleEntity();
        sample1.setId("1");
        sample1.setGameId("game-1");

        ExperienceSampleEntity sample2 = new ExperienceSampleEntity();
        sample2.setId("2");
        sample2.setGameId("game-1");

        when(entityManager.createQuery(anyString(), eq(ExperienceSampleEntity.class)))
                .thenReturn(mockTypedQuery(List.of(sample1, sample2)));

        List<ExperienceSampleEntity> results = service.getExperienceSamples("game-1", 10);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Cache AI model should create and persist entity")
    void cacheAiModel() {
        String modelId = "model-v1";
        String modelPath = "models/brain-node/model.onnx";
        int version = 1;
        String metadataJson = "{\"accuracy\": 0.95}";

        when(entityManager.createQuery(anyString(), eq(AiModelCacheEntity.class)))
                .thenReturn(mockTypedQuery(List.of()));
        when(entityManager.merge(any(AiModelCacheEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AiModelCacheEntity result = service.cacheAiModel(modelId, modelPath, version, metadataJson);

        assertNotNull(result);
        assertEquals(modelId, result.getModelId());
        assertEquals(modelPath, result.getModelPath());
        assertEquals(version, result.getVersion());
        verify(entityManager).merge(any(AiModelCacheEntity.class));
    }

    @Test
    @DisplayName("Get cached AI model should return entity")
    void getCachedAiModel() {
        String modelId = "model-v1";
        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId(modelId);
        entity.setModelPath("models/model.onnx");
        entity.setVersion(1);

        when(entityManager.createQuery(anyString(), eq(AiModelCacheEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity)));

        Optional<AiModelCacheEntity> result = service.getCachedAiModel(modelId);

        assertTrue(result.isPresent());
        assertEquals(modelId, result.get().getModelId());
    }

    @Test
    @DisplayName("Remove cached AI model should delete entity")
    void removeCachedAiModel() {
        String modelId = "model-v1";
        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId(modelId);

        when(entityManager.createQuery(anyString(), eq(AiModelCacheEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity)));

        service.removeCachedAiModel(modelId);

        verify(entityManager).remove(entity);
    }

    @Test
    @DisplayName("Get storage stats should return statistics map")
    void getStorageStats() {
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(mockTypedQuery(5L))
                .thenReturn(mockTypedQuery(100L))
                .thenReturn(mockTypedQuery(3L))
                .thenReturn(mockTypedQuery(2L))
                .thenReturn(mockTypedQuery(10L));

        Map<String, Object> stats = service.getStorageStats();

        assertNotNull(stats);
        assertEquals(5L, stats.get("gameStateCount"));
        assertEquals(100L, stats.get("experienceCount"));
        assertEquals(3L, stats.get("modelCount"));
    }

    @Test
    @DisplayName("Mark game state synced should update entity")
    void markGameStateSynced() {
        String gameId = "test-game-1";
        GameStateEntity entity = new GameStateEntity();
        entity.setGameId(gameId);
        entity.setSyncedWithPi(false);

        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity)));
        when(entityManager.merge(any(GameStateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markGameStateSynced(gameId);

        assertTrue(entity.isSyncedWithPi());
        verify(entityManager).merge(entity);
    }

    @Test
    @DisplayName("Get unsynced game states should return only unsynced")
    void getUnsyncedGameStates() {
        GameStateEntity entity1 = new GameStateEntity();
        entity1.setGameId("game-1");
        entity1.setSyncedWithPi(false);

        when(entityManager.createQuery(anyString(), eq(GameStateEntity.class)))
                .thenReturn(mockTypedQuery(List.of(entity1)));

        List<GameStateEntity> unsynced = service.getUnsyncedGameStates();

        assertEquals(1, unsynced.size());
        assertFalse(unsynced.get(0).isSyncedWithPi());
    }

    private <T> TypedQuery<T> mockTypedQuery(List<T> results) {
        TypedQuery<T> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(results);
        when(query.getSingleResult()).thenReturn(results.isEmpty() ? null : results.get(0));
        return query;
    }
}
