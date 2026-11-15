package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.cargo.CargoGenerationService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Links the tycoon economy to military production.
 * <p>
 * Production chain:
 * <ul>
 *   <li>Mines produce raw materials (STEEL, OIL, COAL) → deposited at stations</li>
 *   <li>Factories process raw materials into components (ELECTRONICS, MACHINERY) → deposited at stations</li>
 *   <li>Military bases consume cargo from stations to produce units</li>
 * </ul>
 * The planetary economic system manages military unit production: cargo flows from industries
 * through stations to military bases, where it is consumed to produce combat units.
 * </p>
 */
@Service
public class MilitaryProductionChainService {

    private static final Logger log = LoggerFactory.getLogger(MilitaryProductionChainService.class);

    /** Cargo required per unit type for production — aligned with MilitaryProductionService.PRODUCTION_COST */
    private static final Map<ThirdReichUnitType, Map<CargoType, Integer>> UNIT_COSTS = Map.of(
            ThirdReichUnitType.INFANTRY, Map.of(
                    CargoType.STEEL, 2
            ),
            ThirdReichUnitType.MECHANIZED, Map.of(
                    CargoType.STEEL, 5,
                    CargoType.REFINED_OIL, 2
            ),
            ThirdReichUnitType.TANK, Map.of(
                    CargoType.STEEL, 10,
                    CargoType.MACHINERY, 3,
                    CargoType.REFINED_OIL, 2
            ),
            ThirdReichUnitType.ARTILLERY, Map.of(
                    CargoType.STEEL, 8,
                    CargoType.REFINED_OIL, 2
            ),
            ThirdReichUnitType.ANTI_AIR, Map.of(
                    CargoType.STEEL, 5,
                    CargoType.ELECTRONICS, 2
            ),
            ThirdReichUnitType.RECON, Map.of(
                    CargoType.STEEL, 2,
                    CargoType.REFINED_OIL, 1
            ),
            ThirdReichUnitType.ENGINEER, Map.of(
                    CargoType.STEEL, 2
            ),
            ThirdReichUnitType.AIR_SUPPORT, Map.of(
                    CargoType.REFINED_OIL, 5,
                    CargoType.ELECTRONICS, 2
            ),
            ThirdReichUnitType.MECH, Map.of(
                    CargoType.STEEL, 20,
                    CargoType.ELECTRONICS, 10,
                    CargoType.MACHINERY, 5
            )
    );

    /** All cargo types relevant to military production */
    private static final Set<CargoType> MILITARY_CARGO_TYPES = Set.of(
            CargoType.STEEL, CargoType.REFINED_OIL, CargoType.ELECTRONICS,
            CargoType.MACHINERY, CargoType.COAL, CargoType.OIL
    );

    private final CargoGenerationService cargoGenerationService;
    private final MilitaryProductionService militaryProductionService;
    private final HullModuleService hullModuleService;

    public MilitaryProductionChainService(
            CargoGenerationService cargoGenerationService,
            MilitaryProductionService militaryProductionService,
            HullModuleService hullModuleService) {
        this.cargoGenerationService = cargoGenerationService;
        this.militaryProductionService = militaryProductionService;
        this.hullModuleService = hullModuleService;
    }

