package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mission Model Tests")
class MissionTest {

    private Mission mission;

    @BeforeEach
    void setUp() {
        mission = new Mission(
                "Test Mission",
                Mission.MissionType.ASSAULT,
                Mission.MissionDifficulty.NORMAL,
                "Federated Suns"
        );
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertEquals("Test Mission", mission.getName());
            assertEquals(Mission.MissionType.ASSAULT, mission.getType());
            assertEquals(Mission.MissionDifficulty.NORMAL, mission.getDifficulty());
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
            assertEquals("Federated Suns", mission.getFaction());
            assertNotNull(mission.getId());
            assertTrue(mission.getObjectives() instanceof ArrayList);
            assertTrue(mission.getRewards() instanceof ArrayList);
            assertEquals(0, mission.getTimeLimit());
            assertEquals(1, mission.getWaveCount());
            assertEquals(4, mission.getEnemyCount());
            assertEquals(4, mission.getPlayerLanceSize());
            assertEquals(10000, mission.getCreditsReward());
            assertEquals(5000, mission.getSalvageReward());
            assertEquals(100, mission.getExperienceReward());
            assertEquals(10, mission.getReputationReward());
            assertFalse(mission.isCampaign());
            assertFalse(mission.isRepeatable());
            assertFalse(mission.isCoop());
            assertEquals(1, mission.getMaxCoopPlayers());
            assertEquals(0, mission.getMinReputation());
            assertEquals(0, mission.getMinPilotLevel());
            assertTrue(mission.getCreatedAt() > 0);
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyMission() {
            Mission empty = new Mission();
            assertNull(empty.getId());
            assertNull(empty.getName());
            assertNull(empty.getObjectives());
        }

        @Test
        @DisplayName("should support all mission types")
        void constructor_shouldSupportAllMissionTypes() {
            for (Mission.MissionType type : Mission.MissionType.values()) {
                Mission m = new Mission("Test", type, Mission.MissionDifficulty.NORMAL, "Test");
                assertEquals(type, m.getType());
            }
        }

        @Test
        @DisplayName("should support all difficulty levels")
        void constructor_shouldSupportAllDifficulties() {
            for (Mission.MissionDifficulty diff : Mission.MissionDifficulty.values()) {
                Mission m = new Mission("Test", Mission.MissionType.CUSTOM, diff, "Test");
                assertEquals(diff, m.getDifficulty());
            }
        }
    }

    // ---- Lifecycle Transitions ----

    @Nested
    @DisplayName("Lifecycle Transition Tests")
    class LifecycleTests {

        @Test
        @DisplayName("should transition from AVAILABLE to ACCEPTED")
        void accept_shouldTransitionToAccepted() {
            mission.accept();
            assertEquals(Mission.MissionStatus.ACCEPTED, mission.getStatus());
            assertTrue(mission.getAcceptedAt() > 0);
        }

        @Test
        @DisplayName("should not accept if not AVAILABLE")
        void accept_shouldNotAcceptIfNotAvailable() {
            mission.setStatus(Mission.MissionStatus.IN_PROGRESS);
            mission.accept();
            assertEquals(Mission.MissionStatus.IN_PROGRESS, mission.getStatus());
        }

        @Test
        @DisplayName("should transition from ACCEPTED to IN_PROGRESS")
        void start_shouldTransitionToInProgress() {
            mission.accept();
            mission.start();
            assertEquals(Mission.MissionStatus.IN_PROGRESS, mission.getStatus());
        }

