package com.yisy.modules.doramcu.thirdreich.adapter;

import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarTrekTo3R4Adapter")
class StarTrekTo3R4AdapterTest {

    private final StarTrekTo3R4Adapter adapter = new StarTrekTo3R4Adapter();

    @Test
    @DisplayName("getSourceGame returns Captains_Log")
    void getSourceGame() {
        assertEquals("Captains_Log", adapter.getSourceGame());
    }

    @Nested
    @DisplayName("to3R4Unit with Map")
    class To3R4UnitWithMap {

        @Test
        @DisplayName("converts generic map to infantry unit")
        void convertsGenericMap() {
            Map<String, Object> data = Map.of(
                    "name", "Security Team",
                    "strength", 80,
                    "morale", 90
            );
            ThirdReichGroundUnit unit = adapter.to3R4Unit(data);

            assertNotNull(unit);
            assertEquals("Security Team", unit.getName());
            assertEquals(ThirdReichUnitType.INFANTRY, unit.getUnitType());
            assertEquals(80, unit.getStrength());
            assertEquals(90, unit.getMorale());
            assertEquals("Captains_Log", unit.getSourceGame());
        }

        @Test
        @DisplayName("converts tank type from map")
        void convertsTankType() {
            Map<String, Object> data = Map.of(
                    "name", "Armor Unit",
                    "type", "TANK"
            );
            ThirdReichGroundUnit unit = adapter.to3R4Unit(data);

            assertEquals(ThirdReichUnitType.TANK, unit.getUnitType());
        }

        @Test
        @DisplayName("converts artillery type from map")
        void convertsArtilleryType() {
            Map<String, Object> data = Map.of(
                    "name", "Phaser Battery",
                    "type", "ARTILLERY"
            );
            ThirdReichGroundUnit unit = adapter.to3R4Unit(data);

            assertEquals(ThirdReichUnitType.ARTILLERY, unit.getUnitType());
        }

        @Test
        @DisplayName("converts recon type from map")
        void convertsReconType() {
            Map<String, Object> data = Map.of(
                    "name", "Scout Team",
                    "type", "RECON"
            );
            ThirdReichGroundUnit unit = adapter.to3R4Unit(data);

            assertEquals(ThirdReichUnitType.RECON, unit.getUnitType());
        }

        @Test
        @DisplayName("converts air support type from map")
        void convertsAirSupportType() {
            Map<String, Object> data = Map.of(
                    "name", "Shuttle Wing",
                    "type", "AEROSPACE"
            );
            ThirdReichGroundUnit unit = adapter.to3R4Unit(data);

            assertEquals(ThirdReichUnitType.AIR_SUPPORT, unit.getUnitType());
        }

        @Test
        @DisplayName("returns null for unsupported type")
        void returnsNullForUnsupported() {
            assertNull(adapter.to3R4Unit("invalid"));
        }
    }

    @Nested
    @DisplayName("to3R4Units")
    class To3R4Units {

        @Test
        @DisplayName("converts list of maps")
        void convertsListOfMaps() {
            List<Map<String, Object>> units = List.of(
                    Map.of("name", "Team 1", "type", "INFANTRY"),
                    Map.of("name", "Team 2", "type", "TANK")
            );
            List<ThirdReichGroundUnit> result = adapter.to3R4Units(units);

            assertEquals(2, result.size());
            assertEquals("Team 1", result.get(0).getName());
            assertEquals(ThirdReichUnitType.TANK, result.get(1).getUnitType());
        }

        @Test
        @DisplayName("skips null conversions")
        void skipsNullConversions() {
            List<Object> units = List.of(
                    Map.of("name", "Team 1"),
                    "invalid"
            );
            List<ThirdReichGroundUnit> result = adapter.to3R4Units(units);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("toCommander")
    class ToCommander {

        @Test
        @DisplayName("converts map with fitness attribute")
        void convertsWithFitness() {
            Map<String, Object> charData = Map.of(
                    "name", "Captain Kirk",
                    "fitness", 14,
                    "aggression", 8
            );
            var commander = adapter.toCommander(charData);

            assertNotNull(commander);
            assertEquals("Captain Kirk", commander.getName());
            assertEquals("Captains_Log", commander.getSourceGame());
            assertEquals(com.yisy.modules.doramcu.thirdreich.commander.AICommander.TacticalPreference.AGGRESSIVE,
                    commander.getTacticalPreference());
        }

        @Test
        @DisplayName("returns null for unsupported type")
        void returnsNullForUnsupported() {
            assertNull(adapter.toCommander("invalid"));
        }
    }

    @Nested
    @DisplayName("mapResult")
    class MapResult {

        @Test
        @DisplayName("maps battle result to map")
        void mapsResult() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setAttackerCasualties(50);
            result.setDefenderCasualties(100);
            result.setRoundsFought(3);
            result.generateNarrative();

            Map<String, Object> mapped = adapter.mapResult(result);

            assertEquals("b-1", mapped.get("battleId"));
            assertEquals("ATTACKER_VICTORY", mapped.get("outcome"));
            assertEquals(50, mapped.get("attackerCasualties"));
            assertEquals(100, mapped.get("defenderCasualties"));
            assertEquals(3, mapped.get("roundsFought"));
            assertNotNull(mapped.get("narrative"));
        }
    }
}
