package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026PartyRepository extends JpaRepository<Earth2026Party, Long> {

    List<Earth2026Party> findByLocationId(Long locationId);

    List<Earth2026Party> findByStatus(String status);

    List<Earth2026Party> findByIsRecruitingTrue();

    long countByLocationId(Long locationId);
}
