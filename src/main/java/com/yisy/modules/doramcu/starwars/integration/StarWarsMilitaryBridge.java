package com.yisy.modules.doramcu.starwars.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/** Bridges Star Wars units to the generic military system. */
@Component
public class StarWarsMilitaryBridge implements ModuleUnitMapper {
    private static final Map<String, GenericUnitType> MAP = new HashMap<>();
    static {
        MAP.put("STORMTROOPER", GenericUnitType.INFANTRY);
        MAP.put("JEDI", GenericUnitType.SPECIAL_FORCES);
        MAP.put("SITH", GenericUnitType.SPECIAL_FORCES);
        MAP.put("ATST", GenericUnitType.ARMORED);
        MAP.put("ATAT", GenericUnitType.ARMORED);
        MAP.put("TIE_FIGHTER", GenericUnitType.AIRCRAFT);
        MAP.put("X_WING", GenericUnitType.AIRCRAFT);
        MAP.put("STAR_DESTROYER", GenericUnitType.SPACECRAFT);
        MAP.put("REBEL_TROOPER", GenericUnitType.INFANTRY);
        MAP.put("BOBA_FETT", GenericUnitType.SPECIAL_FORCES);
    }
    @Override public String getModuleId() { return "starwars"; }
    @Override public GenericUnitType mapToGeneric(String t) { return MAP.getOrDefault(t, GenericUnitType.INFANTRY); }
    @Override public List<String> mapFromGeneric(GenericUnitType g) { List<String> r = new ArrayList<>(); for (var e : MAP.entrySet()) if (e.getValue() == g) r.add(e.getKey()); return r; }
    @Override public List<String> getAvailableUnitTypes(int tl) { return new ArrayList<>(MAP.keySet()); }
    @Override public String getUnitDisplayName(String t) { return t; }
}
