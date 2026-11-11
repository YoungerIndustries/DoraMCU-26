package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderProfile;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThirdReichGroundBattleService")
class ThirdReichGroundBattleServiceTest {

    private final ThirdReichGroundBattleService service = new ThirdReichGroundBattleService();

    @Nested
    @DisplayName("resolveBattle")
    class ResolveBattle {

        @Test
        @DisplayName("throws on unknown game type")
        void throwsOnUnknownGame() {
            List<Map<String, Object>> units = List.of(Map.of("name", "Team"));
            assertThrows(IllegalArgumentException.class, () ->
                    service.resolveBattle("Unknown_Game", units, units, TerrainType.PLAINS, null, null, null));
        }

        @Test
        @DisplayName("resolves battle with Captains_Log adapter")
        void resolvesWithCaptainsLog() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Security Team Alpha", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Klingon Warriors", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, "Terra", null, null);

            assertNotNull(result);
            assertNotNull(result.get("battleId"));
            assertNotNull(result.get("outcome"));
        }

        @Test
        @DisplayName("resolves battle with Traveller adapter")
        void resolvesWithTraveller() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Marines", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Indigenous", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattle(
                    "Traveller", attackers, defenders, TerrainType.URBAN, "Terra", null, null);

            assertNotNull(result);
            assertNotNull(result.get("battleId"));
        }

        @Test
        @DisplayName("resolves battle with terrain")
        void resolvesWithTerrain() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "TANK", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattle(
                    "Captains_Log", attackers, defenders, TerrainType.MOUNTAIN, "Krios", null, null);

            assertNotNull(result);
            assertNotNull(result.get("outcome"));
        }

        @Test
        @DisplayName("resolves battle with AI commanders")
        void resolvesWithCommanders() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            AICommander atkCmd = service.generateCommander(
                    Map.of("name", "Kirk", "leadership", 8, "aggression", 7), "Captains_Log");
            AICommander defCmd = service.generateCommander(
                    Map.of("name", "Kor", "leadership", 7, "aggression", 9), "Captains_Log");

            Map<String, Object> result = service.resolveBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, null, atkCmd, defCmd);

            assertNotNull(result);
            assertNotNull(result.get("outcome"));
        }

        @Test
        @DisplayName("result contains all expected fields")
        void resultContainsFields() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, null, null, null);

            assertNotNull(result.get("battleId"));
            assertNotNull(result.get("outcome"));
            assertNotNull(result.get("attackerCasualties"));
            assertNotNull(result.get("defenderCasualties"));
            assertNotNull(result.get("attackerUnitsLost"));
            assertNotNull(result.get("defenderUnitsLost"));
            assertNotNull(result.get("roundsFought"));
            assertNotNull(result.get("narrative"));
            assertNotNull(result.get("events"));
        }

        @Test
        @DisplayName("battle is stored and retrievable")
        void battleStored() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, null, null, null);

            String battleId = (String) result.get("battleId");
            assertNotNull(battleId);
            assertNotNull(service.getBattle(battleId));
        }
    }

    @Nested
    @DisplayName("resolveAiBattle")
    class ResolveAiBattle {

        @Test
        @DisplayName("resolves battle with historical commanders")
        void resolvesWithHistoricalCommanders() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "TANK", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveAiBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, "Terra",
                    AICommanderProfile.ROMMEL, AICommanderProfile.MONTGOMERY);

            assertNotNull(result);
            assertNotNull(result.get("outcome"));
        }

        @Test
        @DisplayName("resolves battle without commanders")
        void resolvesWithoutCommanders() {
            List<Map<String, Object>> attackers = List.of(
                    Map.of("name", "Atk", "type", "INFANTRY", "strength", 100)
            );
            List<Map<String, Object>> defenders = List.of(
                    Map.of("name", "Def", "type", "INFANTRY", "strength", 100)
            );

            Map<String, Object> result = service.resolveAiBattle(
                    "Captains_Log", attackers, defenders, TerrainType.PLAINS, null, null, null);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Commander Profiles")
    class CommanderProfiles {

        @Test
        @DisplayName("returns all commander profiles")
        void returnsAllProfiles() {
            var profiles = service.getCommanderProfiles();
            assertEquals(10, profiles.size());
        }
    }

    @Nested
    @DisplayName("Generate Commander")
    class GenerateCommander {

        @Test
        @DisplayName("generates commander from stats")
        void generatesCommander() {
            AICommander commander = service.generateCommander(
                    Map.of("name", "Picard", "leadership", 9, "aggression", 4), "Captains_Log");

            assertNotNull(commander);
            assertEquals("Picard", commander.getName());
            assertEquals(9, commander.getLeadership());
        }

        @Test
        @DisplayName("retrieves generated commander")
        void retrievesGeneratedCommander() {
            AICommander commander = service.generateCommander(
                    Map.of("name", "Sisko", "leadership", 8), "Captains_Log");

            assertNotNull(service.getCommander(commander.getId()));
        }
    }

    @Nested
    @DisplayName("Get Battle")
    class GetBattle {

        @Test
        @DisplayName("returns null for unknown battle")
        void returnsNullForUnknown() {
            assertNull(service.getBattle("nonexistent"));
        }
    }
}
