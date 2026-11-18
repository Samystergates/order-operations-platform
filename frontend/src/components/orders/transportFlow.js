import { getNextFlowStatus } from "./orderFlowTransitions";

export function updateTransportFlow(orderRows, key, department, currentStatus, flowValue) {
  const orderIds = key.split(",").map((id) => parseInt(id.trim(), 10));
  const orders = orderRows.map((order) => {
    if (!orderIds.includes(order.id)) {
      return order;
    }

    const departments = order.departments.map((entry) => {
      if (entry.depName !== department) {
        return entry;
      }

      return {
        ...entry,
        prevStatus: currentStatus,
        status: getNextFlowStatus(currentStatus, flowValue, entry.prevStatus),
      };
    });

    return {
      ...order,
      tra: departments.find((entry) => entry.depName === department)?.status,
      departments,
    };
  });

  const allRequestedOrdersComplete = orderIds.every((orderId) => {
    const order = orders.find((item) => item.id === orderId);
    return order
      ? order.departments.every(
          (entry) => entry.status === "G" || entry.status === ""
        )
      : false;
  });

  return { orders, allRequestedOrdersComplete };
}