    /**
     * Get the production chain for a planet.
     * <p>
     * Shows the flow from raw material extraction to military unit production.
     * Reads actual cargo from stations where industries deposit their output.
     * </p>
     *
     * @param planetId the planet ID
     * @param industries list of industries on the planet
     * @param stations list of stations on the planet
     * @return the production chain summary
     */
    public ProductionChainSummary getProductionChain(Long planetId, List<Industry> industries, List<Station> stations) {
        ProductionChainSummary summary = new ProductionChainSummary(planetId);

        // Step 1: Categorize industries
        List<IndustryNode> rawMaterialIndustries = new ArrayList<>();
        List<IndustryNode> processingIndustries = new ArrayList<>();

        for (Industry industry : industries) {
            if (!industry.isActive()) continue;
            var type = industry.getType();
            var outputs = type.getOutput();
            var inputs = type.getInput();

            // Raw material industries have no input requirements
            Hex3D loc = industry.getLocation();
            int level = (int) (industry.getProductionLevel() * 100);
            String name = type.getDisplayName() + " " + industry.getId();
            if (inputs.isEmpty() && !outputs.isEmpty()) {
                rawMaterialIndustries.add(new IndustryNode(
                        industry.getId(), name, type.name(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        new ArrayList<>(outputs.keySet()), List.of(),
                        level
                ));
            } else if (!inputs.isEmpty()) {
                processingIndustries.add(new IndustryNode(
                        industry.getId(), name, type.name(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        new ArrayList<>(outputs.keySet()), new ArrayList<>(inputs.keySet()),
                        level
                ));
            }
        }

        // Step 2: Aggregate cargo available at all stations
        Map<CargoType, Integer> totalCargoAvailable = new HashMap<>();
        for (CargoType ct : MILITARY_CARGO_TYPES) {
            totalCargoAvailable.put(ct, 0);
        }
        for (Station station : stations) {
            for (Map.Entry<CargoType, Integer> entry : station.getCargoWaiting().entrySet()) {
                CargoType ct = entry.getKey();
                if (MILITARY_CARGO_TYPES.contains(ct)) {
                    totalCargoAvailable.merge(ct, entry.getValue(), Integer::sum);
                }
            }
        }

        // Step 3: Military bases
        List<MilitaryBaseNode> militaryBases = new ArrayList<>();
        var modules = hullModuleService.getModulesByPlanet(planetId);
        for (var hm : modules) {
            HullModuleType type = HullModuleType.valueOf(hm.getModuleType());
            if (type.getCategory() == HullModuleType.Category.MILITARY) {
                militaryBases.add(new MilitaryBaseNode(
                        hm.getId().toString(),
                        hm.getName(),
                        hm.getModuleType(),
                        hm.getHexX(), hm.getHexY(), hm.getHexZ(),
                        militaryProductionService.getProductionQueue(hm.getId().toString())
                ));
            }
        }

        // Step 4: Calculate cargo flow with real station data
        List<CargoFlow> cargoFlows = new ArrayList<>();
        for (CargoType ct : MILITARY_CARGO_TYPES) {
            int available = totalCargoAvailable.getOrDefault(ct, 0);
            int consumed = calculateCargoConsumption(ct, militaryBases);
            int surplus = available - consumed;
            cargoFlows.add(new CargoFlow(ct.name(), available, consumed, surplus));
        }

        summary.setRawMaterialIndustries(rawMaterialIndustries);
        summary.setProcessingIndustries(processingIndustries);
        summary.setMilitaryBases(militaryBases);
        summary.setCargoFlows(cargoFlows);
        summary.setTotalMilitaryUnits(militaryProductionService.getProducedUnitsCount());
        summary.setActiveProductionQueues(militaryProductionService.getActiveQueuesCount());

        return summary;
    }

    /**
     * Calculate cargo consumption by military bases.
     */
    private int calculateCargoConsumption(CargoType cargoType, List<MilitaryBaseNode> bases) {
        int total = 0;
        for (MilitaryBaseNode base : bases) {
            for (ProductionQueueEntry queue : base.getProductionQueue()) {
                Map<CargoType, Integer> costs = UNIT_COSTS.get(queue.unitType);
                if (costs != null) {
                    total += costs.getOrDefault(cargoType, 0);
                }
            }
        }
        return total;
    }

    /**
     * Process one turn of the production chain.
     * <p>
     * This method:
     * 1. Generates cargo from industries via CargoGenerationService
     * 2. Reads available cargo from all stations on the planet
     * 3. For each military base with queued production orders, checks if sufficient cargo is available
     * 4. Consumes cargo from stations and advances production queues
     * 5. Produces completed units via MilitaryProductionService
     * </p>
     *
     * @param planetId the planet ID
     * @param industries list of industries on the planet
     * @param settlements list of settlements on the planet
     * @param stations list of stations on the planet
     * @return the production summary
     */
    public ProductionTurnSummary processProductionTurn(
            Long planetId,
            List<Industry> industries,
            List<Settlement> settlements,
            List<Station> stations) {
        ProductionTurnSummary summary = new ProductionTurnSummary(planetId);

        // Step 1: Generate cargo from industries — this deposits cargo at stations
        Map<String, Station> stationMap = new HashMap<>();
        for (Station station : stations) {
            stationMap.put(station.getId(), station);
        }

        int cargoGenerated = cargoGenerationService.generateIndustryCargo(industries, stationMap);
        int passengersGenerated = cargoGenerationService.generateSettlementDemand(settlements, stationMap);
        int cargoDecayed = cargoGenerationService.processCargoDecay(stations);

        // Generate cargo from HullModules (unified building system)
        List<com.yisy.modules.doramcu.hex.planet.hullmodule.HullModule> hullModules =
                hullModuleService.getModuleEntitiesByPlanet(planetId);
        int hullModuleCargo = 0;
        if (!hullModules.isEmpty()) {
            hullModuleCargo = cargoGenerationService.generateHullModuleCargo(hullModules, stationMap);
            cargoGenerated += hullModuleCargo;
            if (hullModuleCargo > 0) {
                log.info("HullModules produced {} cargo units for military production", hullModuleCargo);
            }
        }

        // Step 2: Aggregate all military-relevant cargo from all stations
        Map<CargoType, Integer> availableCargo = new HashMap<>();
        for (CargoType ct : MILITARY_CARGO_TYPES) {
            availableCargo.put(ct, 0);
        }
        for (Station station : stations) {
            for (Map.Entry<CargoType, Integer> entry : station.getCargoWaiting().entrySet()) {
                CargoType ct = entry.getKey();
                if (MILITARY_CARGO_TYPES.contains(ct)) {
                    availableCargo.merge(ct, entry.getValue(), Integer::sum);
                }
            }
        }

        log.info("Planet {} cargo available for military: {}", planetId, availableCargo);

        // Step 3: Process military production queues, consuming cargo from stations
        int unitsProduced = 0;
        List<String> productionEvents = new ArrayList<>();

        // Get military bases and their production queues
        var modules = hullModuleService.getModulesByPlanet(planetId);
        for (var hm : modules) {
            HullModuleType type = HullModuleType.valueOf(hm.getModuleType());
            if (type.getCategory() != HullModuleType.Category.MILITARY) continue;

            String moduleId = hm.getId().toString();
            var queue = militaryProductionService.getProductionQueue(moduleId);

            // Process each queued order: check cargo, consume if available, advance production
            for (var order : queue) {
                ThirdReichUnitType unitType = order.getUnitType();
                Map<CargoType, Integer> costs = UNIT_COSTS.get(unitType);
                if (costs == null || costs.isEmpty()) continue;

                // Check if we have enough cargo for this unit
                boolean canProduce = true;
                for (Map.Entry<CargoType, Integer> cost : costs.entrySet()) {
                    if (availableCargo.getOrDefault(cost.getKey(), 0) < cost.getValue()) {
                        canProduce = false;
                        break;
                    }
                }

                if (canProduce) {
                    // Consume cargo from the planet's stations
                    consumeCargoFromStations(stations, costs);

                    // Update available cargo tracking
                    for (Map.Entry<CargoType, Integer> cost : costs.entrySet()) {
                        availableCargo.merge(cost.getKey(), -cost.getValue(), Integer::sum);
                    }

                    productionEvents.add("Consumed cargo for " + unitType.getDisplayName() + " at " + hm.getName());
                    log.debug("Consumed cargo for {} at {}: {}", unitType.getDisplayName(), hm.getName(), costs);
                } else {
                    // Not enough cargo — production stalls
                    String missing = describeMissingCargo(costs, availableCargo);
                    productionEvents.add("INSUFFICIENT CARGO for " + unitType.getDisplayName() + " at " + hm.getName() + ": " + missing);
                    log.debug("Insufficient cargo for {} at {}: {}", unitType.getDisplayName(), hm.getName(), missing);
                }
            }
        }

        // Step 4: Advance production queues in MilitaryProductionService
        List<?> producedUnits = militaryProductionService.processProductionTurn(planetId);
        unitsProduced = producedUnits.size();
        for (Object unit : producedUnits) {
            productionEvents.add("UNIT PRODUCED: " + unit.toString());
        }

        // Update final cargo state from stations after consumption
        Map<CargoType, Integer> finalCargo = new HashMap<>();
        for (CargoType ct : MILITARY_CARGO_TYPES) {
            finalCargo.put(ct, 0);
        }
        for (Station station : stations) {
            for (Map.Entry<CargoType, Integer> entry : station.getCargoWaiting().entrySet()) {
                CargoType ct = entry.getKey();
                if (MILITARY_CARGO_TYPES.contains(ct)) {
                    finalCargo.merge(ct, entry.getValue(), Integer::sum);
                }
            }
        }

        summary.setCargoGenerated(cargoGenerated);
        summary.setUnitsProduced(unitsProduced);
        summary.setProductionEvents(productionEvents);
        summary.setAvailableCargo(finalCargo);

        log.info("Production turn {}: {} cargo generated, {} decayed, {} units produced, events: {}",
                planetId, cargoGenerated, cargoDecayed, unitsProduced, productionEvents.size());

        return summary;
    }

    /**
     * Consume cargo from stations for military production.
     * Distributes consumption across stations proportionally.
     */
    private void consumeCargoFromStations(List<Station> stations, Map<CargoType, Integer> costs) {
        for (Map.Entry<CargoType, Integer> cost : costs.entrySet()) {
            CargoType ct = cost.getKey();
            int remaining = cost.getValue();

            for (Station station : stations) {
                if (remaining <= 0) break;
                int available = station.getCargoWaiting().getOrDefault(ct, 0);
                int take = Math.min(remaining, available);
                if (take > 0) {
                    station.removeCargo(ct, take);
                    remaining -= take;
                }
            }

            if (remaining > 0) {
                log.warn("Could not fully consume {} {}: only found {} of {}", ct.name(), cost.getValue(), cost.getValue() - remaining, ct.name());
            }
        }
    }

    /**
     * Describe which cargo types are missing for production.
     */
    private String describeMissingCargo(Map<CargoType, Integer> costs, Map<CargoType, Integer> available) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<CargoType, Integer> cost : costs.entrySet()) {
            int have = available.getOrDefault(cost.getKey(), 0);
            int need = cost.getValue();
            if (have < need) {
                missing.add(cost.getKey().name() + ":" + have + "/" + need);
            }
        }
        return String.join(", ", missing);
    }

