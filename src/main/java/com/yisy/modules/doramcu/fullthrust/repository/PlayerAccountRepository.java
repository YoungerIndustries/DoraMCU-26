package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.PlayerAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerAccountRepository extends JpaRepository<PlayerAccountEntity, Long> {
    Optional<PlayerAccountEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<PlayerAccountEntity> findByGoogleEmail(String googleEmail);
    boolean existsByGoogleEmail(String googleEmail);
}
