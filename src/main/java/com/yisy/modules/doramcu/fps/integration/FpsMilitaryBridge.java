package com.yisy.modules.doramcu.fps.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges FPS weapons/enemies to the generic military system.
 */
@Component
public class FpsMilitaryBridge implements ModuleUnitMapper {

    private static final Map<String, GenericUnitType> WEAPON_TO_GENERIC = new HashMap<>();

    static {
        WEAPON_TO_GENERIC.put("PISTOL", GenericUnitType.INFANTRY);
        WEAPON_TO_GENERIC.put("RIFLE", GenericUnitType.INFANTRY);
        WEAPON_TO_GENERIC.put("SHOTGUN", GenericUnitType.INFANTRY);
        WEAPON_TO_GENERIC.put("MACHINE_GUN", GenericUnitType.ARTILLERY);
        WEAPON_TO_GENERIC.put("SNIPER", GenericUnitType.SPECIAL_FORCES);
        WEAPON_TO_GENERIC.put("ROCKET_LAUNCHER", GenericUnitType.ARTILLERY);
        WEAPON_TO_GENERIC.put("GRENADE", GenericUnitType.ARTILLERY);
    }

    @Override
    public String getModuleId() { return "fps"; }

    @Override
    public GenericUnitType mapToGeneric(String moduleUnitType) {
        return WEAPON_TO_GENERIC.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);
    }

    @Override
    public List<String> mapFromGeneric(GenericUnitType genericType) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, GenericUnitType> e : WEAPON_TO_GENERIC.entrySet()) {
            if (e.getValue() == genericType) result.add(e.getKey());
        }
        return result;
    }

    @Override
    public List<String> getAvailableUnitTypes(int techLevel) {
        return new ArrayList<>(WEAPON_TO_GENERIC.keySet());
    }

    @Override
    public String getUnitDisplayName(String moduleUnitType) { return moduleUnitType; }
}
