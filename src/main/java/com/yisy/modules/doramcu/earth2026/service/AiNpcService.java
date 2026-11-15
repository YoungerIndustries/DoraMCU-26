package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.model.WorkOrder;
import com.yisy.modules.doramcu.earth2026.repository.E26NpcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI-driven NPC service with hybrid rule-based + LLM decision making.
 * <p>
 * This service extends the basic E26NpcService with autonomous decision-making
 * capabilities. NPCs can create work orders, assign workers, and make strategic
 * decisions based on the state of the planetary economy.
 * </p>
 * <p>
 * Decision-making approach:
 * - Rule-based engine handles 80% of routine tasks (mining, crafting, restocking)
 * - LLM integration handles complex decisions (negotiations, strategy, unusual situations)
 * - Fallback to rule-based if LLM API is unavailable
 * </p>
 */
@Service
public class AiNpcService {

    private static final Logger log = LoggerFactory.getLogger(AiNpcService.class);

    private final E26NpcRepository npcRepository;
    private final WorkOrderService workOrderService;
    private final PlanetStructureService planetStructureService;

    @Value("${e26.ai.llm.enabled:false}")
    private boolean llmEnabled;

    @Value("${e26.ai.llm.endpoint:}")
    private String llmEndpoint;

    @Value("${e26.ai.llm.model:gpt-4}")
    private String llmModel;

    public AiNpcService(E26NpcRepository npcRepository,
                        WorkOrderService workOrderService,
                        PlanetStructureService planetStructureService) {
        this.npcRepository = npcRepository;
        this.workOrderService = workOrderService;
        this.planetStructureService = planetStructureService;
    }

    /**
     * Make a decision for an NPC based on the current situation.
     * <p>
     * Uses rule-based logic for routine tasks and LLM for complex decisions.
     * </p>
     *
     * @param npc the NPC making the decision
     * @param situation context about the current situation
     * @return the action the NPC should take
     */
    @Transactional
    public AiAction makeDecision(E26Npc npc, SituationContext situation) {
        if (npc == null) {
            log.warn("Cannot make decision for null NPC");
            return AiAction.fail("No NPC provided");
        }

        log.info("AI NPC {} making decision for situation: {}", npc.getName(), situation.getType());

        // Try rule-based first for routine tasks
        AiAction ruleBasedAction = tryRuleBasedDecision(npc, situation);
        if (ruleBasedAction != null) {
            logDecision(npc, situation, "RULE_BASED", ruleBasedAction);
            return ruleBasedAction;
        }

        // Fall back to LLM for complex decisions
        if (llmEnabled && !llmEndpoint.isEmpty()) {
            try {
                AiAction llmAction = tryLlmDecision(npc, situation);
                if (llmAction != null) {
                    logDecision(npc, situation, "LLM", llmAction);
                    return llmAction;
                }
            } catch (Exception e) {
                log.warn("LLM decision failed for NPC {}: {}", npc.getName(), e.getMessage());
            }
        }

        // Final fallback: default action
        AiAction defaultAction = getDefaultAction(npc, situation);
        logDecision(npc, situation, "DEFAULT", defaultAction);
        return defaultAction;
    }

    /**
     * Try rule-based decision for routine tasks.
     */
    private AiAction tryRuleBasedDecision(E26Npc npc, SituationContext situation) {
        switch (situation.getType()) {
            case MINE_NEEDS_ORE:
                return createMiningWorkOrder(npc, situation);

            case FACTORY_NEEDS_MATERIALS:
                return createProductionWorkOrder(npc, situation);

            case STORE_NEEDS_INVENTORY:
                return createRestockingWorkOrder(npc, situation);

            case WORK_ORDER_PENDING:
                return assignWorkerToWorkOrder(npc, situation);

            case WORK_ORDER_IN_PROGRESS:
                return completeWorkOrder(npc, situation);

            default:
                return null; // Let LLM handle complex situations
        }
    }

    /**
     * Try LLM-based decision for complex tasks.
     */
    private AiAction tryLlmDecision(E26Npc npc, SituationContext situation) {
        // Simulate LLM call (in production, this would call the actual LLM API)
        log.info("Calling LLM for NPC {} decision: {}", npc.getName(), situation.getType());

        // Build prompt for LLM
        String prompt = buildLlmPrompt(npc, situation);

        // In production, send prompt to LLM API and parse response
        // For now, return a simulated response
        return new AiAction(
                AiAction.ActionType.STRATEGIC,
                "LLM suggested action for " + situation.getType(),
                "Based on analysis, NPC should " + generateLlmSuggestion(npc, situation),
                situation.getStructureId(),
                LocalDateTime.now()
        );
    }

