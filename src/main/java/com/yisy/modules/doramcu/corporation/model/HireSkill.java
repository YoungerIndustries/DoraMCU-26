package com.yisy.modules.doramcu.corporation.model;

/**
 * Position-specific skills for AI hires.
 * <p>
 * Each corporate position has a primary skill that develops through contract work.
 * Skills range from 1-10, with higher levels improving contract success chances.
 * </p>
 */
public enum HireSkill {

    /** Combat and defense operations */
    COMBAT("Combat", "Security and defense operations"),

    /** Trade and diplomatic negotiations */
    NEGOTIATION("Negotiation", "Trade and diplomatic deals"),

    /** Supply chain and logistics operations */
    LOGISTICS("Logistics", "Supply chain and operations"),

    /** Scientific analysis and research */
    RESEARCH("Research", "Scientific analysis and development"),

    /** Political and administrative management */
    GOVERNANCE("Governance", "Political and administrative management");

    private final String displayName;
    private final String description;

    HireSkill(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the human-readable display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the skill description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Maps a corporate position to its primary skill.
     */
    public static HireSkill fromPosition(CorporatePosition position) {
        if (position == null) {
            return LOGISTICS;
        }
        switch (position) {
            case SECURITY_CHIEF:
            case SHIP_CAPTAIN:
                return COMBAT;
            case CFO:
            case TRADE_MINISTER:
                return NEGOTIATION;
            case COO:
            case ENGINEER:
                return LOGISTICS;
            case CTO:
            case SCIENCE_OFFICER:
                return RESEARCH;
            case SECTOR_GOVERNOR_DELEGATE:
                return GOVERNANCE;
            default:
                return LOGISTICS;
        }
    }

    /**
     * Maps a skill to preferred contract types.
     * Returns the contract types that benefit most from this skill.
     */
    public java.util.List<CorporateContract.ContractType> getPreferredContractTypes() {
        switch (this) {
            case COMBAT:
                return java.util.List.of(
                        CorporateContract.ContractType.SECURITY,
                        CorporateContract.ContractType.MISSION);
            case NEGOTIATION:
                return java.util.List.of(
                        CorporateContract.ContractType.TRADE,
                        CorporateContract.ContractType.LOGISTICS);
            case LOGISTICS:
                return java.util.List.of(
                        CorporateContract.ContractType.LOGISTICS,
                        CorporateContract.ContractType.TRADE);
            case RESEARCH:
                return java.util.List.of(
                        CorporateContract.ContractType.MISSION,
                        CorporateContract.ContractType.TRADE);
            case GOVERNANCE:
                return java.util.List.of(
                        CorporateContract.ContractType.MISSION,
                        CorporateContract.ContractType.SECURITY);
            default:
                return java.util.List.of();
        }
    }
}