        @Test
        @DisplayName("should not start if not ACCEPTED")
        void start_shouldNotStartIfNotAccepted() {
            mission.start();
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should transition from IN_PROGRESS to COMPLETED")
        void complete_shouldTransitionToCompleted() {
            mission.accept();
            mission.start();
            mission.complete();
            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
            assertTrue(mission.getCompletedAt() > 0);
        }

        @Test
        @DisplayName("should not complete if not IN_PROGRESS")
        void complete_shouldNotCompleteIfNotInProgress() {
            mission.complete();
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should transition from IN_PROGRESS to FAILED")
        void fail_shouldTransitionToFailed() {
            mission.accept();
            mission.start();
            mission.fail();
            assertEquals(Mission.MissionStatus.FAILED, mission.getStatus());
            assertTrue(mission.getCompletedAt() > 0);
        }

        @Test
        @DisplayName("should not fail if not IN_PROGRESS")
        void fail_shouldNotFailIfNotInProgress() {
            mission.fail();
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should abandon from ACCEPTED state")
        void abandon_shouldAbandonFromAccepted() {
            mission.accept();
            mission.abandon();
            assertEquals(Mission.MissionStatus.ABANDONED, mission.getStatus());
        }

        @Test
        @DisplayName("should abandon from IN_PROGRESS state")
        void abandon_shouldAbandonFromInProgress() {
            mission.accept();
            mission.start();
            mission.abandon();
            assertEquals(Mission.MissionStatus.ABANDONED, mission.getStatus());
        }

        @Test
        @DisplayName("should not abandon from AVAILABLE state")
        void abandon_shouldNotAbandonFromAvailable() {
            mission.abandon();
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
        }

        @Test
        @DisplayName("should not abandon from COMPLETED state")
        void abandon_shouldNotAbandonFromCompleted() {
            mission.accept();
            mission.start();
            mission.complete();
            mission.abandon();
            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
        }

        @Test
        @DisplayName("should allow full lifecycle: AVAILABLE -> ACCEPTED -> IN_PROGRESS -> COMPLETED")
        void fullLifecycle_shouldCompleteSuccessfully() {
            assertEquals(Mission.MissionStatus.AVAILABLE, mission.getStatus());
            mission.accept();
            assertEquals(Mission.MissionStatus.ACCEPTED, mission.getStatus());
            mission.start();
            assertEquals(Mission.MissionStatus.IN_PROGRESS, mission.getStatus());
            mission.complete();
            assertEquals(Mission.MissionStatus.COMPLETED, mission.getStatus());
        }
    }

    // ---- Objectives ----

    @Nested
    @DisplayName("Objective Tests")
    class ObjectiveTests {

        @Test
        @DisplayName("should add objectives")
        void addObjective_shouldAddObjective() {
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            assertEquals(1, mission.getObjectives().size());
            assertEquals(obj, mission.getObjectives().get(0));
        }

        @Test
        @DisplayName("should return false when no objectives")
        void areAllObjectivesCompleted_shouldReturnFalseWhenEmpty() {
            assertFalse(mission.areAllObjectivesCompleted());
        }

        @Test
        @DisplayName("should return true when all objectives completed")
        void areAllObjectivesCompleted_shouldReturnTrueWhenAllDone() {
            MissionObjective obj1 = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            MissionObjective obj2 = new MissionObjective("Capture position",
                    MissionObjective.ObjectiveType.CAPTURE_POSITION);
            mission.addObjective(obj1);
            mission.addObjective(obj2);

            obj1.forceComplete();
            obj2.forceComplete();

            assertTrue(mission.areAllObjectivesCompleted());
        }

        @Test
        @DisplayName("should return false when any objective incomplete")
        void areAllObjectivesCompleted_shouldReturnFalseWhenOneIncomplete() {
            MissionObjective obj1 = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            MissionObjective obj2 = new MissionObjective("Capture position",
                    MissionObjective.ObjectiveType.CAPTURE_POSITION);
            mission.addObjective(obj1);
            mission.addObjective(obj2);

            obj1.forceComplete();
            // obj2 not completed

            assertFalse(mission.areAllObjectivesCompleted());
        }

        @Test
        @DisplayName("should count completed objectives")
        void getCompletedObjectiveCount_shouldCountCompleted() {
            MissionObjective obj1 = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            MissionObjective obj2 = new MissionObjective("Capture position",
                    MissionObjective.ObjectiveType.CAPTURE_POSITION);
            MissionObjective obj3 = new MissionObjective("Survive",
                    MissionObjective.ObjectiveType.SURVIVE);
            mission.addObjective(obj1);
            mission.addObjective(obj2);
            mission.addObjective(obj3);

            obj1.forceComplete();
            obj3.forceComplete();

            assertEquals(2, mission.getCompletedObjectiveCount());
        }

        @Test
        @DisplayName("should return zero completed when none done")
        void getCompletedObjectiveCount_shouldReturnZero() {
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);

            assertEquals(0, mission.getCompletedObjectiveCount());
        }
    }

    // ---- Rewards ----

    @Nested
    @DisplayName("Reward Tests")
    class RewardTests {

        @Test
        @DisplayName("should add rewards")
        void addReward_shouldAddReward() {
            MissionReward reward = new MissionReward(MissionReward.RewardType.CREDITS, 10000);
            mission.addReward(reward);
            assertEquals(1, mission.getRewards().size());
        }

        @Test
        @DisplayName("should calculate credits with NORMAL difficulty")
        void calculateFinalCredits_shouldCalculateNormal() {
            mission.setCreditsReward(10000);
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            obj.forceComplete();

            // (10000 + 1000) * 1.0 = 11000
            assertEquals(11000, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate credits with EASY difficulty")
        void calculateFinalCredits_shouldCalculateEasy() {
            mission.setDifficulty(Mission.MissionDifficulty.EASY);
            mission.setCreditsReward(10000);
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            obj.forceComplete();

            // (10000 + 1000) * 0.5 = 5500
            assertEquals(5500, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate credits with HARD difficulty")
        void calculateFinalCredits_shouldCalculateHard() {
            mission.setDifficulty(Mission.MissionDifficulty.HARD);
            mission.setCreditsReward(10000);
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            obj.forceComplete();

            // (10000 + 1000) * 1.5 = 16500
            assertEquals(16500, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate credits with EXTREME difficulty")
        void calculateFinalCredits_shouldCalculateExtreme() {
            mission.setDifficulty(Mission.MissionDifficulty.EXTREME);
            mission.setCreditsReward(10000);
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            obj.forceComplete();

            // (10000 + 1000) * 2.0 = 22000
            assertEquals(22000, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate credits with LEGENDARY difficulty")
        void calculateFinalCredits_shouldCalculateLegendary() {
            mission.setDifficulty(Mission.MissionDifficulty.LEGENDARY);
            mission.setCreditsReward(10000);
            MissionObjective obj = new MissionObjective("Destroy enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            mission.addObjective(obj);
            obj.forceComplete();

            // (10000 + 1000) * 3.0 = 33000
            assertEquals(33000, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate credits with no objectives completed")
        void calculateFinalCredits_shouldCalculateNoObjectives() {
            mission.setCreditsReward(10000);
            // No objectives added

            // (10000 + 0) * 1.0 = 10000
            assertEquals(10000, mission.calculateFinalCredits());
        }

        @Test
        @DisplayName("should calculate experience with difficulty multiplier")
        void calculateFinalExperience_shouldCalculateWithMultiplier() {
            mission.setExperienceReward(100);

            // 100 * 1.0 = 100
            assertEquals(100, mission.calculateFinalExperience());

            mission.setDifficulty(Mission.MissionDifficulty.HARD);
            // 100 * 1.5 = 150
            assertEquals(150, mission.calculateFinalExperience());

            mission.setDifficulty(Mission.MissionDifficulty.EXTREME);
            // 100 * 2.0 = 200
            assertEquals(200, mission.calculateFinalExperience());
        }
    }

    // ---- Difficulty Multipliers ----

    @Nested
    @DisplayName("Difficulty Multiplier Tests")
    class DifficultyMultiplierTests {

        @Test
        @DisplayName("EASY should have 0.5 multiplier")
        void easy_shouldHaveHalfMultiplier() {
            assertEquals(0.5, Mission.MissionDifficulty.EASY.getRewardMultiplier());
        }

        @Test
        @DisplayName("NORMAL should have 1.0 multiplier")
        void normal_shouldHaveOneMultiplier() {
            assertEquals(1.0, Mission.MissionDifficulty.NORMAL.getRewardMultiplier());
        }

        @Test
        @DisplayName("HARD should have 1.5 multiplier")
        void hard_shouldHaveOnePointFiveMultiplier() {
            assertEquals(1.5, Mission.MissionDifficulty.HARD.getRewardMultiplier());
        }

        @Test
        @DisplayName("EXTREME should have 2.0 multiplier")
        void extreme_shouldHaveTwoMultiplier() {
            assertEquals(2.0, Mission.MissionDifficulty.EXTREME.getRewardMultiplier());
        }

        @Test
        @DisplayName("LEGENDARY should have 3.0 multiplier")
        void legendary_shouldHaveThreeMultiplier() {
            assertEquals(3.0, Mission.MissionDifficulty.LEGENDARY.getRewardMultiplier());
        }
    }

    // ---- Campaign Chain ----

    @Nested
    @DisplayName("Campaign Chain Tests")
    class CampaignChainTests {

        @Test
        @DisplayName("should set prerequisite and next mission")
        void campaignChain_shouldSetPrerequisites() {
            mission.setCampaign(true);
            mission.setPrerequisiteMissionId("prev-mission-id");
            mission.setNextMissionId("next-mission-id");

            assertTrue(mission.isCampaign());
            assertEquals("prev-mission-id", mission.getPrerequisiteMissionId());
            assertEquals("next-mission-id", mission.getNextMissionId());
        }

        @Test
        @DisplayName("should set min reputation requirement")
        void campaignChain_shouldSetMinReputation() {
            mission.setMinReputation(50);
            assertEquals(50, mission.getMinReputation());
        }

        @Test
        @DisplayName("should set min pilot level requirement")
        void campaignChain_shouldSetMinPilotLevel() {
            mission.setMinPilotLevel(3);
            assertEquals(3, mission.getMinPilotLevel());
        }
    }

    // ---- Co-op Settings ----

    @Nested
    @DisplayName("Co-op Settings Tests")
    class CoopSettingsTests {

        @Test
        @DisplayName("should enable co-op mode")
        void coop_shouldEnableCoopMode() {
            mission.setCoop(true);
            mission.setMaxCoopPlayers(4);

            assertTrue(mission.isCoop());
            assertEquals(4, mission.getMaxCoopPlayers());
        }

        @Test
        @DisplayName("should set repeatable flag")
        void repeatable_shouldSetRepeatableFlag() {
            mission.setRepeatable(true);
            assertTrue(mission.isRepeatable());
        }
    }

    // ---- Planet and Map Settings ----

    @Nested
    @DisplayName("Planet and Map Settings Tests")
    class PlanetMapTests {

        @Test
        @DisplayName("should set planet and system names")
        void planetMap_shouldSetPlanetAndSystem() {
            mission.setPlanetName("Tharkad");
            mission.setSystemName("Tharkad");
            mission.setMapName("Urban Ruins");

            assertEquals("Tharkad", mission.getPlanetName());
            assertEquals("Tharkad", mission.getSystemName());
            assertEquals("Urban Ruins", mission.getMapName());
        }

        @Test
        @DisplayName("should set enemy faction")
        void planetMap_shouldSetEnemyFaction() {
            mission.setEnemyFaction("Draconis Combine");
            assertEquals("Draconis Combine", mission.getEnemyFaction());
        }
    }

    // ---- Briefing and Debriefing ----

    @Nested
    @DisplayName("Briefing and Debriefing Tests")
    class BriefingTests {

        @Test
        @DisplayName("should set briefing text")
        void briefing_shouldSetBriefingText() {
            String briefing = "Enemy forces have been spotted advancing on the colony.";
            mission.setBriefing(briefing);
            assertEquals(briefing, mission.getBriefing());
        }

        @Test
        @DisplayName("should set debriefing text")
        void debriefing_shouldSetDebriefingText() {
            String debriefing = "Mission completed successfully. Well done.";
            mission.setDebriefing(debriefing);
            assertEquals(debriefing, mission.getDebriefing());
        }
    }

    // ---- Time and Wave Settings ----

    @Nested
    @DisplayName("Time and Wave Settings Tests")
    class TimeWaveTests {

        @Test
        @DisplayName("should set time limit")
        void timeLimit_shouldSetTimeLimit() {
            mission.setTimeLimit(300);
            assertEquals(300, mission.getTimeLimit());
        }

        @Test
        @DisplayName("should set wave count")
        void waveCount_shouldSetWaveCount() {
            mission.setWaveCount(5);
            assertEquals(5, mission.getWaveCount());
        }

        @Test
        @DisplayName("should set enemy count")
        void enemyCount_shouldSetEnemyCount() {
            mission.setEnemyCount(10);
            assertEquals(10, mission.getEnemyCount());
        }

        @Test
        @DisplayName("should set player lance size")
        void playerLanceSize_shouldSetPlayerLanceSize() {
            mission.setPlayerLanceSize(2);
            assertEquals(2, mission.getPlayerLanceSize());
        }
    }
}
