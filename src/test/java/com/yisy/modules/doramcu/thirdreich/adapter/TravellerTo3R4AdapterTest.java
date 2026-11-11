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

@DisplayName("TravellerTo3R4Adapter")
class TravellerTo3R4AdapterTest {

    private final TravellerTo3R4Adapter adapter = new TravellerTo3R4Adapter();

    @Test
    @DisplayName("getSourceGame returns Traveller")
    void getSourceGame() {
        assertEquals("Traveller", adapter.getSourceGame());
    }

    @Nested
    @DisplayName("to3R4Unit")
    class To3R4Unit {

        @Test
        @DisplayName("returns null for unsupported type")
        void returnsNullForUnsupported() {
            assertNull(adapter.to3R4Unit("invalid"));
        }

        @Test
        @DisplayName("returns null for null input")
        void returnsNullForNull() {
            assertNull(adapter.to3R4Unit(null));
        }
    }

    @Nested
    @DisplayName("to3R4Units")
    class To3R4Units {

        @Test
        @DisplayName("converts empty list")
        void convertsEmptyList() {
            List<Object> units = List.of();
            List<ThirdReichGroundUnit> result = adapter.to3R4Units(units);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips invalid conversions")
        void skipsInvalidConversions() {
            List<Object> units = List.of("invalid");
            List<ThirdReichGroundUnit> result = adapter.to3R4Units(units);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("toCommander")
    class ToCommander {

        @Test
        @DisplayName("converts map with leadership")
        void convertsWithLeadership() {
            Map<String, Object> charData = Map.of(
                    "name", "Colonial Marshal",
                    "leadership", 8
            );
            var commander = adapter.toCommander(charData);

            assertNotNull(commander);
            assertEquals("Colonial Marshal", commander.getName());
            assertEquals("Traveller", commander.getSourceGame());
            assertEquals(8, commander.getLeadership());
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
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.DEFENDER_VICTORY);
            result.setAttackerCasualties(80);
            result.setDefenderCasualties(20);
            result.setRoundsFought(2);
            result.generateNarrative();

            Map<String, Object> mapped = adapter.mapResult(result);

            assertEquals("b-1", mapped.get("battleId"));
            assertEquals("DEFENDER_VICTORY", mapped.get("outcome"));
            assertEquals(80, mapped.get("attackerCasualties"));
            assertEquals(20, mapped.get("defenderCasualties"));
            assertEquals(2, mapped.get("roundsFought"));
        }
    }
}
