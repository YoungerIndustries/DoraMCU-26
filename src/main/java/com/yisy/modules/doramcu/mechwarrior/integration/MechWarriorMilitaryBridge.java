package com.yisy.modules.doramcu.mechwarrior.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges MechWarrior mech types to the generic military system.
 */
@Component
public class MechWarriorMilitaryBridge implements ModuleUnitMapper {

    private static final Map<String, GenericUnitType> MECH_TO_GENERIC = new HashMap<>();

    static {
        MECH_TO_GENERIC.put("LIGHT_MECH", GenericUnitType.ARMORED);
        MECH_TO_GENERIC.put("MEDIUM_MECH", GenericUnitType.ARMORED);
        MECH_TO_GENERIC.put("HEAVY_MECH", GenericUnitType.ARMORED);
        MECH_TO_GENERIC.put("ASSAULT_MECH", GenericUnitType.ARMORED);
        MECH_TO_GENERIC.put("COMMAND_MECH", GenericUnitType.SPECIAL_FORCES);
        MECH_TO_GENERIC.put("MECH_PILOT", GenericUnitType.INFANTRY);
    }

    @Override
    public String getModuleId() { return "mechwarrior"; }

    @Override
    public GenericUnitType mapToGeneric(String moduleUnitType) {
        return MECH_TO_GENERIC.getOrDefault(moduleUnitType, GenericUnitType.ARMORED);
    }

    @Override
    public List<String> mapFromGeneric(GenericUnitType genericType) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, GenericUnitType> e : MECH_TO_GENERIC.entrySet()) {
            if (e.getValue() == genericType) result.add(e.getKey());
        }
        return result;
    }

    @Override
    public List<String> getAvailableUnitTypes(int techLevel) {
        return new ArrayList<>(MECH_TO_GENERIC.keySet());
    }

    @Override
    public String getUnitDisplayName(String moduleUnitType) { return moduleUnitType; }
}
