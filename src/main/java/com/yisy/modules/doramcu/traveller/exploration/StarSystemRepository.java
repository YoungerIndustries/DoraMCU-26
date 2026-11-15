package com.yisy.modules.doramcu.traveller.exploration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("travellerStarSystemRepository")
public interface StarSystemRepository extends JpaRepository<StarSystem, Long> {
    StarSystem findByHexSector(String hexSector);
    List<StarSystem> findByNameContainingIgnoreCase(String name);
}
