package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import com.yisy.modules.doramcu.model.galaxy.PlanetVehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing planet vehicle persistence.
 */
@Repository
public interface PlanetVehicleRepository extends JpaRepository<PlanetVehicleEntity, Long> {

    /** Find vehicle by its business ID. */
    Optional<PlanetVehicleEntity> findByVehicleId(String vehicleId);

    /** Find all vehicles on a planet. */
    List<PlanetVehicleEntity> findByPlanetId(int planetId);

    /** Find vehicles on a planet owned by an empire. */
    List<PlanetVehicleEntity> findByPlanetIdAndOwnerEmpireId(int planetId, String ownerEmpireId);

    /** Find vehicles by type on a planet. */
    List<PlanetVehicleEntity> findByPlanetIdAndVehicleType(int planetId, String vehicleType);

    /** Delete a vehicle by its business ID. */
    void deleteByVehicleId(String vehicleId);

    /** Delete all vehicles for a planet. */
    void deleteByPlanetId(int planetId);

    /** Count vehicles on a planet. */
    long countByPlanetId(int planetId);
}
