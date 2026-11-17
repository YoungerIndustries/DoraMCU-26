package com.yisy.modules.doraneuron.service.brainnode;

import com.fazecast.jSerialComm.SerialPort;
import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerialConsoleServiceTest {

    private SerialConsoleService service;

    @Mock
    private BrainNodeConfig brainNodeConfig;

    @BeforeEach
    void setUp() {
        service = new SerialConsoleService();
        // Use reflection to set the config since it's final
        try {
            var field = SerialConsoleService.class.getDeclaredField("brainNodeConfig");
            field.setAccessible(true);
            field.set(service, brainNodeConfig);
        } catch (Exception e) {
            fail("Failed to set config: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Auto-detect should return null when no ports available")
    void autoDetectNoPorts() {
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[0]);

            String result = service.autoDetectPort();

            assertNull(result);
            mocked.verify(SerialPort::getCommPorts, times(1));
        }
    }

    @Test
    @DisplayName("Auto-detect should find USB serial port")
    void autoDetectUsbSerialPort() {
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            SerialPort mockPort = mock(SerialPort.class);
            when(mockPort.getSystemPortName()).thenReturn("COM4");
            when(mockPort.getDescription()).thenReturn("USB Serial Port");

            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            String result = service.autoDetectPort();

            assertEquals("COM4", result);
        }
    }

    @Test
    @DisplayName("Auto-detect should find CH340 adapter")
    void autoDetectCh340Adapter() {
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            SerialPort mockPort = mock(SerialPort.class);
            when(mockPort.getSystemPortName()).thenReturn("COM5");
            when(mockPort.getDescription()).thenReturn("CH340 USB to Serial");

            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort});

            String result = service.autoDetectPort();

            assertEquals("COM5", result);
        }
    }

    @Test
    @DisplayName("Get available ports should return list of port names")
    void getAvailablePorts() {
        try (MockedStatic<SerialPort> mocked = mockStatic(SerialPort.class)) {
            SerialPort mockPort1 = mock(SerialPort.class);
            when(mockPort1.getSystemPortName()).thenReturn("COM4");

            SerialPort mockPort2 = mock(SerialPort.class);
            when(mockPort2.getSystemPortName()).thenReturn("COM5");

            mocked.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{mockPort1, mockPort2});

            List<String> ports = service.getAvailablePorts();

            assertEquals(2, ports.size());
            assertTrue(ports.contains("COM4"));
            assertTrue(ports.contains("COM5"));
        }
    }

    @Test
    @DisplayName("Send command should return false when not connected")
    void sendCommandNotConnected() {
        when(brainNodeConfig.isSerialEnabled()).thenReturn(true);
        when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");

        // Not connected, should attempt HTTP fallback which will fail
        boolean result = service.sendCommand("test command");

        // HTTP fallback will fail since no server is running
        assertFalse(result);
    }

    @Test
    @DisplayName("Read response should return null on timeout")
    void readResponseTimeout() {
        String result = service.readResponse(100, TimeUnit.MILLISECONDS);
        assertNull(result);
    }

    @Test
    @DisplayName("Execute command should return null when not connected")
    void executeCommandNotConnected() {
        when(brainNodeConfig.isSerialEnabled()).thenReturn(true);
        when(brainNodeConfig.getPiCoordinatorUrl()).thenReturn("http://localhost:8084");

        String result = service.executeCommand("test", 100, TimeUnit.MILLISECONDS);
        assertNull(result);
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
    @DisplayName("Serial event should handle data available")
    void serialEventDataAvailable() {
        SerialPort mockPort = mock(SerialPort.class);
        when(mockPort.bytesAvailable()).thenReturn(5);

        // Simulate receiving "OK\n"
        byte[] testData = "OK\n".getBytes();
        when(mockPort.readBytes(any(byte[].class), anyInt())).thenReturn(3);

        // We can't easily test the queue without setting up the serialPort field,
        // but we can verify no exception is thrown
        var event = new com.fazecast.jSerialComm.SerialPortEvent(
                mockPort,
                com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE,
                System.currentTimeMillis()
        );

        // Should not throw
        assertDoesNotThrow(() -> service.serialEvent(event));
    }
}
