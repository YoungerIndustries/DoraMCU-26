package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

/**
 * Station types a crew member can man on an SFB ship.
 * Each station has a primary discipline that modifies performance.
 */
public enum SfbCrewStationType {
    HELM("Navigation and movement", CaptainLogCharacter.Discipline.OPERATIONS),
    TACTICAL("Weapons and targeting", CaptainLogCharacter.Discipline.SECURITY),
    ENGINEERING("Shields, power, and repairs", CaptainLogCharacter.Discipline.OPERATIONS),
    SENSORS("Detection and scanning", CaptainLogCharacter.Discipline.SCIENCE),
    MEDICAL("Crew injury recovery", CaptainLogCharacter.Discipline.MEDICINE);

    private final String description;
    private final CaptainLogCharacter.Discipline primaryDiscipline;

    SfbCrewStationType(String description, CaptainLogCharacter.Discipline primaryDiscipline) {
        this.description = description;
        this.primaryDiscipline = primaryDiscipline;
    }

    public String getDescription() {
        return description;
    }

    public CaptainLogCharacter.Discipline getPrimaryDiscipline() {
        return primaryDiscipline;
    }
}
