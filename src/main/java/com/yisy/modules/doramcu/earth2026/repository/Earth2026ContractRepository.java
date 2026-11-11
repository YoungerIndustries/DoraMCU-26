package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026ContractRepository extends JpaRepository<Earth2026Contract, Long> {
    List<Earth2026Contract> findByPlanetId(Long planetId);
    List<Earth2026Contract> findByPlanetIdAndStatus(Long planetId, String status);
    List<Earth2026Contract> findByPlanetIdAndStatusAndActiveTrue(Long planetId, String status);
    Page<Earth2026Contract> findByPlanetIdAndStatusAndActiveTrue(Long planetId, String status, Pageable pageable);
    long countByPlanetIdAndStatusAndActiveTrue(Long planetId, String status);
    List<Earth2026Contract> findByAssigneeId(Long assigneeId);
}
