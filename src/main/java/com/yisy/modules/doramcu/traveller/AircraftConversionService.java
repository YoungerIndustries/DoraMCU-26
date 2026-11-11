package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Service for converting Third Reich aircraft to Traveller equivalents.
 */
public class AircraftConversionService {
    private final Map<String, AircraftConversion> conversions = new HashMap<>();

    public AircraftConversionService() {
        for (ThirdReichAircraft aircraft : ThirdReichAircraft.values()) {
            conversions.put(aircraft.name(), new AircraftConversion(aircraft));
        }
    }

    public TravellerSpacecraft convert(ThirdReichAircraft aircraft) {
        String id = "traveller-" + aircraft.name().toLowerCase();
        TravellerSpacecraft sc = new TravellerSpacecraft(
                id, aircraft.getName() + " (Traveller)", aircraft.getType(), aircraft.getTechLevel());
        sc.setAttackValue(aircraft.getAttackValue());
        sc.setDefenseValue(aircraft.getDefenseValue());
        sc.setMovement(aircraft.getMovement());
        sc.setCost(aircraft.getCost());
        AirWeaponMount weapon = new AirWeaponMount(
                "w1", AirWeaponMount.MountType.GUN, "Cannon", aircraft.getAttackValue(), 5);
        sc.addWeapon(weapon);
        return sc;
    }

    public AircraftConversion getConversion(ThirdReichAircraft aircraft) {
        return conversions.get(aircraft.name());
    }

    public List<AircraftConversion> getAllConversions() {
        return new ArrayList<>(conversions.values());
    }
}
