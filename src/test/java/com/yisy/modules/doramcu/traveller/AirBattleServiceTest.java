package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirBattleService")
class AirBattleServiceTest {

    @Nested
    @DisplayName("Create Battle")
    class CreateBattle {

        @Test
        @DisplayName("creates and stores battle")
        void createsBattle() {
            AirBattleService service = new AirBattleService();
            TravellerSpacecraft atk = new TravellerSpacecraft("atk1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            TravellerSpacecraft def = new TravellerSpacecraft("def1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            AirBattle battle = service.createBattle("battle-1",
                    List.of(atk), List.of(def),
                    AirBattle.Altitude.MEDIUM, AirBattle.BattleMode.ABSTRACT);

            assertEquals("battle-1", battle.getId());
            assertEquals(1, battle.getAttackerUnits().size());
            assertEquals(1, battle.getDefenderUnits().size());
            assertEquals(AirBattle.Altitude.MEDIUM, battle.getAltitude());
        }

        @Test
        @DisplayName("retrieves created battle")
        void retrievesBattle() {
            AirBattleService service = new AirBattleService();
            service.createBattle("battle-1",
                    List.of(new TravellerSpacecraft("atk1", "Fighter", TravellerSpacecraftType.FIGHTER, 7)),
                    List.of(new TravellerSpacecraft("def1", "Fighter", TravellerSpacecraftType.FIGHTER, 7)),
                    AirBattle.Altitude.MEDIUM, AirBattle.BattleMode.ABSTRACT);

            AirBattle battle = service.getBattle("battle-1");
            assertNotNull(battle);
            assertEquals("battle-1", battle.getId());
        }

        @Test
        @DisplayName("returns null for unknown battle")
        void returnsNullForUnknown() {
            AirBattleService service = new AirBattleService();

            assertNull(service.getBattle("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Resolve Battle")
    class ResolveBattle {

        @Test
        @DisplayName("resolves abstract battle")
        void resolvesAbstract() {
            AirBattleService service = new AirBattleService();
            TravellerSpacecraft atk = new TravellerSpacecraft("atk1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            atk.setAttackValue(10);
            TravellerSpacecraft def = new TravellerSpacecraft("def1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            def.setDefenseValue(2);
            service.createBattle("battle-1", List.of(atk), List.of(def),
                    AirBattle.Altitude.MEDIUM, AirBattle.BattleMode.ABSTRACT);

            AirBattleResult result = service.resolveBattle("battle-1");

            assertNotNull(result);
            assertEquals("battle-1", result.getBattleId());
        }

        @Test
        @DisplayName("resolves detailed battle")
        void resolvesDetailed() {
            AirBattleService service = new AirBattleService();
            TravellerSpacecraft atk = new TravellerSpacecraft("atk1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            atk.setAttackValue(10);
            atk.setHullPoints(50);
            TravellerSpacecraft def = new TravellerSpacecraft("def1", "Fighter", TravellerSpacecraftType.FIGHTER, 7);
            def.setAttackValue(1);
            def.setHullPoints(10);
            service.createBattle("battle-1", List.of(atk), List.of(def),
                    AirBattle.Altitude.MEDIUM, AirBattle.BattleMode.DETAILED);

            AirBattleResult result = service.resolveBattle("battle-1");

            assertNotNull(result);
            assertEquals(AirBattle.BattleState.COMPLETED, service.getBattle("battle-1").getState());
        }

        @Test
        @DisplayName("throws for unknown battle")
        void throwsForUnknown() {
            AirBattleService service = new AirBattleService();

            assertThrows(IllegalArgumentException.class, () -> service.resolveBattle("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Create Spacecraft")
    class CreateSpacecraft {

        @Test
        @DisplayName("delegates to factory")
        void delegatesToFactory() {
            AirBattleService service = new AirBattleService();
            TravellerSpacecraft sc = service.createSpacecraft(TravellerSpacecraftType.FIGHTER, 7);

            assertNotNull(sc);
            assertEquals(TravellerSpacecraftType.FIGHTER, sc.getType());
            assertEquals(7, sc.getTechLevel());
        }
    }

    @Nested
    @DisplayName("Convert Aircraft")
    class ConvertAircraft {

        @Test
        @DisplayName("delegates to conversion service")
        void delegatesToConversion() {
            AirBattleService service = new AirBattleService();
            TravellerSpacecraft sc = service.convertAircraft(ThirdReichAircraft.MESSERSCHMITT_BF109);

            assertNotNull(sc);
            assertEquals(TravellerSpacecraftType.FIGHTER, sc.getType());
        }
    }
}
