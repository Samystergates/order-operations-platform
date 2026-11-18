import { privateAxios } from "./helper";

//get all orders

export const loadAllOrders = () => {
  return privateAxios.get(`/index/`)
    .then((response) => response.data);
};

export const loadCustomTraOrders = () => {
  return privateAxios.get(`/index/custom/`)
    .then((response) => response.data);
};

export const loadCRMOrders = () => {
  return privateAxios.post(`/index/refresh/orders`)
    .then((response) => response.data);
};

export const loadOrdersByUsers = (user) => {
  return privateAxios.get(`/index/search/${user}`)
    .then((response) => response.data);
};

export const loadArchivedOrders = () => {
  return privateAxios.get(`/archive/`)
    .then((response) => response.data);
};

export const updateOrders = (flowUpdate, orderDto) => {
  return privateAxios.put(`/index/update/${flowUpdate}`, orderDto)
    .then((response) => response.data);
};

export const updateOrdersColors = (orderNumber, orderDep, orderStatus, flowVal) => {
  return privateAxios.put(`/index/update/colors/${orderNumber}/${orderDep}/${flowVal}`, orderStatus)
    .then((response) => response.data);
};

export const createCustomOrder = (customOrderDto) => {
  return privateAxios.post('/index/create/custom', customOrderDto)
    .then((response) => response.data);
};

// export const deleteCustomOrderApi = (customOrderDto) => {
//   return privateAxios.delete('/index/delete/custom', customOrderDto)
//     .then((response) => response.data);
// };

export const deleteCustomOrderApi = (customOrderDto) => {
  return privateAxios.delete('/index/delete/custom', {
    data: customOrderDto,
  })
    .then((response) => response.data);
};


export const handleDownload = async () => {
  try {
    const response = await privateAxios.get('/index/export-orders', {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;

    link.setAttribute('download', 'orders.xlsx');

    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    console.error('Error downloading the file:', error);
  }
};
