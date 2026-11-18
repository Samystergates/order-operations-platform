package com.web.appts.services;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderPreserves;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface OrderMutationService {
    Boolean createOrder(OrderDto orderDto);

    void deleteOrderData(List<Integer> ids, Map<String, OrderDto> ordersMap);

    List<OrderDto> updateOrder(
            OrderDto orderDto,
            Integer orderId,
            Boolean flowUpdate,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    );

    void setupPreservedFlow(OrderDto orderDto, Map<String, OrderPreserves> ordersPreserveMap);

    void setupDefaultFlow(OrderDto orderDto);

    List<OrderDto> updateAllTekst(String orderNumOrIds, Map<String, OrderDto> ordersMap);
}
