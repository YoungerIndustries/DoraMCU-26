package com.yisy.modules.doramcu.babylon5.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Babylon 5 tech level adapter.
 * <p>
 * Maps generic tech-level modules to Babylon 5 universe lore-appropriate names and stats.
 * Babylon 5 supports TL7–TL9 (Interplanetary through Galactic).
 * </p>
 */
@Component
public class B5TechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL7: Interplanetary (Early B5 era — Earth Alliance colonies) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Ground Station",
                Map.of("pop", 10000, "security", 9, "tradeBonus", 15));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Platform",
                Map.of("pop", 50000, "security", 10));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor",
                Map.of("output", 1000, "energy", 1000));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Mining Station",
                Map.of("output", 500, "minerals", 400));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "BIOTECH_LAB", "Medical Research Lab",
                Map.of("output", 200, "medicalBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Interplanetary Transport",
                Map.of("speed", 1000, "cargo", 200));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MARS_ROVER", "Planetary Rover",
                Map.of("speed", 20, "cargo", 10));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Outpost",
                Map.of("pop", 1000, "security", 8));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "SPACE_ELEVATOR", "Space Elevator",
                Map.of("tradeBonus", 20));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_GUN", "Orbital Defense Platform",
                Map.of("defense", 15, "weapons", "50d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Planetary Shield Generator",
                Map.of("defense", 20));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "DEEP_SPACE_ARRAY", "Deep Space Array",
                Map.of("range", 100000));

        // === TL8: Interstellar (Standard B5 era — Earth Alliance / Centauri / Minbari) ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION", "Spaceport",
                Map.of("pop", 100000, "security", 12));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "DYSON_RING", "Stellar Energy Ring",
                Map.of("pop", 1000000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ANTIMATTER_PLANT", "Antimatter Refinery",
                Map.of("output", 5000, "fuel", 5000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "NANOFAB", "Nanotech Fabrication Plant",
                Map.of("output", 2000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER", "Battlestar",
                Map.of("speed", 5000, "cargo", 500, "weapons", "100d6", "shieldHP", 500));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "TRANSPORT_SHUTTLE", "Transport Shuttle",
                Map.of("speed", 2000, "cargo", 100));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION", "Jump Gate Station",
                Map.of("tradeBonus", 50));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "STARLIFT", "Starlift",
                Map.of("tradeBonus", 30));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON", "Laser Battery",
                Map.of("defense", 25, "weapons", "200d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "SHIELD_GENERATOR", "Shield Generator Array",
                Map.of("defense", 30, "shieldHP", 1000));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "HYPERNET_RELAY", "Hypernet Relay",
                Map.of("range", 1000000));

        // === TL9: Galactic (Advanced B5 era — Shadows / Vorlons / Babylon-class stations) ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE", "Babylon-class Station",
                Map.of("pop", 5000000, "security", 20, "shieldHP", 10000));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "WORLD_CITY", "Gamma World City",
                Map.of("pop", 50000000, "security", 18, "tradeBonus", 30));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "STAR_FORGE", "Shipyard Complex",
                Map.of("output", 100000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "AI_CORE", "Station AI Core",
                Map.of("output", 50000, "aiBonus", 10));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "DREADNOUGHT", "Shadow Warship",
                Map.of("speed", 10000, "cargo", 5000, "weapons", "500d6", "shieldHP", 5000));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "WORMHOLE_DRIVE", "Wormhole Explorer",
                Map.of("speed", 50000, "cargo", 1000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "WORMHOLE_NETWORK", "Wormhole Network Node",
                Map.of("tradeBonus", 100));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "PLANETARY_COMPUTER", "Station Computer",
                Map.of("computePower", 1000000));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "PLANET_CRUSHER", "Planetary Defense Grid",
                Map.of("defense", 50, "shieldHP", 20000));
        putLore(TechLevel.TL9, ModuleCategory.COMMUNICATION, "QUANTUM_NET", "Quantum Communication Network",
                Map.of("range", 10000000));

        // ==========================================
        // CROSS-GALAXY CONVERSION CATEGORIES
        // ==========================================

        // --- TREASURE ---
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GOLD_COINS", "Earth Alliance Credits",
                Map.of("gpValue", 1000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GEMSTONE", "Minbari Crystal",
                Map.of("gpValue", 5000, "rarity", "Rare", "type", "Crystal"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 10000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GEMSTONE", "Vorlon Artifact Shard",
                Map.of("gpValue", 50000, "rarity", "Very Rare", "type", "Artifact"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "GOLD_COINS", "Galactic Credits",
                Map.of("gpValue", 100000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Shadow Relic",
                Map.of("gpValue", 500000, "rarity", "Legendary", "type", "Artifact"));

        // --- WEAPON ---
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Pulse Rifle",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 4));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "HANDGUN", "Pulse Pistol",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Laser Rifle",
                Map.of("damage", "5d6", "damageType", "Energy", "weight", 3));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "HANDGUN", "Laser Pistol",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 1));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "MAGIC_LONGSWORD", "Plasma Sword",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Shadow Weapon",
                Map.of("damage", "8d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "HANDGUN", "Vorlon Device",
                Map.of("damage", "6d6", "damageType", "Energy", "weight", 1));

        // --- ARMOR ---
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "MAGIC_PLATE", "Combat Armor",
                Map.of("ac", 18, "weight", 20, "type", "Ballistic"));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "MAGIC_PLATE", "Energy Shield Armor",
                Map.of("ac", 20, "weight", 10, "type", "Energy Shield", "shieldHP", 100));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "MAGIC_PLATE", "Shadow Shield",
                Map.of("ac", 25, "weight", 5, "type", "Force Field", "shieldHP", 500));

        // --- POTION ---
        putLore(TechLevel.TL7, ModuleCategory.POTION, "ANTIDOTE", "Medpac Antidote",
                Map.of("rarity", "Common", "gpValue", 500, "type", "Medical"));
        putLore(TechLevel.TL7, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Medpac",
                Map.of("rarity", "Common", "gpValue", 1000, "healing", "10d4+10"));
        putLore(TechLevel.TL8, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Regeneration Serum",
                Map.of("rarity", "Uncommon", "gpValue", 2000, "healing", "12d4+12"));
        putLore(TechLevel.TL9, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Minbari Healing Salve",
                Map.of("rarity", "Rare", "gpValue", 5000, "healing", "15d4+15"));

        // --- MAGIC_ITEM → Tech Items ---
        putLore(TechLevel.TL7, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Psi Module",
                Map.of("rarity", "Rare", "type", "Psi", "psiBonus", 3));
        putLore(TechLevel.TL8, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Hyperdrive Core",
                Map.of("rarity", "Very Rare", "type", "Hyperdrive", "speedBonus", 5));
        putLore(TechLevel.TL8, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM", "Teleporter Device",
                Map.of("rarity", "Rare", "type", "Teleporter", "range", 1000));
        putLore(TechLevel.TL9, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "AI Core",
                Map.of("rarity", "Legendary", "type", "AI", "intelligenceBonus", 5));

        // --- NPC ---
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Colonial Trader",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 12));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "WIZARD", "Science Officer",
                Map.of("role", "Scientist", "alignment", "Neutral", "level", 15));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "KNIGHT", "Earth Alliance Commander",
                Map.of("role", "Military", "alignment", "Lawful Good", "level", 18));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Centauri Noble",
                Map.of("role", "Noble", "alignment", "Chaotic Neutral", "level", 15));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Minbari Ambassador",
                Map.of("role", "Diplomat", "alignment", "Lawful Good", "level", 20));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "WIZARD", "Shadow Entity",
                Map.of("role", "Entity", "alignment", "Lawful Evil", "level", 20));

        // --- MONSTER ---
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "MIND_FLAYER", "Void Creature",
                Map.of("cr", 18, "hp", 500, "ac", 20, "type", "Alien", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Rii Predator",
                Map.of("cr", 25, "hp", 1000, "ac", 25, "type", "Alien", "size", "Gargantuan"));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Shadow Warship",
                Map.of("cr", 30, "hp", 5000, "ac", 30, "type", "Construct", "size", "Gargantuan", "shieldHP", 5000));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "LICH", "Vorlon",
                Map.of("cr", 28, "hp", 2000, "ac", 28, "type", "Entity", "size", "Medium", "shieldHP", 2000));

        // --- ADVENTURE_GEAR ---
        putLore(TechLevel.TL7, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Plasma Lamp",
                Map.of("cost", 50, "weight", 0.5, "duration", "100 hours"));
        putLore(TechLevel.TL7, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "Nutrient Paste",
                Map.of("cost", 10, "weight", 0.5, "duration", "1 day"));
        putLore(TechLevel.TL7, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Void Suit",
                Map.of("cost", 5000, "weight", 20, "duration", "72 hours"));
        putLore(TechLevel.TL8, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Environmental Suit",
                Map.of("cost", 10000, "weight", 15, "duration", "168 hours"));
        putLore(TechLevel.TL9, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Personal Force Field",
                Map.of("cost", 50000, "weight", 1, "duration", "Unlimited"));

        // --- DUNGEON_ROOM → Station Rooms ---
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Ship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Airlock",
                Map.of("size", "Small", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Station Command Ring",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Science Lab",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Medbay",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Babylon Station Command",
                Map.of("size", "Colossal", "light", "Bright"));

        // --- FURNITURE → Station Furniture ---
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "STONE_THRONE", "Command Console",
                Map.of("hp", 100, "ac", 20, "weight", 200));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Data Console",
                Map.of("hp", 50, "ac", 15, "weight", 100));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "STONE_THRONE", "Command Chair",
                Map.of("hp", 200, "ac", 25, "weight", 500));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "STONE_THRONE", "Station Commander's Chair",
                Map.of("hp", 500, "ac", 30, "weight", 1000));

        // --- TRAP → Security ---
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "PRESSURE_PLATE", "Laser Grid",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "PRESSURE_PLATE", "Containment Field",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Energy"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "PRESSURE_PLATE", "EMP Burst",
                Map.of("dc", 20, "damage", "10d6", "damageType", "Energy"));

        // --- DOOR → Station Doors ---
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "PORTCULLIS", "Blast Door",
                Map.of("hp", 200, "ac", 25, "classDC", 25));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "PORTCULLIS", "Force Field Door",
                Map.of("hp", 500, "ac", 30, "classDC", 30));
        putLore(TechLevel.TL9, ModuleCategory.DOOR, "PORTCULLIS", "Energy Barrier",
                Map.of("hp", 1000, "ac", 35, "classDC", 35));

        // --- CHEST → Containers ---
        putLore(TechLevel.TL7, ModuleCategory.CHEST, "TREASURE_CHEST", "Cargo Container",
                Map.of("hp", 100, "ac", 20, "capacity", "Huge"));
        putLore(TechLevel.TL8, ModuleCategory.CHEST, "TREASURE_CHEST", "Secure Locker",
                Map.of("hp", 500, "ac", 25, "capacity", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.CHEST, "TREASURE_CHEST", "Quantum Storage",
                Map.of("hp", 1000, "ac", 30, "capacity", "Dimensional"));

        // --- SPELL → Tech Abilities ---
        putLore(TechLevel.TL7, ModuleCategory.SPELL, "TELEPORT", "Psi Powers",
                Map.of("level", 5, "school", "Psi", "range", "100ft"));
        putLore(TechLevel.TL8, ModuleCategory.SPELL, "TELEPORT", "Hypernet Access",
                Map.of("level", 5, "school", "Hypernet", "range", "1000ft"));
        putLore(TechLevel.TL9, ModuleCategory.SPELL, "TELEPORT", "AI Override",
                Map.of("level", 7, "school", "AI", "range", "10000ft"));

        // === ANIMAL mappings (TL7-TL9, B5 style) ===
        // Livestock
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "COW", "Minbari Grazing Beast",
                Map.of("milkYield", 25, "meatYield", 220, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "PIG", "Centauri Swine",
                Map.of("meatYield", 90, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "SHEEP", "Minbari Wool Beast",
                Map.of("milkYield", 3, "woolYield", 4, "meatYield", 35, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "HORSE", "Narn Draft Horse",
                Map.of("carryingCapacity", 120, "meatYield", 170, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "DOG", "Minbari Watchdog",
                Map.of("meatYield", 18, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "COW", "Station Bovine",
                Map.of("milkYield", 30, "meatYield", 250, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "HORSE", "Hover Mount",
                Map.of("carryingCapacity", 150, "meatYield", 180, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "DOG", "Security Hound",
                Map.of("type", "Bio-Engineered", "size", "Medium"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Genetic Grazing Beast",
                Map.of("milkYield", 50, "meatYield", 400, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "HORSE", "Anti-Gravity Steed",
                Map.of("carryingCapacity", 200, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "DOG", "Combat Drone Hound",
                Map.of("type", "Construct", "size", "Medium"));
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
