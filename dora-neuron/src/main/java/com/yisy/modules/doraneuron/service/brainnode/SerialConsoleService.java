package com.yisy.modules.doraneuron.service.brainnode;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Service for USB serial communication with the Raspberry Pi.
 * <p>
 * Wraps jSerialComm to provide auto-detection of the Pi's USB serial port,
 * sending and receiving commands, and monitoring Pi health via serial heartbeat.
 * Falls back to HTTP/REST communication if serial is unavailable.
 */
@Service
@Conditional({BrainNodeConfig.class})
@EnableConfigurationProperties(BrainNodeConfig.class)
public class SerialConsoleService implements SerialPortDataListener {

    private static final Logger log = LoggerFactory.getLogger(SerialConsoleService.class);

    private final BrainNodeConfig brainNodeConfig;

    private SerialPort serialPort;
    private boolean connected = false;
    private final BlockingQueue<String> receiveBuffer = new LinkedBlockingQueue<>(1000);
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        log.info("Initializing SerialConsoleService for Raspberry Pi communication");
        if (!brainNodeConfig.isSerialEnabled()) {
            log.info("Serial communication disabled, using HTTP fallback");
            return;
        }
        running = true;
        connect();
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down SerialConsoleService");
        running = false;
        disconnect();
    }

    /**
     * Connect to the Raspberry Pi via USB serial port.
     *
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            String portName = brainNodeConfig.getSerialPort();

            // Auto-detect if no manual port specified
            if (portName == null || portName.isEmpty()) {
                if (brainNodeConfig.isSerialPortAutoDetect()) {
                    portName = autoDetectPort();
                    if (portName == null) {
                        log.warn("No USB serial port auto-detected for Raspberry Pi");
                        return false;
                    }
                } else {
                    log.warn("Serial port not configured and auto-detect is disabled");
                    return false;
                }
            }

            serialPort = SerialPort.getCommPort(portName);
            if (serialPort == null) {
                log.error("Serial port {} not found", portName);
                return false;
            }

            serialPort.setBaudRate(brainNodeConfig.getSerialBaudRate());
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_NONE);
            serialPort.setReadTimeout(5000);
            serialPort.setWriteTimeout(5000);

            if (!serialPort.openPort(2000)) {
                log.error("Failed to open serial port {}", portName);
                return false;
            }

            // Disable DTR/RTS to prevent Pi reset on connection
            serialPort.disableDTR();
            serialPort.disableRTS();

            // Register data listener
            serialPort.addDataListener(this);

            connected = true;
            log.info("Connected to Raspberry Pi via serial port {} at {} baud",
                    portName, brainNodeConfig.getSerialBaudRate());
            return true;
        } catch (Exception e) {
            log.error("Error connecting to Raspberry Pi via serial: {}", e.getMessage(), e);
            connected = false;
            return false;
        }
    }

    /**
     * Disconnect from the Raspberry Pi serial port.
     */
    public void disconnect() {
        if (serialPort != null) {
            try {
                serialPort.removeDataListener();
                if (serialPort.isOpen()) {
                    serialPort.closePort();
                }
                serialPort = null;
            } catch (Exception e) {
                log.warn("Error disconnecting from serial port: {}", e.getMessage());
            }
        }
        connected = false;
        log.info("Disconnected from Raspberry Pi serial port");
    }

    /**
     * Auto-detect the Raspberry Pi's USB serial port.
     *
     * @return the detected port name or null if not found
     */
    public String autoDetectPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> availablePorts = new ArrayList<>();

        for (SerialPort port : ports) {
            String portName = port.getSystemPortName();
            availablePorts.add(portName);
            log.debug("Found serial port: {} ({})", portName, port.getDescription());

            // Look for common Pi serial port descriptors
            String description = port.getDescription().toLowerCase();
            String portNameLower = portName.toLowerCase();

            if (description.contains("usb") && description.contains("serial")) {
                log.info("Auto-detected Raspberry Pi serial port: {}", portName);
                return portName;
            }
            if (description.contains("cp2102") || description.contains("ch340") || description.contains("ftdi")) {
                log.info("Auto-detected USB-to-serial adapter: {} ({})", portName, description);
                return portName;
            }
            // On Windows, look for COM ports that might be the Pi
            if (portNameLower.startsWith("com")) {
                // Prefer higher COM numbers as they're more likely to be USB devices
                if (portNameLower.contains("usb") || portNameLower.contains("virtual")) {
                    log.info("Auto-detected potential Pi serial port: {}", portName);
                    return portName;
                }
            }
        }

        // Fallback: return the first available port if nothing matched
        if (!availablePorts.isEmpty()) {
            log.warn("No Pi-specific port found, returning first available: {}", availablePorts.get(0));
            return availablePorts.get(0);
        }

        log.warn("No serial ports available for auto-detection");
        return null;
    }

    /**
     * Get all available serial ports.
     *
     * @return list of available port names
     */
    public List<String> getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portNames = new ArrayList<>();
        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortName());
        }
        return portNames;
    }

    /**
     * Send a command to the Raspberry Pi via serial.
     *
     * @param command the command to send
     * @return true if the command was sent successfully
     */
    public boolean sendCommand(String command) {
        if (!connected || serialPort == null) {
            log.warn("Not connected to Raspberry Pi via serial, attempting HTTP fallback");
            return sendCommandViaHttp(command);
        }

        try {
            byte[] data = (command + "\n").getBytes(StandardCharsets.UTF_8);
            int bytesWritten = serialPort.writeBytes(data, data.length);
            log.debug("Sent command to Pi: {} ({} bytes)", command, bytesWritten);
            return bytesWritten == data.length;
        } catch (Exception e) {
            log.error("Error sending command via serial: {}", e.getMessage(), e);
            // Fallback to HTTP
            log.info("Falling back to HTTP for command: {}", command);
            return sendCommandViaHttp(command);
        }
    }

    /**
     * Send a command to the Raspberry Pi via HTTP fallback.
     *
     * @param command the command to send
     * @return true if the command was sent successfully
     */
    public boolean sendCommandViaHttp(String command) {
        try {
            String piUrl = brainNodeConfig.getPiCoordinatorUrl();
            String url = piUrl + "/api/v1/serial/command";

            log.info("Sending command via HTTP fallback: {} to {}", command, url);

            // Simple HTTP POST using Java built-in HTTP client
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "text/plain")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(command))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            log.info("HTTP fallback response: {} - {}", response.statusCode(), response.body());
            return success;
        } catch (Exception e) {
            log.error("HTTP fallback failed for command {}: {}", command, e.getMessage());
            return false;
        }
    }

    /**
     * Read a response from the Raspberry Pi.
     *
     * @param timeout the maximum time to wait for a response
     * @param unit the time unit for the timeout
     * @return the response string or null if timeout
     */
    public String readResponse(long timeout, TimeUnit unit) {
        try {
            String response = receiveBuffer.poll(timeout, unit);
            if (response != null) {
                log.debug("Received response from Pi: {}", response);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for response from Pi");
            return null;
        }
    }

    /**
     * Send a command and read the response.
     *
     * @param command the command to send
     * @param timeout the maximum time to wait for a response
     * @param unit the time unit for the timeout
     * @return the response string or null if timeout
     */
    public String executeCommand(String command, long timeout, TimeUnit unit) {
        if (!sendCommand(command)) {
            log.warn("Failed to send command: {}", command);
            return null;
        }
        return readResponse(timeout, unit);
    }

    /**
     * Check if the Raspberry Pi is reachable via serial heartbeat.
     *
     * @return true if the Pi responded to the heartbeat
     */
    @Scheduled(fixedDelayString = "${brainnode.serial-heartbeat-interval:10000}")
    public boolean checkPiHealth() {
        if (!connected || !running) {
            return false;
        }

        try {
            String heartbeat = "HEARTBEAT";
            if (!sendCommand(heartbeat)) {
                log.warn("Serial heartbeat failed");
                return false;
            }

            String response = readResponse(5, TimeUnit.SECONDS);
            if (response != null && response.contains("OK")) {
                log.debug("Pi heartbeat OK");
                return true;
            }

            log.warn("Pi heartbeat timeout or invalid response: {}", response);
            return false;
        } catch (Exception e) {
            log.error("Pi health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the service is connected to the Raspberry Pi.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && serialPort != null && serialPort.isOpen();
    }

    /**
     * Get the current connection status.
     *
     * @return a status string describing the connection
     */
    public String getConnectionStatus() {
        if (isConnected()) {
            return String.format("Connected to %s at %d baud",
                    serialPort.getSystemPortName(), serialPort.getBaudRate());
        }
        return "Disconnected - using HTTP fallback";
    }

    /**
     * jSerialComm data listener callback.
     */
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            try {
                int bytesAvailable = serialPort.bytesAvailable();
                if (bytesAvailable > 0) {
                    byte[] buffer = new byte[bytesAvailable];
                    int bytesRead = serialPort.readBytes(buffer, buffer.length);
                    if (bytesRead > 0) {
                        String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        // Trim and add to receive buffer
                        String trimmed = data.trim();
                        if (!trimmed.isEmpty()) {
                            // Only keep the last 1000 messages to prevent memory issues
                            if (receiveBuffer.size() >= receiveBuffer.remainingCapacity()) {
                                receiveBuffer.poll(); // Remove oldest
                            }
                            receiveBuffer.offer(trimmed);
                            log.trace("Received data from Pi: {}", trimmed);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error reading serial data: {}", e.getMessage());
            }
        }
    }
}
