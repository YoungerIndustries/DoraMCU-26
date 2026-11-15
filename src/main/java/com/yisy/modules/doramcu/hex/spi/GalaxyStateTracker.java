package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.model.GalaxyObjectState;

import java.util.Map;

/**
 * SPI interface for tracking galaxy object state during galaxy building.
 * <p>
 * This interface allows hex galaxy building code to register objects
 * without creating a circular dependency on the service layer.
 * </p>
 */
public interface GalaxyStateTracker {

	/**
	 * Registers a new galaxy object in the state tracker.
	 *
	 * @param galaxyName the galaxy the object belongs to
	 * @param objectType the type of object (PLANET, CITY_3D, etc.)
	 * @param objectId unique identifier for the object
	 * @param objectName human-readable name
	 * @param sectorRow sector row position (0-9)
	 * @param sectorCol sector column position (0-9)
	 * @param metadata additional object properties as JSON
	 * @return the saved state record
	 */
	GalaxyObjectState registerObject(String galaxyName, String objectType,
									  String objectId, String objectName,
									  Integer sectorRow, Integer sectorCol,
									  Map<String, Object> metadata);
}
