package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.TradeRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRouteRepository extends JpaRepository<TradeRoute, Long> {
    List<TradeRoute> findByGameSessionId(Long gameSessionId);
    List<TradeRoute> findByOwnerFactionId(Long ownerFactionId);
}
