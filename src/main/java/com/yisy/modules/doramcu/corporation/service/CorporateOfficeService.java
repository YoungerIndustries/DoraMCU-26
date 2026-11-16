package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.model.CorporationRelationship;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.service.MultiUniverseContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service that handles the corporate office kiosk: contract browsing, acceptance,
 * completion, AI character hiring, firing, and office state management.
 * <p>
 * This is the central business logic for the hybrid kiosk system â€” a virtual
 * dashboard backed by a physical corporate office building.
 * </p>
 */
@Service
public class CorporateOfficeService {

    private static final Logger log = LoggerFactory.getLogger(CorporateOfficeService.class);

    /** Default maximum number of active contracts per corporation */
    private static final int DEFAULT_MAX_CONTRACTS = 3;

    /** Default daily salary range for AI hires */
    private static final long MIN_SALARY = 100;
    private static final long MAX_SALARY = 1000;

    private final CorporateContractRepository contractRepository;
    private final CorporateHiringRepository hiringRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final ContractExecutionService contractExecutionService;
    private final CorporationEconomyService economyService;
    private final CorporationInteractionService interactionService;
    private final CorporationPersistenceService persistenceService;
    private final MultiUniverseContractService multiUniverseContractService;
    private final EveCorporationRepository eveCorporationRepository;
    private final CharacterTrainingService characterTrainingService;
    private final CrewService crewService;

    private final Random random = new Random();

    public CorporateOfficeService(
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository,
            AiCharacterRepository aiCharacterRepository,
            ContractExecutionService contractExecutionService,
            CorporationEconomyService economyService,
            CorporationInteractionService interactionService,
            CorporationPersistenceService persistenceService,
            MultiUniverseContractService multiUniverseContractService,
            EveCorporationRepository eveCorporationRepository,
            CharacterTrainingService characterTrainingService,
            CrewService crewService) {
        this.contractRepository = contractRepository;
        this.hiringRepository = hiringRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.contractExecutionService = contractExecutionService;
        this.economyService = economyService;
        this.interactionService = interactionService;
        this.persistenceService = persistenceService;
        this.multiUniverseContractService = multiUniverseContractService;
        this.eveCorporationRepository = eveCorporationRepository;
        this.characterTrainingService = characterTrainingService;
        this.crewService = crewService;
    }

    // ---- Contract Operations ----

    /**
     * Browse available contracts, optionally filtered by type.
     *
     * @param corporationId the corporation ID
     * @param contractType  optional contract type filter
     * @return list of available contracts
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "corporationContracts", key = "#corporationId + '_' + #contractType")
    public List<CorporateContract> browseContracts(Long corporationId, CorporateContract.ContractType contractType) {
        if (contractType != null) {
            return contractRepository.findByTypeAndStatus(contractType, CorporateContract.Status.AVAILABLE);
        }
        return contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
    }

    /**
     * Accept a contract, assigning it to the corporation.
     *
     * @param corporationId the corporation ID
     * @param contractId    the contract ID
     * @return the updated contract
     * @throws IllegalArgumentException if contract not found or capacity exceeded
     */
    @Transactional
    @CacheEvict(value = {"corporationOfficeState", "corporationContracts", "corporationFinancialReport"}, allEntries = true)
    public CorporateContract acceptContract(Long corporationId, Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.AVAILABLE) {
            throw new IllegalArgumentException("Contract is not available: " + contract.getStatus());
        }

        // Check capacity
        int maxContracts = getMaxContracts(corporationId);
        long activeCount = contractRepository.findByCorporationIdAndStatus(corporationId, CorporateContract.Status.ACTIVE).size();
        if (activeCount >= maxContracts) {
            throw new IllegalArgumentException("Contract capacity exceeded. Max: " + maxContracts + ", Active: " + activeCount);
        }

