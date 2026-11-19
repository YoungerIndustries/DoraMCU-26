package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.AiModelCacheEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ModelManagementService Tests")
class ModelManagementServiceTest {

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @Mock
    private BrainNodeStorageService storageService;

    @Mock
    private BrainNodeAiService aiService;

    private ModelManagementService service;

    @BeforeEach
    void setUp() {
        service = new ModelManagementService(brainNodeConfig, storageService, aiService);
    }

    @Test
    @DisplayName("Deploy model should save file and cache metadata")
    void deployModel() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test-model.onnx", "application/octet-stream",
                new ByteArrayInputStream("dummy model content".getBytes())
        );

        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("test-model");
        when(storageService.cacheAiModel(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(entity);

        String modelId = service.deployModel(file, "test-model", 1, null);

        assertNotNull(modelId);
        assertEquals("test-model", modelId);
        verify(storageService).cacheAiModel(eq("test-model"), anyString(), eq(1), isNull());
    }

    @Test
    @DisplayName("Deploy model should generate ID if not provided")
    void deployModelGenerateId() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "my-model.onnx", "application/octet-stream",
                new ByteArrayInputStream("dummy model content".getBytes())
        );

        AiModelCacheEntity entity = new AiModelCacheEntity();
        when(storageService.cacheAiModel(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(entity);

        String modelId = service.deployModel(file, null, 1, null);

        assertNotNull(modelId);
        assertTrue(modelId.startsWith("my-model-"));
        verify(storageService).cacheAiModel(eq(modelId), anyString(), eq(1), isNull());
    }

    @Test
    @DisplayName("Deploy model should reject empty file")
    void deployModelRejectEmpty() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.onnx", "application/octet-stream",
                new ByteArrayInputStream(new byte[0])
        );

        assertThrows(IllegalArgumentException.class, () -> service.deployModel(file, "test", 1, null));
    }

    @Test
    @DisplayName("Deploy model should reject non-ONNX extension")
    void deployModelRejectNonOnnx() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "model.txt", "text/plain",
                new ByteArrayInputStream("not a model".getBytes())
        );

        assertThrows(IllegalArgumentException.class, () -> service.deployModel(file, "test", 1, null));
    }

    @Test
    @DisplayName("List models should return all cached models")
    void listModels() {
        AiModelCacheEntity entity1 = new AiModelCacheEntity();
        entity1.setModelId("model-1");
        entity1.setModelPath("models/brain-node/model-1.onnx");
        entity1.setVersion(1);
        entity1.setCachedAt(LocalDateTime.now());

        AiModelCacheEntity entity2 = new AiModelCacheEntity();
        entity2.setModelId("model-2");
        entity2.setModelPath("models/brain-node/model-2.onnx");
        entity2.setVersion(2);
        entity2.setCachedAt(LocalDateTime.now());

        when(storageService.getAllCachedModels()).thenReturn(List.of(entity1, entity2));

        List<Map<String, Object>> models = service.listModels();

        assertEquals(2, models.size());
        assertEquals("model-1", models.get(0).get("modelId"));
        assertEquals("model-2", models.get(1).get("modelId"));
    }

    @Test
    @DisplayName("Get model details should return model information")
    void getModelDetails() {
        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("model-1");
        entity.setModelPath("models/brain-node/model-1.onnx");
        entity.setVersion(1);
        entity.setCachedAt(LocalDateTime.now());
        entity.setMetadataJson("{\"type\":\"test\"}");

        when(storageService.getCachedAiModel("model-1")).thenReturn(Optional.of(entity));

        Map<String, Object> details = service.getModelDetails("model-1");

        assertNotNull(details);
        assertEquals("model-1", details.get("modelId"));
        assertEquals(1, details.get("version"));
        assertEquals("{\"type\":\"test\"}", details.get("metadata"));
    }

    @Test
    @DisplayName("Get model details should return null for unknown model")
    void getModelDetailsNotFound() {
        when(storageService.getCachedAiModel("unknown")).thenReturn(Optional.empty());

        Map<String, Object> details = service.getModelDetails("unknown");

        assertNull(details);
    }

    @Test
    @DisplayName("Activate model should swap into inference engine")
    void activateModel() throws IOException {
        // Create a temporary model file
        Path tempFile = Files.createTempFile("model-1", ".onnx");
        Files.write(tempFile, "dummy model".getBytes());

        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("model-1");
        entity.setModelPath(tempFile.toString());
        entity.setVersion(1);

        when(storageService.getCachedAiModel("model-1")).thenReturn(Optional.of(entity));
        doNothing().when(aiService).swapModel(anyString(), anyLong());

        boolean success = service.activateModel("model-1");

        assertTrue(success);
        assertEquals("model-1", service.getActiveModelId());
        verify(aiService).swapModel(eq(tempFile.toString()), eq(1L));

        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Activate model should fail for unknown model")
    void activateModelNotFound() {
        when(storageService.getCachedAiModel("unknown")).thenReturn(Optional.empty());

        boolean success = service.activateModel("unknown");

        assertFalse(success);
    }

    @Test
    @DisplayName("Activate model should fail when AI service unavailable")
    void activateModelNoAiService() {
        ModelManagementService noAiService = new ModelManagementService(brainNodeConfig, storageService, null);

        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("model-1");
        entity.setModelPath("models/brain-node/model-1.onnx");
        entity.setVersion(1);
        when(storageService.getCachedAiModel("model-1")).thenReturn(Optional.of(entity));

        boolean success = noAiService.activateModel("model-1");

        assertFalse(success);
    }

    @Test
    @DisplayName("Delete model should remove file and cache entry")
    void deleteModel() {
        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("model-1");
        entity.setModelPath("models/brain-node/model-1.onnx");
        entity.setVersion(1);

        when(storageService.getCachedAiModel("model-1")).thenReturn(Optional.of(entity));
        doNothing().when(storageService).removeCachedAiModel("model-1");

        boolean success = service.deleteModel("model-1");

        assertTrue(success);
        verify(storageService).removeCachedAiModel("model-1");
    }

    @Test
    @DisplayName("Delete model should fail for unknown model")
    void deleteModelNotFound() {
        when(storageService.getCachedAiModel("unknown")).thenReturn(Optional.empty());

        boolean success = service.deleteModel("unknown");

        assertFalse(success);
    }

    @Test
    @DisplayName("Delete active model should fail")
    void deleteActiveModel() throws IOException {
        // Create a temporary model file
        Path tempFile = Files.createTempFile("active-model", ".onnx");
        Files.write(tempFile, "dummy model".getBytes());

        AiModelCacheEntity entity = new AiModelCacheEntity();
        entity.setModelId("active-model");
        entity.setModelPath(tempFile.toString());
        entity.setVersion(1);
        when(storageService.getCachedAiModel("active-model")).thenReturn(Optional.of(entity));
        doNothing().when(aiService).swapModel(anyString(), anyLong());
        service.activateModel("active-model");

        // Verify active model is set
        assertEquals("active-model", service.getActiveModelId());

        // Try to delete the active model
        boolean success = service.deleteModel("active-model");

        assertFalse(success, "Cannot delete the active model");
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Create A/B test should register variants and set traffic split")
    void createAbTest() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());
        entityA.setVersion(1);

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());
        entityB.setVersion(1);

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        boolean success = service.createAbTest("model-a", "model-b", 70.0);

        assertTrue(success);
        assertTrue(service.isAbTestActive());
        verify(aiService).registerModelVariant("A", tempFileA.toString());
        verify(aiService).registerModelVariant("B", tempFileB.toString());

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Create A/B test should fail for unknown model")
    void createAbTestModelNotFound() {
        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.empty());

        boolean success = service.createAbTest("model-a", "model-b", 50.0);

        assertFalse(success);
        assertFalse(service.isAbTestActive());
    }

    @Test
    @DisplayName("Create A/B test should reject invalid traffic split")
    void createAbTestInvalidSplit() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        boolean success = service.createAbTest("model-a", "model-b", 150.0);

        assertFalse(success);
        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Get A/B test status should return metrics")
    void getAbTestStatus() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        service.createAbTest("model-a", "model-b", 50.0);

        // Record some inferences
        service.recordAbTestInference("A", 100, false);
        service.recordAbTestInference("A", 50, false);
        service.recordAbTestInference("B", 200, true);

        Map<String, Object> status = service.getAbTestStatus();

        assertNotNull(status);
        assertTrue((Boolean) status.get("active"));
        @SuppressWarnings("unchecked")
        Map<String, Object> variantMetrics = (Map<String, Object>) status.get("variantMetrics");
        @SuppressWarnings("unchecked")
        Map<String, Object> aMetrics = (Map<String, Object>) variantMetrics.get("A");
        assertEquals(2L, aMetrics.get("inferenceCount"));
        assertEquals(75.0, (Double) aMetrics.get("avgLatencyMs"), 0.01);

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Get A/B test status should return null when no active test")
    void getAbTestStatusNoActive() {
        Map<String, Object> status = service.getAbTestStatus();
        assertNull(status);
    }

    @Test
    @DisplayName("Update A/B test traffic split should update split")
    void updateAbTestTrafficSplit() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        service.createAbTest("model-a", "model-b", 50.0);

        boolean success = service.updateAbTestTrafficSplit(80.0);

        assertTrue(success);
        Map<String, Object> status = service.getAbTestStatus();
        @SuppressWarnings("unchecked")
        Map<String, Double> trafficSplit = (Map<String, Double>) status.get("trafficSplit");
        assertEquals(80.0, trafficSplit.get("A"));
        assertEquals(20.0, trafficSplit.get("B"));

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Update A/B test traffic split should fail when no active test")
    void updateAbTestTrafficSplitNoActive() {
        boolean success = service.updateAbTestTrafficSplit(80.0);
        assertFalse(success);
    }

    @Test
    @DisplayName("Stop A/B test should deactivate testing")
    void stopAbTest() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        service.createAbTest("model-a", "model-b", 50.0);
        assertTrue(service.isAbTestActive());

        service.stopAbTest();

        assertFalse(service.isAbTestActive());
        assertNull(service.getAbTestStatus());

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Validate model file should reject null file")
    void validateModelFileNull() {
        assertThrows(IllegalArgumentException.class, () -> service.validateModelFile((MultipartFile) null));
    }

    @Test
    @DisplayName("Validate model file should reject oversized file")
    void validateModelFileOversized() {
        // Mock a file that reports a size larger than 500MB limit without allocating memory
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("large.onnx");
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(501L * 1024 * 1024);

        assertThrows(IllegalArgumentException.class, () -> service.validateModelFile(file));
    }

    @Test
    @DisplayName("Select A/B test variant should respect traffic split")
    void selectAbTestVariant() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        // 100% to A
        service.createAbTest("model-a", "model-b", 100.0);

        String variant = service.selectAbTestVariant();
        assertEquals("A", variant);

        // 0% to A (100% to B)
        service.updateAbTestTrafficSplit(0.0);
        variant = service.selectAbTestVariant();
        assertEquals("B", variant);

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Select A/B test variant should return A when no active test")
    void selectAbTestVariantNoActive() {
        String variant = service.selectAbTestVariant();
        assertEquals("A", variant);
    }

    @Test
    @DisplayName("Record A/B test inference should track metrics")
    void recordAbTestInference() throws IOException {
        Path tempFileA = Files.createTempFile("model-a", ".onnx");
        Path tempFileB = Files.createTempFile("model-b", ".onnx");
        Files.write(tempFileA, "model A".getBytes());
        Files.write(tempFileB, "model B".getBytes());

        AiModelCacheEntity entityA = new AiModelCacheEntity();
        entityA.setModelId("model-a");
        entityA.setModelPath(tempFileA.toString());

        AiModelCacheEntity entityB = new AiModelCacheEntity();
        entityB.setModelId("model-b");
        entityB.setModelPath(tempFileB.toString());

        when(storageService.getCachedAiModel("model-a")).thenReturn(Optional.of(entityA));
        when(storageService.getCachedAiModel("model-b")).thenReturn(Optional.of(entityB));

        service.createAbTest("model-a", "model-b", 50.0);

        service.recordAbTestInference("A", 100, false);
        service.recordAbTestInference("A", 200, true);
        service.recordAbTestInference("B", 50, false);

        Map<String, Object> status = service.getAbTestStatus();
        @SuppressWarnings("unchecked")
        Map<String, Object> variantMetrics = (Map<String, Object>) status.get("variantMetrics");

        @SuppressWarnings("unchecked")
        Map<String, Object> aMetrics = (Map<String, Object>) variantMetrics.get("A");
        assertEquals(2L, aMetrics.get("inferenceCount"));
        assertEquals(150.0, (Double) aMetrics.get("avgLatencyMs"), 0.01);
        assertEquals(1L, aMetrics.get("errorCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> bMetrics = (Map<String, Object>) variantMetrics.get("B");
        assertEquals(1L, bMetrics.get("inferenceCount"));
        assertEquals(0L, bMetrics.get("errorCount"));

        Files.deleteIfExists(tempFileA);
        Files.deleteIfExists(tempFileB);
    }

    @Test
    @DisplayName("Record A/B test inference should be no-op when no active test")
    void recordAbTestInferenceNoActive() {
        // Should not throw
        service.recordAbTestInference("A", 100, false);
        assertNull(service.getAbTestStatus());
    }
}
