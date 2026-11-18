package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@Component
class OrderDepartmentTransitionUpdater {
    private static final Logger logger = LoggerFactory.getLogger(OrderDepartmentTransitionUpdater.class);

    void applyField(Order order, OrderDto orderDto, List<OrderDepartment> orderDepartments, List<OrderDepartment> dtoDepartments, OrderFlowField field) {
        String newStatus = field.valueFrom(orderDto);
        if (newStatus != null) {
            field.applyTo(order, newStatus);
        }
        syncDepartmentStatus(order, orderDto, orderDepartments, dtoDepartments, field, newStatus);
    }

    void syncDepartmentStatus(
            Order order,
            OrderDto orderDto,
            List<OrderDepartment> orderDepartments,
            List<OrderDepartment> dtoDepartments,
            OrderFlowField field,
            String newStatus
    ) {
        if (orderDepartments == null || dtoDepartments == null) {
            return;
        }

        OptionalInt index = IntStream.range(0, orderDepartments.size())
                .filter(i -> orderDepartments.get(i).getDepId() == field.departmentId())
                .findFirst();
        if (index.isEmpty()) {
            return;
        }

        int departmentIndex = index.getAsInt();
        if (departmentIndex >= dtoDepartments.size()) {
            logger.warn(
                    "DTO departments missing index {} for order {} and regel {}",
                    departmentIndex,
                    order.getOrderNumber(),
                    order.getRegel()
            );
            return;
        }

        OrderDepartment currentDepartment = orderDepartments.get(departmentIndex);
        OrderDepartment dtoDepartment = dtoDepartments.get(departmentIndex);
        if (currentDepartment == null || dtoDepartment == null) {
            logger.warn(
                    "Department is null at index {} for order {} and regel {}",
                    departmentIndex,
                    order.getOrderNumber(),
                    order.getRegel()
            );
            return;
        }

        if (currentDepartment.getPrevStatus() == null) {
            currentDepartment.setPrevStatus("n");
        }
        if (dtoDepartment.getPrevStatus() == null) {
            dtoDepartment.setPrevStatus("n");
        }
        if (!currentDepartment.getPrevStatus().equals(dtoDepartment.getPrevStatus())) {
            currentDepartment.setPrevStatus(currentDepartment.getStatus());
        }

        if (currentDepartment.getStatus() == null) {
            logger.warn(
                    "{} status is null for order {} and regel {}",
                    field.departmentName(),
                    order.getOrderNumber(),
                    order.getRegel()
            );
            currentDepartment.setStatus("");
        }

        if (newStatus == null) {
            logger.warn(
                    "DTO {} status is null for order {} and regel {}",
                    field.departmentName(),
                    orderDto.getOrderNumber(),
                    orderDto.getRegel()
            );
            return;
        }

        if (!currentDepartment.getStatus().equals(newStatus)) {
            currentDepartment.setStatus(newStatus);
        }
    }
}
