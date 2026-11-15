package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateContractAssignment;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.model.TravellerContractAssignment;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractAssignmentRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.service.MultiUniverseContractService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service that handles contract execution: auto-assignment of AI crew,
 * turn-based progression, success calculation, and reward distribution.
 * <p>
 * When a contract is accepted, crew is automatically assigned based on
 * position-to-contract-type mapping. Each turn the crew works on the contract,
 * and at the end success is determined by comparing crew performance against
 * the contract difficulty.
 * </p>
 */
@Service
public class ContractExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ContractExecutionService.class);

    /**
     * Maps each CorporatePosition to preferred contract types.
     * Used for auto-assigning crew to contracts.
     */
    private static final Map<CorporatePosition, List<CorporateContract.ContractType>> POSITION_CONTRACT_MAP = Map.of(
            CorporatePosition.CFO, List.of(CorporateContract.ContractType.TRADE, CorporateContract.ContractType.MISSION),
            CorporatePosition.COO, List.of(CorporateContract.ContractType.LOGISTICS, CorporateContract.ContractType.TRADE),
            CorporatePosition.CTO, List.of(CorporateContract.ContractType.MISSION, CorporateContract.ContractType.SECURITY),
            CorporatePosition.SECTOR_GOVERNOR_DELEGATE, List.of(CorporateContract.ContractType.MISSION, CorporateContract.ContractType.SECURITY),
            CorporatePosition.TRADE_MINISTER, List.of(CorporateContract.ContractType.TRADE, CorporateContract.ContractType.LOGISTICS),
            CorporatePosition.SHIP_CAPTAIN, List.of(CorporateContract.ContractType.MISSION, CorporateContract.ContractType.SECURITY),
            CorporatePosition.SECURITY_CHIEF, List.of(CorporateContract.ContractType.SECURITY, CorporateContract.ContractType.MISSION),
            CorporatePosition.SCIENCE_OFFICER, List.of(CorporateContract.ContractType.MISSION, CorporateContract.ContractType.TRADE),
            CorporatePosition.ENGINEER, List.of(CorporateContract.ContractType.LOGISTICS, CorporateContract.ContractType.SECURITY)
    );

    /**
     * Random number generator for success calculation variance.
     */
    private final Random random = new Random();

    private final CorporateContractRepository contractRepository;
    private final CorporateHiringRepository hiringRepository;
    private final CorporateContractAssignmentRepository assignmentRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final CorporationEconomyService economyService;
    private final CareerProgressionService careerProgressionService;
    private final TravellerCorporationService travellerCorporationService;
    private final MultiUniverseContractService multiUniverseContractService;

    public ContractExecutionService(
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository,
            CorporateContractAssignmentRepository assignmentRepository,
            AiCharacterRepository aiCharacterRepository,
            CorporationEconomyService economyService,
            CareerProgressionService careerProgressionService,
            TravellerCorporationService travellerCorporationService,
            MultiUniverseContractService multiUniverseContractService) {
        this.contractRepository = contractRepository;
        this.hiringRepository = hiringRepository;
        this.assignmentRepository = assignmentRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.economyService = economyService;
        this.careerProgressionService = careerProgressionService;
        this.travellerCorporationService = travellerCorporationService;
        this.multiUniverseContractService = multiUniverseContractService;
    }

    /**
     * Get the position-to-contract-type mapping.
     */
    public Map<CorporatePosition, List<CorporateContract.ContractType>> getPositionToContractMapping() {
        return POSITION_CONTRACT_MAP;
    }

    // ---- Auto-Assignment ----

    /**
     * Automatically assign the best available AI hires to a contract
     * based on position-to-contract-type mapping.
     *
     * @param contractId the contract ID
     * @return list of created assignments
     */
    @Transactional
    public List<CorporateContractAssignment> autoAssignCrew(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.ACTIVE) {
            throw new IllegalArgumentException("Contract is not active: " + contract.getStatus());
        }

        Long corporationId = contract.getCorporationId();
        CorporateContract.ContractType contractType = contract.getType();

        // Get all active hires for the corporation
        List<CorporateHiring> activeHires = hiringRepository
                .findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);

        if (activeHires.isEmpty()) {
            log.warn("No active hires found for corporation {} to assign to contract {}", corporationId, contractId);
            // Initialize turnsRemaining even without crew
            contract.setTurnsRemaining(contract.getDurationTurns());
            contract.setProgressPercent(0.0);
            contractRepository.save(contract);
            return Collections.emptyList();
        }

        // Find hires whose positions match the contract type
        List<CorporateHiring> matchedHires = new ArrayList<>();
        for (CorporateHiring hire : activeHires) {
            List<CorporateContract.ContractType> preferredTypes = POSITION_CONTRACT_MAP.get(hire.getPosition());
            if (preferredTypes != null && preferredTypes.contains(contractType)) {
                // Check if this hire is already assigned to another active contract
                if (!isHireBusy(hire.getId(), contractId)) {
                    matchedHires.add(hire);
                }
            }
        }

        // If no exact match, assign all available hires
        if (matchedHires.isEmpty()) {
            for (CorporateHiring hire : activeHires) {
                if (!isHireBusy(hire.getId(), contractId)) {
                    matchedHires.add(hire);
                }
            }
        }

        // Sort by performance rating (best first) and take top hires
        matchedHires.sort((a, b) -> {
            double perfA = a.getPerformanceRating() != null ? a.getPerformanceRating() : 5.0;
            double perfB = b.getPerformanceRating() != null ? b.getPerformanceRating() : 5.0;
            return Double.compare(perfB, perfA);
        });

        // Assign up to 3 best hires
        int maxCrew = Math.min(3, matchedHires.size());
        List<CorporateContractAssignment> assignments = new ArrayList<>();
        List<Long> crewIds = new ArrayList<>();

        for (int i = 0; i < maxCrew; i++) {
            CorporateHiring hire = matchedHires.get(i);
            CorporateContractAssignment assignment = new CorporateContractAssignment(
                    contractId, hire.getId(), hire.getAiCharacterId());
            assignment = assignmentRepository.save(assignment);
            assignments.add(assignment);
            crewIds.add(hire.getAiCharacterId());
            log.info("Assigned AI {} (position: {}) to contract {}",
                    hire.getAiCharacterId(), hire.getPosition().getDisplayName(), contractId);
        }

        // Update contract with crew assignments
        contract.setAssignedCrewIds(crewIds);
        contract.setTurnsRemaining(contract.getDurationTurns());
        contract.setProgressPercent(0.0);
        contractRepository.save(contract);

        log.info("Auto-assigned {} crew members to contract {}", assignments.size(), contractId);
        return assignments;
    }

    /**
     * Check if a hire is currently assigned to an active contract (busy).
     */
    private boolean isHireBusy(Long hiringId, Long excludeContractId) {
        List<CorporateContractAssignment> assignments = assignmentRepository.findByHiringId(hiringId);
        for (CorporateContractAssignment assignment : assignments) {
            if (!assignment.getContractId().equals(excludeContractId)) {
                Optional<CorporateContract> contractOpt = contractRepository.findById(assignment.getContractId());
                if (contractOpt.isPresent() && contractOpt.get().getStatus() == CorporateContract.Status.ACTIVE) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---- Turn Progression ----

    /**
     * Advance a contract by one turn.
     * Decrements turnsRemaining, updates progress, and finalizes if complete.
     *
     * @param contractId the contract ID
     * @return map with contract progress info
     * @throws IllegalArgumentException if contract not found or not active
     */
    @Transactional
    public Map<String, Object> processTurn(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.ACTIVE) {
            throw new IllegalArgumentException("Contract is not active: " + contract.getStatus());
        }

        // Process the turn
        boolean isComplete = contract.processTurn();
        contractRepository.save(contract);

        if (isComplete) {
            // Contract is done â€” finalize it
            log.info("Contract {} completed all turns. Finalizing...", contractId);
            finalizeContract(contractId);
        } else {
            log.info("Contract {} processed turn. Turns remaining: {}", contractId, contract.getTurnsRemaining());
        }

        return buildContractProgressResponse(contract);
    }

    // ---- Contract Finalization ----

    /**
     * Finalize a contract: determine success/failure and distribute rewards.
     * On success: credits the corporation with contract payment, then pays AI hire salaries.
     * On failure: applies a penalty (10% of contract payment as wasted resources).
     *
     * @param contractId the contract ID
     * @return map with finalization results
     */
    @Transactional
    public Map<String, Object> finalizeContract(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.ACTIVE) {
            throw new IllegalArgumentException("Contract is not active: " + contract.getStatus());
        }

        // Calculate success
        boolean success = calculateSuccess(contract);

        // Update contract status
        if (success) {
            contract.setStatus(CorporateContract.Status.COMPLETED);
        } else {
            contract.setStatus(CorporateContract.Status.FAILED);
        }
        contract.setCompletedAt(LocalDateTime.now());
        contractRepository.save(contract);

        long corporationId = contract.getCorporationId();
        long paymentCredits = 0L;
        long salaryPaid = 0L;
        long penaltyAmount = 0L;

        // Distribute rewards
        List<Map<String, Object>> crewXpAwarded = new ArrayList<>();
        if (success) {
            // Credit the corporation with contract payment
            String revenueDesc = "Contract payment: " + contract.getTitle();
            long paymentAmount = contract.getPaymentCredits() != null ? contract.getPaymentCredits().longValue() : 0L;
            economyService.creditCorporation(corporationId, paymentAmount, revenueDesc, contractId);
            paymentCredits = paymentAmount;

            // Pay salaries to all active hires
            salaryPaid = economyService.paySalaries(corporationId, contractId, contract.getDurationTurns());

            // Award XP to crew
            crewXpAwarded = distributeRewards(contract);
        } else {
            // Apply penalty: 10% of contract payment as wasted resources
            long paymentVal = contract.getPaymentCredits() != null ? contract.getPaymentCredits().longValue() : 0L;
            penaltyAmount = paymentVal / 10L;
            if (penaltyAmount > 0) {
                String penaltyDesc = "Failed contract penalty: " + contract.getTitle();
                economyService.debitCorporation(corporationId, penaltyAmount,
                        com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType.PENALTY,
                        penaltyDesc, contractId);
            }
        }

        log.info("Contract {} finalized. Success: {}, Payment: {}, Salaries: {}, Penalty: {}, XP: {}",
                contractId, success, paymentCredits, salaryPaid, penaltyAmount, contract.getXpReward());

        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contract.getId());
        result.put("title", contract.getTitle());
        result.put("status", contract.getStatus().name());
        result.put("success", success);
        result.put("paymentCredits", paymentCredits);
        result.put("salaryPaid", salaryPaid);
        result.put("penaltyAmount", penaltyAmount);
        result.put("xpReward", success ? contract.getXpReward() : 0);
        result.put("crewXpAwarded", crewXpAwarded);
        return result;
    }

    /**
     * Calculate whether a contract succeeds based on crew performance vs difficulty.
     * Uses skill-based effective performance from CareerProgressionService.
     * Also includes Traveller character performance from TravellerCorporationService.
     * Formula: successScore = avg(crew effective performance) * random(0.8, 1.2)
     * Success if successScore >= difficulty
     */
    public boolean calculateSuccess(CorporateContract contract) {
        List<Long> crewIds = contract.getAssignedCrewIds();
        CorporateContract.ContractType contractType = contract.getType();

        // Calculate average crew performance using skill-based effective performance
        double totalPerformance = 0;
        int count = 0;

        for (Long crewId : crewIds) {
            // Find the hiring record for this crew member
            List<CorporateContractAssignment> assignments = assignmentRepository
                    .findByContractId(contract.getId());
            for (CorporateContractAssignment assignment : assignments) {
                if (assignment.getAiCharacterId().equals(crewId)) {
                    Optional<CorporateHiring> hiringOpt = hiringRepository.findById(assignment.getHiringId());
                    if (hiringOpt.isPresent()) {
                        CorporateHiring hire = hiringOpt.get();
                        double effectivePerf = careerProgressionService.calculateEffectivePerformance(hire, contractType);
                        totalPerformance += effectivePerf;
                        count++;
                    }
                    break;
                }
            }
        }

        // Fallback to leadership rating if no hiring records found
        if (count == 0) {
            for (Long crewId : crewIds) {
                Optional<AiCharacter> aiOpt = aiCharacterRepository.findById(crewId);
                if (aiOpt.isPresent()) {
                    AiCharacter ai = aiOpt.get();
                    totalPerformance += ai.getLeadershipRating();
                    count++;
                }
            }
        }

        // Include Traveller character performance
        List<TravellerContractAssignment> travellerAssignments = travellerCorporationService.getAssignmentsForContract(contract.getId());
        for (TravellerContractAssignment tAssignment : travellerAssignments) {
            TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(tAssignment.getTravellerCharacterId());
            if (character != null) {
                double charPerf = travellerCorporationService.calculateCharacterPerformance(character, contractType);
                // Base performance is 7 (average ability), plus the character's bonus
                totalPerformance += 7.0 + charPerf;
                count++;
                log.info("Included Traveller character {} ({}) in contract {} success calculation with performance {}",
                        tAssignment.getTravellerCharacterId(), character.getName(), contract.getId(), 7.0 + charPerf);
            }
        }

        if (count == 0) {
            // No crew at all â€” very low chance of success
            return contract.getDifficulty() <= 2;
        }

        double avgPerformance = totalPerformance / count;

        // Apply cross-universe performance modifier
        com.yisy.modules.doramcu.gamecore.GameType contractGameType = contract.getGameType();
        if (contractGameType != null) {
            // For Traveller characters, use TRAVELLER as crew origin; for AI hires, use contract's universe
            double modifier = multiUniverseContractService.getPerformanceModifier(
                    com.yisy.modules.doramcu.gamecore.GameType.TRAVELLER, contractGameType);
            avgPerformance *= modifier;
            if (modifier != 1.0) {
                log.info("Contract {} cross-universe modifier: {} (crew=TRAVELLER, contract={})",
                        contract.getId(), modifier, contractGameType.getDisplayName());
            }
        }

        // Apply random variance (0.8 to 1.2)
        double variance = 0.8 + (random.nextDouble() * 0.4);
        double successScore = avgPerformance * variance;

        boolean success = successScore >= contract.getDifficulty();
        log.info("Contract {} success calculation: avgPerformance={}, variance={}, score={}, difficulty={}, success={}",
                contract.getId(), avgPerformance, variance, successScore, contract.getDifficulty(), success);
        return success;
    }

    /**
     * Distribute rewards upon successful contract completion.
     * Awards XP to each crew member proportionally.
     * Also awards career XP to hiring records for skill development.
     * Awards XP to Traveller characters assigned to the contract.
     */
    @Transactional
    public List<Map<String, Object>> distributeRewards(CorporateContract contract) {
        List<Map<String, Object>> crewXpAwarded = new ArrayList<>();
        List<Long> crewIds = contract.getAssignedCrewIds();

        // Count total crew (AI + Traveller) for XP split
        int aiCrewCount = crewIds != null ? crewIds.size() : 0;
        List<TravellerContractAssignment> travellerAssignments = travellerCorporationService.getAssignmentsForContract(contract.getId());
        int totalCrewCount = aiCrewCount + travellerAssignments.size();

        if (totalCrewCount == 0) {
            return crewXpAwarded;
        }

        int xpPerCrew = contract.getXpReward() / totalCrewCount;
        CorporateContract.ContractType contractType = contract.getType();

        // Award XP to AI crew
        if (crewIds != null) {
            for (Long crewId : crewIds) {
                Optional<AiCharacter> aiOpt = aiCharacterRepository.findById(crewId);
                if (aiOpt.isPresent()) {
                    AiCharacter ai = aiOpt.get();
                    ai.addExperience(xpPerCrew);
                    aiCharacterRepository.save(ai);

                    // Update assignment record with XP earned
                    List<CorporateContractAssignment> assignments = assignmentRepository
                            .findByContractId(contract.getId());
                    for (CorporateContractAssignment assignment : assignments) {
                        if (assignment.getAiCharacterId().equals(crewId)) {
                            assignment.setXpEarned(xpPerCrew);
                            assignmentRepository.save(assignment);

                            // Award career XP to the hiring record for skill development
                            careerProgressionService.awardXp(assignment.getHiringId(), xpPerCrew, contractType);
                            break;
                        }
                    }

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("aiCharacterId", crewId);
                    entry.put("name", ai.getName());
                    entry.put("xpEarned", xpPerCrew);
                    crewXpAwarded.add(entry);

                    log.info("Awarded {} XP to AI {} for contract {}", xpPerCrew, ai.getName(), contract.getId());
                }
            }
        }

        // Award XP to Traveller characters
        for (TravellerContractAssignment tAssignment : travellerAssignments) {
            TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(tAssignment.getTravellerCharacterId());
            if (character != null) {
                tAssignment.setXpEarned(xpPerCrew);
                travellerCorporationService.getAssignmentRepository().save(tAssignment);

                Map<String, Object> entry = new HashMap<>();
                entry.put("travellerCharacterId", tAssignment.getTravellerCharacterId());
                entry.put("name", character.getName());
                entry.put("xpEarned", xpPerCrew);
                crewXpAwarded.add(entry);

                log.info("Awarded {} XP to Traveller character {} ({}) for contract {}",
                        xpPerCrew, tAssignment.getTravellerCharacterId(), character.getName(), contract.getId());
            }
        }

        return crewXpAwarded;
    }

    // ---- Contract Status Queries ----

    /**
     * Get detailed contract progress information.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContractStatus(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        return buildContractProgressResponse(contract);
    }

    /**
     * Get assigned crew details for a contract.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getContractCrew(Long contractId) {
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        List<Map<String, Object>> crew = new ArrayList<>();
        List<Long> crewIds = contract.getAssignedCrewIds();

        if (crewIds != null) {
            for (Long crewId : crewIds) {
                Optional<AiCharacter> aiOpt = aiCharacterRepository.findById(crewId);
                if (aiOpt.isPresent()) {
                    AiCharacter ai = aiOpt.get();
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("aiCharacterId", ai.getId());
                    entry.put("name", ai.getName());
                    entry.put("leadershipRating", ai.getLeadershipRating());
                    entry.put("primarySkill", ai.getPrimarySkill());

                    // Find the hiring record for position info
                    List<CorporateContractAssignment> assignments = assignmentRepository
                            .findByContractId(contractId);
                    for (CorporateContractAssignment assignment : assignments) {
                        if (assignment.getAiCharacterId().equals(crewId)) {
                            Optional<CorporateHiring> hiringOpt = hiringRepository.findById(assignment.getHiringId());
                            if (hiringOpt.isPresent()) {
                                entry.put("position", hiringOpt.get().getPosition().getDisplayName());
                                entry.put("performanceRating", hiringOpt.get().getPerformanceRating());
                            }
                            entry.put("xpEarned", assignment.getXpEarned());
                            break;
                        }
                    }
                    crew.add(entry);
                }
            }
        }

        return crew;
    }

    /**
     * Calculate estimated success chance for a contract based on current crew.
     */
    public double estimateSuccessChance(CorporateContract contract) {
        List<Long> crewIds = contract.getAssignedCrewIds();

        if (crewIds == null || crewIds.isEmpty()) {
            return contract.getDifficulty() <= 2 ? 50.0 : 10.0;
        }

        double totalPerformance = 0;
        int count = 0;
        for (Long crewId : crewIds) {
            Optional<AiCharacter> aiOpt = aiCharacterRepository.findById(crewId);
            if (aiOpt.isPresent()) {
                totalPerformance += aiOpt.get().getLeadershipRating();
                count++;
            }
        }

        if (count == 0) {
            return 10.0;
        }

        double avgPerformance = totalPerformance / count;
        // Estimate based on average performance vs difficulty
        // At variance 1.0 (middle), success if avgPerformance >= difficulty
        // Chance = (avgPerformance / difficulty) * 100, capped at 100
        double chance = (avgPerformance / contract.getDifficulty()) * 100;
        return Math.min(100.0, Math.max(0.0, chance));
    }

    // ---- Helper Methods ----

    private Map<String, Object> buildContractProgressResponse(CorporateContract contract) {
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contract.getId());
        result.put("title", contract.getTitle());
        result.put("type", contract.getType().name());
        result.put("difficulty", contract.getDifficulty());
        result.put("status", contract.getStatus().name());
        Integer turnsRemaining = contract.getTurnsRemaining();
        if (turnsRemaining == null) {
            turnsRemaining = contract.getDurationTurns();
        }
        result.put("turnsRemaining", turnsRemaining);
        result.put("durationTurns", contract.getDurationTurns());
        result.put("progressPercent", contract.getProgressPercent());
        result.put("assignedCrew", getContractCrew(contract.getId()));
        result.put("estimatedSuccessChance", estimateSuccessChance(contract));

        boolean finalized = contract.getStatus() == CorporateContract.Status.COMPLETED
                || contract.getStatus() == CorporateContract.Status.FAILED;
        result.put("finalized", finalized);

        if (finalized) {
            result.put("success", contract.getStatus() == CorporateContract.Status.COMPLETED);
            result.put("paymentCredits", contract.getPaymentCredits());
            result.put("xpReward", contract.getXpReward());
        }

        return result;
    }
}
