package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.AiPersonality;

import java.util.Map;

/**
 * Makes AiPersonality implement the unified PersonalityProfile interface.
 */
public class AiPersonalityProfileAdapter implements PersonalityProfile {

    private final AiPersonality delegate;

    public AiPersonalityProfileAdapter(AiPersonality delegate) {
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
    public void setTrait(String axis, double value) { /* static is immutable */ }

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
    public PersonalityType getType() { return PersonalityType.STATIC; }
    @Override
    public String getLabel() { return delegate.getLabel(); }

    public AiPersonality getDelegate() { return delegate; }
}
