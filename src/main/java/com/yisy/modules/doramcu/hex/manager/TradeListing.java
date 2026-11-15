package com.yisy.modules.doramcu.hex.manager;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.traveller.Commodity;

import java.util.Objects;

/**
 * Represents a trade listing on the sector-wide MarketBoard.
 * <p>
 * A listing is a buy or sell order placed by an empire on the inter-player
 * market. Listings expire after a set number of turns and can be matched
 * with complementary orders from other empires.
 * </p>
 * <p>
 * Sell listings offer commodities at a fixed price per unit.
 * Buy listings request commodities at a maximum price per unit.
 * When a buy and sell listing match, the trade executes at the listed price
 * and a market tax is applied.
 * </p>
 */
public class TradeListing {

    private final int id;
    private final int empireId;
    private final ListingType type;
    private final Commodity commodity;
    private final int units;
    private final int pricePerUnit;
    private final int turnsToLive;
    private int turnsRemaining;
    private boolean filled;
    private boolean cancelled;
    private int unitsFilled;

    /**
     * The type of trade listing.
     */
    public enum ListingType {
        /** Empire wants to sell a commodity. */
        SELL,
        /** Empire wants to buy a commodity. */
        BUY
    }

    /**
     * Creates a new trade listing.
     *
     * @param id the unique listing ID
     * @param empireId the empire placing the listing
     * @param type whether this is a buy or sell order
     * @param commodity the commodity being traded
     * @param units the number of units
     * @param pricePerUnit the price per unit in credits
     * @param turnsToLive how many turns before the listing expires
     */
    public TradeListing(int id, int empireId, ListingType type, Commodity commodity,
                         int units, int pricePerUnit, int turnsToLive) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units must be positive: " + units);
        }
        if (pricePerUnit <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + pricePerUnit);
        }
        if (turnsToLive <= 0) {
            throw new IllegalArgumentException("Turns to live must be positive: " + turnsToLive);
        }
        this.id = id;
        this.empireId = empireId;
        this.type = type;
        this.commodity = commodity;
        this.units = units;
        this.pricePerUnit = pricePerUnit;
        this.turnsToLive = turnsToLive;
        this.turnsRemaining = turnsToLive;
        this.filled = false;
        this.cancelled = false;
        this.unitsFilled = 0;
    }

    /** @return the unique listing ID */
    public int getId() {
        return id;
    }

    /** @return the empire ID that placed this listing */
    public int getEmpireId() {
        return empireId;
    }

    /** @return whether this is a buy or sell listing */
    public ListingType getType() {
        return type;
    }

    /** @return the commodity being traded */
    public Commodity getCommodity() {
        return commodity;
    }

    /** @return the total units in this listing */
    public int getUnits() {
        return units;
    }

    /** @return the price per unit in credits */
    public int getPricePerUnit() {
        return pricePerUnit;
    }

    /** @return the total turns this listing was set to live */
    public int getTurnsToLive() {
        return turnsToLive;
    }

    /** @return the remaining turns before expiration */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return the number of units already filled */
    public int getUnitsFilled() {
        return unitsFilled;
    }

    /** @return the remaining units available */
    public int getUnitsRemaining() {
        return units - unitsFilled;
    }

    /** @return true if the listing is fully filled */
    public boolean isFilled() {
        return filled;
    }

    /** @return true if the listing is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /** @return true if the listing is still active */
    public boolean isActive() {
        return !filled && !cancelled && turnsRemaining > 0;
    }

    /**
     * Advances the listing by one turn.
     *
     * @return true if the listing just expired
     */
    public boolean processTurn() {
        if (!isActive()) return false;
        turnsRemaining--;
        if (turnsRemaining <= 0) {
            return true;
        }
        return false;
    }

    /**
     * Fills part or all of this listing.
     *
     * @param requestedUnits the number of units to fill
     * @return the actual number of units filled
     */
    public int fill(int requestedUnits) {
        if (!isActive()) return 0;
        int available = getUnitsRemaining();
        int fillAmount = Math.min(requestedUnits, available);
        unitsFilled += fillAmount;
        if (unitsFilled >= units) {
            filled = true;
        }
        return fillAmount;
    }

    /**
     * Cancels the listing.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Checks if this listing can match with another listing.
     * A match requires opposite types, same commodity, and compatible prices.
     *
     * @param other the other listing
     * @return true if the listings can be matched
     */
    public boolean canMatchWith(TradeListing other) {
        if (!isActive() || !other.isActive()) return false;
        if (this.empireId == other.empireId) return false;
        if (this.type == other.type) return false;
        if (this.commodity != other.commodity) return false;

        // For a match: buyer's price >= seller's price
        if (this.type == ListingType.BUY && other.type == ListingType.SELL) {
            return this.pricePerUnit >= other.pricePerUnit;
        }
        if (this.type == ListingType.SELL && other.type == ListingType.BUY) {
            return other.pricePerUnit >= this.pricePerUnit;
        }
        return false;
    }

    /**
     * Returns the execution price when matched with another listing.
     * Uses the seller's price (the lower of the two).
     *
     * @param other the matching listing
     * @return the price per unit for execution
     */
    public int getExecutionPrice(TradeListing other) {
        if (this.type == ListingType.SELL) {
            return this.pricePerUnit;
        }
        return other.pricePerUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeListing that = (TradeListing) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("TradeListing{id=%d, empire=%d, %s %s x%d @ %dcr, turns=%d/%d, filled=%d}",
                id, empireId, type, commodity.getName(), units, pricePerUnit,
                turnsRemaining, turnsToLive, unitsFilled);
    }
}
