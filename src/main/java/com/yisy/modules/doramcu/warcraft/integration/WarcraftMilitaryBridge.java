package com.yisy.modules.doramcu.warcraft.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Warcraft military bridge — maps Warcraft units to generic military types.
 */
@Component
public class WarcraftMilitaryBridge {

    private static final Map<String, GenericUnitType> UNIT_MAPPING = new HashMap<>();

    static {
        UNIT_MAPPING.put("HUMAN_FOOTMAN", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("ORC_GRUNT", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("UNDEAD_SKELETAL", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("NIGHT_ELF_DRUID", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("WARLOCK", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("PALADIN", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("DEATH_KNIGHT", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("GOLEM", GenericUnitType.ARMORED);
        UNIT_MAPPING.put("HYENA", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("WOLF", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("WISP", GenericUnitType.SPECIAL_FORCES);
    }

    public GenericUnitType mapToGenericType(String warcraftUnitType) {
        return UNIT_MAPPING.getOrDefault(warcraftUnitType, GenericUnitType.INFANTRY);
    }

    public Map<String, GenericUnitType> getAllMappings() {
        return new HashMap<>(UNIT_MAPPING);
    }
}
