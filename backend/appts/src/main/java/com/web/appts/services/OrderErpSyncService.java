package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.util.List;
import java.util.function.Supplier;

public interface OrderErpSyncService {
    List<OrderDto> getCRMOrders(
            Supplier<List<OrderDto>> crmOrderLoader,
            Runnable recoverConnection,
            Runnable failFast
    );

    <T> T execute(
            String operationName,
            Supplier<T> operation,
            Runnable recoverConnection,
            Runnable failFast
    );
}
