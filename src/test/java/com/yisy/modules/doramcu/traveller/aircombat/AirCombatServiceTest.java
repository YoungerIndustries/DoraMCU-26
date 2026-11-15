package com.yisy.modules.doramcu.traveller.aircombat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AirCombatService Tests")
@ExtendWith(MockitoExtension.class)
class AirCombatServiceTest {

    @Mock
    private AircraftDesignRepository aircraftRepository;

    @Mock
    private AirCombatMissionRepository missionRepository;

    private AirCombatService service;

    @BeforeEach
    void setUp() {
        service = new AirCombatService(aircraftRepository, missionRepository);
    }

    // ---- Aircraft Queries ----

    @Nested
    @DisplayName("Aircraft Query Tests")
    class AircraftQueryTests {

        @Test
        @DisplayName("getAllAircraft should return all aircraft")
        void getAllAircraft_shouldReturnAll() {
            AircraftDesign f1 = new AircraftDesign();
            f1.setId(1L);
            f1.setName("Star Kraken");
            AircraftDesign f2 = new AircraftDesign();
            f2.setId(2L);
            f2.setName("Star Cobra");
            when(aircraftRepository.findAll()).thenReturn(List.of(f1, f2));

            List<AircraftDesign> aircraft = service.getAllAircraft();

            assertEquals(2, aircraft.size());
            assertEquals("Star Kraken", aircraft.get(0).getName());
        }

        @Test
        @DisplayName("getAllAircraft should return empty list")
        void getAllAircraft_shouldReturnEmpty() {
            when(aircraftRepository.findAll()).thenReturn(List.of());

            List<AircraftDesign> aircraft = service.getAllAircraft();

            assertTrue(aircraft.isEmpty());
        }

        @Test
        @DisplayName("getAircraftByType should filter by type")
        void getAircraftByType_shouldFilter() {
            AircraftDesign f1 = new AircraftDesign();
            f1.setId(1L);
            f1.setType("FIGHTER");
            when(aircraftRepository.findByType("FIGHTER")).thenReturn(List.of(f1));

            List<AircraftDesign> aircraft = service.getAircraftByType("FIGHTER");

            assertEquals(1, aircraft.size());
            assertEquals("FIGHTER", aircraft.get(0).getType());
        }

        @Test
        @DisplayName("getAircraftByType should return empty for unknown type")
        void getAircraftByType_shouldReturnEmpty() {
            when(aircraftRepository.findByType("UNKNOWN")).thenReturn(List.of());

            List<AircraftDesign> aircraft = service.getAircraftByType("UNKNOWN");

            assertTrue(aircraft.isEmpty());
        }
    }

    // ---- Mission Queries ----

    @Nested
    @DisplayName("Mission Query Tests")
    class MissionQueryTests {

        @Test
        @DisplayName("getMissions should return missions for character")
        void getMissions_shouldReturnMissions() {
            AirCombatMission m1 = new AirCombatMission();
            m1.setId(1L);
            m1.setCharacterId("player1");
            m1.setStatus("ACTIVE");
            when(missionRepository.findByCharacterIdOrderByCreatedAtDesc("player1"))
                    .thenReturn(List.of(m1));

            List<AirCombatMission> missions = service.getMissions("player1");

            assertEquals(1, missions.size());
            assertEquals("player1", missions.get(0).getCharacterId());
        }

        @Test
        @DisplayName("getMissions should return empty for no missions")
        void getMissions_shouldReturnEmpty() {
            when(missionRepository.findByCharacterIdOrderByCreatedAtDesc("player1"))
                    .thenReturn(List.of());

            List<AirCombatMission> missions = service.getMissions("player1");

            assertTrue(missions.isEmpty());
        }
    }

    // ---- Mission Creation ----

    @Nested
    @DisplayName("Mission Creation Tests")
    class MissionCreationTests {

        @Test
        @DisplayName("createMission should create active mission")
        void createMission_shouldCreateActive() {
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatMission mission = service.createMission("player1", "AIR_TO_AIR");

            assertNotNull(mission);
            assertEquals("player1", mission.getCharacterId());
            assertEquals("AIR_TO_AIR", mission.getMissionType());
            assertEquals("ACTIVE", mission.getStatus());
            assertNotNull(mission.getTitle());
            assertTrue(mission.getDifficulty() >= 2 && mission.getDifficulty() <= 9);
            assertTrue(mission.getEnemyCount() >= 3 && mission.getEnemyCount() <= 12);
            assertTrue(mission.getXpReward() > 0);
            assertTrue(mission.getCreditReward() > 0);
        }

        @Test
        @DisplayName("createMission with null type should default to AIR_TO_AIR")
        void createMission_nullType_shouldDefault() {
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatMission mission = service.createMission("player1", null);

            assertEquals("AIR_TO_AIR", mission.getMissionType());
        }

        @Test
        @DisplayName("createMission should set enemy type")
        void createMission_shouldSetEnemyType() {
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatMission mission = service.createMission("player1", "AIR_TO_GROUND");

            assertNotNull(mission.getEnemyType());
            assertTrue(mission.getEnemyType().equals("Pirate Fighters") ||
                    mission.getEnemyType().equals("Rebel Bombers") ||
                    mission.getEnemyType().equals("Alien Craft") ||
                    mission.getEnemyType().equals("Mercenary Jets"));
        }

