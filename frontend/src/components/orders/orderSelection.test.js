import {
  getNextBackorderCheckedStates,
  getNextBackorderLineCheckedStates,
} from "./orderSelection";

describe("orderSelection", () => {
  it("selects parent order when any line is a backorder", () => {
    const rows = [
      { id: 1, orderNumber: "100", backOrder: "O" },
      { id: 2, orderNumber: "100", backOrder: "B" },
      { id: 3, orderNumber: "200", backOrder: "O" },
    ];

    expect(getNextBackorderCheckedStates(rows, { 200: true })).toEqual({
      100: true,
      200: false,
    });
  });

  it("selects individual backorder lines and clears open lines", () => {
    const rows = [
      { id: 1, orderNumber: "100", backOrder: "B" },
      { id: 2, orderNumber: "100", backOrder: "O" },
    ];

    expect(getNextBackorderLineCheckedStates(rows, { 2: true })).toEqual({
      1: true,
      2: false,
    });
  });
});
