package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.gamecore.*;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.service.GalaxyCloneService;
import com.yisy.modules.doramcu.service.GalaxyService;
import com.yisy.modules.doramcu.service.OverlayBuilderRegistry;
import com.yisy.modules.doramcu.gamemode.GameModeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the ordered creation of multiple galaxies at startup.
 * <p>
 * This service processes {@link GalaxyStartupEntry} instances in priority order,
 * creating each galaxy sequentially. Each galaxy is generated using its template's
 * configuration, and associated game modules are initialized via
 * {@link GameModule#initializeForGalaxy(String, String)}.
 * </p>
 * <p>
 * The queue ensures that dependencies are respected: Earth2026 builds first (priority 1),
 * then Traveller (priority 2), then D&D (priority 3), and so on. Each galaxy must
 * complete before the next one begins.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Register entries from game modules
 * queue.registerEntry(GalaxyStartupEntry.of(1, "Earth 2026", GalaxyTemplate.EARTH_2026_TEMPLATE, GameType.EARTH_2026));
 * queue.registerEntry(GalaxyStartupEntry.of(2, "Traveller Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, GameType.TRAVELLER));
 *
 * // Process all entries at startup
 * MultiGalaxyBuildQueue.Result result = queue.processAll();
 * log.info("Built {} galaxies, {} failed", result.getSuccessfulCount(), result.getFailedCount());
 * </pre>
 * </p>
 */
@Service
public class MultiGalaxyBuildQueue {

    private static final Logger log = LoggerFactory.getLogger(MultiGalaxyBuildQueue.class);

    private final GalaxyService galaxyService;
    private final GalaxyCloneService galaxyCloneService;
    private final GameModuleRegistry gameModuleRegistry;
    private final GameModeRegistry gameModeRegistry;
    private final OverlayBuilderRegistry overlayBuilderRegistry;

    private final List<GalaxyStartupEntry> entries = new CopyOnWriteArrayList<>();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicInteger successfulCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    /**
     * Listener interface for galaxy build progress events.
     */
    public interface BuildProgressListener {
        void onGalaxyStart(GalaxyStartupEntry entry);
        void onGalaxyComplete(GalaxyStartupEntry entry, Galaxy galaxy);
        void onGalaxyFailed(GalaxyStartupEntry entry, Exception exception);
        void onAllComplete(Result result);
    }

    private final List<BuildProgressListener> listeners = new CopyOnWriteArrayList<>();

    public MultiGalaxyBuildQueue(GalaxyService galaxyService,
                                   GalaxyCloneService galaxyCloneService,
                                   GameModuleRegistry gameModuleRegistry,
                                   GameModeRegistry gameModeRegistry,
                                   OverlayBuilderRegistry overlayBuilderRegistry) {
        this.galaxyService = galaxyService;
        this.galaxyCloneService = galaxyCloneService;
        this.gameModuleRegistry = gameModuleRegistry;
        this.gameModeRegistry = gameModeRegistry;
        this.overlayBuilderRegistry = overlayBuilderRegistry;
    }

    /**
     * Register a galaxy startup entry.
     *
     * @param entry the entry to register
     */
    public void registerEntry(GalaxyStartupEntry entry) {
        if (entry == null || !entry.isEnabled()) {
            return;
        }
        // Check for duplicate galaxy names
        boolean exists = entries.stream()
                .anyMatch(e -> e.getGalaxyName().equalsIgnoreCase(entry.getGalaxyName()));
        if (exists) {
            log.warn("Skipping duplicate galaxy entry: {}", entry.getGalaxyName());
            return;
        }
        entries.add(entry);
        log.info("Registered galaxy startup entry: {}", entry);
    }

    /**
     * Register multiple galaxy startup entries.
     *
     * @param newEntries the entries to register
     */
    public void registerEntries(List<GalaxyStartupEntry> newEntries) {
        if (newEntries == null) {
            return;
        }
        newEntries.forEach(this::registerEntry);
    }

    /**
     * Add a progress listener.
     *
     * @param listener the listener to add
     */
    public void addListener(BuildProgressListener listener) {
        listeners.add(listener);
    }

    /**
     * Returns the registered entries sorted by priority.
     *
     * @return sorted list of entries
     */
    public List<GalaxyStartupEntry> getSortedEntries() {
        return entries.stream()
                .sorted(Comparator.comparingInt(GalaxyStartupEntry::getPriority))
                .toList();
    }

    /**
     * Returns the number of registered entries.
     *
     * @return entry count
     */
    public int getEntryCount() {
        return entries.size();
    }

    /**
     * Process all registered galaxy entries in priority order.
     * <p>
     * This method is idempotent: if already processing, it returns immediately
     * with a partial result. Each galaxy is created sequentially, and failures
     * are logged but do not stop the queue.
     * </p>
     *
     * @return the build result with success/failure counts
     */
    public Result processAll() {
        if (!processing.compareAndSet(false, true)) {
            log.warn("Galaxy build queue is already processing");
            return new Result(successfulCount.get(), failedCount.get(), new ArrayList<>(), new ArrayList<>());
        }

        log.info("Starting multi-galaxy build queue with {} entries", entries.size());
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        List<GalaxyStartupEntry> sortedEntries = getSortedEntries();
        for (int i = 0; i < sortedEntries.size(); i++) {
            GalaxyStartupEntry entry = sortedEntries.get(i);
            log.info("Processing galaxy {}/{}: {}", i + 1, sortedEntries.size(), entry.getGalaxyName());

            // Notify listeners
            listeners.forEach(l -> l.onGalaxyStart(entry));

            try {
                Galaxy galaxy = processEntry(entry);
                if (galaxy != null) {
                    successful.add(entry.getGalaxyName());
                    successfulCount.incrementAndGet();
                    listeners.forEach(l -> l.onGalaxyComplete(entry, galaxy));
                    log.info("Successfully created galaxy: {} ({})", entry.getGalaxyName(), galaxy.getId());
                } else {
                    // Galaxy already exists - count as success
                    successful.add(entry.getGalaxyName());
                    successfulCount.incrementAndGet();
                    log.info("Galaxy already exists, skipping: {}", entry.getGalaxyName());
                }
            } catch (Exception e) {
                failed.add(entry.getGalaxyName());
                failedCount.incrementAndGet();
                listeners.forEach(l -> l.onGalaxyFailed(entry, e));
                log.error("Failed to create galaxy {}: {}", entry.getGalaxyName(), e.getMessage(), e);
            }
        }

        processing.set(false);
        Result result = new Result(successfulCount.get(), failedCount.get(), successful, failed);
        listeners.forEach(l -> l.onAllComplete(result));
        log.info("Multi-galaxy build queue complete: {} successful, {} failed",
                result.getSuccessfulCount(), result.getFailedCount());
        return result;
    }

    /**
     * Process a single galaxy entry.
     *
     * @param entry the entry to process
     * @return the created galaxy, or null if it already exists
     */
    private Galaxy processEntry(GalaxyStartupEntry entry) {
        String galaxyName = entry.getGalaxyName();
        GalaxyTemplate template = entry.getTemplate();
        GameType gameType = entry.getGameType();

        // Check if galaxy already exists
        if (galaxyService.getGalaxyByName(galaxyName).isPresent()) {
            log.info("Galaxy '{}' already exists, initializing game modes and overlay", galaxyName);
            initializeAllGameModes(galaxyName, entry.getGameModes(), entry.getLoreSlug(), gameType);
            applyOverlay(galaxyName, gameType, template.getConfig());
            return null;
        }

        // Get the template configuration
        GalaxyGenerationConfig config = template.getConfig();
        log.info("Generating galaxy '{}' from template {} with config: sectors={}, systems={}",
                galaxyName, template.name(), config.getSectorSize(), config.getSystemCount());

        // Generate a deterministic seed based on galaxy name for reproducibility
        long seed = galaxyName.hashCode();
        if (seed == 0) {
            seed = 1L;
        }

        // Generate the galaxy with empires
        Map<Integer, Empire> empires = new HashMap<>();
        // Add default AI empires for the galaxy (Empire constructor: id, name, rulerPlayerId)
        empires.put(1, new Empire(1, "AI Empire 1", 0));
        empires.put(2, new Empire(2, "AI Empire 2", 0));
        empires.put(3, new Empire(3, "AI Empire 3", 0));

        GalaxyService.GalaxyGenerationResult result = galaxyService.generateAndSaveGalaxyFromTemplate(
                galaxyName,
                empires,
                gameType.name(),
                entry.getLoreSlug(),
                seed,
                config.getSystemCount(),
                config.getPlanetsPerSystem(),
                config.getMoonChance(),
                config.getAsteroidBeltChance(),
                config.getGasGiantChance(),
                config.getNebulaChance(),
                config.getBlackHoleChance(),
                config.getCometChance(),
                config.getSpaceStationChance(),
                config.getHyperlaneDensity()
        );

        // Phase 1: Initialize ALL game modes for this galaxy
        initializeAllGameModes(galaxyName, entry.getGameModes(), entry.getLoreSlug(), gameType);

        // Phase 2: Apply game-specific overlay (themed empires, lore systems)
        applyOverlay(galaxyName, gameType, config);

        return result.getDatabaseGalaxy();
    }

    /**
     * Initialize ALL game modes for a galaxy using GameModeRegistry.
     * This ensures every galaxy supports RPG, 4X, FPS, Tycoon, and all other modes.
     *
     * @param galaxyName the galaxy name
     * @param gameModes the set of game modes to initialize
     * @param loreSlug the lore slug
     * @param gameType the primary game type for this galaxy
     */
    private void initializeAllGameModes(String galaxyName, Set<GameMode> gameModes, String loreSlug, GameType gameType) {
        log.info("Initializing {} game modes for galaxy: {}", gameModes.size(), galaxyName);
        gameModeRegistry.initializeModulesForGalaxy(galaxyName, gameModes, loreSlug);

        // Also initialize the GameModule for this galaxy's primary game type
        GameModule module = gameModuleRegistry.getModule(gameType).orElse(null);
        if (module != null && module.isEnabled()) {
            try {
                module.initializeForGalaxy(galaxyName, loreSlug);
                log.info("Initialized GameModule {} for galaxy {}", module.getModuleName(), galaxyName);
            } catch (Exception e) {
                log.error("Failed to initialize GameModule {} for galaxy {}: {}",
                        module.getModuleName(), galaxyName, e.getMessage());
            }
        }
    }

    /**
     * Apply game-specific overlay (themed empires, lore systems, AI NPCs).
     * Looks up the registered overlay builder and NPC populator for the galaxy's game type.
     * Uses a lightweight Galaxy stub instead of regenerating the full galaxy to avoid OOM.
     *
     * @param galaxyName the galaxy name
     * @param gameType the game type
     * @param config the galaxy generation config
     */
    private void applyOverlay(String galaxyName, GameType gameType, GalaxyGenerationConfig config) {
        // Create a lightweight Galaxy stub for overlay builders (avoids regenerating 20000 systems)
        com.yisy.modules.doramcu.hex.Galaxy galaxyStub = new com.yisy.modules.doramcu.hex.Galaxy(0, galaxyName);

        // Phase 2a: Game overlay builder
        overlayBuilderRegistry.getOverlayBuilder(gameType).ifPresent(builder -> {
            try {
                builder.buildOverlay(galaxyStub, config);
                log.info("Applied overlay builder {} for galaxy {}", builder.getClass().getSimpleName(), galaxyName);
            } catch (Exception e) {
                log.error("Overlay builder failed for galaxy {}: {}", galaxyName, e.getMessage(), e);
            }
        });

        // Phase 2b: AI NPC populator
        overlayBuilderRegistry.getNpcPopulator(gameType).ifPresent(populator -> {
            try {
                populator.populateNpcs(galaxyStub);
                log.info("Applied NPC populator {} for galaxy {}", populator.getClass().getSimpleName(), galaxyName);
            } catch (Exception e) {
                log.error("NPC populator failed for galaxy {}: {}", galaxyName, e.getMessage(), e);
            }
        });

        if (!overlayBuilderRegistry.hasOverlayBuilder(gameType)) {
            log.info("No overlay builder registered for game type {} — galaxy {} uses base 4X only", gameType, galaxyName);
        }
        if (!overlayBuilderRegistry.hasNpcPopulator(gameType)) {
            log.info("No NPC populator registered for game type {} — galaxy {} has no themed NPCs", gameType, galaxyName);
        }
    }

    /**
     * Result of processing the multi-galaxy build queue.
     */
    public static class Result {
        private final int successfulCount;
        private final int failedCount;
        private final List<String> successfulGalaxies;
        private final List<String> failedGalaxies;

        public Result(int successfulCount, int failedCount,
                      List<String> successfulGalaxies, List<String> failedGalaxies) {
            this.successfulCount = successfulCount;
            this.failedCount = failedCount;
            this.successfulGalaxies = successfulGalaxies;
            this.failedGalaxies = failedGalaxies;
        }

        public int getSuccessfulCount() {
            return successfulCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public List<String> getSuccessfulGalaxies() {
            return successfulGalaxies;
        }

        public List<String> getFailedGalaxies() {
            return failedGalaxies;
        }

        @Override
        public String toString() {
            return "Result{successful=" + successfulCount + ", failed=" + failedCount +
                    ", galaxies=" + successfulGalaxies + "}";
        }
    }
}
