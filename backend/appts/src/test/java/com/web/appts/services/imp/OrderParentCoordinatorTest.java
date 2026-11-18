package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OrderParentCoordinatorTest {
    @Mock
    private Consumer<OrderDto> orderUpdater;

    private OrderParentCoordinator coordinator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coordinator = new OrderParentCoordinator();
    }

    @Test
    void keepsLowestLineAsParentAndClearsOtherParentRows() {
        OrderDto firstLine = order(1, "100", "1", 1);
        OrderDto secondLine = order(2, "100", "2", 1);
        OrderDto thirdLine = order(3, "100", "3", 0);
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", firstLine);
        orders.put("100,2", secondLine);
        orders.put("100,3", thirdLine);

        coordinator.adjustParentOrders(orders, orderUpdater);

        assertEquals(1, firstLine.getIsParent());
        assertEquals(0, secondLine.getIsParent());
        verify(orderUpdater).accept(secondLine);
    }

    @Test
    void marksLowestLineWhenNoLineIsCurrentlyParent() {
        OrderDto firstLine = order(1, "100", "1", 0);
        OrderDto secondLine = order(2, "100", "2", 0);
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", firstLine);
        orders.put("100,2", secondLine);

        coordinator.adjustParentOrders(orders, orderUpdater);

        assertEquals(1, firstLine.getIsParent());
        verify(orderUpdater).accept(firstLine);
    }

    @Test
    void doesNotPersistWhenEachOrderAlreadyHasTheCorrectParentFlag() {
        OrderDto firstLine = order(1, "100", "1", 1);
        OrderDto secondLine = order(2, "100", "2", 0);
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", firstLine);
        orders.put("100,2", secondLine);

        coordinator.adjustParentOrders(orders, orderUpdater);

        verifyNoInteractions(orderUpdater);
    }

    private OrderDto order(int id, String orderNumber, String lineNumber, int isParent) {
        OrderDto order = new OrderDto();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setRegel(lineNumber);
        order.setIsParent(isParent);
        return order;
    }
}
