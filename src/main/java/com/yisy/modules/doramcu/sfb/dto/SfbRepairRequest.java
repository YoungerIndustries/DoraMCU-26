package com.yisy.modules.doramcu.sfb.dto;

/**
 * Request to repair a fleet.
 */
public class SfbRepairRequest {
    private String fleetId;
    private int dockyardBonus;

    public String getFleetId() { return fleetId; }
    public void setFleetId(String fleetId) { this.fleetId = fleetId; }
    public int getDockyardBonus() { return dockyardBonus; }
    public void setDockyardBonus(int dockyardBonus) { this.dockyardBonus = dockyardBonus; }
}
