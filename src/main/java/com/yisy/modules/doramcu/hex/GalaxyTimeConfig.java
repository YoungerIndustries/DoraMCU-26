package com.yisy.modules.doramcu.hex;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Configuration for a galaxy's time settings relative to the Earth 2026 master clock.
 * <p>
 * Each galaxy has:
 * <ul>
 *   <li>A time compression ratio that determines how fast time passes relative to Earth 2026</li>
 *   <li>A timezone for displaying local time</li>
 *   <li>An optional offset from the master clock epoch (for galaxies that start at different times)</li>
 * </ul>
 * </p>
 * <p>
 * Default configurations are provided for the predefined galaxies:
 * <ul>
 *   <li>Earth 2026: 1:1 ratio, UTC timezone</li>
 *   <li>Milky Way: 10:1 ratio, UTC timezone</li>
 *   <li>The Void: 10:1 ratio, UTC timezone</li>
 *   <li>Test Galaxy: 100:1 ratio, UTC timezone (faster for testing)</li>
 * </ul>
 * </p>
 */
public class GalaxyTimeConfig {

    /**
     * Default time compression ratio for Earth 2026 (1:1).
     */
    public static final double EARTH_RATIO = 1.0;

    /**
     * Default time compression ratio for space galaxies (10:1).
     */
    public static final double SPACE_RATIO = 10.0;

    /**
     * Time compression ratio for test galaxy (100:1 for faster testing).
     */
    public static final double TEST_RATIO = 100.0;

    /**
     * The time compression ratio for this galaxy.
     */
    private final double compressionRatio;

    /**
     * The timezone for this galaxy's local time display.
     */
    private final ZoneId timezone;

    /**
     * Offset in seconds from the master clock epoch.
     * Positive values mean the galaxy starts after Earth's epoch.
     * Negative values mean the galaxy starts before Earth's epoch.
     */
    private final long epochOffsetSeconds;

    /**
     * Creates a new galaxy time configuration.
     *
     * @param compressionRatio the time compression ratio (0.1 to 1000.0)
     * @param timezone the timezone for local time display
     * @param epochOffsetSeconds offset from master clock epoch in seconds
     */
    public GalaxyTimeConfig(double compressionRatio, ZoneId timezone, long epochOffsetSeconds) {
        MasterClock.validateCompressionRatio(compressionRatio);
        this.compressionRatio = compressionRatio;
        this.timezone = timezone != null ? timezone : ZoneOffset.UTC;
        this.epochOffsetSeconds = epochOffsetSeconds;
    }

    /**
     * Creates a new galaxy time configuration with UTC timezone and zero epoch offset.
     *
     * @param compressionRatio the time compression ratio
     */
    public GalaxyTimeConfig(double compressionRatio) {
        this(compressionRatio, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns the time compression ratio for this galaxy.
     *
     * @return the compression ratio
     */
    public double getCompressionRatio() {
        return compressionRatio;
    }

    /**
     * Returns the timezone for this galaxy's local time display.
     *
     * @return the timezone
     */
    public ZoneId getTimezone() {
        return timezone;
    }

    /**
     * Returns the offset from the master clock epoch in seconds.
     *
     * @return the epoch offset in seconds
     */
    public long getEpochOffsetSeconds() {
        return epochOffsetSeconds;
    }

    /**
     * Returns the default time config for Earth 2026.
     *
     * @return the Earth 2026 time config
     */
    public static GalaxyTimeConfig earth2026() {
        return new GalaxyTimeConfig(EARTH_RATIO, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns the default time config for the Milky Way galaxy.
     *
     * @return the Milky Way time config
     */
    public static GalaxyTimeConfig milkyWay() {
        return new GalaxyTimeConfig(SPACE_RATIO, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns the default time config for The Void galaxy.
     *
     * @return the Void time config
     */
    public static GalaxyTimeConfig voidGalaxy() {
        return new GalaxyTimeConfig(SPACE_RATIO, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns the default time config for the Test Galaxy.
     *
     * @return the Test Galaxy time config
     */
    public static GalaxyTimeConfig testGalaxy() {
        return new GalaxyTimeConfig(TEST_RATIO, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns the default time config for a custom galaxy.
     *
     * @return the default custom galaxy time config
     */
    public static GalaxyTimeConfig defaultCustom() {
        return new GalaxyTimeConfig(SPACE_RATIO, ZoneOffset.UTC, 0L);
    }

    /**
     * Returns a builder for creating custom galaxy time configurations.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("GalaxyTimeConfig{ratio=%.1f, zone=%s, epochOffset=%ds}",
                compressionRatio, timezone, epochOffsetSeconds);
    }

    /**
     * Builder for creating GalaxyTimeConfig instances.
     */
    public static class Builder {
        private double compressionRatio = SPACE_RATIO;
        private ZoneId timezone = ZoneOffset.UTC;
        private long epochOffsetSeconds = 0L;

        /**
         * Sets the time compression ratio.
         *
         * @param ratio the compression ratio (0.1 to 1000.0)
         * @return this builder
         */
        public Builder compressionRatio(double ratio) {
            this.compressionRatio = ratio;
            return this;
        }

        /**
         * Sets the timezone for local time display.
         *
         * @param zone the timezone
         * @return this builder
         */
        public Builder timezone(ZoneId zone) {
            this.timezone = zone;
            return this;
        }

        /**
         * Sets the offset from the master clock epoch in seconds.
         *
         * @param seconds the offset in seconds
         * @return this builder
         */
        public Builder epochOffsetSeconds(long seconds) {
            this.epochOffsetSeconds = seconds;
            return this;
        }

        /**
         * Builds the GalaxyTimeConfig.
         *
         * @return the configured instance
         */
        public GalaxyTimeConfig build() {
            return new GalaxyTimeConfig(compressionRatio, timezone, epochOffsetSeconds);
        }
    }
}
