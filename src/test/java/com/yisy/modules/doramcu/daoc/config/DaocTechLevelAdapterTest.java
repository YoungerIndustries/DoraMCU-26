package com.yisy.modules.doramcu.daoc.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DaocTechLevelAdapter")
class DaocTechLevelAdapterTest {

    private DaocTechLevelAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DaocTechLevelAdapter();
    }

    @Nested
    @DisplayName("Tech Level Range")
    class TechLevelRange {

        @Test
        @DisplayName("min tech level is TL3 (Medieval)")
        void minTechLevelIsTL3() {
            assertEquals(TechLevel.TL3, adapter.getMinTechLevel());
        }

        @Test
        @DisplayName("max tech level is TL7 (Mythical)")
        void maxTechLevelIsTL7() {
            assertEquals(TechLevel.TL7, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("returns null for unsupported tech level TL0")
        void unsupportedTL0() {
            assertNull(adapter.getLoreName(TechLevel.TL0, ModuleCategory.SETTLEMENT, "COLONY"));
            assertTrue(adapter.getLoreStats(TechLevel.TL0, ModuleCategory.SETTLEMENT, "COLONY").isEmpty());
        }

        @Test
        @DisplayName("returns null for unsupported tech level TL12")
        void unsupportedTL12() {
            assertNull(adapter.getLoreName(TechLevel.TL12, ModuleCategory.SETTLEMENT, "COLONY"));
            assertTrue(adapter.getLoreStats(TechLevel.TL12, ModuleCategory.SETTLEMENT, "COLONY").isEmpty());
        }

        @Test
        @DisplayName("returns empty list for unsupported tech level")
        void unsupportedReturnsEmptyList() {
            assertTrue(adapter.getAvailableModules(TechLevel.TL0).isEmpty());
            assertTrue(adapter.getAvailableModules(TechLevel.TL12).isEmpty());
        }
    }

    @Nested
    @DisplayName("TL3 Medieval Era")
    class TL3Medieval {

        @Test
        @DisplayName("maps Colony to Village")
        void colonyToVillage() {
            assertEquals("Village", adapter.getLoreName(TechLevel.TL3, ModuleCategory.SETTLEMENT, "COLONY"));
        }

        @Test
        @DisplayName("Village has correct stats")
        void villageStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.SETTLEMENT, "COLONY");
            assertEquals(180, stats.get("hp"));
            assertEquals(400, stats.get("capacity"));
            assertEquals(40, stats.get("garrison"));
        }

        @Test
        @DisplayName("maps Barracks to Barracks")
        void barracks() {
            assertEquals("Barracks", adapter.getLoreName(TechLevel.TL3, ModuleCategory.SETTLEMENT, "BARRACKS"));
        }

        @Test
        @DisplayName("maps Weapons Factory to Blacksmith")
        void weaponsFactoryToBlacksmith() {
            assertEquals("Blacksmith", adapter.getLoreName(TechLevel.TL3, ModuleCategory.INDUSTRY, "WEARONS_FACTORY"));
        }

        @Test
        @DisplayName("maps Armored Truck to Supply Wagon")
        void armoredTruckToSupplyWagon() {
            assertEquals("Supply Wagon", adapter.getLoreName(TechLevel.TL3, ModuleCategory.VEHICLE, "ARMORED_TRUCK"));
        }

        @Test
        @DisplayName("maps Perimeter Fortification to Stone Wall")
        void perimeterFortificationToStoneWall() {
            assertEquals("Stone Wall", adapter.getLoreName(TechLevel.TL3, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION"));
        }

        @Test
        @DisplayName("maps Watchtower to Watchtower")
        void watchtower() {
            assertEquals("Watchtower", adapter.getLoreName(TechLevel.TL3, ModuleCategory.DEFENSE, "WATCHTOWER"));
        }

        @Test
        @DisplayName("Supply Wagon has correct stats")
        void supplyWagonStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.VEHICLE, "ARMORED_TRUCK");
            assertEquals(4, stats.get("speed"));
            assertEquals(12, stats.get("capacity"));
            assertEquals(6, stats.get("armor"));
        }
    }

    @Nested
    @DisplayName("TL4 Renaissance")
    class TL4Renaissance {

        @Test
        @DisplayName("maps Planetary Base to Fortress")
        void planetaryBaseToFortress() {
            assertEquals("Fortress", adapter.getLoreName(TechLevel.TL4, ModuleCategory.SETTLEMENT, "PLANETARY_BASE"));
        }

        @Test
        @DisplayName("Fortress has correct stats")
        void fortressStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.SETTLEMENT, "PLANETARY_BASE");
            assertEquals(600, stats.get("hp"));
            assertEquals(1800, stats.get("capacity"));
            assertEquals(250, stats.get("garrison"));
        }

        @Test
        @DisplayName("maps Tank to Trebuchet")
        void tankToTrebuchet() {
            assertEquals("Trebuchet", adapter.getLoreName(TechLevel.TL4, ModuleCategory.VEHICLE, "TANK"));
        }

        @Test
        @DisplayName("Trebuchet has cannon damage")
        void trebuchetCannonDamage() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.VEHICLE, "TANK");
            assertEquals("5d6", stats.get("cannonDamage"));
            assertEquals(20, stats.get("armor"));
        }

        @Test
        @DisplayName("maps Anti Tank Battery to Crossbow Emplacement")
        void antiTankBatteryToCrossbowEmplacement() {
            assertEquals("Crossbow Emplacement", adapter.getLoreName(TechLevel.TL4, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY"));
        }
    }

    @Nested
    @DisplayName("TL5 High Magic")
    class TL5HighMagic {

        @Test
        @DisplayName("maps Planetary Base to Magical Citadel")
        void planetaryBaseToMagicalCitadel() {
            assertEquals("Magical Citadel", adapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE"));
        }

        @Test
        @DisplayName("Magical Citadel has correct stats")
        void magicalCitadelStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE");
            assertEquals(1200, stats.get("hp"));
            assertEquals(3500, stats.get("capacity"));
            assertEquals(600, stats.get("garrison"));
        }

        @Test
        @DisplayName("maps Civilian Habitat to Mage Tower")
        void civilianHabitatToMageTower() {
            assertEquals("Mage Tower", adapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT"));
        }

        @Test
        @DisplayName("maps Planetary Battery to Spell Tower")
        void planetaryBatteryToSpellTower() {
            assertEquals("Spell Tower", adapter.getLoreName(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_BATTERY"));
        }

        @Test
        @DisplayName("Spell Tower has attack damage and range")
        void spellTowerStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_BATTERY");
            assertEquals("6d6", stats.get("attackDamage"));
            assertEquals(12, stats.get("range"));
        }

        @Test
        @DisplayName("maps Radar Station to Scrying Pool")
        void radarStationToScryingPool() {
            assertEquals("Scrying Pool", adapter.getLoreName(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION"));
        }

        @Test
        @DisplayName("maps Relic to Power Relic")
        void relicToPowerRelic() {
            assertEquals("Power Relic", adapter.getLoreName(TechLevel.TL5, ModuleCategory.DEFENSE, "RELIC"));
        }

        @Test
        @DisplayName("Power Relic has realm bonus")
        void powerRelicStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.DEFENSE, "RELIC");
            assertEquals(10, stats.get("realmBonus"));
            assertEquals(120, stats.get("captureTime"));
        }
    }

    @Nested
    @DisplayName("TL6 Divine")
    class TL6Divine {

        @Test
        @DisplayName("maps Mobile Infantry Base to Temple of the Gods")
        void mobileInfantryBaseToTemple() {
            assertEquals("Temple of the Gods", adapter.getLoreName(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE"));
        }

        @Test
        @DisplayName("Temple of the Gods has correct stats")
        void templeStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE");
            assertEquals(3500, stats.get("hp"));
            assertEquals(12000, stats.get("capacity"));
            assertEquals(2500, stats.get("garrison"));
        }

        @Test
        @DisplayName("maps Warship to Flying Fortress")
        void warshipToFlyingFortress() {
            assertEquals("Flying Fortress", adapter.getLoreName(TechLevel.TL6, ModuleCategory.VEHICLE, "WARSHIP"));
        }

        @Test
        @DisplayName("Flying Fortress has weapon damage")
        void flyingFortressStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.VEHICLE, "WARSHIP");
            assertEquals("9d6", stats.get("weaponDamage"));
            assertEquals(55, stats.get("armor"));
        }

        @Test
        @DisplayName("maps Planetary Shield to Divine Shield")
        void planetaryShieldToDivineShield() {
            assertEquals("Divine Shield", adapter.getLoreName(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_SHIELD"));
        }

        @Test
        @DisplayName("Divine Shield has shield HP")
        void divineShieldStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_SHIELD");
            assertEquals(6000, stats.get("shieldHP"));
        }

        @Test
        @DisplayName("maps Siege Tower to Siege Tower")
        void siegeTower() {
            assertEquals("Siege Tower", adapter.getLoreName(TechLevel.TL6, ModuleCategory.DEFENSE, "SIEGE_TOWER"));
        }

        @Test
        @DisplayName("Siege Tower bypasses walls")
        void siegeTowerBypassesWalls() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.DEFENSE, "SIEGE_TOWER");
            assertEquals(true, stats.get("wallBypass"));
        }

        @Test
        @DisplayName("maps Healing Tent to Healing Tent")
        void healingTent() {
            assertEquals("Healing Tent", adapter.getLoreName(TechLevel.TL6, ModuleCategory.SETTLEMENT, "HEALING_TENT"));
        }

        @Test
        @DisplayName("Healing Tent has heal rate and radius")
        void healingTentStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.SETTLEMENT, "HEALING_TENT");
            assertEquals(50, stats.get("healRate"));
            assertEquals(10, stats.get("radius"));
        }
    }

    @Nested
    @DisplayName("TL7 Mythical")
    class TL7Mythical {

        @Test
        @DisplayName("maps Federated Capital to Realm Gate")
        void federatedCapitalToRealmGate() {
            assertEquals("Realm Gate", adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL"));
        }

        @Test
        @DisplayName("Realm Gate has correct stats")
        void realmGateStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL");
            assertEquals(10000, stats.get("hp"));
            assertEquals(60000, stats.get("capacity"));
            assertEquals(10, stats.get("tradeBonus"));
        }

        @Test
        @DisplayName("maps Warship to Dragon Mount")
        void warshipToDragonMount() {
            assertEquals("Dragon Mount", adapter.getLoreName(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP"));
        }

        @Test
        @DisplayName("Dragon Mount has highest weapon damage")
        void dragonMountStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP");
            assertEquals("18d6", stats.get("weaponDamage"));
            assertEquals(90, stats.get("armor"));
            assertEquals(25000, stats.get("capacity"));
        }

        @Test
        @DisplayName("maps Planetary Shield to Realm Barrier")
        void planetaryShieldToRealmBarrier() {
            assertEquals("Realm Barrier", adapter.getLoreName(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD"));
        }

        @Test
        @DisplayName("Realm Barrier has highest AC")
        void realmBarrierStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD");
            assertEquals(42, stats.get("ac"));
            assertEquals(16000, stats.get("shieldHP"));
        }

        @Test
        @DisplayName("maps Mech Fortress to Legendary Fortress")
        void mechFortressToLegendaryFortress() {
            assertEquals("Legendary Fortress", adapter.getLoreName(TechLevel.TL7, ModuleCategory.DEFENSE, "MECH_FORTRESS"));
        }

        @Nested
        @DisplayName("RvR Structures")
        class RvRStructures {

            @Test
            @DisplayName("maps Darkness Falls Portal")
            void darknessFalls() {
                assertEquals("Darkness Falls Portal", adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "DARKNESS_FALLS"));
            }

            @Test
            @DisplayName("Darkness Falls has boss level and RvR enabled")
            void darknessFallsStats() {
                Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.SETTLEMENT, "DARKNESS_FALLS");
                assertEquals(70, stats.get("bossLevel"));
                assertEquals(true, stats.get("rvrEnabled"));
            }

            @Test
            @DisplayName("maps Labyrinth of the Minotaur")
            void labyrinth() {
                assertEquals("Labyrinth of the Minotaur", adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "LABYRINTH"));
            }

            @Test
            @DisplayName("Labyrinth has three-way RvR")
            void labyrinthStats() {
                Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.SETTLEMENT, "LABYRINTH");
                assertEquals(80, stats.get("bossLevel"));
                assertEquals(true, stats.get("threeWayRvr"));
            }

            @Test
            @DisplayName("maps Ellan Vannin Central Keep")
            void ellanVannin() {
                assertEquals("Ellan Vannin Central Keep", adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ELLAN_VANNIN"));
            }

            @Test
            @DisplayName("Ellan Vannin is a neutral zone")
            void ellanVanninStats() {
                Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ELLAN_VANNIN");
                assertEquals(true, stats.get("neutralZone"));
                assertEquals(5000, stats.get("capacity"));
            }
        }
    }

    @Nested
    @DisplayName("RvR Structures (TL3)")
    class RvRStructuresTL3 {

        @Test
        @DisplayName("maps Frontier Keep")
        void frontierKeep() {
            assertEquals("Frontier Keep", adapter.getLoreName(TechLevel.TL3, ModuleCategory.DEFENSE, "KEEP"));
        }

        @Test
        @DisplayName("Frontier Keep has correct stats")
        void frontierKeepStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.DEFENSE, "KEEP");
            assertEquals(5000, stats.get("hp"));
            assertEquals(200, stats.get("garrison"));
            assertEquals(60, stats.get("captureTime"));
        }

        @Test
        @DisplayName("maps Frontier Tower")
        void frontierTower() {
            assertEquals("Frontier Tower", adapter.getLoreName(TechLevel.TL3, ModuleCategory.DEFENSE, "TOWER"));
        }

        @Test
        @DisplayName("Frontier Tower has correct stats")
        void frontierTowerStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.DEFENSE, "TOWER");
            assertEquals(1500, stats.get("hp"));
            assertEquals(50, stats.get("garrison"));
            assertEquals(30, stats.get("captureTime"));
        }
    }

    @Nested
    @DisplayName("Available Modules")
    class AvailableModules {

        @Test
        @DisplayName("returns modules for TL3")
        void tl3Modules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL3);
            assertNotNull(modules);
            assertTrue(modules.contains("COLONY"));
            assertTrue(modules.contains("BARRACKS"));
            assertTrue(modules.contains("WEARONS_FACTORY"));
            assertTrue(modules.contains("ARMORED_TRUCK"));
            assertTrue(modules.contains("PERIMETER_FORTIFICATION"));
            assertTrue(modules.contains("WATCHTOWER"));
            assertTrue(modules.contains("KEEP"));
            assertTrue(modules.contains("TOWER"));
        }

        @Test
        @DisplayName("returns modules for TL7")
        void tl7Modules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL7);
            assertNotNull(modules);
            assertTrue(modules.contains("FEDERATED_CAPITAL"));
            assertTrue(modules.contains("WARSHIP"));
            assertTrue(modules.contains("PLANETARY_SHIELD"));
            assertTrue(modules.contains("MECH_FORTRESS"));
            assertTrue(modules.contains("DARKNESS_FALLS"));
            assertTrue(modules.contains("LABYRINTH"));
            assertTrue(modules.contains("ELLAN_VANNIN"));
        }

        @Test
        @DisplayName("returns filtered modules by category")
        void filteredModulesByCategory() {
            List<String> defenseModules = adapter.getAvailableModules(TechLevel.TL5, ModuleCategory.DEFENSE);
            assertNotNull(defenseModules);
            assertTrue(defenseModules.contains("PLANETARY_BATTERY"));
            assertTrue(defenseModules.contains("RELIC"));
        }

        @Test
        @DisplayName("returns empty list for non-existent category")
        void emptyListForNonExistentCategory() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL3, ModuleCategory.ANIMAL);
            assertNotNull(modules);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("TL3 has more modules than TL6")
        void tl3HasMoreModulesThanTL6() {
            List<String> tl3 = adapter.getAvailableModules(TechLevel.TL3);
            List<String> tl6 = adapter.getAvailableModules(TechLevel.TL6);
            assertTrue(tl3.size() > tl6.size());
        }
    }

    @Nested
    @DisplayName("Unknown Module Type")
    class UnknownModuleType {

        @Test
        @DisplayName("returns null for unknown module type")
        void unknownModuleType() {
            assertNull(adapter.getLoreName(TechLevel.TL3, ModuleCategory.SETTLEMENT, "UNKNOWN_MODULE"));
        }

        @Test
        @DisplayName("returns empty map for unknown module type")
        void unknownModuleTypeStats() {
            assertTrue(adapter.getLoreStats(TechLevel.TL3, ModuleCategory.SETTLEMENT, "UNKNOWN_MODULE").isEmpty());
        }
    }
}
