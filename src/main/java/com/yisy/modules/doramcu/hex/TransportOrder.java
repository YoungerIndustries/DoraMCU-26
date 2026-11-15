package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an order to transport resources between two locations.
 * <p>
 * Transport orders move resources from a source (e.g., a mining operation's
 * local storage) to a destination (e.g., a processing facility's storage).
 * Transport takes time based on distance, measured in turns. Supports priority
 * levels, fleet assignment, and multi-resource cargo consolidation.
 * </p>
 */
public class TransportOrder {

    private final int id;
    private final int ownerEmpireId;

    // Source and destination
    private final int sourceBodyId;
    private final int destinationBodyId;

    // Resource being transported (legacy single-resource mode)
    private final RawResourceType resourceType;
    private final int totalAmount;
    private final int amountPerTurn;

    // Multi-resource cargo support (for consolidated transports)
    private final Map<RawResourceType, Integer> cargoManifest;

    // Transport priority
    private final TransportPriority priority;

    // Fleet assignment (optional — null means no dedicated fleet)
    private final Integer assignedFleetId;

    // Transport state
    private int turnsRemaining;
    private int turnsTotal;
    private int amountLoaded;
    private boolean completed;
    private boolean cancelled;

    /**
     * Creates a new transport order.
     *
     * @param id the unique order ID
     * @param ownerEmpireId the empire ID that owns this order
     * @param sourceBodyId the celestial body ID where resources are loaded
     * @param destinationBodyId the celestial body ID where resources are delivered
     * @param resourceType the raw resource type being transported
     * @param totalAmount the total amount to transport
     * @param distance the distance between source and destination (affects transport time)
     */
    public TransportOrder(int id, int ownerEmpireId, int sourceBodyId, int destinationBodyId,
                           RawResourceType resourceType, int totalAmount, int distance) {
        this(id, ownerEmpireId, sourceBodyId, destinationBodyId, resourceType, totalAmount,
                distance, TransportPriority.NORMAL, null);
    }

    /**
     * Creates a new transport order with priority and optional fleet assignment.
     *
     * @param id the unique order ID
     * @param ownerEmpireId the empire ID that owns this order
     * @param sourceBodyId the celestial body ID where resources are loaded
     * @param destinationBodyId the celestial body ID where resources are delivered
     * @param resourceType the raw resource type being transported
     * @param totalAmount the total amount to transport
     * @param distance the distance between source and destination (affects transport time)
     * @param priority the transport priority level
     * @param assignedFleetId the fleet ID assigned to this transport (or null)
     */
    public TransportOrder(int id, int ownerEmpireId, int sourceBodyId, int destinationBodyId,
                           RawResourceType resourceType, int totalAmount, int distance,
                           TransportPriority priority, Integer assignedFleetId) {
        this.id = id;
        this.ownerEmpireId = ownerEmpireId;
        this.sourceBodyId = sourceBodyId;
        this.destinationBodyId = destinationBodyId;
        this.resourceType = resourceType;
        this.totalAmount = Math.max(1, totalAmount);
        // Apply priority speed multiplier to calculate turns
        int adjustedDistance = (int) Math.max(1, distance * priority.getSpeedMultiplier());
        this.amountPerTurn = Math.max(1, totalAmount / adjustedDistance);
        this.turnsTotal = Math.max(1, (totalAmount + amountPerTurn - 1) / amountPerTurn);
        this.turnsRemaining = this.turnsTotal;
        this.amountLoaded = 0;
        this.completed = false;
        this.cancelled = false;
        this.priority = priority;
        this.assignedFleetId = assignedFleetId;
        this.cargoManifest = new EnumMap<>(RawResourceType.class);
        // Populate manifest with the single resource for legacy compatibility
        this.cargoManifest.put(resourceType, totalAmount);
    }

    /**
     * Creates a consolidated transport order carrying multiple resource types.
     *
     * @param id the unique order ID
     * @param ownerEmpireId the empire ID that owns this order
     * @param sourceBodyId the celestial body ID where resources are loaded
     * @param destinationBodyId the celestial body ID where resources are delivered
     * @param cargoManifest map of resource types to amounts
     * @param distance the distance between source and destination
     * @param priority the transport priority level
     * @param assignedFleetId the fleet ID assigned to this transport (or null)
     */
    public TransportOrder(int id, int ownerEmpireId, int sourceBodyId, int destinationBodyId,
                           Map<RawResourceType, Integer> cargoManifest, int distance,
                           TransportPriority priority, Integer assignedFleetId) {
        if (cargoManifest == null || cargoManifest.isEmpty()) {
            throw new IllegalArgumentException("Cargo manifest must not be empty");
        }
        this.id = id;
        this.ownerEmpireId = ownerEmpireId;
        this.sourceBodyId = sourceBodyId;
        this.destinationBodyId = destinationBodyId;
        this.cargoManifest = new EnumMap<>(cargoManifest);
        this.priority = priority;
        this.assignedFleetId = assignedFleetId;
        // Calculate total amount as sum of all cargo
        this.totalAmount = cargoManifest.values().stream().mapToInt(Integer::intValue).sum();
        // Use the primary resource type (first entry) for legacy compatibility
        this.resourceType = cargoManifest.keySet().iterator().next();
        // Apply priority speed multiplier
        int adjustedDistance = (int) Math.max(1, distance * priority.getSpeedMultiplier());
        this.amountPerTurn = Math.max(1, totalAmount / adjustedDistance);
        this.turnsTotal = Math.max(1, (totalAmount + amountPerTurn - 1) / amountPerTurn);
        this.turnsRemaining = this.turnsTotal;
        this.amountLoaded = 0;
        this.completed = false;
        this.cancelled = false;
    }

    /** @return the unique order ID */
    public int getId() {
        return id;
    }

    /** @return the empire ID that owns this order */
    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /** @return the source celestial body ID */
    public int getSourceBodyId() {
        return sourceBodyId;
    }

    /** @return the destination celestial body ID */
    public int getDestinationBodyId() {
        return destinationBodyId;
    }

    /** @return the raw resource type being transported */
    public RawResourceType getResourceType() {
        return resourceType;
    }

    /** @return the total amount to transport */
    public int getTotalAmount() {
        return totalAmount;
    }

    /** @return the amount transported per turn */
    public int getAmountPerTurn() {
        return amountPerTurn;
    }

    /** @return the total number of turns for this transport */
    public int getTurnsTotal() {
        return turnsTotal;
    }

    /** @return the number of turns remaining */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return the amount already loaded and in transit */
    public int getAmountLoaded() {
        return amountLoaded;
    }

    /** @return true if the transport is complete */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if the transport is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /** @return true if the transport is still in progress */
    public boolean isInProgress() {
        return !completed && !cancelled;
    }

    /**
     * Returns the progress as a fraction between 0.0 and 1.0.
     */
    public double getProgress() {
        if (turnsTotal <= 0) {
            return 1.0;
        }
        return 1.0 - ((double) turnsRemaining / turnsTotal);
    }

    /**
     * Processes one turn of transport.
     *
     * @return the amount delivered this turn (non-zero only on the final turn)
     */
    public int processTurn() {
        if (!isInProgress()) {
            return 0;
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            completed = true;
            amountLoaded = totalAmount;
            return totalAmount;
        }

        // Track partial progress
        amountLoaded = totalAmount - (turnsRemaining * amountPerTurn);
        return 0;
    }

    /**
     * Cancels this transport order.
     */
    public void cancel() {
        if (isInProgress()) {
            cancelled = true;
            turnsRemaining = 0;
        }
    }

    /**
     * Returns the cargo manifest for this transport.
     * <p>
     * For single-resource transports, this contains one entry.
     * For consolidated transports, this contains all resource types.
     * </p>
     *
     * @return unmodifiable map of resource types to amounts
     */
    public Map<RawResourceType, Integer> getCargoManifest() {
        return Map.copyOf(cargoManifest);
    }

    /**
     * Returns the transport priority level.
     */
    public TransportPriority getPriority() {
        return priority;
    }

    /**
     * Returns the assigned fleet ID, or null if no fleet is assigned.
     */
    public Integer getAssignedFleetId() {
        return assignedFleetId;
    }

    /**
     * Returns true if this transport carries multiple resource types.
     */
    public boolean isConsolidated() {
        return cargoManifest.size() > 1;
    }

    /**
     * Returns a summary of this transport order.
     */
    public String getSummary() {
        String status;
        if (completed) {
            status = "completed";
        } else if (cancelled) {
            status = "cancelled";
        } else {
            status = String.format("%d/%d turns", turnsTotal - turnsRemaining, turnsTotal);
        }
        String cargoInfo;
        if (isConsolidated()) {
            cargoInfo = cargoManifest.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
        } else {
            cargoInfo = resourceType + "=" + totalAmount;
        }
        return String.format("Transport{from=%d, to=%d, cargo=[%s], priority=%s, %s}",
                sourceBodyId, destinationBodyId, cargoInfo, priority, status);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
