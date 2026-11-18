package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.services.OrderExportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderExportServiceImp implements OrderExportService {
    private static final String[] HEADERS = {
            "Order Number",
            "Product Number",
            "Regel",
            "Creation",
            "Organization",
            "Delivery"
    };

    @Override
    public void generateExcelFile(List<OrderDto> orders, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");
            createHeaderRow(workbook, sheet);
            createDataRows(sheet, orders);
            resizeColumns(sheet);
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate orders export", e);
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = getHeaderStyle(workbook);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createDataRows(Sheet sheet, List<OrderDto> orders) {
        orders.sort(Comparator.comparing(OrderDto::getOrderNumber)
                .thenComparing(order -> Integer.parseInt(order.getRegel())));

        int rowIdx = 1;
        for (OrderDto order : orders) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(order.getOrderNumber());
            row.createCell(1).setCellValue(order.getProduct());
            row.createCell(2).setCellValue(order.getRegel());
            row.createCell(3).setCellValue(order.getCreationDate());
            row.createCell(4).setCellValue(order.getOrganization());
            row.createCell(5).setCellValue(order.getDeliveryDate());
        }
    }

    private void resizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
