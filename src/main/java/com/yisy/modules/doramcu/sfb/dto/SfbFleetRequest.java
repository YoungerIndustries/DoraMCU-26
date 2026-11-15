package com.yisy.modules.doramcu.sfb.dto;

/**
 * Request to create a new fleet.
 */
public class SfbFleetRequest {
    private String id;
    private String name;
    private int ownerSide;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getOwnerSide() { return ownerSide; }
    public void setOwnerSide(int ownerSide) { this.ownerSide = ownerSide; }
}
