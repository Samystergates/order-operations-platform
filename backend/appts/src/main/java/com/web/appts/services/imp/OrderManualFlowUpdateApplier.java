package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class OrderManualFlowUpdateApplier {
    private final ModelMapper modelMapper;
    private final OrderDepartmentTransitionUpdater departmentTransitionUpdater;

    OrderManualFlowUpdateApplier(ModelMapper modelMapper, OrderDepartmentTransitionUpdater departmentTransitionUpdater) {
        this.modelMapper = modelMapper;
        this.departmentTransitionUpdater = departmentTransitionUpdater;
    }

    void apply(Order order, OrderDto orderDto, List<OrderDepartment> orderDepartments, List<OrderDepartment> dtoDepartments) {
        applyExclamation(order, orderDto);
        applyBackOrder(order, orderDto);
        applySme(order, orderDto, orderDepartments, dtoDepartments);
        applySpu(order, orderDto, orderDepartments, dtoDepartments);
    }

    private void applyExclamation(Order order, OrderDto orderDto) {
        if (order.getExclamation() == null) {
            order.setExclamation("");
        }
        if (orderDto.getExclamation() == null) {
            orderDto.setExclamation("");
        }
        if (order.getExcNote() == null) {
            order.setExcNote("");
        }
        if (orderDto.getExcNote() == null) {
            orderDto.setExcNote("");
        }

        if ("JA".equals(order.getExclamation()) && "NEE".equals(orderDto.getExclamation())) {
            order.setExclamation("NEE");
            order.setExcNote("");
        }

        if (("NEE".equals(order.getExclamation()) || "".equals(order.getExclamation())) && "JA".equals(orderDto.getExclamation())) {
            order.setExclamation("JA");
            order.setExcNote(orderDto.getExcNote());
        }
    }

    private void applyBackOrder(Order order, OrderDto orderDto) {
        if (orderDto.getBackOrder() == null) {
            orderDto.setBackOrder("");
        }

        if (!orderDto.getBackOrder().isEmpty() && !"O".equals(orderDto.getBackOrder())) {
            order.setBackOrder(orderDto.getBackOrder());
            return;
        }

        String currentBackOrder = order.getBackOrder();
        if (currentBackOrder != null && !currentBackOrder.isEmpty() && !"O".equals(currentBackOrder)) {
            order.setBackOrder("O");
        }
    }

    private void applySme(Order order, OrderDto orderDto, List<OrderDepartment> orderDepartments, List<OrderDepartment> dtoDepartments) {
        if (orderDto.getSme() == null || orderDepartments == null) {
            return;
        }

        order.setSme(orderDto.getSme());
        if (orderDepartments.stream().noneMatch(dep -> dep.getDepId() == OrderFlowField.SME.departmentId())) {
            int insertIndex = orderDepartments.stream().noneMatch(dep -> dep.getDepId() == 1) ? 0 : 1;
            orderDepartments.add(insertIndex, new OrderDepartment(2, "SME", "R", dtoToOrder(orderDto)));
            return;
        }

        departmentTransitionUpdater.syncDepartmentStatus(order, orderDto, orderDepartments, dtoDepartments, OrderFlowField.SME, orderDto.getSme());
    }

    private void applySpu(Order order, OrderDto orderDto, List<OrderDepartment> orderDepartments, List<OrderDepartment> dtoDepartments) {
        if (orderDto.getSpu() == null || orderDepartments == null) {
            return;
        }

        order.setSpu(orderDto.getSpu());
        if (orderDepartments.stream().noneMatch(dep -> dep.getDepId() == OrderFlowField.SPU.departmentId())) {
            orderDepartments.add(spuInsertIndex(orderDepartments), new OrderDepartment(3, "SPU", "R", dtoToOrder(orderDto)));
            return;
        }

        departmentTransitionUpdater.syncDepartmentStatus(order, orderDto, orderDepartments, dtoDepartments, OrderFlowField.SPU, orderDto.getSpu());
    }

    private int spuInsertIndex(List<OrderDepartment> departments) {
        boolean hasDep1 = departments.stream().anyMatch(dep -> dep.getDepId() == 1);
        boolean hasSme = departments.stream().anyMatch(dep -> dep.getDepId() == 2);
        if (!hasDep1 && !hasSme) {
            return 0;
        }
        if (hasDep1 && hasSme) {
            return 2;
        }
        return 1;
    }

    private Order dtoToOrder(OrderDto orderDto) {
        return this.modelMapper.map(orderDto, Order.class);
    }
}
