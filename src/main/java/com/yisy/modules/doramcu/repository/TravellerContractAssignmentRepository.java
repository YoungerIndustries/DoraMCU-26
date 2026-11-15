package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.TravellerContractAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying Traveller character contract assignments.
 */
@Repository
public interface TravellerContractAssignmentRepository extends JpaRepository<TravellerContractAssignment, Long> {

    /**
     * Find all assignments for a specific contract.
     *
     * @param contractId the contract ID
     * @return list of assignments
     */
    List<TravellerContractAssignment> findByContractId(Long contractId);

    /**
     * Find all assignments for a specific Traveller character.
     *
     * @param travellerCharacterId the character ID
     * @return list of assignments
     */
    List<TravellerContractAssignment> findByTravellerCharacterId(Integer travellerCharacterId);

    /**
     * Check if a character is assigned to a specific contract.
     *
     * @param contractId the contract ID
     * @param travellerCharacterId the character ID
     * @return true if assigned
     */
    boolean existsByContractIdAndTravellerCharacterId(Long contractId, Integer travellerCharacterId);
}
