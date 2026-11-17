package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.IndustryInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndustryInputRepository extends JpaRepository<IndustryInputEntity, Long> {

    List<IndustryInputEntity> findByIndustryCode(String industryCode);
}
