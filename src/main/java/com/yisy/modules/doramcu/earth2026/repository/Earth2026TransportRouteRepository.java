package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying Earth 2026 transport routes.
 */
@Repository
public interface Earth2026TransportRouteRepository extends JpaRepository<Earth2026TransportRoute, Long> {

    /**
     * Find all routes departing from a specific location.
     */
    List<Earth2026TransportRoute> findByFromLocationId(Long fromLocationId);

    /**
     * Find all routes arriving at a specific location.
     */
    List<Earth2026TransportRoute> findByToLocationId(Long toLocationId);

    /**
     * Find all routes on a specific planet.
     */
    List<Earth2026TransportRoute> findByPlanetId(Long planetId);

    /**
     * Find active routes on a specific planet.
     */
    List<Earth2026TransportRoute> findByPlanetIdAndActiveTrue(Long planetId);

    /**
     * Find a route between two specific locations.
     */
    Optional<Earth2026TransportRoute> findByFromLocationIdAndToLocationId(Long fromLocationId, Long toLocationId);

    /**
     * Find active routes departing from a specific location.
     */
    List<Earth2026TransportRoute> findByFromLocationIdAndActiveTrue(Long fromLocationId);

    /**
     * Count active routes on a specific planet.
     */
    long countByPlanetIdAndActiveTrue(Long planetId);

    /**
     * Find routes between two specific cities.
     */
    List<Earth2026TransportRoute> findByFromCityAndToCity(String fromCity, String toCity);

    /**
     * Find routes departing from a specific city.
     */
    List<Earth2026TransportRoute> findByFromCity(String fromCity);

    /**
     * Find routes arriving at a specific city.
     */
    List<Earth2026TransportRoute> findByToCity(String toCity);

    /**
     * Count routes on a specific planet.
     */
    long countByPlanetId(Long planetId);
}
