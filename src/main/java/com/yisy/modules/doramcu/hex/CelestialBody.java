package com.yisy.modules.doramcu.hex;

/**
 * Abstract base class for all celestial bodies in the game.
 * <p>
 * Provides common fields and methods shared by {@link Star}, {@link Planet},
 * {@link GasGiant}, {@link Asteroid}, {@link Comet}, {@link Moon},
 * {@link AsteroidBelt}, {@link BlackHole}, and {@link Nebula}.
 * <p>
 * All celestial bodies have:
 * <ul>
 *   <li>A unique identifier</li>
 *   <li>A radius defining their 3D hex sphere</li>
 *   <li>A {@link Hex3DMap} for internal structure</li>
 *   <li>A display name</li>
 *   <li>Optional strategic resource deposits</li>
 * </ul>
 * <p>
 * Subclasses add type-specific properties (e.g., energy yield for stars,
 * mineral yield for asteroids, tidal strength for moons).
 */
public abstract class CelestialBody {

    protected final int id;
    protected final int radius;
    protected final Hex3DMap map;
    protected String name;
    protected boolean hasStrategicDeposit;
    protected StrategicResource strategicResource;
    protected boolean hexGeometryGenerated = false;  // Lazy generation flag

    /** Resource depletion level (0-100%). 100% = fully stocked, 0% = depleted. */
    protected double resourceLevel = 100.0;

    /** Mining yield multiplier based on body quality (0.5-2.0). */
    protected double miningYield = 1.0;

    /** Number of turns since last mining (for recovery calculation). */
    protected int turnsSinceLastMining = 0;

    /** Recovery rate per turn when not being mined (0.1% per turn by default). */
    public static final double RECOVERY_RATE_PER_TURN = 0.1;

    /**
     * Creates a new celestial body with the given ID and radius.
     *
     * @param id the unique identifier
     * @param radius the radius of the 3D hex sphere
     * @throws IllegalArgumentException if radius is negative
     */
    protected CelestialBody(int id, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be non-negative: " + radius);
        }
        this.id = id;
        this.radius = radius;
        this.map = new Hex3DMap();
        this.name = defaultName(id);
        this.hasStrategicDeposit = false;
        this.strategicResource = null;
    }

    /**
     * Returns a default name for this celestial body type.
     * Subclasses override to provide type-specific naming.
     *
     * @param id the unique identifier
     * @return a default name string
     */
    protected abstract String defaultName(int id);

    /**
     * Generates the 3D hex sphere for this celestial body.
     * Called lazily — hex geometry is only generated when actually needed.
     * For strategic views (galaxy map, sector cards), this is skipped entirely.
     */
    protected void generate() {
        if (hexGeometryGenerated) return;
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, radius);
        hexGeometryGenerated = true;
    }

    /**
     * Forces generation of hex geometry if not already generated.
     * Use this when tactical operations require the full hex map.
     */
    public void ensureGeometryGenerated() {
        if (!hexGeometryGenerated) {
            generate();
        }
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this celestial body
     */
    public int getId() {
        return id;
    }

    /**
     * @return the display name of this celestial body
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this celestial body.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the radius of the 3D hex sphere
     */
    public int getRadius() {
        return radius;
    }

    /**
     * @return the total number of hex cells in this celestial body
     */
    public int size() {
        ensureGeometryGenerated();
        return map.size();
    }

    /**
     * @return true if hex geometry has been generated for this body
     */
    public boolean isHexGeometryGenerated() {
        return hexGeometryGenerated;
    }

    /**
     * @return the internal 3D hex map (triggers lazy generation if needed)
     */
    public Hex3DMap getMap() {
        ensureGeometryGenerated();
        return map;
    }

    // ---- Strategic Resources ----

    /**
     * @return true if this celestial body has a strategic resource deposit
     */
    public boolean hasStrategicDeposit() {
        return hasStrategicDeposit;
    }

    /**
     * Sets whether this celestial body has a strategic resource deposit.
     *
     * @param has true if it has a deposit
     */
    public void setHasStrategicDeposit(boolean has) {
        this.hasStrategicDeposit = has;
    }

    /**
     * @return the strategic resource type, or null if none
     */
    public StrategicResource getStrategicResource() {
        return strategicResource;
    }

    /**
     * Sets the strategic resource type for this celestial body.
     *
     * @param resource the strategic resource type
     */
    public void setStrategicResource(StrategicResource resource) {
        this.strategicResource = resource;
        this.hasStrategicDeposit = resource != null;
    }

    // ---- Resource Depletion ----

    /**
     * @return the current resource level (0-100%)
     */
    public double getResourceLevel() {
        return resourceLevel;
    }

    /**
     * Sets the resource level.
     *
     * @param level the resource level (0-100%)
     */
    public void setResourceLevel(double level) {
        this.resourceLevel = Math.max(0.0, Math.min(100.0, level));
    }

    /**
     * @return the mining yield multiplier (0.5-2.0)
     */
    public double getMiningYield() {
        return miningYield;
    }

    /**
     * Sets the mining yield multiplier.
     *
     * @param yield the yield multiplier (0.5-2.0)
     */
    public void setMiningYield(double yield) {
        this.miningYield = Math.max(0.5, Math.min(2.0, yield));
    }

    /**
     * @return the number of turns since last mining
     */
    public int getTurnsSinceLastMining() {
        return turnsSinceLastMining;
    }

    /**
     * Records that mining occurred on this body.
     * Depletes the resource level based on the amount mined.
     *
     * @param amountMined the amount of resources mined this turn
     * @param maxCapacity the maximum storage capacity of this body
     * @return the effective yield multiplier considering depletion
     */
    public double recordMining(int amountMined, int maxCapacity) {
        turnsSinceLastMining = 0;
        if (maxCapacity <= 0) return 1.0;

        // Deplete based on mining intensity
        double depletionRate = (double) amountMined / maxCapacity * 0.5;
        resourceLevel = Math.max(0.0, resourceLevel - depletionRate);

        // Effective yield: base yield * (resourceLevel / 100)
        return miningYield * (resourceLevel / 100.0);
    }

    /**
     * Processes one turn of resource recovery.
     * If not mined, the resource level slowly recovers.
     */
    public void processRecoveryTurn() {
        turnsSinceLastMining++;
        if (turnsSinceLastMining > 0 && resourceLevel < 100.0) {
            resourceLevel = Math.min(100.0, resourceLevel + RECOVERY_RATE_PER_TURN);
        }
    }

    /**
     * @return true if the body is considered depleted (resource level < 10%)
     */
    public boolean isDepleted() {
        return resourceLevel < 10.0;
    }

    /**
     * @return a string representation of this celestial body
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", radius=" + radius +
                ", size=" + size() +
                ", strategic=" + (hasStrategicDeposit ? strategicResource : "none") +
                ", resourceLevel=" + String.format("%.1f%%", resourceLevel) +
                ", miningYield=" + miningYield +
                '}';
    }
}
