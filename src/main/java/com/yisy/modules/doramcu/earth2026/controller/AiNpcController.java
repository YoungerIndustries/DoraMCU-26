package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import com.yisy.modules.doramcu.earth2026.repository.E26NpcRepository;
import com.yisy.modules.doramcu.earth2026.service.AiNpcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for AI NPC decisions.
 */
@RestController
@RequestMapping("/api/earth2026/ai-npcs")
public class AiNpcController {

    private final AiNpcService aiNpcService;
    private final E26NpcRepository npcRepository;

    public AiNpcController(AiNpcService aiNpcService, E26NpcRepository npcRepository) {
        this.aiNpcService = aiNpcService;
        this.npcRepository = npcRepository;
    }

    /**
     * Get all NPCs.
     */
    @GetMapping
    public List<E26Npc> getAllNpcs() {
        return npcRepository.findAll();
    }

    /**
     * Get an NPC by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<E26Npc> getNpcById(@PathVariable Long id) {
        return npcRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Trigger an AI decision for an NPC.
     */
    @PostMapping("/{npcId}/decide")
    public ResponseEntity<AiAction> triggerDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Object> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        String situationType = (String) body.get("situationType");
        String description = (String) body.get("description");
        Long structureId = body.get("structureId") != null ?
                Long.valueOf(body.get("structureId").toString()) : null;
        Long workOrderId = body.get("workOrderId") != null ?
                Long.valueOf(body.get("workOrderId").toString()) : null;

        SituationContext.SituationType type = parseSituationType(situationType);
        SituationContext situation = new SituationContext(type, description, structureId, workOrderId, null);

        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Trigger a mining decision for an NPC.
     */
    @PostMapping("/{npcId}/decide/mine")
    public ResponseEntity<AiAction> triggerMiningDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Long> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        Long structureId = body.get("structureId");
        if (structureId == null) {
            return ResponseEntity.badRequest().build();
        }

        SituationContext situation = SituationContext.mineNeedsOre(structureId);
        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Trigger a production decision for an NPC.
     */
    @PostMapping("/{npcId}/decide/produce")
    public ResponseEntity<AiAction> triggerProductionDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Long> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        Long structureId = body.get("structureId");
        if (structureId == null) {
            return ResponseEntity.badRequest().build();
        }

        SituationContext situation = SituationContext.factoryNeedsMaterials(structureId);
        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Trigger a restocking decision for an NPC.
     */
    @PostMapping("/{npcId}/decide/restock")
    public ResponseEntity<AiAction> triggerRestockingDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Long> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        Long structureId = body.get("structureId");
        if (structureId == null) {
            return ResponseEntity.badRequest().build();
        }

        SituationContext situation = SituationContext.storeNeedsInventory(structureId);
        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Trigger a work order assignment decision for an NPC.
     */
    @PostMapping("/{npcId}/decide/assign")
    public ResponseEntity<AiAction> triggerAssignmentDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Long> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        Long workOrderId = body.get("workOrderId");
        if (workOrderId == null) {
            return ResponseEntity.badRequest().build();
        }

        SituationContext situation = SituationContext.workOrderPending(workOrderId);
        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Trigger a work order completion decision for an NPC.
     */
    @PostMapping("/{npcId}/decide/complete")
    public ResponseEntity<AiAction> triggerCompletionDecision(
            @PathVariable Long npcId,
            @RequestBody Map<String, Long> body) {
        E26Npc npc = npcRepository.findById(npcId).orElse(null);
        if (npc == null) {
            return ResponseEntity.notFound().build();
        }

        Long workOrderId = body.get("workOrderId");
        if (workOrderId == null) {
            return ResponseEntity.badRequest().build();
        }

        SituationContext situation = SituationContext.workOrderInProgress(workOrderId);
        AiAction action = aiNpcService.makeDecision(npc, situation);
        return ResponseEntity.ok(action);
    }

    /**
     * Parse situation type from string.
     */
    private SituationContext.SituationType parseSituationType(String type) {
        if (type == null) {
            return SituationContext.SituationType.UNKNOWN;
        }
        try {
            return SituationContext.SituationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SituationContext.SituationType.UNKNOWN;
        }
    }
}
