package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MissionObjective Model Tests")
class MissionObjectiveTest {

    private MissionObjective objective;

    @BeforeEach
    void setUp() {
        objective = new MissionObjective("Destroy 5 enemy mechs",
                MissionObjective.ObjectiveType.DESTROY_ENEMIES);
        objective.setTargetCount(5);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertNotNull(objective.getId());
            assertEquals("Destroy 5 enemy mechs", objective.getDescription());
            assertEquals(MissionObjective.ObjectiveType.DESTROY_ENEMIES, objective.getType());
            assertFalse(objective.isCompleted());
            assertEquals(0, objective.getCurrentCount());
            assertFalse(objective.isOptional());
            assertEquals(0, objective.getBonusCredits());
            assertEquals(0, objective.getBonusExperience());
            assertEquals(0, objective.getDurationSeconds());
            assertEquals(0, objective.getElapsedSeconds());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyObjective() {
            MissionObjective empty = new MissionObjective();
            assertNull(empty.getId());
            assertNull(empty.getDescription());
        }

        @Test
        @DisplayName("should support all objective types")
        void constructor_shouldSupportAllObjectiveTypes() {
            for (MissionObjective.ObjectiveType type : MissionObjective.ObjectiveType.values()) {
                MissionObjective obj = new MissionObjective("Test", type);
                assertEquals(type, obj.getType());
            }
        }
    }

    // ---- Progress Tracking ----

    @Nested
    @DisplayName("Progress Tracking Tests")
    class ProgressTests {

        @Test
        @DisplayName("should progress toward target")
        void progress_shouldIncreaseCount() {
            boolean completed = objective.progress(1);
            assertEquals(1, objective.getCurrentCount());
            assertFalse(completed);
        }

        @Test
        @DisplayName("should complete when reaching target")
        void progress_shouldCompleteAtTarget() {
            objective.progress(3);
            boolean completed = objective.progress(2);
            assertTrue(completed);
            assertTrue(objective.isCompleted());
            assertEquals(5, objective.getCurrentCount());
        }

        @Test
        @DisplayName("should cap count at target")
        void progress_shouldCapAtTarget() {
            objective.progress(10);
            assertTrue(objective.isCompleted());
            assertEquals(5, objective.getCurrentCount());
        }

        @Test
        @DisplayName("should not progress if already completed")
        void progress_shouldNotProgressIfCompleted() {
            objective.forceComplete();
            boolean completed = objective.progress(1);
            assertTrue(completed);
            assertEquals(5, objective.getCurrentCount());
        }

        @Test
        @DisplayName("should progress by multiple amounts")
        void progress_shouldProgressByMultiple() {
            objective.progress(3);
            assertEquals(3, objective.getCurrentCount());
            assertFalse(objective.isCompleted());

            objective.progress(2);
            assertEquals(5, objective.getCurrentCount());
            assertTrue(objective.isCompleted());
        }

        @Test
        @DisplayName("should complete in single progress call")
        void progress_shouldCompleteInSingleCall() {
            boolean completed = objective.progress(5);
            assertTrue(completed);
            assertTrue(objective.isCompleted());
        }
    }

    // ---- Time-Based Objectives ----

    @Nested
    @DisplayName("Time-Based Objective Tests")
    class TimeBasedTests {

        @Test
        @DisplayName("should track elapsed time")
        void addElapsedSeconds_shouldTrackTime() {
            objective.setDurationSeconds(100);
            objective.addElapsedSeconds(30);
            assertEquals(30, objective.getElapsedSeconds());
            assertFalse(objective.isCompleted());
        }

        @Test
        @DisplayName("should complete when duration reached")
        void addElapsedSeconds_shouldCompleteAtDuration() {
            objective.setDurationSeconds(100);
            objective.addElapsedSeconds(60);
            assertFalse(objective.isCompleted());

            boolean completed = objective.addElapsedSeconds(40);
            assertTrue(completed);
            assertTrue(objective.isCompleted());
        }

