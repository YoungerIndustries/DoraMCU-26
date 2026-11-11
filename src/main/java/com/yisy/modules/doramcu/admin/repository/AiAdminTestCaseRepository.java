package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminTestCaseRepository extends JpaRepository<AiAdminTestCase, Long> {

    List<AiAdminTestCase> findByReportId(Long reportId);

    List<AiAdminTestCase> findByCategory(String category);
}
