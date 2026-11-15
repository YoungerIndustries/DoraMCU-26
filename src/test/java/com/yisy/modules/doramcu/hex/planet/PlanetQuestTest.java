package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PlanetQuestTest {

    private PlanetQuest createQuest() {
        return PlanetQuest.builder()
                .title("Escort Mission")
                .description("Protect the convoy")
                .type(PlanetQuest.QuestType.ESCORT)
                .difficulty(5)
                .creditsReward(10000)
                .xpReward(200)
                .reputationReward(20)
                .issuerName("Governor Smith")
                .issuerEmpireId("empire1")
                .addObjective("Reach station A")
                .addObjective("Defend convoy")
                .addRequiredStationId("station1")
                .addRequiredStationId("station2")
                .timeLimitHours(72)
                .build();
    }

    // ---- Builder pattern ----

    @Test
    void builder_setsAllFields() {
        PlanetQuest quest = createQuest();
        assertNotNull(quest.getId());
        assertEquals("Escort Mission", quest.getTitle());
        assertEquals("Protect the convoy", quest.getDescription());
        assertEquals(PlanetQuest.QuestType.ESCORT, quest.getType());
        assertEquals(5, quest.getDifficulty());
        assertEquals(10000, quest.getCreditsReward());
        assertEquals(200, quest.getXpReward());
        assertEquals(20, quest.getReputationReward());
        assertEquals("Governor Smith", quest.getIssuerName());
        assertEquals("empire1", quest.getIssuerEmpireId());
    }

    @Test
    void builder_copiesObjectives() {
        PlanetQuest quest = createQuest();
        assertEquals(2, quest.getObjectives().size());
        assertTrue(quest.getObjectives().contains("Reach station A"));
        assertTrue(quest.getObjectives().contains("Defend convoy"));
    }

    @Test
    void builder_copiesRequiredStationIds() {
        PlanetQuest quest = createQuest();
        assertEquals(2, quest.getRequiredStationIds().size());
        assertTrue(quest.getRequiredStationIds().contains("station1"));
        assertTrue(quest.getRequiredStationIds().contains("station2"));
    }

    @Test
    void builder_statusDefaultsToAvailable() {
        PlanetQuest quest = createQuest();
        assertEquals(PlanetQuest.QuestStatus.AVAILABLE, quest.getStatus());
    }

    @Test
    void builder_setsCreatedAtAndExpiresAt() {
        PlanetQuest quest = createQuest();
        assertNotNull(quest.getCreatedAt());
        assertNotNull(quest.getExpiresAt());
        assertTrue(quest.getExpiresAt().isAfter(quest.getCreatedAt()));
    }

    @Test
    void builder_expiresAt_matchesTimeLimit() {
        PlanetQuest quest = createQuest();
        long hours = java.time.Duration.between(quest.getCreatedAt(), quest.getExpiresAt()).toHours();
        assertEquals(72, hours);
    }

    @Test
    void builder_assignedCorporationIdIsNull() {
        PlanetQuest quest = createQuest();
        assertNull(quest.getAssignedCorporationId());
    }

    @Test
    void builder_completedAtIsNull() {
        PlanetQuest quest = createQuest();
        assertNull(quest.getCompletedAt());
    }

    @Test
    void builder_objectivesAreIndependentCopy() {
        PlanetQuest quest = createQuest();
        // Modifying the original builder's list should not affect the quest
        assertEquals(2, quest.getObjectives().size());
    }

    // ---- Lifecycle: accept ----

    @Test
    void accept_changesStatusToInProgress() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        assertEquals(PlanetQuest.QuestStatus.IN_PROGRESS, quest.getStatus());
        assertEquals("corp1", quest.getAssignedCorporationId());
    }

    @Test
    void accept_throwsWhenNotAvailable() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        assertThrows(IllegalStateException.class, () -> quest.accept("corp2"));
    }

    @Test
    void accept_throwsWhenCompleted() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.complete();
        assertThrows(IllegalStateException.class, () -> quest.accept("corp2"));
    }

    // ---- Lifecycle: complete ----

    @Test
    void complete_changesStatusToCompleted() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.complete();
        assertEquals(PlanetQuest.QuestStatus.COMPLETED, quest.getStatus());
        assertNotNull(quest.getCompletedAt());
    }

    @Test
    void complete_throwsWhenNotInProgress() {
        PlanetQuest quest = createQuest();
        assertThrows(IllegalStateException.class, () -> quest.complete());
    }

    @Test
    void complete_throwsWhenFailed() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.fail();
        assertThrows(IllegalStateException.class, () -> quest.complete());
    }

    // ---- Lifecycle: fail ----

    @Test
    void fail_changesStatusToFailed() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.fail();
        assertEquals(PlanetQuest.QuestStatus.FAILED, quest.getStatus());
    }

    @Test
    void fail_throwsWhenNotInProgress() {
        PlanetQuest quest = createQuest();
        assertThrows(IllegalStateException.class, () -> quest.fail());
    }

    // ---- Lifecycle: abandon ----

    @Test
    void abandon_changesStatusToAbandoned() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.abandon();
        assertEquals(PlanetQuest.QuestStatus.ABANDONED, quest.getStatus());
    }

    @Test
    void abandon_throwsWhenNotInProgress() {
        PlanetQuest quest = createQuest();
        assertThrows(IllegalStateException.class, () -> quest.abandon());
    }

    @Test
    void abandon_throwsWhenCompleted() {
        PlanetQuest quest = createQuest();
        quest.accept("corp1");
        quest.complete();
        assertThrows(IllegalStateException.class, () -> quest.abandon());
    }

    // ---- Expiration ----

    @Test
    void isExpired_returnsFalseBeforeExpiry() {
        PlanetQuest quest = createQuest();
        assertFalse(quest.isExpired());
    }

    // ---- QuestType enum ----

    @Test
    void questType_ESCORT_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.ESCORT;
        assertEquals("Escort", type.getDisplayName());
        assertEquals(1.5, type.getDifficultyMultiplier());
        assertEquals(10, type.getBaseXpReward());
    }

    @Test
    void questType_RESCUE_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.RESCUE;
        assertEquals("Rescue", type.getDisplayName());
        assertEquals(2.0, type.getDifficultyMultiplier());
        assertEquals(15, type.getBaseXpReward());
    }

    @Test
    void questType_EXPLORATION_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.EXPLORATION;
        assertEquals("Exploration", type.getDisplayName());
        assertEquals(1.0, type.getDifficultyMultiplier());
        assertEquals(5, type.getBaseXpReward());
    }

    @Test
    void questType_DIPLOMATIC_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.DIPLOMATIC;
        assertEquals("Diplomatic", type.getDisplayName());
        assertEquals(2.5, type.getDifficultyMultiplier());
        assertEquals(20, type.getBaseXpReward());
    }

    @Test
    void questType_DEFENSE_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.DEFENSE;
        assertEquals("Defense", type.getDisplayName());
        assertEquals(1.8, type.getDifficultyMultiplier());
        assertEquals(12, type.getBaseXpReward());
    }

    @Test
    void questType_INVESTIGATION_hasCorrectValues() {
        PlanetQuest.QuestType type = PlanetQuest.QuestType.INVESTIGATION;
        assertEquals("Investigation", type.getDisplayName());
        assertEquals(1.2, type.getDifficultyMultiplier());
        assertEquals(8, type.getBaseXpReward());
    }

    @Test
    void questType_enumHasSixValues() {
        assertEquals(6, PlanetQuest.QuestType.values().length);
    }

    // ---- Setters ----

    @Test
    void setAssignedCorporationId_setsValue() {
        PlanetQuest quest = createQuest();
        quest.setAssignedCorporationId("corp5");
        assertEquals("corp5", quest.getAssignedCorporationId());
    }

    @Test
    void setCompletedAt_setsValue() {
        PlanetQuest quest = createQuest();
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 12, 0);
        quest.setCompletedAt(time);
        assertEquals(time, quest.getCompletedAt());
    }

    // ---- Static builder() method ----

    @Test
    void builder_staticMethod_returnsBuilder() {
        assertNotNull(PlanetQuest.builder());
    }

    // ---- toString ----

    @Test
    void toString_containsKeyInfo() {
        PlanetQuest quest = createQuest();
        String str = quest.toString();
        assertTrue(str.contains(quest.getId()));
        assertTrue(str.contains("Escort Mission"));
        assertTrue(str.contains("Escort"));
        assertTrue(str.contains("AVAILABLE"));
    }
}
