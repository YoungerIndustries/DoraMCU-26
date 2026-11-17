package com.yisy.modules.doramcu.corporation.stock.repository;

import com.yisy.modules.doramcu.corporation.stock.model.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {

    Optional<StockHolding> findByOwnerIdAndStockListingId(Long ownerId, Long stockListingId);

    List<StockHolding> findByOwnerIdAndOwnerType(Long ownerId, String ownerType);

    List<StockHolding> findByStockListingId(Long stockListingId);

    Optional<StockHolding> findByOwnerIdAndOwnerTypeAndStockListingId(Long ownerId, String ownerType, Long stockListingId);

    List<StockHolding> findByOwnerIdAndOwnerTypeAndSharesOwnedGreaterThan(Long ownerId, String ownerType, long shares);
}
