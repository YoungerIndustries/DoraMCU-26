package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.WorkOrder;
import com.yisy.modules.doramcu.earth2026.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing work orders in the Earth 2026 module.
 * <p>
 * Work orders connect the planetary economy: mines extract resources, factories
 * manufacture items, and stores distribute them. AI NPCs create work orders
 * based on resource needs, and AI workers execute them.
 * </p>
 */
@Service
public class WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * Get all work orders.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    /**
     * Get a work order by ID.
     */
    @Transactional(readOnly = true)
    public Optional<WorkOrder> getWorkOrderById(Long id) {
        return workOrderRepository.findById(id);
    }

    /**
     * Get work orders by status.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersByStatus(WorkOrder.WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    /**
     * Get work orders by requesting NPC.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersByRequestingNpc(Long requestingNpcId) {
        return workOrderRepository.findByRequestingNpcId(requestingNpcId);
    }

    /**
     * Get work orders by assigned worker.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersByAssignedWorker(Long assignedWorkerId) {
        return workOrderRepository.findByAssignedWorkerId(assignedWorkerId);
    }

    /**
     * Get work orders by structure.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersByStructure(Long structureId) {
        return workOrderRepository.findByStructureId(structureId);
    }

    /**
     * Get pending work orders ordered by priority.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getPendingWorkOrders() {
        return workOrderRepository.findPendingWorkOrders();
    }

    /**
     * Get work orders by status and structure.
     */
    @Transactional(readOnly = true)
    public List<WorkOrder> getWorkOrdersByStatusAndStructure(WorkOrder.WorkOrderStatus status, Long structureId) {
        return workOrderRepository.findByStatusAndStructureId(status, structureId);
    }

    /**
     * Count work orders by status.
     */
    @Transactional(readOnly = true)
    public long countByStatus(WorkOrder.WorkOrderStatus status) {
        return workOrderRepository.countByStatus(status);
    }

    /**
     * Create a new work order.
     */
    @Transactional
    public WorkOrder createWorkOrder(WorkOrder workOrder) {
        log.info("Creating work order: {} with priority {}", workOrder.getTitle(), workOrder.getPriority());
        return workOrderRepository.save(workOrder);
    }

    /**
     * Assign a worker to a work order.
     * Transitions from PENDING to ASSIGNED.
     */
    @Transactional
    public Optional<WorkOrder> assignWorker(Long workOrderId, Long workerId) {
        return workOrderRepository.findById(workOrderId).map(workOrder -> {
            if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.PENDING) {
                log.warn("Cannot assign worker to work order {} with status {}", workOrderId, workOrder.getStatus());
                return workOrder;
            }
            workOrder.setAssignedWorkerId(workerId);
            workOrder.setStatus(WorkOrder.WorkOrderStatus.ASSIGNED);
            log.info("Assigned worker {} to work order {}", workerId, workOrderId);
            return workOrderRepository.save(workOrder);
        });
    }

    /**
     * Start working on a work order.
     * Transitions from ASSIGNED to IN_PROGRESS.
     */
    @Transactional
    public Optional<WorkOrder> startWorkOrder(Long workOrderId) {
        return workOrderRepository.findById(workOrderId).map(workOrder -> {
            if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.ASSIGNED) {
                log.warn("Cannot start work order {} with status {}", workOrderId, workOrder.getStatus());
                return workOrder;
            }
            workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
            log.info("Started work order {}", workOrderId);
            return workOrderRepository.save(workOrder);
        });
    }

    /**
     * Complete a work order successfully.
     * Transitions from IN_PROGRESS to COMPLETED.
     */
    @Transactional
    public Optional<WorkOrder> completeWorkOrder(Long workOrderId) {
        return workOrderRepository.findById(workOrderId).map(workOrder -> {
            if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.IN_PROGRESS) {
                log.warn("Cannot complete work order {} with status {}", workOrderId, workOrder.getStatus());
                return workOrder;
            }
            workOrder.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);
            workOrder.setCompletedAt(LocalDateTime.now());
            log.info("Completed work order {}", workOrderId);
            return workOrderRepository.save(workOrder);
        });
    }

    /**
     * Fail a work order.
     * Transitions from any status to FAILED.
     */
    @Transactional
    public Optional<WorkOrder> failWorkOrder(Long workOrderId, String reason) {
        return workOrderRepository.findById(workOrderId).map(workOrder -> {
            if (workOrder.getStatus() == WorkOrder.WorkOrderStatus.COMPLETED) {
                log.warn("Cannot fail completed work order {}", workOrderId);
                return workOrder;
            }
            workOrder.setStatus(WorkOrder.WorkOrderStatus.FAILED);
            workOrder.setCompletedAt(LocalDateTime.now());
            // Append reason to description
            if (reason != null && !reason.isEmpty()) {
                workOrder.setDescription(workOrder.getDescription() + " [FAILED: " + reason + "]");
            }
            log.info("Failed work order {}: {}", workOrderId, reason);
            return workOrderRepository.save(workOrder);
        });
    }

    /**
     * Update the priority of a work order.
     */
    @Transactional
    public Optional<WorkOrder> updatePriority(Long workOrderId, int priority) {
        return workOrderRepository.findById(workOrderId).map(workOrder -> {
            workOrder.setPriority(priority);
            log.info("Updated priority of work order {} to {}", workOrderId, priority);
            return workOrderRepository.save(workOrder);
        });
    }

    /**
     * Delete a work order by ID.
     */
    @Transactional
    public boolean deleteWorkOrder(Long id) {
        return workOrderRepository.findById(id).map(workOrder -> {
            log.info("Deleting work order: {}", workOrder.getTitle());
            workOrderRepository.delete(workOrder);
            return true;
        }).orElse(false);
    }
}
