package com.yisy.modules.doramcu.warhammer40k.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Warhammer 40K military bridge — maps Warhammer 40K units to generic military types.
 */
@Component
public class WarhammerMilitaryBridge {

    private static final Map<String, GenericUnitType> UNIT_MAPPING = new HashMap<>();

    static {
        UNIT_MAPPING.put("SPACE_MARINE", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("ORK", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("CHAOS_MARINE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("NECRON", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("ELDAR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("TYRANID", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("IMPERIAL_GUARD", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("DREADNOUGHT", GenericUnitType.ARMORED);
        UNIT_MAPPING.put("BATTLE_TANK", GenericUnitType.ARMORED);
        UNIT_MAPPING.put("DROP_POD", GenericUnitType.AIRCRAFT);
    }

    public GenericUnitType mapToGenericType(String warhammerUnitType) {
        return UNIT_MAPPING.getOrDefault(warhammerUnitType, GenericUnitType.INFANTRY);
    }

    public Map<String, GenericUnitType> getAllMappings() {
        return new HashMap<>(UNIT_MAPPING);
    }
}
