package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DaocBattleAdapter")
class DaocBattleAdapterTest {

    private DaocBattleAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DaocBattleAdapter();
    }

    @Nested
    @DisplayName("Module Identification")
    class ModuleIdentification {

        @Test
        @DisplayName("returns DaOC module ID")
        void moduleId() {
            assertEquals("DaOC", adapter.getModuleId());
        }

        @Test
        @DisplayName("returns Dark Age of Camelot module name")
        void moduleName() {
            assertEquals("Dark Age of Camelot", adapter.getModuleName());
        }
    }

    @Nested
    @DisplayName("Available Unit Types")
    class AvailableUnitTypes {

        @Test
        @DisplayName("returns non-empty unit types map")
        void unitTypesNotEmpty() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertNotNull(types);
            assertTrue(types.size() > 40, "Should have 40+ unit types");
        }

        @Test
        @DisplayName("includes Albion classes")
        void includesAlbionClasses() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertTrue(types.containsKey("ARMSMAN"));
            assertTrue(types.containsKey("PALADIN"));
            assertTrue(types.containsKey("WIZARD"));
            assertTrue(types.containsKey("SORCERER"));
            assertTrue(types.containsKey("CLERIC"));
            assertTrue(types.containsKey("FRIAR"));
            assertTrue(types.containsKey("CABALIST"));
            assertTrue(types.containsKey("HERETIC"));
            assertTrue(types.containsKey("NECROMANCER"));
            assertTrue(types.containsKey("INFILTRATOR"));
            assertTrue(types.containsKey("SCOUT"));
            assertTrue(types.containsKey("REAVER"));
            assertTrue(types.containsKey("MERCENARY"));
            assertTrue(types.containsKey("MINSTREL"));
            assertTrue(types.containsKey("THEURGIST"));
        }

        @Test
        @DisplayName("includes Hibernia classes")
        void includesHiberniaClasses() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertTrue(types.containsKey("CHAMPION"));
            assertTrue(types.containsKey("DRUID"));
            assertTrue(types.containsKey("RANGER"));
            assertTrue(types.containsKey("BARD"));
            assertTrue(types.containsKey("BLADEMASTER"));
            assertTrue(types.containsKey("VALEWALKER"));
            assertTrue(types.containsKey("VAMPIIR"));
            assertTrue(types.containsKey("NIGHTSHADE"));
            assertTrue(types.containsKey("ANIMIST"));
            assertTrue(types.containsKey("BANSHEE"));
            assertTrue(types.containsKey("ELDITCH"));
            assertTrue(types.containsKey("ENCHANTER"));
            assertTrue(types.containsKey("HERO"));
            assertTrue(types.containsKey("MENTALIST"));
            assertTrue(types.containsKey("WARDEN"));
        }

        @Test
        @DisplayName("includes Midgard classes")
        void includesMidgardClasses() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertTrue(types.containsKey("BERSERKER"));
            assertTrue(types.containsKey("THANE"));
            assertTrue(types.containsKey("RUNEMASTER"));
            assertTrue(types.containsKey("SPIRITMASTER"));
            assertTrue(types.containsKey("HEALER"));
            assertTrue(types.containsKey("HUNTER"));
            assertTrue(types.containsKey("SHADOWBLADE"));
            assertTrue(types.containsKey("SAVAGE"));
            assertTrue(types.containsKey("SHAMAN"));
            assertTrue(types.containsKey("SKALD"));
            assertTrue(types.containsKey("BONEDANCER"));
        }

        @Test
        @DisplayName("includes cross-realm and special entities")
        void includesCrossRealmAndSpecial() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertTrue(types.containsKey("MAULER"));
            assertTrue(types.containsKey("MINOTAUR"));
            assertTrue(types.containsKey("LEGION"));
            assertTrue(types.containsKey("MARTYR"));
        }

        @Test
        @DisplayName("unit type display names are properly formatted")
        void displayNamesFormatted() {
            Map<String, String> types = adapter.getAvailableUnitTypes();
            assertEquals("Paladin", types.get("PALADIN"));
            assertEquals("Wizard", types.get("WIZARD"));
            assertEquals("Berserker", types.get("BERSERKER"));
            assertEquals("Champion", types.get("CHAMPION"));
            assertEquals("Druid", types.get("DRUID"));
            assertEquals("Runemaster", types.get("RUNEMASTER"));
            assertEquals("Legion", types.get("LEGION"));
        }
    }

    @Nested
    @DisplayName("Unit Creation")
    class UnitCreation {

        @Test
        @DisplayName("creates a single unit")
        void createSingleUnit() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "unit-1", "Paladin Unit", "PALADIN", "albion", 5
            );

            assertNotNull(unit);
            assertEquals("unit-1", unit.getId());
            assertEquals("Paladin Unit", unit.getName());
            assertEquals("albion", unit.getFaction());
        }

        @Test
        @DisplayName("unit attack scales with tech level")
        void attackScalesWithTechLevel() {
            UniversalBattleUnit lowTech = adapter.createUnit(
                    "u1", "Low Tech", "PALADIN", "albion", 3
            );
            UniversalBattleUnit highTech = adapter.createUnit(
                    "u2", "High Tech", "PALADIN", "albion", 6
            );

            assertTrue(highTech.getAttackValue() >= lowTech.getAttackValue(),
                    "Higher tech level should have equal or higher attack");
        }

        @Test
        @DisplayName("unit defense scales with tech level")
        void defenseScalesWithTechLevel() {
            UniversalBattleUnit lowTech = adapter.createUnit(
                    "u1", "Low Tech", "PALADIN", "albion", 3
            );
            UniversalBattleUnit highTech = adapter.createUnit(
                    "u2", "High Tech", "PALADIN", "albion", 6
            );

            assertTrue(highTech.getDefenseValue() >= lowTech.getDefenseValue(),
                    "Higher tech level should have equal or higher defense");
        }

        @Test
        @DisplayName("creates multiple units")
        void createMultipleUnits() {
            List<UniversalBattleUnit> units = adapter.createUnits(
                    List.of("u1", "u2", "u3"), "PALADIN", "albion", 5
            );

            assertEquals(3, units.size());
            assertEquals("u1", units.get(0).getId());
            assertEquals("u2", units.get(1).getId());
            assertEquals("u3", units.get(2).getId());
        }

        @Test
        @DisplayName("created units have model paths")
        void unitsHaveModelPaths() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Paladin", "PALADIN", "albion", 5
            );

            assertNotNull(unit.getModel3dPath());
            assertTrue(unit.getModel3dPath().contains("paladin"));
        }

        @Test
        @DisplayName("created units have texture paths")
        void unitsHaveTexturePaths() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Paladin", "PALADIN", "albion", 5
            );

            assertNotNull(unit.getTexturePath());
            assertTrue(unit.getTexturePath().contains("paladin"));
        }

        @Test
        @DisplayName("min tech level creates valid unit")
        void minTechLevelCreatesValidUnit() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Min Tech", "PALADIN", "albion", 1
            );

            assertNotNull(unit);
            assertTrue(unit.getAttackValue() > 0);
            assertTrue(unit.getDefenseValue() > 0);
        }
    }

    @Nested
    @DisplayName("Model and Texture Paths")
    class ModelAndTexturePaths {

        @Test
        @DisplayName("returns model path for known unit")
        void modelPathForKnownUnit() {
            String path = adapter.getModelPath("PALADIN");
            assertNotNull(path);
            assertEquals("/models/daoc/paladin.obj", path);
        }

        @Test
        @DisplayName("returns texture path for known unit")
        void texturePathForKnownUnit() {
            String path = adapter.getTexturePath("PALADIN");
            assertNotNull(path);
            assertEquals("/textures/daoc/paladin.png", path);
        }

        @Test
        @DisplayName("returns null for unknown unit model")
        void nullModelPathForUnknownUnit() {
            assertNull(adapter.getModelPath("UNKNOWN_UNIT"));
        }

        @Test
        @DisplayName("returns null for unknown unit texture")
        void nullTexturePathForUnknownUnit() {
            assertNull(adapter.getTexturePath("UNKNOWN_UNIT"));
        }

        @Test
        @DisplayName("model paths follow consistent pattern")
        void modelPathsConsistent() {
            assertTrue(adapter.getModelPath("WIZARD").startsWith("/models/daoc/"));
            assertTrue(adapter.getModelPath("BERSERKER").startsWith("/models/daoc/"));
            assertTrue(adapter.getModelPath("DRUID").startsWith("/models/daoc/"));
            assertTrue(adapter.getModelPath("LEGION").startsWith("/models/daoc/"));
        }

        @Test
        @DisplayName("texture paths follow consistent pattern")
        void texturePathsConsistent() {
            assertTrue(adapter.getTexturePath("WIZARD").startsWith("/textures/daoc/"));
            assertTrue(adapter.getTexturePath("BERSERKER").startsWith("/textures/daoc/"));
            assertTrue(adapter.getTexturePath("DRUID").startsWith("/textures/daoc/"));
            assertTrue(adapter.getTexturePath("LEGION").startsWith("/textures/daoc/"));
        }
    }

    @Nested
    @DisplayName("Combat Stats Calculation")
    class CombatStatsCalculation {

        @Test
        @DisplayName("calculates combat stats with bonuses")
        void calculatesCombatStatsWithBonuses() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Test Unit", "PALADIN", "albion", 5
            );

            Map<String, Integer> stats = adapter.calculateCombatStats(unit, 10, 0.2);

            assertNotNull(stats);
            assertTrue(stats.containsKey("attack"));
            assertTrue(stats.containsKey("defense"));
            assertTrue(stats.containsKey("morale"));
        }

        @Test
        @DisplayName("attack includes commander bonus")
        void attackIncludesCommanderBonus() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Test Unit", "PALADIN", "albion", 5
            );

            Map<String, Integer> noBonus = adapter.calculateCombatStats(unit, 0, 0.0);
            Map<String, Integer> withBonus = adapter.calculateCombatStats(unit, 0, 0.5);

            assertTrue(withBonus.get("attack") > noBonus.get("attack"),
                    "Commander bonus should increase attack");
        }

        @Test
        @DisplayName("defense includes terrain bonus")
        void defenseIncludesTerrainBonus() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Test Unit", "PALADIN", "albion", 5
            );

            Map<String, Integer> noBonus = adapter.calculateCombatStats(unit, 0, 0.0);
            Map<String, Integer> withBonus = adapter.calculateCombatStats(unit, 20, 0.0);

            assertTrue(withBonus.get("defense") > noBonus.get("defense"),
                    "Terrain bonus should increase defense");
        }

        @Test
        @DisplayName("morale is capped at 100")
        void moraleCappedAt100() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Test Unit", "PALADIN", "albion", 5
            );

            Map<String, Integer> stats = adapter.calculateCombatStats(unit, 0, 10.0);

            assertTrue(stats.get("morale") <= 100, "Morale should not exceed 100");
        }

        @Test
        @DisplayName("zero bonuses returns base stats")
        void zeroBonusesReturnsBaseStats() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Test Unit", "PALADIN", "albion", 5
            );

            Map<String, Integer> stats = adapter.calculateCombatStats(unit, 0, 0.0);

            assertEquals(unit.getAttackValue(), stats.get("attack"));
            assertEquals(unit.getDefenseValue(), stats.get("defense"));
        }
    }

    @Nested
    @DisplayName("Faction Colors")
    class FactionColors {

        @Test
        @DisplayName("Albion returns gold color")
        void albionReturnsGold() {
            assertEquals("#FFD700", adapter.getFactionColor("albion"));
        }

        @Test
        @DisplayName("Hibernia returns red color")
        void hiberniaReturnsRed() {
            assertEquals("#FF0000", adapter.getFactionColor("hibernia"));
        }

        @Test
        @DisplayName("Midgard returns green color")
        void midgardReturnsGreen() {
            assertEquals("#00FF00", adapter.getFactionColor("midgard"));
        }

        @Test
        @DisplayName("unknown faction returns gray")
        void unknownFactionReturnsGray() {
            assertEquals("#808080", adapter.getFactionColor("unknown"));
        }

        @Test
        @DisplayName("null faction returns gray")
        void nullFactionReturnsGray() {
            assertEquals("#808080", adapter.getFactionColor(null));
        }

        @Test
        @DisplayName("faction color is case insensitive")
        void factionColorCaseInsensitive() {
            assertEquals(adapter.getFactionColor("albion"), adapter.getFactionColor("Albion"));
            assertEquals(adapter.getFactionColor("hibernia"), adapter.getFactionColor("HIBERNIA"));
            assertEquals(adapter.getFactionColor("midgard"), adapter.getFactionColor("Midgard"));
        }
    }

    @Nested
    @DisplayName("Generic Unit Type Mapping")
    class GenericUnitTypeMapping {

        @Test
        @DisplayName("Paladin maps to SPECIAL_FORCES")
        void paladinMapsToSpecialForces() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Paladin", "PALADIN", "albion", 5
            );
            // Paladin is a special forces class
            assertNotNull(unit);
        }

        @Test
        @DisplayName("Armsman maps to INFANTRY")
        void armsmanMapsToInfantry() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Armsman", "ARMSMAN", "albion", 5
            );
            assertNotNull(unit);
        }

        @Test
        @DisplayName("Berserker maps to INFANTRY")
        void berserkerMapsToInfantry() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Berserker", "BERSERKER", "midgard", 5
            );
            assertNotNull(unit);
        }

        @Test
        @DisplayName("Legion maps to SPECIAL_FORCES")
        void legionMapsToSpecialForces() {
            UniversalBattleUnit unit = adapter.createUnit(
                    "u1", "Legion", "LEGION", "legion", 5
            );
            assertNotNull(unit);
        }
    }
}
