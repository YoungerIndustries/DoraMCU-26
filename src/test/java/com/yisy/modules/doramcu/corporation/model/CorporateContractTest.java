package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateContract Entity Tests")
class CorporateContractTest {

    @Test
    @DisplayName("constructor should set default values correctly")
    void constructorDefaults() {
        CorporateContract contract = new CorporateContract("Test Contract", CorporateContract.ContractType.TRADE, 5000, 1L);

        assertEquals("Test Contract", contract.getTitle());
        assertEquals(CorporateContract.ContractType.TRADE, contract.getType());
        assertEquals(5000, contract.getPaymentCredits());
        assertEquals(1L, contract.getCorporationId());
        assertEquals(50, contract.getXpReward());
        assertEquals(3, contract.getDifficulty());
        assertEquals(1, contract.getDurationTurns());
        assertEquals(CorporateContract.Status.AVAILABLE, contract.getStatus());
        assertNotNull(contract.getCreatedAt());
    }

    @Test
    @DisplayName("all contract types should be present")
    void contractTypes() {
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();
        assertEquals(4, types.length);
        assertContains(types, CorporateContract.ContractType.TRADE);
        assertContains(types, CorporateContract.ContractType.SECURITY);
        assertContains(types, CorporateContract.ContractType.LOGISTICS);
        assertContains(types, CorporateContract.ContractType.MISSION);
    }

    @Test
    @DisplayName("all statuses should be present")
    void statuses() {
        CorporateContract.Status[] statuses = CorporateContract.Status.values();
        assertEquals(5, statuses.length);
        assertContains(statuses, CorporateContract.Status.AVAILABLE);
        assertContains(statuses, CorporateContract.Status.ACTIVE);
        assertContains(statuses, CorporateContract.Status.COMPLETED);
        assertContains(statuses, CorporateContract.Status.FAILED);
        assertContains(statuses, CorporateContract.Status.EXPIRED);
    }

    @Test
    @DisplayName("status transitions should work correctly")
    void statusTransitions() {
        CorporateContract contract = new CorporateContract();
        contract.setId(1L);

        // AVAILABLE -> ACTIVE
        contract.setStatus(CorporateContract.Status.AVAILABLE);
        assertEquals(CorporateContract.Status.AVAILABLE, contract.getStatus());

        contract.setStatus(CorporateContract.Status.ACTIVE);
        assertEquals(CorporateContract.Status.ACTIVE, contract.getStatus());

        // ACTIVE -> COMPLETED
        contract.setStatus(CorporateContract.Status.COMPLETED);
        assertEquals(CorporateContract.Status.COMPLETED, contract.getStatus());
    }

    @Test
    @DisplayName("isExpired should return true when expiry date is in the past")
    void isExpiredPast() {
        CorporateContract contract = new CorporateContract();
        contract.setExpiryDate(LocalDateTime.now().minusDays(1));
        assertTrue(contract.isExpired());
    }

    @Test
    @DisplayName("isExpired should return false when expiry date is in the future")
    void isExpiredFuture() {
        CorporateContract contract = new CorporateContract();
        contract.setExpiryDate(LocalDateTime.now().plusDays(1));
        assertFalse(contract.isExpired());
    }

    @Test
    @DisplayName("isExpired should return false when expiry date is null")
    void isExpiredNull() {
        CorporateContract contract = new CorporateContract();
        contract.setExpiryDate(null);
        assertFalse(contract.isExpired());
    }

    @Test
    @DisplayName("setters should update all fields")
    void setters() {
        CorporateContract contract = new CorporateContract();
        contract.setId(10L);
        contract.setTitle("Updated Title");
        contract.setType(CorporateContract.ContractType.SECURITY);
        contract.setDescription("Test description");
        contract.setPaymentCredits(10000);
        contract.setXpReward(200);
        contract.setDifficulty(8);
        contract.setDurationTurns(3);
        contract.setStatus(CorporateContract.Status.ACTIVE);
        contract.setCorporationId(5L);
        contract.setAssigneeId(42L);
        LocalDateTime now = LocalDateTime.now();
        contract.setCreatedAt(now);
        contract.setCompletedAt(now);
        contract.setExpiryDate(now.plusDays(7));

        assertEquals(10L, contract.getId());
        assertEquals("Updated Title", contract.getTitle());
        assertEquals(CorporateContract.ContractType.SECURITY, contract.getType());
        assertEquals("Test description", contract.getDescription());
        assertEquals(10000, contract.getPaymentCredits());
        assertEquals(200, contract.getXpReward());
        assertEquals(8, contract.getDifficulty());
        assertEquals(3, contract.getDurationTurns());
        assertEquals(CorporateContract.Status.ACTIVE, contract.getStatus());
        assertEquals(5L, contract.getCorporationId());
        assertEquals(42L, contract.getAssigneeId());
        assertEquals(now, contract.getCreatedAt());
        assertEquals(now, contract.getCompletedAt());
        assertEquals(now.plusDays(7), contract.getExpiryDate());
    }

    private <T> void assertContains(T[] array, T expected) {
        for (T item : array) {
            if (item == expected) return;
        }
        fail("Array does not contain: " + expected);
    }