    /**
     * Create a mining work order when a mine needs ore.
     */
    private AiAction createMiningWorkOrder(E26Npc npc, SituationContext situation) {
        Long structureId = situation.getStructureId();
        if (structureId == null) {
            return AiAction.fail("No structure specified for mining");
        }

        Optional<PlanetStructure> structureOpt = planetStructureService.getStructureById(structureId);
        if (structureOpt.isEmpty() || structureOpt.get().getStructureType() != PlanetStructure.StructureType.MINE) {
            return AiAction.fail("Structure is not a mine");
        }

        PlanetStructure mine = structureOpt.get();
        WorkOrder workOrder = new WorkOrder();
        workOrder.setTitle("Mine " + mine.getName());
        workOrder.setDescription("Extract ore from " + mine.getName());
        workOrder.setRequestingNpcId(npc.getId());
        workOrder.setStructureId(structureId);
        workOrder.setRequiredResourcesJson("{}");
        workOrder.setOutputItemsJson("{\"ore\": 100}");
        workOrder.setPriority(5);

        WorkOrder created = workOrderService.createWorkOrder(workOrder);
        log.info("NPC {} created mining work order: {}", npc.getName(), created.getTitle());

        return new AiAction(
                AiAction.ActionType.CREATE_WORK_ORDER,
                "Created mining work order",
                "Work order " + created.getId() + " created for " + mine.getName(),
                structureId,
                LocalDateTime.now()
        );
    }

    /**
     * Create a production work order when a factory needs materials.
     */
    private AiAction createProductionWorkOrder(E26Npc npc, SituationContext situation) {
        Long structureId = situation.getStructureId();
        if (structureId == null) {
            return AiAction.fail("No structure specified for production");
        }

        Optional<PlanetStructure> structureOpt = planetStructureService.getStructureById(structureId);
        if (structureOpt.isEmpty() || structureOpt.get().getStructureType() != PlanetStructure.StructureType.FACTORY) {
            return AiAction.fail("Structure is not a factory");
        }

        PlanetStructure factory = structureOpt.get();
        WorkOrder workOrder = new WorkOrder();
        workOrder.setTitle("Produce at " + factory.getName());
        workOrder.setDescription("Manufacture items at " + factory.getName());
        workOrder.setRequestingNpcId(npc.getId());
        workOrder.setStructureId(structureId);
        workOrder.setRequiredResourcesJson("{\"ore\": 50, \"materials\": 25}");
        workOrder.setOutputItemsJson("{\"steel_beams\": 20, \"components\": 10}");
        workOrder.setPriority(7);

        WorkOrder created = workOrderService.createWorkOrder(workOrder);
        log.info("NPC {} created production work order: {}", npc.getName(), created.getTitle());

        return new AiAction(
                AiAction.ActionType.CREATE_WORK_ORDER,
                "Created production work order",
                "Work order " + created.getId() + " created for " + factory.getName(),
                structureId,
                LocalDateTime.now()
        );
    }

    /**
     * Create a restocking work order when a store needs inventory.
     */
    private AiAction createRestockingWorkOrder(E26Npc npc, SituationContext situation) {
        Long structureId = situation.getStructureId();
        if (structureId == null) {
            return AiAction.fail("No structure specified for restocking");
        }

        Optional<PlanetStructure> structureOpt = planetStructureService.getStructureById(structureId);
        if (structureOpt.isEmpty() || structureOpt.get().getStructureType() != PlanetStructure.StructureType.EQUIPMENT_STORE) {
            return AiAction.fail("Structure is not an equipment store");
        }

        PlanetStructure store = structureOpt.get();
        WorkOrder workOrder = new WorkOrder();
        workOrder.setTitle("Restock " + store.getName());
        workOrder.setDescription("Restock inventory at " + store.getName());
        workOrder.setRequestingNpcId(npc.getId());
        workOrder.setStructureId(structureId);
        workOrder.setRequiredResourcesJson("{\"items\": 50}");
        workOrder.setOutputItemsJson("{\"inventory\": 50}");
        workOrder.setPriority(3);

        WorkOrder created = workOrderService.createWorkOrder(workOrder);
        log.info("NPC {} created restocking work order: {}", npc.getName(), created.getTitle());

        return new AiAction(
                AiAction.ActionType.CREATE_WORK_ORDER,
                "Created restocking work order",
                "Work order " + created.getId() + " created for " + store.getName(),
                structureId,
                LocalDateTime.now()
        );
    }

