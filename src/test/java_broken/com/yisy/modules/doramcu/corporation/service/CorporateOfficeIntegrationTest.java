package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the CorporateOfficeService using an H2 in-memory database.
 * <p>
 * Follows the same pattern as {@code RealEstateIntegrationTest}: minimal Spring config,
 * H2 in-memory database, and {@code @Transactional} for automatic rollback.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CorporateOfficeTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CorporateOfficeService Integration Tests")
class CorporateOfficeIntegrationTest {

    @Autowired
    private CorporateOfficeService corporateOfficeService;

    @Autowired
    private CorporateContractRepository contractRepository;

    @Autowired
    private CorporateHiringRepository hiringRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    private Long testCorporationId;

    @BeforeEach
    void setUp() {
        // Unique corporation ID per test to avoid shared state in officeBuildingLevels
        testCorporationId = (long) (20000 + (int)(Math.random() * 80000));

        // Create sample AI characters for hiring tests
        AiCharacter ai1 = new AiCharacter("Alice Commander", "Leader", "Test Corp");
        ai1.setHirable(true);
        ai1.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        ai1.setLeadershipRating(8);
        ai1.setDailySalary(500);
        aiCharacterRepository.save(ai1);

        AiCharacter ai2 = new AiCharacter("Bob Engineer", "Engineer", "Test Corp");
        ai2.setHirable(true);
        ai2.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        ai2.setLeadershipRating(6);
        ai2.setDailySalary(300);
        aiCharacterRepository.save(ai2);

        // Create sample contracts
        CorporateContract contract1 = new CorporateContract();
        contract1.setTitle("Spice Trade Route");
        contract1.setType(CorporateContract.ContractType.TRADE);
        contract1.setDescription("A trade contract for spice routes.");
        contract1.setPaymentCredits(5000);
        contract1.setXpReward(100);
        contract1.setDifficulty(3);
        contract1.setDurationTurns(2);
        contract1.setStatus(CorporateContract.Status.AVAILABLE);
        contract1.setCorporationId(testCorporationId);
        contract1.setCreatedAt(LocalDateTime.now());
        contract1.setExpiryDate(LocalDateTime.now().plusDays(30));
        contractRepository.save(contract1);

        CorporateContract contract2 = new CorporateContract();
        contract2.setTitle("Convoy Escort");
        contract2.setType(CorporateContract.ContractType.SECURITY);
        contract2.setDescription("A security contract for convoy escort.");
        contract2.setPaymentCredits(8000);
        contract2.setXpReward(200);
        contract2.setDifficulty(5);
        contract2.setDurationTurns(3);
        contract2.setStatus(CorporateContract.Status.AVAILABLE);
        contract2.setCorporationId(testCorporationId);
        contract2.setCreatedAt(LocalDateTime.now());
        contract2.setExpiryDate(LocalDateTime.now().plusDays(30));
        contractRepository.save(contract2);

        CorporateContract contract3 = new CorporateContract();
        contract3.setTitle("Supply Chain Optimization");
        contract3.setType(CorporateContract.ContractType.LOGISTICS);
        contract3.setDescription("A logistics contract for supply chain.");
        contract3.setPaymentCredits(3000);
        contract3.setXpReward(75);
        contract3.setDifficulty(2);
        contract3.setDurationTurns(1);
        contract3.setStatus(CorporateContract.Status.AVAILABLE);
        contract3.setCorporationId(testCorporationId);
        contract3.setCreatedAt(LocalDateTime.now());
        contract3.setExpiryDate(LocalDateTime.now().plusDays(30));
        contractRepository.save(contract3);
    }

    // ---- Contract Tests ----

    @Test
    @DisplayName("Browse contracts should return all available contracts")
    void browseContracts_allAvailable() {
        List<CorporateContract> contracts = corporateOfficeService.browseContracts(testCorporationId, null);
        // browseContracts returns ALL available contracts, not filtered by corporation
        assertTrue(contracts.size() >= 3, "Should have at least 3 contracts");
    }

