package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TravellerContractAssignmentTest {

    @Test
    void constructor_defaults() {
        TravellerContractAssignment assignment = new TravellerContractAssignment();
        assertNull(assignment.getId());
        assertNull(assignment.getContractId());
        assertNull(assignment.getTravellerCharacterId());
        assertNull(assignment.getAssignedAt());
        assertEquals(0, assignment.getXpEarned());
    }

    @Test
    void constructor_with_params() {
        TravellerContractAssignment assignment = new TravellerContractAssignment(100L, 5);
        assertEquals(100L, assignment.getContractId());
        assertEquals(5, assignment.getTravellerCharacterId());
        assertEquals(0, assignment.getXpEarned());
        assertNotNull(assignment.getAssignedAt());
    }

    @Test
    void setters() {
        TravellerContractAssignment assignment = new TravellerContractAssignment();
        assignment.setId(1L);
        assignment.setContractId(42L);
        assignment.setTravellerCharacterId(7);
        assignment.setXpEarned(100);
        LocalDateTime now = LocalDateTime.now();
        assignment.setAssignedAt(now);

        assertEquals(1L, assignment.getId());
        assertEquals(42L, assignment.getContractId());
        assertEquals(7, assignment.getTravellerCharacterId());
        assertEquals(100, assignment.getXpEarned());
        assertEquals(now, assignment.getAssignedAt());
    }

    @Test
    void toString_contains_fields() {
        TravellerContractAssignment assignment = new TravellerContractAssignment(10L, 3);
        assignment.setId(1L);
        assignment.setXpEarned(50);

        String str = assignment.toString();
        assertTrue(str.contains("TravellerContractAssignment"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("contractId=10"));
        assertTrue(str.contains("travellerCharacterId=3"));
        assertTrue(str.contains("xpEarned=50"));
    }
}
