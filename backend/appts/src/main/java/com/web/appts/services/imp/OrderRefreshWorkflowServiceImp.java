package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderLifecycleService;
import com.web.appts.services.OrderRefreshWorkflowService;
import com.web.appts.services.OrderService;
import com.web.appts.services.OrderTextSyncService;
import com.web.appts.utils.OrderSortingUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderRefreshWorkflowServiceImp implements OrderRefreshWorkflowService {
    private final OrderService orderService;
    private final OrderLifecycleService orderLifecycleService;
    private final OrderTextSyncService orderTextSyncService;

    public OrderRefreshWorkflowServiceImp(
            OrderService orderService,
            OrderLifecycleService orderLifecycleService,
            OrderTextSyncService orderTextSyncService
    ) {
        this.orderService = orderService;
        this.orderLifecycleService = orderLifecycleService;
        this.orderTextSyncService = orderTextSyncService;
    }

    @Override
    public List<OrderDto> refreshOrdersFromCrm() {
        orderService.checkMap();
        orderLifecycleService.markExpired();
        orderLifecycleService.checkOrderExistence();
        orderService.getCRMOrders();
        orderLifecycleService.createMonSub();
        orderTextSyncService.updateProductNotes();
        orderLifecycleService.adjustParentOrders();

        List<OrderDto> refreshedOrders = orderService.checkMap();
        OrderSortingUtils.sortByDeliveryOrCreationDateDesc(refreshedOrders);
        return refreshedOrders;
    }
}
