package com.web.appts.client;

import com.sun.net.httpserver.HttpServer;
import com.web.appts.DTO.OrderResultSetVals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = OrdersFeignClientIntegrationTest.TestApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
class OrdersFeignClientIntegrationTest {
    private static final String RESPONSE_BODY =
            "[{\"orderNumber\":\"10001\",\"regel\":\"1\",\"customerName\":\"Demo customer\"}]";
    private static final AtomicReference<String> requestMethod = new AtomicReference<>();
    private static final AtomicReference<String> requestPath = new AtomicReference<>();
    private static final HttpServer stubServer = startStubServer();

    @org.springframework.beans.factory.annotation.Autowired
    private OrdersFeignClient ordersFeignClient;

    @DynamicPropertySource
    static void configureFeignUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "app.feign.erp-adapter-url",
                () -> "http://127.0.0.1:" + stubServer.getAddress().getPort()
        );
    }

    @AfterAll
    static void stopStubServer() {
        stubServer.stop(0);
    }

    @Test
    void fetchesAndDeserializesOrdersFromTheAdapterContract() {
        List<OrderResultSetVals> orders = ordersFeignClient.sendAndReceiveOrders();

        assertEquals("GET", requestMethod.get());
        assertEquals("/api/feign/refresh/orders", requestPath.get());
        assertEquals(1, orders.size());
        assertEquals("10001", orders.get(0).getOrderNumber());
        assertEquals("1", orders.get(0).getRegel());
        assertEquals("Demo customer", orders.get(0).getCustomerName());
    }

    private static HttpServer startStubServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/feign/refresh/orders", exchange -> {
                requestMethod.set(exchange.getRequestMethod());
                requestPath.set(exchange.getRequestURI().getPath());
                byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (java.io.OutputStream output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start ERP adapter test server", e);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = OrdersFeignClient.class)
    static class TestApplication {
    }
}
