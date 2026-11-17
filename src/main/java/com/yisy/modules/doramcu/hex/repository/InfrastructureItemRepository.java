package com.yisy.modules.doramcu.hex.repository;

import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfrastructureItemRepository extends JpaRepository<InfrastructureItem, Long> {

    /**
     * Find all items of a given type (PLANET_FACILITY, SPACE_STATION, STATION_MODULE).
     */
    List<InfrastructureItem> findByItemType(InfrastructureItem.InfrastructureItemType itemType);

    /**
     * Find a specific item by type and name.
     */
    Optional<InfrastructureItem> findByItemTypeAndItemName(InfrastructureItem.InfrastructureItemType itemType, String itemName);

    /**
     * Find all items of a given type, ordered by name.
     */
    List<InfrastructureItem> findByItemTypeOrderByItemNameAsc(InfrastructureItem.InfrastructureItemType itemType);

    /**
     * Find items by name containing a substring (case-insensitive search via Spring Data JPA).
     */
    List<InfrastructureItem> findByItemNameContainingIgnoreCase(String itemName);
}
