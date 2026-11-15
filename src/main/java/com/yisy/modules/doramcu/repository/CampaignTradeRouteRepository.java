package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.CampaignTradeRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing planet trade route persistence.
 */
@Repository
public interface CampaignTradeRouteRepository extends JpaRepository<CampaignTradeRouteEntity, Long> {

    /** Find all trade routes on a specific planet. */
    List<CampaignTradeRouteEntity> findByPlanetId(int planetId);

    /** Find trade route by its unique ID. */
    java.util.Optional<CampaignTradeRouteEntity> findByRouteId(String routeId);

    /** Find trade routes owned by a corporation on a planet. */
    List<CampaignTradeRouteEntity> findByPlanetIdAndOwnerCorporationId(int planetId, String corporationId);

    /** Delete all trade routes for a specific planet. */
    void deleteByPlanetId(int planetId);

    /** Count trade routes on a specific planet. */
    long countByPlanetId(int planetId);
}
