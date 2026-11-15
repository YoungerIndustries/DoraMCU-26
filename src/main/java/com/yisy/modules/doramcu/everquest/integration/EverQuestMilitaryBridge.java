package com.yisy.modules.doramcu.everquest.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * EverQuest military bridge — maps EverQuest units to generic military types.
 */
@Component
public class EverQuestMilitaryBridge {

    private static final Map<String, GenericUnitType> UNIT_MAPPING = new HashMap<>();

    static {
        UNIT_MAPPING.put("FIGHTER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ROGUE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("RANGER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("BARD", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("SHAMAN", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("PALADIN", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MONSTROSITY", GenericUnitType.ARMORED);
    }

    public GenericUnitType mapToGenericType(String eqUnitType) {
        return UNIT_MAPPING.getOrDefault(eqUnitType, GenericUnitType.INFANTRY);
    }

    public Map<String, GenericUnitType> getAllMappings() {
        return new HashMap<>(UNIT_MAPPING);
    }
}
