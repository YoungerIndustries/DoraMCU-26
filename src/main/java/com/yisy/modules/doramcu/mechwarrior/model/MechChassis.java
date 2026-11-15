package com.yisy.modules.doramcu.mechwarrior.model;

/**
 * Represents a BattleMech chassis with tonnage, speed, and hardpoints.
 * <p>
 * Each mech has a tonnage class (Light/Medium/Heavy/Assault), movement speed,
 * jump jet capacity, and hardpoints for weapons and armor.
 * </p>
 */
public class MechChassis {

    public enum MechClass {
        LIGHT,    // 20-35 tons
        MEDIUM,   // 40-55 tons
        HEAVY,    // 60-75 tons
        ASSAULT   // 80-100 tons
    }

    public enum MechManufacturer {
        IBC,      // Incorporated BattleMechs Corporation
        Kallon,
        Pinnacle,
        KFB,      // Kerensky Fabricators
        AFS,      // Andurien Forge Systems
        SMC,      // Shadowevan Manufacturing Company
        LFK,      // Liao Ferrokinetics
        TNS,      // Tharkad Naval Systems
        WSM,      // Wo Sung Manufacturing
        JIN,      // JinTech
        SNB,      // Snow Raven BattleWorks
        CLAN      // Generic Clan manufacturer
    }

    private String id;
    private String name;
    private String designation;
    private MechClass mechClass;
    private int tonnage;
    private double maxSpeed;        // km/h
    private int jumpJets;           // number of jump jets
    private int heatSinkSlots;      // number of heat sink slots
    private int armorPoints;        // total armor points available
    private int structurePoints;    // total structure points
    private int engineRating;       // engine rating
    private int hardpointEnergy;    // energy weapon hardpoints
    private int hardpointBallistic; // ballistic weapon hardpoints
    private int hardpointMissile;   // missile weapon hardpoints
    private int criticalSlots;      // total critical hit slots
    private MechManufacturer manufacturer;
    private String faction;         // IS, Clan, etc.
    private String description;

    public MechChassis() {
    }

    public MechChassis(String name, String designation, MechClass mechClass, int tonnage) {
        this.name = name;
        this.designation = designation;
        this.mechClass = mechClass;
        this.tonnage = tonnage;
        this.heatSinkSlots = tonnage / 5;
        this.armorPoints = tonnage * 2;
        this.structurePoints = tonnage;
        this.engineRating = tonnage * 2;
        this.criticalSlots = tonnage;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public MechClass getMechClass() {
        return mechClass;
    }

    public void setMechClass(MechClass mechClass) {
        this.mechClass = mechClass;
    }

    public int getTonnage() {
        return tonnage;
    }

    public void setTonnage(int tonnage) {
        this.tonnage = tonnage;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getJumpJets() {
        return jumpJets;
    }

    public void setJumpJets(int jumpJets) {
        this.jumpJets = jumpJets;
    }

    public int getHeatSinkSlots() {
        return heatSinkSlots;
    }

    public void setHeatSinkSlots(int heatSinkSlots) {
        this.heatSinkSlots = heatSinkSlots;
    }

    public int getArmorPoints() {
        return armorPoints;
    }

    public void setArmorPoints(int armorPoints) {
        this.armorPoints = armorPoints;
    }

    public int getStructurePoints() {
        return structurePoints;
    }

    public void setStructurePoints(int structurePoints) {
        this.structurePoints = structurePoints;
    }

    public int getEngineRating() {
        return engineRating;
    }

    public void setEngineRating(int engineRating) {
        this.engineRating = engineRating;
    }

    public int getHardpointEnergy() {
        return hardpointEnergy;
    }

    public void setHardpointEnergy(int hardpointEnergy) {
        this.hardpointEnergy = hardpointEnergy;
    }

    public int getHardpointBallistic() {
        return hardpointBallistic;
    }

    public void setHardpointBallistic(int hardpointBallistic) {
        this.hardpointBallistic = hardpointBallistic;
    }

    public int getHardpointMissile() {
        return hardpointMissile;
    }

    public void setHardpointMissile(int hardpointMissile) {
        this.hardpointMissile = hardpointMissile;
    }

    public int getCriticalSlots() {
        return criticalSlots;
    }

    public void setCriticalSlots(int criticalSlots) {
        this.criticalSlots = criticalSlots;
    }

    public MechManufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(MechManufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
