package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class OrderParentCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(OrderParentCoordinator.class);

    public void adjustParentOrders(
            Map<String, OrderDto> ordersMap,
            Consumer<OrderDto> orderUpdater
    ) {
        Map<String, List<OrderDto>> groupedOrders = ordersMap.values().stream()
                .collect(Collectors.groupingBy(OrderDto::getOrderNumber));

        for (Map.Entry<String, List<OrderDto>> entry : groupedOrders.entrySet()) {
            List<OrderDto> orderDtos = entry.getValue();
            OrderDto parentOrder = orderDtos.stream()
                    .min(Comparator.comparingInt(order -> Integer.parseInt(order.getRegel())))
                    .orElseThrow(() -> new IllegalStateException(
                            "No orders found for ordernumber: " + entry.getKey()
                    ));

            List<OrderDto> ordersToUpdate = new ArrayList<>();
            for (OrderDto orderDto : orderDtos) {
                if (Integer.parseInt(orderDto.getRegel()) != Integer.parseInt(parentOrder.getRegel())
                        && orderDto.getIsParent() == 1) {
                    orderDto.setIsParent(0);
                    ordersToUpdate.add(orderDto);
                }
            }
            if (parentOrder.getIsParent() != 1) {
                parentOrder.setIsParent(1);
                ordersToUpdate.add(parentOrder);
            }

            for (OrderDto order : ordersToUpdate) {
                try {
                    orderUpdater.accept(order);
                } catch (ObjectOptimisticLockingFailureException | StaleStateException exception) {
                    logger.warn("Could not update parent status for order id {}", order.getId(), exception);
                } catch (DataIntegrityViolationException exception) {
                    Throwable rootCause = exception.getRootCause();
                    if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                        logger.warn(
                                "SQL constraint prevented parent status update for order id {}: {}",
                                order.getId(),
                                rootCause.getMessage(),
                                exception
                        );
                    } else {
                        logger.warn("Data integrity error updating parent status for order id {}", order.getId(), exception);
                    }
                }
            }
        }
    }
}
