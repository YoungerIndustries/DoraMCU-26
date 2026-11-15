package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.List;

/**
 * SPI for station AI support during battles.
 * <p>
 * Hex domain classes depend on this interface instead of the concrete
 * {@code StationAiSupportService} in the ai package, breaking the circular dependency
 * between hex and ai packages.
 * </p>
 * <p>
 * The ai package implements this interface in {@code StationAiSupportService}.
 * </p>
 */
public interface StationAiSupportSpi {

    /**
     * Returns the list of stations providing support to the given fleet.
     */
    List<SpaceStation> getSupportingStations(Fleet fleet, List<SpaceStation> stations);

    /**
     * Returns the total defense bonus provided to the fleet by all supporting stations.
     */
    double getTotalDefenseBonus(Fleet fleet, List<SpaceStation> stations);

    /**
     * Returns the total tactical support bonus for the fleet.
     */
    double getTotalTacticalBonus(Fleet fleet, List<SpaceStation> stations);
}
