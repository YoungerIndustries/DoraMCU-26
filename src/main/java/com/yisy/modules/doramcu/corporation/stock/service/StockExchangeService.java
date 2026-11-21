package com.yisy.modules.doramcu.corporation.stock.service;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.corporation.stock.model.DividendPayment;
import com.yisy.modules.doramcu.corporation.stock.model.StockHolding;
import com.yisy.modules.doramcu.corporation.stock.model.StockListing;
import com.yisy.modules.doramcu.corporation.stock.model.StockOrder;
import com.yisy.modules.doramcu.corporation.stock.model.StockTrade;
import com.yisy.modules.doramcu.corporation.stock.repository.DividendPaymentRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockHoldingRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockListingRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockOrderRepository;
import com.yisy.modules.doramcu.corporation.stock.repository.StockTradeRepository;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core stock exchange service handling IPO, order matching, and dividend distribution.
 * <p>
 * Features:
 * - IPO application and approval
 * - Order book management with market and limit orders
 * - Order matching engine
 * - 5% market tax collection
 * - Quarterly dividend calculation and distribution
 * </p>
 */
@Service
public class StockExchangeService {

    private static final Logger log = LoggerFactory.getLogger(StockExchangeService.class);

    // Market tax rate (5%)
    private static final BigDecimal MARKET_TAX_RATE = new BigDecimal("0.05");

    // Minimum credit balance for IPO
    private static final long MIN_IPO_CREDIT_BALANCE = 50000L;

    // Default total shares for IPO
    private static final long DEFAULT_TOTAL_SHARES = 1000000L;

    // Default public percentage for IPO (40%)
    private static final double DEFAULT_PUBLIC_PERCENTAGE = 0.4;

    // Maximum shares per trade
    private static final long MAX_SHARES_PER_TRADE = 100000L;

    private final StockListingRepository stockListingRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockTradeRepository stockTradeRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final DividendPaymentRepository dividendPaymentRepository;
    private final EveCorporationRepository corporationRepository;
    private final CorporationEconomyService economyService;
    private final StockPriceService stockPriceService;

    public StockExchangeService(
            StockListingRepository stockListingRepository,
            StockOrderRepository stockOrderRepository,
            StockTradeRepository stockTradeRepository,
            StockHoldingRepository stockHoldingRepository,
            DividendPaymentRepository dividendPaymentRepository,
            EveCorporationRepository corporationRepository,
            CorporationEconomyService economyService,
            StockPriceService stockPriceService) {
        this.stockListingRepository = stockListingRepository;
        this.stockOrderRepository = stockOrderRepository;
        this.stockTradeRepository = stockTradeRepository;
        this.stockHoldingRepository = stockHoldingRepository;
        this.dividendPaymentRepository = dividendPaymentRepository;
        this.corporationRepository = corporationRepository;
        this.economyService = economyService;
        this.stockPriceService = stockPriceService;
    }

    // ---- IPO Management ----

    /**
     * Apply for IPO for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the created stock listing
     * @throws IllegalArgumentException if corporation doesn't meet IPO requirements
     */
    @Transactional
    public StockListing applyForIpo(Long corporationId) {
        // Check if already listed
        if (stockListingRepository.findByCorporationId(corporationId).isPresent()) {
            throw new IllegalArgumentException("Corporation is already listed: " + corporationId);
        }

        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        // Check minimum credit balance
        long creditBalance = corporation.getCreditBalance() != null ? corporation.getCreditBalance() : 0L;
        if (creditBalance < MIN_IPO_CREDIT_BALANCE) {
            throw new IllegalArgumentException(
                    "Corporation must have at least " + MIN_IPO_CREDIT_BALANCE + " credits for IPO. Current: " + creditBalance);
        }

        // Calculate IPO price
        BigDecimal ipoPrice = stockPriceService.calculateIpoPrice(corporationId);

        // Create listing
        StockListing listing = new StockListing();
        listing.setCorporationId(corporationId);
        listing.setTickerSymbol(corporation.getTicker());
        listing.setTotalShares(DEFAULT_TOTAL_SHARES);
        listing.setPublicShares((long) (DEFAULT_TOTAL_SHARES * DEFAULT_PUBLIC_PERCENTAGE));
        listing.setInitialPrice(ipoPrice);
        listing.setCurrentPrice(ipoPrice);
        listing.setDayHigh(ipoPrice);
        listing.setDayLow(ipoPrice);
        listing.setVolume24h(0L);
        listing.setMarketCap(ipoPrice.multiply(BigDecimal.valueOf(DEFAULT_TOTAL_SHARES)));
        listing.setDividendYield(0.0);
        listing.setIpoDate(LocalDateTime.now());
        listing.setIsActive(true);
        listing.setLastPriceUpdate(LocalDateTime.now());

        listing = stockListingRepository.save(listing);

        log.info("IPO approved for corporation {}: ticker={}, initial price={}, public shares={}",
                corporationId, corporation.getTicker(), ipoPrice, listing.getPublicShares());

        return listing;
    }

