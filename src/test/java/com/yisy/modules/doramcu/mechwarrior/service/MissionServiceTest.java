package com.yisy.modules.doramcu.mechwarrior.service;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.model.MissionObjective;
import com.yisy.modules.doramcu.mechwarrior.model.MissionReward;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService.MissionResult;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService.PilotMissionStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MissionService Tests")
class MissionServiceTest {

    @Mock
    private MechWarriorGameEngine gameEngine;

    private MissionService missionService;

    @BeforeEach
    void setUp() {
        missionService = new MissionService(gameEngine);
    }

    // ---- Default Missions ----

    @Nested
    @DisplayName("Default Missions Tests")
    class DefaultMissionsTests {

        @Test
        @DisplayName("should initialize default missions")
        void initialize_shouldCreateDefaultMissions() {
            List<Mission> all = missionService.getAllMissions();
            assertTrue(all.size() >= 9, "Should have at least 9 default missions");
        }

        @Test
        @DisplayName("should have missions of different types")
        void initialize_shouldHaveDifferentTypes() {
            assertTrue(missionService.getMissionsByType(Mission.MissionType.ASSAULT).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.DEFENSE).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.ESCORT).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.RECON).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.SURVIVAL).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.SOLARIS).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.EXTRACTION).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.SABOTAGE).size() > 0);
            assertTrue(missionService.getMissionsByType(Mission.MissionType.CUSTOM).size() > 0);
        }

        @Test
        @DisplayName("should have missions of different difficulties")
        void initialize_shouldHaveDifferentDifficulties() {
            assertTrue(missionService.getMissionsByDifficulty(Mission.MissionDifficulty.EASY).size() > 0);
            assertTrue(missionService.getMissionsByDifficulty(Mission.MissionDifficulty.NORMAL).size() > 0);
            assertTrue(missionService.getMissionsByDifficulty(Mission.MissionDifficulty.HARD).size() > 0);
            assertTrue(missionService.getMissionsByDifficulty(Mission.MissionDifficulty.EXTREME).size() > 0);
        }

        @Test
        @DisplayName("should have missions from different factions")
        void initialize_shouldHaveDifferentFactions() {
            assertTrue(missionService.getMissionsByFaction("Federated Suns").size() > 0);
            assertTrue(missionService.getMissionsByFaction("Comstar").size() > 0);
            assertTrue(missionService.getMissionsByFaction("Clan Wolf").size() > 0);
            assertTrue(missionService.getMissionsByFaction("Solaris Syndicate").size() > 0);
        }

        @Test
        @DisplayName("should have all missions available by default")
        void initialize_shouldHaveAllAvailable() {
            List<Mission> available = missionService.getAvailableMissions();
            List<Mission> all = missionService.getAllMissions();
            assertEquals(all.size(), available.size());
        }

        @Test
        @DisplayName("should have assault mission with correct properties")
        void initialize_shouldHaveAssaultMission() {
            List<Mission> assaults = missionService.getMissionsByType(Mission.MissionType.ASSAULT);
            assertEquals(1, assaults.size());
            Mission assault = assaults.get(0);
            assertEquals("Operation: Iron Hammer", assault.getName());
            assertEquals("Federated Suns", assault.getFaction());
            assertEquals("Draconis Combine", assault.getEnemyFaction());
            assertEquals("Tharkad", assault.getPlanetName());
            assertEquals(6, assault.getEnemyCount());
        }

        @Test
        @DisplayName("should have defense mission with duration objective")
        void initialize_shouldHaveDefenseMission() {
            List<Mission> defenses = missionService.getMissionsByType(Mission.MissionType.DEFENSE);
            assertEquals(1, defenses.size());
            Mission defense = defenses.get(0);
            assertEquals("Hold the Line", defense.getName());
            assertEquals(300, defense.getTimeLimit());
            assertEquals(300, defense.getObjectives().get(0).getDurationSeconds());
        }

        @Test
        @DisplayName("should have repeatable custom mission")
        void initialize_shouldHaveRepeatableCustom() {
            List<Mission> custom = missionService.getMissionsByType(Mission.MissionType.CUSTOM);
            assertEquals(1, custom.size());
            assertTrue(custom.get(0).isRepeatable());
        }
    }

    // ---- Mission Browsing ----

    @Nested
    @DisplayName("Mission Browsing Tests")
    class BrowsingTests {

        @Test
        @DisplayName("should get mission by ID")
        void getMission_shouldReturnMission() {
            List<Mission> all = missionService.getAllMissions();
            String id = all.get(0).getId();
            Mission mission = missionService.getMission(id);
            assertNotNull(mission);
            assertEquals(id, mission.getId());
        }

        @Test
        @DisplayName("should return null for non-existent mission")
        void getMission_shouldReturnNull() {
            assertNull(missionService.getMission("non-existent-id"));
        }

        @Test
        @DisplayName("should filter by type")
        void getMissionsByType_shouldFilter() {
            List<Mission> assaults = missionService.getMissionsByType(Mission.MissionType.ASSAULT);
            for (Mission m : assaults) {
                assertEquals(Mission.MissionType.ASSAULT, m.getType());
            }
        }

        @Test
        @DisplayName("should filter by difficulty")
        void getMissionsByDifficulty_shouldFilter() {
            List<Mission> easy = missionService.getMissionsByDifficulty(Mission.MissionDifficulty.EASY);
            for (Mission m : easy) {
                assertEquals(Mission.MissionDifficulty.EASY, m.getDifficulty());
            }
        }

        @Test
        @DisplayName("should filter by faction")
        void getMissionsByFaction_shouldFilter() {
            List<Mission> fs = missionService.getMissionsByFaction("Federated Suns");
            for (Mission m : fs) {
                assertEquals("Federated Suns", m.getFaction());
            }
        }

        @Test
        @DisplayName("should return empty list for unknown faction")
        void getMissionsByFaction_shouldReturnEmpty() {
            List<Mission> unknown = missionService.getMissionsByFaction("Unknown Faction");
            assertTrue(unknown.isEmpty());
        }
    }

    // ---- Mission Acceptance ----

    @Nested
    @DisplayName("Mission Acceptance Tests")
    class AcceptanceTests {

        @Test
        @DisplayName("should accept available mission")
        void acceptMission_shouldAcceptAvailable() {
            Mission mission = missionService.getAvailableMissions().get(0);
            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertTrue(accepted);
            assertEquals(Mission.MissionStatus.ACCEPTED, mission.getStatus());
        }

        @Test
        @DisplayName("should not accept non-existent mission")
        void acceptMission_shouldNotAcceptNonExistent() {
            boolean accepted = missionService.acceptMission("pilot1", "non-existent");
            assertFalse(accepted);
        }

        @Test
        @DisplayName("should not accept already accepted mission")
        void acceptMission_shouldNotAcceptNotAvailable() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            boolean accepted = missionService.acceptMission("pilot2", mission.getId());
            assertFalse(accepted);
        }

        @Test
        @DisplayName("should not accept if pilot has active mission")
        void acceptMission_shouldNotAcceptIfActive() {
            List<Mission> available = missionService.getAvailableMissions()
                    .stream()
                    .filter(m -> !m.isRepeatable())
                    .collect(Collectors.toList());
            Mission mission1 = available.get(0);
            Mission mission2 = available.get(1);

            missionService.acceptMission("pilot1", mission1.getId());
            boolean accepted = missionService.acceptMission("pilot1", mission2.getId());
            assertFalse(accepted);
        }

        @Test
        @DisplayName("should accept repeatable mission with active mission")
        void acceptMission_shouldAcceptRepeatableWithActive() {
            List<Mission> custom = missionService.getMissionsByType(Mission.MissionType.CUSTOM);
            Mission customMission = custom.get(0);
            Mission otherMission = missionService.getAvailableMissions()
                    .stream()
                    .filter(m -> !m.getId().equals(customMission.getId()))
                    .findFirst()
                    .orElse(missionService.getAvailableMissions().get(0));

            missionService.acceptMission("pilot1", otherMission.getId());
            boolean accepted = missionService.acceptMission("pilot1", customMission.getId());
            assertTrue(accepted);
        }

        @Test
        @DisplayName("should not accept if prerequisite not met")
        void acceptMission_shouldNotAcceptIfPrerequisiteNotMet() {
            Mission mission = missionService.getAvailableMissions().get(0);
            mission.setPrerequisiteMissionId("non-existent-prereq");

            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertFalse(accepted);
        }

        @Test
        @DisplayName("should accept if prerequisite met")
        void acceptMission_shouldAcceptIfPrerequisiteMet() {
            Mission prereq = missionService.getAvailableMissions().get(0);
            Mission mission = missionService.getAvailableMissions().get(1);
            mission.setPrerequisiteMissionId(prereq.getId());

            // Simulate completing the prerequisite
            missionService.acceptMission("pilot1", prereq.getId());
            prereq.start();
            prereq.getObjectives().get(0).forceComplete();
            missionService.completeMission("pilot1", prereq.getId());

            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertTrue(accepted);
        }

        @Test
        @DisplayName("should not accept if reputation insufficient")
        void acceptMission_shouldNotAcceptIfReputationLow() {
            Mission mission = missionService.getAvailableMissions().get(0);
            mission.setMinReputation(100);

            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertFalse(accepted);
        }

        @Test
        @DisplayName("should accept if reputation sufficient")
        void acceptMission_shouldAcceptIfReputationSufficient() {
            Mission mission = missionService.getAvailableMissions().get(0);
            mission.setMinReputation(10);
            missionService.addReputation("pilot1", mission.getFaction(), 20);

            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertTrue(accepted);
        }

        @Test
        @DisplayName("should not accept if pilot level insufficient")
        void acceptMission_shouldNotAcceptIfPilotLevelLow() {
            Mission mission = missionService.getAvailableMissions().get(0);
            mission.setMinPilotLevel(10);

            boolean accepted = missionService.acceptMission("pilot1", mission.getId());
            assertFalse(accepted);
        }
    }

    // ---- Mission Start ----

    @Nested
    @DisplayName("Mission Start Tests")
    class StartTests {

        @Test
        @DisplayName("should start accepted mission")
        void startMission_shouldStartAccepted() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());

            MechWarriorSession session = mock(MechWarriorSession.class);
            when(gameEngine.createSession(anyString(), anyString(), anyInt())).thenReturn(session);

            MechWarriorSession result = missionService.startMission("pilot1", mission.getId());
            assertNotNull(result);
            assertEquals(Mission.MissionStatus.IN_PROGRESS, mission.getStatus());
        }

        @Test
        @DisplayName("should throw if mission not accepted")
        void startMission_shouldThrowIfNotAccepted() {
            Mission mission = missionService.getAvailableMissions().get(0);
            assertThrows(IllegalArgumentException.class, () ->
                    missionService.startMission("pilot1", mission.getId())
            );
        }

        @Test
        @DisplayName("should throw if mission not found")
        void startMission_shouldThrowIfNotFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    missionService.startMission("pilot1", "non-existent")
            );
        }
    }

    // ---- Mission Completion ----

    @Nested
    @DisplayName("Mission Completion Tests")
    class CompletionTests {

        @Test
        @DisplayName("should complete mission and return result")
        void completeMission_shouldReturnResult() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            MissionResult result = missionService.completeMission("pilot1", mission.getId());
            assertNotNull(result);
            assertTrue(result.isCompleted());
            assertEquals(mission.getId(), result.getMissionId());
            assertEquals(mission.getName(), result.getMissionName());
        }

        @Test
        @DisplayName("should update pilot stats on completion")
        void completeMission_shouldUpdatePilotStats() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.completeMission("pilot1", mission.getId());

            PilotMissionStats stats = missionService.getPilotStats("pilot1");
            assertNotNull(stats);
            assertEquals(1, stats.getWins());
            assertTrue(stats.getCompletedMissionIds().contains(mission.getId()));
        }

        @Test
        @DisplayName("should add reputation on completion")
        void completeMission_shouldAddReputation() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.completeMission("pilot1", mission.getId());

            int reputation = missionService.getReputation("pilot1", mission.getFaction());
            assertEquals(mission.getReputationReward(), reputation);
        }

        @Test
        @DisplayName("should add credits on completion")
        void completeMission_shouldAddCredits() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.completeMission("pilot1", mission.getId());

            int credits = missionService.getCredits("pilot1");
            assertTrue(credits > 0);
        }

        @Test
        @DisplayName("should reset repeatable mission on completion")
        void completeMission_shouldResetRepeatable() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.CUSTOM).get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.completeMission("pilot1", mission.getId());

            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should not reset non-repeatable mission")
        void completeMission_shouldNotResetNonRepeatable() {
            Mission mission = missionService.getAvailableMissions().get(0);
            // Make sure it's not repeatable
            mission.setRepeatable(false);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.completeMission("pilot1", mission.getId());

            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
        }

        @Test
        @DisplayName("should throw if mission not found")
        void completeMission_shouldThrowIfNotFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    missionService.completeMission("pilot1", "non-existent")
            );
        }

        @Test
        @DisplayName("should calculate credits with difficulty multiplier")
        void completeMission_shouldCalculateCreditsWithMultiplier() {
            Mission mission = missionService.getAvailableMissions().get(0);
            mission.setDifficulty(Mission.MissionDifficulty.HARD);
            mission.setCreditsReward(10000);
            mission.getObjectives().get(0).setTargetCount(1);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();
            mission.getObjectives().get(0).forceComplete();

            MissionResult result = missionService.completeMission("pilot1", mission.getId());
            // (10000 + 1000) * 1.5 = 16500
            assertEquals(16500, result.getCreditsEarned());
        }
    }

    // ---- Mission Failure ----

    @Nested
    @DisplayName("Mission Failure Tests")
    class FailureTests {

        @Test
        @DisplayName("should fail mission and return result")
        void failMission_shouldReturnResult() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            MissionResult result = missionService.failMission("pilot1", mission.getId());
            assertNotNull(result);
            assertFalse(result.isCompleted());
            assertEquals(mission.getId(), result.getMissionId());
        }

        @Test
        @DisplayName("should update pilot losses on failure")
        void failMission_shouldUpdateLosses() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.failMission("pilot1", mission.getId());

            PilotMissionStats stats = missionService.getPilotStats("pilot1");
            assertEquals(1, stats.getLosses());
        }

        @Test
        @DisplayName("should reduce reputation on failure")
        void failMission_shouldReduceReputation() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.failMission("pilot1", mission.getId());

            // Reputation should not go below 0
            int reputation = missionService.getReputation("pilot1", mission.getFaction());
            assertEquals(0, reputation);
        }

        @Test
        @DisplayName("should reset repeatable mission on failure")
        void failMission_shouldResetRepeatable() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.CUSTOM).get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.failMission("pilot1", mission.getId());

            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should throw if mission not found")
        void failMission_shouldThrowIfNotFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    missionService.failMission("pilot1", "non-existent")
            );
        }
    }

    // ---- Mission Abandon ----

    @Nested
    @DisplayName("Mission Abandon Tests")
    class AbandonTests {

        @Test
        @DisplayName("should abandon mission")
        void abandonMission_shouldAbandon() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.ASSAULT).get(0);
            assertTrue(missionService.acceptMission("pilot1", mission.getId()));

            missionService.abandonMission("pilot1", mission.getId());
            Mission updated = missionService.getMission(mission.getId());
            assertEquals(Mission.MissionStatus.ABANDONED, updated.getStatus());
        }

        @Test
        @DisplayName("should apply reputation penalty on abandon")
        void abandonMission_shouldApplyPenalty() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.addReputation("pilot1", mission.getFaction(), 20);
            missionService.acceptMission("pilot1", mission.getId());

            missionService.abandonMission("pilot1", mission.getId());

            int reputation = missionService.getReputation("pilot1", mission.getFaction());
            assertEquals(15, reputation); // 20 - 5
        }

        @Test
        @DisplayName("should reset repeatable mission on abandon")
        void abandonMission_shouldResetRepeatable() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.CUSTOM).get(0);
            missionService.acceptMission("pilot1", mission.getId());

            missionService.abandonMission("pilot1", mission.getId());

            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }
    }

    // ---- Objective Progress ----

    @Nested
    @DisplayName("Objective Progress Tests")
    class ObjectiveProgressTests {

        @Test
        @DisplayName("should record kill and progress destroy objectives")
        void recordKill_shouldProgressDestroyObjectives() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.ASSAULT).get(0);
            mission.getObjectives().get(0).setTargetCount(6);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.recordKill("pilot1", mission.getId());
            assertEquals(1, mission.getObjectives().get(0).getCurrentCount());
        }

        @Test
        @DisplayName("should auto-complete when all objectives done")
        void recordKill_shouldAutoComplete() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.ASSAULT).get(0);
            mission.getObjectives().get(0).setTargetCount(1);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.recordKill("pilot1", mission.getId());
            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
        }

        @Test
        @DisplayName("should record objective progress")
        void recordObjectiveProgress_shouldProgress() {
            Mission mission = missionService.getAvailableMissions().get(0);
            MissionObjective obj = mission.getObjectives().get(0);
            obj.setTargetCount(5);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.recordObjectiveProgress("pilot1", mission.getId(), obj.getId(), 3);
            assertEquals(3, obj.getCurrentCount());
        }

        @Test
        @DisplayName("should record time elapsed for survive objectives")
        void recordTimeElapsed_shouldProgressSurvive() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.DEFENSE).get(0);
            MissionObjective obj = mission.getObjectives().get(0);
            obj.setDurationSeconds(100);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.recordTimeElapsed("pilot1", mission.getId(), 50);
            assertEquals(50, obj.getElapsedSeconds());
        }

        @Test
        @DisplayName("should auto-complete when survive duration reached")
        void recordTimeElapsed_shouldAutoComplete() {
            Mission mission = missionService.getMissionsByType(Mission.MissionType.DEFENSE).get(0);
            MissionObjective obj = mission.getObjectives().get(0);
            obj.setDurationSeconds(100);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();

            missionService.recordTimeElapsed("pilot1", mission.getId(), 100);
            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
        }
    }

    // ---- Reputation ----

    @Nested
    @DisplayName("Reputation Tests")
    class ReputationTests {

        @Test
        @DisplayName("should get zero reputation by default")
        void getReputation_shouldReturnZero() {
            assertEquals(0, missionService.getReputation("pilot1", "Federated Suns"));
        }

        @Test
        @DisplayName("should add reputation")
        void addReputation_shouldAdd() {
            missionService.addReputation("pilot1", "Federated Suns", 15);
            assertEquals(15, missionService.getReputation("pilot1", "Federated Suns"));
        }

        @Test
        @DisplayName("should accumulate reputation")
        void addReputation_shouldAccumulate() {
            missionService.addReputation("pilot1", "Federated Suns", 10);
            missionService.addReputation("pilot1", "Federated Suns", 5);
            assertEquals(15, missionService.getReputation("pilot1", "Federated Suns"));
        }

        @Test
        @DisplayName("should not go below zero")
        void addReputation_shouldNotGoBelowZero() {
            missionService.addReputation("pilot1", "Federated Suns", 5);
            missionService.addReputation("pilot1", "Federated Suns", -10);
            assertEquals(0, missionService.getReputation("pilot1", "Federated Suns"));
        }

        @Test
        @DisplayName("should track separate reputation per faction")
        void addReputation_shouldTrackPerFaction() {
            missionService.addReputation("pilot1", "Federated Suns", 10);
            missionService.addReputation("pilot1", "Clan Wolf", 20);
            assertEquals(10, missionService.getReputation("pilot1", "Federated Suns"));
            assertEquals(20, missionService.getReputation("pilot1", "Clan Wolf"));
        }

        @Test
        @DisplayName("should get all reputations")
        void getAllReputations_shouldReturnAll() {
            missionService.addReputation("pilot1", "Federated Suns", 10);
            missionService.addReputation("pilot1", "Clan Wolf", 20);

            Map<String, Integer> reps = missionService.getAllReputations("pilot1");
            assertEquals(2, reps.size());
            assertEquals(10, reps.get("Federated Suns").intValue());
            assertEquals(20, reps.get("Clan Wolf").intValue());
        }

        @Test
        @DisplayName("should return empty map for unknown pilot")
        void getAllReputations_shouldReturnEmpty() {
            Map<String, Integer> reps = missionService.getAllReputations("unknown");
            assertTrue(reps.isEmpty());
        }
    }

    // ---- Credits ----

    @Nested
    @DisplayName("Credits Tests")
    class CreditsTests {

        @Test
        @DisplayName("should get zero credits by default")
        void getCredits_shouldReturnZero() {
            assertEquals(0, missionService.getCredits("pilot1"));
        }

        @Test
        @DisplayName("should add credits")
        void addCredits_shouldAdd() {
            missionService.addCredits("pilot1", 10000);
            assertEquals(10000, missionService.getCredits("pilot1"));
        }

        @Test
        @DisplayName("should accumulate credits")
        void addCredits_shouldAccumulate() {
            missionService.addCredits("pilot1", 5000);
            missionService.addCredits("pilot1", 3000);
            assertEquals(8000, missionService.getCredits("pilot1"));
        }

        @Test
        @DisplayName("should spend credits when sufficient")
        void spendCredits_shouldSpend() {
            missionService.addCredits("pilot1", 10000);
            boolean success = missionService.spendCredits("pilot1", 3000);
            assertTrue(success);
            assertEquals(7000, missionService.getCredits("pilot1"));
        }

        @Test
        @DisplayName("should not spend credits when insufficient")
        void spendCredits_shouldNotSpend() {
            missionService.addCredits("pilot1", 1000);
            boolean success = missionService.spendCredits("pilot1", 5000);
            assertFalse(success);
            assertEquals(1000, missionService.getCredits("pilot1"));
        }
    }

    // ---- Active Mission ----

    @Nested
    @DisplayName("Active Mission Tests")
    class ActiveMissionTests {

        @Test
        @DisplayName("should return active mission")
        void getActiveMission_shouldReturnActive() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());

            Mission active = missionService.getActiveMission("pilot1");
            assertNotNull(active);
            assertEquals(mission.getId(), active.getId());
        }

        @Test
        @DisplayName("should return null when no active mission")
        void getActiveMission_shouldReturnNull() {
            assertNull(missionService.getActiveMission("pilot1"));
        }

        @Test
        @DisplayName("should return null after mission completed")
        void getActiveMission_shouldReturnNullAfterComplete() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();
            missionService.completeMission("pilot1", mission.getId());

            assertNull(missionService.getActiveMission("pilot1"));
        }
    }

    // ---- Custom Mission ----

    @Nested
    @DisplayName("Custom Mission Tests")
    class CustomMissionTests {

        @Test
        @DisplayName("should create custom mission")
        void createCustomMission_shouldCreate() {
            int before = missionService.getAllMissions().size();
            Mission custom = missionService.createCustomMission(
                    "Custom Battle",
                    Mission.MissionType.CUSTOM,
                    Mission.MissionDifficulty.NORMAL,
                    "Independent",
                    "Test battle",
                    4,
                    10000
            );
            int after = missionService.getAllMissions().size();
            assertEquals(before + 1, after);
            assertEquals("Custom Battle", custom.getName());
            assertTrue(custom.isRepeatable());
        }

        @Test
        @DisplayName("should create mission with destroy objective")
        void createCustomMission_shouldCreateObjective() {
            Mission custom = missionService.createCustomMission(
                    "Custom Battle",
                    Mission.MissionType.CUSTOM,
                    Mission.MissionDifficulty.NORMAL,
                    "Independent",
                    "Test battle",
                    6,
                    10000
            );
            assertEquals(1, custom.getObjectives().size());
            assertEquals(MissionObjective.ObjectiveType.DESTROY_ENEMIES,
                    custom.getObjectives().get(0).getType());
            assertEquals(6, custom.getObjectives().get(0).getTargetCount());
        }

        @Test
        @DisplayName("should remove mission")
        void removeMission_shouldRemove() {
            Mission custom = missionService.createCustomMission(
                    "Custom Battle",
                    Mission.MissionType.CUSTOM,
                    Mission.MissionDifficulty.NORMAL,
                    "Independent",
                    "Test battle",
                    4,
                    10000
            );
            int before = missionService.getAllMissions().size();
            missionService.removeMission(custom.getId());
            assertEquals(before - 1, missionService.getAllMissions().size());
            assertNull(missionService.getMission(custom.getId()));
        }
    }

    // ---- Pilot Stats ----

    @Nested
    @DisplayName("Pilot Stats Tests")
    class PilotStatsTests {

        @Test
        @DisplayName("should return null for unknown pilot")
        void getPilotStats_shouldReturnNull() {
            assertNull(missionService.getPilotStats("unknown"));
        }

        @Test
        @DisplayName("should create stats on first mission completion")
        void getPilotStats_shouldCreateOnCompletion() {
            Mission mission = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();
            missionService.completeMission("pilot1", mission.getId());

            PilotMissionStats stats = missionService.getPilotStats("pilot1");
            assertNotNull(stats);
            assertEquals(1, stats.getWins());
            assertEquals(0, stats.getLosses());
        }

        @Test
        @DisplayName("should level up based on experience")
        void getPilotStats_shouldLevelUp() {
            // Create a custom mission with known experience reward
            Mission mission = missionService.createCustomMission(
                    "Level Up Test",
                    Mission.MissionType.CUSTOM,
                    Mission.MissionDifficulty.NORMAL,
                    "Independent",
                    "Test level up",
                    1,
                    1000
            );
            mission.setExperienceReward(1000);
            missionService.acceptMission("pilot1", mission.getId());
            mission.start();
            missionService.completeMission("pilot1", mission.getId());

            PilotMissionStats stats = missionService.getPilotStats("pilot1");
            // 1000 XP / 500 + 1 = level 3
            assertEquals(3, stats.getLevel());
        }

        @Test
        @DisplayName("should calculate win rate")
        void getPilotStats_shouldCalculateWinRate() {
            // Complete one mission
            Mission mission1 = missionService.getAvailableMissions().get(0);
            missionService.acceptMission("pilot1", mission1.getId());
            mission1.start();
            missionService.completeMission("pilot1", mission1.getId());

            // Fail another mission
            Mission mission2 = missionService.getAvailableMissions().get(1);
            missionService.acceptMission("pilot1", mission2.getId());
            mission2.start();
            missionService.failMission("pilot1", mission2.getId());

            PilotMissionStats stats = missionService.getPilotStats("pilot1");
            assertEquals(1, stats.getWins());
            assertEquals(1, stats.getLosses());
            assertEquals(50.0, stats.getWinRate());
        }

        @Test
        @DisplayName("should return zero win rate with no games")
        void getPilotStats_shouldReturnZeroWinRate() {
            PilotMissionStats stats = new PilotMissionStats();
            assertEquals(0.0, stats.getWinRate());
        }
    }

    // ---- MissionResult ----

    @Nested
    @DisplayName("MissionResult Tests")
    class MissionResultTests {

        @Test
        @DisplayName("should have timestamp")
        void missionResult_shouldHaveTimestamp() {
            MissionResult result = new MissionResult();
            assertTrue(result.getTimestamp() > 0);
        }

        @Test
        @DisplayName("should set all fields")
        void missionResult_shouldSetAllFields() {
            MissionResult result = new MissionResult();
            result.setMissionId("test-id");
            result.setMissionName("Test Mission");
            result.setCompleted(true);
            result.setCreditsEarned(10000);
            result.setExperienceEarned(200);
            result.setReputationEarned(15);

            assertEquals("test-id", result.getMissionId());
            assertEquals("Test Mission", result.getMissionName());
            assertTrue(result.isCompleted());
            assertEquals(10000, result.getCreditsEarned());
            assertEquals(200, result.getExperienceEarned());
            assertEquals(15, result.getReputationEarned());
        }
    }

    // ---- PilotMissionStats ----

    @Nested
    @DisplayName("PilotMissionStats Tests")
    class PilotMissionStatsTests {

        @Test
        @DisplayName("should initialize with defaults")
        void pilotMissionStats_shouldInitializeDefaults() {
            PilotMissionStats stats = new PilotMissionStats();
            assertEquals(0, stats.getWins());
            assertEquals(0, stats.getLosses());
            assertEquals(0, stats.getTotalCredits());
            assertEquals(0, stats.getTotalExperience());
            assertEquals(1, stats.getLevel());
            assertTrue(stats.getCompletedMissionIds().isEmpty());
        }

        @Test
        @DisplayName("should add completed mission")
        void pilotMissionStats_shouldAddCompletedMission() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addCompletedMission("mission-1");
            assertTrue(stats.getCompletedMissionIds().contains("mission-1"));
        }

        @Test
        @DisplayName("should add credits")
        void pilotMissionStats_shouldAddCredits() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addCredits(10000);
            assertEquals(10000, stats.getTotalCredits());
        }

        @Test
        @DisplayName("should add experience and level up")
        void pilotMissionStats_shouldAddExperienceAndLevelUp() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addExperience(500);
            assertEquals(500, stats.getTotalExperience());
            // 500 / 500 + 1 = 2
            assertEquals(2, stats.getLevel());
        }

        @Test
        @DisplayName("should level up multiple times")
        void pilotMissionStats_shouldLevelUpMultiple() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addExperience(1500);
            // 1500 / 500 + 1 = 4
            assertEquals(4, stats.getLevel());
        }

        @Test
        @DisplayName("should add wins")
        void pilotMissionStats_shouldAddWins() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addWins();
            stats.addWins();
            assertEquals(2, stats.getWins());
        }

        @Test
        @DisplayName("should add losses")
        void pilotMissionStats_shouldAddLosses() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addLosses();
            assertEquals(1, stats.getLosses());
        }

        @Test
        @DisplayName("should calculate 100% win rate")
        void pilotMissionStats_shouldCalculateHundredPercentWinRate() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addWins();
            stats.addWins();
            assertEquals(100.0, stats.getWinRate());
        }

        @Test
        @DisplayName("should calculate 0% win rate")
        void pilotMissionStats_shouldCalculateZeroPercentWinRate() {
            PilotMissionStats stats = new PilotMissionStats();
            stats.addLosses();
            stats.addLosses();
            assertEquals(0.0, stats.getWinRate());
        }
    }
}
