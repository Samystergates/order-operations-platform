package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.services.OrderErpMaintenanceService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class OrderErpMaintenanceServiceImp implements OrderErpMaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(OrderErpMaintenanceServiceImp.class);

    private final OrdersFeignClient ordersFeignClient;
    private final OrderArchiveCoordinator orderArchiveCoordinator;

    public OrderErpMaintenanceServiceImp(
            OrdersFeignClient ordersFeignClient,
            OrderArchiveCoordinator orderArchiveCoordinator
    ) {
        this.ordersFeignClient = ordersFeignClient;
        this.orderArchiveCoordinator = orderArchiveCoordinator;
    }

    @Override
    public void markExpiredOrders(
            Map<String, OrderDto> ordersMap,
            Consumer<OrderDto> orderUpdater,
            Consumer<List<Integer>> archiveOrders
    ) {
        List<String> formattedOrders = new ArrayList<>();
        for (OrderDto order : ordersMap.values()) {
            formattedOrders.add("'" + order.getOrderNumber() + "'");
        }

        if (formattedOrders.isEmpty()) {
            logger.info("No orders to check for expiration. markExpiredInner");
            return;
        }

        String orderNumbers = String.join(",", formattedOrders);
        try {
            List<OrdersFeignData> orderData = ordersFeignClient.sendAndReceiveMarkExpired(orderNumbers);

            if (orderData != null) {
                List<String> existingOrderNumbers = new ArrayList<>();
                for (OrdersFeignData data : orderData) {
                    if (data == null || data.getOrderNumber() == null) {
                        continue;
                    }

                    if (data.getStatus().equals("Z")) {
                        existingOrderNumbers.add(data.getOrderNumber() + "," + data.getRegel());
                    }
                }
                if (existingOrderNumbers.isEmpty()) {
                    return;
                }

                List<OrderDto> orderList = ordersMap.values()
                        .stream()
                        .filter(order -> existingOrderNumbers.contains(order.getOrderNumber() + "," + order.getRegel()))
                        .collect(Collectors.toList());
                List<Integer> filteredOrders = orderList
                        .stream()
                        .map(OrderDto::getId)
                        .collect(Collectors.toList());

                List<OrderDto> matchingObjects = new ArrayList<>();
                if (!filteredOrders.isEmpty()) {
                    matchingObjects = ordersMap.values().stream()
                            .filter(order -> filteredOrders.contains(order.getId()))
                            .peek(order -> order.setIsExpired(true))
                            .collect(Collectors.toList());
                }

                matchingObjects.forEach(order -> {
                    try {
                        orderUpdater.accept(order);
                    } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
                        logger.warn("Could not mark order {} expired due to a concurrent update", order.getId(), e);
                    } catch (DataIntegrityViolationException e) {
                        Throwable rootCause = e.getRootCause();
                        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                            logger.warn("Could not mark order {} expired due to a database constraint violation", order.getId(), e);
                        } else {
                            logger.error("Could not mark order {} expired", order.getId(), e);
                        }
                    }
                });
                archiveOrders.accept(filteredOrders);
            }
        } catch (Exception e) {
            logger.error("Failed to update expired orders from ERP data", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkOrderExistence(
            Map<String, OrderDto> ordersMap,
            Consumer<List<Integer>> deleteOrderIds
    ) {
        String orderNumbers = ordersMap.keySet().stream()
                .map(order -> "'" + order + "'")
                .collect(Collectors.joining(","));
        List<Integer> orderIdsToDelete = new ArrayList<>();
        List<OrdersFeignData> orderData = ordersFeignClient.sendAndReceiveOrderExistence(orderNumbers);

        try {
            if (orderData != null) {
                Set<String> foundOrders = new HashSet<>();
                for (OrdersFeignData data : orderData) {
                    foundOrders.add(data.getOrderNumber() + "," + data.getRegel());
                }

                for (Map.Entry<String, OrderDto> entry : ordersMap.entrySet()) {
                    if (!foundOrders.contains(entry.getKey())) {
                        logger.warn("Order {} not found in the system", entry.getKey());
                        orderIdsToDelete.add(entry.getValue().getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check whether ERP orders still exist", e);
            throw new RuntimeException(e);
        }

        if (!orderIdsToDelete.isEmpty()) {
            logger.warn("OrderIds {} not found in the system", orderIdsToDelete);
            deleteOrderIds.accept(orderIdsToDelete);
        }
    }

    @Override
    public void reconcileDeletedOrders(
            List<Integer> orderIdsToDelete,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    ) {
        List<Integer> filteredIds = new ArrayList<>();
        Set<String> filteredIdsBasedOnStatus = new HashSet<>();

        List<OrdersFeignData> dataList = new ArrayList<>();
        ordersMap.values().stream()
                .filter(order -> orderIdsToDelete.contains(order.getId()))
                .forEach(order -> {
                    OrdersFeignData data = new OrdersFeignData();
                    data.setOrderNumber(order.getOrderNumber());
                    data.setRegel(order.getRegel());
                    dataList.add(data);
                });

        List<OrdersFeignData> returnedData = ordersFeignClient.sendAndReceiveDeleteOrders(dataList);
        if (returnedData != null) {
            logger.debug("Received {} ERP order-deletion checks", returnedData.size());
            Map<String, String> returnedKeys = returnedData.stream()
                    .collect(Collectors.toMap(
                            data -> data.getOrderNumber() + "," + data.getRegel(),
                            OrdersFeignData::getStatus
                    ));

            ordersMap.values().forEach(order -> {
                String orderKey = order.getOrderNumber() + "," + order.getRegel();
                boolean recordExists = returnedKeys.containsKey(orderKey);

                if (recordExists) {
                    logger.info("Found matching order {} with regel {}",
                            order.getOrderNumber(), order.getRegel());
                    if (returnedKeys.get(orderKey).equals("B")) {
                        filteredIdsBasedOnStatus.add(order.getOrderNumber());
                    }
                } else {
                    logger.warn("No matching order found for {} with regel {}", order.getOrderNumber(), order.getRegel());
                    filteredIds.add(order.getId());
                }
            });
        }

        logger.info("{} orders require preservation before deletion", filteredIdsBasedOnStatus.size());
        if (!filteredIds.isEmpty() || !filteredIdsBasedOnStatus.isEmpty()) {
            logger.warn("OrderIds {} really not found in the system", filteredIds);
            List<Integer> preservedIds = orderArchiveCoordinator.preserveBeforeDelete(
                    filteredIdsBasedOnStatus,
                    ordersMap,
                    ordersPreserveMap
            );
            filteredIds.addAll(preservedIds);
            orderArchiveCoordinator.deleteOrderData(filteredIds, ordersMap);
        }
    }
}
