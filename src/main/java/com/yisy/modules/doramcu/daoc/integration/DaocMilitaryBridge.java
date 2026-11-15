package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dark Age of Camelot military bridge — maps DaOC units to generic military types.
 */
@Component
public class DaocMilitaryBridge {

    private static final Map<String, GenericUnitType> UNIT_MAPPING = new HashMap<>();

    static {
        UNIT_MAPPING.put("MARTYR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("CHAMPION", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("WARRIOR", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("RANGER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("BARD", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("DRUID", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
    }

    public GenericUnitType mapToGenericType(String daocUnitType) {
        return UNIT_MAPPING.getOrDefault(daocUnitType, GenericUnitType.INFANTRY);
    }

    public Map<String, GenericUnitType> getAllMappings() {
        return new HashMap<>(UNIT_MAPPING);
    }
}
