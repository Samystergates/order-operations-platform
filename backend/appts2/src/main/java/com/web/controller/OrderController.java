package com.web.controller;

import com.web.dto.OrderResultSetVals;
import com.web.dto.OrdersFeignData;
import com.web.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/feign"})
@CrossOrigin
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping({"/refresh/orders"})
    public List<OrderResultSetVals> getCrmOrders() {
        return this.orderService.getCRMOrders();
    }

    @PostMapping("/mark/expired")
    public List<OrdersFeignData> markExpired(@RequestBody String stringOfOrdersWithCommaAndQuotations) {
        return orderService.markExpired(stringOfOrdersWithCommaAndQuotations);
    }

    @PostMapping("/check/existence")
    public List<OrdersFeignData> checkExistence(@RequestBody String stringOfOrdersWithCommaAndQuotations) {
        return orderService.checkOrderExistence(stringOfOrdersWithCommaAndQuotations);
    }

    @PostMapping("/delete/orders")
    public List<OrdersFeignData> deleteOrders(@RequestBody List<OrdersFeignData> request) {
        return orderService.deleteOrders(request);
    }

    @PostMapping("/update/product/notes")
    public List<OrdersFeignData> productNotes(@RequestBody String stringOfOrdersWithCommaAndQuotations) {
        return orderService.updateProductNotes(stringOfOrdersWithCommaAndQuotations);
    }

    @PostMapping("/update/mon/sub")
    public List<OrdersFeignData> monSub(@RequestBody String stringOfOrdersWithCommaAndQuotations) {
        return orderService.createMonSub(stringOfOrdersWithCommaAndQuotations);
    }

}
