package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorporateContractRepository extends JpaRepository<CorporateContract, Long> {

    /**
     * Find all available contracts for a corporation.
     */
    @EntityGraph(attributePaths = {"assignedCrewIds"})
    List<CorporateContract> findByCorporationIdAndStatus(Long corporationId, CorporateContract.Status status);

    /**
     * Find all available contracts (not assigned to any corporation yet).
     */
    List<CorporateContract> findByStatus(CorporateContract.Status status);

    /**
     * Find contracts by type and status.
     */
    List<CorporateContract> findByTypeAndStatus(CorporateContract.ContractType type, CorporateContract.Status status);

    /**
     * Find active contracts for a corporation with crew assignments eagerly loaded.
     */
    @EntityGraph(attributePaths = {"assignedCrewIds"})
    List<CorporateContract> findByCorporationIdAndStatusIn(Long corporationId, List<CorporateContract.Status> statuses);
}
