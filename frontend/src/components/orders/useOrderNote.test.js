import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import useOrderNote from "./useOrderNote";

function OrderNoteHarness({ onSave }) {
  const { isOpen, note, setNote, toggle, save } = useOrderNote(onSave);

  return (
    <>
      <button onClick={() => toggle("100", "2")}>Open note</button>
      {isOpen && (
        <>
          <textarea
            aria-label="Note"
            value={note}
            onChange={(event) => setNote(event.target.value)}
          />
          <button onClick={save}>Save note</button>
          <button onClick={() => toggle()}>Close note</button>
        </>
      )}
    </>
  );
}

describe("useOrderNote", () => {
  it("saves the note for the selected order line and clears the draft", () => {
    const onSave = jest.fn();
    render(<OrderNoteHarness onSave={onSave} />);

    fireEvent.click(screen.getByText("Open note"));
    fireEvent.change(screen.getByLabelText("Note"), {
      target: { value: "Check dimensions" },
    });
    fireEvent.click(screen.getByText("Save note"));

    expect(onSave).toHaveBeenCalledWith("100", "2", "Check dimensions");
    expect(screen.queryByLabelText("Note")).not.toBeInTheDocument();

    fireEvent.click(screen.getByText("Open note"));
    expect(screen.getByLabelText("Note")).toHaveValue("");
  });

  it("keeps the draft when the modal is closed without saving", () => {
    render(<OrderNoteHarness onSave={jest.fn()} />);

    fireEvent.click(screen.getByText("Open note"));
    fireEvent.change(screen.getByLabelText("Note"), {
      target: { value: "Keep this draft" },
    });
    fireEvent.click(screen.getByText("Close note"));
    fireEvent.click(screen.getByText("Open note"));

    expect(screen.getByLabelText("Note")).toHaveValue("Keep this draft");
  });
});
