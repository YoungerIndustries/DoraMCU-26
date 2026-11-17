package com.yisy.modules.doramcu.corporation.stock.service;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.corporation.stock.model.StockHolding;
import com.yisy.modules.doramcu.corporation.stock.model.StockListing;
import com.yisy.modules.doramcu.corporation.stock.model.StockOrder;
import com.yisy.modules.doramcu.corporation.stock.model.StockTrade;
import com.yisy.modules.doramcu.corporation.stock.repository.StockHoldingRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockListingRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockOrderRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockTradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that manages player and corporation stock portfolios.
 * <p>
 * Features:
 * - Portfolio overview with holdings and total value
 * - Buy/sell execution through the exchange
 * - Dividend receipt tracking
 * - Trade history per player
 * </p>
 */
@Service
public class StockPortfolioService {

    private static final Logger log = LoggerFactory.getLogger(StockPortfolioService.class);

    private final StockHoldingRepository stockHoldingRepository;
    private final StockListingRepository stockListingRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockTradeRepository stockTradeRepository;
    private final StockExchangeService stockExchangeService;

    public StockPortfolioService(
            StockHoldingRepository stockHoldingRepository,
            StockListingRepository stockListingRepository,
            StockOrderRepository stockOrderRepository,
            StockTradeRepository stockTradeRepository,
            StockExchangeService stockExchangeService) {
        this.stockHoldingRepository = stockHoldingRepository;
        this.stockListingRepository = stockListingRepository;
        this.stockOrderRepository = stockOrderRepository;
        this.stockTradeRepository = stockTradeRepository;
        this.stockExchangeService = stockExchangeService;
    }

    /**
     * Get the full portfolio for a player.
     *
     * @param playerId the player ID
     * @return map with holdings, total value, and dividends received
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlayerPortfolio(Long playerId) {
        List<StockHolding> holdings = stockHoldingRepository.findByOwnerIdAndOwnerType(playerId, "PLAYER");

        List<Map<String, Object>> holdingDetails = new ArrayList<>();
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalDividendsReceived = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (StockHolding holding : holdings) {
            Optional<StockListing> listingOpt = stockListingRepository.findById(holding.getStockListingId());
            if (listingOpt.isEmpty()) continue;

            StockListing listing = listingOpt.get();
            BigDecimal currentValue = holding.getSharesOwned()
                    .compareTo(0L) > 0
                    ? listing.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getSharesOwned()))
                    : BigDecimal.ZERO;

            BigDecimal invested = holding.getAverageBuyPrice().multiply(BigDecimal.valueOf(holding.getSharesOwned()));
            totalPortfolioValue = totalPortfolioValue.add(currentValue);
            totalDividendsReceived = totalDividendsReceived.add(holding.getTotalDividendsReceived());
            totalInvested = totalInvested.add(invested);

            Map<String, Object> detail = new HashMap<>();
            detail.put("stockListingId", holding.getStockListingId());
            detail.put("tickerSymbol", listing.getTickerSymbol());
            detail.put("sharesOwned", holding.getSharesOwned());
            detail.put("averageBuyPrice", holding.getAverageBuyPrice());
            detail.put("currentPrice", listing.getCurrentPrice());
            detail.put("currentValue", currentValue);
            detail.put("unrealizedPnl", currentValue.subtract(invested));
            detail.put("dividendsReceived", holding.getTotalDividendsReceived());
            detail.put("lastUpdated", holding.getLastUpdated());
            holdingDetails.add(detail);
        }

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("playerId", playerId);
        portfolio.put("holdings", holdingDetails);
        portfolio.put("totalHoldings", holdingDetails.size());
        portfolio.put("totalPortfolioValue", totalPortfolioValue);
        portfolio.put("totalInvested", totalInvested);
        portfolio.put("totalUnrealizedPnl", totalPortfolioValue.subtract(totalInvested));
        portfolio.put("totalDividendsReceived", totalDividendsReceived);
        return portfolio;
    }

    /**
     * Get the portfolio for a corporation.
     *
     * @param corporationId the corporation ID
     * @return map with holdings and total value
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCorporationPortfolio(Long corporationId) {
        List<StockHolding> holdings = stockHoldingRepository.findByOwnerIdAndOwnerType(corporationId, "CORPORATION");

        List<Map<String, Object>> holdingDetails = new ArrayList<>();
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        for (StockHolding holding : holdings) {
            Optional<StockListing> listingOpt = stockListingRepository.findById(holding.getStockListingId());
            if (listingOpt.isEmpty()) continue;

            StockListing listing = listingOpt.get();
            BigDecimal currentValue = listing.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getSharesOwned()));
            totalPortfolioValue = totalPortfolioValue.add(currentValue);

            Map<String, Object> detail = new HashMap<>();
            detail.put("stockListingId", holding.getStockListingId());
            detail.put("tickerSymbol", listing.getTickerSymbol());
            detail.put("sharesOwned", holding.getSharesOwned());
            detail.put("averageBuyPrice", holding.getAverageBuyPrice());
            detail.put("currentPrice", listing.getCurrentPrice());
            detail.put("currentValue", currentValue);
            holdingDetails.add(detail);
        }

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("corporationId", corporationId);
        portfolio.put("holdings", holdingDetails);
        portfolio.put("totalPortfolioValue", totalPortfolioValue);
        return portfolio;
    }

    /**
     * Place a buy order on behalf of a player.
     *
     * @param stockListingId the stock listing ID
     * @param playerId the player ID
     * @param orderStyle "MARKET" or "LIMIT"
     * @param limitPrice limit price (required for LIMIT orders)
     * @param quantity number of shares to buy
     * @return the created order
     */
    @Transactional
    public StockOrder buy(Long stockListingId, Long playerId, String orderStyle,
                          BigDecimal limitPrice, Long quantity) {
        return stockExchangeService.placeOrder(stockListingId, playerId, "BUY", orderStyle, limitPrice, quantity);
    }

