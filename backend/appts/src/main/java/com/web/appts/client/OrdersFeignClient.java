package com.web.appts.client;

import com.web.appts.DTO.OrderResultSetVals;
import com.web.appts.DTO.OrdersFeignData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "order-feign-service",
        url = "${app.feign.erp-adapter-url:http://localhost:8080}"
)
public interface OrdersFeignClient {

    @GetMapping("/api/feign/refresh/orders")
    List<OrderResultSetVals> sendAndReceiveOrders();

    @PostMapping("/api/feign/mark/expired")
    List<OrdersFeignData> sendAndReceiveMarkExpired(@RequestBody String stringOfOrdersWithCommaAndQuotations);

    @PostMapping("/api/feign/check/existence")
    List<OrdersFeignData> sendAndReceiveOrderExistence(@RequestBody String stringOfOrdersWithCommaAndQuotations);

    @PostMapping("/api/feign/delete/orders")
    List<OrdersFeignData> sendAndReceiveDeleteOrders(@RequestBody List<OrdersFeignData> request);

    @GetMapping("/api/feign/verify/orders")
    void sendAndReceiveVerification();

    @PostMapping("/api/feign/update/product/notes")
    List<OrdersFeignData> sendAndReceiveProductNotes(@RequestBody String stringOfOrdersWithCommaAndQuotations);

    @PostMapping("/api/feign/update/mon/sub")
    List<OrdersFeignData> sendAndReceiveMonSub(@RequestBody String stringOfOrdersWithCommaAndQuotations);

}
