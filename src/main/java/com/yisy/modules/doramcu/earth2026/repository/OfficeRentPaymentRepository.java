package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.OfficeRentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfficeRentPaymentRepository extends JpaRepository<OfficeRentPayment, Long> {
    List<OfficeRentPayment> findByCorporationId(Long corporationId);
    List<OfficeRentPayment> findByCorporationIdAndStatus(Long corporationId, String status);
    List<OfficeRentPayment> findByOfficeUnitId(Long officeUnitId);
    List<OfficeRentPayment> findByCorporationIdOrderByPaymentDateDesc(Long corporationId);
    List<OfficeRentPayment> findByDueDateBeforeAndStatus(LocalDateTime date, String status);
    List<OfficeRentPayment> findByPeriodEndBeforeAndStatus(LocalDateTime date, String status);
}
