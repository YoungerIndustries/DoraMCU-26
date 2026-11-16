package com.yisy.modules.doramcu.corporation.model;

/**
 * Global Game System Credit constants based on a silver reserve treasury.
 * <p>
 * Conversion rates:
 * <ul>
 *   <li>1 ounce of silver = 1,000 DoraMCU credits</li>
 *   <li>1 credit = 0.005 ounce of silver (1/200 oz)</li>
 *   <li>Silver price reference: $5 USD per ounce</li>
 *   <li>Therefore: 1 credit = $0.005 USD (half a cent)</li>
 *   <li>$1 USD = 200 credits</li>
 * </ul>
 * <p>
 * Starting corporation capital:
 * <ul>
 *   <li>$500 USD = 100,000 credits</li>
 *   <li>Player receives $5 USD worth (1,000 credits) as seed capital</li>
 *   <li>Remaining $495 USD (99,000 credits) is a loan from the DoraMCU Treasury</li>
 * </ul>
 * <p>
 * Loan terms:
 * <ul>
 *   <li>Principal: $495 USD (99,000 credits)</li>
 *   <li>Monthly payment: $5 USD (1,000 credits)</li>
 *   <li>Interest rate: 5% per year, compounded annually</li>
 *   <li>Payoff period: ~99 months (8.25 years) at 5% interest</li>
 * </ul>
 */
public final class SilverCurrencyConstants {

    private SilverCurrencyConstants() {
        // utility class
    }

    /** Credits per ounce of silver */
    public static final long CREDITS_PER_OUNCE_SILVER = 1000L;

    /** USD price per ounce of silver (reference rate) */
    public static final double USD_PER_OUNCE_SILVER = 5.0;

    /** Credits per 1 USD: 1000 / 5 = 200 credits per dollar */
    public static final long CREDITS_PER_USD = CREDITS_PER_OUNCE_SILVER / (long) USD_PER_OUNCE_SILVER;

    /** Starting capital for a new corporation: $500 USD = 100,000 credits */
    public static final long STARTING_CAPITAL_USD = 500L;
    public static final long STARTING_CAPITAL_CREDITS = STARTING_CAPITAL_USD * CREDITS_PER_USD;

    /** Seed capital given to the player: $5 USD = 1,000 credits */
    public static final long SEED_CAPITAL_USD = 5L;
    public static final long SEED_CAPITAL_CREDITS = SEED_CAPITAL_USD * CREDITS_PER_USD;

    /** Loan amount from DoraMCU Treasury: $495 USD = 99,000 credits */
    public static final long LOAN_PRINCIPAL_USD = STARTING_CAPITAL_USD - SEED_CAPITAL_USD;
    public static final long LOAN_PRINCIPAL_CREDITS = LOAN_PRINCIPAL_USD * CREDITS_PER_USD;

    /** Monthly loan payment: $5 USD = 1,000 credits */
    public static final long MONTHLY_PAYMENT_USD = 5L;
    public static final long MONTHLY_PAYMENT_CREDITS = MONTHLY_PAYMENT_USD * CREDITS_PER_USD;

    /** Annual interest rate: 5% compounded annually */
    public static final double ANNUAL_INTEREST_RATE = 0.05;

    /** Months per year */
    public static final int MONTHS_PER_YEAR = 12;

    /**
     * Convert USD amount to DoraMCU credits.
     *
     * @param usd the USD amount
     * @return the equivalent in credits
     */
    public static long usdToCredits(long usd) {
        return usd * CREDITS_PER_USD;
    }

    /**
     * Convert DoraMCU credits to USD amount.
     *
     * @param credits the credit amount
     * @return the equivalent in USD
     */
    public static long creditsToUsd(long credits) {
        return credits / CREDITS_PER_USD;
    }

    /**
     * Format a credit amount as a readable string with USD equivalent.
     *
     * @param credits the credit amount
     * @return formatted string like "100,000 credits ($500.00 USD)"
     */
    public static String formatCredits(long credits) {
        long usd = creditsToUsd(credits);
        double usdFraction = (credits % CREDITS_PER_USD) / (double) CREDITS_PER_USD;
        return String.format("%,d credits ($%,d.%02d USD)", credits, usd, (int) Math.round(usdFraction * 100));
    }
}
