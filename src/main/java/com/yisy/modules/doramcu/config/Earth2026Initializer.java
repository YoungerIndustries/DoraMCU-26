package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ScenarioRepository;
import com.yisy.modules.doramcu.gateway.PluginGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the Earth 2026 Hub as the core starting module before any galaxy generation.
 * <p>
 * This CommandLineRunner executes early in the startup sequence, after seed data
 * is loaded but before galaxy generation. It verifies that Earth 2026 locations
 * and scenarios are available (seeded by Flyway migration V30) and ensures the
 * Plugin Gateway is ready for lazy-loading game modules.
 * </p>
 * <p>
 * Earth 2026 is the default starting point for all characters. New users arrive
 * on Earth 2026 and can play modern-day scenarios before choosing to transition
 * to a game galaxy via a portal. Galaxies are created dynamically via GalaxyCreationService.
 * </p>
 */
@Configuration
public class Earth2026Initializer {

    private static final Logger log = LoggerFactory.getLogger(Earth2026Initializer.class);

    private final StartupProgressService startupProgress;

    public Earth2026Initializer(StartupProgressService startupProgress) {
        this.startupProgress = startupProgress;
    }

    /**
     * Initializes Earth 2026 Hub as the core module.
     * <p>
     * This runs after seed data (step 4) and before galaxy generation (step 5).
     * It verifies that Earth 2026 locations and scenarios are seeded and logs
     * the available portal locations for galaxy transitions.
     * </p>
     *
     * @param locationRepository the Earth 2026 location repository
     * @param scenarioRepository the Earth 2026 scenario repository
     * @param pluginGateway the plugin gateway for game module discovery
     * @return a CommandLineRunner that initializes Earth 2026
     */
    @Bean
    CommandLineRunner initEarth2026Hub(Earth2026LocationRepository locationRepository,
                                        Earth2026ScenarioRepository scenarioRepository,
                                        PluginGateway pluginGateway) {
        return args -> {
            startupProgress.startStep("earth_2026_hub", "Initializing Earth 2026 Hub...");
            log.info("=== Initializing Earth 2026 Hub as core starting module ===");

            // Verify Earth 2026 locations are seeded (by Flyway V30 migration)
            long locationCount = locationRepository.count();
            log.info("Earth 2026 locations available: {}", locationCount);

            // Verify portal locations for galaxy transitions
            long portalCount = locationRepository.countByHasPortalTrue();
            log.info("Portal locations available: {}", portalCount);
            locationRepository.findByHasPortalTrue().forEach(location ->
                    log.info("  Portal: {} -> {}", location.getName(), location.getPortalGalaxy())
            );

            // Verify Earth 2026 scenarios are seeded (by Flyway V30 migration)
            long scenarioCount = scenarioRepository.count();
            log.info("Earth 2026 scenarios available: {}", scenarioCount);

            // Verify Plugin Gateway is ready
            long pluginCount = pluginGateway.getAvailablePlugins().size();
            log.info("Game plugins registered: {}", pluginCount);
            long enabledPluginCount = pluginGateway.getEnabledPlugins().size();
            log.info("Enabled plugins (core module): {}", enabledPluginCount);

            startupProgress.completeStep("earth_2026_hub",
                    "Earth 2026 Hub initialized with " + locationCount + " locations, " +
                    scenarioCount + " scenarios, " + portalCount + " portals");
            log.info("=== Earth 2026 Hub initialization complete ===");
        };
    }
}

