package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.MonSubOrders;
import com.web.appts.entities.Order;
import com.web.appts.repositories.MonSubOrdersRepo;
import com.web.appts.repositories.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderMonSubOrderSyncServiceImpTest {

    @Mock
    private OrdersFeignClient ordersFeignClient;
    @Mock
    private MonSubOrdersRepo monSubOrdersRepo;
    @Mock
    private OrderRepo orderRepo;

    private OrderMonSubOrderSyncServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderMonSubOrderSyncServiceImp(
                ordersFeignClient,
                monSubOrdersRepo,
                orderRepo
        );
    }

    @Test
    void createsAndCachesMonSubOrderReturnedByErp() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(8);
        orderDto.setOrderNumber("100");
        orderDto.setRegel("1");
        orderDto.setOrderType("MLE");
        Map<String, OrderDto> orders = new HashMap<>();
        orders.put("100,1", orderDto);

        OrdersFeignData monSubOrderData = new OrdersFeignData();
        monSubOrderData.setOrderNumber("100");
        monSubOrderData.setRegel("1");
        monSubOrderData.setProduct("M");
        monSubOrderData.setDescription("MON component");
        when(ordersFeignClient.sendAndReceiveMonSub("'100'"))
                .thenReturn(Collections.singletonList(monSubOrderData));
        when(monSubOrdersRepo.findByOrderNumberAndRegelAndProduct("100", "1", "M"))
                .thenReturn(Optional.empty());

        Order order = new Order();
        order.setId(8);
        order.setAantal("3");
        when(orderRepo.findById(8)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenReturn(order);

        service.createMonSubOrders(
                orders,
                Collections::emptyList,
                ignored -> { },
                savedOrder -> {
                    OrderDto updatedOrder = new OrderDto();
                    updatedOrder.setOrderNumber(savedOrder.getOrderNumber());
                    updatedOrder.setRegel("1");
                    return updatedOrder;
                }
        );

        assertEquals(1, order.getMonSubOrders().size());
        MonSubOrders createdSubOrder = order.getMonSubOrders().get(0);
        assertEquals("M", createdSubOrder.getProduct());
        assertEquals("3", createdSubOrder.getAantal());
        assertEquals("MON component", createdSubOrder.getOmsumin());
        assertNotNull(orders.get("100,1"));
        verify(orderRepo).save(order);
    }
}
