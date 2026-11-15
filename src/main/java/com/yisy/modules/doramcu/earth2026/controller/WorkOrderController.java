package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.WorkOrder;
import com.yisy.modules.doramcu.earth2026.service.WorkOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for work orders in the Earth 2026 module.
 */
@RestController
@RequestMapping("/api/earth2026/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * Get all work orders.
     */
    @GetMapping
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderService.getAllWorkOrders();
    }

    /**
     * Get a work order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        return workOrderService.getWorkOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get work orders by status.
     */
    @GetMapping("/status/{status}")
    public List<WorkOrder> getWorkOrdersByStatus(@PathVariable WorkOrder.WorkOrderStatus status) {
        return workOrderService.getWorkOrdersByStatus(status);
    }

    /**
     * Get work orders by requesting NPC.
     */
    @GetMapping("/requesting-npc/{npcId}")
    public List<WorkOrder> getWorkOrdersByRequestingNpc(@PathVariable Long npcId) {
        return workOrderService.getWorkOrdersByRequestingNpc(npcId);
    }

    /**
     * Get work orders by assigned worker.
     */
    @GetMapping("/worker/{workerId}")
    public List<WorkOrder> getWorkOrdersByAssignedWorker(@PathVariable Long workerId) {
        return workOrderService.getWorkOrdersByAssignedWorker(workerId);
    }

    /**
     * Get work orders by structure.
     */
    @GetMapping("/structure/{structureId}")
    public List<WorkOrder> getWorkOrdersByStructure(@PathVariable Long structureId) {
        return workOrderService.getWorkOrdersByStructure(structureId);
    }

    /**
     * Get pending work orders ordered by priority.
     */
    @GetMapping("/pending")
    public List<WorkOrder> getPendingWorkOrders() {
        return workOrderService.getPendingWorkOrders();
    }

    /**
     * Get work orders by status and structure.
     */
    @GetMapping("/status/{status}/structure/{structureId}")
    public List<WorkOrder> getWorkOrdersByStatusAndStructure(
            @PathVariable WorkOrder.WorkOrderStatus status,
            @PathVariable Long structureId) {
        return workOrderService.getWorkOrdersByStatusAndStructure(status, structureId);
    }

    /**
     * Get work order statistics.
     */
    @GetMapping("/stats")
    public Map<String, Long> getWorkOrderStats() {
        return Map.of(
                "pending", workOrderService.countByStatus(WorkOrder.WorkOrderStatus.PENDING),
                "assigned", workOrderService.countByStatus(WorkOrder.WorkOrderStatus.ASSIGNED),
                "in_progress", workOrderService.countByStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS),
                "completed", workOrderService.countByStatus(WorkOrder.WorkOrderStatus.COMPLETED),
                "failed", workOrderService.countByStatus(WorkOrder.WorkOrderStatus.FAILED)
        );
    }

    /**
     * Create a new work order.
     */
    @PostMapping
    public WorkOrder createWorkOrder(@RequestBody WorkOrder workOrder) {
        return workOrderService.createWorkOrder(workOrder);
    }

    /**
     * Assign a worker to a work order.
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<WorkOrder> assignWorker(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long workerId = body.get("workerId");
        if (workerId == null) {
            return ResponseEntity.badRequest().build();
        }
        return workOrderService.assignWorker(id, workerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Start working on a work order.
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<WorkOrder> startWorkOrder(@PathVariable Long id) {
        return workOrderService.startWorkOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Complete a work order.
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<WorkOrder> completeWorkOrder(@PathVariable Long id) {
        return workOrderService.completeWorkOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Fail a work order.
     */
    @PatchMapping("/{id}/fail")
    public ResponseEntity<WorkOrder> failWorkOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Unknown error");
        return workOrderService.failWorkOrder(id, reason)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the priority of a work order.
     */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<WorkOrder> updatePriority(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer priority = body.get("priority");
        if (priority == null) {
            return ResponseEntity.badRequest().build();
        }
        return workOrderService.updatePriority(id, priority)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a work order.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        if (workOrderService.deleteWorkOrder(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
