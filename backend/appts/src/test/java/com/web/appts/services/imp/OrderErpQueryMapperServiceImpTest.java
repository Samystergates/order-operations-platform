package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrderResultSetVals;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.repositories.OrderSMERepo;
import com.web.appts.repositories.OrderSPURepo;
import com.web.appts.services.ArchivedOrdersService;
import com.web.appts.services.OrderMutationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderErpQueryMapperServiceImpTest {

    @Mock
    private OrdersFeignClient ordersFeignClient;
    @Mock
    private ArchivedOrdersService archivedOrdersService;
    @Mock
    private OrderSMERepo orderSMERepo;
    @Mock
    private OrderSPURepo orderSPURepo;
    @Mock
    private OrderMutationService orderMutationService;

    private OrderErpQueryMapperServiceImp service;
    private RecordingMutationCalls mutationCalls;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderErpQueryMapperServiceImp(
                ordersFeignClient,
                archivedOrdersService,
                orderSMERepo,
                orderSPURepo,
                orderMutationService
        );
        mutationCalls = new RecordingMutationCalls();
        when(archivedOrdersService.getAllArchivedOrders()).thenReturn(Collections.emptyList());
        when(orderMutationService.createOrder(org.mockito.ArgumentMatchers.any(OrderDto.class))).thenAnswer(invocation -> {
            mutationCalls.createdOrders.add(invocation.getArgument(0));
            return true;
        });
        when(orderMutationService.updateOrder(
                org.mockito.ArgumentMatchers.any(OrderDto.class),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyMap(),
                org.mockito.ArgumentMatchers.any()
        )).thenAnswer(invocation -> {
            mutationCalls.updatedOrders.add(invocation.getArgument(0));
            mutationCalls.updateFlowFlags.add(invocation.getArgument(2));
            return Collections.singletonList(invocation.getArgument(0));
        });
    }

    @Test
    void getCRMOrdersInnerCreatesMissingOrdersFromErpRows() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderResultSetVals erpRow = erpRow("100", "1");
        when(ordersFeignClient.sendAndReceiveOrders()).thenReturn(Collections.singletonList(erpRow));

        service.getCRMOrdersInner(ordersMap, new ConcurrentHashMap<>(), Collections::emptyList);

        assertEquals(1, mutationCalls.createdOrders.size());
        assertEquals("100", mutationCalls.createdOrders.get(0).getOrderNumber());
        assertEquals("1", mutationCalls.createdOrders.get(0).getRegel());
        verify(orderMutationService).setupDefaultFlow(org.mockito.ArgumentMatchers.any(OrderDto.class));
        assertTrue(ordersMap.isEmpty());
    }

    @Test
    void getCRMOrdersInnerUpdatesExistingOrdersWhenErpFieldsChange() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto existingOrder = existingOrder("100", "1");
        ordersMap.put("100,1", existingOrder);

        OrderResultSetVals erpRow = erpRow("100", "1");
        erpRow.setProduct("P2");
        when(ordersFeignClient.sendAndReceiveOrders()).thenReturn(Collections.singletonList(erpRow));

        service.getCRMOrdersInner(ordersMap, new ConcurrentHashMap<>(), Collections::emptyList);

        assertEquals(1, mutationCalls.updatedOrders.size());
        assertEquals("P2", mutationCalls.updatedOrders.get(0).getProduct());
        assertEquals(Boolean.FALSE, mutationCalls.updateFlowFlags.get(0));
        verify(orderSMERepo, never()).findByOrderNumberAndRegel("100", "1");
        verify(orderSPURepo, never()).findByOrderNumberAndRegel("100", "1");
    }

    @Test
    void getCRMOrdersInnerRebuildsFlowWhenProductGroupChanges() {
        Map<String, OrderDto> ordersMap = new ConcurrentHashMap<>();
        OrderDto existingOrder = existingOrder("100", "1");
        existingOrder.setId(7);
        ordersMap.put("100,1", existingOrder);

        OrderResultSetVals erpRow = erpRow("100", "1");
        erpRow.setCdProdGrp("OTHER");
        when(ordersFeignClient.sendAndReceiveOrders()).thenReturn(Collections.singletonList(erpRow));

        service.getCRMOrdersInner(ordersMap, new ConcurrentHashMap<>(), Collections::emptyList);

        verify(orderMutationService).deleteOrderData(Collections.singletonList(7), ordersMap);
        verify(orderMutationService).setupDefaultFlow(org.mockito.ArgumentMatchers.any(OrderDto.class));
        assertEquals(1, mutationCalls.createdOrders.size());
    }

    @Test
    void getCRMOrdersInnerRethrowsInvalidOdbcBufferErrorForRetryCoordinator() {
        when(ordersFeignClient.sendAndReceiveOrders())
                .thenThrow(new RuntimeException("[Microsoft][ODBC Driver Manager] Invalid string or buffer length"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.getCRMOrdersInner(
                        new ConcurrentHashMap<>(),
                        new ConcurrentHashMap<>(),
                        Collections::emptyList
                )
        );

        assertEquals("[Microsoft][ODBC Driver Manager] Invalid string or buffer length", exception.getMessage());
    }

    @Test
    void getCRMOrdersInnerFallsBackToActiveOrdersWhenMappingFails() {
        OrderDto cachedOrder = existingOrder("100", "1");
        List<OrderDto> activeOrders = Collections.singletonList(cachedOrder);
        when(ordersFeignClient.sendAndReceiveOrders())
                .thenReturn(Collections.singletonList(erpRow("100", "1")));
        when(orderMutationService.createOrder(org.mockito.ArgumentMatchers.any(OrderDto.class)))
                .thenThrow(new IllegalStateException("local persistence failed"));

        List<OrderDto> result = service.getCRMOrdersInner(
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                () -> activeOrders
        );

        assertEquals(activeOrders, result);
    }

    private OrderResultSetVals erpRow(String orderNumber, String regel) {
        OrderResultSetVals row = new OrderResultSetVals();
        row.setOrderNumber(orderNumber);
        row.setRegel(regel);
        row.setOrderType("TYPE");
        row.setBackOrder("N");
        row.setUser("user");
        row.setOrganization("ORG");
        row.setCustomerName("Customer");
        row.setStreet("Street");
        row.setHouseNR("1");
        row.setAdditionalAdd("");
        row.setPostCode("1000");
        row.setCity("City");
        row.setCountry("Country");
        row.setDeliveryDate("2026-01-01");
        row.setReferenceInfo("REF");
        row.setCreationDate("2026-01-01");
        row.setModificationDate("2026-01-02");
        row.setVerifierUser("verifier");
        row.setAantal("2");
        row.setProduct("P1");
        row.setOmsumin("Description");
        row.setCdProdGrp("GROUP");
        row.setZoeknaam("Search");
        row.setGel("0");
        return row;
    }

    private OrderDto existingOrder(String orderNumber, String regel) {
        OrderDto order = new OrderDto();
        order.setId(1);
        order.setOrderNumber(orderNumber);
        order.setRegel(regel);
        order.setOrderType("TYPE");
        order.setBackOrder("N");
        order.setOrganization("ORG");
        order.setCustomerName("Customer");
        order.setCity("City");
        order.setCountry("Country");
        order.setDeliveryDate("2026-01-01");
        order.setReferenceInfo("REF");
        order.setAantal("2");
        order.setProduct("P1");
        order.setCdProdGrp("GROUP");
        order.setSme("");
        order.setSpu("");
        return order;
    }

    private static class RecordingMutationCalls {
        private final List<OrderDto> createdOrders = new ArrayList<>();
        private final List<OrderDto> updatedOrders = new ArrayList<>();
        private final List<Boolean> updateFlowFlags = new ArrayList<>();
    }
}
