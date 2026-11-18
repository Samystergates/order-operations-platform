export function getNextBackorderCheckedStates(orderRows, previousStates = {}) {
  const nextStates = { ...previousStates };

  const uniqueOrders = Object.values(
    (orderRows || []).reduce((acc, item) => {
      const key = item.orderNumber;
      if (!acc[key] || (item.backOrder === "B" && acc[key].backOrder !== "B")) {
        acc[key] = item;
      }

      return acc;
    }, {})
  );

  uniqueOrders.forEach((item) => {
    if (item.backOrder === "B" && !nextStates[item.orderNumber]) {
      nextStates[item.orderNumber] = true;
    }
    if (item.backOrder === "O" && nextStates[item.orderNumber]) {
      nextStates[item.orderNumber] = false;
    }
  });

  return nextStates;
}

export function getNextBackorderLineCheckedStates(orderRows, previousStates = {}) {
  const nextStates = { ...previousStates };

  (orderRows || []).forEach((item) => {
    if (item.backOrder === "B" && !nextStates[item.id]) {
      nextStates[item.id] = !nextStates[item.id];
    }
    if (item.backOrder === "O" && nextStates[item.id]) {
      nextStates[item.id] = !nextStates[item.id];
    }
  });

  return nextStates;
}
