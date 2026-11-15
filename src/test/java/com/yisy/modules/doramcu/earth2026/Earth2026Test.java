package com.yisy.modules.doramcu.earth2026;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import com.yisy.modules.doramcu.earth2026.techlevel.Earth2026TechLevelAdapter;
import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Earth2026Test {

    Earth2026TechLevelAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new Earth2026TechLevelAdapter();
    }

    @Nested
    @DisplayName("TechLevelAdapter Basics")
    class AdapterBasics {

        @Test
        @DisplayName("Min tech level is TL5")
        void minTechLevel() {
            assertEquals(TechLevel.TL5, adapter.getMinTechLevel());
        }

        @Test
        @DisplayName("Max tech level is TL6")
        void maxTechLevel() {
            assertEquals(TechLevel.TL6, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("Supports TL5")
        void supportsTL5() {
            assertTrue(adapter.supportsTechLevel(TechLevel.TL5));
        }

        @Test
        @DisplayName("Supports TL6")
        void supportsTL6() {
            assertTrue(adapter.supportsTechLevel(TechLevel.TL6));
        }

        @Test
        @DisplayName("Does not support TL4")
        void doesNotSupportTL4() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL4));
        }

        @Test
        @DisplayName("Does not support TL7")
        void doesNotSupportTL7() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL7));
        }

        @Test
        @DisplayName("Does not support TL0")
        void doesNotSupportTL0() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL0));
        }

        @Test
        @DisplayName("Does not support TL12")
        void doesNotSupportTL12() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL12));
        }
    }

    @Nested
    @DisplayName("Monster Cross-Galaxy Conversion")
    class MonsterConversion {

        @Test
        @DisplayName("GOBLIN maps to Street Gang")
        void goblinToStreetGang() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "GOBLIN");
            assertEquals("Street Gang", name);
        }

        @Test
        @DisplayName("DRAGON maps to Military Jet")
        void dragonToMilitaryJet() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "DRAGON");
            assertEquals("Military Jet", name);
        }

        @Test
        @DisplayName("ZOMBIE maps to Bio-Weapon Subject")
        void zombieToBioWeapon() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "ZOMBIE");
            assertEquals("Bio-Weapon Subject", name);
        }

        @Test
        @DisplayName("SKELETON maps to Cybernetic Drone")
        void skeletonToCyberneticDrone() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "SKELETON");
            assertEquals("Cybernetic Drone", name);
        }

        @Test
        @DisplayName("ORC maps to Paramilitary Unit")
        void orcToParamilitary() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "ORC");
            assertEquals("Paramilitary Unit", name);
        }

        @Test
        @DisplayName("GOLEM maps to Robot")
        void golemToRobot() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "GOLEM");
            assertEquals("Robot", name);
        }

        @Test
        @DisplayName("LICH maps to Rogue Supercomputer")
        void lichToRogueSupercomputer() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "LICH");
            assertEquals("Rogue Supercomputer", name);
        }

        @Test
        @DisplayName("Unknown monster returns null")
        void unknownMonsterReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "UNKNOWN_MONSTER");
            assertNull(name);
        }

        @Test
        @DisplayName("TL4 returns null for monsters")
        void unsupportedTLReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "GOBLIN");
            assertNull(name);
        }

        @Test
        @DisplayName("TL7 returns null for monsters")
        void unsupportedTL7ReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL7, ModuleCategory.MONSTER, "GOBLIN");
            assertNull(name);
        }

        @Test
        @DisplayName("Null generic type returns null")
        void nullGenericTypeReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, null);
            assertNull(name);
        }

        @Test
        @DisplayName("Case insensitive lookup")
        void caseInsensitiveLookup() {
            assertEquals("Street Gang", adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "goblin"));
            assertEquals("Street Gang", adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "Goblin"));
            assertEquals("Street Gang", adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "GOBLIN"));
        }
    }

    @Nested
    @DisplayName("NPC Cross-Galaxy Conversion")
    class NpcConversion {

        @Test
        @DisplayName("WIZARD maps to Hacker")
        void wizardToHacker() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "WIZARD");
            assertEquals("Hacker", name);
        }

        @Test
        @DisplayName("KNIGHT maps to Mercenary")
        void knightToMercenary() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "KNIGHT");
            assertEquals("Mercenary", name);
        }

        @Test
        @DisplayName("INNKEEPER maps to Hotel Manager")
        void innkeeperToHotelManager() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "INNKEEPER");
            assertEquals("Hotel Manager", name);
        }

        @Test
        @DisplayName("MERCHANT maps to Corporate Trader")
        void merchantToCorporateTrader() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "MERCHANT");
            assertEquals("Corporate Trader", name);
        }

        @Test
        @DisplayName("GUARD maps to Security Guard")
        void guardToSecurityGuard() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "GUARD");
            assertEquals("Security Guard", name);
        }

        @Test
        @DisplayName("CLERIC maps to Doctor")
        void clericToDoctor() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "CLERIC");
            assertEquals("Doctor", name);
        }

        @Test
        @DisplayName("KING maps to President")
        void kingToPresident() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "KING");
            assertEquals("President", name);
        }

        @Test
        @DisplayName("ROGUE maps to Thief")
        void rogueToThief() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "ROGUE");
            assertEquals("Thief", name);
        }

        @Test
        @DisplayName("Unknown NPC returns null")
        void unknownNpcReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "UNKNOWN_NPC");
            assertNull(name);
        }
    }

    @Nested
    @DisplayName("Treasure Cross-Galaxy Conversion")
    class TreasureConversion {

        @Test
        @DisplayName("GOLD_COINS maps to Cash")
        void goldCoinsToCash() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TREASURE, "GOLD_COINS");
            assertEquals("Cash", name);
        }

        @Test
        @DisplayName("GEM_DIAMOND maps to Diamond")
        void gemDiamondToDiamond() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TREASURE, "GEM_DIAMOND");
            assertEquals("Diamond", name);
        }

        @Test
        @DisplayName("MAP maps to Digital Map")
        void mapToDigitalMap() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TREASURE, "MAP");
            assertEquals("Digital Map", name);
        }
    }

    @Nested
    @DisplayName("Weapon Cross-Galaxy Conversion")
    class WeaponConversion {

        @Test
        @DisplayName("LONG_SWORD maps to Machete")
        void longSwordToMachete() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.WEAPON, "LONG_SWORD");
            assertEquals("Machete", name);
        }

        @Test
        @DisplayName("CROSSBOW maps to Pistol")
        void crossbowToPistol() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.WEAPON, "CROSSBOW");
            assertEquals("Pistol", name);
        }

        @Test
        @DisplayName("HEAVY_CROSSBOW maps to Assault Rifle")
        void heavyCrossbowToAssaultRifle() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.WEAPON, "HEAVY_CROSSBOW");
            assertEquals("Assault Rifle", name);
        }
    }

    @Nested
    @DisplayName("Armor Cross-Galaxy Conversion")
    class ArmorConversion {

        @Test
        @DisplayName("LEATHER_ARMOR maps to Kevlar Vest")
        void leatherArmorToKevlarVest() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.ARMOR, "LEATHER_ARMOR");
            assertEquals("Kevlar Vest", name);
        }

        @Test
        @DisplayName("PLATE_ARMOR maps to Tactical Armor")
        void plateArmorToTacticalArmor() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.ARMOR, "PLATE_ARMOR");
            assertEquals("Tactical Armor", name);
        }
    }

    @Nested
    @DisplayName("Potion Cross-Galaxy Conversion")
    class PotionConversion {

        @Test
        @DisplayName("POTION_HEALING maps to Medkit")
        void healingPotionToMedkit() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.POTION, "POTION_HEALING");
            assertEquals("Medkit", name);
        }

        @Test
        @DisplayName("POTION_SPEED maps to Adrenaline Shot")
        void speedPotionToAdrenaline() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.POTION, "POTION_SPEED");
            assertEquals("Adrenaline Shot", name);
        }
    }

    @Nested
    @DisplayName("Magic Item Cross-Galaxy Conversion")
    class MagicItemConversion {

        @Test
        @DisplayName("CRYSTAL_BALL maps to Holographic Display")
        void crystalBallToHolographicDisplay() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MAGIC_ITEM, "CRYSTAL_BALL");
            assertEquals("Holographic Display", name);
        }

        @Test
        @DisplayName("WAND maps to Laser Pointer")
        void wandToLaserPointer() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MAGIC_ITEM, "WAND");
            assertEquals("Laser Pointer", name);
        }
    }

    @Nested
    @DisplayName("Spell Cross-Galaxy Conversion")
    class SpellConversion {

        @Test
        @DisplayName("FIREBALL maps to Incendiary Grenade")
        void fireballToIncendiaryGrenade() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SPELL, "FIREBALL");
            assertEquals("Incendiary Grenade", name);
        }

        @Test
        @DisplayName("LIGHTNING_BOLT maps to EMP Blast")
        void lightningBoltToEmpBlast() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SPELL, "LIGHTNING_BOLT");
            assertEquals("EMP Blast", name);
        }

        @Test
        @DisplayName("INVISIBILITY maps to Stealth Mode")
        void invisibilityToStealthMode() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SPELL, "INVISIBILITY");
            assertEquals("Stealth Mode", name);
        }
    }

    @Nested
    @DisplayName("Trap Cross-Galaxy Conversion")
    class TrapConversion {

        @Test
        @DisplayName("POISON_DART maps to Electric Shock")
        void poisonDartToElectricShock() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TRAP, "POISON_DART");
            assertEquals("Electric Shock", name);
        }

        @Test
        @DisplayName("MAGIC_TRAP maps to EMP Trap")
        void magicTrapToEmpTrap() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TRAP, "MAGIC_TRAP");
            assertEquals("EMP Trap", name);
        }
    }

    @Nested
    @DisplayName("Door Cross-Galaxy Conversion")
    class DoorConversion {

        @Test
        @DisplayName("WOODEN_DOOR maps to Security Door")
        void woodenDoorToSecurityDoor() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.DOOR, "WOODEN_DOOR");
            assertEquals("Security Door", name);
        }

        @Test
        @DisplayName("PORTCULLIS maps to Blast Door")
        void portcullisToBlastDoor() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.DOOR, "PORTCULLIS");
            assertEquals("Blast Door", name);
        }
    }

    @Nested
    @DisplayName("Chest Cross-Galaxy Conversion")
    class ChestConversion {

        @Test
        @DisplayName("STRONGBOX maps to Safe")
        void strongboxToSafe() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.CHEST, "STRONGBOX");
            assertEquals("Safe", name);
        }

        @Test
        @DisplayName("TREASURE_VAULT maps to Vault")
        void treasureVaultToVault() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.CHEST, "TREASURE_VAULT");
            assertEquals("Vault", name);
        }
    }

    @Nested
    @DisplayName("Furniture Cross-Galaxy Conversion")
    class FurnitureConversion {

        @Test
        @DisplayName("THRONE maps to Executive Chair")
        void throneToExecutiveChair() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.FURNITURE, "THRONE");
            assertEquals("Executive Chair", name);
        }

        @Test
        @DisplayName("ALTAR maps to Memorial")
        void altarToMemorial() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.FURNITURE, "ALTAR");
            assertEquals("Memorial", name);
        }
    }

    @Nested
    @DisplayName("Dungeon Room Cross-Galaxy Conversion")
    class DungeonRoomConversion {

        @Test
        @DisplayName("TREASURE_ROOM maps to Vault")
        void treasureRoomToVault() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "TREASURE_ROOM");
            assertEquals("Vault", name);
        }

        @Test
        @DisplayName("BOSS_ROOM maps to Command Center")
        void bossRoomToCommandCenter() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "BOSS_ROOM");
            assertEquals("Command Center", name);
        }
    }

    @Nested
    @DisplayName("Adventure Gear Cross-Galaxy Conversion")
    class AdventureGearConversion {

        @Test
        @DisplayName("TORCH maps to Flashlight")
        void torchToFlashlight() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.ADVENTURE_GEAR, "TORCH");
            assertEquals("Flashlight", name);
        }

        @Test
        @DisplayName("RATIONS maps to MRE")
        void rationsToMRE() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.ADVENTURE_GEAR, "RATIONS");
            assertEquals("MRE", name);
        }
    }

    @Nested
    @DisplayName("Lore Stats")
    class LoreStats {

        @Test
        @DisplayName("Stats contain lore name and game type")
        void statsContainLoreNameAndGameType() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.MONSTER, "GOBLIN");
            assertNotNull(stats);
            assertEquals("Street Gang", stats.get("loreName"));
            assertEquals("EARTH_2026", stats.get("gameType"));
        }

        @Test
        @DisplayName("Stats for unknown type is empty")
        void statsForUnknownTypeIsEmpty() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.MONSTER, "UNKNOWN");
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("Stats for unsupported TL is empty")
        void statsForUnsupportedTLEmpty() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.MONSTER, "GOBLIN");
            assertTrue(stats.isEmpty());
        }
    }

    @Nested
    @DisplayName("Available Modules")
    class AvailableModules {

        @Test
        @DisplayName("TL5 returns available modules")
        void tl5ReturnsModules() {
            var modules = adapter.getAvailableModules(TechLevel.TL5);
            assertNotNull(modules);
            assertTrue(modules.size() > 100, "Should have many modules, got: " + modules.size());
        }

        @Test
        @DisplayName("TL6 returns available modules")
        void tl6ReturnsModules() {
            var modules = adapter.getAvailableModules(TechLevel.TL6);
            assertNotNull(modules);
            assertTrue(modules.size() > 100);
        }

        @Test
        @DisplayName("TL4 returns empty list")
        void tl4ReturnsEmpty() {
            var modules = adapter.getAvailableModules(TechLevel.TL4);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("TL7 returns empty list")
        void tl7ReturnsEmpty() {
            var modules = adapter.getAvailableModules(TechLevel.TL7);
            assertTrue(modules.isEmpty());
        }
    }

    @Nested
    @DisplayName("E26Monster Entity")
    class MonsterEntity {

        @Test
        @DisplayName("Create monster with valid fields")
        void createMonster() {
            E26Monster monster = new E26Monster();
            monster.setName("Test Monster");
            monster.setThreatLevel(5);
            monster.setXp(300);
            monster.setSize("Medium");
            monster.setType("Human");
            monster.setAlignment("Chaotic Evil");
            monster.setAc(14);
            monster.setHp(80);
            monster.setSpeed(12);
            monster.setStr(16);
            monster.setDex(14);
            monster.setCon(14);
            monster.setIntel(12);
            monster.setWis(12);
            monster.setCha(10);
            monster.setSenses("Normal");
            monster.setDescription("A test monster.");

            assertEquals("Test Monster", monster.getName());
            assertEquals(5, monster.getThreatLevel());
            assertEquals(300, monster.getXp());
            assertEquals("Medium", monster.getSize());
            assertEquals("Human", monster.getType());
            assertEquals(14, monster.getAc());
            assertEquals(80, monster.getHp());
            assertTrue(monster.isActive());
        }

        @Test
        @DisplayName("Threat level clamped to 1-10")
        void threatLevelClamped() {
            E26Monster monster = new E26Monster();
            monster.setThreatLevel(0);
            assertEquals(1, monster.getThreatLevel());
            monster.setThreatLevel(11);
            assertEquals(10, monster.getThreatLevel());
            monster.setThreatLevel(50);
            assertEquals(10, monster.getThreatLevel());
        }

        @Test
        @DisplayName("Ability scores clamped to 1-20")
        void abilityScoresClamped() {
            E26Monster monster = new E26Monster();
            monster.setStr(0);
            assertEquals(1, monster.getStr());
            monster.setStr(21);
            assertEquals(20, monster.getStr());
            monster.setDex(0);
            assertEquals(1, monster.getDex());
            monster.setIntel(100);
            assertEquals(20, monster.getIntel());
        }

        @Test
        @DisplayName("HP minimum is 1")
        void hpMinimum() {
            E26Monster monster = new E26Monster();
            monster.setHp(0);
            assertEquals(1, monster.getHp());
            monster.setHp(-10);
            assertEquals(1, monster.getHp());
        }

        @Test
        @DisplayName("XP minimum is 0")
        void xpMinimum() {
            E26Monster monster = new E26Monster();
            monster.setXp(-1);
            assertEquals(0, monster.getXp());
        }

        @Test
        @DisplayName("Default values")
        void defaultValues() {
            E26Monster monster = new E26Monster();
            assertEquals(0, monster.getFlySpeed());
            assertEquals(0, monster.getSwimSpeed());
            assertEquals(0, monster.getClimbSpeed());
            assertTrue(monster.isActive());
            assertEquals("[]", monster.getSpecialAbilities());
        }

        @Test
        @DisplayName("ToString includes name and threat level")
        void toStringIncludesName() {
            E26Monster monster = new E26Monster();
            monster.setName("Test");
            monster.setThreatLevel(5);
            monster.setXp(100);
            String str = monster.toString();
            assertTrue(str.contains("Test"));
            assertTrue(str.contains("TL=5"));
        }
    }

    @Nested
    @DisplayName("E26Npc Entity")
    class NpcEntity {

        @Test
        @DisplayName("Create NPC with valid fields")
        void createNpc() {
            E26Npc npc = new E26Npc();
            npc.setName("Test NPC");
            npc.setNpcType("Civilian");
            npc.setOccupation("Teacher");
            npc.setDescription("A test NPC.");
            npc.setMotive("Teach students");
            npc.setHitPoints(20);
            npc.setArmorClass(10);
            npc.setCash(500);

            assertEquals("Test NPC", npc.getName());
            assertEquals("Civilian", npc.getNpcType());
            assertEquals("Teacher", npc.getOccupation());
            assertEquals(20, npc.getHitPoints());
            assertEquals(10, npc.getArmorClass());
            assertEquals(500, npc.getCash());
            assertTrue(npc.isActive());
        }

        @Test
        @DisplayName("Default disposition is Indifferent")
        void defaultDisposition() {
            E26Npc npc = new E26Npc();
            assertEquals("Indifferent", npc.getDisposition());
            assertEquals(0, npc.getDispositionScore());
            assertFalse(npc.isVillain());
            assertFalse(npc.isAlly());
        }

        @Test
        @DisplayName("Disposition score updates disposition string")
        void dispositionScoreUpdatesString() {
            E26Npc npc = new E26Npc();
            npc.setDispositionScore(-10);
            assertEquals("Hostile", npc.getDisposition());
            npc.setDispositionScore(-6);
            assertEquals("Hostile", npc.getDisposition());
            npc.setDispositionScore(-5);
            assertEquals("Unfriendly", npc.getDisposition());
            npc.setDispositionScore(-2);
            assertEquals("Unfriendly", npc.getDisposition());
            npc.setDispositionScore(-1);
            assertEquals("Indifferent", npc.getDisposition());
            npc.setDispositionScore(1);
            assertEquals("Indifferent", npc.getDisposition());
            npc.setDispositionScore(2);
            assertEquals("Friendly", npc.getDisposition());
            npc.setDispositionScore(5);
            assertEquals("Friendly", npc.getDisposition());
            npc.setDispositionScore(6);
            assertEquals("Helpful", npc.getDisposition());
            npc.setDispositionScore(10);
            assertEquals("Helpful", npc.getDisposition());
        }

        @Test
        @DisplayName("Disposition score clamped to -10 to +10")
        void dispositionScoreClamped() {
            E26Npc npc = new E26Npc();
            npc.setDispositionScore(-11);
            assertEquals(-10, npc.getDispositionScore());
            npc.setDispositionScore(11);
            assertEquals(10, npc.getDispositionScore());
        }

        @Test
        @DisplayName("Level clamped to 1-10")
        void levelClamped() {
            E26Npc npc = new E26Npc();
            npc.setLevel(0);
            assertEquals(1, npc.getLevel());
            npc.setLevel(11);
            assertEquals(10, npc.getLevel());
        }

        @Test
        @DisplayName("Threat level clamped to 1-10")
        void threatLevelClamped() {
            E26Npc npc = new E26Npc();
            npc.setThreatLevel(0);
            assertEquals(1, npc.getThreatLevel());
            npc.setThreatLevel(11);
            assertEquals(10, npc.getThreatLevel());
        }

        @Test
        @DisplayName("HP minimum is 0")
        void hpMinimum() {
            E26Npc npc = new E26Npc();
            npc.setHitPoints(-1);
            assertEquals(0, npc.getHitPoints());
        }

        @Test
        @DisplayName("Cash minimum is 0")
        void cashMinimum() {
            E26Npc npc = new E26Npc();
            npc.setCash(-100);
            assertEquals(0, npc.getCash());
        }

        @Test
        @DisplayName("Default items and motives are empty JSON arrays")
        void defaultJsonFields() {
            E26Npc npc = new E26Npc();
            assertEquals("[]", npc.getItems());
            assertEquals("[]", npc.getSecondaryMotives());
        }

        @Test
        @DisplayName("Villain and ally flags")
        void villainAndAllyFlags() {
            E26Npc npc = new E26Npc();
            npc.setVillain(true);
            npc.setAlly(false);
            assertTrue(npc.isVillain());
            assertFalse(npc.isAlly());
        }

        @Test
        @DisplayName("ToString includes name and type")
        void toStringIncludesName() {
            E26Npc npc = new E26Npc();
            npc.setName("Test");
            npc.setNpcType("Civilian");
            npc.setOccupation("Teacher");
            String str = npc.toString();
            assertTrue(str.contains("Test"));
            assertTrue(str.contains("Civilian"));
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandling {

        @Test
        @DisplayName("Null category returns null")
        void nullCategoryReturnsNull() {
            String name = adapter.getLoreName(TechLevel.TL5, null, "GOBLIN");
            assertNull(name);
        }

        @Test
        @DisplayName("Null tech level returns null")
        void nullTechLevelReturnsNull() {
            String name = adapter.getLoreName(null, ModuleCategory.MONSTER, "GOBLIN");
            assertNull(name);
        }

        @Test
        @DisplayName("Stats with null category returns empty")
        void statsNullCategoryEmpty() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, null, "GOBLIN");
            assertTrue(stats.isEmpty());
        }
    }

    @Nested
    @DisplayName("Cross-Module Comparison")
    class CrossModuleComparison {

        @Test
        @DisplayName("Earth 2026 GOBLIN differs from D&D")
        void goblinDiffersFromDnd() {
            // Earth 2026 maps GOBLIN to "Street Gang"
            String e26 = adapter.getLoreName(TechLevel.TL5, ModuleCategory.MONSTER, "GOBLIN");
            assertEquals("Street Gang", e26);
            // D&D would keep it as "Goblin" — different lore
            assertNotEquals("Goblin", e26);
        }

        @Test
        @DisplayName("Earth 2026 WIZARD differs from D&D")
        void wizardDiffersFromDnd() {
            String e26 = adapter.getLoreName(TechLevel.TL5, ModuleCategory.NPC, "WIZARD");
            assertEquals("Hacker", e26);
            assertNotEquals("Wizard", e26);
        }

        @Test
        @DisplayName("Earth 2026 GOLD_COINS differs from D&D")
        void goldCoinsDiffersFromDnd() {
            String e26 = adapter.getLoreName(TechLevel.TL5, ModuleCategory.TREASURE, "GOLD_COINS");
            assertEquals("Cash", e26);
            assertNotEquals("Gold Coins", e26);
        }
    }
}
