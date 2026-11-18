package com.web.appts.services.imp;

import com.web.appts.DTO.ArchivedOrdersDto;
import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.entities.OrderSME;
import com.web.appts.entities.OrderSPU;
import com.web.appts.repositories.OrderPreservesRepo;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.repositories.OrderSMERepo;
import com.web.appts.repositories.OrderSPURepo;
import com.web.appts.services.ArchivedOrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderArchiveCoordinatorTest {

    @Mock
    private ArchivedOrdersService archivedOrdersService;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderPreservesRepo orderPreservesRepo;

    @Mock
    private OrderSMERepo orderSMERepo;

    @Mock
    private OrderSPURepo orderSPURepo;

    private OrderArchiveCoordinator coordinator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coordinator = new OrderArchiveCoordinator(
                archivedOrdersService,
                orderRepo,
                orderPreservesRepo,
                orderSMERepo,
                orderSPURepo
        );
    }

    @Test
    void preserveBeforeDeletePersistsMatchingOrdersAndReturnsIds() {
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        Map<String, OrderPreserves> preserves = new ConcurrentHashMap<>();
        OrderDto matchingOrder = orderDto(10, "100", "1");
        matchingOrder.setSme("R");
        matchingOrder.setSpu("Y");
        orders.put("100,1", matchingOrder);
        orders.put("200,1", orderDto(20, "200", "1"));

        List<Integer> result = coordinator.preserveBeforeDelete(
                Collections.singleton("100"),
                orders,
                preserves
        );

        assertEquals(Collections.singletonList(10), result);
        assertTrue(preserves.containsKey("100,1"));

        ArgumentCaptor<OrderPreserves> preserveCaptor = ArgumentCaptor.forClass(OrderPreserves.class);
        verify(orderPreservesRepo).save(preserveCaptor.capture());
        assertEquals("100", preserveCaptor.getValue().getOrderNumber());
        assertEquals("1", preserveCaptor.getValue().getRegel());
        assertEquals("R", preserveCaptor.getValue().getSme());
        assertEquals("Y", preserveCaptor.getValue().getSpu());
    }

    @Test
    void moveToArchiveRenamesWheelOrdersArchivesAndDeletesMainRows() {
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        Map<String, OrderPreserves> preserves = new ConcurrentHashMap<>();
        orders.put("100,1", orderDto(10, "100", "1"));

        OrderSME sme = new OrderSME();
        OrderSPU spu = new OrderSPU();
        when(orderSMERepo.findByOrderNumberAndRegel("100", "1")).thenReturn(sme);
        when(orderSPURepo.findByOrderNumberAndRegel("100", "1")).thenReturn(spu);
        when(archivedOrdersService.createArchivedOrder(any(OrderDto.class))).thenReturn(true);

        coordinator.moveToArchive(Collections.singletonList(10), orders, preserves);

        assertTrue(orders.isEmpty());
        assertEquals("X100", sme.getOrderNumber());
        assertEquals("X100", spu.getOrderNumber());
        verify(orderSMERepo).save(sme);
        verify(orderSPURepo).save(spu);
        verify(orderRepo).deleteMonSubsForIds(Collections.singletonList(10));
        verify(orderRepo).deleteODForIds(Collections.singletonList(10));
        verify(orderRepo).deleteOrdersByIds(Collections.singletonList(10));
    }

    @Test
    void moveToArchiveDoesNotRenameWheelOrdersWhenOrderWasPreserved() {
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        Map<String, OrderPreserves> preserves = new ConcurrentHashMap<>();
        orders.put("100,1", orderDto(10, "100", "1"));
        preserves.put("100,1", new OrderPreserves());
        when(archivedOrdersService.createArchivedOrder(any(OrderDto.class))).thenReturn(true);

        coordinator.moveToArchive(Collections.singletonList(10), orders, preserves);

        verify(orderSMERepo, never()).findByOrderNumberAndRegel("100", "1");
        verify(orderSPURepo, never()).findByOrderNumberAndRegel("100", "1");
    }

    @Test
    void removeSameArchivedOrdersDeletesActiveOrdersAlreadyInArchive() {
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", orderDto(10, "100", "1"));
        orders.put("200,1", orderDto(20, "200", "1"));

        ArchivedOrdersDto archivedOrder = new ArchivedOrdersDto();
        archivedOrder.setOrderNumber("100");
        when(archivedOrdersService.getAllArchivedOrders()).thenReturn(Collections.singletonList(archivedOrder));

        coordinator.removeSameArchivedOrders(orders);

        verify(orderRepo).deleteMonSubsForIds(Collections.singletonList(10));
        verify(orderRepo).deleteODForIds(Collections.singletonList(10));
        verify(orderRepo).deleteOrdersByIds(Collections.singletonList(10));
    }

    @Test
    void deleteOrderDataSkipsEmptyIds() {
        coordinator.deleteOrderData(Collections.emptyList(), new ConcurrentHashMap<>());

        verify(orderRepo, never()).deleteMonSubsForIds(any());
        verify(orderRepo, never()).deleteODForIds(any());
        verify(orderRepo, never()).deleteOrdersByIds(any());
    }

    private OrderDto orderDto(int id, String orderNumber, String regel) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(id);
        orderDto.setOrderNumber(orderNumber);
        orderDto.setRegel(regel);
        return orderDto;
    }
}
