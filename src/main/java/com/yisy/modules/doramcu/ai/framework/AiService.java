package com.yisy.modules.doramcu.ai.framework;

import java.util.List;
import java.util.Optional;

/**
 * Unified AI service interface that all AI subsystems implement.
 * <p>
 * This interface provides a common contract for AI operations across all modules,
 * eliminating the need for multiple specialized service interfaces. Each AI domain
 * (player, network, testing, learning, NPC) implements this interface to provide
 * a consistent API.
 * </p>
 * <p>
 * The framework follows a layered architecture:
 * <ul>
 *   <li>{@link AiDecisionEngine} - Strategy, Diplomacy, Combat decisions</li>
 *   <li>{@link AiPersonalityManager} - Dynamic personality management</li>
 *   <li>{@link AiNetworkManager} - Entity relationship and network topology</li>
 *   <li>{@link AiTestingService} - Automated AI validation</li>
 *   <li>{@link AiLearningService} - Neural network training and inference</li>
 * </ul>
 * </p>
 */
public interface AiService {

    /**
     * Returns the unique identifier for this AI service.
     *
     * @return the service ID
     */
    String getServiceId();

    /**
     * Returns the human-readable name of this AI service.
     *
     * @return the service name
     */
    String getServiceName();

    /**
     * Returns the current status of this AI service.
     *
     * @return the service status
     */
    AiServiceStatus getStatus();

    /**
     * Initializes the AI service. Called once at startup.
     */
    void initialize();

    /**
     * Shuts down the AI service gracefully.
     */
    void shutdown();

    /**
     * Health check for the AI service.
     *
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Returns metrics for this AI service.
     *
     * @return a map of metric name to value
     */
    java.util.Map<String, Object> getMetrics();

    /**
     * Represents the status of an AI service.
     */
    enum AiServiceStatus {
        /** Service is starting up */
        STARTING,
        /** Service is running normally */
        RUNNING,
        /** Service is degraded but still functional */
        DEGRADED,
        /** Service is stopped */
        STOPPED,
        /** Service encountered an error */
        ERROR
    }
}
