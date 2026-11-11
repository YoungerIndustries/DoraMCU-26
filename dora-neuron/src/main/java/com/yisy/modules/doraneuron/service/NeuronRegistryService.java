package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.model.NeuronStatus;
import com.yisy.modules.doraneuron.protocol.NeuronHeartbeat;
import com.yisy.modules.doraneuron.producer.NeuronEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles self-registration and periodic heartbeats to the Neuron Coordinator.
 *
 * On startup this service:
 * <ol>
 *   <li>Builds a {@link NeuronIdentity} from configuration and runtime info</li>
 *   <li>Registers with the coordinator via {@code POST /api/v1/neuron/register}</li>
 * </ol>
 *
 * Then it sends a heartbeat every {@code neuron.heartbeat-interval-ms} milliseconds
 * and deregisters on shutdown.
 */
@Service
public class NeuronRegistryService {

    private static final Logger log = LoggerFactory.getLogger(NeuronRegistryService.class);

    private final NeuronConfig config;
    private final RestTemplate restTemplate;
    private final NeuronEventProducer eventProducer; // null if Kafka not configured
    private NeuronIdentity identity;

    /** Currently active coordinator URL (may be a fallback). */
    private volatile String currentCoordinatorUrl;

    /** Whether this neuron is connected to a fallback coordinator. */
    private volatile boolean federated = false;

    /** Track if registration has succeeded at least once. */
    private final AtomicBoolean registered = new AtomicBoolean(false);

    @Value("${server.port:8090}")
    private int serverPort;

