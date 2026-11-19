package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.ai.DynamicAiPersonality;
import com.yisy.modules.doramcu.ai.PlayerCloneService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.corporation.model.AiAssistantAction;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.model.misc.CorporateRole;
import com.yisy.modules.doramcu.repository.ai.AiAssistantActionRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service that orchestrates autonomous corporation management per game turn.
 * <p>
 * When the player is in VP mode, the AI assistant (using the player's cloned personality)
 * manages the entire corporation each game turn: processes all active contracts, accepts
 * new contracts from the available pool, hires crew when needed, and fires underperforming
 * crew. All actions are logged for auditability.
 * </p>
 */
@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    /** Maximum contract acceptances per turn to prevent infinite loops */
    static final int MAX_CONTRACT_ACCEPTANCES = 10;

    /** Maximum hires per turn to prevent runaway spending */
    static final int MAX_HIRES_PER_TURN = 2;

    /** Default performance threshold for firing underperforming crew */
    static final double DEFAULT_FIRE_THRESHOLD = 3.0;

    /** Minimum balance multiplier for hiring safety check */
    static final int HIRING_SAFETY_MULTIPLIER = 10;

    private final PlayerCloneService playerCloneService;
    private final CorporateRoleService corporateRoleService;
    private final ContractExecutionService contractExecutionService;
    private final CorporateOfficeService corporateOfficeService;
    private final CorporationEconomyService economyService;
    private final AiAssistantActionRepository actionRepository;
    private final PlayerRepository playerRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final CorporateContractRepository contractRepository;
    private final CorporateHiringRepository hiringRepository;

    public AiAssistantService(
            PlayerCloneService playerCloneService,
            CorporateRoleService corporateRoleService,
            ContractExecutionService contractExecutionService,
            CorporateOfficeService corporateOfficeService,
            CorporationEconomyService economyService,
            AiAssistantActionRepository actionRepository,
            PlayerRepository playerRepository,
            AiCharacterRepository aiCharacterRepository,
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository) {
        this.playerCloneService = playerCloneService;
        this.corporateRoleService = corporateRoleService;
        this.contractExecutionService = contractExecutionService;
        this.corporateOfficeService = corporateOfficeService;
        this.economyService = economyService;
        this.actionRepository = actionRepository;
        this.playerRepository = playerRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.contractRepository = contractRepository;
        this.hiringRepository = hiringRepository;
    }

    // ---- Main Entry Point ----

    /**
     * Run the AI management cycle for a player's corporation.
     * <p>
     * The cycle: 1) Check player role is VP, skip if CEO 2) Get AI personality 3) Process
     * all active contract turns 4) Finalize completed contracts 5) Accept new contracts
     * based on personality 6) Hire crew if needed 7) Fire underperforming crew 8) Log all actions
     * </p>
     *
     * @param playerId the human player ID
     * @param corporationId the corporation ID
     * @return summary of actions taken
     */
    @Transactional
    public Map<String, Object> runManagementCycle(Long playerId, Long corporationId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("playerId", playerId);
        summary.put("corporationId", corporationId);
        summary.put("actionsTaken", new ArrayList<>());

        // Step 1: Check player role
        Optional<CorporateRole> roleOpt = corporateRoleService.getPlayerRole(playerId);
        if (roleOpt.isEmpty() || roleOpt.get() != CorporateRole.VP) {
            String reason = roleOpt.isEmpty() ? "Player not found" : "Player is in " + roleOpt.get().getDisplayName() + " mode";
            log.info("AI management cycle skipped for player {}: {}", playerId, reason);
            logAction(playerId, AiAssistantAction.ActionType.SKIP_TURN, null,
                    AiAssistantAction.Result.SKIPPED, "Skipped: " + reason, null);
            summary.put("skipped", true);
            summary.put("reason", reason);
            return summary;
        }

        // Step 2: Get AI personality
        DynamicAiPersonality personality = getPersonality(playerId);
        String personalitySnapshot = personality != null ? personalityToSnapshot(personality) : null;

        if (personality == null) {
            // Fallback: use default balanced personality
            log.warn("No personality found for player {}. Using defaults.", playerId);
            personalitySnapshot = "{\"aggression\":0.5,\"expansionDrive\":0.5,\"economicFocus\":0.5,\"scientificFocus\":0.5,\"diplomaticStance\":0.5,\"riskTolerance\":0.5}";
        }

        log.info("AI management cycle started for player {} (corporation {}) with personality: {}",
                playerId, corporationId, personalitySnapshot);

        // Step 3: Process all active contract turns
        int contractsProcessed = processAllContractTurns(playerId, corporationId, personalitySnapshot);
        summary.put("contractsProcessed", contractsProcessed);

        // Step 4: Finalize completed contracts (already done in processTurn)
        // Step 5: Accept new contracts based on personality
        int contractsAccepted = acceptNewContracts(playerId, corporationId, personalitySnapshot);
        summary.put("contractsAccepted", contractsAccepted);

        // Step 6: Hire crew if needed
        int hiresMade = hireCrewIfNeeded(playerId, corporationId, personalitySnapshot);
        summary.put("hiresMade", hiresMade);

        // Step 7: Fire underperforming crew
        int firesMade = fireUnderperformingCrew(playerId, corporationId, personalitySnapshot);
        summary.put("firesMade", firesMade);

        summary.put("skipped", false);
        summary.put("personalitySnapshot", personalitySnapshot);

        log.info("AI management cycle completed for player {}: processed={}, accepted={}, hired={}, fired={}",
                playerId, contractsProcessed, contractsAccepted, hiresMade, firesMade);

        return summary;
    }

    // ---- Contract Processing ----

    /**
     * Process all active contract turns for a corporation.
     */
    private int processAllContractTurns(Long playerId, Long corporationId, String personalitySnapshot) {
        List<CorporateContract> activeContracts = corporateOfficeService.getActiveContracts(corporationId);
        int processed = 0;

        for (CorporateContract contract : activeContracts) {
            try {
                Map<String, Object> result = contractExecutionService.processTurn(contract.getId());
                processed++;

                if (result.get("finalized") != null && (Boolean) result.get("finalized")) {
                    Boolean success = (Boolean) result.getOrDefault("success", false);
                    String desc = "Contract '" + contract.getTitle() + "' finalized. " +
                            (success ? "SUCCESS" : "FAILED") + ". Payment: " + contract.getPaymentCredits();
                    logAction(playerId, AiAssistantAction.ActionType.PROCESS_CONTRACT, contract.getId(),
                            success ? AiAssistantAction.Result.SUCCESS : AiAssistantAction.Result.FAILED,
                            desc, personalitySnapshot);
                } else {
                    String desc = "Contract '" + contract.getTitle() + "' advanced. Turns remaining: " +
                            contract.getTurnsRemaining() + "/" + contract.getDurationTurns();
                    logAction(playerId, AiAssistantAction.ActionType.PROCESS_CONTRACT, contract.getId(),
                            AiAssistantAction.Result.SUCCESS, desc, personalitySnapshot);
                }
            } catch (Exception e) {
                log.error("Error processing contract {} for corporation {}: {}",
                        contract.getId(), corporationId, e.getMessage());
                logAction(playerId, AiAssistantAction.ActionType.PROCESS_CONTRACT, contract.getId(),
                        AiAssistantAction.Result.FAILED, "Error: " + e.getMessage(), personalitySnapshot);
            }
        }

        return processed;
    }

    // ---- Contract Acceptance ----

    /**
     * Accept new contracts from the available pool based on personality.
     */
    private int acceptNewContracts(Long playerId, Long corporationId, String personalitySnapshot) {
        List<CorporateContract> availableContracts = corporateOfficeService.browseContracts(corporationId, null);
        int accepted = 0;

        for (CorporateContract contract : availableContracts) {
            if (accepted >= MAX_CONTRACT_ACCEPTANCES) {
                break;
            }

            if (shouldAcceptContract(contract, corporationId, personalitySnapshot)) {
                try {
                    CorporateContract acceptedContract = corporateOfficeService.acceptContract(corporationId, contract.getId());
                    accepted++;
                    String desc = "Accepted contract '" + contract.getTitle() + "' (type: " +
                            contract.getType() + ", difficulty: " + contract.getDifficulty() +
                            ", payment: " + contract.getPaymentCredits() + ")";
                    logAction(playerId, AiAssistantAction.ActionType.ACCEPT_CONTRACT, acceptedContract.getId(),
                            AiAssistantAction.Result.SUCCESS, desc, personalitySnapshot);
                } catch (Exception e) {
                    log.warn("Failed to accept contract {} for corporation {}: {}",
                            contract.getId(), corporationId, e.getMessage());
                    logAction(playerId, AiAssistantAction.ActionType.ACCEPT_CONTRACT, contract.getId(),
                            AiAssistantAction.Result.FAILED, "Failed: " + e.getMessage(), personalitySnapshot);
                }
            }
        }

        return accepted;
    }

    /**
     * Decide whether to accept a contract based on personality traits.
     */
    private boolean shouldAcceptContract(CorporateContract contract, Long corporationId, String personalitySnapshot) {
        // Parse personality snapshot
        Map<String, Double> personality = parsePersonalitySnapshot(personalitySnapshot);
        double aggression = personality.getOrDefault("aggression", 0.5);
        double economicFocus = personality.getOrDefault("economicFocus", 0.5);
        double riskTolerance = personality.getOrDefault("riskTolerance", 0.5);

        // Check current active contract count
        List<CorporateContract> activeContracts = corporateOfficeService.getActiveContracts(corporationId);
        List<CorporateHiring> activeHires = corporateOfficeService.getActiveHires(corporationId);

        // Don't accept if no crew and aggressive AI (high risk of failure)
        if (activeHires.isEmpty() && aggression < 0.6) {
            return false;
        }

        // Aggressive AI accepts high-difficulty contracts regardless of payment
        if (aggression > 0.6) {
            // Accept if difficulty is within risk tolerance
            double difficultyRatio = contract.getDifficulty() / 10.0;
            return difficultyRatio <= (riskTolerance + 0.3); // Aggressive AI takes more risks
        }

        // Economic AI prefers high-payment, low-difficulty contracts
        if (economicFocus > 0.6) {
            // Calculate payment-to-difficulty ratio
            double paymentPerDifficulty = (double) contract.getPaymentCredits() / contract.getDifficulty();
            // Accept if payment is good relative to difficulty
            return paymentPerDifficulty > 500; // Minimum 500 credits per difficulty point
        }

        // Balanced AI: accept if difficulty is manageable
        double difficultyRatio = contract.getDifficulty() / 10.0;
        return difficultyRatio <= riskTolerance;
    }

    // ---- Crew Hiring ----

    /**
     * Hire crew if the number of active contracts exceeds available crew capacity.
     */
    private int hireCrewIfNeeded(Long playerId, Long corporationId, String personalitySnapshot) {
        List<CorporateContract> activeContracts = corporateOfficeService.getActiveContracts(corporationId);
        List<CorporateHiring> activeHires = corporateOfficeService.getActiveHires(corporationId);

        // Check if we need more crew (more than 3 active contracts per hire)
        if (activeHires.size() >= activeContracts.size()) {
            return 0;
        }

        // Check balance for hiring safety
        try {
            Map<String, Object> report = economyService.getFinancialReport(corporationId);
            Long balance = (Long) report.getOrDefault("creditBalance", 0L);
            Long salaryObligations = (Long) report.getOrDefault("totalSalaryObligations", 0L);

            if (balance < salaryObligations * HIRING_SAFETY_MULTIPLIER) {
                log.info("AI skipped hiring for corporation {}: insufficient balance ({}) for safety check",
                        corporationId, balance);
                return 0;
            }
        } catch (Exception e) {
            log.warn("Could not check financial report for corporation {}: {}", corporationId, e.getMessage());
        }

        // Parse personality for hiring decisions
        Map<String, Double> personality = parsePersonalitySnapshot(personalitySnapshot);
        double economicFocus = personality.getOrDefault("economicFocus", 0.5);

        // Economic AI is conservative about hiring
        if (economicFocus > 0.6) {
            // Only hire if critically needed
            if (activeHires.size() >= activeContracts.size() - 1) {
                return 0;
            }
        }

        // Find positions that are needed based on active contract types
        Set<CorporatePosition> neededPositions = determineNeededPositions(activeContracts);
        int hired = 0;

        for (CorporatePosition position : neededPositions) {
            if (hired >= MAX_HIRES_PER_TURN) {
                break;
            }

            // Check if position is already filled
            boolean positionFilled = hiringRepository.existsByCorporationIdAndPositionAndStatus(
                    corporationId, position, CorporateHiring.Status.ACTIVE);
            if (positionFilled) {
                continue;
            }

            // Find best available AI for this position
            AiCharacter bestAi = findBestAiForPosition(corporationId, position, activeHires);
            if (bestAi == null) {
                continue;
            }

            try {
                CorporateHiring hiring = corporateOfficeService.hireAiCharacter(
                        corporationId, bestAi.getId(), position);
                hired++;
                String desc = "Hired AI '" + bestAi.getName() + "' for position " +
                        position.getDisplayName() + " (salary: " + hiring.getDailySalary() + ")";
                logAction(playerId, AiAssistantAction.ActionType.HIRE_CREW, hiring.getId(),
                        AiAssistantAction.Result.SUCCESS, desc, personalitySnapshot);
            } catch (Exception e) {
                log.warn("Failed to hire AI for position {} at corporation {}: {}",
                        position, corporationId, e.getMessage());
                logAction(playerId, AiAssistantAction.ActionType.HIRE_CREW, null,
                        AiAssistantAction.Result.FAILED, "Failed: " + e.getMessage(), personalitySnapshot);
            }
        }

        return hired;
    }

    /**
     * Determine which positions are needed based on active contract types.
     */
    private Set<CorporatePosition> determineNeededPositions(List<CorporateContract> activeContracts) {
        Set<CorporatePosition> needed = new HashSet<>();
        Map<CorporatePosition, List<CorporateContract.ContractType>> mapping =
                contractExecutionService.getPositionToContractMapping();

        // Count contract types
        Map<CorporateContract.ContractType, Integer> typeCounts = new HashMap<>();
        for (CorporateContract contract : activeContracts) {
            typeCounts.merge(contract.getType(), 1, Integer::sum);
        }

        // Find positions that match the most needed contract types
        for (Map.Entry<CorporatePosition, List<CorporateContract.ContractType>> entry : mapping.entrySet()) {
            for (CorporateContract.ContractType type : entry.getValue()) {
                if (typeCounts.containsKey(type)) {
                    needed.add(entry.getKey());
                    break;
                }
            }
        }

        return needed;
    }

    /**
     * Find the best available AI character for a position.
     */
    private AiCharacter findBestAiForPosition(Long corporationId, CorporatePosition position, List<CorporateHiring> activeHires) {
        List<AiCharacter> available = aiCharacterRepository
                .findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);

        // Filter out already hired characters
        Set<Long> hiredIds = new HashSet<>();
        for (CorporateHiring hire : activeHires) {
            hiredIds.add(hire.getAiCharacterId());
        }

        AiCharacter best = null;
        double bestRating = -1;

        for (AiCharacter ai : available) {
            if (hiredIds.contains(ai.getId())) {
                continue;
            }
            double rating = ai.getLeadershipRating() > 0 ? ai.getLeadershipRating() : 5.0;
            if (rating > bestRating) {
                bestRating = rating;
                best = ai;
            }
        }

        return best;
    }

    // ---- Crew Firing ----

    /**
     * Fire underperforming crew based on personality-driven threshold.
     */
    private int fireUnderperformingCrew(Long playerId, Long corporationId, String personalitySnapshot) {
        List<CorporateHiring> activeHires = corporateOfficeService.getActiveHires(corporationId);
        if (activeHires.isEmpty()) {
            return 0;
        }

        // Parse personality for firing threshold
        Map<String, Double> personality = parsePersonalitySnapshot(personalitySnapshot);
        double aggression = personality.getOrDefault("aggression", 0.5);
        double economicFocus = personality.getOrDefault("economicFocus", 0.5);

        // Adjust threshold based on personality
        double threshold = DEFAULT_FIRE_THRESHOLD;
        if (aggression > 0.6) {
            threshold = 2.0; // Aggressive AI has higher tolerance
        } else if (economicFocus > 0.6) {
            threshold = 4.0; // Economic AI has lower tolerance
        }

        int fired = 0;
        List<CorporateHiring> toFire = new ArrayList<>();

        for (CorporateHiring hire : activeHires) {
            double performance = hire.getPerformanceRating() != null ? hire.getPerformanceRating() : 5.0;
            if (performance < threshold) {
                toFire.add(hire);
            }
        }

        // Keep at least 1 hire per category (leadership + crew)
        int leadershipCount = 0;
        int crewCount = 0;
        for (CorporateHiring hire : activeHires) {
            if (hire.getPosition().isLeadership()) {
                leadershipCount++;
            } else {
                crewCount++;
            }
        }

        for (CorporateHiring hire : toFire) {
            // Check if firing would leave no hires in a category
            if (hire.getPosition().isLeadership() && leadershipCount <= 1) {
                continue;
            }
            if (!hire.getPosition().isLeadership() && crewCount <= 1) {
                continue;
            }

            try {
                corporateOfficeService.fireAiCharacter(hire.getId());
                fired++;
                if (hire.getPosition().isLeadership()) {
                    leadershipCount--;
                } else {
                    crewCount--;
                }
                String desc = "Fired AI from position " + hire.getPosition().getDisplayName() +
                        " (performance: " + hire.getPerformanceRating() +
                        ", threshold: " + threshold + ")";
                logAction(playerId, AiAssistantAction.ActionType.FIRE_CREW, hire.getId(),
                        AiAssistantAction.Result.SUCCESS, desc, personalitySnapshot);
            } catch (Exception e) {
                log.warn("Failed to fire hire {} at corporation {}: {}",
                        hire.getId(), corporationId, e.getMessage());
                logAction(playerId, AiAssistantAction.ActionType.FIRE_CREW, hire.getId(),
                        AiAssistantAction.Result.FAILED, "Failed: " + e.getMessage(), personalitySnapshot);
            }
        }

        return fired;
    }

    // ---- Personality Helpers ----

    /**
     * Get the AI personality for a player.
     */
    private DynamicAiPersonality getPersonality(Long playerId) {
        // Try to get from hex.Player first (for PlayerCloneService)
        // The model.Player ID maps to hex.Player ID
        try {
            // Look up the model.Player to get email and empire name
            com.yisy.modules.doramcu.model.players.Player jpaPlayer = playerRepository.findById(playerId)
                    .orElse(null);
            if (jpaPlayer == null) {
                log.warn("Player {} not found for personality lookup", playerId);
                return null;
            }

            // Create a hex.Player from the JPA Player for the clone service
            Player hexPlayer = new Player(
                    playerId.intValue(),
                    jpaPlayer.getEmail(),
                    jpaPlayer.getEmpireName(),
                    mapGovernmentType(jpaPlayer.getGovernmentType()),
                    mapEthos(jpaPlayer.getEthos())
            );
            return playerCloneService.getOrCreatePersonality(hexPlayer);
        } catch (Exception e) {
            log.warn("Could not get personality for player {}: {}", playerId, e.getMessage());
            return null;
        }
    }

    /**
     * Map model.Player.GovernmentType to hex.Player.GovernmentType.
     */
    private com.yisy.modules.doramcu.hex.Player.GovernmentType mapGovernmentType(
            com.yisy.modules.doramcu.model.players.Player.GovernmentType modelType) {
        if (modelType == null) {
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.DEMOCRACY;
        }
        try {
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.valueOf(modelType.name());
        } catch (IllegalArgumentException e) {
            return com.yisy.modules.doramcu.hex.Player.GovernmentType.DEMOCRACY;
        }
    }

    /**
     * Map model.Player.Ethos to hex.Player.Ethos.
     */
    private com.yisy.modules.doramcu.hex.Player.Ethos mapEthos(
            com.yisy.modules.doramcu.model.players.Player.Ethos modelEthos) {
        if (modelEthos == null) {
            return com.yisy.modules.doramcu.hex.Player.Ethos.MATERIALIST;
        }
        try {
            return com.yisy.modules.doramcu.hex.Player.Ethos.valueOf(modelEthos.name());
        } catch (IllegalArgumentException e) {
            return com.yisy.modules.doramcu.hex.Player.Ethos.MATERIALIST;
        }
    }

    /**
     * Convert personality to JSON snapshot string.
     */
    private String personalityToSnapshot(DynamicAiPersonality personality) {
        if (personality == null) {
            return null;
        }
        Map<String, Double> map = personality.toMap();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":")
              .append(String.format("%.2f", entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Parse personality snapshot JSON string back to a map.
     */
    private Map<String, Double> parsePersonalitySnapshot(String snapshot) {
        Map<String, Double> map = new HashMap<>();
        if (snapshot == null || snapshot.isEmpty()) {
            return map;
        }

        try {
            // Simple JSON parsing without external library
            String cleaned = snapshot.replaceAll("[{}\"]", "");
            for (String pair : cleaned.split(",")) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    map.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse personality snapshot: {}", snapshot);
        }

        return map;
    }

    // ---- Action Logging ----

    /**
     * Log an AI action for auditability.
     */
    private void logAction(Long playerId, AiAssistantAction.ActionType actionType,
                           Long targetId, AiAssistantAction.Result result,
                           String description, String personalitySnapshot) {
        try {
            AiAssistantAction action = new AiAssistantAction(
                    playerId, actionType, targetId, result, description, personalitySnapshot);
            actionRepository.save(action);
        } catch (Exception e) {
            log.error("Failed to log AI action for player {}: {}", playerId, e.getMessage());
        }
    }

    // ---- Query Methods ----

    /**
     * Get recent AI actions for a player.
     *
     * @param playerId the player ID
     * @param limit maximum number of actions to return
     * @return list of recent AI actions
     */
    @Transactional(readOnly = true)
    public List<AiAssistantAction> getRecentActions(Long playerId, int limit) {
        List<AiAssistantAction> actions = actionRepository.findByPlayerIdOrderByTimestampDesc(playerId);
        if (limit > 0 && actions.size() > limit) {
            return actions.subList(0, limit);
        }
        return actions;
    }

    /**
     * Check if the AI assistant is active for a player (VP mode with AI assistant).
     *
     * @param playerId the player ID
     * @return true if player is in VP mode and has an AI assistant
     */
    public boolean isAssistantActive(Long playerId) {
        Optional<CorporateRole> roleOpt = corporateRoleService.getPlayerRole(playerId);
        return roleOpt.isPresent() && roleOpt.get() == CorporateRole.VP;
    }
}
