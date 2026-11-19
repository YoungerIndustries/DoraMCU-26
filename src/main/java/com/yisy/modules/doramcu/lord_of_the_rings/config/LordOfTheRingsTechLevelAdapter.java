package com.yisy.modules.doramcu.lord_of_the_rings.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Lord of the Rings tech level adapter.
 * <p>
 * Maps generic tech-level modules to LOTR lore-appropriate names and stats.
 * LOTR spans TL1–TL5 (Primitive through Magical).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL1: Primitive — Hobbits, basic tools, simple weapons</li>
 *   <li>TL2: Medieval — Men of Gondor, Rohan, standard armies</li>
 *   <li>TL3: Advanced Medieval — Dwarven craftsmanship, Elven weapons</li>
 *   <li>TL4: Arcane — Wizard magic, Ring powers</li>
 *   <li>TL5: Mythical — Maiar power, Balrog fire, Ent wrath</li>
 * </ul>
 * </p>
 */
@Component
public class LordOfTheRingsTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL1: Primitive (Hobbit-level) ===
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "COLONY", "Hobbit Hole",
                Map.of("hp", 50, "capacity", 100, "garrison", 5));
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Hobbiton Farm",
                Map.of("hp", 40, "capacity", 200));
        putLore(TechLevel.TL1, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Hobbit Cart",
                Map.of("speed", 3, "capacity", 2, "armor", 1));
        putLore(TechLevel.TL1, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Hedge Wall",
                Map.of("ac", 4, "hp", 60));
        putLore(TechLevel.TL1, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Town Crier",
                Map.of("range", 5, "encryptionLevel", 0));

        // === TL2: Medieval (Men of Gondor/Rohan) ===
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Gondor Fortress",
                Map.of("hp", 300, "capacity", 800, "garrison", 150));
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "BARRACKS", "Rohirrim Barracks",
                Map.of("hp", 150, "capacity", 250, "garrison", 100));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Gondor Armory",
                Map.of("output", 20, "weapons", 15));
        putLore(TechLevel.TL2, ModuleCategory.VEHICLE, "INFANTRY_FIGHTING_VEHICLE", "Rohirrim Charger",
                Map.of("speed", 10, "capacity", 1, "armor", 8, "cannonDamage", "2d6"));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Ballista Emplacement",
                Map.of("ac", 8, "hp", 100, "attackDamage", "3d6", "range", 6));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "WATCHTOWER", "Watchtower of Gondor",
                Map.of("ac", 6, "hp", 80, "visionRange", 8));
        putLore(TechLevel.TL2, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Beacon Tower",
                Map.of("range", 50, "detectionRange", 40));

        // === TL3: Advanced Medieval (Dwarven/Elven) ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Moria Stronghold",
                Map.of("hp", 500, "capacity", 1500, "garrison", 300));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_QUARTERS", "Elven Lodge",
                Map.of("hp", 250, "capacity", 400, "garrison", 200));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "POWERED_ARMOR_FACTORY", "Dwarven Forge",
                Map.of("output", 35, "armorUnits", 15));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "TANK", "Dwarven War Machine",
                Map.of("speed", 4, "capacity", 5, "armor", 25, "cannonDamage", "4d6"));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Elven Ward Stone",
                Map.of("ac", 15, "hp", 400, "attackDamage", "5d6", "range", 8));
        putLore(TechLevel.TL3, ModuleCategory.COMMUNICATION, "ORBITAL_COMMS", "Elven Sending Stone",
                Map.of("range", 200, "encryptionLevel", 3));

        // === TL4: Arcane (Wizard/Ring powers) ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Wizard Tower",
                Map.of("hp", 1000, "capacity", 5000, "garrison", 500));
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Rivendell Sanctuary",
                Map.of("hp", 600, "capacity", 3000, "tradeBonus", 4));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "NUKE_FACTORY", "Ring Forge",
                Map.of("output", 20, "rings", 1));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "POWERED_ARMOR_PROTOTYPE", "Ring-bearer Cloak",
                Map.of("speed", 8, "capacity", 1, "armor", 15, "weaponDamage", "2d6", "invisibility", true));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Elven Ward",
                Map.of("ac", 20, "hp", 1000, "shieldHP", 2000));
        putLore(TechLevel.TL4, ModuleCategory.COMMUNICATION, "FEDERATION_NETWORK", "Palantír Network",
                Map.of("range", 500, "encryptionLevel", 5, "interplanetary", true));

        // === TL5: Mythical (Maiar/Balrog/Ent) ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Valinor Gateway",
                Map.of("hp", 10000, "capacity", 100000, "tradeBonus", 10));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WARSHIP_YARD", "Ship of the Valar",
                Map.of("output", 100, "ships", 20));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "WARSHIP", "Great Eagle",
                Map.of("speed", 20, "capacity", 100, "armor", 50, "weaponDamage", "10d6"));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Power of the Valar",
                Map.of("ac", 50, "hp", 10000, "shieldHP", 20000));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Ent Fortress",
                Map.of("ac", 40, "hp", 5000, "garrison", 100, "turretDamage", "8d6"));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "DEEP_SPACE_SENSOR", "Eye of the Valar",
                Map.of("range", 5000, "detectionRange", 4000, "alienDetection", true));
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
        return TechLevel.TL5;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL1;
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
