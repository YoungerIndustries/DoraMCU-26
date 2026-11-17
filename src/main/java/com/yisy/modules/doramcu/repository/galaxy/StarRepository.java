package com.yisy.modules.doramcu.repository.galaxy;

import com.yisy.modules.doramcu.model.galaxy.Star;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarRepository extends JpaRepository<Star, Long> {

    Star findBySolarSystemId(Long solarSystemId);

    List<Star> findByStarType(Star.StarType starType);

    List<Star> findByStarSize(Star.StarSize starSize);
}
