import {
  deriveTransportColorValue,
  filterAvailableTransportOrders,
  getRouteMainOrderIds,
  resolveMainRouteOrders,
} from "./transportRouteLogic";

describe("transportRouteLogic", () => {
  it("extracts only main order ids from a route", () => {
    expect(
      getRouteMainOrderIds({
        orderIds: [
          { orderType: "MAIN", orderId: 10 },
          { orderType: "CUSTOM", orderId: 20 },
        ],
      })
    ).toEqual(["10"]);
  });

  it("resolves main route orders in route order", () => {
    const orders = [
      { id: 1, orderNumber: "A" },
      { id: 2, orderNumber: "B" },
    ];

    expect(
      resolveMainRouteOrders(
        { orderIds: [{ orderType: "MAIN", orderId: 2 }] },
        orders
      )
    ).toEqual([{ id: 2, orderNumber: "B" }]);
  });

  it("derives transport color only when selected orders agree", () => {
    expect(deriveTransportColorValue([])).toBe("N");
    expect(deriveTransportColorValue([{ tra: "R" }, { tra: "R" }])).toBe("R");
    expect(deriveTransportColorValue([{ tra: "R" }, { tra: "Y" }])).toBeNull();
  });

  it("filters completed and already-routed transport orders", () => {
    const orders = [
      { id: 1, tra: "R" },
      { id: 2, tra: "G" },
      { id: 3, tra: "" },
      { id: 4, tra: "Y" },
    ];
    const routeOrders = [
      {
        order: {
          orderIds: [{ orderId: 4 }],
        },
      },
    ];

    expect(filterAvailableTransportOrders(orders, routeOrders)).toEqual([
      { id: 1, tra: "R" },
    ]);
  });
});
