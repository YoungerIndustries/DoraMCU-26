package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import com.yisy.modules.doramcu.model.galaxy.PlanetStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing planet station persistence.
 */
@Repository
public interface PlanetStationRepository extends JpaRepository<PlanetStationEntity, Long> {

    /** Find station by its business ID. */
    Optional<PlanetStationEntity> findByStationId(String stationId);

    /** Find all stations on a planet. */
    List<PlanetStationEntity> findByPlanetId(int planetId);

    /** Find stations on a planet owned by an empire. */
    List<PlanetStationEntity> findByPlanetIdAndOwnerEmpireId(int planetId, String ownerEmpireId);

    /** Find stations by infrastructure type on a planet. */
    List<PlanetStationEntity> findByPlanetIdAndInfrastructureType(int planetId, String infrastructureType);

    /** Delete a station by its business ID. */
    void deleteByStationId(String stationId);

    /** Delete all stations for a planet. */
    void deleteByPlanetId(int planetId);

    /** Count stations on a planet. */
    long countByPlanetId(int planetId);
}
