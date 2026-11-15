package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.traveller.Commodity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sector-wide market board for inter-player trade.
 * <p>
 * The MarketBoard maintains all active trade listings from all empires
 * and automatically matches complementary buy and sell orders. When a
 * match occurs, the trade executes at the listed price and a market tax
 * is applied to the sector treasury.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Buy and sell listings with turn-based expiration</li>
 *   <li>Automatic matching of complementary orders</li>
 *   <li>Market tax (5%) on each executed trade</li>
 *   <li>Listing history for audit and analysis</li>
 * </ul>
 */
public class MarketBoard {

    /** Default market tax rate (5%). */
    public static final double DEFAULT_MARKET_TAX_RATE = 0.05;

    /** Default listing lifetime in turns. */
    public static final int DEFAULT_LISTING_TURNS = 5;

    /** Maximum listings per empire. */
    public static final int MAX_LISTINGS_PER_EMPIRE = 20;

    private final List<TradeListing> activeListings = new ArrayList<>();
    private final List<TradeListing> completedListings = new ArrayList<>();
    private int nextListingId = 1;

    /** Market tax rate as a fraction (0.05 = 5%). */
    private double marketTaxRate;

    /** Accumulated market tax revenue. */
    private long marketTaxRevenue;

    /** History of executed trades. */
    private final List<ExecutedTrade> tradeHistory = new ArrayList<>();

    /** Reference to CreditExchangeService for EconomicState access. */
    private CreditExchangeService creditExchangeService;

    /**
     * Creates a new MarketBoard.
     */
    public MarketBoard() {
        this.marketTaxRate = DEFAULT_MARKET_TAX_RATE;
        this.marketTaxRevenue = 0;
    }

    /**
     * Sets the CreditExchangeService for EconomicState integration.
     *
     * @param service the credit exchange service
     */
    public void setCreditExchangeService(CreditExchangeService service) {
        this.creditExchangeService = service;
    }

    /** @return the current market tax rate */
    public double getMarketTaxRate() {
        return marketTaxRate;
    }

    /**
     * Sets the market tax rate.
     *
     * @param rate the tax rate as a fraction (0.0 to 1.0)
     */
    public void setMarketTaxRate(double rate) {
        this.marketTaxRate = Math.max(0.0, Math.min(1.0, rate));
    }

    /** @return the accumulated market tax revenue */
    public long getMarketTaxRevenue() {
        return marketTaxRevenue;
    }

    // ============================================================
    // Listing Management
    // ============================================================

    /**
     * Creates a new trade listing.
     *
     * @param empireId the empire placing the listing
     * @param type whether this is a buy or sell order
     * @param commodity the commodity
     * @param units the number of units
     * @param pricePerUnit the price per unit in credits
     * @param turnsToLive how many turns before expiration (0 for default)
     * @return the created listing, or null if the empire has too many listings
     */
    public TradeListing createListing(int empireId, TradeListing.ListingType type,
                                       Commodity commodity, int units, int pricePerUnit,
                                       int turnsToLive) {
        if (turnsToLive <= 0) {
            turnsToLive = DEFAULT_LISTING_TURNS;
        }

        // Check listing limit per empire
        long empireListingCount = activeListings.stream()
                .filter(l -> l.getEmpireId() == empireId)
                .count();
        if (empireListingCount >= MAX_LISTINGS_PER_EMPIRE) {
            return null;
        }

        TradeListing listing = new TradeListing(
                nextListingId++, empireId, type, commodity, units, pricePerUnit, turnsToLive);
        activeListings.add(listing);

        // Try to match immediately
        matchListing(listing);

        return listing;
    }

