package com.web.appts.controllers;


import com.web.appts.DTO.DriverInfoDto;
import com.web.appts.DTO.RouteInfoDto;
import com.web.appts.DTO.TrailerInfoDto;
import com.web.appts.DTO.TruckInfoDto;
import com.web.appts.services.TransportOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/transport/info"})
@CrossOrigin
public class TransportInfoController {

    @Autowired
    private TransportOrderService transportOrderService;

    @GetMapping({"/routes"})
    public ResponseEntity<List<RouteInfoDto>> getRoutes() {
        List<RouteInfoDto> routeInfoDto = this.transportOrderService.getAllRoutes();
        return ResponseEntity.ok(routeInfoDto);
    }

    @PostMapping({"/routes/create"})
    public ResponseEntity<RouteInfoDto> createRoute(@RequestBody RouteInfoDto routeInfoDto) {
        return ResponseEntity.ok(this.transportOrderService.createRoute(routeInfoDto));
    }

    @DeleteMapping({"/routes/delete/{id}"})
    public ResponseEntity<Boolean> deleteRoute(@PathVariable int id) {
        return ResponseEntity.ok(this.transportOrderService.deleteRoute(id));
    }

    @GetMapping({"/drivers"})
    public ResponseEntity<List<DriverInfoDto>> getDrivers() {
        List<DriverInfoDto> driverInfoDto = this.transportOrderService.getAllDrivers();
        return ResponseEntity.ok(driverInfoDto);
    }

    @PostMapping({"/drivers/create"})
    public ResponseEntity<DriverInfoDto> createDriver(@RequestBody DriverInfoDto driverInfoDto) {
        return ResponseEntity.ok(this.transportOrderService.createDriver(driverInfoDto));
    }

    @DeleteMapping("/drivers/delete/{id}")
    public ResponseEntity<Boolean> deleteDriver(@PathVariable int id) {
        return ResponseEntity.ok(this.transportOrderService.deleteDriver(id));
    }

    @GetMapping({"/trucks"})
    public ResponseEntity<List<TruckInfoDto>> getTrucks() {
        List<TruckInfoDto> truckInfoDto = this.transportOrderService.getAllTrucks();
        return ResponseEntity.ok(truckInfoDto);
    }

    @PostMapping({"/trucks/create"})
    public ResponseEntity<TruckInfoDto> createTruck(@RequestBody TruckInfoDto truckInfoDto) {
        return ResponseEntity.ok(this.transportOrderService.createTruck(truckInfoDto));
    }

    @DeleteMapping("trucks/delete/{id}")
    public ResponseEntity<Boolean> deleteTruck(@PathVariable int id) {
        return ResponseEntity.ok(this.transportOrderService.deleteTruck(id));
    }

    @GetMapping({"/trailers"})
    public ResponseEntity<List<TrailerInfoDto>> getTrailers() {
        List<TrailerInfoDto> trailerInfoDto = this.transportOrderService.getAllTrailers();
        return ResponseEntity.ok(trailerInfoDto);
    }

    @PostMapping({"/trailers/create"})
    public ResponseEntity<TrailerInfoDto> createTrailer(@RequestBody TrailerInfoDto trailerInfoDto) {
        return ResponseEntity.ok(this.transportOrderService.createTrailer(trailerInfoDto));
    }

    @DeleteMapping("trailers/delete/{id}")
    public ResponseEntity<Boolean> deleteTrailer(@PathVariable int id) {
        return ResponseEntity.ok(this.transportOrderService.deleteTrailer(id));
    }

}
