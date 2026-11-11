package com.yisy.modules.doramcu.hex.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a transportation corporation in the Business Tycoon campaign.
 * <p>
 * A TransportCorporation is distinct from an Empire. An Empire is a political entity
 * that owns planets; a Corporation is an economic entity that owns stations, vehicles,
 * and trade routes. Multiple corporations can operate within one Empire's territory.
 * </p>
 * <p>
 * The corporation progresses through {@link CorporationRank} levels based on total
 * assets, route count, and market share. Each rank unlocks new capabilities.
 * </p>
 */
public class TransportCorporation {

    private final String id;
    private final String name;
    private final String ownerId;

    /** Current career rank of the corporation */
    private CorporationRank rank;

    /** Available credits for spending */
    private long credits;

    /** Reputation score (0-100) affecting contract availability and prices */
    private int reputation;

    /** Experience points for rank progression */
    private int xp;

    /** IDs of stations owned by this corporation */
    private final Set<String> ownedStationIds;

    /** IDs of vehicles owned by this corporation */
    private final Set<String> ownedVehicleIds;

    /** IDs of trade routes owned by this corporation */
    private final Set<String> ownedRouteIds;

    /** IDs of active contracts */
    private final Set<String> activeContractIds;

    /** Number of planets where this corporation has assets */
    private int planetsOperated;

    /** Lifetime total cargo value delivered (for market share calculation) */
    private long totalCargoDelivered;

    /** Current cargo inventory held by this corporation */
    private final Map<CargoType, Integer> cargoInventory;

    /** Total XP needed for the next rank */
    private int xpToNextRank;

    /** Active RPG quests from AI empires */
    private final List<String> activeQuestIds;

    /** Completed RPG quests history */
    private final List<String> completedQuestIds;

