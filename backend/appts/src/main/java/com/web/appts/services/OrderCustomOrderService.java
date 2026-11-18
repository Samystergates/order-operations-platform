package com.web.appts.services;

import com.web.appts.DTO.CustomOrderDto;
import com.web.appts.DTO.DeleteCustOrderDto;

import java.util.List;

public interface OrderCustomOrderService {
    CustomOrderDto createCustomOrder(CustomOrderDto customOrderDto);

    DeleteCustOrderDto deleteCustomOrder(CustomOrderDto customOrderDto);

    List<CustomOrderDto> getAllCustomOrders();
}
