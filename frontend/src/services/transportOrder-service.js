import { privateAxios } from "./helper";

export const loadAllRoutes = () => {
  return privateAxios.get(`/transport/info/routes`).then((response) => response.data);
};

export const createRouteInfo = (routeDto) => {
  return privateAxios
    .post(`/transport/info/routes/create`, routeDto)
    .then((response) => response.data);
};

export const deleteRouteInfo = (routeId) => {
  return privateAxios
    .delete(`/transport/info/routes/delete/${routeId}`)
    .then((response) => response.data);
};

export const loadAllDrivers = () => {
  return privateAxios.get(`/transport/info/drivers`).then((response) => response.data);
};

export const createDriverInfo = (driverDto) => {
  return privateAxios
    .post(`/transport/info/drivers/create`, driverDto)
    .then((response) => response.data);
};

export const deleteDriverInfo = (driverId) => {
  return privateAxios
    .delete(`/transport/info/drivers/delete/${driverId}`)
    .then((response) => response.data);
};

export const loadAllTrucks = () => {
  return privateAxios.get(`/transport/info/trucks`).then((response) => response.data);
};

export const createTruckInfo = (truckDto) => {
  return privateAxios
    .post(`/transport/info/trucks/create`, truckDto)
    .then((response) => response.data);
};

export const deleteTruckInfo = (truckId) => {
  return privateAxios
    .delete(`/transport/info/trucks/delete/${truckId}`)
    .then((response) => response.data);
};

export const loadAllTrailers = () => {
  return privateAxios.get(`/transport/info/trailers`).then((response) => response.data);
};

export const createTrailerInfo = (trailerDto) => {
  return privateAxios
    .post(`/transport/info/trailers/create`, trailerDto)
    .then((response) => response.data);
};

export const deleteTrailerInfo = (trailerId) => {
  return privateAxios
    .delete(`/transport/info/trailers/delete/${trailerId}`)
    .then((response) => response.data);
};

export const loadAllTraOrders = () => {
  return privateAxios.get(`/transport/route/orders`).then((response) => response.data);
};

export const loadTra = (traId) => {
  return privateAxios.get(`/transport/route/orders/${traId}`).then((response) => response.data);
};

export const saveTra = (traDto) => {
  return privateAxios.post(`/transport/route/orders/save`, traDto).then((response) => response.data);
};

export const updateTra = (traDto) => {
  return privateAxios
    .put(`/transport/route/orders/update`, traDto)
    .then((response) => response.data);
};

export const updateTraColors = (traIds, entryId) => {
  return privateAxios
    .put(`/transport/route/orders/save/colors/${entryId}`, traIds)
    .then((response) => response.data);
};

export const deleteTra = (traId) => {
  return privateAxios
    .delete(`/transport/route/orders/delete/${traId}`)
    .then((response) => response.data);
};
