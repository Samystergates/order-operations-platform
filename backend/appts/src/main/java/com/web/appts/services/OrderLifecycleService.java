package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.util.Map;

public interface OrderLifecycleService {
    void markExpired();

    void checkOrderExistence();

    void adjustParentOrders();

    void verifyCrmOrders();

    Map<String, OrderDto> createMonSub();

    void removingSameArchivedOrders();
}