    /**
     * Delist a corporation's stock.
     *
     * @param corporationId the corporation ID
     */
    @Transactional
    public void delistStock(Long corporationId) {
        StockListing listing = stockListingRepository.findByCorporationId(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Stock listing not found: " + corporationId));

        listing.setIsActive(false);
        stockListingRepository.save(listing);

        // Cancel all pending orders for this listing
        List<StockOrder> pendingOrders = stockOrderRepository.findByStockListingIdAndStatus(listing.getId(), "PENDING");
        for (StockOrder order : pendingOrders) {
            order.setStatus("CANCELLED");
            stockOrderRepository.save(order);
        }

        log.info("Delisted stock for corporation {}. Cancelled {} pending orders.", corporationId, pendingOrders.size());
    }

    // ---- Order Management ----

    /**
     * Place a new stock order.
     *
     * @param stockListingId the stock listing ID
     * @param playerId the player placing the order
     * @param orderType "BUY" or "SELL"
     * @param orderStyle "MARKET" or "LIMIT"
     * @param limitPrice limit price (required for LIMIT orders)
     * @param quantity number of shares
     * @return the created order
     */
    @Transactional
    public StockOrder placeOrder(Long stockListingId, Long playerId, String orderType, String orderStyle,
                                  BigDecimal limitPrice, Long quantity) {
        // Validate listing
        StockListing listing = stockListingRepository.findById(stockListingId)
                .orElseThrow(() -> new IllegalArgumentException("Stock listing not found: " + stockListingId));

        if (!listing.getIsActive()) {
            throw new IllegalArgumentException("Stock is not actively trading: " + listing.getTickerSymbol());
        }

        // Validate order type
        if (!"BUY".equals(orderType) && !"SELL".equals(orderType)) {
            throw new IllegalArgumentException("Order type must be BUY or SELL");
        }

        // Validate order style
        if (!"MARKET".equals(orderStyle) && !"LIMIT".equals(orderStyle)) {
            throw new IllegalArgumentException("Order style must be MARKET or LIMIT");
        }

        // Validate limit price
        if ("LIMIT".equals(orderStyle) && (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Limit price must be positive for LIMIT orders");
        }

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > MAX_SHARES_PER_TRADE) {
            throw new IllegalArgumentException("Maximum shares per trade: " + MAX_SHARES_PER_TRADE);
        }

        // For SELL orders, check if player has enough shares
        if ("SELL".equals(orderType)) {
            Optional<StockHolding> holdingOpt = stockHoldingRepository
                    .findByOwnerIdAndOwnerTypeAndStockListingId(playerId, "PLAYER", stockListingId);
            if (holdingOpt.isEmpty() || holdingOpt.get().getSharesOwned() < quantity) {
                long available = holdingOpt.map(StockHolding::getSharesOwned).orElse(0L);
                throw new IllegalArgumentException("Insufficient shares. Available: " + available + ", Requested: " + quantity);
            }
        }

        // Create order
        StockOrder order = new StockOrder();
        order.setStockListingId(stockListingId);
        order.setPlayerId(playerId);
        order.setOrderType(orderType);
        order.setOrderStyle(orderStyle);
        order.setLimitPrice(limitPrice);
        order.setQuantity(quantity);
        order.setFilledQuantity(0L);
        order.setStatus("PENDING");
        order.setExpiresAt(LocalDateTime.now().plusDays(7));

        order = stockOrderRepository.save(order);
        log.info("Order placed: {} {} {} shares at {} for {}",
                orderType, orderStyle, quantity, limitPrice, listing.getTickerSymbol());

        // Try to match the order
        matchOrder(order.getId());

        return order;
    }

    /**
     * Cancel a pending order.
     *
     * @param orderId the order ID
     * @param playerId the player ID (for authorization)
     */
    @Transactional
    public void cancelOrder(Long orderId, Long playerId) {
        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Order does not belong to this player");
        }

        if (!"PENDING".equals(order.getStatus()) && !"PARTIAL".equals(order.getStatus())) {
            throw new IllegalArgumentException("Can only cancel PENDING or PARTIAL orders");
        }

        order.setStatus("CANCELLED");
        stockOrderRepository.save(order);

        log.info("Order cancelled: {} by player {}", orderId, playerId);
    }

