package com.yisy.modules.doramcu.hex.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a build contract in the economic production system.
 * <p>
 * Every buildable item is broken into contracts that a single crew group
 * (1 leader + 3 members) can execute. Contracts track material requirements,
 * labor requirements, progress, and costs.
 * </p>
 * <p>
 * Key mechanics:
 * - Status Flow: PENDING → ASSIGNED → IN_PROGRESS → COMPLETED (or FAILED)
 * - Contract Type: MINE, PROCESS, ASSEMBLE, CONSTRUCT, REPAIR
 * - Crew Size: Fixed at 4 (1 leader + 3 members)
 * - Material Requirements: JSON string specifying resources needed
 * - Dependencies: JSON array of contract IDs that must complete first
 * - Leadership Modifier: Affects labor cost efficiency
 * </p>
 */
@Entity
@Table(name = "build_contracts", indexes = {
        @Index(name = "idx_build_contracts_status", columnList = "status"),
        @Index(name = "idx_build_contracts_type", columnList = "contract_type"),
        @Index(name = "idx_build_contracts_crew", columnList = "assigned_crew_id"),
        @Index(name = "idx_build_contracts_leader", columnList = "assigned_leader_id"),
        @Index(name = "idx_build_contracts_priority", columnList = "priority"),
        @Index(name = "idx_build_contracts_galaxy", columnList = "galaxy_id"),
        @Index(name = "idx_build_contracts_created", columnList = "created_at")
})
public class BuildContract {

    public enum ContractType {
        MINE("Mine", "Extract raw resources from celestial bodies"),
        PROCESS("Process", "Convert raw resources to refined resources"),
        ASSEMBLE("Assemble", "Assemble refined resources into construction modules"),
        CONSTRUCT("Construct", "Build final products from construction modules"),
        REPAIR("Repair", "Repair damaged structures or stations");

        private final String displayName;
        private final String description;

