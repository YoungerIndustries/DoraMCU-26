package com.yisy.modules.doramcu.hex.planet.fps.dto;

/**
 * Industry state for the FPS 3D renderer.
 */
public class FpsIndustryStateDto {

    private final String id;
    private final String type;
    private final int x, y, z;
    private final boolean active;
    private final int workerCount;
    private final int productionLevel;

    public FpsIndustryStateDto(String id, String type, int x, int y, int z,
                                boolean active, int workerCount, int productionLevel) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.active = active;
        this.workerCount = workerCount;
        this.productionLevel = productionLevel;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isActive() { return active; }
    public int getWorkerCount() { return workerCount; }
    public int getProductionLevel() { return productionLevel; }
}
