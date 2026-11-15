package com.yisy.modules.doramcu.hex.planet.military;

/**
 * Module-agnostic battle unit that allows any game module's units to participate
 * in universal battles resolved by the 3rd Reich combat engine.
 * <p>
 * A UniversalBattleUnit carries the module identifier, module-specific unit type,
 * generic unit type mapping, combat stats, and 3D model reference. The
 * {@link UniversalBattleManager} converts these units to {@link com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit}
 * for battle resolution, then maps results back.
 * </p>
 * <p>
 * Example: A MechWarrior BattleMech becomes a UniversalBattleUnit with
 * moduleId="MechWarrior", moduleUnitType="HEAVY_MECH", genericType=ARMORED,
 * and a 3D model path. The battle resolver sees it as a ThirdReichGroundUnit
 * of type TANK, but the FPS renderer knows it's a mech from MechWarrior.
 * </p>
 */
public class UniversalBattleUnit {

    private final String id;
    private final String name;
    private final String moduleId;
    private final String moduleUnitType;
    private final GenericUnitType genericType;
    private String faction;
    private int strength;
    private int maxStrength;
    private int morale;
    private int attackValue;
    private int defenseValue;
    private int mobility;
    private int supplyCost;
    private String commanderId;
    private String model3dPath;
    private String texturePath;
    private double aiCoreBonus;
    private int hexX, hexY, hexZ;

    /**
     * Builder for constructing UniversalBattleUnit instances.
     */
    public static class Builder {
        private final String id;
        private final String name;
        private final String moduleId;
        private final String moduleUnitType;
        private final GenericUnitType genericType;
        private String faction = "NEUTRAL";
        private int strength = 100;
        private int maxStrength = 100;
        private int morale = 100;
        private int attackValue;
        private int defenseValue;
        private int mobility;
        private int supplyCost;
        private String commanderId;
        private String model3dPath;
        private String texturePath;
        private double aiCoreBonus = 0.0;
        private int hexX, hexY, hexZ;

        public Builder(String id, String name, String moduleId, String moduleUnitType, GenericUnitType genericType) {
            this.id = id;
            this.name = name;
            this.moduleId = moduleId;
            this.moduleUnitType = moduleUnitType;
            this.genericType = genericType;
            this.attackValue = genericType.getBaseAttack();
            this.defenseValue = genericType.getBaseDefense();
            this.mobility = genericType.getMobility();
            this.supplyCost = genericType.getSupplyCost();
        }

        public Builder faction(String faction) {
            this.faction = faction;
            return this;
        }

        public Builder strength(int strength) {
            this.strength = strength;
            return this;
        }

        public Builder maxStrength(int maxStrength) {
            this.maxStrength = maxStrength;
            this.strength = Math.min(this.strength, maxStrength);
            return this;
        }

        public Builder morale(int morale) {
            this.morale = Math.max(0, Math.min(100, morale));
            return this;
        }

        public Builder attackValue(int attackValue) {
            this.attackValue = attackValue;
            return this;
        }

        public Builder defenseValue(int defenseValue) {
            this.defenseValue = defenseValue;
            return this;
        }

        public Builder mobility(int mobility) {
            this.mobility = mobility;
            return this;
        }

        public Builder supplyCost(int supplyCost) {
            this.supplyCost = supplyCost;
            return this;
        }

        public Builder commanderId(String commanderId) {
            this.commanderId = commanderId;
            return this;
        }

        public Builder model3dPath(String model3dPath) {
            this.model3dPath = model3dPath;
            return this;
        }

        public Builder texturePath(String texturePath) {
            this.texturePath = texturePath;
            return this;
        }

        public Builder aiCoreBonus(double aiCoreBonus) {
            this.aiCoreBonus = aiCoreBonus;
            return this;
        }

        public Builder position(int hexX, int hexY, int hexZ) {
            this.hexX = hexX;
            this.hexY = hexY;
            this.hexZ = hexZ;
            return this;
        }

        public UniversalBattleUnit build() {
            return new UniversalBattleUnit(this);
        }
    }

