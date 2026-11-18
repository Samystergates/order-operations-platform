import React from "react";

const STATUS_CLASS_NAMES = {
  R: "red",
  G: "green",
  Y: "yellow",
  B: "blue",
};

const STATUS_PRIORITY = ["B", "R", "Y", "G"];

export function getGroupedOrderStatus(orders, orderNumber, dep) {
  const values = (orders || [])
    .filter((item) => item.orderNumber === orderNumber)
    .map((item) => item?.[dep]);

  return STATUS_PRIORITY.find((status) => values.includes(status)) || "";
}

export function getOrderLineStatus(orders, orderId, dep) {
  const order = (orders || []).find((item) => item.id === orderId);
  return order?.[dep] || "";
}

export default function OrderStatusDot({ status }) {
  const className = STATUS_CLASS_NAMES[status];

  if (!className) {
    return null;
  }

  return <div className={`dot ${className}`} />;
}
