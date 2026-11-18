package com.web.appts.services;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderPreserves;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface OrderErpQueryMapperService {
    List<OrderDto> getCRMOrdersInner(
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    );
}
