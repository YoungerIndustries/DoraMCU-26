package com.yisy.modules.doramcu.fps.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * FPS tech level adapter.
 * <p>
 * Maps generic tech-level modules to FPS game universe lore-appropriate names and stats.
 * FPS supports TL6–TL10 (Information Age through Post-Human).
 * </p>
 */
@Component
public class FpsTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL6: Information Age (Modern military) ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Forward Operating Base",
                Map.of("pop", 1000, "security", 10, "tradeBonus", 5));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Space Station",
                Map.of("pop", 5000, "security", 12));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Power Plant",
                Map.of("output", 500, "energy", 500));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Quarry",
                Map.of("output", 300, "minerals", 200));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Cargo Plane",
                Map.of("speed", 600, "cargo", 200));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Military Outpost",
                Map.of("pop", 200, "security", 10));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "WALL", "Concrete Bunker",
                Map.of("defense", 8, "health", 200));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "RIFLE", "Assault Rifle",
                Map.of("damage", 12, "range", 100));
        putLore(TechLevel.TL6, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Body Armor",
                Map.of("defense", 5, "speedPenalty", 1));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "MERCHANT", "Arms Dealer",
                Map.of("credits", 1000, "reputation", 40));
        putLore(TechLevel.TL6, ModuleCategory.MONSTER, "BANDIT", "Insurgent",
                Map.of("hp", 40, "attack", 10, "defense", 4));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "COW", "Farm Cow",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Livestock", "size", "Large"));

        // === TL7: Interplanetary (Near-future military) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Combat Base",
                Map.of("pop", 5000, "security", 12, "tradeBonus", 10));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Platform",
                Map.of("pop", 20000, "security", 15));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor",
                Map.of("output", 1000, "energy", 1000));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Deep Mine",
                Map.of("output", 600, "minerals", 400));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Drop Ship",
                Map.of("speed", 1500, "cargo", 500));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Base",
                Map.of("pop", 1000, "security", 12));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "WALL", "Shield Generator",
                Map.of("defense", 15, "health", 500));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Plasma Rifle",
                Map.of("damage", 20, "range", 150));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Powered Armor",
                Map.of("defense", 10, "speedPenalty", 0));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Black Market Vendor",
                Map.of("credits", 5000, "reputation", 60));
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "BANDIT", "Cyborg Assassin",
                Map.of("hp", 80, "attack", 20, "defense", 10));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "COW", "Lab-Grown Livestock",
                Map.of("milkYield", 40, "meatYield", 350, "type", "Bio-Engineered", "size", "Large"));

        // === TL8: Interstellar (Space military) ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Space Fortress",
                Map.of("pop", 20000, "security", 15, "tradeBonus", 20));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Space Station",
                Map.of("pop", 100000, "security", 18));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Antimatter Core",
                Map.of("output", 3000, "energy", 3000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Refinery",
                Map.of("output", 1500, "minerals", 1000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Starfighter",
                Map.of("speed", 5000, "cargo", 200));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Orbital Defense Platform",
                Map.of("pop", 5000, "security", 18));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "WALL", "Energy Barrier",
                Map.of("defense", 25, "health", 2000));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Beam Weapon",
                Map.of("damage", 35, "range", 300));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Energy Shield",
                Map.of("defense", 15, "speedPenalty", 0));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Galactic Trader",
                Map.of("credits", 20000, "reputation", 80));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "BANDIT", "Alien Warlord",
                Map.of("hp", 200, "attack", 40, "defense", 20));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "COW", "Synthetic Beast",
                Map.of("milkYield", 80, "meatYield", 600, "type", "Synthetic", "size", "Large"));

        // === TL9: Galactic (Empire-scale military) ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Galactic Command",
                Map.of("pop", 100000, "security", 20, "tradeBonus", 40));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Megastructure",
                Map.of("pop", 500000, "security", 25));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Singularity Reactor",
                Map.of("output", 10000, "energy", 10000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Planet Harvester",
                Map.of("output", 5000, "minerals", 4000));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Dreadnought",
                Map.of("speed", 10000, "cargo", 5000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Galactic Hub",
                Map.of("pop", 50000, "security", 25));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "WALL", "Dimensional Shield",
                Map.of("defense", 50, "health", 10000));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Quantum Cannon",
                Map.of("damage", 70, "range", 1000));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Phase Armor",
                Map.of("defense", 30, "speedPenalty", 0));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Empire Merchant",
                Map.of("credits", 100000, "reputation", 95));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "BANDIT", "Fleet Commander",
                Map.of("hp", 800, "attack", 80, "defense", 40));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Holographic Animal",
                Map.of("milkYield", 200, "meatYield", 2000, "type", "Holographic", "size", "Large"));

        // === TL10: Post-Human (Transcendent military) ===
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Reality Fortress",
                Map.of("pop", 1000000, "security", 30, "tradeBonus", 100));
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Dyson Sphere",
                Map.of("pop", 5000000, "security", 35));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Vacuum Tap",
                Map.of("output", 100000, "energy", 100000));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Matter Synthesizer",
                Map.of("output", 100000, "minerals", 100000));
        putLore(TechLevel.TL10, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Transwarp Carrier",
                Map.of("speed", 50000, "cargo", 50000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Reality Anchor",
                Map.of("pop", 500000, "security", 35));
        putLore(TechLevel.TL10, ModuleCategory.DEFENSE, "WALL", "Existence Barrier",
                Map.of("defense", 100, "health", 100000));
        putLore(TechLevel.TL10, ModuleCategory.WEAPON, "RIFLE", "Reality Weapon",
                Map.of("damage", 200, "range", 10000));
        putLore(TechLevel.TL10, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Existence Armor",
                Map.of("defense", 60, "speedPenalty", 0));
        putLore(TechLevel.TL10, ModuleCategory.NPC, "MERCHANT", "Transcendent Trader",
                Map.of("credits", 1000000, "reputation", 100));
        putLore(TechLevel.TL10, ModuleCategory.MONSTER, "BANDIT", "Dimensional Entity",
                Map.of("hp", 5000, "attack", 200, "defense", 100));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "COW", "Energy Lifeform",
                Map.of("milkYield", 1000, "meatYield", 10000, "type", "Energy", "size", "Variable"));
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
        return TechLevel.TL10;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL6;
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
