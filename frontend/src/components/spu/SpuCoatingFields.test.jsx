import { fireEvent, render, screen } from "@testing-library/react";
import SpuCoatingFields from "./SpuCoatingFields";

describe("SpuCoatingFields", () => {
  it("disables natlakken-only fields when poedercoaten is selected", () => {
    render(
      <SpuCoatingFields
        selectedRadio="poedercoaten"
        checkedStates={{ Stralen: "NEE" }}
        onChange={jest.fn()}
      />
    );

    expect(screen.getByLabelText("Stralen")).toBeDisabled();
  });

  it("calls onChange for selected coating options", () => {
    const onChange = jest.fn();

    render(
      <SpuCoatingFields
        selectedRadio="natLakken"
        checkedStates={{ Stralen: "NEE" }}
        onChange={onChange}
      />
    );

    fireEvent.click(screen.getByLabelText("Stralen"));
    expect(onChange).toHaveBeenCalled();
  });
});
