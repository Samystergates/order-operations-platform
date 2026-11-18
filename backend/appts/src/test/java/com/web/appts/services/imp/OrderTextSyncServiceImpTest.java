package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderTextSyncServiceImpTest {

    @Mock
    private OrderService orderService;

    private OrderTextSyncServiceImp service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderTextSyncServiceImp(orderService);
    }

    @Test
    void delegatesTextSyncOperations() {
        List<OrderDto> updatedOrders = new ArrayList<>();
        when(orderService.updateAllTekst("A1")).thenReturn(updatedOrders);

        service.updateProductNotes();
        assertSame(updatedOrders, service.updateAllText("A1"));

        verify(orderService).updateProductNotes();
        verify(orderService).updateAllTekst("A1");
    }
}
