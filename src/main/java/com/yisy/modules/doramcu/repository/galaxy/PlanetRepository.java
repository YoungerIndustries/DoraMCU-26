package com.yisy.modules.doramcu.repository.galaxy;
import com.yisy.modules.doramcu.hex.MineralQuality;
import com.yisy.modules.doramcu.hex.PlanetSize;

import com.yisy.modules.doramcu.model.misc.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetRepository extends JpaRepository<Planet, Long> {

    List<Planet> findBySolarSystemId(Long solarSystemId);

    List<Planet> findByPlanetSize(Planet.PlanetSize planetSize);

    List<Planet> findByMineralQuality(Planet.MineralQuality mineralQuality);

    long countBySolarSystemId(Long solarSystemId);
}
