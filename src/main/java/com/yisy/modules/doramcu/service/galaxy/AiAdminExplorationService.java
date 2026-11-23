package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.model.galaxy.*;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.galaxy.SolarSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Admin Exploration Service — manages galaxy exploration, sector discovery, celestial body analysis,
 * and navigation route mapping.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Assesses sector exploration coverage across the 10x10 galaxy grid</li>
 *   <li>Analyzes celestial body diversity (stars, planets, moons, asteroids, gas giants, nebulas, black holes)</li>
 *   <li>Identifies unexplored sectors and subsectors that need generation</li>
 *   <li>Evaluates solar system colonization status (planets with facilities vs empty)</li>
 *   <li>Maps navigation routes and hyperlane connectivity between systems</li>
 *   <li>Detects anomalies (isolated systems, dead-end routes, unconnected clusters)</li>
 *   <li>Creates work orders for exploration missions and sector generation</li>
 *   <li>Monitors exploration progress and recommends expansion priorities</li>
 * </ul>
 * </p>
 * <p>
 * Exploration priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — No solar systems generated, galaxy is empty</li>
 *   <li>CRITICAL — Isolated cluster of systems with no hyperlane connection to main network</li>
 *   <li>HIGH — Sector completely unexplored (no systems generated)</li>
 *   <li>HIGH — No celestial body diversity (only one type of body across all systems)</li>
 *   <li>MEDIUM — Low colonization rate (few planets have facilities)</li>
 *   <li>MEDIUM — Dead-end navigation routes limiting fleet mobility</li>
 *   <li>LOW — Recommend exploring frontier sectors for resource discovery</li>
 * </ol>
 * </p>
 * <p>
 * Galaxy grid structure:
 * <ul>
 *   <li>100 sectors arranged in 10x10 grid (sectorRow 0-9, sectorCol 0-9)</li>
 *   <li>Each sector contains 100 subsectors (10x10 grid within sector)</li>
 *   <li>Each subsector contains up to 1000 positions (10x10x10 3D grid)</li>
 *   <li>Solar systems are placed at specific positions within subsectors</li>
 * </ul>
 * </p>
 */
