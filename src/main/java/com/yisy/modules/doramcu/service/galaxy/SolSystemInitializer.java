package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.galaxy.SolarSystem;
import com.yisy.modules.doramcu.model.galaxy.Star;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.galaxy.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.galaxy.StarRepository;
import com.yisy.modules.doramcu.repository.galaxy.PlanetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Initializes the Sol solar system with Earth as the landing planet in every galaxy.
 * <p>
 * This service ensures that every galaxy has a minimal but functional starting point
 * for players. The Sol system contains:
 * <ul>
 *   <li>A Sun star (YELLOW, MEDIUM) — the central star of the system</li>
 *   <li>Earth — the landing planet where players can spawn</li>
 * </ul>
 * </p>
 * <p>
 * The Younger Industries HQ basement serves as the universal hub across all galaxies.
 * When players land on Earth in any galaxy, they arrive at the YI HQ basement, which
 * provides access to portals, shops, and other galaxy-specific content.
 * </p>
 * <p>
 * This is called after each galaxy is created in the MultiGalaxyBuildQueue, so every
 * galaxy has at least this minimal infrastructure before any dynamic building begins.
 * </p>
 */
@Service
public class SolSystemInitializer {

    private static final Logger log = LoggerFactory.getLogger(SolSystemInitializer.class);

    /** The name of the Sol solar system — consistent across all galaxies. */
    public static final String SOL_SYSTEM_NAME = "Sol";

    /** The name of the Sun star — consistent across all galaxies. */
    public static final String SUN_STAR_NAME = "Sun";

    /** The name of the Earth planet — the landing planet in every galaxy. */
    public static final String EARTH_PLANET_NAME = "Earth";

    private final GalaxyRepository galaxyRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final StarRepository starRepository;
    private final PlanetRepository planetRepository;

    public SolSystemInitializer(GalaxyRepository galaxyRepository,
                                 SolarSystemRepository solarSystemRepository,
                                 StarRepository starRepository,
                                 PlanetRepository planetRepository) {
        this.galaxyRepository = galaxyRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.starRepository = starRepository;
        this.planetRepository = planetRepository;
    }

    /**
     * Initializes the Sol system with Earth in the given galaxy.
     * <p>
     * If the galaxy already has a Sol system, this method skips creation
     * to avoid duplicates. This is safe to call multiple times.
     * </p>
     *
     * @param galaxy the galaxy to initialize
     */
    @Transactional
    public void initializeSolSystem(Galaxy galaxy) {
        if (galaxy == null) {
            log.warn("Cannot initialize Sol system: galaxy is null");
            return;
        }

        // Check if Sol system already exists in this galaxy
        Optional<SolarSystem> existingSol = solarSystemRepository.findByGalaxyIdAndName(galaxy.getId(), SOL_SYSTEM_NAME);
        if (existingSol.isPresent()) {
            log.info("Galaxy '{}' already has a Sol system, skipping initialization", galaxy.getName());
            return;
        }

        log.info("Initializing Sol system with Earth in galaxy '{}'", galaxy.getName());

        // Create the Sol solar system at the center of the galaxy (sector 5,5, subsector 5,5, position 5,5,5)
        SolarSystem solSystem = new SolarSystem();
        solSystem.setGalaxy(galaxy);
        solSystem.setName(SOL_SYSTEM_NAME);
        solSystem.setSectorRow(5);
        solSystem.setSectorCol(5);
        solSystem.setSubSectorRow(5);
        solSystem.setSubSectorCol(5);
        solSystem.setPosX(5);
        solSystem.setPosY(5);
        solSystem.setPosZ(5);
        solSystem = solarSystemRepository.save(solSystem);
        log.info("Created Sol system (id={}) in galaxy '{}'", solSystem.getId(), galaxy.getName());

        // Create the Sun star
        Star sun = new Star();
        sun.setName(SUN_STAR_NAME);
        sun.setSolarSystem(solSystem);
        sun.setStarSize(Star.StarSize.MEDIUM);
        sun.setStarType(Star.StarType.YELLOW);
        sun.setRadius(10);
        sun.setEnergyYield(20);
        sun.setStationSlots(4);
        sun.setOccupiedSlots(0);
        sun.setRadiationDamage(0.05);
        sun.setGravityWellBonus(0.5);
        starRepository.save(sun);
        log.info("Created Sun star in Sol system");

        // Create Earth — the landing planet
        Planet earth = new Planet();
        earth.setName(EARTH_PLANET_NAME);
        earth.setSolarSystem(solSystem);
        earth.setPlanetSize(Planet.PlanetSize.LARGE);
        earth.setMineralQuality(Planet.MineralQuality.RICH);
        earth.setRadius(6371); // Earth radius in km
        earth.setMineralProduction(100);
        earth.setEnergyProduction(100);
        earth.setFoodProduction(100);
        earth.setResearchProduction(100);
        earth.setInfluenceProduction(100);
        earth.setCurrentPopulation(1000); // Starting population, grows dynamically
        earth.setMaxPopulation(100000); // Capacity for initial phase
        earth.setSocialStat(10);
        earth.setIntelligenceStat(10);
        earth.setDiligenceStat(10);
        earth.setResolveStat(10);
        earth.setOwnerEmpireId(null); // Unclaimed, available for players
        planetRepository.save(earth);
        log.info("Created Earth planet in Sol system — player landing point");

        log.info("Sol system initialization complete for galaxy '{}': Sol system with Sun and Earth", galaxy.getName());
    }

    /**
     * Checks if a galaxy has the Sol system initialized.
     *
     * @param galaxyId the galaxy ID
     * @return true if the Sol system exists
     */
    public boolean hasSolSystem(Long galaxyId) {
        return solarSystemRepository.findByGalaxyIdAndName(galaxyId, SOL_SYSTEM_NAME).isPresent();
    }
}
