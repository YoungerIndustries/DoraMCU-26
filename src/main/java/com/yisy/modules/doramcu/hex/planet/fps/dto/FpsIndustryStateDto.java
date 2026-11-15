package com.yisy.modules.doramcu.hex.planet.fps.dto;

/**
 * Industry state for the FPS 3D renderer.
 * <p>
 * Includes the industry type enum name (e.g. "MINE", "STEEL_MILL") so the 3D renderer
 * can select the correct visual model, as well as the display name for tooltips.
 * </p>
 */
public class FpsIndustryStateDto {

    private final String id;
    private final String typeEnum;       // e.g. "MINE", "STEEL_MILL" – for 3D model selection
    private final String displayName;    // e.g. "Mine", "Steel Mill" – for tooltips
    private final int x, y, z;
    private final boolean active;
    private final int workerCount;
    private final int productionLevel;

    public FpsIndustryStateDto(String id, String typeEnum, String displayName, int x, int y, int z,
                                boolean active, int workerCount, int productionLevel) {
        this.id = id;
        this.typeEnum = typeEnum;
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.active = active;
        this.workerCount = workerCount;
        this.productionLevel = productionLevel;
    }

    public String getId() { return id; }
    public String getTypeEnum() { return typeEnum; }
    public String getDisplayName() { return displayName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isActive() { return active; }
    public int getWorkerCount() { return workerCount; }
    public int getProductionLevel() { return productionLevel; }
}
