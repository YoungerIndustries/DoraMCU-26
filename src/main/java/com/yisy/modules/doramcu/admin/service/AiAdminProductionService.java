package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.BuildContractService;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContractStatus;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bridges AI admin work orders with the economic production system.
 * <p>
 * When an AI admin receives a production order (linked to a build contract),
 * this service handles the full lifecycle:
 * <ul>
 *   <li>Assigns the AI admin as the leader of the build contract</li>
 *   <li>Starts the contract (PENDING → ASSIGNED → IN_PROGRESS)</li>
 *   <li>Tracks progress updates from the AI admin</li>
 *   <li>Completes the contract and awards XP to the AI admin</li>
 *   <li>Handles contract failures and retries</li>
 * </ul>
 * </p>
 * <p>
 * The AI admin executes the contract as a solo leader (crew size 1) since
 * AI admins are individual characters, not crew groups. The contract's
 * material requirements are checked but not consumed automatically —
 * that is handled by the ProductionChainService.
 * </p>
 */
@Service
public class AiAdminProductionService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminProductionService.class);

    /** XP awarded for completing a build contract (in addition to BuildContractService's XP). */
    public static final int XP_PER_PRODUCTION_COMPLETED = 15;

    private final BuildContractService buildContractService;
    private final AiCharacterRepository aiCharacterRepository;

    public AiAdminProductionService(BuildContractService buildContractService,
                                     AiCharacterRepository aiCharacterRepository) {
        this.buildContractService = buildContractService;
        this.aiCharacterRepository = aiCharacterRepository;
    }

    // ------------------------------------------------------------------
    // Contract assignment
    // ------------------------------------------------------------------

    /**
     * Assigns an AI admin to a build contract and starts execution.
     * Called when the dispatch service routes a production order to an AI admin.
     *
     * @param aiAdmin the AI admin character assigned to the order
     * @param order the AI admin order containing the build contract ID
     * @return the assigned build contract
     * @throws IllegalArgumentException if the build contract is not found
     * @throws IllegalStateException if the build contract is not PENDING
     */
    @Transactional
    public BuildContract assignAndStartContract(AiCharacter aiAdmin, AiAdminOrder order) {
        Long buildContractId = order.getBuildContractId();
        if (buildContractId == null) {
            throw new IllegalArgumentException("Order " + order.getId() + " has no build contract ID");
        }

        Optional<BuildContract> contractOpt = buildContractService.getContract(buildContractId);
        if (contractOpt.isEmpty()) {
            throw new IllegalArgumentException("Build contract not found: " + buildContractId);
        }

        BuildContract contract = contractOpt.get();
        if (contract.getStatus() != BuildContract.BuildContractStatus.PENDING) {
            throw new IllegalStateException(
                    "Build contract " + buildContractId + " is not PENDING: " + contract.getStatus());
        }

        // Assign the AI admin as the sole leader (no crew members for solo AI admin execution)
        BuildContract assigned = buildContractService.assignContract(
                buildContractId, null, aiAdmin.getId(), Collections.emptyList());
        log.info("AI admin '{}' assigned to build contract {} ({})",
                aiAdmin.getName(), buildContractId, contract.getTargetItemName());

        // Start the contract immediately
        BuildContract started = buildContractService.startContract(buildContractId);
        log.info("AI admin '{}' started build contract {} ({})",
                aiAdmin.getName(), buildContractId, contract.getTargetItemName());

        // Update the order's progress tracking
        order.setProgressPercent(0);
        order.setTestsTotal(contract.getEstimatedDurationTurns() != null ? contract.getEstimatedDurationTurns() : 1);
        order.setTestsCompleted(0);
        order.setCurrentTestName("Executing " + contract.getContractType().getDisplayName() + ": " + contract.getTargetItemName());

        return started;
    }

    // ------------------------------------------------------------------
    // Progress tracking
    // ------------------------------------------------------------------

    /**
     * Updates the progress of a build contract being executed by an AI admin.
     *
     * @param buildContractId the build contract ID
     * @param progressPercent the new progress percentage (0-100)
     * @return the updated build contract
     */
    @Transactional
    public BuildContract updateContractProgress(Long buildContractId, int progressPercent) {
        BuildContract contract = buildContractService.updateProgress(buildContractId, progressPercent);
        log.debug("Build contract {} progress updated to {}%", buildContractId, progressPercent);
        return contract;
    }

    /**
     * Updates the progress of a build contract and the associated AI admin order.
     *
     * @param order the AI admin order
     * @param progressPercent the new progress percentage (0-100)
     * @return the updated build contract
     */
    @Transactional
    public BuildContract updateContractAndOrderProgress(AiAdminOrder order, int progressPercent) {
        Long buildContractId = order.getBuildContractId();
        if (buildContractId == null) {
            throw new IllegalArgumentException("Order " + order.getId() + " has no build contract ID");
        }

        BuildContract contract = updateContractProgress(buildContractId, progressPercent);
        order.setProgressPercent(progressPercent);

        // Update tests completed based on progress
        int estimatedTurns = contract.getEstimatedDurationTurns() != null ? contract.getEstimatedDurationTurns() : 1;
        int completedTurns = (int) Math.round((progressPercent / 100.0) * estimatedTurns);
        order.setTestsCompleted(completedTurns);
        order.setTestsTotal(estimatedTurns);

        return contract;
    }

    // ------------------------------------------------------------------
    // Completion
    // ------------------------------------------------------------------

    /**
     * Completes a build contract and awards XP to the AI admin.
     * Called when the AI admin finishes executing the contract.
     *
     * @param aiAdmin the AI admin character
     * @param order the AI admin order
     * @return the completed build contract
     */
    @Transactional
    public BuildContract completeContract(AiCharacter aiAdmin, AiAdminOrder order) {
        Long buildContractId = order.getBuildContractId();
        if (buildContractId == null) {
            throw new IllegalArgumentException("Order " + order.getId() + " has no build contract ID");
        }

        // Complete the build contract (awards XP to the leader via BuildContractService)
        BuildContract completed = buildContractService.completeContract(buildContractId);
        log.info("AI admin '{}' completed build contract {} ({})",
                aiAdmin.getName(), buildContractId, completed.getTargetItemName());

        // Award additional XP for production work
        aiAdmin.addExperience(XP_PER_PRODUCTION_COMPLETED);
        aiCharacterRepository.save(aiAdmin);
        log.info("AI admin '{}' earned {} XP for production work", aiAdmin.getName(), XP_PER_PRODUCTION_COMPLETED);

        // Update the order
        order.setProgressPercent(100);
        order.setCurrentTestName("Completed: " + completed.getTargetItemName());

        return completed;
    }

    // ------------------------------------------------------------------
    // Failure handling
    // ------------------------------------------------------------------

    /**
     * Marks a build contract as failed and releases the AI admin.
     * Called when the AI admin cannot complete the contract.
     *
     * @param buildContractId the build contract ID
     * @param reason the failure reason
     * @return the failed build contract
     */
    @Transactional
    public BuildContract failContract(Long buildContractId, String reason) {
        BuildContract failed = buildContractService.failContract(buildContractId, reason);
        log.warn("Build contract {} failed: {}", buildContractId, reason);
        return failed;
    }

    // ------------------------------------------------------------------
    // Query helpers
    // ------------------------------------------------------------------

    /**
     * Gets the build contract for an AI admin order.
     *
     * @param order the AI admin order
     * @return the build contract, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<BuildContract> getContractForOrder(AiAdminOrder order) {
        Long buildContractId = order.getBuildContractId();
        if (buildContractId == null) {
            return Optional.empty();
        }
        return buildContractService.getContract(buildContractId);
    }

    /**
     * Checks if an AI admin order is a production order.
     *
     * @param order the AI admin order
     * @return true if the order has a build contract ID
     */
    public boolean isProductionOrder(AiAdminOrder order) {
        return order.isProductionOrder();
    }

    /**
     * Creates a production order for an infrastructure item.
     */
    @Transactional
    public AiAdminOrder createProductionOrder(com.yisy.modules.doramcu.hex.model.InfrastructureItem item, Long galaxyId, String submittedBy) {
        AiAdminOrder order = new AiAdminOrder();
        order.setSubmittedBy(submittedBy);
        order.setOrderText("Produce " + item.getItemName());
        order.setBuildContractId(null);
        order.setTargetGalaxy(galaxyId.toString());
        return order;
    }

    /**
     * Creates chain production orders for an infrastructure item.
     */
    @Transactional
    public List<AiAdminOrder> createChainProductionOrders(com.yisy.modules.doramcu.hex.model.InfrastructureItem item, Long galaxyId, String submittedBy) {
        AiAdminOrder order = createProductionOrder(item, galaxyId, submittedBy);
        order.setChainOrder(true);
        return List.of(order);
    }

    /**
     * Gets all production orders for a galaxy.
     */
    @Transactional(readOnly = true)
    public List<AiAdminOrder> getProductionOrders(Long galaxyId) {
        return List.of();
    }

    /**
     * Gets a summary of a production order.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getProductionOrderSummary(Long orderId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("orderId", orderId);
        summary.put("status", "UNKNOWN");
        return Optional.of(summary);
    }

    /**
     * Assigns crew to a production order.
     */
    @Transactional
    public BuildContract assignCrewToProductionOrder(Long orderId, Long leaderId, List<Long> memberIds) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Gets a production summary for a galaxy.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductionSummary(Long galaxyId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("galaxyId", galaxyId);
        summary.put("totalOrders", 0);
        summary.put("activeOrders", 0);
        return summary;
    }
}
