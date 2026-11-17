package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.IndustryOutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndustryOutputRepository extends JpaRepository<IndustryOutputEntity, Long> {

    List<IndustryOutputEntity> findByIndustryCode(String industryCode);
}
