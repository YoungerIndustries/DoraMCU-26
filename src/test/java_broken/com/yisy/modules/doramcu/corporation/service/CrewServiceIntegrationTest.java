package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.model.Crew;
import com.yisy.modules.doramcu.corporation.model.CrewMember;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewMemberRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the CrewService using an H2 in-memory database.
 * <p>
 * Tests crew creation, member management, employee pool queries,
 * one-button crew assembly, and contract assignment.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TrainingAndCrewTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CrewService Integration Tests")
class CrewServiceIntegrationTest {

    private static final Long TEST_CORPORATION_ID = 1L;

    @Autowired
    private CrewService crewService;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private CrewMemberRepository crewMemberRepository;

    @Autowired
    private CorporateHiringRepository hiringRepository;

    @Autowired
    private CorporateContractRepository contractRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    private Long leaderHiringId;
    private Long memberHiringId1;
    private Long memberHiringId2;

    @BeforeEach
    void setUp() {
        // Create AI characters for crew members
        AiCharacter leaderAi = new AiCharacter("Cmdr. Vance", "Captain", "Test Corp");
        leaderAi.setHirable(true);
        leaderAi.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        leaderAi.setLeadershipRating(8);
        leaderAi.setPrimarySkill("COMBAT");
        leaderAi.setDailySalary(500L);
        leaderAi = aiCharacterRepository.save(leaderAi);

        AiCharacter memberAi1 = new AiCharacter("Lt. Chen", "Engineer", "Test Corp");
        memberAi1.setHirable(true);
        memberAi1.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        memberAi1.setLeadershipRating(6);
        memberAi1.setPrimarySkill("LOGISTICS");
        memberAi1.setDailySalary(300L);
        memberAi1 = aiCharacterRepository.save(memberAi1);

        AiCharacter memberAi2 = new AiCharacter("Ensign Patel", "Security", "Test Corp");
        memberAi2.setHirable(true);
        memberAi2.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        memberAi2.setLeadershipRating(5);
        memberAi2.setPrimarySkill("RESEARCH");
        memberAi2.setDailySalary(250L);
        memberAi2 = aiCharacterRepository.save(memberAi2);

        // Create hiring records
        CorporateHiring leaderHire = new CorporateHiring();
        leaderHire.setCorporationId(TEST_CORPORATION_ID);
        leaderHire.setAiCharacterId(leaderAi.getId());
        leaderHire.setPosition(CorporatePosition.SHIP_CAPTAIN);
        leaderHire.setStatus(CorporateHiring.Status.ACTIVE);
        leaderHire.setHireDate(LocalDateTime.now());
        leaderHire.setDailySalary(500L);
        leaderHire.setPerformanceRating(8.5);
        leaderHire = hiringRepository.save(leaderHire);
        leaderHiringId = leaderHire.getId();

        CorporateHiring memberHire1 = new CorporateHiring();
        memberHire1.setCorporationId(TEST_CORPORATION_ID);
        memberHire1.setAiCharacterId(memberAi1.getId());
        memberHire1.setPosition(CorporatePosition.ENGINEER);
        memberHire1.setStatus(CorporateHiring.Status.ACTIVE);
        memberHire1.setHireDate(LocalDateTime.now());
        memberHire1.setDailySalary(300L);
        memberHire1.setPerformanceRating(7.0);
        memberHire1 = hiringRepository.save(memberHire1);
        memberHiringId1 = memberHire1.getId();

        CorporateHiring memberHire2 = new CorporateHiring();
        memberHire2.setCorporationId(TEST_CORPORATION_ID);
        memberHire2.setAiCharacterId(memberAi2.getId());
        memberHire2.setPosition(CorporatePosition.SECURITY_CHIEF);
        memberHire2.setStatus(CorporateHiring.Status.ACTIVE);
        memberHire2.setHireDate(LocalDateTime.now());
        memberHire2.setDailySalary(250L);
        memberHire2.setPerformanceRating(6.5);
        memberHire2 = hiringRepository.save(memberHire2);
        memberHiringId2 = memberHire2.getId();
    }

