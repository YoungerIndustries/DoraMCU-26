package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a work order in the Earth 2026 module.
 * <p>
 * Work orders connect the planetary economy: mines extract resources, factories
 * manufacture items, and stores distribute them. AI NPCs create work orders
 * based on resource needs, and AI workers execute them.
 * </p>
 * <p>
 * Key mechanics:
 * - Status Flow: PENDING → ASSIGNED → IN_PROGRESS → COMPLETED (or FAILED)
 * - Required Resources: JSON string specifying resources needed for the work
 * - Output Items: JSON string specifying items produced by completing the work
 * - Priority: Higher priority work orders are processed first
 * - Structure Link: Work orders are tied to specific planet structures
 * </p>
 */
@Entity
@Table(name = "work_orders", indexes = {
        @Index(name = "idx_work_order_status", columnList = "status"),
        @Index(name = "idx_work_order_requesting_npc", columnList = "requesting_npc_id"),
        @Index(name = "idx_work_order_assigned_worker", columnList = "assigned_worker_id"),
        @Index(name = "idx_work_order_structure", columnList = "structure_id"),
        @Index(name = "idx_work_order_priority", columnList = "priority"),
        @Index(name = "idx_work_order_created", columnList = "created_at")
})
public class WorkOrder {

    public enum WorkOrderStatus {
        PENDING("Pending", "Work order created, waiting for assignment"),
        ASSIGNED("Assigned", "Work order assigned to a worker"),
        IN_PROGRESS("In Progress", "Worker is actively working on the order"),
        COMPLETED("Completed", "Work order finished successfully"),
        FAILED("Failed", "Work order failed to complete");

        private final String displayName;
        private final String description;

        WorkOrderStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Title of the work order.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Detailed description of the work to be done.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * NPC who requested this work order.
     */
    @Column(name = "requesting_npc_id")
    private Long requestingNpcId;

    /**
     * NPC assigned to execute this work order.
     */
    @Column(name = "assigned_worker_id")
    private Long assignedWorkerId;

    /**
     * Current status of the work order.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkOrderStatus status;

    /**
     * JSON string specifying resources required for this work.
     * Example: {"iron_ore": 100, "copper": 50}
     */
    @Column(name = "required_resources_json", columnDefinition = "TEXT")
    private String requiredResourcesJson;

    /**
     * JSON string specifying items produced by completing this work.
     * Example: {"steel_beams": 50, "circuit_boards": 25}
     */
    @Column(name = "output_items_json", columnDefinition = "TEXT")
    private String outputItemsJson;

    /**
     * Priority of the work order (higher = more urgent).
     */
    private Integer priority;

    /**
     * Planet structure where this work takes place.
     */
    @Column(name = "structure_id")
    private Long structureId;

    /**
     * When the work order was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the work order was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When the work order was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = WorkOrderStatus.PENDING;
        }
        if (priority == null) {
            priority = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRequestingNpcId() {
        return requestingNpcId;
    }

    public void setRequestingNpcId(Long requestingNpcId) {
        this.requestingNpcId = requestingNpcId;
    }

    public Long getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(Long assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public String getRequiredResourcesJson() {
        return requiredResourcesJson;
    }

    public void setRequiredResourcesJson(String requiredResourcesJson) {
        this.requiredResourcesJson = requiredResourcesJson;
    }

    public String getOutputItemsJson() {
        return outputItemsJson;
    }

    public void setOutputItemsJson(String outputItemsJson) {
        this.outputItemsJson = outputItemsJson;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getStructureId() {
        return structureId;
    }

    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "WorkOrder{title='" + title + "', status=" + status +
                ", priority=" + priority + ", structureId=" + structureId + "}";
    }
}
