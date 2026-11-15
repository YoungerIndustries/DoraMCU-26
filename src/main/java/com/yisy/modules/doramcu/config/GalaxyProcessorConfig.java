package com.yisy.modules.doramcu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for per-galaxy bounded context processing.
 * <p>
 * Creates isolated thread pools and event queues for each galaxy, enabling
 * independent scaling and fault isolation within the same JVM process.
 * This is the foundation for future multi-process galaxy separation.
 * </p>
 * <p>
 * Each galaxy gets its own:
 * <ul>
 *   <li>Thread pool for turn processing and AI actions</li>
 *   <li>Event queue for galaxy-specific events</li>
 *   <li>Health check scheduler</li>
 * </ul>
 * </p>
 */
@Configuration
public class GalaxyProcessorConfig {

    private static final Logger log = LoggerFactory.getLogger(GalaxyProcessorConfig.class);

    @Value("${doramcu.galaxy.processor.max-galaxies:10}")
    private int maxGalaxies;

    @Value("${doramcu.galaxy.processor.threads-per-galaxy:2}")
    private int threadsPerGalaxy;

    @Value("${doramcu.galaxy.processor.event-queue-size:500}")
    private int eventQueueSize;

    /**
     * Creates a GalaxyProcessorManager that manages per-galaxy bounded contexts.
     * <p>
     * The manager dynamically creates and destroys galaxy contexts as galaxies
     * are added or removed, ensuring resource isolation between galaxies.
     * </p>
     */
    @Bean("galaxyProcessorManager")
    public GalaxyProcessorManager galaxyProcessorManager() {
        GalaxyProcessorManager manager = new GalaxyProcessorManager(
                maxGalaxies,
                threadsPerGalaxy,
                eventQueueSize
        );
        log.info("Galaxy processor manager created (max galaxies: {}, threads per galaxy: {}, event queue size: {})",
                maxGalaxies, threadsPerGalaxy, eventQueueSize);
        return manager;
    }
}
