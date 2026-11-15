package com.yisy.modules.doramcu.sfb.dto;

/**
 * Request to build a new ship.
 */
public class SfbBuildShipRequest {
    private String fleetId;
    private String empire;
    private String shipClass;

    public String getFleetId() { return fleetId; }
    public void setFleetId(String fleetId) { this.fleetId = fleetId; }
    public String getEmpire() { return empire; }
    public void setEmpire(String empire) { this.empire = empire; }
    public String getShipClass() { return shipClass; }
    public void setShipClass(String shipClass) { this.shipClass = shipClass; }
}
