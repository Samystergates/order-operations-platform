package com.web.services.imp;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpRetryExecutorTest {
    private final ErpRetryExecutor executor = new ErpRetryExecutor();

    @Test
    void doesNotRecoverWhenAnAttemptSucceeds() {
        AtomicBoolean recovered = new AtomicBoolean(false);

        String result = executor.execute("query", () -> "orders", () -> recovered.set(true));

        assertEquals("orders", result);
        assertFalse(recovered.get());
    }

    @Test
    void recoversOnceAfterTheInitialRetryWindowIsExhausted() {
        AtomicInteger attempts = new AtomicInteger();
        AtomicBoolean recovered = new AtomicBoolean(false);

        String result = executor.execute(
                "query",
                () -> attempts.incrementAndGet() <= 5
                        ? throwUnavailable()
                        : "orders",
                () -> recovered.set(true)
        );

        assertEquals("orders", result);
        assertEquals(6, attempts.get());
        assertTrue(recovered.get());
    }

    @Test
    void surfacesFailureAfterBothRetryWindows() {
        AtomicInteger attempts = new AtomicInteger();
        AtomicBoolean recovered = new AtomicBoolean(false);

        assertThrows(
                IllegalStateException.class,
                () -> executor.execute(
                        "query",
                        () -> {
                            attempts.incrementAndGet();
                            return throwUnavailable();
                        },
                        () -> recovered.set(true)
                )
        );

        assertEquals(10, attempts.get());
        assertTrue(recovered.get());
    }

    private String throwUnavailable() {
        throw new IllegalStateException("ERP unavailable");
    }
}
