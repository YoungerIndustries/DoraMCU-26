package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.BuildContractService;
import com.yisy.modules.doramcu.hex.ProductionChainService;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.BuildContract.BuildContractStatus;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /** Default number of retries for failed production orders. */
    public static final int DEFAULT_MAX_RETRIES = 3;

    private final BuildContractService buildContractService;
    private final ProductionChainService productionChainService;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminOrderRepository orderRepository;

    public AiAdminProductionService(BuildContractService buildContractService,
                                     ProductionChainService productionChainService,
                                     AiCharacterRepository aiCharacterRepository,
                                     AiAdminOrderRepository orderRepository) {
        this.buildContractService = buildContractService;
        this.productionChainService = productionChainService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.orderRepository = orderRepository;
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

    // ------------------------------------------------------------------
    // Production order creation
    // ------------------------------------------------------------------

    /**
     * Creates a production order for an infrastructure item.
     * Creates a build contract for the item, links it to the order,
     * and persists the order to the database.
     *
     * @param item the infrastructure item to produce
     * @param galaxyId the galaxy where production takes place
     * @param submittedBy the submitter identifier (e.g., "GalaxyOverseer:Alpha")
     * @return the saved production order with build contract ID
     */
    @Transactional
    public AiAdminOrder createProductionOrder(InfrastructureItem item, Long galaxyId, String submittedBy) {
        // Create the build contract for this infrastructure item
        BuildContract contract = buildContractService.createContract(item, galaxyId);
        log.info("Created build contract {} for production order: {} in galaxy {}",
                contract.getId(), item.getItemName(), galaxyId);

        // Create and persist the order
        AiAdminOrder order = new AiAdminOrder();
        order.setSubmittedBy(submittedBy);
        order.setOrderText(String.format("Produce %s (%s) — Build contract %d",
                item.getItemName(), item.getItemType().getDisplayName(), contract.getId()));
        order.setBuildContractId(contract.getId());
        order.setTargetGalaxy(galaxyId.toString());
        order.setMaxRetries(DEFAULT_MAX_RETRIES);
        order.setChainOrder(false);

        AiAdminOrder saved = orderRepository.save(order);
        log.info("Created production order {} for {} (contract {})", saved.getId(), item.getItemName(), contract.getId());
        return saved;
    }

    /**
     * Creates a production order with an explicit contract type.
     * Useful for creating MINE, PROCESS, or ASSEMBLE orders directly.
     *
     * @param contractType the contract type (MINE, PROCESS, ASSEMBLE, CONSTRUCT)
     * @param targetItemType the type of target item
     * @param targetItemName the name of target item
     * @param materialRequirementsJson JSON string of material requirements
     * @param estimatedDurationTurns estimated turns to complete
     * @param galaxyId the galaxy where production takes place
     * @param submittedBy the submitter identifier
     * @return the saved production order with build contract ID
     */
    @Transactional
    public AiAdminOrder createProductionOrder(BuildContract.ContractType contractType,
                                               String targetItemType,
                                               String targetItemName,
                                               String materialRequirementsJson,
                                               int estimatedDurationTurns,
                                               Long galaxyId,
                                               String submittedBy) {
        BuildContract contract = buildContractService.createContract(
                contractType, targetItemType, targetItemName,
                materialRequirementsJson, estimatedDurationTurns, galaxyId);

        AiAdminOrder order = new AiAdminOrder();
        order.setSubmittedBy(submittedBy);
        order.setOrderText(String.format("%s %s — Build contract %d",
                contractType.getDisplayName(), targetItemName, contract.getId()));
        order.setBuildContractId(contract.getId());
        order.setTargetGalaxy(galaxyId.toString());
        order.setMaxRetries(DEFAULT_MAX_RETRIES);
        order.setChainOrder(false);

        AiAdminOrder saved = orderRepository.save(order);
        log.info("Created production order {} for {} ({}) contract {}",
                saved.getId(), contractType, targetItemName, contract.getId());
        return saved;
    }

    /**
     * Creates chain production orders for an infrastructure item.
     * Creates a full 4-phase production chain (MINE → PROCESS → ASSEMBLE → CONSTRUCT),
     * generates all build contracts with dependency tracking, and creates a single
     * order that represents the entire chain.
     *
     * @param item the infrastructure item to produce
     * @param galaxyId the galaxy where production takes place
     * @param submittedBy the submitter identifier
     * @return list containing the chain production order
     */
    @Transactional
    public List<AiAdminOrder> createChainProductionOrders(InfrastructureItem item, Long galaxyId, String submittedBy) {
        // Create the production chain
        ProductionChainTracking chain = productionChainService.createProductionChain(item, galaxyId);
        log.info("Created production chain {} for {} in galaxy {}", chain.getId(), item.getItemName(), galaxyId);

        // Generate all contracts for the chain (MINE, PROCESS, ASSEMBLE, CONSTRUCT)
        ProductionChainTracking generated = productionChainService.generateContracts(chain);
        log.info("Generated {} contracts for chain {}", generated.getTotalContracts(), chain.getId());

        // Create a single order that represents the entire chain
        AiAdminOrder order = new AiAdminOrder();
        order.setSubmittedBy(submittedBy);
        order.setOrderText(String.format("Full production chain for %s (%s) — Chain %d with %d contracts",
                item.getItemName(), item.getItemType().getDisplayName(),
                chain.getId(), generated.getTotalContracts()));
        order.setProductionChainId(chain.getId());
        order.setBuildContractId(null); // Chain order — no single contract
        order.setTargetGalaxy(galaxyId.toString());
        order.setMaxRetries(DEFAULT_MAX_RETRIES);
        order.setChainOrder(true);
        order.setTestsTotal(generated.getTotalContracts());
        order.setTestsCompleted(0);

        AiAdminOrder saved = orderRepository.save(order);
        log.info("Created chain production order {} for {} (chain {}, {} contracts)",
                saved.getId(), item.getItemName(), chain.getId(), generated.getTotalContracts());

        return List.of(saved);
    }

    /**
     * Creates chain production orders for multiple infrastructure items.
     * Useful when the Economic Manager needs to produce multiple facilities at once.
     *
     * @param items the infrastructure items to produce
     * @param galaxyId the galaxy where production takes place
     * @param submittedBy the submitter identifier
     * @return list of all created chain production orders
     */
    @Transactional
    public List<AiAdminOrder> createChainProductionOrders(List<InfrastructureItem> items, Long galaxyId, String submittedBy) {
        List<AiAdminOrder> allOrders = new ArrayList<>();
        for (InfrastructureItem item : items) {
            allOrders.addAll(createChainProductionOrders(item, galaxyId, submittedBy));
        }
        log.info("Created {} chain production orders for {} items in galaxy {}",
                allOrders.size(), items.size(), galaxyId);
        return allOrders;
    }

    /**
     * Gets all production orders for a galaxy.
     * Queries orders that target the given galaxy and are production-related
     * (contain facility, station, or production keywords in the order text).
     *
     * @param galaxyId the galaxy ID
     * @return list of production orders for the galaxy
     */
    @Transactional(readOnly = true)
    public List<AiAdminOrder> getProductionOrders(Long galaxyId) {
        String galaxyPrefix = "GalaxyOverseer:";
        List<AiAdminOrder> allOrders = orderRepository.findBySubmittedByContaining(galaxyPrefix);
        String galaxyIdStr = galaxyId.toString();
        return allOrders.stream()
                .filter(order -> order.getTargetGalaxy() != null &&
                        order.getTargetGalaxy().equals(galaxyIdStr))
                .collect(Collectors.toList());
    }

    /**
     * Gets a detailed summary of a production order including build contract status.
     *
     * @param orderId the order ID
     * @return summary map with order details, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getProductionOrderSummary(Long orderId) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }
        AiAdminOrder order = orderOpt.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("orderId", orderId);
        summary.put("status", order.getStatus().name());
        summary.put("orderText", order.getOrderText());
        summary.put("submittedBy", order.getSubmittedBy());
        summary.put("progressPercent", order.getProgressPercent());
        summary.put("createdAt", order.getCreatedAt());
        summary.put("completedAt", order.getCompletedAt());
        summary.put("assignedAiAdminId", order.getAssignedAiAdminId());
        if (order.getBuildContractId() != null) {
            buildContractService.getContract(order.getBuildContractId()).ifPresent(contract -> {
                summary.put("buildContractStatus", contract.getStatus().name());
                summary.put("buildContractItem", contract.getTargetItemName());
                summary.put("buildContractProgress", contract.getProgressPercent());
            });
        }
        return Optional.of(summary);
    }

    // ------------------------------------------------------------------
    // Order execution
    // ------------------------------------------------------------------

    /**
     * Assigns crew to a production order and starts the build contract.
     * For chain orders, assigns the first ready contract in the chain.
     *
     * @param orderId the production order ID
     * @param leaderId the leader AI character ID
     * @param memberIds list of member AI character IDs
     * @return the assigned build contract
     */
    @Transactional
    public BuildContract assignCrewToProductionOrder(Long orderId, Long leaderId, List<Long> memberIds) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        AiAdminOrder order = orderOpt.get();

        // For chain orders, find the first ready contract
        if (order.isChainOrder() && order.getProductionChainId() != null) {
            List<BuildContract> readyContracts = productionChainService.getReadyContracts(order.getProductionChainId());
            if (readyContracts.isEmpty()) {
                throw new IllegalStateException("No ready contracts in chain " + order.getProductionChainId() +
                        " — prerequisites not yet completed");
            }
            BuildContract firstReady = readyContracts.get(0);
            BuildContract assigned = buildContractService.assignContract(
                    firstReady.getId(), null, leaderId, memberIds != null ? memberIds : Collections.emptyList());
            order.setBuildContractId(firstReady.getId());
            orderRepository.save(order);
            log.info("Assigned chain order {} to contract {} (leader {})", orderId, firstReady.getId(), leaderId);
            return assigned;
        }

        // For single-contract orders
        Long buildContractId = order.getBuildContractId();
        if (buildContractId == null) {
            throw new IllegalArgumentException("Order " + orderId + " has no build contract ID");
        }
        BuildContract assigned = buildContractService.assignContract(
                buildContractId, null, leaderId, memberIds != null ? memberIds : Collections.emptyList());
        log.info("Assigned order {} to contract {} (leader {})", orderId, buildContractId, leaderId);
        return assigned;
    }

    /**
     * Executes a production order end-to-end: assigns the AI admin, starts the contract,
     * and simulates progress to completion.
     * For chain orders, executes each contract in dependency order.
     *
     * @param aiAdmin the AI admin character executing the order
     * @param order the production order to execute
     * @return the final build contract (or last contract in chain)
     */
    @Transactional
    public BuildContract executeProductionOrder(AiCharacter aiAdmin, AiAdminOrder order) {
        if (order.isChainOrder() && order.getProductionChainId() != null) {
            return executeChainOrder(aiAdmin, order);
        } else {
            return executeSingleContractOrder(aiAdmin, order);
        }
    }

    /**
     * Executes a single-contract production order.
     */
    private BuildContract executeSingleContractOrder(AiCharacter aiAdmin, AiAdminOrder order) {
        log.info("AI admin '{}' executing single-contract order {} (contract {})",
                aiAdmin.getName(), order.getId(), order.getBuildContractId());

        // Assign and start the contract
        BuildContract started = assignAndStartContract(aiAdmin, order);

        // Simulate progress to completion
        BuildContract completed = completeContract(aiAdmin, order);
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("AI admin '{}' completed single-contract order {} (contract {})",
                aiAdmin.getName(), order.getId(), order.getBuildContractId());
        return completed;
    }

    /**
     * Executes a chain production order by processing contracts in dependency order.
     */
    private BuildContract executeChainOrder(AiCharacter aiAdmin, AiAdminOrder order) {
        Long chainId = order.getProductionChainId();
        log.info("AI admin '{}' executing chain order {} (chain {})",
                aiAdmin.getName(), order.getId(), chainId);

        int totalContracts = order.getTestsTotal() != null ? order.getTestsTotal() : 0;
        int completedCount = 0;
        BuildContract lastContract = null;

        // Execute contracts in dependency order
        while (completedCount < totalContracts) {
            List<BuildContract> readyContracts = productionChainService.getReadyContracts(chainId);
            if (readyContracts.isEmpty()) {
                // Check if all contracts are already completed
                boolean allDone = true;
                for (BuildContractStatus status : BuildContractStatus.values()) {
                    List<BuildContract> contracts = buildContractService.getContractsByStatus(status);
                    for (BuildContract c : contracts) {
                        if (chainId.equals(c.getProductionChainId()) &&
                                c.getStatus() != BuildContractStatus.COMPLETED &&
                                c.getStatus() != BuildContractStatus.FAILED) {
                            allDone = false;
                            break;
                        }
                    }
                }
                if (allDone) break;
                log.warn("No ready contracts in chain {} but not all completed — possible deadlock", chainId);
                break;
            }

            // Execute the first ready contract
            BuildContract ready = readyContracts.get(0);
            order.setBuildContractId(ready.getId());
            orderRepository.save(order);

            // Assign and start
            buildContractService.assignContract(ready.getId(), null, aiAdmin.getId(), Collections.emptyList());
            buildContractService.startContract(ready.getId());

            // Complete the contract
            buildContractService.completeContract(ready.getId());
            lastContract = ready;
            completedCount++;

            // Award XP for each contract
            aiAdmin.addExperience(XP_PER_PRODUCTION_COMPLETED);
            aiCharacterRepository.save(aiAdmin);

            // Update progress
            int progress = (int) ((double) completedCount / totalContracts * 100);
            order.setProgressPercent(progress);
            order.setTestsCompleted(completedCount);
            order.setCurrentTestName("Chain phase " + completedCount + "/" + totalContracts + ": " + ready.getTargetItemName());
            orderRepository.save(order);

            log.info("AI admin '{}' completed chain contract {}/{}: {} ({})",
                    aiAdmin.getName(), completedCount, totalContracts,
                    ready.getTargetItemName(), ready.getContractType());
        }

        // Update chain progress
        productionChainService.updateChainProgress(chainId);

        // Mark order as completed
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setProgressPercent(100);
        order.setCompletedAt(LocalDateTime.now());
        order.setCurrentTestName("Chain completed: " + completedCount + "/" + totalContracts + " contracts");
        orderRepository.save(order);

        log.info("AI admin '{}' completed chain order {} — {}/{} contracts",
                aiAdmin.getName(), order.getId(), completedCount, totalContracts);

        return lastContract != null ? lastContract : null;
    }

    // ------------------------------------------------------------------
    // Retry logic
    // ------------------------------------------------------------------

    /**
     * Retries a failed production order.
     * If the order has not exceeded max retries, resets the status to PENDING
     * and increments the retry count.
     *
     * @param orderId the order ID to retry
     * @return the retried order
     * @throws IllegalStateException if max retries exceeded
     */
    @Transactional
    public AiAdminOrder retryFailedOrder(Long orderId) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        AiAdminOrder order = orderOpt.get();

        if (order.getStatus() != AiAdminOrder.OrderStatus.FAILED) {
            throw new IllegalStateException("Order " + orderId + " is not FAILED: " + order.getStatus());
        }

        if (order.getRetryCount() >= order.getMaxRetries()) {
            throw new IllegalStateException("Order " + orderId + " has exceeded max retries (" +
                    order.getMaxRetries() + ")");
        }

        // Reset the order for retry
        order.setRetryCount(order.getRetryCount() + 1);
        order.setStatus(AiAdminOrder.OrderStatus.PENDING);
        order.setProgressPercent(0);
        order.setCompletedAt(null);
        order.setCurrentTestName("Retry " + order.getRetryCount() + ": " + order.getOrderText());

        AiAdminOrder saved = orderRepository.save(order);
        log.info("Retried order {} (attempt {}/{})", orderId, saved.getRetryCount(), saved.getMaxRetries());
        return saved;
    }

    /**
     * Retries all failed production orders for a galaxy that have not exceeded max retries.
     *
     * @param galaxyId the galaxy ID
     * @return list of retried orders
     */
    @Transactional
    public List<AiAdminOrder> retryFailedOrdersForGalaxy(Long galaxyId) {
        List<AiAdminOrder> allOrders = getProductionOrders(galaxyId);
        List<AiAdminOrder> retried = new ArrayList<>();

        for (AiAdminOrder order : allOrders) {
            if (order.getStatus() == AiAdminOrder.OrderStatus.FAILED &&
                    order.getRetryCount() < order.getMaxRetries()) {
                try {
                    retried.add(retryFailedOrder(order.getId()));
                } catch (IllegalStateException e) {
                    log.warn("Skipping retry for order {}: {}", order.getId(), e.getMessage());
                }
            }
        }

        log.info("Retried {} failed orders for galaxy {}", retried.size(), galaxyId);
        return retried;
    }

    /**
     * Marks an order as permanently failed after exceeding max retries.
     *
     * @param orderId the order ID
     */
    @Transactional
    public void markPermanentlyFailed(Long orderId) {
        Optional<AiAdminOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        AiAdminOrder order = orderOpt.get();
        order.setStatus(AiAdminOrder.OrderStatus.FAILED);
        order.setCompletedAt(LocalDateTime.now());
        order.setCurrentTestName("Permanently failed after " + order.getRetryCount() + " retries");
        orderRepository.save(order);
        log.warn("Order {} permanently failed after {} retries", orderId, order.getRetryCount());
    }

    /**
     * Gets a production summary for a galaxy including order counts by status.
     *
     * @param galaxyId the galaxy ID
     * @return summary map with production statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductionSummary(Long galaxyId) {
        List<AiAdminOrder> productionOrders = getProductionOrders(galaxyId);
        Map<String, Object> summary = new HashMap<>();
        summary.put("galaxyId", galaxyId);
        summary.put("totalOrders", productionOrders.size());
        summary.put("activeOrders", productionOrders.stream()
                .filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.RUNNING).count());
        summary.put("pendingOrders", productionOrders.stream()
                .filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.PENDING).count());
        summary.put("completedOrders", productionOrders.stream()
                .filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.COMPLETED).count());
        summary.put("failedOrders", productionOrders.stream()
                .filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.FAILED).count());
        return summary;
    }
}
