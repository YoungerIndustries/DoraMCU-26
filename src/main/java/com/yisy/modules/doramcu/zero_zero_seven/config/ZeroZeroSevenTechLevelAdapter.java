package com.yisy.modules.doramcu.zero_zero_seven.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 007 Espionage tech level adapter.
 * <p>
 * Maps generic tech-level modules to 007 universe lore-appropriate names and stats.
 * 007 supports TL7–TL9 (Interplanetary through Galactic — Modern Espionage era).
 * </p>
 */
@Component
public class ZeroZeroSevenTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL7: Interplanetary (Modern espionage) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "MI6 Safehouse",
                Map.of("pop", 100, "security", 15, "tradeBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Spy Station",
                Map.of("pop", 500, "security", 20));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Secret Lab",
                Map.of("output", 500, "energy", 500));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Underground Facility",
                Map.of("output", 300, "minerals", 200));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Spy Plane",
                Map.of("speed", 1200, "cargo", 50));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Covert Outpost",
                Map.of("pop", 50, "security", 15));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "WALL", "Perimeter Alarm",
                Map.of("defense", 10, "health", 200));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Walther PPK",
                Map.of("damage", 10, "range", 50));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Bulletproof Vest",
                Map.of("defense", 5, "speedPenalty", 1));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Black Market Dealer",
                Map.of("credits", 5000, "reputation", 30));
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "BANDIT", "Rival Agent",
                Map.of("hp", 50, "attack", 12, "defense", 8));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "COW", "Farm Animal",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Livestock", "size", "Large"));

        // === TL8: Interstellar (Advanced espionage) ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Spectre Headquarters",
                Map.of("pop", 500, "security", 20, "tradeBonus", 10));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Space Surveillance Hub",
                Map.of("pop", 2000, "security", 25));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Quantum Computing Center",
                Map.of("output", 2000, "energy", 2000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Deep Underground Base",
                Map.of("output", 1000, "minerals", 800));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Stealth Submarine",
                Map.of("speed", 3000, "cargo", 100));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Listening Post",
                Map.of("pop", 200, "security", 20));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "WALL", "Electronic Countermeasures",
                Map.of("defense", 20, "health", 1000));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Smart Rifle",
                Map.of("damage", 20, "range", 200));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Stealth Suit",
                Map.of("defense", 10, "speedPenalty", 0));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Arms Smuggler",
                Map.of("credits", 20000, "reputation", 50));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "BANDIT", "Spectre Assassin",
                Map.of("hp", 100, "attack", 25, "defense", 15));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "COW", "Lab Animal",
                Map.of("milkYield", 40, "meatYield", 350, "type", "Bio-Engineered", "size", "Large"));

        // === TL9: Galactic (Empire-scale espionage) ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Global Intelligence Hub",
                Map.of("pop", 5000, "security", 25, "tradeBonus", 20));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Command Center",
                Map.of("pop", 20000, "security", 30));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "AI Research Facility",
                Map.of("output", 5000, "energy", 5000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Planetary Surveillance Net",
                Map.of("output", 3000, "minerals", 2000));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Stealth Fighter",
                Map.of("speed", 8000, "cargo", 200));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Deep Space Intelligence",
                Map.of("pop", 1000, "security", 25));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "WALL", "Cyber Defense Grid",
                Map.of("defense", 35, "health", 5000));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Plasma Sidearm",
                Map.of("damage", 40, "range", 500));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Invisibility Cloak",
                Map.of("defense", 20, "speedPenalty", 0));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "International Syndicate",
                Map.of("credits", 100000, "reputation", 70));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "BANDIT", "Mega-Villain",
                Map.of("hp", 300, "attack", 50, "defense", 25));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Synthetic Animal",
                Map.of("milkYield", 80, "meatYield", 600, "type", "Synthetic", "size", "Large"));
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
        return TechLevel.TL9;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL7;
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
