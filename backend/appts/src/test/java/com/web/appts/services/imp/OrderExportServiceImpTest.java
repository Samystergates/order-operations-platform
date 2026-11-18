package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderExportServiceImpTest {

    @Test
    void generateExcelFileWritesHeaderAndSortedRows() throws Exception {
        OrderDto laterOrderNumber = new OrderDto();
        laterOrderNumber.setOrderNumber("200");
        laterOrderNumber.setProduct("P-2");
        laterOrderNumber.setRegel("1");
        laterOrderNumber.setCreationDate("2025-01-01");
        laterOrderNumber.setOrganization("Org B");
        laterOrderNumber.setDeliveryDate("2025-01-03");

        OrderDto earlierOrderNumber = new OrderDto();
        earlierOrderNumber.setOrderNumber("100");
        earlierOrderNumber.setProduct("P-1");
        earlierOrderNumber.setRegel("2");
        earlierOrderNumber.setCreationDate("2025-01-02");
        earlierOrderNumber.setOrganization("Org A");
        earlierOrderNumber.setDeliveryDate("2025-01-04");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        new OrderExportServiceImp().generateExcelFile(
                Arrays.asList(laterOrderNumber, earlierOrderNumber),
                outputStream
        );

        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()));

        assertEquals("Orders", workbook.getSheetAt(0).getSheetName());
        assertEquals("Order Number", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals("100", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        assertEquals("200", workbook.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());

        workbook.close();
    }
}
