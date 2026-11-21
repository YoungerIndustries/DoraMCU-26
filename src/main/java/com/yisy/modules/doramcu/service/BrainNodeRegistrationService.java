package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.config.BrainNodeProperties;
import com.yisy.modules.doramcu.dto.NodeRegistrationRequest;
import com.yisy.modules.doramcu.dto.NodeRegistrationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Base64;

/**
 * Service that registers this server as a Brain Node with the Pi Brain Stem.
 * <p>
 * On startup, this service sends a registration request to the brain stem's
 * /api/v1/nodes/register endpoint. It then sends periodic heartbeats to keep
 * the node status active. If the brain stem is unavailable during startup,
 * it will retry on the next heartbeat interval.
 * </p>
 */
@Service
public class BrainNodeRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeRegistrationService.class);

    private final BrainNodeProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String assignedNodeId;
    private boolean registered = false;

    public BrainNodeRegistrationService(BrainNodeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    /**
     * Register this node with the brain stem on startup.
     */
    @Scheduled(initialDelayString = "${doramcu.brain-node.registration-delay-ms:5000}", fixedDelayString = "${doramcu.brain-node.heartbeat-interval-seconds:60}000")
    public void registerAndHeartbeat() {
        if (!properties.isEnabled()) {
            return;
        }

        String brainStemUrl = properties.getBrainStemUrl();
        if (brainStemUrl == null || brainStemUrl.isEmpty()) {
            log.warn("Brain node enabled but brainStemUrl is not configured. Set doramcu.brain-node.brain-stem-url.");
            return;
        }

        try {
            if (!registered) {
                register();
            } else if (assignedNodeId != null) {
                sendHeartbeat();
            }
        } catch (Exception e) {
            log.warn("Failed to contact brain stem at {}: {}", brainStemUrl, e.getMessage());
        }
    }

    private void register() throws Exception {
        String brainStemUrl = properties.getBrainStemUrl();
        String registerUrl = brainStemUrl + "/api/v1/nodes/register";

        NodeRegistrationRequest request = buildRegistrationRequest();
        String jsonBody = objectMapper.writeValueAsString(request);

        log.info("Registering as brain node with {} at {}", properties.getNodeType(), registerUrl);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(registerUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            NodeRegistrationResponse regResponse = objectMapper.readValue(response.body(), NodeRegistrationResponse.class);
            assignedNodeId = regResponse.getNodeId();
            registered = true;
            log.info("Successfully registered as brain node: id={}, type={}, status={}",
                    assignedNodeId, regResponse.getNodeType(), regResponse.getStatus());
        } else {
            log.error("Registration failed with status {}: {}", response.statusCode(), response.body());
            registered = false;
        }
    }

    private void sendHeartbeat() throws Exception {
        String brainStemUrl = properties.getBrainStemUrl();
        String heartbeatUrl = brainStemUrl + "/api/v1/nodes/" + assignedNodeId + "/heartbeat";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(heartbeatUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            log.debug("Heartbeat sent to brain stem for node: {}", assignedNodeId);
        } else {
            log.warn("Heartbeat failed for node {} with status: {}", assignedNodeId, response.statusCode());
        }
    }

    private NodeRegistrationRequest buildRegistrationRequest() {
        NodeRegistrationRequest request = new NodeRegistrationRequest();
        request.setNodeType(properties.getNodeType());
        request.setDeviceId(getDeviceId());

        // Build capabilities
        NodeRegistrationRequest.DeviceCapabilities capabilities = new NodeRegistrationRequest.DeviceCapabilities();
        capabilities.setCpuCores(properties.getCpuCores() > 0 ? properties.getCpuCores() : Runtime.getRuntime().availableProcessors());
        capabilities.setRamMb(properties.getRamMb() > 0 ? properties.getRamMb() : (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        capabilities.setGpuTier(properties.getGpuTier());
        request.setCapabilities(capabilities);

        // Build network info
        NodeRegistrationRequest.NetworkInfo networkInfo = new NodeRegistrationRequest.NetworkInfo();
        networkInfo.setIpAddress(getIpAddress());
        networkInfo.setPort(properties.getPort());
        request.setNetworkInfo(networkInfo);

        return request;
    }

    private String getDeviceId() {
        if (properties.getDeviceId() != null && !properties.getDeviceId().isEmpty()) {
            return properties.getDeviceId();
        }
        // Generate a stable device ID based on host name
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return "pc-" + hostName.toLowerCase().replace(".", "-").replace(" ", "-");
        } catch (UnknownHostException e) {
            return "pc-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    private String getIpAddress() {
        if (properties.getIpAddress() != null && !properties.getIpAddress().isEmpty()) {
            return properties.getIpAddress();
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public String getAssignedNodeId() {
        return assignedNodeId;
    }

    public boolean isRegistered() {
        return registered;
    }
}