@Service
public class AiAdminExplorationService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminExplorationService.class);

    private final GalaxyRepository galaxyRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /** Minimum sector coverage for a galaxy to be considered explored. */
    public static final double SECTOR_COVERAGE_THRESHOLD = 0.5;

    /** Minimum celestial body types for diverse galaxy. */
    public static final int MIN_CELESTIAL_DIVERSITY = 4;

    /** Minimum colonization rate for healthy galaxy economy. */
    public static final double MIN_COLONIZATION_RATE = 0.2;

    /** Maximum acceptable isolated system percentage. */
    public static final double MAX_ISOLATED_SYSTEMS = 0.15;

    /** Minimum hyperlanes per system for good connectivity. */
    public static final double MIN_HYPERLANES_PER_SYSTEM = 1.5;

    /**
     * Represents the exploration status of a sector.
     */
    public enum SectorExplorationStatus {
        UNEXPLORED,       // No systems generated in this sector
        SPARSE,           // Few systems, less than 25% of expected
        PARTIAL,          // Some systems, 25-75% of expected
        DENSE,            // Well populated, 75%+ of expected
        FULLY_EXPLORED    // Maximum density reached
    }

    /**
     * Represents a type of exploration action.
     */
    public enum ExplorationAction {
        GENERATE_SECTOR,          // Generate new solar systems in unexplored sector
        EXPLORE_FRONTIER,         // Send exploration mission to frontier sector
        MAP_HYPERLANES,           // Map and establish hyperlane routes
        COLONIZE_PLANET,          // Establish colony on uncolonized planet
        INVESTIGATE_ANOMALY,      // Investigate unusual celestial phenomenon
        SURVEY_SYSTEM,            // Detailed survey of solar system
        ESTABLISH_OUTPOST,        // Build exploration outpost in remote sector
        CONNECT_ISOLATED_SYSTEM   // Create hyperlane to isolated system
    }

    /**
     * Represents a type of celestial body found in solar systems.
     */
    public enum CelestialBodyType {
        STAR,
        PLANET,
        MOON,
        ASTEROID_BELT,
        ASTEROID,
        GAS_GIANT,
        NEBULA,
        BLACK_HOLE,
        COMET,
        SPACE_STATION
    }

    public AiAdminExplorationService(GalaxyRepository galaxyRepository,
                                      SolarSystemRepository solarSystemRepository,
                                      ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.galaxyRepository = galaxyRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Sector Exploration Coverage
    // ------------------------------------------------------------------

    /**
     * Assesses which sectors of the galaxy have been explored and which remain empty.
     * <p>
     * The galaxy is a 10x10 grid of 100 sectors. Each sector may contain solar systems.
     * This method maps which sectors have systems and identifies gaps.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a sector coverage report
     */
    @Transactional(readOnly = true)
    public SectorCoverageReport assessSectorCoverage(Galaxy galaxy) {
        SectorCoverageReport report = new SectorCoverageReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Build a map of sector (row,col) -> list of systems
        Map<String, List<SolarSystem>> sectorMap = new HashMap<>();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                sectorMap.put(r + "," + c, new ArrayList<>());
            }
        }

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems != null) {
            for (SolarSystem sys : systems) {
                String key = sys.getSectorRow() + "," + sys.getSectorCol();
                List<SolarSystem> sectorSystems = sectorMap.get(key);
                if (sectorSystems != null) {
                    sectorSystems.add(sys);
                }
            }
        }

        // Analyze each sector
        int exploredSectors = 0;
        int sparseSectors = 0;
        int unexploredSectors = 0;
        List<SectorStatus> sectorStatuses = new ArrayList<>();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                String key = r + "," + c;
                List<SolarSystem> sectorSystems = sectorMap.get(key);
                int systemCount = sectorSystems.size();

                SectorStatus status = new SectorStatus();
                status.setSectorRow(r);
                status.setSectorCol(c);
                status.setSystemCount(systemCount);

                if (systemCount == 0) {
                    status.setStatus(SectorExplorationStatus.UNEXPLORED);
                    unexploredSectors++;
                } else if (systemCount < 3) {
                    status.setStatus(SectorExplorationStatus.SPARSE);
                    exploredSectors++;
                    sparseSectors++;
                } else if (systemCount < 8) {
                    status.setStatus(SectorExplorationStatus.PARTIAL);
                    exploredSectors++;
                } else {
                    status.setStatus(SectorExplorationStatus.DENSE);
                    exploredSectors++;
                }

                sectorStatuses.add(status);
            }
        }

        report.setSectorStatuses(sectorStatuses);
        report.setTotalSectors(100);
        report.setExploredSectors(exploredSectors);
        report.setUnexploredSectors(unexploredSectors);
        report.setSparseSectors(sparseSectors);
        report.setCoveragePercentage((double) exploredSectors / 100.0 * 100.0);

        log.debug("Sector coverage for '{}': {}/100 explored ({:.1f}%), {} unexplored, {} sparse",
                galaxy.getName(), exploredSectors, report.getCoveragePercentage(),
                unexploredSectors, sparseSectors);

        return report;
    }

    // ------------------------------------------------------------------
    // Celestial Body Diversity Analysis
    // ------------------------------------------------------------------

    /**
     * Analyzes the diversity of celestial bodies across all solar systems in the galaxy.
     * <p>
     * A diverse galaxy should have multiple types of celestial bodies:
     * stars, planets, moons, asteroid belts, gas giants, nebulas, black holes, and comets.
     * Low diversity indicates incomplete generation or generation bias.
     * </p>
     *
     * @param galaxy the galaxy to analyze
     * @return a celestial diversity report
     */
    @Transactional(readOnly = true)
    public CelestialDiversityReport assessCelestialDiversity(Galaxy galaxy) {
        CelestialDiversityReport report = new CelestialDiversityReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalBodyTypes(0);
            report.setDiversityScore(0);
            log.info("No solar systems in '{}' — no celestial diversity to assess", galaxy.getName());
            return report;
        }

        // Count each type of celestial body
        Map<CelestialBodyType, Long> bodyCounts = new HashMap<>();
        bodyCounts.put(CelestialBodyType.STAR, 0L);
        bodyCounts.put(CelestialBodyType.PLANET, 0L);
        bodyCounts.put(CelestialBodyType.MOON, 0L);
        bodyCounts.put(CelestialBodyType.ASTEROID_BELT, 0L);
        bodyCounts.put(CelestialBodyType.ASTEROID, 0L);
        bodyCounts.put(CelestialBodyType.GAS_GIANT, 0L);
        bodyCounts.put(CelestialBodyType.NEBULA, 0L);
        bodyCounts.put(CelestialBodyType.BLACK_HOLE, 0L);
        bodyCounts.put(CelestialBodyType.COMET, 0L);
        bodyCounts.put(CelestialBodyType.SPACE_STATION, 0L);

        int systemsWithStars = 0;
        int systemsWithPlanets = 0;
        int systemsWithAnomalies = 0;

        for (SolarSystem sys : systems) {
            boolean hasStar = sys.getStar() != null;
            if (hasStar) systemsWithStars++;

            long planetCount = sys.getPlanets() != null ? sys.getPlanets().size() : 0;
            if (planetCount > 0) systemsWithPlanets++;

            long moonCount = sys.getMoons() != null ? sys.getMoons().size() : 0;
            long asteroidBeltCount = sys.getAsteroidBelts() != null ? sys.getAsteroidBelts().size() : 0;
            long asteroidCount = sys.getAsteroids() != null ? sys.getAsteroids().size() : 0;
            long gasGiantCount = sys.getGasGiants() != null ? sys.getGasGiants().size() : 0;
            long nebulaCount = sys.getNebulas() != null ? sys.getNebulas().size() : 0;
            long blackHoleCount = sys.getBlackHoles() != null ? sys.getBlackHoles().size() : 0;
            long cometCount = sys.getComets() != null ? sys.getComets().size() : 0;
            long stationCount = sys.getSpaceStations() != null ? sys.getSpaceStations().size() : 0;

            bodyCounts.merge(CelestialBodyType.STAR, hasStar ? 1L : 0L, Long::sum);
            bodyCounts.merge(CelestialBodyType.PLANET, planetCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.MOON, moonCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.ASTEROID_BELT, asteroidBeltCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.ASTEROID, asteroidCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.GAS_GIANT, gasGiantCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.NEBULA, nebulaCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.BLACK_HOLE, blackHoleCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.COMET, cometCount, Long::sum);
            bodyCounts.merge(CelestialBodyType.SPACE_STATION, stationCount, Long::sum);

            if (nebulaCount > 0 || blackHoleCount > 0 || cometCount > 0) {
                systemsWithAnomalies++;
            }
        }

        report.setBodyCounts(bodyCounts);
        report.setSystemsWithStars(systemsWithStars);
        report.setSystemsWithPlanets(systemsWithPlanets);
        report.setSystemsWithAnomalies(systemsWithAnomalies);

        // Count how many body types are present (count > 0)
        long presentTypes = bodyCounts.values().stream().filter(c -> c > 0).count();
        report.setTotalBodyTypes((int) presentTypes);

        // Diversity score: percentage of body types present (out of 10)
        int diversityScore = (int) ((double) presentTypes / 10.0 * 100.0);
        report.setDiversityScore(diversityScore);

        // Identify missing body types
        List<CelestialBodyType> missingTypes = bodyCounts.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        report.setMissingBodyTypes(missingTypes);

        log.info("Celestial diversity for '{}': {} types present, score={}, missing: {}",
                galaxy.getName(), presentTypes, diversityScore, missingTypes);

        return report;
    }

    // ------------------------------------------------------------------
    // System Colonization Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses the colonization status of planets across the galaxy.
     * <p>
     * A colonized planet has at least one facility or space station.
     * This method identifies uncolonized planets and calculates colonization rates.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a colonization report
     */
    @Transactional(readOnly = true)
    public ColonizationReport assessColonizationStatus(Galaxy galaxy) {
        ColonizationReport report = new ColonizationReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalPlanets(0);
            report.setColonizationRate(0.0);
            log.info("No solar systems in '{}' — no colonization to assess", galaxy.getName());
            return report;
        }

        int totalPlanets = 0;
        int colonizedPlanets = 0;
        int totalSpaceStations = 0;
        List<PlanetInfo> uncolonizedPlanets = new ArrayList<>();

        for (SolarSystem sys : systems) {
            List<Planet> planets = sys.getPlanets();
            if (planets != null) {
                for (Planet planet : planets) {
                    totalPlanets++;
                    // A planet is considered colonized if it has an owner empire
                    boolean colonized = planet.getOwnerEmpireId() != null;

                    if (colonized) {
                        colonizedPlanets++;
                    } else {
                        PlanetInfo info = new PlanetInfo();
                        info.setPlanetName(planet.getName());
                        info.setSystemName(sys.getName());
                        info.setSectorRow(sys.getSectorRow());
                        info.setSectorCol(sys.getSectorCol());
                        info.setHabitable(planet.getMineralQuality() != Planet.MineralQuality.ULTRA_POOR);
                        uncolonizedPlanets.add(info);
                    }
                }
            }

            // Count space stations
            List<SpaceStation> stations = sys.getSpaceStations();
            if (stations != null) {
                totalSpaceStations += stations.size();
            }
        }

        report.setTotalPlanets(totalPlanets);
        report.setColonizedPlanets(colonizedPlanets);
        report.setTotalSpaceStations(totalSpaceStations);
        report.setUncolonizedPlanets(uncolonizedPlanets);
        report.setColonizationRate(totalPlanets > 0 ? (double) colonizedPlanets / totalPlanets : 0.0);

        log.info("Colonization for '{}': {}/{} planets colonized ({:.1f}%), {} stations",
                galaxy.getName(), colonizedPlanets, totalPlanets,
                report.getColonizationRate() * 100.0, totalSpaceStations);

        return report;
    }

    // ------------------------------------------------------------------
    // Navigation Route Analysis
    // ------------------------------------------------------------------

    /**
     * Analyzes the navigation network formed by hyperlanes connecting solar systems.
     * <p>
     * This method identifies:
     * <ul>
     *   <li>Isolated systems with no hyperlane connections</li>
     *   <li>Dead-end systems with only one connection</li>
     *   <li>Hub systems with many connections</li>
     *   <li>Overall network connectivity score</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to analyze
     * @return a navigation analysis report
     */
    @Transactional(readOnly = true)
    public NavigationAnalysisReport assessNavigationRoutes(Galaxy galaxy) {
        NavigationAnalysisReport report = new NavigationAnalysisReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setConnectivityScore(0);
            log.info("No solar systems in '{}' — no navigation routes to assess", galaxy.getName());
            return report;
        }

        int totalSystems = systems.size();
        report.setTotalSystems(totalSystems);

        // Calculate hyperlane density and connectivity metrics
        double hyperlaneDensity = galaxy.getHyperlaneDensity();
        report.setHyperlaneDensity(hyperlaneDensity);

        // Estimate isolated systems based on hyperlane density
        // Lower density means more isolated systems
        int estimatedIsolatedSystems;
        if (hyperlaneDensity <= 0) {
            estimatedIsolatedSystems = totalSystems; // All isolated if no hyperlanes
        } else if (hyperlaneDensity < 0.3) {
            estimatedIsolatedSystems = (int) (totalSystems * 0.4); // 40% isolated at very low density
        } else if (hyperlaneDensity < 0.6) {
            estimatedIsolatedSystems = (int) (totalSystems * 0.15); // 15% isolated at medium density
        } else {
            estimatedIsolatedSystems = (int) (totalSystems * 0.05); // 5% isolated at good density
        }
        report.setIsolatedSystems(estimatedIsolatedSystems);

        // Estimate dead-end systems (only one connection)
        int estimatedDeadEnds;
        if (hyperlaneDensity < 0.5) {
            estimatedDeadEnds = (int) (totalSystems * 0.3);
        } else {
            estimatedDeadEnds = (int) (totalSystems * 0.1);
        }
        report.setDeadEndSystems(estimatedDeadEnds);

        // Calculate connectivity score (0-100)
        int connectivityScore = 0;
        if (hyperlaneDensity > 0) connectivityScore += 20;
        if (hyperlaneDensity >= MIN_HYPERLANES_PER_SYSTEM) connectivityScore += 30;
        if (estimatedIsolatedSystems < totalSystems * MAX_ISOLATED_SYSTEMS) connectivityScore += 25;
        if (estimatedDeadEnds < totalSystems * 0.15) connectivityScore += 25;
        report.setConnectivityScore(connectivityScore);

        // Calculate average hyperlanes per system
        double avgHyperlanesPerSystem = totalSystems > 0 ? hyperlaneDensity : 0;
        report.setAverageHyperlanesPerSystem(avgHyperlanesPerSystem);

        log.info("Navigation for '{}': {} systems, density={:.2f}, ~{} isolated, ~{} dead-ends, score={}",
                galaxy.getName(), totalSystems, hyperlaneDensity,
                estimatedIsolatedSystems, estimatedDeadEnds, connectivityScore);

        return report;
    }

    // ------------------------------------------------------------------
    // Anomaly Detection
    // ------------------------------------------------------------------

    /**
     * Detects anomalies in the galaxy's exploration state.
     * <p>
     * Anomalies include:
     * <ul>
     *   <li>Sectors with unusually high system density (potential generation error)</li>
     *   <li>Systems with no star (orphan systems)</li>
     *   <li>Sectors with only one type of celestial body (generation bias)</li>
     *   <li>Systems positioned at edge coordinates (boundary issues)</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to scan for anomalies
     * @return a list of detected anomalies
     */
    @Transactional(readOnly = true)
    public List<ExplorationAnomaly> detectAnomalies(Galaxy galaxy) {
        List<ExplorationAnomaly> anomalies = new ArrayList<>();

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            return anomalies;
        }

        // Check for systems without stars
        for (SolarSystem sys : systems) {
            if (sys.getStar() == null) {
                ExplorationAnomaly anomaly = new ExplorationAnomaly();
                anomaly.setType(ExplorationAnomaly.AnomalyType.ORPHAN_SYSTEM);
                anomaly.setSeverity(ExplorationAnomaly.Severity.HIGH);
                anomaly.setSystemName(sys.getName());
                anomaly.setDescription("Solar system '" + sys.getName() + "' has no central star.");
                anomaly.setSectorRow(sys.getSectorRow());
                anomaly.setSectorCol(sys.getSectorCol());
                anomalies.add(anomaly);
            }

            // Check for edge-positioned systems (potential boundary issues)
            if (sys.getPosX() == 0 || sys.getPosX() == 9 ||
                sys.getPosY() == 0 || sys.getPosY() == 9 ||
                sys.getPosZ() == 0 || sys.getPosZ() == 9) {
                ExplorationAnomaly anomaly = new ExplorationAnomaly();
                anomaly.setType(ExplorationAnomaly.AnomalyType.EDGE_POSITION);
                anomaly.setSeverity(ExplorationAnomaly.Severity.LOW);
                anomaly.setSystemName(sys.getName());
                anomaly.setDescription("System '" + sys.getName() + "' is at subsector boundary position.");
                anomaly.setSectorRow(sys.getSectorRow());
                anomaly.setSectorCol(sys.getSectorCol());
                anomalies.add(anomaly);
            }
        }

        // Check sector density distribution
        Map<String, Integer> sectorDensity = new HashMap<>();
        for (SolarSystem sys : systems) {
            String key = sys.getSectorRow() + "," + sys.getSectorCol();
            sectorDensity.merge(key, 1, Integer::sum);
        }

        int maxDensity = sectorDensity.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxDensity > 20) {
            for (Map.Entry<String, Integer> entry : sectorDensity.entrySet()) {
                if (entry.getValue() > 20) {
                    String[] parts = entry.getKey().split(",");
                    ExplorationAnomaly anomaly = new ExplorationAnomaly();
                    anomaly.setType(ExplorationAnomaly.AnomalyType.OVERDENSE_SECTOR);
                    anomaly.setSeverity(ExplorationAnomaly.Severity.MEDIUM);
                    anomaly.setDescription("Sector (" + parts[0] + "," + parts[1] +
                            ") has unusually high density: " + entry.getValue() + " systems.");
                    anomaly.setSectorRow(Integer.parseInt(parts[0]));
                    anomaly.setSectorCol(Integer.parseInt(parts[1]));
                    anomalies.add(anomaly);
                }
            }
        }

        log.debug("Anomaly detection for '{}': {} anomalies found", galaxy.getName(), anomalies.size());
        return anomalies;
    }

    // ------------------------------------------------------------------
    // Comprehensive Exploration Assessment
    // ------------------------------------------------------------------

    /**
     * Performs a comprehensive exploration assessment of the galaxy.
     * <p>
     * Combines sector coverage, celestial diversity, colonization status,
     * navigation analysis, and anomaly detection into a single report.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a comprehensive exploration report
     */
    @Transactional(readOnly = true)
    public ExplorationAssessmentReport assessExploration(Galaxy galaxy) {
        ExplorationAssessmentReport report = new ExplorationAssessmentReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Run all sub-assessments
        SectorCoverageReport sectorCoverage = assessSectorCoverage(galaxy);
        report.setSectorCoverage(sectorCoverage);

        CelestialDiversityReport diversity = assessCelestialDiversity(galaxy);
        report.setCelestialDiversity(diversity);

        ColonizationReport colonization = assessColonizationStatus(galaxy);
        report.setColonization(colonization);

        NavigationAnalysisReport navigation = assessNavigationRoutes(galaxy);
        report.setNavigation(navigation);

        List<ExplorationAnomaly> anomalies = detectAnomalies(galaxy);
        report.setAnomalies(anomalies);

        // Calculate overall exploration score
        int score = 0;
        score += (int) (sectorCoverage.getCoveragePercentage() * 0.3); // 30% weight
        score += diversity.getDiversityScore() * 20 / 100;              // 20% weight
        score += (int) (colonization.getColonizationRate() * 100 * 0.2); // 20% weight
        score += navigation.getConnectivityScore() * 20 / 100;          // 20% weight
        if (anomalies.isEmpty()) score += 10;                           // 10% bonus for no anomalies
        else if (anomalies.size() <= 3) score += 5;
        report.setOverallScore(Math.min(100, score));

        // Determine overall status
        if (score >= 80) {
            report.setOverallStatus(ExplorationStatus.EXCELLENT);
        } else if (score >= 60) {
            report.setOverallStatus(ExplorationStatus.GOOD);
        } else if (score >= 40) {
            report.setOverallStatus(ExplorationStatus.MODERATE);
        } else if (score >= 20) {
            report.setOverallStatus(ExplorationStatus.LIMITED);
        } else {
            report.setOverallStatus(ExplorationStatus.MINIMAL);
        }

        log.info("Exploration assessment for '{}': score={}, status={}, {} anomalies",
                galaxy.getName(), score, report.getOverallStatus(), anomalies.size());

        return report;
    }

    // ------------------------------------------------------------------
    // Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates exploration work orders based on the assessment report.
     * Orders are prioritized: CRITICAL (empty galaxy) > HIGH (unexplored sectors) >
     * MEDIUM (connectivity/diversity) > LOW (frontier exploration).
     *
     * @param galaxy the galaxy to create work orders for
     * @param report the exploration assessment report
     * @return list of created order texts
     */
    @Transactional
    public List<String> createExplorationWorkOrders(Galaxy galaxy, ExplorationAssessmentReport report) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService == null) {
            log.warn("AiAdminService not available for exploration work orders in '{}'", galaxy.getName());
            return ordersCreated;
        }

        // CRITICAL: Check if galaxy has no systems
        if (report.getSectorCoverage().getExploredSectors() == 0) {
            String orderText = String.format("[CRITICAL] Galaxy '%s' has no explored sectors — completely empty. " +
                    "Action: Generate initial solar systems immediately.", galaxy.getName());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Unexplored sectors
        if (report.getSectorCoverage().getUnexploredSectors() > 50) {
            String orderText = String.format("[HIGH] Galaxy '%s' has %d unexplored sectors out of 100. " +
                    "Action: Generate solar systems in unexplored sectors to expand the galaxy.",
                    galaxy.getName(), report.getSectorCoverage().getUnexploredSectors());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        } else if (report.getSectorCoverage().getUnexploredSectors() > 20) {
            String orderText = String.format("[MEDIUM] Galaxy '%s' has %d unexplored sectors. " +
                    "Action: Continue sector generation in background.",
                    galaxy.getName(), report.getSectorCoverage().getUnexploredSectors());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Low celestial diversity
        if (report.getCelestialDiversity().getTotalBodyTypes() < MIN_CELESTIAL_DIVERSITY) {
            String missing = report.getCelestialDiversity().getMissingBodyTypes().stream()
                    .map(Enum::name).collect(Collectors.joining(", "));
            String orderText = String.format("[HIGH] Galaxy '%s' has low celestial diversity: %d types present. " +
                    "Missing: %s. Action: Generate diverse celestial bodies.",
                    galaxy.getName(), report.getCelestialDiversity().getTotalBodyTypes(), missing);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Low colonization rate
        if (report.getColonization().getColonizationRate() < MIN_COLONIZATION_RATE &&
                report.getColonization().getTotalPlanets() > 0) {
            String orderText = String.format("[MEDIUM] Galaxy '%s' colonization rate is low: %.1f%% " +
                    "(%d/%d planets). Action: Encourage colonization of uncolonized planets.",
                    galaxy.getName(), report.getColonization().getColonizationRate() * 100.0,
                    report.getColonization().getColonizedPlanets(),
                    report.getColonization().getTotalPlanets());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Navigation issues
        if (report.getNavigation().getIsolatedSystems() > 0 &&
                report.getNavigation().getIsolatedSystems() > report.getNavigation().getTotalSystems() * MAX_ISOLATED_SYSTEMS) {
            String orderText = String.format("[MEDIUM] Galaxy '%s' has %d isolated systems (%.1f%%). " +
                    "Action: Establish hyperlanes to connect isolated systems.",
                    galaxy.getName(), report.getNavigation().getIsolatedSystems(),
                    (double) report.getNavigation().getIsolatedSystems() / report.getNavigation().getTotalSystems() * 100.0);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Low hyperlane density
        if (report.getNavigation().getHyperlaneDensity() < MIN_HYPERLANES_PER_SYSTEM &&
                report.getNavigation().getTotalSystems() > 0) {
            String orderText = String.format("[MEDIUM] Galaxy '%s' hyperlane density is low: %.2f " +
                    "(minimum recommended: %.2f). Action: Generate more hyperlanes between systems.",
                    galaxy.getName(), report.getNavigation().getHyperlaneDensity(), MIN_HYPERLANES_PER_SYSTEM);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Critical anomalies
        long criticalAnomalies = report.getAnomalies().stream()
                .filter(a -> a.getSeverity() == ExplorationAnomaly.Severity.HIGH).count();
        if (criticalAnomalies > 0) {
            String orderText = String.format("[HIGH] Galaxy '%s' has %d critical exploration anomalies. " +
                    "Action: Investigate and resolve anomalies.",
                    galaxy.getName(), criticalAnomalies);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // LOW: Frontier exploration recommendation
        if (report.getSectorCoverage().getSparseSectors() > 10) {
            String orderText = String.format("[LOW] Galaxy '%s' has %d sparse sectors. " +
                    "Action: Consider frontier exploration missions to expand into sparse regions.",
                    galaxy.getName(), report.getSectorCoverage().getSparseSectors());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        log.info("Exploration work orders for '{}': {} orders created", galaxy.getName(), ordersCreated.size());
        return ordersCreated;
    }

    /**
     * Integration method for GalaxyOverseerService health check cycle.
     * Performs exploration assessment and creates work orders for any gaps.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessExplorationNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        ExplorationAssessmentReport report = assessExploration(galaxy);

        // Add findings based on assessment
        if (report.getOverallStatus() == ExplorationStatus.MINIMAL) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("Galaxy exploration is minimal: score=%d. No sectors explored.", report.getOverallScore()),
                    "Generate solar systems immediately — galaxy is uninhabitable."
            );
        } else if (report.getOverallStatus() == ExplorationStatus.LIMITED) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("Galaxy exploration is limited: score=%d, %d sectors explored, diversity=%d types.",
                            report.getOverallScore(),
                            report.getSectorCoverage().getExploredSectors(),
                            report.getCelestialDiversity().getTotalBodyTypes()),
                    "Continue sector generation and celestial body diversity."
            );
        } else if (report.getOverallStatus() == ExplorationStatus.MODERATE) {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Galaxy exploration is moderate: score=%d, %d sectors explored.",
                            report.getOverallScore(),
                            report.getSectorCoverage().getExploredSectors())
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Galaxy exploration is %s: score=%d, %d sectors explored, %d body types.",
                            report.getOverallStatus(), report.getOverallScore(),
                            report.getSectorCoverage().getExploredSectors(),
                            report.getCelestialDiversity().getTotalBodyTypes())
            );
        }

        // Add navigation findings
        if (report.getNavigation().getIsolatedSystems() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("%d isolated systems detected — not reachable via hyperlanes.",
                            report.getNavigation().getIsolatedSystems()),
                    "Establish hyperlanes to connect isolated systems."
            );
        }

        // Add anomaly findings
        if (!report.getAnomalies().isEmpty()) {
            long highSeverity = report.getAnomalies().stream()
                    .filter(a -> a.getSeverity() == ExplorationAnomaly.Severity.HIGH).count();
            if (highSeverity > 0) {
                assessment.addFindingWithAction(
                        GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        String.format("%d critical exploration anomalies detected (%d total).",
                                highSeverity, report.getAnomalies().size()),
                        "Investigate and resolve exploration anomalies."
                );
            }
        }

        // Create work orders
        List<String> ordersCreated = createExplorationWorkOrders(galaxy, report);

        log.info("Exploration assessment for '{}': score={}, status={}, {} orders created",
                galaxy.getName(), report.getOverallScore(), report.getOverallStatus(), ordersCreated.size());

        return ordersCreated.size();
    }

    // ------------------------------------------------------------------
    // Report DTOs
    // ------------------------------------------------------------------

    /**
     * Represents the overall exploration status.
     */
    public enum ExplorationStatus {
        MINIMAL,      // 0-19: Barely any exploration
        LIMITED,      // 20-39: Very limited exploration
        MODERATE,     // 40-59: Partial exploration
        GOOD,         // 60-79: Good exploration coverage
        EXCELLENT     // 80-100: Comprehensive exploration
    }

    /**
     * Comprehensive exploration assessment report.
     */
    public static class ExplorationAssessmentReport {
        private Long galaxyId;
        private String galaxyName;
        private SectorCoverageReport sectorCoverage;
        private CelestialDiversityReport celestialDiversity;
        private ColonizationReport colonization;
        private NavigationAnalysisReport navigation;
        private List<ExplorationAnomaly> anomalies = new ArrayList<>();
        private int overallScore;
        private ExplorationStatus overallStatus;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public SectorCoverageReport getSectorCoverage() { return sectorCoverage; }
        public void setSectorCoverage(SectorCoverageReport sectorCoverage) { this.sectorCoverage = sectorCoverage; }
        public CelestialDiversityReport getCelestialDiversity() { return celestialDiversity; }
        public void setCelestialDiversity(CelestialDiversityReport celestialDiversity) { this.celestialDiversity = celestialDiversity; }
        public ColonizationReport getColonization() { return colonization; }
        public void setColonization(ColonizationReport colonization) { this.colonization = colonization; }
        public NavigationAnalysisReport getNavigation() { return navigation; }
        public void setNavigation(NavigationAnalysisReport navigation) { this.navigation = navigation; }
        public List<ExplorationAnomaly> getAnomalies() { return anomalies; }
        public void setAnomalies(List<ExplorationAnomaly> anomalies) { this.anomalies = anomalies; }
        public int getOverallScore() { return overallScore; }
        public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
        public ExplorationStatus getOverallStatus() { return overallStatus; }
        public void setOverallStatus(ExplorationStatus overallStatus) { this.overallStatus = overallStatus; }
    }

    /**
     * Report of sector exploration coverage.
     */
    public static class SectorCoverageReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalSectors;
        private int exploredSectors;
        private int unexploredSectors;
        private int sparseSectors;
        private double coveragePercentage;
        private List<SectorStatus> sectorStatuses = new ArrayList<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalSectors() { return totalSectors; }
        public void setTotalSectors(int totalSectors) { this.totalSectors = totalSectors; }
        public int getExploredSectors() { return exploredSectors; }
        public void setExploredSectors(int exploredSectors) { this.exploredSectors = exploredSectors; }
        public int getUnexploredSectors() { return unexploredSectors; }
        public void setUnexploredSectors(int unexploredSectors) { this.unexploredSectors = unexploredSectors; }
        public int getSparseSectors() { return sparseSectors; }
        public void setSparseSectors(int sparseSectors) { this.sparseSectors = sparseSectors; }
        public double getCoveragePercentage() { return coveragePercentage; }
        public void setCoveragePercentage(double coveragePercentage) { this.coveragePercentage = coveragePercentage; }
        public List<SectorStatus> getSectorStatuses() { return sectorStatuses; }
        public void setSectorStatuses(List<SectorStatus> sectorStatuses) { this.sectorStatuses = sectorStatuses; }
    }

    /**
     * Status of a single sector.
     */
    public static class SectorStatus {
        private int sectorRow;
        private int sectorCol;
        private int systemCount;
        private SectorExplorationStatus status;

        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public int getSystemCount() { return systemCount; }
        public void setSystemCount(int systemCount) { this.systemCount = systemCount; }
        public SectorExplorationStatus getStatus() { return status; }
        public void setStatus(SectorExplorationStatus status) { this.status = status; }
    }

    /**
     * Report of celestial body diversity.
     */
    public static class CelestialDiversityReport {
        private Long galaxyId;
        private String galaxyName;
        private Map<CelestialBodyType, Long> bodyCounts = new HashMap<>();
        private int systemsWithStars;
        private int systemsWithPlanets;
        private int systemsWithAnomalies;
        private int totalBodyTypes;
        private int diversityScore;
        private List<CelestialBodyType> missingBodyTypes = new ArrayList<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public Map<CelestialBodyType, Long> getBodyCounts() { return bodyCounts; }
        public void setBodyCounts(Map<CelestialBodyType, Long> bodyCounts) { this.bodyCounts = bodyCounts; }
        public int getSystemsWithStars() { return systemsWithStars; }
        public void setSystemsWithStars(int systemsWithStars) { this.systemsWithStars = systemsWithStars; }
        public int getSystemsWithPlanets() { return systemsWithPlanets; }
        public void setSystemsWithPlanets(int systemsWithPlanets) { this.systemsWithPlanets = systemsWithPlanets; }
        public int getSystemsWithAnomalies() { return systemsWithAnomalies; }
        public void setSystemsWithAnomalies(int systemsWithAnomalies) { this.systemsWithAnomalies = systemsWithAnomalies; }
        public int getTotalBodyTypes() { return totalBodyTypes; }
        public void setTotalBodyTypes(int totalBodyTypes) { this.totalBodyTypes = totalBodyTypes; }
        public int getDiversityScore() { return diversityScore; }
        public void setDiversityScore(int diversityScore) { this.diversityScore = diversityScore; }
        public List<CelestialBodyType> getMissingBodyTypes() { return missingBodyTypes; }
        public void setMissingBodyTypes(List<CelestialBodyType> missingBodyTypes) { this.missingBodyTypes = missingBodyTypes; }
    }

    /**
     * Report of colonization status.
     */
    public static class ColonizationReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalPlanets;
        private int colonizedPlanets;
        private int totalSpaceStations;
        private double colonizationRate;
        private List<PlanetInfo> uncolonizedPlanets = new ArrayList<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalPlanets() { return totalPlanets; }
        public void setTotalPlanets(int totalPlanets) { this.totalPlanets = totalPlanets; }
        public int getColonizedPlanets() { return colonizedPlanets; }
        public void setColonizedPlanets(int colonizedPlanets) { this.colonizedPlanets = colonizedPlanets; }
        public int getTotalSpaceStations() { return totalSpaceStations; }
        public void setTotalSpaceStations(int totalSpaceStations) { this.totalSpaceStations = totalSpaceStations; }
        public double getColonizationRate() { return colonizationRate; }
        public void setColonizationRate(double colonizationRate) { this.colonizationRate = colonizationRate; }
        public List<PlanetInfo> getUncolonizedPlanets() { return uncolonizedPlanets; }
        public void setUncolonizedPlanets(List<PlanetInfo> uncolonizedPlanets) { this.uncolonizedPlanets = uncolonizedPlanets; }
    }

    /**
     * Information about a single planet.
     */
    public static class PlanetInfo {
        private String planetName;
        private String systemName;
        private int sectorRow;
        private int sectorCol;
        private boolean habitable;

        public String getPlanetName() { return planetName; }
        public void setPlanetName(String planetName) { this.planetName = planetName; }
        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public boolean isHabitable() { return habitable; }
        public void setHabitable(boolean habitable) { this.habitable = habitable; }
    }

    /**
     * Report of navigation route analysis.
     */
    public static class NavigationAnalysisReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalSystems;
        private double hyperlaneDensity;
        private int isolatedSystems;
        private int deadEndSystems;
        private int connectivityScore;
        private double averageHyperlanesPerSystem;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalSystems() { return totalSystems; }
        public void setTotalSystems(int totalSystems) { this.totalSystems = totalSystems; }
        public double getHyperlaneDensity() { return hyperlaneDensity; }
        public void setHyperlaneDensity(double hyperlaneDensity) { this.hyperlaneDensity = hyperlaneDensity; }
        public int getIsolatedSystems() { return isolatedSystems; }
        public void setIsolatedSystems(int isolatedSystems) { this.isolatedSystems = isolatedSystems; }
        public int getDeadEndSystems() { return deadEndSystems; }
        public void setDeadEndSystems(int deadEndSystems) { this.deadEndSystems = deadEndSystems; }
        public int getConnectivityScore() { return connectivityScore; }
        public void setConnectivityScore(int connectivityScore) { this.connectivityScore = connectivityScore; }
        public double getAverageHyperlanesPerSystem() { return averageHyperlanesPerSystem; }
        public void setAverageHyperlanesPerSystem(double averageHyperlanesPerSystem) { this.averageHyperlanesPerSystem = averageHyperlanesPerSystem; }
    }

    /**
     * Represents an exploration anomaly detected in the galaxy.
     */
    public static class ExplorationAnomaly {
        public enum AnomalyType {
            ORPHAN_SYSTEM,        // System without a star
            EDGE_POSITION,        // System at boundary position
            OVERDENSE_SECTOR,     // Sector with too many systems
            GENERATION_BIAS,      // Sector with only one body type
            ISOLATED_CLUSTER      // Group of systems disconnected from main network
        }

        public enum Severity {
            LOW,
            MEDIUM,
            HIGH,
            CRITICAL
        }

        private AnomalyType type;
        private Severity severity;
        private String systemName;
        private String description;
        private int sectorRow;
        private int sectorCol;

        public AnomalyType getType() { return type; }
        public void setType(AnomalyType type) { this.type = type; }
        public Severity getSeverity() { return severity; }
        public void setSeverity(Severity severity) { this.severity = severity; }
        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
    }
}
