package com.yisy.modules.doramcu.hex.planet.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DTO representing a Transport Corporation operating on a planet.
 * <p>
 * Includes the corporation's rank, credits, reputation, owned assets
 * (stations, vehicles, routes), and active contracts.
 * </p>
 */
public class CorporationDto {

    private String id;
    private String name;
    private String ownerEmpireId;
    private String rank;
    private long credits;
    private int reputation;
    private int xp;
    private int xpToNextRank;
    private int planetsOperated;
    private long totalCargoDelivered;
    private Set<String> ownedStationIds;
    private Set<String> ownedVehicleIds;
    private Set<String> ownedRouteIds;
    private Set<String> activeContractIds;
    private Map<String, Integer> cargoInventory;
    private List<String> activeQuestIds;
    private List<String> completedQuestIds;
    private int totalAssetCount;

    public CorporationDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXpToNextRank() {
        return xpToNextRank;
    }

    public void setXpToNextRank(int xpToNextRank) {
        this.xpToNextRank = xpToNextRank;
    }

    public int getPlanetsOperated() {
        return planetsOperated;
    }

    public void setPlanetsOperated(int planetsOperated) {
        this.planetsOperated = planetsOperated;
    }

    public long getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void setTotalCargoDelivered(long totalCargoDelivered) {
        this.totalCargoDelivered = totalCargoDelivered;
    }

    public Set<String> getOwnedStationIds() {
        return ownedStationIds;
    }

    public void setOwnedStationIds(Set<String> ownedStationIds) {
        this.ownedStationIds = ownedStationIds;
    }

    public Set<String> getOwnedVehicleIds() {
        return ownedVehicleIds;
    }

    public void setOwnedVehicleIds(Set<String> ownedVehicleIds) {
        this.ownedVehicleIds = ownedVehicleIds;
    }

    public Set<String> getOwnedRouteIds() {
        return ownedRouteIds;
    }

    public void setOwnedRouteIds(Set<String> ownedRouteIds) {
        this.ownedRouteIds = ownedRouteIds;
    }

    public Set<String> getActiveContractIds() {
        return activeContractIds;
    }

    public void setActiveContractIds(Set<String> activeContractIds) {
        this.activeContractIds = activeContractIds;
    }

    public Map<String, Integer> getCargoInventory() {
        return cargoInventory;
    }

    public void setCargoInventory(Map<String, Integer> cargoInventory) {
        this.cargoInventory = cargoInventory;
    }

    public List<String> getActiveQuestIds() {
        return activeQuestIds;
    }

    public void setActiveQuestIds(List<String> activeQuestIds) {
        this.activeQuestIds = activeQuestIds;
    }

    public List<String> getCompletedQuestIds() {
        return completedQuestIds;
    }

    public void setCompletedQuestIds(List<String> completedQuestIds) {
        this.completedQuestIds = completedQuestIds;
    }

    public int getTotalAssetCount() {
        return totalAssetCount;
    }

    public void setTotalAssetCount(int totalAssetCount) {
        this.totalAssetCount = totalAssetCount;
    }
}
