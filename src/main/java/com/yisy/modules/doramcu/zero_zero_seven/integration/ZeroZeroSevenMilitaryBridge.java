package com.yisy.modules.doramcu.zero_zero_seven.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/** Bridges 007 units to the generic military system. */
@Component
public class ZeroZeroSevenMilitaryBridge implements ModuleUnitMapper {
    private static final Map<String, GenericUnitType> MAP = new HashMap<>();
    static {
        MAP.put("SPY", GenericUnitType.SPECIAL_FORCES);
        MAP.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
        MAP.put("DOUBLE_AGENT", GenericUnitType.SPECIAL_FORCES);
        MAP.put("FIELD_AGENT", GenericUnitType.SPECIAL_FORCES);
        MAP.put("HANDLERS", GenericUnitType.ENGINEER);
    }
    @Override public String getModuleId() { return "zero_zero_seven"; }
    @Override public GenericUnitType mapToGeneric(String t) { return MAP.getOrDefault(t, GenericUnitType.SPECIAL_FORCES); }
    @Override public List<String> mapFromGeneric(GenericUnitType g) { List<String> r = new ArrayList<>(); for (var e : MAP.entrySet()) if (e.getValue() == g) r.add(e.getKey()); return r; }
    @Override public List<String> getAvailableUnitTypes(int tl) { return new ArrayList<>(MAP.keySet()); }
    @Override public String getUnitDisplayName(String t) { return t; }
}
