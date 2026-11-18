package com.web.appts.services;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderPreserves;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface OrderErpMaintenanceService {
    void markExpiredOrders(
            Map<String, OrderDto> ordersMap,
            Consumer<OrderDto> orderUpdater,
            Consumer<List<Integer>> archiveOrders
    );

    void checkOrderExistence(
            Map<String, OrderDto> ordersMap,
            Consumer<List<Integer>> deleteOrderIds
    );

    void reconcileDeletedOrders(
            List<Integer> orderIdsToDelete,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    );
}
