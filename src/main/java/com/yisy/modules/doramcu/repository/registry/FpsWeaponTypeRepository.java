package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.FpsWeaponTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FpsWeaponTypeRepository extends JpaRepository<FpsWeaponTypeEntity, Long> {

    Optional<FpsWeaponTypeEntity> findByWeaponCode(String weaponCode);
}
