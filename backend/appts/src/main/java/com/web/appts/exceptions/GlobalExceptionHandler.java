package com.web.appts.exceptions;

import com.web.appts.DTO.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> resourceNotFoundExceptionHandler(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> methodNotValidExceptionHandler(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                traceId(request)
        );
        response.setErrors(fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ApiErrorResponse> apiExceptionHandler(ApiException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "API_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler({HttpMessageNotWritableException.class, ConcurrentModificationException.class})
    public ResponseEntity<ApiErrorResponse> handleSerializationOrConcurrentError(
            Exception ex,
            HttpServletRequest request,
            HandlerMethod handlerMethod) {

        Throwable cause = ex.getCause();
        if (cause instanceof ConcurrentModificationException) {
            logger.error("Concurrent modification detected during response serialization");
        }

        String controller = handlerMethod.getBeanType().getName();
        String method = handlerMethod.getMethod().getName();
        logger.error("Controller method producing the error: {}.{}()", controller, method);
        logger.error("Request URI: {}", request.getRequestURI());

        for (StackTraceElement element : ex.getStackTrace()) {
            if (element.getClassName().startsWith("com.web.appts")) {
                logger.error(
                        "Origin: {}.{} (line {})",
                        element.getClassName(),
                        element.getMethodName(),
                        element.getLineNumber()
                );
                break;
            }
        }

        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        logger.error("Root cause: {}", root.getClass().getName(), ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Internal server error",
                request
        );
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiErrorResponse> unhandledExceptionHandler(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled API exception for {}", request.getRequestURI(), ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Internal server error",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                code,
                message,
                request.getRequestURI(),
                traceId(request)
        );
        return new ResponseEntity<>(response, status);
    }

    private String traceId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.trim().isEmpty()
                ? UUID.randomUUID().toString()
                : requestId;
    }
}
