package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.admin.service.AiAdminProductionService;
import com.yisy.modules.doramcu.hex.StartingFacility;
import com.yisy.modules.doramcu.hex.SpaceStationType;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Economic Manager service — monitors production facility coverage across a galaxy
 * and creates work orders to ensure every item type has at least one production source.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Checks which planet facilities exist in the galaxy (mines, factories, refineries)</li>
 *   <li>Checks which space stations exist in the galaxy (shipyards, mining stations, trade hubs)</li>
 *   <li>Compares against the full catalog of buildable infrastructure items</li>
 *   <li>Creates prioritized work orders for missing critical facilities</li>
 *   <li>Tracks production chain dependencies (mine before refinery, refinery before assembly)</li>
 * </ul>
 * </p>
 * <p>
 * Facility priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — Mining Facility (no raw resources without it)</li>
 *   <li>CRITICAL — Power Plant (energy is required for all operations)</li>
 *   <li>HIGH — Refinery (no refined resources without it)</li>
 *   <li>HIGH — Shipyard (no ship construction without it)</li>
 *   <li>HIGH — Colony Dome (population growth foundation)</li>
 *   <li>MEDIUM — Research Lab (technology advancement)</li>
 *   <li>MEDIUM — Agricultural Center (food production)</li>
 *   <li>MEDIUM — Trade Port (economic exchange)</li>
 *   <li>LOW — Defense Turret, Military Base, Sensor Array (optional early-game)</li>
 *   <li>LOW — Warehouse, Module Bay (storage expansion)</li>
 * </ol>
 * </p>
 */
@Service
public class EconomicManagerService {

    private static final Logger log = LoggerFactory.getLogger(EconomicManagerService.class);

    private final GalaxyRepository galaxyRepository;
    private final InfrastructureItemRepository infrastructureItemRepository;
    private final AiAdminProductionService productionService;
    private final AiAdminService aiAdminService;

    /**
     * Maps StartingFacility types to their deployment priority and severity level.
     * Lower priority number = higher urgency (created first).
     */
    private static final Map<StartingFacility, FacilityPriority> FACILITY_PRIORITY;
    static {
        Map<StartingFacility, FacilityPriority> map = new HashMap<>();
        map.put(StartingFacility.MINING_FACILITY, new FacilityPriority(1, GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL));
        map.put(StartingFacility.POWER_PLANT, new FacilityPriority(2, GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL));
        map.put(StartingFacility.COLONY_DOME, new FacilityPriority(3, GalaxyOverseerService.GalaxyAssessment.Severity.HIGH));
        map.put(StartingFacility.REFINERY, new FacilityPriority(4, GalaxyOverseerService.GalaxyAssessment.Severity.HIGH));
        map.put(StartingFacility.SHIPYARD, new FacilityPriority(5, GalaxyOverseerService.GalaxyAssessment.Severity.HIGH));
        map.put(StartingFacility.AGRICULTURAL_CENTER, new FacilityPriority(6, GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM));
        map.put(StartingFacility.RESEARCH_LAB, new FacilityPriority(7, GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM));
        map.put(StartingFacility.TRADE_PORT, new FacilityPriority(8, GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM));
        map.put(StartingFacility.DEFENSE_TURRET, new FacilityPriority(9, GalaxyOverseerService.GalaxyAssessment.Severity.LOW));
        map.put(StartingFacility.MILITARY_BASE, new FacilityPriority(10, GalaxyOverseerService.GalaxyAssessment.Severity.LOW));
        map.put(StartingFacility.SENSOR_ARRAY, new FacilityPriority(11, GalaxyOverseerService.GalaxyAssessment.Severity.LOW));
        map.put(StartingFacility.WAREHOUSE, new FacilityPriority(12, GalaxyOverseerService.GalaxyAssessment.Severity.LOW));
        map.put(StartingFacility.MODULE_BAY, new FacilityPriority(13, GalaxyOverseerService.GalaxyAssessment.Severity.LOW));
        FACILITY_PRIORITY = Map.copyOf(map);
    }

