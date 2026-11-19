package com.yisy.modules.doramcu.repository.galaxy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GalaxyRepository extends JpaRepository<Galaxy, Long> {

    List<Galaxy> findByNameContainingIgnoreCase(String name);

    Optional<Galaxy> findByName(String name);

    List<Galaxy> findByLoreSlug(String loreSlug);
}
