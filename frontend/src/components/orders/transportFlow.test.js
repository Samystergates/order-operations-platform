import { updateTransportFlow } from "./transportFlow";

describe("updateTransportFlow", () => {
  it("updates the selected rows and records the previous status", () => {
    const rows = [
      {
        id: 1,
        tra: "R",
        departments: [
          { depName: "TRA", status: "R", prevStatus: "" },
          { depName: "EXP", status: "", prevStatus: "" },
        ],
      },
      {
        id: 2,
        tra: "",
        departments: [{ depName: "TRA", status: "", prevStatus: "" }],
      },
    ];

    const result = updateTransportFlow(rows, "1", "TRA", "R", "FWD");

    expect(result.orders[0].tra).toBe("Y");
    expect(result.orders[0].departments[0]).toEqual({
      depName: "TRA",
      status: "Y",
      prevStatus: "R",
    });
    expect(result.orders[1]).toBe(rows[1]);
    expect(result.allRequestedOrdersComplete).toBe(false);
  });

  it("reports completion only when every selected row is complete", () => {
    const rows = [
      {
        id: 1,
        departments: [
          { depName: "TRA", status: "Y", prevStatus: "R" },
          { depName: "EXP", status: "", prevStatus: "" },
        ],
      },
      {
        id: 2,
        departments: [{ depName: "TRA", status: "G", prevStatus: "Y" }],
      },
    ];

    const result = updateTransportFlow(rows, "1, 2", "TRA", "Y", "FWD");

    expect(result.allRequestedOrdersComplete).toBe(true);
    expect(result.orders[0].tra).toBe("G");
  });

  it("does not report completion when a requested row is missing", () => {
    const result = updateTransportFlow([], "99", "TRA", "R", "FWD");

    expect(result.allRequestedOrdersComplete).toBe(false);
  });
});
