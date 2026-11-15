package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.GroundVehicle;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.Optional;

/**
 * SPI interface for registering hex domain entities as AI characters.
 * <p>
 * This interface allows hex domain objects to register ships, stations, vehicles,
 * facilities, and army units as AI characters without creating a circular dependency
 * on the service layer.
 * </p>
 */
public interface AiEntityRegistration {

	/** Registers a ship's computer as an AI character. */
	AiCharacter registerShip(Ship ship, String empireName);

	/** Registers a space station's computer as an AI character. */
	AiCharacter registerStation(SpaceStation station, String empireName);

	/** Registers a ground vehicle's computer as an AI character. */
	AiCharacter registerVehicle(GroundVehicle vehicle, String empireName);

	/** Registers a facility's computer as an AI character. */
	AiCharacter registerFacility(PlanetFacility facility, String empireName);

	/** Registers an army unit's computer as an AI character. */
	AiCharacter registerArmyUnit(ArmyUnit unit, String empireName);

	/** Finds the AI character for a ship by ship ID. */
	Optional<AiCharacter> findShipAiCharacter(int shipId);

	/** Finds the AI character for a vehicle by vehicle ID string. */
	Optional<AiCharacter> findVehicleAiCharacter(String vehicleId);

	/** Deactivates the AI character for a ship when the ship is destroyed. */
	boolean deactivateShipAiCharacter(int shipId);

	/** Deactivates the AI character for a station when the station is destroyed. */
	boolean deactivateStationAiCharacter(int stationId);

	/** Deactivates the AI character for an army unit when the unit is destroyed. */
	boolean deactivateArmyUnitAiCharacter(int unitId);

	/** Deactivates the AI character for a facility when the facility is disabled. */
	boolean deactivateFacilityAiCharacter(int facilityId);

	/** Deactivates the AI character for a vehicle when the vehicle is destroyed. */
	boolean deactivateVehicleAiCharacter(String vehicleId);
}
