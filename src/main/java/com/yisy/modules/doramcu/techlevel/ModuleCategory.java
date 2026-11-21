package com.yisy.modules.doramcu.techlevel;
import com.yisy.modules.doramcu.model.players.Player;

/**
 * Categories of planet-building modules.
 * <p>
 * Each category represents a type of structure or asset that can be placed
 * on a planet, filtered by tech level.
 * </p>
 */
public enum ModuleCategory {

    // === Planet-building categories ===
    SETTLEMENT("Settlement", "Habitations, villages, cities, and population centers"),
    INDUSTRY("Industry", "Factories, mines, farms, and production facilities"),
    VEHICLE("Vehicle", "Transportation: carts, ships, aircraft, and space craft"),
    INFRASTRUCTURE("Infrastructure", "Roads, bridges, power plants, and communication towers"),
    DEFENSE("Defense", "Walls, fortresses, weapon emplacements, and shields"),
    COMMUNICATION("Communication", "Signal towers, relay stations, and data networks"),

    // === D&D dungeon object categories ===
    DUNGEON_ROOM("Dungeon Room", "Interior rooms: entrances, traps, treasure, puzzles, boss chambers"),
    FURNITURE("Furniture", "Chairs, tables, beds, thrones, altars, braziers, tapestries"),
    TREASURE("Treasure", "Gold coins, gems, jewels, artifacts, and hoards"),
    TRAP("Trap", "Poison darts, pit traps, pressure plates, falling rocks, gas vents, magic traps"),
    DOOR("Door", "Wooden doors, iron doors, secret doors, magic doors, portcullises"),
    CHEST("Chest", "Wooden chests, iron-banded chests, strongboxes, treasure vaults"),
    MONSTER("Monster", "Creatures and enemies: beasts, humanoids, dragons, undead, etc."),
    MAGIC_ITEM("Magic Item", "Enchanted objects: weapons, armor, wondrous items, rings, rods, staffs"),
    NPC("NPC", "Non-player characters: innkeepers, merchants, guards, wizards, nobles"),
    SPELL("Spell", "Magical spells organized by level and school"),
    POTION("Potion", "Healing potions, antidotes, poisons, and elixirs"),
    WEAPON("Weapon", "Simple, martial, ranged, and magic weapons"),
    ARMOR("Armor", "Light, medium, heavy armor, and shields"),
    ADVENTURE_GEAR("Adventure Gear", "Ropes, torches, rations, tools, and exploration equipment"),

    // === Animal category for industry support ===
    ANIMAL("Animal", "Real-world animals for farming, ranching, transport, and industry");

    private final String displayName;
    private final String description;

    ModuleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
