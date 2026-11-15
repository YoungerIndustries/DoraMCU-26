package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages military unit production from HullModule military structures.
 * <p>
 * Production chain:
 * <ul>
 *   <li>Army Base → produces Infantry, Mechanized units</li>
 *   <li>Tank Factory → produces Tank units</li>
 *   <li>Artillery Fort → produces Artillery units</li>
 *   <li>Naval Base → produces naval ships</li>
 *   <li>Space Navy Base → produces space ships</li>
 *   <li>Air Base → produces Air Support units</li>
 * </ul>
 * </p>
 */
@Service
public class MilitaryProductionService {

    private static final Logger log = LoggerFactory.getLogger(MilitaryProductionService.class);

    /** Production time in turns for each unit type */
    private static final Map<ThirdReichUnitType, Integer> PRODUCTION_TIME = Map.of(
            ThirdReichUnitType.INFANTRY, 1,
            ThirdReichUnitType.MECHANIZED, 2,
            ThirdReichUnitType.TANK, 3,
            ThirdReichUnitType.ARTILLERY, 2,
            ThirdReichUnitType.ANTI_AIR, 2,
            ThirdReichUnitType.RECON, 1,
            ThirdReichUnitType.ENGINEER, 1,
            ThirdReichUnitType.AIR_SUPPORT, 3,
            ThirdReichUnitType.MECH, 5
    );

    /** Resource cost for producing each unit type */
    private static final Map<ThirdReichUnitType, Map<CargoType, Integer>> PRODUCTION_COST = Map.of(
            ThirdReichUnitType.INFANTRY, Map.of(CargoType.STEEL, 2),
            ThirdReichUnitType.MECHANIZED, Map.of(CargoType.STEEL, 5, CargoType.FUEL, 2),
            ThirdReichUnitType.TANK, Map.of(CargoType.STEEL, 10, CargoType.MACHINERY, 3, CargoType.FUEL, 2),
            ThirdReichUnitType.ARTILLERY, Map.of(CargoType.STEEL, 8, CargoType.FUEL, 2),
            ThirdReichUnitType.ANTI_AIR, Map.of(CargoType.STEEL, 5, CargoType.ELECTRONICS, 2),
            ThirdReichUnitType.RECON, Map.of(CargoType.STEEL, 2, CargoType.FUEL, 1),
            ThirdReichUnitType.ENGINEER, Map.of(CargoType.STEEL, 2),
            ThirdReichUnitType.AIR_SUPPORT, Map.of(CargoType.FUEL, 5, CargoType.ELECTRONICS, 2),
            ThirdReichUnitType.MECH, Map.of(CargoType.STEEL, 20, CargoType.ELECTRONICS, 10, CargoType.MACHINERY, 5)
    );

    /** Which HullModuleType can produce which unit types */
    private static final Map<HullModuleType, List<ThirdReichUnitType>> PRODUCTION_CAPABILITY = Map.of(
            HullModuleType.ARMY_BASE, List.of(
                    ThirdReichUnitType.INFANTRY, ThirdReichUnitType.MECHANIZED,
                    ThirdReichUnitType.RECON, ThirdReichUnitType.ENGINEER),
            HullModuleType.TANK_FACTORY, List.of(ThirdReichUnitType.TANK),
            HullModuleType.ARTILLERY_FORT, List.of(ThirdReichUnitType.ARTILLERY, ThirdReichUnitType.ANTI_AIR),
            HullModuleType.AIR_BASE, List.of(ThirdReichUnitType.AIR_SUPPORT),
            HullModuleType.MISSILE_SILO, List.of(),
            HullModuleType.NAVY_BASE, List.of(),
            HullModuleType.SPACE_NAVY_BASE, List.of(),
            HullModuleType.BARRACKS, List.of(ThirdReichUnitType.INFANTRY),
            HullModuleType.MILITARY_ACADEMY, List.of()
    );

    private final HullModuleService hullModuleService;
    private final Map<String, List<ThirdReichGroundUnit>> militaryUnits;
    private final Map<String, ProductionQueue> productionQueues;
    private final AtomicInteger unitIdCounter;

    public MilitaryProductionService(HullModuleService hullModuleService) {
        this.hullModuleService = hullModuleService;
        this.militaryUnits = new ConcurrentHashMap<>();
        this.productionQueues = new ConcurrentHashMap<>();
        this.unitIdCounter = new AtomicInteger(1);
    }

