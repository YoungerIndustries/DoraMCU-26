package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Third Reich aircraft to Traveller equivalents.
 */
public class AircraftConversion {
    private String thirdReichAircraftId;
    private String travellerSpacecraftName;
    private int techLevel;
    private int cost;
    private List<String> requiredSkills = new ArrayList<>();
    private String description;

    public AircraftConversion() {}

    public AircraftConversion(ThirdReichAircraft aircraft) {
        this.thirdReichAircraftId = aircraft.name();
        this.travellerSpacecraftName = aircraft.getName() + " (Traveller)";
        this.techLevel = aircraft.getTechLevel();
        this.cost = aircraft.getCost();
        this.requiredSkills.add("Pilot");
        this.requiredSkills.add("Engineering");
        this.description = "Converted from " + aircraft.getNationality() + " " + aircraft.getName();
    }

    public String getThirdReichAircraftId() { return thirdReichAircraftId; }
    public void setThirdReichAircraftId(String id) { this.thirdReichAircraftId = id; }
    public String getTravellerSpacecraftName() { return travellerSpacecraftName; }
    public void setTravellerSpacecraftName(String name) { this.travellerSpacecraftName = name; }
    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) { this.techLevel = techLevel; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> skills) { this.requiredSkills = skills; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