        @Test
        @DisplayName("createMission should set title from predefined list")
        void createMission_shouldSetTitle() {
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatMission mission = service.createMission("player1", "AIR_TO_AIR");

            assertNotNull(mission.getTitle());
            assertTrue(mission.getTitle().length() > 0);
        }

        @Test
        @DisplayName("createMission should set description")
        void createMission_shouldSetDescription() {
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatMission mission = service.createMission("player1", "AIR_TO_AIR");

            assertNotNull(mission.getDescription());
            assertTrue(mission.getDescription().contains("AIR_TO_AIR"));
        }
    }

    // ---- Mission Resolution ----

    @Nested
    @DisplayName("Mission Resolution Tests")
    class MissionResolutionTests {

        private AirCombatMission activeMission;

        @BeforeEach
        void createMission() {
            activeMission = new AirCombatMission();
            activeMission.setId(1L);
            activeMission.setCharacterId("player1");
            activeMission.setStatus("ACTIVE");
            activeMission.setDifficulty(5);
            activeMission.setXpReward(100);
            activeMission.setCreditReward(500);
            activeMission.setEnemyType("Pirate Fighters");
        }

        @Test
        @DisplayName("resolveMission should throw for missing mission")
        void resolveMission_shouldThrowForMissing() {
            when(missionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.resolveMission(999L));
        }

        @Test
        @DisplayName("resolveMission should throw for non-active mission")
        void resolveMission_shouldThrowForNonActive() {
            activeMission.setStatus("COMPLETED");
            when(missionRepository.findById(1L)).thenReturn(Optional.of(activeMission));

            assertThrows(IllegalStateException.class, () -> service.resolveMission(1L));
        }

        @Test
        @DisplayName("resolveMission should return result")
        void resolveMission_shouldReturnResult() {
            AirCombatMission mission = new AirCombatMission();
            mission.setId(1L);
            mission.setCharacterId("player1");
            mission.setStatus("ACTIVE");
            mission.setDifficulty(5);
            mission.setXpReward(100);
            mission.setCreditReward(500);
            mission.setEnemyType("Pirate Fighters");

            when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatService.MissionResult result = service.resolveMission(1L);
            assertNotNull(result);
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("resolveMission should set status to COMPLETED or FAILED")
        void resolveMission_shouldSetStatus() {
            AirCombatMission mission = new AirCombatMission();
            mission.setId(1L);
            mission.setCharacterId("player1");
            mission.setStatus("ACTIVE");
            mission.setDifficulty(5);
            mission.setXpReward(100);
            mission.setCreditReward(500);
            mission.setEnemyType("Pirate Fighters");

            when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatService.MissionResult result = service.resolveMission(1L);
            assertNotNull(result);
            // Status is either COMPLETED or FAILED
            assertTrue("COMPLETED".equals(mission.getStatus()) || "FAILED".equals(mission.getStatus()));
        }

        @Test
        @DisplayName("resolveMission should grant XP rewards")
        void resolveMission_shouldGrantXP() {
            AirCombatMission mission = new AirCombatMission();
            mission.setId(1L);
            mission.setCharacterId("player1");
            mission.setStatus("ACTIVE");
            mission.setDifficulty(5);
            mission.setXpReward(100);
            mission.setCreditReward(500);
            mission.setEnemyType("Pirate Fighters");

            when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatService.MissionResult result = service.resolveMission(1L);
            // XP is always granted: full (100), half (50), or quarter (25)
            assertTrue(result.getXpEarned() >= 25 && result.getXpEarned() <= 100);
        }

        @Test
        @DisplayName("resolveMission should grant credits on success")
        void resolveMission_shouldGrantCredits() {
            AirCombatMission mission = new AirCombatMission();
            mission.setId(1L);
            mission.setCharacterId("player1");
            mission.setStatus("ACTIVE");
            mission.setDifficulty(5);
            mission.setXpReward(100);
            mission.setCreditReward(500);
            mission.setEnemyType("Pirate Fighters");

            when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
            when(missionRepository.save(any(AirCombatMission.class))).thenAnswer(inv -> inv.getArgument(0));

            AirCombatService.MissionResult result = service.resolveMission(1L);
            // Credits: full (500), half (250), or 0 on failure
            assertTrue(result.getCreditsEarned() >= 0 && result.getCreditsEarned() <= 500);
        }
    }

    // ---- MissionResult ----

    @Nested
    @DisplayName("MissionResult Tests")
    class MissionResultTests {

        @Test
        @DisplayName("MissionResult should store all fields")
        void missionResult_shouldStoreFields() {
            AirCombatService.MissionResult result = new AirCombatService.MissionResult();
            result.setSuccess(true);
            result.setMessage("Complete!");
            result.setXpEarned(100);
            result.setCreditsEarned(500);

            assertTrue(result.isSuccess());
            assertEquals("Complete!", result.getMessage());
            assertEquals(100, result.getXpEarned());
            assertEquals(500, result.getCreditsEarned());
        }
    }
}
