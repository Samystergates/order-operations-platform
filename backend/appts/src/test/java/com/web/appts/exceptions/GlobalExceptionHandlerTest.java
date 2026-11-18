package com.web.appts.exceptions;

import com.web.appts.DTO.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    void apiExceptionReturnsStandardErrorEnvelope() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("X-Request-Id", "request-123");

        ResponseEntity<ApiErrorResponse> response = handler.apiExceptionHandler(
                new ApiException("Invalid Username or Password"),
                request
        );

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("API_ERROR", response.getBody().getCode());
        assertEquals("Invalid Username or Password", response.getBody().getMessage());
        assertEquals("/api/v1/auth/login", response.getBody().getPath());
        assertEquals("request-123", response.getBody().getTraceId());
        assertNotNull(response.getBody().getTimestamp());
    }
}
