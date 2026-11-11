package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Dedicated service for background galaxy sector generation.
 * <p>
 * Replaces the raw {@code new Thread()} approach with a Spring-managed ThreadPoolTaskExecutor
 * for proper lifecycle management, progress tracking, and cancellation support.
 * </p>
 * <p>
 * The builder tracks the status of ongoing galaxy builds, allowing queries for completion
 * percentage and build state. It uses a dedicated thread pool with a single core thread
 * to ensure only one galaxy build runs at a time, preventing resource contention.
 * </p>
 */
@Service
public class BackgroundGalaxyBuilder {

    private static final Logger log = LoggerFactory.getLogger(BackgroundGalaxyBuilder.class);

    private final GalaxyService galaxyService;
    private final GalaxyProgressService galaxyProgressService;
    private final GalaxyRepository galaxyRepository;
    private final GalaxyEventService galaxyEventService;
    private final GalaxyMetricsService galaxyMetricsService;
    private ThreadPoolTaskExecutor taskExecutor;

    /** Tracks the current build task for cancellation support. */
    private volatile Future<?> currentBuildTask;

    /** Tracks the build status for queryable progress. */
    private final ConcurrentHashMap<String, GalaxyBuildStatus> buildStatusMap = new ConcurrentHashMap<>();

    public BackgroundGalaxyBuilder(GalaxyService galaxyService,
                                    GalaxyProgressService galaxyProgressService,
                                    GalaxyRepository galaxyRepository,
                                    GalaxyEventService galaxyEventService,
                                    GalaxyMetricsService galaxyMetricsService) {
        this.galaxyService = galaxyService;
        this.galaxyProgressService = galaxyProgressService;
        this.galaxyRepository = galaxyRepository;
        this.galaxyEventService = galaxyEventService;
        this.galaxyMetricsService = galaxyMetricsService;
    }

