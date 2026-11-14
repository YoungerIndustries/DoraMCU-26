package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.GalaxyObjectState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing galaxy object lifecycle states.
 */
@Repository
public interface GalaxyObjectStateRepository extends JpaRepository<GalaxyObjectState, Long> {

    /**
     * Find all objects in a galaxy.
     */
    List<GalaxyObjectState> findByGalaxyName(String galaxyName);

    /**
     * Find objects by galaxy and type.
     */
    List<GalaxyObjectState> findByGalaxyNameAndObjectType(String galaxyName, String objectType);

    /**
     * Find a specific object by galaxy, type, and ID.
     */
    GalaxyObjectState findByGalaxyNameAndObjectTypeAndObjectId(
            String galaxyName, String objectType, String objectId);

    /**
     * Find all destroyed objects in a galaxy.
     */
    List<GalaxyObjectState> findByGalaxyNameAndState(
            String galaxyName, GalaxyObjectState.ObjectState state);

    /**
     * Find objects near a sector position (within the given radius).
     */
    @Query("SELECT g FROM GalaxyObjectState g WHERE g.galaxyName = :galaxyName " +
            "AND g.sectorRow BETWEEN :minRow AND :maxRow " +
            "AND g.sectorCol BETWEEN :minCol AND :maxCol")
    List<GalaxyObjectState> findNearSector(@Param("galaxyName") String galaxyName,
                                           @Param("minRow") int minRow,
                                           @Param("maxRow") int maxRow,
                                           @Param("minCol") int minCol,
                                           @Param("maxCol") int maxCol);

    /**
     * Find objects that need 3D detail but don't have it yet.
     */
    List<GalaxyObjectState> findByGalaxyNameAndRequires3DTrueAndHas3DDetailFalse(
            String galaxyName);

    /**
     * Find all objects on a specific planet.
     */
    List<GalaxyObjectState> findByGalaxyNameAndPlanetName(
            String galaxyName, String planetName);

    /**
     * Count objects by state in a galaxy.
     */
    @Query("SELECT g.state, COUNT(g) FROM GalaxyObjectState g " +
            "WHERE g.galaxyName = :galaxyName GROUP BY g.state")
    List<Object[]> countByStateGrouped(@Param("galaxyName") String galaxyName);

    /**
     * Count total objects in a galaxy.
     */
    long countByGalaxyName(String galaxyName);

    /**
     * Delete all objects for a galaxy (used when resetting a galaxy).
     */
    void deleteByGalaxyName(String galaxyName);
}
