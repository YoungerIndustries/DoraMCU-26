package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateContractAssignment Entity Tests")
class CorporateContractAssignmentTest {

    @Test
    @DisplayName("constructor should set default values correctly")
    void constructorDefaults() {
        CorporateContractAssignment assignment = new CorporateContractAssignment(1L, 10L, 100L);

        assertEquals(1L, assignment.getContractId());
        assertEquals(10L, assignment.getHiringId());
        assertEquals(100L, assignment.getAiCharacterId());
        assertNotNull(assignment.getAssignedAt());
        assertEquals(0, assignment.getXpEarned());
    }

    @Test
    @DisplayName("no-arg constructor should create empty assignment")
    void noArgConstructor() {
        CorporateContractAssignment assignment = new CorporateContractAssignment();

        assertNull(assignment.getId());
        assertNull(assignment.getContractId());
        assertNull(assignment.getHiringId());
        assertNull(assignment.getAiCharacterId());
        assertNull(assignment.getAssignedAt());
        assertEquals(0, assignment.getXpEarned());
    }

    @Test
    @DisplayName("setters should update all fields")
    void setters() {
        CorporateContractAssignment assignment = new CorporateContractAssignment();
        assignment.setId(1L);
        assignment.setContractId(2L);
        assignment.setHiringId(3L);
        assignment.setAiCharacterId(4L);
        LocalDateTime now = LocalDateTime.now();
        assignment.setAssignedAt(now);
        assignment.setXpEarned(100);

        assertEquals(1L, assignment.getId());
        assertEquals(2L, assignment.getContractId());
        assertEquals(3L, assignment.getHiringId());
        assertEquals(4L, assignment.getAiCharacterId());
        assertEquals(now, assignment.getAssignedAt());
        assertEquals(100, assignment.getXpEarned());
    }

    @Test
    @DisplayName("toString should include key fields")
    void toStringContainsFields() {
        CorporateContractAssignment assignment = new CorporateContractAssignment(1L, 10L, 100L);
        assignment.setId(1L);
        assignment.setXpEarned(50);

        String str = assignment.toString();
        assertTrue(str.contains("contractId=1"));
        assertTrue(str.contains("hiringId=10"));
        assertTrue(str.contains("aiCharacterId=100"));
        assertTrue(str.contains("xpEarned=50"));
    }
}
