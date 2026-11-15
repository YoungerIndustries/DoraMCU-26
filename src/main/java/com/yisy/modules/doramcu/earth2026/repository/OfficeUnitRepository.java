package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.OfficeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeUnitRepository extends JpaRepository<OfficeUnit, Long> {
    List<OfficeUnit> findByHqId(Long hqId);
    List<OfficeUnit> findByHqIdAndStatus(Long hqId, String status);
    Optional<OfficeUnit> findByOwnerIdAndStatus(Long ownerId, String status);
    List<OfficeUnit> findByStatus(String status);
}