    @Test
    @DisplayName("Create crew should create a crew with a leader")
    void createCrew() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Trade Crew", leaderHiringId,
                "Primary trade delivery crew");

        assertNotNull(crew.getId());
        assertEquals("Alpha Trade Crew", crew.getName());
        assertEquals(Crew.Status.ACTIVE, crew.getStatus());
        assertEquals(leaderHiringId, crew.getLeaderHiringId());

        // Verify leader is assigned to crew
        Optional<CorporateHiring> leaderOpt = hiringRepository.findById(leaderHiringId);
        assertTrue(leaderOpt.isPresent());
        assertEquals(crew.getId(), leaderOpt.get().getCrewId());

        // Verify crew member record exists
        List<CrewMember> members = crewMemberRepository.findByCrewId(crew.getId());
        assertEquals(1, members.size());
        assertTrue(members.get(0).getIsLeader());
    }

    @Test
    @DisplayName("Create crew should fail if leader is already in a crew")
    void createCrew_leaderAlreadyInCrew() {
        crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "First crew");

        assertThrows(IllegalArgumentException.class, () ->
                crewService.createCrew(TEST_CORPORATION_ID, "Beta Crew", leaderHiringId, "Second crew")
        );
    }

    @Test
    @DisplayName("Add member to crew should add a member")
    void addMemberToCrew() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");

        crewService.addMemberToCrew(crew.getId(), memberHiringId1);

        List<CrewMember> members = crewMemberRepository.findByCrewId(crew.getId());
        assertEquals(2, members.size());

        Optional<CorporateHiring> memberOpt = hiringRepository.findById(memberHiringId1);
        assertTrue(memberOpt.isPresent());
        assertEquals(crew.getId(), memberOpt.get().getCrewId());
    }

    @Test
    @DisplayName("Remove member from crew should remove a member")
    void removeMemberFromCrew() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");
        crewService.addMemberToCrew(crew.getId(), memberHiringId1);

        crewService.removeMemberFromCrew(crew.getId(), memberHiringId1);

        List<CrewMember> members = crewMemberRepository.findByCrewId(crew.getId());
        assertEquals(1, members.size());

        Optional<CorporateHiring> memberOpt = hiringRepository.findById(memberHiringId1);
        assertTrue(memberOpt.isPresent());
        assertNull(memberOpt.get().getCrewId());
    }

    @Test
    @DisplayName("Disband crew should return members to employee pool")
    void disbandCrew() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");
        crewService.addMemberToCrew(crew.getId(), memberHiringId1);

        crewService.disbandCrew(crew.getId());

        assertEquals(Crew.Status.DISBANDED, crewRepository.findById(crew.getId()).get().getStatus());

        // All members should be back in employee pool
        Optional<CorporateHiring> leaderOpt = hiringRepository.findById(leaderHiringId);
        assertTrue(leaderOpt.isPresent());
        assertNull(leaderOpt.get().getCrewId());

        Optional<CorporateHiring> memberOpt = hiringRepository.findById(memberHiringId1);
        assertTrue(memberOpt.isPresent());
        assertNull(memberOpt.get().getCrewId());
    }

    @Test
    @DisplayName("Get employee pool should return hires not in a crew")
    void getEmployeePool() {
        // Initially all 3 hires are in the employee pool
        List<CorporateHiring> pool = crewService.getEmployeePool(TEST_CORPORATION_ID);
        assertEquals(3, pool.size());

        // After creating a crew with leader, leader is no longer in pool
        crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");
        List<CorporateHiring> poolAfter = crewService.getEmployeePool(TEST_CORPORATION_ID);
        assertEquals(2, poolAfter.size());
    }

    @Test
    @DisplayName("Get active crews should return active crews")
    void getActiveCrews() {
        assertEquals(0, crewService.getActiveCrews(TEST_CORPORATION_ID).size());

        crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");
        List<Crew> activeCrews = crewService.getActiveCrews(TEST_CORPORATION_ID);
        assertEquals(1, activeCrews.size());
        assertEquals("Alpha Crew", activeCrews.get(0).getName());
    }

    @Test
    @DisplayName("Get crew by ID should return crew with members")
    void getCrewById() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");
        crewService.addMemberToCrew(crew.getId(), memberHiringId1);

        Crew fetched = crewService.getCrewById(crew.getId());
        assertEquals(crew.getId(), fetched.getId());

        // Verify members via repository to avoid lazy loading issues
        List<CrewMember> members = crewMemberRepository.findByCrewId(crew.getId());
        assertEquals(2, members.size());
    }

    @Test
    @DisplayName("Auto assemble crew should create crew from employee pool")
    void autoAssembleCrew() {
        // Create a TRADE contract
        CorporateContract contract = new CorporateContract();
        contract.setCorporationId(TEST_CORPORATION_ID);
        contract.setTitle("Ore Delivery");
        contract.setType(CorporateContract.ContractType.TRADE);
        contract.setDifficulty(3);
        contract.setDurationTurns(4);
        contract.setPaymentCredits(50000);
        contract.setXpReward(100);
        contract.setStatus(CorporateContract.Status.AVAILABLE);
        contract.setCreatedAt(LocalDateTime.now());
        contract.setExpiryDate(LocalDateTime.now().plusDays(30));
        contract = contractRepository.save(contract);

        // Auto-assemble crew
        Crew crew = crewService.autoAssembleCrew(TEST_CORPORATION_ID, contract.getId());

        assertNotNull(crew);
        assertNotNull(crew.getId());
        assertTrue(crew.getName().contains("Trade Crew"));

        // Verify crew has members
        List<CrewMember> members = crewMemberRepository.findByCrewId(crew.getId());
        assertTrue(members.size() >= 1, "Crew should have at least a leader");

        // Verify contract is assigned to crew
        Optional<CorporateContract> contractOpt = contractRepository.findById(contract.getId());
        assertTrue(contractOpt.isPresent());
        assertEquals(crew.getId(), contractOpt.get().getCrewId());

        // Verify crew status is ON_CONTRACT
        assertEquals(Crew.Status.ON_CONTRACT, crew.getStatus());
    }

    @Test
    @DisplayName("Assign crew to contract should link crew and contract")
    void assignCrewToContract() {
        Crew crew = crewService.createCrew(TEST_CORPORATION_ID, "Alpha Crew", leaderHiringId, "Test crew");

        CorporateContract contract = new CorporateContract();
        contract.setCorporationId(TEST_CORPORATION_ID);
        contract.setTitle("Security Detail");
        contract.setType(CorporateContract.ContractType.SECURITY);
        contract.setDifficulty(4);
        contract.setDurationTurns(6);
        contract.setPaymentCredits(75000);
        contract.setXpReward(150);
        contract.setStatus(CorporateContract.Status.AVAILABLE);
        contract.setCreatedAt(LocalDateTime.now());
        contract.setExpiryDate(LocalDateTime.now().plusDays(30));
        contract = contractRepository.save(contract);

        crewService.assignCrewToContract(crew.getId(), contract.getId());

        assertEquals(Crew.Status.ON_CONTRACT, crew.getStatus());
        assertEquals(contract.getId(), crew.getContractId());

        Optional<CorporateContract> contractOpt = contractRepository.findById(contract.getId());
        assertTrue(contractOpt.isPresent());
        assertEquals(crew.getId(), contractOpt.get().getCrewId());
    }
}
