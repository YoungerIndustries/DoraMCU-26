package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Wormhole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WormholeRepository extends JpaRepository<Wormhole, Long> {

    Optional<Wormhole> findByGalaxyName(String galaxyName);

    List<Wormhole> findAllByIsActiveTrue();

    boolean existsByGalaxyName(String galaxyName);

    Optional<Wormhole> findByConnectedWormholeId(Long connectedWormholeId);
}
