package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {

    /**
     * Find all crews with a given status for a corporation.
     */
    List<Crew> findByCorporationIdAndStatus(Long corporationId, Crew.Status status);

    /**
     * Find all crews for a corporation.
     */
    List<Crew> findByCorporationId(Long corporationId);

    /**
     * Find a crew by ID.
     */
    Optional<Crew> findById(Long id);

    /**
     * Find a crew assigned to a specific contract.
     */
    Optional<Crew> findByContractId(Long contractId);
}
