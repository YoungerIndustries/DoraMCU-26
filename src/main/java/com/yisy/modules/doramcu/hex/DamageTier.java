package com.yisy.modules.doramcu.hex;

/**
 * Represents damage severity tiers for cell-level damage overlays.
 * <p>
 * Each tier corresponds to a range of hitpoint ratios (current HP / max HP):
 * </p>
 * <ul>
 *   <li><b>PRISTINE</b> — 100-90% HP: no visible damage</li>
 *   <li><b>LIGHT</b> — 89-70% HP: minor scratches and scuffs</li>
 *   <li><b>MODERATE</b> — 69-50% HP: visible dents and scorch marks</li>
 *   <li><b>HEAVY</b> — 49-30% HP: structural breaches and exposed wiring</li>
 *   <li><b>CRITICAL</b> — 29-0% HP: catastrophic damage, destruction imminent</li>
 * </ul>
 */
public enum DamageTier {
    /**
     * 100-90% HP: no visible damage.
     */
    PRISTINE,

    /**
     * 89-70% HP: minor scratches and scuffs.
     */
    LIGHT,

    /**
     * 69-50% HP: visible dents and scorch marks.
     */
    MODERATE,

    /**
     * 49-30% HP: structural breaches and exposed wiring.
     */
    HEAVY,

    /**
     * 29-0% HP: catastrophic damage, destruction imminent.
     */
    CRITICAL
}
