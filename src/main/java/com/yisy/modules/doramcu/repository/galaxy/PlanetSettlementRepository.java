package com.yisy.modules.doramcu.repository.galaxy;

import com.yisy.modules.doramcu.model.galaxy.PlanetSettlementEntity;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing planet settlement persistence.
 */
@Repository
public interface PlanetSettlementRepository extends JpaRepository<PlanetSettlementEntity, Long> {

    /** Find settlement by its business ID. */
    Optional<PlanetSettlementEntity> findBySettlementId(String settlementId);

    /** Find all settlements on a planet. */
    List<PlanetSettlementEntity> findByPlanetId(int planetId);

    /** Find settlements on a planet owned by an empire. */
    List<PlanetSettlementEntity> findByPlanetIdAndOwnerEmpireId(int planetId, String ownerEmpireId);

    /** Delete a settlement by its business ID. */
    void deleteBySettlementId(String settlementId);

    /** Delete all settlements for a planet. */
    void deleteByPlanetId(int planetId);

    /** Count settlements on a planet. */
    long countByPlanetId(int planetId);
}
