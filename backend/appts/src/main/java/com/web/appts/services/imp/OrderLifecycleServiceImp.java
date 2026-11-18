package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderLifecycleService;
import com.web.appts.services.OrderService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderLifecycleServiceImp implements OrderLifecycleService {
    private final OrderService orderService;

    public OrderLifecycleServiceImp(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void markExpired() {
        orderService.markExpired();
    }

    @Override
    public void checkOrderExistence() {
        orderService.checkOrderExistence();
    }

    @Override
    public void adjustParentOrders() {
        orderService.adjustParentOrders();
    }

    @Override
    public void verifyCrmOrders() {
        orderService.verifyCRMOrders();
    }

    @Override
    public Map<String, OrderDto> createMonSub() {
        return orderService.createMonSub();
    }

    @Override
    public void removingSameArchivedOrders() {
        orderService.removingSameArchivedOrders();
    }
}