    /**
     * Queue production of a military unit at a military structure.
     *
     * @param moduleId the HullModule ID (military structure)
     * @param unitType the type of unit to produce
     * @param factionId the owning faction
     * @return true if production was queued successfully
     */
    public boolean queueProduction(String moduleId, ThirdReichUnitType unitType, String factionId) {
        // Check if the module can produce this unit type
        HullModuleType moduleType = hullModuleService.getModule(moduleId).map(HullModuleService.HullModuleSummary::getModuleType).orElse(null);
        if (moduleType == null) {
            log.warn("Module not found: {}", moduleId);
            return false;
        }

        List<ThirdReichUnitType> capabilities = PRODUCTION_CAPABILITY.getOrDefault(moduleType, List.of());
        if (!capabilities.contains(unitType)) {
            log.warn("Module {} cannot produce {}", moduleType.getDisplayName(), unitType.getDisplayName());
            return false;
        }

        // Check resource costs
        Map<CargoType, Integer> cost = PRODUCTION_COST.get(unitType);
        if (cost == null || cost.isEmpty()) {
            log.warn("No production cost defined for {}", unitType.getDisplayName());
            return false;
        }

        // Add to production queue
        ProductionQueue queue = productionQueues.computeIfAbsent(moduleId, k -> new ProductionQueue());
        queue.add(new ProductionOrder(unitType, factionId, PRODUCTION_TIME.getOrDefault(unitType, 1)));
        log.info("Queued production of {} at module {} for faction {}", unitType.getDisplayName(), moduleId, factionId);
        return true;
    }

    /**
     * Process one turn of production for all military structures.
     *
     * @param planetId the planet ID
     * @return list of newly produced units
     */
    public List<ThirdReichGroundUnit> processProductionTurn(Long planetId) {
        List<ThirdReichGroundUnit> produced = new ArrayList<>();

        for (Map.Entry<String, ProductionQueue> entry : productionQueues.entrySet()) {
            String moduleId = entry.getKey();
            ProductionQueue queue = entry.getValue();

            while (!queue.isEmpty()) {
                ProductionOrder order = queue.peek();
                order.processTurn();

                if (order.isComplete()) {
                    queue.poll();
                    ThirdReichGroundUnit unit = createUnit(order, moduleId);
                    if (unit != null) {
                        produced.add(unit);
                        String factionKey = order.factionId;
                        militaryUnits.computeIfAbsent(factionKey, k -> new ArrayList<>()).add(unit);
                    }
                }
            }
        }

        if (!produced.isEmpty()) {
            log.info("Produced {} military units this turn", produced.size());
        }
        return produced;
    }

    /**
     * Get all military units for a faction.
     */
    public List<ThirdReichGroundUnit> getUnitsByFaction(String factionId) {
        return new ArrayList<>(militaryUnits.getOrDefault(factionId, List.of()));
    }

    /**
     * Get all military units across all factions.
     */
    public List<ThirdReichGroundUnit> getAllUnits() {
        List<ThirdReichGroundUnit> all = new ArrayList<>();
        militaryUnits.values().forEach(all::addAll);
        return all;
    }

    /**
     * Remove destroyed units from a faction.
     */
    public void removeUnit(String factionId, String unitId) {
        List<ThirdReichGroundUnit> units = militaryUnits.get(factionId);
        if (units != null) {
            units.removeIf(u -> u.getId().equals(unitId) && !u.isCombatEffective());
        }
    }

    /**
     * Get unit types that can be produced at a given module type.
     */
    public List<ThirdReichUnitType> getProducibleUnitTypes(HullModuleType moduleType) {
        return new ArrayList<>(PRODUCTION_CAPABILITY.getOrDefault(moduleType, List.of()));
    }

    /**
     * Get the production cost for a unit type.
     */
    public Map<CargoType, Integer> getProductionCost(ThirdReichUnitType unitType) {
        return PRODUCTION_COST.getOrDefault(unitType, Map.of());
    }

    /**
     * Get the production time for a unit type.
     */
    public int getProductionTime(ThirdReichUnitType unitType) {
        return PRODUCTION_TIME.getOrDefault(unitType, 1);
    }

    private ThirdReichGroundUnit createUnit(ProductionOrder order, String moduleId) {
        String unitId = "UNIT-" + unitIdCounter.getAndIncrement();
        String name = order.unitType.getDisplayName() + " " + unitId;
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(unitId, name, order.unitType);
        unit.setSourceGame("DoraMCU");
        return unit;
    }

    /**
     * Production queue for a military structure.
     */
    static class ProductionQueue {
        private final Queue<ProductionOrder> orders = new LinkedList<>();

        void add(ProductionOrder order) { orders.add(order); }
        ProductionOrder peek() { return orders.peek(); }
        ProductionOrder poll() { return orders.poll(); }
        boolean isEmpty() { return orders.isEmpty(); }
        int size() { return orders.size(); }
    }

    /**
     * A single production order.
     */
    static class ProductionOrder {
        final ThirdReichUnitType unitType;
        final String factionId;
        final int totalTurns;
        int turnsRemaining;

        ProductionOrder(ThirdReichUnitType unitType, String factionId, int totalTurns) {
            this.unitType = unitType;
            this.factionId = factionId;
            this.totalTurns = totalTurns;
            this.turnsRemaining = totalTurns;
        }

        void processTurn() {
            turnsRemaining--;
        }

        boolean isComplete() {
            return turnsRemaining <= 0;
        }

        double getProgress() {
            return 1.0 - (double) turnsRemaining / totalTurns;
        }
    }
}
