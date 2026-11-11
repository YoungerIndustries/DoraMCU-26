package com.yisy.modules.doramcu.babylon5.model;

/**
 * 12-axis personality profile for a Babylon 5 character.
 * Values range from 0.0 to 1.0.
 */
public class B5Personality {

    private double aggression;
    private double diplomacy;
    private double economicFocus;
    private double scientificFocus;
    private double expansionDrive;
    private double riskTolerance;
    private double altruism;
    private double determination;
    private double adaptability;
    private double leadership;
    private double cunning;
    private double honor;

    public B5Personality() {
    }

    public double getAggression() {
        return aggression;
    }

    public void setAggression(double aggression) {
        this.aggression = aggression;
    }

    public double getDiplomacy() {
        return diplomacy;
    }

    public void setDiplomacy(double diplomacy) {
        this.diplomacy = diplomacy;
    }

    public double getEconomicFocus() {
        return economicFocus;
    }

    public void setEconomicFocus(double economicFocus) {
        this.economicFocus = economicFocus;
    }

    public double getScientificFocus() {
        return scientificFocus;
    }

    public void setScientificFocus(double scientificFocus) {
        this.scientificFocus = scientificFocus;
    }

    public double getExpansionDrive() {
        return expansionDrive;
    }

    public void setExpansionDrive(double expansionDrive) {
        this.expansionDrive = expansionDrive;
    }

    public double getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(double riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public double getAltruism() {
        return altruism;
    }

    public void setAltruism(double altruism) {
        this.altruism = altruism;
    }

    public double getDetermination() {
        return determination;
    }

    public void setDetermination(double determination) {
        this.determination = determination;
    }

    public double getAdaptability() {
        return adaptability;
    }

    public void setAdaptability(double adaptability) {
        this.adaptability = adaptability;
    }

    public double getLeadership() {
        return leadership;
    }

    public void setLeadership(double leadership) {
        this.leadership = leadership;
    }

    public double getCunning() {
        return cunning;
    }

    public void setCunning(double cunning) {
        this.cunning = cunning;
    }

    public double getHonor() {
        return honor;
    }

    public void setHonor(double honor) {
        this.honor = honor;
    }

    /**
     * Returns all 12 axes as a map.
     *
     * @return a map of axis name to value
     */
    public java.util.Map<String, Double> toMap() {
        java.util.Map<String, Double> map = new java.util.HashMap<>();
        map.put("aggression", aggression);
        map.put("diplomacy", diplomacy);
        map.put("economicFocus", economicFocus);
        map.put("scientificFocus", scientificFocus);
        map.put("expansionDrive", expansionDrive);
        map.put("riskTolerance", riskTolerance);
        map.put("altruism", altruism);
        map.put("determination", determination);
        map.put("adaptability", adaptability);
        map.put("leadership", leadership);
        map.put("cunning", cunning);
        map.put("honor", honor);
        return map;
    }

    @Override
    public String toString() {
        return "B5Personality{" +
                "aggression=" + String.format("%.2f", aggression) +
                ", diplomacy=" + String.format("%.2f", diplomacy) +
                ", economic=" + String.format("%.2f", economicFocus) +
                ", scientific=" + String.format("%.2f", scientificFocus) +
                ", expansion=" + String.format("%.2f", expansionDrive) +
                ", risk=" + String.format("%.2f", riskTolerance) +
                ", altruism=" + String.format("%.2f", altruism) +
                ", determination=" + String.format("%.2f", determination) +
                ", adaptability=" + String.format("%.2f", adaptability) +
                ", leadership=" + String.format("%.2f", leadership) +
                ", cunning=" + String.format("%.2f", cunning) +
                ", honor=" + String.format("%.2f", honor) +
                '}';
    }
}