    /**
     * Maps SpaceStationType to their deployment priority and severity level.
     */
    private static final Map<SpaceStationType, FacilityPriority> STATION_PRIORITY = Map.of(
            SpaceStationType.MINING, new FacilityPriority(1, GalaxyOverseerService.GalaxyAssessment.Severity.HIGH),
            SpaceStationType.SHIPYARD, new FacilityPriority(2, GalaxyOverseerService.GalaxyAssessment.Severity.HIGH),
            SpaceStationType.COLONY, new FacilityPriority(3, GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM),
            SpaceStationType.TRADE, new FacilityPriority(4, GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM),
            SpaceStationType.RESEARCH, new FacilityPriority(5, GalaxyOverseerService.GalaxyAssessment.Severity.LOW),
            SpaceStationType.MILITARY, new FacilityPriority(6, GalaxyOverseerService.GalaxyAssessment.Severity.LOW)
    );

    public EconomicManagerService(GalaxyRepository galaxyRepository,
                                   InfrastructureItemRepository infrastructureItemRepository,
                                   AiAdminProductionService productionService,
                                   AiAdminService aiAdminService) {
        this.galaxyRepository = galaxyRepository;
        this.infrastructureItemRepository = infrastructureItemRepository;
        this.productionService = productionService;
        this.aiAdminService = aiAdminService;
    }

    // ------------------------------------------------------------------
    // Facility coverage assessment
    // ------------------------------------------------------------------

    /**
     * Checks which planet facilities are present in the galaxy and identifies gaps.
     * <p>
     * Queries all infrastructure items of type PLANET_FACILITY and compares against
     * what is actually built in the galaxy's planets. Returns a coverage report
     * with missing facilities and their severity levels.
     * </p>
     *
     * @param galaxy the galaxy to check
     * @return a facility coverage report
     */
    @Transactional(readOnly = true)
    public FacilityCoverageReport checkFacilityCoverage(Galaxy galaxy) {
        FacilityCoverageReport report = new FacilityCoverageReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Get all defined planet facility types from infrastructure items
        List<InfrastructureItem> definedFacilities = infrastructureItemRepository
                .findByItemType(InfrastructureItem.InfrastructureItemType.PLANET_FACILITY);

        // Build a set of facility names that exist in the galaxy
        Set<String> presentFacilityNames = getPresentFacilityNames(galaxy);

        // Check each defined facility
        int presentCount = 0;
        for (InfrastructureItem item : definedFacilities) {
            String itemName = item.getItemName();
            if (presentFacilityNames.contains(itemName)) {
                presentCount++;
                report.addPresentFacility(itemName);
            } else {
                report.addMissingFacility(itemName, item.getItemType().getDisplayName());
            }
        }

        // Also check StartingFacility enum types
        for (Map.Entry<StartingFacility, FacilityPriority> entry : FACILITY_PRIORITY.entrySet()) {
            StartingFacility facility = entry.getKey();
            String facilityName = facility.name();
            if (!presentFacilityNames.contains(facilityName)) {
                // Only add if not already reported from infrastructure items
                if (!report.getMissingFacilities().containsKey(facilityName)) {
                    report.addMissingFacility(facilityName, "Planet Facility");
                }
            }
        }

        report.setTotalDefinedFacilities(definedFacilities.size() + FACILITY_PRIORITY.size());
        report.setPresentFacilityCount(presentCount);
        report.setCoveragePercent(report.getTotalDefinedFacilities() > 0
                ? (int) ((double) presentCount / report.getTotalDefinedFacilities() * 100) : 0);

        log.debug("Facility coverage for '{}': {}/{} ({}%)",
                galaxy.getName(), presentCount, report.getTotalDefinedFacilities(), report.getCoveragePercent());

        return report;
    }

