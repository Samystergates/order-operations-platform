package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class OrderFlowUpdateApplier {
    private final OrderDepartmentTransitionUpdater departmentTransitionUpdater;
    private final OrderManualFlowUpdateApplier manualFlowUpdateApplier;

    public OrderFlowUpdateApplier(
            OrderDepartmentTransitionUpdater departmentTransitionUpdater,
            OrderManualFlowUpdateApplier manualFlowUpdateApplier
    ) {
        this.departmentTransitionUpdater = departmentTransitionUpdater;
        this.manualFlowUpdateApplier = manualFlowUpdateApplier;
    }

    public void apply(Order order, OrderDto orderDto) {
        String orderType = order.getOrderType();
        List<OrderDepartment> orderDepartments = order.getDepartments();
        List<OrderDepartment> dtoDepartments = orderDto.getDepartments();
        if (orderDepartments != null) {
            orderDepartments.sort(Comparator.comparingInt(OrderDepartment::getDepId));
        }

        if (orderDto.getCompleted() == null) {
            orderDto.setCompleted("");
        }
        if ("C".equals(orderDto.getCompleted())) {
            order.setCompleted("C");
        }

        if (orderType != null && orderType.equals(orderDto.getOrderType())) {
            for (OrderFlowField field : OrderTypeFlowRules.fieldsFor(orderType)) {
                departmentTransitionUpdater.applyField(order, orderDto, orderDepartments, dtoDepartments, field);
            }
        }

        manualFlowUpdateApplier.apply(order, orderDto, orderDepartments, dtoDepartments);
        order.setDepartments(orderDepartments);
        markCompletedWhenAllDepartmentsDone(order, orderDto);
    }

    private void markCompletedWhenAllDepartmentsDone(Order order, OrderDto orderDto) {
        if (orderDto.getDepartments() == null) {
            return;
        }

        boolean allStatusGOrEmpty = orderDto.getDepartments().stream()
                .allMatch(department -> "G".equals(department.getStatus()) || "".equals(department.getStatus()));
        if (allStatusGOrEmpty) {
            order.setCompleted("C");
            orderDto.setCompleted("C");
        }
    }
}
