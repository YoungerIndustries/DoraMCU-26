package com.yisy.modules.doramcu.repository.galaxy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HyperlaneRepository extends JpaRepository<Hyperlane, Long> {

    List<Hyperlane> findByGalaxyId(Long galaxyId);

    long countByGalaxyId(Long galaxyId);

    void deleteByGalaxyId(Long galaxyId);
}