    /**
     * Get the cargo cost for a unit type.
     */
    public Map<CargoType, Integer> getUnitCosts(ThirdReichUnitType unitType) {
        return UNIT_COSTS.getOrDefault(unitType, Map.of());
    }

    /**
     * Production chain summary for a planet.
     */
    public static class ProductionChainSummary {
        private final Long planetId;
        private List<IndustryNode> rawMaterialIndustries;
        private List<IndustryNode> processingIndustries;
        private List<MilitaryBaseNode> militaryBases;
        private List<CargoFlow> cargoFlows;
        private int totalMilitaryUnits;
        private int activeProductionQueues;

        public ProductionChainSummary(Long planetId) {
            this.planetId = planetId;
        }

        public Long getPlanetId() { return planetId; }
        public List<IndustryNode> getRawMaterialIndustries() { return rawMaterialIndustries; }
        public void setRawMaterialIndustries(List<IndustryNode> rawMaterialIndustries) { this.rawMaterialIndustries = rawMaterialIndustries; }
        public List<IndustryNode> getProcessingIndustries() { return processingIndustries; }
        public void setProcessingIndustries(List<IndustryNode> processingIndustries) { this.processingIndustries = processingIndustries; }
        public List<MilitaryBaseNode> getMilitaryBases() { return militaryBases; }
        public void setMilitaryBases(List<MilitaryBaseNode> militaryBases) { this.militaryBases = militaryBases; }
        public List<CargoFlow> getCargoFlows() { return cargoFlows; }
        public void setCargoFlows(List<CargoFlow> cargoFlows) { this.cargoFlows = cargoFlows; }
        public int getTotalMilitaryUnits() { return totalMilitaryUnits; }
        public void setTotalMilitaryUnits(int totalMilitaryUnits) { this.totalMilitaryUnits = totalMilitaryUnits; }
        public int getActiveProductionQueues() { return activeProductionQueues; }
        public void setActiveProductionQueues(int activeProductionQueues) { this.activeProductionQueues = activeProductionQueues; }
    }

