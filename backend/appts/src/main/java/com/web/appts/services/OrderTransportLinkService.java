package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.util.Map;

public interface OrderTransportLinkService {
    Boolean updateTraColors(String ids, Map<String, OrderDto> ordersMap);
}
