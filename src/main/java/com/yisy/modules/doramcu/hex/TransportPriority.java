package com.yisy.modules.doramcu.hex;

/**
 * Priority level for transport orders.
 * <p>
 * Higher priority transports are processed first and may use faster ships
 * (scouts) at the cost of reduced cargo capacity. Lower priority transports
 * use slower but higher-capacity ships (freighters).
 * </p>
 */
public enum TransportPriority {

    /**
     * Normal priority — standard freighter speed and capacity.
     * Default for most transports.
     */
    NORMAL(0, 1.0, 100),

    /**
     * Urgent priority — uses scout ships for faster delivery.
     * Reduces cargo capacity by 50% but cuts transport time by 50%.
     */
    URGENT(1, 0.5, 50),

    /**
     * Bulk priority — uses large freighters for maximum capacity.
     * Increases cargo capacity by 100% but adds 1 turn to delivery.
     */
    BULK(-1, 1.5, 200);

    private final int sortOrder;
    private final double speedMultiplier;
    private final int baseCapacity;

    TransportPriority(int sortOrder, double speedMultiplier, int baseCapacity) {
        this.sortOrder = sortOrder;
        this.speedMultiplier = speedMultiplier;
        this.baseCapacity = baseCapacity;
    }

    /**
     * Returns the sort order for prioritizing transport processing.
     * Higher values are processed first.
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Returns the speed multiplier affecting transport time.
     * Values &lt; 1.0 mean faster delivery, values &gt; 1.0 mean slower.
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Returns the base cargo capacity for this priority level.
     */
    public int getBaseCapacity() {
        return baseCapacity;
    }
}
