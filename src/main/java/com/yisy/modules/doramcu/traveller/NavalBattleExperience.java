package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Logs a naval battle experience, extending {@link CharacterExperience} with naval-specific fields.
 * <p>
 * Each naval battle experience captures:
 * </p>
 * <ul>
 *   <li><b>Battle ID</b> — reference to the specific battle</li>
 *   <li><b>Fleet Name</b> — the fleet or vessel name</li>
 *   <li><b>Nationality</b> — the nation (Germany, United States, etc.)</li>
 *   <li><b>Sea Zone</b> — the sea zone where the battle took place</li>
 *   <li><b>Casualties</b> — number of vessels lost</li>
 *   <li><b>Odds Ratio</b> — the combat odds ratio</li>
 *   <li><b>CRT Result</b> — the Combat Results Table result label</li>
 *   <li><b>Vessels</b> — list of vessels used in the battle</li>
 * </ul>
 * <p>
 * Used by {@link NavalCampaignLog} to aggregate battles into campaigns
 * and by {@link NavalCampaignBookGenerator} to generate narrative books.
 * </p>
 */
public class NavalBattleExperience extends CharacterExperience {

    private String battleId;
    private String fleetName;
    private String nationality;
    private String seaZone;
    private int casualties;
    private int enemyCasualties;
    private String oddsRatio;
    private String crtResult;
    private final List<String> vesselNames;
    private int roundsFought;
    private String battleMode;

    /**
     * Creates a new naval battle experience.
     *
     * @param battleId the battle identifier
     * @param title a short headline for the battle
     */
    public NavalBattleExperience(String battleId, String title) {
        super(ExperienceType.BATTLE, title);
        this.battleId = Objects.requireNonNull(battleId, "Battle ID must not be null");
        this.fleetName = "";
        this.nationality = "";
        this.seaZone = "";
        this.casualties = 0;
        this.enemyCasualties = 0;
        this.oddsRatio = "";
        this.crtResult = "";
        this.vesselNames = new ArrayList<>();
        this.roundsFought = 0;
        this.battleMode = "Abstract";
    }

    // ---- Fluent Setters ----

    /**
     * Sets the battle ID.
     *
     * @param battleId the battle identifier
     * @return this experience for chaining
     */
    public NavalBattleExperience battleId(String battleId) {
        this.battleId = Objects.requireNonNull(battleId, "Battle ID must not be null");
        return this;
    }

    /**
     * Sets the fleet name.
     *
     * @param fleetName the fleet or vessel name
     * @return this experience for chaining
     */
    public NavalBattleExperience fleetName(String fleetName) {
        this.fleetName = Objects.requireNonNull(fleetName, "Fleet name must not be null");
        return this;
    }

    /**
     * Sets the nationality.
     *
     * @param nationality the nation (e.g., "Germany", "United States")
     * @return this experience for chaining
     */
    public NavalBattleExperience nationality(String nationality) {
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
        return this;
    }

    /**
     * Sets the sea zone.
     *
     * @param seaZone the sea zone (e.g., "North Atlantic", "Mediterranean")
     * @return this experience for chaining
     */
    public NavalBattleExperience seaZone(String seaZone) {
        this.seaZone = Objects.requireNonNull(seaZone, "Sea zone must not be null");
        return this;
    }

    /**
     * Sets the number of friendly vessels lost.
     *
     * @param casualties the number of casualties
     * @return this experience for chaining
     */
    public NavalBattleExperience casualties(int casualties) {
        this.casualties = Math.max(0, casualties);
        return this;
    }

    /**
     * Sets the number of enemy vessels destroyed.
     *
     * @param enemyCasualties the number of enemy casualties
     * @return this experience for chaining
     */
    public NavalBattleExperience enemyCasualties(int enemyCasualties) {
        this.enemyCasualties = Math.max(0, enemyCasualties);
        return this;
    }

    /**
     * Sets the combat odds ratio.
     *
     * @param oddsRatio the odds ratio (e.g., "2:1", "1:2")
     * @return this experience for chaining
     */
    public NavalBattleExperience oddsRatio(String oddsRatio) {
        this.oddsRatio = Objects.requireNonNull(oddsRatio, "Odds ratio must not be null");
        return this;
    }

    /**
     * Sets the CRT result label.
     *
     * @param crtResult the CRT result (e.g., "SMA", "MPA", "MDA")
     * @return this experience for chaining
     */
    public NavalBattleExperience crtResult(String crtResult) {
        this.crtResult = Objects.requireNonNull(crtResult, "CRT result must not be null");
        return this;
    }

    /**
     * Sets the number of rounds fought.
     *
     * @param rounds the number of rounds
     * @return this experience for chaining
     */
    public NavalBattleExperience roundsFought(int rounds) {
        this.roundsFought = Math.max(0, rounds);
        return this;
    }

