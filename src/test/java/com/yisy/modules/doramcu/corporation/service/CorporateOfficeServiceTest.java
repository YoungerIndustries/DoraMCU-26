package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.service.MultiUniverseContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorporateOfficeService Tests")
class CorporateOfficeServiceTest {

    @Mock
    private CorporateContractRepository contractRepository;

    @Mock
    private CorporateHiringRepository hiringRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private ContractExecutionService contractExecutionService;

    @Mock
    private CorporationEconomyService economyService;

    @Mock
    private CorporationInteractionService interactionService;

    @Mock
    private CorporationPersistenceService persistenceService;

    @Mock
    private MultiUniverseContractService multiUniverseContractService;

    @Mock
    private EveCorporationRepository eveCorporationRepository;

    @InjectMocks
    private CorporateOfficeService corporateOfficeService;

    private CorporateContract testContract;
    private AiCharacter testAiCharacter;

    @BeforeEach
    void setUp() {
        testContract = new CorporateContract();
        testContract.setId(1L);
        testContract.setTitle("Test Contract");
        testContract.setType(CorporateContract.ContractType.TRADE);
        testContract.setDescription("A test trade contract");
        testContract.setPaymentCredits(5000);
        testContract.setXpReward(100);
        testContract.setDifficulty(3);
        testContract.setDurationTurns(2);
        testContract.setStatus(CorporateContract.Status.AVAILABLE);
        testContract.setCorporationId(1L);
        testContract.setCreatedAt(LocalDateTime.now());
        testContract.setExpiryDate(LocalDateTime.now().plusDays(30));

        testAiCharacter = new AiCharacter();
        testAiCharacter.setId(100L);
        testAiCharacter.setName("Test AI");
        testAiCharacter.setHirable(true);
        testAiCharacter.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        testAiCharacter.setLeadershipRating(7);
    }

    // ---- Contract Operations ----

    @Test
    @DisplayName("browseContracts should return all available contracts when no type filter")
    void browseContracts_noFilter() {
        when(contractRepository.findByStatus(CorporateContract.Status.AVAILABLE))
                .thenReturn(List.of(testContract));

        List<CorporateContract> result = corporateOfficeService.browseContracts(1L, null);

        assertEquals(1, result.size());
        assertEquals("Test Contract", result.get(0).getTitle());
        verify(contractRepository).findByStatus(CorporateContract.Status.AVAILABLE);
    }

    @Test
    @DisplayName("browseContracts should filter by contract type")
    void browseContracts_withTypeFilter() {
        when(contractRepository.findByTypeAndStatus(CorporateContract.ContractType.TRADE, CorporateContract.Status.AVAILABLE))
                .thenReturn(List.of(testContract));

        List<CorporateContract> result = corporateOfficeService.browseContracts(1L, CorporateContract.ContractType.TRADE);

        assertEquals(1, result.size());
        verify(contractRepository).findByTypeAndStatus(CorporateContract.ContractType.TRADE, CorporateContract.Status.AVAILABLE);
    }

