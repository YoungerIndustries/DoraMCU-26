package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminTestReportRepository extends JpaRepository<AiAdminTestReport, Long> {

    List<AiAdminTestReport> findByOrderId(Long orderId);
}