    /**
     * Place a sell order on behalf of a player.
     *
     * @param stockListingId the stock listing ID
     * @param playerId the player ID
     * @param orderStyle "MARKET" or "LIMIT"
     * @param limitPrice limit price (required for LIMIT orders)
     * @param quantity number of shares to sell
     * @return the created order
     */
    @Transactional
    public StockOrder sell(Long stockListingId, Long playerId, String orderStyle,
                           BigDecimal limitPrice, Long quantity) {
        return stockExchangeService.placeOrder(stockListingId, playerId, "SELL", orderStyle, limitPrice, quantity);
    }

    /**
     * Get trade history for a player.
     *
     * @param playerId the player ID
     * @param limit maximum number of trades to return
     * @return list of trades
     */
    @Transactional(readOnly = true)
    public List<StockTrade> getTradeHistory(Long playerId, int limit) {
        List<StockTrade> trades = new ArrayList<>();
        trades.addAll(stockTradeRepository.findByBuyerId(playerId));
        trades.addAll(stockTradeRepository.findBySellerId(playerId));

        // Sort by timestamp descending and limit
        return trades.stream()
                .sorted(Comparator.comparing(StockTrade::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get active orders for a player.
     *
     * @param playerId the player ID
     * @return list of active orders
     */
    @Transactional(readOnly = true)
    public List<StockOrder> getActiveOrders(Long playerId) {
        return stockOrderRepository.findByPlayerIdAndStatus(playerId, "PENDING");
    }

    /**
     * Get dividend history for a player.
     *
     * @param playerId the player ID
     * @return map with total dividends and per-stock breakdown
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDividendHistory(Long playerId) {
        List<StockHolding> holdings = stockHoldingRepository.findByOwnerIdAndOwnerType(playerId, "PLAYER");

        BigDecimal totalDividends = BigDecimal.ZERO;
        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (StockHolding holding : holdings) {
            if (holding.getTotalDividendsReceived().compareTo(BigDecimal.ZERO) > 0) {
                Optional<StockListing> listingOpt = stockListingRepository.findById(holding.getStockListingId());
                String ticker = listingOpt.map(StockListing::getTickerSymbol).orElse("UNKNOWN");

                Map<String, Object> item = new HashMap<>();
                item.put("tickerSymbol", ticker);
                item.put("dividendsReceived", holding.getTotalDividendsReceived());
                item.put("sharesOwned", holding.getSharesOwned());
                breakdown.add(item);

                totalDividends = totalDividends.add(holding.getTotalDividendsReceived());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("totalDividends", totalDividends);
        result.put("breakdown", breakdown);
        return result;
    }

    /**
     * Get price history for a stock listing (based on trades).
     *
     * @param corporationId the corporation ID
     * @param days number of days of history
     * @return list of price points with timestamps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPriceHistory(Long corporationId, int days) {
        Optional<StockListing> listingOpt = stockListingRepository.findByCorporationId(corporationId);
        if (listingOpt.isEmpty()) {
            return Collections.emptyList();
        }

        StockListing listing = listingOpt.get();
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<StockTrade> trades = stockTradeRepository
                .findByStockListingIdAndTimestampAfter(listing.getId(), since);

        List<Map<String, Object>> pricePoints = new ArrayList<>();
        for (StockTrade trade : trades) {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", trade.getTimestamp());
            point.put("price", trade.getPricePerShare());
            point.put("volume", trade.getSharesTraded());
            pricePoints.add(point);
        }

        // Sort by timestamp ascending
        pricePoints.sort(Comparator.comparing(m -> (LocalDateTime) m.get("timestamp")));
        return pricePoints;
    }

    /**
     * Check if a player owns shares in a specific stock.
     *
     * @param playerId the player ID
     * @param stockListingId the stock listing ID
     * @return number of shares owned, 0 if none
     */
    @Transactional(readOnly = true)
    public long getSharesOwned(Long playerId, Long stockListingId) {
        Optional<StockHolding> holding = stockHoldingRepository
                .findByOwnerIdAndOwnerTypeAndStockListingId(playerId, "PLAYER", stockListingId);
        return holding.map(StockHolding::getSharesOwned).orElse(0L);
    }
}
