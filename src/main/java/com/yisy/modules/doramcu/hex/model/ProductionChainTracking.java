package com.yisy.modules.doramcu.hex.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a complete production chain from raw materials to final product.
 * <p>
 * A production chain tracks the entire process of building an infrastructure item,
 * from mining raw resources through processing, assembly, and final construction.
 * It aggregates all phases, contracts, costs, and timing information.
 * </p>
 * <p>
 * Key mechanics:
 * - Status Flow: PLANNED → IN_PROGRESS → COMPLETED (or FAILED)
 * - Phases: Mining → Processing → Assembly → Construction
 * - Total Costs: Aggregated material and labor costs across all phases
 * - Phase Breakdown: JSON defining all phases and their sub-contracts
 * - Minimum Crews: Number of parallel crew groups needed for optimal execution
 * </p>
 */
@Entity
@Table(name = "production_chain_tracking", indexes = {
        @Index(name = "idx_production_chain_status", columnList = "status"),
        @Index(name = "idx_production_chain_target", columnList = "target_item_id"),
        @Index(name = "idx_production_chain_created", columnList = "created_at")
})
public class ProductionChainTracking {

    public enum ProductionChainStatus {
        PLANNED("Planned", "Production chain defined, not yet started"),
        IN_PROGRESS("In Progress", "One or more contracts are actively being worked"),
        COMPLETED("Completed", "All contracts in the chain are finished"),
        FAILED("Failed", "One or more critical contracts failed");

        private final String displayName;
        private final String description;

        ProductionChainStatus(String displayName, String description) {
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
     * Name of the production chain (e.g., "Military Station - Full Chain").
     */
    @Column(name = "chain_name", nullable = false, length = 200)
    private String chainName;

    /**
     * Reference to the target infrastructure item being built.
     */
    @Column(name = "target_item_id")
    private Long targetItemId;

    /**
     * Name of the target infrastructure item being built.
     */
    @Column(name = "target_item_name", length = 200)
    private String targetItemName;

    /**
     * Type of the target infrastructure item.
     */
    @Column(name = "target_item_type", length = 50)
    private String targetItemType;

    /**
     * Number of contracts completed so far.
     */
    @Column(name = "completed_contracts")
    private Integer completedContracts;

    /**
     * Number of contracts that have failed.
     */
    @Column(name = "failed_contracts")
    private Integer failedContracts;

    /**
     * Overall progress percentage (0-100).
     */
    @Column(name = "progress_percent")
    private Integer progressPercent;

    /**
     * JSON defining the phases of the production chain.
     */
    @Column(name = "phases_json", columnDefinition = "TEXT")
    private String phasesJson;

    /**
     * Galaxy this production chain belongs to.
     */
    @Column(name = "galaxy_id")
    private Long galaxyId;

    /**
     * Last updated timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Total number of phases in this production chain.
     */
    @Column(name = "total_phases")
    private Integer totalPhases;

    /**
     * Total number of contracts across all phases.
     */
    @Column(name = "total_contracts")
    private Integer totalContracts;

    /**
     * Total material cost in credits across all phases.
     */
    @Column(name = "total_material_cost_credits", precision = 15, scale = 2)
    private BigDecimal totalMaterialCostCredits;

    /**
     * Total labor cost in credits across all phases.
     */
    @Column(name = "total_labor_cost_credits", precision = 15, scale = 2)
    private BigDecimal totalLaborCostCredits;

    /**
     * Total duration in turns (sequential execution).
     */
    @Column(name = "total_duration_turns")
    private Integer totalDurationTurns;

    /**
     * Minimum number of parallel crew groups needed for optimal execution.
     */
    @Column(name = "min_crews_required")
    private Integer minCrewsRequired;

    /**
     * Current status of the production chain.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionChainStatus status;

    /**
     * JSON object defining the full production chain with all phases and sub-contracts.
     * Example: {"phases": [{"name": "Mining", "contracts": [...]}, ...]}
     */
    @Column(name = "phase_breakdown_json", columnDefinition = "TEXT")
    private String phaseBreakdownJson;

    /**
     * When the production chain was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the production chain was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ProductionChainStatus.PLANNED;
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

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public Long getTargetItemId() {
        return targetItemId;
    }

    public void setTargetItemId(Long targetItemId) {
        this.targetItemId = targetItemId;
    }

    public Integer getTotalPhases() {
        return totalPhases;
    }

    public void setTotalPhases(Integer totalPhases) {
        this.totalPhases = totalPhases;
    }

    public Integer getTotalContracts() {
        return totalContracts;
    }

    public void setTotalContracts(Integer totalContracts) {
        this.totalContracts = totalContracts;
    }

    public BigDecimal getTotalMaterialCostCredits() {
        return totalMaterialCostCredits;
    }

    public void setTotalMaterialCostCredits(BigDecimal totalMaterialCostCredits) {
        this.totalMaterialCostCredits = totalMaterialCostCredits;
    }

    public BigDecimal getTotalLaborCostCredits() {
        return totalLaborCostCredits;
    }

    public void setTotalLaborCostCredits(BigDecimal totalLaborCostCredits) {
        this.totalLaborCostCredits = totalLaborCostCredits;
    }

    public Integer getTotalDurationTurns() {
        return totalDurationTurns;
    }

    public void setTotalDurationTurns(Integer totalDurationTurns) {
        this.totalDurationTurns = totalDurationTurns;
    }

    public Integer getMinCrewsRequired() {
        return minCrewsRequired;
    }

    public void setMinCrewsRequired(Integer minCrewsRequired) {
        this.minCrewsRequired = minCrewsRequired;
    }

    public ProductionChainStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionChainStatus status) {
        this.status = status;
    }

    public String getPhaseBreakdownJson() {
        return phaseBreakdownJson;
    }

    public void setPhaseBreakdownJson(String phaseBreakdownJson) {
        this.phaseBreakdownJson = phaseBreakdownJson;
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

    public String getTargetItemName() {
        return targetItemName;
    }

    public void setTargetItemName(String targetItemName) {
        this.targetItemName = targetItemName;
    }

    public String getTargetItemType() {
        return targetItemType;
    }

    public void setTargetItemType(String targetItemType) {
        this.targetItemType = targetItemType;
    }

    public Integer getCompletedContracts() {
        return completedContracts;
    }

    public void setCompletedContracts(Integer completedContracts) {
        this.completedContracts = completedContracts;
    }

    public Integer getFailedContracts() {
        return failedContracts;
    }

    public void setFailedContracts(Integer failedContracts) {
        this.failedContracts = failedContracts;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getPhasesJson() {
        return phasesJson;
    }

    public void setPhasesJson(String phasesJson) {
        this.phasesJson = phasesJson;
    }

    public Long getGalaxyId() {
        return galaxyId;
    }

    public void setGalaxyId(Long galaxyId) {
        this.galaxyId = galaxyId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ProductionChainTracking{name='" + chainName + "', status=" + status +
                ", phases=" + totalPhases + ", contracts=" + totalContracts + "}";
    }
}
