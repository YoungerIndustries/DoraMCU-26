package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EveCorporationRepository extends JpaRepository<EveCorporation, Long> {

    Optional<EveCorporation> findByTicker(String ticker);

    Optional<EveCorporation> findByName(String name);
}
