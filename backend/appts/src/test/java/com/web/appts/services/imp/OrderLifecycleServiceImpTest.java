package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderLifecycleServiceImpTest {

    @Mock
    private OrderService orderService;

    private OrderLifecycleServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderLifecycleServiceImp(orderService);
    }

    @Test
    void delegatesLifecycleOperations() {
        HashMap<String, OrderDto> monSubMap = new HashMap<>();
        when(orderService.createMonSub()).thenReturn(monSubMap);

        service.markExpired();
        service.checkOrderExistence();
        service.adjustParentOrders();
        service.verifyCrmOrders();
        service.removingSameArchivedOrders();
        assertSame(monSubMap, service.createMonSub());

        verify(orderService).markExpired();
        verify(orderService).checkOrderExistence();
        verify(orderService).adjustParentOrders();
        verify(orderService).verifyCRMOrders();
        verify(orderService).removingSameArchivedOrders();
        verify(orderService).createMonSub();
    }
}
