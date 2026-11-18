import { render, screen } from "@testing-library/react";
import OrderRow from "./OrderRow";

const baseOrder = {
  id: 1,
  orderNumber: "SO-100",
  regel: 1,
  orderType: "LAS",
  backOrder: "O",
  user: "Planner",
  organization: "Org",
  customerName: "Customer",
  postCode: "1000",
  city: "City",
  country: "NL",
  deliveryDate: "2026-06-18",
  referenceInfo: "Ref",
  creationDate: "2026-06-01",
  verifierUser: "Verifier",
};

function renderOrderRow(overrides = {}) {
  const renderDropdownItems = jest.fn((dep) => <span key={dep}>{dep}</span>);

  render(
    <table>
      <tbody>
        <OrderRow
          item={{ ...baseOrder, ...overrides }}
          isExpanded={false}
          rowMainDropDownOpen={{}}
          user={{ departmentsSet: [{ depName: "SALES" }] }}
          orderX={[{ ...baseOrder, ...overrides }]}
          tooltipOpen={{}}
          flowReverse={{ current: "RVS" }}
          handleColClickMain={jest.fn()}
          handleRowClick={jest.fn()}
          renderBackorderColumn={() => <span>Backorder</span>}
          getColorDot={() => <span>Status</span>}
          renderDropdownItems={renderDropdownItems}
          orderDepON={() => ({ exclamation: "NEE" })}
          toggleNoteModal={jest.fn()}
          toggleBOFlow={jest.fn()}
          toggleTooltip={jest.fn()}
        />
      </tbody>
    </table>
  );

  return { renderDropdownItems };
}

describe("OrderRow", () => {
  it("renders parent order cells", () => {
    renderOrderRow();

    expect(screen.getByText("SO-100")).toBeInTheDocument();
    expect(screen.getByText("Customer")).toBeInTheDocument();
    expect(screen.getByText("Backorder")).toBeInTheDocument();
  });

  it("delegates workflow menu items for the order type", () => {
    const { renderDropdownItems } = renderOrderRow();

    expect(renderDropdownItems).toHaveBeenCalledWith(
      "EXP",
      1,
      "SO-100",
      "SO-100,1",
      undefined
    );
  });
});
