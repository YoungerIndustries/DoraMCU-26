package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.YoungerIndustriesHQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YoungerIndustriesHQRepository extends JpaRepository<YoungerIndustriesHQ, Long> {
    List<YoungerIndustriesHQ> findByActiveTrue();
    List<YoungerIndustriesHQ> findByLocationId(Long locationId);
}