        ContractType(String displayName, String description) {
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

    public enum BuildContractStatus {
        PENDING("Pending", "Contract created, waiting for assignment"),
        ASSIGNED("Assigned", "Contract assigned to a crew group"),
        IN_PROGRESS("In Progress", "Crew is actively working on the contract"),
        COMPLETED("Completed", "Contract finished successfully"),
        FAILED("Failed", "Contract failed to complete");

        private final String displayName;
        private final String description;

        BuildContractStatus(String displayName, String description) {
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
     * Type of contract: mine, process, assemble, construct, or repair.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 50)
    private ContractType contractType;

    /**
     * Type of target item being built (e.g., FACILITY, STATION_MODULE, SHIP_COMPONENT).
     */
    @Column(name = "target_item_type", nullable = false, length = 100)
    private String targetItemType;

    /**
     * Name of the target item being built.
     */
    @Column(name = "target_item_name", nullable = false, length = 200)
    private String targetItemName;

    /**
     * Description of the contract work.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * JSON string specifying material requirements for this contract.
     * Example: {"METAL_ALLOY": 2, "COMPOSITE": 1}
     */
    @Column(name = "material_requirements_json", nullable = false, columnDefinition = "TEXT")
    private String materialRequirementsJson;

    /**
     * Required leader role (e.g., Engineer, Miner, Scientist, General).
     */
    @Column(name = "leader_role_required", length = 50)
    private String leaderRoleRequired;

    /**
     * Minimum leadership rating required for the leader (1-10).
     */
    @Column(name = "leader_min_leadership")
    private Integer leaderMinLeadership;

    /**
     * Crew size for this contract (default 4: 1 leader + 3 members).
     */
    @Column(name = "crew_size")
    private Integer crewSize;

    /**
     * JSON string specifying required crew skills.
     * Example: ["Engineer", "Engineer", "Worker"]
     */
    @Column(name = "crew_skills_required_json", columnDefinition = "TEXT")
    private String crewSkillsRequiredJson;

    /**
     * Estimated duration in turns to complete this contract.
     */
    @Column(name = "estimated_duration_turns", nullable = false)
    private Integer estimatedDurationTurns;

    /**
     * Priority of the contract (1-10, higher = more urgent).
     */
    private Integer priority;

    /**
     * Current status of the contract.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BuildContractStatus status;

    /**
     * ID of the assigned crew group.
     */
    @Column(name = "assigned_crew_id")
    private Long assignedCrewId;

    /**
     * ID of the assigned leader (AI Character).
     */
    @Column(name = "assigned_leader_id")
    private Long assignedLeaderId;

    /**
     * ID of crew member 1 (AI Character).
     */
    @Column(name = "crew_member_1_id")
    private Long crewMember1Id;

    /**
     * ID of crew member 2 (AI Character).
     */
    @Column(name = "crew_member_2_id")
    private Long crewMember2Id;

    /**
     * ID of crew member 3 (AI Character).
     */
    @Column(name = "crew_member_3_id")
    private Long crewMember3Id;

    /**
     * Progress percentage (0-100).
     */
    @Column(name = "progress_percent")
    private Integer progressPercent;

    /**
     * Total material cost in credits.
     */
    @Column(name = "material_cost_total_credits", precision = 15, scale = 2)
    private BigDecimal materialCostTotalCredits;

    /**
     * Total labor cost in credits.
     */
    @Column(name = "labor_cost_total_credits", precision = 15, scale = 2)
    private BigDecimal laborCostTotalCredits;

    /**
     * JSON array of contract IDs that must complete before this one.
     * Example: ["contract_id_1", "contract_id_2"]
     */
    @Column(name = "dependencies_json", columnDefinition = "TEXT")
    private String dependenciesJson;

    /**
     * ID of the parent production chain this contract belongs to.
     */
    @Column(name = "production_chain_id")
    private Long productionChainId;

    /**
     * Game turn when work started.
     */
    @Column(name = "start_turn")
    private Integer startTurn;

    /**
     * Game turn when work is expected to complete.
     */
    @Column(name = "completion_turn")
    private Integer completionTurn;

    /**
     * Galaxy ID where this contract takes place.
     */
    @Column(name = "galaxy_id")
    private Long galaxyId;

    /**
     * Planet ID where this contract takes place (if applicable).
     */
    @Column(name = "planet_id")
    private Long planetId;

    /**
     * Space station ID where this contract takes place (if applicable).
     */
    @Column(name = "station_id")
    private Long stationId;

    /**
     * When the contract was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the contract was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BuildContractStatus.PENDING;
        }
        if (priority == null) {
            priority = 5;
        }
        if (crewSize == null) {
            crewSize = 4;
        }
        if (progressPercent == null) {
            progressPercent = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public String getTargetItemType() {
        return targetItemType;
    }

    public void setTargetItemType(String targetItemType) {
        this.targetItemType = targetItemType;
    }

    public String getTargetItemName() {
        return targetItemName;
    }

    public void setTargetItemName(String targetItemName) {
        this.targetItemName = targetItemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaterialRequirementsJson() {
        return materialRequirementsJson;
    }

    public void setMaterialRequirementsJson(String materialRequirementsJson) {
        this.materialRequirementsJson = materialRequirementsJson;
    }

    public String getLeaderRoleRequired() {
        return leaderRoleRequired;
    }

    public void setLeaderRoleRequired(String leaderRoleRequired) {
        this.leaderRoleRequired = leaderRoleRequired;
    }

    public Integer getLeaderMinLeadership() {
        return leaderMinLeadership;
    }

    public void setLeaderMinLeadership(Integer leaderMinLeadership) {
        this.leaderMinLeadership = leaderMinLeadership;
    }

    public Integer getCrewSize() {
        return crewSize;
    }

    public void setCrewSize(Integer crewSize) {
        this.crewSize = crewSize;
    }

    public String getCrewSkillsRequiredJson() {
        return crewSkillsRequiredJson;
    }

    public void setCrewSkillsRequiredJson(String crewSkillsRequiredJson) {
        this.crewSkillsRequiredJson = crewSkillsRequiredJson;
    }

    public Integer getEstimatedDurationTurns() {
        return estimatedDurationTurns;
    }

    public void setEstimatedDurationTurns(Integer estimatedDurationTurns) {
        this.estimatedDurationTurns = estimatedDurationTurns;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public BuildContractStatus getStatus() {
        return status;
    }

    public void setStatus(BuildContractStatus status) {
        this.status = status;
    }

    public Long getAssignedCrewId() {
        return assignedCrewId;
    }

    public void setAssignedCrewId(Long assignedCrewId) {
        this.assignedCrewId = assignedCrewId;
    }

    public Long getAssignedLeaderId() {
        return assignedLeaderId;
    }

    public void setAssignedLeaderId(Long assignedLeaderId) {
        this.assignedLeaderId = assignedLeaderId;
    }

    public Long getCrewMember1Id() {
        return crewMember1Id;
    }

    public void setCrewMember1Id(Long crewMember1Id) {
        this.crewMember1Id = crewMember1Id;
    }

    public Long getCrewMember2Id() {
        return crewMember2Id;
    }

    public void setCrewMember2Id(Long crewMember2Id) {
        this.crewMember2Id = crewMember2Id;
    }

    public Long getCrewMember3Id() {
        return crewMember3Id;
    }

    public void setCrewMember3Id(Long crewMember3Id) {
        this.crewMember3Id = crewMember3Id;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public BigDecimal getMaterialCostTotalCredits() {
        return materialCostTotalCredits;
    }

    public void setMaterialCostTotalCredits(BigDecimal materialCostTotalCredits) {
        this.materialCostTotalCredits = materialCostTotalCredits;
    }

    public BigDecimal getLaborCostTotalCredits() {
        return laborCostTotalCredits;
    }

    public void setLaborCostTotalCredits(BigDecimal laborCostTotalCredits) {
        this.laborCostTotalCredits = laborCostTotalCredits;
    }

    public String getDependenciesJson() {
        return dependenciesJson;
    }

    public void setDependenciesJson(String dependenciesJson) {
        this.dependenciesJson = dependenciesJson;
    }

    public Long getProductionChainId() {
        return productionChainId;
    }

    public void setProductionChainId(Long productionChainId) {
        this.productionChainId = productionChainId;
    }

    public Integer getStartTurn() {
        return startTurn;
    }

    public void setStartTurn(Integer startTurn) {
        this.startTurn = startTurn;
    }

    public Integer getCompletionTurn() {
        return completionTurn;
    }

    public void setCompletionTurn(Integer completionTurn) {
        this.completionTurn = completionTurn;
    }

    public Long getGalaxyId() {
        return galaxyId;
    }

    public void setGalaxyId(Long galaxyId) {
        this.galaxyId = galaxyId;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
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
        return "BuildContract{type=" + contractType + ", target='" + targetItemName +
                "', status=" + status + ", progress=" + progressPercent + "%}";
    }
}
