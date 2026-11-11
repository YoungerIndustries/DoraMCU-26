package com.yisy.modules.doramcu.hex;

import java.util.List;

/**
 * Interface for building empires in a galaxy.
 * <p>
 * Implementations provide themed empire creation logic — for example,
 * {@code Babylon5EmpireBuilder} creates empires from B5 races, while
 * {@code DefaultEmpireBuilder} creates generic empires with random stats.
 * </p>
 * <p>
 * The {@link GalaxyTemplate} enum can hold an optional {@link EmpireBuilder}
 * reference so that each template automatically populates its galaxy with
 * themed empires when generated.
 * </p>
 */
public interface EmpireBuilder {

    /**
     * Builds empires and registers them in the game.
     * <p>
     * A typical implementation creates one empire per faction/race, sets up
     * home systems with capital planets, and assigns starting fleets.
     * </p>
     *
     * @param game the game to register empires in
     * @param galaxy the galaxy containing solar systems for home planets
     * @return the list of created empires
     */
    List<Empire> buildEmpires(Game game, Galaxy galaxy);

    /**
     * Creates a single empire with the given configuration.
     * <p>
     * This method allows fine-grained control over empire creation,
     * useful when building empires outside the {@link #buildEmpires(Game, Galaxy)}
     * bulk operation.
     * </p>
     *
     * @param name the empire name
     * @param game the game context for empire registration
     * @param player the player controlling the empire
     * @param galaxy the galaxy for home system registration
     * @param config configuration for the empire (planet size, mineral quality, facilities, etc.)
     * @return the created empire
     */
    Empire createEmpire(String name, Game game, Player player, Galaxy galaxy, EmpireConfig config);

    /**
     * Configuration for creating a single empire.
     * <p>
     * Provides planet properties, starting facilities, and fleet composition
     * for a themed empire.
     * </p>
     */
    class EmpireConfig {
        private PlanetSize planetSize = PlanetSize.MEDIUM;
        private MineralQuality mineralQuality = MineralQuality.NORMAL;
        private StartingFacility[] startingFacilities = new StartingFacility[]{StartingFacility.SHIPYARD};
        private int industrialLevel = 1;
        private boolean hasMilitaryFleet = false;
        private boolean hasMiningFleet = true;
        private boolean hasReserveFleet = false;

        public EmpireConfig() {
        }

        public EmpireConfig(PlanetSize planetSize, MineralQuality mineralQuality) {
            this.planetSize = planetSize;
            this.mineralQuality = mineralQuality;
        }

        public PlanetSize getPlanetSize() {
            return planetSize;
        }

        public void setPlanetSize(PlanetSize planetSize) {
            this.planetSize = planetSize;
        }

        public MineralQuality getMineralQuality() {
            return mineralQuality;
        }

        public void setMineralQuality(MineralQuality mineralQuality) {
            this.mineralQuality = mineralQuality;
        }

        public StartingFacility[] getStartingFacilities() {
            return startingFacilities;
        }

        public void setStartingFacilities(StartingFacility[] startingFacilities) {
            this.startingFacilities = startingFacilities;
        }

        public int getIndustrialLevel() {
            return industrialLevel;
        }

        public void setIndustrialLevel(int industrialLevel) {
            this.industrialLevel = industrialLevel;
        }

        public boolean isHasMilitaryFleet() {
            return hasMilitaryFleet;
        }

        public void setHasMilitaryFleet(boolean hasMilitaryFleet) {
            this.hasMilitaryFleet = hasMilitaryFleet;
        }

        public boolean isHasMiningFleet() {
            return hasMiningFleet;
        }

        public void setHasMiningFleet(boolean hasMiningFleet) {
            this.hasMiningFleet = hasMiningFleet;
        }

        public boolean isHasReserveFleet() {
            return hasReserveFleet;
        }

        public void setHasReserveFleet(boolean hasReserveFleet) {
            this.hasReserveFleet = hasReserveFleet;
        }

        @Override
        public String toString() {
            return "EmpireConfig{planetSize=" + planetSize + ", mineralQuality=" + mineralQuality +
                    ", industrialLevel=" + industrialLevel + '}';
        }
    }
}
