package com.yisy.modules.doramcu.corporation.stock.repository;

import com.yisy.modules.doramcu.corporation.stock.model.StockTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTradeRepository extends JpaRepository<StockTrade, Long> {

    List<StockTrade> findByStockListingId(Long stockListingId);

    List<StockTrade> findByBuyerId(Long buyerId);

    List<StockTrade> findBySellerId(Long sellerId);

    List<StockTrade> findByStockListingIdOrderByTimestampDesc(Long stockListingId);

    @Query("SELECT st FROM StockTrade st WHERE st.timestamp >= :since ORDER BY st.timestamp DESC")
    List<StockTrade> findByTimestampAfterOrderByTimestampDesc(LocalDateTime since);

    @Query("SELECT st FROM StockTrade st WHERE st.stockListingId = :listingId AND st.timestamp >= :since ORDER BY st.timestamp DESC")
    List<StockTrade> findByStockListingIdAndTimestampAfter(Long listingId, LocalDateTime since);
}
