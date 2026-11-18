import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import OrderUserFilterDropdown from "./OrderUserFilterDropdown";

jest.mock("reactstrap", () => {
  const react = require("react");
  const ToggleContext = react.createContext(() => {});

  return {
    Dropdown: ({ toggle, children }) =>
      react.createElement(
        ToggleContext.Provider,
        { value: toggle },
        react.createElement("div", null, children)
      ),
    DropdownItem: ({ children }) => react.createElement("div", null, children),
    DropdownMenu: ({ children }) => react.createElement("div", null, children),
    DropdownToggle: ({ tag: Tag, children }) => {
      const toggle = react.useContext(ToggleContext);
      return react.createElement(Tag || "button", { onClick: toggle }, children);
    },
  };
});

describe("OrderUserFilterDropdown", () => {
  const renderDropdown = (props) =>
    render(
      <table>
        <thead>
          <tr>
            <OrderUserFilterDropdown {...props} />
          </tr>
        </thead>
      </table>
    );

  it("initially selects all users when no current selection matches", () => {
    const onSelectedUsersChange = jest.fn();
    renderDropdown({
      uniqueUsers: ["Alice", "Bob"],
      selectedUsers: [],
      onSelectedUsersChange,
      onApply: jest.fn(),
    });

    expect(onSelectedUsersChange).toHaveBeenCalledWith(["Alice", "Bob"]);
  });

  it("applies the temporary user selection", () => {
    const onApply = jest.fn();
    renderDropdown({
      uniqueUsers: ["Alice", "Bob"],
      selectedUsers: ["Alice", "Bob"],
      onSelectedUsersChange: jest.fn(),
      onApply,
    });

    fireEvent.click(screen.getByText("Gebruiker (I)"));
    fireEvent.click(screen.getAllByRole("checkbox")[2]);
    fireEvent.click(screen.getByText("Apply"));

    expect(onApply).toHaveBeenCalledWith(["Alice"]);
  });

  it("allows clearing the selection and resetting it to all users", () => {
    const onApply = jest.fn();
    renderDropdown({
      uniqueUsers: ["Alice", "Bob"],
      selectedUsers: ["Alice", "Bob"],
      onSelectedUsersChange: jest.fn(),
      onApply,
    });

    fireEvent.click(screen.getByText("Gebruiker (I)"));
    fireEvent.click(screen.getAllByRole("checkbox")[0]);
    fireEvent.click(screen.getByText("Apply"));
    expect(onApply).toHaveBeenLastCalledWith([]);

    fireEvent.click(screen.getByText("Gebruiker (I)"));
    fireEvent.click(screen.getByText("Reset"));
    fireEvent.click(screen.getByText("Apply"));
    expect(onApply).toHaveBeenLastCalledWith(["Alice", "Bob"]);
  });
});