    /**
     * Match a specific order against the order book.
     *
     * @param orderId the order ID to match
     */
    @Transactional
    public void matchOrder(Long orderId) {
        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!"PENDING".equals(order.getStatus()) && !"PARTIAL".equals(order.getStatus())) {
            return;
        }

        StockListing listing = stockListingRepository.findById(order.getStockListingId())
                .orElseThrow(() -> new IllegalArgumentException("Stock listing not found: " + order.getStockListingId()));

        if (!listing.getIsActive()) {
            return;
        }

        // Get counter orders
        String counterType = "BUY".equals(order.getOrderType()) ? "SELL" : "BUY";
        List<StockOrder> counterOrders = stockOrderRepository
                .findActiveOrdersByType(order.getStockListingId(), counterType);

        for (StockOrder counterOrder : counterOrders) {
            if (order.getRemainingQuantity() <= 0) break;
            if (counterOrder.getRemainingQuantity() <= 0) continue;

            // Determine execution price
            BigDecimal executionPrice;
            if ("MARKET".equals(order.getOrderStyle())) {
                // Market order executes at counter's limit price or current price
                executionPrice = counterOrder.getLimitPrice() != null
                        ? counterOrder.getLimitPrice()
                        : listing.getCurrentPrice();
            } else if ("MARKET".equals(counterOrder.getOrderStyle())) {
                executionPrice = order.getLimitPrice();
            } else {
                // Both are limit orders — check if prices match
                if ("BUY".equals(order.getOrderType())) {
                    // Buyer's limit >= Seller's limit
                    if (order.getLimitPrice().compareTo(counterOrder.getLimitPrice()) < 0) {
                        continue; // No match
                    }
                    executionPrice = counterOrder.getLimitPrice();
                } else {
                    // Seller's limit <= Buyer's limit
                    if (order.getLimitPrice().compareTo(counterOrder.getLimitPrice()) > 0) {
                        continue; // No match
                    }
                    executionPrice = counterOrder.getLimitPrice();
                }
            }

            // Calculate trade quantity
            long tradeQuantity = Math.min(order.getRemainingQuantity(), counterOrder.getRemainingQuantity());

            // Execute trade
            executeTrade(listing, order, counterOrder, tradeQuantity, executionPrice);
        }

