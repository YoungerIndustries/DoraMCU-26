package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.SanDiegoTransitRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanDiegoTransitRouteRepository extends JpaRepository<SanDiegoTransitRoute, Long> {

    List<SanDiegoTransitRoute> findByActiveTrue();

    List<SanDiegoTransitRoute> findByRouteTypeAndActiveTrue(String routeType);

    List<SanDiegoTransitRoute> findByStartLocationContainingAndActiveTrue(String location);

    List<SanDiegoTransitRoute> findByEndLocationContainingAndActiveTrue(String location);
}