    /**
     * Creates a new transport corporation.
     *
     * @param name      the corporation name
     * @param ownerId   the empire/player ID that owns this corporation
     * @param startingCredits initial credits
     */
    public TransportCorporation(String name, String ownerId, long startingCredits) {
        this.id = UUID.randomUUID().toString();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Corporation name must not be empty");
        }
        if (ownerId == null || ownerId.isEmpty()) {
            throw new IllegalArgumentException("Owner ID must not be empty");
        }
        this.name = name;
        this.ownerId = ownerId;
        this.rank = CorporationRank.FREELANCER;
        this.credits = startingCredits;
        this.reputation = 10;
        this.xp = 0;
        this.ownedStationIds = new HashSet<>();
        this.ownedVehicleIds = new HashSet<>();
        this.ownedRouteIds = new HashSet<>();
        this.activeContractIds = new HashSet<>();
        this.planetsOperated = 0;
        this.totalCargoDelivered = 0;
        this.cargoInventory = new HashMap<>();
        this.xpToNextRank = 100;
        this.activeQuestIds = new java.util.ArrayList<>();
        this.completedQuestIds = new java.util.ArrayList<>();
    }

    /** @return the unique corporation ID */
    public String getId() {
        return id;
    }

    /** @return the corporation display name */
    public String getName() {
        return name;
    }

    /** @return the empire/player ID that owns this corporation */
    public String getOwnerId() {
        return ownerId;
    }

    /** @return the current career rank */
    public CorporationRank getRank() {
        return rank;
    }

    /** Sets the current career rank */
    public void setRank(CorporationRank rank) {
        if (rank == null) {
            throw new IllegalArgumentException("Rank must not be null");
        }
        this.rank = rank;
    }

    /** @return the available credits */
    public long getCredits() {
        return credits;
    }

    /**
     * Adds credits to the corporation.
     *
     * @param amount the amount to add
     */
    public void addCredits(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        }
        this.credits += amount;
    }

    /**
     * Spends credits from the corporation.
     *
     * @param amount the amount to spend
     * @return true if the spend was successful, false if insufficient funds
     */
    public boolean spendCredits(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        }
        if (this.credits < amount) {
            return false;
        }
        this.credits -= amount;
        return true;
    }

    /** @return the reputation score (0-100) */
    public int getReputation() {
        return reputation;
    }

    /**
     * Updates the reputation score.
     *
     * @param delta the change in reputation (positive or negative)
     */
    public void updateReputation(int delta) {
        this.reputation = Math.max(0, Math.min(100, this.reputation + delta));
    }

    /** @return the current experience points */
    public int getXp() {
        return xp;
    }

    /**
     * Adds experience points and checks for rank progression.
     *
     * @param amount the XP to add
     * @return true if the corporation ranked up
     */
    public boolean addXp(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("XP amount must be non-negative: " + amount);
        }
        this.xp += amount;
        if (this.xp >= this.xpToNextRank) {
            this.xp -= this.xpToNextRank;
            this.xpToNextRank = (int) (this.xpToNextRank * 1.5);
            CorporationRank next = this.rank.nextRank();
            if (next != null) {
                this.rank = next;
                return true;
            }
        }
        return false;
    }

    /** @return the XP needed for the next rank */
    public int getXpToNextRank() {
        return xpToNextRank;
    }

    /** @return unmodifiable set of owned station IDs */
    public Set<String> getOwnedStationIds() {
        return Collections.unmodifiableSet(ownedStationIds);
    }

    /**
     * Adds a station to this corporation's assets.
     *
     * @param stationId the station ID
     */
    public void addStation(String stationId) {
        ownedStationIds.add(Objects.requireNonNull(stationId, "Station ID must not be null"));
    }

    /**
     * Removes a station from this corporation's assets.
     *
     * @param stationId the station ID
     * @return true if the station was found and removed
     */
    public boolean removeStation(String stationId) {
        return ownedStationIds.remove(stationId);
    }

    /** @return unmodifiable set of owned vehicle IDs */
    public Set<String> getOwnedVehicleIds() {
        return Collections.unmodifiableSet(ownedVehicleIds);
    }

    /**
     * Adds a vehicle to this corporation's assets.
     *
     * @param vehicleId the vehicle ID
     */
    public void addVehicle(String vehicleId) {
        ownedVehicleIds.add(Objects.requireNonNull(vehicleId, "Vehicle ID must not be null"));
    }

    /**
     * Removes a vehicle from this corporation's assets.
     *
     * @param vehicleId the vehicle ID
     * @return true if the vehicle was found and removed
     */
    public boolean removeVehicle(String vehicleId) {
        return ownedVehicleIds.remove(vehicleId);
    }

    /** @return unmodifiable set of owned route IDs */
    public Set<String> getOwnedRouteIds() {
        return Collections.unmodifiableSet(ownedRouteIds);
    }

    /**
     * Adds a trade route to this corporation's assets.
     *
     * @param routeId the route ID
     */
    public void addRoute(String routeId) {
        ownedRouteIds.add(Objects.requireNonNull(routeId, "Route ID must not be null"));
    }

    /**
     * Removes a trade route from this corporation's assets.
     *
     * @param routeId the route ID
     * @return true if the route was found and removed
     */
    public boolean removeRoute(String routeId) {
        return ownedRouteIds.remove(routeId);
    }

    /** @return unmodifiable set of active contract IDs */
    public Set<String> getActiveContractIds() {
        return Collections.unmodifiableSet(activeContractIds);
    }

    /**
     * Adds a contract to this corporation's active contracts.
     *
     * @param contractId the contract ID
     */
    public void addContract(String contractId) {
        activeContractIds.add(Objects.requireNonNull(contractId, "Contract ID must not be null"));
    }

    /**
     * Removes a contract from this corporation's active contracts.
     *
     * @param contractId the contract ID
     * @return true if the contract was found and removed
     */
    public boolean removeContract(String contractId) {
        return activeContractIds.remove(contractId);
    }

    /** @return the number of planets where this corporation has assets */
    public int getPlanetsOperated() {
        return planetsOperated;
    }

    /** Sets the number of planets operated */
    public void setPlanetsOperated(int planetsOperated) {
        if (planetsOperated < 0) {
            throw new IllegalArgumentException("Planets operated must be non-negative");
        }
        this.planetsOperated = planetsOperated;
    }

    /** @return the lifetime total cargo value delivered */
    public long getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    /**
     * Adds to the total cargo delivered count.
     *
     * @param cargoValue the cargo value delivered
     */
    public void addCargoDelivered(long cargoValue) {
        if (cargoValue < 0) {
            throw new IllegalArgumentException("Cargo value must be non-negative");
        }
        this.totalCargoDelivered += cargoValue;
    }

    /** @return the current cargo inventory */
    public Map<CargoType, Integer> getCargoInventory() {
        return Collections.unmodifiableMap(cargoInventory);
    }

    /**
     * Adds cargo to the inventory.
     *
     * @param type  the cargo type
     * @param amount the amount to add
     */
    public void addCargo(CargoType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        cargoInventory.merge(Objects.requireNonNull(type), amount, Integer::sum);
    }

    /**
     * Removes cargo from the inventory.
     *
     * @param type  the cargo type
     * @param amount the amount to remove
     * @return true if the cargo was removed successfully
     */
    public boolean removeCargo(CargoType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        Integer current = cargoInventory.get(type);
        if (current == null || current < amount) {
            return false;
        }
        if (current == amount) {
            cargoInventory.remove(type);
        } else {
            cargoInventory.put(type, current - amount);
        }
        return true;
    }

    /**
     * Returns the total number of assets (stations + vehicles + routes).
     *
     * @return total asset count
     */
    public int getTotalAssetCount() {
        return ownedStationIds.size() + ownedVehicleIds.size() + ownedRouteIds.size();
    }

    /** @return unmodifiable list of active quest IDs */
    public List<String> getActiveQuestIds() {
        return Collections.unmodifiableList(activeQuestIds);
    }

    /**
     * Adds a quest to this corporation's active quests.
     *
     * @param questId the quest ID
     */
    public void addActiveQuest(String questId) {
        activeQuestIds.add(Objects.requireNonNull(questId, "Quest ID must not be null"));
    }

    /**
     * Removes a quest from active and adds it to completed.
     *
     * @param questId the quest ID
     * @return true if the quest was found and completed
     */
    public boolean completeQuest(String questId) {
        if (activeQuestIds.remove(questId)) {
            completedQuestIds.add(questId);
            return true;
        }
        return false;
    }

    /** @return unmodifiable list of completed quest IDs */
    public List<String> getCompletedQuestIds() {
        return Collections.unmodifiableList(completedQuestIds);
    }

    /**
     * Returns the total number of completed quests.
     *
     * @return completed quest count
     */
    public int getCompletedQuestCount() {
        return completedQuestIds.size();
    }

    @Override
    public String toString() {
        return "TransportCorporation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rank=" + rank.getTitle() +
                ", credits=" + credits +
                ", reputation=" + reputation +
                ", assets=" + getTotalAssetCount() +
                '}';
    }
}
