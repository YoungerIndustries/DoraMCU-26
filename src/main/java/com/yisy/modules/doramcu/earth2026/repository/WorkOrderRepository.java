package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for WorkOrder entities.
 */
@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    /**
     * Find work orders by status.
     */
    List<WorkOrder> findByStatus(WorkOrder.WorkOrderStatus status);

    /**
     * Find work orders by requesting NPC.
     */
    List<WorkOrder> findByRequestingNpcId(Long requestingNpcId);

    /**
     * Find work orders by assigned worker.
     */
    List<WorkOrder> findByAssignedWorkerId(Long assignedWorkerId);

    /**
     * Find work orders by structure.
     */
    List<WorkOrder> findByStructureId(Long structureId);

    /**
     * Find work orders by status and structure.
     */
    List<WorkOrder> findByStatusAndStructureId(WorkOrder.WorkOrderStatus status, Long structureId);

    /**
     * Find pending work orders ordered by priority (highest first).
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.status = 'PENDING' ORDER BY w.priority DESC, w.createdAt ASC")
    List<WorkOrder> findPendingWorkOrders();

    /**
     * Find work orders by requesting NPC and status.
     */
    List<WorkOrder> findByRequestingNpcIdAndStatus(Long requestingNpcId, WorkOrder.WorkOrderStatus status);

    /**
     * Find work orders by assigned worker and status.
     */
    List<WorkOrder> findByAssignedWorkerIdAndStatus(Long assignedWorkerId, WorkOrder.WorkOrderStatus status);

    /**
     * Find work orders by structure and status.
     */
    List<WorkOrder> findByStructureIdAndStatus(Long structureId, WorkOrder.WorkOrderStatus status);

    /**
     * Count work orders by status.
     */
    long countByStatus(WorkOrder.WorkOrderStatus status);

    /**
     * Count work orders by requesting NPC.
     */
    long countByRequestingNpcId(Long requestingNpcId);

    /**
     * Count work orders by assigned worker.
     */
    long countByAssignedWorkerId(Long assignedWorkerId);

    /**
     * Find work orders by priority range.
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.priority BETWEEN :minPriority AND :maxPriority AND w.status = :status")
    List<WorkOrder> findByPriorityRangeAndStatus(
            @Param("minPriority") int minPriority,
            @Param("maxPriority") int maxPriority,
            @Param("status") WorkOrder.WorkOrderStatus status);
}
