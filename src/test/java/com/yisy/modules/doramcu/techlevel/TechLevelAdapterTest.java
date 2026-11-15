package com.yisy.modules.doramcu.techlevel;

import com.yisy.modules.doramcu.babylon5.config.B5TechLevelAdapter;
import com.yisy.modules.doramcu.dnd5e.config.Dnd5eTechLevelAdapter;
import com.yisy.modules.doramcu.earth2026.techlevel.Earth2026TechLevelAdapter;
import com.yisy.modules.doramcu.star_trek_4x.config.StarTrekTechLevelAdapter;
import com.yisy.modules.doramcu.traveller.config.TravellerTechLevelAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tech Level Adapter Tests")
class TechLevelAdapterTest {

    // ==========================================
    // TechLevel Enum Tests
    // ==========================================

    @Nested
    @DisplayName("TechLevel Enum Tests")
    class TechLevelEnumTests {

        @Test
        @DisplayName("should have 13 tech levels from TL0 to TL12")
        void shouldHave13TechLevels() {
            assertEquals(13, TechLevel.values().length);
        }

        @Test
        @DisplayName("should return correct level numbers")
        void shouldReturnCorrectLevelNumbers() {
            assertEquals(0, TechLevel.TL0.getLevel());
            assertEquals(6, TechLevel.TL6.getLevel());
            assertEquals(12, TechLevel.TL12.getLevel());
        }

        @Test
        @DisplayName("should return correct names and descriptions")
        void shouldReturnCorrectNamesAndDescriptions() {
            assertEquals("Prehistoric", TechLevel.TL0.getName());
            assertTrue(TechLevel.TL0.getDescription().contains("Stone tools"));

            assertEquals("Medieval", TechLevel.TL2.getName());
            assertTrue(TechLevel.TL2.getDescription().contains("Castles"));

            assertEquals("Interstellar", TechLevel.TL8.getName());
            assertTrue(TechLevel.TL8.getDescription().contains("FTL"));

            assertEquals("Post-Singularity", TechLevel.TL12.getName());
            assertTrue(TechLevel.TL12.getDescription().contains("God-like"));
        }

        @Test
        @DisplayName("fromLevel should find matching tech level")
        void fromLevel_shouldFindMatchingTechLevel() {
            assertEquals(TechLevel.TL0, TechLevel.fromLevel(0));
            assertEquals(TechLevel.TL6, TechLevel.fromLevel(6));
            assertEquals(TechLevel.TL12, TechLevel.fromLevel(12));
        }

        @Test
        @DisplayName("fromLevel should return null for invalid level")
        void fromLevel_shouldReturnNullForInvalidLevel() {
            assertNull(TechLevel.fromLevel(-1));
            assertNull(TechLevel.fromLevel(13));
            assertNull(TechLevel.fromLevel(100));
        }
    }

    // ==========================================
    // ModuleCategory Enum Tests
    // ==========================================

    @Nested
    @DisplayName("ModuleCategory Enum Tests")
    class ModuleCategoryEnumTests {

        @Test
        @DisplayName("should have 21 categories (6 planet + 14 dungeon + 1 animal)")
        void shouldHave21Categories() {
            assertEquals(21, ModuleCategory.values().length);
        }

        @Test
        @DisplayName("should return correct display names")
        void shouldReturnCorrectDisplayNames() {
            assertEquals("Settlement", ModuleCategory.SETTLEMENT.getDisplayName());
            assertEquals("Industry", ModuleCategory.INDUSTRY.getDisplayName());
            assertEquals("Vehicle", ModuleCategory.VEHICLE.getDisplayName());
            assertEquals("Infrastructure", ModuleCategory.INFRASTRUCTURE.getDisplayName());
            assertEquals("Defense", ModuleCategory.DEFENSE.getDisplayName());
            assertEquals("Communication", ModuleCategory.COMMUNICATION.getDisplayName());
        }
    }

    // ==========================================
    // D&D 5e Adapter Tests
    // ==========================================

    @Nested
    @DisplayName("D&D 5e Tech Level Adapter Tests")
    class Dnd5eAdapterTests {

        private final Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();

