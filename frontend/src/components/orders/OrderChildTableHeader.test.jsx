import { render, screen } from "@testing-library/react";
import OrderChildTableHeader from "./OrderChildTableHeader";

describe("OrderChildTableHeader", () => {
  it("renders child-order specific columns", () => {
    render(
      <table>
        <OrderChildTableHeader />
      </table>
    );

    expect(screen.getByText("Regel")).toBeInTheDocument();
    expect(screen.getByText("Artikelnummer")).toBeInTheDocument();
  });
});
