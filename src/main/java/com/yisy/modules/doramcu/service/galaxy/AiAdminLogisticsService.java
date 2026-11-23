package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.hex.ConstructionModuleType;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.RawResourceType;
import com.yisy.modules.doramcu.hex.RefinedResourceType;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.ResourceStorage;
import com.yisy.modules.doramcu.hex.TransportOrder;
import com.yisy.modules.doramcu.hex.TransportPriority;
import com.yisy.modules.doramcu.hex.manager.EconomicState;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logistics Manager service — monitors resource distribution, transport efficiency,
 * and supply chain health across a galaxy's empires.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * </p>
 * <ul>
 *   <li>Monitors transport order backlog and identifies stalled transports</li>
 *   <li>Checks resource storage levels against consumption rates to detect shortages</li>
 *   <li>Evaluates fleet utilization for transport assignments</li>
 *   <li>Creates work orders to address supply chain bottlenecks</li>
 *   <li>Recommends transport consolidation for efficiency</li>
 *   <li>Tracks resource flow balance between production and consumption</li>
 * </ul>
 * <p>
 * Logistics priority order for work order creation:
 * </p>
 * <ol>
 *   <li>CRITICAL — Resource starvation: consumption exceeds production with low reserves</li>
 *   <li>CRITICAL — Stalled transports: transports stuck for more than expected turns</li>
 *   <li>HIGH — Unassigned urgent transports: urgent orders without fleet assignment</li>
 *   <li>HIGH — Storage capacity approaching limit: risk of resource overflow</li>
 *   <li>MEDIUM — Consolidation opportunity: multiple transports on same route</li>
 *   <li>MEDIUM — Fleet underutilization: available fleets not assigned to transports</li>
 *   <li>LOW — Optimization: suggest route improvements or priority adjustments</li>
 * </ol>
 */
