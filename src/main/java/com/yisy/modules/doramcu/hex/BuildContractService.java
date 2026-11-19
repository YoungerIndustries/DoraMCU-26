package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.repository.BuildContractRepository;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Manages the lifecycle of build contracts in the economic production system.
 * <p>
 * Each contract represents a unit of work that a single crew group (1 leader + 3 members)
 * can execute. This service handles contract creation, crew assignment, progress tracking,
 * completion with XP rewards, and failure handling.
 * </p>
 * <p>
 * Contract status flow: PENDING → ASSIGNED → IN_PROGRESS → COMPLETED (or FAILED)
 * </p>
 */
@Service
public class BuildContractService {

    private static final Logger log = LoggerFactory.getLogger(BuildContractService.class);

    /** XP awarded for completing a build contract successfully. */
    public static final int XP_PER_CONTRACT_COMPLETED = 20;

    /** XP bonus per point of leadership rating above 5. */
    public static final int XP_LEADERSHIP_BONUS = 5;

    private final BuildContractRepository contractRepository;
    private final InfrastructureItemRepository infrastructureItemRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final CostCalculatorService costCalculatorService;

    public BuildContractService(BuildContractRepository contractRepository,
                                 InfrastructureItemRepository infrastructureItemRepository,
                                 AiCharacterRepository aiCharacterRepository,
                                 CostCalculatorService costCalculatorService) {
        this.contractRepository = contractRepository;
        this.infrastructureItemRepository = infrastructureItemRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.costCalculatorService = costCalculatorService;
    }

    // ------------------------------------------------------------------
    // Contract creation
    // ------------------------------------------------------------------

    /**
     * Create a new build contract from an infrastructure item.
     *
     * @param infrastructureItem the item to build
     * @param galaxyId the galaxy where the contract takes place
     * @return the created contract
     */
    @Transactional
    public BuildContract createContract(InfrastructureItem infrastructureItem, Long galaxyId) {
        BuildContract contract = new BuildContract();
        contract.setTargetItemType(infrastructureItem.getItemType().name());
        contract.setTargetItemName(infrastructureItem.getItemName());
        contract.setDescription("Build " + infrastructureItem.getItemName() + " (" + infrastructureItem.getItemType().getDisplayName() + ")");
        contract.setMaterialRequirementsJson(infrastructureItem.getRawResourcesJson());
        contract.setEstimatedDurationTurns(infrastructureItem.getBuildTimeTurns());
        contract.setGalaxyId(galaxyId);
        contract.setMaterialCostTotalCredits(infrastructureItem.getLaborCostCredits());
        contract.setPriority(5);
        contract.setCrewSize(4);

        BuildContract saved = contractRepository.save(contract);
        log.info("Created build contract {} for {} in galaxy {}", saved.getId(), infrastructureItem.getItemName(), galaxyId);
        return saved;
    }

    /**
     * Create a new build contract with explicit parameters.
     *
     * @param contractType type of contract (MINE, PROCESS, ASSEMBLE, CONSTRUCT, REPAIR)
     * @param targetItemType type of target item
     * @param targetItemName name of target item
     * @param materialRequirementsJson JSON string of material requirements
     * @param estimatedDurationTurns estimated turns to complete
     * @param galaxyId galaxy where the contract takes place
     * @return the created contract
     */
    @Transactional
    public BuildContract createContract(BuildContract.ContractType contractType,
                                         String targetItemType,
                                         String targetItemName,
                                         String materialRequirementsJson,
                                         int estimatedDurationTurns,
                                         Long galaxyId) {
        BuildContract contract = new BuildContract();
        contract.setContractType(contractType);
        contract.setTargetItemType(targetItemType);
        contract.setTargetItemName(targetItemName);
        contract.setDescription(contractType.getDisplayName() + " " + targetItemName);
        contract.setMaterialRequirementsJson(materialRequirementsJson);
        contract.setEstimatedDurationTurns(estimatedDurationTurns);
        contract.setGalaxyId(galaxyId);
        contract.setPriority(5);
        contract.setCrewSize(4);

        BuildContract saved = contractRepository.save(contract);
        log.info("Created build contract {} ({}) for {}", saved.getId(), contractType, targetItemName);
        return saved;
    }

