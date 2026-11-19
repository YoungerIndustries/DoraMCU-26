package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing planet contract persistence.
 */
@Repository
public interface PlanetContractRepository extends JpaRepository<PlanetContractEntity, Long> {

    /** Find all contracts on a specific planet. */
    List<PlanetContractEntity> findByPlanetId(int planetId);

    /** Find contract by its unique ID. */
    java.util.Optional<PlanetContractEntity> findByContractId(String contractId);

    /** Find contracts by status on a planet. */
    List<PlanetContractEntity> findByPlanetIdAndStatus(int planetId, String status);

    /** Find contracts assigned to a corporation on a planet. */
    List<PlanetContractEntity> findByPlanetIdAndAssignedCorporationId(int planetId, String corporationId);

    /** Delete all contracts for a specific planet. */
    void deleteByPlanetId(int planetId);

    /** Count contracts on a specific planet. */
    long countByPlanetId(int planetId);
}
