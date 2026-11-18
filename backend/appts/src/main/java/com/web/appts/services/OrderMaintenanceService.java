package com.web.appts.services;

public interface OrderMaintenanceService {
    void removeArchiveAndMainOrders();

    void createMonSubOrders();

    void adjustParentOrders();

    void markOrdersExpired();

    void checkDeletedOrders();

    void verifyCrmOrders();
}
