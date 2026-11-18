package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderErpSyncServiceImpTest {

    private final OrderErpSyncServiceImp service = new OrderErpSyncServiceImp();

    @Test
    void getCRMOrdersReturnsImmediatelyWhenLoaderSucceeds() {
        List<OrderDto> expected = Collections.singletonList(new OrderDto());
        AtomicBoolean recovered = new AtomicBoolean(false);
        AtomicBoolean failedFast = new AtomicBoolean(false);

        List<OrderDto> result = service.getCRMOrders(
                () -> expected,
                () -> recovered.set(true),
                () -> failedFast.set(true)
        );

        assertSame(expected, result);
        assertFalse(recovered.get());
        assertFalse(failedFast.get());
    }

    @Test
    void getCRMOrdersRecoversConnectionAfterFirstRetryWindowFails() {
        List<OrderDto> expected = Collections.singletonList(new OrderDto());
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicBoolean recovered = new AtomicBoolean(false);

        List<OrderDto> result = service.getCRMOrders(
                () -> {
                    if (attempts.incrementAndGet() <= 5) {
                        throw new RuntimeException("[Microsoft][ODBC Driver Manager] Invalid string or buffer length");
                    }
                    return expected;
                },
                () -> recovered.set(true),
                () -> {
                    throw new AssertionError("failFast should not be called");
                }
        );

        assertSame(expected, result);
        assertEquals(6, attempts.get());
        assertTrue(recovered.get());
    }

    @Test
    void getCRMOrdersThrowsAfterFailingBothRetryWindows() {
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicBoolean recovered = new AtomicBoolean(false);
        AtomicBoolean failedFast = new AtomicBoolean(false);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> service.getCRMOrders(
                        () -> {
                            attempts.incrementAndGet();
                            throw new RuntimeException("ERP unavailable");
                        },
                        () -> recovered.set(true),
                        () -> failedFast.set(true)
                )
        );

        assertEquals(10, attempts.get());
        assertTrue(recovered.get());
        assertTrue(failedFast.get());
        assertEquals("ERP operation getCRMOrders failed after 10 attempts", failure.getMessage());
        assertEquals("ERP unavailable", failure.getCause().getMessage());
    }

    @Test
    void executeReturnsImmediatelyForSuccessfulVoidOperation() {
        AtomicBoolean recovered = new AtomicBoolean(false);
        AtomicBoolean failedFast = new AtomicBoolean(false);

        Object result = service.execute(
                "updateProductNotes",
                () -> "ok",
                () -> recovered.set(true),
                () -> failedFast.set(true)
        );

        assertEquals("ok", result);
        assertFalse(recovered.get());
        assertFalse(failedFast.get());
    }

    @Test
    void executeRecoversAndRetriesGenericOperation() {
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicBoolean recovered = new AtomicBoolean(false);

        Map<String, OrderDto> result = service.execute(
                "genericOperation",
                () -> {
                    if (attempts.incrementAndGet() <= 5) {
                        throw new RuntimeException("ERP unavailable");
                    }
                    return Collections.singletonMap("100,1", new OrderDto());
                },
                () -> recovered.set(true),
                () -> {
                    throw new AssertionError("failFast should not be called");
                }
        );

        assertEquals(6, attempts.get());
        assertTrue(recovered.get());
        assertEquals(1, result.size());
    }
}