        @Test
        @DisplayName("should not complete if no duration set")
        void addElapsedSeconds_shouldNotCompleteWithoutDuration() {
            objective.setDurationSeconds(0);
            objective.addElapsedSeconds(1000);
            assertFalse(objective.isCompleted());
        }

        @Test
        @DisplayName("should not add time if already completed")
        void addElapsedSeconds_shouldNotAddIfCompleted() {
            objective.setDurationSeconds(100);
            objective.forceComplete();
            objective.addElapsedSeconds(50);
            assertEquals(0, objective.getElapsedSeconds());
        }

        @Test
        @DisplayName("should calculate time remaining")
        void getTimeRemaining_shouldCalculateRemaining() {
            objective.setDurationSeconds(100);
            objective.addElapsedSeconds(30);
            assertEquals(70, objective.getTimeRemaining());
        }

        @Test
        @DisplayName("should return zero time remaining when no duration")
        void getTimeRemaining_shouldReturnZeroWithoutDuration() {
            objective.setDurationSeconds(0);
            assertEquals(0, objective.getTimeRemaining());
        }

        @Test
        @DisplayName("should return zero time remaining when completed")
        void getTimeRemaining_shouldReturnZeroWhenCompleted() {
            objective.setDurationSeconds(100);
            objective.addElapsedSeconds(100);
            assertEquals(0, objective.getTimeRemaining());
        }

        @Test
        @DisplayName("should not return negative time remaining")
        void getTimeRemaining_shouldNotReturnNegative() {
            objective.setDurationSeconds(100);
            objective.setElapsedSeconds(150);
            assertEquals(0, objective.getTimeRemaining());
        }
    }

    // ---- Completion Percentage ----

    @Nested
    @DisplayName("Completion Percentage Tests")
    class CompletionPercentageTests {

        @Test
        @DisplayName("should return 0% when not started")
        void getCompletionPercentage_shouldReturnZero() {
            assertEquals(0.0, objective.getCompletionPercentage());
        }

        @Test
        @DisplayName("should return 50% at half progress")
        void getCompletionPercentage_shouldReturnFifty() {
            objective.progress(2); // 2/5 = 40%
            assertEquals(40.0, objective.getCompletionPercentage());
        }

        @Test
        @DisplayName("should return 100% when completed")
        void getCompletionPercentage_shouldReturnHundred() {
            objective.forceComplete();
            assertEquals(100.0, objective.getCompletionPercentage());
        }

        @Test
        @DisplayName("should return 100% when force completed with zero target")
        void getCompletionPercentage_shouldReturnHundredWhenForceCompleted() {
            objective.setTargetCount(0);
            objective.forceComplete();
            assertEquals(100.0, objective.getCompletionPercentage());
        }

        @Test
        @DisplayName("should return 0% when not completed with zero target")
        void getCompletionPercentage_shouldReturnZeroWithZeroTarget() {
            objective.setTargetCount(0);
            assertEquals(0.0, objective.getCompletionPercentage());
        }
    }

    // ---- Force Complete ----

    @Nested
    @DisplayName("Force Complete Tests")
    class ForceCompleteTests {

        @Test
        @DisplayName("should mark as completed")
        void forceComplete_shouldMarkCompleted() {
            objective.forceComplete();
            assertTrue(objective.isCompleted());
        }

        @Test
        @DisplayName("should set current count to target")
        void forceComplete_shouldSetCountToTarget() {
            objective.forceComplete();
            assertEquals(5, objective.getCurrentCount());
        }
    }

    // ---- Optional Objectives ----

    @Nested
    @DisplayName("Optional Objective Tests")
    class OptionalTests {

        @Test
        @DisplayName("should set optional flag")
        void setOptional_shouldSetFlag() {
            objective.setOptional(true);
            assertTrue(objective.isOptional());
        }

