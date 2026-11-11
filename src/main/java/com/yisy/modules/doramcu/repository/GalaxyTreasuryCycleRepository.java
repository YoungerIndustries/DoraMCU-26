package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.GalaxyTreasuryCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalaxyTreasuryCycleRepository extends JpaRepository<GalaxyTreasuryCycle, Long> {

    /**
     * Find all cycles for a galaxy, ordered by cycle number.
     */
    @Query("SELECT gtc FROM GalaxyTreasuryCycle gtc WHERE gtc.galaxy.id = :galaxyId ORDER BY gtc.cycleNumber ASC")
    List<GalaxyTreasuryCycle> findByGalaxyIdOrderByCycleNumberAsc(@Param("galaxyId") Long galaxyId);

    /**
     * Find the latest cycle for a galaxy.
     */
    @Query("SELECT gtc FROM GalaxyTreasuryCycle gtc WHERE gtc.galaxy.id = :galaxyId ORDER BY gtc.cycleNumber DESC LIMIT 1")
    GalaxyTreasuryCycle findLatestByGalaxyId(@Param("galaxyId") Long galaxyId);

    /**
     * Find cycles for a galaxy within a cycle range.
     */
    @Query("SELECT gtc FROM GalaxyTreasuryCycle gtc WHERE gtc.galaxy.id = :galaxyId AND gtc.cycleNumber BETWEEN :start AND :end ORDER BY gtc.cycleNumber ASC")
    List<GalaxyTreasuryCycle> findByGalaxyIdAndCycleRange(
            @Param("galaxyId") Long galaxyId,
            @Param("start") Long start,
            @Param("end") Long end);
}
