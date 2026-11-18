package com.web.appts.services;

import com.web.appts.DTO.OrderDto;

import java.io.OutputStream;
import java.util.List;

public interface OrderExportService {
    void generateExcelFile(List<OrderDto> orders, OutputStream outputStream);
}
