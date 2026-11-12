package com.yisy.modules.doramcu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for generating random promo codes.
 * <p>
 * Generates unique, human-readable promo codes with configurable length and prefix.
 * Uses ThreadLocalRandom for thread-safe random number generation.
 * </p>
 */
public final class PromoCodeGenerator {

    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    // Excludes I, O, 1, 0 to avoid ambiguous characters

    private PromoCodeGenerator() {
        // Utility class — prevent instantiation
    }

    /**
     * Generates a single random promo code.
     *
     * @param length the length of the generated code (default 8)
     * @return a random promo code string
     */
    public static String generate(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Promo code length must be at least 4, got: " + length);
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_SET.charAt(ThreadLocalRandom.current().nextInt(CHAR_SET.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a single random promo code with the default length of 8.
     *
     * @return a random promo code string
     */
    public static String generate() {
        return generate(8);
    }

    /**
     * Generates a random promo code with a prefix.
     *
     * @param prefix the prefix to prepend (e.g., "WELCOME-", "BONUS-")
     * @param length the length of the random portion
     * @return a promo code with the format "PREFIX-XXXX"
     */
    public static String generateWithPrefix(String prefix, int length) {
        if (prefix == null || prefix.isEmpty()) {
            return generate(length);
        }
        return prefix.toUpperCase() + "-" + generate(length);
    }

    /**
     * Generates a batch of unique promo codes.
     *
     * @param count the number of codes to generate
     * @param length the length of each code
     * @return a list of unique promo codes
     */
    public static List<String> generateBatch(int count, int length) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1, got: " + count);
        }
        java.util.Set<String> generated = new java.util.HashSet<>();
        List<String> result = new ArrayList<>(count);
        int attempts = 0;
        int maxAttempts = count * 10; // Prevent infinite loops

        while (result.size() < count && attempts < maxAttempts) {
            String code = generate(length);
            if (generated.add(code)) {
                result.add(code);
            }
            attempts++;
        }

        if (result.size() < count) {
            throw new RuntimeException("Failed to generate " + count + " unique codes after " + attempts + " attempts");
        }

        return result;
    }

    /**
     * Generates a batch of unique promo codes with the default length of 8.
     *
     * @param count the number of codes to generate
     * @return a list of unique promo codes
     */
    public static List<String> generateBatch(int count) {
        return generateBatch(count, 8);
    }

    /**
     * Generates a batch of unique promo codes with a prefix.
     *
     * @param prefix the prefix to prepend to each code
     * @param count the number of codes to generate
     * @param length the length of the random portion
     * @return a list of unique promo codes with the format "PREFIX-XXXX"
     */
    public static List<String> generateBatchWithPrefix(String prefix, int count, int length) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1, got: " + count);
        }
        java.util.Set<String> generated = new java.util.HashSet<>();
        List<String> result = new ArrayList<>(count);
        int attempts = 0;
        int maxAttempts = count * 10;

        while (result.size() < count && attempts < maxAttempts) {
            String code = generateWithPrefix(prefix, length);
            if (generated.add(code)) {
                result.add(code);
            }
            attempts++;
        }

        if (result.size() < count) {
            throw new RuntimeException("Failed to generate " + count + " unique codes after " + attempts + " attempts");
        }

        return result;
    }
}
