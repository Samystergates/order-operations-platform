package com.web.services.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ErpRetryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ErpRetryExecutor.class);
    private static final int MAX_ATTEMPTS = 5;

    public <T> T execute(String operationName, Supplier<T> operation, Runnable recoverConnection) {
        try {
            return runAttempts(operationName, "initial", operation);
        } catch (RetryExhaustedException initialFailure) {
            logger.warn("ERP operation {} failed; recovering the connection before retrying", operationName);
        }

        recoverConnection.run();
        return runAttempts(operationName, "after connection recovery", operation);
    }

    private <T> T runAttempts(String operationName, String stage, Supplier<T> operation) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException exception) {
                lastFailure = exception;
                logger.warn(
                        "ERP operation {} failed at {} attempt {}/{}",
                        operationName,
                        stage,
                        attempt,
                        MAX_ATTEMPTS,
                        exception
                );
            }
        }

        throw new RetryExhaustedException(
                "ERP operation " + operationName + " failed after " + stage + " attempts",
                lastFailure
        );
    }

    private static class RetryExhaustedException extends IllegalStateException {
        private RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
