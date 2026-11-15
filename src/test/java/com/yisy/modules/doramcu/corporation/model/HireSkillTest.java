package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HireSkillTest {

    @Test
    void testEnumValues() {
        HireSkill[] skills = HireSkill.values();
        assertEquals(5, skills.length);
    }

    @Test
    void testDisplayNames() {
        assertEquals("Combat", HireSkill.COMBAT.getDisplayName());
        assertEquals("Negotiation", HireSkill.NEGOTIATION.getDisplayName());
        assertEquals("Logistics", HireSkill.LOGISTICS.getDisplayName());
        assertEquals("Research", HireSkill.RESEARCH.getDisplayName());
        assertEquals("Governance", HireSkill.GOVERNANCE.getDisplayName());
    }

    @Test
    void testFromPosition_leadership() {
        assertEquals(HireSkill.NEGOTIATION, HireSkill.fromPosition(CorporatePosition.CFO));
        assertEquals(HireSkill.LOGISTICS, HireSkill.fromPosition(CorporatePosition.COO));
        assertEquals(HireSkill.RESEARCH, HireSkill.fromPosition(CorporatePosition.CTO));
        assertEquals(HireSkill.GOVERNANCE, HireSkill.fromPosition(CorporatePosition.SECTOR_GOVERNOR_DELEGATE));
        assertEquals(HireSkill.NEGOTIATION, HireSkill.fromPosition(CorporatePosition.TRADE_MINISTER));
    }

    @Test
    void testFromPosition_crew() {
        assertEquals(HireSkill.COMBAT, HireSkill.fromPosition(CorporatePosition.SHIP_CAPTAIN));
        assertEquals(HireSkill.COMBAT, HireSkill.fromPosition(CorporatePosition.SECURITY_CHIEF));
        assertEquals(HireSkill.RESEARCH, HireSkill.fromPosition(CorporatePosition.SCIENCE_OFFICER));
        assertEquals(HireSkill.LOGISTICS, HireSkill.fromPosition(CorporatePosition.ENGINEER));
    }

    @Test
    void testFromPosition_null() {
        assertEquals(HireSkill.LOGISTICS, HireSkill.fromPosition(null));
    }

    @Test
    void testPreferredContractTypes_combat() {
        var types = HireSkill.COMBAT.getPreferredContractTypes();
        assertTrue(types.contains(CorporateContract.ContractType.SECURITY));
        assertTrue(types.contains(CorporateContract.ContractType.MISSION));
    }

    @Test
    void testPreferredContractTypes_negotiation() {
        var types = HireSkill.NEGOTIATION.getPreferredContractTypes();
        assertTrue(types.contains(CorporateContract.ContractType.TRADE));
        assertTrue(types.contains(CorporateContract.ContractType.LOGISTICS));
    }

    @Test
    void testPreferredContractTypes_logistics() {
        var types = HireSkill.LOGISTICS.getPreferredContractTypes();
        assertTrue(types.contains(CorporateContract.ContractType.LOGISTICS));
        assertTrue(types.contains(CorporateContract.ContractType.TRADE));
    }

    @Test
    void testPreferredContractTypes_research() {
        var types = HireSkill.RESEARCH.getPreferredContractTypes();
        assertTrue(types.contains(CorporateContract.ContractType.MISSION));
        assertTrue(types.contains(CorporateContract.ContractType.TRADE));
    }

    @Test
    void testPreferredContractTypes_governance() {
        var types = HireSkill.GOVERNANCE.getPreferredContractTypes();
        assertTrue(types.contains(CorporateContract.ContractType.MISSION));
        assertTrue(types.contains(CorporateContract.ContractType.SECURITY));
    }

    @Test
    void testCorporatePosition_getPrimarySkill() {
        assertEquals(HireSkill.COMBAT, CorporatePosition.SECURITY_CHIEF.getPrimarySkill());
        assertEquals(HireSkill.NEGOTIATION, CorporatePosition.CFO.getPrimarySkill());
        assertEquals(HireSkill.LOGISTICS, CorporatePosition.COO.getPrimarySkill());
        assertEquals(HireSkill.RESEARCH, CorporatePosition.CTO.getPrimarySkill());
        assertEquals(HireSkill.GOVERNANCE, CorporatePosition.SECTOR_GOVERNOR_DELEGATE.getPrimarySkill());
    }
}