        // Update order status
        if (order.getRemainingQuantity() <= 0) {
            order.setStatus("FILLED");
        } else if (order.getFilledQuantity() > 0) {
            order.setStatus("PARTIAL");
        }
        stockOrderRepository.save(order);
    }

    /**
     * Execute a trade between two orders.
     */
    private void executeTrade(StockListing listing, StockOrder buyerOrder, StockOrder sellerOrder,
                              long shares, BigDecimal pricePerShare) {
        // Determine buyer and seller
        StockOrder buyer = "BUY".equals(buyerOrder.getOrderType()) ? buyerOrder : sellerOrder;
        StockOrder seller = "BUY".equals(buyerOrder.getOrderType()) ? sellerOrder : buyerOrder;

        // Calculate values
        BigDecimal totalValue = pricePerShare.multiply(BigDecimal.valueOf(shares));
        BigDecimal marketTax = totalValue.multiply(MARKET_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sellerProceeds = totalValue.subtract(marketTax);

        // Create trade record
        StockTrade trade = new StockTrade();
        trade.setStockListingId(listing.getId());
        trade.setBuyerId(buyer.getPlayerId());
        trade.setSellerId(seller.getPlayerId());
        trade.setSharesTraded(shares);
        trade.setPricePerShare(pricePerShare);
        trade.setTotalValue(totalValue);
        trade.setMarketTax(marketTax);
        trade.setTimestamp(LocalDateTime.now());
        stockTradeRepository.save(trade);

        // Update buyer's holding
        updateHolding(buyer.getPlayerId(), listing.getId(), shares, pricePerShare);

        // Update seller's holding
        Optional<StockHolding> sellerHoldingOpt = stockHoldingRepository
                .findByOwnerIdAndOwnerTypeAndStockListingId(seller.getPlayerId(), "PLAYER", listing.getId());
        if (sellerHoldingOpt.isPresent()) {
            StockHolding sellerHolding = sellerHoldingOpt.get();
            sellerHolding.setSharesOwned(sellerHolding.getSharesOwned() - shares);
            if (sellerHolding.getSharesOwned() <= 0) {
                stockHoldingRepository.delete(sellerHolding);
            } else {
                stockHoldingRepository.save(sellerHolding);
            }
        }

        // Update order fill quantities
        buyer.setFilledQuantity(buyer.getFilledQuantity() + shares);
        seller.setFilledQuantity(seller.getFilledQuantity() + shares);
        stockOrderRepository.save(buyer);
        stockOrderRepository.save(seller);

        // Update listing volume
        listing.setVolume24h(listing.getVolume24h() + shares);
        stockListingRepository.save(listing);

        // Collect market tax to exchange (log as expense from seller's corporation if applicable)
        log.info("Trade executed: {} shares at {} for {}. Tax: {}", shares, pricePerShare, totalValue, marketTax);
    }

    /**
     * Update or create a holding for a player.
     */
    private void updateHolding(Long playerId, Long stockListingId, long shares, BigDecimal pricePerShare) {
        Optional<StockHolding> holdingOpt = stockHoldingRepository
                .findByOwnerIdAndOwnerTypeAndStockListingId(playerId, "PLAYER", stockListingId);

        if (holdingOpt.isPresent()) {
            StockHolding holding = holdingOpt.get();
            long currentShares = holding.getSharesOwned();
            BigDecimal currentAvg = holding.getAverageBuyPrice();

            // Recalculate average buy price
            BigDecimal totalCost = currentAvg.multiply(BigDecimal.valueOf(currentShares))
                    .add(pricePerShare.multiply(BigDecimal.valueOf(shares)));
            BigDecimal newAvg = totalCost.divide(BigDecimal.valueOf(currentShares + shares), 2, RoundingMode.HALF_UP);

            holding.setSharesOwned(currentShares + shares);
            holding.setAverageBuyPrice(newAvg);
            stockHoldingRepository.save(holding);
        } else {
            StockHolding holding = new StockHolding();
            holding.setOwnerId(playerId);
            holding.setOwnerType("PLAYER");
            holding.setStockListingId(stockListingId);
            holding.setSharesOwned(shares);
            holding.setAverageBuyPrice(pricePerShare);
            stockHoldingRepository.save(holding);
        }
    }

    // ---- Dividend Distribution ----

    /**
     * Process quarterly dividends for all active listings.
     */
    @Transactional
    public void processQuarterlyDividends() {
        List<StockListing> activeListings = stockListingRepository.findByIsActiveTrue();
        String quarter = getCurrentQuarter();
        int processed = 0;

        for (StockListing listing : activeListings) {
            try {
                // Check if dividend already paid for this quarter
                if (dividendPaymentRepository.findByStockListingIdAndQuarter(listing.getId(), quarter).isPresent()) {
                    continue;
                }

                // Calculate dividend per share based on corporation profits
                BigDecimal dividendPerShare = calculateDividendPerShare(listing);
                if (dividendPerShare.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // No profit, no dividend
                }

                // Get all holders
                List<StockHolding> holders = stockHoldingRepository.findByStockListingId(listing.getId());
                long totalSharesHeld = holders.stream().mapToLong(StockHolding::getSharesOwned).sum();
                BigDecimal totalPaid = dividendPerShare.multiply(BigDecimal.valueOf(totalSharesHeld));

                // Create dividend payment record
                DividendPayment payment = new DividendPayment();
                payment.setStockListingId(listing.getId());
                payment.setAmountPerShare(dividendPerShare);
                payment.setTotalPaid(totalPaid);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setQuarter(quarter);
                dividendPaymentRepository.save(payment);

                // Update each holder's dividends received
                for (StockHolding holder : holders) {
                    BigDecimal received = dividendPerShare.multiply(BigDecimal.valueOf(holder.getSharesOwned()));
                    holder.setTotalDividendsReceived(
                            holder.getTotalDividendsReceived().add(received));
                    stockHoldingRepository.save(holder);
                }

                // Update dividend yield
                if (listing.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
                    // Annualized yield (4 quarters)
                    double yield = dividendPerShare.multiply(BigDecimal.valueOf(4))
                            .doubleValue() / listing.getCurrentPrice().doubleValue();
                    listing.setDividendYield(yield * 100); // Percentage
                }
                stockListingRepository.save(listing);

                processed++;
                log.info("Dividend paid for {}: {} per share, total: {} ({})",
                        listing.getTickerSymbol(), dividendPerShare, totalPaid, quarter);
            } catch (Exception e) {
                log.error("Failed to process dividend for listing {}: {}", listing.getId(), e.getMessage());
            }
        }

        log.info("Processed dividends for {} listings in quarter {}", processed, quarter);
    }

    /**
     * Calculate dividend per share based on corporation's net profit.
     */
    private BigDecimal calculateDividendPerShare(StockListing listing) {
        try {
            Map<String, Object> breakdown = economyService.getFinancialBreakdown(listing.getCorporationId());
            long netProfit = (long) breakdown.getOrDefault("netProfit", 0L);

            if (netProfit <= 0) {
                return BigDecimal.ZERO;
            }

            // Pay 50% of net profit as dividends
            long dividendPool = netProfit / 2;
            BigDecimal shares = BigDecimal.valueOf(listing.getPublicShares());

            return BigDecimal.valueOf(dividendPool).divide(shares, 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("Could not calculate dividend for listing {}: {}", listing.getId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get current quarter string (e.g., "Q1-2026").
     */
    private String getCurrentQuarter() {
        LocalDateTime now = LocalDateTime.now();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        return "Q" + quarter + "-" + now.getYear();
    }

    // ---- Order Expiration ----

    /**
     * Cancel all expired pending orders.
     */
    @Transactional
    public void cancelExpiredOrders() {
        List<StockOrder> expiredOrders = stockOrderRepository.findExpiredOrders(LocalDateTime.now());
        for (StockOrder order : expiredOrders) {
            order.setStatus("CANCELLED");
            stockOrderRepository.save(order);
        }
        if (!expiredOrders.isEmpty()) {
            log.info("Cancelled {} expired orders", expiredOrders.size());
        }
    }

    // ---- Market Data ----

    /**
     * Get recent trades for a stock listing.
     */
    public List<StockTrade> getRecentTrades(Long stockListingId, int limit) {
        List<StockTrade> trades = stockTradeRepository.findByStockListingIdOrderByTimestampDesc(stockListingId);
        return trades.subList(0, Math.min(limit, trades.size()));
    }

    /**
     * Get all active orders for a stock listing.
     */
    public List<StockOrder> getActiveOrders(Long stockListingId) {
        return stockOrderRepository.findByStockListingIdAndStatus(stockListingId, "PENDING");
    }
}
