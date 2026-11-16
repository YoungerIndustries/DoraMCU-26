package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.babylon5.model.B5Personality;

import java.util.Map;

/**
 * Makes B5Personality implement the unified PersonalityProfile interface.
 */
public class B5PersonalityProfileAdapter implements PersonalityProfile {

    private final B5Personality delegate;

    public B5PersonalityProfileAdapter(B5Personality delegate) {
        this.delegate = delegate;
    }

    @Override
    public double getTrait(String axis) {
        return switch (axis) {
            case "aggression" -> delegate.getAggression();
            case "expansionDrive" -> delegate.getExpansionDrive();
            case "economicFocus" -> delegate.getEconomicFocus();
            case "scientificFocus" -> delegate.getScientificFocus();
            case "diplomaticStance", "diplomacy" -> delegate.getDiplomacy();
            case "riskTolerance" -> delegate.getRiskTolerance();
            case "altruism" -> delegate.getAltruism();
            case "determination" -> delegate.getDetermination();
            case "adaptability" -> delegate.getAdaptability();
            case "leadership" -> delegate.getLeadership();
            case "cunning" -> delegate.getCunning();
            case "honor" -> delegate.getHonor();
            default -> 0.5;
        };
    }

    @Override
    public void setTrait(String axis, double value) {
        switch (axis) {
            case "aggression": delegate.setAggression(value); break;
            case "expansionDrive": delegate.setExpansionDrive(value); break;
            case "economicFocus": delegate.setEconomicFocus(value); break;
            case "scientificFocus": delegate.setScientificFocus(value); break;
            case "diplomaticStance", "diplomacy": delegate.setDiplomacy(value); break;
            case "riskTolerance": delegate.setRiskTolerance(value); break;
            case "altruism": delegate.setAltruism(value); break;
            case "determination": delegate.setDetermination(value); break;
            case "adaptability": delegate.setAdaptability(value); break;
            case "leadership": delegate.setLeadership(value); break;
            case "cunning": delegate.setCunning(value); break;
            case "honor": delegate.setHonor(value); break;
            default: break;
        }
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
    public double getDiplomaticStance() { return delegate.getDiplomacy(); }
    @Override
    public double getRiskTolerance() { return delegate.getRiskTolerance(); }
    @Override
    public Map<String, Double> toMap() { return delegate.toMap(); }
    @Override
    public PersonalityType getType() { return PersonalityType.EXTENDED; }
    @Override
    public String getLabel() { return "B5_" + delegate.getAggression() + "_" + delegate.getDiplomacy(); }

    public B5Personality getDelegate() { return delegate; }
}
