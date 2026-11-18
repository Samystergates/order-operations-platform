package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderService;
import com.web.appts.services.OrderTextSyncService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderTextSyncServiceImp implements OrderTextSyncService {
    private final OrderService orderService;

    public OrderTextSyncServiceImp(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void updateProductNotes() {
        orderService.updateProductNotes();
    }

    @Override
    public List<OrderDto> updateAllText(String orderNumOrIds) {
        return orderService.updateAllTekst(orderNumOrIds);
    }
}
