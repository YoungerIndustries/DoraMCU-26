package com.yisy.modules.doramcu.dnd5e.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * D&D 5e tech level adapter.
 * <p>
 * Maps generic tech-level modules to D&D 5e lore-appropriate names and stats.
 * D&D supports TL0–TL4 (Prehistoric through Renaissance).
 * </p>
 */
@Component
public class Dnd5eTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL0: Prehistoric ===
        putLore(TechLevel.TL0, ModuleCategory.SETTLEMENT, "CAVE_DWELLING", "Cave Dwelling",
                Map.of("hp", 10, "capacity", 5));
        putLore(TechLevel.TL0, ModuleCategory.SETTLEMENT, "HUT_VILLAGE", "Hut Village",
                Map.of("hp", 20, "capacity", 20));
        putLore(TechLevel.TL0, ModuleCategory.INDUSTRY, "HUNTING_GROUND", "Hunting Ground",
                Map.of("output", 10, "food", 5));
        putLore(TechLevel.TL0, ModuleCategory.INDUSTRY, "GATHERING_SITE", "Gathering Site",
                Map.of("output", 8, "herbs", 3));
        putLore(TechLevel.TL0, ModuleCategory.VEHICLE, "RAFT", "Wooden Raft",
                Map.of("speed", 1, "capacity", 3));
        putLore(TechLevel.TL0, ModuleCategory.INFRASTRUCTURE, "FIRE_PIT", "Campfire",
                Map.of("light", 1, "warmth", 1));
        putLore(TechLevel.TL0, ModuleCategory.DEFENSE, "PALISADE", "Wooden Palisade",
                Map.of("ac", 10, "hp", 30));

        // === TL1: Ancient ===
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "STONE_HUT_VILLAGE", "Stone Hut Village",
                Map.of("hp", 50, "capacity", 50));
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "TEMPLE_COMPOUND", "Temple of the Gods",
                Map.of("hp", 100, "capacity", 30, "divineBonus", 2));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "FARM", "Open-Field Farm",
                Map.of("output", 25, "food", 20));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "BRONZE_SMITHY", "Bronze Smithy",
                Map.of("output", 15, "weapons", 5));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "CLAY_POTTERY", "Clay Pottery Workshop",
                Map.of("output", 10, "craftDC", 10));
        putLore(TechLevel.TL1, ModuleCategory.VEHICLE, "OX_CART", "Ox-Drawn Cart",
                Map.of("speed", 3, "capacity", 5));
        putLore(TechLevel.TL1, ModuleCategory.VEHICLE, "ROWBOAT", "Rowboat",
                Map.of("speed", 5, "capacity", 4));
        putLore(TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "STONE_ROAD", "Stone Road",
                Map.of("travelBonus", 1));
        putLore(TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "WELL", "Stone Well",
                Map.of("water", 1));
        putLore(TechLevel.TL1, ModuleCategory.DEFENSE, "STONE_WALL", "Stone Wall",
                Map.of("ac", 12, "hp", 100));
        putLore(TechLevel.TL1, ModuleCategory.DEFENSE, "WATCH_TOWER", "Watch Tower",
                Map.of("ac", 10, "hp", 60, "visionRange", 2));
        putLore(TechLevel.TL1, ModuleCategory.COMMUNICATION, "SMOKE_SIGNAL", "Smoke Signal Tower",
                Map.of("range", 5));

        // === TL2: Medieval ===
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE", "Stone Castle",
                Map.of("hp", 200, "capacity", 100, "garrison", 20));
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "MARKET_TOWN", "Market Town",
                Map.of("hp", 150, "capacity", 200, "tradeBonus", 3));
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "MONASTERY", "Monastery",
                Map.of("hp", 120, "capacity", 50, "divineBonus", 3));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "BLACKSMITH_FORGE", "Blacksmith Forge",
                Map.of("output", 20, "weapons", 10, "armorDC", 13));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "WATER_MILL", "Water Mill",
                Map.of("output", 30, "grain", 25));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "WOOL_WORKSHOP", "Wool Workshop",
                Map.of("output", 15, "textiles", 10));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "QUARRY", "Stone Quarry",
                Map.of("output", 25, "stone", 20));
        putLore(TechLevel.TL2, ModuleCategory.VEHICLE, "HORSE_DRAWN_CART", "Horse-Drawn Cart",
                Map.of("speed", 5, "capacity", 8));
        putLore(TechLevel.TL2, ModuleCategory.VEHICLE, "CARGO_SHIP", "Sailing Cog",
                Map.of("speed", 8, "capacity", 20));
        putLore(TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "STONE_BRIDGE", "Stone Bridge",
                Map.of("travelBonus", 2));
        putLore(TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "CATHEDRAL", "Cathedral",
                Map.of("hp", 300, "divineBonus", 5));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "CASTLE_WALL", "Castle Wall",
                Map.of("ac", 15, "hp", 250));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "BARRACKS", "Barracks",
                Map.of("hp", 100, "garrison", 30));
        putLore(TechLevel.TL2, ModuleCategory.COMMUNICATION, "MOUNTED_MESSENGER", "Mounted Messenger Station",
                Map.of("range", 20, "speedBonus", 2));

        // === TL3: Renaissance ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "HARBOR_CITY", "Harbor City",
                Map.of("hp", 300, "capacity", 500, "tradeBonus", 5));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "UNIVERSITY", "Arcane University",
                Map.of("hp", 200, "capacity", 100, "spellBonus", 3));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "GUNPOWDER_WORKS", "Alchemist's Fireworks",
                Map.of("output", 20, "explosives", 10));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "PRINTING_PRESS", "Scriptorium Press",
                Map.of("output", 30, "books", 25));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "SHIPYARD", "Shipyard",
                Map.of("output", 40, "ships", 5));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "GALLEON", "Galleon",
                Map.of("speed", 12, "capacity", 50, "cannonDamage", "2d6"));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "STAGE_COACH", "Stage Coach",
                Map.of("speed", 6, "capacity", 10));
        putLore(TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "CANAL", "Canal",
                Map.of("tradeBonus", 3));
        putLore(TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "LANTERN_POST", "Lantern Post Network",
                Map.of("light", 3));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "ARTILLERY_FORT", "Artillery Fort",
                Map.of("ac", 18, "hp", 300, "cannonDamage", "4d6"));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "MOAT", "Defensive Moat",
                Map.of("ac", 10, "hp", 100));
        putLore(TechLevel.TL3, ModuleCategory.COMMUNICATION, "POST_OFFICE", "Post Office",
                Map.of("range", 50));

        // === TL4: Industrial (D&D upper limit — rare, mostly for Eberron-style settings) ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY", "Industrial City",
                Map.of("hp", 500, "capacity", 2000, "tradeBonus", 8));
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "RAILWAY_STATION", "Railway Station",
                Map.of("hp", 200, "capacity", 500));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "STEAM_FACTORY", "Steam Factory",
                Map.of("output", 100, "goods", 80));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "COAL_MINE", "Coal Mine",
                Map.of("output", 80, "fuel", 60));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "STEEL_MILL", "Steel Mill",
                Map.of("output", 120, "steel", 100));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "TEXTILE_MILL", "Textile Mill",
                Map.of("output", 60, "textiles", 50));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "STEAM_LOCOMOTIVE", "Steam Locomotive",
                Map.of("speed", 20, "capacity", 50));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "STEAMSHIP", "Steamship",
                Map.of("speed", 15, "capacity", 100));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "BUS_DEPOT", "Bus Depot",
                Map.of("speed", 10, "capacity", 30));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "RAILROAD", "Railroad",
                Map.of("travelBonus", 5));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "TELEGRAPH_STATION", "Telegraph Station",
                Map.of("range", 100));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "GAS_LAMP_GRID", "Gas Lamp Grid",
                Map.of("light", 5));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "IRONCLAD_FORT", "Ironclad Fort",
                Map.of("ac", 20, "hp", 400));
        putLore(TechLevel.TL4, ModuleCategory.COMMUNICATION, "TELEGRAPH_NETWORK", "Telegraph Network",
                Map.of("range", 500));

        // ==========================================
        // DUNGEON OBJECTS — TL0: Prehistoric
        // ==========================================

        // DUNGEON_ROOM
        putLore(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "CAVE_ENTRANCE", "Cave Entrance",
                Map.of("size", "Medium", "light", "Dim"));
        putLore(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "CAVE_CHAMBER", "Cave Chamber",
                Map.of("size", "Large", "light", "Dark"));
        putLore(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "UNDERGROUND_POOL", "Underground Pool",
                Map.of("size", "Medium", "water", "Fresh"));

        // FURNITURE
        putLore(TechLevel.TL0, ModuleCategory.FURNITURE, "STONE_ALTAR", "Stone Altar",
                Map.of("hp", 20, "ac", 5, "ritualDC", 10));
        putLore(TechLevel.TL0, ModuleCategory.FURNITURE, "FIRE_PIT", "Fire Pit",
                Map.of("hp", 5, "light", "10ft", "warmthRadius", "10ft"));
        putLore(TechLevel.TL0, ModuleCategory.FURNITURE, "FUR_BED", "Fur Bed",
                Map.of("hp", 2, "restBonus", 1));

        // TREASURE
        putLore(TechLevel.TL0, ModuleCategory.TREASURE, "BONE_NECKLACE", "Bone Necklace",
                Map.of("gpValue", 1, "rarity", "Common", "type", "Wondrous Item"));
        putLore(TechLevel.TL0, ModuleCategory.TREASURE, "STONE_AMULET", "Stone Amulet",
                Map.of("gpValue", 2, "rarity", "Common", "type", "Wondrous Item"));

        // TRAP
        putLore(TechLevel.TL0, ModuleCategory.TRAP, "SPIKE_PIT", "Spike Pit",
                Map.of("dc", 10, "damage", "1d8", "damageType", "Piercing", "size", "5ft"));
        putLore(TechLevel.TL0, ModuleCategory.TRAP, "FALLING_ROCK", "Falling Rock Trap",
                Map.of("dc", 8, "damage", "2d6", "damageType", "Bludgeoning", "size", "10ft"));

        // DOOR
        putLore(TechLevel.TL0, ModuleCategory.DOOR, "WOODEN_DOOR", "Wooden Door",
                Map.of("hp", 15, "ac", 10, "classDC", 10, "material", "Wood"));

        // CHEST
        putLore(TechLevel.TL0, ModuleCategory.CHEST, "WOODEN_BOX", "Wooden Box",
                Map.of("hp", 5, "ac", 5, "capacity", "Small"));

        // MONSTER
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_SPIDER", "Giant Spider",
                Map.of("cr", 0.5, "hp", 26, "ac", 12, "type", "Beast", "size", "Large",
                        "speed", 30, "climb", 30, "damageImmunities", "Poison"));
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_WORM", "Giant Worm",
                Map.of("cr", 1, "hp", 45, "ac", 12, "type", "Beast", "size", "Huge",
                        "speed", 40, "burrow", 40, "blindsight", 60));
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "KOBOLD", "Kobold",
                Map.of("cr", 0.125, "hp", 22, "ac", 12, "type", "Humanoid", "size", "Small",
                        "speed", 30, "burrow", 10, "sunsetSunrise", true));

        // WEAPON
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_SPEAR", "Stone Spear",
                Map.of("damage", "1d6", "damageType", "Piercing", "weight", 6,
                        "properties", "Thrown(20/60), Versatile(1d8)"));
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_AXE", "Stone Axe",
                Map.of("damage", "1d6", "damageType", "Slashing", "weight", 4,
                        "properties", "Thrown(20/60), Versatile(1d8)"));
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "BOW", "Shortbow",
                Map.of("damage", "1d8", "damageType", "Piercing", "weight", 2,
                        "properties", "Ammunition(80/320), Two-Handed"));

        // ARMOR
        putLore(TechLevel.TL0, ModuleCategory.ARMOR, "LEATHER_ARMOR", "Leather Armor",
                Map.of("ac", 11, "weight", 10, "type", "Light", "stealthDisadvantage", false));
        putLore(TechLevel.TL0, ModuleCategory.ARMOR, "HIDE_ARMOR", "Hide Armor",
                Map.of("ac", 12, "weight", 12, "type", "Light", "stealthDisadvantage", false));

        // ADVENTURE_GEAR
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Torch",
                Map.of("cost", 0, "weight", 1, "duration", "6 hours", "light", "Bright 20ft, Dim 20ft"));
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "ROPE", "Hemp Rope",
                Map.of("cost", 1, "weight", 10, "length", "50ft", "hp", 5, "breakDC", 12));
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "Rations",
                Map.of("cost", 0, "weight", 2, "duration", "1 day"));
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "WATER_SKIN", "Water Skin",
                Map.of("cost", 2, "weight", 5, "capacity", "1 gallon"));

        // ==========================================
        // DUNGEON OBJECTS — TL1: Ancient
        // ==========================================

        // DUNGEON_ROOM
        putLore(TechLevel.TL1, ModuleCategory.DUNGEON_ROOM, "TEMPLE_HALL", "Temple Hall",
                Map.of("size", "Large", "light", "Dim", "divineAura", true));
        putLore(TechLevel.TL1, ModuleCategory.DUNGEON_ROOM, "TOMB_CHAMBER", "Tomb Chamber",
                Map.of("size", "Large", "light", "Dark", "cursed", true));
        putLore(TechLevel.TL1, ModuleCategory.DUNGEON_ROOM, "TREASURE_VAULT", "Treasure Vault",
                Map.of("size", "Medium", "light", "Dim", "trapped", true));

        // FURNITURE
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "STONE_THRONE", "Stone Throne",
                Map.of("hp", 50, "ac", 5, "authority", true));
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "BRONZE_BRAZIER", "Bronze Brazier",
                Map.of("hp", 10, "light", "Bright 15ft, Dim 15ft", "weight", 15));
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Wooden Table",
                Map.of("hp", 15, "ac", 5, "weight", 20));
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "WOODEN_CHAIR", "Wooden Chair",
                Map.of("hp", 5, "ac", 5, "weight", 5));

        // TREASURE
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "GOLD_COINS", "Gold Coins",
                Map.of("gpValue", 100, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "GEMSTONE", "Gemstone",
                Map.of("gpValue", 50, "rarity", "Uncommon", "type", "Gem"));
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "JEWELRY", "Jewelry",
                Map.of("gpValue", 75, "rarity", "Uncommon", "type", "Wondrous Item"));

        // TRAP
        putLore(TechLevel.TL1, ModuleCategory.TRAP, "POISON_DART", "Poison Dart Trap",
                Map.of("dc", 13, "damage", "2d4", "damageType", "Poison", "save", "Dex"));
        putLore(TechLevel.TL1, ModuleCategory.TRAP, "PRESSURE_PLATE", "Pressure Plate Trap",
                Map.of("dc", 15, "damage", "1d10", "damageType", "Piercing", "save", "Dex"));
        putLore(TechLevel.TL1, ModuleCategory.TRAP, "GAS_VENT", "Gas Vent Trap",
                Map.of("dc", 12, "damage", "3d6", "damageType", "Poison", "save", "Con"));

        // DOOR
        putLore(TechLevel.TL1, ModuleCategory.DOOR, "IRON_DOOR", "Iron Door",
                Map.of("hp", 45, "ac", 15, "classDC", 15, "material", "Iron"));
        putLore(TechLevel.TL1, ModuleCategory.DOOR, "SECRET_DOOR", "Secret Door",
                Map.of("hp", 15, "ac", 10, "classDC", 15, "hiddenDC", 15, "material", "Wood"));

        // CHEST
        putLore(TechLevel.TL1, ModuleCategory.CHEST, "IRON_BANDED_CHEST", "Iron-Banded Chest",
                Map.of("hp", 15, "ac", 10, "capacity", "Medium", "locked", true));
        putLore(TechLevel.TL1, ModuleCategory.CHEST, "STRONGBOX", "Strongbox",
                Map.of("hp", 20, "ac", 12, "capacity", "Small", "locked", true, "thiefDC", 15));

        // MONSTER
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "GOBLIN", "Goblin",
                Map.of("cr", 0.25, "hp", 7, "ac", 15, "type", "Humanoid", "size", "Small",
                        "speed", 30, "darkvision", 60, "surpriseBonus", 2));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "SKELETON", "Skeleton",
                Map.of("cr", 1, "hp", 13, "ac", 13, "type", "Undead", "size", "Medium",
                        "speed", 30, "damageImmunities", "Poison", "conditionImmunities", "Exhaustion, Poison, Charmed, Frightened"));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "ZOMBIE", "Zombie",
                Map.of("cr", 0.125, "hp", 22, "ac", 8, "type", "Undead", "size", "Medium",
                        "speed", 20, "damageImmunities", "Poison", "vulnerable", "Bludgeoning"));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "GNOLL", "Gnoll",
                Map.of("cr", 1, "hp", 22, "ac", 12, "type", "Humanoid", "size", "Medium",
                        "speed", 30, "darkvision", 60, "packTactics", true));

        // MAGIC_ITEM
        putLore(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM", "Wand of Wisdom",
                Map.of("rarity", "Uncommon", "type", "Wand", "charges", 7, "wisdomScore", 10));
        putLore(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "POTION_OF_HEALING", "Potion of Healing",
                Map.of("rarity", "Common", "type", "Potion", "healing", "2d4+2", "actions", "Action"));

        // NPC
        putLore(TechLevel.TL1, ModuleCategory.NPC, "INNKEEPER", "Innkeeper",
                Map.of("role", "Service", "alignment", "Neutral Good", "level", 1));
        putLore(TechLevel.TL1, ModuleCategory.NPC, "MERCHANT", "Merchant",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 2, "gpValue", 500));
        putLore(TechLevel.TL1, ModuleCategory.NPC, "GUARD_CAPTAIN", "Guard Captain",
                Map.of("role", "Military", "alignment", "Lawful Neutral", "level", 3, "followers", 10));

        // SPELL
        putLore(TechLevel.TL1, ModuleCategory.SPELL, "MAGIC_MISSILE", "Magic Missile",
                Map.of("level", 1, "school", "Evocation", "range", "120ft", "duration", "Instantaneous"));
        putLore(TechLevel.TL1, ModuleCategory.SPELL, "SHIELD", "Shield",
                Map.of("level", 1, "school", "Abjuration", "range", "Self", "duration", "1 round", "acBonus", 5));
        putLore(TechLevel.TL1, ModuleCategory.SPELL, "BURNING_HANDS", "Burning Hands",
                Map.of("level", 1, "school", "Evocation", "range", "Self(15ft cone)", "duration", "Instantaneous",
                        "damage", "3d6", "damageType", "Fire", "save", "Dex"));

        // POTION
        putLore(TechLevel.TL1, ModuleCategory.POTION, "ANTIDOTE", "Antidote",
                Map.of("rarity", "Common", "gpValue", 50, "type", "Consumable", "cures", "Poison"));
        putLore(TechLevel.TL1, ModuleCategory.POTION, "POISON_VIAL", "Poison Vial",
                Map.of("dc", 10, "damage", "2d4", "damageType", "Poison", "save", "Con", "type", "Contact"));

        // WEAPON
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "SHORTSWORD", "Shortsword",
                Map.of("damage", "1d6", "damageType", "Piercing", "weight", 2,
                        "properties", "Finesse, Light"));
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "LONGSWORD", "Longsword",
                Map.of("damage", "1d8", "damageType", "Slashing", "weight", 3,
                        "properties", "Versatile(1d10)"));
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "LONGBOW", "Longbow",
                Map.of("damage", "1d8", "damageType", "Piercing", "weight", 2,
                        "properties", "Ammunition(150/600), Heavy, Two-Handed"));

        // ARMOR
        putLore(TechLevel.TL1, ModuleCategory.ARMOR, "CHAIN_MAIL", "Chain Mail",
                Map.of("ac", 16, "weight", 55, "type", "Heavy", "stealthDisadvantage", true, "strengthRequired", 13));
        putLore(TechLevel.TL1, ModuleCategory.ARMOR, "SHIELD", "Shield",
                Map.of("acBonus", 2, "weight", 6, "type", "Shield"));

        // ADVENTURE_GEAR
        putLore(TechLevel.TL1, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Backpack",
                Map.of("cost", 2, "weight", 5, "capacity", "30-60 lbs"));
        putLore(TechLevel.TL1, ModuleCategory.ADVENTURE_GEAR, "HAMMER", "Hammer",
                Map.of("cost", 1, "weight", 3, "damage", "1d8", "damageType", "Bludgeoning",
                        "properties", "Light, Thrown(20/60)"));
        putLore(TechLevel.TL1, ModuleCategory.ADVENTURE_GEAR, "CLIMBERS_KIT", "Climber's Kit",
                Map.of("cost", 25, "weight", 12, "checkBonus", "Proficiency"));

        // ==========================================
        // DUNGEON OBJECTS — TL2: Medieval
        // ==========================================

        // DUNGEON_ROOM
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Castle Great Hall",
                Map.of("size", "Huge", "light", "Dim", "banners", true));
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Dungeon Cell",
                Map.of("size", "Small", "light", "Dark", "bars", true));
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "ARMORY", "Armory",
                Map.of("size", "Medium", "light", "Dim", "weapons", true));
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "BOSS_CHAMBER", "Boss Chamber",
                Map.of("size", "Huge", "light", "Dim", "traps", true, "bossEncounter", true));

        // FURNITURE
        putLore(TechLevel.TL2, ModuleCategory.FURNITURE, "GOLDEN_THRONE", "Golden Throne",
                Map.of("hp", 30, "ac", 5, "gpValue", 200, "authority", true));
        putLore(TechLevel.TL2, ModuleCategory.FURNITURE, "TAPESTRY", "Tapestry",
                Map.of("hp", 2, "ac", 5, "gpValue", 20, "weight", 5));
        putLore(TechLevel.TL2, ModuleCategory.FURNITURE, "IRON_BED", "Iron Bed",
                Map.of("hp", 10, "ac", 5, "weight", 15));
        putLore(TechLevel.TL2, ModuleCategory.FURNITURE, "CANDLESTICK", "Candlestick",
                Map.of("hp", 2, "ac", 5, "weight", 2, "light", "Bright 5ft, Dim 5ft"));

        // TREASURE
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "GOLD_HOARD", "Gold Hoard",
                Map.of("gpValue", 500, "rarity", "Rare", "type", "Currency", "guarded", true));
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "DIAMOND", "Diamond",
                Map.of("gpValue", 200, "rarity", "Rare", "type", "Gem"));
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "CROWN_JEWELS", "Crown Jewels",
                Map.of("gpValue", 1000, "rarity", "Very Rare", "type", "Wondrous Item", "cursed", false));

        // TRAP
        putLore(TechLevel.TL2, ModuleCategory.TRAP, "FALLING_ROCKS", "Falling Rocks Trap",
                Map.of("dc", 13, "damage", "3d8", "damageType", "Bludgeoning", "save", "Dex"));
        putLore(TechLevel.TL2, ModuleCategory.TRAP, "ARROW_TRAP", "Arrow Trap",
                Map.of("dc", 14, "damage", "2d8", "damageType", "Piercing", "save", "Dex"));
        putLore(TechLevel.TL2, ModuleCategory.TRAP, "MAGIC_TRAP", "Magic Trap",
                Map.of("dc", 15, "damage", "4d6", "damageType", "Force", "save", "Dex", "arcane", true));

        // DOOR
        putLore(TechLevel.TL2, ModuleCategory.DOOR, "PORTCULLIS", "Portcullis",
                Map.of("hp", 60, "ac", 20, "classDC", 20, "material", "Iron"));
        putLore(TechLevel.TL2, ModuleCategory.DOOR, "MAGIC_DOOR", "Magic Door",
                Map.of("hp", 50, "ac", 15, "classDC", 15, "material", "Wood", "warded", true));

        // CHEST
        putLore(TechLevel.TL2, ModuleCategory.CHEST, "TREASURE_CHEST", "Treasure Chest",
                Map.of("hp", 20, "ac", 10, "capacity", "Large", "locked", true, "ornate", true));

        // MONSTER
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "ORC", "Orc",
                Map.of("cr", 2, "hp", 15, "ac", 13, "type", "Humanoid", "size", "Medium",
                        "speed", 30, "darkvision", 60, "menacing", true));
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "OGRE", "Ogre",
                Map.of("cr", 2, "hp", 59, "ac", 11, "type", "Giant", "size", "Huge",
                        "speed", 40, "darkvision", 60, "powerfulBuild", true));
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "WOLF", "Giant Wolf",
                Map.of("cr", 1, "hp", 22, "ac", 13, "type", "Beast", "size", "Large",
                        "speed", 40, "packTactics", true, "keenHearing", true, "keenSmell", true));
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "BANSHEE", "Banshee",
                Map.of("cr", 3, "hp", 45, "ac", 12, "type", "Undead", "size", "Medium",
                        "speed", 0, "fly", 30, "frightfulScream", true, "wail", "6d6 Necrotic"));

        // MAGIC_ITEM
        putLore(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "RING_OF_PROTECTION", "Ring of Protection",
                Map.of("rarity", "Rare", "type", "Ring", "acBonus", 1, "saveBonus", 1, "attuned", true));
        putLore(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "STAFF_OF_HEALING", "Staff of Healing",
                Map.of("rarity", "Rare", "type", "Staff", "charges", 10, "healingPerCharge", "1d4", "attuned", true));
        putLore(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "AMULET_OF_HEALTH", "Amulet of Health",
                Map.of("rarity", "Rare", "type", "Amulet", "constitutionScore", 19, "attuned", true));

        // NPC
        putLore(TechLevel.TL2, ModuleCategory.NPC, "KNIGHT", "Knight",
                Map.of("role", "Military", "alignment", "Lawful Good", "level", 5, "mount", "Warhorse"));
        putLore(TechLevel.TL2, ModuleCategory.NPC, "WIZARD", "Wizard",
                Map.of("role", "Spellcaster", "alignment", "Neutral", "level", 7, "spellSlots", 10));
        putLore(TechLevel.TL2, ModuleCategory.NPC, "BLACKSMITH", "Blacksmith",
                Map.of("role", "Craftsman", "alignment", "Neutral Good", "level", 3, "craftDC", 13));

        // SPELL
        putLore(TechLevel.TL2, ModuleCategory.SPELL, "FIREBALL", "Fireball",
                Map.of("level", 3, "school", "Evocation", "range", "150ft", "duration", "Instantaneous",
                        "damage", "8d6", "damageType", "Fire", "save", "Dex"));
        putLore(TechLevel.TL2, ModuleCategory.SPELL, "LIGHTNING_BOLT", "Lightning Bolt",
                Map.of("level", 3, "school", "Evocation", "range", "Self(100ft line)", "duration", "Instantaneous",
                        "damage", "8d6", "damageType", "Lightning", "save", "Dex"));
        putLore(TechLevel.TL2, ModuleCategory.SPELL, "INVISIBILITY", "Invisibility",
                Map.of("level", 2, "school", "Illusion", "range", "Touch", "duration", "1 hour", "concentration", true));

        // POTION
        putLore(TechLevel.TL2, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Greater Potion of Healing",
                Map.of("rarity", "Uncommon", "gpValue", 150, "type", "Potion", "healing", "4d4+4", "actions", "Action"));
        putLore(TechLevel.TL2, ModuleCategory.POTION, "POISON_VIAL_GREATER", "Greater Poison Vial",
                Map.of("dc", 12, "damage", "4d4", "damageType", "Poison", "save", "Con", "type", "Contact"));

        // WEAPON
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "WARHAMMER", "Warhammer",
                Map.of("damage", "1d8", "damageType", "Bludgeoning", "weight", 2,
                        "properties", "Versatile(1d10)"));
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "HALBERD", "Halberd",
                Map.of("damage", "1d10", "damageType", "Slashing", "weight", 6,
                        "properties", "Heavy, Reach, Two-Handed"));
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "CROSSBOW", "Heavy Crossbow",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 18,
                        "properties", "Ammunition(100/400), Heavy, Loading, Two-Handed"));

        // ARMOR
        putLore(TechLevel.TL2, ModuleCategory.ARMOR, "PLATE_ARMOR", "Plate Armor",
                Map.of("ac", 18, "weight", 65, "type", "Heavy", "stealthDisadvantage", true, "strengthRequired", 15));
        putLore(TechLevel.TL2, ModuleCategory.ARMOR, "CHAIN_SHIRT", "Chain Shirt",
                Map.of("ac", 13, "weight", 20, "type", "Medium", "maxDexBonus", 2, "strengthRequired", 13));

        // ADVENTURE_GEAR
        putLore(TechLevel.TL2, ModuleCategory.ADVENTURE_GEAR, "ALCHEMISTS_FIRE", "Alchemist's Fire",
                Map.of("cost", 50, "weight", 1, "damage", "1d8", "damageType", "Fire", "duration", "1 round"));
        putLore(TechLevel.TL2, ModuleCategory.ADVENTURE_GEAR, "HOLY_WATER", "Holy Water",
                Map.of("cost", 25, "weight", 0.5, "damage", "2d6", "damageType", "Radiant", "target", "Undead/Fiends"));

        // ==========================================
        // DUNGEON OBJECTS — TL3: Renaissance
        // ==========================================

        // DUNGEON_ROOM
        putLore(TechLevel.TL3, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Arcane Laboratory",
                Map.of("size", "Large", "light", "Dim", "arcaneAura", true, "components", true));
        putLore(TechLevel.TL3, ModuleCategory.DUNGEON_ROOM, "LIBRARY", "Ancient Library",
                Map.of("size", "Huge", "light", "Dim", "books", 1000, "scrolls", 500));
        putLore(TechLevel.TL3, ModuleCategory.DUNGEON_ROOM, "OBSERVATORY", "Observatory",
                Map.of("size", "Medium", "light", "Dark", "astronomy", true, "telescope", true));

        // FURNITURE
        putLore(TechLevel.TL3, ModuleCategory.FURNITURE, "BOOKSHELF", "Bookshelf",
                Map.of("hp", 10, "ac", 5, "weight", 50, "capacity", "50 books"));
        putLore(TechLevel.TL3, ModuleCategory.FURNITURE, "WRITING_DESK", "Writing Desk",
                Map.of("hp", 10, "ac", 5, "weight", 20, "ink", true, "parchment", true));
        putLore(TechLevel.TL3, ModuleCategory.FURNITURE, "ASTROLABE", "Astrolabe",
                Map.of("hp", 5, "ac", 5, "weight", 5, "gpValue", 50, "navigationBonus", true));

        // TREASURE
        putLore(TechLevel.TL3, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Ancient Artifact",
                Map.of("gpValue", 500, "rarity", "Very Rare", "type", "Wondrous Item", "cursed", false));
        putLore(TechLevel.TL3, ModuleCategory.TREASURE, "SPELL_SCROLL", "Spell Scroll",
                Map.of("gpValue", 100, "rarity", "Uncommon", "type", "Scroll", "weight", 0.1));

        // TRAP
        putLore(TechLevel.TL3, ModuleCategory.TRAP, "FIRE_TRAP", "Fire Trap",
                Map.of("dc", 14, "damage", "4d6", "damageType", "Fire", "save", "Dex"));
        putLore(TechLevel.TL3, ModuleCategory.TRAP, "LIGHTNING_TRAP", "Lightning Trap",
                Map.of("dc", 15, "damage", "5d6", "damageType", "Lightning", "save", "Dex"));

        // MONSTER
        putLore(TechLevel.TL3, ModuleCategory.MONSTER, "MIND_FLAYER", "Mind Flayer",
                Map.of("cr", 10, "hp", 130, "ac", 13, "type", "Aberration", "size", "Medium",
                        "speed", 30, "swim", 30, "brainBite", "2d8+4 Necrotic", "telepathy", 60));
        putLore(TechLevel.TL3, ModuleCategory.MONSTER, "DRAGON_WYRM", "Young Dragon",
                Map.of("cr", 6, "hp", 136, "ac", 17, "type", "Dragon", "size", "Large",
                        "speed", 40, "fly", 80, "breathWeapon", "5d8 Fire", "legendaryActions", 3));

        // MAGIC_ITEM
        putLore(TechLevel.TL3, ModuleCategory.MAGIC_ITEM, "WINGED_BOOTS", "Winged Boots",
                Map.of("rarity", "Rare", "type", "Boots", "flySpeed", 60, "attuned", true));
        putLore(TechLevel.TL3, ModuleCategory.MAGIC_ITEM, "BAG_OF_HOLDING", "Bag of Holding",
                Map.of("rarity", "Uncommon", "type", "Bag", "capacity", "640 lbs", "volume", "64 cubic ft"));

        // NPC
        putLore(TechLevel.TL3, ModuleCategory.NPC, "ALCHEMIST", "Alchemist",
                Map.of("role", "Craftsman", "alignment", "Neutral", "level", 5, "potionDC", 15));
        putLore(TechLevel.TL3, ModuleCategory.NPC, "NOBLE", "Noble",
                Map.of("role", "Leader", "alignment", "Lawful Neutral", "level", 4, "influence", 10));

        // SPELL
        putLore(TechLevel.TL3, ModuleCategory.SPELL, "COUNTERSPELL", "Counterspell",
                Map.of("level", 3, "school", "Abjuration", "range", "60ft", "duration", "Instantaneous"));
        putLore(TechLevel.TL3, ModuleCategory.SPELL, "TELEPORT", "Teleport",
                Map.of("level", 5, "school", "Conjuration", "range", "10ft", "duration", "Instantaneous",
                        "familiarityCheck", true));

        // WEAPON
        putLore(TechLevel.TL3, ModuleCategory.WEAPON, "HANDGUN", "Handgun",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 2,
                        "properties", "Ammunition(30/120), Loading, Two-Handed"));

        // ARMOR
        putLore(TechLevel.TL3, ModuleCategory.ARMOR, "BRONZE_PLATE", "Bronze Plate",
                Map.of("ac", 16, "weight", 60, "type", "Heavy", "stealthDisadvantage", true, "strengthRequired", 15));

        // ADVENTURE_GEAR
        putLore(TechLevel.TL3, ModuleCategory.ADVENTURE_GEAR, "POISON_TIP", "Poison Tip",
                Map.of("cost", 10, "weight", 0.1, "dc", 10, "damage", "1d4", "damageType", "Poison", "save", "Con"));

        // ==========================================
        // DUNGEON OBJECTS — TL4: Industrial (Eberron-style)
        // ==========================================

        // DUNGEON_ROOM
        putLore(TechLevel.TL4, ModuleCategory.DUNGEON_ROOM, "STEAM_FACTORY", "Steam Factory",
                Map.of("size", "Huge", "light", "Dim", "machinery", true, "noise", true));
        putLore(TechLevel.TL4, ModuleCategory.DUNGEON_ROOM, "RAILWAY_TUNNEL", "Railway Tunnel",
                Map.of("size", "Large", "light", "Dark", "tracks", true, "trains", true));

        // FURNITURE
        putLore(TechLevel.TL4, ModuleCategory.FURNITURE, "TELEGRAPH", "Telegraph Machine",
                Map.of("hp", 15, "ac", 5, "weight", 30, "range", "Unlimited", "costPerMessage", 1));
        putLore(TechLevel.TL4, ModuleCategory.FURNITURE, "GAS_LAMP", "Gas Lamp",
                Map.of("hp", 5, "ac", 5, "weight", 5, "light", "Bright 20ft, Dim 20ft"));

        // TREASURE
        putLore(TechLevel.TL4, ModuleCategory.TREASURE, "DRAGON_HOARD", "Dragon's Hoard",
                Map.of("gpValue", 50000, "rarity", "Legendary", "type", "Currency", "guarded", true, "cursed", false));

        // TRAP
        putLore(TechLevel.TL4, ModuleCategory.TRAP, "STEAM_VENT", "Steam Vent Trap",
                Map.of("dc", 13, "damage", "4d6", "damageType", "Fire", "save", "Dex"));
        putLore(TechLevel.TL4, ModuleCategory.TRAP, "ELECTRIC_TRAP", "Electric Trap",
                Map.of("dc", 14, "damage", "6d6", "damageType", "Lightning", "save", "Dex"));

        // MONSTER
        {
            Map<String, Object> dragonStats = new java.util.HashMap<>();
            dragonStats.put("cr", 24);
            dragonStats.put("hp", 546);
            dragonStats.put("ac", 22);
            dragonStats.put("type", "Dragon");
            dragonStats.put("size", "Gargantuan");
            dragonStats.put("speed", 40);
            dragonStats.put("fly", 200);
            dragonStats.put("climb", 40);
            dragonStats.put("breathWeapon", "7d6 Fire");
            dragonStats.put("legendaryActions", 3);
            dragonStats.put("lairActions", true);
            String k = key(TechLevel.TL4, ModuleCategory.MONSTER, "ANCIENT_DRAGON");
            LORE_NAMES.put(k, "Ancient Red Dragon");
            LORE_STATS.put(k, dragonStats);
        }
        {
            Map<String, Object> lichStats = new java.util.HashMap<>();
            lichStats.put("cr", 21);
            lichStats.put("hp", 135);
            lichStats.put("ac", 17);
            lichStats.put("type", "Undead");
            lichStats.put("size", "Medium");
            lichStats.put("speed", 30);
            lichStats.put("damageImmunities", "Poison,Necrotic");
            lichStats.put("conditionImmunities", "Charmed, Exhaustion, Frightened, Paralyzed, Poisoned");
            lichStats.put("legendaryActions", 3);
            lichStats.put("phylactery", true);
            String k = key(TechLevel.TL4, ModuleCategory.MONSTER, "LICH");
            LORE_NAMES.put(k, "Lich");
            LORE_STATS.put(k, lichStats);
        }
        putLore(TechLevel.TL4, ModuleCategory.MONSTER, "DEVIL", "Archdevil",
                Map.of("cr", 21, "hp", 145, "ac", 19, "type", "Fiend", "size", "Large",
                        "speed", 40, "fly", 40, "damageImmunities", "Fire, Cold",
                        "legendaryActions", 3, "command", true));

        // MAGIC_ITEM
        putLore(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Staff of Power",
                Map.of("rarity", "Very Rare", "type", "Staff", "charges", 20, "acBonus", 2, "attuned", true));
        putLore(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "DEATHS_DOOR", "Death's Door",
                Map.of("rarity", "Legendary", "type", "Weapon", "damageBonus", "1d10", "lifeDrain", true, "attuned", true));
        putLore(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "ROBE_OF_THE_ARCHMAGE", "Robe of the Archmage",
                Map.of("rarity", "Legendary", "type", "Robe", "spellDCBonus", 1, "attuned", true, "attunedClass", "Spellcaster"));

        // NPC
        putLore(TechLevel.TL4, ModuleCategory.NPC, "DRAGONLORD", "Dragonlord",
                Map.of("role", "Ruler", "alignment", "Lawful Evil", "level", 20, "dragonMount", true));
        putLore(TechLevel.TL4, ModuleCategory.NPC, "ARCHMAGE", "Archmage",
                Map.of("role", "Spellcaster", "alignment", "Neutral", "level", 20, "spellSlots", 22));

        // SPELL
        putLore(TechLevel.TL4, ModuleCategory.SPELL, "WISH", "Wish",
                Map.of("level", 9, "school", "Conjuration", "range", "150ft", "duration", "Instantaneous",
                        "stressDC", 15));
        putLore(TechLevel.TL4, ModuleCategory.SPELL, "METAMORPHOSIS", "Metamorphosis",
                Map.of("level", 8, "school", "Transmutation", "range", "Self", "duration", "24 hours",
                        "concentration", true));

        // WEAPON
        putLore(TechLevel.TL4, ModuleCategory.WEAPON, "RIFLE", "Rifle",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 5,
                        "properties", "Ammunition(100/400), Heavy, Loading, Two-Handed"));
        putLore(TechLevel.TL4, ModuleCategory.WEAPON, "MAGIC_LONGSWORD", "Magic Longsword +1",
                Map.of("damage", "1d8+1", "damageType", "Slashing", "weight", 3,
                        "properties", "Versatile(1d10+1)", "rarity", "Uncommon", "magicBonus", 1));

        // ARMOR
        putLore(TechLevel.TL4, ModuleCategory.ARMOR, "MAGIC_PLATE", "Magic Plate +1",
                Map.of("ac", 19, "weight", 65, "type", "Heavy", "stealthDisadvantage", true,
                        "strengthRequired", 15, "rarity", "Uncommon", "magicBonus", 1));

        // ADVENTURE_GEAR
        putLore(TechLevel.TL4, ModuleCategory.ADVENTURE_GEAR, "GUNPOWDER", "Gunpowder",
                Map.of("cost", 50, "weight", 1, "damage", "2d6", "damageType", "Fire", "explosive", true));

        // === ANIMAL mappings (TL0-TL4, real animals) ===
        // Livestock
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "COW", "Cow",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "PIG", "Pig",
                Map.of("meatYield", 80, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SHEEP", "Sheep",
                Map.of("milkYield", 2, "woolYield", 3, "meatYield", 30, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "GOAT", "Goat",
                Map.of("milkYield", 3, "woolYield", 1, "meatYield", 25, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CHICKEN", "Chicken",
                Map.of("eggRate", 5, "meatYield", 2, "type", "Beast", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DUCK", "Duck",
                Map.of("eggRate", 4, "meatYield", 1.5, "type", "Beast", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "TURKEY", "Turkey",
                Map.of("eggRate", 3, "meatYield", 15, "type", "Beast", "size", "Small"));
        // Transport
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE", "Horse",
                Map.of("carryingCapacity", 100, "meatYield", 150, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DONKEY", "Donkey",
                Map.of("carryingCapacity", 80, "meatYield", 60, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "MULE", "Mule",
                Map.of("carryingCapacity", 120, "meatYield", 70, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CAMEL", "Camel",
                Map.of("carryingCapacity", 150, "meatYield", 100, "type", "Beast", "size", "Huge"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "OX", "Ox",
                Map.of("carryingCapacity", 200, "meatYield", 250, "type", "Beast", "size", "Huge"));
        // Companion
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DOG", "Dog",
                Map.of("meatYield", 15, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CAT", "Cat",
                Map.of("meatYield", 3, "type", "Beast", "size", "Small"));
        // Industry
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "BEE", "Bee",
                Map.of("type", "Beast", "size", "Tiny", "product", "Honey"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SILK_WORM", "Silk Worm",
                Map.of("type", "Beast", "size", "Tiny", "product", "Silk"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SALMON", "Salmon",
                Map.of("meatYield", 4, "type", "Beast", "size", "Small"));
        // Wild
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DEER", "Deer",
                Map.of("meatYield", 40, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "RABBIT", "Rabbit",
                Map.of("meatYield", 1, "type", "Beast", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "BEAVER", "Beaver",
                Map.of("meatYield", 8, "type", "Beast", "size", "Small"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "OTTER", "Otter",
                Map.of("meatYield", 5, "type", "Beast", "size", "Small"));
        // TL1+ animals
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "LLAMA", "Llama",
                Map.of("carryingCapacity", 60, "meatYield", 40, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "YAK", "Yak",
                Map.of("milkYield", 5, "woolYield", 2, "carryingCapacity", 100, "meatYield", 120, "type", "Beast", "size", "Huge"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "BUFFALO", "Buffalo",
                Map.of("milkYield", 15, "carryingCapacity", 150, "meatYield", 300, "type", "Beast", "size", "Huge"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "GOOSE", "Goose",
                Map.of("eggRate", 3, "meatYield", 4, "type", "Beast", "size", "Small"));
        // TL2+ animals
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "WAR_HORSE", "War Horse",
                Map.of("carryingCapacity", 120, "meatYield", 200, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "HAWK", "Hawk",
                Map.of("type", "Beast", "size", "Tiny"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "BEAR", "Bear",
                Map.of("meatYield", 150, "type", "Beast", "size", "Huge"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "WOLF", "Wolf",
                Map.of("meatYield", 30, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "EAGLE", "Eagle",
                Map.of("type", "Beast", "size", "Small"));
        // TL3+ animals
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "ELEPHANT", "Elephant",
                Map.of("milkYield", 60, "carryingCapacity", 500, "meatYield", 1000, "type", "Beast", "size", "Gargantuan"));
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "ZEBRA", "Zebra",
                Map.of("carryingCapacity", 80, "meatYield", 100, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "PONY", "Pony",
                Map.of("carryingCapacity", 60, "meatYield", 50, "type", "Beast", "size", "Medium"));
        // TL4+ animals
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "RACE_HORSE", "Race Horse",
                Map.of("carryingCapacity", 80, "meatYield", 180, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "SHOW_COW", "Show Cow",
                Map.of("milkYield", 25, "meatYield", 250, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "CANARY", "Canary",
                Map.of("type", "Beast", "size", "Tiny"));
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
        return TechLevel.TL4;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL0;
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
