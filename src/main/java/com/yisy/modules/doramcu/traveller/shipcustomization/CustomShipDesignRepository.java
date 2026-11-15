package com.yisy.modules.doramcu.traveller.shipcustomization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for custom ship designs.
 */
@Repository
public interface CustomShipDesignRepository extends JpaRepository<CustomShipDesign, Long> {

    /**
     * Find all designs owned by a specific owner.
     */
    List<CustomShipDesign> findByOwnerId(String ownerId);

    /**
     * Find designs by name containing the given string (case-insensitive).
     */
    List<CustomShipDesign> findByNameContainingIgnoreCase(String name);

    /**
     * Find validated designs only.
     */
    List<CustomShipDesign> findByValidatedTrue();
}
