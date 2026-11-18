package com.web.appts.services;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrderMonSubOrderSyncService {
    void createMonSubOrders(
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> allOrdersLoader,
            Consumer<List<OrderDto>> orderDtoListUpdater,
            Function<Order, OrderDto> orderMapper
    );
}
