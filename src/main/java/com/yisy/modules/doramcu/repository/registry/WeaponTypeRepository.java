package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.WeaponTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeaponTypeRepository extends JpaRepository<WeaponTypeEntity, Long> {

    Optional<WeaponTypeEntity> findByWeaponCode(String weaponCode);
}
