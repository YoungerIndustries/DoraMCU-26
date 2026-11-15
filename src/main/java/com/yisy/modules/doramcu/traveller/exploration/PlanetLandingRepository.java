package com.yisy.modules.doramcu.traveller.exploration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetLandingRepository extends JpaRepository<PlanetLanding, Long> {
    List<PlanetLanding> findByCharacterIdOrderByTimestampDesc(String characterId);
}
