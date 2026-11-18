package com.web.appts.services.imp;

import com.web.appts.DTO.CustomOrderDto;
import com.web.appts.DTO.DeleteCustOrderDto;
import com.web.appts.entities.CustomOrder;
import com.web.appts.entities.TransportOrderLines;
import com.web.appts.repositories.CustomOrderRepo;
import com.web.appts.repositories.TransportOrderLinesRepo;
import com.web.appts.utils.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCustomOrderServiceImpTest {

    @Mock
    private CustomOrderRepo customOrderRepo;

    @Mock
    private TransportOrderLinesRepo transportOrderLinesRepo;

    private OrderCustomOrderServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderCustomOrderServiceImp(
                customOrderRepo,
                transportOrderLinesRepo,
                new ModelMapper()
        );
    }

    @Test
    void createCustomOrderPersistsAndMapsResponse() {
        CustomOrder saved = new CustomOrder();
        saved.setId(12L);
        saved.setOrderNumber("C-12");
        saved.setCustomerName("Customer");
        when(customOrderRepo.save(org.mockito.ArgumentMatchers.any(CustomOrder.class))).thenReturn(saved);

        CustomOrderDto request = new CustomOrderDto();
        request.setOrderNumber("C-12");

        CustomOrderDto result = service.createCustomOrder(request);

        assertEquals(12L, result.getId());
        assertEquals("C-12", result.getOrderNumber());
        assertEquals("Customer", result.getCustomerName());
    }

    @Test
    void deleteCustomOrderRemovesRelatedTransportLineWhenDeleted() {
        CustomOrderDto request = new CustomOrderDto();
        request.setId(7L);

        TransportOrderLines line = new TransportOrderLines();
        line.setId(99L);
        line.setOrderId(7L);
        line.setOrderType(OrderType.CUSTOM);

        when(customOrderRepo.existsById(7L)).thenReturn(false);
        when(transportOrderLinesRepo.findByOrderIdAndOrderType(7L, OrderType.CUSTOM))
                .thenReturn(Optional.of(line));

        DeleteCustOrderDto result = service.deleteCustomOrder(request);

        assertTrue(result.getResult());
        assertSame(line, result.getTransportOrderLines());
        verify(transportOrderLinesRepo).deleteById(99L);
    }

    @Test
    void getAllCustomOrdersMapsRepositoryResults() {
        CustomOrder first = new CustomOrder();
        first.setId(1L);
        first.setOrderNumber("C-1");
        CustomOrder second = new CustomOrder();
        second.setId(2L);
        second.setOrderNumber("C-2");
        when(customOrderRepo.findAll()).thenReturn(Arrays.asList(first, second));

        List<CustomOrderDto> result = service.getAllCustomOrders();

        assertEquals(2, result.size());
        assertEquals("C-1", result.get(0).getOrderNumber());
        assertEquals("C-2", result.get(1).getOrderNumber());
    }
}
