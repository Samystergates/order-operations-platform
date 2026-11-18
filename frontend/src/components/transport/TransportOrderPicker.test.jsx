import { fireEvent, render, screen } from "@testing-library/react";
import TransportOrderPicker from "./TransportOrderPicker";

describe("TransportOrderPicker", () => {
  it("renders available orders and triggers move actions", () => {
    const onMoveRight = jest.fn();

    render(
      <TransportOrderPicker
        filteredOrder={[{ id: 1, orderNumber: "SO-1", user: "User", postCode: "1000", city: "City", country: "NL", deliveryDate: "2026-06-18" }]}
        orderMeta={[]}
        tempOrders={[]}
        custOrders={[]}
        draggedIndex={null}
        setDraggedIndex={jest.fn()}
        setOrderMeta={jest.fn()}
        colorValue="N"
        isSelected={() => false}
        onRowClick={jest.fn()}
        onRowDoubleClick={jest.fn()}
        onMoveRight={onMoveRight}
        onMoveLeft={jest.fn()}
      />
    );

    expect(screen.getByText("SO-1")).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole("button")[0]);
    expect(onMoveRight).toHaveBeenCalled();
  });
});
