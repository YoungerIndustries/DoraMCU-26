package com.yisy.modules.doramcu.hex;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Master clock service that uses Earth 2026 as the reference time for all galaxies.
 * <p>
 * This service provides a unified time system where:
 * <ul>
 *   <li>Earth 2026 is the master clock with 1:1 real-world time ratio</li>
 *   <li>Each galaxy has a time compression ratio that determines how fast time passes relative to Earth 2026</li>
 *   <li>All galaxies can query their local time based on the master clock and their compression ratio</li>
 * </ul>
 * </p>
 * <p>
 * Example: If Earth 2026 is at 2026-01-01 12:00:00 UTC and a galaxy has a compression ratio of 10.0,
 * then 10 seconds pass in that galaxy for every 1 second on Earth. If the galaxy started at the same
 * time as Earth, its local time would be 10 seconds ahead of Earth's time.
 * </p>
 * <p>
 * The master clock starts at a fixed epoch (2026-01-01 00:00:00 UTC) to provide a consistent reference point.
 * </p>
 */
public class MasterClock {

    /**
     * The fixed epoch for Earth 2026: 2026-01-01 00:00:00 UTC.
     * All time calculations are relative to this epoch.
     */
    public static final Instant EARTH_2026_EPOCH = Instant.parse("2026-01-01T00:00:00Z");

    /**
     * Default time compression ratio for Earth 2026 (1:1 with real-world time).
     */
    public static final double EARTH_TIME_RATIO = 1.0;

    /**
     * Default time compression ratio for space galaxies (10:1 with Earth time).
     * This means 10 seconds pass in space for every 1 second on Earth.
     */
    public static final double DEFAULT_SPACE_GALAXY_RATIO = 10.0;

    /**
     * Minimum allowed time compression ratio (0.1:1 — very slow time).
     */
    public static final double MIN_TIME_RATIO = 0.1;

    /**
     * Maximum allowed time compression ratio (1000:1 — very fast time).
     */
    public static final double MAX_TIME_RATIO = 1000.0;

    /**
     * The master clock's current time in nanoseconds since the Earth 2026 epoch.
     * This is updated periodically to reflect real-world time passage.
     */
    private final AtomicLong masterTimeNanos = new AtomicLong(0);

    /**
     * The system time (in nanos) when the master clock was last updated.
     * Used to calculate elapsed time since last update.
     */
    private volatile long lastUpdateSystemNanos = 0;

    /**
     * The ZoneId used for Earth 2026 timestamps (UTC by default).
     */
    private final ZoneId earthZone;

    /**
     * Creates a new master clock with UTC timezone.
     */
    public MasterClock() {
        this(ZoneOffset.UTC);
    }

    /**
     * Creates a new master clock with the specified timezone.
     *
     * @param zone the timezone for Earth 2026 timestamps
     */
    public MasterClock(ZoneId zone) {
        this.earthZone = zone;
        this.lastUpdateSystemNanos = System.nanoTime();
        this.masterTimeNanos.set(0);
    }

    /**
     * Updates the master clock to reflect the current real-world time.
     * <p>
     * This should be called periodically (e.g., at the start of each turn or game tick)
     * to ensure the master clock stays synchronized with real-world time.
     * </p>
     * @return this master clock for chaining
     */
    public MasterClock update() {
        long currentSystemNanos = System.nanoTime();
        long elapsedNanos = currentSystemNanos - lastUpdateSystemNanos;
        
        if (elapsedNanos > 0) {
            masterTimeNanos.addAndGet(elapsedNanos);
            lastUpdateSystemNanos = currentSystemNanos;
        }
        
        return this;
    }

    /**
     * Returns the current master time as an Instant (Earth 2026 time).
     * <p>
     * This represents the current time on Earth 2026, which serves as the reference
     * for all other galaxies.
     * </p>
     * @return the current Earth 2026 time as an Instant
     */
    public Instant getEarthTime() {
        update();
        return EARTH_2026_EPOCH.plusNanos(masterTimeNanos.get());
    }

    /**
     * Returns the current master time as a ZonedDateTime in the Earth 2026 timezone.
     *
     * @return the current Earth 2026 time as a ZonedDateTime
     */
    public ZonedDateTime getEarthZonedDateTime() {
        return ZonedDateTime.ofInstant(getEarthTime(), earthZone);
    }

    /**
     * Returns the current master time as a LocalDateTime in the Earth 2026 timezone.
     *
     * @return the current Earth 2026 time as a LocalDateTime
     */
    public LocalDateTime getEarthLocalDateTime() {
        return getEarthZonedDateTime().toLocalDateTime();
    }

