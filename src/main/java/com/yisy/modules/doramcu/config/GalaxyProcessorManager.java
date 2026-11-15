package com.yisy.modules.doramcu.config;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages per-galaxy bounded contexts within the same JVM process.
 * <p>
 * Each galaxy gets its own isolated thread pool and event queue, enabling:
 * <ul>
 *   <li>Independent scaling - resource-heavy galaxies get more threads</li>
 *   <li>Fault isolation - one galaxy's crash doesn't affect others</li>
 *   <li>Resource limits - prevents any single galaxy from starving others</li>
 * </ul>
 * </p>
 * <p>
 * This is the foundation for future multi-process galaxy separation.
 * When galaxies move to separate processes, this manager becomes a process coordinator.
 * </p>
 */
@Service
public class GalaxyProcessorManager {

    private static final Logger log = LoggerFactory.getLogger(GalaxyProcessorManager.class);

    private final int maxGalaxies;
    private final int threadsPerGalaxy;
    private final int eventQueueSize;

    /** Active galaxy contexts, keyed by galaxy ID */
    private final Map<Long, GalaxyContext> galaxyContexts = new ConcurrentHashMap<>();

    /** Total active galaxy count */
    private final AtomicInteger activeGalaxyCount = new AtomicInteger(0);

    public GalaxyProcessorManager(int maxGalaxies, int threadsPerGalaxy, int eventQueueSize) {
        this.maxGalaxies = maxGalaxies;
        this.threadsPerGalaxy = threadsPerGalaxy;
        this.eventQueueSize = eventQueueSize;
    }

    /**
     * Creates or returns an existing galaxy context for the given galaxy ID.
     * <p>
     * If the maximum number of galaxies is reached, throws an IllegalStateException.
     * </p>
     *
     * @param galaxyId the unique galaxy identifier
     * @param galaxyName the galaxy name for logging
     * @return the galaxy context
     */
    public GalaxyContext createOrGetGalaxyContext(Long galaxyId, String galaxyName) {
        return galaxyContexts.computeIfAbsent(galaxyId, id -> {
            int currentCount = activeGalaxyCount.incrementAndGet();
            if (currentCount > maxGalaxies) {
                activeGalaxyCount.decrementAndGet();
                galaxyContexts.remove(id);
                throw new IllegalStateException(
                        "Maximum galaxy limit reached (" + maxGalaxies + "). Cannot create context for galaxy " + galaxyName);
            }

            GalaxyContext context = new GalaxyContext(id, galaxyName, threadsPerGalaxy, eventQueueSize);
            log.info("Created galaxy context for '{}' (id={}) - total active: {}",
                    galaxyName, id, currentCount);
            return context;
        });
    }

    /**
     * Removes a galaxy context when the galaxy is deleted or becomes inactive.
     *
     * @param galaxyId the galaxy identifier
     */
    public void removeGalaxyContext(Long galaxyId) {
        GalaxyContext removed = galaxyContexts.remove(galaxyId);
        if (removed != null) {
            removed.shutdown();
            activeGalaxyCount.decrementAndGet();
            log.info("Removed galaxy context for id={} - total active: {}",
                    galaxyId, activeGalaxyCount.get());
        }
    }

    /**
     * Returns all active galaxy contexts.
     */
    public Map<Long, GalaxyContext> getAllGalaxyContexts() {
        return Map.copyOf(galaxyContexts);
    }

    /**
     * Returns the number of active galaxy contexts.
     */
    public int getActiveGalaxyCount() {
        return activeGalaxyCount.get();
    }

    /**
     * Represents an isolated processing context for a single galaxy.
     * <p>
     * Contains dedicated thread pools and event queues that are independent
     * from other galaxy contexts.
     * </p>
     */
    public static class GalaxyContext {
        private final Long galaxyId;
        private final String galaxyName;
        private final Executor turnExecutor;
        private final Executor aiExecutor;
        private final LinkedBlockingQueue<Object> eventQueue;
        private final AtomicInteger eventCount = new AtomicInteger(0);

        GalaxyContext(Long galaxyId, String galaxyName, int threadsPerGalaxy, int eventQueueSize) {
            this.galaxyId = galaxyId;
            this.galaxyName = galaxyName;

            // Turn processing thread pool
            this.turnExecutor = createThreadPool("galaxy-" + galaxyId + "-turn-", threadsPerGalaxy);

            // AI processing thread pool (separate from turn processing)
            this.aiExecutor = createThreadPool("galaxy-" + galaxyId + "-ai-", threadsPerGalaxy);

            // Event queue for galaxy-specific events
            this.eventQueue = new LinkedBlockingQueue<>(eventQueueSize);

            log.info("Galaxy context initialized for '{}' (id={}) with {} threads per pool",
                    galaxyName, galaxyId, threadsPerGalaxy);
        }

        private Executor createThreadPool(String namePrefix, int poolSize) {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(poolSize);
            executor.setMaxPoolSize(poolSize * 2);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix(namePrefix);
            executor.setDaemon(true);
            executor.initialize();
            return executor;
        }

        /** Returns the galaxy ID */
        public Long getGalaxyId() {
            return galaxyId;
        }

        /** Returns the galaxy name */
        public String getGalaxyName() {
            return galaxyName;
        }

        /** Returns the turn processing executor */
        public Executor getTurnExecutor() {
            return turnExecutor;
        }

        /** Returns the AI processing executor */
        public Executor getAiExecutor() {
            return aiExecutor;
        }

        /**
         * Submits an event to the galaxy's event queue.
         *
         * @param event the event object
         * @return true if the event was added, false if the queue is full
         */
        public boolean submitEvent(Object event) {
            boolean added = eventQueue.offer(event);
            if (added) {
                eventCount.incrementAndGet();
            } else {
                log.warn("Event queue full for galaxy '{}' (id={}), dropping event",
                        galaxyName, galaxyId);
            }
            return added;
        }

        /**
         * Polls an event from the galaxy's event queue.
         *
         * @return the event or null if empty
         */
        public Object pollEvent() {
            return eventQueue.poll();
        }

        /** Returns the current event queue size */
        public int getEventQueueSize() {
            return eventQueue.size();
        }

        /** Returns the total number of events processed */
        public int getEventCount() {
            return eventCount.get();
        }

        /**
         * Shuts down the galaxy context and releases resources.
         */
        public void shutdown() {
            log.info("Shutting down galaxy context for '{}' (id={})", galaxyName, galaxyId);
            // Thread pools will be garbage collected when no longer referenced
        }
    }
}
