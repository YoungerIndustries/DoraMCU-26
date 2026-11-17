package com.yisy.modules.doramcu.corporation.stock.service;

import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.corporation.stock.model.StockListing;
import com.yisy.modules.doramcu.corporation.stock.repository.StockListingRepository;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service that calculates stock prices based on corporation metrics.
 * <p>
 * Price formula uses a weighted combination of:
 * - Credit balance (30%)
 * - Contract revenue (25%)
 * - Reputation (20%) — approximated by active employee count
 * - Employee count (15%)
 * - Building count (10%)
 * </p>
 */
@Service
public class StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    // Weight factors for price calculation
    private static final double WEIGHT_CREDIT_BALANCE = 0.30;
    private static final double WEIGHT_REVENUE = 0.25;
    private static final double WEIGHT_REPUTATION = 0.20;
    private static final double WEIGHT_EMPLOYEES = 0.15;
    private static final double WEIGHT_BUILDINGS = 0.10;

    // Minimum and maximum price bounds
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000.00");

    // Base price multiplier
    private static final BigDecimal BASE_MULTIPLIER = new BigDecimal("0.001");

    private final EveCorporationRepository corporationRepository;
    private final StockListingRepository stockListingRepository;
    private final CorporationEconomyService economyService;

    public StockPriceService(
            EveCorporationRepository corporationRepository,
            StockListingRepository stockListingRepository,
            CorporationEconomyService economyService) {
        this.corporationRepository = corporationRepository;
        this.stockListingRepository = stockListingRepository;
        this.economyService = economyService;
    }

    /**
     * Calculate the stock price for a given corporation based on its metrics.
     *
     * @param corporationId the corporation ID
     * @return calculated stock price
     */
    public BigDecimal calculatePrice(Long corporationId) {
        EveCorporation corporation = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        // Credit balance score
        long creditBalance = corporation.getCreditBalance() != null ? corporation.getCreditBalance() : 0L;
        double creditScore = Math.log10(Math.max(creditBalance, 1));

        // Revenue score from financial breakdown
        double revenueScore = 0;
        try {
            Map<String, Object> breakdown = economyService.getFinancialBreakdown(corporationId);
            long totalRevenue = (long) breakdown.getOrDefault("totalRevenue", 0L);
            revenueScore = Math.log10(Math.max(totalRevenue, 1));
        } catch (Exception e) {
            log.warn("Could not get financial breakdown for corporation {}: {}", corporationId, e.getMessage());
        }

        // Employee count (reputation proxy)
        double employeeScore = 0;
        try {
            Map<String, Object> payroll = economyService.getPayrollSummary(corporationId);
            int activeHireCount = (int) payroll.getOrDefault("activeHireCount", 0);
            employeeScore = Math.log10(Math.max(activeHireCount, 1));
        } catch (Exception e) {
            log.warn("Could not get payroll summary for corporation {}: {}", corporationId, e.getMessage());
        }

        // Reputation score — use employee count as a proxy
        double reputationScore = employeeScore;

        // Building count score — use tycoon service if available
        double buildingScore = 0;
        // Buildings are tracked in CorporationTycoonService; for now, estimate from credit balance
        buildingScore = Math.log10(Math.max(creditBalance / 1000, 1));

        // Weighted combination
        double weightedScore =
                (WEIGHT_CREDIT_BALANCE * creditScore) +
                (WEIGHT_REVENUE * revenueScore) +
                (WEIGHT_REPUTATION * reputationScore) +
                (WEIGHT_EMPLOYEES * employeeScore) +
                (WEIGHT_BUILDINGS * buildingScore);

        // Convert to price
        BigDecimal price = BigDecimal.valueOf(weightedScore)
                .multiply(BASE_MULTIPLIER)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        // Apply bounds
        if (price.compareTo(MIN_PRICE) < 0) {
            price = MIN_PRICE;
        } else if (price.compareTo(MAX_PRICE) > 0) {
            price = MAX_PRICE;
        }

        log.debug("Calculated stock price for corporation {}: {} (score: {})", corporationId, price, weightedScore);
        return price;
    }

    /**
     * Update the current price for an active stock listing.
     * Also updates day high/low and last price update timestamp.
     *
     * @param listingId the stock listing ID
     */
    @Transactional
    public void updatePrice(Long listingId) {
        StockListing listing = stockListingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Stock listing not found: " + listingId));

        if (!listing.getIsActive()) {
            log.warn("Cannot update price for inactive listing: {}", listingId);
            return;
        }

        BigDecimal newPrice = calculatePrice(listing.getCorporationId());

        // Update day high/low
        if (listing.getDayHigh() == null || newPrice.compareTo(listing.getDayHigh()) > 0) {
            listing.setDayHigh(newPrice);
        }
        if (listing.getDayLow() == null || newPrice.compareTo(listing.getDayLow()) < 0) {
            listing.setDayLow(newPrice);
        }

        listing.setCurrentPrice(newPrice);
        listing.setLastPriceUpdate(LocalDateTime.now());

        // Update market cap
        BigDecimal marketCap = newPrice.multiply(BigDecimal.valueOf(listing.getTotalShares()));
        listing.setMarketCap(marketCap);

        stockListingRepository.save(listing);

        log.info("Updated stock price for {}: {} -> {}, market cap: {}",
                listing.getTickerSymbol(), listing.getCurrentPrice(), newPrice, marketCap);
    }

    /**
     * Update prices for all active stock listings.
     */
    @Transactional
    public void updateAllPrices() {
        var listings = stockListingRepository.findByIsActiveTrue();
        for (StockListing listing : listings) {
            try {
                updatePrice(listing.getId());
            } catch (Exception e) {
                log.error("Failed to update price for listing {}: {}", listing.getId(), e.getMessage());
            }
        }
        log.info("Updated prices for {} active stock listings", listings.size());
    }

    /**
     * Calculate the initial IPO price for a corporation.
     * Uses a more conservative formula than regular price updates.
     *
     * @param corporationId the corporation ID
     * @return initial IPO price
     */
    public BigDecimal calculateIpoPrice(Long corporationId) {
        BigDecimal regularPrice = calculatePrice(corporationId);
        // IPO price is 80% of regular price to give buyers an incentive
        return regularPrice.multiply(new BigDecimal("0.8"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
