import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import OrderBackorderCheckbox from "./OrderBackorderCheckbox";

describe("OrderBackorderCheckbox", () => {
  it.each([
    [true, "B"],
    [false, "O"],
  ])("maps an outer checkbox state to its backorder value", (checked, value) => {
    const onChange = jest.fn();
    render(
      <OrderBackorderCheckbox
        checked={checked}
        isOuter
        value="ignored"
        onChange={onChange}
      />
    );

    fireEvent.click(screen.getByRole("checkbox"));

    expect(onChange).toHaveBeenCalledWith(value);
  });

  it("uses the supplied line value for an inner checkbox", () => {
    const onChange = jest.fn();
    render(
      <OrderBackorderCheckbox
        checked={false}
        isOuter={false}
        value="B"
        onChange={onChange}
      />
    );

    fireEvent.click(screen.getByRole("checkbox"));

    expect(onChange).toHaveBeenCalledWith("B");
  });
});
