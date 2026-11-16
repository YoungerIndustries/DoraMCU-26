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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
     */
    private static final Map<GameType, GalaxyRegistration> DEFAULT_REGISTRATIONS = Map.ofEntries(
            Map.entry(GameType.EARTH_2026, new GalaxyRegistration(1, "Earth 2026", GalaxyTemplate.EARTH_2026_TEMPLATE, "earth-2026")),
            Map.entry(GameType.TRAVELLER, new GalaxyRegistration(2, "Traveller Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "traveller")),
            Map.entry(GameType.DND_5E, new GalaxyRegistration(3, "Forgotten Realms", GalaxyTemplate.TRAVELLER_CLASSIC, "dnd-5e")),
            Map.entry(GameType.STAR_TREK_4X, new GalaxyRegistration(4, "Star Trek Galaxy", GalaxyTemplate.STAR_TREK, "star-trek")),
            Map.entry(GameType.BABYLON_5, new GalaxyRegistration(5, "Babylon 5 Galaxy", GalaxyTemplate.BABYLON5, "babylon5")),
            Map.entry(GameType.STAR_WARS, new GalaxyRegistration(6, "Star Wars Galaxy", GalaxyTemplate.TRAVELLER_CLASSIC, "star-wars")),
            Map.entry(GameType.DORAMCU_4X, new GalaxyRegistration(7, "DoraMCU 4X Galaxy", GalaxyTemplate.DORAMCU_4X_TEMPLATE, "doramcu-4x")),
            Map.entry(GameType.WARHAMMER_40K, new GalaxyRegistration(8, "Warhammer 40K Galaxy", GalaxyTemplate.VOID, "warhammer-40k")),
            Map.entry(GameType.CAPTAINS_LOG, new GalaxyRegistration(9, "Captain's Log Galaxy", GalaxyTemplate.STAR_TREK, "captains-log")),
            Map.entry(GameType.SFB, new GalaxyRegistration(10, "Star Frontiers Galaxy", GalaxyTemplate.FRONTIER, "star-frontiers")),
            Map.entry(GameType.MECHWARRIOR, new GalaxyRegistration(11, "MechWarrior Galaxy", GalaxyTemplate.FRONTIER, "mechwarrior")),
            Map.entry(GameType.STARSHIP_TROOPERS, new GalaxyRegistration(12, "Starship Troopers Galaxy", GalaxyTemplate.FRONTIER, "starship-troopers")),
            Map.entry(GameType.THIRD_REICH, new GalaxyRegistration(13, "Third Reich Galaxy", GalaxyTemplate.EARTH_2026_TEMPLATE, "third-reich")),
            Map.entry(GameType.ZERO_ZERO_SEVEN, new GalaxyRegistration(14, "007 Espionage Galaxy", GalaxyTemplate.EARTH_2026_TEMPLATE, "007")),
            Map.entry(GameType.TRANSPORT_TYCOON, new GalaxyRegistration(15, "Transport Tycoon Galaxy", GalaxyTemplate.DORAMCU_4X_TEMPLATE, "transport-tycoon")),
            Map.entry(GameType.FPS, new GalaxyRegistration(16, "FPS Combat Galaxy", GalaxyTemplate.EARTH_2026_TEMPLATE, "fps-combat"))
    );

    private final MultiGalaxyBuildQueue buildQueue;
    private final GameModuleRegistry gameModuleRegistry;

    public GalaxyStartupRegistry(MultiGalaxyBuildQueue buildQueue,
                                  GameModuleRegistry gameModuleRegistry) {
        this.buildQueue = buildQueue;
        this.gameModuleRegistry = gameModuleRegistry;
    }

    /**
     * Registers all galaxy entries with the build queue and processes them.
     * Uses ApplicationReadyEvent with LOWEST_VALUE order to ensure it fires
     * AFTER all game modules have registered themselves (they use default order).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void registerAndProcessGalaxies() {
        log.info("All application components ready — registering and processing multi-galaxy build queue");
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
                    registration.loreSlug
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
     * Defines a galaxy registration for a game module.
     */
    public static class GalaxyRegistration {
        private final int priority;
        private final String galaxyName;
        private final GalaxyTemplate template;
        private final String loreSlug;

        public GalaxyRegistration(int priority, String galaxyName,
                                   GalaxyTemplate template, String loreSlug) {
            this.priority = priority;
            this.galaxyName = galaxyName;
            this.template = template;
            this.loreSlug = loreSlug;
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
    }
}
