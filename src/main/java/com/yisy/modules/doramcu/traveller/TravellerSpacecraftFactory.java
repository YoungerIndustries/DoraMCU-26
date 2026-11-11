package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates Traveller spacecraft based on TL and world type.
 */
public class TravellerSpacecraftFactory {

    public TravellerSpacecraft createSpacecraft(TravellerSpacecraftType type, int tl) {
        String id = "sc-" + type.name().toLowerCase() + "-" + tl + "-" + System.currentTimeMillis();
        TravellerSpacecraft sc = new TravellerSpacecraft(id, type.getDisplayName() + " TL" + tl, type, tl);
        AirWeaponMount weapon = new AirWeaponMount(
                "w1", AirWeaponMount.MountType.LASER, "Laser " + tl, tl, 3 + tl);
        sc.addWeapon(weapon);
        return sc;
    }

    public List<TravellerSpacecraftType> getAvailableSpacecraft(int tl) {
        List<TravellerSpacecraftType> available = new ArrayList<>();
        for (TravellerSpacecraftType type : TravellerSpacecraftType.values()) {
            if (type.getBaseCost() <= tl * 100) {
                available.add(type);
            }
        }
        return available;
    }

    public boolean canBuild(TravellerSpacecraft spacecraft, int worldTl) {
        return spacecraft.getTechLevel() <= worldTl;
    }
}
