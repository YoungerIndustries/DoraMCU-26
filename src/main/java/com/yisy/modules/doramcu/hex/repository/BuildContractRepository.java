package com.yisy.modules.doramcu.hex.repository;

import com.yisy.modules.doramcu.hex.model.BuildContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildContractRepository extends JpaRepository<BuildContract, Long> {

    /**
     * Find all contracts with a given status (PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, FAILED).
     */
    List<BuildContract> findByStatus(BuildContract.BuildContractStatus status);

    /**
     * Find contracts assigned to a specific crew group.
     */
    List<BuildContract> findByAssignedCrewId(Long assignedCrewId);

    /**
     * Find contracts assigned to a specific leader (AI Character).
     */
    List<BuildContract> findByAssignedLeaderId(Long assignedLeaderId);

    /**
     * Find contracts with a given status and priority greater than or equal to the threshold.
     * Useful for priority-based dispatch.
     */
    List<BuildContract> findByStatusAndPriorityGreaterThanEqual(BuildContract.BuildContractStatus status, int priority);

    /**
     * Find all contracts in a specific galaxy.
     */
    List<BuildContract> findByGalaxyId(Long galaxyId);

    /**
     * Find contracts with a given status on a specific planet.
     */
    List<BuildContract> findByStatusAndPlanetId(BuildContract.BuildContractStatus status, Long planetId);

    /**
     * Find pending contracts that have not yet been assigned to a crew.
     */
    List<BuildContract> findByStatusAndAssignedCrewIdIsNull(BuildContract.BuildContractStatus status);

    /**
     * Find contracts by type and status.
     */
    List<BuildContract> findByContractTypeAndStatus(BuildContract.ContractType contractType, BuildContract.BuildContractStatus status);
}
