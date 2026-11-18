import { useRef, useState } from "react";

export default function useOrderNote(onSave) {
  const [isOpen, setIsOpen] = useState(false);
  const [note, setNote] = useState("");
  const orderNumberRef = useRef("");
  const lineNumberRef = useRef("");

  const toggle = (orderNumber, lineNumber) => {
    orderNumberRef.current = orderNumber;
    lineNumberRef.current = lineNumber;
    setIsOpen((open) => !open);
  };

  const save = () => {
    onSave(orderNumberRef.current, lineNumberRef.current, note);
    setNote("");
    setIsOpen(false);
  };

  return { isOpen, note, setNote, toggle, save };
}