    /**
     * Create a new build contract with dependency tracking and production chain association.
     * Used by ProductionChainService to create contracts with prerequisite contract IDs.
     *
     * @param contractType type of contract (MINE, PROCESS, ASSEMBLE, CONSTRUCT, REPAIR)
     * @param targetItemType type of target item
     * @param targetItemName name of target item
     * @param materialRequirementsJson JSON string of material requirements
     * @param estimatedDurationTurns estimated turns to complete
     * @param galaxyId galaxy where the contract takes place
     * @param productionChainId ID of the parent production chain
     * @param dependencyContractIds list of contract IDs that must complete before this one
     * @return the created contract
     */
    @Transactional
    public BuildContract createContractWithDependencies(BuildContract.ContractType contractType,
                                                         String targetItemType,
                                                         String targetItemName,
                                                         String materialRequirementsJson,
                                                         int estimatedDurationTurns,
                                                         Long galaxyId,
                                                         Long productionChainId,
                                                         List<Long> dependencyContractIds) {
        BuildContract contract = new BuildContract();
        contract.setContractType(contractType);
        contract.setTargetItemType(targetItemType);
        contract.setTargetItemName(targetItemName);
        contract.setDescription(contractType.getDisplayName() + " " + targetItemName);
        contract.setMaterialRequirementsJson(materialRequirementsJson);
        contract.setEstimatedDurationTurns(estimatedDurationTurns);
        contract.setGalaxyId(galaxyId);
        contract.setProductionChainId(productionChainId);
        contract.setPriority(5);
        contract.setCrewSize(4);

        if (dependencyContractIds != null && !dependencyContractIds.isEmpty()) {
            contract.setDependenciesJson(dependencyContractIds.toString());
        }

        BuildContract saved = contractRepository.save(contract);
        log.info("Created build contract {} ({}) for {} with chain {} and {} dependencies",
                saved.getId(), contractType, targetItemName, productionChainId,
                dependencyContractIds != null ? dependencyContractIds.size() : 0);
        return saved;
    }

    // ------------------------------------------------------------------
    // Crew assignment
    // ------------------------------------------------------------------

    /**
     * Assign a crew group to a pending contract.
     *
     * @param contractId the contract to assign
     * @param crewId the crew group ID
     * @param leaderId the leader AI character ID
     * @param memberIds list of member AI character IDs (up to 3)
     * @return the updated contract
     */
    @Transactional
    public BuildContract assignContract(Long contractId, Long crewId, Long leaderId, List<Long> memberIds) {
        Optional<BuildContract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Contract not found: " + contractId);
        }
        BuildContract contract = opt.get();
        if (contract.getStatus() != BuildContractStatus.PENDING) {
            throw new IllegalStateException("Contract is not PENDING: " + contract.getStatus());
        }

        contract.setAssignedCrewId(crewId);
        contract.setAssignedLeaderId(leaderId);
        contract.setStatus(BuildContractStatus.ASSIGNED);

        if (memberIds != null && !memberIds.isEmpty()) {
            if (memberIds.size() >= 1) contract.setCrewMember1Id(memberIds.get(0));
            if (memberIds.size() >= 2) contract.setCrewMember2Id(memberIds.get(1));
            if (memberIds.size() >= 3) contract.setCrewMember3Id(memberIds.get(2));
        }

        // Calculate labor cost with leadership modifier
        aiCharacterRepository.findById(leaderId).ifPresent(leader -> {
            int duration = contract.getEstimatedDurationTurns() != null ? contract.getEstimatedDurationTurns() : 1;
            int crewSize = contract.getCrewSize() != null ? contract.getCrewSize() : 4;
            BigDecimal laborCost = costCalculatorService.calculateLaborCost(
                    crewSize, leader.getDailySalary(), duration, leader.getLeadershipRating());
            contract.setLaborCostTotalCredits(laborCost);
        });

