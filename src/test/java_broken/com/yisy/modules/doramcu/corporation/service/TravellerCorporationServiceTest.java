package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.model.TravellerContractAssignment;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.repository.TravellerContractAssignmentRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TravellerCorporationServiceTest {

    @Mock
    CharacterService characterService;
    @Mock
    CorporateContractRepository contractRepository;
    @Mock
    TravellerContractAssignmentRepository assignmentRepository;

    TravellerCorporationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TravellerCorporationService(characterService, contractRepository, assignmentRepository);
    }

    // ---- Ability Bonus Tests ----

    @Test
    void getAbilityBonus_highInt_mission() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.INT, 12);

        double bonus = service.getAbilityBonus(character, CorporateContract.ContractType.MISSION);
        assertEquals(1.0, bonus, 0.01); // (12-7)*0.2 = 1.0, capped at 1.0
    }

    @Test
    void getAbilityBonus_lowInt_mission() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.INT, 2);

        double bonus = service.getAbilityBonus(character, CorporateContract.ContractType.MISSION);
        assertEquals(0.0, bonus, 0.01); // Negative bonus is clamped to 0 by max(0, bonus)
    }

    @Test
    void getAbilityBonus_averageAbility() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.EDU, 7);

        double bonus = service.getAbilityBonus(character, CorporateContract.ContractType.TRADE);
        assertEquals(0.0, bonus, 0.01); // (7-7)*0.2 = 0
    }

    @Test
    void getAbilityBonus_highEnd_security() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.END, 10);

        double bonus = service.getAbilityBonus(character, CorporateContract.ContractType.SECURITY);
        assertEquals(0.6, bonus, 0.01); // (10-7)*0.2 = 0.6
    }

    @Test
    void getAbilityBonus_highDex_logistics() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.DEX, 11);

        double bonus = service.getAbilityBonus(character, CorporateContract.ContractType.LOGISTICS);
        assertEquals(0.8, bonus, 0.01); // (11-7)*0.2 = 0.8
    }

    @Test
    void getAbilityBonus_ST_allTypes() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.ST, 10);

        for (CorporateContract.ContractType type : CorporateContract.ContractType.values()) {
            double bonus = service.getAbilityBonus(character, type);
            assertTrue(bonus >= 0.6, "ST should boost all contract types");
        }
    }

    // ---- Skill Bonus Tests ----

    @Test
    void getSkillBonus_combatSkills_security() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.addSkill(Skill.MELEE);
        character.addSkill(Skill.FIREARMS);

        double bonus = service.getSkillBonus(character, CorporateContract.ContractType.SECURITY);
        assertEquals(1.0, bonus, 0.01); // 2 combat skills * 0.5 = 1.0
    }

    @Test
    void getSkillBonus_socialSkills_trade() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.addSkill(Skill.ENTREPRENEUR);
        character.addSkill(Skill.DIPLOMACY);
        character.addSkill(Skill.ADMINISTRATION);

        double bonus = service.getSkillBonus(character, CorporateContract.ContractType.TRADE);
        assertEquals(1.5, bonus, 0.01); // 3 social skills * 0.5 = 1.5
    }

    @Test
    void getSkillBonus_cappedAt3() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        // Add many skills across categories
        for (Skill skill : Skill.values()) {
            character.addSkill(skill);
        }

        double bonus = service.getSkillBonus(character, CorporateContract.ContractType.MISSION);
        assertTrue(bonus <= 3.0, "Skill bonus should be capped at 3.0");
    }

    @Test
    void getSkillBonus_noSkills() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");

        double bonus = service.getSkillBonus(character, CorporateContract.ContractType.TRADE);
        assertEquals(0.0, bonus, 0.01);
    }

    // ---- Character Performance Tests ----

    @Test
    void calculateCharacterPerformance_combined() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.setAbility(AbilityScore.INT, 10); // +0.6 bonus
        character.addSkill(Skill.MELEE); // +0.5 for COMBAT->SECURITY

        double perf = service.calculateCharacterPerformance(character, CorporateContract.ContractType.MISSION);
        assertTrue(perf > 0, "Performance should be positive with matching ability");
    }

    // ---- Qualified Contracts Tests ----

    @Test
    void isQualified_entrepreneur_trade() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.addSkill(Skill.ENTREPRENEUR);

        CorporateContract contract = new CorporateContract("Trade Deal", CorporateContract.ContractType.TRADE, 1000, 1L);
        assertTrue(service.isQualified(character, character.getSkills(), contract));
    }

    @Test
    void isQualified_diplomacy_mission() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.addSkill(Skill.DIPLOMACY);

        CorporateContract contract = new CorporateContract("Diplomatic Mission", CorporateContract.ContractType.MISSION, 2000, 1L);
        assertTrue(service.isQualified(character, character.getSkills(), contract));
    }

    @Test
    void isQualified_combatSkill_security() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.addSkill(Skill.MELEE);

        CorporateContract contract = new CorporateContract("Security Contract", CorporateContract.ContractType.SECURITY, 1500, 1L);
        assertTrue(service.isQualified(character, character.getSkills(), contract));
    }

    @Test
    void isQualified_noSkills_notQualified() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");

        CorporateContract contract = new CorporateContract("Some Contract", CorporateContract.ContractType.TRADE, 1000, 1L);
        assertFalse(service.isQualified(character, character.getSkills(), contract));
    }

    // ---- Assignment Tests ----

    @Test
    void assignCharacter_success() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        when(characterService.getCharacter(1)).thenReturn(character);

        CorporateContract contract = new CorporateContract("Test Contract", CorporateContract.ContractType.TRADE, 1000, 1L);
        contract.setStatus(CorporateContract.Status.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(assignmentRepository.existsByContractIdAndTravellerCharacterId(10L, 1)).thenReturn(false);
        when(assignmentRepository.findByTravellerCharacterId(1)).thenReturn(Collections.emptyList());
        when(assignmentRepository.save(any(TravellerContractAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        TravellerContractAssignment assignment = service.assignCharacterToContract(1, 10L);

        assertNotNull(assignment);
        assertEquals(10L, assignment.getContractId());
        assertEquals(1, assignment.getTravellerCharacterId());
    }

    @Test
    void assignCharacter_characterNotFound() {
        when(characterService.getCharacter(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.assignCharacterToContract(999, 10L));
    }

    @Test
    void assignCharacter_contractNotFound() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        when(characterService.getCharacter(1)).thenReturn(character);
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.assignCharacterToContract(1, 999L));
    }

    @Test
    void assignCharacter_contractNotActive() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        when(characterService.getCharacter(1)).thenReturn(character);

        CorporateContract contract = new CorporateContract("Test", CorporateContract.ContractType.TRADE, 1000, 1L);
        contract.setStatus(CorporateContract.Status.AVAILABLE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThrows(IllegalStateException.class, () -> service.assignCharacterToContract(1, 10L));
    }

    @Test
    void assignCharacter_alreadyAssigned() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        when(characterService.getCharacter(1)).thenReturn(character);

        CorporateContract contract = new CorporateContract("Test", CorporateContract.ContractType.TRADE, 1000, 1L);
        contract.setStatus(CorporateContract.Status.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(assignmentRepository.existsByContractIdAndTravellerCharacterId(10L, 1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.assignCharacterToContract(1, 10L));
    }

    // ---- Character Stats Tests ----

    @Test
    void getCharacterStats_characterNotFound() {
        when(characterService.getCharacter(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.getCharacterStats(999));
    }

    @Test
    void getCharacterStats_empty() {
        TravellerCharacter character = new TravellerCharacter(1, "Test");
        character.rollAbilities();
        when(characterService.getCharacter(1)).thenReturn(character);
        when(assignmentRepository.findByTravellerCharacterId(1)).thenReturn(Collections.emptyList());

        Map<String, Object> stats = service.getCharacterStats(1);

        assertEquals(1, stats.get("characterId"));
        assertEquals("Test", stats.get("characterName"));
        assertEquals(0, stats.get("totalContracts"));
        assertEquals(0, stats.get("totalXpEarned"));
    }

    // ---- Getters Tests ----

    @Test
    void getCharacterService_returnsService() {
        assertSame(characterService, service.getCharacterService());
    }

    @Test
    void getAssignmentRepository_returnsRepository() {
        assertSame(assignmentRepository, service.getAssignmentRepository());
    }
}