        @Test
        @DisplayName("should set bonus credits")
        void setBonusCredits_shouldSetBonus() {
            objective.setBonusCredits(5000);
            assertEquals(5000, objective.getBonusCredits());
        }

        @Test
        @DisplayName("should set bonus experience")
        void setBonusExperience_shouldSetBonus() {
            objective.setBonusExperience(100);
            assertEquals(100, objective.getBonusExperience());
        }
    }

    // ---- Target and Location ----

    @Nested
    @DisplayName("Target and Location Tests")
    class TargetLocationTests {

        @Test
        @DisplayName("should set target name")
        void setTargetName_shouldSetTargetName() {
            objective.setTargetName("Enemy Commander");
            assertEquals("Enemy Commander", objective.getTargetName());
        }

        @Test
        @DisplayName("should set location name")
        void setLocationName_shouldSetLocationName() {
            objective.setLocationName("Northern Ridge");
            assertEquals("Northern Ridge", objective.getLocationName());
        }

        @Test
        @DisplayName("should set duration seconds")
        void setDurationSeconds_shouldSetDuration() {
            objective.setDurationSeconds(300);
            assertEquals(300, objective.getDurationSeconds());
        }
    }

    // ---- Objective Type Specific Tests ----

    @Nested
    @DisplayName("Objective Type Specific Tests")
    class ObjectiveTypeTests {

        @Test
        @DisplayName("should create survive objective with duration")
        void surviveObjective_shouldHaveDuration() {
            MissionObjective survive = new MissionObjective("Survive 5 minutes",
                    MissionObjective.ObjectiveType.SURVIVE);
            survive.setDurationSeconds(300);

            survive.addElapsedSeconds(150);
            assertFalse(survive.isCompleted());

            survive.addElapsedSeconds(150);
            assertTrue(survive.isCompleted());
        }

        @Test
        @DisplayName("should create destroy enemies objective")
        void destroyEnemiesObjective_shouldTrackCount() {
            MissionObjective destroy = new MissionObjective("Destroy 6 enemies",
                    MissionObjective.ObjectiveType.DESTROY_ENEMIES);
            destroy.setTargetCount(6);

            destroy.progress(1);
            destroy.progress(1);
            destroy.progress(1);
            assertEquals(3, destroy.getCurrentCount());
            assertFalse(destroy.isCompleted());

            destroy.progress(3);
            assertTrue(destroy.isCompleted());
        }

        @Test
        @DisplayName("should create hold position objective")
        void holdPositionObjective_shouldTrackDuration() {
            MissionObjective hold = new MissionObjective("Hold position for 2 minutes",
                    MissionObjective.ObjectiveType.HOLD_POSITION);
            hold.setDurationSeconds(120);

            hold.addElapsedSeconds(60);
            assertFalse(hold.isCompleted());

            hold.addElapsedSeconds(60);
            assertTrue(hold.isCompleted());
        }

        @Test
        @DisplayName("should create recon objective")
        void reconObjective_shouldTrackLocations() {
            MissionObjective recon = new MissionObjective("Locate 3 positions",
                    MissionObjective.ObjectiveType.RECON);
            recon.setTargetCount(3);

            recon.progress(1);
            assertEquals(1, recon.getCurrentCount());
            recon.progress(1);
            assertEquals(2, recon.getCurrentCount());
            recon.progress(1);
            assertTrue(recon.isCompleted());
        }

        @Test
        @DisplayName("should create kill leader objective")
        void killLeaderObjective_shouldHaveTargetName() {
            MissionObjective kill = new MissionObjective("Eliminate enemy commander",
                    MissionObjective.ObjectiveType.KILL_LEADER);
            kill.setTargetName("General Kerensky");
            kill.setTargetCount(1);

            assertEquals("General Kerensky", kill.getTargetName());
            kill.progress(1);
            assertTrue(kill.isCompleted());
        }
    }
}
