package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderErpTextUpdateApplierTest {
    private final OrderErpTextUpdateApplier applier = new OrderErpTextUpdateApplier();

    @Test
    void appliesChangedAndMissingNotesAndNormalizesLineNumbers() {
        OrderDto changed = order("100", "1", "Old");
        OrderDto missing = order("100", "2", null);
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", changed);
        orders.put("100,2", missing);
        AtomicInteger updates = new AtomicInteger();

        OrdersFeignData changedData = data("100", "01", "New");
        OrdersFeignData missingData = data("100", "2", "Added");

        applier.applyProductNotes(
                Arrays.asList(changedData, missingData),
                orders,
                order -> updates.incrementAndGet()
        );

        assertEquals("New", changed.getTekst());
        assertEquals("Added", missing.getTekst());
        assertEquals(2, updates.get());
    }

    @Test
    void skipsInvalidLinesNullDataAndOrdersNotInCache() {
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        AtomicInteger updates = new AtomicInteger();

        applier.applyProductNotes(
                Arrays.asList(
                        null,
                        data("100", "not-a-line", "Ignored"),
                        data("999", "1", "Unknown")
                ),
                orders,
                order -> updates.incrementAndGet()
        );

        assertEquals(0, updates.get());
    }

    @Test
    void doesNotUpdateAnUnchangedNote() {
        OrderDto order = order("100", "1", "Same");
        Map<String, OrderDto> orders = new ConcurrentHashMap<>();
        orders.put("100,1", order);
        AtomicInteger updates = new AtomicInteger();

        applier.applyProductNotes(
                Collections.singletonList(data("100", "1", "Same")),
                orders,
                updated -> updates.incrementAndGet()
        );

        assertEquals("Same", order.getTekst());
        assertEquals(0, updates.get());
    }

    private OrderDto order(String orderNumber, String line, String text) {
        OrderDto order = new OrderDto();
        order.setId(1);
        order.setOrderNumber(orderNumber);
        order.setRegel(line);
        order.setTekst(text);
        return order;
    }

    private OrdersFeignData data(String orderNumber, String line, String text) {
        OrdersFeignData data = new OrdersFeignData();
        data.setOrderNumber(orderNumber);
        data.setRegel(line);
        data.setNotes(text);
        return data;
    }
}
