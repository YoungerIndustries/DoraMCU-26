package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
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
 * <p>
 * Uses reflection to load jSerialComm classes lazily, so the bean can be created
 * even when jSerialComm native libraries are unavailable (e.g., on Java 27).
 */
@Service
@ConditionalOnBean(BrainNodeConfig.class)
@EnableConfigurationProperties(BrainNodeConfig.class)
public class SerialConsoleService {

    private static final Logger log = LoggerFactory.getLogger(SerialConsoleService.class);

    private final BrainNodeConfig brainNodeConfig;

    public SerialConsoleService(BrainNodeConfig brainNodeConfig) {
        this.brainNodeConfig = brainNodeConfig;
    }

    private Object serialPort;
    private boolean connected = false;
    private boolean jSerialCommAvailable = false;
    private final BlockingQueue<String> receiveBuffer = new LinkedBlockingQueue<>(1000);
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        log.info("Initializing SerialConsoleService for Raspberry Pi communication");
        
        // Check if jSerialComm is available
        if (!checkJSerialCommAvailable()) {
            log.warn("jSerialComm library not available, using HTTP fallback only");
            return;
        }
        
        if (!brainNodeConfig.isSerialEnabled()) {
            log.info("Serial communication disabled, using HTTP fallback");
            return;
        }
        running = true;
        connect();
    }

    /**
     * Check if jSerialComm library is available using reflection.
     * This method also tests if the native library can be loaded, not just if the class exists.
     *
     * @return true if jSerialComm classes can be loaded and native library works
     */
    private boolean checkJSerialCommAvailable() {
        try {
            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            // Try to get comm ports to test if native library loads correctly
            Method getCommPortsMethod = serialPortClass.getMethod("getCommPorts");
            getCommPortsMethod.invoke(null);
            jSerialCommAvailable = true;
            log.debug("jSerialComm library is available and native library loaded successfully");
            return true;
        } catch (ClassNotFoundException e) {
            log.warn("jSerialComm library not found: {}", e.getMessage());
            jSerialCommAvailable = false;
            return false;
        } catch (UnsatisfiedLinkError e) {
            log.warn("jSerialComm native library cannot be loaded (architecture mismatch or missing DLL): {}", e.getMessage());
            jSerialCommAvailable = false;
            return false;
        } catch (Exception e) {
            log.warn("jSerialComm initialization failed: {}", e.getMessage());
            jSerialCommAvailable = false;
            return false;
        }
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
        if (!jSerialCommAvailable) {
            log.warn("jSerialComm not available, cannot connect via serial");
            return false;
        }
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

            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            Method getCommPortMethod = serialPortClass.getMethod("getCommPort", String.class);
            serialPort = getCommPortMethod.invoke(null, portName);
            
            if (serialPort == null) {
                log.error("Serial port {} not found", portName);
                return false;
            }

            // Configure serial port using reflection
            Method setBaudRateMethod = serialPortClass.getMethod("setBaudRate", int.class);
            setBaudRateMethod.invoke(serialPort, brainNodeConfig.getSerialBaudRate());
            
            Method setNumDataBitsMethod = serialPortClass.getMethod("setNumDataBits", int.class);
            setNumDataBitsMethod.invoke(serialPort, 8);
            
            Method setNumStopBitsMethod = serialPortClass.getMethod("setNumStopBits", int.class);
            setNumStopBitsMethod.invoke(serialPort, 1);
            
            // Set parity to NO_PARITY (0)
            Method setParityMethod = serialPortClass.getMethod("setParity", int.class);
            setParityMethod.invoke(serialPort, 0);
            
            // Set flow control to FLOW_CONTROL_DISABLED (0)
            Method setFlowControlMethod = serialPortClass.getMethod("setFlowControl", int.class);
            setFlowControlMethod.invoke(serialPort, 0);
            
            // Open the port
            Method openPortMethod = serialPortClass.getMethod("openPort", int.class);
            boolean opened = (boolean) openPortMethod.invoke(serialPort, 2000);
            if (!opened) {
                log.error("Failed to open serial port {}", portName);
                return false;
            }

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
        if (serialPort != null && jSerialCommAvailable) {
            try {
                Class<?> serialPortClass = serialPort.getClass();
                Method isOpenMethod = serialPortClass.getMethod("isOpen");
                boolean isOpen = (boolean) isOpenMethod.invoke(serialPort);
                if (isOpen) {
                    Method closePortMethod = serialPortClass.getMethod("closePort");
                    closePortMethod.invoke(serialPort);
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
        if (!jSerialCommAvailable) {
            log.warn("jSerialComm not available, cannot auto-detect port");
            return null;
        }
        try {
            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            Method getCommPortsMethod = serialPortClass.getMethod("getCommPorts");
            Object[] ports = (Object[]) getCommPortsMethod.invoke(null);
            List<String> availablePorts = new ArrayList<>();

            for (Object port : ports) {
                Method getSystemPortNameMethod = port.getClass().getMethod("getSystemPortName");
                String portName = (String) getSystemPortNameMethod.invoke(port);
                availablePorts.add(portName);
                log.debug("Found serial port: {}", portName);

                // Look for common Pi serial port descriptors
                String description = portName.toLowerCase();
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
        } catch (Exception e) {
            log.error("Error auto-detecting serial port: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get all available serial ports.
     *
     * @return list of available port names
     */
    public List<String> getAvailablePorts() {
        if (!jSerialCommAvailable) {
            log.warn("jSerialComm not available, cannot list ports");
            return new ArrayList<>();
        }
        try {
            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            Method getCommPortsMethod = serialPortClass.getMethod("getCommPorts");
            Object[] ports = (Object[]) getCommPortsMethod.invoke(null);
            List<String> portNames = new ArrayList<>();
            for (Object port : ports) {
                Method getSystemPortNameMethod = port.getClass().getMethod("getSystemPortName");
                String portName = (String) getSystemPortNameMethod.invoke(port);
                portNames.add(portName);
            }
            return portNames;
        } catch (Exception e) {
            log.error("Error getting available ports: {}", e.getMessage());
            return new ArrayList<>();
        }
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
            Method writeBytesMethod = serialPort.getClass().getMethod("writeBytes", byte[].class, int.class);
            int bytesWritten = (int) writeBytesMethod.invoke(serialPort, data, data.length);
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
        if (!connected || serialPort == null) {
            return false;
        }
        try {
            Method isOpenMethod = serialPort.getClass().getMethod("isOpen");
            return (boolean) isOpenMethod.invoke(serialPort);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the current connection status.
     *
     * @return a status string describing the connection
     */
    public String getConnectionStatus() {
        if (isConnected()) {
            try {
                Method getSystemPortNameMethod = serialPort.getClass().getMethod("getSystemPortName");
                String portName = (String) getSystemPortNameMethod.invoke(serialPort);
                return String.format("Connected to %s at %d baud", portName, brainNodeConfig.getSerialBaudRate());
            } catch (Exception e) {
                log.error("Error getting connection status: {}", e.getMessage());
            }
        }
        return "Disconnected - using HTTP fallback";
    }

    /**
     * Process incoming serial data and add to receive buffer.
     * This method is called when serial data is available.
     */
    public void processSerialData() {
        if (!connected || serialPort == null || !jSerialCommAvailable) {
            return;
        }
        try {
            Method bytesAvailableMethod = serialPort.getClass().getMethod("bytesAvailable");
            int bytesAvailable = (int) bytesAvailableMethod.invoke(serialPort);
            if (bytesAvailable > 0) {
                byte[] buffer = new byte[bytesAvailable];
                Method readBytesMethod = serialPort.getClass().getMethod("readBytes", byte[].class, int.class);
                int bytesRead = (int) readBytesMethod.invoke(serialPort, buffer, buffer.length);
                if (bytesRead > 0) {
                    String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    String trimmed = data.trim();
                    if (!trimmed.isEmpty()) {
                        if (receiveBuffer.size() >= receiveBuffer.remainingCapacity()) {
                            receiveBuffer.poll();
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