    /**
     * An industry node in the production chain.
     */
    public static class IndustryNode {
        private final String id;
        private final String name;
        private final String type;
        private final int x, y, z;
        private final List<CargoType> outputs;
        private final List<CargoType> inputs;
        private final int productionLevel;

        public IndustryNode(String id, String name, String type, int x, int y, int z,
                            List<CargoType> outputs, List<CargoType> inputs, int productionLevel) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.outputs = outputs;
            this.inputs = inputs;
            this.productionLevel = productionLevel;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public List<CargoType> getOutputs() { return outputs; }
        public List<CargoType> getInputs() { return inputs; }
        public int getProductionLevel() { return productionLevel; }
    }

    /**
     * A military base node in the production chain.
     */
    public static class MilitaryBaseNode {
        private final String id;
        private final String name;
        private final String moduleType;
        private final int x, y, z;
        private final List<ProductionQueueEntry> productionQueue;

        public MilitaryBaseNode(String id, String name, String moduleType,
                                int x, int y, int z, List<ProductionQueueEntry> productionQueue) {
            this.id = id;
            this.name = name;
            this.moduleType = moduleType;
            this.x = x;
            this.y = y;
            this.z = z;
            this.productionQueue = productionQueue;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getModuleType() { return moduleType; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public List<ProductionQueueEntry> getProductionQueue() { return productionQueue; }
    }

    /**
     * Cargo flow between stages.
     */
    public static class CargoFlow {
        private final String cargoType;
        private final int produced;
        private final int consumed;
        private final int surplus;

        public CargoFlow(String cargoType, int produced, int consumed, int surplus) {
            this.cargoType = cargoType;
            this.produced = produced;
            this.consumed = consumed;
            this.surplus = surplus;
        }

        public String getCargoType() { return cargoType; }
        public int getProduced() { return produced; }
        public int getConsumed() { return consumed; }
        public int getSurplus() { return surplus; }
    }

    /**
     * Production queue entry.
     */
    public static class ProductionQueueEntry {
        private final ThirdReichUnitType unitType;
        private final int turnsRemaining;
        private final double progress;

        public ProductionQueueEntry(ThirdReichUnitType unitType, int turnsRemaining, double progress) {
            this.unitType = unitType;
            this.turnsRemaining = turnsRemaining;
            this.progress = progress;
        }

        public ThirdReichUnitType getUnitType() { return unitType; }
        public int getTurnsRemaining() { return turnsRemaining; }
        public double getProgress() { return progress; }
    }

    /**
     * Summary of a production turn.
     */
    public static class ProductionTurnSummary {
        private final Long planetId;
        private int cargoGenerated;
        private int unitsProduced;
        private List<String> productionEvents;
        private Map<CargoType, Integer> availableCargo;

        public ProductionTurnSummary(Long planetId) {
            this.planetId = planetId;
            this.productionEvents = new ArrayList<>();
            this.availableCargo = new HashMap<>();
        }

        public Long getPlanetId() { return planetId; }
        public int getCargoGenerated() { return cargoGenerated; }
        public void setCargoGenerated(int cargoGenerated) { this.cargoGenerated = cargoGenerated; }
        public int getUnitsProduced() { return unitsProduced; }
        public void setUnitsProduced(int unitsProduced) { this.unitsProduced = unitsProduced; }
        public List<String> getProductionEvents() { return productionEvents; }
        public void setProductionEvents(List<String> productionEvents) { this.productionEvents = productionEvents; }
        public Map<CargoType, Integer> getAvailableCargo() { return availableCargo; }
        public void setAvailableCargo(Map<CargoType, Integer> availableCargo) { this.availableCargo = availableCargo; }
    }
}
