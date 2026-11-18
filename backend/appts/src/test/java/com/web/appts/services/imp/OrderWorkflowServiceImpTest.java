package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.repositories.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class OrderWorkflowServiceImpTest {

    @Mock
    private OrderRepo orderRepo;

    private OrderWorkflowServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderWorkflowServiceImp(orderRepo);
    }

    @Test
    void updateOrderColorsMovesReadyDepartmentToInProgress() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto order = orderWithDepartment(10, "100", "monLb", "R", "R");
        ordersMap.put("100,1", order);
        AtomicBoolean activeOrdersLoaded = new AtomicBoolean(false);

        List<OrderDto> result = service.updateOrderColors(
                "100",
                "monLb",
                "R",
                "FWD",
                ordersMap,
                () -> {
                    activeOrdersLoaded.set(true);
                    return Collections.singletonList(order);
                }
        );

        assertTrue(activeOrdersLoaded.get());
        assertEquals(1, result.size());
        assertEquals("Y", order.getMonLb());
        assertEquals("Y", order.getDepartments().get(0).getStatus());
        assertEquals("R", order.getDepartments().get(0).getPrevStatus());
        verify(orderRepo).updateFieldForIdsMainmonLb("Y", Collections.singletonList(10));
        verify(orderRepo).updateOrderDepartmentStatusMain("Y", "R", "MONLB", Collections.singletonList(10));
    }

    @Test
    void updateOrderColorsMovesInProgressDepartmentToDoneAndCompletesOrder() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto order = orderWithDepartment(10, "100", "exp", "Y", "Y");
        ordersMap.put("100,1", order);

        List<OrderDto> result = service.updateOrderColors(
                "100",
                "exp",
                "Y",
                "FWD",
                ordersMap,
                () -> Collections.singletonList(order)
        );

        assertEquals(1, result.size());
        assertEquals("G", order.getExp());
        assertEquals("G", order.getDepartments().get(0).getStatus());
        assertEquals("Y", order.getDepartments().get(0).getPrevStatus());
        assertEquals("C", order.getCompleted());
        verify(orderRepo).updateFieldForIdsMainexp("G", Collections.singletonList(10));
        verify(orderRepo).updateOrderDepartmentStatusMain("G", "Y", "EXP", Collections.singletonList(10));
    }

    private OrderDto orderWithDepartment(
            int id,
            String orderNumber,
            String department,
            String fieldStatus,
            String departmentStatus
    ) {
        OrderDepartment orderDepartment = new OrderDepartment();
        orderDepartment.setDepName(department.toUpperCase());
        orderDepartment.setStatus(departmentStatus);

        OrderDto order = new OrderDto();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setRegel("1");
        order.setDepartments(Collections.singletonList(orderDepartment));

        if ("monLb".equals(department)) {
            order.setMonLb(fieldStatus);
        }
        if ("exp".equals(department)) {
            order.setExp(fieldStatus);
        }
        return order;
    }
}