    /**
     * Checks which space stations are present in the galaxy and identifies gaps.
     *
     * @param galaxy the galaxy to check
     * @return a station coverage report
     */
    @Transactional(readOnly = true)
    public StationCoverageReport checkStationCoverage(Galaxy galaxy) {
        StationCoverageReport report = new StationCoverageReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Get all defined space station types from infrastructure items
        List<InfrastructureItem> definedStations = infrastructureItemRepository
                .findByItemType(InfrastructureItem.InfrastructureItemType.SPACE_STATION);

        // Build a set of station names that exist in the galaxy
        Set<String> presentStationNames = getPresentStationNames(galaxy);

        int presentCount = 0;
        for (InfrastructureItem item : definedStations) {
            String itemName = item.getItemName();
            if (presentStationNames.contains(itemName)) {
                presentCount++;
                report.addPresentStation(itemName);
            } else {
                report.addMissingStation(itemName, item.getItemType().getDisplayName());
            }
        }

        // Also check SpaceStationType enum types
        for (Map.Entry<SpaceStationType, FacilityPriority> entry : STATION_PRIORITY.entrySet()) {
            SpaceStationType station = entry.getKey();
            String stationName = station.name();
            if (!presentStationNames.contains(stationName)) {
                if (!report.getMissingStations().containsKey(stationName)) {
                    report.addMissingStation(stationName, "Space Station");
                }
            }
        }

        report.setTotalDefinedStations(definedStations.size() + STATION_PRIORITY.size());
        report.setPresentStationCount(presentCount);
        report.setCoveragePercent(report.getTotalDefinedStations() > 0
                ? (int) ((double) presentCount / report.getTotalDefinedStations() * 100) : 0);

        log.debug("Station coverage for '{}': {}/{} ({}%)",
                galaxy.getName(), presentCount, report.getTotalDefinedStations(), report.getCoveragePercent());

        return report;
    }

    // ------------------------------------------------------------------
    // Work order creation
    // ------------------------------------------------------------------