@Service
public class AiAdminLogisticsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiAdminLogisticsService.class);

    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /** Threshold: raw resource amount below which a shortage is flagged. */
    public static final int RAW_RESOURCE_SHORTAGE_THRESHOLD = 20;

    /** Threshold: refined resource amount below which a shortage is flagged. */
    public static final int REFINED_RESOURCE_SHORTAGE_THRESHOLD = 10;

    /** Threshold: module amount below which a shortage is flagged. */
    public static final int MODULE_SHORTAGE_THRESHOLD = 5;

    /** Threshold: storage usage percentage above which capacity warning is raised. */
    public static final int STORAGE_CAPACITY_WARNING_PERCENT = 80;

    /** Threshold: turns remaining above which a transport is considered stalled. */
    public static final int STALLED_TRANSPORT_TURNS = 10;

    /** Default number of transports per route before suggesting consolidation. */
    public static final int CONSOLIDATION_THRESHOLD = 3;

    public AiAdminLogisticsService(ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Transport assessment
    // ------------------------------------------------------------------

    /**
     * Assesses the transport state of a resource pipeline and identifies issues.
     * <p>
     * Checks for stalled transports, unassigned urgent orders, and consolidation
     * opportunities. Returns a logistics report with findings and recommendations.
     * </p>
     *
     * @param pipeline the resource pipeline to assess
     * @return a logistics report with transport findings
     */
    public LogisticsReport assessTransportState(ResourcePipelineManager pipeline) {
        LogisticsReport report = new LogisticsReport();
        report.setEmpireId(pipeline.getOwnerEmpireId());

        List<TransportOrder> activeTransports = pipeline.getActiveTransportOrders();
        report.setActiveTransportCount(activeTransports.size());

        // Check for stalled transports
        List<TransportOrder> stalled = new ArrayList<>();
        for (TransportOrder transport : activeTransports) {
            if (transport.getTurnsRemaining() > STALLED_TRANSPORT_TURNS) {
                stalled.add(transport);
            }
        }
        report.setStalledTransportCount(stalled.size());
        report.setStalledTransports(stalled);

        // Check for unassigned urgent transports
        List<TransportOrder> unassignedUrgent = new ArrayList<>();
        for (TransportOrder transport : activeTransports) {
            if (transport.getAssignedFleetId() == null &&
                    (transport.getPriority() == TransportPriority.URGENT ||
                     transport.getPriority() == TransportPriority.BULK)) {
                unassignedUrgent.add(transport);
            }
        }
        report.setUnassignedUrgentCount(unassignedUrgent.size());

        // Check for consolidation opportunities
        int consolidations = pipeline.consolidateTransports();
        report.setConsolidationsPerformed(consolidations);

        // Calculate total cargo in transit
        int totalCargo = 0;
        for (TransportOrder transport : activeTransports) {
            totalCargo += transport.getTotalAmount();
        }
        report.setTotalCargoInTransit(totalCargo);

        // Count by priority
        Map<String, Integer> byPriority = new HashMap<>();
        for (TransportPriority priority : TransportPriority.values()) {
            byPriority.put(priority.name(), 0);
        }
        for (TransportOrder transport : activeTransports) {
            String key = transport.getPriority().name();
            byPriority.merge(key, 1, Integer::sum);
        }
        report.setTransportsByPriority(byPriority);

        log.debug("Transport assessment for empire {}: {} active, {} stalled, {} unassigned urgent",
                pipeline.getOwnerEmpireId(), activeTransports.size(), stalled.size(), unassignedUrgent.size());

        return report;
    }

    // ------------------------------------------------------------------
    // Resource storage assessment
    // ------------------------------------------------------------------

    /**
     * Assesses resource storage levels and identifies shortages or capacity issues.
     * <p>
     * Compares current resource amounts against thresholds and consumption rates
     * to detect potential shortages. Also checks storage capacity utilization.
     * </p>
     *
     * @param pipeline the resource pipeline to assess
     * @return a resource storage report
     */
    public ResourceStorageReport assessResourceStorage(ResourcePipelineManager pipeline) {
        ResourceStorageReport report = new ResourceStorageReport();
        report.setEmpireId(pipeline.getOwnerEmpireId());

        // Assess raw resources
        ResourceStorage<RawResourceType> rawStorage = pipeline.getRawStorage();
        Map<String, Integer> rawLevels = new HashMap<>();
        List<String> rawShortages = new ArrayList<>();
        for (RawResourceType type : RawResourceType.values()) {
            int amount = rawStorage.getAmount(type);
            rawLevels.put(type.name(), amount);
            if (amount < RAW_RESOURCE_SHORTAGE_THRESHOLD) {
                rawShortages.add(type.name() + "=" + amount);
            }
        }
        report.setRawResourceLevels(rawLevels);
        report.setRawShortages(rawShortages);
        report.setRawCapacityUtilization(rawStorage.getTotalCapacity() > 0
                ? (int) ((double) rawStorage.getTotalAmount() / rawStorage.getTotalCapacity() * 100) : 0);

        // Assess refined resources
        ResourceStorage<RefinedResourceType> refinedStorage = pipeline.getRefinedStorage();
        Map<String, Integer> refinedLevels = new HashMap<>();
        List<String> refinedShortages = new ArrayList<>();
        for (RefinedResourceType type : RefinedResourceType.values()) {
            int amount = refinedStorage.getAmount(type);
            refinedLevels.put(type.name(), amount);
            if (amount < REFINED_RESOURCE_SHORTAGE_THRESHOLD) {
                refinedShortages.add(type.name() + "=" + amount);
            }
        }
        report.setRefinedResourceLevels(refinedLevels);
        report.setRefinedShortages(refinedShortages);
        report.setRefinedCapacityUtilization(refinedStorage.getTotalCapacity() > 0
                ? (int) ((double) refinedStorage.getTotalAmount() / refinedStorage.getTotalCapacity() * 100) : 0);

        // Assess modules
        ResourceStorage<ConstructionModuleType> moduleStorage = pipeline.getModuleStorage();
        Map<String, Integer> moduleLevels = new HashMap<>();
        List<String> moduleShortages = new ArrayList<>();
        for (ConstructionModuleType type : ConstructionModuleType.values()) {
            int amount = moduleStorage.getAmount(type);
            moduleLevels.put(type.name(), amount);
            if (amount < MODULE_SHORTAGE_THRESHOLD) {
                moduleShortages.add(type.name() + "=" + amount);
            }
        }
        report.setModuleLevels(moduleLevels);
        report.setModuleShortages(moduleShortages);
        report.setModuleCapacityUtilization(moduleStorage.getTotalCapacity() > 0
                ? (int) ((double) moduleStorage.getTotalAmount() / moduleStorage.getTotalCapacity() * 100) : 0);

        // Check for capacity warnings
        List<String> capacityWarnings = new ArrayList<>();
        if (report.getRawCapacityUtilization() >= STORAGE_CAPACITY_WARNING_PERCENT) {
            capacityWarnings.add("Raw storage at " + report.getRawCapacityUtilization() + "% capacity");
        }
        if (report.getRefinedCapacityUtilization() >= STORAGE_CAPACITY_WARNING_PERCENT) {
            capacityWarnings.add("Refined storage at " + report.getRefinedCapacityUtilization() + "% capacity");
        }
        if (report.getModuleCapacityUtilization() >= STORAGE_CAPACITY_WARNING_PERCENT) {
            capacityWarnings.add("Module storage at " + report.getModuleCapacityUtilization() + "% capacity");
        }
        report.setCapacityWarnings(capacityWarnings);

        log.debug("Resource storage for empire {}: {} raw shortages, {} refined shortages, {} module shortages",
                pipeline.getOwnerEmpireId(), rawShortages.size(), refinedShortages.size(), moduleShortages.size());

        return report;
    }

    // ------------------------------------------------------------------
    // Fleet utilization assessment
    // ------------------------------------------------------------------

    /**
     * Assesses fleet utilization for transport operations.
     * <p>
     * Checks how many fleets are available vs. assigned to transports,
     * and identifies underutilization opportunities.
     * </p>
     *
     * @param availableFleets list of available fleets
     * @param assignedFleetIds set of fleet IDs currently assigned to transports
     * @return a fleet utilization report
     */
    public FleetUtilizationReport assessFleetUtilization(List<Fleet> availableFleets,
                                                          java.util.Set<Integer> assignedFleetIds) {
        FleetUtilizationReport report = new FleetUtilizationReport();

        int totalFleets = availableFleets != null ? availableFleets.size() : 0;
        int assignedCount = 0;
        int availableCount = 0;

        if (availableFleets != null) {
            for (Fleet fleet : availableFleets) {
                if (assignedFleetIds != null && assignedFleetIds.contains(fleet.getId())) {
                    assignedCount++;
                } else {
                    availableCount++;
                }
            }
        }

        report.setTotalFleets(totalFleets);
        report.setAssignedFleets(assignedCount);
        report.setAvailableFleets(availableCount);
        report.setUtilizationPercent(totalFleets > 0
                ? (int) ((double) assignedCount / totalFleets * 100) : 0);

        log.debug("Fleet utilization: {}/{} assigned ({}%)", assignedCount, totalFleets, report.getUtilizationPercent());

        return report;
    }

    // ------------------------------------------------------------------
    // Production-consumption balance
    // ------------------------------------------------------------------

    /**
     * Assesses the balance between resource production and consumption rates.
     * <p>
     * Compares mining rates against ship maintenance consumption to identify
     * resources where consumption exceeds production (starvation risk).
     * </p>
     *
     * @param pipeline the resource pipeline to assess
     * @param fleets list of fleets for maintenance consumption calculation
     * @return a production-consumption balance report
     */
    public ProductionConsumptionReport assessProductionConsumptionBalance(
            ResourcePipelineManager pipeline, List<Fleet> fleets) {

        ProductionConsumptionReport report = new ProductionConsumptionReport();
        report.setEmpireId(pipeline.getOwnerEmpireId());

        // Get aggregate mining rate
        Map<RawResourceType, Integer> miningRates = pipeline.getAggregateMiningRate();

        // Calculate consumption from ship maintenance
        Map<RawResourceType, Integer> consumptionRates = new HashMap<>();
        int totalFuelConsumed = 0;
        if (fleets != null) {
            for (Fleet fleet : fleets) {
                for (com.yisy.modules.doramcu.hex.Ship ship : fleet.getShips()) {
                    if (ship.isDestroyed()) continue;
                    int fuelNeeded = ship.getFuelConsumptionPerTurn();
                    totalFuelConsumed += fuelNeeded;
                    consumptionRates.merge(RawResourceType.GAS, fuelNeeded, Integer::sum);
                }
            }
        }
        report.setTotalFuelConsumption(totalFuelConsumed);

        // Compare production vs consumption for each resource
        Map<String, BalanceStatus> balanceStatus = new HashMap<>();
        for (RawResourceType type : RawResourceType.values()) {
            int production = miningRates.getOrDefault(type, 0);
            int consumption = consumptionRates.getOrDefault(type, 0);
            int net = production - consumption;

            BalanceStatus status;
            if (net < 0) {
                status = BalanceStatus.DEFICIT;
            } else if (net == 0 && consumption > 0) {
                status = BalanceStatus.BREAKEVEN;
            } else {
                status = BalanceStatus.SURPLUS;
            }
            balanceStatus.put(type.name(), status);
        }
        report.setBalanceStatus(balanceStatus);

        // Identify deficit resources
        List<String> deficitResources = new ArrayList<>();
        for (Map.Entry<String, BalanceStatus> entry : balanceStatus.entrySet()) {
            if (entry.getValue() == BalanceStatus.DEFICIT) {
                deficitResources.add(entry.getKey());
            }
        }
        report.setDeficitResources(deficitResources);

        log.debug("Production-consumption for empire {}: {} deficits, fuel consumption={}",
                pipeline.getOwnerEmpireId(), deficitResources.size(), totalFuelConsumed);

        return report;
    }

    // ------------------------------------------------------------------
    // Work order creation
    // ------------------------------------------------------------------

    /**
     * Creates work orders to address logistics issues identified by the reports.
     * <p>
     * Orders are created in priority order: CRITICAL first, then HIGH, MEDIUM, LOW.
     * </p>
     *
     * @param galaxyName the galaxy name for context
     * @param transportReport the transport assessment report
     * @param storageReport the resource storage report
     * @param balanceReport the production-consumption balance report
     * @return list of created order texts
     */
    public List<String> createLogisticsWorkOrders(String galaxyName,
                                                    LogisticsReport transportReport,
                                                    ResourceStorageReport storageReport,
                                                    ProductionConsumptionReport balanceReport) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxyName;

        // CRITICAL: Resource starvation — consumption exceeds production
        if (!balanceReport.getDeficitResources().isEmpty()) {
            for (String deficitResource : balanceReport.getDeficitResources()) {
                String orderText = String.format("[CRITICAL] Resource starvation for %s in galaxy '%s'. " +
                        "Consumption exceeds production. Action: Increase mining operations or reduce fleet size.",
                        deficitResource, galaxyName);
                submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
            }
        }

        // CRITICAL: Stalled transports
        if (transportReport.getStalledTransportCount() > 0) {
            String orderText = String.format("[CRITICAL] %d stalled transports detected in galaxy '%s'. " +
                    "Transports stuck for more than %d turns. Action: Reassign fleets or cancel and recreate orders.",
                    transportReport.getStalledTransportCount(), galaxyName, STALLED_TRANSPORT_TURNS);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Unassigned urgent transports
        if (transportReport.getUnassignedUrgentCount() > 0) {
            String orderText = String.format("[HIGH] %d urgent transports without fleet assignment in galaxy '%s'. " +
                    "Action: Assign available fleets to urgent transport orders.",
                    transportReport.getUnassignedUrgentCount(), galaxyName);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Storage capacity warnings
        if (!storageReport.getCapacityWarnings().isEmpty()) {
            String orderText = String.format("[HIGH] Storage capacity warnings in galaxy '%s': %s. " +
                    "Action: Expand storage or accelerate resource consumption through production.",
                    galaxyName, String.join("; ", storageReport.getCapacityWarnings()));
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Raw resource shortages
        if (!storageReport.getRawShortages().isEmpty()) {
            String orderText = String.format("[HIGH] Raw resource shortages in galaxy '%s': %s. " +
                    "Action: Deploy mining operations to affected resource types.",
                    galaxyName, String.join(", ", storageReport.getRawShortages()));
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Refined resource shortages
        if (!storageReport.getRefinedShortages().isEmpty()) {
            String orderText = String.format("[HIGH] Refined resource shortages in galaxy '%s': %s. " +
                    "Action: Add processing facilities to convert raw resources.",
                    galaxyName, String.join(", ", storageReport.getRefinedShortages()));
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Module shortages
        if (!storageReport.getModuleShortages().isEmpty()) {
            String orderText = String.format("[HIGH] Module shortages in galaxy '%s': %s. " +
                    "Action: Add module assemblers to build construction modules.",
                    galaxyName, String.join(", ", storageReport.getModuleShortages()));
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Consolidation opportunities
        if (transportReport.getConsolidationsPerformed() > 0) {
            String orderText = String.format("[MEDIUM] %d transport consolidations performed in galaxy '%s'. " +
                    "Routes optimized for efficiency.",
                    transportReport.getConsolidationsPerformed(), galaxyName);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        log.info("Created {} logistics work orders for galaxy '{}'", ordersCreated.size(), galaxyName);
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Assessment integration
    // ------------------------------------------------------------------

    /**
     * Performs a complete logistics assessment for a galaxy's empire and adds findings
     * to the galaxy assessment. This is called by GalaxyOverseerService during
     * the health check cycle.
     *
     * @param galaxyName the galaxy name for context
     * @param pipeline the resource pipeline to assess
     * @param fleets the fleets in the galaxy for maintenance calculation
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    public int assessLogisticsNeeds(String galaxyName,
                                     ResourcePipelineManager pipeline,
                                     List<Fleet> fleets,
                                     GalaxyOverseerService.GalaxyAssessment assessment) {
        LogisticsReport transportReport = assessTransportState(pipeline);
        ResourceStorageReport storageReport = assessResourceStorage(pipeline);
        ProductionConsumptionReport balanceReport = assessProductionConsumptionBalance(pipeline, fleets);

        // Add transport findings
        if (transportReport.getStalledTransportCount() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("%d stalled transports in galaxy '%s'. Supply chain at risk.",
                            transportReport.getStalledTransportCount(), galaxyName),
                    "Reassign fleets to stalled transports or cancel and recreate orders."
            );
        } else if (transportReport.getActiveTransportCount() > 0) {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Transport health: %d active transports, %d cargo in transit.",
                            transportReport.getActiveTransportCount(), transportReport.getTotalCargoInTransit())
            );
        }

        // Add resource shortage findings
        int totalShortages = storageReport.getRawShortages().size() +
                            storageReport.getRefinedShortages().size() +
                            storageReport.getModuleShortages().size();
        if (totalShortages > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("Resource shortages detected: %d raw, %d refined, %d module types below threshold.",
                            storageReport.getRawShortages().size(),
                            storageReport.getRefinedShortages().size(),
                            storageReport.getModuleShortages().size()),
                    "Deploy mining operations and processing facilities to address shortages."
            );
        }

        // Add production-consumption balance findings
        if (!balanceReport.getDeficitResources().isEmpty()) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("Production-consumption deficit for: %s. Resources will be depleted.",
                            String.join(", ", balanceReport.getDeficitResources())),
                    "Increase mining operations for deficit resources or reduce fleet consumption."
            );
        }

        // Add capacity warnings
        if (!storageReport.getCapacityWarnings().isEmpty()) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("Storage capacity warnings: %s.",
                            String.join("; ", storageReport.getCapacityWarnings())),
                    "Expand storage facilities or accelerate resource processing."
            );
        }

        // Create work orders
        List<String> ordersCreated = createLogisticsWorkOrders(
                galaxyName, transportReport, storageReport, balanceReport);

        log.info("Logistics assessment for '{}': {} active transports, {} shortages, {} deficits, {} orders",
                galaxyName, transportReport.getActiveTransportCount(), totalShortages,
                balanceReport.getDeficitResources().size(), ordersCreated.size());

        return ordersCreated.size();
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /**
     * Submits an order through the AiAdminService if available.
     */
    private void submitOrder(String submittedBy, String orderText) {
        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService != null) {
            aiAdminService.submitOrder(submittedBy, orderText);
        }
    }

    // ------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------

    /**
     * Balance status between production and consumption.
     */
    public enum BalanceStatus {
        /** Production exceeds consumption — resources accumulating. */
        SURPLUS,
        /** Production equals consumption — steady state. */
        BREAKEVEN,
        /** Consumption exceeds production — resources depleting. */
        DEFICIT
    }

    /**
     * Report of transport state for an empire.
     */
    public static class LogisticsReport {
        private int empireId;
        private int activeTransportCount;
        private int stalledTransportCount;
        private List<TransportOrder> stalledTransports = new ArrayList<>();
        private int unassignedUrgentCount;
        private int consolidationsPerformed;
        private int totalCargoInTransit;
        private Map<String, Integer> transportsByPriority = new HashMap<>();

        public int getEmpireId() { return empireId; }
        public void setEmpireId(int empireId) { this.empireId = empireId; }
        public int getActiveTransportCount() { return activeTransportCount; }
        public void setActiveTransportCount(int activeTransportCount) { this.activeTransportCount = activeTransportCount; }
        public int getStalledTransportCount() { return stalledTransportCount; }
        public void setStalledTransportCount(int stalledTransportCount) { this.stalledTransportCount = stalledTransportCount; }
        public List<TransportOrder> getStalledTransports() { return stalledTransports; }
        public void setStalledTransports(List<TransportOrder> stalledTransports) { this.stalledTransports = stalledTransports; }
        public int getUnassignedUrgentCount() { return unassignedUrgentCount; }
        public void setUnassignedUrgentCount(int unassignedUrgentCount) { this.unassignedUrgentCount = unassignedUrgentCount; }
        public int getConsolidationsPerformed() { return consolidationsPerformed; }
        public void setConsolidationsPerformed(int consolidationsPerformed) { this.consolidationsPerformed = consolidationsPerformed; }
        public int getTotalCargoInTransit() { return totalCargoInTransit; }
        public void setTotalCargoInTransit(int totalCargoInTransit) { this.totalCargoInTransit = totalCargoInTransit; }
        public Map<String, Integer> getTransportsByPriority() { return transportsByPriority; }
        public void setTransportsByPriority(Map<String, Integer> transportsByPriority) { this.transportsByPriority = transportsByPriority; }
    }

    /**
     * Report of resource storage levels for an empire.
     */
    public static class ResourceStorageReport {
        private int empireId;
        private Map<String, Integer> rawResourceLevels = new HashMap<>();
        private List<String> rawShortages = new ArrayList<>();
        private int rawCapacityUtilization;
        private Map<String, Integer> refinedResourceLevels = new HashMap<>();
        private List<String> refinedShortages = new ArrayList<>();
        private int refinedCapacityUtilization;
        private Map<String, Integer> moduleLevels = new HashMap<>();
        private List<String> moduleShortages = new ArrayList<>();
        private int moduleCapacityUtilization;
        private List<String> capacityWarnings = new ArrayList<>();

        public int getEmpireId() { return empireId; }
        public void setEmpireId(int empireId) { this.empireId = empireId; }
        public Map<String, Integer> getRawResourceLevels() { return rawResourceLevels; }
        public void setRawResourceLevels(Map<String, Integer> rawResourceLevels) { this.rawResourceLevels = rawResourceLevels; }
        public List<String> getRawShortages() { return rawShortages; }
        public void setRawShortages(List<String> rawShortages) { this.rawShortages = rawShortages; }
        public int getRawCapacityUtilization() { return rawCapacityUtilization; }
        public void setRawCapacityUtilization(int rawCapacityUtilization) { this.rawCapacityUtilization = rawCapacityUtilization; }
        public Map<String, Integer> getRefinedResourceLevels() { return refinedResourceLevels; }
        public void setRefinedResourceLevels(Map<String, Integer> refinedResourceLevels) { this.refinedResourceLevels = refinedResourceLevels; }
        public List<String> getRefinedShortages() { return refinedShortages; }
        public void setRefinedShortages(List<String> refinedShortages) { this.refinedShortages = refinedShortages; }
        public int getRefinedCapacityUtilization() { return refinedCapacityUtilization; }
        public void setRefinedCapacityUtilization(int refinedCapacityUtilization) { this.refinedCapacityUtilization = refinedCapacityUtilization; }
        public Map<String, Integer> getModuleLevels() { return moduleLevels; }
        public void setModuleLevels(Map<String, Integer> moduleLevels) { this.moduleLevels = moduleLevels; }
        public List<String> getModuleShortages() { return moduleShortages; }
        public void setModuleShortages(List<String> moduleShortages) { this.moduleShortages = moduleShortages; }
        public int getModuleCapacityUtilization() { return moduleCapacityUtilization; }
        public void setModuleCapacityUtilization(int moduleCapacityUtilization) { this.moduleCapacityUtilization = moduleCapacityUtilization; }
        public List<String> getCapacityWarnings() { return capacityWarnings; }
        public void setCapacityWarnings(List<String> capacityWarnings) { this.capacityWarnings = capacityWarnings; }
    }

    /**
     * Report of fleet utilization for transport operations.
     */
    public static class FleetUtilizationReport {
        private int totalFleets;
        private int assignedFleets;
        private int availableFleets;
        private int utilizationPercent;

        public int getTotalFleets() { return totalFleets; }
        public void setTotalFleets(int totalFleets) { this.totalFleets = totalFleets; }
        public int getAssignedFleets() { return assignedFleets; }
        public void setAssignedFleets(int assignedFleets) { this.assignedFleets = assignedFleets; }
        public int getAvailableFleets() { return availableFleets; }
        public void setAvailableFleets(int availableFleets) { this.availableFleets = availableFleets; }
        public int getUtilizationPercent() { return utilizationPercent; }
        public void setUtilizationPercent(int utilizationPercent) { this.utilizationPercent = utilizationPercent; }
    }

    /**
     * Report of production-consumption balance for an empire.
     */
    public static class ProductionConsumptionReport {
        private int empireId;
        private Map<String, BalanceStatus> balanceStatus = new HashMap<>();
        private List<String> deficitResources = new ArrayList<>();
        private int totalFuelConsumption;

        public int getEmpireId() { return empireId; }
        public void setEmpireId(int empireId) { this.empireId = empireId; }
        public Map<String, BalanceStatus> getBalanceStatus() { return balanceStatus; }
        public void setBalanceStatus(Map<String, BalanceStatus> balanceStatus) { this.balanceStatus = balanceStatus; }
        public List<String> getDeficitResources() { return deficitResources; }
        public void setDeficitResources(List<String> deficitResources) { this.deficitResources = deficitResources; }
        public int getTotalFuelConsumption() { return totalFuelConsumption; }
        public void setTotalFuelConsumption(int totalFuelConsumption) { this.totalFuelConsumption = totalFuelConsumption; }
    }
}
