package com.yisy.modules.doramcu.starwars.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Star Wars tech level adapter.
 * <p>
 * Maps generic tech-level modules to Star Wars lore-appropriate names and stats.
 * Star Wars spans TL4–TL8 (Blaster era through Death Star superweapons).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL4: Primitive/Outer Rim — basic blasters, landspeeders</li>
 *   <li>TL5: Republic Era — clone armor, starfighters, capital ships</li>
 *   <li>TL6: Imperial Era — stormtrooper armor, Star Destroyers, TIE fighters</li>
 *   <li>TL7: Advanced Imperial — Death Star, superweapons, advanced droids</li>
 *   <li>TL8: Sith Eternal — Sith fleet, ultimate superweapons</li>
 * </ul>
 * </p>
 */
@Component
public class StarWarsTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL4: Outer Rim Primitive ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "COLONY", "Outer Rim Outpost",
                Map.of("hp", 100, "capacity", 200, "garrison", 20));
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Desert Settlement",
                Map.of("hp", 80, "capacity", 500));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Landspeeder",
                Map.of("speed", 6, "capacity", 4, "armor", 3));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Sandwall Fortification",
                Map.of("ac", 8, "hp", 150));
        putLore(TechLevel.TL4, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Subspace Transceiver",
                Map.of("range", 30, "encryptionLevel", 1));

        // === TL5: Republic Era ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Republic Forward Base",
                Map.of("hp", 400, "capacity", 1000, "garrison", 200));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "BARRACKS", "Clone Trooper Barracks",
                Map.of("hp", 200, "capacity", 300, "garrison", 150));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Blaster Weapons Factory",
                Map.of("output", 30, "weapons", 20));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "VEHICLE_PLANT", "Starfighter Assembly Plant",
                Map.of("output", 25, "vehicles", 8));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "INFANTRY_FIGHTING_VEHICLE", "LAAT/i Gunship",
                Map.of("speed", 12, "capacity", 20, "armor", 15, "cannonDamage", "3d6"));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "TANK", "AT-TE Walker",
                Map.of("speed", 5, "capacity", 10, "armor", 30, "cannonDamage", "5d6"));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Anti-Air Emplacement",
                Map.of("ac", 12, "hp", 120, "attackDamage", "4d6", "range", 8));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "WATCHTOWER", "Sentinel Tower",
                Map.of("ac", 10, "hp", 100, "visionRange", 6));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Sensor Array",
                Map.of("range", 80, "detectionRange", 60));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "AIRFIELD", "Starfighter Hangar",
                Map.of("hp", 300, "aircraftCapacity", 20));

        // === TL6: Imperial Era ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Imperial Garrison Base",
                Map.of("hp", 600, "capacity", 2000, "garrison", 500));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_QUARTERS", "Stormtrooper Barracks",
                Map.of("hp", 350, "capacity", 500, "garrison", 300));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "POWERED_ARMOR_FACTORY", "Stormtrooper Armor Factory",
                Map.of("output", 40, "armorUnits", 20));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "DROPShip_YARD", "Shuttle Assembly Yard",
                Map.of("output", 25, "ships", 5));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "DROPShip", "Lambda-class Shuttle",
                Map.of("speed", 18, "capacity", 15, "armor", 20, "troopBay", 10));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "POWERED_ARMOR_PROTOTYPE", "Stormtrooper Powered Armor",
                Map.of("speed", 10, "capacity", 1, "armor", 25, "weaponDamage", "3d6"));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "WARSHIP", "Imperial Star Destroyer",
                Map.of("speed", 4, "capacity", 500, "armor", 50, "weaponDamage", "10d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Turbolaser Defense Battery",
                Map.of("ac", 20, "hp", 500, "attackDamage", "6d6", "range", 10));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Ion Cannon Silo",
                Map.of("ac", 18, "hp", 400, "missiles", 8, "range", 40));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "ORBITAL_COMMS", "Imperial Comms Relay",
                Map.of("range", 300, "encryptionLevel", 5));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "STARPORT", "Imperial Starport",
                Map.of("hp", 500, "shipCapacity", 3));

        // === TL7: Advanced Imperial / Rebel Era ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Death Star Garrison",
                Map.of("hp", 2000, "capacity", 50000, "garrison", 5000));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Rebel Hidden Base",
                Map.of("hp", 800, "capacity", 5000, "tradeBonus", 3));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "NUKE_FACTORY", "Superweapon Arsenal",
                Map.of("output", 30, "nukes", 10));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "WARSHIP_YARD", "Star Destroyer Shipyard",
                Map.of("output", 40, "ships", 3));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MOBILE_INFANTRY_ARMOR", "Dark Trooper Armor",
                Map.of("speed", 12, "capacity", 1, "armor", 40, "weaponDamage", "5d6", "jumpJets", true));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP", "Executor-class Super Star Destroyer",
                Map.of("speed", 3, "capacity", 2000, "armor", 80, "weaponDamage", "15d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Shield Generator Dome",
                Map.of("ac", 25, "hp", 1500, "shieldHP", 3000));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_BATTERY", "Orbital Turbolaser Platform",
                Map.of("ac", 22, "hp", 800, "attackDamage", "8d6", "range", 15));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "FEDERATION_NETWORK", "Galactic HoloNet",
                Map.of("range", 1000, "encryptionLevel", 7, "interplanetary", true));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "ORBITAL_DOCK", "Orbital Shipyard",
                Map.of("hp", 1200, "shipCapacity", 8));

        // === TL8: Sith Eternal / Ultimate ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Exegol Sith Temple",
                Map.of("hp", 5000, "capacity", 100000, "tradeBonus", 5));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "WARSHIP_YARD", "Sith Eternal Shipyard",
                Map.of("output", 60, "ships", 10));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "WARSHIP", "Sith Star Destroyer",
                Map.of("speed", 5, "capacity", 3000, "armor", 100, "weaponDamage", "20d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Sith Shield Network",
                Map.of("ac", 35, "hp", 5000, "shieldHP", 10000));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Final Order Fortress",
                Map.of("ac", 30, "hp", 3000, "garrison", 500, "turretDamage", "10d6"));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "DEEP_SPACE_SENSOR", "Sith Surveillance Network",
                Map.of("range", 1000, "detectionRange", 800, "alienDetection", true));
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
        return TechLevel.TL8;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL4;
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
