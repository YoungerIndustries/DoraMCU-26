package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.AiPersonality;
import com.yisy.modules.doramcu.ai.DynamicAiPersonality;
import com.yisy.modules.doramcu.hex.Player;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified personality calculator.
 */
@Component
public class PersonalityCalculator {

    public static final String[] CORE_AXES = {
            "aggression", "expansionDrive", "economicFocus",
            "scientificFocus", "diplomaticStance", "riskTolerance"
    };

    public static final String[] EXTENDED_AXES = {
            "aggression", "expansionDrive", "economicFocus",
            "scientificFocus", "diplomaticStance", "riskTolerance",
            "altruism", "determination", "adaptability",
            "leadership", "cunning", "honor"
    };

    public AiPersonality calculateStaticPersonality(Player player) {
        return AiPersonality.fromPlayer(player);
    }

    public DynamicAiPersonality createDynamicPersonality(Player player) {
        return new DynamicAiPersonality(player);
    }

    public Map<String, Double> blend(PersonalityProfile base, PersonalityProfile observed, double observedWeight) {
        Map<String, Double> blended = new HashMap<>();
        for (String axis : CORE_AXES) {
            double baseValue = base.getTrait(axis);
            double observedValue = observed.getTrait(axis);
            blended.put(axis, baseValue * (1.0 - observedWeight) + observedValue * observedWeight);
        }
        return blended;
    }

    public double calculateObservedWeight(int actionCount) {
        if (actionCount <= 10) return Math.min(0.4, 0.1 + (actionCount / 10.0) * 0.3);
        else if (actionCount <= 50) return 0.4 + ((actionCount - 10) / 40.0) * 0.3;
        else if (actionCount <= 200) return 0.7 + ((actionCount - 50) / 150.0) * 0.2;
        else return 0.9;
    }

    public Map<String, Double> toB5Personality(PersonalityProfile profile) {
        Map<String, Double> b5 = new HashMap<>();
        for (String axis : EXTENDED_AXES) {
            double value = profile.getTrait(axis);
            if (value == 0.0 && !isCoreAxis(axis)) value = 0.5;
            b5.put(axis, value);
        }
        return b5;
    }

    public boolean isCoreAxis(String axis) {
        for (String coreAxis : CORE_AXES) {
            if (coreAxis.equals(axis)) return true;
        }
        return false;
    }

    public boolean isValid(PersonalityProfile profile) {
        for (String axis : CORE_AXES) {
            double value = profile.getTrait(axis);
            if (value < 0.0 || value > 1.0) return false;
        }
        return true;
    }
}
