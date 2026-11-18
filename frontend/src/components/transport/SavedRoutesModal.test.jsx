import { fireEvent, render, screen } from "@testing-library/react";
import SavedRoutesModal from "./SavedRoutesModal";

describe("SavedRoutesModal", () => {
  it("renders saved routes and selects a route by order id", () => {
    const onSelectRoute = jest.fn();

    render(
      <SavedRoutesModal
        isOpen
        toggle={jest.fn()}
        routeOrders={[{ id: "Route A", order: { id: 42 } }]}
        selectedRouteRowId="Route A"
        onSelectRoute={onSelectRoute}
      />
    );

    fireEvent.click(screen.getByText("Route A"));

    expect(onSelectRoute).toHaveBeenCalledWith(42);
  });
});
