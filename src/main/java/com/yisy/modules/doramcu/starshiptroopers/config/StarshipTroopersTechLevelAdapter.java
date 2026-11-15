package com.yisy.modules.doramcu.starshiptroopers.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Starship Troopers tech level adapter.
 * <p>
 * Maps generic tech-level modules to Starship Troopers lore-appropriate names and stats.
 * Starship Troopers supports TL5–TL7 (Modern through Interplanetary era).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL5: Modern infantry, rifles, basic vehicles, Federation colonies begin</li>
 *   <li>TL6: Powered armor prototypes, orbital drop ships, Federated Solar System</li>
 *   <li>TL7: Full Mobile Infantry powered armor, Federated Solar System colonies, Arachnid warfare</li>
 * </ul>
 * </p>
 */
@Component
public class StarshipTroopersTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL5: Modern Era — Federation Foundation ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "COLONY", "Federation Colony Outpost",
                Map.of("hp", 200, "capacity", 500, "garrison", 50));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "BARRACKS", "Mobile Infantry Barracks",
                Map.of("hp", 150, "capacity", 100, "garrison", 80));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Civilian Habitat Dome",
                Map.of("hp", 180, "capacity", 1000));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "AMMUNITION_FACTORY", "Ammunition Factory",
                Map.of("output", 30, "ammo", 25));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Weapons Factory",
                Map.of("output", 25, "weapons", 15));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "VEHICLE_PLANT", "Vehicle Assembly Plant",
                Map.of("output", 20, "vehicles", 5));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Armored Transport Truck",
                Map.of("speed", 8, "capacity", 20, "armor", 10));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "INFANTRY_FIGHTING_VEHICLE", "Infantry Fighting Vehicle",
                Map.of("speed", 10, "capacity", 15, "armor", 15, "cannonDamage", "2d6"));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "TANK", "Main Battle Tank",
                Map.of("speed", 6, "capacity", 5, "armor", 25, "cannonDamage", "4d6"));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "ROAD_NETWORK", "Paved Road Network",
                Map.of("travelBonus", 2));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "AIRFIELD", "Military Airfield",
                Map.of("hp", 200, "aircraftCapacity", 10));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Perimeter Fortification",
                Map.of("ac", 15, "hp", 300));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "WATCHTOWER", "Armored Watchtower",
                Map.of("ac", 12, "hp", 100, "visionRange", 5));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Anti-Tank Battery",
                Map.of("ac", 10, "hp", 80, "attackDamage", "3d6", "range", 8));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Military Radio Network",
                Map.of("range", 50, "encryptionLevel", 3));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Radar Station",
                Map.of("range", 100, "detectionRange", 80));

        // === TL6: Powered Armor Prototypes — Federated Solar System ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Planetary Defense Base",
                Map.of("hp", 500, "capacity", 2000, "garrison", 200));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_QUARTERS", "Mobile Infantry Quarters",
                Map.of("hp", 300, "capacity", 500, "garrison", 300));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "COLONY_TOWN", "Federated Colony Town",
                Map.of("hp", 400, "capacity", 5000, "tradeBonus", 3));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "POWERED_ARMOR_FACTORY", "Powered Armor Prototype Factory",
                Map.of("output", 30, "armorUnits", 10));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "DROPShip_YARD", "DropShip Assembly Yard",
                Map.of("output", 20, "ships", 3));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "NUCLEAR_FACILITY", "Nuclear Weapons Facility",
                Map.of("output", 15, "nukes", 2));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "DROPShip", "Orbital DropShip",
                Map.of("speed", 15, "capacity", 100, "armor", 30, "troopBay", 50));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "POWERED_ARMOR_PROTOTYPE", "Powered Armor Prototype",
                Map.of("speed", 12, "capacity", 1, "armor", 20, "weaponDamage", "2d6"));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "AIR_CAR", "Air Car Transport",
                Map.of("speed", 20, "capacity", 8, "armor", 5));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "RETRIEVAL_BOAT", "Retrieval Boat",
                Map.of("speed", 10, "capacity", 12, "armor", 8, "recoveryRate", 5));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR", "Orbital Elevator Station",
                Map.of("hp", 800, "throughput", 50));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "STARPORT", "Planetary Starport",
                Map.of("hp", 600, "shipCapacity", 5));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Planetary Defense Battery",
                Map.of("ac", 20, "hp", 500, "attackDamage", "6d6", "range", 10));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Missile Silo Complex",
                Map.of("ac", 18, "hp", 400, "missiles", 10, "range", 50));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "ARMORED_FORTRESS", "Armored Fortress",
                Map.of("ac", 22, "hp", 800, "garrison", 100));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "ORBITAL_COMMS", "Orbital Communications Relay",
                Map.of("range", 500, "encryptionLevel", 5));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "SENSOR_ARRAY", "Planetary Sensor Array",
                Map.of("range", 200, "detectionRange", 150));

        // === TL7: Full Mobile Infantry — Federated Solar System Peak ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Mobile Infantry Forward Base",
                Map.of("hp", 1000, "capacity", 5000, "garrison", 1000));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Federated Solar System Capital",
                Map.of("hp", 2000, "capacity", 50000, "tradeBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "BRAIN_BUG_RESEARCH", "Arachnid Research Facility",
                Map.of("hp", 500, "capacity", 200, "researchBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "MOBILE_INFANTRY_ARMORY", "Mobile Infantry Full Armor Factory",
                Map.of("output", 50, "armorUnits", 25));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "WARSHIP_YARD", "Warship Construction Yard",
                Map.of("output", 30, "ships", 2));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "NUKE_FACTORY", "Tactical Nuclear Arsenal Factory",
                Map.of("output", 20, "nukes", 5));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MOBILE_INFANTRY_ARMOR", "Mobile Infantry Powered Armor",
                Map.of("speed", 15, "capacity", 1, "armor", 35, "weaponDamage", "4d6", "jumpJets", true));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MARAUDER_SQUAD", "Marauder Squad Vehicle",
                Map.of("speed", 12, "capacity", 10, "armor", 25, "weaponDamage", "3d6"));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP", "Federation WarShip",
                Map.of("speed", 25, "capacity", 500, "armor", 50, "weaponDamage", "10d6"));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "JUMPSHIP", "Interplanetary JumpShip",
                Map.of("speed", 30, "capacity", 200, "armor", 40, "jumpRange", "interplanetary"));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "ORBITAL_DOCK", "Orbital Ship Dock",
                Map.of("hp", 1500, "shipCapacity", 10));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "COLONY_DOME", "Pressurized Colony Dome",
                Map.of("hp", 1200, "capacity", 10000));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Planetary Shield Generator",
                Map.of("ac", 30, "hp", 2000, "shieldHP", 5000));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_BATTERY", "Orbital Defense Battery",
                Map.of("ac", 25, "hp", 1000, "attackDamage", "8d6", "range", 20));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Mech Fortress (Anti-Arachnid)",
                Map.of("ac", 28, "hp", 1500, "garrison", 200, "turretDamage", "6d6"));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "FEDERATION_NETWORK", "Federation-wide Communication Network",
                Map.of("range", 1000, "encryptionLevel", 7, "interplanetary", true));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "DEEP_SPACE_SENSOR", "Deep Space Sensor Network",
                Map.of("range", 500, "detectionRange", 300, "alienDetection", true));
    }

    private static void putLore(TechLevel tl, ModuleCategory cat, String genericType,
                                String loreName, Map<String, Object> stats) {
        String key = key(tl, cat, genericType);
        LORE_NAMES.put(key, loreName);
        LORE_STATS.put(key, stats);
    }

    private static String key(TechLevel tl, ModuleCategory cat, String genericType) {
        return tl.name() + "|" + cat.name() + "|" + genericType;
    }

    @Override
    public String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return null;
        return LORE_NAMES.getOrDefault(key(techLevel, category, genericType), null);
    }

    @Override
    public Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyMap();
        return LORE_STATS.getOrDefault(key(techLevel, category, genericType), Collections.emptyMap());
    }

    @Override
    public TechLevel getMaxTechLevel() {
        return TechLevel.TL7;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL5;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(techLevel.name() + "|")) {
                String[] parts = key.split("\\|");
                if (parts.length >= 3) {
                    result.add(parts[2]);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel, ModuleCategory category) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        String prefix = techLevel.name() + "|" + category.name() + "|";
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(prefix)) {
                result.add(key.substring(prefix.length()));
            }
        }
        return result;
    }
}