    /**
     * Sets the battle mode.
     *
     * @param mode the battle mode (e.g., "Abstract", "Detailed")
     * @return this experience for chaining
     */
    public NavalBattleExperience battleMode(String mode) {
        this.battleMode = Objects.requireNonNull(mode, "Battle mode must not be null");
        return this;
    }

    // ---- Vessel Management ----

    /**
     * Returns the list of vessel names used in the battle.
     *
     * @return unmodifiable list of vessel names
     */
    public List<String> getVesselNames() {
        return Collections.unmodifiableList(vesselNames);
    }

    /**
     * Adds a vessel name to the experience.
     *
     * @param vesselName the name of the vessel
     * @return this experience for chaining
     */
    public NavalBattleExperience addVessel(String vesselName) {
        Objects.requireNonNull(vesselName, "Vessel name must not be null");
        vesselNames.add(vesselName);
        return this;
    }

    // ---- Getters ----

    /** Returns the battle ID. */
    public String getBattleId() { return battleId; }

    /** Returns the fleet name. */
    public String getFleetName() { return fleetName; }

    /** Returns the nationality. */
    public String getNationality() { return nationality; }

    /** Returns the sea zone. */
    public String getSeaZone() { return seaZone; }

    /** Returns the number of friendly casualties. */
    public int getCasualties() { return casualties; }

    /** Returns the number of enemy casualties. */
    public int getEnemyCasualties() { return enemyCasualties; }

    /** Returns the odds ratio. */
    public String getOddsRatio() { return oddsRatio; }

    /** Returns the CRT result. */
    public String getCrtResult() { return crtResult; }

    /** Returns the number of rounds fought. */
    public int getRoundsFought() { return roundsFought; }

    /** Returns the battle mode. */
    public String getBattleMode() { return battleMode; }

    // ---- Narrative Generation ----

    /**
     * Generates a narrative description of the battle.
     * <p>
     * Creates a prose-style description suitable for inclusion in a campaign book.
     * </p>
     *
     * @return a narrative description
     */
    public String generateNarrative() {
        StringBuilder sb = new StringBuilder();

        // Opening
        sb.append("The ");
        if (!seaZone.isEmpty()) {
            sb.append(seaZone).append(" saw ");
        }
        sb.append("the ");
        if (!fleetName.isEmpty()) {
            sb.append(fleetName).append(" engage ");
        }
        sb.append("the enemy fleet");
        if (!seaZone.isEmpty()) {
            sb.append(" in the waters of the " + seaZone);
        }
        sb.append(".\n\n");

        // Battle details
        if (!oddsRatio.isEmpty()) {
            sb.append("The odds stood at ").append(oddsRatio);
            if (oddsRatio.contains(":1") && !oddsRatio.startsWith("1:")) {
                sb.append(", favoring our forces");
            } else if (oddsRatio.endsWith(":1")) {
                sb.append(", favoring the enemy");
            } else {
                sb.append(", nearly even");
            }
            sb.append(".\n\n");
        }

        if (roundsFought > 0) {
            sb.append("The battle raged for ").append(roundsFought).append(" rounds");
            sb.append(" of intense combat.\n\n");
        }

        // Results
        if (!crtResult.isEmpty()) {
            sb.append("The Combat Results Table showed: ").append(crtResult).append(".\n\n");
        }

        // Casualties
        sb.append("Our forces suffered ").append(casualties).append(" vessel(s) lost");
        sb.append(" while destroying ").append(enemyCasualties).append(" enemy vessel(s).\n");

        // Outcome
        if (getOutcome() == Outcome.SUCCESS) {
            sb.append("\nDespite the losses, the battle was a victory for our fleet.");
        } else if (getOutcome() == Outcome.FAILURE) {
            sb.append("\nThe heavy toll weighed on the survivors as they withdrew from the battlefield.");
        } else if (getOutcome() == Outcome.MIXED) {
            sb.append("\nThe battle ended in a pyrrhic victory — we held the field, but at great cost.");
        }

        return sb.toString();
    }

    /**
     * Sets the narrative description.
     *
     * @param narrative the narrative text
     * @return this experience for chaining
     */
    @Override
    public NavalBattleExperience narrative(String narrative) {
        super.narrative(narrative);
        return this;
    }

    /**
     * Auto-generates the narrative and sets it on this experience.
     *
     * @return this experience for chaining
     */
    public NavalBattleExperience autoGenerateNarrative() {
        narrative(generateNarrative());
        return this;
    }

    @Override
    public String toString() {
        return "NavalBattleExperience{" +
                "battleId='" + battleId + "', " +
                "fleet='" + fleetName + "', " +
                "zone='" + seaZone + "', " +
                "casualties=" + casualties + ", " +
                "enemyCasualties=" + enemyCasualties + ", " +
                "outcome=" + getOutcome().getDisplayName() + "}";
    }
}
