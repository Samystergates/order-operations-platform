package com.web.appts.utils;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.ArchivedOrdersDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderSortingUtilsTest {

    @Test
    void sortsByDeliveryDateDescendingWithFallbackToCreationDate() {
        OrderDto orderA = new OrderDto();
        orderA.setOrderNumber("A");
        orderA.setDeliveryDate("2024-01-01");
        orderA.setCreationDate("2023-12-31");

        OrderDto orderB = new OrderDto();
        orderB.setOrderNumber("B");
        orderB.setDeliveryDate("");
        orderB.setCreationDate("2025-01-01");

        OrderDto orderC = new OrderDto();
        orderC.setOrderNumber("C");
        orderC.setDeliveryDate("2023-01-01");
        orderC.setCreationDate("2022-01-01");

        List<OrderDto> orders = new ArrayList<>();
        orders.add(orderA);
        orders.add(orderB);
        orders.add(orderC);

        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orders);

        assertEquals("B", orders.get(0).getOrderNumber());
        assertEquals("A", orders.get(1).getOrderNumber());
        assertEquals("C", orders.get(2).getOrderNumber());
    }

    @Test
    void supportsMultipleDateFormats() {
        OrderDto orderA = new OrderDto();
        orderA.setOrderNumber("A");
        orderA.setDeliveryDate("05/01/2025");

        OrderDto orderB = new OrderDto();
        orderB.setOrderNumber("B");
        orderB.setDeliveryDate("2024-12-31");

        List<OrderDto> orders = new ArrayList<>();
        orders.add(orderB);
        orders.add(orderA);

        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orders, "MM/dd/yyyy", "yyyy-MM-dd");

        assertEquals("A", orders.get(0).getOrderNumber());
        assertEquals("B", orders.get(1).getOrderNumber());
    }

    @Test
    void supportsGenericDateSortingForArchiveDtos() {
        ArchivedOrdersDto older = new ArchivedOrdersDto();
        older.setOrderNumber("100");
        older.setDeliveryDate("2024-01-01");

        ArchivedOrdersDto newer = new ArchivedOrdersDto();
        newer.setOrderNumber("200");
        newer.setDeliveryDate("01/15/2025");

        List<ArchivedOrdersDto> orders = new ArrayList<>();
        orders.add(older);
        orders.add(newer);

        OrderSortingUtils.sortByDateDesc(
                orders,
                ArchivedOrdersDto::getDeliveryDate,
                ArchivedOrdersDto::getCreationDate,
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        );

        assertEquals("200", orders.get(0).getOrderNumber());
        assertEquals("100", orders.get(1).getOrderNumber());
    }
}
