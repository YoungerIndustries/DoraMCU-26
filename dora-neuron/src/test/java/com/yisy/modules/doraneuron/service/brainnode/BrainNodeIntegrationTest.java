package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.brainnode.BrainNodeStatus;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private InferenceEngine engine;

    @BeforeEach
    void setUp() {
        service = new BrainNodeService(brainNodeConfig, neuronConfig, serialConsoleService, storageService, aiService);

        when(brainNodeConfig.isEnabled()).thenReturn(true);
        when(brainNodeConfig.getNodeType()).thenReturn("BRAIN_NODE");
        when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");
        when(brainNodeConfig.isAutoFailoverEnabled()).thenReturn(true);
        when(brainNodeConfig.getStorageType()).thenReturn("H2");
        when(brainNodeConfig.getStoragePath()).thenReturn("backups/brain-node/");

        when(neuronConfig.getNodeId()).thenReturn("test-node-1");
        when(neuronConfig.getNodeName()).thenReturn("Test Brain Node");

        when(serialConsoleService.isConnected()).thenReturn(false);
        when(serialConsoleService.getConnectionStatus()).thenReturn("Disconnected - using HTTP fallback");

        when(aiService.isReady()).thenReturn(true);
        when(aiService.getCurrentModelVersion()).thenReturn(1L);
        when(aiService.getInferenceStats()).thenReturn(Map.of(
                "inferenceCount", 0L,
                "errorCount", 0L,
                "currentModelVersion", 1L
        ));

        when(storageService.getStorageStats()).thenReturn(Map.of(
                "gameStateCount", 0L,
                "experienceCount", 0L,
                "modelCount", 0L
        ));
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
    @DisplayName("Check Pi health should return false when unreachable")
    void checkPiHealthUnreachable() {
        boolean reachable = service.checkPiHealth();

        assertFalse(reachable);
        assertFalse(service.isPiReachable());
    }

    @Test
    @DisplayName("Trigger sync should fail when Pi unreachable")
    void triggerSyncPiUnreachable() {
        boolean success = service.triggerSync();

        assertFalse(success);
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
