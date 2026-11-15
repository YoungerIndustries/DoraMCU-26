package com.yisy.modules.doramcu.hex.planet.fps.dto;

/**
 * Cargo state for the FPS 3D renderer.
 * <p>
 * Represents cargo items at stations, industries, or in transit as 3D objects.
 * </p>
 */
public class FpsCargoStateDto {

    private final String cargoType; // "IRON_ORE", "STEEL", "GRAIN", etc.
    private final int quantity;
    private final int x, y, z; // Hex position where cargo is located
    private final String locationName; // Name of the station/industry
    private final String category; // "RAW_MATERIAL", "PROCESSED", "ADVANCED", "SPACE_CARGO"
    private final String color; // Visual color for 3D rendering

    public FpsCargoStateDto(String cargoType, int quantity,
                             int x, int y, int z,
                             String locationName, String category, String color) {
        this.cargoType = cargoType;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.locationName = locationName;
        this.category = category;
        this.color = color;
    }

    public String getCargoType() { return cargoType; }
    public int getQuantity() { return quantity; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getLocationName() { return locationName; }
    public String getCategory() { return category; }
    public String getColor() { return color; }
}