    /**
     * Returns the local time for a galaxy based on its time compression ratio.
     * <p>
     * The local time is calculated as:
     * <pre>
     * localTime = epoch + (masterTime * compressionRatio)
     * </pre>
     * </p>
     *
     * @param galaxyName the name of the galaxy (for identification)
     * @param compressionRatio the time compression ratio for this galaxy
     * @return the local time for the galaxy as an Instant
     */
    public Instant getGalaxyTime(String galaxyName, double compressionRatio) {
        validateCompressionRatio(compressionRatio);
        update();
        
        long masterNanos = masterTimeNanos.get();
        long galaxyNanos = (long) (masterNanos * compressionRatio);
        
        return EARTH_2026_EPOCH.plusNanos(galaxyNanos);
    }

    /**
     * Returns the local time for a galaxy as a ZonedDateTime.
     *
     * @param galaxyName the name of the galaxy
     * @param compressionRatio the time compression ratio
     * @param zone the timezone for the galaxy's local time
     * @return the local time for the galaxy as a ZonedDateTime
     */
    public ZonedDateTime getGalaxyZonedDateTime(String galaxyName, double compressionRatio, ZoneId zone) {
        return ZonedDateTime.ofInstant(getGalaxyTime(galaxyName, compressionRatio), zone);
    }

    /**
     * Returns the local time for a galaxy as a LocalDateTime in UTC.
     *
     * @param galaxyName the name of the galaxy
     * @param compressionRatio the time compression ratio
     * @return the local time for the galaxy as a LocalDateTime
     */
    public LocalDateTime getGalaxyLocalDateTime(String galaxyName, double compressionRatio) {
        return getGalaxyZonedDateTime(galaxyName, compressionRatio, ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Returns the time compression ratio for Earth 2026 (always 1.0).
     *
     * @return 1.0
     */
    public double getEarthTimeRatio() {
        return EARTH_TIME_RATIO;
    }

    /**
     * Returns the default time compression ratio for space galaxies.
     *
     * @return the default space galaxy ratio
     */
    public double getDefaultSpaceGalaxyRatio() {
        return DEFAULT_SPACE_GALAXY_RATIO;
    }

    /**
     * Returns the master time in nanoseconds since the Earth 2026 epoch.
     *
     * @return the master time in nanoseconds
     */
    public long getMasterTimeNanos() {
        update();
        return masterTimeNanos.get();
    }

    /**
     * Returns the master time in seconds since the Earth 2026 epoch.
     *
     * @return the master time in seconds
     */
    public long getMasterTimeSeconds() {
        return getMasterTimeNanos() / 1_000_000_000L;
    }

    /**
     * Returns the Earth 2026 epoch instant.
     *
     * @return the epoch instant
     */
    public Instant getEpoch() {
        return EARTH_2026_EPOCH;
    }

    /**
     * Returns the Earth 2026 timezone.
     *
     * @return the timezone
     */
    public ZoneId getEarthZone() {
        return earthZone;
    }

    /**
     * Sets the master clock to a specific time (for testing or game reset).
     *
     * @param instant the Earth 2026 time to set
     */
    public void setEarthTime(Instant instant) {
        long nanosSinceEpoch = instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
        masterTimeNanos.set(nanosSinceEpoch);
        lastUpdateSystemNanos = System.nanoTime();
    }

    /**
     * Advances the master clock by the specified duration.
     * <p>
     * This is useful for fast-forwarding time in tests or for time-based events.
     * </p>
     *
     * @param seconds the number of seconds to advance
     */
    public void advanceTime(long seconds) {
        masterTimeNanos.addAndGet(seconds * 1_000_000_000L);
        lastUpdateSystemNanos = System.nanoTime();
    }

    /**
     * Resets the master clock to the epoch (2026-01-01 00:00:00 UTC).
     */
    public void reset() {
        masterTimeNanos.set(0);
        lastUpdateSystemNanos = System.nanoTime();
    }

    /**
     * Validates that a compression ratio is within the allowed range.
     *
     * @param ratio the compression ratio to validate
     * @throws IllegalArgumentException if the ratio is out of range
     */
    public static void validateCompressionRatio(double ratio) {
        if (ratio < MIN_TIME_RATIO || ratio > MAX_TIME_RATIO) {
            throw new IllegalArgumentException(
                    "Time compression ratio must be between " + MIN_TIME_RATIO + " and " + MAX_TIME_RATIO + ": " + ratio);
        }
    }

    @Override
    public String toString() {
        return String.format("MasterClock{earthTime=%s, masterNanos=%d}", 
                getEarthTime(), masterTimeNanos.get());
    }
}
