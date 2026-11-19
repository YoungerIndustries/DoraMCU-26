package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.model.misc.Planet;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository for managing planet terrain cell persistence.
 */
@Repository
public interface PlanetTerrainCellRepository extends JpaRepository<PlanetTerrainCell, Long> {

    /** Find all terrain cells for a planet. */
    List<PlanetTerrainCell> findByPlanetId(int planetId);

    /** Find a terrain cell by planet and hex coordinates. */
    PlanetTerrainCell findByPlanetIdAndHexXAndHexYAndHexZ(
            int planetId, int hexX, int hexY, int hexZ);

    /** Find terrain cells occupied by a settlement. */
    List<PlanetTerrainCell> findByPlanetIdAndSettlementId(int planetId, String settlementId);

    /** Find terrain cells occupied by an industry. */
    List<PlanetTerrainCell> findByPlanetIdAndIndustryId(int planetId, String industryId);

    /** Delete all terrain cells for a planet. */
    void deleteByPlanetId(int planetId);

    /** Count terrain cells for a planet. */
    long countByPlanetId(int planetId);

    /** Find terrain cells with a specific infrastructure type on a planet. */
    List<PlanetTerrainCell> findByPlanetIdAndInfrastructure(int planetId, String infrastructure);

    /** Find all distinct planet IDs that have terrain data. */
    @Query("SELECT DISTINCT t.planetId FROM PlanetTerrainCell t")
    Set<Integer> findDistinctPlanetIds();
}
