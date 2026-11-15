package com.yisy.modules.doramcu.earth2026.techlevel;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Earth2026TechLevelAdapter implements TechLevelAdapter {

    private static final TechLevel MIN_TL = TechLevel.TL5;
    private static final TechLevel MAX_TL = TechLevel.TL6;

    private static final Map<String, String> MONSTER = new LinkedHashMap<>();
    private static final Map<String, String> NPC = new LinkedHashMap<>();
    private static final Map<String, String> TREASURE = new LinkedHashMap<>();
    private static final Map<String, String> WEAPON = new LinkedHashMap<>();
    private static final Map<String, String> ARMOR = new LinkedHashMap<>();
    private static final Map<String, String> POTION = new LinkedHashMap<>();
    private static final Map<String, String> MAGIC_ITEM = new LinkedHashMap<>();
    private static final Map<String, String> ADVENTURE_GEAR = new LinkedHashMap<>();
    private static final Map<String, String> DUNGEON_ROOM = new LinkedHashMap<>();
    private static final Map<String, String> FURNITURE = new LinkedHashMap<>();
    private static final Map<String, String> TRAP = new LinkedHashMap<>();
    private static final Map<String, String> DOOR = new LinkedHashMap<>();
    private static final Map<String, String> CHEST = new LinkedHashMap<>();
    private static final Map<String, String> SPELL = new LinkedHashMap<>();
    private static final Map<String, String> ANIMAL = new LinkedHashMap<>();

    static {
        // === MONSTERS: Fantasy → Near-Future Threats ===
        MONSTER.put("GOBLIN", "Street Gang");
        MONSTER.put("ORC", "Paramilitary Unit");
        MONSTER.put("OGRE", "Enforcer");
        MONSTER.put("HOBGOBLIN", "Gang Leader");
        MONSTER.put("BANDIT", "Armed Robber");
        MONSTER.put("BANDIT_CAPTAIN", "Crime Boss");
        MONSTER.put("PIRATE", "Smuggler");
        MONSTER.put("ASSASSIN", "Hitman");
        MONSTER.put("DRAGON", "Military Jet");
        MONSTER.put("DRAGON_RED", "Stealth Fighter");
        MONSTER.put("DRAGON_GOLD", "Aircraft Carrier");
        MONSTER.put("GIANT", "Armored Vehicle");
        MONSTER.put("GIANT_HILL", "APC");
        MONSTER.put("GIANT_STONE", "Main Battle Tank");
        MONSTER.put("ZOMBIE", "Bio-Weapon Subject");
        MONSTER.put("SKELETON", "Cybernetic Drone");
        MONSTER.put("SKELETON_WARRIOR", "Combat Robot");
        MONSTER.put("GHOST", "Hacked AI");
        MONSTER.put("LICH", "Rogue Supercomputer");
        MONSTER.put("DEATH_KNIGHT", "Terrorist Leader");
        MONSTER.put("GOLEM", "Robot");
        MONSTER.put("GOLEM_IRON", "Security Robot");
        MONSTER.put("GOLEM_DEATH", "Autonomous Weapon");
        MONSTER.put("ELEMENTAL", "Drone Swarm");
        MONSTER.put("BEAR", "Attack Dog");
        MONSTER.put("WOLF", "Stray Dog Pack");
        MONSTER.put("ABERRATION", "Alien Entity");
        MONSTER.put("FIEND", "Criminal Mastermind");
        MONSTER.put("CELESTIAL", "Hero");
        MONSTER.put("MIMIC", "Disguised Camera");
        MONSTER.put("DROW", "Cyberpunk Hacker");
        MONSTER.put("VAMPIRE", "Blood Cult Leader");
        MONSTER.put("WIGHT", "Rogue Android");
        MONSTER.put("WRAITH", "Corrupted AI");
        MONSTER.put("MUMMY", "Bio-Engineered Creature");

        // === NPCs: Fantasy Roles → Modern Occupations ===
        NPC.put("INNKEEPER", "Hotel Manager");
        NPC.put("TAPSTER", "Bartender");
        NPC.put("COOK", "Chef");
        NPC.put("MERCHANT", "Corporate Trader");
        NPC.put("BLACKSMITH", "Mechanic");
        NPC.put("ALCHEMIST", "Chemist");
        NPC.put("BANKER", "Investment Banker");
        NPC.put("SMUGGLER", "Contraband Dealer");
        NPC.put("KNIGHT", "Mercenary");
        NPC.put("GUARD", "Security Guard");
        NPC.put("CAPTAIN", "Police Captain");
        NPC.put("GENERAL", "Military General");
        NPC.put("PALADIN", "SWAT Commander");
        NPC.put("WIZARD", "Hacker");
        NPC.put("SORCERER", "AI Researcher");
        NPC.put("WARLOCK", "Cybercriminal");
        NPC.put("DRUID", "Environmental Scientist");
        NPC.put("CLERIC", "Doctor");
        NPC.put("MONK", "Martial Artist");
        NPC.put("BARD", "Musician");
        NPC.put("ROGUE", "Thief");
        NPC.put("FIGHTER", "Bouncer");
        NPC.put("BARBARIAN", "Ex-Military");
        NPC.put("NOBLE", "Politician");
        NPC.put("KING", "President");
        NPC.put("MAYOR", "City Manager");
        NPC.put("JUDGE", "Judge");
        NPC.put("LAWYER", "Attorney");
        NPC.put("DETECTIVE", "Detective");
        NPC.put("SPY", "Intelligence Agent");
        NPC.put("SCIENTIST", "Researcher");
        NPC.put("ENGINEER", "Engineer");
        NPC.put("PILOT", "Pilot");
        NPC.put("JOURNALIST", "Journalist");
        NPC.put("DOCTOR", "Surgeon");
        NPC.put("TEACHER", "Teacher");
        NPC.put("COMMONER", "Citizen");
        NPC.put("FARMER", "Urban Farmer");
        NPC.put("MINER", "Construction Worker");
        NPC.put("CARTER", "Delivery Driver");
        NPC.put("SCHOLAR", "Professor");
        NPC.put("HEALER", "Paramedic");
        NPC.put("HERBALIST", "Pharmacist");
        NPC.put("MECHANIC", "Auto Mechanic");
        NPC.put("NURSE", "Nurse");
        NPC.put("CHEF", "Restaurant Owner");

        // === TREASURE: Gold/Gems → Modern Valuables ===
        TREASURE.put("GOLD_COINS", "Cash");
        TREASURE.put("SILVER_COINS", "Silver Certificates");
        TREASURE.put("PLATINUM_COINS", "Platinum Bars");
        TREASURE.put("GEM_RUBY", "Ruby");
        TREASURE.put("GEM_DIAMOND", "Diamond");
        TREASURE.put("GEM_EMERALD", "Emerald");
        TREASURE.put("ARTIFACT", "Rare Artifact");
        TREASURE.put("RELIC", "Historical Relic");
        TREASURE.put("MAP", "Digital Map");
        TREASURE.put("SCROLL", "Document");
        TREASURE.put("DEED", "Property Deed");
        TREASURE.put("DRIVE", "Data Drive");
        TREASURE.put("CRYPTO_KEY", "Crypto Key");
        TREASURE.put("CLASSIFIED_DOSSIER", "Classified Dossier");
        TREASURE.put("EVIDENCE", "Evidence");

        // === WEAPONS: Fantasy Weapons → Modern Firearms ===
        WEAPON.put("DAGGER", "Knife");
        WEAPON.put("SHORT_SWORD", "Combat Knife");
        WEAPON.put("LONG_SWORD", "Machete");
        WEAPON.put("GREATAXE", "Fire Axe");
        WEAPON.put("MACE", "Nightstick");
        WEAPON.put("FLAIL", "Crowbar");
        WEAPON.put("SPEAR", "Baton");
        WEAPON.put("CROSSBOW", "Pistol");
        WEAPON.put("HEAVY_CROSSBOW", "Assault Rifle");
        WEAPON.put("BOW", "Bow");
        WEAPON.put("DART", "Taser");
        WEAPON.put("PISTOL", "Pistol");
        WEAPON.put("REVOLVER", "Revolver");
        WEAPON.put("SHOTGUN", "Shotgun");
        WEAPON.put("ASSAULT_RIFLE", "Assault Rifle");
        WEAPON.put("SNIPER_RIFLE", "Sniper Rifle");
        WEAPON.put("GRENADE", "Grenade");
        WEAPON.put("ROCKET_LAUNCHER", "Rocket Launcher");
        WEAPON.put("TEAR_GAS", "Tear Gas");
        WEAPON.put("FLASH_BANG", "Flash Bang");

        // === ARMOR: Fantasy Armor → Modern Protection ===
        ARMOR.put("LEATHER_ARMOR", "Kevlar Vest");
        ARMOR.put("CHAIN_MAIL", "Bulletproof Vest");
        ARMOR.put("PLATE_ARMOR", "Tactical Armor");
        ARMOR.put("BREASTPLATE", "Body Armor");
        ARMOR.put("SHIELD", "Shield");
        ARMOR.put("HELMET", "Helmet");

        // === POTIONS: Magic Potions → Medical/Chemical ===
        POTION.put("POTION_HEALING", "Medkit");
        POTION.put("POTION_GREATER_HEALING", "Advanced Medkit");
        POTION.put("POTION_INVISIBILITY", "Stealth Cloak");
        POTION.put("POTION_WATER_BREATHING", "Breathing Apparatus");
        POTION.put("POTION_SPEED", "Adrenaline Shot");
        POTION.put("POTION_POISON_RESISTANCE", "Antidote");
        POTION.put("POTION_DISEASE_RESISTANCE", "Vaccine");
        POTION.put("POTION_ANTIDOTE", "Antidote");
        POTION.put("POTION_HEROISM", "Combat Stimulant");
        POTION.put("POTION_SLEEP", "Sedative");
        POTION.put("POTION_POISON", "Poison");

        // === MAGIC ITEMS: Magic → Tech Devices ===
        MAGIC_ITEM.put("WAND", "Laser Pointer");
        MAGIC_ITEM.put("STAFF", "Tactical Baton");
        MAGIC_ITEM.put("RING", "Smart Ring");
        MAGIC_ITEM.put("SCROLL", "Data Chip");
        MAGIC_ITEM.put("CLOAK", "Stealth Cloak");
        MAGIC_ITEM.put("CRYSTAL_BALL", "Holographic Display");
        MAGIC_ITEM.put("EYE_OF_THE_DAY", "Night Vision Goggles");
        MAGIC_ITEM.put("NECKLACE_OF_ADAPTATION", "Environmental Suit");
        MAGIC_ITEM.put("PORTABLE_HOLE", "Black Hole Device");
        MAGIC_ITEM.put("ROBE_OF_POWER", "Power Suit");

        // === ADVENTURE GEAR: Medieval Gear → Modern Equipment ===
        ADVENTURE_GEAR.put("ROPE", "Paracord");
        ADVENTURE_GEAR.put("TORCH", "Flashlight");
        ADVENTURE_GEAR.put("RATIONS", "MRE");
        ADVENTURE_GEAR.put("WATER_FLASK", "Water Bottle");
        ADVENTURE_GEAR.put("BACKPACK", "Tactical Backpack");
        ADVENTURE_GEAR.put("LOCKPICK", "Lockpick Set");
        ADVENTURE_GEAR.put("CLIMBING_KIT", "Climbing Gear");
        ADVENTURE_GEAR.put("DISGUISE_KIT", "Disguise Kit");
        ADVENTURE_GEAR.put("HERBALISM_KIT", "First Aid Kit");
        ADVENTURE_GEAR.put("THIEFS_TOOLS", "Thief Tools");
        ADVENTURE_GEAR.put("NAVIGATION_TOOLS", "GPS Device");
        ADVENTURE_GEAR.put("BINOCULARS", "Binoculars");
        ADVENTURE_GEAR.put("COMPASS", "Compass");
        ADVENTURE_GEAR.put("TENT", "Tent");
        ADVENTURE_GEAR.put("SLEEPING_BAG", "Sleeping Bag");
        ADVENTURE_GEAR.put("SURVIVAL_KIT", "Survival Kit");
        ADVENTURE_GEAR.put("FIRST_AID_KIT", "First Aid Kit");
        ADVENTURE_GEAR.put("RADIO", "Radio");
        ADVENTURE_GEAR.put("CELL_PHONE", "Cell Phone");
        ADVENTURE_GEAR.put("GPS", "GPS Device");
        ADVENTURE_GEAR.put("CAMERA", "Camera");
        ADVENTURE_GEAR.put("COMPUTER", "Laptop");
        ADVENTURE_GEAR.put("DRONE", "Drone");
        ADVENTURE_GEAR.put("NIGHT_VISION", "Night Vision Goggles");
        ADVENTURE_GEAR.put("TASER", "Taser");
        ADVENTURE_GEAR.put("CUFFS", "Handcuffs");
        ADVENTURE_GEAR.put("PEPPER_SPRAY", "Pepper Spray");

        // === DUNGEON ROOMS: Fantasy Rooms → Modern Rooms ===
        DUNGEON_ROOM.put("ENTRANCE", "Lobby");
        DUNGEON_ROOM.put("TRAP_ROOM", "Security Checkpoint");
        DUNGEON_ROOM.put("TREASURE_ROOM", "Vault");
        DUNGEON_ROOM.put("BOSS_ROOM", "Command Center");
        DUNGEON_ROOM.put("SOCIAL_ROOM", "Conference Room");
        DUNGEON_ROOM.put("LORE_ROOM", "Archive Room");
        DUNGEON_ROOM.put("REST_ROOM", "Break Room");
        DUNGEON_ROOM.put("PUZZLE_ROOM", "Puzzle Room");

        // === FURNITURE: Medieval Furniture → Modern Furniture ===
        FURNITURE.put("CHAIR", "Office Chair");
        FURNITURE.put("TABLE", "Conference Table");
        FURNITURE.put("BED", "Cot");
        FURNITURE.put("THRONE", "Executive Chair");
        FURNITURE.put("ALTAR", "Memorial");
        FURNITURE.put("BRAZIER", "Heater");
        FURNITURE.put("TAPESTRY", "Poster");
        FURNITURE.put("BARREL", "Trash Can");
        FURNITURE.put("BUCKET", "Bucket");
        FURNITURE.put("CANDLESTICK", "Lamp");
        FURNITURE.put("RUG", "Carpet");
        FURNITURE.put("MIRROR", "Mirror");
        FURNITURE.put("SHELF", "Shelf");
        FURNITURE.put("CABINET", "Cabinet");
        FURNITURE.put("DESK", "Desk");
        FURNITURE.put("BENCH", "Bench");
        FURNITURE.put("STOOL", "Stool");
        FURNITURE.put("LAMP", "Lamp");
        FURNITURE.put("CHEST_FURNITURE", "Locker");
        FURNITURE.put("CURTAIN", "Curtain");
        FURNITURE.put("WINDOW", "Window");
        FURNITURE.put("DOOR_FURNITURE", "Door");
        FURNITURE.put("WALL", "Wall");
        FURNITURE.put("FLOOR", "Floor");
        FURNITURE.put("CEILING", "Ceiling");
        FURNITURE.put("STAIRS", "Stairs");
        FURNITURE.put("PILLAR", "Pillar");
        FURNITURE.put("STATUE", "Statue");
        FURNITURE.put("SIGN", "Sign");
        FURNITURE.put("BANNER", "Banner");
        FURNITURE.put("FLAG", "Flag");
        FURNITURE.put("POSTER", "Poster");
        FURNITURE.put("WHITEBOARD", "Whiteboard");
        FURNITURE.put("CLOCK", "Clock");
        FURNITURE.put("SPEAKER", "Speaker");
        FURNITURE.put("TELEPHONE", "Telephone");
        FURNITURE.put("MONITOR", "Monitor");
        FURNITURE.put("TELEVISION", "Television");
        FURNITURE.put("COMPUTER", "Computer");
        FURNITURE.put("PRINTER", "Printer");
        FURNITURE.put("VENDING_MACHINE", "Vending Machine");
        FURNITURE.put("ATM", "ATM");
        FURNITURE.put("ELEVATOR", "Elevator");
        FURNITURE.put("TOILET", "Toilet");
        FURNITURE.put("SINK", "Sink");
        FURNITURE.put("SHOWER", "Shower");
        FURNITURE.put("MICROWAVE", "Microwave");
        FURNITURE.put("REFRIGERATOR", "Refrigerator");
        FURNITURE.put("SOFA", "Sofa");
        FURNITURE.put("BOOKSHELF", "Bookshelf");
        FURNITURE.put("LOCKER", "Locker");
        FURNITURE.put("WORKBENCH", "Workbench");

        // === TRAPS: Dungeon Traps → Security Devices ===
        TRAP.put("POISON_DART", "Electric Shock");
        TRAP.put("PIT_TRAP", "Sewer Grate");
        TRAP.put("PRESSURE_PLATE", "Pressure Plate");
        TRAP.put("FALLING_ROCK", "Falling Debris");
        TRAP.put("GAS_VENT", "Gas Vent");
        TRAP.put("MAGIC_TRAP", "EMP Trap");
        TRAP.put("SPIKE_TRAP", "Spike Trap");
        TRAP.put("NET_TRAP", "Net Trap");
        TRAP.put("ROPE_TRAP", "Tripwire");
        TRAP.put("ARROW_TRAP", "Dart Trap");
        TRAP.put("LANDMINE_TRAP", "Landmine");
        TRAP.put("ELECTROMAGNETIC_TRAP", "EMP Device");
        TRAP.put("CYBER_TRAP", "Cyber Weapon");
        TRAP.put("LASER_TRAP", "Laser Grid");
        TRAP.put("FORCE_FIELD_TRAP", "Force Field");

        // === DOORS: Fantasy Doors → Security Doors ===
        DOOR.put("WOODEN_DOOR", "Security Door");
        DOOR.put("IRON_DOOR", "Steel Door");
        DOOR.put("SECRET_DOOR", "Hidden Door");
        DOOR.put("MAGIC_DOOR", "Electronic Door");
        DOOR.put("PORTCULLIS", "Blast Door");
        DOOR.put("GATE", "Gate");
        DOOR.put("SLIDING_DOOR", "Sliding Door");
        DOOR.put("REVOLVING_DOOR", "Revolving Door");
        DOOR.put("AUTO_DOOR", "Auto Door");
        DOOR.put("TURNSTILE", "Turnstile");

        // === CHESTS: Treasure Chests → Modern Containers ===
        CHEST.put("WOODEN_CHEST", "Locker");
        CHEST.put("IRON_BAND_CHEST", "Metal Locker");
        CHEST.put("STRONGBOX", "Safe");
        CHEST.put("TREASURE_VAULT", "Vault");
        CHEST.put("LOCKBOX", "Lockbox");
        CHEST.put("SAFE_DEPOSIT_BOX", "Safe Deposit Box");
        CHEST.put("CRATE", "Crate");
        CHEST.put("BOX", "Box");
        CHEST.put("BRIEFCASE", "Briefcase");

        // === SPELLS: Magic Spells → Tech Abilities ===
        SPELL.put("MAGIC_MISSILE", "Taser Shot");
        SPELL.put("FIREBALL", "Incendiary Grenade");
        SPELL.put("LIGHTNING_BOLT", "EMP Blast");
        SPELL.put("ICE_STORM", "Freeze Spray");
        SPELL.put("HEAL", "Medkit");
        SPELL.put("CURE_WOUNDS", "First Aid");
        SPELL.put("INVISIBILITY", "Stealth Mode");
        SPELL.put("TELEPORT", "Quick Escape");
        SPELL.put("SHIELD", "Shield Generator");
        SPELL.put("FORCE_CAGE", "Containment Field");
        SPELL.put("MIND_READ", "Wiretap");
        SPELL.put("CHARM_PERSON", "Persuasion");
        SPELL.put("FEAR", "Fear Toxin");
        SPELL.put("SLEEP", "Sedative");
        SPELL.put("PARALYZE", "Paralytic");
        SPELL.put("HASTE", "Stimulant");

        // === ANIMALS: Fantasy Animals → Modern Farm Animals ===
        ANIMAL.put("COW", "Dairy Cow");
        ANIMAL.put("PIG", "Berkshire Pig");
        ANIMAL.put("SHEEP", "Merino Sheep");
        ANIMAL.put("GOAT", "Nubian Goat");
        ANIMAL.put("CHICKEN", "Broiler Chicken");
        ANIMAL.put("DUCK", "Pekin Duck");
        ANIMAL.put("TURKEY", "Broad Breasted Turkey");
        ANIMAL.put("HORSE", "Quarter Horse");
        ANIMAL.put("DONKEY", "Burro");
        ANIMAL.put("MULE", "Mule");
        ANIMAL.put("CAMEL", "Bactrian Camel");
        ANIMAL.put("OX", "Draft Ox");
        ANIMAL.put("DOG", "German Shepherd");
        ANIMAL.put("CAT", "Domestic Cat");
        ANIMAL.put("BEE", "Honey Bee");
        ANIMAL.put("SILK_WORM", "Silk Worm");
        ANIMAL.put("SALMON", "Atlantic Salmon");
        ANIMAL.put("DEER", "White-tailed Deer");
        ANIMAL.put("RABBIT", "Rabbit");
        ANIMAL.put("BEAVER", "Beaver");
        ANIMAL.put("OTTER", "River Otter");
        ANIMAL.put("LLAMA", "Llama");
        ANIMAL.put("YAK", "Yak");
        ANIMAL.put("BUFFALO", "American Buffalo");
        ANIMAL.put("GOOSE", "Domestic Goose");
        ANIMAL.put("WAR_HORSE", "War Horse");
        ANIMAL.put("HAWK", "Red-tailed Hawk");
        ANIMAL.put("BEAR", "Brown Bear");
        ANIMAL.put("WOLF", "Gray Wolf");
        ANIMAL.put("EAGLE", "Bald Eagle");
        ANIMAL.put("ELEPHANT", "African Elephant");
        ANIMAL.put("ZEBRA", "Zebra");
        ANIMAL.put("PONY", "Pony");
        ANIMAL.put("RACE_HORSE", "Thoroughbred");
        ANIMAL.put("SHOW_COW", "Show Cow");
        ANIMAL.put("CANARY", "Canary");
    }

    @Override
    public String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel) || genericType == null) return null;
        Map<String, String> map = getCategoryMap(category);
        return map != null ? map.get(genericType.toUpperCase()) : null;
    }

    @Override
    public Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType) {
        Map<String, Object> stats = new HashMap<>();
        String loreName = getLoreName(techLevel, category, genericType);
        if (loreName != null) {
            stats.put("loreName", loreName);
            stats.put("gameType", "EARTH_2026");
        }
        return stats;
    }

    @Override
    public TechLevel getMaxTechLevel() {
        return MAX_TL;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return MIN_TL;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> all = new ArrayList<>();
        for (Map<String, String> map : Arrays.asList(MONSTER, NPC, TREASURE, WEAPON, ARMOR, POTION,
                MAGIC_ITEM, ADVENTURE_GEAR, DUNGEON_ROOM, FURNITURE, TRAP, DOOR, CHEST, SPELL, ANIMAL)) {
            if (map != null) all.addAll(map.keySet());
        }
        return all;
    }

    private Map<String, String> getCategoryMap(ModuleCategory category) {
        if (category == null) return null;
        return switch (category) {
            case MONSTER -> MONSTER;
            case NPC -> NPC;
            case TREASURE -> TREASURE;
            case WEAPON -> WEAPON;
            case ARMOR -> ARMOR;
            case POTION -> POTION;
            case MAGIC_ITEM -> MAGIC_ITEM;
            case ADVENTURE_GEAR -> ADVENTURE_GEAR;
            case DUNGEON_ROOM -> DUNGEON_ROOM;
            case FURNITURE -> FURNITURE;
            case TRAP -> TRAP;
            case DOOR -> DOOR;
            case CHEST -> CHEST;
            case SPELL -> SPELL;
            case ANIMAL -> ANIMAL;
            default -> null;
        };
    }
}
