package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.GalaxyTreasury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GalaxyTreasuryRepository extends JpaRepository<GalaxyTreasury, Long> {

    /**
     * Find the treasury for a given galaxy.
     */
    Optional<GalaxyTreasury> findByGalaxyId(Long galaxyId);

    /**
     * Check if a treasury exists for a galaxy.
     */
    boolean existsByGalaxyId(Long galaxyId);
}