    public NeuronRegistryService(NeuronConfig config, RestTemplate restTemplate,
                                  @jakarta.annotation.Nullable NeuronEventProducer eventProducer) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.eventProducer = eventProducer;
    }

    /**
     * Build identity and register with the coordinator on startup.
     * Tries the primary coordinator first, then fallback coordinators.
     */
    @PostConstruct
    public void register() {
        identity = buildIdentity();
        log.info("Neuron identity: {}", identity);

        // Try primary coordinator first
        if (tryRegister(config.getCoordinatorUrl())) {
            return;
        }

        // Try fallback coordinators
        if (config.isFailoverEnabled()) {
            List<String> fallbacks = config.getCoordinatorFallbackUrlList();
            for (String fallbackUrl : fallbacks) {
                if (tryRegister(fallbackUrl)) {
                    federated = true;
                    log.info("Failed over to coordinator at {}", fallbackUrl);
                    return;
                }
            }
        }

        log.warn("Could not register with any coordinator");
    }

    /**
     * Try to register with a specific coordinator URL.
     * @return true if registration succeeded
     */
    private boolean tryRegister(String url) {
        try {
            String registerUrl = url + "/api/v1/neuron/register";
            ResponseEntity<Void> response = restTemplate.postForEntity(registerUrl, identity, Void.class);
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                currentCoordinatorUrl = url;
                registered.set(true);
                log.info("Registered with coordinator at {}", url);
                return true;
            } else {
                log.warn("Coordinator {} returned {} during registration", url, response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to register with coordinator at {}: {}", url, e.getMessage());
        }
        return false;
    }

    /**
     * Send a periodic heartbeat to the coordinator.
     * Uses Kafka first (event-driven), then HTTP POST (fallback).
     */
    @Scheduled(fixedDelayString = "${neuron.heartbeat-interval-ms:15000}")
    public void sendHeartbeat() {
        if (identity == null) {
            return;
        }

        NeuronHeartbeat heartbeat = new NeuronHeartbeat(
                identity.getNodeId(),
                identity.getStatus().name(),
                identity.getCurrentModelVersion(),
                identity.getExperienceCount(),
                System.currentTimeMillis()
        );

        // Try Kafka first (event-driven)
        if (eventProducer != null && eventProducer.isAvailable()) {
            try {
                eventProducer.publishHeartbeat(heartbeat);
                log.debug("Heartbeat published via Kafka");
                return;
            } catch (Exception e) {
                log.warn("Kafka heartbeat failed: {} — falling back to HTTP", e.getMessage());
            }
        }

        // Fallback to HTTP POST
        String url = getCurrentCoordinatorUrl() + "/api/v1/neuron/heartbeat";
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, heartbeat, Void.class);
            log.debug("Heartbeat sent to coordinator, status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("Heartbeat failed to {}: {}", getCurrentCoordinatorUrl(), e.getMessage());
            // Try to failover if heartbeat fails
            if (config.isFailoverEnabled() && !federated) {
                attemptFailover();
            }
        }
    }

    /**
     * Attempt to failover to a fallback coordinator.
     */
    private void attemptFailover() {
        List<String> fallbacks = config.getCoordinatorFallbackUrlList();
        for (String fallbackUrl : fallbacks) {
            if (fallbackUrl.equals(currentCoordinatorUrl)) continue;
            if (tryRegister(fallbackUrl)) {
                federated = true;
                log.info("Failed over to coordinator at {}", fallbackUrl);
                return;
            }
        }
    }

    /**
     * Periodically check if the primary coordinator is available again.
     * If so, re-register with the primary coordinator.
     */
    @Scheduled(fixedDelayString = "${neuron.failover-check-interval-ms:30000}")
    public void checkPrimaryCoordinator() {
        if (!federated) return;
        try {
            String healthUrl = config.getCoordinatorUrl() + "/api/health";
            restTemplate.getForObject(healthUrl, String.class);
            log.info("Primary coordinator is available again at {}", config.getCoordinatorUrl());
            if (tryRegister(config.getCoordinatorUrl())) {
                federated = false;
                log.info("Re-registered with primary coordinator");
            }
        } catch (Exception e) {
            // Primary still unavailable
        }
    }

    /**
     * Deregister gracefully on shutdown.
     */
    @EventListener(ContextClosedEvent.class)
    public void deregister() {
        if (identity == null) {
            return;
        }

        String url = getCurrentCoordinatorUrl() + "/api/v1/neuron/deregister";
        try {
            restTemplate.delete(url + "?nodeId=" + identity.getNodeId());
            log.info("Deregistered from coordinator");
        } catch (Exception e) {
            log.warn("Failed to deregister: {}", e.getMessage());
        }
    }

    /**
     * Capture the actual server port after the web server starts (in case dynamic port 0 is used).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onServerStarted(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof WebServerApplicationContext wac) {
            serverPort = wac.getWebServer().getPort();
            if (identity != null) {
                identity.setPort(serverPort);
            }
        }
    }

    private NeuronIdentity buildIdentity() {
        NeuronIdentity id = new NeuronIdentity();
        id.setNodeId(config.getNodeId() != null ? config.getNodeId() : UUID.randomUUID().toString());
        id.setNodeName(config.getNodeName() != null ? config.getNodeName() : "Neuron-" + id.getNodeId().substring(0, 8));
        id.setRegion(config.getNodeRegion());
        id.setHostAddress(getHostAddress());
        id.setPort(serverPort);
        id.setRegisteredAt(System.currentTimeMillis());
        id.setLastHeartbeat(System.currentTimeMillis());
        id.setStatus(NeuronStatus.ACTIVE);
        id.setVoteWeight(config.getVoteWeight());
        id.setCurrentModelVersion(0);
        id.setExperienceCount(0);
        return id;
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * Return the current identity (used by other services).
     */
    public NeuronIdentity getIdentity() {
        return identity;
    }

    /**
     * Get the current coordinator URL (primary or fallback).
     */
    public String getCurrentCoordinatorUrl() {
        return currentCoordinatorUrl != null ? currentCoordinatorUrl : config.getCoordinatorUrl();
    }

    /**
     * Check if this neuron is connected to a fallback coordinator.
     */
    public boolean isFederated() {
        return federated;
    }

    /**
     * Check if registration has succeeded.
     */
    public boolean isRegistered() {
        return registered.get();
    }
}