    /**
     * Assign a worker to a pending work order.
     */
    private AiAction assignWorkerToWorkOrder(E26Npc npc, SituationContext situation) {
        Long workOrderId = situation.getWorkOrderId();
        if (workOrderId == null) {
            return AiAction.fail("No work order specified");
        }

        // Find available workers
        List<E26Npc> workers = npcRepository.findByOccupationAndActive("WORKER", true);
        if (workers.isEmpty()) {
            return AiAction.fail("No available workers");
        }

        // Assign a random worker
        E26Npc worker = workers.get(new Random().nextInt(workers.size()));
        workOrderService.assignWorker(workOrderId, worker.getId());

        log.info("NPC {} assigned worker {} to work order {}", npc.getName(), worker.getName(), workOrderId);

        return new AiAction(
                AiAction.ActionType.ASSIGN_WORKER,
                "Assigned worker to work order",
                "Worker " + worker.getName() + " assigned to work order " + workOrderId,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Complete a work order that's in progress.
     */
    private AiAction completeWorkOrder(E26Npc npc, SituationContext situation) {
        Long workOrderId = situation.getWorkOrderId();
        if (workOrderId == null) {
            return AiAction.fail("No work order specified");
        }

        workOrderService.completeWorkOrder(workOrderId);
        log.info("NPC {} completed work order {}", npc.getName(), workOrderId);

        return new AiAction(
                AiAction.ActionType.COMPLETE_WORK_ORDER,
                "Completed work order",
                "Work order " + workOrderId + " completed",
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Get default action for an NPC.
     */
    private AiAction getDefaultAction(E26Npc npc, SituationContext situation) {
        return new AiAction(
                AiAction.ActionType.WAIT,
                "No action taken",
                "NPC " + npc.getName() + " has no immediate action for " + situation.getType(),
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Build LLM prompt for decision making.
     */
    private String buildLlmPrompt(E26Npc npc, SituationContext situation) {
        return String.format(
                "You are %s, a %s in Earth 2026. Your occupation is %s. " +
                "Current situation: %s. %s " +
                "What action should you take? Respond with a JSON object containing: action_type, description, priority.",
                npc.getName(),
                npc.getNpcType(),
                npc.getOccupation(),
                situation.getType(),
                situation.getDescription() != null ? situation.getDescription() : ""
        );
    }

    /**
     * Generate LLM suggestion (simulated).
     */
    private String generateLlmSuggestion(E26Npc npc, SituationContext situation) {
        Random random = new Random();
        String[] suggestions = {
                "investigate the situation further",
                "create a work order to address the need",
                "assign available workers to pending tasks",
                "negotiate with other NPCs for resources",
                "prioritize high-value tasks first"
        };
        return suggestions[random.nextInt(suggestions.length)];
    }

    /**
     * Log an AI decision for debugging and transparency.
     */
    private void logDecision(E26Npc npc, SituationContext situation, String method, AiAction action) {
        log.info("AI Decision: NPC={}, Situation={}, Method={}, Action={}",
                npc.getName(), situation.getType(), method, action.getDescription());
    }

    /**
     * Context about the current situation for AI decision making.
     */
    public static class SituationContext {
        public enum SituationType {
            MINE_NEEDS_ORE,
            FACTORY_NEEDS_MATERIALS,
            STORE_NEEDS_INVENTORY,
            WORK_ORDER_PENDING,
            WORK_ORDER_IN_PROGRESS,
            STRATEGIC_PLANNING,
            NEGOTIATION,
            EMERGENCY,
            UNKNOWN
        }

        private final SituationType type;
        private final String description;
        private final Long structureId;
        private final Long workOrderId;
        private final Map<String, Object> additionalData;

        public SituationContext(SituationType type, String description, Long structureId,
                                Long workOrderId, Map<String, Object> additionalData) {
            this.type = type;
            this.description = description;
            this.structureId = structureId;
            this.workOrderId = workOrderId;
            this.additionalData = additionalData != null ? additionalData : new HashMap<>();
        }

        public static SituationContext mineNeedsOre(Long structureId) {
            return new SituationContext(SituationType.MINE_NEEDS_ORE,
                    "Mine needs ore extraction", structureId, null, null);
        }

        public static SituationContext factoryNeedsMaterials(Long structureId) {
            return new SituationContext(SituationType.FACTORY_NEEDS_MATERIALS,
                    "Factory needs materials for production", structureId, null, null);
        }

        public static SituationContext storeNeedsInventory(Long structureId) {
            return new SituationContext(SituationType.STORE_NEEDS_INVENTORY,
                    "Store needs inventory restocking", structureId, null, null);
        }

        public static SituationContext workOrderPending(Long workOrderId) {
            return new SituationContext(SituationType.WORK_ORDER_PENDING,
                    "Work order is pending assignment", null, workOrderId, null);
        }

        public static SituationContext workOrderInProgress(Long workOrderId) {
            return new SituationContext(SituationType.WORK_ORDER_IN_PROGRESS,
                    "Work order is in progress", null, workOrderId, null);
        }

        public SituationType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public Long getStructureId() {
            return structureId;
        }

        public Long getWorkOrderId() {
            return workOrderId;
        }

        public Map<String, Object> getAdditionalData() {
            return additionalData;
        }
    }

    /**
     * Action taken by an AI NPC.
     */
    public static class AiAction {
        public enum ActionType {
            CREATE_WORK_ORDER,
            ASSIGN_WORKER,
            COMPLETE_WORK_ORDER,
            FAIL_WORK_ORDER,
            STRATEGIC,
            NEGOTIATION,
            WAIT,
            FAIL
        }

        private final ActionType type;
        private final String title;
        private final String description;
        private final Long structureId;
        private final LocalDateTime timestamp;

        public AiAction(ActionType type, String title, String description,
                        Long structureId, LocalDateTime timestamp) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.structureId = structureId;
            this.timestamp = timestamp;
        }

        public static AiAction fail(String reason) {
            return new AiAction(ActionType.FAIL, "Failed", reason, null, LocalDateTime.now());
        }

        public ActionType getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Long getStructureId() {
            return structureId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "AiAction{type=" + type + ", title='" + title + "'}";
        }
    }
}
