package com.yisy.modules.doramcu.traveller.shipcustomization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ship components.
 */
@Repository
public interface ShipComponentRepository extends JpaRepository<ShipComponent, Long> {

    /**
     * Find all components of a given type.
     */
    List<ShipComponent> findByType(ComponentType type);

    /**
     * Find components by type and minimum tech level.
     */
    List<ShipComponent> findByTypeAndMinTechLevelLessThanEqual(ComponentType type, int techLevel);

    /**
     * Find components by name containing the given string (case-insensitive).
     */
    List<ShipComponent> findByNameContainingIgnoreCase(String name);
}
