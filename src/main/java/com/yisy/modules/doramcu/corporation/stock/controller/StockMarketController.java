package com.yisy.modules.doramcu.corporation.stock.controller;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.corporation.stock.model.StockListing;
import com.yisy.modules.doramcu.corporation.stock.model.StockOrder;
import com.yisy.modules.doramcu.corporation.stock.model.StockTrade;
import com.yisy.modules.doramcu.corporation.stock.repository.StockListingRepository;
import com.yisy.modules.doramcu.corporation.stock.service.StockExchangeService;
import com.yisy.modules.doramcu.corporation.stock.service.StockPriceService;
import com.yisy.modules.doramcu.corporation.stock.service.StockPortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for stock market operations.
 * <p>
 * Endpoints:
 * - GET /listings — all active stock listings
 * - GET /listings/{corporationId} — specific corporation stock
 * - POST /orders — place buy/sell order
 * - DELETE /orders/{id} — cancel order
 * - GET /portfolio/{playerId} — player's stock portfolio
 * - POST /ipo/{corporationId} — apply for IPO
 * - GET /trades — recent trade history
 * - GET /prices/{corporationId}/history — price history chart data
 * </p>
 */
@RestController
@RequestMapping("/api/v1/stock-market")
public class StockMarketController {

    private final StockExchangeService stockExchangeService;
    private final StockPortfolioService stockPortfolioService;
    private final StockPriceService stockPriceService;
    private final StockListingRepository stockListingRepository;

    public StockMarketController(
            StockExchangeService stockExchangeService,
            StockPortfolioService stockPortfolioService,
            StockPriceService stockPriceService,
            StockListingRepository stockListingRepository) {
        this.stockExchangeService = stockExchangeService;
        this.stockPortfolioService = stockPortfolioService;
        this.stockPriceService = stockPriceService;
        this.stockListingRepository = stockListingRepository;
    }

    /**
     * Get all active stock listings with current prices.
     */
    @GetMapping("/listings")
    public List<StockListing> getAllListings() {
        return stockListingRepository.findByIsActiveTrue();
    }

    /**
     * Get stock listing for a specific corporation.
     */
    @GetMapping("/listings/{corporationId}")
    public ResponseEntity<?> getListingByCorporation(@PathVariable Long corporationId) {
        Optional<StockListing> listing = stockListingRepository.findByCorporationId(corporationId);
        if (listing.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No stock listing found for corporation: " + corporationId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(listing.get());
    }

    /**
     * Place a buy or sell order.
     */
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> request) {
        try {
            Long stockListingId = Long.valueOf(request.get("stockListingId").toString());
            Long playerId = Long.valueOf(request.get("playerId").toString());
            String orderType = request.get("orderType").toString(); // "BUY" or "SELL"
            String orderStyle = request.get("orderStyle").toString(); // "MARKET" or "LIMIT"
            BigDecimal limitPrice = request.get("limitPrice") != null
                    ? new BigDecimal(request.get("limitPrice").toString())
                    : null;
            Long quantity = Long.valueOf(request.get("quantity").toString());

            StockOrder order = stockExchangeService.placeOrder(
                    stockListingId, playerId, orderType, orderStyle, limitPrice, quantity);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cancel a pending order.
     */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestParam Long playerId) {
        try {
            stockExchangeService.cancelOrder(orderId, playerId);
            Map<String, String> result = new HashMap<>();
            result.put("status", "cancelled");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get player's stock portfolio.
     */
    @GetMapping("/portfolio/{playerId}")
    public Map<String, Object> getPlayerPortfolio(@PathVariable Long playerId) {
        return stockPortfolioService.getPlayerPortfolio(playerId);
    }

    /**
     * Apply for IPO for a corporation.
     */
    @PostMapping("/ipo/{corporationId}")
    public ResponseEntity<?> applyForIpo(@PathVariable Long corporationId) {
        try {
            StockListing listing = stockExchangeService.applyForIpo(corporationId);
            return ResponseEntity.ok(listing);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get recent trade history.
     */
    @GetMapping("/trades")
    public List<StockTrade> getRecentTrades(
            @RequestParam(required = false) Long stockListingId,
            @RequestParam(defaultValue = "50") int limit) {
        if (stockListingId != null) {
            return stockExchangeService.getRecentTrades(stockListingId, limit);
        }
        // Return all recent trades if no listing specified
        return stockPortfolioService.getTradeHistory(null, limit);
    }

    /**
     * Get price history for a corporation's stock.
     */
    @GetMapping("/prices/{corporationId}/history")
    public List<Map<String, Object>> getPriceHistory(
            @PathVariable Long corporationId,
            @RequestParam(defaultValue = "30") int days) {
        return stockPortfolioService.getPriceHistory(corporationId, days);
    }

    /**
     * Get active orders for a player.
     */
    @GetMapping("/orders/{playerId}/active")
    public List<StockOrder> getActiveOrders(@PathVariable Long playerId) {
        return stockPortfolioService.getActiveOrders(playerId);
    }

    /**
     * Get dividend history for a player.
     */
    @GetMapping("/dividends/{playerId}")
    public Map<String, Object> getDividendHistory(@PathVariable Long playerId) {
        return stockPortfolioService.getDividendHistory(playerId);
    }

    /**
     * Update stock prices for all active listings (admin endpoint).
     */
    @PostMapping("/admin/update-prices")
    public Map<String, String> updateAllPrices() {
        stockPriceService.updateAllPrices();
        Map<String, String> result = new HashMap<>();
        result.put("status", "prices updated");
        return result;
    }

    /**
     * Process quarterly dividends (admin endpoint).
     */
    @PostMapping("/admin/process-dividends")
    public Map<String, String> processDividends() {
        stockExchangeService.processQuarterlyDividends();
        Map<String, String> result = new HashMap<>();
        result.put("status", "dividends processed");
        return result;
    }

    /**
     * Cancel expired orders (admin endpoint).
     */
    @PostMapping("/admin/cancel-expired-orders")
    public Map<String, String> cancelExpiredOrders() {
        stockExchangeService.cancelExpiredOrders();
        Map<String, String> result = new HashMap<>();
        result.put("status", "expired orders cancelled");
        return result;
    }
}
