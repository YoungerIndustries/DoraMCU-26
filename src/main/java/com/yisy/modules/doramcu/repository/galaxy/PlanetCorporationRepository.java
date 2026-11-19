package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing planet corporation persistence.
 * <p>
 * Provides access to saved Transport Corporations so the Galaxy Overlord can
 * recreate the corporate landscape of a planet without random regeneration.
 * </p>
 */
@Repository
public interface PlanetCorporationRepository extends JpaRepository<PlanetCorporationEntity, Long> {

    /** Find all corporations operating on a specific planet. */
    List<PlanetCorporationEntity> findByPlanetId(int planetId);

    /** Find corporation by its unique ID. */
    PlanetCorporationEntity findByCorporationId(String corporationId);

    /** Find corporations owned by a specific empire on a planet. */
    List<PlanetCorporationEntity> findByPlanetIdAndOwnerEmpireId(int planetId, String ownerEmpireId);

    /** Delete all corporations for a specific planet. */
    void deleteByPlanetId(int planetId);

    /** Count corporations on a specific planet. */
    long countByPlanetId(int planetId);

    /** Find all distinct planet IDs that have corporation data. */
    @Query("SELECT DISTINCT c.planetId FROM PlanetCorporationEntity c")
    java.util.Set<Integer> findDistinctPlanetIds();
}
