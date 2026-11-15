package com.yisy.modules.doramcu.earth2026.data;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Scenario;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ScenarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with Earth 2026 locations and scenarios.
 * <p>
 * This initializer runs in dev mode when Flyway migrations are disabled.
 * In production, the V30 Flyway migration handles this data.
 * </p>
 */
@Component
public class Earth2026LocationScenarioInitializer {

    private static final Logger log = LoggerFactory.getLogger(Earth2026LocationScenarioInitializer.class);

    private final Earth2026LocationRepository locationRepository;
    private final Earth2026ScenarioRepository scenarioRepository;

    @Autowired
    public Earth2026LocationScenarioInitializer(Earth2026LocationRepository locationRepository,
                                                 Earth2026ScenarioRepository scenarioRepository) {
        this.locationRepository = locationRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        // Skip if data already exists (e.g., from Flyway migration)
        if (locationRepository.count() > 0) {
            log.info("Earth 2026 locations already seeded (count: {}), skipping.", locationRepository.count());
            return;
        }

        log.info("Seeding Earth 2026 locations and scenarios...");

        // === Seed Locations ===
        List<Earth2026Location> locations = new ArrayList<>();

        locations.add(createLocation(
                "San Diego Convention Center", "San Diego", "CA", "USA", "Landmark",
                "Starting location for new characters. A bustling convention center where characters first arrive.",
                false, null, false, 32.7095, -117.1601
        ));

        locations.add(createLocation(
                "Younger Industries HQ", "Spring Valley", "CA", "USA", "Corporate",
                "Headquarters of Younger Industries at 4906 Campo Road, Spring Valley, San Diego, CA 91977. " +
                        "CEO Butch Younger and AI VP Sif Younger operate from here. " +
                        "Accessible via Route 922 bus to Mission Valley, then Blue Line Trolley to downtown.",
                false, null, false, 32.9356, -117.0347
        ));

        locations.add(createLocation(
                "The Portal Room", "San Diego", "CA", "USA", "Portal",
                "A secret room containing doors to other galaxies. Characters can choose their destination here.",
                true, "Milky Way", true, 32.7100, -117.1620
        ));

        locations.add(createLocation(
                "Area 51", "Rachel", "NV", "USA", "Secret Base",
                "Top-secret military base. Rumored to have alien technology and portals.",
                true, "The Void", true, 37.2350, -115.8090
        ));

        locations.add(createLocation(
                "New York City", "New York", "NY", "USA", "City",
                "The city that never sleeps. Full of opportunities and mysteries.",
                false, null, false, 40.7128, -74.0060
        ));

        locations.add(createLocation(
                "London", "London", null, "UK", "City",
                "Historic city with ancient secrets and modern technology.",
                false, null, false, 51.5074, -0.1278
        ));

        locations.add(createLocation(
                "Tokyo", "Tokyo", null, "Japan", "City",
                "Futuristic metropolis blending tradition with cutting-edge technology.",
                false, null, false, 35.6762, 139.6503
        ));

        locations.add(createLocation(
                "Cthulhu's Call", "Arkham", "MA", "USA", "Supernatural",
                "A mysterious location where the veil between worlds is thin.",
                true, "Earth 2026", true, 42.3601, -71.0589
        ));

        locationRepository.saveAll(locations);
        log.info("Seeded {} Earth 2026 locations.", locations.size());

        // === Seed Scenarios ===
        List<Earth2026Scenario> scenarios = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        scenarios.add(createScenario(
                "The Missing Package",
                "A package has gone missing from Younger Industries. Investigate and recover it.",
                "Investigation", 2, 1, 50, locations.get(1), false, null, null, 60, now
        ));

        scenarios.add(createScenario(
                "Convention Mystery",
                "Strange things are happening at the convention center. Discover what's going on.",
                "Mystery", 3, 1, 75, locations.get(0), false, null, null, 90, now
        ));

        scenarios.add(createScenario(
                "First Contact",
                "An alien artifact has been discovered. Determine its origin and purpose.",
                "Supernatural", 5, 3, 150, locations.get(3), true, 10, null, 120, now
        ));

        scenarios.add(createScenario(
                "Street Smarts",
                "Navigate the streets of New York and complete a series of challenges.",
                "Social", 2, 1, 50, locations.get(4), false, null, null, 45, now
        ));

        scenarios.add(createScenario(
                "The Portal Guardian",
                "A mysterious guardian blocks the portal. Prove yourself worthy to pass through.",
                "Combat", 4, 2, 100, locations.get(2), false, null, null, 90, now
        ));

        scenarios.add(createScenario(
                "Whispers in the Dark",
                "Strange whispers lead you to a hidden cult. Investigate before it's too late.",
                "Supernatural", 6, 4, 200, locations.get(7), true, 15, null, 120, now
        ));

        scenarioRepository.saveAll(scenarios);
        log.info("Seeded {} Earth 2026 scenarios.", scenarios.size());
        log.info("Earth 2026 seed data initialization complete.");
    }

    private Earth2026Location createLocation(String name, String city, String state, String country,
                                              String type, String description, boolean hasPortal,
                                              String portalGalaxy, boolean isSecret,
                                              Double latitude, Double longitude) {
        Earth2026Location location = new Earth2026Location();
        location.setName(name);
        location.setCity(city);
        location.setState(state);
        location.setCountry(country);
        location.setType(type);
        location.setDescription(description);
        location.setHasPortal(hasPortal);
        location.setPortalGalaxy(portalGalaxy);
        location.setSecret(isSecret);
        location.setActive(true);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    private Earth2026Scenario createScenario(String name, String description, String type,
                                              int difficulty, int minLevel, int xpReward,
                                              Earth2026Location location, boolean supernatural,
                                              Integer sanityLoss, Integer partySize,
                                              Integer durationMinutes, LocalDateTime createdAt) {
        Earth2026Scenario scenario = new Earth2026Scenario();
        scenario.setName(name);
        scenario.setDescription(description);
        scenario.setType(type);
        scenario.setStatus(Earth2026Scenario.ScenarioStatus.AVAILABLE);
        scenario.setDifficulty(difficulty);
        scenario.setMinLevel(minLevel);
        scenario.setXpReward(xpReward);
        scenario.setLocation(location);
        scenario.setSupernatural(supernatural);
        scenario.setSanityLoss(sanityLoss);
        scenario.setPartySize(partySize);
        scenario.setDurationMinutes(durationMinutes);
        scenario.setActive(true);
        scenario.setCreatedAt(createdAt);
        scenario.setUpdatedAt(createdAt);
        return scenario;
    }
}