    @Test
    @DisplayName("acceptContract should throw when contract not found")
    void acceptContract_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.acceptContract(1L, 999L));
    }

    @Test
    @DisplayName("acceptContract should throw when contract is not available")
    void acceptContract_notAvailable() {
        testContract.setStatus(CorporateContract.Status.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.acceptContract(1L, 1L));
    }

    @Test
    @DisplayName("acceptContract should assign contract to corporation")
    void acceptContract_success() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(invocation -> {
            CorporateContract c = invocation.getArgument(0);
            return c;
        });
        when(contractExecutionService.autoAssignCrew(1L)).thenReturn(Collections.emptyList());

        CorporateContract result = corporateOfficeService.acceptContract(1L, 1L);

        assertEquals(CorporateContract.Status.ACTIVE, result.getStatus());
        assertEquals(1L, result.getCorporationId());
        assertNotNull(result.getExpiryDate());
        verify(contractRepository).save(any(CorporateContract.class));
    }

    @Test
    @DisplayName("acceptContract should throw when capacity exceeded")
    void acceptContract_capacityExceeded() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(List.of(testContract, testContract, testContract));

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.acceptContract(1L, 1L));
    }

    @Test
    @DisplayName("completeContract should throw when contract not found")
    void completeContract_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.completeContract(999L));
    }

    @Test
    @DisplayName("completeContract should throw when contract is not active")
    void completeContract_notActive() {
        testContract.setStatus(CorporateContract.Status.AVAILABLE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.completeContract(1L));
    }

    @Test
    @DisplayName("completeContract should mark contract as completed")
    void completeContract_success() {
        testContract.setStatus(CorporateContract.Status.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorporateContract result = corporateOfficeService.completeContract(1L);

        assertEquals(CorporateContract.Status.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(contractRepository).save(any(CorporateContract.class));
    }

    @Test
    @DisplayName("getActiveContracts should return active contracts for corporation")
    void getActiveContracts() {
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(List.of(testContract));

        List<CorporateContract> result = corporateOfficeService.getActiveContracts(1L);

        assertEquals(1, result.size());
    }

    // ---- AI Hiring Operations ----

    @Test
    @DisplayName("browseHirableAI should return available AI characters")
    void browseHirableAI() {
        when(aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE))
                .thenReturn(List.of(testAiCharacter));
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<AiCharacter> result = corporateOfficeService.browseHirableAI(1L, null);

        assertEquals(1, result.size());
        assertEquals("Test AI", result.get(0).getName());
    }

    @Test
    @DisplayName("hireAiCharacter should throw when AI not found")
    void hireAiCharacter_notFound() {
        when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.hireAiCharacter(1L, 999L, CorporatePosition.CFO));
    }

    @Test
    @DisplayName("hireAiCharacter should throw when AI is not available")
    void hireAiCharacter_notAvailable() {
        testAiCharacter.setHiringStatus(AiCharacter.HiringStatus.HIRED);
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.hireAiCharacter(1L, 100L, CorporatePosition.CFO));
    }

    @Test
    @DisplayName("hireAiCharacter should throw when position is already filled")
    void hireAiCharacter_positionFilled() {
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(hiringRepository.existsByCorporationIdAndPositionAndStatus(1L, CorporatePosition.CFO, CorporateHiring.Status.ACTIVE))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.hireAiCharacter(1L, 100L, CorporatePosition.CFO));
    }

    @Test
    @DisplayName("hireAiCharacter should create hiring record and update AI status")
    void hireAiCharacter_success() {
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(hiringRepository.existsByCorporationIdAndPositionAndStatus(1L, CorporatePosition.CFO, CorporateHiring.Status.ACTIVE))
                .thenReturn(false);
        when(hiringRepository.save(any(CorporateHiring.class))).thenAnswer(invocation -> {
            CorporateHiring h = invocation.getArgument(0);
            h.setId(1L);
            return h;
        });
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interactionService.applyTradeAgreementBonuses(anyLong(), anyLong())).thenAnswer(invocation -> invocation.getArgument(1));

        CorporateHiring result = corporateOfficeService.hireAiCharacter(1L, 100L, CorporatePosition.CFO);

        assertNotNull(result);
        assertEquals(CorporatePosition.CFO, result.getPosition());
        assertEquals(CorporateHiring.Status.ACTIVE, result.getStatus());
        assertNotNull(result.getDailySalary());
        verify(aiCharacterRepository).save(any(AiCharacter.class));
    }

    @Test
    @DisplayName("fireAiCharacter should throw when hiring not found")
    void fireAiCharacter_notFound() {
        when(hiringRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                corporateOfficeService.fireAiCharacter(999L));
    }

    @Test
    @DisplayName("fireAiCharacter should terminate hiring and free AI character")
    void fireAiCharacter_success() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setId(1L);

        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hiring));
        when(hiringRepository.save(any(CorporateHiring.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(testAiCharacter));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> corporateOfficeService.fireAiCharacter(1L));

        assertEquals(CorporateHiring.Status.TERMINATED, hiring.getStatus());
        assertEquals(AiCharacter.HiringStatus.AVAILABLE, testAiCharacter.getHiringStatus());
    }

    @Test
    @DisplayName("getActiveHires should return active hires for corporation")
    void getActiveHires() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(hiring));

        List<CorporateHiring> result = corporateOfficeService.getActiveHires(1L);

        assertEquals(1, result.size());
        assertEquals(CorporatePosition.CFO, result.get(0).getPosition());
    }

    // ---- Office State ----

    @Test
    @DisplayName("getOfficeState should return office dashboard data")
    void getOfficeState() {
        when(persistenceService.getOfficeBuildingLevel(1L)).thenReturn(0);
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(economyService.getFinancialReport(1L)).thenReturn(Map.of(
                "creditBalance", 0L, "totalSalaryObligations", 0L,
                "isInsolvent", false, "activeHireCount", 0
        ));
        when(interactionService.getRelationships(1L)).thenReturn(Collections.emptyList());
        when(interactionService.getTradePartnerCount(1L)).thenReturn(0);
        when(contractRepository.findByStatus(CorporateContract.Status.AVAILABLE)).thenReturn(Collections.emptyList());
        when(aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE)).thenReturn(Collections.emptyList());
        EveCorporation corp = new EveCorporation();
        corp.setId(1L);
        corp.setName("Test Corporation");
        when(eveCorporationRepository.findById(1L)).thenReturn(Optional.of(corp));

        Map<String, Object> state = corporateOfficeService.getOfficeState(1L);

        assertEquals(1L, state.get("corporationId"));
        assertEquals(0, ((List<?>) state.get("activeContracts")).size());
        assertEquals(3, state.get("maxContracts"));
        assertEquals(0, state.get("officeBuildingLevel"));
        assertEquals(0L, state.get("creditBalance"));
    }

    @Test
    @DisplayName("constructOfficeBuilding should increase building level")
    void constructOfficeBuilding() {
        when(persistenceService.incrementOfficeBuildingLevel(1L)).thenReturn(1, 2);

        int level1 = corporateOfficeService.constructOfficeBuilding(1L, 10L);
        assertEquals(1, level1);

        int level2 = corporateOfficeService.constructOfficeBuilding(1L, 10L);
        assertEquals(2, level2);
    }

    @Test
    @DisplayName("office building level should increase max contracts")
    void officeBuildingIncreasesMaxContracts() {
        when(persistenceService.getOfficeBuildingLevel(1L)).thenReturn(0, 1);
        when(persistenceService.incrementOfficeBuildingLevel(1L)).thenReturn(1);

        corporateOfficeService.constructOfficeBuilding(1L, 10L);

        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(economyService.getFinancialReport(1L)).thenReturn(Map.of(
                "creditBalance", 0L, "totalSalaryObligations", 0L,
                "isInsolvent", false, "activeHireCount", 0
        ));
        when(interactionService.getRelationships(1L)).thenReturn(Collections.emptyList());
        when(interactionService.getTradePartnerCount(1L)).thenReturn(0);
        when(contractRepository.findByStatus(CorporateContract.Status.AVAILABLE)).thenReturn(Collections.emptyList());
        when(aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE)).thenReturn(Collections.emptyList());
        EveCorporation corp2 = new EveCorporation();
        corp2.setId(1L);
        corp2.setName("Test Corporation");
        when(eveCorporationRepository.findById(1L)).thenReturn(Optional.of(corp2));

        Map<String, Object> state = corporateOfficeService.getOfficeState(1L);

        assertEquals(5, state.get("maxContracts")); // 3 default + 2 per level
    }

    // ---- Contract Generation ----

    @Test
    @DisplayName("generateSampleContracts should create multiple contracts")
    void generateSampleContracts() {
        when(contractRepository.save(any(CorporateContract.class))).thenAnswer(invocation -> {
            CorporateContract c = invocation.getArgument(0);
            c.setId((long) (Math.random() * 1000));
            return c;
        });

        List<CorporateContract> contracts = corporateOfficeService.generateSampleContracts(1L);

        assertEquals(5, contracts.size());
        for (CorporateContract c : contracts) {
            assertEquals(CorporateContract.Status.AVAILABLE, c.getStatus());
            assertEquals(1L, c.getCorporationId());
        }
    }
}
