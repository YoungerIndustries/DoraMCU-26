package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.PlanetaryBattle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetaryBattleRepository extends JpaRepository<PlanetaryBattle, Long> {
    List<PlanetaryBattle> findByGameSessionId(Long gameSessionId);
    List<PlanetaryBattle> findByStarSystemId(Long starSystemId);
}
