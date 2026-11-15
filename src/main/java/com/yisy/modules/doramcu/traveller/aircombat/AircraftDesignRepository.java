package com.yisy.modules.doramcu.traveller.aircombat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AircraftDesignRepository extends JpaRepository<AircraftDesign, Long> {
    List<AircraftDesign> findByType(String type);
}
