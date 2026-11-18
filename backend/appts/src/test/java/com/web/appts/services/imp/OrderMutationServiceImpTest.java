package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.repositories.OrderPreservesRepo;
import com.web.appts.repositories.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderMutationServiceImpTest {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderPreservesRepo orderPreservesRepo;
    @Mock
    private OrderArchiveCoordinator orderArchiveCoordinator;

    private OrderMutationServiceImp service;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        modelMapper = new ModelMapper();
        service = new OrderMutationServiceImp(
                modelMapper,
                orderRepo,
                orderPreservesRepo,
                orderArchiveCoordinator,
                flowUpdateApplier()
        );
    }

    @Test
    void setupDefaultFlowCreatesDepartmentsForOrderType() {
        OrderDto order = order("100", "1", "MLO", "500");

        service.setupDefaultFlow(order);

        assertEquals("R", order.getMonLb());
        assertEquals("R", order.getTra());
        assertEquals(2, order.getDepartments().size());
        assertEquals("MONLB", order.getDepartments().get(0).getDepName());
        assertEquals("TRA", order.getDepartments().get(1).getDepName());
    }

    @Test
    void setupPreservedFlowRestoresPreservedDepartmentsAndDeletesPreserveRow() {
        Map<String, OrderPreserves> preservesMap = new ConcurrentHashMap<>();
        OrderPreserves preserved = new OrderPreserves();
        preserved.setId(5L);
        preserved.setOrderNumber("100");
        preserved.setRegel("1");
        preserved.setSme("R");
        preserved.setTra("Y");
        preserved.setBackOrder("B");
        preservesMap.put("100,1", preserved);

        OrderDto order = order("100", "1", "LOS", "500");

        service.setupPreservedFlow(order, preservesMap);

        assertEquals("R", order.getSme());
        assertEquals("Y", order.getTra());
        assertEquals("B", order.getBackOrder());
        assertEquals(2, order.getDepartments().size());
        assertFalse(preservesMap.containsKey("100,1"));
        verify(orderPreservesRepo).deleteById(5L);
    }

    @Test
    void createOrderSavesMappedOrder() {
        OrderDto orderDto = order("100", "1", "LOS", "500");
        orderDto.setId(10);
        when(orderRepo.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Boolean created = service.createOrder(orderDto);

        assertEquals(Boolean.TRUE, created);
        verify(orderRepo).save(org.mockito.ArgumentMatchers.argThat(order -> {
            assertNotNull(order);
            return order.getId() == 10 && "100".equals(order.getOrderNumber());
        }));
    }

    private OrderDto order(String orderNumber, String regel, String orderType, String cdProdGrp) {
        OrderDto order = new OrderDto();
        order.setOrderNumber(orderNumber);
        order.setRegel(regel);
        order.setOrderType(orderType);
        order.setCdProdGrp(cdProdGrp);
        return order;
    }

    private OrderFlowUpdateApplier flowUpdateApplier() {
        OrderDepartmentTransitionUpdater transitionUpdater = new OrderDepartmentTransitionUpdater();
        return new OrderFlowUpdateApplier(
                transitionUpdater,
                new OrderManualFlowUpdateApplier(modelMapper, transitionUpdater)
        );
    }
}
