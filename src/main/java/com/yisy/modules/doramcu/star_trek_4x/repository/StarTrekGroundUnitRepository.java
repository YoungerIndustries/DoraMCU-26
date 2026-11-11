package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekGroundUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarTrekGroundUnitRepository extends JpaRepository<StarTrekGroundUnit, Long> {
    List<StarTrekGroundUnit> findByOwnerFactionId(Long ownerFactionId);
    List<StarTrekGroundUnit> findByGameSessionId(Long gameSessionId);
}