    private UniversalBattleUnit(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.moduleId = builder.moduleId;
        this.moduleUnitType = builder.moduleUnitType;
        this.genericType = builder.genericType;
        this.faction = builder.faction;
        this.strength = builder.strength;
        this.maxStrength = builder.maxStrength;
        this.morale = builder.morale;
        this.attackValue = builder.attackValue;
        this.defenseValue = builder.defenseValue;
        this.mobility = builder.mobility;
        this.supplyCost = builder.supplyCost;
        this.commanderId = builder.commanderId;
        this.model3dPath = builder.model3dPath;
        this.texturePath = builder.texturePath;
        this.aiCoreBonus = builder.aiCoreBonus;
        this.hexX = builder.hexX;
        this.hexY = builder.hexY;
        this.hexZ = builder.hexZ;
    }

    /**
     * Create a new builder for UniversalBattleUnit.
     */
    public static Builder builder(String id, String name, String moduleId, String moduleUnitType, GenericUnitType genericType) {
        return new Builder(id, name, moduleId, moduleUnitType, genericType);
    }

    // ---- Accessors ----

    public String getId() { return id; }
    public String getName() { return name; }
    public String getModuleId() { return moduleId; }
    public String getModuleUnitType() { return moduleUnitType; }
    public GenericUnitType getGenericType() { return genericType; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }

    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = Math.max(0, Math.min(maxStrength, strength)); }

    public int getMaxStrength() { return maxStrength; }
    public void setMaxStrength(int maxStrength) {
        this.maxStrength = maxStrength;
        this.strength = Math.min(strength, maxStrength);
    }

    public int getMorale() { return morale; }
    public void setMorale(int morale) { this.morale = Math.max(0, Math.min(100, morale)); }

    public int getAttackValue() { return attackValue; }
    public void setAttackValue(int attackValue) { this.attackValue = attackValue; }

    public int getDefenseValue() { return defenseValue; }
    public void setDefenseValue(int defenseValue) { this.defenseValue = defenseValue; }

    public int getMobility() { return mobility; }
    public void setMobility(int mobility) { this.mobility = mobility; }

    public int getSupplyCost() { return supplyCost; }
    public void setSupplyCost(int supplyCost) { this.supplyCost = supplyCost; }

    public String getCommanderId() { return commanderId; }
    public void setCommanderId(String commanderId) { this.commanderId = commanderId; }

    public String getModel3dPath() { return model3dPath; }
    public void setModel3dPath(String model3dPath) { this.model3dPath = model3dPath; }

    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }

    public double getAiCoreBonus() { return aiCoreBonus; }
    public void setAiCoreBonus(double aiCoreBonus) { this.aiCoreBonus = aiCoreBonus; }

    public int getHexX() { return hexX; }
    public int getHexY() { return hexY; }
    public int getHexZ() { return hexZ; }
    public void setPosition(int hexX, int hexY, int hexZ) {
        this.hexX = hexX;
        this.hexY = hexY;
        this.hexZ = hexZ;
    }

    // ---- Combat Calculations ----

    /**
     * Returns the effective attack value modified by strength, morale, and AI Core bonus.
     */
    public int getEffectiveAttack() {
        double strengthRatio = strength / (double) maxStrength;
        double moraleFactor = Math.max(0.5, morale / 100.0);
        double aiCoreMultiplier = 1.0 + aiCoreBonus;
        return Math.max(1, (int) (attackValue * strengthRatio * moraleFactor * aiCoreMultiplier));
    }

    /**
     * Returns the effective defense value modified by strength, morale, and AI Core bonus.
     */
    public int getEffectiveDefense() {
        double strengthRatio = strength / (double) maxStrength;
        double moraleFactor = Math.max(0.5, morale / 100.0);
        double aiCoreMultiplier = 1.0 + aiCoreBonus;
        return Math.max(1, (int) (defenseValue * strengthRatio * moraleFactor * aiCoreMultiplier));
    }

    /**
     * Applies damage to this unit. Returns true if the unit is destroyed.
     */
    public boolean takeDamage(int damage) {
        strength = Math.max(0, strength - damage);
        if (strength <= 0) {
            morale = 0;
        }
        return strength <= 0;
    }

    /**
     * Adjusts morale.
     */
    public void adjustMorale(int delta) {
        morale = Math.max(0, Math.min(100, morale + delta));
    }

    /**
     * Returns true if this unit is still combat-effective.
     */
    public boolean isCombatEffective() {
        return strength > 0 && morale > 0;
    }

    @Override
    public String toString() {
        return String.format("UniversalUnit{id='%s', module='%s', type='%s', generic=%s, strength=%d/%d, morale=%d}",
                id, moduleId, moduleUnitType, genericType, strength, maxStrength, morale);
    }
}
