package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder.OrderStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class AiAdminServiceTest {

    @Mock
    private AiAdminOrderRepository orderRepository;

    @Mock
    private AiAdminTestReportRepository reportRepository;

    @Mock
    private TestExecutor testExecutor;

    @Mock
    private TestReportGenerator testReportGenerator;

    @InjectMocks
    private AiAdminService aiAdminService;

    private AiAdminOrder testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new AiAdminOrder("admin", "Test all endpoints");
        testOrder.setId(1L);
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("submitOrder should save order and return it")
    void submitOrder_shouldSaveAndReturn() {
        when(orderRepository.save(any(AiAdminOrder.class))).thenReturn(testOrder);

        AiAdminOrder result = aiAdminService.submitOrder("admin", "Test all endpoints");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getSubmittedBy());
        assertEquals("Test all endpoints", result.getOrderText());
        // save is called multiple times: initial save + background thread status updates
        verify(orderRepository, atLeastOnce()).save(any(AiAdminOrder.class));
    }

    @Test
    @DisplayName("getOrder should return order by ID")
    void getOrder_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Optional<AiAdminOrder> result = aiAdminService.getOrder(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("getOrder should return empty for non-existent order")
    void getOrder_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminOrder> result = aiAdminService.getOrder(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllOrders should return all orders")
    void getAllOrders_shouldReturnAll() {
        List<AiAdminOrder> orders = List.of(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<AiAdminOrder> result = aiAdminService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getReport should return report by ID")
    void getReport_shouldReturnReport() {
        AiAdminTestReport report = new AiAdminTestReport(1L);
        report.setId(10L);
        report.setPassed(5);
        report.setFailed(0);
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));

        Optional<AiAdminTestReport> result = aiAdminService.getReport(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        assertEquals(5, result.get().getPassed());
    }

    @Test
    @DisplayName("getReportForOrder should return report for order")
    void getReportForOrder_shouldReturnReport() {
        AiAdminTestReport report = new AiAdminTestReport(1L);
        report.setId(10L);
        when(reportRepository.findByOrderId(1L)).thenReturn(List.of(report));

        Optional<AiAdminTestReport> result = aiAdminService.getReportForOrder(1L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }
}
