package com.yisy.modules.doramcu.hex.repository;

import com.yisy.modules.doramcu.hex.model.MaterialCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialCostRepository extends JpaRepository<MaterialCost, Long> {

    /**
     * Find all materials of a given type (RAW_RESOURCE, REFINED_RESOURCE, CONSTRUCTION_MODULE).
     */
    List<MaterialCost> findByMaterialType(MaterialCost.MaterialCostType materialType);

    /**
     * Find a material by its name (e.g., "ORE", "METAL_ALLOY", "HULL_MODULE").
     */
    Optional<MaterialCost> findByMaterialName(String materialName);

    /**
     * Find all materials of a given type, sorted by base credit value descending.
     */
    List<MaterialCost> findByMaterialTypeOrderByBaseValueCreditsDesc(MaterialCost.MaterialCostType materialType);
}
