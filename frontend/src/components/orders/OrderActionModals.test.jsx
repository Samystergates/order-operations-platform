import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import OrderActionModals from "./OrderActionModals";

jest.mock("reactstrap", () => {
  const React = require("react");
  const simple = (tag) => ({ children, onClick, value, color }) =>
    React.createElement(tag, { onClick, value, "data-color": color }, children);
  return {
    Button: simple("button"),
    Modal: ({ isOpen, children }) => isOpen ? React.createElement("div", null, children) : null,
    ModalHeader: simple("h2"),
    ModalBody: simple("div"),
    ModalFooter: simple("footer"),
  };
});

describe("OrderActionModals", () => {
  it("renders backorder, confirmation, and note actions", () => {
    const onToggleBackorder = jest.fn();
    const onSaveNote = jest.fn();
    render(
      <OrderActionModals
        backorderModal
        confirmationModal
        noteModal
        backorderOrderNumber="100"
        note=""
        onNoteChange={jest.fn()}
        onToggleBackorder={onToggleBackorder}
        onToggleConfirmation={jest.fn()}
        onToggleNote={jest.fn()}
        onSaveNote={onSaveNote}
      />
    );

    expect(screen.getByText(/Pas back-order status/)).toBeInTheDocument();
    expect(screen.getByText(/Start CRM Orders/)).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Enter your note here")).toBeInTheDocument();
    fireEvent.click(screen.getAllByText("Yes")[0]);
    fireEvent.click(screen.getByText("Save"));
    expect(onToggleBackorder).toHaveBeenCalledWith("yes");
    expect(onSaveNote).toHaveBeenCalled();
  });
});
