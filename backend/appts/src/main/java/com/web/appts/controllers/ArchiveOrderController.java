
package com.web.appts.controllers;

import com.web.appts.DTO.ArchivedOrdersDto;
import com.web.appts.services.ArchivedOrdersService;
import com.web.appts.utils.OrderSortingUtils;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/archive"})
@CrossOrigin
public class ArchiveOrderController {
    @Autowired
    private ArchivedOrdersService archivedOrdersService;

    public ArchiveOrderController() {
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<ArchivedOrdersDto>> getArchivedOrders() {
        this.archivedOrdersService.validateArchiveMap();
        List<ArchivedOrdersDto> archivedOrderDto = this.archivedOrdersService.getAllArchivedOrders();
        OrderSortingUtils.sortByDateDesc(
                archivedOrderDto,
                ArchivedOrdersDto::getDeliveryDate,
                ArchivedOrdersDto::getCreationDate,
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        );

        return ResponseEntity.ok(archivedOrderDto);
    }

    @GetMapping({"/search/{userName}"})
    public ResponseEntity<List<ArchivedOrdersDto>> getOrdersByUser(@PathVariable("userName") String userName) {
        List<ArchivedOrdersDto> archivedOrderDto = this.archivedOrdersService.getArchivedOrdersByUser(userName);
        OrderSortingUtils.sortByDateDesc(
                archivedOrderDto,
                ArchivedOrdersDto::getDeliveryDate,
                ArchivedOrdersDto::getCreationDate,
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        );

        return ResponseEntity.ok(archivedOrderDto);
    }

    @GetMapping("/search/regel/{regel}")
    public ResponseEntity<List<ArchivedOrdersDto>> getArchivedOrdersByRegel(@PathVariable("regel") String regel) {
        List<ArchivedOrdersDto> archivedOrderDto = archivedOrdersService.getArchivedOrdersByRegel(regel);
        OrderSortingUtils.sortByDateDesc(
                archivedOrderDto,
                ArchivedOrdersDto::getDeliveryDate,
                ArchivedOrdersDto::getCreationDate,
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        );
        return ResponseEntity.ok(archivedOrderDto);
    }

}
