export function getRouteMainOrderIds(routeOrder) {
  return (routeOrder?.orderIds || [])
    .filter((line) => line.orderType === "MAIN")
    .map((line) => String(line.orderId));
}

export function resolveMainRouteOrders(routeOrder, orders) {
  const selectedIds = getRouteMainOrderIds(routeOrder);

  return selectedIds
    .map((id) => (orders || []).find((order) => String(order?.id) === id))
    .filter(Boolean);
}

export function deriveTransportColorValue(orders) {
  if (!orders || orders.length === 0) {
    return "N";
  }

  if (orders.every((order) => order.tra === "R")) {
    return "R";
  }

  if (orders.every((order) => order.tra === "Y")) {
    return "Y";
  }

  if (orders.every((order) => order.tra === "G")) {
    return "G";
  }

  return null;
}

export function filterAvailableTransportOrders(orders, routeOrders) {
  const routedOrderIds = new Set(
    (routeOrders || [])
      .flatMap((value) => value?.order?.orderIds?.map((line) => String(line?.orderId)) || [])
  );

  return (orders || [])
    .filter((item) => item.tra !== null && item.tra !== "" && item.tra !== "G")
    .filter((order) => !routedOrderIds.has(String(order?.id)));
}