    @Test
    @DisplayName("Browse contracts should filter by type")
    void browseContracts_filterByType() {
        List<CorporateContract> tradeContracts = corporateOfficeService.browseContracts(
                testCorporationId, CorporateContract.ContractType.TRADE);
        assertTrue(tradeContracts.size() >= 1, "Should have at least 1 trade contract");
        assertEquals(CorporateContract.ContractType.TRADE, tradeContracts.get(0).getType());
    }

    @Test
    @DisplayName("Browse contracts should return empty for non-existent type")
    void browseContracts_noMissionContracts() {
        List<CorporateContract> missionContracts = corporateOfficeService.browseContracts(
                testCorporationId, CorporateContract.ContractType.MISSION);
        assertEquals(0, missionContracts.size());
    }

    @Test
    @DisplayName("Accept contract should change status to ACTIVE")
    void acceptContract_statusChange() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        assertEquals(3, available.size());

        CorporateContract contract = available.get(0);
        CorporateContract accepted = corporateOfficeService.acceptContract(testCorporationId, contract.getId());

        assertEquals(CorporateContract.Status.ACTIVE, accepted.getStatus());
        assertEquals(testCorporationId, accepted.getCorporationId());
        assertNotNull(accepted.getExpiryDate());
    }

    @Test
    @DisplayName("Accept non-existent contract should throw")
    void acceptContract_nonExistent() {
        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.acceptContract(testCorporationId, 99999L)
        );
    }

    @Test
    @DisplayName("Accept already active contract should throw")
    void acceptContract_alreadyActive() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        CorporateContract contract = available.get(0);
        corporateOfficeService.acceptContract(testCorporationId, contract.getId());

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.acceptContract(testCorporationId, contract.getId())
        );
    }

    @Test
    @DisplayName("Complete contract should change status to COMPLETED")
    void completeContract_statusChange() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        CorporateContract contract = available.get(0);
        corporateOfficeService.acceptContract(testCorporationId, contract.getId());

        CorporateContract completed = corporateOfficeService.completeContract(contract.getId());
        assertEquals(CorporateContract.Status.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("Complete non-active contract should throw")
    void completeContract_notActive() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        CorporateContract contract = available.get(0);

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.completeContract(contract.getId())
        );
    }

    @Test
    @DisplayName("Get active contracts should return accepted contracts")
    void getActiveContracts() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        CorporateContract contract = available.get(0);
        corporateOfficeService.acceptContract(testCorporationId, contract.getId());

        List<CorporateContract> active = corporateOfficeService.getActiveContracts(testCorporationId);
        assertEquals(1, active.size());
        assertEquals(contract.getId(), active.get(0).getId());
    }

    // ---- AI Hiring Tests ----

    @Test
    @DisplayName("Browse hirable AI should return available characters")
    void browseHirableAI() {
        List<AiCharacter> available = corporateOfficeService.browseHirableAI(testCorporationId, null);
        assertTrue(available.size() >= 2, "Should have at least 2 available AI characters");
    }

    @Test
    @DisplayName("Hire AI character should create hiring record")
    void hireAiCharacter_createsRecord() {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        AiCharacter ai = available.get(0);

        CorporateHiring hiring = corporateOfficeService.hireAiCharacter(
                testCorporationId, ai.getId(), CorporatePosition.CFO);

        assertNotNull(hiring.getId());
        assertEquals(testCorporationId, hiring.getCorporationId());
        assertEquals(ai.getId(), hiring.getAiCharacterId());
        assertEquals(CorporatePosition.CFO, hiring.getPosition());
        assertEquals(CorporateHiring.Status.ACTIVE, hiring.getStatus());
        assertNotNull(hiring.getHireDate());
        assertNotNull(hiring.getExpiryDate());
    }

    @Test
    @DisplayName("Hire AI character should update AI status to HIRED")
    void hireAiCharacter_updatesAiStatus() {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        AiCharacter ai = available.get(0);

        corporateOfficeService.hireAiCharacter(testCorporationId, ai.getId(), CorporatePosition.CFO);

        AiCharacter updated = aiCharacterRepository.findById(ai.getId()).orElseThrow();
        assertEquals(AiCharacter.HiringStatus.HIRED, updated.getHiringStatus());
    }

    @Test
    @DisplayName("Hire already hired AI should throw")
    void hireAiCharacter_alreadyHired() {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        AiCharacter ai = available.get(0);

        corporateOfficeService.hireAiCharacter(testCorporationId, ai.getId(), CorporatePosition.CFO);

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.hireAiCharacter(testCorporationId, ai.getId(), CorporatePosition.COO)
        );
    }

    @Test
    @DisplayName("Hire for filled position should throw")
    void hireAiCharacter_positionFilled() {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);

        corporateOfficeService.hireAiCharacter(testCorporationId, available.get(0).getId(), CorporatePosition.CFO);

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.hireAiCharacter(testCorporationId, available.get(1).getId(), CorporatePosition.CFO)
        );
    }

    @Test
    @DisplayName("Fire AI character should terminate hiring")
    void fireAiCharacter_terminates() {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        AiCharacter ai = available.get(0);

        CorporateHiring hiring = corporateOfficeService.hireAiCharacter(
                testCorporationId, ai.getId(), CorporatePosition.CFO);

        corporateOfficeService.fireAiCharacter(hiring.getId());

        CorporateHiring fired = hiringRepository.findById(hiring.getId()).orElseThrow();
        assertEquals(CorporateHiring.Status.TERMINATED, fired.getStatus());

        AiCharacter updated = aiCharacterRepository.findById(ai.getId()).orElseThrow();
        assertEquals(AiCharacter.HiringStatus.AVAILABLE, updated.getHiringStatus());
    }

    @Test
    @DisplayName("Get office state should return contracts and hires")
    void getOfficeState() {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        corporateOfficeService.acceptContract(testCorporationId, available.get(0).getId());

        List<AiCharacter> aiList = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        corporateOfficeService.hireAiCharacter(testCorporationId, aiList.get(0).getId(), CorporatePosition.CFO);

        java.util.Map<String, Object> state = corporateOfficeService.getOfficeState(testCorporationId);

        assertEquals(1, ((java.util.List<?>) state.get("activeContracts")).size());
        assertEquals(1, ((java.util.List<?>) state.get("activeHires")).size());
        assertEquals(0, state.get("officeBuildingLevel"));
    }

    @Test
    @DisplayName("Construct office building should increase level")
    void constructOfficeBuilding() {
        int level1 = corporateOfficeService.constructOfficeBuilding(testCorporationId, 1L);
        assertEquals(1, level1);

        int level2 = corporateOfficeService.constructOfficeBuilding(testCorporationId, 1L);
        assertEquals(2, level2);
    }

    @Test
    @DisplayName("Office building should increase max contracts via office state")
    void officeBuilding_increasesMaxContracts() {
        java.util.Map<String, Object> state1 = corporateOfficeService.getOfficeState(testCorporationId);
        assertEquals(3, state1.get("maxContracts"));

        corporateOfficeService.constructOfficeBuilding(testCorporationId, 1L);
        java.util.Map<String, Object> state2 = corporateOfficeService.getOfficeState(testCorporationId);
        assertEquals(5, state2.get("maxContracts"));
    }

    @Test
    @DisplayName("Generate sample contracts should create new contracts")
    void generateSampleContracts() {
        long before = contractRepository.count();
        corporateOfficeService.generateSampleContracts(testCorporationId);
        long after = contractRepository.count();
        assertTrue(after > before, "Should have created new contracts");
    }
}
