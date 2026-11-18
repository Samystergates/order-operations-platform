import { sortOrderRows } from "./orderSorting";

describe("sortOrderRows", () => {
  it("sorts text fields and returns the next click direction", () => {
    const rows = [
      { orderNumber: "200" },
      { orderNumber: "100" },
    ];

    const ascending = sortOrderRows(rows, "Verkooporder", "Asc");
    const descending = sortOrderRows(rows, "Verkooporder", ascending.nextDirection);

    expect(ascending.rows.map((row) => row.orderNumber)).toEqual(["100", "200"]);
    expect(ascending.nextDirection).toBe("Dsc");
    expect(descending.rows.map((row) => row.orderNumber)).toEqual(["200", "100"]);
    expect(descending.nextDirection).toBe("Asc");
  });

  it("preserves order lines while sorting backorder groups", () => {
    const rows = [
      { orderNumber: "200", backOrder: "O", regel: "1" },
      { orderNumber: "100", backOrder: "B", regel: "2" },
      { orderNumber: "100", backOrder: "O", regel: "1" },
    ];

    const result = sortOrderRows(rows, "Backorder", "Asc");

    expect(result.rows.map((row) => `${row.orderNumber}-${row.backOrder}`)).toEqual([
      "100-B",
      "100-O",
      "200-O",
    ]);
  });

  it("sorts city rows in the requested direction without sorting twice", () => {
    const rows = [{ city: "Utrecht" }, { city: "Amsterdam" }];

    const result = sortOrderRows(rows, "Plaats", "Dsc");

    expect(result.rows.map((row) => row.city)).toEqual(["Utrecht", "Amsterdam"]);
    expect(result.nextDirection).toBe("Asc");
  });

  it("puts missing dates last when sorting ascending", () => {
    const rows = [
      { deliveryDate: null },
      { deliveryDate: "2025-01-01" },
      { deliveryDate: "2024-01-01" },
    ];

    const result = sortOrderRows(rows, "Leverdatum", "Asc");

    expect(result.rows.map((row) => row.deliveryDate)).toEqual([
      "2024-01-01",
      "2025-01-01",
      null,
    ]);
  });

  it("filters exception rows without changing sort direction", () => {
    const rows = [{ exclamation: "JA" }, { exclamation: "NEE" }];

    const result = sortOrderRows(rows, "EXC", "Dsc");

    expect(result.rows).toEqual([{ exclamation: "JA" }]);
    expect(result.nextDirection).toBe("Dsc");
  });
});
