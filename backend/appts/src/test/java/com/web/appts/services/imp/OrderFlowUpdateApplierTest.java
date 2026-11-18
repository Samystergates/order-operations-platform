package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderFlowUpdateApplierTest {

    private OrderFlowUpdateApplier applier;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        OrderDepartmentTransitionUpdater transitionUpdater = new OrderDepartmentTransitionUpdater();
        applier = new OrderFlowUpdateApplier(
                transitionUpdater,
                new OrderManualFlowUpdateApplier(modelMapper, transitionUpdater)
        );
    }

    @Test
    void applyUpdatesConfiguredDepartmentsForOrderType() {
        Order order = order("100", "1", "MLO");
        order.setDepartments(new ArrayList<>(Arrays.asList(
                department(4, "MONLB", "R"),
                department(8, "TRA", "Y")
        )));

        OrderDto dto = orderDto("100", "1", "MLO");
        dto.setMonLb("Y");
        dto.setTra("G");
        dto.setDepartments(new ArrayList<>(Arrays.asList(
                department(4, "MONLB", "R"),
                department(8, "TRA", "Y")
        )));

        applier.apply(order, dto);

        assertEquals("Y", order.getMonLb());
        assertEquals("G", order.getTra());
        assertEquals("Y", order.getDepartments().get(0).getStatus());
        assertEquals("G", order.getDepartments().get(1).getStatus());
        assertEquals("n", order.getDepartments().get(0).getPrevStatus());
        assertEquals("n", order.getDepartments().get(1).getPrevStatus());
    }

    @Test
    void applyAddsSmeDepartmentWhenManualSmeValueIsIntroduced() {
        Order order = order("100", "1", "LOS");
        order.setDepartments(new ArrayList<>());

        OrderDto dto = orderDto("100", "1", "LOS");
        dto.setSme("R");
        dto.setDepartments(new ArrayList<>());

        applier.apply(order, dto);

        assertEquals("R", order.getSme());
        assertEquals(1, order.getDepartments().size());
        assertEquals(2, order.getDepartments().get(0).getDepId());
        assertEquals("SME", order.getDepartments().get(0).getDepName());
        assertEquals("R", order.getDepartments().get(0).getStatus());
    }

    @Test
    void applyMarksOrderCompleteWhenAllDtoDepartmentsAreDoneOrEmpty() {
        Order order = order("100", "1", "LOS");
        order.setDepartments(new ArrayList<>(Arrays.asList(
                department(8, "TRA", "Y"),
                department(9, "EXP", "")
        )));

        OrderDto dto = orderDto("100", "1", "LOS");
        dto.setTra("G");
        dto.setDepartments(new ArrayList<>(Arrays.asList(
                department(8, "TRA", "G"),
                department(9, "EXP", "")
        )));

        applier.apply(order, dto);

        assertEquals("C", order.getCompleted());
        assertEquals("C", dto.getCompleted());
    }

    private Order order(String orderNumber, String regel, String orderType) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setRegel(regel);
        order.setOrderType(orderType);
        return order;
    }

    private OrderDto orderDto(String orderNumber, String regel, String orderType) {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderNumber(orderNumber);
        orderDto.setRegel(regel);
        orderDto.setOrderType(orderType);
        return orderDto;
    }

    private OrderDepartment department(int id, String name, String status) {
        OrderDepartment department = new OrderDepartment();
        department.setDepId(id);
        department.setDepName(name);
        department.setStatus(status);
        return department;
    }
}
