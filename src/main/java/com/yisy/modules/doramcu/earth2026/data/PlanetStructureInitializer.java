package com.yisy.modules.doramcu.earth2026.data;

import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.repository.PlanetStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with initial planet structures for San Diego, Earth.
 * <p>
 * This initializer runs in dev mode when Flyway migrations are disabled.
 * In production, the Flyway migration handles this data.
 * </p>
 */
@Component
public class PlanetStructureInitializer {

    private static final Logger log = LoggerFactory.getLogger(PlanetStructureInitializer.class);

    // Planet ID for Earth (San Diego)
    private static final Long EARTH_PLANET_ID = 1L;

    // San Diego coordinates
    private static final double SD_LAT = 32.7157;
    private static final double SD_LON = -117.1611;

    private final PlanetStructureRepository planetStructureRepository;

    public PlanetStructureInitializer(PlanetStructureRepository planetStructureRepository) {
        this.planetStructureRepository = planetStructureRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedPlanetStructures() {
        if (planetStructureRepository.count() > 0) {
            log.info("Planet structures already seeded, skipping.");
            return;
        }

        log.info("Seeding planet structures for San Diego, Earth...");

        // Mines
        createStructure("San Diego Iron Mine", PlanetStructure.StructureType.MINE,
                EARTH_PLANET_ID, SD_LAT + 0.15, SD_LON - 0.1, 500.0,
                "{\"iron_ore\": 5000, \"steel\": 0}", 1000,
                "Large iron ore mine in the hills north of San Diego");

        createStructure("Mira Mesa Copper Mine", PlanetStructure.StructureType.MINE,
                EARTH_PLANET_ID, SD_LAT + 0.08, SD_LON - 0.05, 300.0,
                "{\"copper_ore\": 3000, \"copper\": 0}", 800,
                "Copper mine in Mira Mesa area");

        createStructure("Spring Valley Gold Mine", PlanetStructure.StructureType.MINE,
                EARTH_PLANET_ID, SD_LAT + 0.22, SD_LON - 0.08, 600.0,
                "{\"gold_ore\": 500, \"gold\": 0}", 200,
                "Small gold mine near Spring Valley");

        // Equipment Stores
        createStructure("San Diego Equipment Depot", PlanetStructure.StructureType.EQUIPMENT_STORE,
                EARTH_PLANET_ID, SD_LAT - 0.01, SD_LON + 0.01, 20.0,
                "{\"tools\": 100, \"weapons\": 50, \"supplies\": 200}", 500,
                "Main equipment store in downtown San Diego");

        createStructure("Mission Valley Supply Store", PlanetStructure.StructureType.EQUIPMENT_STORE,
                EARTH_PLANET_ID, SD_LAT - 0.03, SD_LON - 0.02, 15.0,
                "{\"tools\": 50, \"supplies\": 150, \"food\": 300}", 400,
                "Supply store in Mission Valley");

        // Transport Stations
        createStructure("San Diego Central Station", PlanetStructure.StructureType.TRANSPORT_STATION,
                EARTH_PLANET_ID, SD_LAT - 0.005, SD_LON + 0.005, 10.0,
                "{}", 2000,
                "Main transport hub for buses, trolleys, and trains in downtown San Diego");

        createStructure("Mira Mesa Transit Hub", PlanetStructure.StructureType.TRANSPORT_STATION,
                EARTH_PLANET_ID, SD_LAT + 0.08, SD_LON - 0.03, 25.0,
                "{}", 1000,
                "Transit hub connecting Mira Mesa to downtown");

        createStructure("Spring Valley Station", PlanetStructure.StructureType.TRANSPORT_STATION,
                EARTH_PLANET_ID, SD_LAT + 0.22, SD_LON - 0.06, 400.0,
                "{}", 500,
                "Transport station in Spring Valley near Younger Industries HQ");

        // Spaceport
        createStructure("San Diego Spaceport", PlanetStructure.StructureType.SPACEPORT,
                EARTH_PLANET_ID, SD_LAT - 0.15, SD_LON - 0.2, 50.0,
                "{\"fuel\": 10000, \"parts\": 100}", 5000,
                "Main spaceport for interplanetary travel and moon missions");

        // Factories
        createStructure("San Diego Steel Factory", PlanetStructure.StructureType.FACTORY,
                EARTH_PLANET_ID, SD_LAT - 0.05, SD_LON - 0.05, 30.0,
                "{\"steel\": 0, \"iron_ore\": 0}", 2000,
                "Factory that processes iron ore into steel beams and components");

        createStructure("Electronics Manufacturing Plant", PlanetStructure.StructureType.FACTORY,
                EARTH_PLANET_ID, SD_LAT + 0.05, SD_LON - 0.02, 20.0,
                "{\"circuit_boards\": 0, \"copper\": 0}", 1500,
                "Factory that manufactures electronic components and devices");

        // Residences
        createStructure("Younger Industries HQ", PlanetStructure.StructureType.RESIDENCE,
                EARTH_PLANET_ID, SD_LAT + 0.22, SD_LON - 0.07, 450.0,
                "{}", 100,
                "Headquarters of Younger Industries in Spring Valley");

        createStructure("San Diego Convention Center", PlanetStructure.StructureType.RESIDENCE,
                EARTH_PLANET_ID, SD_LAT - 0.01, SD_LON + 0.01, 10.0,
                "{}", 5000,
                "Convention center and meeting place in downtown San Diego");

        log.info("Seeded {} planet structures for San Diego, Earth.", planetStructureRepository.count());
    }

    private PlanetStructure createStructure(String name, PlanetStructure.StructureType type,
                                            Long planetId, double lat, double lon,
                                            double elevation, String resourcesJson,
                                            int capacity, String description) {
        PlanetStructure structure = new PlanetStructure();
        structure.setName(name);
        structure.setStructureType(type);
        structure.setPlanetId(planetId);
        structure.setLatitude(lat);
        structure.setLongitude(lon);
        structure.setElevation(elevation);
        structure.setResourcesJson(resourcesJson);
        structure.setCapacity(capacity);
        structure.setActive(true);
        structure.setDescription(description);
        return planetStructureRepository.save(structure);
    }
}
