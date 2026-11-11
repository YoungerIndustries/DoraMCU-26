package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestResultAnalyzerTest {

    @Mock
    private AiAdminTestCaseRepository testCaseRepository;

    @Mock
    private AiAdminTestReportRepository reportRepository;

    private TestResultAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TestResultAnalyzer(testCaseRepository, reportRepository);
    }

    @Test
    @DisplayName("analyzeReport should calculate pass rate correctly")
    void analyzeReport_passRate() {
        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(1L);
        report.setTotalTests(10);
        report.setPassed(8);
        report.setFailed(2);
        report.setSkipped(0);

        AiAdminTestCase passedTest = new AiAdminTestCase(null, "Test 1", "API");
        passedTest.setStatus(TestStatus.PASSED);
        passedTest.setDurationMs(100);

        AiAdminTestCase failedTest = new AiAdminTestCase(null, "Test 2", "GAME");
        failedTest.setStatus(TestStatus.FAILED);
        failedTest.setDurationMs(200);

        when(testCaseRepository.findByReportId(1L)).thenReturn(Arrays.asList(passedTest, failedTest));

        TestResultAnalyzer.TestAnalysis analysis = analyzer.analyzeReport(report);

        assertNotNull(analysis);
        assertEquals(0.8, analysis.getPassRate(), 0.01);
        assertEquals(10, analysis.getTotalTests());
        assertEquals(8, analysis.getPassed());
        assertEquals(2, analysis.getFailed());
    }

    @Test
    @DisplayName("analyzeReport should identify failures by category")
    void analyzeReport_failuresByCategory() {
        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(2L);
        report.setTotalTests(3);
        report.setPassed(1);
        report.setFailed(2);
        report.setSkipped(0);

        AiAdminTestCase apiFail = new AiAdminTestCase(null, "API Test", "API");
        apiFail.setStatus(TestStatus.FAILED);
        apiFail.setDurationMs(100);

        AiAdminTestCase gameFail = new AiAdminTestCase(null, "Game Test", "GAME");
        gameFail.setStatus(TestStatus.FAILED);
        gameFail.setDurationMs(200);

        AiAdminTestCase passedTest = new AiAdminTestCase(null, "Auth Test", "AUTH");
        passedTest.setStatus(TestStatus.PASSED);
        passedTest.setDurationMs(50);

        when(testCaseRepository.findByReportId(2L)).thenReturn(Arrays.asList(apiFail, gameFail, passedTest));

        TestResultAnalyzer.TestAnalysis analysis = analyzer.analyzeReport(report);

        assertTrue(analysis.getFailuresByCategory().contains("API"));
        assertTrue(analysis.getFailuresByCategory().contains("GAME"));
    }

    @Test
    @DisplayName("analyzeReport should identify slow tests")
    void analyzeReport_slowTests() {
        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(3L);
        report.setTotalTests(2);
        report.setPassed(2);
        report.setFailed(0);
        report.setSkipped(0);

        AiAdminTestCase fastTest = new AiAdminTestCase(null, "Fast Test", "API");
        fastTest.setStatus(TestStatus.PASSED);
        fastTest.setDurationMs(100);

        AiAdminTestCase slowTest = new AiAdminTestCase(null, "Slow Test", "GAME");
        slowTest.setStatus(TestStatus.PASSED);
        slowTest.setDurationMs(5000);

        when(testCaseRepository.findByReportId(3L)).thenReturn(Arrays.asList(fastTest, slowTest));

        TestResultAnalyzer.TestAnalysis analysis = analyzer.analyzeReport(report);

        assertEquals(1, analysis.getSlowTests());
    }

    @Test
    @DisplayName("analyzeReport should generate recommendations for failures")
    void analyzeReport_recommendations() {
        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(4L);
        report.setTotalTests(2);
        report.setPassed(1);
        report.setFailed(1);
        report.setSkipped(0);

        AiAdminTestCase apiFail = new AiAdminTestCase(null, "API Test", "API");
        apiFail.setStatus(TestStatus.FAILED);
        apiFail.setDurationMs(100);

        AiAdminTestCase passedTest = new AiAdminTestCase(null, "Auth Test", "AUTH");
        passedTest.setStatus(TestStatus.PASSED);
        passedTest.setDurationMs(50);

        when(testCaseRepository.findByReportId(4L)).thenReturn(Arrays.asList(apiFail, passedTest));

        TestResultAnalyzer.TestAnalysis analysis = analyzer.analyzeReport(report);

        assertNotNull(analysis.getRecommendations());
        assertFalse(analysis.getRecommendations().isEmpty());
        assertTrue(analysis.getRecommendations().stream()
                .anyMatch(r -> r.contains("API endpoint")));
    }

    @Test
    @DisplayName("analyzeReport should generate summary")
    void analyzeReport_summary() {
        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(5L);
        report.setTotalTests(5);
        report.setPassed(5);
        report.setFailed(0);
        report.setSkipped(0);

        when(testCaseRepository.findByReportId(5L)).thenReturn(List.of());

        TestResultAnalyzer.TestAnalysis analysis = analyzer.analyzeReport(report);

        assertNotNull(analysis.getSummary());
        assertTrue(analysis.getSummary().contains("Test Analysis Summary"));
        assertTrue(analysis.getSummary().contains("100%"));
    }

    @Test
    @DisplayName("analyzeTrends should find recurring failures")
    void analyzeTrends_recurringFailures() {
        AiAdminTestReport report1 = new AiAdminTestReport();
        report1.setId(1L);
        report1.setTotalTests(2);
        report1.setPassed(1);
        report1.setFailed(1);

        AiAdminTestReport report2 = new AiAdminTestReport();
        report2.setId(2L);
        report2.setTotalTests(2);
        report2.setPassed(1);
        report2.setFailed(1);

        AiAdminTestCase failedTest1 = new AiAdminTestCase(null, "Flaky Test", "API");
        failedTest1.setStatus(TestStatus.FAILED);

        AiAdminTestCase passedTest1 = new AiAdminTestCase(null, "Stable Test", "AUTH");
        passedTest1.setStatus(TestStatus.PASSED);

        AiAdminTestCase failedTest2 = new AiAdminTestCase(null, "Flaky Test", "API");
        failedTest2.setStatus(TestStatus.FAILED);

        AiAdminTestCase passedTest2 = new AiAdminTestCase(null, "Stable Test", "AUTH");
        passedTest2.setStatus(TestStatus.PASSED);

        when(reportRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(report1, report2));
        when(testCaseRepository.findByReportId(1L)).thenReturn(Arrays.asList(failedTest1, passedTest1));
        when(testCaseRepository.findByReportId(2L)).thenReturn(Arrays.asList(failedTest2, passedTest2));

        TestResultAnalyzer.TrendAnalysis trend = analyzer.analyzeTrends(Arrays.asList(1L, 2L));

        assertNotNull(trend);
        assertEquals(2, trend.getTotalReports());
        assertTrue(trend.getRecurringFailures().contains("Flaky Test"));
    }

    @Test
    @DisplayName("analyzeTrends should identify flaky tests")
    void analyzeTrends_flakyTests() {
        AiAdminTestReport report1 = new AiAdminTestReport();
        report1.setId(1L);
        report1.setTotalTests(1);
        report1.setPassed(1);
        report1.setFailed(0);

        AiAdminTestReport report2 = new AiAdminTestReport();
        report2.setId(2L);
        report2.setTotalTests(1);
        report2.setPassed(0);
        report2.setFailed(1);

        AiAdminTestCase passedTest = new AiAdminTestCase(null, "Flaky Test", "API");
        passedTest.setStatus(TestStatus.PASSED);

        AiAdminTestCase failedTest = new AiAdminTestCase(null, "Flaky Test", "API");
        failedTest.setStatus(TestStatus.FAILED);

        when(reportRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(report1, report2));
        when(testCaseRepository.findByReportId(1L)).thenReturn(List.of(passedTest));
        when(testCaseRepository.findByReportId(2L)).thenReturn(List.of(failedTest));

        TestResultAnalyzer.TrendAnalysis trend = analyzer.analyzeTrends(Arrays.asList(1L, 2L));

        assertTrue(trend.getFlakyTests().contains("Flaky Test"));
    }

    @Test
    @DisplayName("analyzeTrends should handle empty report list")
    void analyzeTrends_emptyList() {
        TestResultAnalyzer.TrendAnalysis trend = analyzer.analyzeTrends(List.of());

        assertNotNull(trend);
        assertEquals(0, trend.getTotalReports());
    }
}