        BuildContract saved = contractRepository.save(contract);
        log.info("Assigned contract {} to crew {} with leader {}", contractId, crewId, leaderId);
        return saved;
    }

    // ------------------------------------------------------------------
    // Progress tracking
    // ------------------------------------------------------------------

    /**
     * Start a contract (transition from ASSIGNED to IN_PROGRESS).
     *
     * @param contractId the contract to start
     * @return the updated contract
     */
    @Transactional
    public BuildContract startContract(Long contractId) {
        Optional<BuildContract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Contract not found: " + contractId);
        }
        BuildContract contract = opt.get();
        if (contract.getStatus() != BuildContractStatus.ASSIGNED) {
            throw new IllegalStateException("Contract is not ASSIGNED: " + contract.getStatus());
        }

        contract.setStatus(BuildContractStatus.IN_PROGRESS);
        contract.setProgressPercent(0);

        BuildContract saved = contractRepository.save(contract);
        log.info("Started contract {}", contractId);
        return saved;
    }

    /**
     * Update the progress of an in-progress contract.
     *
     * @param contractId the contract to update
     * @param progressPercent progress percentage (0-100)
     * @return the updated contract
     */
    @Transactional
    public BuildContract updateProgress(Long contractId, int progressPercent) {
        Optional<BuildContract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Contract not found: " + contractId);
        }
        BuildContract contract = opt.get();
        if (contract.getStatus() != BuildContractStatus.IN_PROGRESS) {
            throw new IllegalStateException("Contract is not IN_PROGRESS: " + contract.getStatus());
        }

        contract.setProgressPercent(Math.max(0, Math.min(100, progressPercent)));

        BuildContract saved = contractRepository.save(contract);
        log.debug("Updated contract {} progress to {}%", contractId, progressPercent);
        return saved;
    }

    // ------------------------------------------------------------------
    // Completion and failure
    // ------------------------------------------------------------------

    /**
     * Complete a contract, awarding XP to the leader and crew members.
     *
     * @param contractId the contract to complete
     * @return the completed contract
     */
    @Transactional
    public BuildContract completeContract(Long contractId) {
        Optional<BuildContract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Contract not found: " + contractId);
        }
        BuildContract contract = opt.get();
        if (contract.getStatus() != BuildContractStatus.IN_PROGRESS &&
            contract.getStatus() != BuildContractStatus.ASSIGNED) {
            throw new IllegalStateException("Contract cannot be completed from status: " + contract.getStatus());
        }

        contract.setStatus(BuildContractStatus.COMPLETED);
        contract.setProgressPercent(100);
        contract.setCompletedAt(LocalDateTime.now());

        // Award XP to the leader
        if (contract.getAssignedLeaderId() != null) {
            aiCharacterRepository.findById(contract.getAssignedLeaderId()).ifPresent(leader -> {
                int xpBonus = Math.max(0, (leader.getLeadershipRating() - 5) * XP_LEADERSHIP_BONUS);
                int totalXp = XP_PER_CONTRACT_COMPLETED + xpBonus;
                leader.setTotalExperience(leader.getTotalExperience() + totalXp);
                aiCharacterRepository.save(leader);
                log.info("Awarded {} XP to leader {} for contract {}", totalXp, leader.getName(), contractId);
            });
        }

        // Award XP to crew members
        for (Long memberId : List.of(contract.getCrewMember1Id(), contract.getCrewMember2Id(), contract.getCrewMember3Id())) {
            if (memberId != null) {
                aiCharacterRepository.findById(memberId).ifPresent(member -> {
                    member.setTotalExperience(member.getTotalExperience() + XP_PER_CONTRACT_COMPLETED);
                    aiCharacterRepository.save(member);
                    log.info("Awarded {} XP to crew member {} for contract {}", XP_PER_CONTRACT_COMPLETED, member.getName(), contractId);
                });
            }
        }

        BuildContract saved = contractRepository.save(contract);
        log.info("Completed contract {} ({})", contractId, contract.getTargetItemName());
        return saved;
    }

    /**
     * Mark a contract as failed with a reason.
     *
     * @param contractId the contract to fail
     * @param reason the failure reason
     * @return the failed contract
     */
    @Transactional
    public BuildContract failContract(Long contractId, String reason) {
        Optional<BuildContract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Contract not found: " + contractId);
        }
        BuildContract contract = opt.get();
        if (contract.getStatus() == BuildContractStatus.COMPLETED) {
            throw new IllegalStateException("Completed contract cannot be failed");
        }

        contract.setStatus(BuildContractStatus.FAILED);
        if (reason != null) {
            contract.setDescription((contract.getDescription() != null ? contract.getDescription() + "\n" : "") +
                    "FAILED: " + reason);
        }
        contract.setCompletedAt(LocalDateTime.now());

        BuildContract saved = contractRepository.save(contract);
        log.warn("Failed contract {}: {}", contractId, reason);
        return saved;
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    /**
     * Get pending contracts sorted by priority (highest first) for a galaxy.
     *
     * @param galaxyId the galaxy to query
     * @return list of pending contracts sorted by priority descending
     */
    public List<BuildContract> getPendingContractsByPriority(Long galaxyId) {
        List<BuildContract> contracts = contractRepository.findByStatus(BuildContractStatus.PENDING);
        return contracts.stream()
                .filter(c -> galaxyId == null || galaxyId.equals(c.getGalaxyId()))
                .sorted(Comparator.comparingInt(BuildContract::getPriority).reversed())
                .toList();
    }

    /**
     * Get all contracts for a specific crew.
     *
     * @param crewId the crew ID
     * @return list of contracts
     */
    public List<BuildContract> getContractsByCrew(Long crewId) {
        return contractRepository.findByAssignedCrewId(crewId);
    }

    /**
     * Get all contracts for a specific leader.
     *
     * @param leaderId the leader AI character ID
     * @return list of contracts
     */
    public List<BuildContract> getContractsByLeader(Long leaderId) {
        return contractRepository.findByAssignedLeaderId(leaderId);
    }

    /**
     * Get contracts by status.
     *
     * @param status the contract status
     * @return list of contracts
     */
    public List<BuildContract> getContractsByStatus(BuildContractStatus status) {
        return contractRepository.findByStatus(status);
    }

    /**
     * Get a contract by ID.
     *
     * @param contractId the contract ID
     * @return optional contract
     */
    public Optional<BuildContract> getContract(Long contractId) {
        return contractRepository.findById(contractId);
    }

    /**
     * Get contracts by type.
     *
     * @param contractType the contract type
     * @return list of contracts
     */
    public List<BuildContract> getContractsByType(BuildContract.ContractType contractType) {
        List<BuildContract> all = contractRepository.findAll();
        List<BuildContract> result = new java.util.ArrayList<>();
        for (BuildContract c : all) {
            if (c.getContractType() == contractType) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Get contracts by type and status.
     *
     * @param contractType the contract type
     * @param status the contract status
     * @return list of contracts
     */
    public List<BuildContract> getContractsByTypeAndStatus(BuildContract.ContractType contractType, BuildContractStatus status) {
        return contractRepository.findByContractTypeAndStatus(contractType, status);
    }
}
