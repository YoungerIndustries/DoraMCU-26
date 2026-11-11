package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.fourx.FourxTradeRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FourxTradeRouteRepository extends JpaRepository<FourxTradeRouteEntity, Long> {

    Optional<FourxTradeRouteEntity> findByTradeRouteId(String tradeRouteId);

    List<FourxTradeRouteEntity> findByOwnerFactionId(String ownerFactionId);

    List<FourxTradeRouteEntity> findBySession_SessionId(String sessionId);
}