    // ---- Phase 2: Turn-based progression tests ----

    @Test
    @DisplayName("processTurn should decrement turnsRemaining and update progress")
    void processTurn_decrementsTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setId(1L);
        contract.setDurationTurns(5);
        contract.setTurnsRemaining(5);
        contract.setProgressPercent(0.0);
        contract.setStatus(CorporateContract.Status.ACTIVE);

        boolean complete = contract.processTurn();

        assertFalse(complete);
        assertEquals(4, contract.getTurnsRemaining());
        assertEquals(20.0, contract.getProgressPercent(), 0.1);
    }

    @Test
    @DisplayName("processTurn should return true when contract completes")
    void processTurn_completes() {
        CorporateContract contract = new CorporateContract();
        contract.setId(1L);
        contract.setDurationTurns(3);
        contract.setTurnsRemaining(1);
        contract.setProgressPercent(66.6);
        contract.setStatus(CorporateContract.Status.ACTIVE);

        boolean complete = contract.processTurn();

        assertTrue(complete);
        assertEquals(0, contract.getTurnsRemaining());
        assertEquals(100.0, contract.getProgressPercent(), 0.1);
    }

    @Test
    @DisplayName("processTurn should be no-op for non-active contracts")
    void processTurn_nonActive() {
        CorporateContract contract = new CorporateContract();
        contract.setDurationTurns(3);
        contract.setTurnsRemaining(3);
        contract.setStatus(CorporateContract.Status.AVAILABLE);

        boolean complete = contract.processTurn();

        assertFalse(complete);
        assertEquals(3, contract.getTurnsRemaining());
    }

    @Test
    @DisplayName("processTurn should be no-op when turnsRemaining is null")
    void processTurn_nullTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setDurationTurns(3);
        contract.setTurnsRemaining(null);
        contract.setStatus(CorporateContract.Status.ACTIVE);

        boolean complete = contract.processTurn();

        assertFalse(complete);
    }

    @Test
    @DisplayName("isComplete should return true when turnsRemaining is 0")
    void isComplete_zeroTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setTurnsRemaining(0);

        assertTrue(contract.isComplete());
    }

    @Test
    @DisplayName("isComplete should return true when turnsRemaining is negative")
    void isComplete_negativeTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setTurnsRemaining(-1);

        assertTrue(contract.isComplete());
    }

    @Test
    @DisplayName("isComplete should return false when turnsRemaining is positive")
    void isComplete_positiveTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setTurnsRemaining(3);

        assertFalse(contract.isComplete());
    }

    @Test
    @DisplayName("isComplete should return false when turnsRemaining is null")
    void isComplete_nullTurns() {
        CorporateContract contract = new CorporateContract();
        contract.setTurnsRemaining(null);

        assertFalse(contract.isComplete());
    }

    @Test
    @DisplayName("assignedCrewIds should default to empty list")
    void assignedCrewIds_default() {
        CorporateContract contract = new CorporateContract();

        assertNotNull(contract.getAssignedCrewIds());
        assertTrue(contract.getAssignedCrewIds().isEmpty());
    }

    @Test
    @DisplayName("assignedCrewIds should be settable")
    void assignedCrewIds_setter() {
        CorporateContract contract = new CorporateContract();
        List<Long> crew = Arrays.asList(100L, 101L);
        contract.setAssignedCrewIds(crew);

        assertEquals(2, contract.getAssignedCrewIds().size());
        assertEquals(100L, contract.getAssignedCrewIds().get(0));
    }

    @Test
    @DisplayName("progressPercent should default to 0.0")
    void progressPercent_default() {
        CorporateContract contract = new CorporateContract();

        assertEquals(0.0, contract.getProgressPercent());
    }

    @Test
    @DisplayName("progressPercent should cap at 100.0")
    void progressPercent_capped() {
        CorporateContract contract = new CorporateContract();
        contract.setDurationTurns(5);
        contract.setTurnsRemaining(5);
        contract.setStatus(CorporateContract.Status.ACTIVE);

        // Process all turns
        for (int i = 0; i < 10; i++) {
            contract.processTurn();
        }

        assertEquals(100.0, contract.getProgressPercent(), 0.1);
    }

    @Test
    @DisplayName("single-turn contract should complete in one processTurn call")
    void processTurn_singleTurnContract() {
        CorporateContract contract = new CorporateContract();
        contract.setDurationTurns(1);
        contract.setTurnsRemaining(1);
        contract.setStatus(CorporateContract.Status.ACTIVE);

        boolean complete = contract.processTurn();

        assertTrue(complete);
        assertEquals(0, contract.getTurnsRemaining());
    }

    @Test
    @DisplayName("setters for new fields should work")
    void newFieldSetters() {
        CorporateContract contract = new CorporateContract();
        contract.setTurnsRemaining(5);
        contract.setProgressPercent(50.0);
        contract.setAssignedCrewIds(Arrays.asList(1L, 2L));

        assertEquals(5, contract.getTurnsRemaining());
        assertEquals(50.0, contract.getProgressPercent());
        assertEquals(2, contract.getAssignedCrewIds().size());
    }
}