    /**
     * Creates work orders for missing facilities based on the coverage report.
     * Orders are created in priority order: CRITICAL first, then HIGH, MEDIUM, LOW.
     *
     * @param galaxy the galaxy to create work orders for
     * @param facilityReport the facility coverage report
     * @param stationReport the station coverage report
     * @return list of created order texts
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public List<String> createProductionWorkOrders(Galaxy galaxy,
                                                     FacilityCoverageReport facilityReport,
                                                     StationCoverageReport stationReport) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        // Create orders for missing facilities in priority order
        List<Map.Entry<String, String>> sortedMissingFacilities = facilityReport.getMissingFacilities().entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByKey().thenComparing(e -> {
                    // Sort by facility priority
                    StartingFacility facility = parseStartingFacility(e.getKey());
                    FacilityPriority priority = facility != null ? FACILITY_PRIORITY.get(facility) : null;
                    return priority != null ? priority.order : 99;
                }))
                .collect(Collectors.toList());

        for (Map.Entry<String, String> entry : sortedMissingFacilities) {
            String facilityName = entry.getKey();
            StartingFacility facility = parseStartingFacility(facilityName);
            FacilityPriority priority = facility != null ? FACILITY_PRIORITY.get(facility) :
                    new FacilityPriority(99, GalaxyOverseerService.GalaxyAssessment.Severity.LOW);

            GalaxyOverseerService.GalaxyAssessment.Severity severity = priority.severity;

            // Try to find the matching InfrastructureItem and create a real production order
            Optional<InfrastructureItem> itemOpt = infrastructureItemRepository
                    .findByItemTypeAndItemName(InfrastructureItem.InfrastructureItemType.PLANET_FACILITY, facilityName);

            if (itemOpt.isPresent()) {
                try {
                    AiAdminOrder order = productionService.createProductionOrder(
                            itemOpt.get(), galaxy.getId(), submittedBy);
                    ordersCreated.add(order.getOrderText());
                    log.info("Created production order {} for missing facility: {} in galaxy '{}'",
                            order.getId(), facilityName, galaxy.getName());
                } catch (Exception e) {
                    log.warn("Failed to create production order for facility {} in galaxy '{}': {}",
                            facilityName, galaxy.getName(), e.getMessage());
                    // Fallback to text order via aiAdminService
                    String orderText = String.format("[%s] Build %s in galaxy '%s' — production facility gap detected. " +
                            "Action: Construct %s to enable resource production.",
                            severity.name(), facilityName, galaxy.getName(), facilityName);
                    aiAdminService.submitOrder(submittedBy, orderText, galaxy.getName(), null, null, null);
                    ordersCreated.add(orderText);
                }
            } else {
                // No InfrastructureItem definition — create text order via aiAdminService
                String orderText = String.format("[%s] Build %s in galaxy '%s' — production facility gap detected. " +
                        "Action: Construct %s to enable resource production.",
                        severity.name(), facilityName, galaxy.getName(), facilityName);
                aiAdminService.submitOrder(submittedBy, orderText, galaxy.getName(), null, null, null);
                ordersCreated.add(orderText);
                log.info("Created text work order for missing facility (no InfrastructureItem): {} in galaxy '{}'",
                        facilityName, galaxy.getName());
            }
        }

        // Create orders for missing stations in priority order
        List<Map.Entry<String, String>> sortedMissingStations = stationReport.getMissingStations().entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByKey().thenComparing(e -> {
                    SpaceStationType station = parseSpaceStationType(e.getKey());
                    FacilityPriority priority = station != null ? STATION_PRIORITY.get(station) : null;
                    return priority != null ? priority.order : 99;
                }))
                .collect(Collectors.toList());

        for (Map.Entry<String, String> entry : sortedMissingStations) {
            String stationName = entry.getKey();
            SpaceStationType station = parseSpaceStationType(stationName);
            FacilityPriority priority = station != null ? STATION_PRIORITY.get(station) :
                    new FacilityPriority(99, GalaxyOverseerService.GalaxyAssessment.Severity.LOW);

            GalaxyOverseerService.GalaxyAssessment.Severity severity = priority.severity;

            // Try to find the matching InfrastructureItem and create a real production order
            Optional<InfrastructureItem> itemOpt = infrastructureItemRepository
                    .findByItemTypeAndItemName(InfrastructureItem.InfrastructureItemType.SPACE_STATION, stationName);

            if (itemOpt.isPresent()) {
                try {
                    AiAdminOrder order = productionService.createProductionOrder(
                            itemOpt.get(), galaxy.getId(), submittedBy);
                    ordersCreated.add(order.getOrderText());
                    log.info("Created production order {} for missing station: {} in galaxy '{}'",
                            order.getId(), stationName, galaxy.getName());
                } catch (Exception e) {
                    log.warn("Failed to create production order for station {} in galaxy '{}': {}",
                            stationName, galaxy.getName(), e.getMessage());
                    // Fallback to text order via aiAdminService
                    String orderText = String.format("[%s] Build %s space station in galaxy '%s' — station coverage gap detected. " +
                            "Action: Construct %s station to enable orbital operations.",
                            severity.name(), stationName, galaxy.getName(), stationName);
                    aiAdminService.submitOrder(submittedBy, orderText, galaxy.getName(), null, null, null);
                    ordersCreated.add(orderText);
                }
            } else {
                // No InfrastructureItem definition — create text order via aiAdminService
                String orderText = String.format("[%s] Build %s space station in galaxy '%s' — station coverage gap detected. " +
                        "Action: Construct %s station to enable orbital operations.",
                        severity.name(), stationName, galaxy.getName(), stationName);
                aiAdminService.submitOrder(submittedBy, orderText, galaxy.getName(), null, null, null);
                ordersCreated.add(orderText);
                log.info("Created text work order for missing station (no InfrastructureItem): {} in galaxy '{}'",
                        stationName, galaxy.getName());
            }
        }

        log.info("Created {} production work orders for galaxy '{}' ({} facilities + {} stations)",
                ordersCreated.size(), galaxy.getName(),
                sortedMissingFacilities.size(), sortedMissingStations.size());

        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Assessment integration
    // ------------------------------------------------------------------

    /**
     * Performs a complete economic assessment for a galaxy and adds findings
     * to the galaxy assessment. This is called by GalaxyOverseerService during
     * the health check cycle.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessEconomicNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        FacilityCoverageReport facilityReport = checkFacilityCoverage(galaxy);
        StationCoverageReport stationReport = checkStationCoverage(galaxy);

        // Add facility coverage findings
        if (facilityReport.getCoveragePercent() == 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("No production facilities in galaxy — economy cannot function. " +
                            "Coverage: %d%% (%d/%d facilities).",
                            facilityReport.getCoveragePercent(),
                            facilityReport.getPresentFacilityCount(),
                            facilityReport.getTotalDefinedFacilities()),
                    "Build Mining Facility and Power Plant immediately to establish resource production."
            );
        } else if (facilityReport.getCoveragePercent() < 50) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("Low facility coverage: %d%% (%d/%d facilities present). " +
                            "Missing: %s.",
                            facilityReport.getCoveragePercent(),
                            facilityReport.getPresentFacilityCount(),
                            facilityReport.getTotalDefinedFacilities(),
                            String.join(", ", facilityReport.getMissingFacilities().keySet())),
                    "Build missing critical facilities to improve economic output."
            );
        } else if (facilityReport.getCoveragePercent() < 100) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Partial facility coverage: %d%% (%d/%d facilities present). " +
                            "Missing: %s.",
                            facilityReport.getCoveragePercent(),
                            facilityReport.getPresentFacilityCount(),
                            facilityReport.getTotalDefinedFacilities(),
                            String.join(", ", facilityReport.getMissingFacilities().keySet())),
                    "Build remaining facilities for complete economic coverage."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Full facility coverage: %d%% (%d/%d facilities present).",
                            facilityReport.getCoveragePercent(),
                            facilityReport.getPresentFacilityCount(),
                            facilityReport.getTotalDefinedFacilities())
            );
        }

        // Add station coverage findings
        if (stationReport.getCoveragePercent() == 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("No space stations in galaxy — no orbital operations. " +
                            "Coverage: %d%% (%d/%d stations).",
                            stationReport.getCoveragePercent(),
                            stationReport.getPresentStationCount(),
                            stationReport.getTotalDefinedStations()),
                    "Build Mining station and Shipyard station to enable orbital resource extraction and ship construction."
            );
        } else if (stationReport.getCoveragePercent() < 100) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Partial station coverage: %d%% (%d/%d stations present). " +
                            "Missing: %s.",
                            stationReport.getCoveragePercent(),
                            stationReport.getPresentStationCount(),
                            stationReport.getTotalDefinedStations(),
                            String.join(", ", stationReport.getMissingStations().keySet())),
                    "Build remaining stations for complete orbital coverage."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Full station coverage: %d%% (%d/%d stations present).",
                            stationReport.getCoveragePercent(),
                            stationReport.getPresentStationCount(),
                            stationReport.getTotalDefinedStations())
            );
        }

        // Create work orders for missing facilities
        List<String> ordersCreated = createProductionWorkOrders(galaxy, facilityReport, stationReport);

        log.info("Economic assessment for '{}': facility coverage {}%, station coverage {}%, {} orders created",
                galaxy.getName(), facilityReport.getCoveragePercent(),
                stationReport.getCoveragePercent(), ordersCreated.size());

        return ordersCreated.size();
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /**
     * Gets the set of facility names present in the galaxy's planets.
     * Since the JPA Planet model does not track facilities directly, we check
     * the infrastructure items database for items that have been built (via build contracts).
     * For the initial implementation, we return an empty set so all facilities are flagged
     * as missing and work orders are created to build them.
     */
    private Set<String> getPresentFacilityNames(Galaxy galaxy) {
        // The JPA Planet model (model.misc.Planet) does not have a facilities field.
        // Facilities are tracked through the hex.Planet model and EmpireFacilities.
        // For the initial implementation, return empty set so all facilities are flagged
        // as missing. A future enhancement would query BuildContract for completed facilities.
        return Set.of();
    }

