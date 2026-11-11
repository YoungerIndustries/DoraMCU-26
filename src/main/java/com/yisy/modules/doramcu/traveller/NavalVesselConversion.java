package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Maps a Third Reich naval vessel to its Traveller equivalent.
 * <p>
 * This conversion mapping defines how a historical naval vessel (from the 3R4 game)
 * translates to a Traveller naval vessel that can be built on appropriate Tech Worlds.
 * The conversion includes:
 * </p>
 * <ul>
 *   <li><b>Tech Level</b> — the TL required to build the Traveller equivalent (TL7-9 for WWII-era)</li>
 *   <li><b>Cost</b> — construction cost in credits</li>
 *   <li><b>Required Skills</b> — skills needed to design and build the vessel</li>
 *   <li><b>Description</b> — notes about the conversion and any differences</li>
 * </ul>
 * <p>
 * Used by {@link NavalVesselConversionService} to perform the actual conversion.
 * </p>
 */
public class NavalVesselConversion {

    private final String id;
    private final String thirdReichVesselId;
    private String travellerVesselName;
    private TravellerNavalVessel.VesselType travellerType;
    private int techLevel;
    private int armorClass;
    private int tonnage;
    private long cost;
    private final List<String> requiredSkills;
    private String description;

    /**
     * Creates a new conversion mapping.
     *
     * @param id the unique conversion ID
     * @param thirdReichVesselId the ID of the source Third Reich vessel
     */
    public NavalVesselConversion(String id, String thirdReichVesselId) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.thirdReichVesselId = Objects.requireNonNull(thirdReichVesselId, "Third Reich vessel ID must not be null");
        this.travellerVesselName = "";
        this.techLevel = 7;
        this.armorClass = 0;
        this.tonnage = 1000;
        this.cost = 0;
        this.requiredSkills = new ArrayList<>();
        this.description = "";
    }

    // ---- Accessors & Mutators ----

    /** Returns the conversion ID. */
    public String getId() { return id; }

    /** Returns the source Third Reich vessel ID. */
    public String getThirdReichVesselId() { return thirdReichVesselId; }

    /** Returns the Traveller vessel name. */
    public String getTravellerVesselName() { return travellerVesselName; }

    /** Sets the Traveller vessel name. */
    public void setTravellerVesselName(String travellerVesselName) {
        this.travellerVesselName = Objects.requireNonNull(travellerVesselName, "Name must not be null");
    }

    /** Returns the Traveller vessel type. */
    public TravellerNavalVessel.VesselType getTravellerType() { return travellerType; }

    /** Sets the Traveller vessel type. */
    public void setTravellerType(TravellerNavalVessel.VesselType travellerType) {
        this.travellerType = Objects.requireNonNull(travellerType, "Type must not be null");
    }

    /** Returns the required tech level (7-15). */
    public int getTechLevel() { return techLevel; }

    /** Sets the required tech level. */
    public void setTechLevel(int techLevel) {
        this.techLevel = Math.max(7, Math.min(15, techLevel));
    }

    /** Returns the armor class (0-9). */
    public int getArmorClass() { return armorClass; }

    /** Sets the armor class. */
    public void setArmorClass(int armorClass) {
        this.armorClass = Math.max(0, Math.min(9, armorClass));
    }

    /** Returns the tonnage. */
    public int getTonnage() { return tonnage; }

    /** Sets the tonnage. */
    public void setTonnage(int tonnage) {
        this.tonnage = Math.max(100, tonnage);
    }

    /** Returns the construction cost in credits. */
    public long getCost() { return cost; }

    /** Sets the construction cost. */
    public void setCost(long cost) {
        this.cost = Math.max(0, cost);
    }

    /** Returns the list of required skills. */
    public List<String> getRequiredSkills() {
        return Collections.unmodifiableList(requiredSkills);
    }

    /**
     * Adds a required skill.
     *
     * @param skill the skill name
     */
    public void addRequiredSkill(String skill) {
        Objects.requireNonNull(skill, "Skill must not be null");
        requiredSkills.add(skill);
    }

    /** Returns the conversion description. */
    public String getDescription() { return description; }

    /** Sets the conversion description. */
    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    // ---- Utility Methods ----

    /**
     * Checks if a world with the given tech level can build this vessel.
     *
     * @param worldTl the world's tech level
     * @return true if the vessel can be built
     */
    public boolean canBuildOnWorld(int worldTl) {
        return worldTl >= techLevel;
    }

    /**
     * Creates a Traveller naval vessel from this conversion.
     *
     * @param name the vessel name
     * @return a new TravellerNavalVessel based on this conversion
     */
    public TravellerNavalVessel createVessel(String name) {
        if (travellerType == null) {
            throw new IllegalStateException("Traveller type not set for conversion: " + id);
        }
        TravellerNavalVessel vessel = new TravellerNavalVessel(id + "-traveller", name, travellerType);
        vessel.setTechLevel(techLevel);
        vessel.setArmorClass(armorClass);
        if (tonnage >= travellerType.getMinTonnage() && tonnage <= travellerType.getMaxTonnage()) {
            vessel.setTotalTonnage(tonnage);
        }
        return vessel;
    }

    /**
     * Returns a summary of the conversion.
     *
     * @return a formatted summary
     */
    public String getSummary() {
        return String.format("%s → %s (%s, TL%d, %d tons, %d cr)",
                thirdReichVesselId, travellerVesselName,
                travellerType != null ? travellerType.getDisplayName() : "N/A",
                techLevel, tonnage, cost);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
