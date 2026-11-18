package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderLifecycleService;
import com.web.appts.services.OrderService;
import com.web.appts.services.OrderTextSyncService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderRefreshWorkflowServiceImpTest {

    @Test
    void refreshOrdersFromCrmRunsWorkflowAndReturnsSortedOrders() {
        OrderService orderService = mock(OrderService.class);
        OrderLifecycleService orderLifecycleService = mock(OrderLifecycleService.class);
        OrderTextSyncService orderTextSyncService = mock(OrderTextSyncService.class);
        OrderRefreshWorkflowServiceImp service = new OrderRefreshWorkflowServiceImp(
                orderService,
                orderLifecycleService,
                orderTextSyncService
        );

        OrderDto older = new OrderDto();
        older.setOrderNumber("100");
        older.setDeliveryDate("2024-01-01");

        OrderDto newer = new OrderDto();
        newer.setOrderNumber("200");
        newer.setDeliveryDate("2025-01-01");

        List<OrderDto> unsorted = Arrays.asList(older, newer);
        when(orderService.checkMap()).thenReturn(unsorted, unsorted);

        List<OrderDto> result = service.refreshOrdersFromCrm();

        verify(orderService, times(2)).checkMap();
        verify(orderService).getCRMOrders();
        verify(orderLifecycleService).markExpired();
        verify(orderLifecycleService).checkOrderExistence();
        verify(orderLifecycleService).createMonSub();
        verify(orderTextSyncService).updateProductNotes();
        verify(orderLifecycleService).adjustParentOrders();

        assertEquals("200", result.get(0).getOrderNumber());
        assertEquals("100", result.get(1).getOrderNumber());
    }
}
