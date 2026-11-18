import { render, screen } from "@testing-library/react";
import TransportRouteActions from "./TransportRouteActions";

const noop = () => {};

function renderActions(overrides = {}) {
  return render(
    <TransportRouteActions
      canManageTransport
      unloadedEntry={false}
      colorValue="R"
      onCustomOrder={noop}
      onSave={noop}
      onUpdate={noop}
      onDelete={noop}
      onExisting={noop}
      onStartRoute={noop}
      onFinishRoute={noop}
      {...overrides}
    />
  );
}

describe("TransportRouteActions", () => {
  it("shows only existing action when user cannot manage transport", () => {
    renderActions({ canManageTransport: false });

    expect(screen.getByText("Existing")).toBeInTheDocument();
    expect(screen.queryByText("Opslaan")).not.toBeInTheDocument();
    expect(screen.queryByText("Start Route")).not.toBeInTheDocument();
  });

  it("disables update and delete after route is completed", () => {
    renderActions({ colorValue: "G" });

    expect(screen.getByText("Wijzigen")).toBeDisabled();
    expect(screen.getByText("Verwijder")).toBeDisabled();
  });

  it("shows save action for unloaded entries", () => {
    renderActions({ unloadedEntry: true });

    expect(screen.getByText("Opslaan")).toBeInTheDocument();
    expect(screen.queryByText("Wijzigen")).not.toBeInTheDocument();
  });
});
