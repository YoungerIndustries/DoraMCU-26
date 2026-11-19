package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.brainnode.BrainNodeStatus;
import com.yisy.modules.doraneuron.service.DataSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BrainNodeIntegrationTest {

    private BrainNodeService service;

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @Mock
    private NeuronConfig neuronConfig;

    @Mock
    private SerialConsoleService serialConsoleService;

    @Mock
    private BrainNodeStorageService storageService;

    @Mock
    private BrainNodeAiService aiService;

    @BeforeEach
    void setUp() {
        service = new BrainNodeService(brainNodeConfig, neuronConfig, serialConsoleService, storageService, aiService, null);

        lenient().when(brainNodeConfig.isEnabled()).thenReturn(true);
        lenient().when(brainNodeConfig.getNodeType()).thenReturn("BRAIN_NODE");
        lenient().when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");
        lenient().when(brainNodeConfig.isAutoFailoverEnabled()).thenReturn(true);
        lenient().when(brainNodeConfig.getStorageType()).thenReturn("H2");
        lenient().when(brainNodeConfig.getStoragePath()).thenReturn("backups/brain-node/");

        lenient().when(neuronConfig.getNodeId()).thenReturn("test-node-1");
        lenient().when(neuronConfig.getNodeName()).thenReturn("Test Brain Node");

        lenient().when(serialConsoleService.isConnected()).thenReturn(false);
        lenient().when(serialConsoleService.getConnectionStatus()).thenReturn("Disconnected - using HTTP fallback");

        lenient().when(aiService.isReady()).thenReturn(true);
        lenient().when(aiService.getCurrentModelVersion()).thenReturn(1L);
        lenient().when(aiService.getInferenceStats()).thenReturn(Map.of(
                "inferenceCount", 0L,
                "errorCount", 0L,
                "currentModelVersion", 1L
        ));

        lenient().when(storageService.getStorageStats()).thenReturn(Map.of(
                "gameStateCount", 0L,
                "experienceCount", 0L,
                "modelCount", 0L
        ));

        lenient().when(storageService.getUnsyncedGameStates()).thenReturn(java.util.Collections.emptyList());
        lenient().when(storageService.getUnsyncedExperienceSamples()).thenReturn(java.util.Collections.emptyList());
    }

    @Test
    @DisplayName("Init should initialize all components")
    void init() {
        service.init();

        assertTrue(service.isInitialized());
    }

    @Test
    @DisplayName("Init should handle disabled Brain Node")
    void initDisabled() {
        when(brainNodeConfig.isEnabled()).thenReturn(false);
        service.init();

        assertFalse(service.isInitialized());
    }

    @Test
    @DisplayName("Get status should return comprehensive status")
    void getStatus() {
        service.init();

        BrainNodeStatus status = service.getStatus();

        assertNotNull(status);
        assertEquals("test-node-1", status.getNodeId());
        assertEquals("Test Brain Node", status.getNodeName());
        assertEquals("BRAIN_NODE", status.getNodeType());
        assertTrue(status.isEnabled());
        assertEquals("Disconnected - using HTTP fallback", status.getSerialConnectionStatus());
        assertTrue(status.isAiReady());
        assertEquals(1L, status.getCurrentModelVersion());
        assertEquals("http://localhost:8084", status.getPiCoordinatorUrl());
        assertTrue(status.isAutoFailoverEnabled());
        assertEquals("H2", status.getStorageType());
        assertEquals("backups/brain-node/", status.getStoragePath());
    }

    @Test
    @DisplayName("Check Pi health should not throw")
    void checkPiHealthUnreachable() {
        // Serial is disconnected, HTTP health check may succeed or fail depending on environment
        // The important thing is the method does not throw
        assertDoesNotThrow(() -> service.checkPiHealth());
    }

    @Test
    @DisplayName("Trigger sync should work without DataSyncService")
    void triggerSyncWithoutDataSyncService() {
        boolean success = service.triggerSync();

        // Should succeed with local storage sync even without DataSyncService
        assertTrue(success);
    }

    @Test
    @DisplayName("Get sync status should return local status without DataSyncService")
    void getSyncStatusWithoutDataSyncService() {
        Map<String, Object> syncStatus = service.getSyncStatus();

        assertNotNull(syncStatus);
        assertFalse((Boolean) syncStatus.get("dataSyncServiceAvailable"));
        assertEquals(0, syncStatus.get("unsyncedGameStates"));
        assertEquals(0, syncStatus.get("unsyncedExperienceSamples"));
    }

    @Test
    @DisplayName("Get sync status with DataSyncService")
    void getSyncStatusWithDataSyncService() {
        DataSyncService dataSyncService = mock(DataSyncService.class);
        when(dataSyncService.getSyncStats()).thenReturn(Map.of(
                "queueSize", 5,
                "vectorClock", Map.of("node-1", 10L),
                "syncInProgress", false,
                "serverReachable", true,
                "isCoServer", false
        ));

        BrainNodeService serviceWithDataSync = new BrainNodeService(
                brainNodeConfig, neuronConfig, serialConsoleService, storageService, aiService, dataSyncService);

        Map<String, Object> syncStatus = serviceWithDataSync.getSyncStatus();

        assertNotNull(syncStatus);
        assertTrue((Boolean) syncStatus.get("dataSyncServiceAvailable"));
        assertEquals(5, syncStatus.get("queueSize"));
        assertTrue((Boolean) syncStatus.get("serverReachable"));
    }

    @Test
    @DisplayName("Send command to Pi should delegate to serial service")
    void sendCommandToPi() {
        when(serialConsoleService.executeCommand(anyString(), anyLong(), any()))
                .thenReturn("OK");

        String response = service.sendCommandToPi("test command");

        assertEquals("OK", response);
        verify(serialConsoleService).executeCommand("test command", 5L, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Restart serial connection should reconnect")
    void restartSerialConnection() {
        when(serialConsoleService.connect()).thenReturn(true);

        boolean success = service.restartSerialConnection();

        assertTrue(success);
        verify(serialConsoleService).disconnect();
        verify(serialConsoleService).connect();
    }

    @Test
    @DisplayName("Scheduled health check should update reachability")
    void scheduledHealthCheck() {
        service.init();

        // Should not throw
        assertDoesNotThrow(() -> service.scheduledPiHealthCheck());
    }

    @Test
    @DisplayName("Destroy should set initialized to false")
    void destroy() {
        service.init();
        assertTrue(service.isInitialized());

        service.destroy();

        assertFalse(service.isInitialized());
    }
}
