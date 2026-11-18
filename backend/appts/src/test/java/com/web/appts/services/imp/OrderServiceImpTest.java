package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.repositories.OrderPreservesRepo;
import com.web.appts.services.OrderErpConnectionRecoveryService;
import com.web.appts.services.OrderErpMaintenanceService;
import com.web.appts.services.OrderMonSubOrderSyncService;
import com.web.appts.services.OrderErpSyncService;
import org.modelmapper.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceImpTest {

    @Mock
    private OrderPreservesRepo orderPreservesRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private OrderErpSyncService orderErpSyncService;

    @Mock
    private OrderErpConnectionRecoveryService orderErpConnectionRecoveryService;

    @Mock
    private OrderErpMaintenanceService orderErpMaintenanceService;

    @Mock
    private OrderMonSubOrderSyncService orderMonSubOrderSyncService;

    @InjectMocks
    private OrderServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadsPreservedOrdersWhenCacheIsEmpty() {
        OrderPreserves preservedOrder = new OrderPreserves();
        preservedOrder.setOrderNumber("100");
        preservedOrder.setRegel("1");
        when(orderPreservesRepo.findAll()).thenReturn(Collections.singletonList(preservedOrder));

        service.getAllPreservedOrders();

        assertSame(preservedOrder, service.ordersPreserveMap.get("100,1"));
        verify(orderPreservesRepo).findAll();
    }

    @Test
    void verifyCrmOrdersUsesSupportedErpRefresh() {
        service.verifyCRMOrders();

        verify(orderErpSyncService).getCRMOrders(any(), any(), any());
    }

    @Test
    void loadsDatabaseOrdersIntoTheSharedCache() {
        Order order = new Order();
        order.setDepartments(new ArrayList<>());
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderNumber("100");
        orderDto.setRegel("1");
        when(orderRepo.findAll()).thenReturn(Collections.singletonList(order));
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        List<OrderDto> loadedOrders = service.getAllOrders();

        assertSame(orderDto, loadedOrders.get(0));
        assertSame(orderDto, service.getMap().get("100,1"));
        verify(orderRepo).findAll();
        verifyNoInteractions(orderErpSyncService);
    }

    @Test
    void checkMapReloadsDatabaseOrdersWhenCacheCountDiffers() {
        OrderDto staleOrder = new OrderDto();
        staleOrder.setOrderNumber("old");
        staleOrder.setRegel("1");
        staleOrder.setDepartments(new ArrayList<>());
        service.ordersMap.put("old,1", staleOrder);

        Order firstOrder = new Order();
        firstOrder.setId(1);
        firstOrder.setDepartments(new ArrayList<>());
        OrderDto firstDto = new OrderDto();
        firstDto.setOrderNumber("100");
        firstDto.setRegel("1");
        firstDto.setDepartments(new ArrayList<>());
        Order secondOrder = new Order();
        secondOrder.setId(2);
        secondOrder.setDepartments(new ArrayList<>());
        OrderDto secondDto = new OrderDto();
        secondDto.setOrderNumber("200");
        secondDto.setRegel("1");
        secondDto.setDepartments(new ArrayList<>());
        when(orderRepo.count()).thenReturn(2L);
        when(orderRepo.findAll()).thenReturn(Arrays.asList(firstOrder, secondOrder));
        when(modelMapper.map(firstOrder, OrderDto.class)).thenReturn(firstDto);
        when(modelMapper.map(secondOrder, OrderDto.class)).thenReturn(secondDto);

        List<OrderDto> reloadedOrders = service.checkMap();

        assertEquals(2, reloadedOrders.size());
        assertFalse(service.ordersMap.containsKey("old,1"));
        assertSame(firstDto, service.ordersMap.get("100,1"));
        assertSame(secondDto, service.ordersMap.get("200,1"));
    }

    @Test
    void checkMapFallsBackToCrmWhenDatabaseIsEmpty() {
        OrderDto crmOrder = new OrderDto();
        when(orderRepo.findAll()).thenReturn(Collections.emptyList());
        when(orderErpSyncService.getCRMOrders(any(), any(), any()))
                .thenReturn(Collections.singletonList(crmOrder));

        assertSame(crmOrder, service.checkMap().get(0));

        verify(orderErpSyncService).getCRMOrders(any(), any(), any());
    }
}
