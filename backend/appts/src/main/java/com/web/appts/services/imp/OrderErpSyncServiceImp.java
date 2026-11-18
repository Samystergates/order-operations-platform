package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderErpSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class OrderErpSyncServiceImp implements OrderErpSyncService {
    private static final Logger logger = LoggerFactory.getLogger(OrderErpSyncServiceImp.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final String ODBC_BUFFER_ERROR =
            "[Microsoft][ODBC Driver Manager] Invalid string or buffer length";

    @Override
    public List<OrderDto> getCRMOrders(
            Supplier<List<OrderDto>> crmOrderLoader,
            Runnable recoverConnection,
            Runnable failFast
    ) {
        return execute("getCRMOrders", crmOrderLoader, recoverConnection, failFast);
    }

    @Override
    public <T> T execute(
            String operationName,
            Supplier<T> operation,
            Runnable recoverConnection,
            Runnable failFast
    ) {
        LoadResult<T> firstAttempt = tryLoad(operation, operationName + " sql exc 2");
        if (firstAttempt.successful) {
            logger.info("{} worked", operationName);
            return firstAttempt.value;
        }

        recoverConnection.run();

        LoadResult<T> recoveredAttempt = tryLoad(operation, operationName + " sql exc 3");
        if (recoveredAttempt.successful) {
            logger.info("{} fixed", operationName);
            return recoveredAttempt.value;
        }

        failFast.run();
        throw new IllegalStateException(
                "ERP operation " + operationName + " failed after "
                        + (MAX_ATTEMPTS * 2) + " attempts",
                recoveredAttempt.failure
        );
    }

    private <T> LoadResult<T> tryLoad(Supplier<T> loader, String logPrefix) {
        int retry = 0;
        Exception lastFailure = null;
        while (retry < MAX_ATTEMPTS) {
            try {
                return LoadResult.success(loader.get());
            } catch (Exception exception) {
                lastFailure = exception;
                retry++;
                logger.info(logPrefix);
                if (isRecoverableOdbcError(exception)) {
                    logger.info("{} retry is: {}", logPrefix, retry);
                } else {
                    logger.warn("ERP operation attempt {} failed", retry, exception);
                }
            }
        }
        return LoadResult.failed(lastFailure);
    }

    private boolean isRecoverableOdbcError(Exception exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains(ODBC_BUFFER_ERROR);
    }

    private static class LoadResult<T> {
        private final boolean successful;
        private final T value;
        private final Exception failure;

        private LoadResult(boolean successful, T value, Exception failure) {
            this.successful = successful;
            this.value = value;
            this.failure = failure;
        }

        private static <T> LoadResult<T> success(T value) {
            return new LoadResult<>(true, value, null);
        }

        private static <T> LoadResult<T> failed(Exception failure) {
            return new LoadResult<>(false, null, failure);
        }
    }
}
