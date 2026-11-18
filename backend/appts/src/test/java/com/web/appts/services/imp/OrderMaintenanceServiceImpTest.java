package com.web.appts.services.imp;

import com.web.appts.services.OrderLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class OrderMaintenanceServiceImpTest {

    @Mock
    private OrderLifecycleService orderLifecycleService;

    private OrderMaintenanceServiceImp orderMaintenanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderMaintenanceService = new OrderMaintenanceServiceImp(orderLifecycleService);
    }

    @Test
    void delegatesToLifecycleService() {
        orderMaintenanceService.removeArchiveAndMainOrders();
        orderMaintenanceService.createMonSubOrders();
        orderMaintenanceService.adjustParentOrders();
        orderMaintenanceService.markOrdersExpired();
        orderMaintenanceService.checkDeletedOrders();
        orderMaintenanceService.verifyCrmOrders();

        verify(orderLifecycleService).removingSameArchivedOrders();
        verify(orderLifecycleService).createMonSub();
        verify(orderLifecycleService).adjustParentOrders();
        verify(orderLifecycleService).markExpired();
        verify(orderLifecycleService).checkOrderExistence();
        verify(orderLifecycleService).verifyCrmOrders();
    }
}
