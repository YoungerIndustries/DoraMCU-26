package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.ApartmentBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentBuildingRepository extends JpaRepository<ApartmentBuilding, Long> {
    List<ApartmentBuilding> findByActiveTrue();
    List<ApartmentBuilding> findByLocationId(Long locationId);
}
