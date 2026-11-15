package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges DnD5e character classes to the generic military system.
 * <p>
 * Maps DnD5e classes to GenericUnitType:
 * <ul>
 *   <li>Fighter, Paladin → ARMORED</li>
 *   <li>Rogue, Ranger → SPECIAL_FORCES</li>
 *   <li>Wizard, Sorcerer → SPECIAL_FORCES</li>
 *   <li>Barbarian, Monk → INFANTRY</li>
 *   <li>Cleric, Druid → INFANTRY</li>
 *   <li>Bard → SPECIAL_FORCES</li>
 *   <li>Artificer → ENGINEER</li>
 * </ul>
 * </p>
 */
@Component
public class Dnd5eMilitaryBridge implements ModuleUnitMapper {

    private static final Map<String, GenericUnitType> CLASS_TO_GENERIC = new HashMap<>();
    private static final Map<GenericUnitType, List<String>> GENERIC_TO_CLASS = new EnumMap<>(GenericUnitType.class);

    static {
        // DnD5e class → GenericUnitType mapping
        CLASS_TO_GENERIC.put("Fighter", GenericUnitType.ARMORED);
        CLASS_TO_GENERIC.put("Paladin", GenericUnitType.ARMORED);
        CLASS_TO_GENERIC.put("Rogue", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Ranger", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Wizard", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Sorcerer", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Warlock", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Barbarian", GenericUnitType.INFANTRY);
        CLASS_TO_GENERIC.put("Monk", GenericUnitType.INFANTRY);
        CLASS_TO_GENERIC.put("Cleric", GenericUnitType.INFANTRY);
        CLASS_TO_GENERIC.put("Druid", GenericUnitType.INFANTRY);
        CLASS_TO_GENERIC.put("Bard", GenericUnitType.SPECIAL_FORCES);
        CLASS_TO_GENERIC.put("Artificer", GenericUnitType.ENGINEER);

        // Reverse mapping
        GENERIC_TO_CLASS.put(GenericUnitType.INFANTRY, Arrays.asList("Barbarian", "Monk", "Cleric", "Druid"));
        GENERIC_TO_CLASS.put(GenericUnitType.ARMORED, Arrays.asList("Fighter", "Paladin"));
        GENERIC_TO_CLASS.put(GenericUnitType.SPECIAL_FORCES, Arrays.asList("Rogue", "Ranger", "Wizard", "Sorcerer", "Warlock", "Bard"));
        GENERIC_TO_CLASS.put(GenericUnitType.ENGINEER, Collections.singletonList("Artificer"));
    }

    @Override
    public String getModuleId() {
        return "dnd5e";
    }

    @Override
    public GenericUnitType mapToGeneric(String moduleUnitType) {
        return CLASS_TO_GENERIC.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);
    }

    @Override
    public List<String> mapFromGeneric(GenericUnitType genericType) {
        return GENERIC_TO_CLASS.getOrDefault(genericType, Collections.emptyList());
    }

    @Override
    public List<String> getAvailableUnitTypes(int techLevel) {
        // DnD5e is TL0-TL4, all classes available at TL2+
        if (techLevel >= 2) {
            return new ArrayList<>(CLASS_TO_GENERIC.keySet());
        }
        return Collections.emptyList();
    }

    @Override
    public String getUnitDisplayName(String moduleUnitType) {
        return moduleUnitType;
    }
}
