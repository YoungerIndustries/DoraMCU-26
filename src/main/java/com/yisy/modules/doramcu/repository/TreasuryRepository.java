package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.eve.model.Treasury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreasuryRepository extends JpaRepository<Treasury, Long> {

    List<Treasury> findByOwnerIdAndOwnerType(String ownerId, String ownerType);

    boolean existsByOwnerIdAndOwnerType(String ownerId, String ownerType);
}
