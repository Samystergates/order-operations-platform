package com.web.appts.services.imp;

import com.web.appts.services.OrderMaintenanceService;
import com.web.appts.services.OrderLifecycleService;
import org.springframework.stereotype.Service;

@Service
public class OrderMaintenanceServiceImp implements OrderMaintenanceService {
    private final OrderLifecycleService orderLifecycleService;

    public OrderMaintenanceServiceImp(OrderLifecycleService orderLifecycleService) {
        this.orderLifecycleService = orderLifecycleService;
    }

    @Override
    public void removeArchiveAndMainOrders() {
        orderLifecycleService.removingSameArchivedOrders();
    }

    @Override
    public void createMonSubOrders() {
        orderLifecycleService.createMonSub();
    }

    @Override
    public void adjustParentOrders() {
        orderLifecycleService.adjustParentOrders();
    }

    @Override
    public void markOrdersExpired() {
        orderLifecycleService.markExpired();
    }

    @Override
    public void checkDeletedOrders() {
        orderLifecycleService.checkOrderExistence();
    }

    @Override
    public void verifyCrmOrders() {
        orderLifecycleService.verifyCrmOrders();
    }
}
