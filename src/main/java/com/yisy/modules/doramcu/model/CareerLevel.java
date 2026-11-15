package com.yisy.modules.doramcu.model;

/**
 * Career progression levels for AI hires.
 * <p>
 * Hires start at JUNIOR and can be promoted through SENIOR, LEAD, to DIRECTOR.
 * Each level has an XP threshold and a salary multiplier that increases by 20% per level.
 * </p>
 */
public enum CareerLevel {

    /** Starting level, 0 XP required, 100% base salary */
    JUNIOR(0, 1.0),

    /** 100 XP required, 120% base salary */
    SENIOR(100, 1.2),

    /** 500 XP required, 144% base salary */
    LEAD(500, 1.44),

    /** 2000 XP required, 172.8% base salary (maximum level) */
    DIRECTOR(2000, 1.728);

    private final int xpThreshold;
    private final double salaryMultiplier;

    CareerLevel(int xpThreshold, double salaryMultiplier) {
        this.xpThreshold = xpThreshold;
        this.salaryMultiplier = salaryMultiplier;
    }

    /**
     * Returns the XP threshold required to reach this level.
     */
    public int getXpThreshold() {
        return xpThreshold;
    }

    /**
     * Returns the salary multiplier for this level.
     * Base salary is multiplied by this value to get the effective salary.
     */
    public double getSalaryMultiplier() {
        return salaryMultiplier;
    }

    /**
     * Returns the index of this level (0=JUNIOR, 1=SENIOR, 2=LEAD, 3=DIRECTOR).
     */
    public int getIndex() {
        return this.ordinal();
    }

    /**
     * Checks if this is the maximum career level.
     */
    public boolean isMaxLevel() {
        return this == DIRECTOR;
    }

    /**
     * Returns the next higher career level, or null if already at max.
     */
    public CareerLevel nextLevel() {
        if (isMaxLevel()) {
            return null;
        }
        return CareerLevel.values()[this.ordinal() + 1];
    }

    /**
     * Returns the next lower career level, or null if already at minimum.
     */
    public CareerLevel previousLevel() {
        if (this == JUNIOR) {
            return null;
        }
        return CareerLevel.values()[this.ordinal() - 1];
    }

    /**
     * Returns the XP threshold for the next level, or Integer.MAX_VALUE if at max.
     */
    public int getNextLevelThreshold() {
        CareerLevel next = nextLevel();
        return next != null ? next.getXpThreshold() : Integer.MAX_VALUE;
    }
}
