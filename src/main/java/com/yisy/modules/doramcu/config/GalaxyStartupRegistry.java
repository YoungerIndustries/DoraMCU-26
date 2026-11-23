package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auto-discovers game modules and registers their galaxy entries with the
 * {@link MultiGalaxyBuildQueue} at startup.
 * <p>
 * This component uses a registration map to define which game modules provide
 * galaxies at startup, along with their priority order and template. The default
 * configuration includes:
 * <ul>
 *   <li>Earth2026 (priority 1) - EARTH_2026_TEMPLATE</li>
 *   <li>Traveller (priority 2) - TRAVELLER_CLASSIC</li>
 *   <li>D&D 5E (priority 3) - TRAVELLER_CLASSIC</li>
 *   <li>Star Trek 4X (priority 4) - STAR_TREK</li>
 *   <li>Babylon 5 (priority 5) - BABYLON5</li>
 * </ul>
 * </p>
 * <p>
 * The registry provides a {@link #registerAndProcessAll()} method that should be
 * called by {@link DataInitializer} after all game modules have registered.
 * </p>
 * <p>
 * Configuration:
 * <pre>
 * # Enable/disable multi-galaxy startup (default: false)
 * doramcu.galaxy.multi-galaxy.enabled=true
 *
 * # Override individual module galaxy creation
 * doramcu.galaxy.modules.earth2026.enabled=true
 * doramcu.galaxy.modules.traveller.enabled=true
 * doramcu.galaxy.modules.dnd5e.enabled=true
 * </pre>
 * </p>
 */
@Component
@ConditionalOnProperty(name = "doramcu.galaxy.multi-galaxy.enabled", havingValue = "true", matchIfMissing = false)
public class GalaxyStartupRegistry {

    private static final Logger log = LoggerFactory.getLogger(GalaxyStartupRegistry.class);

    /**
     * Default galaxy registration map: GameType -> registration function.
     * Defines which modules provide galaxies and their startup priority.
     * <p>
     * Galaxy templates are chosen based on game type structure:
     * <ul>
     *   <li>Tier 1 (Full galaxies): Traveller, Star Wars, Star Trek 4X, Warhammer 40K, Babylon 5, DoraMCU 4X</li>
     *   <li>Tier 2 (Modified galaxies): Star Frontiers, MechWarrior, 007, Captain's Log</li>
     *   <li>Tier 3 (Single system): D&D 5E, Lord of the Rings, Starship Troopers</li>
     * </ul>
     * <p>
     * Tier 4 game types (Earth 2026, Third Reich, Transport Tycoon, FPS) do NOT have
     * separate galaxies — they are layers on the Earth 2026 hub galaxy.
     */
    private static final Map<GameType, GalaxyRegistration> DEFAULT_REGISTRATIONS = Map.ofEntries(
            // Tier 1: Full galaxy games (space-faring, multi-sector)
            Map.entry(GameType.EARTH_2026, new GalaxyRegistration(1, "Earth 2026", GalaxyTemplate.EARTH_2026_TEMPLATE, "earth-2026",
                    "Medium", "Near-Future Sci-Fi", "Modern Earth and near-space operations. Start at Younger Industries HQ in New York and explore the solar system.")),
            Map.entry(GameType.TRAVELLER, new GalaxyRegistration(2, "Traveller Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "traveller",
                    "Hard", "Classic Space Opera", "The Third Imperium and beyond. Explore procedurally generated star systems in the tradition of classic tabletop space travel.")),
            Map.entry(GameType.STAR_WARS, new GalaxyRegistration(3, "Star Wars Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "star-wars",
                    "Medium", "Space Fantasy", "A galaxy far, far away. Navigate the Outer Rim, Core Worlds, and everything between in the Star Wars universe.")),
            Map.entry(GameType.STAR_TREK_4X, new GalaxyRegistration(4, "Star Trek Galaxy", GalaxyTemplate.STAR_TREK, "star-trek",
                    "Medium", "Exploration Diplomacy", "The Alpha and Beta Quadrants await. Command starships, explore new worlds, and uphold the ideals of Starfleet.")),
            Map.entry(GameType.WARHAMMER_40K, new GalaxyRegistration(5, "Warhammer 40K Galaxy", GalaxyTemplate.VOID, "warhammer-40k",
                    "Extreme", "Grimdark Warfare", "In the grim darkness of the far future, there is only war. Survive the galactic conflicts of the 41st millennium.")),
            Map.entry(GameType.BABYLON_5, new GalaxyRegistration(6, "Babylon 5 Galaxy", GalaxyTemplate.BABYLON5, "babylon5",
                    "Medium", "Space Drama", "The Babylon 5 universe. Political intrigue, alien alliances, and the fate of humanity hang in the balance.")),
            Map.entry(GameType.DORAMCU_4X, new GalaxyRegistration(7, "DoraMCU 4X Galaxy", GalaxyTemplate.DORAMCU_4X_TEMPLATE, "doramcu-4x",
                    "Medium", "Grand Strategy", "The flagship DoraMCU 4X experience. Explore, expand, exploit, and exterminate across the stars.")),

            // Tier 2: Modified galaxy games (space-faring, reduced scale)
            Map.entry(GameType.SFB, new GalaxyRegistration(8, "Star Frontiers Galaxy", GalaxyTemplate.FRONTIER, "star-frontiers",
                    "Hard", "Frontier Exploration", "The Vega Frontier. Pioneers, corporations, and alien threats in a classic 80s sci-fi setting.")),
            Map.entry(GameType.MECHWARRIOR, new GalaxyRegistration(9, "MechWarrior Galaxy", GalaxyTemplate.FRONTIER, "mechwarrior",
                    "Hard", "Mech Warfare", "The Succession Wars. Command BattleMechs across the Inner Sphere in epic armored combat.")),
            Map.entry(GameType.ZERO_ZERO_SEVEN, new GalaxyRegistration(10, "007 Espionage", GalaxyTemplate.EARTH_2026_TEMPLATE, "007",
                    "Medium", "Espionage Thriller", "International espionage and covert operations. Play as a secret agent in a modern-day thriller.")),
            Map.entry(GameType.CAPTAINS_LOG, new GalaxyRegistration(11, "Captain's Log", GalaxyTemplate.SUBSECTOR_RPG, "captains-log",
                    "Easy", "Space Adventure", "Chart your course through uncharted space. A captain's adventure of exploration and discovery.")),

            // Tier 3: Single system games (planetary focus)
            Map.entry(GameType.DND_5E, new GalaxyRegistration(12, "Forgotten Realms", GalaxyTemplate.SINGLE_SYSTEM_RPG, "dnd-5e",
                    "Medium", "High Fantasy", "The Forgotten Realms campaign setting. Classic D&D 5E adventure with dragons, dungeons, and divine magic.")),
            Map.entry(GameType.LORD_OF_THE_RINGS, new GalaxyRegistration(13, "Middle-earth", GalaxyTemplate.SINGLE_SYSTEM_RPG, "lotr",
                    "Medium", "Epic Fantasy", "J.R.R. Tolkien's Middle-earth. From the Shire to Mordor, embark on the quest to save the world.")),
            Map.entry(GameType.STARSHIP_TROOPERS, new GalaxyRegistration(14, "Starship Troopers", GalaxyTemplate.SINGLE_SYSTEM_RPG, "starship-troopers",
                    "Hard", "Military Sci-Fi", "The Federated Solar Systems fight for survival against the Bug menace. Serve in the Mobile Infantry."))
    );

    private final MultiGalaxyBuildQueue buildQueue;
    private final GameModuleRegistry gameModuleRegistry;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final com.yisy.modules.doramcu.service.galaxy.GalaxyService galaxyService;

    public GalaxyStartupRegistry(MultiGalaxyBuildQueue buildQueue,
                                  GameModuleRegistry gameModuleRegistry,
                                  ThreadPoolTaskExecutor taskExecutor,
                                  com.yisy.modules.doramcu.service.galaxy.GalaxyService galaxyService) {
        this.buildQueue = buildQueue;
        this.gameModuleRegistry = gameModuleRegistry;
        this.taskExecutor = taskExecutor;
        this.galaxyService = galaxyService;
    }

    /**
     * Registers all galaxy entries with the build queue and processes them.
     * Uses ApplicationReadyEvent with LOWEST_VALUE order to ensure it fires
     * AFTER all game modules have registered themselves (they use default order).
     * Runs asynchronously so Tomcat can start immediately without waiting
     * for all galaxies to initialize.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void registerAndProcessGalaxies() {
        log.info("All application components ready — submitting multi-galaxy build queue asynchronously");
        taskExecutor.submit(() -> {
            try {
                log.info("Starting multi-galaxy build queue in background thread");
                registerGalaxyEntries();
                MultiGalaxyBuildQueue.Result result = buildQueue.processAll();
                log.info("Multi-galaxy build queue completed: {} successful, {} failed",
                        result.getSuccessfulCount(), result.getFailedCount());
                for (String galaxyName : result.getSuccessfulGalaxies()) {
                    log.info("  ✓ Galaxy '{}' is ready for gameplay", galaxyName);
                }
                if (!result.getFailedGalaxies().isEmpty()) {
                    log.warn("  Failed galaxies: {}", result.getFailedGalaxies());
                }
                
                // Clean up orphaned galaxies not in the registry
                cleanupOrphanedGalaxies();
            } catch (Exception e) {
                log.error("Multi-galaxy build queue failed: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Registers all galaxy entries with the build queue.
     * This method is idempotent: calling it multiple times is safe.
     */
    public void registerGalaxyEntries() {
        log.info("Registering galaxy startup entries for multi-galaxy build queue");

        List<GalaxyStartupEntry> entries = new ArrayList<>();

        for (Map.Entry<GameType, GalaxyRegistration> entry : DEFAULT_REGISTRATIONS.entrySet()) {
            GameType gameType = entry.getKey();
            GalaxyRegistration registration = entry.getValue();

            // Check if the module is registered and enabled (optional — galaxy can exist without a module)
            GameModule module = gameModuleRegistry.getModule(gameType).orElse(null);
            if (module != null && !module.isEnabled()) {
                log.debug("Skipping galaxy for {} - module explicitly disabled", gameType.getDisplayName());
                continue;
            }

            GalaxyStartupEntry startupEntry = GalaxyStartupEntry.of(
                    registration.priority,
                    registration.galaxyName,
                    registration.template,
                    gameType,
                    registration.loreSlug,
                    registration.difficulty,
                    registration.theme,
                    registration.description
            );

            entries.add(startupEntry);
            log.info("Registered galaxy entry: {}", startupEntry);
        }

        buildQueue.registerEntries(entries);
        log.info("Total galaxy entries registered: {}", buildQueue.getEntryCount());
    }


    /**
     * Returns the default registrations map.
     *
     * @return unmodifiable map of game type to registration
     */
    public Map<GameType, GalaxyRegistration> getDefaultRegistrations() {
        return Map.copyOf(DEFAULT_REGISTRATIONS);
    }

    /**
     * Looks up the startup priority for a galaxy by name.
     * Returns 999 (lowest priority) if the galaxy is not found in the registry.
     *
     * @param galaxyName the name of the galaxy
     * @return the startup priority (lower = higher priority), or 999 if not found
     */
    public static int getPriorityByGalaxyName(String galaxyName) {
        if (galaxyName == null) return 999;
        for (GalaxyRegistration reg : DEFAULT_REGISTRATIONS.values()) {
            if (reg.getGalaxyName().equals(galaxyName)) {
                return reg.getPriority();
            }
        }
        return 999;
    }

    /**
     * Deletes any galaxies that are not registered in {@link #DEFAULT_REGISTRATIONS}.
     * This cleans up orphaned galaxies from old creation services (e.g., "Milky Way" from
     * the deprecated {@link com.yisy.modules.doramcu.hex.GalaxyFactory#GAME_GALAXY_NAME}).
     * <p>
     * Called after the multi-galaxy build queue completes to ensure only registered
     * galaxies remain in the database.
     */
    public void cleanupOrphanedGalaxies() {
        Set<String> registeredNames = DEFAULT_REGISTRATIONS.values().stream()
                .map(GalaxyRegistration::getGalaxyName)
                .collect(Collectors.toSet());

        log.info("Checking for orphaned galaxies... (registered: {})", registeredNames);

        List<com.yisy.modules.doramcu.model.galaxy.Galaxy> allGalaxies = galaxyService.getAllGalaxies();
        int deleted = 0;
        for (com.yisy.modules.doramcu.model.galaxy.Galaxy galaxy : allGalaxies) {
            if (!registeredNames.contains(galaxy.getName())) {
                log.warn("Deleting orphaned galaxy '{}' (id={}) — not in GalaxyStartupRegistry", galaxy.getName(), galaxy.getId());
                try {
                    galaxyService.deleteGalaxy(galaxy.getName());
                    deleted++;
                } catch (Exception e) {
                    log.error("Failed to delete orphaned galaxy '{}': {}", galaxy.getName(), e.getMessage());
                }
            }
        }
        
        if (deleted > 0) {
            log.info("Cleaned up {} orphaned galaxy(ies). Remaining: {} registered galaxies.", deleted, registeredNames.size());
        } else {
            log.info("No orphaned galaxies found. All {} galaxies are registered.", allGalaxies.size());
        }
    }

    /**
     * Defines a galaxy registration for a game module.
     */
    public static class GalaxyRegistration {
        private final int priority;
        private final String galaxyName;
        private final GalaxyTemplate template;
        private final String loreSlug;
        private final String difficulty;
        private final String theme;
        private final String description;

        public GalaxyRegistration(int priority, String galaxyName,
                                   GalaxyTemplate template, String loreSlug,
                                   String difficulty, String theme, String description) {
            this.priority = priority;
            this.galaxyName = galaxyName;
            this.template = template;
            this.loreSlug = loreSlug;
            this.difficulty = difficulty;
            this.theme = theme;
            this.description = description;
        }

        public int getPriority() {
            return priority;
        }

        public String getGalaxyName() {
            return galaxyName;
        }

        public GalaxyTemplate getTemplate() {
            return template;
        }

        public String getLoreSlug() {
            return loreSlug;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public String getTheme() {
            return theme;
        }

        public String getDescription() {
            return description;
        }
    }
}