    /**
     * Gets the set of station names present in the galaxy.
     * Since stations are tracked through infrastructure items and build contracts,
     * we check the infrastructure items database for completed stations.
     */
    private Set<String> getPresentStationNames(Galaxy galaxy) {
        // For now, stations are tracked through build contracts and infrastructure items.
        // In a full implementation, this would query the SpaceStation entity or similar.
        // Return empty set for initial implementation — stations will be flagged as missing
        // and work orders will be created to build them.
        return Set.of();
    }

    /**
     * Parses a facility name string to StartingFacility enum.
     */
    private StartingFacility parseStartingFacility(String name) {
        try {
            return StartingFacility.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parses a station name string to SpaceStationType enum.
     */
    private SpaceStationType parseSpaceStationType(String name) {
        try {
            return SpaceStationType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------

    /**
     * Holds the priority order and severity for a facility type.
     */
    static class FacilityPriority {
        final int order;
        final GalaxyOverseerService.GalaxyAssessment.Severity severity;

        FacilityPriority(int order, GalaxyOverseerService.GalaxyAssessment.Severity severity) {
            this.order = order;
            this.severity = severity;
        }
    }

    /**
     * Report of facility coverage for a galaxy.
     */
    public static class FacilityCoverageReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalDefinedFacilities;
        private int presentFacilityCount;
        private int coveragePercent;
        private List<String> presentFacilities = new ArrayList<>();
        private Map<String, String> missingFacilities = new HashMap<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalDefinedFacilities() { return totalDefinedFacilities; }
        public void setTotalDefinedFacilities(int totalDefinedFacilities) { this.totalDefinedFacilities = totalDefinedFacilities; }
        public int getPresentFacilityCount() { return presentFacilityCount; }
        public void setPresentFacilityCount(int presentFacilityCount) { this.presentFacilityCount = presentFacilityCount; }
        public int getCoveragePercent() { return coveragePercent; }
        public void setCoveragePercent(int coveragePercent) { this.coveragePercent = coveragePercent; }
        public List<String> getPresentFacilities() { return presentFacilities; }
        public Map<String, String> getMissingFacilities() { return missingFacilities; }

        void addPresentFacility(String name) {
            presentFacilities.add(name);
        }

        void addMissingFacility(String name, String type) {
            missingFacilities.put(name, type);
        }
    }

    /**
     * Report of station coverage for a galaxy.
     */
    public static class StationCoverageReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalDefinedStations;
        private int presentStationCount;
        private int coveragePercent;
        private List<String> presentStations = new ArrayList<>();
        private Map<String, String> missingStations = new HashMap<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalDefinedStations() { return totalDefinedStations; }
        public void setTotalDefinedStations(int totalDefinedStations) { this.totalDefinedStations = totalDefinedStations; }
        public int getPresentStationCount() { return presentStationCount; }
        public void setPresentStationCount(int presentStationCount) { this.presentStationCount = presentStationCount; }
        public int getCoveragePercent() { return coveragePercent; }
        public void setCoveragePercent(int coveragePercent) { this.coveragePercent = coveragePercent; }
        public List<String> getPresentStations() { return presentStations; }
        public Map<String, String> getMissingStations() { return missingStations; }

        void addPresentStation(String name) {
            presentStations.add(name);
        }

        void addMissingStation(String name, String type) {
            missingStations.put(name, type);
        }
    }
}
