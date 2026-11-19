package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.WeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaponRepository extends JpaRepository<WeaponEntity, String> {
    void deleteByShipId(String shipId);
    java.util.List<WeaponEntity> findByShipId(String shipId);
}
