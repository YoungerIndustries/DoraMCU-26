package com.yisy.modules.doramcu.warhammer40k.model;

/**
 * Special abilities available to Warhammer 40K units.
 */
public class SpecialAbility {

    private String id;
    private String name;
    private String description;
    private int powerLevel;
    private String targetType;

    public SpecialAbility() {
    }

    public SpecialAbility(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPowerLevel() { return powerLevel; }
    public void setPowerLevel(int powerLevel) { this.powerLevel = powerLevel; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
}
