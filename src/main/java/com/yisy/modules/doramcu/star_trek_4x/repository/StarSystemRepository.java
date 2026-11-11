package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarSystemRepository extends JpaRepository<StarSystem, Long> {
    List<StarSystem> findByGameSessionId(Long gameSessionId);
    List<StarSystem> findBySectorId(int sectorId);
    List<StarSystem> findByDiscoveredFalse();
}
