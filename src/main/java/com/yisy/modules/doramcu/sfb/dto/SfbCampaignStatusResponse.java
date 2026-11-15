package com.yisy.modules.doramcu.sfb.dto;

/**
 * Response containing overall campaign status.
 */
public class SfbCampaignStatusResponse {
    private int fleetCount;
    private int activeBattleCount;
    private int shipyardSize;

    public SfbCampaignStatusResponse(int fleetCount, int activeBattleCount, int shipyardSize) {
        this.fleetCount = fleetCount;
        this.activeBattleCount = activeBattleCount;
        this.shipyardSize = shipyardSize;
    }

    public int getFleetCount() { return fleetCount; }
    public void setFleetCount(int fleetCount) { this.fleetCount = fleetCount; }
    public int getActiveBattleCount() { return activeBattleCount; }
    public void setActiveBattleCount(int activeBattleCount) { this.activeBattleCount = activeBattleCount; }
    public int getShipyardSize() { return shipyardSize; }
    public void setShipyardSize(int shipyardSize) { this.shipyardSize = shipyardSize; }
}
