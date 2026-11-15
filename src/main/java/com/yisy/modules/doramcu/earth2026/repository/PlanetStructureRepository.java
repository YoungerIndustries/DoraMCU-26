package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for PlanetStructure entities.
 */
@Repository
public interface PlanetStructureRepository extends JpaRepository<PlanetStructure, Long> {

    /**
     * Find structures by planet ID.
     */
    List<PlanetStructure> findByPlanetId(Long planetId);

    /**
     * Find structures by planet ID and active status.
     */
    List<PlanetStructure> findByPlanetIdAndActive(Long planetId, boolean active);

    /**
     * Find structures by type.
     */
    List<PlanetStructure> findByStructureType(PlanetStructure.StructureType structureType);

    /**
     * Find structures by planet ID and type.
     */
    List<PlanetStructure> findByPlanetIdAndStructureType(Long planetId, PlanetStructure.StructureType structureType);

    /**
     * Find structures by planet ID, type, and active status.
     */
    List<PlanetStructure> findByPlanetIdAndStructureTypeAndActive(
            Long planetId, PlanetStructure.StructureType structureType, boolean active);

    /**
     * Find structures within geographic bounds.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.latitude BETWEEN :minLat AND :maxLat AND " +
            "p.longitude BETWEEN :minLon AND :maxLon AND " +
            "p.active = true")
    List<PlanetStructure> findByGeographicBounds(
            @Param("planetId") Long planetId,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon);

    /**
     * Find mines on a planet.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.structureType = 'MINE' AND p.active = true")
    List<PlanetStructure> findMinesByPlanet(@Param("planetId") Long planetId);

    /**
     * Find equipment stores on a planet.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.structureType = 'EQUIPMENT_STORE' AND p.active = true")
    List<PlanetStructure> findEquipmentStoresByPlanet(@Param("planetId") Long planetId);

    /**
     * Find transport stations on a planet.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.structureType = 'TRANSPORT_STATION' AND p.active = true")
    List<PlanetStructure> findTransportStationsByPlanet(@Param("planetId") Long planetId);

    /**
     * Find spaceports on a planet.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.structureType = 'SPACEPORT' AND p.active = true")
    List<PlanetStructure> findSpaceportsByPlanet(@Param("planetId") Long planetId);

    /**
     * Find factories on a planet.
     */
    @Query("SELECT p FROM PlanetStructure p WHERE p.planetId = :planetId AND " +
            "p.structureType = 'FACTORY' AND p.active = true")
    List<PlanetStructure> findFactoriesByPlanet(@Param("planetId") Long planetId);
}
