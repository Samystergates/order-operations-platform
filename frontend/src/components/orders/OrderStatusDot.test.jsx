import { render } from "@testing-library/react";
import OrderStatusDot, {
  getGroupedOrderStatus,
  getOrderLineStatus,
} from "./OrderStatusDot";

describe("OrderStatusDot", () => {
  it("uses the highest priority status for an order group", () => {
    const rows = [
      { id: 1, orderNumber: "100", sme: "G" },
      { id: 2, orderNumber: "100", sme: "R" },
      { id: 3, orderNumber: "100", sme: "B" },
    ];

    expect(getGroupedOrderStatus(rows, "100", "sme")).toBe("B");
  });

  it("reads line status by order id", () => {
    expect(getOrderLineStatus([{ id: 4, tra: "Y" }], 4, "tra")).toBe("Y");
  });

  it("renders a colored dot for known statuses", () => {
    const { container } = render(<OrderStatusDot status="R" />);

    expect(container.firstChild).toHaveClass("dot", "red");
  });
});
