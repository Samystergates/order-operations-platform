import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import OrderFlowDropdownItems from "./OrderFlowDropdownItems";

jest.mock("reactstrap", () => {
  const react = require("react");
  return {
    DropdownItem: ({ children, onClick }) =>
      react.createElement("button", { type: "button", onClick }, children),
  };
});

const flowForward = { current: "FWD" };
const flowHalt = { current: "HLT" };
const flowReverse = { current: "RVS" };

describe("OrderFlowDropdownItems", () => {
  it("shows and dispatches the start action for an authorized department", () => {
    const initToggleFlow = jest.fn();
    render(
      <OrderFlowDropdownItems
        dep="MWE"
        oId={10}
        orderKey="V100,1"
        user={{ departmentsSet: [{ depName: "Werkplaats" }] }}
        orderX={[{ id: 10, mwe: "R" }]}
        initToggleFlow={initToggleFlow}
        flowForward={flowForward}
        flowHalt={flowHalt}
        flowReverse={flowReverse}
      />
    );

    fireEvent.click(screen.getByText("Start MWE"));

    expect(initToggleFlow).toHaveBeenCalledWith(
      "V100,1",
      10,
      "MWE",
      "R",
      "FWD"
    );
  });

  it("does not show the start action to an unauthorized department", () => {
    render(
      <OrderFlowDropdownItems
        dep="MWE"
        oId={10}
        orderKey="V100,1"
        user={{ departmentsSet: [{ depName: "Smederij" }] }}
        orderX={[{ id: 10, mwe: "R" }]}
        initToggleFlow={jest.fn()}
        flowForward={flowForward}
        flowHalt={flowHalt}
        flowReverse={flowReverse}
      />
    );

    expect(screen.queryByText("Start MWE")).not.toBeInTheDocument();
  });
});
