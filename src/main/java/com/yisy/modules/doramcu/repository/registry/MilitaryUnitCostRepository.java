package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.MilitaryUnitCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilitaryUnitCostRepository extends JpaRepository<MilitaryUnitCostEntity, Long> {

    List<MilitaryUnitCostEntity> findByUnitCode(String unitCode);
}
