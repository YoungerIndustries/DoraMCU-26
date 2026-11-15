package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporateContractAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for querying CorporateContractAssignment entities.
 */
public interface CorporateContractAssignmentRepository extends JpaRepository<CorporateContractAssignment, Long> {

    /**
     * Find all assignments for a specific contract.
     */
    List<CorporateContractAssignment> findByContractId(Long contractId);

    /**
     * Find all assignments for a specific hiring record.
     */
    List<CorporateContractAssignment> findByHiringId(Long hiringId);

    /**
     * Find all assignments for a specific AI character.
     */
    List<CorporateContractAssignment> findByAiCharacterId(Long aiCharacterId);

    /**
     * Delete all assignments for a specific contract.
     */
    void deleteByContractId(Long contractId);
}
