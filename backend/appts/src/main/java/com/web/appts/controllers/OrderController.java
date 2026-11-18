
package com.web.appts.controllers;

import com.web.appts.DTO.CustomOrderDto;
import com.web.appts.DTO.DeleteCustOrderDto;
import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderTextSyncService;
import com.web.appts.services.OrderRefreshWorkflowService;
import com.web.appts.services.OrderService;
import com.web.appts.utils.OrderSortingUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.web.appts.services.OrderTRAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/index", "/api/home"})
@CrossOrigin
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderTRAService orderTRAService;
    @Autowired
    private OrderRefreshWorkflowService orderRefreshWorkflowService;
    @Autowired
    private OrderTextSyncService orderTextSyncService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public OrderController() {
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<OrderDto>> getOrders() {
        List<OrderDto> orderDto = this.orderService.getAllOrders();
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orderDto);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping({"/refresh/orders"})
    public ResponseEntity<List<OrderDto>> getCrmOrdersLegacy() {
        List<OrderDto> refreshedOrders = orderRefreshWorkflowService.refreshOrdersFromCrm();
        this.messagingTemplate.convertAndSend("/topic/orderUpdate", refreshedOrders);
        return ResponseEntity.ok(refreshedOrders);
    }

    @PostMapping({"/refresh/orders"})
    public ResponseEntity<List<OrderDto>> getCrmOrders() {
        List<OrderDto> refreshedOrders = orderRefreshWorkflowService.refreshOrdersFromCrm();
        this.messagingTemplate.convertAndSend("/topic/orderUpdate", refreshedOrders);
        return ResponseEntity.ok(refreshedOrders);
    }

    @GetMapping({"/search/{userName}"})
    public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable("userName") String userName) {
        List<OrderDto> orderDto = this.orderService.getOrdersByUser(userName);
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orderDto);
        return ResponseEntity.ok(orderDto);
    }

    @PutMapping({"/update/{flowUpdate}"})
    public ResponseEntity<List<OrderDto>> updatingOrder(@RequestBody OrderDto orderDto, @PathVariable("flowUpdate") Boolean flowUpdate) {
        List<OrderDto> updatedOrders = this.orderService.updateOrder(orderDto, orderDto.getId(), flowUpdate);
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(updatedOrders);
        messagingTemplate.convertAndSend("/topic/orderUpdate", updatedOrders);
        return ResponseEntity.ok(updatedOrders);
    }

    @PostMapping({"/create"})
    public ResponseEntity<List<OrderDto>> creatingOrder(@RequestBody OrderDto orderDto) {
        this.orderService.createOrder(orderDto);
        List<OrderDto> orderDtos = this.orderService.getAllOrders();
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orderDtos);
        messagingTemplate.convertAndSend("/topic/orderUpdate", orderDtos);
        return ResponseEntity.ok(orderDtos);
    }

    @PostMapping({"/create/custom"})
    public ResponseEntity<CustomOrderDto> creatingCustomOrder(@RequestBody CustomOrderDto customOrderDto) {
        CustomOrderDto co = this.orderService.createCustomOrder(customOrderDto);
        return ResponseEntity.ok(co);
    }

    @DeleteMapping({"/delete/custom"})
    public ResponseEntity<Boolean> deletingCustomOrder(@RequestBody CustomOrderDto customOrderDto) {
        DeleteCustOrderDto dto = this.orderService.deleteCustomOrder(customOrderDto);
        if(dto.getTransportOrderLines() != null) {
            this.orderTRAService.deleteLineFromTra(dto.getTransportOrderLines());
        }
        return ResponseEntity.ok(dto.getResult());
    }

    @GetMapping({"/custom"})
    public ResponseEntity<List<CustomOrderDto>> getCustomOrders() {
        List<CustomOrderDto> cOrders = this.orderService.getAllCustomOrders();
        return ResponseEntity.ok(cOrders);
    }

    @PutMapping({"/update/colors/{orderNumber}/{orderDep}/{flowVal}"})
    public ResponseEntity<List<OrderDto>> updatingOrderColors(@RequestBody String orderStatus, @PathVariable("orderNumber") String orderNumber, @PathVariable("orderDep") String orderDep, @PathVariable("flowVal") String flowVal) {
        this.orderService.updateOrderColors(orderNumber, orderDep, orderStatus, flowVal);
        List<OrderDto> updatedOrdersWithTekst = this.orderTextSyncService.updateAllText(orderNumber);
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(updatedOrdersWithTekst);
        this.messagingTemplate.convertAndSend("/topic/orderUpdate", updatedOrdersWithTekst);
        return ResponseEntity.ok(updatedOrdersWithTekst);
    }

    @GetMapping("/export-orders")
    public ResponseEntity<byte[]> exportOrders() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            orderService.generateExcelFile(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(out.toByteArray());
        } catch (Exception e) {
            logger.error("Failed to export orders workbook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/regel/{regel}")
    public ResponseEntity<List<OrderDto>> getOrdersByRegel(@PathVariable("regel") String regel) {
        List<OrderDto> orderDto = orderService.getOrdersByRegel(regel);
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(orderDto);
        return ResponseEntity.ok(orderDto);
    }

}
