package com.yisy.modules.doramcu.corporation.stock.repository;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.corporation.stock.model.StockOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {

    List<StockOrder> findByStockListingIdAndStatus(Long stockListingId, String status);

    List<StockOrder> findByPlayerIdAndStatus(Long playerId, String status);

    List<StockOrder> findByStockListingIdAndOrderTypeAndStatus(Long stockListingId, String orderType, String status);

    @Query("SELECT so FROM StockOrder so WHERE so.status = 'PENDING' AND so.expiresAt < :now")
    List<StockOrder> findExpiredOrders(LocalDateTime now);

    @Query("SELECT so FROM StockOrder so WHERE so.stockListingId = :listingId AND so.orderType = :orderType AND so.status = 'PENDING' ORDER BY so.createdAt ASC")
    List<StockOrder> findActiveOrdersByType(Long listingId, String orderType);

    List<StockOrder> findByStatusAndExpiresAtBefore(String status, LocalDateTime expiresAt);
}