        @Test
        @DisplayName("should support TL0 to TL4")
        void shouldSupportTL0ToTL4() {
            assertEquals(TechLevel.TL0, adapter.getMinTechLevel());
            assertEquals(TechLevel.TL4, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("should support tech levels within range")
        void supportsTechLevel_shouldReturnTrueForInRange() {
            assertTrue(adapter.supportsTechLevel(TechLevel.TL0));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL2));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL4));
        }

        @Test
        @DisplayName("should not support tech levels outside range")
        void supportsTechLevel_shouldReturnFalseForOutOfRange() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL5));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL6));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL12));
        }

        @Test
        @DisplayName("should not support null tech level")
        void supportsTechLevel_shouldReturnFalseForNull() {
            assertFalse(adapter.supportsTechLevel(null));
        }

        @Test
        @DisplayName("TL2 SETTLEMENT should return Stone Castle")
        void getLoreName_TL2_SETTLEMENT_shouldReturnStoneCastle() {
            String name = adapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertEquals("Stone Castle", name);
        }

        @Test
        @DisplayName("TL1 SETTLEMENT should return Stone Hut Village")
        void getLoreName_TL1_SETTLEMENT_shouldReturnStoneHutVillage() {
            String name = adapter.getLoreName(TechLevel.TL1, ModuleCategory.SETTLEMENT, "STONE_HUT_VILLAGE");
            assertEquals("Stone Hut Village", name);
        }

        @Test
        @DisplayName("TL2 VEHICLE should return Horse-Drawn Cart")
        void getLoreName_TL2_VEHICLE_shouldReturnHorseDrawnCart() {
            String name = adapter.getLoreName(TechLevel.TL2, ModuleCategory.VEHICLE, "HORSE_DRAWN_CART");
            assertEquals("Horse-Drawn Cart", name);
        }

        @Test
        @DisplayName("TL3 INDUSTRY should return Blacksmith Forge")
        void getLoreName_TL3_INDUSTRY_shouldReturnBlacksmithForge() {
            String name = adapter.getLoreName(TechLevel.TL3, ModuleCategory.INDUSTRY, "GUNPOWDER_WORKS");
            assertEquals("Alchemist's Fireworks", name);
        }

        @Test
        @DisplayName("TL2 INDUSTRY should return Blacksmith Forge")
        void getLoreName_TL2_INDUSTRY_shouldReturnBlacksmithForge() {
            String name = adapter.getLoreName(TechLevel.TL2, ModuleCategory.INDUSTRY, "BLACKSMITH_FORGE");
            assertEquals("Blacksmith Forge", name);
        }

        @Test
        @DisplayName("TL4 SETTLEMENT should return Industrial City")
        void getLoreName_TL4_SETTLEMENT_shouldReturnIndustrialCity() {
            String name = adapter.getLoreName(TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY");
            assertEquals("Industrial City", name);
        }

        @Test
        @DisplayName("should return null for unsupported tech level")
        void getLoreName_unsupportedTechLevel_shouldReturnNull() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertNull(name);
        }

        @Test
        @DisplayName("should return null for unknown generic type")
        void getLoreName_unknownGenericType_shouldReturnNull() {
            String name = adapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "UNKNOWN_TYPE");
            assertNull(name);
        }

        @Test
        @DisplayName("should return D&D-specific stats")
        void getLoreStats_shouldReturnDnDStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertNotNull(stats);
            assertTrue(stats.containsKey("hp"));
            assertEquals(200, stats.get("hp"));
            assertTrue(stats.containsKey("garrison"));
        }

        @Test
        @DisplayName("should return empty map for unsupported tech level")
        void getLoreStats_unsupportedTechLevel_shouldReturnEmptyMap() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL5, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("should return available modules for TL2")
        void getAvailableModules_TL2_shouldReturnModules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL2);
            assertNotNull(modules);
            assertFalse(modules.isEmpty());
            assertTrue(modules.contains("STONE_CASTLE"));
            assertTrue(modules.contains("HORSE_DRAWN_CART"));
            assertTrue(modules.contains("BLACKSMITH_FORGE"));
        }

        @Test
        @DisplayName("should return empty list for unsupported tech level")
        void getAvailableModules_unsupportedTechLevel_shouldReturnEmptyList() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL5);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("should filter modules by category")
        void getAvailableModules_byCategory_shouldFilterCorrectly() {
            List<String> settlements = adapter.getAvailableModules(TechLevel.TL2, ModuleCategory.SETTLEMENT);
            assertNotNull(settlements);
            assertFalse(settlements.isEmpty());
            assertTrue(settlements.contains("STONE_CASTLE"));
            assertTrue(settlements.contains("MARKET_TOWN"));
            assertFalse(settlements.contains("HORSE_DRAWN_CART"));
        }

        @Test
        @DisplayName("TL0 should have prehistoric modules")
        void TL0_shouldHavePrehistoricModules() {
            String name = adapter.getLoreName(TechLevel.TL0, ModuleCategory.SETTLEMENT, "CAVE_DWELLING");
            assertEquals("Cave Dwelling", name);

            String vehicle = adapter.getLoreName(TechLevel.TL0, ModuleCategory.VEHICLE, "RAFT");
            assertEquals("Wooden Raft", vehicle);
        }

        @Test
        @DisplayName("TL3 should have Renaissance modules")
        void TL3_shouldHaveRenaissanceModules() {
            String name = adapter.getLoreName(TechLevel.TL3, ModuleCategory.VEHICLE, "GALLEON");
            assertEquals("Galleon", name);

            String defense = adapter.getLoreName(TechLevel.TL3, ModuleCategory.DEFENSE, "ARTILLERY_FORT");
            assertEquals("Artillery Fort", defense);
        }
    }

    // ==========================================
    // Traveller Adapter Tests
    // ==========================================

    @Nested
    @DisplayName("Traveller Tech Level Adapter Tests")
    class TravellerAdapterTests {

        private final TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();

        @Test
        @DisplayName("should support TL0 to TL12 (full range)")
        void shouldSupportFullRange() {
            assertEquals(TechLevel.TL0, adapter.getMinTechLevel());
            assertEquals(TechLevel.TL12, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("should support all tech levels")
        void supportsTechLevel_shouldReturnTrueForAllLevels() {
            for (TechLevel tl : TechLevel.values()) {
                assertTrue(adapter.supportsTechLevel(tl), "Should support " + tl.getName());
            }
        }

        @Test
        @DisplayName("TL4 VEHICLE should return Bus Depot")
        void getLoreName_TL4_VEHICLE_shouldReturnBusDepot() {
            String name = adapter.getLoreName(TechLevel.TL4, ModuleCategory.VEHICLE, "BUS_DEPOT");
            assertEquals("Bus Depot", name);
        }

        @Test
        @DisplayName("TL6 INFRASTRUCTURE should return Orbital Elevator")
        void getLoreName_TL6_INFRASTRUCTURE_shouldReturnOrbitalElevator() {
            String name = adapter.getLoreName(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR");
            assertEquals("Orbital Elevator", name);
        }

        @Test
        @DisplayName("TL8 INFRASTRUCTURE should return Jump Gate Station")
        void getLoreName_TL8_INFRASTRUCTURE_shouldReturnJumpGateStation() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION");
            assertEquals("Jump Gate Station", name);
        }

        @Test
        @DisplayName("TL10 SETTLEMENT should return Megastructure Hub")
        void getLoreName_TL10_SETTLEMENT_shouldReturnMegastructureHub() {
            String name = adapter.getLoreName(TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB");
            assertEquals("Megastructure Hub", name);
        }

        @Test
        @DisplayName("TL6 VEHICLE should return Hover Transport")
        void getLoreName_TL6_VEHICLE_shouldReturnHoverTransport() {
            String name = adapter.getLoreName(TechLevel.TL6, ModuleCategory.VEHICLE, "HOVER_TRANSPORT");
            assertEquals("Hover Transport", name);
        }

        @Test
        @DisplayName("TL2 SETTLEMENT should return Feudal Hold")
        void getLoreName_TL2_SETTLEMENT_shouldReturnFeudalHold() {
            String name = adapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertEquals("Feudal Hold", name);
        }

        @Test
        @DisplayName("should return Traveller-specific stats")
        void getLoreStats_shouldReturnTravellerStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY");
            assertNotNull(stats);
            assertTrue(stats.containsKey("pop"));
            assertEquals(50000, stats.get("pop"));
        }

        @Test
        @DisplayName("TL12 should have post-singularity modules")
        void TL12_shouldHavePostSingularityModules() {
            String name = adapter.getLoreName(TechLevel.TL12, ModuleCategory.SETTLEMENT, "OMNIVERSAL_THRONE");
            assertEquals("Omniversal Throne", name);

            String defense = adapter.getLoreName(TechLevel.TL12, ModuleCategory.DEFENSE, "EXISTENCE_SHIELD");
            assertEquals("Existence Shield", defense);
        }

        @Test
        @DisplayName("TL5 should have modern modules")
        void TL5_shouldHaveModernModules() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS");
            assertEquals("Metropolis", name);

            String vehicle = adapter.getLoreName(TechLevel.TL5, ModuleCategory.VEHICLE, "COMMERCIAL_AIRLINER");
            assertEquals("Commercial Airliner", vehicle);
        }

        @Test
        @DisplayName("should return available modules for TL8")
        void getAvailableModules_TL8_shouldReturnModules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL8);
            assertNotNull(modules);
            assertFalse(modules.isEmpty());
            assertTrue(modules.contains("JUMP_GATE_STATION"));
            assertTrue(modules.contains("FTL_CRUISER"));
        }

        @Test
        @DisplayName("should filter modules by category for TL6")
        void getAvailableModules_TL6_byCategory_shouldFilterCorrectly() {
            List<String> vehicles = adapter.getAvailableModules(TechLevel.TL6, ModuleCategory.VEHICLE);
            assertNotNull(vehicles);
            assertFalse(vehicles.isEmpty());
            assertTrue(vehicles.contains("HOVER_TRANSPORT"));
            assertTrue(vehicles.contains("ORBITAL_ROCKET"));
        }
    }

    // ==========================================
    // Star Trek 4X Adapter Tests
    // ==========================================

    @Nested
    @DisplayName("Star Trek 4X Tech Level Adapter Tests")
    class StarTrekAdapterTests {

        private final StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();

        @Test
        @DisplayName("should support TL6 to TL10")
        void shouldSupportTL6ToTL10() {
            assertEquals(TechLevel.TL6, adapter.getMinTechLevel());
            assertEquals(TechLevel.TL10, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("should support tech levels within range")
        void supportsTechLevel_shouldReturnTrueForInRange() {
            assertTrue(adapter.supportsTechLevel(TechLevel.TL6));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL8));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL10));
        }

        @Test
        @DisplayName("should not support tech levels below TL6")
        void supportsTechLevel_shouldReturnFalseForBelowRange() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL0));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL4));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL5));
        }

        @Test
        @DisplayName("should not support tech levels above TL10")
        void supportsTechLevel_shouldReturnFalseForAboveRange() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL11));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL12));
        }

        @Test
        @DisplayName("TL7 SETTLEMENT should return Colonial Outpost")
        void getLoreName_TL7_SETTLEMENT_shouldReturnColonialOutpost() {
            String name = adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST");
            assertEquals("Colonial Outpost", name);
        }

        @Test
        @DisplayName("TL8 VEHICLE should return Transport Shuttle")
        void getLoreName_TL8_VEHICLE_shouldReturnTransportShuttle() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "TRANSPORT_SHUTTLE");
            assertEquals("Transport Shuttle", name);
        }

        @Test
        @DisplayName("TL9 SETTLEMENT should return Starbase")
        void getLoreName_TL9_SETTLEMENT_shouldReturnStarbase() {
            String name = adapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");
            assertEquals("Starbase", name);
        }

        @Test
        @DisplayName("TL10 VEHICLE should return Transwarp Gateway")
        void getLoreName_TL10_VEHICLE_shouldReturnTranswarpGateway() {
            String name = adapter.getLoreName(TechLevel.TL10, ModuleCategory.VEHICLE, "WARP_GATE");
            assertEquals("Transwarp Gateway", name);
        }

        @Test
        @DisplayName("TL8 INFRASTRUCTURE should return Warp Gate Station")
        void getLoreName_TL8_INFRASTRUCTURE_shouldReturnWarpGateStation() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION");
            assertEquals("Warp Gate Station", name);
        }

        @Test
        @DisplayName("should return null for unsupported tech level")
        void getLoreName_unsupportedTechLevel_shouldReturnNull() {
            String name = adapter.getLoreName(TechLevel.TL4, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            assertNull(name);
        }

        @Test
        @DisplayName("should return Star Trek-specific stats")
        void getLoreStats_shouldReturnStarTrekStats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            assertNotNull(stats);
            assertTrue(stats.containsKey("shieldHP"));
            assertEquals(500, stats.get("shieldHP"));
        }

        @Test
        @DisplayName("should return empty map for unsupported tech level")
        void getLoreStats_unsupportedTechLevel_shouldReturnEmptyMap() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.SETTLEMENT, "HARBOR_CITY");
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("should return available modules for TL9")
        void getAvailableModules_TL9_shouldReturnModules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL9);
            assertNotNull(modules);
            assertFalse(modules.isEmpty());
            assertTrue(modules.contains("STARBASE"));
            assertTrue(modules.contains("DREADNOUGHT"));
        }

        @Test
        @DisplayName("should return empty list for unsupported tech level")
        void getAvailableModules_unsupportedTechLevel_shouldReturnEmptyList() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL2);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("TL8 VEHICLE FTL_CRUISER should be Constitution-Class")
        void getLoreName_TL8_FTL_CRUISER_shouldReturnConstitutionClass() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            assertEquals("Constitution-Class Cruiser", name);
        }

        @Test
        @DisplayName("TL8 DEFENSE should return Phaser Array")
        void getLoreName_TL8_DEFENSE_shouldReturnPhaserArray() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON");
            assertEquals("Phaser Array", name);
        }
    }

    // ==========================================
    // Babylon 5 Adapter Tests
    // ==========================================

    @Nested
    @DisplayName("Babylon 5 Tech Level Adapter Tests")
    class B5AdapterTests {

        private final B5TechLevelAdapter adapter = new B5TechLevelAdapter();

        @Test
        @DisplayName("should support TL7 to TL9")
        void shouldSupportTL7ToTL9() {
            assertEquals(TechLevel.TL7, adapter.getMinTechLevel());
            assertEquals(TechLevel.TL9, adapter.getMaxTechLevel());
        }

        @Test
        @DisplayName("should support tech levels within range")
        void supportsTechLevel_shouldReturnTrueForInRange() {
            assertTrue(adapter.supportsTechLevel(TechLevel.TL7));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL8));
            assertTrue(adapter.supportsTechLevel(TechLevel.TL9));
        }

        @Test
        @DisplayName("should not support tech levels below TL7")
        void supportsTechLevel_shouldReturnFalseForBelowRange() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL0));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL5));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL6));
        }

        @Test
        @DisplayName("should not support tech levels above TL9")
        void supportsTechLevel_shouldReturnFalseForAboveRange() {
            assertFalse(adapter.supportsTechLevel(TechLevel.TL10));
            assertFalse(adapter.supportsTechLevel(TechLevel.TL12));
        }

        @Test
        @DisplayName("TL7 SETTLEMENT should return Ground Station")
        void getLoreName_TL7_SETTLEMENT_shouldReturnGroundStation() {
            String name = adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST");
            assertEquals("Ground Station", name);
        }

        @Test
        @DisplayName("TL8 SETTLEMENT should return Spaceport")
        void getLoreName_TL8_SETTLEMENT_shouldReturnSpaceport() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION");
            assertEquals("Spaceport", name);
        }

        @Test
        @DisplayName("TL9 SETTLEMENT should return Babylon-class Station")
        void getLoreName_TL9_SETTLEMENT_shouldReturnBabylonClassStation() {
            String name = adapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");
            assertEquals("Babylon-class Station", name);
        }

        @Test
        @DisplayName("TL8 VEHICLE should return Battlestar")
        void getLoreName_TL8_VEHICLE_shouldReturnBattlestar() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            assertEquals("Battlestar", name);
        }

        @Test
        @DisplayName("TL9 VEHICLE should return Shadow Warship")
        void getLoreName_TL9_VEHICLE_shouldReturnShadowWarship() {
            String name = adapter.getLoreName(TechLevel.TL9, ModuleCategory.VEHICLE, "DREADNOUGHT");
            assertEquals("Shadow Warship", name);
        }

        @Test
        @DisplayName("should return null for unsupported tech level")
        void getLoreName_unsupportedTechLevel_shouldReturnNull() {
            String name = adapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS");
            assertNull(name);
        }

        @Test
        @DisplayName("should return B5-specific stats")
        void getLoreStats_shouldReturnB5Stats() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");
            assertNotNull(stats);
            assertTrue(stats.containsKey("shieldHP"));
            assertEquals(10000, stats.get("shieldHP"));
        }

        @Test
        @DisplayName("should return empty map for unsupported tech level")
        void getLoreStats_unsupportedTechLevel_shouldReturnEmptyMap() {
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY");
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("should return available modules for TL8")
        void getAvailableModules_TL8_shouldReturnModules() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL8);
            assertNotNull(modules);
            assertFalse(modules.isEmpty());
            assertTrue(modules.contains("JUMP_GATE_STATION"));
            assertTrue(modules.contains("FTL_CRUISER"));
        }

        @Test
        @DisplayName("should return empty list for unsupported tech level")
        void getAvailableModules_unsupportedTechLevel_shouldReturnEmptyList() {
            List<String> modules = adapter.getAvailableModules(TechLevel.TL3);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("TL8 DEFENSE should return Laser Battery")
        void getLoreName_TL8_DEFENSE_shouldReturnLaserBattery() {
            String name = adapter.getLoreName(TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON");
            assertEquals("Laser Battery", name);
        }

        @Test
        @DisplayName("TL7 should have interplanetary modules")
        void TL7_shouldHaveInterplanetaryModules() {
            String name = adapter.getLoreName(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR");
            assertEquals("Fusion Reactor", name);

            String vehicle = adapter.getLoreName(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP");
            assertEquals("Interplanetary Transport", vehicle);
        }
    }

    // ==========================================
    // Cross-Module Comparison Tests
    // ==========================================

    @Nested
    @DisplayName("Cross-Module Comparison Tests")
    class CrossModuleTests {

        private final Dnd5eTechLevelAdapter dndAdapter = new Dnd5eTechLevelAdapter();
        private final TravellerTechLevelAdapter travellerAdapter = new TravellerTechLevelAdapter();
        private final StarTrekTechLevelAdapter starTrekAdapter = new StarTrekTechLevelAdapter();
        private final B5TechLevelAdapter b5Adapter = new B5TechLevelAdapter();

        @Test
        @DisplayName("same tech level should return different results for different games")
        void sameTechLevel_shouldReturnDifferentResults() {
            // TL8 INFRASTRUCTURE JUMP_GATE_STATION should differ across games
            String travellerName = travellerAdapter.getLoreName(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION");
            String starTrekName = starTrekAdapter.getLoreName(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION");
            String b5Name = b5Adapter.getLoreName(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION");

            assertEquals("Jump Gate Station", travellerName);
            assertEquals("Warp Gate Station", starTrekName);
            assertEquals("Jump Gate Station", b5Name);
            // Star Trek differs from Traveller
            assertNotEquals(travellerName, starTrekName);
        }

        @Test
        @DisplayName("TL8 VEHICLE FTL_CRUISER should differ across games")
        void TL8_FTL_CRUISER_shouldDifferAcrossGames() {
            String travellerName = travellerAdapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            String starTrekName = starTrekAdapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            String b5Name = b5Adapter.getLoreName(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");

            assertEquals("FTL Cruiser", travellerName);
            assertEquals("Constitution-Class Cruiser", starTrekName);
            assertEquals("Battlestar", b5Name);
            // All three should be different
            assertNotEquals(travellerName, starTrekName);
            assertNotEquals(travellerName, b5Name);
            assertNotEquals(starTrekName, b5Name);
        }

        @Test
        @DisplayName("D&D should not support TL8")
        void DnD_shouldNotSupportTL8() {
            assertFalse(dndAdapter.supportsTechLevel(TechLevel.TL8));
            assertNull(dndAdapter.getLoreName(TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION"));
            assertTrue(dndAdapter.getAvailableModules(TechLevel.TL8).isEmpty());
        }

        @Test
        @DisplayName("Star Trek should not support TL2")
        void StarTrek_shouldNotSupportTL2() {
            assertFalse(starTrekAdapter.supportsTechLevel(TechLevel.TL2));
            assertNull(starTrekAdapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE"));
        }

        @Test
        @DisplayName("B5 should not support TL6")
        void B5_shouldNotSupportTL6() {
            assertFalse(b5Adapter.supportsTechLevel(TechLevel.TL6));
            assertNull(b5Adapter.getLoreName(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY"));
        }

        @Test
        @DisplayName("Traveller should support all tech levels")
        void Traveller_shouldSupportAllTechLevels() {
            for (TechLevel tl : TechLevel.values()) {
                assertTrue(travellerAdapter.supportsTechLevel(tl), "Traveller should support " + tl.getName());
            }
        }

        @Test
        @DisplayName("TL9 SETTLEMENT STARBASE should differ across games")
        void TL9_STARBASE_shouldDifferAcrossGames() {
            String travellerName = travellerAdapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");
            String starTrekName = starTrekAdapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");
            String b5Name = b5Adapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE");

            assertEquals("Starbase", travellerName);
            assertEquals("Starbase", starTrekName);
            assertEquals("Babylon-class Station", b5Name);
            // B5 should differ
            assertNotEquals(travellerName, b5Name);
        }

        @Test
        @DisplayName("TL7 SETTLEMENT should differ across games")
        void TL7_SETTLEMENT_shouldDifferAcrossGames() {
            String travellerName = travellerAdapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST");
            String starTrekName = starTrekAdapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST");
            String b5Name = b5Adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST");

            assertEquals("Colonial Outpost", travellerName);
            assertEquals("Colonial Outpost", starTrekName);
            assertEquals("Ground Station", b5Name);
            // B5 should differ
            assertNotEquals(travellerName, b5Name);
        }

        @Test
        @DisplayName("TL2 SETTLEMENT should differ between D&D and Traveller")
        void TL2_SETTLEMENT_shouldDifferBetweenDnDAndTraveller() {
            String dndName = dndAdapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE");
            String travellerName = travellerAdapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE");

            assertEquals("Stone Castle", dndName);
            assertEquals("Feudal Hold", travellerName);
            assertNotEquals(dndName, travellerName);
        }

        @Test
        @DisplayName("stats should differ across games for same module")
        void stats_shouldDifferAcrossGames() {
            // TL8 VEHICLE FTL_CRUISER stats comparison
            Map<String, Object> travellerStats = travellerAdapter.getLoreStats(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            Map<String, Object> starTrekStats = starTrekAdapter.getLoreStats(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");
            Map<String, Object> b5Stats = b5Adapter.getLoreStats(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER");

            // All should have speed
            assertTrue(travellerStats.containsKey("speed"));
            assertTrue(starTrekStats.containsKey("speed"));
            assertTrue(b5Stats.containsKey("speed"));

            // Star Trek and B5 should have shieldHP, Traveller should not
            assertTrue(starTrekStats.containsKey("shieldHP"));
            assertTrue(b5Stats.containsKey("shieldHP"));
            assertFalse(travellerStats.containsKey("shieldHP"));
        }
    }

    // ==========================================
    // Edge Case Tests
    // ==========================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("null tech level should be handled gracefully")
        void nullTechLevel_shouldBeHandledGracefully() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertNull(adapter.getLoreName(null, ModuleCategory.SETTLEMENT, "STONE_CASTLE"));
            assertTrue(adapter.getLoreStats(null, ModuleCategory.SETTLEMENT, "STONE_CASTLE").isEmpty());
            assertTrue(adapter.getAvailableModules(null).isEmpty());
            assertFalse(adapter.supportsTechLevel(null));
        }

        @Test
        @DisplayName("null category should throw NPE (key generation)")
        void nullCategory_shouldThrowNPE() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertThrows(NullPointerException.class, () ->
                adapter.getLoreName(TechLevel.TL2, null, "STONE_CASTLE"));
        }

        @Test
        @DisplayName("null genericType should return null lore name")
        void nullGenericType_shouldReturnNullLoreName() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertNull(adapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, null));
        }

        @Test
        @DisplayName("unknown generic type should return null")
        void unknownGenericType_shouldReturnNull() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertNull(adapter.getLoreName(TechLevel.TL2, ModuleCategory.SETTLEMENT, "NONEXISTENT_MODULE"));
        }

        @Test
        @DisplayName("unknown generic type should return empty stats")
        void unknownGenericType_shouldReturnEmptyStats() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.SETTLEMENT, "NONEXISTENT_MODULE");
            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("getAvailableModules with null category should throw NPE")
        void getAvailableModules_nullCategory_shouldThrowNPE() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertThrows(NullPointerException.class, () ->
                adapter.getAvailableModules(TechLevel.TL2, null));
        }

        @Test
        @DisplayName("boundary tech levels should work correctly")
        void boundaryTechLevels_shouldWorkCorrectly() {
            // D&D boundary: TL0 and TL4
            Dnd5eTechLevelAdapter dndAdapter = new Dnd5eTechLevelAdapter();
            assertNotNull(dndAdapter.getLoreName(TechLevel.TL0, ModuleCategory.SETTLEMENT, "CAVE_DWELLING"));
            assertNotNull(dndAdapter.getLoreName(TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY"));

            // Star Trek boundary: TL6 and TL10
            StarTrekTechLevelAdapter starTrekAdapter = new StarTrekTechLevelAdapter();
            assertNotNull(starTrekAdapter.getLoreName(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY"));
            assertNotNull(starTrekAdapter.getLoreName(TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB"));

            // B5 boundary: TL7 and TL9
            B5TechLevelAdapter b5Adapter = new B5TechLevelAdapter();
            assertNotNull(b5Adapter.getLoreName(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST"));
            assertNotNull(b5Adapter.getLoreName(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE"));
        }

        @Test
        @DisplayName("just outside boundary should return null")
        void justOutsideBoundary_shouldReturnNull() {
            // D&D: TL5 is just outside
            Dnd5eTechLevelAdapter dndAdapter = new Dnd5eTechLevelAdapter();
            assertNull(dndAdapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS"));

            // Star Trek: TL5 is just below, TL11 is just above
            StarTrekTechLevelAdapter starTrekAdapter = new StarTrekTechLevelAdapter();
            assertNull(starTrekAdapter.getLoreName(TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS"));
            assertNull(starTrekAdapter.getLoreName(TechLevel.TL11, ModuleCategory.SETTLEMENT, "DIMENSIONAL_HUB"));

            // B5: TL6 is just below, TL10 is just above
            B5TechLevelAdapter b5Adapter = new B5TechLevelAdapter();
            assertNull(b5Adapter.getLoreName(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY"));
            assertNull(b5Adapter.getLoreName(TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB"));
        }
    }

    // ==========================================
    // Module Count Tests
    // ==========================================

    @Nested
    @DisplayName("Module Count Tests")
    class ModuleCountTests {

        @Test
        @DisplayName("D&D should have modules for all supported tech levels")
        void DnD_shouldHaveModulesForAllSupportedLevels() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            for (int i = 0; i <= 4; i++) {
                TechLevel tl = TechLevel.fromLevel(i);
                List<String> modules = adapter.getAvailableModules(tl);
                assertFalse(modules.isEmpty(), "D&D should have modules for " + tl.getName());
            }
        }

        @Test
        @DisplayName("Traveller should have modules for all tech levels")
        void Traveller_shouldHaveModulesForAllLevels() {
            TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();
            for (TechLevel tl : TechLevel.values()) {
                List<String> modules = adapter.getAvailableModules(tl);
                assertFalse(modules.isEmpty(), "Traveller should have modules for " + tl.getName());
            }
        }

        @Test
        @DisplayName("Star Trek should have modules for TL6 to TL10")
        void StarTrek_shouldHaveModulesForSupportedLevels() {
            StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();
            for (int i = 6; i <= 10; i++) {
                TechLevel tl = TechLevel.fromLevel(i);
                List<String> modules = adapter.getAvailableModules(tl);
                assertFalse(modules.isEmpty(), "Star Trek should have modules for " + tl.getName());
            }
        }

        @Test
        @DisplayName("B5 should have modules for TL7 to TL9")
        void B5_shouldHaveModulesForSupportedLevels() {
            B5TechLevelAdapter adapter = new B5TechLevelAdapter();
            for (int i = 7; i <= 9; i++) {
                TechLevel tl = TechLevel.fromLevel(i);
                List<String> modules = adapter.getAvailableModules(tl);
                assertFalse(modules.isEmpty(), "B5 should have modules for " + tl.getName());
            }
        }

        @Test
        @DisplayName("each adapter should have modules in multiple categories")
        void eachAdapter_shouldHaveMultipleCategories() {
            // D&D TL2 should have modules in the original 6 planet-building categories
            ModuleCategory[] planetCategories = {
                    ModuleCategory.SETTLEMENT, ModuleCategory.INDUSTRY, ModuleCategory.VEHICLE,
                    ModuleCategory.INFRASTRUCTURE, ModuleCategory.DEFENSE, ModuleCategory.COMMUNICATION
            };
            Dnd5eTechLevelAdapter dndAdapter = new Dnd5eTechLevelAdapter();
            for (ModuleCategory cat : planetCategories) {
                List<String> modules = dndAdapter.getAvailableModules(TechLevel.TL2, cat);
                assertFalse(modules.isEmpty(), "D&D TL2 should have " + cat.getDisplayName() + " modules");
            }
        }
    }

    // ==========================================
    // D&D Dungeon Object Tests
    // ==========================================

    @Nested
    @DisplayName("D&D Dungeon Object Tests")
    class DndDungeonObjectTests {

        private final Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();

        // --- Dungeon Room Tests ---

        @Test
        @DisplayName("TL0 should have Cave Entrance room")
        void TL0_dungeonRoom_caveEntrance() {
            assertEquals("Cave Entrance", adapter.getLoreName(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "CAVE_ENTRANCE"));
        }

        @Test
        @DisplayName("TL2 should have Boss Chamber")
        void TL2_dungeonRoom_bossChamber() {
            assertEquals("Boss Chamber", adapter.getLoreName(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "BOSS_CHAMBER"));
        }

        @Test
        @DisplayName("TL3 should have Arcane Laboratory")
        void TL3_dungeonRoom_arcaneLab() {
            assertEquals("Arcane Laboratory", adapter.getLoreName(TechLevel.TL3, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB"));
        }

        // --- Furniture Tests ---

        @Test
        @DisplayName("TL0 should have Stone Altar")
        void TL0_furniture_stoneAltar() {
            assertEquals("Stone Altar", adapter.getLoreName(TechLevel.TL0, ModuleCategory.FURNITURE, "STONE_ALTAR"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.FURNITURE, "STONE_ALTAR");
            assertNotNull(stats);
            assertEquals(20, stats.get("hp"));
        }

        @Test
        @DisplayName("TL1 should have Wooden Chair")
        void TL1_furniture_woodenChair() {
            assertEquals("Wooden Chair", adapter.getLoreName(TechLevel.TL1, ModuleCategory.FURNITURE, "WOODEN_CHAIR"));
        }

        @Test
        @DisplayName("TL2 should have Golden Throne")
        void TL2_furniture_goldenThrone() {
            assertEquals("Golden Throne", adapter.getLoreName(TechLevel.TL2, ModuleCategory.FURNITURE, "GOLDEN_THRONE"));
        }

        // --- Treasure Tests ---

        @Test
        @DisplayName("TL1 should have Gold Coins")
        void TL1_treasure_goldCoins() {
            assertEquals("Gold Coins", adapter.getLoreName(TechLevel.TL1, ModuleCategory.TREASURE, "GOLD_COINS"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL1, ModuleCategory.TREASURE, "GOLD_COINS");
            assertNotNull(stats);
            assertEquals(100, stats.get("gpValue"));
            assertEquals("Common", stats.get("rarity"));
        }

        @Test
        @DisplayName("TL2 should have Crown Jewels")
        void TL2_treasure_crownJewels() {
            assertEquals("Crown Jewels", adapter.getLoreName(TechLevel.TL2, ModuleCategory.TREASURE, "CROWN_JEWELS"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.TREASURE, "CROWN_JEWELS");
            assertEquals("Very Rare", stats.get("rarity"));
        }

        @Test
        @DisplayName("TL4 should have Dragon's Hoard")
        void TL4_treasure_dragonHoard() {
            assertEquals("Dragon's Hoard", adapter.getLoreName(TechLevel.TL4, ModuleCategory.TREASURE, "DRAGON_HOARD"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.TREASURE, "DRAGON_HOARD");
            assertEquals("Legendary", stats.get("rarity"));
            assertEquals(50000, stats.get("gpValue"));
        }

        // --- Trap Tests ---

        @Test
        @DisplayName("TL0 should have Spike Pit trap")
        void TL0_trap_spikePit() {
            assertEquals("Spike Pit", adapter.getLoreName(TechLevel.TL0, ModuleCategory.TRAP, "SPIKE_PIT"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.TRAP, "SPIKE_PIT");
            assertEquals(10, stats.get("dc"));
            assertEquals("1d8", stats.get("damage"));
            assertEquals("Piercing", stats.get("damageType"));
        }

        @Test
        @DisplayName("TL1 should have Poison Dart Trap")
        void TL1_trap_poisonDart() {
            assertEquals("Poison Dart Trap", adapter.getLoreName(TechLevel.TL1, ModuleCategory.TRAP, "POISON_DART"));
        }

        @Test
        @DisplayName("TL2 should have Magic Trap")
        void TL2_trap_magicTrap() {
            assertEquals("Magic Trap", adapter.getLoreName(TechLevel.TL2, ModuleCategory.TRAP, "MAGIC_TRAP"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.TRAP, "MAGIC_TRAP");
            assertEquals("Force", stats.get("damageType"));
        }

        // --- Door Tests ---

        @Test
        @DisplayName("TL0 should have Wooden Door")
        void TL0_door_woodenDoor() {
            assertEquals("Wooden Door", adapter.getLoreName(TechLevel.TL0, ModuleCategory.DOOR, "WOODEN_DOOR"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.DOOR, "WOODEN_DOOR");
            assertEquals(15, stats.get("hp"));
        }

        @Test
        @DisplayName("TL2 should have Portcullis")
        void TL2_door_portcullis() {
            assertEquals("Portcullis", adapter.getLoreName(TechLevel.TL2, ModuleCategory.DOOR, "PORTCULLIS"));
        }

        // --- Chest Tests ---

        @Test
        @DisplayName("TL0 should have Wooden Box")
        void TL0_chest_woodenBox() {
            assertEquals("Wooden Box", adapter.getLoreName(TechLevel.TL0, ModuleCategory.CHEST, "WOODEN_BOX"));
        }

        @Test
        @DisplayName("TL2 should have Treasure Chest")
        void TL2_chest_treasureChest() {
            assertEquals("Treasure Chest", adapter.getLoreName(TechLevel.TL2, ModuleCategory.CHEST, "TREASURE_CHEST"));
        }

        // --- Monster Tests ---

        @Test
        @DisplayName("TL0 should have Giant Spider")
        void TL0_monster_giantSpider() {
            assertEquals("Giant Spider", adapter.getLoreName(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_SPIDER"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_SPIDER");
            assertEquals(0.5, stats.get("cr"));
            assertEquals(26, stats.get("hp"));
            assertEquals("Beast", stats.get("type"));
        }

        @Test
        @DisplayName("TL1 should have Goblin")
        void TL1_monster_goblin() {
            assertEquals("Goblin", adapter.getLoreName(TechLevel.TL1, ModuleCategory.MONSTER, "GOBLIN"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL1, ModuleCategory.MONSTER, "GOBLIN");
            assertEquals(0.25, stats.get("cr"));
            assertEquals("Humanoid", stats.get("type"));
        }

        @Test
        @DisplayName("TL2 should have Orc")
        void TL2_monster_orc() {
            assertEquals("Orc", adapter.getLoreName(TechLevel.TL2, ModuleCategory.MONSTER, "ORC"));
        }

        @Test
        @DisplayName("TL3 should have Mind Flayer")
        void TL3_monster_mindFlayer() {
            assertEquals("Mind Flayer", adapter.getLoreName(TechLevel.TL3, ModuleCategory.MONSTER, "MIND_FLAYER"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL3, ModuleCategory.MONSTER, "MIND_FLAYER");
            assertEquals(10, stats.get("cr"));
            assertEquals("Aberration", stats.get("type"));
        }

        @Test
        @DisplayName("TL4 should have Ancient Red Dragon")
        void TL4_monster_ancientDragon() {
            assertEquals("Ancient Red Dragon", adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "ANCIENT_DRAGON"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.MONSTER, "ANCIENT_DRAGON");
            assertEquals(24, stats.get("cr"));
            assertEquals(546, stats.get("hp"));
            assertEquals("Dragon", stats.get("type"));
            assertEquals("Gargantuan", stats.get("size"));
        }

        @Test
        @DisplayName("TL4 should have Lich")
        void TL4_monster_lich() {
            assertEquals("Lich", adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "LICH"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.MONSTER, "LICH");
            assertEquals(21, stats.get("cr"));
            assertEquals("Undead", stats.get("type"));
        }

        // --- Magic Item Tests ---

        @Test
        @DisplayName("TL1 should have Wand of Wisdom")
        void TL1_magicItem_wandOfWisdom() {
            assertEquals("Wand of Wisdom", adapter.getLoreName(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM");
            assertEquals("Uncommon", stats.get("rarity"));
        }

        @Test
        @DisplayName("TL2 should have Ring of Protection")
        void TL2_magicItem_ringOfProtection() {
            assertEquals("Ring of Protection", adapter.getLoreName(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "RING_OF_PROTECTION"));
        }

        @Test
        @DisplayName("TL4 should have Staff of Power")
        void TL4_magicItem_staffOfPower() {
            assertEquals("Staff of Power", adapter.getLoreName(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER");
            assertEquals("Very Rare", stats.get("rarity"));
        }

        // --- NPC Tests ---

        @Test
        @DisplayName("TL1 should have Innkeeper")
        void TL1_npc_innkeeper() {
            assertEquals("Innkeeper", adapter.getLoreName(TechLevel.TL1, ModuleCategory.NPC, "INNKEEPER"));
        }

        @Test
        @DisplayName("TL2 should have Wizard")
        void TL2_npc_wizard() {
            assertEquals("Wizard", adapter.getLoreName(TechLevel.TL2, ModuleCategory.NPC, "WIZARD"));
        }

        // --- Spell Tests ---

        @Test
        @DisplayName("TL1 should have Magic Missile")
        void TL1_spell_magicMissile() {
            assertEquals("Magic Missile", adapter.getLoreName(TechLevel.TL1, ModuleCategory.SPELL, "MAGIC_MISSILE"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL1, ModuleCategory.SPELL, "MAGIC_MISSILE");
            assertEquals(1, stats.get("level"));
            assertEquals("Evocation", stats.get("school"));
        }

        @Test
        @DisplayName("TL2 should have Fireball")
        void TL2_spell_fireball() {
            assertEquals("Fireball", adapter.getLoreName(TechLevel.TL2, ModuleCategory.SPELL, "FIREBALL"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.SPELL, "FIREBALL");
            assertEquals(3, stats.get("level"));
            assertEquals("8d6", stats.get("damage"));
        }

        @Test
        @DisplayName("TL4 should have Wish")
        void TL4_spell_wish() {
            assertEquals("Wish", adapter.getLoreName(TechLevel.TL4, ModuleCategory.SPELL, "WISH"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL4, ModuleCategory.SPELL, "WISH");
            assertEquals(9, stats.get("level"));
        }

        // --- Potion Tests ---

        @Test
        @DisplayName("TL1 should have Antidote")
        void TL1_potion_antidote() {
            assertEquals("Antidote", adapter.getLoreName(TechLevel.TL1, ModuleCategory.POTION, "ANTIDOTE"));
        }

        // --- Weapon Tests ---

        @Test
        @DisplayName("TL0 should have Stone Spear")
        void TL0_weapon_stoneSpear() {
            assertEquals("Stone Spear", adapter.getLoreName(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_SPEAR"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_SPEAR");
            assertEquals("1d6", stats.get("damage"));
            assertEquals("Piercing", stats.get("damageType"));
        }

        @Test
        @DisplayName("TL2 should have Halberd")
        void TL2_weapon_halberd() {
            assertEquals("Halberd", adapter.getLoreName(TechLevel.TL2, ModuleCategory.WEAPON, "HALBERD"));
        }

        // --- Armor Tests ---

        @Test
        @DisplayName("TL0 should have Leather Armor")
        void TL0_armor_leatherArmor() {
            assertEquals("Leather Armor", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ARMOR, "LEATHER_ARMOR"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.ARMOR, "LEATHER_ARMOR");
            assertEquals(11, stats.get("ac"));
            assertEquals("Light", stats.get("type"));
        }

        @Test
        @DisplayName("TL2 should have Plate Armor")
        void TL2_armor_plateArmor() {
            assertEquals("Plate Armor", adapter.getLoreName(TechLevel.TL2, ModuleCategory.ARMOR, "PLATE_ARMOR"));
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL2, ModuleCategory.ARMOR, "PLATE_ARMOR");
            assertEquals(18, stats.get("ac"));
        }

        // --- Adventure Gear Tests ---

        @Test
        @DisplayName("TL0 should have Torch")
        void TL0_gear_torch() {
            assertEquals("Torch", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "TORCH"));
        }

        @Test
        @DisplayName("TL2 should have Alchemist's Fire")
        void TL2_gear_alchemistsFire() {
            assertEquals("Alchemist's Fire", adapter.getLoreName(TechLevel.TL2, ModuleCategory.ADVENTURE_GEAR, "ALCHEMISTS_FIRE"));
        }

        // --- Dungeon category display names ---

        @Test
        @DisplayName("all dungeon categories should have display names")
        void dungeonCategories_shouldHaveDisplayNames() {
            assertEquals("Dungeon Room", ModuleCategory.DUNGEON_ROOM.getDisplayName());
            assertEquals("Furniture", ModuleCategory.FURNITURE.getDisplayName());
            assertEquals("Treasure", ModuleCategory.TREASURE.getDisplayName());
            assertEquals("Trap", ModuleCategory.TRAP.getDisplayName());
            assertEquals("Door", ModuleCategory.DOOR.getDisplayName());
            assertEquals("Chest", ModuleCategory.CHEST.getDisplayName());
            assertEquals("Monster", ModuleCategory.MONSTER.getDisplayName());
            assertEquals("Magic Item", ModuleCategory.MAGIC_ITEM.getDisplayName());
            assertEquals("NPC", ModuleCategory.NPC.getDisplayName());
            assertEquals("Spell", ModuleCategory.SPELL.getDisplayName());
            assertEquals("Potion", ModuleCategory.POTION.getDisplayName());
            assertEquals("Weapon", ModuleCategory.WEAPON.getDisplayName());
            assertEquals("Armor", ModuleCategory.ARMOR.getDisplayName());
            assertEquals("Adventure Gear", ModuleCategory.ADVENTURE_GEAR.getDisplayName());
            assertEquals("Animal", ModuleCategory.ANIMAL.getDisplayName());
        }

        // --- Dungeon module counts ---

        @Test
        @DisplayName("D&D TL2 should have dungeon objects in all dungeon categories")
        void TL2_dungeonObjects_allCategories() {
            ModuleCategory[] dungeonCategories = {
                    ModuleCategory.DUNGEON_ROOM, ModuleCategory.FURNITURE, ModuleCategory.TREASURE,
                    ModuleCategory.TRAP, ModuleCategory.DOOR, ModuleCategory.CHEST, ModuleCategory.MONSTER,
                    ModuleCategory.MAGIC_ITEM, ModuleCategory.NPC, ModuleCategory.SPELL, ModuleCategory.POTION,
                    ModuleCategory.WEAPON, ModuleCategory.ARMOR, ModuleCategory.ADVENTURE_GEAR, ModuleCategory.ANIMAL
            };
            for (ModuleCategory cat : dungeonCategories) {
                List<String> modules = adapter.getAvailableModules(TechLevel.TL2, cat);
                assertFalse(modules.isEmpty(), "D&D TL2 should have " + cat.getDisplayName() + " modules");
            }
        }

        @Test
        @DisplayName("D&D TL4 should have high-level monsters")
        void TL4_highLevelMonsters() {
            assertNotNull(adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "ANCIENT_DRAGON"));
            assertNotNull(adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "LICH"));
            assertNotNull(adapter.getLoreName(TechLevel.TL4, ModuleCategory.MONSTER, "DEVIL"));
        }

        @Test
        @DisplayName("D&D TL0 should have low-level monsters")
        void TL0_lowLevelMonsters() {
            assertNotNull(adapter.getLoreName(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_SPIDER"));
            assertNotNull(adapter.getLoreName(TechLevel.TL0, ModuleCategory.MONSTER, "KOBOLD"));
        }
    }

    // ==========================================
    // isFictional Flag Tests
    // ==========================================

    @Nested
    @DisplayName("isFictional Flag Tests")
    class IsFictionalFlagTests {

        @Test
        @DisplayName("Dnd5eMonster should default isFictional to true")
        void dnd5eMonster_defaultFictional() {
            com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster monster = new com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster();
            assertTrue(monster.isFictional());
        }

        @Test
        @DisplayName("E26Monster should default isFictional to true")
        void e26Monster_defaultFictional() {
            com.yisy.modules.doramcu.earth2026.model.E26Monster monster = new com.yisy.modules.doramcu.earth2026.model.E26Monster();
            assertTrue(monster.isFictional());
        }

        @Test
        @DisplayName("Dnd5eNpc should default isFictional to true")
        void dnd5eNpc_defaultFictional() {
            com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc npc = new com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc();
            assertTrue(npc.isFictional());
        }

        @Test
        @DisplayName("E26Npc should default isFictional to true")
        void e26Npc_defaultFictional() {
            com.yisy.modules.doramcu.earth2026.model.E26Npc npc = new com.yisy.modules.doramcu.earth2026.model.E26Npc();
            assertTrue(npc.isFictional());
        }

        @Test
        @DisplayName("Dnd5eMonster should allow setting isFictional to false")
        void dnd5eMonster_setFictionalFalse() {
            com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster monster = new com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster();
            monster.setFictional(false);
            assertFalse(monster.isFictional());
        }

        @Test
        @DisplayName("E26Monster should allow setting isFictional to false")
        void e26Monster_setFictionalFalse() {
            com.yisy.modules.doramcu.earth2026.model.E26Monster monster = new com.yisy.modules.doramcu.earth2026.model.E26Monster();
            monster.setFictional(false);
            assertFalse(monster.isFictional());
        }

        @Test
        @DisplayName("Dnd5eNpc should allow setting isFictional to false")
        void dnd5eNpc_setFictionalFalse() {
            com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc npc = new com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc();
            npc.setFictional(false);
            assertFalse(npc.isFictional());
        }

        @Test
        @DisplayName("E26Npc should allow setting isFictional to false")
        void e26Npc_setFictionalFalse() {
            com.yisy.modules.doramcu.earth2026.model.E26Npc npc = new com.yisy.modules.doramcu.earth2026.model.E26Npc();
            npc.setFictional(false);
            assertFalse(npc.isFictional());
        }
    }

    // ==========================================
    // ANIMAL Category Tests
    // ==========================================

    @Nested
    @DisplayName("ANIMAL Category Tests")
    class AnimalCategoryTests {

        @Test
        @DisplayName("ANIMAL category should have correct display name and description")
        void animalCategory_displayName() {
            assertEquals("Animal", ModuleCategory.ANIMAL.getDisplayName());
            assertTrue(ModuleCategory.ANIMAL.getDescription().contains("farming") ||
                    ModuleCategory.ANIMAL.getDescription().contains("ranching") ||
                    ModuleCategory.ANIMAL.getDescription().contains("transport"));
        }

        @Test
        @DisplayName("PlanetBuildingModuleEntity should have animal fields")
        void entity_animalFields() {
            com.yisy.modules.doramcu.model.PlanetBuildingModuleEntity entity =
                    new com.yisy.modules.doramcu.model.PlanetBuildingModuleEntity();
            entity.setMilkYield(20.0);
            entity.setWoolYield(3.0);
            entity.setCarryingCapacity(100);
            entity.setEggRate(5);
            entity.setMeatYield(200.0);
            assertEquals(20.0, entity.getMilkYield());
            assertEquals(3.0, entity.getWoolYield());
            assertEquals(100, entity.getCarryingCapacity());
            assertEquals(5, entity.getEggRate());
            assertEquals(200.0, entity.getMeatYield());
        }

        @Test
        @DisplayName("D&D adapter should return Cow for COW at TL0")
        void dnd_animal_cow() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertEquals("Cow", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("D&D adapter should return Horse for HORSE at TL0")
        void dnd_animal_horse() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertEquals("Horse", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("Traveller adapter should return Protean for COW at TL0")
        void traveller_animal_cow() {
            TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();
            assertEquals("Protean", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("Traveller adapter should return Protean Steed for HORSE at TL0")
        void traveller_animal_horse() {
            TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();
            assertEquals("Protean Steed", adapter.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("Star Trek adapter should return Vulcan Cow for COW at TL6")
        void starTrek_animal_cow() {
            StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();
            assertEquals("Vulcan Cow", adapter.getLoreName(TechLevel.TL6, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("Star Trek adapter should return Vulcan Horse for HORSE at TL6")
        void starTrek_animal_horse() {
            StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();
            assertEquals("Vulcan Horse", adapter.getLoreName(TechLevel.TL6, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("B5 adapter should return Minbari Grazing Beast for COW at TL7")
        void b5_animal_cow() {
            B5TechLevelAdapter adapter = new B5TechLevelAdapter();
            assertEquals("Minbari Grazing Beast", adapter.getLoreName(TechLevel.TL7, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("B5 adapter should return Narn Draft Horse for HORSE at TL7")
        void b5_animal_horse() {
            B5TechLevelAdapter adapter = new B5TechLevelAdapter();
            assertEquals("Narn Draft Horse", adapter.getLoreName(TechLevel.TL7, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("Earth 2026 adapter should return Dairy Cow for COW at TL5")
        void earth2026_animal_cow() {
            Earth2026TechLevelAdapter adapter = new Earth2026TechLevelAdapter();
            assertEquals("Dairy Cow", adapter.getLoreName(TechLevel.TL5, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("Earth 2026 adapter should return Quarter Horse for HORSE at TL5")
        void earth2026_animal_horse() {
            Earth2026TechLevelAdapter adapter = new Earth2026TechLevelAdapter();
            assertEquals("Quarter Horse", adapter.getLoreName(TechLevel.TL5, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("Cross-galaxy COW conversion should differ across all adapters")
        void crossGalaxy_cowConversion() {
            Dnd5eTechLevelAdapter dnd = new Dnd5eTechLevelAdapter();
            TravellerTechLevelAdapter traveller = new TravellerTechLevelAdapter();
            StarTrekTechLevelAdapter starTrek = new StarTrekTechLevelAdapter();
            B5TechLevelAdapter b5 = new B5TechLevelAdapter();
            Earth2026TechLevelAdapter earth2026 = new Earth2026TechLevelAdapter();

            String dndCow = dnd.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "COW");
            String travellerCow = traveller.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "COW");
            String starTrekCow = starTrek.getLoreName(TechLevel.TL6, ModuleCategory.ANIMAL, "COW");
            String b5Cow = b5.getLoreName(TechLevel.TL7, ModuleCategory.ANIMAL, "COW");
            String earth2026Cow = earth2026.getLoreName(TechLevel.TL5, ModuleCategory.ANIMAL, "COW");

            assertNotNull(dndCow);
            assertNotNull(travellerCow);
            assertNotNull(starTrekCow);
            assertNotNull(b5Cow);
            assertNotNull(earth2026Cow);

            assertNotEquals(dndCow, travellerCow);
            assertNotEquals(dndCow, starTrekCow);
            assertNotEquals(dndCow, b5Cow);
            assertNotEquals(dndCow, earth2026Cow);
        }

        @Test
        @DisplayName("Cross-galaxy HORSE conversion should differ across all adapters")
        void crossGalaxy_horseConversion() {
            Dnd5eTechLevelAdapter dnd = new Dnd5eTechLevelAdapter();
            TravellerTechLevelAdapter traveller = new TravellerTechLevelAdapter();
            StarTrekTechLevelAdapter starTrek = new StarTrekTechLevelAdapter();
            B5TechLevelAdapter b5 = new B5TechLevelAdapter();
            Earth2026TechLevelAdapter earth2026 = new Earth2026TechLevelAdapter();

            String dndHorse = dnd.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE");
            String travellerHorse = traveller.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE");
            String starTrekHorse = starTrek.getLoreName(TechLevel.TL6, ModuleCategory.ANIMAL, "HORSE");
            String b5Horse = b5.getLoreName(TechLevel.TL7, ModuleCategory.ANIMAL, "HORSE");
            String earth2026Horse = earth2026.getLoreName(TechLevel.TL5, ModuleCategory.ANIMAL, "HORSE");

            assertNotNull(dndHorse);
            assertNotNull(travellerHorse);
            assertNotNull(starTrekHorse);
            assertNotNull(b5Horse);
            assertNotNull(earth2026Horse);

            assertNotEquals(dndHorse, travellerHorse);
            assertNotEquals(dndHorse, starTrekHorse);
            assertNotEquals(dndHorse, b5Horse);
            assertNotEquals(dndHorse, earth2026Horse);
        }

        @Test
        @DisplayName("D&D adapter should have animal stats")
        void dnd_animal_stats() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.ANIMAL, "COW");
            assertNotNull(stats);
            assertEquals(20, stats.get("milkYield"));
            assertEquals(200, stats.get("meatYield"));
        }

        @Test
        @DisplayName("Traveller adapter should have Protean animal stats")
        void traveller_animal_stats() {
            TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();
            Map<String, Object> stats = adapter.getLoreStats(TechLevel.TL0, ModuleCategory.ANIMAL, "COW");
            assertNotNull(stats);
            assertEquals("Protean", stats.get("type"));
        }

        @Test
        @DisplayName("D&D adapter should return null for unsupported tech level")
        void dnd_animal_unsupportedTL() {
            Dnd5eTechLevelAdapter adapter = new Dnd5eTechLevelAdapter();
            assertNull(adapter.getLoreName(TechLevel.TL10, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("Star Trek adapter should return null for unsupported tech level")
        void starTrek_animal_unsupportedTL() {
            StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();
            assertNull(adapter.getLoreName(TechLevel.TL0, ModuleCategory.ANIMAL, "COW"));
        }

        @Test
        @DisplayName("Traveller should have high-tech animals at TL8")
        void traveller_highTechAnimals() {
            TravellerTechLevelAdapter adapter = new TravellerTechLevelAdapter();
            assertEquals("Anti-Gravity Mount", adapter.getLoreName(TechLevel.TL8, ModuleCategory.ANIMAL, "HORSE"));
        }

        @Test
        @DisplayName("Star Trek should have high-tech animals at TL10")
        void starTrek_highTechAnimals() {
            StarTrekTechLevelAdapter adapter = new StarTrekTechLevelAdapter();
            assertEquals("Transwarp Bovine", adapter.getLoreName(TechLevel.TL10, ModuleCategory.ANIMAL, "COW"));
        }
    }
}
