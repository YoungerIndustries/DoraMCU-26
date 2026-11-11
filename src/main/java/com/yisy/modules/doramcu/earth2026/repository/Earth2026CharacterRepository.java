package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026CharacterRepository extends JpaRepository<Earth2026Character, Long> {

    List<Earth2026Character> findByNameContainingIgnoreCase(String name);

    List<Earth2026Character> findByHasTransitionedFalse();

    List<Earth2026Character> findByTargetGalaxy(String targetGalaxy);

    List<Earth2026Character> findByEarthLocation(String earthLocation);

    List<Earth2026Character> findByPlayerId(Long playerId);
}
