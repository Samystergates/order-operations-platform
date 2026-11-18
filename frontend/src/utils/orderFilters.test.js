import {
  applyOrderColorFilters,
  applyOrderSearchFilter,
  applyCombinedOrderFilters,
  paginateOrders,
  isOrderFilterActive,
} from "./orderFilters";

describe("orderFilters", () => {
  test("isOrderFilterActive evaluates selected states correctly", () => {
    const filters = { SME: ["G", "Y"], SPU: [] };

    expect(isOrderFilterActive(filters, "SME")).toBe(true);
    expect(isOrderFilterActive(filters, "SPU")).toBe(false);
  });

  test("applyOrderColorFilters keeps orders matching highest priority color", () => {
    const rows = [
      { orderNumber: "A1", sme: "Y" },
      { orderNumber: "A1", sme: "G" },
      { orderNumber: "B1", sme: "R" },
      { orderNumber: "C1", sme: "" },
    ];

    const filters = { SME: ["R"], SPU: [], MONLB: [], MONTR: [], MWE: [], SER: [], TRA: [], EXP: [] };
    const filtered = applyOrderColorFilters(rows, filters);
    const orderNumbers = [...new Set(filtered.map((row) => row.orderNumber))];

    expect(orderNumbers).toEqual(["B1"]);
  });

  test("applyOrderSearchFilter searches text fields and ignores null values", () => {
    const rows = [
      { customerName: "Usama", city: "Rotterdam", orderNumber: "100" },
      { customerName: null, city: null, orderNumber: null },
      { customerName: "Client", city: "Amsterdam", orderNumber: "200" },
    ];

    expect(applyOrderSearchFilter(rows, "amster").map((row) => row.orderNumber))
      .toEqual(["200"]);
    expect(applyOrderSearchFilter(rows, "")).toBe(rows);
  });

  test("applyCombinedOrderFilters combines search and user selection", () => {
    const rows = [
      { customerName: "Usama", city: "Rotterdam", orderNumber: "100", user: "A" },
      { customerName: "Usama", city: "Amsterdam", orderNumber: "200", user: "B" },
    ];

    expect(applyCombinedOrderFilters(rows, "amster", ["A", "B"]))
      .toEqual([rows[1]]);
    expect(applyCombinedOrderFilters(rows, "usama", ["A"]))
      .toEqual([rows[0]]);
  });

  test("paginateOrders returns the requested bounded page", () => {
    const rows = [{ id: 1 }, { id: 2 }, { id: 3 }];

    expect(paginateOrders(rows, 2, 2)).toEqual([{ id: 3 }]);
    expect(paginateOrders(rows, 0, 2)).toEqual([{ id: 1 }, { id: 2 }]);
  });
});