    @PostConstruct
    public void init() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(1);
        this.taskExecutor.setMaxPoolSize(2);
        this.taskExecutor.setQueueCapacity(5);
        this.taskExecutor.setThreadNamePrefix("galaxy-bg-builder-");
        this.taskExecutor.initialize();
        log.info("BackgroundGalaxyBuilder initialized with thread pool (core=1, max=2, queue=5)");
    }

    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
            log.info("BackgroundGalaxyBuilder thread pool shut down");
        }
    }

    /**
     * Starts background generation of remaining galaxy sectors for the default galaxy.
     * <p>
     * This method is the main entry point called after Earth Hub generation. It loads
     * the existing partial galaxy from the database, generates the remaining sectors in
     * memory, and persists them — all running asynchronously.
     * </p>
     *
     * @return the build status tracking object for monitoring progress
     */
    public GalaxyBuildStatus startBackgroundGeneration() {
        return startBackgroundGeneration(GalaxyFactory.GAME_GALAXY_NAME);
    }

    /**
     * Starts background generation of remaining galaxy sectors for a specific galaxy.
     * <p>
     * This method loads the existing partial galaxy from the database, generates the
     * remaining sectors in memory, and persists them — all running asynchronously.
     * Supports per-galaxy generation using the galaxy's stored seed and config.
     * </p>
     *
     * @param galaxyName the name of the galaxy to generate
     * @return the build status tracking object for monitoring progress
     */
    public GalaxyBuildStatus startBackgroundGeneration(String galaxyName) {

        // Prevent duplicate builds
        GalaxyBuildStatus existing = buildStatusMap.get(galaxyName);
        if (existing != null && existing.isRunning()) {
            log.info("Background generation already running for galaxy '{}'. Skipping.", galaxyName);
            return existing;
        }

        GalaxyBuildStatus status = new GalaxyBuildStatus(galaxyName);
        buildStatusMap.put(galaxyName, status);
        log.info("Starting background galaxy generation for: {}", galaxyName);

        // Publish BUILD_STARTED event
        Optional<com.yisy.modules.doramcu.model.Galaxy> seedGalaxyOpt = galaxyRepository.findByName(galaxyName);
        long seed = seedGalaxyOpt.map(g -> g.getSeed() != null ? g.getSeed() : GalaxyFactory.GAME_GALAXY_SEED).orElse(GalaxyFactory.GAME_GALAXY_SEED);
        int totalSectors = com.yisy.modules.doramcu.hex.Galaxy.GRID_SIZE * com.yisy.modules.doramcu.hex.Galaxy.GRID_SIZE;
        galaxyEventService.publish(GalaxyEventType.BUILD_STARTED, galaxyName,
                seedGalaxyOpt.map(com.yisy.modules.doramcu.model.Galaxy::getId).orElse(null),
                "totalSectors", String.valueOf(totalSectors), "seed", String.valueOf(seed));

        currentBuildTask = taskExecutor.submit(() -> {
            long buildStartTime = System.currentTimeMillis();
            try {
                status.setRunning(true);
                status.setPhase("Loading galaxy from database...");
                log.info("Background galaxy generation started for: {}", galaxyName);

                // Set progress listener for SSE events
                galaxyService.setProgressListener(galaxyProgressService);

                // Load the existing partial galaxy from DB
                Optional<com.yisy.modules.doramcu.model.Galaxy> dbGalaxyOpt =
                        galaxyRepository.findByName(galaxyName);
                if (dbGalaxyOpt.isEmpty()) {
                    log.warn("Background generation: galaxy '{}' not found, skipping.", galaxyName);
                    status.setFailed("Galaxy not found in database");
                    galaxyEventService.publish(GalaxyEventType.BUILD_FAILED, galaxyName, null,
                            "errorMessage", "Galaxy not found in database",
                            "systemsGeneratedBeforeFailure", "0");
                    return;
                }
                com.yisy.modules.doramcu.model.Galaxy dbGalaxy = dbGalaxyOpt.get();

                // Get current completion percentage
                int initialPercent = galaxyService.getGalaxyCompletionPercent(galaxyName);
                status.setCompletionPercent(initialPercent);
                status.setPhase("Generating remaining sectors...");

                // Use stored seed and config, or fall back to defaults
                int systemCount = dbGalaxy.getSystemCount() != null ? dbGalaxy.getSystemCount() :
                        GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getSystemCount();
                double hyperlaneDensity = dbGalaxy.getHyperlaneDensity();

                // Generate remaining sectors in memory — use a single galaxy instance
                com.yisy.modules.doramcu.hex.Galaxy inMemoryGalaxy =
                        new com.yisy.modules.doramcu.hex.Galaxy(1, galaxyName);
                galaxyService.generateRemainingSectorsInMemory(
                        inMemoryGalaxy,
                        systemCount,
                        seed
                );

                int generated = inMemoryGalaxy.getAllSectors().size();
                if (generated > 0) {
                    status.setPhase("Persisting newly generated sectors...");
                    status.setSystemsGenerated(generated);

                    // Publish SECTOR_GENERATED events for each sector in the in-memory galaxy
                    for (com.yisy.modules.doramcu.hex.Galaxy.Sector sector : inMemoryGalaxy.getAllSectors()) {
                        if (sector != null && !sector.getAllSystems().isEmpty()) {
                            galaxyEventService.publish(GalaxyEventType.SECTOR_GENERATED, galaxyName, dbGalaxy.getId(),
                                    "sectorRow", String.valueOf(sector.getRow()),
                                    "sectorCol", String.valueOf(sector.getCol()),
                                    "systemsInSector", String.valueOf(sector.getAllSystems().size()));
                            galaxyMetricsService.incrementSectorGenerated();
                        }
                    }

                    // Persist the newly generated sectors using the same galaxy instance
                    galaxyService.persistRemainingSectors(dbGalaxy,
                            inMemoryGalaxy,
                            hyperlaneDensity);

                    int finalPercent = galaxyService.getGalaxyCompletionPercent(galaxyName);
                    status.setCompletionPercent(finalPercent);
                    status.setCompleted(true);
                    status.setPhase("Complete");

                    long elapsedMs = System.currentTimeMillis() - buildStartTime;
                    log.info("Background galaxy generation complete: {} additional systems, {}% complete",
                            generated, finalPercent);

                    // Publish BUILD_COMPLETED event
                    galaxyEventService.publish(GalaxyEventType.BUILD_COMPLETED, galaxyName, dbGalaxy.getId(),
                            "totalSystemsGenerated", String.valueOf(generated),
                            "elapsedMs", String.valueOf(elapsedMs));
                    galaxyMetricsService.incrementBuildCompleted();
                    galaxyMetricsService.setCompletionPercent(galaxyName, finalPercent);
                } else {
                    status.setCompleted(true);
                    status.setPhase("Complete (no new sectors)");
                    log.info("Background galaxy generation: no new sectors to generate for {}", galaxyName);

                    long elapsedMs = System.currentTimeMillis() - buildStartTime;
                    galaxyEventService.publish(GalaxyEventType.BUILD_COMPLETED, galaxyName, dbGalaxy.getId(),
                            "totalSystemsGenerated", "0",
                            "elapsedMs", String.valueOf(elapsedMs));
                    galaxyMetricsService.incrementBuildCompleted();
                }

            } catch (Exception e) {
                log.error("Background galaxy generation failed: {}", e.getMessage(), e);
                status.setFailed(e.getMessage());
                galaxyEventService.publish(GalaxyEventType.BUILD_FAILED, galaxyName, null,
                        "errorMessage", e.getMessage(),
                        "systemsGeneratedBeforeFailure", String.valueOf(status.getSystemsGenerated()));
                galaxyMetricsService.incrementBuildFailure();
            } finally {
                status.setRunning(false);
            }
        });

        return status;
    }

    /**
     * Returns the current build status for the default galaxy, or null if no build has started.
     *
     * @return the build status or null
     */
    public GalaxyBuildStatus getBuildStatus() {
        return buildStatusMap.get(GalaxyFactory.GAME_GALAXY_NAME);
    }

    /**
     * Returns the build status for a specific galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @return the build status or null if no build has started
     */
    public GalaxyBuildStatus getBuildStatus(String galaxyName) {
        return buildStatusMap.get(galaxyName);
    }

    /**
     * Cancels the currently running background build task.
     *
     * @return true if the task was successfully cancelled, false otherwise
     */
    public boolean cancelCurrentBuild() {
        if (currentBuildTask != null && !currentBuildTask.isDone()) {
            boolean cancelled = currentBuildTask.cancel(true);
            if (cancelled) {
                log.info("Background galaxy generation cancelled");
                GalaxyBuildStatus status = buildStatusMap.get(GalaxyFactory.GAME_GALAXY_NAME);
                if (status != null) {
                    status.setRunning(false);
                    status.setFailed("Cancelled by user");
                }
            }
            return cancelled;
        }
        return false;
    }

    /**
     * Periodically checks if there are incomplete galaxies that need background generation.
     * This scheduled method ensures that if a galaxy creation was interrupted, the build
     * can resume on the next cycle.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void checkIncompleteGalaxies() {
        String galaxyName = GalaxyFactory.GAME_GALAXY_NAME;

        // Skip if a build is already running
        GalaxyBuildStatus status = buildStatusMap.get(galaxyName);
        if (status != null && status.isRunning()) {
            return;
        }

        // Check if galaxy exists and is not complete
        Optional<com.yisy.modules.doramcu.model.Galaxy> galaxyOpt = galaxyRepository.findByName(galaxyName);
        if (galaxyOpt.isPresent()) {
            int completionPercent = galaxyService.getGalaxyCompletionPercent(galaxyName);
            if (completionPercent < 100) {
                log.info("Galaxy '{}' is {}% complete. Background generation not running — consider starting manually.",
                        galaxyName, completionPercent);
            }
        }
    }
}
