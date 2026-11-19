package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevelopmentTestHelperTest {

    @Mock
    private AiAdminService aiAdminService;

    private DevelopmentTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new DevelopmentTestHelper(aiAdminService);
    }

    @Test
    @DisplayName("runTestsAndReport should submit order and return report")
    void runTestsAndReport_success() {
        AiAdminOrder order = new AiAdminOrder("admin", "test API");
        order.setId(1L);
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(1L);
        report.setOrderId(1L);
        report.setTotalTests(3);
        report.setPassed(3);
        report.setFailed(0);

        when(aiAdminService.submitOrder(anyString(), anyString())).thenReturn(order);
        when(aiAdminService.waitForOrder(eq(1L), anyLong())).thenReturn(true);
        when(aiAdminService.getReportForOrder(1L)).thenReturn(Optional.of(report));

        AiAdminTestReport result = helper.runTestsAndReport("admin", "test API");

        assertNotNull(result);
        assertEquals(3, result.getPassed());
        assertEquals(0, result.getFailed());
        verify(aiAdminService).submitOrder(eq("admin"), eq("test API"));
        verify(aiAdminService).waitForOrder(eq(1L), anyLong());
    }

    @Test
    @DisplayName("runTestsAndReport should return null on timeout")
    void runTestsAndReport_timeout() {
        AiAdminOrder order = new AiAdminOrder("admin", "test all");
        order.setId(2L);
        order.setStatus(AiAdminOrder.OrderStatus.RUNNING);

        when(aiAdminService.submitOrder(anyString(), anyString())).thenReturn(order);
        when(aiAdminService.waitForOrder(eq(2L), eq(10L))).thenReturn(false);
        when(aiAdminService.getOrder(2L)).thenReturn(Optional.of(order));

        AiAdminTestReport result = helper.runTestsAndReport("admin", "test all", 10);

        assertNull(result);
        verify(aiAdminService).waitForOrder(eq(2L), eq(10L));
    }

    @Test
    @DisplayName("runTestsAndReport should return null on order failure")
    void runTestsAndReport_orderFailed() {
        AiAdminOrder order = new AiAdminOrder("admin", "test game");
        order.setId(3L);
        order.setStatus(AiAdminOrder.OrderStatus.FAILED);
        order.setCompletedAt(LocalDateTime.now());

        when(aiAdminService.submitOrder(anyString(), anyString())).thenReturn(order);
        when(aiAdminService.waitForOrder(eq(3L), anyLong())).thenReturn(false);
        when(aiAdminService.getOrder(3L)).thenReturn(Optional.of(order));

        AiAdminTestReport result = helper.runTestsAndReport("admin", "test game");

        assertNull(result);
    }

    @Test
    @DisplayName("runTestsForCategory should run tests for a category")
    void runTestsForCategory() {
        AiAdminOrder order = new AiAdminOrder("admin", "test api");
        order.setId(4L);
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(2L);
        report.setOrderId(4L);
        report.setTotalTests(3);
        report.setPassed(3);
        report.setFailed(0);

        when(aiAdminService.submitOrder(anyString(), anyString())).thenReturn(order);
        when(aiAdminService.waitForOrder(eq(4L), anyLong())).thenReturn(true);
        when(aiAdminService.getReportForOrder(4L)).thenReturn(Optional.of(report));

        AiAdminTestReport result = helper.runTestsForCategory("admin", "API");

        assertNotNull(result);
        assertEquals(3, result.getPassed());
    }

    @Test
    @DisplayName("runAllTests should run all tests")
    void runAllTests() {
        AiAdminOrder order = new AiAdminOrder("admin", "test all");
        order.setId(5L);
        order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        AiAdminTestReport report = new AiAdminTestReport();
        report.setId(3L);
        report.setOrderId(5L);
        report.setTotalTests(8);
        report.setPassed(7);
        report.setFailed(1);

        when(aiAdminService.submitOrder(anyString(), anyString())).thenReturn(order);
        when(aiAdminService.waitForOrder(eq(5L), anyLong())).thenReturn(true);
        when(aiAdminService.getReportForOrder(5L)).thenReturn(Optional.of(report));

        AiAdminTestReport result = helper.runAllTests("admin");

        assertNotNull(result);
        assertEquals(7, result.getPassed());
        assertEquals(1, result.getFailed());
    }

    @Test
    @DisplayName("getDefaultTimeoutSeconds should return default value")
    void getDefaultTimeoutSeconds() {
        // Default is 0 in unit tests without Spring context, which is fine
        // In production it's set via @Value to 60
        assertEquals(0, helper.getDefaultTimeoutSeconds());
    }
}
