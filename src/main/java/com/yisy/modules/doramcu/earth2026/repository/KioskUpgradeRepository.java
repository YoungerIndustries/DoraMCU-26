package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.KioskUpgrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KioskUpgradeRepository extends JpaRepository<KioskUpgrade, Long> {
    List<KioskUpgrade> findByCorporationId(Long corporationId);
    List<KioskUpgrade> findByCorporationIdAndOfficeUnitId(Long corporationId, Long officeUnitId);
    Optional<KioskUpgrade> findByCorporationIdAndOfficeUnitIdAndUpgradeType(
            Long corporationId, Long officeUnitId, String upgradeType);
    boolean existsByCorporationIdAndOfficeUnitIdAndUpgradeType(
            Long corporationId, Long officeUnitId, String upgradeType);
    void deleteByCorporationIdAndOfficeUnitId(Long corporationId, Long officeUnitId);
}
