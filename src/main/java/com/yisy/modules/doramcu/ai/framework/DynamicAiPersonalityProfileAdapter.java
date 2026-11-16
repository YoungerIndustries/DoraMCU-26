package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.DynamicAiPersonality;

import java.util.Map;

/**
 * Makes DynamicAiPersonality implement the unified PersonalityProfile interface.
 */
public class DynamicAiPersonalityProfileAdapter implements PersonalityProfile {

    private final DynamicAiPersonality delegate;

    public DynamicAiPersonalityProfileAdapter(DynamicAiPersonality delegate) {
        this.delegate = delegate;
    }

    @Override
    public double getTrait(String axis) {
        return switch (axis) {
            case "aggression" -> delegate.getAggression();
            case "expansionDrive" -> delegate.getExpansionDrive();
            case "economicFocus" -> delegate.getEconomicFocus();
            case "scientificFocus" -> delegate.getScientificFocus();
            case "diplomaticStance" -> delegate.getDiplomaticStance();
            case "riskTolerance" -> delegate.getRiskTolerance();
            default -> 0.5;
        };
    }

    @Override
    public void setTrait(String axis, double value) {
        delegate.updateObservedTrait(axis, value);
    }

    @Override
    public double getAggression() { return delegate.getAggression(); }
    @Override
    public double getExpansionDrive() { return delegate.getExpansionDrive(); }
    @Override
    public double getEconomicFocus() { return delegate.getEconomicFocus(); }
    @Override
    public double getScientificFocus() { return delegate.getScientificFocus(); }
    @Override
    public double getDiplomaticStance() { return delegate.getDiplomaticStance(); }
    @Override
    public double getRiskTolerance() { return delegate.getRiskTolerance(); }
    @Override
    public Map<String, Double> toMap() { return delegate.toMap(); }
    @Override
    public double getConfidenceScore() { return delegate.getConfidenceScore(); }
    @Override
    public int getObservedActionCount() { return delegate.getObservedActionCount(); }
    @Override
    public PersonalityType getType() { return PersonalityType.DYNAMIC; }
    @Override
    public String getLabel() { return delegate.getLabel(); }

    public DynamicAiPersonality getDelegate() { return delegate; }
}
