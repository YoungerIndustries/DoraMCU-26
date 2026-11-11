package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpgQuestTest {

    private RpgQuest createQuest() {
        return new RpgQuest(
            "q1", "Deliver Cargo", "Deliver food to colony",
            RpgQuest.QuestType.DELIVERY, 1, 1000,
            500, 100, 10, 10, 0, CorporationRank.FREELANCER,
            CargoType.GRAIN, "planet1"
        );
    }

    @Test
    void constructor_setsFields() {
        RpgQuest quest = createQuest();
        assertEquals("q1", quest.getId());
        assertEquals("Deliver Cargo", quest.getTitle());
        assertEquals(RpgQuest.QuestType.DELIVERY, quest.getType());
        assertEquals(1, quest.getIssuingEmpireId());
        assertEquals(1000, quest.getTargetValue());
        assertEquals(0, quest.getCurrentValue());
        assertEquals(RpgQuest.QuestStatus.AVAILABLE, quest.getStatus());
        assertEquals(500, quest.getRewardCredits());
        assertEquals(100, quest.getRewardXp());
        assertEquals(10, quest.getRewardReputation());
        assertEquals(10, quest.getTurnsRemaining());
        assertEquals(CargoType.GRAIN, quest.getCargoType());
        assertEquals("planet1", quest.getDestinationPlanetId());
    }

    @Test
    void accept_changesStatusToActive() {
        RpgQuest quest = createQuest();
        assertTrue(quest.accept());
        assertEquals(RpgQuest.QuestStatus.ACTIVE, quest.getStatus());
    }

    @Test
    void accept_alreadyActive_returnsFalse() {
        RpgQuest quest = createQuest();
        quest.accept();
        assertFalse(quest.accept());
    }

    @Test
    void addProgress_increasesCurrentValue() {
        RpgQuest quest = createQuest();
        quest.accept();
        quest.addProgress(30);
        assertEquals(30, quest.getCurrentValue());
        quest.addProgress(20);
        assertEquals(50, quest.getCurrentValue());
    }

    @Test
    void addProgress_notActive_returnsFalse() {
        RpgQuest quest = createQuest();
        assertFalse(quest.addProgress(50));
        assertEquals(0, quest.getCurrentValue());
    }

    @Test
    void addProgress_clampsToTarget() {
        RpgQuest quest = createQuest();
        quest.accept();
        quest.addProgress(2000);
        assertEquals(1000, quest.getCurrentValue());
    }

    @Test
    void addProgress_completesQuest() {
        RpgQuest quest = createQuest();
        quest.accept();
        assertTrue(quest.addProgress(1000));
        assertEquals(RpgQuest.QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void processTurn_decreasesTurnsRemaining() {
        RpgQuest quest = createQuest();
        quest.accept();
        assertFalse(quest.processTurn());
        assertEquals(9, quest.getTurnsRemaining());
    }

    @Test
    void processTurn_expiresQuest() {
        RpgQuest q = new RpgQuest(
            "q1", "Test", "Test", RpgQuest.QuestType.DELIVERY, 1,
            100, 500, 100, 10, 1, 0, CorporationRank.FREELANCER,
            CargoType.GRAIN, "planet1"
        );
        q.accept();
        assertTrue(q.processTurn());
        assertEquals(RpgQuest.QuestStatus.EXPIRED, q.getStatus());
    }

    @Test
    void fail_setsFailedStatus() {
        RpgQuest quest = createQuest();
        quest.accept();
        quest.fail();
        assertEquals(RpgQuest.QuestStatus.FAILED, quest.getStatus());
    }

    @Test
    void getProgressPercentage_calculatesCorrectly() {
        RpgQuest quest = createQuest();
        quest.accept();
        quest.addProgress(500);
        assertEquals(0.5, quest.getProgressPercentage(), 0.001);
    }

    @Test
    void questTypes_enumValues() {
        RpgQuest.QuestType[] types = RpgQuest.QuestType.values();
        assertTrue(types.length >= 6);
    }

    @Test
    void isCompletableBy_checksPrerequisites() {
        RpgQuest q = new RpgQuest(
            "q1", "Test", "Test", RpgQuest.QuestType.DELIVERY, 1,
            100, 500, 100, 10, 10, 30, CorporationRank.STARTUP,
            CargoType.GRAIN, "planet1"
        );
        TransportCorporation corp = new TransportCorporation("Test", "p1", 10000);
        // Default rank is FREELANCER, min required is STARTUP
        assertFalse(q.isCompletableBy(corp, 50));
        corp.setRank(CorporationRank.STARTUP);
        assertTrue(q.isCompletableBy(corp, 50));
        assertFalse(q.isCompletableBy(corp, 20));
    }

    @Test
    void getCreatedAt_returnsTimestamp() {
        RpgQuest quest = createQuest();
        assertTrue(quest.getCreatedAt() > 0);
    }

    @Test
    void toString_containsKeyInfo() {
        RpgQuest quest = createQuest();
        String str = quest.toString();
        assertTrue(str.contains("q1"));
        assertTrue(str.contains("Deliver Cargo"));
    }
}
