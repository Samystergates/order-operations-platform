package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.util.List;

public interface OrderTextSyncService {
    void updateProductNotes();

    List<OrderDto> updateAllText(String orderNumOrIds);
}
