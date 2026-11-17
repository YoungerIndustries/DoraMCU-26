package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.IndustryTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryTypeRepository extends JpaRepository<IndustryTypeEntity, Long> {

    Optional<IndustryTypeEntity> findByIndustryCode(String industryCode);

    List<IndustryTypeEntity> findByProductionLevel(String productionLevel);
}
