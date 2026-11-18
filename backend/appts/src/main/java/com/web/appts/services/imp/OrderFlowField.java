package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;

import java.util.function.BiConsumer;
import java.util.function.Function;

enum OrderFlowField {
    SME(2, "SME", Order::setSme, OrderDto::getSme),
    SPU(3, "SPU", Order::setSpu, OrderDto::getSpu),
    MON_LB(4, "MONLB", Order::setMonLb, OrderDto::getMonLb),
    MON_TR(5, "MONTR", Order::setMonTr, OrderDto::getMonTr),
    MWE(6, "MWE", Order::setMwe, OrderDto::getMwe),
    SER(7, "SER", Order::setSer, OrderDto::getSer),
    TRA(8, "TRA", Order::setTra, OrderDto::getTra),
    EXP(9, "EXP", Order::setExp, OrderDto::getExp);

    private final int departmentId;
    private final String departmentName;
    private final BiConsumer<Order, String> orderSetter;
    private final Function<OrderDto, String> dtoGetter;

    OrderFlowField(
            int departmentId,
            String departmentName,
            BiConsumer<Order, String> orderSetter,
            Function<OrderDto, String> dtoGetter
    ) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.orderSetter = orderSetter;
        this.dtoGetter = dtoGetter;
    }

    int departmentId() {
        return departmentId;
    }

    String departmentName() {
        return departmentName;
    }

    String valueFrom(OrderDto orderDto) {
        return dtoGetter.apply(orderDto);
    }

    void applyTo(Order order, String value) {
        orderSetter.accept(order, value);
    }
}
