import { render, screen } from "@testing-library/react";
import OrderModalSummary from "./OrderModalSummary";

describe("OrderModalSummary", () => {
  it("renders the common modal order details", () => {
    render(
      <OrderModalSummary
        order={{
          orderNumber: "SO-100",
          user: "Planner",
          customerName: "Demo Customer",
          deliveryDate: "2026-06-18",
          product: "WHEEL-1",
          aantal: 2,
          omsumin: "Wheel order",
        }}
      />
    );

    expect(screen.getByText("SO-100")).toBeInTheDocument();
    expect(screen.getByText("Demo Customer")).toBeInTheDocument();
    expect(screen.getByText("2.00")).toBeInTheDocument();
    expect(screen.getByText("Wheel order")).toBeInTheDocument();
  });

  it.each([undefined, null, "", "not-a-number"])(
    "shows a placeholder when quantity is missing or invalid (%s)",
    (aantal) => {
      render(<OrderModalSummary order={{ aantal }} />);

      expect(screen.getByText("—")).toBeInTheDocument();
    }
  );
});
