package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.repositories.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderTransportLinkServiceImpTest {

    @Mock
    private OrderRepo orderRepo;

    private OrderTransportLinkServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderTransportLinkServiceImp(orderRepo);
    }

    @Test
    void updateTraColorsMovesReadyOrdersToInProgress() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto order = orderWithTra(10, "R", "R");
        ordersMap.put("100,1", order);

        Boolean result = service.updateTraColors("10", ordersMap);

        assertTrue(result);
        assertEquals("Y", order.getTra());
        assertEquals("Y", order.getDepartments().get(0).getStatus());
        assertEquals("R", order.getDepartments().get(0).getPrevStatus());
        verify(orderRepo).updateFieldForRIds("Y", Collections.singletonList(10));
        verify(orderRepo).updateOrderDepartmentStatusR("Y", "R", Collections.singletonList(10));
    }

    @Test
    void updateTraColorsMovesInProgressOrdersToDoneAndMarksCompleted() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto order = orderWithTra(10, "Y", "Y");
        ordersMap.put("100,1", order);

        Boolean result = service.updateTraColors("10", ordersMap);

        assertTrue(result);
        assertEquals("G", order.getTra());
        assertEquals("G", order.getDepartments().get(0).getStatus());
        assertEquals("Y", order.getDepartments().get(0).getPrevStatus());
        assertEquals("C", order.getCompleted());
        verify(orderRepo).updateFieldForYIds("G", Collections.singletonList(10));
        verify(orderRepo).updateOrderDepartmentStatusY("G", "Y", Collections.singletonList(10));
    }

    @Test
    void updateTraColorsSkipsEmptyIds() {
        Boolean result = service.updateTraColors(" ", new ConcurrentHashMap<>());

        assertFalse(result);
        verify(orderRepo, never()).updateFieldForRIds(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyList());
        verify(orderRepo, never()).updateFieldForYIds(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyList());
    }

    private OrderDto orderWithTra(int id, String tra, String departmentStatus) {
        OrderDepartment department = new OrderDepartment();
        department.setDepName("TRA");
        department.setStatus(departmentStatus);

        OrderDto order = new OrderDto();
        order.setId(id);
        order.setOrderNumber("100");
        order.setRegel("1");
        order.setTra(tra);
        order.setDepartments(Collections.singletonList(department));
        return order;
    }
}
