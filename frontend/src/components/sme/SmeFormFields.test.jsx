import { fireEvent, render, screen } from "@testing-library/react";
import SmeFormFields from "./SmeFormFields";

describe("SmeFormFields", () => {
  it("updates dimension fields through setEntry", () => {
    const setEntry = jest.fn();

    render(
      <SmeFormFields
        entry={{ naafgat: "10" }}
        setEntry={setEntry}
        selectedTypeBoutgat=""
        onTypeBoutgatChange={jest.fn()}
      />
    );

    fireEvent.change(screen.getByLabelText(/Naafgat/i), {
      target: { value: "12" },
    });

    expect(setEntry).toHaveBeenCalledWith({ naafgat: "12" });
  });
});
