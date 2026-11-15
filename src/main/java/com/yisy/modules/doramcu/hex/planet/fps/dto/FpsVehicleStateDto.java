package com.yisy.modules.doramcu.hex.planet.fps.dto;

/**
 * Vehicle state for the FPS 3D renderer.
 */
public class FpsVehicleStateDto {

    private final String id;
    private final String type;
    private final int x, y, z;
    private final boolean active;
    private final double reliability;
    private final int cargoCount;
    private final int passengerCount;

    public FpsVehicleStateDto(String id, String type, int x, int y, int z,
                               boolean active, double reliability,
                               int cargoCount, int passengerCount) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.active = active;
        this.reliability = reliability;
        this.cargoCount = cargoCount;
        this.passengerCount = passengerCount;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isActive() { return active; }
    public double getReliability() { return reliability; }
    public int getCargoCount() { return cargoCount; }
    public int getPassengerCount() { return passengerCount; }
}
