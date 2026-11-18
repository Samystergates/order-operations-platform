package com.web.services;

import com.web.dto.OrderResultSetVals;
import com.web.dto.OrdersFeignData;

import java.util.List;

public interface OrderService {

    List<OrdersFeignData> checkOrderExistence(String stringOfOrdersWithCommaAndQuotations);

    List<OrderResultSetVals> getCRMOrders();

    List<OrdersFeignData> deleteOrders(List<OrdersFeignData> dataList);

    List<OrdersFeignData> createMonSub(String stringOfOrdersWithCommaAndQuotations);

    List<OrdersFeignData> updateProductNotes(String stringOfOrdersWithCommaAndQuotations);

    List<OrdersFeignData> markExpired(String stringOfOrdersWithCommaAndQuotations);
}
