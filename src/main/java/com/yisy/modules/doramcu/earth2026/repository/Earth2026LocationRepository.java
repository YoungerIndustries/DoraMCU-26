package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026LocationRepository extends JpaRepository<Earth2026Location, Long> {

    List<Earth2026Location> findByCity(String city);

    List<Earth2026Location> findByType(String type);

    List<Earth2026Location> findByHasPortalTrue();

    long countByHasPortalTrue();

    List<Earth2026Location> findByActiveTrue();
}
