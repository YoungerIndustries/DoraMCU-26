package com.yisy.modules.doramcu.corporation.stock.repository;

import com.yisy.modules.doramcu.corporation.stock.model.StockListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockListingRepository extends JpaRepository<StockListing, Long> {

    Optional<StockListing> findByCorporationId(Long corporationId);

    Optional<StockListing> findByTickerSymbol(String tickerSymbol);

    List<StockListing> findByIsActiveTrue();

    @Query("SELECT sl FROM StockListing sl WHERE sl.isActive = true ORDER BY sl.marketCap DESC")
    List<StockListing> findAllActiveOrderByMarketCapDesc();

    @Query("SELECT sl FROM StockListing sl WHERE sl.isActive = true ORDER BY sl.volume24h DESC")
    List<StockListing> findAllActiveOrderByVolumeDesc();
}
