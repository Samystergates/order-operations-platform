package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface OrderWorkflowService {
    List<OrderDto> updateOrderColors(
            String orderNumber,
            String orderDep,
            String orderStatus,
            String flowVal,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    );
}
