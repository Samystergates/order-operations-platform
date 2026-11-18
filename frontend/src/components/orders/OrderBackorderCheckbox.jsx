import React from "react";

function OrderBackorderCheckbox({ checked, value, isOuter, onChange }) {
  const nextValue = isOuter ? (checked ? "B" : "O") : value;

  return (
    <input
      type="checkbox"
      style={{ width: "24px", height: "24px" }}
      color="primary"
      checked={checked}
      onChange={() => onChange(nextValue)}
    />
  );
}

export default OrderBackorderCheckbox;
