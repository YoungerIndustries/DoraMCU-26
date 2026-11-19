package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerialConsoleServiceTest {

    private SerialConsoleService service;

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @BeforeEach
    void setUp() {
        service = new SerialConsoleService(brainNodeConfig);
    }

    @Test
    @DisplayName("Service should be created without errors")
    void serviceCreation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("Is connected should return false initially")
    void isConnectedInitially() {
        assertFalse(service.isConnected());
    }

    @Test
    @DisplayName("Get connection status should return disconnected message")
    void getConnectionStatusDisconnected() {
        String status = service.getConnectionStatus();
        assertEquals("Disconnected - using HTTP fallback", status);
    }

    @Test
    @DisplayName("Read response should return null on timeout")
    void readResponseTimeout() {
        String result = service.readResponse(100, TimeUnit.MILLISECONDS);
        assertNull(result);
    }

    @Test
    @DisplayName("Send command should return false when not connected")
    void sendCommandNotConnected() {
        when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");

        // Not connected, should attempt HTTP fallback which will fail
        boolean result = service.sendCommand("test command");

        // HTTP fallback will fail since no server is running
        assertFalse(result);
    }

    @Test
    @DisplayName("Execute command should return null when not connected")
    void executeCommandNotConnected() {
        when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");

        String result = service.executeCommand("test", 100, TimeUnit.MILLISECONDS);
        assertNull(result);
    }

    @Test
    @DisplayName("Disconnect should not throw when not connected")
    void disconnectNotConnected() {
        assertDoesNotThrow(() -> service.disconnect());
    }

    @Test
    @DisplayName("Get available ports should not throw")
    void getAvailablePorts() {
        // jSerialComm may not be available in all test environments,
        // so we just verify the method doesn't crash the test
        try {
            var ports = service.getAvailablePorts();
            assertNotNull(ports);
        } catch (UnsatisfiedLinkError e) {
            // Native library not available — skip gracefully
        }
    }
}
