package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.OrderPreserves;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderErpMaintenanceServiceImpTest {

    @Mock
    private OrdersFeignClient ordersFeignClient;

    @Mock
    private OrderArchiveCoordinator orderArchiveCoordinator;

    private OrderErpMaintenanceServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderErpMaintenanceServiceImp(ordersFeignClient, orderArchiveCoordinator);
    }

    @Test
    void markExpiredOrdersUpdatesAndArchivesOrdersMarkedExpiredByErp() {
        OrderDto order = order("100", "1", 7);
        Map<String, OrderDto> orders = new HashMap<>();
        orders.put("100,1", order);

        OrdersFeignData expiredOrder = new OrdersFeignData();
        expiredOrder.setOrderNumber("100");
        expiredOrder.setRegel("1");
        expiredOrder.setStatus("Z");
        when(ordersFeignClient.sendAndReceiveMarkExpired("'100'"))
                .thenReturn(Collections.singletonList(expiredOrder));

        List<OrderDto> updatedOrders = new ArrayList<>();
        List<List<Integer>> archivedIds = new ArrayList<>();
        service.markExpiredOrders(orders, updatedOrders::add, archivedIds::add);

        assertSame(order, updatedOrders.get(0));
        assertEquals(Boolean.TRUE, order.getIsExpired());
        assertEquals(Collections.singletonList(Collections.singletonList(7)), archivedIds);
    }

    @Test
    void checkOrderExistenceDeletesOrdersMissingFromErpResponse() {
        Map<String, OrderDto> orders = new HashMap<>();
        orders.put("100,1", order("100", "1", 7));
        when(ordersFeignClient.sendAndReceiveOrderExistence("'100,1'"))
                .thenReturn(Collections.emptyList());
        AtomicReference<List<Integer>> deletedIds = new AtomicReference<>();

        service.checkOrderExistence(orders, deletedIds::set);

        assertEquals(Collections.singletonList(7), deletedIds.get());
        verify(ordersFeignClient).sendAndReceiveOrderExistence("'100,1'");
    }

    @Test
    void reconcileDeletedOrdersPreservesBackordersAndDeletesMissingOrders() {
        Map<String, OrderDto> orders = new LinkedHashMap<>();
        orders.put("100,1", order("100", "1", 1));
        orders.put("200,2", order("200", "2", 2));
        Map<String, OrderPreserves> preserves = new HashMap<>();
        OrdersFeignData backorder = new OrdersFeignData();
        backorder.setOrderNumber("100");
        backorder.setRegel("1");
        backorder.setStatus("B");
        when(ordersFeignClient.sendAndReceiveDeleteOrders(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Collections.singletonList(backorder));
        when(orderArchiveCoordinator.preserveBeforeDelete(
                eq(Collections.singleton("100")),
                same(orders),
                same(preserves)
        )).thenReturn(Collections.singletonList(1));

        service.reconcileDeletedOrders(Arrays.asList(1, 2), orders, preserves);

        verify(orderArchiveCoordinator).preserveBeforeDelete(
                eq(Collections.singleton("100")),
                same(orders),
                same(preserves)
        );
        verify(orderArchiveCoordinator).deleteOrderData(Arrays.asList(2, 1), orders);
    }

    private OrderDto order(String orderNumber, String regel, int id) {
        OrderDto order = new OrderDto();
        order.setOrderNumber(orderNumber);
        order.setRegel(regel);
        order.setId(id);
        return order;
    }
}
