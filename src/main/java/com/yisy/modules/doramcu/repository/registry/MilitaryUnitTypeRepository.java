package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.MilitaryUnitTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MilitaryUnitTypeRepository extends JpaRepository<MilitaryUnitTypeEntity, Long> {

    Optional<MilitaryUnitTypeEntity> findByUnitCode(String unitCode);
}
