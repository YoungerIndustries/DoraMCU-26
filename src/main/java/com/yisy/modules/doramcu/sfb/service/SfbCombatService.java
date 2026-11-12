package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.fourx.FourxCombatService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SFB implementation of the Combat 4X pillar.
 * <p>
 * Wraps the existing SfbCombatEngine and SfbShipLoader to provide fleet creation,
 * ship types, and round-based combat. Uses SFB ship classes (D7, D9, K'Vort, etc.)
 * for faction-specific fleets.
 * </p>
 */
@Service
@Primary
public class SfbCombatService implements FourxCombatService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(SfbCombatService.class);

    /** Ship types keyed by faction. */
    private final Map<String, List<Map<String, Object>>> shipTypes = new ConcurrentHashMap<>();

    /** Fleets keyed by fleet ID. */
    private final Map<String, Map<String, Object>> fleets = new ConcurrentHashMap<>();

    /** Fleets per faction. */
    private final Map<String, List<String>> factionFleets = new ConcurrentHashMap<>();

    /** Active combats keyed by combat ID. */
    private final Map<String, Map<String, Object>> combats = new ConcurrentHashMap<>();

    private final SfbShipLoader shipLoader;
    private final AtomicLong fleetIdCounter = new AtomicLong(1);
    private final AtomicLong combatIdCounter = new AtomicLong(1);
    private final Random random = new Random();

    public SfbCombatService() {
        this.shipLoader = new SfbShipLoader();
    }

    public SfbCombatService(SfbShipLoader shipLoader) {
        this.shipLoader = shipLoader;
    }

    @PostConstruct
    public void initShipTypes() {
        // Federation ship types
        shipTypes.put("FEDERATION", List.of(
                createShipType("D7", "D7-Class (Constellation)", "Frigate", 100, 20, 30, 25, 3, 200),
                createShipType("D9", "D9-Class (Ambassador)", "Cruiser", 200, 35, 50, 40, 2, 400),
                createShipType("GALAXY", "Galaxy-Class", "Cruiser", 180, 30, 45, 35, 2, 350),
                createShipType("SOVEREIGN", "Sovereign-Class", "Battle Cruiser", 300, 50, 70, 55, 2, 600),
                createShipType("NEGOTIATOR", "Negotiator-Class", "Science", 120, 20, 25, 30, 2, 250)
        ));

        // Klingon ship types
        shipTypes.put("KLINGON", List.of(
                createShipType("KORT", "Kort-Class", "Frigate", 110, 25, 35, 20, 2, 220),
                createShipType("KVORT", "K'Vort-Class", "Cruiser", 220, 40, 55, 35, 2, 450),
                createShipType("K'WANA", "K'Wana-Class", "Cruiser", 200, 35, 50, 30, 2, 400),
                createShipType("D7_K", "D7-Class (Klingon)", "Battle Cruiser", 280, 45, 65, 45, 2, 550),
                createShipType("HAMMERHEAD", "Hammerhead-Class", "Destroyer", 150, 30, 45, 25, 3, 300)
        ));

        // Romulan ship types
        shipTypes.put("ROMULAN", List.of(
                createShipType("D'DERID", "D'Deridex-Class", "Battle Cruiser", 250, 40, 60, 50, 2, 500),
                createShipType("KINTA", "Kinta-Class", "Cruiser", 180, 30, 45, 40, 2, 350),
                createShipType("WARBRINGER", "Warbringer-Class", "Cruiser", 200, 35, 50, 45, 2, 400),
                createShipType("NARADA", "Narada-Class", "Battlestar", 350, 55, 75, 60, 2, 700),
                createShipType("REVENANT", "Revenant-Class", "Frigate", 100, 20, 30, 30, 3, 200)
        ));

        // Gorn ship types
        shipTypes.put("GORN", List.of(
                createShipType("GORN_CRUISER", "Gorn Cruiser", "Cruiser", 230, 45, 55, 30, 1, 500),
                createShipType("GORN_BATTLE", "Gorn Battle Cruiser", "Battle Cruiser", 300, 55, 70, 40, 1, 700),
                createShipType("GORN_DESTROYER", "Gorn Destroyer", "Destroyer", 160, 35, 45, 25, 2, 350)
        ));

        // Tholian ship types
        shipTypes.put("THOLIAN", List.of(
                createShipType("THOLIAN_CRUISER", "Tholian Cruiser", "Cruiser", 190, 30, 40, 45, 2, 380),
                createShipType("THOLIAN_WEB", "Tholian Web Ship", "Special", 150, 25, 35, 50, 2, 300),
                createShipType("THOLIAN_DESTROYER", "Tholian Destroyer", "Destroyer", 130, 20, 30, 35, 3, 250)
        ));

        // M'Haari ship types
        shipTypes.put("M'HAARI", List.of(
                createShipType("MH_CRUISER", "M'Haari Cruiser", "Cruiser", 210, 35, 50, 35, 2, 420),
                createShipType("MH_BATTLE", "M'Haari Battle Cruiser", "Battle Cruiser", 280, 45, 65, 45, 2, 560),
                createShipType("MH_FRIGATE", "M'Haari Frigate", "Frigate", 120, 20, 30, 25, 3, 240)
        ));

        // Suliban ship types
        shipTypes.put("SULIBAN", List.of(
                createShipType("SULIBAN_SHIP", "Suliban Ship", "Frigate", 100, 15, 25, 30, 4, 180),
                createShipType("SULIBAN_CLOAKED", "Suliban Cloaked Ship", "Special", 130, 20, 35, 40, 3, 220)
        ));

        // Independent ship types
        shipTypes.put("INDEPENDENT", List.of(
                createShipType("FREIGHTER", "Freighter", "Freighter", 80, 15, 10, 15, 2, 150),
                createShipType("CORVETTE", "Corvette", "Frigate", 90, 18, 25, 20, 3, 170),
                createShipType("RAIDER", "Raider", "Destroyer", 140, 25, 40, 25, 3, 280)
        ));

        log.info("Initialized ship types for {} factions", shipTypes.size());
    }

    private Map<String, Object> createShipType(String id, String name, String classType,
                                                int hull, int armor, int weapons, int defenses,
                                                int speed, int crew) {
        Map<String, Object> shipType = new LinkedHashMap<>();
        shipType.put("id", id);
        shipType.put("name", name);
        shipType.put("classType", classType);
        shipType.put("hull", hull);
        shipType.put("armor", armor);
        shipType.put("weapons", weapons);
        shipType.put("defenses", defenses);
        shipType.put("speed", speed);
        shipType.put("crew", crew);
        return shipType;
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.CUSTOM;
    }

    @Override
    public Map<String, Object> initiateCombat(String attackerFleetId, String defenderFleetId) {
        Map<String, Object> attackerFleet = fleets.get(attackerFleetId);
        Map<String, Object> defenderFleet = fleets.get(defenderFleetId);

        if (attackerFleet == null || defenderFleet == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Fleet not found");
            error.put("attackerFleetId", attackerFleetId);
            error.put("defenderFleetId", defenderFleetId);
            return error;
        }

        String combatId = "combat-" + combatIdCounter.getAndIncrement();
        Map<String, Object> combat = new LinkedHashMap<>();
        combat.put("id", combatId);
        combat.put("attackerFleetId", attackerFleetId);
        combat.put("defenderFleetId", defenderFleetId);
        combat.put("round", 0);
        combat.put("active", true);

        // Copy fleet states for combat
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attackerShips = (List<Map<String, Object>>) attackerFleet.get("ships");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> defenderShips = (List<Map<String, Object>>) defenderFleet.get("ships");

        combat.put("attackerShips", new ArrayList<>(attackerShips));
        combat.put("defenderShips", new ArrayList<>(defenderShips));
        combat.put("attackerDamage", 0);
        combat.put("defenderDamage", 0);
        combat.put("attackerCasualties", 0);
        combat.put("defenderCasualties", 0);
        combat.put("log", new ArrayList<String>());

        combats.put(combatId, combat);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("combatId", combatId);
        result.put("status", "initiated");
        result.put("attackerFleet", attackerFleet.get("name"));
        result.put("defenderFleet", defenderFleet.get("name"));
        result.put("attackerShips", attackerShips.size());
        result.put("defenderShips", defenderShips.size());

        log.info("Combat initiated: {} vs {} ({})", attackerFleet.get("name"),
                defenderFleet.get("name"), combatId);
        return result;
    }

    @Override
    public Map<String, Object> executeCombatRound(String combatId) {
        Map<String, Object> combat = combats.get(combatId);
        if (combat == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Combat not found: " + combatId);
            return error;
        }

        if (!(boolean) combat.get("active")) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("combatId", combatId);
            result.put("status", "ended");
            result.put("message", "Combat already ended.");
            return result;
        }

        int round = (int) combat.get("round") + 1;
        combat.put("round", round);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attackerShips = (List<Map<String, Object>>) combat.get("attackerShips");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> defenderShips = (List<Map<String, Object>>) combat.get("defenderShips");

        @SuppressWarnings("unchecked")
        List<String> log = (List<String>) combat.get("log");

        // Calculate attacker damage
        int attackerPower = calculateFleetPower(attackerShips);
        int defenderPower = calculateFleetPower(defenderShips);

        // Attacker strikes
        int attackerRoll = random.nextInt(attackerPower) + 1;
        int defenderDefense = (int) defenderShips.stream()
                .mapToInt(s -> (int) s.getOrDefault("defenses", 0)).sum() / 2;
        int defenderHit = Math.max(0, attackerRoll - defenderDefense / 2);
        int defenderHullLoss = 0;
        for (Map<String, Object> ship : new ArrayList<>(defenderShips)) {
            int hull = (int) ship.getOrDefault("hull", 0);
            int damage = Math.min(hull, defenderHit / Math.max(1, defenderShips.size()));
            hull -= damage;
            ship.put("hull", hull);
            defenderHullLoss += damage;
            if (hull <= 0) {
                defenderShips.remove(ship);
                combat.put("defenderCasualties", (int) combat.getOrDefault("defenderCasualties", 0) + 1);
                log.add("Round " + round + ": " + ship.get("name") + " destroyed!");
            }
        }
        combat.put("defenderDamage", (int) combat.getOrDefault("defenderDamage", 0) + defenderHullLoss);

        // Defender counter-attacks
        int defenderRoll = random.nextInt(defenderPower) + 1;
        int attackerDefense = (int) attackerShips.stream()
                .mapToInt(s -> (int) s.getOrDefault("defenses", 0)).sum() / 2;
        int attackerHit = Math.max(0, defenderRoll - attackerDefense / 2);
        int attackerHullLoss = 0;
        for (Map<String, Object> ship : new ArrayList<>(attackerShips)) {
            int hull = (int) ship.getOrDefault("hull", 0);
            int damage = Math.min(hull, attackerHit / Math.max(1, attackerShips.size()));
            hull -= damage;
            ship.put("hull", hull);
            attackerHullLoss += damage;
            if (hull <= 0) {
                attackerShips.remove(ship);
                combat.put("attackerCasualties", (int) combat.getOrDefault("attackerCasualties", 0) + 1);
                log.add("Round " + round + ": " + ship.get("name") + " destroyed!");
            }
        }
        combat.put("attackerDamage", (int) combat.getOrDefault("attackerDamage", 0) + attackerHullLoss);

        log.add("Round " + round + ": Attacker dealt " + defenderHullLoss + " damage, " +
                "Defender dealt " + attackerHullLoss + " damage");

        // Check for combat end
        boolean combatOver = attackerShips.isEmpty() || defenderShips.isEmpty();
        if (combatOver) {
            combat.put("active", false);
            String winner = attackerShips.isEmpty() ? "defender" : "attacker";
            combat.put("winner", winner);
            log.add("Combat ended: " + winner + " wins!");
        }

        // Build result
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("combatId", combatId);
        result.put("round", round);
        result.put("attackerShipsRemaining", attackerShips.size());
        result.put("defenderShipsRemaining", defenderShips.size());
        result.put("attackerDamageDealt", defenderHullLoss);
        result.put("defenderDamageDealt", attackerHullLoss);
        result.put("attackerTotalDamage", combat.get("attackerDamage"));
        result.put("defenderTotalDamage", combat.get("defenderDamage"));
        result.put("attackerCasualties", combat.get("attackerCasualties"));
        result.put("defenderCasualties", combat.get("defenderCasualties"));
        result.put("active", !combatOver);
        if (combatOver) {
            result.put("winner", combat.get("winner"));
        }
        result.put("log", log);

        return result;
    }

    @Override
    public Map<String, Object> getCombatStatus(String combatId) {
        Map<String, Object> combat = combats.get(combatId);
        if (combat == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Combat not found: " + combatId);
            return error;
        }

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("combatId", combatId);
        status.put("round", combat.get("round"));
        status.put("active", combat.get("active"));
        status.put("attackerShips", ((List<?>) combat.get("attackerShips")).size());
        status.put("defenderShips", ((List<?>) combat.get("defenderShips")).size());
        status.put("attackerDamage", combat.get("attackerDamage"));
        status.put("defenderDamage", combat.get("defenderDamage"));
        status.put("attackerCasualties", combat.get("attackerCasualties"));
        status.put("defenderCasualties", combat.get("defenderCasualties"));
        if (combat.containsKey("winner")) {
            status.put("winner", combat.get("winner"));
        }
        return status;
    }

    @Override
    public List<Map<String, Object>> getShipTypes(String factionId) {
        List<Map<String, Object>> types = shipTypes.get(factionId.toUpperCase());
        if (types == null) {
            // Fall back to independent ships
            return shipTypes.getOrDefault("INDEPENDENT", List.of());
        }
        return types;
    }

    @Override
    public Map<String, Object> createFleet(String factionId, String fleetName,
                                            String shipTypeId, int count) {
        String id = "fleet-" + fleetIdCounter.getAndIncrement();
        Map<String, Object> fleet = new LinkedHashMap<>();
        fleet.put("id", id);
        fleet.put("name", fleetName);
        fleet.put("factionId", factionId);
        fleet.put("ships", new ArrayList<Map<String, Object>>());

        List<Map<String, Object>> types = getShipTypes(factionId);
        Map<String, Object> shipType = null;
        for (Map<String, Object> type : types) {
            if (type.get("id").equals(shipTypeId)) {
                shipType = type;
                break;
            }
        }

        if (shipType == null) {
            // Use first available ship type
            if (!types.isEmpty()) {
                shipType = types.get(0);
            } else {
                shipType = createShipType("GENERIC", "Generic Ship", "Frigate", 100, 20, 30, 25, 3, 200);
            }
        }

        // Create ships
        List<Map<String, Object>> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> ship = new LinkedHashMap<>(shipType);
            ship.put("instanceId", id + "-" + i);
            ship.put("name", fleetName + "-" + (i + 1));
            ships.add(ship);
        }
        fleet.put("ships", ships);

        fleets.put(id, fleet);
        factionFleets.computeIfAbsent(factionId, k -> new ArrayList<>()).add(id);

        log.info("Created fleet: {} ({}) with {} ships for {}", fleetName, id, count, factionId);
        return fleet;
    }

    @Override
    public List<Map<String, Object>> getFleets(String factionId) {
        List<String> fleetIds = factionFleets.get(factionId);
        if (fleetIds == null) return List.of();

        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : fleetIds) {
            Map<String, Object> fleet = fleets.get(id);
            if (fleet != null) {
                result.add(fleet);
            }
        }
        return result;
    }

    @Override
    public int calculateCombatPower(String fleetId) {
        Map<String, Object> fleet = fleets.get(fleetId);
        if (fleet == null) return 0;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ships = (List<Map<String, Object>>) fleet.get("ships");
        return calculateFleetPower(ships);
    }

    @Override
    public List<String> getActiveCombats() {
        List<String> active = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : combats.entrySet()) {
            if ((boolean) entry.getValue().getOrDefault("active", false)) {
                active.add(entry.getKey());
            }
        }
        return active;
    }

    private int calculateFleetPower(List<Map<String, Object>> ships) {
        return ships.stream()
                .mapToInt(s -> (int) s.getOrDefault("weapons", 0) +
                        (int) s.getOrDefault("defenses", 0) +
                        (int) s.getOrDefault("armor", 0))
                .sum();
    }
}
