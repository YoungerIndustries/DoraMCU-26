package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.GalaxyBond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GalaxyBondRepository extends JpaRepository<GalaxyBond, Long> {

    /**
     * Find all outstanding (unredeemed) bonds for a galaxy.
     */
    @Query("SELECT gb FROM GalaxyBond gb WHERE gb.galaxy.id = :galaxyId AND gb.redeemed = false")
    List<GalaxyBond> findOutstandingByGalaxyId(@Param("galaxyId") Long galaxyId);

    /**
     * Find all bonds (redeemed and outstanding) for a galaxy.
     */
    @Query("SELECT gb FROM GalaxyBond gb WHERE gb.galaxy.id = :galaxyId ORDER BY gb.issuedAt DESC")
    List<GalaxyBond> findByGalaxyId(@Param("galaxyId") Long galaxyId);

    /**
     * Calculate the total outstanding debt for a galaxy.
     */
    @Query("SELECT COALESCE(SUM(gb.totalOutstanding), 0) FROM GalaxyBond gb WHERE gb.galaxy.id = :galaxyId AND gb.redeemed = false")
    BigDecimal sumTotalOutstandingByGalaxyId(@Param("galaxyId") Long galaxyId);

    /**
     * Count outstanding bonds for a galaxy.
     */
    @Query("SELECT COUNT(gb) FROM GalaxyBond gb WHERE gb.galaxy.id = :galaxyId AND gb.redeemed = false")
    long countOutstandingByGalaxyId(@Param("galaxyId") Long galaxyId);
}
