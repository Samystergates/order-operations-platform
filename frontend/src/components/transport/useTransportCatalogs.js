import { useCallback, useMemo, useState } from "react";
import {
  createDriverInfo,
  createRouteInfo,
  createTrailerInfo,
  createTruckInfo,
  deleteDriverInfo,
  deleteRouteInfo,
  deleteTrailerInfo,
  deleteTruckInfo,
  loadAllDrivers,
  loadAllRoutes,
  loadAllTrailers,
  loadAllTrucks,
} from "../../services/transportOrder-service";
import {
  mapOptions,
  mapTruckOptions,
} from "./TransportDropdownField";

export default function useTransportCatalogs() {
  const [routes, setRoutes] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [trucks, setTrucks] = useState([]);
  const [trailers, setTrailers] = useState([]);

  const reloadRoutes = useCallback(() => {
    return loadAllRoutes()
      .then((data) => setRoutes(Array.isArray(data) ? [...data] : []));
  }, []);

  const reloadDrivers = useCallback(() => {
    return loadAllDrivers()
      .then((data) => setDrivers(Array.isArray(data) ? [...data] : []));
  }, []);

  const reloadTrucks = useCallback(() => {
    return loadAllTrucks()
      .then((data) => setTrucks(Array.isArray(data) ? [...data] : []));
  }, []);

  const reloadTrailers = useCallback(() => {
    return loadAllTrailers()
      .then((data) => setTrailers(Array.isArray(data) ? [...data] : []));
  }, []);

  const reloadAllCatalogs = useCallback(() => {
    return Promise.all([
      reloadRoutes(),
      reloadDrivers(),
      reloadTrucks(),
      reloadTrailers(),
    ]);
  }, [reloadDrivers, reloadRoutes, reloadTrailers, reloadTrucks]);

  const optionConfigs = useMemo(
    () => ({
      ROUTE: {
        label: "Route",
        placeholder: "Nieuwe route",
        helperText: "",
        create: (value) => createRouteInfo({ route: value }),
        remove: (id) => deleteRouteInfo(id),
        reload: reloadRoutes,
      },
      DRIVER: {
        label: "Chauffeur",
        placeholder: "Naam chauffeur",
        helperText: "",
        create: (value) => createDriverInfo({ driver: value }),
        remove: (id) => deleteDriverInfo(id),
        reload: reloadDrivers,
      },
      TRUCK: {
        label: "Truck",
        placeholder: "Truck, Type",
        helperText: "Gebruik format: Trucknaam, Type (optioneel)",
        create: (value) => {
          const [truck = "", truckType = ""] = value
            .split(",")
            .map((part) => part.trim());
          return createTruckInfo({ truck, truckType });
        },
        remove: (id) => deleteTruckInfo(id),
        reload: reloadTrucks,
      },
      TRAILER: {
        label: "Trailer",
        placeholder: "Trailernummer",
        helperText: "",
        create: (value) => createTrailerInfo({ trailer: value }),
        remove: (id) => deleteTrailerInfo(id),
        reload: reloadTrailers,
      },
    }),
    [reloadDrivers, reloadRoutes, reloadTrailers, reloadTrucks]
  );

  const routeOptions = useMemo(
    () => mapOptions(routes, (item) => item.route),
    [routes]
  );
  const driverOptions = useMemo(
    () => mapOptions(drivers, (item) => item.driver),
    [drivers]
  );
  const truckOptions = useMemo(() => mapTruckOptions(trucks), [trucks]);
  const trailerOptions = useMemo(
    () => mapOptions(trailers, (item) => item.trailer),
    [trailers]
  );

  return {
    optionConfigs,
    routeOptions,
    driverOptions,
    truckOptions,
    trailerOptions,
    reloadAllCatalogs,
  };
}
