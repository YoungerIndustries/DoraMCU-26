package com.yisy.modules.doramcu.traveller;

/**
 * Enum for High Guard ship armor types.
 */
public enum HighGuardArmor {

    CRYSTALIIRON(8, 0.04, ArmorMaterial.CRYSTALIRON, 10, "Crystal-iron composite armor"),
    CRYSTALIRON_2(10, 0.05, ArmorMaterial.CRYSTALIRON, 11, "Double layer crystal-iron armor"),
    CRYSTALIRON_3(12, 0.06, ArmorMaterial.CRYSTALIRON, 12, "Triple layer crystal-iron armor"),
    STANDARD(4, 0.02, ArmorMaterial.STANDARD, 5, "Standard steel armor"),
    REINFORCED(6, 0.03, ArmorMaterial.REINFORCED, 7, "Reinforced steel armor"),
    TITANIUM_STEEL(5, 0.025, ArmorMaterial.TITANIUM_STEEL, 6, "Titanium-steel alloy armor"),
    TITANIUM_STEEL_1(6, 0.03, ArmorMaterial.TITANIUM_STEEL, 7, "Single layer titanium-steel armor"),
    TITANIUM_STEEL_2(7, 0.035, ArmorMaterial.TITANIUM_STEEL, 8, "Double layer titanium-steel armor"),
    TITANIUM_STEEL_3(8, 0.04, ArmorMaterial.TITANIUM_STEEL, 9, "Triple layer titanium-steel armor"),
    BONDED_SUPERDENSE(7, 0.035, ArmorMaterial.BONDED_SUPERDENSE, 9, "Bonded superdense armor"),
    BONDED_SUPERDENSE_2(9, 0.045, ArmorMaterial.BONDED_SUPERDENSE, 10, "Double layer bonded superdense armor"),
    BONDED_SUPERDENSE_3(11, 0.055, ArmorMaterial.BONDED_SUPERDENSE, 11, "Triple layer bonded superdense armor"),
    NONE(0, 0.0, ArmorMaterial.NONE, 0, "No armor");

    private final int protection;
    private final double costPerTon;
    private final ArmorMaterial material;
    private final int minTechLevel;
    private final String description;

    HighGuardArmor(int protection, double costPerTon, ArmorMaterial material, int minTechLevel, String description) {
        this.protection = protection;
        this.costPerTon = costPerTon;
        this.material = material;
        this.minTechLevel = minTechLevel;
        this.description = description;
    }

    public String getName() {
        return name().replace('_', ' ');
    }

    public int getProtection() {
        return protection;
    }

    public double getHullPercent() {
        return costPerTon * 100;
    }

    public ArmorMaterial getMaterial() {
        return material;
    }

    public int getMinTechLevel() {
        return minTechLevel;
    }

    public String getDescription() {
        return description;
    }

    public int getTonsRequired(int totalTonnage) {
        return (int) (totalTonnage * costPerTon);
    }

    public int getLayers() {
        if (name().contains("_3")) return 3;
        if (name().contains("_2")) return 2;
        if (name().contains("_1")) return 1;
        return 1;
    }

    public static int calculateArmorCostPerTon(int protection) {
        return protection * 50;
    }

    public enum ArmorMaterial {
        CRYSTALIRON, STANDARD, REINFORCED, TITANIUM_STEEL, BONDED_SUPERDENSE, NONE
    }
}
