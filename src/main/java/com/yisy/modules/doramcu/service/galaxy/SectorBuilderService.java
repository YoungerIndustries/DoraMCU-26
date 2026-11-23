package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.PlanetNameDatabase;
import com.yisy.modules.doramcu.hex.StarNameDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Handles on-demand sector generation for lazy-loaded galaxies.
 * <p>
 * When a fleet enters a sector that hasn't been built yet, this service
 * generates all solar systems in that sector using the galaxy's seed for
 * deterministic generation. The generated systems are persisted to the
 * database immediately so they survive server restarts.
 * </p>
 * <p>
 * This is the core of the three-tier lazy loading system:
 * <ul>
 *   <li>Tier 1: Galaxy map (always loaded) — sectors and subsectors exist but no systems</li>
 *   <li>Tier 2: Sector build (on first entry) — this service generates all systems in a sector</li>
 *   <li>Tier 3: SubSector detail (on fleet proximity) — subsector generateSystems() already handles this</li>
 * </ul>
 * </p>
 */
@Service
public class SectorBuilderService {

    private static final Logger log = LoggerFactory.getLogger(SectorBuilderService.class);

    private final GalaxyService galaxyService;
    
    /** Whether to generate UWP data for planets during sector building. Enabled for Traveller-style galaxies. */
    private boolean enableUWPGeneration = true;

    public SectorBuilderService(GalaxyService galaxyService) {
        this.galaxyService = galaxyService;
    }

    /**
     * Sets whether to generate UWP data for planets during sector building.
     * Non-Traveller galaxies should disable this for performance.
     *
     * @param enable true to generate UWP data (default), false to skip
     */
    public void setEnableUWPGeneration(boolean enable) {
        this.enableUWPGeneration = enable;
    }

    /**
     * @return true if UWP generation is enabled (default)
     */
    public boolean isEnableUWPGeneration() {
        return enableUWPGeneration;
    }

    /**
     * Generates all solar systems in a sector when a fleet first enters.
     * Uses the galaxy's seed to ensure deterministic generation.
     * <p>
     * If the sector is already built, this method returns immediately.
     *
     * @param galaxy the in-memory galaxy
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @return the number of systems generated, or 0 if already built
     */
    public int buildSector(Galaxy galaxy, int sectorRow, int sectorCol) {
        if (galaxy == null) {
            log.warn("Cannot build sector: galaxy is null");
            return 0;
        }

        Galaxy.Sector sector = galaxy.getSector(sectorRow, sectorCol);
        if (sector.isBuilt()) {
            log.debug("Sector ({}, {}) in galaxy '{}' is already built — skipping",
                    sectorRow, sectorCol, galaxy.getName());
            return 0;
        }

        log.info("Building sector ({}, {}) '{}' in galaxy '{}' — lazy loading triggered",
                sectorRow, sectorCol, sector.getName(), galaxy.getName());

        // Use galaxy seed + sector position for deterministic generation
        long seed = galaxy.getSeed() != null ? galaxy.getSeed() : galaxy.getName().hashCode();
        if (seed == 0) seed = 1L;
        long sectorSeed = seed ^ (sectorRow * 10 + sectorCol);
        Random random = new Random(sectorSeed);

        StarNameDatabase starNames = new StarNameDatabase();
        PlanetNameDatabase planetNames = new PlanetNameDatabase();

        // Generate all subsectors in this sector
        int generated = sector.generateAllSubSectors(random, starNames, planetNames, enableUWPGeneration);

        // Copy generated systems to the galaxy's global map
        for (var entry : sector.getAllSystems().entrySet()) {
            galaxy.getAllSystemsMap().put(entry.getKey(), entry.getValue());
        }

        log.info("Built sector ({}, {}) in galaxy '{}': {} systems generated",
                sectorRow, sectorCol, galaxy.getName(), generated);

        return generated;
    }

    /**
     * Checks whether a sector needs to be built (lazy loading check).
     *
     * @param galaxy the in-memory galaxy
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @return true if the sector needs to be built
     */
    public boolean needsBuilding(Galaxy galaxy, int sectorRow, int sectorCol) {
        if (galaxy == null) return false;
        Galaxy.Sector sector = galaxy.getSector(sectorRow, sectorCol);
        return !sector.isBuilt();
    }

    /**
     * Ensures a sector is built, building it if necessary.
     * This is the main entry point for fleet movement triggers.
     *
     * @param galaxy the in-memory galaxy
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @return the number of systems generated (0 if already built)
     */
    public int ensureSectorBuilt(Galaxy galaxy, int sectorRow, int sectorCol) {
        if (needsBuilding(galaxy, sectorRow, sectorCol)) {
            return buildSector(galaxy, sectorRow, sectorCol);
        }
        return 0;
    }
}