    /**
     * Cancels a listing by ID.
     *
     * @param listingId the listing ID
     * @param empireId the empire ID (must match)
     * @return true if the listing was cancelled
     */
    public boolean cancelListing(int listingId, int empireId) {
        for (TradeListing listing : activeListings) {
            if (listing.getId() == listingId && listing.getEmpireId() == empireId) {
                listing.cancel();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all active listings.
     *
     * @return list of active listings
     */
    public List<TradeListing> getActiveListings() {
        return new ArrayList<>(activeListings);
    }

    /**
     * Returns active listings for a specific empire.
     *
     * @param empireId the empire ID
     * @return list of active listings for that empire
     */
    public List<TradeListing> getActiveListings(int empireId) {
        return activeListings.stream()
                .filter(l -> l.getEmpireId() == empireId)
                .collect(Collectors.toList());
    }

    /**
     * Returns active listings for a specific commodity.
     *
     * @param commodity the commodity
     * @return list of active listings for that commodity
     */
    public List<TradeListing> getActiveListings(Commodity commodity) {
        return activeListings.stream()
                .filter(l -> l.getCommodity() == commodity)
                .collect(Collectors.toList());
    }

    /**
     * Returns the trade history.
     *
     * @return list of executed trades
     */
    public List<ExecutedTrade> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }

    // ============================================================
    // Matching Engine
    // ============================================================

    /**
     * Tries to match a specific listing with complementary orders.
     *
     * @param listing the listing to match
     */
    private void matchListing(TradeListing listing) {
        List<TradeListing> candidates = activeListings.stream()
                .filter(l -> l != listing && listing.canMatchWith(l))
                .collect(Collectors.toList());

        // Sort by best price for the new listing
        if (listing.getType() == TradeListing.ListingType.BUY) {
            // Buyer wants lowest seller price
            candidates.sort(Comparator.comparingInt(TradeListing::getPricePerUnit));
        } else {
            // Seller wants highest buyer price
            candidates.sort(Comparator.comparingInt(TradeListing::getPricePerUnit).reversed());
        }

        for (TradeListing candidate : candidates) {
            if (!listing.isActive() || !candidate.isActive()) continue;

            int unitsRemaining = listing.getUnitsRemaining();
            if (unitsRemaining <= 0) break;

            int candidateRemaining = candidate.getUnitsRemaining();
            if (candidateRemaining <= 0) continue;

            int matchUnits = Math.min(unitsRemaining, candidateRemaining);
            int pricePerUnit = listing.getExecutionPrice(candidate);
            executeTrade(listing, candidate, matchUnits, pricePerUnit);
        }
    }

    /**
     * Executes a trade between two matched listings.
     *
     * @param listing1 the first listing
     * @param listing2 the second listing
     * @param units the number of units traded
     * @param pricePerUnit the execution price
     */
    private void executeTrade(TradeListing listing1, TradeListing listing2,
                               int units, int pricePerUnit) {
        listing1.fill(units);
        listing2.fill(units);

        long totalValue = (long) units * pricePerUnit;
        long taxAmount = (long) (totalValue * marketTaxRate);
        marketTaxRevenue += taxAmount;

        // Determine buyer and seller
        TradeListing buyer = listing1.getType() == TradeListing.ListingType.BUY ? listing1 : listing2;
        TradeListing seller = listing1.getType() == TradeListing.ListingType.SELL ? listing1 : listing2;

        // Transfer credits between empires via EconomicState
        if (creditExchangeService != null) {
            EconomicState buyerState = creditExchangeService.getEconomicState(buyer.getEmpireId());
            EconomicState sellerState = creditExchangeService.getEconomicState(seller.getEmpireId());

            if (buyerState != null && sellerState != null) {
                // Buyer pays
                buyerState.spendCredits(EconomicState.IncomeSource.INTER_PLAYER_TRADE, totalValue);
                // Seller receives (minus tax)
                sellerState.addCredits(EconomicState.IncomeSource.INTER_PLAYER_TRADE, totalValue - taxAmount);
            }
        }

        // Record the trade
        ExecutedTrade trade = new ExecutedTrade(
                listing1.getCommodity(), units, pricePerUnit,
                buyer.getEmpireId(), seller.getEmpireId(), taxAmount);
        tradeHistory.add(trade);

        // Move fully filled listings to completed
        if (listing1.isFilled()) {
            activeListings.remove(listing1);
            completedListings.add(listing1);
        }
        if (listing2.isFilled() && activeListings.contains(listing2)) {
            activeListings.remove(listing2);
            completedListings.add(listing2);
        }
    }

    /**
     * Attempts to match all active listings.
     * Called each turn to find new matches.
     *
     * @return the number of new matches executed
     */
    public int processTurn() {
        int matches = 0;

        // Advance all listings
        Iterator<TradeListing> it = activeListings.iterator();
        while (it.hasNext()) {
            TradeListing listing = it.next();
            if (listing.processTurn()) {
                // Expired
                it.remove();
                completedListings.add(listing);
            }
        }

        // Try to match all remaining active listings
        List<TradeListing> snapshot = new ArrayList<>(activeListings);
        for (TradeListing listing : snapshot) {
            if (listing.isActive()) {
                int before = tradeHistory.size();
                matchListing(listing);
                matches += tradeHistory.size() - before;
            }
        }

        return matches;
    }

    // ============================================================
    // Summary
    // ============================================================

    /**
     * Returns a summary of the market board state.
     *
     * @return a map of market metrics
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("activeListings", activeListings.size());
        summary.put("completedListings", completedListings.size());
        summary.put("marketTaxRate", marketTaxRate);
        summary.put("marketTaxRevenue", marketTaxRevenue);
        summary.put("totalTrades", tradeHistory.size());

        // Listings by commodity
        Map<String, Integer> byCommodity = new LinkedHashMap<>();
        for (Commodity c : Commodity.values()) {
            long count = activeListings.stream()
                    .filter(l -> l.getCommodity() == c)
                    .count();
            if (count > 0) {
                byCommodity.put(c.getName(), (int) count);
            }
        }
        summary.put("listingsByCommodity", byCommodity);

        // Listings by type
        long buyCount = activeListings.stream()
                .filter(l -> l.getType() == TradeListing.ListingType.BUY)
                .count();
        long sellCount = activeListings.stream()
                .filter(l -> l.getType() == TradeListing.ListingType.SELL)
                .count();
        summary.put("buyListings", buyCount);
        summary.put("sellListings", sellCount);

        return summary;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Record of an executed trade between two empires.
     */
    public static class ExecutedTrade {
        public final Commodity commodity;
        public final int units;
        public final int pricePerUnit;
        public final int buyerEmpireId;
        public final int sellerEmpireId;
        public final long taxAmount;

        public ExecutedTrade(Commodity commodity, int units, int pricePerUnit,
                              int buyerEmpireId, int sellerEmpireId, long taxAmount) {
            this.commodity = commodity;
            this.units = units;
            this.pricePerUnit = pricePerUnit;
            this.buyerEmpireId = buyerEmpireId;
            this.sellerEmpireId = sellerEmpireId;
            this.taxAmount = taxAmount;
        }

        /** @return the total trade value */
        public long getTotalValue() {
            return (long) units * pricePerUnit;
        }

        @Override
        public String toString() {
            return String.format("ExecutedTrade{%s x%d @ %dcr, buyer=%d, seller=%d, tax=%d}",
                    commodity.getName(), units, pricePerUnit,
                    buyerEmpireId, sellerEmpireId, taxAmount);
        }
    }
}
