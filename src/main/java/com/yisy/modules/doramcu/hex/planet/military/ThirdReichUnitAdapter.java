package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Adapter that converts between {@link GenericUnitType} and {@link ThirdReichUnitType}.
 * <p>
 * This allows the generic military production system to work with the 3rd Reich
 * battle resolution engine. The 3rd Reich engine remains the battle resolver,
 * but units can come from any module via the generic type system.
 * </p>
 */
@Component
public class ThirdReichUnitAdapter implements ModuleUnitMapper {

    private static final Map<GenericUnitType, ThirdReichUnitType> GENERIC_TO_THIRD_REICH = new EnumMap<>(GenericUnitType.class);
    private static final Map<ThirdReichUnitType, GenericUnitType> THIRD_REICH_TO_GENERIC = new EnumMap<>(ThirdReichUnitType.class);

    static {
        // Generic → Third Reich mapping
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.INFANTRY, ThirdReichUnitType.INFANTRY);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.ARMORED, ThirdReichUnitType.TANK);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.ARTILLERY, ThirdReichUnitType.ARTILLERY);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.AIRCRAFT, ThirdReichUnitType.AIR_SUPPORT);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.NAVAL, ThirdReichUnitType.MECHANIZED);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.SPACECRAFT, ThirdReichUnitType.MECH);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.SPECIAL_FORCES, ThirdReichUnitType.RECON);
        GENERIC_TO_THIRD_REICH.put(GenericUnitType.ENGINEER, ThirdReichUnitType.ENGINEER);

        // Third Reich → Generic mapping (reverse)
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.INFANTRY, GenericUnitType.INFANTRY);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.MECHANIZED, GenericUnitType.ARMORED);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.TANK, GenericUnitType.ARMORED);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.ARTILLERY, GenericUnitType.ARTILLERY);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.ANTI_AIR, GenericUnitType.ARTILLERY);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.RECON, GenericUnitType.SPECIAL_FORCES);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.ENGINEER, GenericUnitType.ENGINEER);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.AIR_SUPPORT, GenericUnitType.AIRCRAFT);
        THIRD_REICH_TO_GENERIC.put(ThirdReichUnitType.MECH, GenericUnitType.SPACECRAFT);
    }

    /**
     * Converts a GenericUnitType to a ThirdReichUnitType for battle resolution.
     *
     * @param genericType the generic unit type
     * @return the corresponding Third Reich unit type
     */
    public ThirdReichUnitType toThirdReich(GenericUnitType genericType) {
        return GENERIC_TO_THIRD_REICH.getOrDefault(genericType, ThirdReichUnitType.INFANTRY);
    }

    /**
     * Converts a ThirdReichUnitType to a GenericUnitType.
     *
     * @param thirdReichType the Third Reich unit type
     * @return the corresponding generic unit type
     */
    public GenericUnitType fromThirdReich(ThirdReichUnitType thirdReichType) {
        return THIRD_REICH_TO_GENERIC.getOrDefault(thirdReichType, GenericUnitType.INFANTRY);
    }

    @Override
    public String getModuleId() {
        return "thirdreich";
    }

    @Override
    public GenericUnitType mapToGeneric(String moduleUnitType) {
        try {
            ThirdReichUnitType thirdReichType = ThirdReichUnitType.valueOf(moduleUnitType);
            return fromThirdReich(thirdReichType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<String> mapFromGeneric(GenericUnitType genericType) {
        ThirdReichUnitType thirdReichType = toThirdReich(genericType);
        return thirdReichType != null ? Collections.singletonList(thirdReichType.name()) : Collections.emptyList();
    }

    @Override
    public List<String> getAvailableUnitTypes(int techLevel) {
        List<String> available = new ArrayList<>();
        for (ThirdReichUnitType type : ThirdReichUnitType.values()) {
            if (techLevel >= 5 && type == ThirdReichUnitType.INFANTRY) available.add(type.name());
            if (techLevel >= 5 && type == ThirdReichUnitType.MECHANIZED) available.add(type.name());
            if (techLevel >= 5 && type == ThirdReichUnitType.ENGINEER) available.add(type.name());
            if (techLevel >= 6 && type == ThirdReichUnitType.ARTILLERY) available.add(type.name());
            if (techLevel >= 6 && type == ThirdReichUnitType.ANTI_AIR) available.add(type.name());
            if (techLevel >= 6 && type == ThirdReichUnitType.RECON) available.add(type.name());
            if (techLevel >= 7 && type == ThirdReichUnitType.TANK) available.add(type.name());
            if (techLevel >= 7 && type == ThirdReichUnitType.AIR_SUPPORT) available.add(type.name());
            if (techLevel >= 10 && type == ThirdReichUnitType.MECH) available.add(type.name());
        }
        return available;
    }

    @Override
    public String getUnitDisplayName(String moduleUnitType) {
        try {
            return ThirdReichUnitType.valueOf(moduleUnitType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return moduleUnitType;
        }
    }
}
