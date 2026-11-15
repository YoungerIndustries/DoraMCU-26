package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.ShipTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipCombatService Tests")
class ShipCombatServiceTest {

    private ShipCombatService service;

    @BeforeEach
    void setUp() {
        service = new ShipCombatService();
    }

    // ---- getAllTemplates ----

    @Nested
    @DisplayName("getAllTemplates Tests")
    class GetAllTemplatesTests {

        @Test
        @DisplayName("should return all 8 ship templates")
        void getAllTemplates_shouldReturnAll() {
            List<ShipTemplate> templates = service.getAllTemplates();

            assertNotNull(templates);
            assertEquals(8, templates.size());
        }

        @Test
        @DisplayName("should include all ship classes")
        void getAllTemplates_shouldIncludeAllClasses() {
            List<ShipTemplate> templates = service.getAllTemplates();

            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.SCOUT));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.DESTROYER));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.LIGHT_CRUISER));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.HEAVY_CRUISER));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.BATTLECRUISER));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.BATTLESHIP));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.CARRIER));
            assertTrue(templates.stream().anyMatch(t -> t.getShipClass() == ShipTemplate.ShipClass.DREADNOUGHT));
        }
    }

    // ---- getTemplatesByClass ----

    @Nested
    @DisplayName("getTemplatesByClass Tests")
    class GetTemplatesByClassTests {

        @Test
        @DisplayName("should return templates for a specific class")
        void getTemplatesByClass_shouldReturnTemplates() {
            List<ShipTemplate> templates = service.getTemplatesByClass(ShipTemplate.ShipClass.DESTROYER);

            assertNotNull(templates);
            assertTrue(templates.size() >= 1);
            assertEquals(ShipTemplate.ShipClass.DESTROYER, templates.get(0).getShipClass());
        }
    }

    // ---- createBattle ----

    @Nested
    @DisplayName("createBattle Tests")
    class CreateBattleTests {

        @Test
        @DisplayName("should create a High Guard battle")
        void createBattle_shouldCreateHighGuard() {
            BattleSession session = service.createBattle(
                    "battle-1", "Imperial Navy", List.of("Destroyer"),
                    "Rebels", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Core World");

            assertNotNull(session);
            assertEquals("battle-1", session.getId());
            assertEquals(BattleSession.RulesType.HIGH_GUARD, session.getRulesType());
            assertEquals(BattleSession.BattleState.IN_PROGRESS, session.getState());
            assertEquals("Core World", session.getSector());
        }

        @Test
        @DisplayName("should create a battle with multiple ships")
        void createBattle_shouldCreateMultipleShips() {
            BattleSession session = service.createBattle(
                    "battle-2", "Fleet A", List.of("Destroyer", "Light Cruiser"),
                    "Fleet B", List.of("Scout/Courier", "Destroyer"),
                    BattleSession.RulesType.HIGH_GUARD, "Unknown Sector");

            assertEquals(2, session.getFleet0().getShips().size());
            assertEquals(2, session.getFleet1().getShips().size());
        }

        @Test
        @DisplayName("should throw for unknown template")
        void createBattle_shouldThrowForUnknownTemplate() {
            assertThrows(IllegalArgumentException.class, () -> service.createBattle(
                    "battle-3", "Fleet A", List.of("Nonexistent"),
                    "Fleet B", List.of("Destroyer"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector"));
        }

        @Test
        @DisplayName("should store battle in memory")
        void createBattle_shouldStoreInMemory() {
            service.createBattle(
                    "battle-4", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            assertTrue(service.listBattles().contains("battle-4"));
        }

        @Test
        @DisplayName("should create battle with Classic rules (falls back to High Guard)")
        void createBattle_classicRulesFallback() {
            BattleSession session = service.createBattle(
                    "battle-5", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.CLASSIC_TRAVELLER, "Sector");

            assertNotNull(session);
            assertEquals(BattleSession.BattleState.IN_PROGRESS, session.getState());
            // Classic rules with High Guard templates falls back to High Guard engine
            assertNotNull(session.getHighGuardCombat());
        }

        @Test
        @DisplayName("should add battle started event")
        void createBattle_shouldAddStartedEvent() {
            BattleSession session = service.createBattle(
                    "battle-6", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            assertEquals(1, session.getEvents().size());
            assertEquals("BATTLE_STARTED", session.getEvents().get(0).get("type"));
        }
    }

    // ---- getBattle ----

    @Nested
    @DisplayName("getBattle Tests")
    class GetBattleTests {

        @Test
        @DisplayName("should return battle by ID")
        void getBattle_shouldReturnBattle() {
            service.createBattle(
                    "battle-7", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            BattleSession session = service.getBattle("battle-7");
            assertEquals("battle-7", session.getId());
        }

        @Test
        @DisplayName("should throw for unknown battle")
        void getBattle_shouldThrowForUnknown() {
            assertThrows(IllegalArgumentException.class, () -> service.getBattle("nonexistent"));
        }
    }

    // ---- resolveRound ----

    @Nested
    @DisplayName("resolveRound Tests")
    class ResolveRoundTests {

        @Test
        @DisplayName("should resolve a round and update state")
        void resolveRound_shouldUpdateState() {
            service.createBattle(
                    "battle-8", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            BattleSession session = service.resolveRound("battle-8");

            assertNotNull(session);
            assertTrue(session.getCurrentRound() >= 1);
            assertTrue(session.getEvents().size() > 1); // Started + round events
        }

        @Test
        @DisplayName("should throw for completed battle")
        void resolveRound_shouldThrowForCompleted() {
            service.createBattle(
                    "battle-9", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            // Resolve to completion first
            service.resolveToCompletion("battle-9");

            assertThrows(IllegalStateException.class, () -> service.resolveRound("battle-9"));
        }

        @Test
        @DisplayName("should throw for unknown battle")
        void resolveRound_shouldThrowForUnknown() {
            assertThrows(IllegalArgumentException.class, () -> service.resolveRound("nonexistent"));
        }
    }

    // ---- resolveToCompletion ----

    @Nested
    @DisplayName("resolveToCompletion Tests")
    class ResolveToCompletionTests {

        @Test
        @DisplayName("should resolve battle to completion")
        void resolveToCompletion_shouldComplete() {
            service.createBattle(
                    "battle-10", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            BattleSession session = service.resolveToCompletion("battle-10");

            assertNotEquals(BattleSession.BattleState.IN_PROGRESS, session.getState());
            assertNotNull(session.getOutcome());
            assertTrue(session.getCurrentRound() > 0);
        }

        @Test
        @DisplayName("should have a valid outcome")
        void resolveToCompletion_shouldHaveOutcome() {
            service.createBattle(
                    "battle-11", "Fleet A", List.of("Battleship"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            BattleSession session = service.resolveToCompletion("battle-11");

            assertTrue(session.getOutcome().contains("SIDE_0") ||
                    session.getOutcome().contains("SIDE_1") ||
                    session.getOutcome().contains("MUTUAL"));
        }

        @Test
        @DisplayName("should log battle ended event")
        void resolveToCompletion_shouldLogEndedEvent() {
            service.createBattle(
                    "battle-12", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            service.resolveToCompletion("battle-12");
            BattleSession session = service.getBattle("battle-12");

            boolean hasEnded = session.getEvents().stream()
                    .anyMatch(e -> "BATTLE_ENDED".equals(e.get("type")));
            assertTrue(hasEnded);
        }
    }

    // ---- listBattles ----

    @Nested
    @DisplayName("listBattles Tests")
    class ListBattlesTests {

        @Test
        @DisplayName("should return empty list initially")
        void listBattles_shouldReturnEmpty() {
            assertTrue(service.listBattles().isEmpty());
        }

        @Test
        @DisplayName("should return battle IDs")
        void listBattles_shouldReturnIds() {
            service.createBattle(
                    "battle-a", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");
            service.createBattle(
                    "battle-b", "Fleet A", List.of("Destroyer"),
                    "Fleet B", List.of("Scout/Courier"),
                    BattleSession.RulesType.HIGH_GUARD, "Sector");

            List<String> battles = service.listBattles();
            assertEquals(2, battles.size());
            assertTrue(battles.contains("battle-a"));
            assertTrue(battles.contains("battle-b"));
        }
    }
}
