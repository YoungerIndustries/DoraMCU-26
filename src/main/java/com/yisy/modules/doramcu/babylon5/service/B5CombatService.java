package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5ShipType;
import com.yisy.modules.doramcu.fourx.FourxCombatService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B5 implementation of the Combat 4X pillar.
 * <p>
 * Handles ship-to-ship combat with B5-specific ship types (Nightstar, Crusader,
 * Battle Cruiser, etc.), fleet management, and tactical combat rounds.
 * </p>
 */
@Service
public class B5CombatService implements FourxCombatService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(B5CombatService.class);

    private final Map<String, B5ShipType> shipTypes = new HashMap<>();
    private final Map<String, Map<String, Object>> fleets = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> combats = new ConcurrentHashMap<>();
    private final AtomicLong fleetIdCounter = new AtomicLong(1);
    private final AtomicLong combatIdCounter = new AtomicLong(1);
    private final Random random = new Random();

    public B5CombatService() {
    }

    @PostConstruct
    public void initShipTypes() {
        // Earth Alliance ships
        shipTypes.put("EA_CORVETTE", createShip("EA_CORVETTE", "Corvette", "Earth Alliance", "Frigate",
                100, 20, 30, 15, 8, 50,
                List.of("Lasers", "Missiles"), List.of("Point Defense")));

        shipTypes.put("EA_DESTROYER", createShip("EA_DESTROYER", "Destroyer", "Earth Alliance", "Destroyer",
                250, 40, 60, 30, 6, 150,
                List.of("Lasers", "Missiles", "Torpedoes"), List.of("Point Defense", "Energy Shield")));

        shipTypes.put("EA_CRUISER", createShip("EA_CRUISER", "Cruiser", "Earth Alliance", "Cruiser",
                500, 60, 100, 50, 5, 300,
                List.of("Lasers", "Missiles", "Torpedoes", "Particle Beam"),
                List.of("Point Defense", "Energy Shield", "Deflector")));

        // Narn ships
        shipTypes.put("NARN_CRUSADER", createShip("NARN_CRUSADER", "Crusader", "Narn", "Frigate",
                120, 25, 40, 20, 9, 60,
                List.of("Lasers", "Missiles"), List.of("Point Defense", "Reinforced Hull")));

        shipTypes.put("NARN_BATTLECRUISER", createShip("NARN_BATTLECRUISER", "Battle Cruiser", "Narn", "Cruiser",
                400, 70, 90, 45, 7, 200,
                List.of("Lasers", "Missiles", "Torpedoes"),
                List.of("Point Defense", "Reinforced Hull", "Energy Shield")));

        // Centauri ships
        shipTypes.put("CENTAURI_NIGHTSTAR", createShip("CENTAURI_NIGHTSTAR", "Nightstar", "Centauri", "Frigate",
                150, 30, 50, 25, 10, 80,
                List.of("Lasers", "Missiles", "Particle Beam"),
                List.of("Point Defense", "Energy Shield")));

        shipTypes.put("CENTAURI_BATTLECRUISER", createShip("CENTAURI_BATTLECRUISER", "Battle Cruiser", "Centauri", "Cruiser",
                600, 80, 120, 60, 6, 400,
                List.of("Lasers", "Missiles", "Torpedoes", "Particle Beam", "Graviton"),
                List.of("Point Defense", "Energy Shield", "Deflector")));

        // Minbari ships
        shipTypes.put("MINBARI_CRUISER", createShip("MINBARI_CRUISER", "Cruiser", "Minbari", "Cruiser",
                450, 65, 95, 55, 7, 250,
                List.of("Lasers", "Missiles", "Particle Beam"),
                List.of("Point Defense", "Energy Shield", "Deflector")));

        shipTypes.put("MINBARI_BATTLESHIP", createShip("MINBARI_BATTLESHIP", "Battlestar", "Minbari", "Battle Cruiser",
                800, 100, 150, 80, 5, 600,
                List.of("Lasers", "Missiles", "Torpedoes", "Particle Beam", "Graviton"),
                List.of("Point Defense", "Energy Shield", "Deflector", "Reinforced Hull")));

        // Independent / other
        shipTypes.put("INDEPENDENT_FREIGHTER", createShip("INDEPENDENT_FREIGHTER", "Freighter", "Independent", "Freighter",
                80, 15, 10, 10, 4, 30,
                List.of("Lasers"), List.of("Point Defense")));

        log.info("Initialized {} B5 ship types", shipTypes.size());
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.BABYLON5;
    }

    @Override
    public Map<String, Object> initiateCombat(String attackerFleetId, String defenderFleetId) {
        Map<String, Object> attackerFleet = fleets.get(attackerFleetId);
        Map<String, Object> defenderFleet = fleets.get(defenderFleetId);
        if (attackerFleet == null || defenderFleet == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Fleet not found");
            return error;
        }

        String combatId = String.valueOf(combatIdCounter.getAndIncrement());
        Map<String, Object> combat = new LinkedHashMap<>();
        combat.put("id", combatId);
        combat.put("attackerFleetId", attackerFleetId);
        combat.put("defenderFleetId", defenderFleetId);
        combat.put("round", 0);
        combat.put("active", true);
        combat.put("attackerDamage", 0);
        combat.put("defenderDamage", 0);
        combat.put("attackerShipsRemaining", getShipCount(attackerFleet));
        combat.put("defenderShipsRemaining", getShipCount(defenderFleet));
        combat.put("winner", null);
        combat.put("log", new ArrayList<String>());

        combats.put(combatId, combat);
        log.info("Combat initiated: {} vs {} (ID: {})", 
                attackerFleet.get("name"), defenderFleet.get("name"), combatId);
        return combat;
    }

    @Override
    public Map<String, Object> executeCombatRound(String combatId) {
        Map<String, Object> combat = combats.get(combatId);
        if (combat == null || !(Boolean) combat.get("active")) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Combat not found or already ended");
            return error;
        }

        String attackerFleetId = (String) combat.get("attackerFleetId");
        String defenderFleetId = (String) combat.get("defenderFleetId");
        Map<String, Object> attackerFleet = fleets.get(attackerFleetId);
        Map<String, Object> defenderFleet = fleets.get(defenderFleetId);

        int round = (int) combat.get("round") + 1;
        combat.put("round", round);

        // Calculate damage
        int attackerPower = calculateCombatPower(attackerFleetId);
        int defenderPower = calculateCombatPower(defenderFleetId);

        // Attacker strikes first
        int attackerDamage = Math.max(1, (attackerPower / 10) + random.nextInt(attackerPower / 5));
        int defenderDamage = Math.max(1, (defenderPower / 10) + random.nextInt(defenderPower / 5));

        // Apply randomness
        attackerDamage = (int) (attackerDamage * (0.8 + random.nextDouble() * 0.4));
        defenderDamage = (int) (defenderDamage * (0.8 + random.nextDouble() * 0.4));

        int totalAttackerDamage = (int) combat.get("attackerDamage") + attackerDamage;
        int totalDefenderDamage = (int) combat.get("defenderDamage") + defenderDamage;
        combat.put("attackerDamage", totalAttackerDamage);
        combat.put("defenderDamage", totalDefenderDamage);

        // Update ships remaining
        int attackerShips = (int) combat.get("attackerShipsRemaining");
        int defenderShips = (int) combat.get("defenderShipsRemaining");
        int attackerLosses = Math.min(attackerShips, defenderDamage / 50);
        int defenderLosses = Math.min(defenderShips, attackerDamage / 50);
        attackerShips -= defenderLosses;
        defenderShips -= attackerLosses;
        combat.put("attackerShipsRemaining", Math.max(0, attackerShips));
        combat.put("defenderShipsRemaining", Math.max(0, defenderShips));

        // Add to log
        @SuppressWarnings("unchecked")
        List<String> logList = (List<String>) combat.get("log");
        logList.add(String.format("Round %d: Attacker deals %d damage, Defender deals %d damage",
                round, attackerDamage, defenderDamage));

        // Check for combat end
        if (attackerShips <= 0 || defenderShips <= 0) {
            combat.put("active", false);
            String winner = attackerShips > 0 ? "attacker" : "defender";
            combat.put("winner", winner);
            logList.add(String.format("Combat ended: %s wins!", winner));
            log.info("Combat {} ended: {} wins", combatId, winner);
        }

        return combat;
    }

    @Override
    public Map<String, Object> getCombatStatus(String combatId) {
        return combats.get(combatId);
    }

    @Override
    public List<Map<String, Object>> getShipTypes(String factionId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (B5ShipType ship : shipTypes.values()) {
            if (ship.getFaction().equalsIgnoreCase(factionId) || "Independent".equals(ship.getFaction())) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", ship.getId());
                entry.put("name", ship.getName());
                entry.put("class", ship.getClassType());
                entry.put("hull", ship.getHull());
                entry.put("armor", ship.getArmor());
                entry.put("weapons", ship.getWeapons());
                entry.put("defenses", ship.getDefenses());
                entry.put("speed", ship.getSpeed());
                entry.put("crew", ship.getCrew());
                entry.put("combatPower", ship.getCombatPower());
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> createFleet(String factionId, String fleetName, String shipTypeId, int count) {
        B5ShipType shipType = shipTypes.get(shipTypeId);
        if (shipType == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Ship type not found: " + shipTypeId);
            return error;
        }

        String fleetId = String.valueOf(fleetIdCounter.getAndIncrement());
        Map<String, Object> fleet = new LinkedHashMap<>();
        fleet.put("id", fleetId);
        fleet.put("name", fleetName);
        fleet.put("factionId", factionId);
        fleet.put("shipTypeId", shipTypeId);
        fleet.put("shipTypeName", shipType.getName());
        fleet.put("shipCount", count);
        fleet.put("hull", shipType.getHull() * count);
        fleet.put("armor", shipType.getArmor() * count);
        fleet.put("weapons", shipType.getWeapons() * count);
        fleet.put("defenses", shipType.getDefenses() * count);
        fleet.put("speed", shipType.getSpeed());
        fleet.put("combatPower", shipType.getCombatPower() * count);

        fleets.put(fleetId, fleet);
        log.info("Created fleet {} ({}) for faction {} with {} {}", 
                fleetName, fleetId, factionId, count, shipType.getName());
        return fleet;
    }

    @Override
    public List<Map<String, Object>> getFleets(String factionId) {
        return fleets.values().stream()
                .filter(f -> f.get("factionId").equals(factionId))
                .toList();
    }

    @Override
    public int calculateCombatPower(String fleetId) {
        Map<String, Object> fleet = fleets.get(fleetId);
        if (fleet == null) return 0;
        return (int) fleet.getOrDefault("combatPower", 0);
    }

    @Override
    public List<String> getActiveCombats() {
        return combats.entrySet().stream()
                .filter(e -> (Boolean) e.getValue().get("active"))
                .map(Map.Entry::getKey)
                .toList();
    }

    private B5ShipType createShip(String id, String name, String faction, String classType,
                                   int hull, int armor, int weapons, int defenses,
                                   int speed, int crew,
                                   List<String> weaponSystems, List<String> defensiveSystems) {
        B5ShipType ship = new B5ShipType(id, name, faction, classType);
        ship.setHull(hull);
        ship.setArmor(armor);
        ship.setWeapons(weapons);
        ship.setDefenses(defenses);
        ship.setSpeed(speed);
        ship.setCrew(crew);
        ship.setWeaponSystems(weaponSystems);
        ship.setDefensiveSystems(defensiveSystems);
        return ship;
    }

    private int getShipCount(Map<String, Object> fleet) {
        return (int) fleet.getOrDefault("shipCount", 0);
    }
}
