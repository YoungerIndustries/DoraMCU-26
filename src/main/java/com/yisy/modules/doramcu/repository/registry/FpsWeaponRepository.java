package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.FpsWeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FpsWeaponRepository extends JpaRepository<FpsWeaponEntity, Long> {

    Optional<FpsWeaponEntity> findByWeaponUuid(String weaponUuid);

    List<FpsWeaponEntity> findByWeaponTypeCode(String weaponTypeCode);
}