        contract.setCorporationId(corporationId);
        contract.setStatus(CorporateContract.Status.ACTIVE);
        contract.setExpiryDate(LocalDateTime.now().plusDays((long) contract.getDurationTurns() * 7));
        contract = contractRepository.save(contract);

        // Auto-assign crew to the contract
        contractExecutionService.autoAssignCrew(contractId);

        log.info("Corporation {} accepted contract {}: {}", corporationId, contractId, contract.getTitle());
        return contractRepository.findById(contractId).orElse(contract);
    }

    /**
     * Complete a contract, awarding credits and XP.
     *
     * @param contractId the contract ID
     * @return the completed contract
     * @throws IllegalArgumentException if contract not found or not active
     */
    @Transactional
    @CacheEvict(value = {"corporationOfficeState", "corporationContracts", "corporationFinancialReport"}, allEntries = true)
    public CorporateContract completeContract(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.ACTIVE) {
            throw new IllegalArgumentException("Contract is not active: " + contract.getStatus());
        }

        contract.setStatus(CorporateContract.Status.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());
        contractRepository.save(contract);

        log.info("Contract {} completed. Payment: {} credits, XP: {}",
                contractId, contract.getPaymentCredits(), contract.getXpReward());
        return contract;
    }

    /**
     * Get all active contracts for a corporation.
     */
    @Transactional(readOnly = true)
    public List<CorporateContract> getActiveContracts(Long corporationId) {
        return contractRepository.findByCorporationIdAndStatus(corporationId, CorporateContract.Status.ACTIVE);
    }

    /**
     * Get detailed contract progress information.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContractStatus(Long contractId) {
        return contractExecutionService.getContractStatus(contractId);
    }

    /**
     * Browse available contracts filtered by universe (GameType).
     *
     * @param gameType the game type to filter by
     * @return list of available contracts from the specified universe
     */
    @Transactional(readOnly = true)
    public List<CorporateContract> browseContractsByUniverse(GameType gameType) {
        List<CorporateContract> allAvailable = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        List<CorporateContract> result = new ArrayList<>();
        for (CorporateContract contract : allAvailable) {
            if (contract.getGameType() == gameType) {
                result.add(contract);
            }
        }
        return result;
    }

    /**
     * Generate new contracts for a specific universe and save them.
     *
     * @param gameType the game type to generate contracts for
     * @param count    number of contracts to generate
     * @return list of generated contracts
     */
    @Transactional
    @CacheEvict(value = "corporationContracts", allEntries = true)
    public List<CorporateContract> generateContractsByUniverse(GameType gameType, int count) {
        List<CorporateContract> contracts = multiUniverseContractService.generateContracts(gameType, count);
        contracts = contractRepository.saveAll(contracts);
        log.info("Generated {} contracts for universe: {}", count, gameType.getDisplayName());
        return contracts;
    }

    /**
     * Generate new contracts from all universes and save them.
     *
     * @param perType number of contracts per universe
     * @return list of generated contracts
     */
    @Transactional
    @CacheEvict(value = "corporationContracts", allEntries = true)
    public List<CorporateContract> generateAllUniverseContracts(int perType) {
        List<CorporateContract> contracts = multiUniverseContractService.generateAllUniverseContracts(perType);
        contracts = contractRepository.saveAll(contracts);
        log.info("Generated {} contracts from all universes ({}/universe)", contracts.size(), perType);
        return contracts;
    }

    // ---- AI Hiring Operations ----

    /**
     * Browse available AI characters for hiring, optionally filtered by position type.
     *
     * @param corporationId the corporation ID
     * @param position      optional position filter
     * @return list of available AI characters
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> browseHirableAI(Long corporationId, CorporatePosition position) {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);

        // Filter out already hired characters for this corporation
        List<CorporateHiring> activeHires = hiringRepository
                .findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);
        List<Long> hiredIds = new ArrayList<>();
        for (CorporateHiring hire : activeHires) {
            hiredIds.add(hire.getAiCharacterId());
        }

        List<AiCharacter> result = new ArrayList<>();
        for (AiCharacter ai : available) {
            if (!hiredIds.contains(ai.getId())) {
                result.add(ai);
            }
        }
        return result;
    }

    /**
     * Hire an AI character for a position.
     *
     * @param corporationId the corporation ID
     * @param aiCharacterId the AI character ID
     * @param position      the position to hire for
     * @return the hiring record
     * @throws IllegalArgumentException if AI not found or already hired
     */
    @Transactional
    @CacheEvict(value = {"corporationOfficeState", "corporationCrew", "corporationFinancialReport"}, allEntries = true)
    public CorporateHiring hireAiCharacter(Long corporationId, Long aiCharacterId, CorporatePosition position) {
        AiCharacter aiCharacter = aiCharacterRepository.findById(aiCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("AI character not found: " + aiCharacterId));

        if (aiCharacter.getHiringStatus() != AiCharacter.HiringStatus.AVAILABLE) {
            throw new IllegalArgumentException("AI character is not available for hiring: " + aiCharacter.getHiringStatus());
        }

        // Check if position is already filled
        boolean positionFilled = hiringRepository.existsByCorporationIdAndPositionAndStatus(
                corporationId, position, CorporateHiring.Status.ACTIVE);
        if (positionFilled) {
            throw new IllegalArgumentException("Position " + position.getDisplayName() + " is already filled");
        }

        // Calculate salary based on AI character's experience and position
        long baseSalary = calculateSalary(aiCharacter, position);

        // Apply trade agreement bonuses (reduced hiring costs)
        long salary = interactionService.applyTradeAgreementBonuses(corporationId, baseSalary);
        if (salary < baseSalary) {
            log.info("Trade agreement discount applied for corporation {}: salary {} -> {}", 
                    corporationId, baseSalary, salary);
        }

        // Create hiring record
        CorporateHiring hiring = new CorporateHiring(corporationId, aiCharacterId, position, salary);
        hiring.setExpiryDate(LocalDateTime.now().plusDays(365));
        hiring.setPerformanceRating(aiCharacter.getLeadershipRating() > 0 ? (double) aiCharacter.getLeadershipRating() : 5.0);
        hiring = hiringRepository.save(hiring);

        // Update AI character status
        aiCharacter.setHiringStatus(AiCharacter.HiringStatus.HIRED);
        aiCharacterRepository.save(aiCharacter);

        log.info("Hired AI character {} for position {} at corporation {}. Salary: {}",
                aiCharacterId, position.getDisplayName(), corporationId, salary);
        return hiring;
    }

    /**
     * Fire an AI character (terminate hiring).
     *
     * @param hiringId the hiring record ID
     * @throws IllegalArgumentException if hiring not found
     */
    @Transactional
    @CacheEvict(value = {"corporationOfficeState", "corporationCrew", "corporationFinancialReport"}, allEntries = true)
    public void fireAiCharacter(Long hiringId) {
        CorporateHiring hiring = hiringRepository.findById(hiringId)
                .orElseThrow(() -> new IllegalArgumentException("Hiring record not found: " + hiringId));

        hiring.setStatus(CorporateHiring.Status.TERMINATED);
        hiringRepository.save(hiring);

        // Update AI character status
        AiCharacter aiCharacter = aiCharacterRepository.findById(hiring.getAiCharacterId()).orElse(null);
        if (aiCharacter != null) {
            aiCharacter.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
            aiCharacterRepository.save(aiCharacter);
        }

        log.info("Fired AI character {} from position {} at corporation {}",
                hiring.getAiCharacterId(), hiring.getPosition().getDisplayName(), hiring.getCorporationId());
    }

    /**
     * Get all active hires for a corporation.
     */
    @Transactional(readOnly = true)
    public List<CorporateHiring> getActiveHires(Long corporationId) {
        return hiringRepository.findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);
    }

    // ---- Office State ----

    /**
     * Get the full office state (dashboard data).
     *
     * @param corporationId the corporation ID
     * @return map containing office state information
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "corporationOfficeState", key = "#corporationId")
    public Map<String, Object> getOfficeState(Long corporationId) {
        int buildingLevel = persistenceService.getOfficeBuildingLevel(corporationId);
        int maxContracts = getMaxContracts(corporationId);
        int contractSlotBonus = buildingLevel * 2;
        double hiringCostDiscount = buildingLevel * 0.1;

        List<CorporateContract> activeContracts = getActiveContracts(corporationId);
        List<CorporateHiring> activeHiresRaw = getActiveHires(corporationId);

        // Build active hires summary list with AI character names for frontend
        List<Map<String, Object>> activeHires = new ArrayList<>();
        for (CorporateHiring hiring : activeHiresRaw) {
            Map<String, Object> hireSummary = new HashMap<>();
            hireSummary.put("id", hiring.getId());
            hireSummary.put("aiCharacterId", hiring.getAiCharacterId());
            AiCharacter aiChar = aiCharacterRepository.findById(hiring.getAiCharacterId()).orElse(null);
            hireSummary.put("aiCharacterName", aiChar != null ? aiChar.getName() : "Unknown");
            hireSummary.put("position", hiring.getPosition().name());
            hireSummary.put("status", hiring.getStatus().name());
            hireSummary.put("dailySalary", hiring.getDailySalary());
            hireSummary.put("performanceRating", hiring.getPerformanceRating());
            hireSummary.put("careerLevel", hiring.getCareerLevel() != null ? hiring.getCareerLevel().name() : "JUNIOR");
            hireSummary.put("xpEarned", hiring.getXpEarned());
            hireSummary.put("skills", hiring.getSkills());
            activeHires.add(hireSummary);
        }

        // Build detailed contract progress list
        List<Map<String, Object>> contractProgress = new ArrayList<>();
        for (CorporateContract contract : activeContracts) {
            Map<String, Object> progress = new HashMap<>();
            progress.put("contractId", contract.getId());
            progress.put("title", contract.getTitle());
            progress.put("type", contract.getType().name());
            progress.put("difficulty", contract.getDifficulty());
            progress.put("status", contract.getStatus().name());
            progress.put("turnsRemaining", contract.getTurnsRemaining());
            progress.put("durationTurns", contract.getDurationTurns());
            progress.put("progressPercent", contract.getProgressPercent());
            progress.put("assignedCrewIds", contract.getAssignedCrewIds());
            contractProgress.add(progress);
        }

        // Look up corporation name from EveCorporation
        String corporationName = "Unknown Corporation";
        try {
            EveCorporation corp = eveCorporationRepository.findById(corporationId).orElse(null);
            if (corp != null) {
                corporationName = corp.getName();
            }
        } catch (Exception e) {
            log.warn("Failed to look up corporation name for {}", corporationId, e);
        }

        // Build active contracts summary list for frontend
        List<Map<String, Object>> activeContractSummaries = new ArrayList<>();
        for (CorporateContract contract : activeContracts) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", contract.getId());
            summary.put("title", contract.getTitle());
            summary.put("type", contract.getType().name());
            summary.put("payment", contract.getPaymentCredits());
            summary.put("xpReward", contract.getXpReward());
            summary.put("difficulty", contract.getDifficulty());
            summary.put("durationTurns", contract.getDurationTurns());
            summary.put("status", contract.getStatus().name());
            summary.put("turnsRemaining", contract.getTurnsRemaining());
            summary.put("progressPercent", contract.getProgressPercent());
            summary.put("assignedCrewCount", contract.getAssignedCrewIds() != null ? contract.getAssignedCrewIds().size() : 0);
            activeContractSummaries.add(summary);
        }

        // Build available contracts summary list for frontend
        List<CorporateContract> availableContracts = browseContracts(corporationId, null);
        List<Map<String, Object>> availableContractSummaries = new ArrayList<>();
        for (CorporateContract contract : availableContracts) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", contract.getId());
            summary.put("title", contract.getTitle());
            summary.put("type", contract.getType().name());
            summary.put("payment", contract.getPaymentCredits());
            summary.put("xpReward", contract.getXpReward());
            summary.put("difficulty", contract.getDifficulty());
            summary.put("durationTurns", contract.getDurationTurns());
            summary.put("status", contract.getStatus().name());
            availableContractSummaries.add(summary);
        }

        // Build available AI characters summary list for frontend
        List<AiCharacter> availableAiChars = browseHirableAI(corporationId, null);
        List<Map<String, Object>> availableAiSummaries = new ArrayList<>();
        for (AiCharacter aiChar : availableAiChars) {
            Map<String, Object> aiSummary = new HashMap<>();
            aiSummary.put("id", aiChar.getId());
            aiSummary.put("name", aiChar.getName());
            aiSummary.put("leadershipRating", aiChar.getLeadershipRating());
            aiSummary.put("primarySkill", aiChar.getPrimarySkill());
            aiSummary.put("totalExperience", aiChar.getTotalExperience());
            availableAiSummaries.add(aiSummary);
        }

        Map<String, Object> state = new HashMap<>();
        state.put("corporationId", corporationId);
        state.put("corporationName", corporationName);
        state.put("activeContracts", activeContractSummaries);
        state.put("availableContracts", availableContractSummaries);
        state.put("availableAiCharacters", availableAiSummaries);
        state.put("maxContracts", maxContracts);
        state.put("contracts", contractProgress);
        state.put("activeHires", activeHires);
        state.put("officeBuildingLevel", buildingLevel);
        state.put("currentRole", "CEO");
        state.put("aiAssistantActive", false);
        state.put("activeTrainingTours", characterTrainingService.getActiveTrainingTours(corporationId).size());
        state.put("activeCrews", crewService.getActiveCrews(corporationId).size());
        state.put("bonuses", Map.of(
                "contractSlotBonus", contractSlotBonus,
                "hiringCostDiscount", hiringCostDiscount
        ));

        // Add economy data - check if corporation exists first to avoid transaction rollback
        boolean corporationExists = eveCorporationRepository.findById(corporationId).isPresent();
        if (corporationExists) {
            try {
                Map<String, Object> financialReport = economyService.getFinancialReport(corporationId);
                state.put("creditBalance", financialReport.get("creditBalance"));
                state.put("totalSalaryObligations", financialReport.get("totalSalaryObligations"));
                state.put("isInsolvent", financialReport.get("isInsolvent"));
                state.put("activeHireCount", financialReport.get("activeHireCount"));
            } catch (Exception e) {
                log.warn("Failed to get financial report for corporation {}: {}", corporationId, e.getMessage());
                state.put("creditBalance", 0L);
                state.put("totalSalaryObligations", 0L);
                state.put("isInsolvent", false);
                state.put("activeHireCount", activeHires.size());
            }
        } else {
            // Corporation not found in EveCorporation table â€” use defaults
            state.put("creditBalance", 0L);
            state.put("totalSalaryObligations", 0L);
            state.put("isInsolvent", false);
            state.put("activeHireCount", activeHires.size());
        }

        // Add relationship summary
        try {
            List<CorporationRelationship> relationships = interactionService.getRelationships(corporationId);
            List<Map<String, Object>> relationshipSummary = new ArrayList<>();
            for (CorporationRelationship rel : relationships) {
                Map<String, Object> relEntry = new HashMap<>();
                Long otherCorpId = rel.getCorporation1Id().equals(corporationId) ? 
                        rel.getCorporation2Id() : rel.getCorporation1Id();
                relEntry.put("corporationId", otherCorpId);
                relEntry.put("relationshipType", rel.getRelationshipType().name());
                relEntry.put("trustLevel", rel.getTrustLevel());
                relEntry.put("isFriendly", rel.isFriendly());
                relEntry.put("isHostile", rel.isHostile());
                relationshipSummary.add(relEntry);
            }
            state.put("relationships", relationshipSummary);
            state.put("tradePartnerCount", interactionService.getTradePartnerCount(corporationId));
        } catch (Exception e) {
            state.put("relationships", new ArrayList<>());
            state.put("tradePartnerCount", 0);
        }

        return state;
    }

    /**
     * Construct or upgrade the physical office building.
     *
     * @param corporationId the corporation ID
     * @param planetId      the planet ID where the building is constructed
     * @return the new building level
     */
    @Transactional
    @CacheEvict(value = "corporationOfficeState", key = "#corporationId")
    public int constructOfficeBuilding(Long corporationId, Long planetId) {
        int newLevel = persistenceService.incrementOfficeBuildingLevel(corporationId);

        log.info("Constructed office building for corporation {} on planet {}. Level: {}",
                corporationId, planetId, newLevel);
        return newLevel;
    }

    // ---- Contract Generation ----

    /**
     * Generate sample contracts for testing/demo purposes.
     *
     * @param corporationId the corporation ID
     * @return list of generated contracts
     */
    @Transactional
    public List<CorporateContract> generateSampleContracts(Long corporationId) {
        String[] tradeTitles = {"Spice Trade Route", "Mineral Export Deal", "Tech Import Agreement", "Agricultural Supply"};
        String[] securityTitles = {"Convoy Escort", "Station Defense", "Pirate Patrol", "Diplomatic Protection"};
        String[] logisticsTitles = {"Supply Chain Optimization", "Cargo Transport", "Fuel Delivery", "Equipment Shipment"};
        String[] missionTitles = {"Corporate Espionage", "Negotiation Mission", "Asset Recovery", "Market Analysis"};

        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();
        String[][] titles = {tradeTitles, securityTitles, logisticsTitles, missionTitles};

        List<CorporateContract> contracts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int typeIdx = random.nextInt(types.length);
            CorporateContract.ContractType type = types[typeIdx];
            String title = titles[typeIdx][random.nextInt(titles[typeIdx].length)];

            CorporateContract contract = new CorporateContract();
            contract.setTitle(title + " " + (i + 1));
            contract.setType(type);
            contract.setDescription("A " + type.name().toLowerCase() + " contract for the corporation.");
            contract.setPaymentCredits(random.nextInt(9000) + 1000);
            contract.setXpReward(random.nextInt(450) + 50);
            contract.setDifficulty(random.nextInt(10) + 1);
            contract.setDurationTurns(random.nextInt(5) + 1);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(corporationId);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setExpiryDate(LocalDateTime.now().plusDays(30));
            contracts.add(contractRepository.save(contract));
        }

        log.info("Generated {} sample contracts for corporation {}", contracts.size(), corporationId);
        return contracts;
    }

    // ---- Helper Methods ----

    private int getMaxContracts(Long corporationId) {
        int buildingLevel = persistenceService.getOfficeBuildingLevel(corporationId);
        return DEFAULT_MAX_CONTRACTS + (buildingLevel * 2);
    }

    private long calculateSalary(AiCharacter aiCharacter, CorporatePosition position) {
        long baseSalary = MIN_SALARY + (long) ((MAX_SALARY - MIN_SALARY) * (aiCharacter.getLeadershipRating() > 0 ? aiCharacter.getLeadershipRating() / 10.0 : 0.5));

        // Leadership positions pay more
        if (position.isLeadership()) {
            baseSalary = (long) (baseSalary * 1.5);
        }

        return baseSalary;
    }

    // ---- Getters for controller access ----

    public CorporateContractRepository getContractRepository() {
        return contractRepository;
    }

    public CorporateHiringRepository getHiringRepository() {
        return hiringRepository;
    }
}
