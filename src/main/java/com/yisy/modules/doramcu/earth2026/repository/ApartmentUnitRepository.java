package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.ApartmentUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentUnitRepository extends JpaRepository<ApartmentUnit, Long> {
    List<ApartmentUnit> findByBuildingId(Long buildingId);
    List<ApartmentUnit> findByBuildingIdAndStatus(Long buildingId, String status);
    Optional<ApartmentUnit> findByTenantIdAndStatus(Long tenantId, String status);
    List<ApartmentUnit> findByStatus(String status);
}
