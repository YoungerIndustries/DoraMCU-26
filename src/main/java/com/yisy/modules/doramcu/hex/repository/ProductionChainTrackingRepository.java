package com.yisy.modules.doramcu.hex.repository;

import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionChainTrackingRepository extends JpaRepository<ProductionChainTracking, Long> {

    /**
     * Find all production chains with a given status (PLANNED, IN_PROGRESS, COMPLETED, FAILED).
     */
    List<ProductionChainTracking> findByStatus(ProductionChainTracking.ProductionChainStatus status);

    /**
     * Find the production chain for a specific target infrastructure item.
     */
    Optional<ProductionChainTracking> findByTargetItemId(Long targetItemId);

    /**
     * Find all production chains for a specific target item (may have multiple chains).
     */
    List<ProductionChainTracking> findByTargetItemIdAndStatus(Long targetItemId, ProductionChainTracking.ProductionChainStatus status);
}
