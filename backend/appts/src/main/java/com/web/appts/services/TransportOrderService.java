
package com.web.appts.services;

import com.web.appts.DTO.DriverInfoDto;
import com.web.appts.DTO.RouteInfoDto;
import com.web.appts.DTO.TrailerInfoDto;
import com.web.appts.DTO.TruckInfoDto;
import java.util.List;

public interface TransportOrderService {
	List<RouteInfoDto> getAllRoutes();

	RouteInfoDto createRoute(RouteInfoDto routeInfoDto);

	Boolean deleteRoute(int id);

	List<DriverInfoDto> getAllDrivers();

	DriverInfoDto createDriver(DriverInfoDto driverInfoDto);

	Boolean deleteDriver(int id);

	List<TrailerInfoDto> getAllTrailers();

	TrailerInfoDto createTrailer(TrailerInfoDto trailerInfoDto);

	Boolean deleteTrailer(int id);

	List<TruckInfoDto> getAllTrucks();

	TruckInfoDto createTruck(TruckInfoDto truckInfoDto);

	Boolean deleteTruck(int id);
}
